package com.seeyon.cap4.form.manager.impl;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFlowBusinessBean;
import com.seeyon.cap4.form.bean.FormRelationshipBean;
import com.seeyon.cap4.form.bean.FormRelationshipMapBean;
import com.seeyon.cap4.form.manager.ShowFormDataBtnManager;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.cap4.form.modules.engin.relation.CAP4FormRelationActionManager;
import com.seeyon.cap4.form.modules.engin.relation.FormRelationEnums;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.magic.constants.MagicPrivateConstants;
import com.seeyon.cap4.magic.utils.MagicHandleFormUtils;
import com.seeyon.cap4.template.service.CAPCustomService;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaox on 2019/1/15.
 */
public class ShowFormDataBtnManagerImpl implements ShowFormDataBtnManager {

    private static final Log LOGGER = CtpLogFactory.getLog(ShowFormDataBtnManagerImpl.class);

    private CAPCustomService capCustomService;
    private CAP4FormCacheManager cap4FormCacheManager;
    private CAP4FormDataDAO cap4FormDataDAO;
    private CAP4FormRelationActionManager cap4FormRelationActionManager;

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String CODE0 = "2000";
    private static final String CODE1 = "2001";
    private static final String MESSAGE1 = "com.cap.btn.showFormDataBtn.exception";
    private static final String CODE2 = "2002";
    private static final String MESSAGE2 = "com.cap.btn.showFormDataBtn.paramNull";
    private static final String CODE4 = "2004";
    private static final String MESSAGE4 = "com.cap.btn.showFormDataBtn.noShip";
    private static final String CODE5 = "2005";
    private static final String MESSAGE5 = "com.cap.btn.showFormDataBtn.noData";
    private static final String CODE6 = "2006";
    private static final String MESSAGE6 = "com.cap.btn.showFormDataBtn.noAuth";
    private static final String CODE7 = "2004";
    private static final String MESSAGE7 = "com.cap.btn.showFormDataBtn.shipDisable";

    private static final String FORM_ID = "formId";
    private static final String CONTENT_DATA_ID = "contentDataId";
    private static final String DATA = "data";
    private static final String FORM_TEMPLATE_ID = "formTemplateId";
    private static final String TYPE = "type";
    private static final String CUSTOM_PARAM = "customParam";

    @Override
    public Map<String, Object> getShowRecord(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<String, Object>();
        String formId = (String) params.get(FORM_ID);
        String contentDataId = (String) params.get(CONTENT_DATA_ID);
        String type = (String) params.get(TYPE);
        String formTemplateId = (String) params.get(FORM_TEMPLATE_ID);
        if (Strings.isBlank(formId) || Strings.isBlank(formTemplateId) || Strings.isBlank(contentDataId) || Strings.isBlank(type)) {
            result.put(CODE, CODE2);
            result.put(MESSAGE, String.format(ResourceUtil.getString(MESSAGE2), "formId or formTemplateId or contentDataId or type"));
            return result;
        }
        FormBean formBean = cap4FormCacheManager.getForm(Long.valueOf(formId));
        FormBindAuthBean formBindAuthBean = formBean.getBind().getFormBindAuthBean(formTemplateId);
        Map<String, Object> customBtn = null;
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
        try {
            Map<String, Object> customParam = (Map) JSONUtil.parseJSONString((String) customBtn.get(CUSTOM_PARAM));
            if (customParam == null || customParam.isEmpty()) {
                result.put(CODE, CODE4);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE4));
                return result;
            }
            Map<String, Object> targetFormInfo = (Map) customParam.get("targetFormInfo");
            Long relationId = Long.valueOf((String) targetFormInfo.get("relationId"));
            FormRelationshipBean formRelationshipBean = cap4FormCacheManager.getFormRelationshipBean(relationId);
            if (formRelationshipBean == null || formRelationshipBean.getState() != 1) {
                result.put(CODE, CODE7);
                result.put(MESSAGE, ResourceUtil.getString(MESSAGE7));
                return result;
            } else {
                String pcView = this.getCheckedRights((List) targetFormInfo.get("pcRights"));
                String phoneView = this.getCheckedRights((List) targetFormInfo.get("mbRights"));
                if (Strings.isBlank(pcView) && Strings.isBlank(phoneView)) {
                    result.put(CODE, CODE6);
                    result.put(MESSAGE, ResourceUtil.getString(MESSAGE6));
                    return result;
                }
                FormDataMasterBean formDataMasterBean = cap4FormDataDAO.selectDataByMasterId(Long.valueOf(contentDataId), formBean, null);
                FormRelationshipMapBean formRelationshipMapBean = formRelationshipBean.getRelationMapList().get(0);
                List<Map<String, Object>> dataList = cap4FormRelationActionManager.getSysRelationDatas(formDataMasterBean, null, formRelationshipMapBean);
                if (Strings.isEmpty(dataList)) {
                    result.put(CODE, CODE5);
                    result.put(MESSAGE, ResourceUtil.getString(MESSAGE5));
                } else {
                    Map<String, Object> dataMap = new HashMap<String, Object>();
                    capCustomService.addShowRecordRight(pcView);
                    capCustomService.addShowRecordRight(phoneView);
                    Long memberId = AppContext.currentUserId();
                    String moduleId = String.valueOf(dataList.get(0).get("id"));
                    LOGGER.info("查看按钮处理穿透权限：memberId" + memberId + ", id: " + moduleId);
                    CAPFormUtil.addFormAccessControl(Long.valueOf(moduleId), memberId);
                    dataMap.put("pcView", pcView);
                    dataMap.put("phoneView", phoneView);
                    dataMap.put("toMasterDataId", moduleId);
                    dataMap.put("toVersion", formRelationshipBean.getTargetFormVersion());
                    FormBean toFormBean = cap4FormCacheManager.getForm(formRelationshipBean.getTargetFormId());
                    //OA-178808
                    //点击查看表单记录提示数据不存在
                    if (toFormBean == null && FormRelationEnums.relationApplicationType.inner.getKey().equals(formRelationshipBean.getApplicationType())) {
                        String bid = ParamUtil.getString(formRelationshipMapBean.getParams(), MagicPrivateConstants.DATAMAGIC_BID, "");
                        String version = ParamUtil.getString(formRelationshipMapBean.getParams(), MagicPrivateConstants.DATAMAGIC_VERSION, "");
                        String methodName = ParamUtil.getString(formRelationshipMapBean.getParams(), MagicPrivateConstants.DATAMAGIC_METHOD_NAME, "");
                        Long targetFormId = MagicHandleFormUtils.getProviderFormId(bid, version, methodName);
                        toFormBean = cap4FormCacheManager.getForm(targetFormId);
                    }
                    if (toFormBean != null) {
                        dataMap.put("toFormType", ModuleType.cap4UnflowForm.getValue());
                    } else {
                        dataMap.put("toFormType", ModuleType.unflowInfo.getValue());
                    }
                    result.put(CODE, CODE0);
                    result.put(DATA, dataMap);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            result.put(CODE, CODE1);
            result.put(MESSAGE, ResourceUtil.getString(MESSAGE1));
        }

        return result;
    }

    private String getCheckedRights(List<Map<String, Object>> rightIds) {
        StringBuilder rightId = new StringBuilder();
        if (Strings.isNotEmpty(rightIds)) {
            for (Map<String, Object> map : rightIds) {
                if ("true".equals(String.valueOf(map.get("checked"))) && Strings.isNotBlank(String.valueOf(map.get("rightId")))) {
                    if (Strings.isNotBlank(rightId.toString())) {
                        rightId.append("_");
                    }
                    rightId.append(String.valueOf(map.get("rightId")));
                }
            }
        }
        return rightId.toString();
    }

    public void setCapCustomService(CAPCustomService capCustomService) {
        this.capCustomService = capCustomService;
    }

    public void setCap4FormCacheManager(CAP4FormCacheManager cap4FormCacheManager) {
        this.cap4FormCacheManager = cap4FormCacheManager;
    }

    public void setCap4FormRelationActionManager(CAP4FormRelationActionManager cap4FormRelationActionManager) {
        this.cap4FormRelationActionManager = cap4FormRelationActionManager;
    }

    public void setCap4FormDataDAO(CAP4FormDataDAO cap4FormDataDAO) {
        this.cap4FormDataDAO = cap4FormDataDAO;
    }
}