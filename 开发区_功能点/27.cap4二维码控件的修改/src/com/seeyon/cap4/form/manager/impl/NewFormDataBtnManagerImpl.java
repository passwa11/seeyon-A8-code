package com.seeyon.cap4.form.manager.impl;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFlowBusinessBean;
import com.seeyon.cap4.form.manager.NewFormDataBtnManager;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.template.service.CAPCustomService;
import com.seeyon.cap4.template.util.CAPAttachmentUtil;
import com.seeyon.cap4.template.util.CAPParamUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaox on 2019/1/9.
 */
public class NewFormDataBtnManagerImpl implements NewFormDataBtnManager {

    private static final Log LOGGER = CtpLogFactory.getLog(NewFormDataBtnManagerImpl.class);

    private CAP4FormCacheManager cap4FormCacheManager;
    private CAP4FormDataDAO cap4FormDataDAO;
    private CAPCustomService capCustomService;
    private AttachmentManager attachmentManager;
    private CollaborationApi collaborationApi;

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String CODE0 = "2000";
    private static final String CODE1 = "2001";
    private static final String MESSAGE1 = "com.cap.btn.newFormDataBtn.exception";
    private static final String CODE2 = "2002";
    private static final String MESSAGE2 = "com.cap.btn.newFormDataBtn.paramNull";
    private static final String CODE3 = "2003";
    private static final String MESSAGE3 = "com.cap.btn.newFormDataBtn.targetDelete";
    private static final String CODE4 = "2004";
    private static final String MESSAGE4 = "com.cap.btn.newFormDataBtn.noShip";

    private static final String FORM_ID = "formId";
    private static final String DATA_IDS = "dataIds";
    private static final String FORM_TEMPLATE_ID = "formTemplateId";
    private static final String TYPE = "type";
    private static final String CUSTOM_PARAM = "customParam";
    private static final String MAPPING_DATA_KEY = "mappingDataKey";

    @Override
    public Map<String, Object> dealMappingData(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<String> dataIds = CAPParamUtil.getParamsListByKey(params, DATA_IDS);
        String formId = (String) params.get(FORM_ID);
        String formTemplateId = (String) params.get(FORM_TEMPLATE_ID);
        String type = (String) params.get(TYPE);
        if (Strings.isBlank(formId) || Strings.isBlank(formTemplateId) || Strings.isEmpty(dataIds) || Strings.isBlank(type)) {
            result.put(CODE, CODE2);
            result.put(MESSAGE, String.format(ResourceUtil.getString(MESSAGE2), "formId or formTemplateId or dataIds or type"));
            return result;
        }
        try {
            FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
            Map<String, Object> customBtn = null;
            FormBindAuthBean formBindAuthBean = formBean.getBind().getFormBindAuthBean(formTemplateId);
            if (formBindAuthBean == null) {
                FormFlowBusinessBean businessBean = formBean.getBind().getFlowBusinessById(Long.valueOf(formTemplateId));
                if (businessBean != null) {
                    customBtn = businessBean.getCustomBtnInfoById(type);
                }
            } else {
                customBtn = formBindAuthBean.getCustomBtnInfoById(type);
            }
            if (customBtn == null || Strings.isBlank((String) customBtn.get(CUSTOM_PARAM))) {
                result.put(CODE, CODE4);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE4));
                return result;
            }
            Map<String, Object> customParam = (Map) JSONUtil.parseJSONString((String) customBtn.get(CUSTOM_PARAM));
            Map<String, Object> targetFormInfo = (Map) customParam.get("targetFormInfo");
            Map<String, Object> targetForm = (Map) targetFormInfo.get("targetForm");
            Long targetFormId = Long.valueOf(String.valueOf(targetForm.get(FORM_ID)));
            FormBean targetFormBean = cap4FormCacheManager.getForm(targetFormId);
            if (targetFormBean == null) {
                result.put(CODE, CODE3);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE3));
                return result;
            }
            Long[] masterIds = new Long[dataIds.size()];
            for (int i = 0; i < dataIds.size(); i++) {
                masterIds[i] = Long.valueOf(dataIds.get(i));
            }
            List<Long> references = new ArrayList<Long>();
            Map<Long, Long> masterId2SummaryIdMap = null;
            if (formBean.isFlowForm()) {
                masterId2SummaryIdMap = collaborationApi.getColSummaryIdByFormRecordIds(Arrays.asList(masterIds));
                if (masterId2SummaryIdMap != null) {
                    references.addAll(masterId2SummaryIdMap.values());
                }
            } else {
                references.addAll(Arrays.asList(masterIds));
            }
            List<Attachment> attachments = attachmentManager.getByReference(references);
            // 先按数据ID分类放好，可能不同数据的subReference不一致
            Map<String, List<Attachment>> reference2Attachments = new HashMap<String, List<Attachment>>();
            if (Strings.isNotEmpty(attachments)) {
                for (Attachment attachment : attachments) {
                    if (attachment.getCategory().intValue() != ApplicationCategoryEnum.cap4Form.key()) {
                        continue;
                    }
                    String reference = String.valueOf(attachment.getReference());
                    List<Attachment> formAttachments = reference2Attachments.get(reference);
                    if (formAttachments == null) {
                        formAttachments = new ArrayList();
                        reference2Attachments.put(reference, formAttachments);
                    }
                    formAttachments.add(attachment);
                }
            }
            List<FormDataMasterBean> formDataMasterBeans = cap4FormDataDAO.selectMasterDataById(masterIds, formBean, null);
            List<Map<String, Object>> mappingDatas = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> mappedFields = (List) targetFormInfo.get("mappedFields");
            List<Map<String, String>> mappingFields = new ArrayList<Map<String, String>>();
            for (FormDataMasterBean formDataMasterBean : formDataMasterBeans) {
                Long reference = formBean.isFlowForm() ? (masterId2SummaryIdMap == null ? null : masterId2SummaryIdMap.get(formDataMasterBean.getId())) : formDataMasterBean.getId();
                Map<String, List<Attachment>> subReferenceAttachments = CAPAttachmentUtil.buildAttachmentSubReferenceToList(reference2Attachments.get(String.valueOf(reference)));
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
                        Object mappingValue = formDataMasterBean.getFieldValue(srcFieldBean.getName());
                        if (srcFieldBean.isAttachment() && mappingValue != null) {
                            List<Attachment> subReferenceAttachment = subReferenceAttachments == null ? null : subReferenceAttachments.get(String.valueOf(mappingValue));
                            mappingData.put(srcFieldBean.getName(), capCustomService.getFieldMappingData(srcFieldBean, mappingValue, subReferenceAttachment));
                        } else {
                            mappingData.put(srcFieldBean.getName(), capCustomService.getFieldMappingData(formDataMasterBean, srcFieldBean, mappingValue));
                        }
                    }
                }
                mappingDatas.add(mappingData);
            }
            String mappingDataKey = capCustomService.putMappingDataToCache(mappingDatas, mappingFields);
            result.put(CODE, CODE0);
            result.put(MAPPING_DATA_KEY, mappingDataKey);
        } catch (Exception e) {
            LOGGER.error(e);
            result.put(CODE, CODE1);
            result.put(MESSAGE, ResourceUtil.getString(MESSAGE1));
        }
        return result;
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setCap4FormDataDAO(CAP4FormDataDAO cap4FormDataDAO) {
        this.cap4FormDataDAO = cap4FormDataDAO;
    }

    public void setCapCustomService(CAPCustomService capCustomService) {
        this.capCustomService = capCustomService;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
}