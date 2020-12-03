package com.seeyon.cap4.form.manager.impl;

import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormViewBean;
import com.seeyon.cap4.form.manager.CheckFlowManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.trustdoDx.DxXrdWfAuthForFormDao;
import com.seeyon.cap4.form.util.PageUtil;
import com.seeyon.cap4.form.util.parse.FlowParser;
import com.seeyon.cap4.form.util.parse.Node;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.TypeReference;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.seeyon.cap4.form.util.Enums.ViewType.SeeyonForm;
import static com.seeyon.cap4.form.util.parse.FlowParser.EDIT_AUTH;
import static com.seeyon.cap4.form.util.parse.FlowParser.INNER_SEAL_TYPES;
import static com.seeyon.cap4.form.util.parse.FlowParser.OUTER_SEAL_TYPE;
import static com.seeyon.cap4.form.util.parse.FlowParser.SEAL_NODE;
import static com.seeyon.cap4.form.util.parse.FlowParser.SEAL_TYPE;
import static com.seeyon.cap4.form.util.parse.FlowParser.SIGN_NODE;
import static com.seeyon.cap4.form.util.parse.FlowParser.SIGN_TYPE;
import static com.seeyon.cap4.form.util.parse.FlowParser.SUPER_NODE;

/**
 *
 * create at 2019-06-22 12:12
 * </br>
 * @author fuqiang
 * @since v7.1sp
 * @see com.seeyon.cap4.form.manager.CheckFlowManager
 *
 */
public class CheckFlowManagerImpl implements CheckFlowManager {

    private Log log = CtpLogFactory.getLog(CheckFlowManagerImpl.class);

    private DxXrdWfAuthForFormDao dxXrdWfAuthForFormDao;

    public void setDxXrdWfAuthForFormDao(DxXrdWfAuthForFormDao dxXrdWfAuthForFormDao) {
        this.dxXrdWfAuthForFormDao = dxXrdWfAuthForFormDao;
    }

    private CAP4FormCacheManager cap4FormCacheManager;

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    @Override
    public String onSaveWorkFlow(String formAppId, String processXml, String processId) {
        log.info("流程检查前置校验,formAppId:" + formAppId + ",processId:" + processId);
        if (StringUtils.isBlank(formAppId)) {
            log.info("此流程为自由流程，直接提交流程，不做后续处理...");
            return getResult(ResultEnum.rs200);
        }
        LinkedHashMap<Double, List<Node>> nodeMap = FlowParser.parse(processXml);
        ResultEnum resultEnum = checkNodes(nodeMap);
        if (resultEnum != ResultEnum.rs200) {
            return getResult(resultEnum);
        }
        //检查节点是否含有信任签字或盖章
        int count = checkTrust(nodeMap);
        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formAppId));
        if (count == 0) {
            if (formBean != null) {
                ResultEnum re = checkTrustCtrls(formBean);
                if (re != ResultEnum.rs200) {
                    return getResult(re);
                }
            }
            log.info("未检测到信任签字或信任盖章节点，直接提交流程...");
            return getResult(ResultEnum.rs200);
        }
        if (formBean == null) {
            log.info("检测到信任签字和信任盖章节点，未保存表单...");
            return getResult(ResultEnum.rs205);
        }

        FormFileData formFileData = checkForm(formBean);
        List<NodeRelCtrl> nodeRelCtrls = new ArrayList<NodeRelCtrl>();
        ResultEnum resultEnum1 = checkFlow(formFileData, nodeMap, formBean, nodeRelCtrls);
        if (formFileData.getSignWay() == SignWay.form) {
            if(resultEnum1 == ResultEnum.rs200) {
                saveNodeRelCtrls(formAppId, processId, nodeRelCtrls);
            }
            if(!ResultEnum.isForce(resultEnum1)) {
                NodeRelCtrl nodeRelCtrl = new NodeRelCtrl(
                        ResultEnum.rs_999.getMsg(),
                        Integer.toString(ResultEnum.rs_999.getCode()),
                        0,
                        SignWay.form.getType());
                nodeRelCtrls = new ArrayList<NodeRelCtrl>();
                nodeRelCtrls.add(nodeRelCtrl);
                saveNodeRelCtrls(formAppId, processId, nodeRelCtrls);
                return getResult(resultEnum1, ResultEnum.rs1.getCode());
            }
            return getResult(resultEnum1);

        } else {
            //附件方式存储数据
            if(resultEnum1 == ResultEnum.rs200) {
                saveNodeRelCtrls(formAppId, processId, nodeRelCtrls);
            }
            return getResult(resultEnum1);
        }

    }

    /**
     * 保存节点和控件的id
     *
     * @param formAppId    表单appid
     * @param processId    流程id
     * @param nodeRelCtrls 节点和控件对应关系
     */
    private ResultEnum saveNodeRelCtrls(String formAppId,
                                        String processId,
                                        List<NodeRelCtrl> nodeRelCtrls) {
        if (CollectionUtils.isNotEmpty(nodeRelCtrls)) {
            List<Object> deleteParams = new ArrayList<Object>();
            deleteParams.add(formAppId);
            deleteParams.add(processId);
            int r = dxXrdWfAuthForFormDao.deleteWfAuth(deleteParams);
            if (r == -1) {
                return ResultEnum.rs_1;
            }
            for (NodeRelCtrl nodeRelCtrl : nodeRelCtrls) {
                List<Object> saveParams = new ArrayList<Object>();
                saveParams.add(UUIDLong.longUUID());
                saveParams.add(formAppId);
                saveParams.add(processId);
                saveParams.add(nodeRelCtrl.getNodeId());
                saveParams.add(nodeRelCtrl.getCtrlId());
                saveParams.add(nodeRelCtrl.getSignLayout());
                saveParams.add(nodeRelCtrl.getOrderno());
                r = dxXrdWfAuthForFormDao.saveWfAuth(saveParams);
                if (r == -1) {
                    return ResultEnum.rs_1;
                }
            }
        }
        return ResultEnum.rs200;
    }


    /**
     * 表单签署：超级节点不能在第一位，
     * 超级节点后，不能在有盖章和签字节点；
     *
     * @param nodeMap
     * @return
     */
    private ResultEnum checkPreAndAfterNode(LinkedHashMap<Double, List<Node>> nodeMap) {
        int preCount = 0, afterCount = 0;
        boolean superNodeFlag = false;
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> entry = iterator.next();
            List<Node> nodes = entry.getValue();
            if (nodes.size() == 1) {
                if (!superNodeFlag) {
                    if (SEAL_NODE.equals(nodes.get(0).getIsXrdNode())
                            || SIGN_NODE.equals(nodes.get(0).getIsXrdNode())) {
                        preCount++;
                    }
                    if (SUPER_NODE.equals(nodes.get(0).getIsXrdNode())) {
                        superNodeFlag = true;
                    }
                } else {
                    if (SEAL_NODE.equals(nodes.get(0).getIsXrdNode())
                            || SIGN_NODE.equals(nodes.get(0).getIsXrdNode())) {
                        afterCount++;
                    }
                }
            } else {
                for (Node node : nodes) {
                    if (!superNodeFlag) {
                        if (SEAL_NODE.equals(node.getIsXrdNode())
                                || SIGN_NODE.equals(node.getIsXrdNode())) {
                            preCount++;
                        }
                        if (SUPER_NODE.equals(node.getIsXrdNode())) {
                            superNodeFlag = true;
                        }
                    } else {
                        if (SEAL_NODE.equals(node.getIsXrdNode())
                                || SIGN_NODE.equals(node.getIsXrdNode())) {
                            afterCount++;
                        }
                    }
                }
            }
        }
        if (superNodeFlag) {
            if (afterCount != 0) {
                log.error("表单签署流程不支持有信任签字或信任盖章节点...");
                return ResultEnum.rs213;
            }
        }
        return ResultEnum.rs200;
    }

    /**
     * 检查签署是否有并列节点
     *
     * @param nodeMap
     * @return
     */
    private ResultEnum checkNodes(LinkedHashMap<Double, List<Node>> nodeMap) {
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        //连续序列map,
        List<Integer> series = new ArrayList<Integer>();
        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> entry = iterator.next();
            List<Node> nodes = entry.getValue();
            if (nodes.size() > 1) {
                int hasNode = 0;
                for (Node node : nodes) {
                    if (SEAL_NODE.equals(node.getIsXrdNode())) {
                        hasNode++;
                    }
                    if (SIGN_NODE.equals(node.getIsXrdNode())) {
                        hasNode++;
                    }
                    if (SUPER_NODE.equals(node.getIsXrdNode())) {
                        hasNode++;
                    }
                }
                //如果hasSuperNode和hasSealOrSign同时有值，说明为并行，不支持
                if (hasNode > 1) {
                    log.error("暂不支持外部签署节点、信任盖章、信任签字不允许合并处理...");
                    return ResultEnum.rs211;
                }
                //存储此处串行数据
                series.add(hasNode);
            } else {
                series.add(null);
            }
        }
        Integer count = 0;
        for(int i =0; i < series.size(); i++) {
            count = series.get(i);
            if(count == null) {
                count = 0;
                continue;
            } else {
                if(i < series.size() - 1) {
                    Integer nextCount = series.get(i + 1);
                    if(nextCount == null) {
                        count = 0;
                        continue;
                    }
                    count += nextCount;
                } else {
                    count += series.get(i) == null ? 0 : series.get(i);
                }

            }
        }
        if(count > 1) {
            log.error("暂不支持外部签署节点、信任盖章、信任签字不允许合并处理...");
            return ResultEnum.rs211;
        }
        return ResultEnum.rs200;
    }

    /**
     * 根据盖章，签字，超级节点数量判断是否为并列节点.
     *
     * @param hasSeal      盖章数量
     * @param hasSign      签字数量
     * @param hasSuperNode 超级节点数量
     * @return
     */
    private boolean _check(int hasSeal, int hasSign, int hasSuperNode) {
        if ((hasSeal == 1 && hasSign == 0 && hasSuperNode == 0)
                || (hasSeal == 0 && hasSign == 1 && hasSuperNode == 0)
                || (hasSeal == 0 && hasSign == 0 && hasSuperNode == 1)
                || (hasSeal == 0 && hasSign == 0 && hasSuperNode == 0)) {
            return true;
        }
        return false;
    }

    /**
     * 检测表单中是否存在，控件
     *
     * @param formBean
     * @return
     */
    private ResultEnum checkTrustCtrls(FormBean formBean) {
        List<FormFieldBean> customFieldBeans = formBean.getCustomFields();
        if (CollectionUtils.isEmpty(customFieldBeans)) {
            return ResultEnum.rs200;
        }
        int sealCount = 0, signCount = 0;
        for (FormFieldBean formFieldBean : customFieldBeans) {
            if (SEAL_TYPE.equals(formFieldBean.getInputType())) {
                sealCount++;
            } else if (SIGN_TYPE.equals(formFieldBean.getInputType())) {
                signCount++;
            }
        }
        if (sealCount != 0) {
            return ResultEnum.rs203;
        }
        if (signCount != 0) {
            return ResultEnum.rs201;
        }
        return ResultEnum.rs200;
    }

    /**
     * 检测是否有信任度节点
     *
     * @param nodeMap
     * @return
     */
    private int checkTrust(LinkedHashMap<Double, List<Node>> nodeMap) {
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> entry = iterator.next();
            List<Node> nodes = entry.getValue();
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                if (SEAL_NODE.equals(node.getIsXrdNode())
                        || SIGN_NODE.equals(node.getIsXrdNode())
                        || SUPER_NODE.equals(node.getIsXrdNode())) {
                    count++;
                }
            }
        }
        return count;
    }


    /**
     * 检查表单中是否存在签署控件：返回参见签署方式枚举
     *
     * @param formBean 表单缓存信息
     * @return
     */
    private FormFileData checkForm(FormBean formBean) {
        log.info("检查表单中是否存在签署控件...");

        List<FormFieldBean> customFieldBeans = formBean.getCustomFields();
        if (CollectionUtils.isEmpty(customFieldBeans)) {
            return new FormFileData(SignWay.att, null);
        }
        List<FormField> formFields = new ArrayList<FormField>();
        for (FormFieldBean formFieldBean : customFieldBeans) {
            if (SEAL_TYPE.equals(formFieldBean.getInputType())
                    || SIGN_TYPE.equals(formFieldBean.getInputType())) {
                FormField formField = new FormField();
                formField.setFieldId(formFieldBean.getName());
                formField.setInputType(formFieldBean.getInputType());
                List<FormViewBean> allPCViews = formBean.getFormViewList(SeeyonForm);
                if (CollectionUtils.isNotEmpty(allPCViews)) {
                    formField.setFormViewId(allPCViews.get(0).getFormBeanId());
                }
                if (StringUtils.isNotBlank(formFieldBean.getCustomParam())) {
                    formField.setSignetSelect(formFieldBean.getCustomParam());
                }
                formField.setFormViewType(SeeyonForm.getText());
                formFields.add(formField);
            }
        }
        if (CollectionUtils.isNotEmpty(formFields)) {
            return new FormFileData(SignWay.form, formFields);
        }
        return new FormFileData(SignWay.att, null);
    }

    /**
     * 校验流程：包含表单校验和附件校验
     *
     * @param formFileData
     * @param nodeMap      节点map
     * @param formBean     表单bean
     * @param nodeRelCtrls 节点关联有权限的控件id
     * @return
     */
    private ResultEnum checkFlow(FormFileData formFileData,
                                 LinkedHashMap<Double, List<Node>> nodeMap,
                                 FormBean formBean,
                                 List<NodeRelCtrl> nodeRelCtrls) {

        if (formFileData.getSignWay() == SignWay.form) {
            //校验节点的顺序问题
            ResultEnum resultEnum = checkPreAndAfterNode(nodeMap);
            if (resultEnum != ResultEnum.rs200) {
                return resultEnum;
            }
            //表单签署检验
            return checkFormFlow(formBean,
                    formFileData.getFormFields(),
                    nodeMap, nodeRelCtrls);
        } else if (formFileData.getSignWay() == SignWay.att) {
            //附件校验
            return checkAtt(nodeMap, nodeRelCtrls);
        }
        return ResultEnum.rs200;
    }

    /**
     * 检查附件签署
     *
     * @param nodeMap
     * @param nodeRelCtrls 节点相关，ctrlId存储的是签字或盖章，1-盖章；2-签字；3-超级节点（外部签署）
     * @return
     */
    private ResultEnum checkAtt(LinkedHashMap<Double, List<Node>> nodeMap, List<NodeRelCtrl> nodeRelCtrls) {
        int signNodeCount = 0; //签字节点个数
        int superNodeCount = 0; //超级节点个数
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> entry = iterator.next();
            List<Node> nodes = entry.getValue();
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                if (SIGN_NODE.equals(node.getIsXrdNode())) {
                    signNodeCount++;
                    nodeRelCtrls.add(new NodeRelCtrl(SIGN_NODE, node.getNodeId(), index, 2));
                } else if (SUPER_NODE.equals(node.getIsXrdNode())) {
                    superNodeCount++;
                    nodeRelCtrls.add(new NodeRelCtrl(SUPER_NODE, node.getNodeId(), index, 2));
                } else if (SEAL_NODE.equals(node.getIsXrdNode())) {
                    nodeRelCtrls.add(new NodeRelCtrl(SEAL_NODE, node.getNodeId(), index, 2));
                }
            }
            index++;
        }
        if (signNodeCount > 1) {
            log.error("仅支持1个签字节点...");
            return ResultEnum.rs208;
        }
        if (superNodeCount > 1) {
            log.error("仅支持1个外部节点...");
            return ResultEnum.rs209;
        }
        return ResultEnum.rs200;
    }

    /**
     * 检查表单流程
     *
     * @param formBean
     * @param formFields
     * @param nodeMap
     * @return
     */
    private ResultEnum checkFormFlow(FormBean formBean,
                                     List<FormField> formFields,
                                     LinkedHashMap<Double, List<Node>> nodeMap,
                                     List<NodeRelCtrl> nodeRelCtrls) {
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        List<FormField> copyFormFields = new ArrayList<FormField>();
        copyFormFields.addAll(formFields);
        //用于检测是否所有的控件，均被授权
        //List<FormField> notAuthFormFields = new ArrayList<FormField>();
        //notAuthFormFields.addAll(formFields);
        //用于判断多个节点对一个控件有编辑权限
        //一个节点匹配多个控件
        Map<String, List<FormField>> nodeMatchFields = new HashMap<String, List<FormField>>();
        Map<String, Node> nodesMap = new HashMap<String, Node>();
        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> entry = iterator.next();
            List<Node> nodes = entry.getValue();
            for (Node node : nodes) {
                ResultEnum checkNodeCode = checkNode(formBean, node, copyFormFields, nodeMatchFields, nodesMap);
                if(checkNodeCode != ResultEnum.rs200) {
                    return checkNodeCode;
                }
                node.setOrderno(index);
            }
            index++;
        }

        ResultEnum code = validNodesMatcher(nodeMatchFields, nodesMap);
        setNodeRelCtrl(nodeMatchFields, nodeRelCtrls, nodesMap);
        if(code != ResultEnum.rs200) {
            return code;
        }
        return ResultEnum.rs200;
    }

    /**
     * 校验某一节点对控件的编辑权限
     * @param nodeMatchFields      节点所绑定的控件
     * @param nodeMap              所有节点map
     * @return
     */
    private ResultEnum validNodesMatcher(Map<String, List<FormField>> nodeMatchFields,
                                         Map<String, Node> nodeMap) {
        int allFieldsCount = 0;
        Map<String, Integer> fieldsCount = new HashMap<String, Integer>();
        Set<String> keys = nodeMatchFields.keySet();
        for (String key : keys) {
            List<FormField> formFields = nodeMatchFields.get(key);
            if(CollectionUtils.isNotEmpty(formFields)) {
                allFieldsCount++;
            }
            Set<String> superSet = new HashSet<String>();
            for(FormField formField : formFields) {
                //校验每个节点是否对应相应的控件
                Node node = nodeMap.get(formField.getActivityId());
                if(SEAL_NODE.equals(node.getIsXrdNode())) {
                    if(SIGN_TYPE.equals(formField.getInputType())) {
                        log.error("信任盖章节点不能绑定信任签字控件...");
                        return ResultEnum.rs219;
                    }
                    if(!validInner(formField.getSignetSelect())) {
                        log.error("信任盖章节点绑定控件类型不匹配...");
                        return ResultEnum.rs218;
                    }
                }
                if(SIGN_NODE.equals(node.getIsXrdNode())) {
                    if(SEAL_TYPE.equals(formField.getInputType())) {
                        log.error("信任签字节点不能绑定信任盖章控件...");
                        return ResultEnum.rs220;
                    }
                }
                if(SUPER_NODE.equals(node.getIsXrdNode())) {
                    if(SEAL_TYPE.equals(formField.getInputType())
                            && !validOuter(formField.getSignetSelect())) {
                        log.error("外部签署节点绑定的信任盖章控件类型不匹配...");
                        return ResultEnum.rs221;
                    }
                    superSet.add(formField.getInputType());
                }

                //首先需把各个控件所绑定的数量放进去
                Integer count = fieldsCount.get(formField.getFieldId());
                if(count == null) {
                    fieldsCount.put(formField.getFieldId(), 1);
                } else {
                    fieldsCount.put(formField.getFieldId(), count + 1);
                }
            }
            if(superSet.size() > 1) {
                log.error("外部签署节点不能同时配置信任签字控件和信任盖章控件...");
                return ResultEnum.rs222;
            }
        }
        if(allFieldsCount == 0) {
            log.error("盖章节点/签字节点/外部签署未绑定任何控件...");
            return ResultEnum.rs216;
        }
        Set<String> fieldKeys = fieldsCount.keySet();
        for(String fieldKey : fieldKeys) {
            if(fieldsCount.get(fieldKey) != null
                    && fieldsCount.get(fieldKey) > 1) {
                log.error("对于某一个控件，有多个信任签字/信任盖章/外部签署对其有编辑权限，请检查...");
                return ResultEnum.rs217;
            }
        }
        return ResultEnum.rs200;
    }

    /**
     * 对表单控件及节点权限赋值
     * @param nodeMatchFields
     * @param nodeRelCtrls
     */
    private void setNodeRelCtrl(Map<String, List<FormField>> nodeMatchFields,
                                List<NodeRelCtrl> nodeRelCtrls,
                                Map<String, Node> nodeMap) {
        Set<String> keys = nodeMatchFields.keySet();
        for(String key : keys) {
            List<FormField> formFields = nodeMatchFields.get(key);
            for(FormField formField : formFields) {
                nodeRelCtrls.add(new NodeRelCtrl(formField.getFieldId(),
                        formField.getActivityId(),
                        nodeMap.get(formField.getActivityId()).getOrderno(),
                        1));
            }
        }
    }

    /**
     * 返回结果
     * @param resultEnum 结果枚举
     * @return
     */
    private String getResult(ResultEnum resultEnum) {
        return ResultEnum.getRsJson(resultEnum);
    }

    /**
     * 返回带data的结果集
     * @param resultEnum
     * @param data
     * @return
     */
    private String getResult(ResultEnum resultEnum, Object data) {
        return PageUtil.getResult(Integer.toString(resultEnum.getCode()), resultEnum.getMsg(), data);
    }

    /**
     * 用于判断多个节点对一个控件有编辑权限
     *
     * @param formBean
     * @param node
     * @param copyFormFields
     * @param nodeMatchFields  节点匹配多个控件,map类型，value = 控件实体FormField
     * @return
     */
    private ResultEnum checkNode(FormBean formBean,
                                 Node node,
                                 List<FormField> copyFormFields,
                                 Map<String, List<FormField>> nodeMatchFields,
                                 Map<String, Node> nodeMap) {
        if (SEAL_NODE.equals(node.getIsXrdNode())
                || SIGN_NODE.equals(node.getIsXrdNode())
                || SUPER_NODE.equals(node.getIsXrdNode())) {
            nodeMap.put(node.getNodeId(), node);
            String fv = node.getFv();
            if (StringUtils.isBlank(fv)) {
                log.error("表单签署流程需要您先保存权限...");
                return ResultEnum.rs215;
            }
            String[] allViews = fv.split("_");
            String[] firstPCView = allViews[0].split("\\.");
            FormAuthViewBean authViewBean = formBean.getAuthViewBeanById(Long.parseLong(firstPCView[1]));
            if (authViewBean == null) {
                log.error("表单签署流程需要您先保存权限...");
                return ResultEnum.rs215;
            }
            Map<String, FormAuthViewFieldBean> fields = authViewBean.getFields();
            for (FormField formField : copyFormFields) {
                FormAuthViewFieldBean authViewFieldBean = fields.get(formField.getFieldId());
                if (authViewFieldBean != null) {
                    if (EDIT_AUTH.equals(authViewFieldBean.getAccess())
                            && authViewFieldBean.getFormFieldBean() != null
                            && (SEAL_TYPE.equals(authViewFieldBean.getFormFieldBean().getInputType())
                            || SIGN_TYPE.equals(authViewFieldBean.getFormFieldBean().getInputType()))) {
                        List<FormField> formFields = nodeMatchFields.get(node.getNodeId());
                        if(CollectionUtils.isEmpty(formFields)) {
                            formFields = new ArrayList<FormField>();
                            nodeMatchFields.put(node.getNodeId(), formFields);
                        }
                        nodeMatchFields.get(node.getNodeId()).
                                add(new FormField(node.getNodeId(),
                                        formField.getFieldId(),
                                        authViewFieldBean.getFormFieldBean() != null ?
                                                authViewFieldBean.getFormFieldBean().getInputType() : null,
                                        EDIT_AUTH,
                                        getSignetSelect(authViewFieldBean.getFormFieldBean().getCustomParam())
                                ));

                    }
                }
            }
        }
        return ResultEnum.rs200;
    }

    /**
     * 校验是否符合内部公章
     *
     * @param signetSelect 已选公章
     * @return
     */
    private boolean validInner(String signetSelect) {
        for (String s : INNER_SEAL_TYPES) {
            if (s.equals(signetSelect)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验是否符合外部公章
     *
     * @param signetSelect 已选公章
     * @return
     */
    private boolean validOuter(String signetSelect) {
        return OUTER_SEAL_TYPE.equals(signetSelect);
    }

    /**
     * 解析自定义控件中自定义params
     * @param input
     * @return
     */
    private String getSignetSelect(String input) {
        try {
            Type mapType = new TypeReference<Map<String, Object>>() {
            }.getType();
            Map<String, Object> rsMap = JSONUtil.parseJSONString(input, mapType);
            Map<String, Object> mappingMap = JSONUtil.parseJSONString(rsMap.get("mapping").toString(), mapType);
            return mappingMap.get("signetSelect").toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 签署方式枚举
     */
    public enum SignWay {
        //表单签署
        form(1),
        //附件
        att(2),
        //书签
        mark(3),
        //表单word正文
        word(4),
        //无签署
        none(0);

        private int type;

        SignWay(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * 响应结果DTO
     */
    private class ResultDTO {

        private String formAppId;
        private String processId;
        private List<NodeRelCtrl> nodeRelCtrls;

        public ResultDTO(String formAppId, String processId, List<NodeRelCtrl> nodeRelCtrls) {
            this.formAppId = formAppId;
            this.processId = processId;
            this.nodeRelCtrls = nodeRelCtrls;
        }

        public String getFormAppId() {
            return formAppId;
        }

        public void setFormAppId(String formAppId) {
            this.formAppId = formAppId;
        }

        public String getProcessId() {
            return processId;
        }

        public void setProcessId(String processId) {
            this.processId = processId;
        }

        public List<NodeRelCtrl> getNodeRelCtrls() {
            return nodeRelCtrls;
        }

        public void setNodeRelCtrls(List<NodeRelCtrl> nodeRelCtrls) {
            this.nodeRelCtrls = nodeRelCtrls;
        }
    }
    /**
     * 返回结果枚举
     */
    private enum ResultEnum {
        /**
         * 不强制
         */
        rs201(201, "存在信任度签字控件，但不存在签署节点，请检查。"),
        /**
         * 不强制
         */
        rs203(203, "存在信任度盖章控件，但不存在签署节点，请检查。"),
        /**
         * 强制
         */
        rs205(205, "本流程因有签署节点,请先保存表单，然后在进行流程编辑和保存。"),
        /**
         * 强制
         */
        rs208(208, "附件签署流程不支持多个信任签字节点,请检查。"),
        /**
         * 强制
         */
        rs209(209, "附件签署流程不支持多个外部签署节点,请检查。"),
        /**
         * 强制
         */
        rs_1(-1, "存储流程权限数据异常"),
        /**
         * 强制
         */
        rs211(211, "暂不支持签署节点的并发执行,请检查。"),

        rs212(212, "表单签署超级节点必须在签字节点或盖章节点之后,请检查。"),
        /**
         * 强制
         */
        rs213(213, "表单签署流程外部签署节点后不支持有信任签字或信任盖章节点,请检查。"),
        /**
         * 强制
         */
        rs215(215, "表单签署流程需要您先保存权限。"),
        /**
         * 不强制
         */
        rs216(216, "当前为表单签署流程，您的签署节点没有对任一签字控件或者盖章控件具有编辑权限，会造成签署不成功，请检查。"),
        /**
         * 不强制
         */
        rs217(217, "存在信任度签字/盖章控件，有多个签署节点对其有编辑权限，会造成签署不成功，请检查。"),
        /**
         * 不强制
         */
        rs218(218, "内部的信任盖章节点与设置的信任度盖章控件的外部公章类型不匹配，请检查。"),
        /**
         * 强制
         */
        rs219(219, "信任盖章节点的表单编辑操作权限给对应到了信任度签字控件上，请检查。"),
        /**
         * 强制
         */
        rs220(220, "信任签字节点的表单编辑操作权限给对应到了信任度盖章控件上，请检查。"),
        /**
         * 不强制
         */
        rs221(221, "外部签署节点与设置的信任度盖章控件的内部公章类型不匹配，会造成签署不成功，请检查。"),
        /**
         * 强制
         */
        rs222(222, "外部签署节点不能同时对信任度盖章和信任度签字控件有编辑权限，请检查。"),
        /**
         * 必过
         */
        rs200(200, "成功"),
        /**
         * 当不强制，并且有问题时，则存储一条记录，并且绑定id为error
         */
        rs_999(-999, "error"),

        rs1(1, "不强制节点");
        private int code;
        private String msg;

        ResultEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        /**
         * 获取返回结果集json
         * @param resultEnum    结果枚举
         * @return
         */
        public static String getRsJson(ResultEnum resultEnum) {
            return PageUtil.getResult(Integer.toString(resultEnum.getCode()), resultEnum.getMsg());
        }

        /**
         * 获取是否强制
         * @param resultEnum 返回枚举
         * @return true-强制；false-不强制，则需要保存数据
         */
        public static boolean isForce(ResultEnum resultEnum) {
            List<ResultEnum> unForceList = new ArrayList<ResultEnum>();
            unForceList.add(ResultEnum.rs216);
            unForceList.add(ResultEnum.rs217);
            unForceList.add(ResultEnum.rs218);
            unForceList.add(ResultEnum.rs221);
            return !unForceList.contains(resultEnum);
        }
    }


    /**
     * 表单控件外层实体
     */
    public class FormFileData {
        private SignWay signWay;
        private List<FormField> formFields;

        public FormFileData(SignWay signWay, List<FormField> formFields) {
            this.signWay = signWay;
            this.formFields = formFields;
        }

        public SignWay getSignWay() {
            return signWay;
        }

        public void setSignWay(SignWay signWay) {
            this.signWay = signWay;
        }

        public List<FormField> getFormFields() {
            return formFields;
        }

        public void setFormFields(List<FormField> formFields) {
            this.formFields = formFields;
        }
    }

    /**
     * 表单控件实体
     */
    private class FormField {
        //控件id
        private String fieldId;
        //视图id
        private long formViewId;
        //视图类型：phone-手机视图；seeyonform-pc视图
        private String formViewType;
        //节点id
        private String activityId;
        //信任签字，信任盖章类型
        private String inputType;
        //权限:edit,browser
        private String auth;
        //印章类型
        private String signetSelect;

        public FormField(){}
        public FormField(String activityId, String fieldId, String inputType, String auth, String signetSelect) {
            this.activityId = activityId;
            this.fieldId = fieldId;
            this.inputType = inputType;
            this.auth = auth;
            this.signetSelect = signetSelect;
        }

        public String getSignetSelect() {
            return signetSelect;
        }

        public void setSignetSelect(String signetSelect) {
            this.signetSelect = signetSelect;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        public String getInputType() {
            return inputType;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public String getFieldId() {
            return fieldId;
        }

        public void setFieldId(String fieldId) {
            this.fieldId = fieldId;
        }

        public long getFormViewId() {
            return formViewId;
        }

        public void setFormViewId(long formViewId) {
            this.formViewId = formViewId;
        }

        public String getFormViewType() {
            return formViewType;
        }

        public void setFormViewType(String formViewType) {
            this.formViewType = formViewType;
        }

        public String getActivityId() {
            return activityId;
        }

        public void setActivityId(String activityId) {
            this.activityId = activityId;
        }
    }

    private class NodeRelCtrl {
        private String nodeId;
        private String ctrlId;
        private int orderno;
        private int signLayout; //签署形式：1-表单；2-附件；3-书签；4-表单word正文；

        public NodeRelCtrl(String ctrlId,
                           String nodeId,
                           int orderno,
                           int signLayout) {
            this.ctrlId = ctrlId;
            this.nodeId = nodeId;
            this.orderno = orderno;
            this.signLayout = signLayout;
        }

        public int getSignLayout() {
            return signLayout;
        }

        public void setSignLayout(int signLayout) {
            this.signLayout = signLayout;
        }

        public int getOrderno() {
            return orderno;
        }

        public void setOrderno(int orderno) {
            this.orderno = orderno;
        }

        public String getCtrlId() {
            return ctrlId;
        }

        public void setCtrlId(String ctrlId) {
            this.ctrlId = ctrlId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }
    }
}
