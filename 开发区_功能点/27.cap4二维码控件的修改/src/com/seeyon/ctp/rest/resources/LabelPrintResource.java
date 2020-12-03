package com.seeyon.ctp.rest.resources;


import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFlowBusinessBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCtrl;
import com.seeyon.cap4.form.manager.PrintTemplateManager;
import com.seeyon.cap4.form.po.PrintTemplate;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: 标签打印按钮自定义控件资源
 * </p>
 * <p>
 * Description: 标签打印按钮自定义控件接口
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 *
 * @since CAP4.0
 */
@Path("cap4/labelPrint")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class LabelPrintResource extends BaseResource  {
    private static final Log LOGGER = CtpLogFactory.getLog(LabelPrintResource.class);

    /**
     * 获取当前环境所有的标签打印模板
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("allPrintTemplate")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response allPrintTemplate() throws BusinessException {
        PrintTemplateManager printTemplateManager = (PrintTemplateManager) AppContext.getBean("printTemplateManager");
        List<PrintTemplate> templates =  printTemplateManager.selectAllTemplate();
        Map<String,Object> result = new HashMap<String,Object>();
        List<Map<String,Object>> templateMaps = new ArrayList<Map<String, Object>>();
        for(PrintTemplate template:templates){
            Map<String,Object> templateMap = new HashMap<String, Object>();
            templateMap.put("id",String.valueOf(template.getId()));
            templateMap.put("templateName",template.getTemplateName());
            templateMap.put("content",template.getContent());
            templateMaps.add(templateMap);
        }
        result.put("templates",templateMaps);
        return success(result);
    }

    /**
     * 选择数据之后点击打印按钮的响应接口，获取按钮绑定的模板信息以及所选的表单数据信息。
     * @param params {formId：表单id，labelPrintBtnId：标签打印按钮id，templateId：应用绑定模板id，dataIds：数据id（多个以逗号隔开）}
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("selectedPrintData")
    public Response selectedPrintData(Map<String, Object> params) throws BusinessException {
        Map<String,Object> result = new HashMap<String,Object>();
        Long formId = Long.parseLong(String.valueOf(params.get("formId")));//表单id
        String labelPrintBtnId = String.valueOf(params.get("labelPrintBtnId"));//按钮id
        Long templateId = Long.parseLong(String.valueOf(params.get("templateId")));//应用绑定模板id
        String dataIds = String.valueOf(params.get("dataIds"));//所选数据id，多个以逗号分隔
        List<String> dataIdArray = Arrays.asList(dataIds.split(","));
        if(dataIdArray.size()<=0){
            throw new BusinessException("dataIds is empty!");
        }
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(formId,false);
        PrintTemplateManager printTemplateManager = (PrintTemplateManager) AppContext.getBean("printTemplateManager");
        List<Long> dataIdLong = new ArrayList<Long>();
        for(String dataId:dataIdArray){
            dataIdLong.add(Long.parseLong(dataId));
        }
        Map<String, Object> printSetting = null;
        if(formBean.isFlowForm()){
            FormFlowBusinessBean formFlowBusinessBean = formBean.getBind().getFlowBusinessBean(String.valueOf(templateId));
            printSetting = formFlowBusinessBean.getCustomBtnInfoById(labelPrintBtnId);
        }else{
            FormBindAuthBean formBindAuthBean = formBean.getBind().getUnFlowTemplateById(templateId);
            printSetting = formBindAuthBean.getCustomBtnInfoById(labelPrintBtnId);
        }
        if(null==printSetting){
            throw new BusinessException("find labelPrintBtn info error!");
        }
        String customParam = (String)printSetting.get("customParam");
        if(StringUtil.checkNull(customParam)){
            throw new BusinessException(ResourceUtil.getString("com.cap.btn.labelPrintBtn.noSetTempelete"));
        }
        Map<String,Object> labelPrintInfo = (Map<String,Object>)JSONUtil.parseJSONString(customParam);
        Map<String,Object> infoMap = (Map<String,Object>)labelPrintInfo.get("labelPrintInfo");
        Map<String,Object> info = (Map<String,Object>)infoMap.get("labelPrintInfo");
        String printTemplateId = String.valueOf(info.get("templateId"));
        try {
            PrintTemplate printTemplate = printTemplateManager.selectTemplateById(Long.parseLong(printTemplateId));
            result.put("content",printTemplate.getContent());
            List<Map<String,Object>> mappings = (List<Map<String,Object>>)info.get("data");
            Set<String> fields = new HashSet<String>();
            for(Map<String,Object> mapping : mappings){
                String fieldName = (String)mapping.get("fieldId");
                if(null!=fieldName && !fieldName.equals("")) {
                    fields.add(fieldName);
                }
            }
            if(fields.size()<=0){
                throw new BusinessException(ResourceUtil.getString("com.cap.btn.labelPrintBtn.noMappingSet"));
            }
            String[] fieldArray = new String[fields.size()];
            fields.toArray(fieldArray);
            Long[] idLongs = new Long[dataIdLong.size()];
            dataIdLong.toArray(idLongs);
            FormTableBean masterTableBean = formBean.getMasterTableBean();
            List<FormDataMasterBean> datas = printTemplateManager.selectMasterDataById(idLongs,masterTableBean,fieldArray);
            if(datas.size()<=0){
                throw new BusinessException("no form data find!");
            }
            Map<Long,FormDataMasterBean> orderMap = new HashMap<Long, FormDataMasterBean>();
            for(FormDataMasterBean dataMasterBean:datas){
                orderMap.put(dataMasterBean.getId(),dataMasterBean);
            }
            List<Map<String,Object>> formDataMap = new ArrayList<Map<String, Object>>();
            for(Long id:idLongs){
                FormDataMasterBean dataMasterBean = orderMap.get(id);
                Map<String,Object> dataMap = new HashMap<String, Object>();
                dataMap.put("dataId",String.valueOf(dataMasterBean.getId()));
                formDataMap.add(dataMap);
                for(String fieldName:fields){
                    FormFieldBean fieldBean = masterTableBean.getFieldBeanByName(fieldName);
                    FormFieldCtrl fieldCtrl = fieldBean.getFieldCtrl();
                    Object printVal = fieldCtrl.getLabelPrintVal(fieldBean,dataMasterBean);
                    dataMap.put(fieldName,printVal);
                }
            }
            result.put("formDatas",formDataMap);
            result.put("customParam",customParam);
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(),e);
            throw e;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
            throw new BusinessException(e);
        }
        return success(result);
    }

}
