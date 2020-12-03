package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.mplus.api.MplusApi;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.fieldCtrl.EinvoiceFieldType;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.cap4.template.util.HttpClientUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.json.JSONUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangz on 2018/3/20.
 * 自定义控件--电子发票
 */
@Path("cap4/formEinvoice")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class FormEinvoiceResource extends BaseResource {

    final String serviceCode = "m20000000000003003";

    //云联中心解析电子发票的接口地址
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getEinvoiceFieldInfo")
    public Response getEinvoiceFieldInfo(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        Map<String, Object> res = new HashMap<String, Object>();
        Map<String, Object> einvoiceFields = new HashMap<String, Object>();
        for (EinvoiceFieldType type : EinvoiceFieldType.values()) {
            einvoiceFields.put(type.getKey(), type.getText());
        }
        DataContainer formFieldMap = getFieldsJsonObject(formId, fieldType, currentField);
        res.put("einvoiceMap", einvoiceFields);
        res.put("formFieldMap", formFieldMap);
        return success(res);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getFormFields")
    public Response getFormFields(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        DataContainer result = getFieldsJsonObject(formId, fieldType, currentField);
        return success(result);
    }

    private DataContainer getFieldsJsonObject(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        FormTableBean currentTable = formBean.getFormTableBeanByFieldName(currentField);
        DataContainer dc = new DataContainer();
        //如果fieldType为空，说明是要获取本表所有字段
        //if(StringUtil.checkNull(fieldType)){
        List<FormFieldBean> currentTableFields = currentTable.getFields();
        for (FormFieldBean field : currentTableFields) {
            if (!field.isCustomerCtrl() && EinvoiceFieldType.isFieldCanBeChoose(field.getInputType())) {
                Map<String, Object> tempMap = field.getJsonObj4Design(false);
                tempMap.put("ownerTableName", field.getOwnerTableName());
                dc.put(field.getName(), tempMap);
            }
        }
        return dc;
    }


    /**
     * 电子发票上传附件之后的回调rest
     *
     * @param formId    表单id
     * @param rightId   权限id
     * @param fieldName 当前发票控件是哪个字段fieldxxxx
     * @param fileId    发票文件id
     * @param masterId  当前这条表单数据id
     * @param subId     如果是明细表字段，需要传递当前明细表行数据id
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parseEinvoiceFileAndFillBack")
    @SuppressWarnings("unchecked")
    public Response parseEinvoiceFileAndFillBack(@QueryParam("formId") String formId, @QueryParam("rightId") String rightId, @QueryParam("fieldName") String fieldName, @QueryParam("fileId") String fileId,
                                                 @QueryParam("masterId") String masterId, @QueryParam("subId") String subId) throws BusinessException {
        Map<String, Object> result = new HashMap<String, Object>();
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
        FormFieldBean field = formBean.getFieldBeanByName(fieldName);
        //FormAuthViewBean auth = formBean.getAuthViewBeanById(Long.parseLong(rightId));
        //解析配置的映射
        String customParams = field.getCustomParam();
        //设置的时候可能会出现没有进行数据绑定的情况
        if(StringUtil.checkNull(customParams)||"{}".equals(customParams)){
            throw new BusinessException("此电子发票控件没有设置属性映射！");
        }else {
            MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
            //通过serviceCode获取ticket
            String ticket = mplusApi.getTicket(serviceCode);
            String domain = mplusApi.getDomain();
            String url = domain + "/svr/invoice/pdf?ticket=" + ticket;
            FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
            File file = fileManager.getFile(Long.parseLong(fileId), DateUtil.currentDate());
            Map<String, Object> res = HttpClientUtil.doFilePost(url, file, null);
            if (!"1000".equals(String.valueOf(res.get("code")))) {
                String msg = (String) res.get("msg");
                if (StringUtil.checkNull(msg)) {
                    msg = "Cloud Union Central Interface Call Error！code:" + res.get("code");
                } else {
                    msg = msg + "code:" + res.get("code") + "。";
                }
                throw new BusinessException(msg);
            } else {
                //通过主表数据id从session中取到数据对象
                FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.parseLong(masterId));
                if (null == cacheFormData) {
                    throw new BusinessException("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
                }
                FormDataSubBean formSubData = null;
                if (!field.isMasterField()) {
                    //如果是明细表字段，通过明细表数据id取到明细表数据行对象
                    formSubData = cacheFormData.getFormDataSubBeanById(field.getOwnerTableName(), Long.parseLong(subId));
                    if (null == formSubData) {
                        throw new BusinessException("电子发票控件是明细表字段，但是通过明细表行id：" + subId + "找不到明细表数据");
                    }
                }
                Map<String, Object> data = (Map<String, Object>) res.get("data");
                Map<String, Object> definition = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
                List<Map<String, String>> array = (List<Map<String, String>>) definition.get("mapping");
                for (Map<String, String> defMap : array) {
                    String source = defMap.get("source");
                    String target = defMap.get("target");
                    FormFieldBean conffield = formBean.getFieldBeanByName(target);
                    Object val = data.get(source);
                    if ("[]".equals(String.valueOf(val))) {
                        val = "";
                    }
                    //主表字段
                    if (conffield.isMasterField()) {
                        //合并数据到内存中
                        cacheFormData.addFieldValue(target, val);
                        val = cacheFormData.getFieldValue(target);
                        Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
                        result.put(target, tempRes);
                    } else {//明细表字段
                        if(null!=formSubData) {
                            //合并数据到内存中
                            formSubData.addFieldValue(target, val);
                            val = formSubData.getFieldValue(target);
                            Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
                            //明细表字段在构造回填map的时候，key中添加明细表行的id，方便前端拿到数据回填到对应的明细表行上
                            result.put(target + "_" + formSubData.getId(), tempRes);
                        }
                    }
                }
            }
        }
        return success(result);
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkMapping")
    @SuppressWarnings("unchecked")
    public Response checkMapping(Map<String, Object> params) {
        Long formId = Long.parseLong("" + params.get("formId"));
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
        Object datas = params.get("datas");
        List<Map<String, String>> mappings = (List<Map<String, String>>) datas;
        boolean checkResult = true;
        String errorMsg = "";
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map<String, String> mapping : mappings) {
            String source = mapping.get("source");
            String target = mapping.get("target");
            FormFieldBean fieldBean = formBean.getFieldBeanByName(target);
            EinvoiceFieldType sourceType = EinvoiceFieldType.getEnumByKey(source);
            FormFieldComEnum[] supportTypes = sourceType.getSupportFieldType();
            boolean hasEquals = false;
            for (FormFieldComEnum supportField : supportTypes) {
                if (supportField.getKey().equals(fieldBean.getInputType())) {
                    //是数字,只支持发票合计金额、加税合计小写、税额合计、金额明细、税率明细、税额明细
                    if (fieldBean.getFieldType().equals(Enums.FieldType.DECIMAL.getKey())) {
                        if (source.equalsIgnoreCase(EinvoiceFieldType.TOTALINVOICE.getKey())
                                || source.equalsIgnoreCase(EinvoiceFieldType.VALOREMSUM.getKey())
                                || source.equalsIgnoreCase(EinvoiceFieldType.AMOUNTTAXSUM.getKey())) {
                            hasEquals = true;
                            break;
                        }
                    } else {
                        hasEquals = true;
                        break;
                    }
                }
            }
            if (!hasEquals) {
                String supportTypeStrs = "";
                FormFieldComEnum[] types = sourceType.getSupportFieldType();
                for (int i = 0; i < types.length; i++) {
                    FormFieldComEnum t = types[i];
                    //最后一个
                    if (i == types.length - 1) {
                        supportTypeStrs = supportTypeStrs + t.getText();
                    } else {
                        supportTypeStrs = supportTypeStrs + t.getText() + "、";
                    }
                }
                String fieldType = "";
                if (fieldBean.isNumberField()) {
                    fieldType = ResourceUtil.getString("cap.formDesign.ctrlArea.number");
                } else {
                    fieldType = fieldBean.getFieldCtrl().getText();
                }
                errorMsg = sourceType.getText() + "[" + supportTypeStrs + "]->" + fieldBean.getDisplay() + "[" + fieldType + "] ctrl type is not same！";
                result.put("errorMsg", errorMsg);
                checkResult = false;
                break;
            }
        }
        result.put("result", String.valueOf(checkResult));
        return success(result);
    }
}
