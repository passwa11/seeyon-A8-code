package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.mplus.api.MplusApi;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.BankCardProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.BusinessLicenceProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.IdCardBackProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.IdCardProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.OcrType;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.OrgCodeCertProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.TaxRegCertProp;
import com.seeyon.cap4.form.bean.fieldCtrl.enums.VisitCardProp;
import com.seeyon.cap4.form.bean.fieldCtrl.utils.ImgUtil;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.cap4.template.util.HttpClientUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 证照识别rest接口
 */
@Path("cap4/formOcrCtrl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FormOcrCtrlResource extends BaseResource {

    private static final String SERVICE_CODE = "m20000000000003001";

    /**
     * 获取所有可以识别的证件类型
     *
     * @return
     */
    @GET
    @Path("ocrType")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getOcrType() throws BusinessException {
        List<Map<String, Object>> types = null;
        //非开发版本，ocr能够使用哪些服务受云联中心控制
        if (!AppContext.isRunningModeDevelop()) {
            MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
            String ticket = mplusApi.getTicket(SERVICE_CODE);
            String domain = mplusApi.getDomain();
            String url = domain + "/svr/usable";
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("ticket",ticket);
            param.put("parameterType", "2");
            //调用云联接口返回当前V5环境云联服务使用情况
            Map<String, Object> res = HttpClientUtil.doPost(url, param);
            if (!"1000".equals(String.valueOf(res.get("code")))) {
                String msg = (String) res.get("msg");
                if (StringUtil.checkNull(msg)) {
                    msg = "Cloud Union Central Interface Call Error！code:" + res.get("code");
                } else {
                    msg = msg + "code:" + res.get("code") + "。";
                }
                throw new BusinessException(msg);
            } else {
                types = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> dataMaps = (List<Map<String, Object>>) res.get("data");
                for (Map<String, Object> data : dataMaps) {
                    String tempType = String.valueOf(data.get("type"));
                    String val = String.valueOf(data.get("usable"));
                    if ("1".equals(tempType) && "1".equals(val)) {//身份证
                        types.add(getTypeMap(OcrType.IDENTIFICATION_CARD_FRONT));
                        types.add(getTypeMap(OcrType.IDENTIFICATION_CARD_BACK));
                    } else if ("2".equals(tempType) && "1".equals(val)) {//银行卡
                        types.add(getTypeMap(OcrType.BANK_CARD));
                    } else if ("3".equals(tempType) && "1".equals(val)) {//名片
                        types.add(getTypeMap(OcrType.VISITING_CARD));
                    } else if("4".equals(tempType) && "1".equals(val)){//营业执照、组织机构代码证、税务登记证
                        types.add(getTypeMap(OcrType.BUSINESS_LICENCE));
                        types.add(getTypeMap(OcrType.ORGANIZATION_CODE_CERTIFICATE));
                        types.add(getTypeMap(OcrType.TAX_REGISTRATION_CERTIFICATE));
                    }
                }
            }
        } else {
            types = OcrType.list();
        }
        return success(types);
    }

    private Map<String,Object> getTypeMap(OcrType type){
        Map<String,Object> res = new HashMap<String, Object>();
        res.put("key",type.getKey());
        res.put("name",type.getName());
        return res;
    }

    /**
     * 根据ocr类型返回可以设置的属性
     *
     * @param type
     * @return
     */
    @GET
    @Path("fieldProperty/{type}")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getFieldProperty(@PathParam("type") String type) {
        OcrType ocrType = OcrType.getByKey(type);
        return success(getSubProperty(ocrType));
    }

    /**
     * 获取当前ocr属性可以配置的表单字段
     *
     * @param formId
     * @param ocrSubProp
     * @param ocrType
     * @param currentField
     * @return
     */
    @GET
    @Path("formFields")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getFormFields(@QueryParam("formId") String formId, @QueryParam("ocrSubProp") String ocrSubProp, @QueryParam("ocrType") String ocrType, @QueryParam("currentField") String currentField) {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        FormTableBean currentTable = formBean.getFormTableBeanByFieldName(currentField);
        List<FormFieldBean> currentTableFields = currentTable.getFields();

        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        for (FormFieldBean field : currentTableFields) {
            if (!field.isCustomerCtrl() && fieldIsPermission(ocrType, ocrSubProp, field)) {
                resList.add(field.getJsonObj4Design(false));
            }
        }
        return success(resList);
    }

    /**
     * 识别证件信息并返回数据
     *
     * @param formId    表单id
     * @param fieldName 字段名
     * @param fileId    附件id
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("recognition")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    @SuppressWarnings("unchecked")
    public Response recognition(@QueryParam("formId") String formId, @QueryParam("fieldName") String fieldName, @QueryParam("fileId") String fileId, @QueryParam("formDataId") String formDataId, @QueryParam("formSubDataId") String formSubDataId, @QueryParam("rightId") String rightId) throws BusinessException {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(Long.parseLong(formId), false);
        FormFieldBean field = formBean.getFieldBeanByName(fieldName);

        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.parseLong(formDataId));
        if (null == cacheFormData) {
            throw new BusinessException("表单数据在session中找不到（masterId:" + formDataId + "），请尝试重新打开。");
        }
        FormDataSubBean formSubData = null;
        if (!field.isMasterField()) {
            formSubData = cacheFormData.getFormDataSubBeanById(field.getOwnerTableName(), Long.parseLong(formSubDataId));
            if (null == formSubData) {
                throw new BusinessException("OCR控件是明细表字段，但是通过明细表行id：" + formSubDataId + "找不到明细表数据");
            }
        }

        String customParams = field.getCustomParam();
        Map<String, Object> define = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
        Map<String, Object> mapping = (Map<String, Object>) define.get("mapping");
        if (mapping == null) {
            return success("No corresponding OCR settings");
        }
        String ocrType = String.valueOf(mapping.get("ocrType"));

        Map<String, Object> data = getRecognition(fileId, OcrType.getByKey(ocrType));
        List<Map<String, String>> ocrPropMapping = (List<Map<String, String>>) mapping.get("subPropMapping");
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (Map<String, String> defineMap : ocrPropMapping) {
            Map<String, Object> valueMap = new HashMap<String, Object>();
            String ocrKey = defineMap.get("source");
            String formFieldName = defineMap.get("target");

            if (Strings.isEmpty(ocrKey) || Strings.isEmpty(formFieldName)) {
                continue;
            }

            Object val = data.get(ocrKey);
            FormFieldBean formFieldBean = formBean.getFieldBeanByName(formFieldName);
            boolean isDate = FormFieldComEnum.EXTEND_DATE.getKey().equalsIgnoreCase(formFieldBean.getInputType());
            String formFieldValue = val == null ? null : String.valueOf(val);
            //如果是日期需要做转换转换成2018-7-11这种格式
            if (isDate && !StringUtil.checkNull(formFieldValue)) {
                val = formFieldValue.replaceAll("年|月", "-").replaceAll("日", "");
            }
            if (formFieldBean.isMasterField()) {
                cacheFormData.addFieldValue(formFieldName, val);
                val = cacheFormData.getFieldValue(formFieldName);
            } else {
                formSubData.addFieldValue(formFieldName, val);
                val = formSubData.getFieldValue(formFieldName);
            }
            Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, formFieldBean, null);
            valueMap.put(formFieldName, tempRes);
            results.add(valueMap);
        }
        return success(results);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getRecognition(String fileId, OcrType type) throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        FileManager fileManager = (FileManager) AppContext.getBean("fileManager");

        String ticket = mplusApi.getTicket(SERVICE_CODE);
        String domain = mplusApi.getDomain();
        String url = domain + "/svr/ocr?ticket=" + ticket + "&type=" + type.getSortNum();
        File file = fileManager.getFile(Long.parseLong(fileId), DateUtil.currentDate());
        File compressFile = new File(file.getParentFile(), file.getName() + "_compress");
        try {
            ImgUtil.compress(file, compressFile);
        } catch (IOException e) {
            throw new BusinessException("图片压缩出错");
        }

        Map<String, Object> res = HttpClientUtil.doFilePost(url, compressFile, null);
        if (!res.containsKey("data"))
            throw new BusinessException(String.valueOf(res.get("msg")));

        return (Map<String, Object>) res.get("data");
    }

    private boolean fieldIsPermission(String ocrType, String ocrSubProp, FormFieldBean formField) {
        FormFieldComEnum[] supported = getPropSupportType(ocrType, ocrSubProp);
        for (FormFieldComEnum formFieldComEnum : supported) {
            //ocr不支持回填数字类型的字段
            if (formFieldComEnum.getKey().equalsIgnoreCase(formField.getInputType()) && !"DECIMAL".equalsIgnoreCase(formField.getFieldType()))
                return true;
        }
        return false;
    }

    private List<Map<String, Object>> getSubProperty(OcrType ocrType) {
        switch (ocrType) {
            case IDENTIFICATION_CARD_FRONT:
                return IdCardProp.list();
            case IDENTIFICATION_CARD_BACK:
                return IdCardBackProp.list();
            case VISITING_CARD:
                return VisitCardProp.list();
            case BANK_CARD:
                return BankCardProp.list();
            case BUSINESS_LICENCE:
                return BusinessLicenceProp.list();
            case TAX_REGISTRATION_CERTIFICATE:
                return TaxRegCertProp.list();
            case ORGANIZATION_CODE_CERTIFICATE:
                return OrgCodeCertProp.list();
            default:
                return Collections.emptyList();
        }
    }

    private FormFieldComEnum[] getPropSupportType(String ocrType, String ocrSubProp) {
        OcrType type = OcrType.getByKey(ocrType);
        switch (type) {
            case IDENTIFICATION_CARD_FRONT:
                return IdCardProp.getByKey(ocrSubProp).getSupportFieldType();
            case IDENTIFICATION_CARD_BACK:
                return IdCardBackProp.getByKey(ocrSubProp).getSupportFieldType();
            case VISITING_CARD:
                return VisitCardProp.getByKey(ocrSubProp).getSupportFieldType();
            case BANK_CARD:
                return BankCardProp.getByKey(ocrSubProp).getSupportFieldType();
            case BUSINESS_LICENCE:
                return BusinessLicenceProp.getByKey(ocrSubProp).getSupportFieldType();
            case TAX_REGISTRATION_CERTIFICATE:
                return TaxRegCertProp.getByKey(ocrSubProp).getSupportFieldType();
            case ORGANIZATION_CODE_CERTIFICATE:
                return OrgCodeCertProp.getByKey(ocrSubProp).getSupportFieldType();
            default:
                return new FormFieldComEnum[]{};
        }
    }
}
