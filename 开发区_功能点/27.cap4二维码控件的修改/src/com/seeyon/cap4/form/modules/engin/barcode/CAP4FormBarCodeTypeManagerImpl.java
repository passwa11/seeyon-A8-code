package com.seeyon.cap4.form.modules.engin.barcode;

import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormRelationshipMapBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.ImpResultBean;
import com.seeyon.cap4.form.modules.engin.relation.CAP4FormRelationActionManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.BarcodeConstant;
import com.seeyon.cap4.form.util.BarcodeEnum;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.template.manager.CAPRuntimeCalcManager;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.barCode.vo.ResultVO;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.common.publicqrcode.dao.PublicQrCodeDao;
import com.seeyon.ctp.common.publicqrcode.po.PublicQrCodePO;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段二维码内容组装实现
 * Created by wangh on 2018-1-30
 */
public class CAP4FormBarCodeTypeManagerImpl implements BarCodeTypeManager {

    private static final Log log = CtpLogFactory.getLog(CAP4FormManager.class);
    private CAP4FormManager cap4FormManager;
    private PublicQrCodeDao publicQrCodeDao;
    private CAP4FormRelationActionManager cap4FormRelationActionManager;
    private CAPRuntimeCalcManager capRuntimeCalcManager;

    public PublicQrCodeDao getPublicQrCodeDao() {
        return publicQrCodeDao;
    }

    public void setPublicQrCodeDao(PublicQrCodeDao publicQrCodeDao) {
        this.publicQrCodeDao = publicQrCodeDao;
    }
    /**
     * 返回当前实现服务的类型
     *
     * @return 类型
     */
    @Override
    public String getType() {
        return "cap4form";
    }

    /**
     * 根据传入的参数获取需要转换为二维码的字符串
     *
     * @param param 参数，自定义的参数
     * @return 需要转换的字符串
     */
    @Override
    public String getContentStr(Map<String, Object> param) {
        Map<String,Object> contentJson = new HashMap<String, Object>(16);
        Long formId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_FORM_ID, 0L);
        Long dataId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_DATA_ID, 0L);
        String fieldName = ParamUtil.getString(param, BarcodeConstant.BARCODE_PARAM_FIELD_NAME, "");
        Long recordId = ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_SUB_DATA_ID, 0L);

        FormBean formBean = cap4FormManager.getForm(formId,false);
        String version = FormConstant.cap4version;
        String contents = "";
        FormDataMasterBean cacheMasterData = cap4FormManager.getSessioMasterDataBean(dataId);
        FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
        if (formFieldBean == null) {
            log.info("-----------生成二维码图片时，控件为空,fieldName="+fieldName+"。表单ID为：" + formId + "------------");
            return contents;
        }
        String customParam = formFieldBean.getCustomParam();
        Map<String,Object> customParamMap = (Map<String,Object>)JSONUtil.parseJSONString(customParam);
        Map<String,Object> barcodeInfo = (Map<String,Object>)customParamMap.get("barcodeInfo");
        Map<String,Object> contentTemplate  = (Map<String,Object>)barcodeInfo.get("content");
        String fields = String.valueOf(contentTemplate.get("content"));
        if (Strings.isBlank(fields)) {
            log.info("-------------二维码组成项为空,fieldName="+fieldName+"。表单ID为：" + formId + "-----------------");
            return contents;
        }
        //重复表当前行数据
        Map<String, Object> subDataMap = new HashMap<String, Object>();
        if (formFieldBean.isSubField()) {
            subDataMap = cacheMasterData.getSubDataMapById(formFieldBean.getOwnerTableName(), recordId);
        }
        String[] fieldArray = fields.split("[,]");
        for (String field : fieldArray) {
            FormFieldBean tempField = formBean.getFieldBeanByName(field);
            Object value = null;
            if (tempField == null) {
                log.info("生成【"+formFieldBean.getDisplay()+"】字段二维码时，内容组成项中该字段为空："+field+";formId:"+formId);
                continue;
            }
            if (tempField.isMasterField()) {
                value = cacheMasterData.getFieldValue(tempField.getName());
            } else {
                value = subDataMap.get(tempField.getName());
            }
            BarcodeEnum.NeedSaveDisplayType type = tempField == null?null: BarcodeEnum.NeedSaveDisplayType.getEnumByKey(tempField.getInputType());
            if(type != null){
                //需要存入显示值的
                try {
                    Object[] objs = tempField.getDisplayValue(value,true,true);
                    value = objs[1];
                } catch (BusinessException e) {
                    log.error(e.getMessage(), e);
                }
            }
            contentJson.put(tempField.getDisplay(), value == null ? "" : value);
        }
        //如果组成项设置了回填到那个具体的模版，也要生成到二维码中
        String template = contentTemplate.get("template") == null?"":String.valueOf(contentTemplate.get("template"));
        if(Strings.isNotBlank(template)){
            contentJson.put(BarcodeConstant.BARCODE_PARAM_TEMPLATE,template);
        }
        if (!contentJson.isEmpty()) {
            //不为空的情况下，将formId也放进去
            contentJson.put(BarcodeConstant.BARCODE_PARAM_FORM_ID, String.valueOf(formId));
            contentJson.put(BarcodeConstant.BARCODE_PARAM_CURRENT_DATE, DateUtil.currentDateString("yyyy-MM-dd HH:mm:ss"));
            contentJson.put(BarcodeConstant.BARCODE_PARAM_VERSION,version);

        }
        String linkParamStr = JSONUtil.toJSONString(contentJson);
        //如果二维码内容长度超过表字段长度，则返回原始json，外层manager判断是否超长，超长添加日志记录
        if(linkParamStr.length()>1000){
            return linkParamStr;
        }
        Date date = new Date();
        PublicQrCodePO publicQrCodePO = new PublicQrCodePO();
        Long id = UUIDLong.longUUID();
        publicQrCodePO.setId(id);
        publicQrCodePO.setCategory(ApplicationCategoryEnum.cap4Form.name());
        publicQrCodePO.setObjectId(dataId);
        publicQrCodePO.setLinkParams(linkParamStr);
        publicQrCodePO.setState(0);
        publicQrCodePO.setCreateDate(date);
        publicQrCodePO.setUpdateDate(date);
        publicQrCodePO.setAccountId(AppContext.currentAccountId());
        try {
            publicQrCodeDao.createPublicQrCode(publicQrCodePO);
        } catch (BusinessException e) {
            log.error(e.getMessage(),e);
        }
        return String.valueOf(id);
    }

    /**
     * 当生成的二维码内容超过长度之后，生成自己想要的特殊二维码
     *
     * @param param
     * @return
     */
    @Override
    public String getContent4OutOfLength(Map<String, Object> param) {
        DataContainer dc = new DataContainer();
        dc.put(BarcodeConstant.BARCODE_PARAM_FORM_ID,ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_FORM_ID, 0L));
        dc.put(BarcodeConstant.BARCODE_PARAM_DATA_ID,ParamUtil.getLong(param, BarcodeConstant.BARCODE_PARAM_DATA_ID, 0L));
        dc.put(BarcodeConstant.BARCODE_PARAM_CURRENT_DATE, DateUtil.currentDateString("yyyy-MM-dd HH:mm:ss"));
        dc.put(BarcodeConstant.BARCODE_PARAM_VERSION,FormConstant.cap4version);
        dc.put(BarcodeConstant.BARCODE_PARAM_OUT_OF_LENGTH,"true");
        return dc.getJson();
    }

    /**
     * cap4二维码表内扫一扫，文本类型二维码解析并返回回填值接口
     *
     * @param decodeStr 二维码中数据
     * @param param     解析参数
     * @return 解析结果
     */
    @Override
    public Object decode(String decodeStr, Map<String, Object> param) throws BusinessException{
        Long formId = Long.valueOf(String.valueOf(param.get("formId")));//表单id
        Long dataId = Long.valueOf(String.valueOf(param.get("dataId")));//数据id
        Long rightId = Long.valueOf(String.valueOf(param.get("rightId")));//权限id
        String recordId = String.valueOf(param.get("recordId"));//重复行id
        String tableName = String.valueOf(param.get("tableName"));//重复表名称
        FormDataMasterBean cacheMasterData = cap4FormManager.getSessioMasterDataBean(dataId);
        FormBean formBean = cap4FormManager.getForm(formId,false);
        if(null == formBean){
            throw new BusinessException("找不到表单定义，formId：" + formId);
        }
        if(null == cacheMasterData){
            throw new BusinessException("找不到表单数据缓存，dataId：" + dataId);
        }
        Object json = null;
        if(decodeStr.contains("{")&&decodeStr.contains("}")) {//兼容老的二维码
            try {
                json = JSONUtil.parseJSONString(decodeStr);
            } catch (Exception e) {
                log.error("扫一扫录入二维码数据异常：" + decodeStr, e);
                throw new BusinessException("扫一扫录入二维码数据异常：" + decodeStr);
            }
        }else{
            PublicQrCodePO qrCodePO = publicQrCodeDao.getPublicQrCode(Long.parseLong(decodeStr));
            try {
                json = JSONUtil.parseJSONString(qrCodePO.getLinkParams());
            } catch (Exception e) {
                log.error("扫一扫录入二维码数据异常：" + decodeStr, e);
                throw new BusinessException("扫一扫录入二维码数据异常：" + decodeStr);
            }
        }
        if (null == json) {
            return new ResultVO(false, "Unsupported two-dimensional code data！");
        }
        FormAuthViewBean authViewBean = null;
        if(cacheMasterData.getExtraMap().containsKey(FormConstant.viewRight)) {
            authViewBean = (FormAuthViewBean)cacheMasterData.getExtraAttr(FormConstant.viewRight);
        }
        if(null == authViewBean){
            authViewBean = formBean.getAuthViewBeanById(rightId);
        }
        Map<String,Object> res = new HashMap<String, Object>();
        Map<String,Object> mainUpdate = new HashMap<String, Object>();
        Map<String, List<FormRelationshipMapBean>> manualFormRelationshipMapBeans = cap4FormRelationActionManager.getFormField4ManualRelationMapBeans(formBean);
        if (json instanceof Map) {
            Map<String,Object> contentMap = (Map<String,Object>)json;
            String outOfLength = String.valueOf(contentMap.get("outOfLength"));
            if("true".equals(outOfLength)){
                return new ResultVO(false, ResourceUtil.getString("form.barcode.out.of.length.tips"));
            }
            FormTableBean formTableBean = null;
            if(!StringUtil.checkNull(tableName)){
                formTableBean = formBean.getTableByTableName(tableName);
            }else{
                formTableBean = formBean.getMasterTableBean();
            }
            //从二维码内容中将字段按照表进行分组
            Map<String,List<FormFieldBean>> tableGroupMap = new HashMap<String, List<FormFieldBean>>();
            for(Map.Entry<String,Object> entry:contentMap.entrySet()){
                String key = entry.getKey();
                FormFieldBean fieldBean = formBean.getFieldBeanByDisplay(key);//二维码中使用的显示名称
                if(null !=fieldBean && !fieldBean.isConstantField()){
                    if(!tableGroupMap.containsKey(fieldBean.getOwnerTableName())){
                        List<FormFieldBean> fields = new ArrayList<FormFieldBean>();
                        fields.add(fieldBean);
                        tableGroupMap.put(fieldBean.getOwnerTableName(),fields);
                    }else{
                        List<FormFieldBean> fields = tableGroupMap.get(fieldBean.getOwnerTableName());
                        fields.add(fieldBean);
                    }
                }
            }
            //如果是从主表扫一扫进入，则回填所有能够在二维码中找到和当前表单字段相同的所有数据
            if(formTableBean.isMainTable()){
                for(Map.Entry<String,List<FormFieldBean>> entry:tableGroupMap.entrySet()){
                    String tName = entry.getKey();
                    List<FormFieldBean> fields = entry.getValue();
                    FormTableBean tempTableBean = formBean.getTableByTableName(tName);
                    if(fields.size() > 0) {
                        if (tempTableBean.isMainTable()) {
                            for (FormFieldBean field : fields) {
                                FormAuthViewFieldBean auth = authViewBean.getFormAuthorizationField(field.getName());
                                //流程处理意见和非编辑权限的单元格不回填
                                if(FormFieldComEnum.FLOWDEALOPITION.getKey().equals(field.getInputTypeEnum().getKey()) || !auth.isEditAuth() || field.isCalcField()){
                                    continue;
                                }
                                ImpResultBean impResultBean = field.getValue4Import(contentMap.get(field.getDisplay()), false, true);
                                if(null != impResultBean) {
                                    cacheMasterData.addFieldValue(field.getName(), impResultBean.getResult());
                                    cacheMasterData.addFieldChanges4Calc(field, impResultBean.getResult(), null);
                                    Map<String, String> authMap = CAPFormUtil.getAuthMap(field, auth, null, false);
                                    Map<String, Object> fieldValMap = capRuntimeCalcManager.getCalcFieldDataMap(cacheMasterData, field, authMap, manualFormRelationshipMapBeans);
                                    fieldValMap.putAll(authMap);
                                    mainUpdate.put(field.getName(), fieldValMap);
                                }
                            }
                        } else {
                            //如果明细表只有一行，且为空行，则覆盖第一行
                            List<FormDataSubBean> subDatas = cacheMasterData.getSubData(tempTableBean.getTableName());
                            FormDataSubBean formDataSubBean = null;
                            boolean isNew = false;
                            if(subDatas.size()==1 && subDatas.get(0).isEmpty(true)){
                                formDataSubBean = subDatas.get(0);
                            }else {
                                formDataSubBean = new FormDataSubBean(tempTableBean, cacheMasterData, true);
                                cacheMasterData.addSubData(tempTableBean.getTableName(),formDataSubBean);
                                formDataSubBean.setFormmainId(cacheMasterData.getId());
                                isNew = true;
                            }
                            //替换明细表行中的值
                            Map<String,Object> subfieldValMap = null;
                            if(!isNew){
                                subfieldValMap = new HashMap<String, Object>();
                            }
                            for (FormFieldBean field : fields) {
                                FormAuthViewFieldBean auth = authViewBean.getFormAuthorizationField(field.getName());
                                //流程处理意见和非编辑权限的单元格不回填
                                if (FormFieldComEnum.FLOWDEALOPITION.getKey().equals(field.getInputTypeEnum().getKey()) || !auth.isEditAuth() || field.isCalcField()) {
                                    continue;
                                }
                                ImpResultBean impResultBean = field.getValue4Import(contentMap.get(field.getDisplay()), false, true);
                                if (null != impResultBean) {
                                    formDataSubBean.addFieldValue(field.getName(), impResultBean.getResult());
                                    cacheMasterData.addFieldChanges4Calc(field, impResultBean.getResult(), formDataSubBean);
                                }
                                if(!isNew){
                                    Map<String, String> authMap = CAPFormUtil.getAuthMap(field, auth, null, false);
                                    Map<String, Object> fieldValMap = capRuntimeCalcManager.getCalcFieldDataMap(formDataSubBean, field, authMap, manualFormRelationshipMapBeans);
                                    fieldValMap.putAll(authMap);
                                    subfieldValMap.put(field.getName(), fieldValMap);
                                }
                            }
                            //按照前端要求组织回填数据
                            //新增行
                            if(isNew) {
                                Map<String, Object> addSubTableMap = new HashMap<String, Object>();
                                addSubTableMap.put("recordId", String.valueOf(formDataSubBean.getId()));
                                List<FormFieldBean> subFields = tempTableBean.getFields();
                                for (FormFieldBean subfield : subFields) {
                                    FormAuthViewFieldBean auth = authViewBean.getFormAuthorizationField(subfield.getName());
                                    Map<String, String> authMap = CAPFormUtil.getAuthMap(subfield, auth, null, false);
                                    Map<String, Object> fieldValMap = capRuntimeCalcManager.getCalcFieldDataMap(formDataSubBean, subfield, authMap, manualFormRelationshipMapBeans);
                                    fieldValMap.putAll(authMap);
                                    addSubTableMap.put(subfield.getName(), fieldValMap);
                                }
                                Map<String, List<Map<String, Object>>> newAddSubMap = new HashMap<String, List<Map<String, Object>>>();
                                List<Map<String, Object>> addDatas = new ArrayList<Map<String, Object>>();
                                addDatas.add(addSubTableMap);
                                newAddSubMap.put("add", addDatas);
                                res.put(tempTableBean.getTableName(), newAddSubMap);
                            }else{
                                if(subfieldValMap.size()>0) {
                                    Map<String, Object> updateSubTableMap = new HashMap<String, Object>();
                                    updateSubTableMap.put(String.valueOf(formDataSubBean.getId()), subfieldValMap);
                                    Map<String, Map<String, Object>> updateSubMap = new HashMap<String, Map<String, Object>>();
                                    updateSubMap.put("update", updateSubTableMap);
                                    res.put(tempTableBean.getTableName(), updateSubMap);
                                }
                            }
                        }
                    }
                }
            }else{//如果是从明细表扫一扫进入，则回填能够在二维码中找到的主表字段和当前明细表字段
                for(Map.Entry<String,List<FormFieldBean>> entry:tableGroupMap.entrySet()) {
                    String tName = entry.getKey();
                    List<FormFieldBean> fields = entry.getValue();
                    FormTableBean tempTableBean = formBean.getTableByTableName(tName);
                    if (fields.size() > 0) {
                        if(tempTableBean.isMainTable()){
                            for (FormFieldBean field : fields) {
                                FormAuthViewFieldBean auth = authViewBean.getFormAuthorizationField(field.getName());
                                //流程处理意见和非编辑权限的单元格不回填
                                if(FormFieldComEnum.FLOWDEALOPITION.getKey().equals(field.getInputTypeEnum().getKey()) || !auth.isEditAuth() || field.isCalcField()){
                                    continue;
                                }
                                ImpResultBean impResultBean = field.getValue4Import(contentMap.get(field.getDisplay()), false, true);
                                if(null != impResultBean) {
                                    cacheMasterData.addFieldValue(field.getName(), impResultBean.getResult());
                                    cacheMasterData.addFieldChanges4Calc(field, impResultBean.getResult(), null);
                                    Map<String, String> authMap = CAPFormUtil.getAuthMap(field, auth, null, false);
                                    Map<String, Object> fieldValMap = capRuntimeCalcManager.getCalcFieldDataMap(cacheMasterData, field, authMap, manualFormRelationshipMapBeans);
                                    fieldValMap.putAll(authMap);
                                    mainUpdate.put(field.getName(), fieldValMap);
                                }
                            }
                        }else if(tName.equalsIgnoreCase(formTableBean.getTableName())){//当前打开的明细表
                            //从明细表扫一扫进入必须传递表名和recordId 明细表行号
                            if(Strings.isBlank(recordId)){
                                throw new BusinessException("从明细表中扫码录入必须传入行号！");
                            }
                            Long recordIdLong = Long.parseLong(recordId);
                            FormDataSubBean formDataSubBean = cacheMasterData.getFormDataSubBeanById(formTableBean.getTableName(),recordIdLong);
                            if(null == formDataSubBean){
                                throw new BusinessException("从当前数据缓存中找不到明细表行，tableName：" + formTableBean.getTableName() + " 行号：" + recordId);
                            }
                            Map<String,Object> subfieldValMap = new HashMap<String, Object>();
                            for (FormFieldBean field : fields) {
                                FormAuthViewFieldBean auth = authViewBean.getFormAuthorizationField(field.getName());
                                //流程处理意见和非编辑权限的单元格不回填
                                if(FormFieldComEnum.FLOWDEALOPITION.getKey().equals(field.getInputTypeEnum().getKey()) || !auth.isEditAuth() || field.isCalcField()){
                                    continue;
                                }
                                ImpResultBean impResultBean = field.getValue4Import(contentMap.get(field.getDisplay()), false, true);
                                if(null != impResultBean) {
                                    formDataSubBean.addFieldValue(field.getName(), impResultBean.getResult());
                                    cacheMasterData.addFieldChanges4Calc(field, impResultBean.getResult(), formDataSubBean);
                                    Map<String, String> authMap = CAPFormUtil.getAuthMap(field, auth, null, false);
                                    Map<String, Object> fieldValMap = capRuntimeCalcManager.getCalcFieldDataMap(formDataSubBean, field, authMap, manualFormRelationshipMapBeans);
                                    fieldValMap.putAll(authMap);
                                    subfieldValMap.put(field.getName(), fieldValMap);
                                }
                            }
                            Map<String,Object> updateSubTableMap = new HashMap<String, Object>();
                            updateSubTableMap.put(String.valueOf(formDataSubBean.getId()),subfieldValMap);
                            Map<String,Map<String,Object>> updateSubMap = new HashMap<String, Map<String,Object>>();
                            updateSubMap.put("update",updateSubTableMap);
                            res.put(formTableBean.getTableName(),updateSubMap);
                        }
                    }
                }
            }
            //主表字段回填
            if(!mainUpdate.isEmpty()){
                Map<String,Map<String,Object>> mainUpdateMap = new HashMap<String, Map<String,Object>>();
                mainUpdateMap.put("update",mainUpdate);
                res.put(formBean.getMasterTableBean().getTableName(),mainUpdateMap);
            }
        }
        return res;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }

    public void setCap4FormRelationActionManager(CAP4FormRelationActionManager cap4FormRelationActionManager) {
        this.cap4FormRelationActionManager = cap4FormRelationActionManager;
    }

    public void setCapRuntimeCalcManager(CAPRuntimeCalcManager capRuntimeCalcManager) {
        this.capRuntimeCalcManager = capRuntimeCalcManager;
    }
}
