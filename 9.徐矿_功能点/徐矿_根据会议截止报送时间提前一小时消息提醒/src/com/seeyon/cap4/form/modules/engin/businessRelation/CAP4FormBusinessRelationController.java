package com.seeyon.cap4.form.modules.engin.businessRelation;

import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.bean.fieldCtrl.*;
import com.seeyon.cap4.form.modules.engin.relation.FormRelationEnums.ViewSelectType;
import com.seeyon.cap4.form.modules.engin.trigger.FormTriggerUtil;
import com.seeyon.cap4.form.modules.engin.trigger.design.FormTriggerBaseDesignManager;
import com.seeyon.cap4.form.modules.formlist.CAP4FormListManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormDesignUtil;
import com.seeyon.cap4.form.vo.FormTriggerDataRightVo;
import com.seeyon.cap4.magic.constants.MagicPrivateConstants;
import com.seeyon.cap4.magic.dao.IMagicInterfaceDefineDao;
import com.seeyon.cap4.magic.po.MagicInterfaceDefineEntity;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.worktimeset.domain.WorkTimeCurrency;
import com.seeyon.v3x.worktimeset.manager.WorkTimeSetManager;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 业务关系设置的控制器 Created by wangh on 2017/9/29.
 */
@CheckRoleAccess(roleTypes = {OrgConstants.Role_NAME.BusinessDesigner})
public class CAP4FormBusinessRelationController extends BaseController {
    private CAP4FormCacheManager cap4FormCacheManager;
    private CAP4FormManager cap4FormManager;
    private CAP4FormBusinessRelationManager cap4FormBusinessRelationManager;
    private IMagicInterfaceDefineDao magicInterfaceDefineDao;
    private CAP4FormListManager cap4FormListManager;

    private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormBusinessRelationController.class);

    /**
     * 关联设置编辑、查看
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView editRelation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("cap4/form/businessRelation/cap4FormRelation");
        // 调用表单
        Long formId = ReqUtil.getLong(request, "formId", 0L);
        // 关联底表(可以是流程表，可以是无流程)
        Long targetFormId = ReqUtil.getLong(request, "targetFormId", 0L);
        // 关联Id
        Long relationId = ReqUtil.getLong(request, "relationId", 0L);
        // 类型：new 新增；modify 修改； view 查看
        String type = ReqUtil.getString(request, "type", "view");
        // 根据参数判断是否是从表单管理的业务关系进入
        String from = ReqUtil.getString(request, "formManager", "false");
        FormDesignUtil.checkBusinessRelationPlugins(type, from);
        FormRelationshipBean formRelationshipBean = new FormRelationshipBean();
        if (!FormDesignUtil.NEW.equals(type)) {
            // 修改和查看的时候，sourceFormId和targetFormId从关系里面取，因为前端可能切换了源头，导致sourceFormId和targetFormId对调了
            formRelationshipBean = cap4FormCacheManager.getFormRelationshipBean(relationId);
            formId = formRelationshipBean.getSourceFormId();
            targetFormId = formRelationshipBean.getTargetFormId();
        }
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        FormBean targetFormBean = cap4FormCacheManager.getForm(targetFormId);
        mav.addObject("formBean", formBean);
        mav.addObject("targetFormBean", targetFormBean);
        List<FormFieldBean> targetAllField = targetFormBean.getAllFieldBeans();
        //排除B表不能参与映射的字段
        List<FormFieldBean> targetRemoveField = new ArrayList<FormFieldBean>();
        for (FormFieldBean formFieldBean : targetAllField) {
            if (formFieldBean.isCustomerCtrl()) {
                targetRemoveField.add(formFieldBean);
            }
        }
        targetAllField.removeAll(targetRemoveField);
        mav.addObject("targetFieldList", targetAllField);
        // 唯一字段
        List<List<String>> uniques = targetFormBean.getUniqueFieldList();
        List<List<FormFieldBean>> uniqueList = new ArrayList<List<FormFieldBean>>();
        //B表所有的唯一标识字段
        List<FormFieldBean> tempFields = new ArrayList<FormFieldBean>();
        //清理B表的
        for (List<String> list : uniques) {
            List<FormFieldBean> fieldBeanList = new ArrayList<FormFieldBean>();
            for (String fieldName : list) {
                FormFieldBean formField = targetFormBean.getFieldBeanByName(fieldName);
                fieldBeanList.add(formField);
                tempFields.add(formField);
            }
            uniqueList.add(removeExtendField(fieldBeanList));
        }
        boolean hasUnique = uniqueList.size() != 0;
        mav.addObject("uniqueList", uniqueList);
        mav.addObject("hasUnique", hasUnique);
        List<FormFieldBean> allFields = formBean.getAllFieldBeans();
        // 自动关联A表可参与条件的字段
        mav.addObject("srcAllField", removeExtendField(allFields));
        //B表除了唯一标识之外的可以参与自动关联条件的字段
        List<FormFieldBean> otherFields = targetFormBean.getAllFieldBeans();
        otherFields.removeAll(tempFields);
        mav.addObject("otherFields", removeExtendField(otherFields));
        List<FormFieldBean> fieldList = new ArrayList<FormFieldBean>();
        //清理A表单可以参与映射的字段
        for (FormFieldBean temp : allFields) {
            FormFieldCtrl formFieldCtrl = temp.getFieldCtrl();
            // 根据控件自身属性判断是否能设置关联
            if (!formFieldCtrl.canRelation()) {
                continue;
            }
            // 已经是数据关联的字段，直接排除
            if (!temp.isRelationField()) {
                //20180619 产品要求，去掉关联映射对已参与表内关联的限制，放开可以多次参与映射
                fieldList.add(temp);
            } else {
                // 修改和查看的时候，本关系的关联字段也要放到前台去
                if (!FormDesignUtil.NEW.equals(type) && temp.getRelationId() == relationId.longValue()) {
                    fieldList.add(temp);
                }
            }
        }
        mav.addObject("fieldList", fieldList);
        mav.addObject("targetFormName", targetFormBean.getFormName());
        List<FormTableBean> targetTableList = targetFormBean.getTableList();
        mav.addObject("targetTableList", targetTableList);
        mav.addObject("uniqueFieldList", targetFormBean.getUniqueFieldList());
        mav.addObject("showSystem", !targetFormBean.isFlowForm());
        mav.addObject("flowForm",formBean.isFlowForm());
        mav.addObject("sourceTableList", formBean.getTableList());
        mav.addObject("hasDetailTable",formBean.getTableList().size() > 1);
        boolean canSwitch = true;
        if (FormDesignUtil.MODIFY.equals(type)) {
            canSwitch = formBean.getState() == Enums.FormStateEnum.official.getKey() ? cap4FormBusinessRelationManager.canSwitchRelationState(formId) : true;
        }
        mav.addObject("canSwitch", canSwitch);
        if (FormDesignUtil.NEW.equals(type)) {
            formRelationshipBean.setSourceFormId(formId);
            formRelationshipBean.setTargetFormId(targetFormId);
            formRelationshipBean.setState(Enums.BusinessRelationState.ON.getKey());
            formRelationshipBean.setRelationType(ViewSelectType.user.getKey());
            // 设置关系的初始值
            String targetTable = "";
            String targetTableDisplay = targetFormBean.getFormName() + "(";
            int i = 0;
            for (FormTableBean temp : targetTableList) {
                if (i > 0) {
                    targetTable += ",";
                    targetTableDisplay += "、";
                }
                targetTable += temp.getTableName();
                targetTableDisplay += temp.getDisplay();
                i++;
            }
            targetTableDisplay += ")";
            formRelationshipBean.setTargetTable(targetTable);
            formRelationshipBean.putExtraAttr("targetTableDisplay", targetTableDisplay);
        } else {
            String targetTable = formRelationshipBean.getTargetTable();
            String[] selectedTable = targetTable.split(",");
            int i = 0;
            String newTargetTable = "";
            String targetTableDisplay = targetFormBean.getFormName() + "(";
            for (String tableName : selectedTable) {
                FormTableBean formTableBean = targetFormBean.getTableByTableName(tableName);
                if(formTableBean == null){
                    //这里兼容一下，之前sp2开发的时候，删除明细表的时候，校验表名那里有bug，导致没有校验到系统关联已经引用了该明细表，直接被删除了
                    continue;
                }
                if (i > 0) {
                    targetTableDisplay += "、";
                    newTargetTable += ",";
                }
                newTargetTable += tableName;
                targetTableDisplay += formTableBean.getDisplay();
                i++;
            }
            formRelationshipBean.setTargetTable(newTargetTable);
            targetTableDisplay += ")";
            formRelationshipBean.putExtraAttr("targetTableDisplay", targetTableDisplay);
            AppContext.putThreadContext("toDisplay", true);
            mav.addObject("dataJson", formRelationshipBean.toJSON());
        }
        mav.addObject("formRelationshipBean", formRelationshipBean);
        return mav;
    }

    /**
     * 系统关联唯一标识过滤掉扩展控件和地图控件
     *
     * @param fields 参与判断的字段集
     */
    private List<FormFieldBean> removeExtendField(List<FormFieldBean> fields) {
        List<FormFieldBean> result = new ArrayList<FormFieldBean>();
        for (FormFieldBean field : fields) {
            FormFieldCtrl ctrl = field.getFieldCtrl();
            if (ctrl instanceof FormFieldExtendCtrl
                    || ctrl instanceof FormFieldMapCtrl
                    || ctrl instanceof FormFieldCustomCtrl
                    || ctrl instanceof FormFieldLineNumber) {
                continue;
            }
            result.add(field);
        }
        return result;
    }

    public ModelAndView listFormData(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("cap4/form/business/bizconfigApp/businessFlowFormList");
        FlipInfo flipInfo = cap4FormListManager.showFormList(new FlipInfo(-1), null);
        List data = flipInfo.getData();
        modelAndView.addObject("formList", JSONUtil.toJSONString(data));
        return modelAndView;
    }

    public ModelAndView formCommitSetting(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessRelation/formCommitSetting");
        Long formId = ReqUtil.getLong(request, "formId", 0L);
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        List<FormViewBean> pcViewList = formBean.getFormViewList(Enums.ViewType.SeeyonForm);
        List<FormViewBean> phoneViewList = formBean.getFormViewList(Enums.ViewType.Phone);
        mav.addObject("pcViewList", pcViewList);
        mav.addObject("phoneViewList", phoneViewList);
        return mav;
    }

    /**
     * 选择穿透的视图，包括关联列表穿透视图和浏览态数据的穿透视图
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView selectView(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessRelation/selectView");
        Long formId = ReqUtil.getLong(request, "formId", 0L);
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        List<FormViewBean> pcViewList = formBean.getFormViewList(Enums.ViewType.SeeyonForm);
        List<FormViewBean> phoneViewList = formBean.getFormViewList(Enums.ViewType.Phone);
        mav.addObject("browse", Enums.FormAuthorizationType.show.getKey());
        mav.addObject("pcViewList", pcViewList);
        mav.addObject("phoneViewList", phoneViewList);
        return mav;
    }



    /**
     * 关联映射
     */
    public ModelAndView relationSelectorSet(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessRelation/relationSelectorSet");

        return mav;
    }




    /**
     * 选择触发的指定操作
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView selectOperate(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessTrigger/selectOperate");
        Long formId = ReqUtil.getLong(request, "formId", 0L);
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        List<FormViewBean> pcViewList = formBean.getFormViewList(Enums.ViewType.SeeyonForm);
        List<FormViewBean> phoneViewList = formBean.getFormViewList(Enums.ViewType.Phone);
        mav.addObject("browse", Enums.FormAuthorizationType.show.getKey());
        mav.addObject("pcViewList", pcViewList);
        mav.addObject("phoneViewList", phoneViewList);
        return mav;
    }

    /**
     * 触发时间调度设置
     */
    public ModelAndView triggerDateTimeSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("cap4/form/businessTrigger/timeset4CAP4");
        Long formId = ParamUtil.getLong(request.getParameterMap(), "formId", -1L);
        FormBean fb = cap4FormCacheManager.getForm(formId);
        mav.addObject("formBean", fb);
        WorkTimeSetManager workTimeSetManager = (WorkTimeSetManager) AppContext.getBean("workTimeSetManager");
        WorkTimeCurrency workTimeCurrency = workTimeSetManager.findComnWorkTimeSet(
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),
                String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1), AppContext.currentAccountId(), false);
        String amWorkTimeBeginTime = workTimeCurrency.getAmWorkTimeBeginTime();
        if (amWorkTimeBeginTime.startsWith("0")) {
            amWorkTimeBeginTime = amWorkTimeBeginTime.substring(1);
        }
        mav.addObject("timeQuartz", amWorkTimeBeginTime);
        return mav;
    }

    /**
     * 添加预写设置方法
     * targetId 表单ID
     * selectedFieldList 映射字段已经选择的数字型字段
     * type 调用预写设置的类型  form（表单）或者magicInterface（魔方接口） 默认为form
     */
    public ModelAndView prewriteSet(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView model = new ModelAndView("cap4/form/businessTrigger/prewriteSet4CAP4");
        List<Object> targetFieldList = new LinkedList<Object>();
        List<Object> sourceFieldList = new LinkedList<Object>();

        String selectedFieldList = ReqUtil.getString(request, "selectedFieldList", "");
        Long targetId = ReqUtil.getLong(request, "targetId", -1L);
        String type = ReqUtil.getString(request, "type", "form");
        model.addObject("type", type);//数据魔方适配，增加参数，正常的模板选择传入的是form，数据魔方传入的是magicInterface

        if ("magicInterface".equals(type)) {
            MagicInterfaceDefineEntity entity = magicInterfaceDefineDao.loadDefineById(targetId);
            if (entity != null) {
                Map extMapper = JSONUtil.parseJSONString(entity.getExt1(), Map.class);
                if (extMapper != null) {
                    List fillback = (List) extMapper.get(MagicPrivateConstants.COMMON_IDENTIFIER_FILLBACK);
                    Map fieldLimit = (Map) extMapper.get(MagicPrivateConstants.COMMON_IDENTIFIER_FIELDLIMIT);
                    for (Object t : fillback) {
                        Object limit = fieldLimit.get(t.toString());
                        if (limit != null) {
                            Map<String, Object> limitMapper = new HashMap<String, Object>();
                            limitMapper.put("limitConfig", JSONUtil.parseJSONString(limit.toString()));
                            limitMapper.put("name", t);
                            if (selectedFieldList.contains(t.toString())) {
                                targetFieldList.add(limitMapper);
                            } else {
                                sourceFieldList.add(limitMapper);
                            }
                        }
                    }
                }
            }
        } else {
            FormBean targetFormBean = this.cap4FormCacheManager.getForm(targetId);
            if (targetFormBean != null) {
                for (FormFieldBean ffb : targetFormBean.getAllFieldBeans()) {
                    if (Enums.FieldType.DECIMAL.getKey().equals(ffb.getFieldType())
                            && FormFieldComEnum.TEXT.getKey().equals(ffb.getInputType())) {
                        if (selectedFieldList.contains(ffb.getName())) {
                            targetFieldList.add(ffb);
                        } else {
                            sourceFieldList.add(ffb);
                        }
                    }
                }
            }
        }
        model.addObject("sourceFieldList", sourceFieldList);
        model.addObject("targetFieldList", targetFieldList);
        return model;
    }

    /**
     * 模板选择界面 仅新建流程才调用此方法，无流程的都是直接选择权限了
     * 关键参数
     * type 调用预写设置的类型  form（表单）或者magicInterface（魔方接口） 默认为form
     * targetId :如果type是 form 则是表单ID ，如果是接口 则是接口Id
     */
    public ModelAndView triggerTemplateSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("cap4/form/businessTrigger/triggerTemplate4CAP4");
        String type = ReqUtil.getString(request, "type");//数据魔方适配，增加参数，正常的模板选择传入的是form，数据魔方传入的是magicInterface
        if ("magicInterface".equals(type)) {
            String targetId = ReqUtil.getString(request, "targetId");
            try {
                long id = Long.parseLong(targetId);
                MagicInterfaceDefineEntity data = magicInterfaceDefineDao.loadDefineById(id);
                if (data != null) {
                    Map extMapper = JSONUtil.parseJSONString(data.getExt1(), Map.class);
                    if (extMapper != null) {
                        mav.addObject("templateList", extMapper.get("templates"));
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("触发模版选择是 数据接口出错，原因: " + e.getLocalizedMessage());
            }
        } else {
            String targetFormId = ReqUtil.getString(request, "targetFormId");
            FormBean targetForm = cap4FormCacheManager.getForm(Long.valueOf(targetFormId));
            List<SimpleObjectBean> templateList = new ArrayList<SimpleObjectBean>();
            if (targetForm != null) {
                if (targetForm.isFlowForm()) {
                    List<CtpTemplate> list = cap4FormManager.getFormSystemTemplate(targetForm.getId());
                    for (CtpTemplate template : list) {
                        // 过滤掉未发布的
                        if (template.getState() != TemplateEnum.State.normal.ordinal()) {
                            continue;
                        }
                        SimpleObjectBean object = new SimpleObjectBean();
                        object.setId(template.getId());
                        object.setName(template.getSubject());
                        templateList.add(object);
                    }
                }
            }
            mav.addObject("templateList", templateList);
        }

        return mav;
    }

    /**
     * 选择目标数据新建/修改权限设置 此方法，表间触发、表内触发公用
     */
    public ModelAndView dataRightSet(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessTrigger/dataRightSet");
        Long formId = ReqUtil.getLong(request, "targetFormId", 0L);
        FormBean formBean = cap4FormCacheManager.getForm(formId);
        String actionType = ReqUtil.getString(request, "actionType", "");// 触发类型，此参数仅触发消息时传入，其余的都没有此参数
        if (Strings.isNotEmpty(actionType)) {
            if ("triggerSource".equals(actionType)) {
                //触发源设置 界面和消息权限界面有所不同，不能共用
                mav = new ModelAndView("cap4/form/businessTrigger/triggerSourceSet");
                List<FormViewBean> pcViewList = formBean.getFormViewList(Enums.ViewType.SeeyonForm);
                List<FormViewBean> phoneViewList = formBean.getFormViewList(Enums.ViewType.Phone);
                mav.addObject("browse", Enums.FormAuthorizationType.show.getKey());
                mav.addObject("pcViewList", pcViewList);
                mav.addObject("phoneViewList", phoneViewList);
            } else {
                // 触发消息显示的权限为浏览
                mav = new ModelAndView("cap4/form/businessTrigger/dataRightSet4Msg");
                List<FormTriggerDataRightVo> pcViewList = new ArrayList<FormTriggerDataRightVo>();
                List<FormTriggerDataRightVo> phoneViewList = new ArrayList<FormTriggerDataRightVo>();
                FormTriggerDataRightVo showVo;
                for (FormViewBean viewBean : formBean.getFormViewList()) {
                    for (FormAuthViewBean auth : viewBean.getAllOperations()) {
                        if (!auth.isDelete() && Enums.FormAuthorizationType.show.getKey().equals(auth.getType())) {
                            showVo = new FormTriggerDataRightVo();
                            showVo.setRightId(viewBean.getId() + "." + auth.getId());
                            showVo.setRightName(viewBean.getFormViewName() + "." + auth.getName());
                            if (Enums.ViewType.SeeyonForm.getText().equals(viewBean.getFormViewType())) {
                                pcViewList.add(showVo);
                            } else {
                                phoneViewList.add(showVo);
                            }
                        }
                    }
                }
                mav.addObject("pcViewList", pcViewList);
                mav.addObject("phoneViewList", phoneViewList);
            }
        } else {
            List<FormTriggerDataRightVo> addAuthList = new ArrayList<FormTriggerDataRightVo>();
            List<FormTriggerDataRightVo> editAuthList = new ArrayList<FormTriggerDataRightVo>();
            String viewShowName;
            FormTriggerDataRightVo rightVo;
            for (FormViewBean viewBean : formBean.getFormViewList()) {
                if (Enums.ViewType.SeeyonForm.getText().equals(viewBean.getFormViewType())) {
                    viewShowName = "电脑端." + viewBean.getFormViewName();
                } else {
                    viewShowName = "移动端." + viewBean.getFormViewName();
                }
                for (FormAuthViewBean auth : viewBean.getAllOperations()) {
                    if (auth.isDelete()) {
                        continue;
                    }
                    rightVo = new FormTriggerDataRightVo();
                    rightVo.setRightId(viewBean.getId() + "." + auth.getId());
                    rightVo.setRightName(viewShowName + "." + auth.getName());

                    if (Enums.FormAuthorizationType.add.getKey().equals(auth.getType())) {
                        addAuthList.add(rightVo);
                    } else if (Enums.FormAuthorizationType.update.getKey().equals(auth.getType())) {
                        editAuthList.add(rightVo);
                    }
                }
            }
            mav.addObject("addAuthList", addAuthList);
            mav.addObject("editAuthList", editAuthList);
        }
        return mav;
    }

    /**
     * 触发设置编辑、查看
     */
    public ModelAndView editTrigger(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView("cap4/form/businessTrigger/outerDesign4CAP4");
        Long formId = ReqUtil.getLong(request, "formId", 0L);// 源表
        Long targetFormId = ReqUtil.getLong(request, "targetFormId", 0L);// 目标表
        String type = ReqUtil.getString(request, "type", "view");// 类型：new 新增；modify 修改； view 查看
        // 根据参数判断是否是从表单管理的业务关系进入
        String from = ReqUtil.getString(request, "formManager", "false");
        FormDesignUtil.checkBusinessRelationPlugins(type, from);
        if (!FormDesignUtil.NEW.equals(type)) {
            // 非新建状态下的源表和目标表需要从触发设置中取，避免在修改或者查看状态下点击新建切换表单后传入后台的表单发生互转
            Long triggerId = ReqUtil.getLong(request, "triggerId", 0L);// 触发Id
            FormTriggerBean nowTriggerBean = cap4FormCacheManager.getFormTriggerBean(triggerId);
            if (nowTriggerBean == null) {
                throw new BusinessException("环境异常或者，当前触发已删除！" + triggerId);
            }
            formId = nowTriggerBean.getSourceFormId();
            targetFormId = nowTriggerBean.getTargetFormId();
        }
        FormBean targetFormBean = cap4FormCacheManager.getForm(targetFormId);

        FormBean sourceFormBean = cap4FormCacheManager.getForm(formId);
        // 源表字段
        List<FormFieldBean> sourceFieldList = new ArrayList<FormFieldBean>();
        List<FormFieldBean> sourceConditionFieldList = new ArrayList<FormFieldBean>();
        if (sourceFormBean.isFlowForm()) {
            //流程表单增加流程名称的拷贝
            sourceFieldList.add(FormTriggerUtil.getSourceFieldBean(sourceFormBean, FormTriggerUtil.FLOW_TITLE_NAME));
        }
        FormTriggerUtil.filterMappingFields(sourceFormBean, sourceFieldList, sourceConditionFieldList);

        // 目标表字段
        List<FormFieldBean> targetFormFieldList = new ArrayList<FormFieldBean>();
        List<FormFieldBean> targetConditionFieldList = new ArrayList<FormFieldBean>();
        FormTriggerUtil.filterMappingFields(targetFormBean, targetFormFieldList, targetConditionFieldList);

        // 唯一标示字段组合
        List<List<FormFieldBean>> uniqueFieldList = new ArrayList<List<FormFieldBean>>();
        List<List<String>> uniqueFields = targetFormBean.getUniqueFieldList();
        if (Strings.isNotEmpty(uniqueFields)) {
            List<FormFieldBean> uniques;
            for (List<String> fields : uniqueFields) {
                uniques = new ArrayList<FormFieldBean>();
                for (String field : fields) {
                    FormFieldBean fieldBean = targetFormBean.getFieldBeanByName(field);
                    uniques.add(fieldBean);
                }
                targetConditionFieldList.removeAll(uniques);
                uniqueFieldList.add(uniques);
            }
        }

        mav.addObject("targetFormBean", targetFormBean);
        mav.addObject("hasUnique", Strings.isNotEmpty(uniqueFieldList));
        mav.addObject("uniqueFieldList", uniqueFieldList);
        mav.addObject("targetFieldList", targetFormFieldList);
        mav.addObject("targetConditionFieldList", targetConditionFieldList);

        Enums.FormType sourceType = Enums.FormType.getEnumByKey(sourceFormBean.getFormType());
        Enums.FormType targetType = Enums.FormType.getEnumByKey(targetFormBean.getFormType());
        List<FormTriggerBaseDesignManager> actionList = new ArrayList<FormTriggerBaseDesignManager>();
        actionList.addAll(FormTriggerUtil.getDesignManagerList(sourceType, targetType, FormTriggerBean.TriggerBusinessType.FormOuter));
        //防xss注入
        type = Strings.escapeJavascript(type);
        mav.addObject("type", type);
        mav.addObject("sourceFormBean", sourceFormBean);
        mav.addObject("sourceFieldList", sourceFieldList);
        mav.addObject("sourceConditionFieldList", sourceConditionFieldList);
        mav.addObject("actionList", actionList);
        return mav;
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setCap4FormBusinessRelationManager(CAP4FormBusinessRelationManager cap4FormBusinessRelationManager) {
        this.cap4FormBusinessRelationManager = cap4FormBusinessRelationManager;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }

    public IMagicInterfaceDefineDao getMagicInterfaceDefineDao() {
        return magicInterfaceDefineDao;
    }

    public void setMagicInterfaceDefineDao(IMagicInterfaceDefineDao magicInterfaceDefineDao) {
        this.magicInterfaceDefineDao = magicInterfaceDefineDao;
    }

    public void setCap4FormListManager(CAP4FormListManager cap4FormListManager) {
        this.cap4FormListManager = cap4FormListManager;
    }
}
