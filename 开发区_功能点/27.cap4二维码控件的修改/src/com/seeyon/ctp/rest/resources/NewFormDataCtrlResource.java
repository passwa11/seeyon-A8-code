package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.manager.NewFormDataCtrlManager;
import com.seeyon.cap4.form.modules.business.BusinessManager;
import com.seeyon.cap4.form.po.CAPFormDefinition;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-29.
 * 新建表单按钮自定义按钮控件rest接口类
 */
@Path("cap4/newFormDataCtrl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NewFormDataCtrlResource  extends BaseResource {

    /**
     * 表单内按钮处理映射数据
     * params{formId:"表单ID",contentDataId:"数据ID",fieldName:"字段name",recordId:"明细表记录ID，在明细表中时比传"}
     * @return
     */
    @POST
    @Path("dealMappingData")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response dealMappingData(Map<String, Object> params) {
        NewFormDataCtrlManager newFormDataCtrlManager = (NewFormDataCtrlManager) AppContext.getBean("newFormDataCtrlManager");
        return success(newFormDataCtrlManager.dealMappingData(params));
    }

    /**
     * 1、如果传递的表单id是应用内的表单，则返回应用内的所有无流程应用绑定和有流程应用绑定
     * 2、如果传递的表单id是表单管理中的表单，则返回本单位所有流程表单应用绑定和无流程应用绑定
     * @return
     */
    @GET
    @Path("sameBizFormBinds")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response sameBizFormBinds(@QueryParam("currentFormId") String currentFormId, @QueryParam("bizConfigId") String bizConfigId) throws BusinessException {
        Long formId = Long.parseLong(currentFormId);
        NewFormDataCtrlManager newFormDataCtrlManager = (NewFormDataCtrlManager)AppContext.getBean("newFormDataCtrlManager");
        BusinessManager businessManager4 = (BusinessManager)AppContext.getBean("businessManager4");
        //如果前端没有传递bizConfigId，则说明是单表表单
        List<Long> formIds = null;
        if(StringUtil.checkNull(bizConfigId)){
            formIds = newFormDataCtrlManager.getCurrentAccountFormIds();
        }else{//否则是应用内的表单
            List<CAPFormDefinition> formDefinitions = businessManager4.getCAP4FormByBizId(Long.parseLong(bizConfigId));
            formIds = new ArrayList<Long>();
            for(CAPFormDefinition formDefinition : formDefinitions){
                formIds.add(formDefinition.getId());
            }
        }
        List<Map<String,Object>> bindInfos = getBindInfos(formIds,formId);
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("success","true");
        result.put("result",bindInfos);
        return success(result);
    }

    /**
     * 通过表单定义id获取表单主表、明细表以及字段信息，如果表单或者模板找不到了，则返回信息中deleteTag为true
     * @param formId 表单定义id
     * @param bindId 模板id
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("formTableInfo")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response formTableInfo(@QueryParam("formId") String formId,@QueryParam("bindId") String bindId) throws BusinessException {
        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(Long.parseLong(formId),false);
        Map<String, Object> result = new HashMap<String, Object>();
        if(null != formBean) {
            if(!StringUtil.checkNull(bindId)) {
                if(formBean.isFlowForm()){
                    TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
                    CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.parseLong(bindId));
                    boolean delete = (null == ctpTemplate || ctpTemplate.isDelete());
                    if(delete){
                        result.put("deleteTag", true);
                    }
                }else {
                    FormBindAuthBean formBindAuthBean = formBean.getBind().getUnFlowTemplateById(Long.parseLong(bindId));
                    if (null == formBindAuthBean) {
                        result.put("deleteTag", true);
                    }
                }
            }
            List<Map<String, Object>> tableInfoMaps = new ArrayList<Map<String, Object>>();
            List<FormTableBean> tableBeans = formBean.getTableList();
            for (FormTableBean tableBean : tableBeans) {
                Map<String, Object> tableInfoMap = new HashMap<String, Object>();
                tableInfoMap.put("tableName", tableBean.getTableName());
                List<Map<String, Object>> fieldInfoMaps = new ArrayList<Map<String, Object>>();
                List<FormFieldBean> fields = tableBean.getFields();
                for (FormFieldBean fieldBean : fields) {
                    fieldInfoMaps.add(fieldBean.getJsonObj4Design(false, true));
                }
                tableInfoMap.put("fieldInfo", fieldInfoMaps);
                tableInfoMaps.add(tableInfoMap);
            }
            result.put("result", tableInfoMaps);
        }else{
            result.put("deleteTag",true);
        }
        result.put("success", true);
        return success(result);
    }

    private List<Map<String,Object>> getBindInfos(List<Long> formIds,Long currentFormId) throws BusinessException {
        List<Map<String,Object>> result = new ArrayList<Map<String, Object>>();
        CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
        for(Long id:formIds){
            FormBean formBean = cap4FormManager.getForm(id,false);
            if(null != formBean){
                if (formBean.isFlowForm()){
                    List<CtpTemplate> templateList = cap4FormManager.getFormSystemTemplate(formBean.getId());
                    for(CtpTemplate template:templateList) {
                        Map<String, Object> formBeanInfoMap = new HashMap<String, Object>();
                        formBeanInfoMap.put("formId", String.valueOf(id));
                        formBeanInfoMap.put("formType", String.valueOf(formBean.getFormType()));
                        formBeanInfoMap.put("bindId",String.valueOf(template.getId()));
                        formBeanInfoMap.put("bindName",template.getSubject());
                        result.add(formBeanInfoMap);
                    }
                }else{
                    Map<String, FormBindAuthBean> unflowFormBinds = formBean.getBind().getUnFlowTemplateMap();
                    for(Map.Entry<String, FormBindAuthBean> unflowEntry:unflowFormBinds.entrySet()){
                        FormBindAuthBean bindAuthBean = unflowEntry.getValue();
                        Map<String, Object> formBeanInfoMap = new HashMap<String, Object>();
                        formBeanInfoMap.put("formId", String.valueOf(id));
                        formBeanInfoMap.put("formType", String.valueOf(formBean.getFormType()));
                        formBeanInfoMap.put("bindId",String.valueOf(bindAuthBean.getId()));
                        formBeanInfoMap.put("bindName",bindAuthBean.getName());
                        result.add(formBeanInfoMap);
                    }
                }

            }
        }
        return result;
    }
}
