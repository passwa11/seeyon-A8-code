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
package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.dao.Col4WFAnalysisDao;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.common.taglibs.functions.Functions;

/**
 * @author mujun
 *
 */
public class Col4WFAnalysisManagerImpl implements Col4WFAnalysisManager {
	private static Log LOG = CtpLogFactory.getLog(Col4WFAnalysisManagerImpl.class);
	
    private Col4WFAnalysisDao col4WFAnalysisDao;
    
    private EdocApi edocApi;
    
    public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}

	public Col4WFAnalysisDao getCol4WFAnalysisDao() {
        return col4WFAnalysisDao;
    }

    public void setCol4WFAnalysisDao(Col4WFAnalysisDao col4wfAnalysisDao) {
        col4WFAnalysisDao = col4wfAnalysisDao;
    }

    public Integer getCaseCountByTempleteId(
            Long accountId,
            Long templateId,
            List<Integer> workFlowState, 
            Date startDate, 
            Date endDate) {
        return col4WFAnalysisDao.getCaseCountByTempleteId(
                accountId,
                templateId,
                workFlowState, 
                startDate, 
                endDate);
    }
   
    @Override
    public Integer getCaseCountGTSD(Long accountId, 
            Long templeteId, 
            List<Integer> workFlowState, 
            Date startDate,
            Date endDate, 
            Integer standarduration) {
       return col4WFAnalysisDao.getCaseCountGTSD(accountId, 
               templeteId, 
               workFlowState, 
               startDate, 
               endDate, 
               standarduration);
    }
    public Double getOverCaseRatioByTempleteId(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState,
            Date startDate,
            Date endDate){
        return col4WFAnalysisDao.getOverCaseRatioByTempleteId(accountId,
                templeteId, 
                workFlowState, 
                startDate,
                endDate);
    }
    public List<ColSummary> getColSummaryList( 
            Long acccountId,
            Long templeteId,
            List<Integer> workFlowState, Date startDate, Date endDate) {
        return col4WFAnalysisDao.getColSummaryList(acccountId, templeteId, workFlowState, startDate, endDate);
    }
    public Integer getAvgRunWorkTimeByTempleteId(
            Long accountId,
            Long templeteId,
            List<Integer> workFlowState, 
            Date startDate, 
            Date endDate){
        return col4WFAnalysisDao.getAvgRunWorkTimeByTempleteId(accountId, templeteId, workFlowState, startDate, endDate);
    }
    
    @Override
    public List<Object[]> statByAccount(int appType, List<Long> templateId, List<Long> entityId, String entityType,
            Date beginDate, Date endDate) {
        if(entityId==null || entityId.size()==0)
            return null;
        //自建流程
        boolean onlySelfFlow = templateId.size()==1 && templateId.get(0)==-1;
        
        List<Object[]> result = new ArrayList<Object[]>();
        Map<String,Integer> rowMap = new HashMap<String,Integer>();
        if(!V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType)) {
            int i = 0;
            String key = "";
            for(Long template:templateId) {
                for(Long entity:entityId) {
                    if(template==-1)
                        continue;
                    Object[] arr = new Object[10];
                    if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType))
                        arr[2] = entity;
                    else
                        arr[3] = entity;
                    result.add(arr);
                    
                    key = template+"_"+entity;
                    rowMap.put(key, i);
                    i++;
                }
            }
        }
        Map<String,Object> map = new HashMap<String,Object>();
        List<Integer> states = new ArrayList<Integer>();
        states.add(StateEnum.col_sent.getKey());
        states.add(StateEnum.col_pending.getKey());
        states.add(StateEnum.col_done.getKey());
        
        map.put("state", states);
        List<Integer> apps = new ArrayList<Integer>();
        //branches_a8_v350sp1_r_gov GOV-4842魏俊标--【流程分析】-【流程统计】，应用类型选择'信息报送'时统计的结果是'公文'.start
        try{
	        if(AppContext.hasPlugin("edoc") && edocApi.isEdoc(appType) && appType!=ApplicationCategoryEnum.info.getKey()){//branches_a8_v350sp1_r_gov GOV-4842魏俊标--【流程分析】-【流程统计】，应用类型选择'信息报送'时统计的结果是'公文'.end
	            apps.addAll(edocApi.findEdocAllAppEnumKeys());
	        }if(appType==ApplicationCategoryEnum.form.getKey()
	                ||appType==ApplicationCategoryEnum.collaboration.getKey() ){
	            apps.add(ApplicationCategoryEnum.collaboration.getKey());
	        }else if(appType==ApplicationCategoryEnum.info.getKey()){
	            apps.add(ApplicationCategoryEnum.info.getKey());
	        }
        }catch(Exception e) {
        	LOG.error("添加公文应用类型！",e);
        }
        //branches_a8_v350sp1_r_gov GOV-4842魏俊标--【流程分析】-【流程统计】，应用类型选择'信息报送'时统计的结果是'公文'.start
        map.put("app",apps );
        List<Object[]> returnValue =col4WFAnalysisDao.getAccountStat(appType, templateId, onlySelfFlow, entityId, entityType, beginDate, endDate, map);
        
        int position = -1;
        Object[] row = null;
        int total1 = 0;        //已发
        int total2 = 0;        //已办
        int total3 = 0;        //待办
        int total4 = 0;        //超期个数
        String key = "";
        String appName = "";
        if(returnValue != null) {
            int state = -1;
            for(int i=0;i<returnValue.size();i++) {
                Object[] obj = returnValue.get(i);
                if(obj[0]==null) {
                    key = "-2";
                    appName = ApplicationCategoryEnum.collaboration.name();
                }else
                    key = obj[0].toString();
                key += "_" + (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)?obj[4]:obj[1]);
                if(rowMap.get(key) == null) {
                    row = new Object[10];
                    if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType) || V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType))
                        row[2] = obj[1];
                    else
                        row[3] = obj[4];
                    result.add(row);
                    rowMap.put(key, rowMap.size());
                    position = result.size()-1;
                }else {
                    position = rowMap.get(key);
                    row = result.get(position);
                }
                row[9] = appName;
                row[0] = obj[0];
                row[2] = obj[1];
                
                if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)) {
                    row[3] = obj[4];
                    if(!onlySelfFlow)
                        row[1] = obj[5];
                }else if(!onlySelfFlow)
                    row[1] = obj[4];
                state = ((Integer)obj[3]).intValue();
                switch (state){
                    case 2:
                        row[4] = obj[2];
                        if(obj[2] != null) 
                            total1 = total1 + ((Number)obj[2]).intValue();
                        break;
                    case 3:
                        row[6] = obj[2];
                        if(obj[2] != null) 
                            total3 = total3 + ((Number)obj[2]).intValue();
                        break;
                    case 4:
                        row[5] = obj[2];
                        if(obj[2] != null) 
                            total2 = total2 + ((Number)obj[2]).intValue();
                        break;
                }
            }
        }
        
//      超期时间
        states.remove(0);
        map.remove("bodyType");
        returnValue = col4WFAnalysisDao.getAccountDeadline(appType, templateId, onlySelfFlow, entityId, entityType, beginDate, endDate, map);
        if(returnValue != null) {
            int[] countArr = new int[result.size()];
            long[] handleArr = new long[result.size()] ;
            
            for(int i=0;i<returnValue.size();i++) {
                long overWorkTime = 0l;
                Long orgAccountId = 0l;
                Date receiveTime = null;
                Date completeTime = null;
                long deadlineDate = 0;
                boolean isCovertime = false;
                key = "";
                Object[] obj = returnValue.get(i);
                if(obj[0]==null) {
                    key = "-2";
                }else
                    key = obj[0].toString();
                key += "_" + obj[1];
                receiveTime = (Date)obj[2];
                completeTime = (Date)obj[3];
                if(completeTime == null)
                    completeTime = new Date();
                
                if(null != obj[8]){
                  deadlineDate = ((Long)obj[8]).longValue();
                }
                
                //超期时间
                if(obj[5]!=null){
                    overWorkTime = ((Number)obj[5]).longValue();
                }
                
                if(obj[6]!=null){
                    orgAccountId = ((Number)obj[6]).longValue();
                }
                
                //设置了流程期限并且系统中没有已经计算好的超期时间
                if(obj[5] == null && deadlineDate>0){ 
                    double runWorkTime = ColUtil.getMinutesBetweenDatesByWorkTimehasDecimal(receiveTime,completeTime,orgAccountId);
                    Long workStandarDuration = ColUtil.convert2WorkTime(Long.valueOf(deadlineDate),orgAccountId);
                    double overWork = runWorkTime-(double)workStandarDuration;
                    if(overWork>0){
                        overWorkTime= overWork <1 ? 1: (long)overWork;
                    }
                }
                
                if(rowMap.get(key)==null)
                    continue;
                position = rowMap.get(key);
                handleArr[position] = handleArr[position] + overWorkTime;
                //判断超期的个数
                isCovertime = (Boolean)obj[7];
                if(isCovertime){
                	countArr[position] = countArr[position] + 1;
                }
            }
            
            int j = 0;
            int kk = 0;
            while(j<result.size()) {
                Object[] obj = result.get(j);
                kk++;
                if(obj[4] == null && obj[5] == null && obj[6] == null) {
                    result.remove(j);
                    continue;
                }
                obj[7] = Integer.valueOf(countArr[kk-1]);
                obj[8] = Functions.showDateByWork(((Long)handleArr[kk-1]).intValue());
                total4 += countArr[kk-1];
                j++;
            }
        }
        
//      汇总
        Object[] total = new Object[4];
        total[0] = total1;
        total[1] = total2;
        total[2] = total3;
        total[3] = total4;
        result.add(total);
        return result;
    
    }
    @Override
    public List<Object[]> statByGroup(int appType, List<Long> entityId, String entityType, Date beginDate, Date endDate) {

        if(entityId==null || entityId.size()==0)
            return null;
        
        List<Object[]> result = new ArrayList<Object[]>();
        Map<Long,Integer> rowMap = new HashMap<Long,Integer>();
        if(!V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(entityType) && !V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType)) {
            int i = 0;
            for(Long entity:entityId) {
                Object[] arr = new Object[7];
                arr[1] = entity;
                result.add(arr);
                rowMap.put(entity, i);
                i++;
            }
        }
        List<Integer> states = new ArrayList<Integer>();
        states.add(StateEnum.col_sent.getKey());
        states.add(StateEnum.col_pending.getKey());
        states.add(StateEnum.col_done.getKey());
        Map<String,Object> map = new HashMap<String,Object>();
        List<Integer> apps = new ArrayList<Integer>();
        try{
	        if(AppContext.hasPlugin("edoc") && edocApi.isEdoc(appType)){
	            apps.addAll(edocApi.findEdocAllAppEnumKeys());
	        }if(appType==ApplicationCategoryEnum.form.getKey()
	                ||appType==ApplicationCategoryEnum.collaboration.getKey() ){
	            //appType = ApplicationCategoryEnum.collaboration.getKey();
	            apps.add(ApplicationCategoryEnum.collaboration.getKey());
	            
	        }
        }catch(Exception e) {
        	LOG.error("添加公文应用类型！",e);
        }
      //branches_a8_v350sp1_r_gov GOV-4843 魏俊标 组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面. start
        if(appType==ApplicationCategoryEnum.info.getKey()){
            apps.add(ApplicationCategoryEnum.info.getKey());
        }
      //branches_a8_v350sp1_r_gov GOV-4843 魏俊标 组织管理员，工作管理-流程统计，点击信息报送统计结果里的数字链接出现红三角页面. end
        map.put("app",apps );
        map.put("state", states);
        
        List<Object> indexParameter = null;
        String hql = "";
        boolean hasCommon = false;
        List<Object[]> returnValue=new ArrayList<Object[]>();
        int len=entityId.size();
        if(len>=950){
          	//集团管理员子部门或者人员个数超过950个,会报错,需要分批统计,以后无法再支持特定排序
          	for(int i=0;i<(len%500==0?len/500:(len/500+1));i++){
          		List<Long> ids=entityId.subList(i*500, (i+1)*500>len?len:(i+1)*500);
          		returnValue.addAll(col4WFAnalysisDao.getGroupStat(appType, ids, entityType, beginDate, endDate, map));
          	}
        }else{
        	returnValue = col4WFAnalysisDao.getGroupStat(appType, entityId, entityType, beginDate, endDate, map);
        }
        int position = -1;
        Object[] row = null;
        int total1 = 0;        //已发
        int total2 = 0;        //已办
        int total3 = 0;        //待办
        int total4 = 0;        //超期个数
        if(returnValue != null) {
            int state = -1;
            for(Object[] obj:returnValue) {
                Long key = (Long)(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType)?obj[3]:obj[0]);
                if(rowMap.get(key) == null) {
                    row = new Object[7];
                    if(!V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType))
                        row[0] = key;
                    else
                        row[1] = key;
                    result.add(row);
                    rowMap.put(key, rowMap.size());
                }else {
                    position = rowMap.get(key);
                    row = result.get(position);
                }
                row[0] = obj[0];
                if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entityType))
                    row[1] = obj[3];
                state = ((Integer)obj[2]).intValue();
                switch (state){
                    case 2:
                        row[2] = obj[1];
                        if(obj[1] != null) 
                            total1 = total1 + ((Number)obj[1]).intValue();
                        break;
                    case 3:
                        row[4] = obj[1];
                        if(obj[1] != null) 
                            total3 = total3 + ((Number)obj[1]).intValue();
                        break;
                    case 4:
                        row[3] = obj[1];
                        if(obj[1] != null) 
                            total2 = total2 + ((Number)obj[1]).intValue();
                        break;
                }
            }
        }
        
        //超期时间
        states.remove(0);
        map.remove("bodyType");
        returnValue.clear();
        if(len>=950){
          	//集团管理员子部门或者人员个数超过950个,会报错,需要分批统计,以后无法再支持特定排序
          	for(int i=0;i<(len%500==0?len/500:(len/500+1));i++){
          		List<Long> ids=entityId.subList(i*500, (i+1)*500>len?len:(i+1)*500);
          		returnValue.addAll(col4WFAnalysisDao.getGroupDeadline(appType, ids, entityType, beginDate, endDate, map));
          	}
        }else{
        	returnValue = col4WFAnalysisDao.getGroupDeadline(appType, entityId, entityType, beginDate, endDate, map);
        }
        if(returnValue != null) {
            int[] countArr = new int[result.size()];
            long[] handleArr = new long[result.size()] ;
            Date receiveTime = null;
            Date completeTime = null;
            long deadlineDate = 0;
            long overWorkTime = 0;
            Long orgAccountId = 0l;
            for(Object[] obj:returnValue) {
                receiveTime = (Date)obj[1];
                completeTime = (Date)obj[2];
                if(completeTime == null)
                    completeTime = new Date();
                if(null != obj[6]){
                  deadlineDate = ((Long)obj[6]).longValue();
                }
                //超期时间
                if(obj[4]!=null){
                    overWorkTime = ((Number)obj[4]).longValue();
                }
                
                if(obj[5]!=null){
                    orgAccountId = ((Number)obj[5]).longValue();
                }
                
                //设置了流程期限并且系统中没有已经计算好的超期时间
                if(obj[4] == null && deadlineDate>0){ 
                    double runWorkTime = ColUtil.getMinutesBetweenDatesByWorkTimehasDecimal(receiveTime,completeTime,orgAccountId);
                    Long workStandarDuration = ColUtil.convert2WorkTime(Long.valueOf(deadlineDate),orgAccountId);
                    double overWork = runWorkTime-(double)workStandarDuration;
                    if(overWork>0){
                        overWorkTime= overWork < 1 ? 1 : (long)overWork;
                    }

                }
                if(rowMap.get(obj[0])==null)
                    continue;
                position = rowMap.get(obj[0]);
                countArr[position] = countArr[position] + 1;
                handleArr[position] = handleArr[position] + overWorkTime;
            }
            for(int j=0;j<result.size();j++) {
                Object[] obj = result.get(j);
                obj[5] = Integer.valueOf(countArr[j]);
                total4 += countArr[j];
                if(handleArr[j]!=0) {
                    obj[6] = Functions.showDateByWork(((Number)handleArr[j]).intValue());
                }
            }
        }
        
        //汇总
        Object[] total = new Object[4];
        total[0] = total1;
        total[1] = total2;
        total[2] = total3;
        total[3] = total4;
        result.add(total);
        return result;
    }
    public List<Object[]> transStatList(FlipInfo flipInfo, Map<String, Object> query){
        return col4WFAnalysisDao.transStatList(flipInfo, query);
    }

	@Override
	public List<Object[]> getAccountStat(Integer appType,
			List<Long> templateId, Boolean onlySelfFlow, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> map) {
		return col4WFAnalysisDao.getAccountStat(appType, templateId, onlySelfFlow, entityId, entityType, beginDate, endDate, map);
	}

	@Override
	public List<Object[]> getAccountDeadline(int appType,
			List<Long> templateId, boolean onlySelfFlow, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> namedParameterMap) {
		return col4WFAnalysisDao.getAccountDeadline(appType, templateId, onlySelfFlow, entityId, entityType, beginDate, endDate, namedParameterMap);
	}

	@Override
	public List<Object[]> getGroupDeadline(int appType, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> namedParameterMap) {
		return col4WFAnalysisDao.getGroupDeadline(appType, entityId, entityType, beginDate, endDate, namedParameterMap);
	}
    
    
}
