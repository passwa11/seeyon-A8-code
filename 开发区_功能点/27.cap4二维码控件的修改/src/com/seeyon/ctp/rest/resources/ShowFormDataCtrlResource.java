package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.FormRelationshipBean;
import com.seeyon.cap4.form.manager.ShowFormDataCtrlManager;
import com.seeyon.cap4.form.modules.engin.businessRelation.CAP4FormBusinessRelationManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by weijh on 2018-1-3.
 * 查看表单数据按钮自定义按钮控件rest接口类
 */
@Path("cap4/showFormDataCtrl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShowFormDataCtrlResource extends BaseResource{

    private CAP4FormBusinessRelationManager cap4FormBusinessRelationManager = (CAP4FormBusinessRelationManager)AppContext.getBean("cap4FormBusinessRelationManager");
    private CAP4FormCacheManager cap4FormCacheManager = (CAP4FormCacheManager)AppContext.getBean("cap4FormCacheManager");
    /**
     * 获取查看控件 打开表单所需参数
     */
    @POST
    @Path("getShowRecord")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getShowRecord(Map<String, Object> params) {
        ShowFormDataCtrlManager showFormDataCtrlManager = (ShowFormDataCtrlManager) AppContext.getBean("showFormDataCtrlManager");
        return success(showFormDataCtrlManager.getShowRecord(params));
    }

    /**
     * 获取当前表单设置了哪些系统关联
     * @param formId
     * @return
     */
    @GET
    @Path("formRelations")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response formRelations(@QueryParam("formId") String formId, @QueryParam("tableName") String tableName){
        Map<String,Object> relationsInfoMap = cap4FormBusinessRelationManager.filterRelationship4QueryBtn(Long.parseLong(formId),tableName);
        relationsInfoMap.put("success",true);
        return success(relationsInfoMap);
    }

    /**
     * 根据关联ID，获取关联条件
     * */
    @GET
    @Path("getRelationConditionById")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getRelationConditionById(@QueryParam("relationId") String relationId){
        FormRelationshipBean relationshipBean = cap4FormCacheManager.getFormRelationshipBean(Long.valueOf(relationId));
        Map<String, String> conditionMap = relationshipBean.getRelationMapList().get(0).getSystemRelationConditionMap();
        return success(conditionMap);
    }

    /**
     * 根据关联ID，获取当前关联的所有信息，用于按钮设置态修改、显示
     * */
    @GET
    @Path("getRelationDetailInfoMapById")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response getRelationDetailInfoMapById(@QueryParam("relationId") String relationId) {
        Map<String, Object> detailInfoMap = cap4FormBusinessRelationManager.getSysRelationDetailInfoMap(relationId);
        return success(detailInfoMap);
    }
}
