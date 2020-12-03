package com.seeyon.cap4.form.manager.impl;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.manager.NewFormDataCtrlManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.template.service.CAPCustomService;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-29.
 */
public class NewFormDataCtrlManagerImpl implements NewFormDataCtrlManager {
    private static final Log LOGGER = CtpLogFactory.getLog(NewFormDataCtrlManagerImpl.class);
    private CAP4FormCacheManager cap4FormCacheManager;
    private CAPCustomService capCustomService;
    private CAP4FormManager cap4FormManager;
    private AttachmentManager attachmentManager;
    private MainbodyManager ctpMainbodyManager;

    private static final String MERGE_DATA = "mergeData";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String CODE0 = "2000";
    private static final String CODE1 = "2001";
    private static final String MESSAGE1 = "form.ctrl.newformdatactrl.exception";
    private static final String CODE2 = "2002";
    private static final String MESSAGE2 = "form.ctrl.newformdatactrl.paramNull";
    private static final String CODE3 = "2003";
    private static final String MESSAGE3 = "form.ctrl.newformdatactrl.targetDelete";
    private static final String CODE4 = "2004";
    private static final String MESSAGE4 = "form.ctrl.newformdatactrl.noShip";

    private static final String FORM_ID = "formId";
    private static final String CONTENT_DATA_ID = "contentDataId";
    private static final String RECORD_ID = "recordId";
    private static final String FIELD_NAME = "fieldName";
    private static final String MAPPING_DATA_KEY = "mappingDataKey";

    @Override
    public Map<String, Object> dealMappingData(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<String, Object>();
        String formId = (String) params.get(FORM_ID);
        String contentDataId = (String) params.get(CONTENT_DATA_ID);
        String recordId = (params.get(RECORD_ID) == null || Strings.isBlank(String.valueOf(params.get(RECORD_ID)))) ? null : (String) params.get(RECORD_ID);
        String fieldName = (String) params.get(FIELD_NAME);
        if (Strings.isBlank(formId) || Strings.isBlank(contentDataId) || Strings.isBlank(fieldName)) {
            result.put(CODE, CODE2);
            result.put(MESSAGE, String.format(ResourceUtil.getString(MESSAGE2), "formId or contentDataId or fieldName"));
            return result;
        }
        try {
            FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
            FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
            if (Strings.isBlank(formFieldBean.getCustomParam())) {
                // 导入的表单，这个关系是清空了的
                result.put(CODE, CODE4);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE4));
                return result;
            }
            Map<String, Object> customParam = (Map) JSONUtil.parseJSONString(formFieldBean.getCustomParam());
            if (customParam == null || customParam.isEmpty()) {
                // 导入的表单，这个关系是清空了的
                result.put(CODE, CODE4);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE4));
                return result;
            }
            Map<String, Object> targetFormInfo = (Map) customParam.get("targetFormInfo");
            Map<String, Object> targetForm = (Map) targetFormInfo.get("targetForm");
            Long targetFormId = Long.valueOf(String.valueOf(targetForm.get(FORM_ID)));
            FormBean targetFormBean = cap4FormCacheManager.getForm(targetFormId);
            if (targetFormBean == null) {
                result.put(CODE, CODE3);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE3));
                return result;
            }
            FormDataMasterBean formDataMasterBean = getRealFormDataMasterBean(formBean, Long.valueOf(contentDataId), params);
            if (cap4FormManager.getSessioMasterDataBean(Long.valueOf(contentDataId)) == null) {
                // 浏览表单打开
                Long reference = formDataMasterBean.getId();
                if (formBean.isFlowForm()) {
                    List<CtpContentAll> contentAlls = ctpMainbodyManager.getContentListByContentDataIdAndModuleType(ModuleType.collaboration.getKey(), formDataMasterBean.getId());
                    reference = CollectionUtils.isNotEmpty(contentAlls) ? contentAlls.get(0).getModuleId() : null;
                }
                List<Attachment> attachments = attachmentManager.getByReference(reference);
                Map<String, List<Attachment>> subReferenceAttachments = CAPAttachmentUtil.buildAttachmentSubReferenceToList(attachments);
                for (Map.Entry<String, List<Attachment>> entry : subReferenceAttachments.entrySet()) {
                    formDataMasterBean.putSessionAttachments(entry.getKey(), entry.getValue());
                }
            }
            List<Map<String, Object>> mappedFields = (List) targetFormInfo.get("mappedFields");
            List<Map<String, String>> mappingFields = new ArrayList<Map<String, String>>();
            Map<String, Object> mappingData = new HashMap<String, Object>();
            for (Map<String, Object> fieldMap : mappedFields) {
                Map<String, Object> dstMap = (Map) fieldMap.get("dst");
                Map<String, Object> srcMap = (Map) fieldMap.get("src");
                FormFieldBean dstFieldBean = targetFormBean.getFieldBeanByName(String.valueOf(dstMap.get("name")));
                FormFieldBean srcFieldBean = formBean.getFieldBeanByName(String.valueOf(srcMap.get("name")));
                if (srcFieldBean != null && dstFieldBean != null) {
                    Map<String, String> mappingField = new HashMap<String, String>(2);
                    mappingField.put(CAPCustomService.SRC, srcFieldBean.getName());
                    mappingField.put(CAPCustomService.DST, dstFieldBean.getName());
                    mappingFields.add(mappingField);
                    Object mappingValue;
                    if (Strings.isNotBlank(recordId)) {
                        //按钮位于明细表中
                        if (srcFieldBean.isSubField()) {
                            //明细表字段
                            FormDataSubBean formDataSubBean = formDataMasterBean.getFormDataSubBeanById(srcFieldBean.getOwnerTableName(), Long.valueOf(recordId));
                            mappingValue = formDataSubBean.getFieldValue(srcFieldBean.getName());
                        } else {
                            mappingValue = formDataMasterBean.getFieldValue(srcFieldBean.getName());
                        }
                    } else {
                        mappingValue = formDataMasterBean.getFieldValue(srcFieldBean.getName());
                    }
                    mappingData.put(srcFieldBean.getName(), capCustomService.getFieldMappingData(formDataMasterBean, srcFieldBean, mappingValue));
                }
            }
            String mappingDataKey = capCustomService.putMappingDataToCache(mappingData, mappingFields);
            result.put(CODE, CODE0);
            result.put(MAPPING_DATA_KEY, mappingDataKey);
        } catch (Exception e) {
            LOGGER.error(e);
            result.put(CODE, CODE1);
            result.put(MESSAGE, ResourceUtil.getString(MESSAGE1));
        }
        return result;
    }

    /**
     * 查询当前登录人员所在单位的所有单表表单id
     */
    @Override
    public List<Long> getCurrentAccountFormIds() {
        String sql = "select cap_form_definition.id from cap_form_definition,form_owner where form_owner.ORG_ACCOUNT_ID='" + AppContext.currentAccountId() + "' and form_owner.form_id = cap_form_definition.id and cap_form_definition.id not in(select FORMID from cap_biz_config_item) order by cap_form_definition.create_time desc";
        JDBCAgent jdbcAgent = new JDBCAgent();
        List<Long> result = new ArrayList<Long>();
        try {
            jdbcAgent.execute(sql);
            List<Map<String, Object>> results = jdbcAgent.resultSetToList();
            for (Map<String, Object> map : results) {
                Object idObj = map.get("id");
                result.add(Long.parseLong(String.valueOf(idObj)));
            }
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            jdbcAgent.close();
        }
        return result;
    }

    /**
     * 获取masterData
     */
    private FormDataMasterBean getRealFormDataMasterBean(FormBean formBean, Long contentDataId, Map<String, Object> params) throws BusinessException, SQLException {
        FormDataMasterBean formDataMasterBean = cap4FormManager.getSessioMasterDataBean(contentDataId);
        if (formDataMasterBean == null) {
            formDataMasterBean = cap4FormManager.getDataMasterBeanById(contentDataId, formBean, null);
        } else {
            if (params.get(MERGE_DATA) != null) {
                capCustomService.mergeFormData(formBean, formDataMasterBean, (Map) params.get(MERGE_DATA));
            }
        }
        return formDataMasterBean;
    }

    public void setCapCustomService(CAPCustomService capCustomService) {
        this.capCustomService = capCustomService;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
        this.ctpMainbodyManager = ctpMainbodyManager;
    }
}