package com.seeyon.apps.cap4.template.service;

import com.google.common.collect.Maps;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormAuthorizationTableBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormDataBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormRelationshipBean;
import com.seeyon.cap4.form.bean.FormRelationshipMapBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.FormTriggerBean;
import com.seeyon.cap4.form.bean.SimpleObjectBean;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;
import com.seeyon.cap4.form.modules.business.BizConfigBean;
import com.seeyon.cap4.form.modules.business.BusinessManager;
import com.seeyon.cap4.form.modules.engin.relation.FormRelationEnums;
import com.seeyon.cap4.form.modules.engin.trigger.CAP4FormTriggerManager;
import com.seeyon.cap4.form.modules.event.FormDataAfterSubmitEvent;
import com.seeyon.cap4.form.modules.event.FormDataBeforeSubmitEvent;
import com.seeyon.cap4.form.modules.serialNumber.CAP4SerialNumberManager;
import com.seeyon.cap4.form.po.CAPFormRelationRecord;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.template.annotation.JMCalling4Service;
import com.seeyon.cap4.template.annotation.ValidateRequestParam;
import com.seeyon.cap4.template.bean.FlowFormCopyDataParamBean;
import com.seeyon.cap4.template.bean.FormDataAttachmentParamBean;
import com.seeyon.cap4.template.bean.FormDataCalculateParamBean;
import com.seeyon.cap4.template.bean.FormDataSubRowOperationParamBean;
import com.seeyon.cap4.template.bean.LockObjectBean;
import com.seeyon.cap4.template.bean.ScreenCapture4FormParamBean;
import com.seeyon.cap4.template.bean.UnFlowFormGetFormDataRightParamBean;
import com.seeyon.cap4.template.result.FormDataResult;
import com.seeyon.cap4.template.result.FormDataSubRowOperationResult;
import com.seeyon.cap4.template.result.ScreenCaptureResult;
import com.seeyon.cap4.template.service.AbstractCAPFormDataService;
import com.seeyon.cap4.template.service.CAPScreenCaptureService;
import com.seeyon.cap4.template.util.CAPFormDataLogUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.monitor.perf.jdbcmonitor.JMCallingObj;
import com.seeyon.ctp.monitor.perf.jdbcmonitor.proxyobj.JMTrackUtils;
import com.seeyon.cap4.monitor.utils.CAP4MonitorUtil;
import com.seeyon.cap4.template.constant.CAPBusinessConstant;
import com.seeyon.cap4.template.constant.CAPBusinessEnum;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.cap4.template.util.CAPParamUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.datasource.CtpDynamicDataSource;
import com.seeyon.ctp.datasource.annotation.DataSourceName;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.monitor.performance.FormRunTypeEnum;
import com.seeyon.ctp.monitor.performance.JMMonitorService;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xiaox on 2017/9/25.
 * 表单运行相关方法
 * 表单新建,保存,查看,关联,计算公式,校验规则,唯一标识
 */
public class CAPFormDataService extends AbstractCAPFormDataService {
    private static final Log LOGGER = CtpLogFactory.getLog(CAPFormDataService.class);
    private static final String UPDATE_LOG = "cap.template.form.data.log.update";
    private static final String CREATE_LOG = "cap.template.form.data.log.create";

    private CAP4FormTriggerManager cap4FormTriggerManager;
    private CAP4SerialNumberManager cap4SerialNumberManager;
    private JMMonitorService jmMonitorService;
    private CAPScreenCaptureService capScreenCaptureService;
    private FormApi4Cap4 formApi4Cap4;

    private static final String SAVE_TYPE = "saveType";
    private static final String CUSTOM_FIELDS = "customFields";
    private static final String TABLE_NAME_TO_ADD_SUB_OBJECTS = "tableName2AddSubObjects";
    private static final String TABLE_NAME_TO_DELETE_IDS = "table2DeleteIds";
    private static final String FILL_BACK_FIELDS = "fillBackFields";
    private static final String CLEAR_TABLES = "clearTables";

    /**
     * 流程表单：一键复制表单数据
     *
     * @param paramBean
     * @return
     */
    @ValidateRequestParam(notEmpty = "formMasterDataId", validateCacheBeanKey = "formMasterDataId")
    public FormDataResult copyFormData(FlowFormCopyDataParamBean paramBean) {
        CtpContentAll contentAll = getCtpContentAllByModuleIdAndType(paramBean.getCopyFrom());
        FormDataResult result = new FormDataResult();
        if (contentAll == null) {
            result.error(CODE11, ResourceUtil.getString(MESSAGE11));
            return result;
        }

        try {
            Set<String> fillBackFields = new HashSet<String>();
            Set<String> clearTables = new HashSet<String>();

            FormBean formBean = cap4FormCacheManager.getForm(paramBean.getFormId());
            FormDataMasterBean formDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterDataId());
            FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(paramBean.getRightId());
            FormDataMasterBean copyFromMasterBean = capFormDataCacheManager.get4Data(contentAll.getContentDataId(), formBean.getId());

            Map<String, List<FormFieldBean>> fieldBeanMap = CAPFormUtil.getTableName2FieldBeans(formBean, formAuthViewBean.getViewBean(formBean));
            if (fieldBeanMap.containsKey(formBean.getMasterTableBean().getTableName())) {
                // 处理主表数据
                List<FormFieldBean> masterFields = fieldBeanMap.get(formBean.getMasterTableBean().getTableName());
                for (FormFieldBean fieldBean : masterFields) {
                    fillCopyFormFieldData(formBean, formDataMasterBean, copyFromMasterBean, fieldBean, formAuthViewBean, fillBackFields);
                }
            }

            Map<String, List<FormDataSubBean>> tableName2AddSubBeans = new HashMap<String, List<FormDataSubBean>>();
            // 返回前端已删除的值
            Map<String, Set<String>> tableName2DeleteIds = new HashMap<String, Set<String>>();
            for (String tableName : fieldBeanMap.keySet()) {
                FormTableBean tableBean = formBean.getTableByTableName(tableName);
                if (tableBean.isMainTable()) {
                    continue;
                }
                Set<String> oldIds = new HashSet<String>();
                List<FormDataSubBean> oldSubBeans = formDataMasterBean.getSubData(tableBean.getTableName());
                for (FormDataSubBean subBean : oldSubBeans) {
                    oldIds.add(String.valueOf(subBean.getId()));
                }
                tableName2DeleteIds.put(tableBean.getTableName(), oldIds);

                List<FormFieldBean> subFields = fieldBeanMap.get(tableName);
                List<FormDataSubBean> copyFromSubBeans = copyFromMasterBean.getSubData(tableBean.getTableName());
                List<FormDataSubBean> newSubBeans = new ArrayList<FormDataSubBean>();
                for (FormDataSubBean subBean : copyFromSubBeans) {
                    FormDataSubBean newSubBean = new FormDataSubBean(tableBean, formDataMasterBean, true);
                    newSubBean.setFormmainId(formDataMasterBean.getId());
                    this.initAddSubDataBean(formDataMasterBean, formAuthViewBean, tableBean, newSubBean);
                    for (FormFieldBean fieldBean : subFields) {
                        fillCopyFormFieldData(formBean, newSubBean, subBean, fieldBean, formAuthViewBean, null);
                    }
                    newSubBeans.add(newSubBean);
                }
                formDataMasterBean.setSubData(tableBean.getTableName(), newSubBeans);
                clearTables.add(tableBean.getTableName());
                tableName2AddSubBeans.put(tableBean.getTableName(), newSubBeans);
            }
            Set<String> calcFillBackFields = capRuntimeCalcManager.execCalcAll(formBean, formDataMasterBean, formAuthViewBean, CAPFormUtil.getTableName2FormSubDataBeanIds(tableName2AddSubBeans));
            fillBackFields.addAll(calcFillBackFields);
            Map<String, Map<String, Object>> tableName2DataMaps = new HashMap<String, Map<String, Object>>();
            capRuntimeCalcManager.buildCalcAllResult(formBean, formDataMasterBean, formAuthViewBean, tableName2AddSubBeans, fillBackFields, CAPBusinessEnum.SubBeanNewFrom.COPY_FORM_DATA, tableName2DataMaps);
            this.mergeAutoDelete2Result(tableName2DeleteIds, tableName2DataMaps);

            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put(TABLE_DATA, tableName2DataMaps);
            resultData.put(CLEAR_TABLES, clearTables);
            result.success(resultData);
            this.buildExtendResult(formBean, formDataMasterBean, formAuthViewBean, result);
            cap4FormManager.saveSessioMasterDataBean(formDataMasterBean.getId(), formDataMasterBean);
        } catch (BusinessException e) {
            LOGGER.error(e);
            result.error(CODE1, ResourceUtil.getString(MESSAGE1));
        }
        return result;
    }

    private boolean checkEnumParentAuth(FormBean formBean, FormAuthViewBean formAuthViewBean, FormFieldBean fieldBean) {
        boolean result = true;
        if (Strings.isNotEmpty(fieldBean.getEnumParent())) {
            FormAuthViewFieldBean parentAuthField = formAuthViewBean.getFormAuthorizationField(fieldBean.getEnumParent());
            if (!Enums.FieldAccessType.edit.getKey().equals(parentAuthField.getAccess())) {
                //非编辑权限不支持
                return false;
            } else {
                FormFieldBean parentFieldBean = formBean.getFieldBeanByName(fieldBean.getEnumParent());
                if (Strings.isNotEmpty(parentFieldBean.getEnumParent())) {
                    return checkEnumParentAuth(formBean, formAuthViewBean, parentFieldBean);
                }
            }
        }
        return result;
    }

    private void fillCopyFormFieldData(FormBean formBean, FormDataBean cacheDataBean, FormDataBean copyFromBean, FormFieldBean fieldBean, FormAuthViewBean formAuthViewBean, Set<String> fillBackFields) throws BusinessException {
        if (fieldBean.isFormulaSn() || fieldBean.isSn() || fieldBean.isCustomerCtrl() || FormFieldComEnum.FLOWDEALOPITION.getKey().equalsIgnoreCase(fieldBean.getInputType())) {
            // 自定义控件和流程处理意见不支持
            return;
        }
        FormAuthViewFieldBean formAuthViewFieldBean = formAuthViewBean.getFormAuthorizationField(fieldBean.getName());
        if (!Enums.FieldAccessType.edit.getKey().equals(formAuthViewFieldBean.getAccess())) {
            //非编辑权限不支持
            return;
        }
        if (fieldBean.isEnumField() && !this.checkEnumParentAuth(formBean, formAuthViewBean, fieldBean)) {
            //父级枚举有非编辑权限不支持
            return;
        }
        FormDataSubBean cacheSubBean = cacheDataBean instanceof FormDataSubBean ? (FormDataSubBean) cacheDataBean : null;
        FormDataMasterBean cacheFormDataMasterBean = cacheSubBean != null ? cacheSubBean.getMasterData() : (FormDataMasterBean) cacheDataBean;
        Object oldDataValue = cacheDataBean.getFieldValue(fieldBean.getName());
        Object value = copyFromBean.getFieldValue(fieldBean.getName());
        if (value != null) {
            if ((fieldBean.isAttachment() || fieldBean.isMap()) && Strings.isNotBlank(String.valueOf(value))) {
                Long subReference = Long.valueOf(String.valueOf(value));
                Long newSubReference = null;
                if (fieldBean.isMap()) {
                    //拍照定位的newSubReference下面附件要使用
                    newSubReference = CAPFormUtil.copyLBS(formBean.getId(), cacheFormDataMasterBean.getId(), null, subReference, fieldBean);
                }
                if (fieldBean.isAttachment()) {
                    newSubReference = CAPAttachmentUtil.copyAttachment(cacheFormDataMasterBean, fieldBean, subReference, cacheFormDataMasterBean.getId(), newSubReference, false);
                }
                value = newSubReference;
            }
        }
        if ((fieldBean.isAttachment() || fieldBean.isMap()) && oldDataValue != null) {
            if (!String.valueOf(oldDataValue).equals(String.valueOf(value))) {
                cacheFormDataMasterBean.putSessionAttachments(String.valueOf(oldDataValue), null);
            }
        }
        cacheDataBean.addFieldValue(fieldBean.getName(), value);
        if (fillBackFields != null) {
            fillBackFields.add(cacheDataBean.generageFillbackKey(fieldBean.getName()));
        }
        cacheFormDataMasterBean.addFieldChanges4Calc(fieldBean, value, cacheSubBean);
    }

    /**
     * 移动端无流程表单：获取表单的视图信息
     *
     * @param paramBean
     * @return
     */
    public FormDataResult getFormDataRightInfo(UnFlowFormGetFormDataRightParamBean paramBean) {
        FormDataResult result = new FormDataResult();
        CtpContentAll contentAll = getCtpContentAll(paramBean.getModuleId(), paramBean.getModuleType());
        if (contentAll == null) {
            result.error(CODE11, ResourceUtil.getString(MESSAGE11));
            return result;
        }
        try {
            Long formId = contentAll.getContentTemplateId();
            String rightId = paramBean.getRightId();
            if (contentAll.getModuleType() == ModuleType.cap4UnflowForm.getKey() && contentAll.getContentDataId() == null) {
                if (StringUtils.equals(DEFAULT_RIGHT_ID, rightId)) {
                    if (paramBean.getFormTemplateId() != null) {
                        FormBean formBean = cap4FormCacheManager.getForm(formId);
                        FormBindAuthBean formBindAuthBean = formBean.getBind().getUnFlowTemplateById(paramBean.getFormTemplateId());
                        SimpleObjectBean simpleObjectBean = formBindAuthBean.getAuthObjByName(FormBindAuthBean.AuthName.ADD.getKey());
                        rightId = simpleObjectBean.getValue() + FormConstant.DOWNLINE + simpleObjectBean.getPhoneValue();
                    }
                }
            }
            result.success(cap4FormManager.getFormDataRightInfo(formId, rightId, contentAll, AppContext.getLocale().toString()));
        } catch (BusinessException e) {
            LOGGER.error("移动端无流程表单——获取表单的视图信息:", e);
            result.error(CODE1, e.getMessage());
        }
        return result;
    }

    public Map<String, Object> removeSessionFormCache(String contentDataId) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(CAPBusinessConstant.KEY_CODE, CODE);
        result.put(CAPBusinessConstant.KEY_MESSAGE, ResourceUtil.getString(MESSAGE));
        cap4FormManager.removeSessionMasterDataBean(Long.valueOf(contentDataId));
        return result;
    }

    public Map<String, Object> removeSessionFormCache(String contentDataId, String moduleId, String moduleType) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(CAPBusinessConstant.KEY_CODE, CODE);
        result.put(CAPBusinessConstant.KEY_MESSAGE, MESSAGE);
        contentDataId = "undefined".equalsIgnoreCase(contentDataId) ? "" : contentDataId;
        moduleId = "undefined".equalsIgnoreCase(moduleId) ? "" : moduleId;
        moduleType = "undefined".equalsIgnoreCase(moduleType) ? "" : moduleType;
        Long userId = AppContext.currentUserId();
        if (!StringUtil.checkNull(contentDataId)) {
            cap4FormManager.removeSessionMasterDataBean(Long.valueOf(contentDataId));
            cap4FormRelationActionManager.removeCacheRelationData(Long.valueOf(contentDataId));
            LOGGER.info("removeSessionFormCache by contentDataId " + contentDataId + " " + userId);
        }
        if (!StringUtil.checkNull(moduleId) && !StringUtil.checkNull(moduleType)
                && !String.valueOf(contentDataId).equals(String.valueOf(moduleId))) {
            LOGGER.info("removeSessionFormCache by moduleId " + moduleId + " " + userId);
            Map contentParams = new HashMap(2);
            contentParams.put(MODULE_TYPE, Integer.parseInt(moduleType));
            contentParams.put(MODULE_ID, Long.valueOf(moduleId));
            List<CtpContentAll> contentPoList = this.ctpMainbodyManager.getContentList(contentParams);
            CtpContentAll ctpContentAll = null;
            if (contentPoList != null && contentPoList.size() > 0) {
                ctpContentAll = contentPoList.get(0);
            }
            if (ctpContentAll != null && ctpContentAll.getContentDataId() != null) {
                cap4FormManager.removeSessionMasterDataBean(ctpContentAll.getContentDataId());
                cap4FormRelationActionManager.removeCacheRelationData(ctpContentAll.getContentDataId());
                LOGGER.info("removeSessionFormCache by moduleId " + moduleId + " contentDataId " + ctpContentAll.getContentDataId() + " " + userId);
            }
        }
        return result;
    }

    /**
     * 获取参数传递的boolean值，适配true或者1
     */
    private static Boolean getParamBooleanValue(Object value, boolean inputBoolean) {
        if (value instanceof Boolean) {
            inputBoolean = (Boolean) value;
        } else if (value instanceof String) {
            inputBoolean = Strings.isNotBlank((String) value) && "1".equals((String) value);
        }
        return inputBoolean;
    }

    /**
     * 保存表单，对前端开放的接口
     */
    public FormDataResult<Map<String, Object>> saveOrUpdateForm(Map<String, Object> params) {
        FormDataResult<Map<String, Object>> result = new FormDataResult<Map<String, Object>>();
        result.error(CODE1, ResourceUtil.getString(MESSAGE1));
        try {
            Map<String, Object> content = (Map<String, Object>) params.get(CONTENT);
            if (content == null) {
                result.error(CODE4, String.format(ResourceUtil.getString(MESSAGE4), CONTENT));
            } else {
                String contentDataId = (String) content.get(CONTENT_DATA_ID);
                FormDataMasterBean cacheFormDataMasterBean = cap4FormManager.getSessioMasterDataBean(Long.valueOf(contentDataId));
                if (cacheFormDataMasterBean == null) {
                    result.error(CODE10, ResourceUtil.getString(MESSAGE10));
                } else {
                    result = this.saveFormData(cacheFormDataMasterBean, content, params);
                    cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("saveFormData.BusinessException.error is", e);
        } catch (SQLException e) {
            LOGGER.error("saveFormData.SQLException.error is", e);
        } catch (CloneNotSupportedException e) {
            LOGGER.error("saveFormData.CloneNotSupportedException.error is", e);
        }
        return result;
    }

    /**
     * 流程二次提交，content数据在缓存里面，params传递空，不需要再合并缓存
     */
    public Map<String, Object> saveOrUpdateFlowFormData(Long masterId, Integer formSaveType, boolean needSn, boolean needCheckRule) throws BusinessException, SQLException {
        FormDataResult<Map<String, Object>> result = new FormDataResult<Map<String, Object>>();
        result.error(CODE1, ResourceUtil.getString(MESSAGE1));
        FormDataMasterBean cacheFormDataMasterBean = cap4FormManager.getSessioMasterDataBean(masterId);
        try {
            if (cacheFormDataMasterBean == null) {
                result.error(CODE10, ResourceUtil.getString(MESSAGE10));
            } else {
                Map<String, Object> content = (Map) cacheFormDataMasterBean.getExtraMap().get(CONTENT);
                if (content == null) {
                    result.error(CODE4, String.format(ResourceUtil.getString(MESSAGE4), CONTENT));
                } else {
                    // 正式提交，这三个参数由协同控制。现修改为后端接口，需要处理下之前放入缓存的值
                    content.put(IS_MERGE, CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
                    content.put(NEED_CHECK_RULE, needCheckRule ? CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey() : CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
                    content.put(NEED_SN, needSn ? CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey() : CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
                    content.put(SAVE_TYPE, String.valueOf(formSaveType));
                    result = this.saveFormData(cacheFormDataMasterBean, content, null);
                    cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
                }
            }
        } catch (CloneNotSupportedException e) {
            LOGGER.error("saveFormData.CloneNotSupportedException.error is", e);
            throw new BusinessException(e);
        }
        if (!result.isSuccessResult()) {
            LOGGER.info("saveOrUpdateFlowFormData fail " + result);
            throw new BusinessException(result.getMessage());
        }
        Map map = new BeanMap(result);
        return map;
    }

    /**
     * 保存表单或者预提交
     */
    private FormDataResult<Map<String, Object>> saveFormData(FormDataMasterBean cacheFormDataMasterBean, Map<String, Object> content, Map<String, Object> params) throws BusinessException, SQLException, CloneNotSupportedException {
        FormDataResult<Map<String, Object>> result = new FormDataResult<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        result.success(data);
        String operateType = (String) content.get(OPERATE_TYPE);
        Object formsonNumThreshold = content.get(FORMSON_NUM_THRESHOLD);
        if (CAPBusinessEnum.FormOperateType.BROWSE.getKey().equals(operateType) || formsonNumThreshold != null) {
            return result;
        }
        String rightId = (String) content.get(RIGHT_ID);
        String moduleId = (String) content.get(MODULE_ID);
        String moduleType = (String) content.get(MODULE_TYPE);
        String moduleTemplateId = (String) content.get(MODULE_TEMPLATE_ID);
        String contentTemplateId = (String) content.get(CONTENT_TEMPLATE_ID);
        String contentType = (String) content.get(CONTENT_TYPE);
        boolean isNew = cacheFormDataMasterBean.getExtraAttr("isNew") == null ? false : true;
        // 正文id
        String id = (String) content.get(ID);
        Integer saveType = Integer.valueOf((String) content.get(SAVE_TYPE));
        String isMerge = Strings.isNotBlank((String) content.get(IS_MERGE)) ? (String) content.get(IS_MERGE) : "0";
        Boolean needCheckRule = getParamBooleanValue(content.get(NEED_CHECK_RULE), true);
        Boolean needDataUnique = getParamBooleanValue(content.get(NEED_DATA_UNIQUE), true);
        Boolean needSn = getParamBooleanValue(content.get(NEED_SN), true);
        ModuleType mType = ModuleType.getEnumByKey(Integer.parseInt(moduleType));
        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(contentTemplateId));
        Long formMasterDataId = cacheFormDataMasterBean.getId();
        if (formBean == null) {
            result.error(CODE6, ResourceUtil.getString(MESSAGE6));
            return result;
        }
        // 将数据对象中的表单定义换成最新的，因为在编辑表单数据的过程中，表单定义可能修改了
        cacheFormDataMasterBean.refreshFormTable(formBean);
        // 1169 1775  put的isNew

        // 表单性能跟踪-表单保存
        String nodeName = String.valueOf(AppContext.getSessionContext(String.valueOf(cacheFormDataMasterBean.getId())));
        JMCallingObj jmCallingObj = null;
        try {
            BizConfigBean bizConfigBean = businessManager4.findBizConfigByFormId(formBean.getId());
            if (isNew) {
                jmCallingObj = jmMonitorService.putJMCall(Long.parseLong(contentTemplateId), FormRunTypeEnum.NEW, "CAPFormDataService.saveFormData", nodeName != null && !("null").equals(nodeName) ? nodeName : AppContext.currentUserName(),formBean,bizConfigBean);
            } else {
                jmCallingObj = jmMonitorService.putJMCall(Long.parseLong(contentTemplateId), FormRunTypeEnum.SAVE, "CAPFormDataService.saveFormData", nodeName != null && !("null").equals(nodeName) ? nodeName : AppContext.currentUserName(),formBean,bizConfigBean);
            }
            CtpContentAll ctpContentAll = null;

            if (null != bizConfigBean) {
                JMTrackUtils.getAndNew("跟踪SQL", "表单保存", CAP4MonitorUtil.contactTag(bizConfigBean), CAP4MonitorUtil.contactTag(formBean));
            } else {
                JMTrackUtils.getAndNew("跟踪SQL", "表单保存", CAP4MonitorUtil.contactTag(formBean));
            }
            ctpContentAll = this.ctpMainbodyManager.getContentById(Long.valueOf(id));
            Date nowDate = new Date();
            LockObjectBean lockObjectBean = capRuntimeDataLockManager.get(formMasterDataId);
            synchronized (lockObjectBean) {
                if (CAPBusinessEnum.FormSaveType.SAVE_AS_TEMPLATE.getType().equals(saveType) || isNew) {
                    //存个人模板 或者 新建
                    ctpContentAll = new CtpContentAll();
                    ctpContentAll.setIdIfNew();
                    ctpContentAll.setModuleType(Integer.valueOf(moduleType));
                    ctpContentAll.setModuleId(Long.valueOf(moduleId));
                    if (CAPBusinessEnum.FormSaveType.SAVE_AS_TEMPLATE.getType().equals(saveType)) {
                        isNew = false;
                        ctpContentAll.setModuleTemplateId(-1L);
                        ctpContentAll.setContentDataId(UUIDLong.longUUID());
                    } else {
                        if (mType == ModuleType.cap4UnflowForm) {
                            ctpContentAll.setModuleTemplateId(Long.valueOf(id));
                        } else if (mType == ModuleType.collaboration) {
                            ctpContentAll.setModuleTemplateId(Long.valueOf(moduleTemplateId));
                        }
                        ctpContentAll.setContentDataId(cacheFormDataMasterBean.getId());
                    }
                    ctpContentAll.setContentType(Integer.valueOf(contentType));
                    ctpContentAll.setContentTemplateId(Long.valueOf(contentTemplateId));

                    ctpContentAll.setModifyId(AppContext.currentUserId());
                    ctpContentAll.setModifyDate(nowDate);
                    ctpContentAll.setCreateId(AppContext.currentUserId());
                    ctpContentAll.setCreateDate(nowDate);
                    ctpContentAll.setSort(0);
                    cacheFormDataMasterBean.setStartDate(nowDate);
                    cacheFormDataMasterBean.setStartMemberId(AppContext.currentUserId());
                    cacheFormDataMasterBean.setModifyDate(nowDate);
                    cacheFormDataMasterBean.setModifyMemberId(AppContext.currentUserId());
                } else {
                    boolean formMasterExit = cap4FormDataDAO.isExist(String.valueOf(cacheFormDataMasterBean.getId()), formBean.getMasterTableBean().getTableName());
                    if (!formMasterExit && ctpContentAll == null) {
                        result.error(CODE11, ResourceUtil.getString(MESSAGE11));
                        return result;
                    }
                    cacheFormDataMasterBean.setModifyDate(nowDate);
                    cacheFormDataMasterBean.setModifyMemberId(AppContext.currentUserId());
                }

                FormAuthViewBean formAuthViewBean = CAPFormUtil.getFormAuthViewBean(formBean, cacheFormDataMasterBean, Long.valueOf(rightId));
                if (params != null) {
                    this.mergeFormData(cacheFormDataMasterBean, params);
                }
                // 先处理自动汇总、新增明细行，再去执行校验规则等
                Map<String, Object> autoSubDataMap = this.dealAutoCollectSubDatas(formBean, cacheFormDataMasterBean, formAuthViewBean, rightId);
                if (needCheckRule) {
                    //校验规则
                    String validateResult = this.dealCheckRule(formBean, cacheFormDataMasterBean, formAuthViewBean, isNew);
                    if (Strings.isNotBlank(validateResult)) {
                        result.error(CODE2, ResourceUtil.getString(MESSAGE2));
                        data.put("validateResult", validateResult);
                        data.put(TABLE_DATA, this.buildSaveMergeAutoData(formBean, cacheFormDataMasterBean, formAuthViewBean, autoSubDataMap));
                        data.put(CLEAR_TABLES, autoSubDataMap.get(CLEAR_TABLES));
                        this.buildExtendResult(formBean, cacheFormDataMasterBean, formAuthViewBean, result);
                        cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
                        return result;
                    }
                }
                if (needDataUnique) {
                    // 数据唯一性检查
                    Map<String, Object> checkResult = cap4FormDataManager.validateDataUnique(formBean, cacheFormDataMasterBean);
                    if (checkResult.size() == 2) {
                        result.error(CODE3, ResourceUtil.getString(MESSAGE3));
                        data.put("validateDataUnique", checkResult);
                        data.put(TABLE_DATA, this.buildSaveMergeAutoData(formBean, cacheFormDataMasterBean, formAuthViewBean, autoSubDataMap));
                        data.put(CLEAR_TABLES, autoSubDataMap.get(CLEAR_TABLES));
                        this.buildExtendResult(formBean, cacheFormDataMasterBean, formAuthViewBean, result);
                        cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
                        return result;
                    }
                }
                if (CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey().equals(isMerge)) {
                    if (mType == ModuleType.collaboration) {
                        // 如果是预提交，把content放入缓存中，流程提交时从缓存里面取值
                        cacheFormDataMasterBean.getExtraMap().put(CONTENT, content);
                        if (null != params) {
                            cacheFormDataMasterBean.getExtraMap().put(CUSTOM_FIELDS, params.get(CUSTOM_FIELDS));
                        }
                        data.put(CONTENT, JSONUtil.parseJSONString(ctpContentAll.toJSON()));
                    }
                    data.put(TABLE_DATA, this.buildSaveMergeAutoData(formBean, cacheFormDataMasterBean, formAuthViewBean, autoSubDataMap));
                    data.put(CLEAR_TABLES, autoSubDataMap.get(CLEAR_TABLES));
                    this.buildExtendResult(formBean, cacheFormDataMasterBean, formAuthViewBean, result);
                    cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
                    return result;
                }
                boolean typeNotDealSn = CAPBusinessEnum.FormSaveType.SAVE_AS.getType().equals(saveType) || CAPBusinessEnum.FormSaveType.SAVE_AS_TEMPLATE.getType().equals(saveType);
                if (needSn && !typeNotDealSn) {
                    //是否需要生成流水号
                    List snInfos = dealSn(formBean, formAuthViewBean, cacheFormDataMasterBean);
                    data.put("snInfo", snInfos);
                }
                // 更新关联记录
                List<CAPFormRelationRecord> relationRecords = cacheFormDataMasterBean.getRelationRecords();
                cap4FormRelationActionManager.saveOrUpdateCAPFormRelationRecords(cacheFormDataMasterBean.getId(), relationRecords);
                if (CAPBusinessEnum.FormSaveType.SAVE_AS_TEMPLATE.getType().equals(saveType)) {
                    // 保存个人模板 TODO formDataMasterBean 由前端提交的数据bean转成了cache里面的bean，待验证存个人模板会不会有其他问题
                    ctpContentAll.setContent(cacheFormDataMasterBean.getDataJsonString());
                    data.put(CONTENT, JSONUtil.parseJSONString(ctpContentAll.toJSON()));
                    try {
                        CtpDynamicDataSource.setDataSourceKey(DataSourceName.BASE.getSource());
                        ctpMainbodyManager.saveOrUpdateContentAll(ctpContentAll);
                    } finally {
                        CtpDynamicDataSource.clearDataSourceKey();
                    }
                    return result;
                }
                cacheFormDataMasterBean.setModifyDate(nowDate);
                if (isNew) {
                    //仅新建的时候才需要设置数据的状态，非新建的数据的state都是根据后续的逻辑需要单独处理的
                    if (mType == ModuleType.cap4UnflowForm) {
                        cacheFormDataMasterBean.setState(Enums.FormDataStateEnum.FLOW_UNAUDITED.getKey());
                    } else if (mType == ModuleType.collaboration) {
                        cacheFormDataMasterBean.setState(Enums.FormDataStateEnum.FLOW_UNOFFICIAL.getKey());
                    }
                }

                FormDataMasterBean oldFormDataMasterBean = null;
                if (mType == ModuleType.cap4UnflowForm) {
                    oldFormDataMasterBean = capFormDataCacheManager.get4Data(cacheFormDataMasterBean.getId(), formBean.getId());
                }
                FormDataBeforeSubmitEvent formDataBeforeSubmitEvent = new FormDataBeforeSubmitEvent(cacheFormDataMasterBean);
                // 触发表单数据入库前的事件
                EventDispatcher.fireEvent(formDataBeforeSubmitEvent);
                // 处理自定义控件的保存
                Map<String, Object> customFields = params != null ? (Map<String, Object>) params.get(CUSTOM_FIELDS) : (Map<String, Object>) cacheFormDataMasterBean.getExtraAttr(CUSTOM_FIELDS);
                doSaveCustomCtrl(customFields, moduleId, formBean, cacheFormDataMasterBean);
                CAPFormDataLogUtil.recordTrace(cacheFormDataMasterBean, "保存入库前");
                cap4FormManager.saveOrUpdateFormData(cacheFormDataMasterBean, formBean.getId(), true);
                try {
                    CtpDynamicDataSource.setDataSourceKey(DataSourceName.BASE.getSource());
                    ctpMainbodyManager.saveOrUpdateContentAll(ctpContentAll);
                } finally {
                    CtpDynamicDataSource.clearDataSourceKey();
                }
                this.saveAttachments(formBean, cacheFormDataMasterBean, moduleId, mType);
                // 缓存sql跟踪声明以及移除由aop CAPFormDataAspect 实现
                LOGGER.info("数据保存结束:" + cacheFormDataMasterBean.getId() + " 人员:" + AppContext.currentUserId());
                FormDataAfterSubmitEvent formDataAfterSubmitEvent = new FormDataAfterSubmitEvent(cacheFormDataMasterBean);
                // 触发表单数据入库后的事件
                EventDispatcher.fireEvent(formDataAfterSubmitEvent);

                if (!CAPBusinessEnum.FormSaveType.SAVE_CURRENT.getType().equals(saveType)) {
                    cacheFormDataMasterBean.setRelationRecords(null);
                }
                if (mType == ModuleType.cap4UnflowForm) {
                    if (AppContext.hasPlugin("index")) {
                        //处理CAP4无流程全文检索
                        IndexApi indexApi = (IndexApi) AppContext.getBean("indexApi");
                        indexApi.update(ctpContentAll.getModuleId(), ApplicationCategoryEnum.form.getKey());
                    }
                    //处理无流程日志
                    String dataLog = null;
                    int capLogType = Enums.CapLogType.INSERT.getKey();
                    if (isNew) {
                        dataLog = String.format(ResourceUtil.getString(CREATE_LOG), formBean.getFormName()) + capRunningLogManager.getDataLog(formBean, cacheFormDataMasterBean, MainbodyStatus.STATUS_POST_SAVE);
                    } else {
                        capLogType = Enums.CapLogType.MODIFY.getKey();
                        dataLog = String.format(ResourceUtil.getString(UPDATE_LOG), formBean.getFormName()) + capRunningLogManager.getDataLog(formBean, cacheFormDataMasterBean, MainbodyStatus.STATUS_POST_UPDATE, oldFormDataMasterBean);
                    }
                    Long bizId = bizConfigBean != null ? bizConfigBean.getId() : null;
                    String bizName = bizConfigBean != null ? bizConfigBean.getName() : null;
                    capRunningLogManager.saveRunningLog(bizId, bizName, formBean.getId(), formBean.getFormName(), cacheFormDataMasterBean.getId(), capLogType, dataLog, AppContext.currentUserId(),
                            cacheFormDataMasterBean.getStartMemberId(), cacheFormDataMasterBean.getStartDate(), AppContext.currentAccountName(), AppContext.getRemoteAddr());
                    if (!CAPBusinessEnum.FormSaveType.SAVE_CURRENT.getType().equals(saveType)) {
                        cap4FormManager.removeSessionMasterDataBean(ctpContentAll.getContentDataId());
                    }
                    cap4FormTriggerManager.doTrigger(Integer.parseInt(moduleType), cacheFormDataMasterBean.getId(), formBean.getId(), rightId, null, "dataSave");
                    if (CAPBusinessEnum.FormSaveType.SAVE_AND_COPY.getType().equals(saveType)) {
                        FormDataMasterBean cloneFormDataMasterBean = this.copyFormDataMasterBean(formBean, cacheFormDataMasterBean, formAuthViewBean, relationRecords);
                        data.put("formMasterId", String.valueOf(cloneFormDataMasterBean.getId()));
                    }
                } else if (mType == ModuleType.collaboration) {
                    data.put(CONTENT, JSONUtil.parseJSONString(ctpContentAll.toJSON()));
                    cacheFormDataMasterBean.removeExtraMap(CONTENT);
                    cacheFormDataMasterBean.removeExtraMap(CUSTOM_FIELDS);
                }
            }
            LOGGER.info("save form data finish, id:" + ctpContentAll.getContentDataId() + ", moduleId:" + moduleId);
            JMTrackUtils.clearTrack();
        } finally {
            if (jmCallingObj != null) {
                jmMonitorService.endJMCall(jmCallingObj);
            }
        }
        return result;
    }

    /**
     * 保存并复制
     */
    private FormDataMasterBean copyFormDataMasterBean(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean, FormAuthViewBean formAuthViewBean, List<CAPFormRelationRecord> capFormRelationRecords) throws BusinessException, CloneNotSupportedException {
        // 保存并复制将cloneBean重新放入session
        // 保存并复制需要移除session里面的缓存附件，适配轻表单和表单切换
        cacheFormDataMasterBean.clearSessionAttachments();
        FormDataMasterBean cloneFormDataMasterBean = (FormDataMasterBean) cacheFormDataMasterBean.clone();
        cloneFormDataMasterBean.putExtraAttr("isNew", true);
        cloneFormDataMasterBean.setNewId();
        cap4FormManager.putSessioMasterDataBean(formBean, cloneFormDataMasterBean, true, false);
        List<FormFieldBean> masterFields = formBean.getMasterTableBean().getFields();
        for (FormFieldBean formFieldBean : masterFields) {
            /**
             * 自定义控件不支持拷贝
             * */
            if (formFieldBean.isCustomerCtrl()) {
                cloneFormDataMasterBean.addFieldValue(formFieldBean.getName(), null);
                continue;
            }
            /**
             * 处理附件和地理位置
             * */
            if (formFieldBean.isAttachment() || formFieldBean.isMap()) {
                Object fieldValue = cacheFormDataMasterBean.getFieldValue(formFieldBean.getName());
                if (fieldValue != null) {
                    Long subReference = Long.parseLong(String.valueOf(fieldValue));
                    Long newSubReference = null;
                    if (formFieldBean.isMap()) {
                        //拍照定位的newSubReference下面附件要使用
                        newSubReference = CAPFormUtil.copyLBS(formBean.getId(), cloneFormDataMasterBean.getId(), null, subReference, formFieldBean);
                    }
                    if (formFieldBean.isAttachment()) {
                        newSubReference = newSubReference == null ? UUIDLong.longUUID() : newSubReference;
                        newSubReference = CAPAttachmentUtil.copyAttachment(cloneFormDataMasterBean, formFieldBean, subReference, cloneFormDataMasterBean.getId(), newSubReference, false);
                    }
                    cloneFormDataMasterBean.addFieldValue(formFieldBean.getName(), newSubReference);
                }
            }
            FormAuthViewFieldBean fieldAuth = formAuthViewBean.getFormAuthorizationField(formFieldBean.getName());
            if (fieldAuth.isSerialNumberDefaultValue()) {
                // 将克隆的流水号字段置为空
                cloneFormDataMasterBean.addFieldValue(fieldAuth.getFieldName(), null);
            }
        }
        Map<String, String> oldSubId2NewId = new HashMap<String, String>();
        List<FormTableBean> subTableBeans = formBean.getSubTableBean();
        for (FormTableBean formTableBean : subTableBeans) {
            List<FormDataSubBean> subDatas = cloneFormDataMasterBean.getSubData(formTableBean.getTableName());
            if (subDatas == null) {
                continue;
            }
            List<FormFieldBean> subFields = formTableBean.getFields();
            List<FormDataSubBean> newSubDatas = new ArrayList<FormDataSubBean>();
            for (FormDataSubBean oldBean : subDatas) {
                Long oldId = oldBean.getId();
                FormDataSubBean formDataSubBean = new FormDataSubBean(oldBean.getRowData(), formTableBean, cloneFormDataMasterBean, true);
                formDataSubBean.setNewId();
                oldSubId2NewId.put(oldId.toString(), formDataSubBean.getId().toString());
                for (FormFieldBean formFieldBean : subFields) {
                    if (formFieldBean.isCustomerCtrl()) {
                        formDataSubBean.addFieldValue(formFieldBean.getName(), null);
                        continue;
                    }
                    if (formFieldBean.isAttachment() || formFieldBean.isMap()) {
                        Object fieldValue = formDataSubBean.getFieldValue(formFieldBean.getName());
                        if (fieldValue != null) {
                            Long subReference = Long.parseLong(String.valueOf(fieldValue));
                            Long newSubReference = null;
                            if (formFieldBean.isMap()) {
                                //拍照定位的newSubReference下面附件要使用
                                newSubReference = CAPFormUtil.copyLBS(formBean.getId(), cloneFormDataMasterBean.getId(), formDataSubBean.getId(), subReference, formFieldBean);
                            }
                            if (formFieldBean.isAttachment()) {
                                newSubReference = newSubReference == null ? UUIDLong.longUUID() : newSubReference;
                                newSubReference = CAPAttachmentUtil.copyAttachment(cloneFormDataMasterBean, formFieldBean, subReference, cloneFormDataMasterBean.getId(), newSubReference, false);
                            }
                            formDataSubBean.addFieldValue(formFieldBean.getName(), newSubReference);
                        }
                    }
                }
                newSubDatas.add(formDataSubBean);
            }
            cloneFormDataMasterBean.setSubData(formTableBean.getTableName(), newSubDatas);
        }
        if (Strings.isNotEmpty(capFormRelationRecords)) {
            for (CAPFormRelationRecord capFormRelationRecord : capFormRelationRecords) {
                capFormRelationRecord.setNewId();
                capFormRelationRecord.setFromMasterDataId(cloneFormDataMasterBean.getId());
                if (capFormRelationRecord.getFromSubdataId() != null && oldSubId2NewId.get(capFormRelationRecord.getFromSubdataId().toString()) != null) {
                    capFormRelationRecord.setFromSubdataId(Long.valueOf(oldSubId2NewId.get(capFormRelationRecord.getFromSubdataId().toString())));
                }
            }
        }
        cloneFormDataMasterBean.setRelationRecords(capFormRelationRecords);
        cloneFormDataMasterBean.initRelationRecordMap();
        return cloneFormDataMasterBean;
    }

    /**
     * 处理自定义控件的保存
     */
    private void doSaveCustomCtrl(Map<String, Object> customFields, String moduleId, FormBean formBean, FormDataMasterBean formDataMasterBean) {
        List<FormFieldBean> allCustomFields = formBean.getCustomFields();
        try {
            if (null == customFields) {
                for (FormFieldBean formFieldBean : allCustomFields) {
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("formFieldBean", formFieldBean);
                    param.put("formDataMasterBean", formDataMasterBean);
                    param.put("formBean", formBean);
                    param.put("moduleId", Long.valueOf(moduleId));
                    ((FormFieldCustomCtrl) formFieldBean.getFieldCtrl()).handleSaving(param);
                }
            } else {
                //有customFields参数的保存需要特殊处理
                //先执行customFields参数中所含自定义控件的保存
                List<FormFieldBean> temp = new ArrayList<FormFieldBean>();
                for (String key : customFields.keySet()) {
                    String fieldName = key.split(FormConstant.DOWNLINE)[0];
                    FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
                    Object object = customFields.get(key);
                    if (object != null && object instanceof Map) {
                        Map<String, Object> customParam = (Map<String, Object>) object;
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("fieldName", fieldName);
                        param.put("formDataMasterBean", formDataMasterBean);
                        customParam.put("param", param);
                        ((FormFieldCustomCtrl) formFieldBean.getFieldCtrl()).handleSaving(customParam);
                        temp.add(formFieldBean);
                    }
                }
                //然后执行customFields参数中未包含自定义控件的保存
                if (temp.size() < allCustomFields.size()) {
                    allCustomFields.removeAll(temp);
                    for (FormFieldBean formFieldBean : allCustomFields) {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("formFieldBean", formFieldBean);
                        param.put("formDataMasterBean", formDataMasterBean);
                        param.put("formBean", formBean);
                        param.put("moduleId", Long.valueOf(moduleId));
                        ((FormFieldCustomCtrl) formFieldBean.getFieldCtrl()).handleSaving(param);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("failed to save custom controls! cause:" + e.getMessage(), e);
        }
    }

    /**
     * 处理自动汇总数据
     */
    private Map<String, Object> dealAutoCollectSubDatas(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean, FormAuthViewBean formAuthViewBean, String rightId) throws BusinessException {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, List<FormDataSubBean>> autoCollectSubMaps = new HashMap<String, List<FormDataSubBean>>();
        List<FormRelationshipMapBean> autoCollectBeans = cap4FormRelationActionManager.getValidFormRelationshipMapBeansByType(formBean, FormRelationEnums.ViewSelectType.auto_collect.getKey());
        Map<String, Set<String>> tableName2DeleteIds = new HashMap<String, Set<String>>();
        if (Strings.isNotEmpty(autoCollectBeans)) {
            List<FormRelationshipMapBean> needAutoCollectBeans = new LinkedList<FormRelationshipMapBean>();
            for (FormRelationshipMapBean mapBean : autoCollectBeans) {
                if (mapBean.isForceRefresh()) {
                    if (mapBean.getRefreshOperationUpdate() != null && mapBean.getRefreshOperationUpdate().contains(rightId)) {
                        needAutoCollectBeans.add(mapBean);
                    } else if (mapBean.getRefreshOperationAdd() != null && mapBean.getRefreshOperationAdd().contains(rightId)) {
                        needAutoCollectBeans.add(mapBean);
                    }
                }
            }
            // 20190611 V7.1 SP1 取消按照修改时间排正序逻辑，统一使用创建时间排序逻辑。
            // 因为以前的是关系是无序状态，本次放开多字段后统一按创建时间。
            // 20190618 按照产品设计，还原为以前的代码逻辑，按修改时间排序
            Collections.sort(needAutoCollectBeans, new Comparator<FormRelationshipMapBean>() {
                @Override
                public int compare(FormRelationshipMapBean o1, FormRelationshipMapBean o2) {
                    return o1.getRelationshipBean().getModifyTime().compareTo(o2.getRelationshipBean().getModifyTime());
                }
            });

            // 结果表
            List<String> resultTables = new ArrayList<String>();
            List<FormRelationshipMapBean> noResultAutoCollectBeans = new ArrayList<FormRelationshipMapBean>();
            List<String> dealedTables = new ArrayList<String>();
            for (FormRelationshipMapBean mapBean : needAutoCollectBeans) {
                for (Map.Entry<String, String> entry : mapBean.getCollectMapping().entrySet()) {
                    // 此map只有一组直映射关系
                    resultTables.add(entry.getValue());
                }
            }
            for (FormRelationshipMapBean mapBean : needAutoCollectBeans) {
                for (Map.Entry<String, String> entry : mapBean.getCollectMapping().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (resultTables.contains(key)) {
                        noResultAutoCollectBeans.add(mapBean);
                    } else {
                        // 不存在依赖关系的自动汇总
                        Map<String, List<FormDataSubBean>> data = cap4FormRelationActionManager.getRelationAutoSummaryFromSelf(formBean, cacheFormDataMasterBean, formAuthViewBean, mapBean, tableName2DeleteIds);
                        capRuntimeCalcManager.execCalcAll(formBean, cacheFormDataMasterBean, formAuthViewBean, CAPFormUtil.getTableName2FormSubDataBeanIds(data));
                        dealedTables.add(value);
                        autoCollectSubMaps.putAll(data);
                    }
                }
            }
            /*执行存在依赖关系的自动汇总  a-->b  b-->c  c-->d
            * 若是当执行到b-->c需要得到b的汇总结果
            * */
            while(noResultAutoCollectBeans.size() > 0){
                Iterator<FormRelationshipMapBean> iterator = noResultAutoCollectBeans.iterator();
                while (iterator.hasNext()) {
                    FormRelationshipMapBean mapBean = iterator.next();
                    for (Map.Entry<String, String> entry : mapBean.getCollectMapping().entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (dealedTables.contains(key)) {
                            Map<String, List<FormDataSubBean>> data = cap4FormRelationActionManager.getRelationAutoSummaryFromSelf(formBean, cacheFormDataMasterBean, formAuthViewBean, mapBean, tableName2DeleteIds);
                            capRuntimeCalcManager.execCalcAll(formBean, cacheFormDataMasterBean, formAuthViewBean, CAPFormUtil.getTableName2FormSubDataBeanIds(data));
                            dealedTables.add(value);
                            autoCollectSubMaps.putAll(data);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        if (autoCollectSubMaps.size() > 0) {
            Set<String> fillBackFields = capRuntimeCalcManager.execCalcAll(formBean, cacheFormDataMasterBean, formAuthViewBean, CAPFormUtil.getTableName2FormSubDataBeanIds(autoCollectSubMaps));
            Iterator<String> iterator = autoCollectSubMaps.keySet().iterator();
            while (iterator.hasNext()) {
                String tableName = iterator.next();
                autoCollectSubMaps.put(tableName, cacheFormDataMasterBean.getSubData(tableName));
            }
            LOGGER.info("save do autoSubData. calc change filed " + fillBackFields.toString() + " , dataId " + cacheFormDataMasterBean.getId());
            result.put(TABLE_NAME_TO_ADD_SUB_OBJECTS, autoCollectSubMaps);
            result.put(FILL_BACK_FIELDS, fillBackFields);
            result.put(TABLE_NAME_TO_DELETE_IDS, tableName2DeleteIds);
        }
        return result;
    }

    /**
     * 当为合并缓存，或者校验规则不通过的时候，自动汇总、新建明细行的数据需要返回给前端
     */
    private Map<String, Map<String, Object>> buildSaveMergeAutoData(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean, FormAuthViewBean formAuthViewBean, Map<String, Object> data) throws BusinessException {
        Map<String, Map<String, Object>> tableName2DataMaps = new HashMap<String, Map<String, Object>>();
        if (data == null) {
            return tableName2DataMaps;
        }
        Set<String> fillBackFields = (Set) data.get(FILL_BACK_FIELDS);
        if (fillBackFields == null) {
            fillBackFields = new HashSet<String>();
        }
        Map<String, List<FormDataSubBean>> tableName2AddBeans = (Map) data.get(TABLE_NAME_TO_ADD_SUB_OBJECTS);
        if (tableName2AddBeans != null && !tableName2AddBeans.isEmpty()) {
            // tableName2AddSubObjects 是自动汇总、新增明细行之后的表，所以数据前端需要清空
            data.put(CLEAR_TABLES, tableName2AddBeans.keySet());
        }
        capRuntimeCalcManager.buildCalcAllResult(formBean, cacheFormDataMasterBean, formAuthViewBean, tableName2AddBeans, fillBackFields, CAPBusinessEnum.SubBeanNewFrom.SAVE_AUTO_COLLECT, tableName2DataMaps);
        this.mergeAutoDelete2Result((Map) data.get(TABLE_NAME_TO_DELETE_IDS), tableName2DataMaps);
        return tableName2DataMaps;
    }

    /**
     * 需要忽略的附件信息
     * @param formBean
     * @param cacheFormDataMasterBean
     * @return
     */
    private List<Long> getIgnoreSubReferences(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean) {
        List<Long> ignoreSubReferences = new ArrayList<Long>();
        List<FormFieldBean> wordFields = formApi4Cap4.getAllWordInjectionCtrls(formBean.getId());
        if (CollectionUtils.isEmpty(wordFields)) {
            return ignoreSubReferences;
        }
        Set<String> wordInjectionCtrlSet = (Set<String>) cacheFormDataMasterBean.getExtraMap().get("wordInjectionCtrl");
        if (CollectionUtils.isNotEmpty(wordInjectionCtrlSet)) {
            Iterator<FormFieldBean> iterator = wordFields.iterator();
            while (iterator.hasNext()) {
                FormFieldBean fieldBean = iterator.next();
                if (wordInjectionCtrlSet.contains(fieldBean.getName())) {
                    iterator.remove();
                }
            }
        }
        for (FormFieldBean formFieldBean : wordFields) {
            if (cacheFormDataMasterBean.getFieldValue(formFieldBean.getName()) != null) {
                ignoreSubReferences.add(Long.parseLong(String.valueOf(cacheFormDataMasterBean.getFieldValue(formFieldBean.getName()))));
            }
        }
        return ignoreSubReferences;
    }

    /**
     * 保存表单的时候 保存附件
     */
    private void saveAttachments(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean, String moduleId, ModuleType mType) {
        // 数据库中已存在的附件的id
        Set<Long> dbExitsAttIds = new HashSet<Long>();
        // 已经存在的最大的sort值
        Map<Long, Integer> exitMaxSortMap = new HashMap<Long, Integer>();
        Long realModuleId = this.getAttachmentRealReference(mType, Long.valueOf(moduleId));
        List<Attachment> dbAttachments = attachmentManager.getByReference(realModuleId);
        List<Long> ignoreSubReferences = this.getIgnoreSubReferences(formBean, cacheFormDataMasterBean);
        if (Strings.isNotEmpty(dbAttachments)) {
            for (Attachment attachment : dbAttachments) {
                // 只处理分类为cap4Form的数据 并且是当前视图下的
                if (attachment.getCategory().intValue() == ApplicationCategoryEnum.cap4Form.key() && attachment.getSubReference() != null) {
                    if (!ignoreSubReferences.contains(attachment.getSubReference())) {
                        dbExitsAttIds.add(attachment.getId());
                    }
                    Integer exitMaxSort = exitMaxSortMap.get(attachment.getSubReference());
                    exitMaxSort = exitMaxSort == null ? 0 : exitMaxSort;
                    exitMaxSort = exitMaxSort < attachment.getSort() ? attachment.getSort() : exitMaxSort;
                    exitMaxSortMap.put(attachment.getSubReference(), exitMaxSort);
                }
            }
        }
        // session 中包括db中已经存在的数据
        List<Attachment> sessionAttachments = cacheFormDataMasterBean.getSessionAttachments(null);
        // 等待添加的附件
        List<Attachment> insertAttachments = new ArrayList<Attachment>();
        if (Strings.isNotEmpty(sessionAttachments)) {
            int sortAll = 0;
            for (Attachment attachment : sessionAttachments) {
                if (ignoreSubReferences.contains(attachment.getSubReference())) {
                    // 表单转文档，如果没有修改的数据，则剔除不需要入库。上面已处理不删除
                    continue;
                }
                // 因为新建的时候有流程给的moduleId其实不正确，需要重置替换
                if (mType == ModuleType.collaboration) {
                    attachment.setReference(realModuleId);
                }
                if (exitMaxSortMap.get(attachment.getSubReference()) != null) {
                    int sort = exitMaxSortMap.get(attachment.getSubReference()) + 1;
                    attachment.setSort(sort);
                    exitMaxSortMap.put(attachment.getSubReference(), sort);
                } else {
                    attachment.setSort(sortAll++);
                }
                insertAttachments.add(attachment);
            }
        }
        // 结束处理session里面的附件信息
        if (dbExitsAttIds.size() > 0) {
            List<Long> list = new ArrayList<Long>();
            list.addAll(dbExitsAttIds);
            attachmentManager.deleteByIds(list);
        }
        if (insertAttachments.size() > 0) {
            for (Attachment attachment : insertAttachments) {
                attachment.setNewId();
            }
            attachmentManager.create(insertAttachments, false);
        }
    }

    /**
     * 判断当前表单是否存在有预提的更新记录设置，主要用于后面批处理或者权限控制的时候
     * @return boolean : true--有预提；false--没有预提
     */
    private boolean hasWithholdingAction(FormBean formBean) {
        List<Long> formTriggerIdList = formBean.getFormTriggerIdList();
        if (Strings.isNotEmpty(formTriggerIdList)) {
            for (Long triggerId : formTriggerIdList) {
                FormTriggerBean triggerBean = cap4FormCacheManager.getFormTriggerBean(triggerId);
                if (triggerBean != null && triggerBean.hasWithholdingAction()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 校验规则
     * */
    private String dealCheckRule(FormBean formBean, FormDataMasterBean cacheFormDataMasterBean, FormAuthViewBean formAuthViewBean, boolean isNew) throws CloneNotSupportedException, BusinessException {
        FormDataMasterBean checkRuleFormDataMasterBean = null;
        if ((isNew || cacheFormDataMasterBean.getState() == 0) && formBean.isFlowForm() && this.hasWithholdingAction(formBean)) {
            Set<FormRelationshipBean> formRelationshipBeans = cap4FormCacheManager.getFormRelationshipBeanListByFormId(formBean.getId());
            for (FormRelationshipBean formRelationshipBean : formRelationshipBeans) {
                // 校验规则前做一次系统关联
                if (formRelationshipBean != null && formRelationshipBean.getRelationType() == FormRelationEnums.ViewSelectType.system.getKey()) {
                    if (checkRuleFormDataMasterBean == null) {
                        checkRuleFormDataMasterBean = (FormDataMasterBean) cacheFormDataMasterBean.clone();
                    }
                    List<FormRelationshipMapBean> formRelationshipMapBeans = formRelationshipBean.getRelationMapList();
                    for (FormRelationshipMapBean formRelationshipMapBean : formRelationshipMapBeans) {
                        Map<String, String> conditionFields = cap4FormRelationActionManager.getRelationFormulaFieldMap(formRelationshipMapBean.getConditionFormula());
                        List<FormFieldBean> masterFieldBeans = new ArrayList<FormFieldBean>();
                        List<FormFieldBean> subFieldBeans = new ArrayList<FormFieldBean>();
                        Iterator<String> iterator = conditionFields.keySet().iterator();
                        while (iterator.hasNext()) {
                            String fieldName = iterator.next();
                            FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
                            if (formFieldBean.isMasterField()) {
                                masterFieldBeans.add(formFieldBean);
                            } else {
                                subFieldBeans.add(formFieldBean);
                            }
                        }
                        // 如果是主从混合条件，则不去进行主表的计算
                        if (masterFieldBeans.size() > 0 && subFieldBeans.isEmpty()) {
                            cap4FormRelationActionManager.dealSysRelationByShipBean4FieldIn(formBean, checkRuleFormDataMasterBean, formAuthViewBean, null, null, formRelationshipBean);
                        }
                        if (!subFieldBeans.isEmpty()) {
                            FormFieldBean formFieldBean = subFieldBeans.get(0);
                            List<FormDataSubBean> formDataSubBeans = checkRuleFormDataMasterBean.getSubData(formFieldBean.getOwnerTableName());
                            for (FormDataSubBean formDataSubBean : formDataSubBeans) {
                                cap4FormRelationActionManager.dealSysRelationByShipBean4FieldIn(formBean, checkRuleFormDataMasterBean, formAuthViewBean, formDataSubBean, null, formRelationshipBean);
                            }
                        }
                    }
                }
            }
        }
        String validateResult = cap4FormManager.validate(formBean.getId(), checkRuleFormDataMasterBean == null ? cacheFormDataMasterBean : checkRuleFormDataMasterBean);
        return validateResult;
    }

    /**
     * 处理流水号
     */
    private List dealSn(FormBean formBean, FormAuthViewBean formAuth, FormDataMasterBean cacheMasterData) throws BusinessException {
        List snInfos = new ArrayList();
        List<FormFieldBean> masterFields = formBean.getMasterTableBean().getFields();
        for (FormFieldBean formFieldBean : masterFields) {
            FormAuthViewFieldBean fieldAuth = formAuth.getFormAuthorizationField(formFieldBean.getName());
            // 2018-3-2 隐藏权限下要生成流水号
            if (fieldAuth.isSerialNumberDefaultValue()) {
                String val = (String) cacheMasterData.getFieldValue(fieldAuth.getFieldName());
                if (Strings.isBlank(val)) {
                    //流水号字段生成一次，因此为空才生成
                    FormFieldBean authSnField = formBean.getFieldBeanByName(fieldAuth.getFieldName());
                    String snmu = cap4SerialNumberManager.getSerialNumber(fieldAuth);
                    if (Strings.isNotBlank(snmu)) {
                        if (FormFieldComEnum.TEXTAREA != authSnField.getInputTypeEnum() && snmu.length() > authSnField.getMaxLength(false)) {
                            cacheMasterData.addFieldValue(fieldAuth.getFieldName(), snmu.substring(0, authSnField.getMaxLength(true)));
                        } else {
                            cacheMasterData.addFieldValue(fieldAuth.getFieldName(), snmu);
                        }
                        Map<String, Object> snInfo = new HashMap<String, Object>();
                        snInfo.put("snmu", snmu);
                        snInfo.put("display", formFieldBean.getDisplay());
                        snInfos.add(snInfo);
                    }
                    cap4FormDataManager.calcAllWithFieldIn(formBean, authSnField, cacheMasterData, null, null, formAuth, false);
                }
            }
        }
        CAPFormUtil.processSerial4Formula(cacheMasterData, formBean, formAuth);
        return snInfos;
    }

    /**
     * 初始化复制的明细行
     */
    private List<FormDataSubBean> initCopyAddSubDataBean(FormDataMasterBean formDataMasterBean, FormAuthViewBean formAuthViewBean, FormTableBean formTableBean, List<String> recordIds) throws BusinessException {
        List<FormDataSubBean> newFormDataSubBeans = new ArrayList<FormDataSubBean>();
        Map<String, CAPFormRelationRecord> capFormRelationRecordMap = null;
        List<CAPFormRelationRecord> capFormRelationRecords = formDataMasterBean.getRelationRecords();
        if (CollectionUtils.isNotEmpty(capFormRelationRecords)) {
            capFormRelationRecordMap = formDataMasterBean.getRelationRecordMap();
        }
        List<CAPFormRelationRecord> addRelationRecords = new ArrayList<CAPFormRelationRecord>();
        for (String recordId : recordIds) {
            FormDataSubBean formDataSubBean = new FormDataSubBean(formTableBean, formDataMasterBean, true);
            formDataSubBean.setFormmainId(formDataMasterBean.getId());
            newFormDataSubBeans.add(formDataSubBean);
            formDataMasterBean.addSubData4CopyRow(formTableBean.getTableName(), formDataSubBean, Long.valueOf(recordId));
            if (Strings.isNotEmpty(capFormRelationRecords)) {
                // 复制关联信息
                for (CAPFormRelationRecord relationRecord : capFormRelationRecords) {
                    if (relationRecord.getFromSubdataId() != null && relationRecord.getFromSubdataId().toString().equals(recordId)) {
                        try {
                            CAPFormRelationRecord nrr = (CAPFormRelationRecord) relationRecord.clone();
                            nrr.setNewId();
                            nrr.setFromSubdataId(formDataSubBean.getId());
                            addRelationRecords.add(nrr);
                            capFormRelationRecordMap.put(relationRecord.getFieldName() + FormConstant.DOWNLINE + formDataSubBean.getId(), nrr);
                        } catch (CloneNotSupportedException e) {
                            throw new BusinessException(e);
                        }
                    }
                }
            }
        }
        if (!addRelationRecords.isEmpty()) {
            // 如果addRelationRecords有值，那么capFormRelationRecords必然不为空
            capFormRelationRecords.addAll(addRelationRecords);
        }
        return this.initAddSubDataBeans(formDataMasterBean, formAuthViewBean, formTableBean, newFormDataSubBeans);
    }

    /**
     * 初始化手动触发新的明细行
     */
    private List<FormDataSubBean> initNewAddSubDataBean(FormDataMasterBean formDataMasterBean, FormAuthViewBean formAuthViewBean, FormTableBean formTableBean, List<String> recordIds, Long preRecordId) throws BusinessException {
        List<FormDataSubBean> newFormDataSubBeans = new ArrayList<FormDataSubBean>();
        Long preId = preRecordId != null ? preRecordId : null;
        for (String recordId : recordIds) {
            FormDataSubBean formDataSubBean = new FormDataSubBean(formTableBean, formDataMasterBean, true);
            formDataSubBean.setFormmainId(formDataMasterBean.getId());
            newFormDataSubBeans.add(formDataSubBean);
            if (preId != null) {
                // 如果前一行preId不为空，则在此行后面插入数据
                if (preId.equals(-1L)) {
                    // 后面有激活行，但是激活了第一行
                    formDataMasterBean.addSubDataWithIndex(formTableBean.getTableName(), formDataSubBean, 0);
                } else {
                    formDataMasterBean.addSubData(formTableBean.getTableName(), formDataSubBean, preId);
                }
            } else {
                formDataMasterBean.addSubData(formTableBean.getTableName(), formDataSubBean);
            }
            preId = formDataSubBean.getId();
        }
        return this.initAddSubDataBeans(formDataMasterBean, formAuthViewBean, formTableBean, newFormDataSubBeans);
    }

    /**
     * 明细表删除与增加
     * @param params <pre>
     *                formMasterId
     *                formId
     *                rightId
     *                tableName 操作的明细表表名
     *                type add/delete/copy
     *                firstRecordId 可选，重复表第一行数据，用于判定是否空值，适用于关联自动回填或者插入多条时，需要删除第一行空行数据
     *                preRecordId 可选，当type为add时，需要传入插入行的位置
     *                recordIds List。type为add，并且是关联自动回填此list有值，否则为空ID；
     *                                type为copy，值代表copy源数据；
     *                                type为delete时，值代表需要删除的源数据
     *                relationParams 可选，关联自动回填时的对应值，用于处理当控件为浏览或者隐藏、以及视图没有字段时的值处理到缓存中。
     *              </pre>
     * @return
     */
    /**
     * 添加明细表（包括计算）
     */
    @ValidateRequestParam(notEmpty = {"recordIds"}, validateCacheBeanKey = "formMasterId")
    @JMCalling4Service(runType = FormRunTypeEnum.SAVE)
    public FormDataSubRowOperationResult<Map<String, Object>> addOrDelDataSubBean(FormDataSubRowOperationParamBean paramBean) {
        FormDataSubRowOperationResult<Map<String, Object>> result = new FormDataSubRowOperationResult<Map<String, Object>>();
        String tableName = paramBean.getTableName();
        String type = paramBean.getType();
        String firstRecordId = paramBean.getFirstRecordId();
        String preRecordId = paramBean.getPreRecordId();
        try {
            FormBean formBean = cap4FormCacheManager.getForm(paramBean.getFormId());
            LockObjectBean lockObjectBean = capRuntimeDataLockManager.get(paramBean.getFormMasterId());
            synchronized (lockObjectBean) {
                FormDataMasterBean cacheFormDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterId());
                // 合并缓存
                this.mergeFormData(cacheFormDataMasterBean, paramBean.getMergeData());
                FormAuthViewBean formAuthViewBean = CAPFormUtil.getFormAuthViewBean(formBean, cacheFormDataMasterBean, paramBean.getRightId());
                FormTableBean formTableBean = formBean.getTableByTableName(tableName);
                // 验证权限
                if (DELETE.equals(type)) {
                    type = paramBean.getRecordIds().size() == cacheFormDataMasterBean.getSubData(tableName).size() ? DELETE_ALL : DELETE;
                }
                if (!checkAddOrDelDataSubBeanAuth(formAuthViewBean, formTableBean, type)) {
                    result.error(CODE6, ResourceUtil.getString(MESSAGE6));
                    return result;
                }
                Map<String, Object> data = new HashMap<String, Object>();
                // 判断第一行数据是否为空，处理判断第一行是否为空
                if (Strings.isNotBlank(firstRecordId)) {
                    FormDataSubBean formDataSubBean = cacheFormDataMasterBean.getFormDataSubBeanById(tableName, Long.valueOf(firstRecordId));
                    result.setFirstIsNull((formDataSubBean == null || formDataSubBean.isEmpty()) ? CAPBusinessEnum.TrueOrFalseEnum.TRUE.getKey() : CAPBusinessEnum.TrueOrFalseEnum.FALSE.getKey());
                }
                // key tableName value list 新增的明细行
                Map<String, List<Long>> tableName2AddSubIds = new HashMap<String, List<Long>>();
                // key tableName value map<key-value {add-list(map)},{delete-list(string)},{update-map}>
                Map<String, Map<String, Object>> tableName2DataMaps = new HashMap<String, Map<String, Object>>();
                if (DELETE.equals(type) || DELETE_ALL.equals(type) || REPEAL.equals(type)) {
                    Map<String, Object> tableMap = tableName2DataMaps.get(tableName);
                    if (tableMap == null) {
                        tableMap = new HashMap<String, Object>();
                        tableName2DataMaps.put(tableName, tableMap);
                    }
                    Set<String> deleteIds = (Set) tableMap.get(DELETE);
                    if (deleteIds == null) {
                        deleteIds = new HashSet<String>();
                        tableMap.put(DELETE, deleteIds);
                    }
                    for (String recordId : paramBean.getRecordIds()) {
                        cacheFormDataMasterBean.removeSubData(tableName, Long.valueOf(recordId));
                        deleteIds.add(recordId);
                    }
                    if (cacheFormDataMasterBean.getSubData(tableName) == null || cacheFormDataMasterBean.getSubData(tableName).isEmpty()) {
                        // 删除全部了，需要新增一条数据
                        List<String> addRows = new ArrayList<String>();
                        addRows.add("");
                        List<FormDataSubBean> formDataSubBeans = this.initNewAddSubDataBean(cacheFormDataMasterBean, formAuthViewBean, formTableBean, addRows, null);
                        List<Long> subIds = new ArrayList<Long>();
                        for (FormDataSubBean formDataSubBean : formDataSubBeans) {
                            subIds.add(formDataSubBean.getId());
                        }
                        tableName2AddSubIds.put(tableName, subIds);
                    }
                } else if (COPY.equals(type) || ADD.equals(type)) {
                    List<FormDataSubBean> formDataSubBeans;
                    if (ADD.equals(type)) {
                        formDataSubBeans = this.initNewAddSubDataBean(cacheFormDataMasterBean, formAuthViewBean, formTableBean, paramBean.getRecordIds(), (Strings.isBlank(preRecordId) ? null : Long.valueOf(preRecordId)));
                    } else {
                        formDataSubBeans = this.initCopyAddSubDataBean(cacheFormDataMasterBean, formAuthViewBean, formTableBean, paramBean.getRecordIds());
                    }
                    // 组装添加或复制的数据map
                    List<Long> subIds = new ArrayList<Long>();
                    for (FormDataSubBean formDataSubBean : formDataSubBeans) {
                        subIds.add(formDataSubBean.getId());
                    }
                    tableName2AddSubIds.put(tableName, subIds);
                }
                Set<String> fillBackFields = capRuntimeCalcManager.execCalcAll(formBean, cacheFormDataMasterBean, formAuthViewBean, tableName2AddSubIds);
                capRuntimeCalcManager.buildCalcAllResult(formBean, cacheFormDataMasterBean, formAuthViewBean, tableName2AddSubIds, fillBackFields, CAPBusinessEnum.SubBeanNewFrom.FRONT_SUBMIT, tableName2DataMaps);
                data.put(TABLE_DATA, tableName2DataMaps);
                result.success(data);
                this.buildExtendResult(formBean, cacheFormDataMasterBean, formAuthViewBean, result);
                cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
            }
        } catch (BusinessException e) {
            LOGGER.error("addOrDelDataSubBean is exception masterId " + paramBean.getFormMasterId() + " , type " + type + " , recordIds " + paramBean.getRecordIds() + " " + e.getMessage(), e);
            result.error(CODE1, e.getMessage());
        }
        return result;
    }

    /**
     * 校验明细表操作权限
     * @param formAuthViewBean
     * @param formTableBean
     * @param type
     * @return
     */
    public boolean validateSubTableAuth(FormAuthViewBean formAuthViewBean, FormTableBean formTableBean, Integer type) {
        return this.validateSubTableAuth(formAuthViewBean,formTableBean,CAPBusinessEnum.FormsonButtonType.getEnumByType(type));
    }

    private boolean validateSubTableAuth(FormAuthViewBean formAuthViewBean, FormTableBean formTableBean, CAPBusinessEnum.FormsonButtonType buttonType) {
        FormAuthorizationTableBean authTableBean = formAuthViewBean == null ? null : formAuthViewBean.getSubTableAuth(formTableBean.getDisplay());
        boolean isAccess = false;
        if (authTableBean != null && buttonType != null) {
            switch (buttonType) {
                case ADD:
                    isAccess = authTableBean.isAdd();
                    break;
                case COPY:
                    isAccess = authTableBean.isCopy();
                    break;
                case IMPORT_EXCEL:
                    isAccess = authTableBean.isImportData();
                    break;
                case EXPORT_EXCEL:
                    isAccess = authTableBean.isExportData();
                    break;
                case DELETE:
                    isAccess = authTableBean.isDelete();
                    break;
                case DELETE_ALL:
                    isAccess = authTableBean.isDeleteAll();
                    break;
                default:
                    break;
            }
        }
        return isAccess;
    }

    /**
     * 校验明细表操作权限
     *
     * @param paramBean
     * @return
     */
    @ValidateRequestParam(notEmpty = {"type", "formMasterId", "formId", "rightId", "tableName"})
    public FormDataSubRowOperationResult validateSubTableAuth(FormDataSubRowOperationParamBean paramBean) {
        FormDataSubRowOperationResult result = new FormDataSubRowOperationResult<Map>();
        result.error(CODE6, ResourceUtil.getString(MESSAGE6));
        FormBean formBean = cap4FormCacheManager.getForm(paramBean.getFormId());
        FormDataMasterBean cacheFormDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterId());
        FormAuthViewBean formAuthViewBean = CAPFormUtil.getFormAuthViewBean(formBean, cacheFormDataMasterBean, paramBean.getRightId());
        FormTableBean formTableBean = formBean.getTableByTableName(paramBean.getTableName());
        if (this.validateSubTableAuth(formAuthViewBean, formTableBean, Integer.parseInt(paramBean.getType()))) {
            result.success();
        }
        return result;
    }

    private boolean checkAddOrDelDataSubBeanAuth(FormAuthViewBean formAuthViewBean, FormTableBean tableBean, String operation) {
        // 产品jiapan要求，轻表单新建明细行后点击返回，标识为撤销（删除），不做权限校验
        if (REPEAL.equals(operation)) {
            return true;
        }
        return this.validateSubTableAuth(formAuthViewBean, tableBean, CAPBusinessEnum.FormsonButtonType.getEnumByAlias(operation));
    }

    /**
     * 表单计算/本表关联/系统关联
     * 通过ServiceProxy访问
     */
    @ValidateRequestParam(notEmpty = {"formMasterDataId", "formId", "rightId"}, validateCacheBeanKey = "formMasterDataId")
    public FormDataResult calculate(FormDataCalculateParamBean paramBean) {
        FormDataResult result = new FormDataResult();
        String fieldName = paramBean.getFieldName();
        Long fromRecordId = paramBean.getFromRecordId();
        String formSonTableName = paramBean.getFormSonTableName();
        boolean calcAll = Strings.isBlank(fieldName);
        FormDataMasterBean cacheFormDataMasterBean = null;
        try {
            FormBean formBean = cap4FormCacheManager.getForm(paramBean.getFormId());
            FormFieldBean formFieldBean = Strings.isBlank(fieldName) ? null : formBean.getFieldBeanByName(fieldName);
            LockObjectBean lockObjectBean = capRuntimeDataLockManager.get(paramBean.getFormMasterDataId());
            synchronized (lockObjectBean) {
                cacheFormDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterDataId());
                FormDataMasterBean oldCacheFormDataMasterBean = null;
                if (!calcAll) {
                    oldCacheFormDataMasterBean = (FormDataMasterBean) cacheFormDataMasterBean.clone();
                }
                this.mergeFormData(cacheFormDataMasterBean, paramBean.getMergeData());
                Set<String> fillBackFields;
                FormAuthViewBean formAuthViewBean = CAPFormUtil.getFormAuthViewBean(formBean, cacheFormDataMasterBean, paramBean.getRightId());
                Map<String, Set<String>> tableName2DeleteIds = new HashMap<String, Set<String>>();
                Map<String, List<FormDataSubBean>> tableName2AddSubBeans = null;
                Map<String, Map<String, Object>> tableName2DataMaps = new HashMap<String, Map<String, Object>>();
                if (calcAll) {
                    if (fromRecordId != null && Strings.isNotBlank(formSonTableName)) {
                        // 移动端编辑明细行点击取消返回
                        // OA-160224 电脑端保存待发的数据，移动端编辑待发，修改明细行数据后，返回主表页面，导致明细表多级枚举丢失
                        FormDataSubBean formDataSubBean = cacheFormDataMasterBean.getFormDataSubBeanById(formSonTableName, fromRecordId);
                        List<FormFieldBean> formFieldBeans = formBean.getTableByTableName(formSonTableName).getFields();
                        for (FormFieldBean fieldBean : formFieldBeans) {
                            cacheFormDataMasterBean.addFieldChanges4Calc(fieldBean, formDataSubBean.getFieldValue(fieldBean.getName()), formDataSubBean);
                        }
                    }
                    fillBackFields = capRuntimeCalcManager.execCalcAll(formBean, cacheFormDataMasterBean, formAuthViewBean, null);
                } else {
                    fillBackFields = new HashSet<String>();
                    if (fromRecordId != null && Strings.isNotBlank(formSonTableName)) {
                        FormDataSubBean subBean = cacheFormDataMasterBean.getFormDataSubBeanById(formSonTableName, fromRecordId);
                        FormDataSubBean oldSubBean = oldCacheFormDataMasterBean.getFormDataSubBeanById(formSonTableName, fromRecordId);
                        if (capRuntimeCalcManager.needCalc(oldSubBean, subBean, formFieldBean, null)) {
                            cap4FormDataManager.calcAllWithFieldIn(formBean, formFieldBean, cacheFormDataMasterBean, subBean, fillBackFields, formAuthViewBean, capRuntimeCalcManager.needCalcSysRelation(oldSubBean, subBean, formFieldBean, null, null));
                        }
                    } else {
                        if (capRuntimeCalcManager.needCalc(oldCacheFormDataMasterBean, cacheFormDataMasterBean, formFieldBean, null)) {
                            cap4FormDataManager.calcAllWithFieldIn(formBean, formFieldBean, cacheFormDataMasterBean, null, fillBackFields, formAuthViewBean, capRuntimeCalcManager.needCalcSysRelation(oldCacheFormDataMasterBean, cacheFormDataMasterBean, formFieldBean, null, null));
                        }
                    }
                    tableName2AddSubBeans = this.calculateAutoIncreaseByField(formBean, cacheFormDataMasterBean, formAuthViewBean, formFieldBean, fillBackFields, tableName2DeleteIds, true);
                }
                capRuntimeCalcManager.buildCalcAllResult(formBean, cacheFormDataMasterBean, formAuthViewBean, tableName2AddSubBeans, fillBackFields, CAPBusinessEnum.SubBeanNewFrom.FRONT_COLLECT_OR_INCREASE, tableName2DataMaps);
                if (!tableName2DeleteIds.isEmpty()) {
                    this.mergeAutoDelete2Result(tableName2DeleteIds, tableName2DataMaps);
                }
                Map<String, Object> resultData = new HashMap<String, Object>();
                resultData.put(TABLE_DATA, tableName2DataMaps);
                result.success(resultData);
                this.buildExtendResult(formBean, cacheFormDataMasterBean, formAuthViewBean, result);
                cap4FormManager.saveSessioMasterDataBean(cacheFormDataMasterBean.getId(), cacheFormDataMasterBean);
            }
        } catch (BusinessException e) {
            LOGGER.error("calculate.getDataJSON.error ", e);
            result.error(CODE1,e.getMessage());
        } catch (CloneNotSupportedException e) {
            LOGGER.error("calculate.getDataJSON.clone.error ", e);
            result.error(CODE1,e.getMessage());
        } finally {
            AppContext.removeThreadContext(CAPBusinessConstant.FIELD_INNER_CHANGE_MAP);
            if (cacheFormDataMasterBean != null) {
                cacheFormDataMasterBean.clearFieldChangesByCalc();
            }
            JMTrackUtils.clearTrack();
        }
        return result;
    }

    /**
     * 根据fileUrl修改附件Reference和SubReference
     * 通过ServiceProxy访问
     */
    @ValidateRequestParam(notEmpty = {"attachments", "subReference", "reference", "formMasterId"}, validateCacheBeanKey = "formMasterId")
    public FormDataResult<List<Map<String, String>>> addOrDelAttachment(FormDataAttachmentParamBean paramBean) {
        FormDataResult<List<Map<String, String>>> result = new FormDataResult<List<Map<String, String>>>();
        // 支持add delete replace
        String type = paramBean.getType() != null ? paramBean.getType() : "add";
        String subType = paramBean.getSubType() != null ? paramBean.getSubType() : "";
        // 要操作的附件对象
        Map<String, String> baseInfo = paramBean.getBaseAttachmentInfo();
        FormDataMasterBean formDataMasterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterId());
        List<Attachment> sessionAttachment = formDataMasterBean.getSessionAttachments(String.valueOf(paramBean.getSubReference()));
        if (sessionAttachment == null || ("document".equals(subType) && "add".equals(type))) {
            // 如果是关联文档，则全覆盖，并且删除的ID增加
            sessionAttachment = new ArrayList<Attachment>();
        }
        for (Map<String, Object> map : paramBean.getAttachments()) {
            if ("delete".equals(type)) {
                Long attId = CAPParamUtil.getLong(map, ID);
                if (Strings.isNotEmpty(sessionAttachment)) {
                    // 删除后需要移除session中的attachement
                    Iterator<Attachment> iterator = sessionAttachment.iterator();
                    while (iterator.hasNext()) {
                        Attachment temp = iterator.next();
                        if (attId.equals(temp.getId()) || ("document".equals(subType) && (attId.equals(temp.getId()) || attId.equals(temp.getFileUrl())))) {
                            iterator.remove();
                        }
                    }
                }
            } else {
                Map<String, String> attMap = new HashMap<String, String>();
                Iterator<String> mapIterator = map.keySet().iterator();
                while (mapIterator.hasNext()) {
                    String key = mapIterator.next();
                    Object value = map.get(key);
                    if (value instanceof String) {
                        attMap.put(key, String.valueOf(value));
                    }
                }
                // OA-160254 新建协同调用CAP4流程报错待发，页面一直转圈，关闭页面后，待发列表中无该数据，但报表统计却统计出了该数据
                // 附件返回拼装的日志不对，兼容createDate
                if (attMap.get(Attachment.PROP_CREATEDATE) == null && attMap.get("createDate") != null) {
                    attMap.put(Attachment.PROP_CREATEDATE, attMap.get("createDate"));
                }
                attMap.put(Attachment.PROP_SUB_REFERENCE, String.valueOf(paramBean.getSubReference()));
                attMap.put(Attachment.PROP_REFERENCE, String.valueOf(paramBean.getReference()));
                attMap.put(Attachment.PROP_CATEGORY, baseInfo.get(Attachment.PROP_CATEGORY));
                attMap.put(Attachment.PROP_TYPE, baseInfo.get(Attachment.PROP_TYPE));
                attMap.put(Attachment.PROP_MIME_TYPE, String.valueOf(map.get(Attachment.PROP_MIME_TYPE) == null ? baseInfo.get(Attachment.PROP_MIME_TYPE) : map.get(Attachment.PROP_MIME_TYPE)));
                Attachment att = new Attachment(attMap);
                if (Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal() == att.getType()) {
                    att.setGenesisId(Strings.isNotBlank(attMap.get(Attachment.Prop_GenesisId)) && !"null".equals(attMap.get(Attachment.Prop_GenesisId)) ? Long.valueOf(attMap.get(Attachment.Prop_GenesisId)) : null);
                    att.setDescription((attMap.get(Attachment.PROP_DESCRIPTION) != null && !"null".equals(attMap.get(Attachment.PROP_DESCRIPTION))) ? attMap.get(Attachment.PROP_DESCRIPTION) : null);
                    att.setSort((attMap.get(Attachment.PROP_SORT) != null && !"null".equals(attMap.get(Attachment.PROP_SORT))) ? Integer.parseInt(String.valueOf(attMap.get(Attachment.PROP_SORT))) : 0);
                }
                att.setId((attMap.get(ID) != null && !"null".equals(attMap.get(ID))) ? Long.valueOf(attMap.get(ID)) : UUIDLong.longUUID());
                att.putExtraAttr("v", attMap.get("v"));
                if ("update".equals(type)) {
                    // 更新附件，比如office的在线编辑后，提交
                    Iterator<Attachment> iterator = sessionAttachment.iterator();
                    int index = 0;
                    while (iterator.hasNext()) {
                        Attachment temp = iterator.next();
                        if (att.getId().equals(temp.getId())) {
                            iterator.remove();
                            break;
                        }
                        index++;
                    }
                    sessionAttachment.add(index, att);
                } else {
                    sessionAttachment.add(att);
                }
            }
        }
        formDataMasterBean.putSessionAttachments(String.valueOf(paramBean.getSubReference()), sessionAttachment);
        cap4FormManager.saveSessioMasterDataBean(formDataMasterBean.getId(), formDataMasterBean);
        result.success(CAPAttachmentUtil.buildAttachmentMaps(sessionAttachment, true));
        return result;
    }

    /**
     * 表单截图
     * @param paramBean
     * @return
     */
    public ScreenCaptureResult screenCapture(ScreenCapture4FormParamBean paramBean){
        if (paramBean.getFormMasterId() != null) {
            FormDataMasterBean masterBean = cap4FormManager.getSessioMasterDataBean(paramBean.getFormMasterId());
            if (masterBean != null) {
                this.mergeFormData(masterBean, paramBean.getMergeData());
            }
        }
        return capScreenCaptureService.doFormScreenCapture4File(paramBean);
    }

    public void setBusinessManager4(BusinessManager businessManager4) {
        this.businessManager4 = businessManager4;
    }

    public void setCap4FormTriggerManager(CAP4FormTriggerManager cap4FormTriggerManager) {
        this.cap4FormTriggerManager = cap4FormTriggerManager;
    }

    public void setCap4SerialNumberManager(CAP4SerialNumberManager cap4SerialNumberManager) {
        this.cap4SerialNumberManager = cap4SerialNumberManager;
    }

    public void setJmMonitorService( JMMonitorService jmMonitorService ) {
        this.jmMonitorService = jmMonitorService;
    }

    public void setCapScreenCaptureService(CAPScreenCaptureService capScreenCaptureService) {
        this.capScreenCaptureService = capScreenCaptureService;
    }

    public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
        this.formApi4Cap4 = formApi4Cap4;
    }
}
