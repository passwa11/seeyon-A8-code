/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

/**
 * @author mujun
 *
 */
public class Col4WFAnalysisDaoImpl implements Col4WFAnalysisDao {
	private EdocApi edocApi;
	private static Log LOG = CtpLogFactory.getLog(Col4WFAnalysisDaoImpl.class);
	
    public Integer getCaseCountByTempleteId (Long accountId,
            Long templeteId,
            List<Integer> workFlowState, 
            Date startDate, 
            Date endDate){
        Integer c = getInfo(accountId,
                templeteId, workFlowState, startDate, endDate).get("COUNT");
        return  c == null ? 0: c;
    }
    private Map<String,Integer>  getInfo(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState, Date startDate, Date endDate){
        StringBuilder sb = new StringBuilder();
        sb.append(" select " );
        sb.append(" avg(summary.runWorktime),");
        sb.append(" count(summary.id) ");
        getQueryHql(sb);
        Map<String, Object> parameter = setParameter2Map(
                accountId,
                templeteId, workFlowState, startDate, endDate);
        List l = DBAgent.find(sb.toString(), parameter);
        Map<String,Integer> map = new HashMap<String,Integer>(); 
        if(Strings.isNotEmpty(l)){
            Object[] obj = (Object[])l.get(0);
            Integer  avgRunWorkTime = 0;
            if(obj[0]!=null){
                avgRunWorkTime = ((Number)obj[0]).intValue();
            }
            Integer c = ((Number)obj[1]).intValue();
            map.put("AVG", avgRunWorkTime);
            map.put("COUNT", c);
        }
        return map;
    }
    
    public Integer  getCaseCountGTSD(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState, 
            Date startDate, 
            Date endDate,
            Integer standarduration){
        StringBuilder sb = new StringBuilder();
        sb.append("select count(summary.id) ");
        getQueryHql(sb);
        sb.append(" and summary.runWorktime > :sd ");
        Map<String, Object> map = setParameter2Map(
                accountId,
                templeteId, workFlowState, startDate, endDate);
        map.put("sd", standarduration == null ?0L:Long.valueOf(standarduration));
        List l = DBAgent.find(sb.toString(), map);
        if(Strings.isNotEmpty(l)){
            return ((Number)l.get(0)).intValue();
        }
        return 0;
    }
    public Double getOverCaseRatioByTempleteId(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState,
            Date startDate,
            Date endDate){
        
        StringBuilder sb = new StringBuilder();
        sb.append("select createDate,finishDate,deadline,overWorktime,state");
        getQueryHql(sb);
        
        Map<String, Object> parameter = setParameter2Map(
                accountId,
                templeteId,
                workFlowState, 
                startDate, 
                endDate);
        
        List<Object[]> l = (List<Object[]>)DBAgent.find(sb.toString(), parameter);
        
        Integer countAll = 0;
        Integer countOver = 0;
        if(Strings.isNotEmpty(l)){
            for(Object[] obj : l){
                countAll++;
                Date sdate = (Date)obj[0];
                Date edate = (Date)obj[1];
                Long  deadline = 0L;
                if(obj[2]!=null){
                    deadline = ((Number)obj[2]).longValue();
                }
                
                //没有设置流程期限就不算超期。
                if(deadline == null || deadline == 0)
                    continue;
                
                Long overWorkTime = 0L;
                if(obj[3]!=null){
                    overWorkTime  =  ((Number)obj[3]).longValue();
                }
                
                Integer state = 0;
                if(obj[4]!=null){
                    state  =  ((Number)obj[4]).intValue();
                }
                
                if(overWorkTime>0) {
                    countOver++;
                }else{
                    if(edate == null)
                        edate = new Date();
                    
                    double run = ColUtil.getMinutesBetweenDatesByWorkTimehasDecimal(sdate,edate,accountId);
                    Long workDeadline = ColUtil.convert2WorkTime(deadline, accountId);
                    if(run>workDeadline){
                        countOver++;
                    }
                }
            }
        }
        double ratio = 0.0;
        if(countAll!=0){
            ratio = countOver/(countAll*1.0);
        }
        return  ratio;
    }
    private void getQueryHql(StringBuilder sb) {
        sb.append(" from ColSummary as summary ");
        sb.append(" where ");
        sb.append(" summary.templeteId=:templeteId ");
        sb.append(" and summary.state in (:state) ");
        sb.append(" and (summary.startDate between :startDate and :endDate) ");
        sb.append(" and summary.orgAccountId = :orgAccountId");
    }
    private Map<String, Object> setParameter2Map(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState, Date startDate, Date endDate) {
        Map<String,Object> parameter = new HashMap<String,Object>();
        parameter.put("orgAccountId", accountId);
        parameter.put("templeteId", templeteId);
        parameter.put("state", workFlowState);
        parameter.put("startDate", startDate);
        parameter.put("endDate", endDate);
        return parameter;
    }
    
    public List<ColSummary> getColSummaryList( 
            Long acccountId,
            Long templeteId,
            List<Integer> workFlowState, Date startDate, Date endDate) {
        StringBuilder sb = new StringBuilder();
        getQueryHql(sb);
        sb.append(" order by summary.runWorktime,summary.id  ");
        Map<String, Object> parameter = setParameter2Map(
                acccountId,
                templeteId, workFlowState,
                startDate, endDate);
        return DBAgent.find(sb.toString(), parameter);
    }
    
    public List<ColSummary> getColSummaryListByFinishDate( 
            Long acccountId,
            Long templeteId,
            List<Integer> workFlowState, Date startDate, Date endDate) {
        StringBuilder sb = new StringBuilder();
        getQueryHqlByFD(sb);
        sb.append(" order by summary.runWorktime,summary.id  ");
        Map<String, Object> parameter = setParameter2Map(
                acccountId,
                templeteId, workFlowState,
                startDate, endDate);
        return DBAgent.find(sb.toString(), parameter);
    }
    private void getQueryHqlByFD(StringBuilder sb) {
        sb.append(" from ColSummary as summary ");
        sb.append(" where ");
        sb.append(" summary.templeteId=:templeteId ");
        sb.append(" and summary.state in (:state) ");
        sb.append(" and (summary.finishDate between :startDate and :endDate) ");
        sb.append(" and summary.orgAccountId = :orgAccountId");
    }
    
    public Integer getAvgRunWorkTimeByTempleteId(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState, 
            Date startDate, 
            Date endDate){
        
            StringBuilder sb = new StringBuilder();
            sb.append("select createDate,finishDate,runWorktime,state ");
            getQueryHql(sb);
            
            Map<String, Object> parameter = setParameter2Map(
                    accountId,
                    templeteId,
                    workFlowState, 
                    startDate, 
                    endDate);
            List<Object[]> l = (List<Object[]>)DBAgent.find(sb.toString(), parameter);
            
            Long sumRunWorkTime = 0L;
            Long avgRunWorkTime = 0L;
            if(Strings.isNotEmpty(l)){
                for(Object[] obj : l){
                    Date sdate = (Date)obj[0];
                    Date edate = (Date)obj[1];
                    Long runWrokTime = null;
                    if(obj[2]!=null){
                        runWrokTime = ((Number)obj[2]).longValue();
                    }
                    Integer state = 0;
                    if(obj[3]!=null){
                        state = ((Number)obj[3]).intValue();
                    }
                    //如果有已经计算出来的运行时长，直接取运行时长
                    if(runWrokTime != null){
                        sumRunWorkTime += runWrokTime;
                        continue;
                    }else{
                        if(edate == null)
                            edate = new Date();
                        
                        sumRunWorkTime += ColUtil.getMinutesBetweenDatesByWorkTime(sdate,edate,accountId);
                    }
                }
                avgRunWorkTime = sumRunWorkTime / l.size();
            }
            return  avgRunWorkTime.intValue();
    }
    public List<Object[]> getAccountStat(
            Integer appType,
            List<Long> templateId,
            Boolean onlySelfFlow, 
            List<Long> entityId, 
            String entityType, 
            Date beginDate, 
            Date endDate, 
            Map<String,Object > map){
        StringBuilder hql = new StringBuilder();
        String app = " and a.app in( :app )";
        boolean isEdoc = false;
        if(AppContext.hasPlugin("edoc")) {
        	//TODO 检测当前的appKEy是否是公文
        	//isEdoc = EdocUtil.isEdocCheckByAppKey(appType);
        	try {
				isEdoc= appType != null && edocApi.isEdoc(appType);
			} catch (BusinessException e) {
			    LOG.error("判断是否是公文类型异常", e);
			}
        }
        hql.append("select c.templeteId ,max(m.orgDepartmentId),count(*),a.state");
        if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
            hql.append(",a.memberId");
        }
        if(!onlySelfFlow)
            hql.append(",max(t.subject)");
        //branches_a8_v350sp1_r_gov GOV-4842魏俊标--【流程分析】-【流程统计】，应用类型选择'信息报送'时统计的结果是'公文'.start
        if(appType == ApplicationCategoryEnum.info.key()){
            hql.append(" from CtpAffair as a,OrgMember as m,InfoSummary as c");
        }else{
            hql.append(" from CtpAffair as a,OrgMember as m,"+(isEdoc?"EdocSummary":"ColSummary")+" as c");
        }
        //branches_a8_v350sp1_r_gov GOV-4842魏俊标--【流程分析】-【流程统计】，应用类型选择'信息报送'时统计的结果是'公文'.start
        if(!onlySelfFlow)
            hql.append(",CtpTemplate as t");
        hql.append(" where a.objectId=c.id and a.memberId=m.id"+ app + " and a.state in (:state)");
        if(!onlySelfFlow){
            hql.append(" and t.id = c.templeteId ");
        }
        hql.append(" and (");
        if(Strings.isNotEmpty(templateId) 
                &&Long.valueOf(1).equals(templateId.get(0))){
            //全部模板
            hql.append(" c.templeteId is not null ");
        }else if(!onlySelfFlow 
                && Strings.isNotEmpty(templateId) && !Long.valueOf(1).equals(templateId.get(0)) ) {
            //选择具体模板
            hql.append("(c.templeteId in (:templateId) and c.templeteId=t.id)");
            map.put("templateId", templateId);
        }else if(onlySelfFlow && Strings.isNotEmpty(templateId) && Long.valueOf(-1L).equals(templateId.get(0))){
            //自由协同
            hql.append(" c.templeteId is null");
        }
        hql.append(")");
        
        if(Integer.valueOf(ApplicationCategoryEnum.form.key()).equals(appType)){
            hql.append(" and a.bodyType = :bodyType ");
            map.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }else if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(appType)){
            hql.append(" and a.bodyType != :bodyType ");
            map.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }
        
        String groupBy = " group by c.templeteId";  
        StringBuilder orderBy = new StringBuilder();
        orderBy.append(" order by c.templeteId ");
        if(entityId != null) {
            if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)) {
                hql.append(" and m.orgAccountId=:orgAccountId");
                groupBy += ",m.orgDepartmentId,a.state";
                orderBy.append(",m.orgDepartmentId ");
                map.put("orgAccountId", entityId.get(0));
            }else if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType)) {
                hql.append(" and m.orgDepartmentId in (:departmentIds)");
                groupBy += ",m.orgDepartmentId,a.state";
                orderBy.append(",m.orgDepartmentId ");
                map.put("departmentIds", entityId);
            }else if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
                hql.append(" and a.memberId in (:memberIds)");
                groupBy += ",a.memberId,a.state";
                orderBy.append(",a.memberId ");
                map.put("memberIds", entityId);
            }
        }
        
        if(beginDate != null) {
            if(isEdoc || appType == ApplicationCategoryEnum.info.key())
                hql.append(" and c.createTime>=:beginDate");
            else
                hql.append(" and c.createDate>=:beginDate");
            map.put("beginDate", beginDate);
        }
        if(endDate != null) {
            if(isEdoc || appType == ApplicationCategoryEnum.info.key())
                hql.append(" and c.createTime<=:endDate");
            else
                hql.append(" and c.createDate<=:endDate");
            map.put("endDate", endDate);
        }
        hql.append(" and a.delete =:isDelete");
        map.put("isDelete", false);
        hql.append(groupBy);
        hql.append(orderBy);
        if(entityId != null && entityId.size() >999) {
        	List allList = new ArrayList();
        	List<Long>[] splitList = Strings.splitList(entityId, 999);
        	for(int a =0; a<splitList.length; a++){
        		 map.put("departmentIds", splitList[a]);
        		 List find = DBAgent.find(hql.toString(),map);
        		 allList.addAll(find);
        	}
        	return allList;
        }else{
        	return DBAgent.find(hql.toString(),map);
        }
    }
    
    public   List<Object[]>  getAccountDeadline(
            int appType,
            List<Long> templateId,
            boolean onlySelfFlow,
            List<Long> entityId,
            String entityType,
            Date beginDate,
            Date endDate,
            Map<String,Object> namedParameterMap) {
        StringBuilder hql = new StringBuilder();
        String app = " and a.app in (:app)";
        boolean isEdoc = false;
        if(AppContext.hasPlugin("edoc")) {
        	//TODO 检测当前的appKEy是否是公文
        	//isEdoc = EdocUtil.isEdocCheckByAppKey(appType);
        	try {
				isEdoc= edocApi.isEdoc(appType);
			} catch (BusinessException e) {
			    LOG.error("判断是否是公文类型异常", e);
			}
        }
        String from = " from CtpAffair as a,"+(isEdoc?"EdocSummary":"ColSummary")+" as c,OrgMember as m";
        if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
            hql.append("select c.templeteId,a.memberId,a.receiveTime,a.completeTime,a.expectedProcessTime,a.overWorktime ,c.orgAccountId,a.coverTime,a.deadlineDate from CtpAffair as a,"+(isEdoc?"EdocSummary":"ColSummary") +" as c");
        }else
            hql.append("select c.templeteId,m.orgDepartmentId,a.receiveTime,a.completeTime,a.expectedProcessTime,a.overWorktime,c.orgAccountId,a.coverTime,a.deadlineDate "+from);
        String where = " where a.memberId=m.id and c.id=a.objectId"+app+" and a.state in (:state)";
        if(entityId != null) {
            if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)) {
                hql.append(where +  " and m.orgAccountId=:orgAccountId");
            }else if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType)) {
                hql.append(where +  " and m.orgDepartmentId in (:departmentIds)");
            }else if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
                hql.append(" where c.id=a.objectId" + app + " and a.state in (:state) and a.memberId in (:memberIds)");
            }
        }
        hql.append(" and (");

        if(Strings.isNotEmpty(templateId)&& Long.valueOf(1).equals(templateId.get(0)) ){
            //全部模板
            hql.append(" c.templeteId is not null ");
        }else if(!onlySelfFlow && !Long.valueOf(1).equals(templateId.get(0)) ) {
            //选择具体模板
            hql.append(" c.templeteId in (:templateId)");
            if(namedParameterMap.get("templateId")== null){
                namedParameterMap.put("templateId", templateId);
            }
        }else if(onlySelfFlow && Long.valueOf(-1L).equals(templateId.get(0))){
            //自由协同
            hql.append(" c.templeteId is null");
        }
        hql.append(")");
        
        if(Integer.valueOf(ApplicationCategoryEnum.form.key()).equals(appType)){
            hql.append(" and a.bodyType = :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }else   if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(appType)){
            hql.append(" and a.bodyType != :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }
        
        if(beginDate != null) {
            if(isEdoc)
                hql.append(" and c.createTime>=:beginDate");
            else
                hql.append(" and c.createDate>=:beginDate");
        }
        if(endDate != null) {
            if(isEdoc)
                hql.append(" and c.createTime<=:endDate");
            else
                hql.append(" and c.createDate<=:endDate");
        }
        hql.append(" and a.coverTime = :isOvertopTime ");
        namedParameterMap.put("isOvertopTime", Boolean.TRUE);
        hql.append(" and a.delete =:isDelete");
        namedParameterMap.put("isDelete", false);
        return DBAgent.find(hql.toString(), namedParameterMap);
    }
    public  List<Object[]>  getGroupStat(int appType,List<Long> entityId,String entityType,Date beginDate,Date endDate,Map<String,Object> namedParameterMap) {
        StringBuilder hql = new StringBuilder();
        if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)){
            hql.append("select max(m.orgAccountId),count(*),a.state");
        }else{
            hql.append("select max(m.orgDepartmentId),count(*),a.state");
        }
        String from = " from CtpAffair as a,OrgMember as m";
        //String from1 = ",ColSummary as c";
        String app = " and a.app in(:app )";
        /*boolean isEdoc = appType==ApplicationCategoryEnum.edocSend.key()||appType==ApplicationCategoryEnum.edocRec.key()||appType==ApplicationCategoryEnum.edocSign.key();
        if(isEdoc) {
            from1 = ",EdocSummary as c";
        }*/
        if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
            hql.append(",a.memberId");
        }
        hql.append(from);
        String where = " where a.memberId=m.id" + app + " and a.state in (:state)";
        
        hql.append(where);
        String groupBy = " group by";
        if(Integer.valueOf(ApplicationCategoryEnum.form.key()).equals(appType)){
            hql.append(" and a.bodyType = :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }else   if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(appType)){
            hql.append(" and a.bodyType != :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }
        if(entityId != null) {
            if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
                hql.append(" and a.memberId in (:memberIds)");
                groupBy += " a.memberId,a.state";
                namedParameterMap.put("memberIds", entityId);
            }else{
                if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)) {
                    hql.append(" and m.orgAccountId in(:orgAccountId)");
                    groupBy += " m.orgAccountId";
                    //groupBy += ",m.orgDepartmentId";
                    namedParameterMap.put("orgAccountId", entityId);
                    groupBy += ",a.state";
                }
                
                //groupBy += " m.orgAccountId";
                if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType)){
                    hql.append(" and m.orgDepartmentId in(:orgAccountId)");
                    groupBy += " m.orgAccountId";
                    groupBy += ",m.orgDepartmentId";
                    namedParameterMap.put("orgAccountId", entityId);
                    groupBy += ",a.state";
                }
                
                
            }
        }
        if(beginDate != null) {
            /*if(isEdoc)
                hql.append(" and c.createTime>=:beginDate");
            else*/
                hql.append(" and a.createDate>=:beginDate");
            namedParameterMap.put("beginDate", beginDate);
        }
        if(endDate != null) {
            /*if(isEdoc)
                hql.append(" and c.createTime<=:endDate");
            else*/
                hql.append(" and a.createDate<=:endDate");
            namedParameterMap.put("endDate", endDate);
        }
        hql.append(" and a.delete =:isDelete");
        namedParameterMap.put("isDelete", false);
        hql.append(groupBy);
        if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType) || V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType))
            hql.append(" order by m.orgAccountId");
        return DBAgent.find(hql.toString(),namedParameterMap);
    }
    public  List<Object[]>  getGroupDeadline(int appType,
            List<Long> entityId,
            String entityType,
            Date beginDate,
            Date endDate,
            Map<String,Object> namedParameterMap) {
       
        boolean isEdoc = false;
        if(AppContext.hasPlugin("edoc")) {
        	//TODO 检测当前的appKEy是否是公文
        	//isEdoc = EdocUtil.isEdocCheckByAppKey(appType);
        	try {
				isEdoc=edocApi.isEdoc(appType);
			} catch (BusinessException e) {
			    LOG.error("判断是否是公文类型异常", e);
			}
        }
        
        StringBuilder hql = new StringBuilder();
        String from = " from CtpAffair as a,OrgMember as m";
        String app = " a.app in (:app)";
        if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
            hql.append("select a.memberId,a.receiveTime,a.completeTime,a.expectedProcessTime,a.overWorktime ,c.orgAccountId,a.deadlineDate  from CtpAffair as a");
        }else if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)){
            hql.append("select m.orgAccountId,a.receiveTime,a.completeTime,a.expectedProcessTime,a.overWorktime ,c.orgAccountId,a.deadlineDate "+from);
        }else{
            hql.append("select m.orgDepartmentId,a.receiveTime,a.completeTime,a.expectedProcessTime,a.overWorktime ,c.orgAccountId,a.deadlineDate "+from);
        }
        hql.append(","+(isEdoc?"EdocSummary":"ColSummary")+" as c ");
        
        String where = " where a.memberId=m.id and" + app + " and a.state in (:state)";
        if(entityId != null) {
        	if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
                hql.append(" where" + app + " and a.state in (:state) and a.memberId in (:memberIds)");
            }else if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)){
                hql.append(where +  " and m.orgAccountId in(:orgAccountId)");
            }else{
            	hql.append(where +  " and m.orgDepartmentId in(:orgAccountId)");
            }
        }
        hql.append(" and c.id = a.objectId ");
        if(Integer.valueOf(ApplicationCategoryEnum.form.key()).equals(appType)){
            hql.append(" and a.bodyType = :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }else   if(Integer.valueOf(ApplicationCategoryEnum.collaboration.key()).equals(appType)){
            hql.append(" and a.bodyType != :bodyType ");
            namedParameterMap.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
        }
        if(beginDate != null) {
             hql.append(" and a.createDate>=:beginDate");
        }
        if(endDate != null) {
             hql.append(" and a.createDate<=:endDate");
        }
        hql.append(" and (a.deadlineDate>0 or a.expectedProcessTime is not null) and a.coverTime = :isOvertopTime ");
        namedParameterMap.put("isOvertopTime", Boolean.TRUE);
        hql.append(" and a.delete =:isDelete");
        namedParameterMap.put("isDelete", false);
        return DBAgent.find(hql.toString(), namedParameterMap);
    }
    public List<Object[]> transStatList(FlipInfo flipInfo, Map<String, Object> query){
        int appType = (Integer)query.get("appType");
        Long entityId = (Long)query.get("entityId");
        int state = (Integer)query.get("state");
        String entityType = (String)query.get("entityType");
        Date beginDate = (Date)query.get("beginDate");
        Date endDate = (Date)query.get("endDate");
        Long templateId=(Long)query.get("templateId");
        String statScope = (String)query.get("statScope");
        
        boolean isEdoc = false;
        if(AppContext.hasPlugin("edoc")) {
        	//TODO 检测当前的appKEy是否是公文
        	
        	//isEdoc = EdocUtil.isEdocCheckByAppKey(appType);
        	try {
				isEdoc=edocApi.isEdoc(appType);
			} catch (BusinessException e) {
			    LOG.error("判断是否是公文类型异常", e);
			}
        }
        String startMemberId = "c.startMemberId";
        String createDate = "c.createDate";
        String finishDate = "c.finishDate";
        if(isEdoc || appType == ApplicationCategoryEnum.info.key()) {
            startMemberId = "c.startUserId";
            createDate = "c.createTime";
            finishDate = "c.completeTime";
        }
        StringBuilder hql = new StringBuilder();
        Map<String,Object> map = new HashMap<String,Object>();
        //hql.append("select  c.id,"+startMemberId+",c.subject,"+createDate+","+finishDate+" from");
        hql.append("select  c.id,"+startMemberId+",c.subject,"+createDate+","+finishDate+",a.expectedProcessTime,a.receiveTime,a.completeTime, a.memberId, a.overWorktime, a.runWorktime from");
        hql.append(" CtpAffair as a, ");
        if(isEdoc)
            hql.append(" EdocSummary");//branches_a8_v350sp1_r_gov GOV-4843.组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面.start
        else if(appType == ApplicationCategoryEnum.info.key()){
            hql.append(" InfoSummary");//branches_a8_v350sp1_r_gov GOV-4843.组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面end
        }else{
            hql.append(" ColSummary");
        }
        hql.append(" as c ");
        String where = "";
        if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType)) {
            hql.append(",OrgMember as m");
            where = " and a.memberId=m.id and m.orgDepartmentId=:entityId";
        }else if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)){
            hql.append(",OrgMember as m");
            where = " and a.memberId=m.id and m.orgAccountId=:entityId";
        }else{
            where = " and a.memberId=:entityId";
        }
        
        hql.append(" where c.id=a.objectId");
        if (state==-1){
            hql.append(" and a.coverTime=:isOvertopTime1  ");// OA-126791 自由协同deadlineDate为空先去掉   and  a.deadlineDate>0
            hql.append(" and a.state in(:state)");
            List<Integer> stateList = new ArrayList<Integer>();
            stateList.add(StateEnum.col_sent.key());
            stateList.add(StateEnum.col_pending.key());
            stateList.add(StateEnum.col_done.key());
            map.put("isOvertopTime1", true);
            map.put("state", stateList);
        } else {
            hql.append(" and a.state=:state");
            map.put("state", state);
        }
        
        hql.append(where);
        
        if (appType == ApplicationCategoryEnum.edoc.key()) {
        	//TODO 获取公文appID列表
            //List<Integer> appList = EdocUtil.getAllEdocApplicationCategoryEnumKey();
            //map.put("app", appList);////branches_a8_v350sp1_r_gov GOV-4843 魏俊标 组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面 start
        }else if(appType == ApplicationCategoryEnum.info.key()){
        	hql.append(" and a.app in(:app)");
//            List<Integer> appList = EdocUtil.getAllEdocApplicationCategoryEnumKey();
        	List<Integer> appList=new ArrayList<Integer>();
        	appList.add(ApplicationCategoryEnum.info.key());
            map.put("app", appList);////branches_a8_v350sp1_r_gov GOV-4843 魏俊标 组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面 end
        } else {
        	hql.append(" and a.app in(:app)");
            List<Integer> appList = new ArrayList<Integer>();
            if(appType  ==  ApplicationCategoryEnum.form.key() ){
                appList.add(ApplicationCategoryEnum.collaboration.key());
                hql.append(" and a.bodyType = :bodyType");
            }else if(appType  ==  ApplicationCategoryEnum.collaboration.key() ){
                appList.add(ApplicationCategoryEnum.collaboration.key());
                hql.append(" and a.bodyType != :bodyType");
            }
            map.put("bodyType", String.valueOf(MainbodyType.FORM.getKey()));
            map.put("app", appList);
        }
        
        map.put("entityId", entityId);
        if(!"group".equals(statScope)) {
            hql.append(" and c.templeteId");
            if(templateId==null)
                hql.append(" is null");
            else {
                hql.append("=:templateId");
                map.put("templateId", templateId);
            }
        }
        if(beginDate != null && endDate != null) {
            if(isEdoc || appType == ApplicationCategoryEnum.info.key())
                hql.append(" and c.createTime between :beginDate");
            else
                hql.append(" and a.createDate between :beginDate");
            map.put("beginDate", beginDate);
            
            hql.append(" and :endDate");
            map.put("endDate",endDate);
        }
        hql.append(" order by "+createDate+" desc ");
        return DBAgent.find(hql.toString(), map,flipInfo);
    }
     
    public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
}
