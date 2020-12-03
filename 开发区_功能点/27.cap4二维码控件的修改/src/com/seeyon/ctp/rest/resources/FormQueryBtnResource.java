package com.seeyon.ctp.rest.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.report.engine.api.ReportConstants.FieldComType;
import com.seeyon.ctp.report.engine.api.ReportConstants.FieldType;
import com.seeyon.ctp.report.engine.api.ReportConstants.UserConModel;
import com.seeyon.ctp.report.engine.api.bo.FilterFieldBO;
import com.seeyon.ctp.report.engine.api.bo.ReportConfig;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.report.engine.api.manager.ReportResultApi;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;



import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: 查询统计按钮自定义控件资源
 * </p>
 * <p>
 * Description: 查询统计按钮自定义控件接口
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
@Path("cap4/formquerybtn")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class FormQueryBtnResource extends BaseResource {

    private static final Log LOGGER = CtpLogFactory.getLog(FormQueryBtnResource.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getQueryDefinitions")
    /**
     * 根据类型查找当前登录人有权限的查询或者统计，designType 类型 （"query"查询,"statistics"统计）
     * @apiParam  {String} [designType] 类型
     */
    public Response getQueryDefinitions(@QueryParam("designType") String designType) {
        ReportApi reportApi =  (ReportApi)AppContext.getBean("reportApi");
        Map<String,Object> param = new HashMap<String, Object>();
        List<Map<String,Object>> designs = null;
        Map<String,Object> result = new HashMap<String, Object>();
        List<Map<String,Object>> designJsons = new ArrayList<Map<String,Object>>();

        /**
         * 添加应用类型的查询条件
         */
        List<String> categoryList = new ArrayList<String>();
        categoryList.add(ApplicationCategoryEnum.global.name());
        categoryList.add(ApplicationCategoryEnum.cap4biz.name());

        try {
            if("query".equalsIgnoreCase(designType)){
                param.put("designType","QUERY");
                param.put("categoryList", categoryList);
                designs = reportApi.findDesignWithoutAuth(param);
            }else if("statistics".equalsIgnoreCase(designType)){
                param.put("designType","STATISTICS");
                param.put("categoryList", categoryList);
                designs = reportApi.findDesignWithoutAuth(param);
                if(null==designs || designs.isEmpty()){
                    designs = new ArrayList<Map<String, Object>>();
                }
                param.put("designType","MULTSTATS");
                designs.addAll(reportApi.findDesignWithoutAuth(param));
            }
            if(null!=designs) {
                for (Map<String, Object> r : designs) {
                    Map<String,Object> designJsonObj = new HashMap<String, Object>();
                    designJsonObj.put("designId",String.valueOf(r.get("designId")));
                    designJsonObj.put("categoryId",String.valueOf(r.get("categoryId")));
                    designJsonObj.put("category",String.valueOf(r.get("category")));
                    designJsonObj.put("title",String.valueOf(r.get("title")));
                    designJsonObj.put("designType",String.valueOf(r.get("designType")));
                    designJsons.add(designJsonObj);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error(e.getMessage(),e);
        }
        result.put("result",designJsons);
        return success(result);
    }

    /**
     * 获取已选查询统计的筛选条件
     * @param designId
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("condition")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response getCondition(@QueryParam("designId") String designId) throws BusinessException {
        ReportApi reportApi = (ReportApi)AppContext.getBean("reportApi");
        if(reportApi == null) {
            throw new BusinessException("无法调用报表API");
        }
        //获取查询统计的筛选条件
        Map<String,Object> reportConfig = reportApi.getReport(Long.parseLong(designId));
        List<FilterFieldBO> filterCondition = (List<FilterFieldBO>) reportConfig.get(ReportConfig.filterFields);
        return success(filterCondition);
    }

    /**
     * 处理查询统计筛选条件
     * （获取配置的字段值，放入session中）
     * @param formId
     * @param fieldName
     * @param formDataId
     * @param formSubDataId
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("dealCdtMapping")
    @RestInterfaceAnnotation(OpenExternal = RestInterfaceAnnotation.External.NO, StartVersion = "V7.0")
    public Response dealCdtMapping(@QueryParam("formId") String formId, @QueryParam("fieldName") String fieldName, @QueryParam("formDataId") String formDataId, @QueryParam("formSubDataId") String formSubDataId, @QueryParam("designId") String designId) throws BusinessException, SQLException {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        FormBean formBean = cap4FormManager.getForm(Long.parseLong(formId), false);
        FormFieldBean field = formBean.getFieldBeanByName(fieldName);

        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.parseLong(formDataId));

        //查看状态下从数据库中获取FormDataMasterBean
        if (null == cacheFormData) {
            cacheFormData = cap4FormManager.getDataMasterBeanById(Long.parseLong(formDataId), formBean, null);
        }
        if (null == cacheFormData) {
            throw new BusinessException("表单数据在session中找不到（masterId:" + formDataId + "），请尝试重新打开。");
        }
        FormDataSubBean formSubData = null;
        if (!field.isMasterField()) {
            formSubData = cacheFormData.getFormDataSubBeanById(field.getOwnerTableName(), Long.parseLong(formSubDataId));
            if (null == formSubData) {
                throw new BusinessException("查询统计控件是明细表字段，但是通过明细表行id：" + formSubDataId + "找不到明细表数据");
            }
        }

//        //增加权限控制
//        ReportApi reportApi =  (ReportApi)AppContext.getBean("reportApi");
//        reportApi.addAccessControl(Long.parseLong(designId), AppContext.currentUserId());
        
        String customParams = field.getCustomParam();
        Map<String,Object> define = (Map<String,Object>) JSONUtil.parseJSONString(customParams);
        List<Map<String,Map<String,Object>>> mapping = (List<Map<String,Map<String,Object>>>) define.get("mapping");
        if(mapping == null) {
        	return success("No filter condition mapping is set");
        }
        ReportApi reportApi = (ReportApi)AppContext.getBean("reportApi");
        ReportResultApi reportResultApi = (ReportResultApi)AppContext.getBean("reportResultApi");
        if(reportApi == null || null == reportResultApi) {
            throw new BusinessException("无法调用报表API");
        }
        ReportConfig design = reportApi.getReport(Long.valueOf(designId));
        String category = MapUtils.getString(design, ReportConfig.category);
        Map<String,Object> ret = Maps.newHashMap();
        ret.put("category", category);
        if(ApplicationCategoryEnum.global.name().equals(category)) {
        	List<Map<String, Object>> userCondition = Lists.newArrayList();
        	for(int i = 0;i < mapping.size();i++){
                Map<String,Map<String,Object>> defineMap = mapping.get(i);
                Map<String,Object> valueMap = Maps.newHashMap();
                Map<String,Object> cdtInfo = defineMap.get("source");
                Map<String,Object> fieldInfo = defineMap.get("target");
                if(cdtInfo == null || fieldInfo == null) {
                    continue;
                }
                String mappedFieldName = (String)fieldInfo.get("name");
                FormFieldBean formFieldBean = formBean.getFieldBeanByName(mappedFieldName);
                Object fieldValue;
                if(formFieldBean.isMasterField()){
                    fieldValue = cacheFormData.getFieldValue(mappedFieldName);
                }else{
                    fieldValue = formSubData.getFieldValue(mappedFieldName);
                }
                valueMap.put("leftChar", "(");
                valueMap.put("rightChar", ")");
                valueMap.put("aliasTableName", MapUtils.getString(cdtInfo, "aliasTableName"));
                valueMap.put("fieldName", MapUtils.getString(cdtInfo, "name"));
                String fieldComType = MapUtils.getString(cdtInfo, "fieldComType");
                String dbType = MapUtils.getString(cdtInfo, "dbType");
                
                if(null == fieldValue || StringUtils.isBlank(fieldValue.toString())) {
                	fieldValue = "";
                }else {
                	if((FieldComType.TEXT.getComName().equals(fieldComType) && FieldType.DECIMAL.name().equals(dbType))
                			|| FieldComType.DATE.getComName().equals(fieldComType)
                			|| FieldComType.DATETIME.getComName().equals(fieldComType)) {
                		valueMap.put("operation", "Equal");
                	}
                	if(FieldComType.DATE.getComName().equals(fieldComType)) {
                		if(fieldValue instanceof Date) {
                			fieldValue = Datetimes.formatDate((Date)fieldValue);
                		}
                	}else if(FieldComType.DATETIME.getComName().equals(fieldComType)) {
                		if(fieldValue instanceof Date) {
                			fieldValue = Datetimes.formatDatetime((Date)fieldValue);
                		}
                	}else if(FieldComType.MEMBER.getComName().equals(fieldComType)) {
                		fieldValue = JSONUtil.toJSONString(Lists.newArrayList("Member|"+fieldValue));
                	}else if(FieldComType.MULTIMEMBER.getComName().equals(fieldComType)) {
                		String[] arr = fieldValue.toString().split(",");
                		StringBuilder sb = new StringBuilder();
                		for(String str :arr) {
                			sb.append(",").append("Member|").append(str);
                		}
                		fieldValue = sb.deleteCharAt(0).toString();
                	}else if(FieldComType.DEPARTMENT.getComName().equals(fieldComType)) {
                		fieldValue = JSONUtil.toJSONString(Lists.newArrayList("Department|"+fieldValue+"|1"));
                	}else if(FieldComType.MULTIDEPARTMENT.getComName().equals(fieldComType)) {
                		String[] arr = fieldValue.toString().split(",");
                		StringBuilder sb = new StringBuilder();
                		for(String str :arr) {
                			sb.append(",").append("Department|").append(str).append("|1");
                		}
                		fieldValue = sb.deleteCharAt(0).toString();
                	}else if(FieldComType.LEVEL.getComName().equals(fieldComType)) {
                		fieldValue = Lists.newArrayList("Level|"+fieldValue);
                	}else if(FieldComType.MULTILEVEL.getComName().equals(fieldComType)) {
                		String[] arr = fieldValue.toString().split(",");
                		StringBuilder sb = new StringBuilder();
                		for(String str :arr) {
                			sb.append(",").append("Level|").append(str);
                		}
                		fieldValue = sb.deleteCharAt(0).toString();
                	}else if(FieldComType.POST.getComName().equals(fieldComType)) {
                		fieldValue = Lists.newArrayList("Post|"+fieldValue);
                	}else if(FieldComType.MULTIPOST.getComName().equals(fieldComType)) {
                		String[] arr = fieldValue.toString().split(",");
                		StringBuilder sb = new StringBuilder();
                		for(String str :arr) {
                			sb.append(",").append("Post|").append(str);
                		}
                		fieldValue = sb.deleteCharAt(0).toString();
                	}else if(FieldComType.ACCOUNT.getComName().equals(fieldComType)) {
                		fieldValue = JSONUtil.toJSONString(Lists.newArrayList("Account|"+fieldValue));
                	}else if(FieldComType.MULTIACCOUNT.getComName().equals(fieldComType)) {
                		String[] arr = fieldValue.toString().split(",");
                		StringBuilder sb = new StringBuilder();
                		for(String str :arr) {
                			sb.append(",").append("Account|").append(str);
                		}
                		fieldValue = sb.deleteCharAt(0).toString();
                	}
                }
                valueMap.put("fieldValue",fieldValue);
                userCondition.add(valueMap);
            }
        	Long conditionId = reportResultApi.setUserCondition(UserConModel.BASIC, userCondition);
        	ret.put("conditionId", conditionId.toString());
        }else {
        	List<Map<Object,Object>> result = new ArrayList<Map<Object, Object>>();
            for(int i = 0;i < mapping.size();i++){
                Map<String,Map<String,Object>> defineMap = mapping.get(i);
                Map<Object,Object> valueMap = new HashMap<Object, Object>();
                Map<String,Object> cdtInfo = defineMap.get("source");
                Map<String,Object> fieldInfo = defineMap.get("target");

                if(cdtInfo == null || fieldInfo == null) {
                    continue;
                }

                String mappedFieldName = (String)fieldInfo.get("name");
                FormFieldBean formFieldBean = formBean.getFieldBeanByName(mappedFieldName);
                Object fieldValue;
                if(formFieldBean.isMasterField()){
                    fieldValue = cacheFormData.getFieldValue(mappedFieldName);
                }else{
                    fieldValue = formSubData.getFieldValue(mappedFieldName);
                }
                valueMap.put(cdtInfo,fieldValue);
                result.add(valueMap);
            }
            //保存到session中
            AppContext.putSessionContext(designId,result);
        }
        return success(ret);
    }
}
