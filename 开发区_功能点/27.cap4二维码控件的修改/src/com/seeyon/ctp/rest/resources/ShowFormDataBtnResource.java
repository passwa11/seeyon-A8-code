package com.seeyon.ctp.rest.resources;

import com.seeyon.cap4.form.bean.FormRelationshipBean;
import com.seeyon.cap4.form.manager.ShowFormDataBtnManager;
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
 * @author chenxb
 * @desc 应用绑定处查看表单自定义按钮rest接口类
 * @date 2019-1-4 15:08
 * @since A8+  seeyon.com
 */
@Path("cap4/showFormDataBtn")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShowFormDataBtnResource extends BaseResource {
    private CAP4FormBusinessRelationManager cap4FormBusinessRelationManager = (CAP4FormBusinessRelationManager)AppContext.getBean("cap4FormBusinessRelationManager");
    private CAP4FormCacheManager cap4FormCacheManager = (CAP4FormCacheManager)AppContext.getBean("cap4FormCacheManager");
    /**
     * 获取当前表单设置了哪些系统关联
     *
     * @param formId 当前表单ID
     * @return 系统关联json信息，包含关联的条件、映射、穿透权限
     */
    @GET
    @Path("formRelations")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.1")
    public Response formRelations(@QueryParam("formId") String formId,@QueryParam("tableName") String tableName) {
        Map<String, Object> relationsInfoMap = cap4FormBusinessRelationManager.filterRelationship4QueryBtn(Long.parseLong(formId),tableName);
        relationsInfoMap.put("success", true);
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

    /**
     * 应用绑定新建按钮处理映射数据
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getShowRecord")
    public Response getShowRecord(Map<String, Object> params) {
        ShowFormDataBtnManager showFormDataBtnManager = (ShowFormDataBtnManager) AppContext.getBean("showFormDataBtnManager");
        return success(showFormDataBtnManager.getShowRecord(params));
    }
}
