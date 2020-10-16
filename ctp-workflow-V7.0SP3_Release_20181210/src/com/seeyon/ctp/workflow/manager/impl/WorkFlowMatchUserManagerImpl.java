/**
 * Author: wangchw
 * Rev: WorkFlowMatchUserManagerImpl.java
 * Date: 20122012-7-3下午09:52:52
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.processlog.ProcessLogDetail;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.HumenNodeMatchManager;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.po.HistoryWorkitemBLOBDAO;
import com.seeyon.ctp.workflow.po.WFProcessProperty;
import com.seeyon.ctp.workflow.po.WorkitemDAO;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.vo.WorkflowMatchLogVO;
import com.seeyon.ctp.workflow.vo.WorkflowOrgnazitionVO;
import com.seeyon.ctp.workflow.wapi.HumenNodeMatchInterface;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;
import com.seeyon.ctp.workflow.wapi.WorkflowNodeUsersMatchResult;
import com.seeyon.v3x.common.web.login.CurrentUser;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStart;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.DynamicFormMasterInfo;
import net.joinwork.bpm.engine.execute.ProcessEngineImpl;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkItemManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.BPMWorkItemList;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流人员匹配接口实现类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-3 下午09:52:52
*/
public class WorkFlowMatchUserManagerImpl implements WorkFlowMatchUserManager {
    
    public static final Logger log = Logger.getLogger(WorkFlowMatchUserManagerImpl.class);
    
    private static Map<String,LinkedHashMap<String,WorkflowMatchLogVO>> workflowAutoSkipCacheMap;//不能自动跳过的原因集合
    
    private static Map<String,HashMap<String,WorkflowNodeUsersMatchResult>> workflowMatchResultCacheMap;//匹配到人员的节点集合
    
    private static Map<String,HashMap<String,Integer>> workflowNeedSelectPeopleNodeCacheMap;//需要选人的节点集合
    
    private static Map<String,LinkedHashMap<String,WorkflowMatchLogVO>> workflowNeedSelectBranchNodeCacheMap;//需要选分支的节点集合
    
    private static Map<String,Long> workflowMatchResultTimestampCacheMap;//节点匹配时间戳
    
    private static Map<String,BPMProcess> workflowBPMProcessCacheMap;//发送和提交处理时流程BPMProcess对象缓存
    
    private static Map<String,Integer> workflowProcessStateCacheMap;//发送和提交处理时流程BPMProcess对象缓存
    
    private static Map<String,String> workflowProcessXmlCacheMap;//发送和提交处理时流程BPMProcess对象缓存
    
    private static Map<String,Map<String,Object>> workflowFormDataCacheMap;//发送和提交处理时流程单据数据对象缓存
    
    private static Map<String,Map<String,WorkflowFormFieldBO>> workflowFormDataDefCacheMap;//发送和提交处理时流程单据数据对象缓存
    
    private static Map<String,Map<String,WFProcessProperty>> wfProcessPropertyCacheMap;//发送和提交处理时流程WFProcessProperty对象缓存
    
    private static final long expireTime=  1*60*60*1000/2;//只保留0.5个小时
    
    private static final long sleepTime= 1*60*60*1000/4;//0.5个小时扫描一次
    
    private OrgManager orgManager;
    private BPMWorkItemList itemlist;
    
    public void setItemlist(BPMWorkItemList itemlist) {
        this.itemlist = itemlist;
    }
    
    public void init(){
    	wfProcessPropertyCacheMap= new ConcurrentHashMap<String,Map<String,WFProcessProperty>>();
    	workflowFormDataCacheMap= new ConcurrentHashMap<String, Map<String,Object>>();
    	workflowFormDataDefCacheMap= new ConcurrentHashMap<String, Map<String,WorkflowFormFieldBO>>();
    	workflowBPMProcessCacheMap= new ConcurrentHashMap<String, BPMProcess>();
    	workflowProcessStateCacheMap= new ConcurrentHashMap<String, Integer>();
    	workflowProcessXmlCacheMap= new ConcurrentHashMap<String, String>();
    	workflowAutoSkipCacheMap= new ConcurrentHashMap<String, LinkedHashMap<String,WorkflowMatchLogVO>>();
    	workflowMatchResultCacheMap= new ConcurrentHashMap<String, HashMap<String,WorkflowNodeUsersMatchResult>>();
    	workflowNeedSelectPeopleNodeCacheMap= new ConcurrentHashMap<String, HashMap<String,Integer>>();
    	workflowNeedSelectBranchNodeCacheMap= new ConcurrentHashMap<String, LinkedHashMap<String,WorkflowMatchLogVO>>();
    	workflowMatchResultTimestampCacheMap= new ConcurrentHashMap<String, Long>();
    	WorkflowMatchResultTimestampCacheMonitor clearThread= new WorkflowMatchResultTimestampCacheMonitor();
    	clearThread.setName("无效缓存清理线程");
    	clearThread.start();
    }
    
    public void removeWorkflowMatchResult(String key){
    	if(Strings.isNotBlank(key)){
    		workflowMatchResultCacheMap.remove(key);
    		workflowMatchResultTimestampCacheMap.remove(key);
    		workflowNeedSelectPeopleNodeCacheMap.remove(key);
    		workflowAutoSkipCacheMap.remove(key);
    		workflowNeedSelectBranchNodeCacheMap.remove(key);
    		workflowBPMProcessCacheMap.remove(key);
    		workflowProcessStateCacheMap.remove(key);
    		workflowProcessXmlCacheMap.remove(key);
    		workflowFormDataCacheMap.remove(key);
    		workflowFormDataDefCacheMap.remove(key);
    		wfProcessPropertyCacheMap.remove(key);
    	}
    }
    
    @Override
	public void removeWorkflowMatchResult(String matchRequestToken, String autoSkipNodeId) {
    	LinkedHashMap<String,WorkflowMatchLogVO> autoSkipNodeMap= workflowAutoSkipCacheMap.get(matchRequestToken);
    	if(null!=autoSkipNodeMap){
    		autoSkipNodeMap.remove(autoSkipNodeId);
    		workflowAutoSkipCacheMap.put(matchRequestToken,autoSkipNodeMap);
    	}
	}
    
    private class WorkflowMatchResultTimestampCacheMonitor extends Thread{
		@Override
		public void run() {
			log.info("启动");
			while(true){
				try{
					if( null!=workflowMatchResultTimestampCacheMap && !workflowMatchResultTimestampCacheMap.isEmpty() ){
						Set<String> keys= workflowMatchResultTimestampCacheMap.keySet();
						for (String key : keys) {
							Long createTime= workflowMatchResultTimestampCacheMap.get(key);
							if( System.currentTimeMillis()-createTime >= expireTime ){
								removeWorkflowMatchResult(key);
							}
						}
					}
					Thread.sleep(sleepTime);
				}catch(Throwable e){
					log.error("",e);
				}
			}
		}
    }
    
    @Override
	public WFProcessProperty getCaseProcessPropertyFromCache(String matchRequestToken, String processId) {
    	if(Strings.isNotBlank(matchRequestToken) && Strings.isNotBlank(processId)){
    		if(null==wfProcessPropertyCacheMap.get(matchRequestToken)){
				Map<String,WFProcessProperty> map= new HashMap<String, WFProcessProperty>();
				wfProcessPropertyCacheMap.put(matchRequestToken, map);
			}
    		return wfProcessPropertyCacheMap.get(matchRequestToken).get(processId);
    	}
		return null;
	}

	@Override
	public void putCaseProcessPropertyToCache(String matchRequestToken, WFProcessProperty processProperty) {
		if(Strings.isNotBlank(matchRequestToken) && null!=processProperty){
			if(null==wfProcessPropertyCacheMap.get(matchRequestToken)){
				Map<String,WFProcessProperty> map= new HashMap<String, WFProcessProperty>();
				wfProcessPropertyCacheMap.put(matchRequestToken, map);
			}
			wfProcessPropertyCacheMap.get(matchRequestToken).put(processProperty.getProcessId().toString(), processProperty);
    		if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
    	}
	}
	
	@Override
	public void cacheManualSelectLog(String cacheKey, BPMProcess process, 
            Map<String, Map<String,Object>> selectPeopleParams){

        
        if(selectPeopleParams != null){
            
            com.seeyon.ctp.common.authenticate.domain.User currentUser = CurrentUser.get();
            String currentUserName = currentUser.getName();
            
            Set<String> nodeIds = selectPeopleParams.keySet();
            for(String nodeId : nodeIds){
                
              //人员选择情况
                Map<String, Object> nodeSelectInfo = selectPeopleParams.get(nodeId);
                
                if(nodeSelectInfo != null){
                    
                    Boolean hasLoged = (Boolean) nodeSelectInfo.get("hasLoged");
                    if(hasLoged != null && hasLoged.booleanValue()){
                        //已经记录过日志了， 不在进行日志记录
                        continue;
                    }
                    nodeSelectInfo.put("hasLoged", Boolean.TRUE);
                    
                    BPMActivity activity = process.getActivityById(nodeId);
                    
                    String[] pepoles = (String[]) nodeSelectInfo.get("pepole");
                    
                    if(pepoles != null && pepoles.length > 0){
                        
                        String isOrderExecute = (String) nodeSelectInfo.get(ProcessEngineImpl.IS_ORDER_EXECUTE);
                        
                        boolean isMultiMode = false;
                        String nodeName = "";
                        if(activity == null){
                            
                            //并发执行，节点被替换了
                            if(isOrderExecute != null){
                                isMultiMode = true;
                            }
                            
                        }else{
                            isMultiMode = activity.isMultipleProcessMode();
                            nodeName = activity.getName();
                        }
                        
                        
                        String addition = Strings.join(",", pepoles);
                        
                        List<V3xOrgMember> aresult=  getUserListByAddition(addition, false);
                        //String canNotSkipMsg= currentUserName+"选择了人员："+WorkflowUtil.getMemberNames(aresult);
                        String canNotSkipMsg= ResourceUtil.getString("workflow.match.log.selectPeople", currentUserName, WorkflowUtil.getMemberNames(aresult));
                        putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step2, cacheKey, nodeId, nodeName, canNotSkipMsg);
                        
                        if(isMultiMode){//多人执行模式,以addition中的为准
                                
                                //多人按顺序执行
                                if(isOrderExecute != null){
                                    
                                    String[] multiAddMember = (String[]) nodeSelectInfo.get(ProcessEngineImpl.MULTI_ADD_MEMBER);
                                    
                                    String multiOrderLog = "";
                                    String allExecutors = WorkflowUtil.getMemberNames(aresult, false).toString();
                                    String logType = null;
                                    if("true".equals(isOrderExecute)){
                                        
                                        if(multiAddMember != null && multiAddMember.length > 0){
                                            
                                            List<V3xOrgMember> addMembers =  getUserListByAddition(Strings.join(",", multiAddMember), false);
                                            
                                            String addMemberNames = WorkflowUtil.getMemberNames(addMembers, false).toString();
                                            //multiOrderLog += "新增人员:" + addMemberNames;
                                            multiOrderLog = ResourceUtil.getString("workflow.match.log.mutiAdd", currentUserName, addMemberNames);
                                        }
                                        //"执行顺序：" + allExecutors
                                        multiOrderLog += ResourceUtil.getString("workflow.match.log.mutiOrder", allExecutors);
                                        logType = WorkflowMatchLogMessageConstants.step8;
                                    }else {
                                        
                                        //currentUserName + "选择了同时执行：" + allExecutors;
                                        multiOrderLog = ResourceUtil.getString("workflow.match.log.mutiConcurrence", currentUserName, allExecutors); 
                                        logType = WorkflowMatchLogMessageConstants.step9;
                                    }
                                    putWorkflowMatchLogMsgToCache(logType, cacheKey, nodeId, nodeName, multiOrderLog);
                                }
                        }/*else if(activity.isSingleProcessMode()){
                          //单人执行模式
                        }else{
                          //全体和竞争执行
                        }*/
                    }
                }
            }
        }
	}
    
    @Override
	public void putWorkflowFormDataToCacheRequestScope(String matchRequestToken, Map<String, Object> formData) {
    	if(Strings.isNotBlank(matchRequestToken) && null!=formData){
    		workflowFormDataCacheMap.put(matchRequestToken, formData);
    		if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
    	}
	}

	@Override
	public Map<String, Object> getWorkflowFormDataFromCacheRequestScope(String matchRequestToken) {
		if(Strings.isNotBlank(matchRequestToken)){
			return workflowFormDataCacheMap.get(matchRequestToken);
		}
		return null;
	}
	
	@Override
	public void putWorkflowFormDataDefToCacheRequestScope(String matchRequestToken,
			Map<String, WorkflowFormFieldBO> formDataObjDef) {
		if(Strings.isNotBlank(matchRequestToken) && null!=formDataObjDef){
			workflowFormDataDefCacheMap.put(matchRequestToken, formDataObjDef);
    		if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
    	}
	}

	@Override
	public Map<String, WorkflowFormFieldBO> getWorkflowFormDataDefFromCacheRequestScope(String matchRequestToken) {
		if(Strings.isNotBlank(matchRequestToken)){
			return workflowFormDataDefCacheMap.get(matchRequestToken);
		}
		return null;
	}
    
    @Override
	public void putBPMProcessToCacheRequestScope(String matchRequestToken, BPMProcess process) {
    	if(Strings.isNotBlank(matchRequestToken) && null!=process){
    		workflowBPMProcessCacheMap.put(matchRequestToken, process);
    		if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
    	}
	}

	@Override
	public BPMProcess getBPMProcessFromCacheRequestScope(String matchRequestToken) {
		if(Strings.isNotBlank(matchRequestToken)){
			return workflowBPMProcessCacheMap.get(matchRequestToken);
		}
		return null;
	}
	
	@Override
	public void putProcessStateToCacheRequestScope(String matchRequestToken, Integer processState) {
		if(Strings.isNotBlank(matchRequestToken) && null!=processState){
			workflowProcessStateCacheMap.put(matchRequestToken, processState);
			if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
		}
	}

	@Override
	public Integer getProcessStateFromCacheRequestScope(String matchRequestToken) {
		if(Strings.isNotBlank(matchRequestToken)){
			return workflowProcessStateCacheMap.get(matchRequestToken);
		}
		return null;
	}

	@Override
	public void putProcessXmlToCacheRequestScope(String matchRequestToken, String processXml) {
		if(Strings.isNotBlank(matchRequestToken) && Strings.isNotBlank(processXml)){
			workflowProcessXmlCacheMap.put(matchRequestToken, processXml);
			if(null==workflowMatchResultTimestampCacheMap.get(matchRequestToken)){
    			workflowMatchResultTimestampCacheMap.put(matchRequestToken, System.currentTimeMillis());
    		}
		}
	}

	@Override
	public String getProcessXmlFromCacheRequestScope(String matchRequestToken) {
		if(Strings.isNotBlank(matchRequestToken)){
			return workflowProcessXmlCacheMap.get(matchRequestToken);
		}
		return null;
	}
    
    @Override
	public WorkflowNodeUsersMatchResult getWorkflowMatchedCacheResultMsg(String key, String activityId) {
    	Map<String,WorkflowNodeUsersMatchResult> cacheResult= workflowMatchResultCacheMap.get(key);
		if(null!=cacheResult){
			WorkflowNodeUsersMatchResult result= cacheResult.get(activityId);//人员匹配结果缓存ID
			return result;
		}
		return null;
	}

    public Integer getWorkflowNeedSelectPeopleNode(String key, String activityId) {
    	Map<String,Integer> cacheResult= workflowNeedSelectPeopleNodeCacheMap.get(key);
		if(null!=cacheResult){
			Integer result= cacheResult.get(activityId);//人员匹配结果缓存ID
			if(null!=result){
				return result;
			}
		}
		return 0;
	}
    
    public List<V3xOrgMember> getWorkflowMatchedCacheResult(String key, String activityId) {
    	Map<String,WorkflowNodeUsersMatchResult> cacheResult= workflowMatchResultCacheMap.get(key);
		if(null!=cacheResult){
			WorkflowNodeUsersMatchResult result= cacheResult.get(activityId);//人员匹配结果缓存ID
			if(null!=result){
				return result.getMembers();
			}
		}
		return null;
	}
    
    public void putWorkflowMatchedCacheResult(String key, String activityId,WorkflowNodeUsersMatchResult matchResult) {
    	HashMap<String,WorkflowNodeUsersMatchResult> cacheResult= workflowMatchResultCacheMap.get(key);
		if(null==cacheResult){
			cacheResult= new HashMap<String, WorkflowNodeUsersMatchResult>();
		}
		cacheResult.put(activityId, matchResult);
		workflowMatchResultCacheMap.put(key, cacheResult);
		
		HashMap<String,Integer> cacheMatched= workflowNeedSelectPeopleNodeCacheMap.get(key);
		if(null==cacheMatched){
			cacheMatched= new HashMap<String, Integer>();
		}
		cacheMatched.put(activityId, 1);
		workflowNeedSelectPeopleNodeCacheMap.put(key, cacheMatched);
		
		if(null==workflowMatchResultTimestampCacheMap.get(key)){
			workflowMatchResultTimestampCacheMap.put(key, System.currentTimeMillis());
		}
    }
    
    public void putWorkflowNeedSelectBranchNodeCacheMap(String key, String activityId,String nodeName){
    	LinkedHashMap<String,WorkflowMatchLogVO> needSelectBranchNodeMap= workflowNeedSelectBranchNodeCacheMap.get(key);
		if(null==needSelectBranchNodeMap){
			needSelectBranchNodeMap= new LinkedHashMap<String, WorkflowMatchLogVO>();
		}
		WorkflowMatchLogVO vo= new WorkflowMatchLogVO();
		vo.setNodeId(activityId);
		vo.setNodeName(nodeName);
		needSelectBranchNodeMap.put(activityId, vo);
		
		workflowNeedSelectBranchNodeCacheMap.put(key, needSelectBranchNodeMap);
		
		if(null==workflowMatchResultTimestampCacheMap.get(key)){
			workflowMatchResultTimestampCacheMap.put(key, System.currentTimeMillis());
		}
    }
    
    public LinkedHashMap<String,WorkflowMatchLogVO> getAllWorkflowNeedSelectBranchNodeCacheMap(String key){
    	LinkedHashMap<String,WorkflowMatchLogVO> needSelectBranchNodeMap= workflowNeedSelectBranchNodeCacheMap.get(key);
		if(null==needSelectBranchNodeMap){
			needSelectBranchNodeMap= new LinkedHashMap<String, WorkflowMatchLogVO>();
		}
		return needSelectBranchNodeMap;
    }
    
    @Override
	public void putWorkflowMatchLogMatchNodeNameToCache(String matchRequestToken, String autoSkipNodeId,
			String nodeName) {
    	if(Strings.isNotBlank(matchRequestToken) && Strings.isNotBlank(autoSkipNodeId)){
    		WorkflowMatchLogVO workflowMatchLogVO = getOrInitWorkflowMatchLogVO(matchRequestToken, autoSkipNodeId, nodeName);
	    	workflowMatchLogVO.setNodeName(nodeName);
    	}
	}
    
    @Override
	public void putWorkflowMatchLogProcessModeToCache(String key, String autoSkipNodeId,String nodeName, List<String> processMode) {
    	if(Strings.isNotBlank(key) && Strings.isNotBlank(autoSkipNodeId)){
    		WorkflowMatchLogVO workflowMatchLogVO = getOrInitWorkflowMatchLogVO(key, autoSkipNodeId, nodeName);
	    	workflowMatchLogVO.getProcessMode().addAll(processMode);
    	}
	}
    
    @Override
    public void putWorkflowMatchLogNodeTypeToCache(String key, String autoSkipNodeId,String nodeName, String nodeType){
    	if(Strings.isNotBlank(key) && Strings.isNotBlank(autoSkipNodeId)){
    		WorkflowMatchLogVO workflowMatchLogVO = getOrInitWorkflowMatchLogVO(key, autoSkipNodeId, nodeName);
	    	workflowMatchLogVO.setNodeType(nodeType);
    	}
    }
    
    private WorkflowMatchLogVO getOrInitWorkflowMatchLogVO(String key,String autoSkipNodeId,String nodeName) {
    	LinkedHashMap<String,WorkflowMatchLogVO> autoSkipNodeMap= workflowAutoSkipCacheMap.get(key);
    	if(null==autoSkipNodeMap){
    		autoSkipNodeMap= new LinkedHashMap<String, WorkflowMatchLogVO>();
    	}
    	WorkflowMatchLogVO workflowMatchLogVO= autoSkipNodeMap.get(autoSkipNodeId);
    	if(null==workflowMatchLogVO){
    		workflowMatchLogVO= new WorkflowMatchLogVO();
    		workflowMatchLogVO.setNodeId(autoSkipNodeId);
    		workflowMatchLogVO.setNodeName(nodeName);
    		workflowMatchLogVO.setProcessMode(new ArrayList<String>());
    		workflowMatchLogVO.setWorkflowMatchMsgMap(new LinkedHashMap<String, List<String>>());
    	}
    	autoSkipNodeMap.put(autoSkipNodeId, workflowMatchLogVO);
    	workflowAutoSkipCacheMap.put(key, autoSkipNodeMap);
    	if(null==workflowMatchResultTimestampCacheMap.get(key)){
			workflowMatchResultTimestampCacheMap.put(key, System.currentTimeMillis());
		}
    	return workflowMatchLogVO;
	}

	@Override
	public void putWorkflowMatchLogMatchStateToCache(String key, String autoSkipNodeId,String nodeName, int matchState) {
    	if(Strings.isNotBlank(key) && Strings.isNotBlank(autoSkipNodeId)){
    		WorkflowMatchLogVO workflowMatchLogVO = getOrInitWorkflowMatchLogVO(key, autoSkipNodeId, nodeName);
	    	workflowMatchLogVO.setMatchState(matchState);
    	}
	}
    
	@Override
	public void putWorkflowMatchLogToCache(String stepIndex,String key, String autoSkipNodeId,String nodeName,List<String> canNotSkipMsgList) {
		putWorkflowMatchLogToCache(stepIndex,false, key, autoSkipNodeId, nodeName, canNotSkipMsgList);
	}
	
	@Override
	public void putWorkflowMatchLogToCacheHead(String stepIndex,String key, String autoSkipNodeId, String nodeName,List<String> canNotSkipMsgList) {
		putWorkflowMatchLogToCache(stepIndex,true, key, autoSkipNodeId, nodeName, canNotSkipMsgList);
	}
	
	private void putWorkflowMatchLogToCache(String stepIndex,boolean isAddFirst,String key, String autoSkipNodeId, String nodeName,List<String> canNotSkipMsgList){
		if(Strings.isNotBlank(key) && Strings.isNotBlank(autoSkipNodeId)){
			WorkflowMatchLogVO workflowMatchLogVO = getOrInitWorkflowMatchLogVO(key, autoSkipNodeId, nodeName);
	    	if(null==workflowMatchLogVO.getWorkflowMatchMsgMap().get(stepIndex)){
	    		workflowMatchLogVO.getWorkflowMatchMsgMap().put(stepIndex,new ArrayList<String>());
	    	}
	    	if(isAddFirst){
	    		workflowMatchLogVO.getWorkflowMatchMsgMap().get(stepIndex).addAll(0,canNotSkipMsgList);
	    	}else{
	    		workflowMatchLogVO.getWorkflowMatchMsgMap().get(stepIndex).addAll(canNotSkipMsgList);
	    	}
		}
	}
	
	@Override
	public void putWorkflowMatchLogMsgToCache(String stepIndex,String key, String autoSkipNodeId,String nodeName, String canNotSkipMsg) {
		if(Strings.isNotBlank(key) && Strings.isNotBlank(autoSkipNodeId)){
			List<String> canNotSkipMsgList= new ArrayList<String>();
			canNotSkipMsgList.add(canNotSkipMsg);
			putWorkflowMatchLogToCache(stepIndex,key, autoSkipNodeId,nodeName, canNotSkipMsgList);
		}
	}
	
	@Override
	public WorkflowMatchLogVO getWorkflowMatchLogList(String key, String nodeId) {
		if(Strings.isNotBlank(key) && Strings.isNotBlank(nodeId)){
			Map<String,WorkflowMatchLogVO> autoSkipNodeMap= workflowAutoSkipCacheMap.get(key);
	    	if(null!=autoSkipNodeMap){
	    		return autoSkipNodeMap.get(nodeId);
	    	}
		}
		return null;
	}
	
	@Override
	public List<ProcessLogDetail> getAllWorkflowMatchLogStr(String key,Set<String> selectNodeIds,ProcessLogDetail processLogDetail1) {
		int sort=0;
		List<ProcessLogDetail> allProcessLogDetailList= new ArrayList<ProcessLogDetail>();
		if(null!=processLogDetail1){
			processLogDetail1.setSort(sort);
			allProcessLogDetailList.add(processLogDetail1);
			sort ++;
		}
		Map<String,WorkflowMatchLogVO> autoSkipNodeMap= workflowAutoSkipCacheMap.get(key);
		if(null!=autoSkipNodeMap){
			Set<String> keys= autoSkipNodeMap.keySet();
			for (String nodeId : keys) {
				if( null!=selectNodeIds && !selectNodeIds.isEmpty() && !selectNodeIds.contains(nodeId)){ 
					continue;
				}
				WorkflowMatchLogVO vo= autoSkipNodeMap.get(nodeId);
				String nodeName= vo.getNodeName();
				
				LinkedHashMap<String, List<String>> workflowMatchMsgMap= vo.getWorkflowMatchMsgMap();
				String nodeMsg= JSONUtil.toJSONString(workflowMatchMsgMap);
				String processMode= JSONUtil.toJSONString(vo.getProcessMode());
				ProcessLogDetail processLogDetail= new ProcessLogDetail();
				processLogDetail.setIdIfNew();
				processLogDetail.setNodeId(nodeId);
				processLogDetail.setNodeName(nodeName);
				processLogDetail.setNodeType(vo.getNodeType());
				processLogDetail.setProcessMode(processMode);
				processLogDetail.setMatchSate(vo.getMatchState());
				processLogDetail.setNodeMsg(nodeMsg);
				processLogDetail.setSort(sort);
				allProcessLogDetailList.add(processLogDetail);
				sort++;
			}
		}
		return allProcessLogDetailList;
	}
    
    /* (non-Javadoc)
     * @see com.seeyon.v3x.common.workflow.engine.org.WorkFlowMatchUserManager#getUserList(java.lang.String, net.joinwork.bpm.definition.BPMHumenActivity, net.joinwork.bpm.engine.wapi.SeeyonBpmContext)
     */
    @Override
    public List<V3xOrgMember> getUserList(String domain, BPMHumenActivity humenActivity, WorkflowBpmContext context,boolean isUseAdditonUserIds)
            throws BPMException {
        ArrayList<V3xOrgMember> result = new UniqueList<V3xOrgMember>();
        @SuppressWarnings("rawtypes")
        List actorList = humenActivity.getActorList();
        if (actorList == null){
            return result;
        }
        if(context.isDebugMode()){//调试模式，则不调用组织模型的接口对人员信息进行校验
            return result;
        }


        //不知道为什么超级节点要在这里空绕一圈, 在超级节点那个点把这个信息去掉了
        if(ObjectName.isSuperNode(humenActivity)) {//超级节点
			V3xOrgMember newMember = new V3xOrgMember();
			newMember.setId(WorkflowUtil.getTableKey());
			newMember.setName(humenActivity.getName());
			newMember.setSortId(0l);
			newMember.setIsValid(true);
			newMember.setOrgAccountId(-1l);
			result.add(newMember);
			return result;
		}


		for (int i = 0; i < actorList.size(); i++) {
            BPMActor actor = (BPMActor) actorList.get(i);
            BPMParticipant party = actor.getParty();
            //actor标签的partyType属性值
            String partyTypeId = party.getType().id;
            //actor标签的partyId属性值
            String partyId = party.getId();
            if(WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(partyId)){//空节点，不用匹配
                break;
            }else{//其它类型的节点
                //log.info("需要匹配的人员类型:="+partyTypeId+","+partyId);
                try {
                    List<V3xOrgMember> users = getUserList(context, humenActivity, actor,isUseAdditonUserIds);
                    if(users!=null && users.size()>0){
                        result.addAll(users);
                    }
                }
                catch (Exception e) {
                	log.error("人员匹配异常", e);
                    throw new BPMException(e);
                }
            }
        }
        return result;
    }
    
    /**
     *  岗位：
     *  1.全集团、单位：getMembersByPost
     *  2.发起部门：getMembersByMemberPostOfUp（自动向上查找）
     *  3.表单控件：部门：getMembersByDepartmentPostOfDown（包含子部门）；单位：getMembersByPost
     *  4.发起者上级单位：summaryAccountId = account.getSuperior();----> getMembersByPost
     *  
     *  角色：
     *  xxx的工作部门指定角色对应的人员：getMembersByMemberRoleUp，accountId默认都给null
     *  
     *  单位：getMembersByType
     *  部门：getMembersByDepartment，是否包含子部门，通过参数控制
     *  职务级别：getMembersByType
     *  人员：getMembersByType
     *  组：getMembersByType
     *  部门岗位：getMembersByType
     *  部门角色：getMembersByType
     * @param context
     * @param activity
     * @param actor
     * @param partyTypeId
     * @return
     * @throws BPMException
     */
    private List<V3xOrgMember> getUserList(WorkflowBpmContext context, BPMHumenActivity activity, BPMActor actor,boolean isUseAdditonUserIds) throws Exception {
        BPMParticipant party = actor.getParty();
        //如果是动态角色，但经前一结点进行人工选择了处理者，则取到选择的人（addition存的内容）
        String addition = WorkflowUtil.getNodeAdditionFromContext(context, activity.getId(), party, "addition");
        String sender = context.getStartUserId();//发起者
        if(sender==null || "".equals(sender.trim()) ){
            if(context.getTheCase()!=null){
                sender = context.getTheCase().getStartUser();
            }else if(AppContext.getCurrentUser()!=null){
                sender = String.valueOf(AppContext.getCurrentUser().getId());
            }
            if(sender==null){
                log.info("WorkFlow 人员匹配:发起者为null，当前登录者也为null。");
                throw new BPMException("WorkFlow human match:sender is null, current user is null too.");
            }
            context.setStartUserId(sender);
            BPMProcess process= context.getProcess();
            BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
            context.setStartAccountId(startActor.getParty().getAccountId());
        }
        if(isUseAdditonUserIds){//优先从addition获取节点执行人员
            BPMSeeyonPolicy policy= activity.getSeeyonPolicy();
            if ( (addition != null && !"".equals(addition.trim())) || "2".equals(policy.getNa())) { 
                List<V3xOrgMember> aresult=  getUserListByAddition(addition,false);
                return aresult;
            }else{//有限从组织模型查询
                return getUserListFromOrgModule(context, activity, actor,true);
            }
        }else{//有限从组织模型查询
            return getUserListFromOrgModule(context, activity, actor,true);
        }
    }
    
    private List<V3xOrgMember>  getUserListFromCopyNode(WorkflowBpmContext context,String copyFrom, BPMHumenActivity activity, BPMActor actor) throws Exception {
        BPMProcess process= context.getProcess();
        BPMCase theCase= context.getTheCase();
        List<V3xOrgMember> aresult= new ArrayList<V3xOrgMember>();
        BPMActivity parentBpmActivity= process.getActivityById(copyFrom);
        boolean isFindMemberFromCopyNode= false;
        if(null!=parentBpmActivity){
            BPMActor pActor = (BPMActor)parentBpmActivity.getActorList().get(0);
            BPMParticipant pParty= pActor.getParty();
            String partyTypeId = pParty.getType().id;
            String partyId = pParty.getId();
            String nodeId= parentBpmActivity.getId();
            String keyId= nodeId+"_bakUserId";
            String oldUserId= WorkflowUtil.getOldUserInfoFromCase(theCase,keyId);
            if ("user".equals(partyTypeId) || WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.equals(partyId)){//人员节点
                V3xOrgMember member = null;
                if(partyId.equals(WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER)){
                    member = orgManager.getMemberById(Long.parseLong(context.getStartUserId()));
                }else{
                    if(WorkflowUtil.isNotBlank(oldUserId)){//C节点超期转给指定人了，则要用原来的节点的人
                        member= orgManager.getMemberById(Long.parseLong(oldUserId));
                    }else{
                        member = orgManager.getMemberById(Long.parseLong(partyId));
                    }
                }
                if(null!=member){
					if(!member.isValid()){//是否设置代理
						List<AgentModel> agentToList = MemberAgentBean.getInstance().getAgentModelToList(member.getId());
				        if(Strings.isNotEmpty(agentToList)){
				        	aresult.add(member);
				        }
					}else{
						aresult.add(member);
					}
                }
                isFindMemberFromCopyNode= true;
            }else{
                String prAddition= WorkflowUtil.getNodeAdditionFromContext(context, parentBpmActivity.getId(), pParty, "raddition");
                String keyRAddition= nodeId+"_bakRAddition";
                String bakRAddition= WorkflowUtil.getOldUserInfoFromCase(theCase,keyRAddition);
                if(WorkflowUtil.isNotBlank(bakRAddition)){////C节点超期转给指定人了，则要用原来的节点的人
                    prAddition= bakRAddition;
                }
                String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parentBpmActivity, "isDelete");
                if("false".equals(isDelete)){
                    if(WorkflowUtil.isNotBlank(bakRAddition)){////C节点超期转给指定人了，则要用原来的节点的人
                        aresult=  getUserListByAddition(prAddition,true);
                        isFindMemberFromCopyNode= true;
                    }else if(parentBpmActivity.getId().equals(context.getCurrentActivityId()) && parentBpmActivity.isCompetitionProcessMode()){
                        aresult=  getUserListByAddition(context.getCurrentUserId(),true);
                        if(null==aresult || aresult.isEmpty()){
                        	if(Strings.isNotBlank(context.getCurrentUserId()) && "-1".equals(context.getCurrentUserId())){
                        		if (WorkflowUtil.isNotBlank(prAddition) && !"null".equals(prAddition.trim()) && !"undefined".equals(prAddition.trim())){
                        			aresult=  getUserListByAddition(prAddition,true);
                                    isFindMemberFromCopyNode= true;
                        		}
                        	}
                        }
                        isFindMemberFromCopyNode= true;
                    }else if (WorkflowUtil.isNotBlank(prAddition) && !"null".equals(prAddition.trim()) && !"undefined".equals(prAddition.trim())){
                        aresult=  getUserListByAddition(prAddition,true);
                        isFindMemberFromCopyNode= true;
                    }else{
                        if(!context.getAllNotSelectNodes().contains(parentBpmActivity.getId())){
                        	isFindMemberFromCopyNode= true;
                            if(null!=context.getCopyNodePeopleMap().get(parentBpmActivity.getId())){
                                String[] addition= context.getCopyNodePeopleMap().get(parentBpmActivity.getId());
                                aresult=  getUserListByAddition(addition,true);
                            }else{
                            	//if( "2".equals(parentBpmActivity.getSeeyonPolicy().getNa())){
                            		
                            	//}else{
                            		//aresult=  getUserListFromOrgModule(context, activity, actor,false);
                            	//}
                            }
                        }else{
                            aresult=  getUserListFromOrgModule(context, activity, actor,false);
                        }
                    }
                }else{
                    aresult=  getUserListFromOrgModule(context, activity, actor,false);
                }
            }
        }
        if(isFindMemberFromCopyNode){
        	String text = parentBpmActivity.getCopyNumber();
    		if(text.length()<=1){
    			text = "0" + text;
    		}
    		String ctext = "C" + text;
    		String ptext= "P"+ text;
        	String matchRuleMsg= /*"人员匹配结果为:"*/ResourceUtil.getString("workflow.match.user.result")+WorkflowUtil.getMemberNames(aresult)+"["+ResourceUtil.getString("workflow.match.copy.from")+parentBpmActivity.getName()+"("+ctext+")]";
        	WorkflowNodeUsersMatchResult matchResult= new WorkflowNodeUsersMatchResult();
        	matchResult.setMatchRuleMsg(matchRuleMsg);
        	matchResult.setMembers(aresult);
        	matchResult.setNodeOrgType(/*"复制节点"*/ResourceUtil.getString("workflow.match.copy.node")+"["+ptext+"]");
        	putWorkflowMatchedCacheResult(context.getMatchRequestToken(), activity.getId(), matchResult);
        }
        return aresult;
    }

    private List<V3xOrgMember> getUserListFromOrgModule(WorkflowBpmContext context, BPMHumenActivity activity, BPMActor actor,boolean isGoCopayLogic) throws Exception {
        List<V3xOrgMember> result = null;
        BPMParticipant party = actor.getParty();
        //partyTypeId就是actor标签的partyType属性的值
        String partyTypeId = party.getType().id;
        String partyId = party.getId();
        String vjoin= party.getvJoin();
        String addition= WorkflowUtil.getNodeAdditionFromContext(context, activity.getId(), party, "addition");
        String copyFrom= activity.getCopyFrom();
        String copyNumber= activity.getCopyNumber();
        BPMProcess process= context.getProcess();
        BPMActivity parentBpmActivity= process.getActivityById(copyFrom);
        if(isGoCopayLogic && WorkflowUtil.isNotBlank(copyFrom) 
                && WorkflowUtil.isNotBlank(copyNumber)
                && !"null".equals(copyFrom)
                && !"null".equals(copyNumber)
                && !"undefined".equals(copyFrom)
                && !"undefined".equals(copyNumber) 
                && null!=parentBpmActivity){//节点复制，优先级最高
            result= getUserListFromCopyNode(context, copyFrom,activity,actor);
            if(null==result || result.size()<=0){
                if (addition != null && !"".equals(addition.trim())) {
                    List<V3xOrgMember> aresult=  getUserListByAddition(addition,false);
                    return aresult;
                }
            } 
        }else{
        	String key= context.getMatchRequestToken();
        	Integer needSelected=0;
        	WorkflowNodeUsersMatchResult cacheMatchResult = null;
        	if(Strings.isNotBlank(key)){
        		cacheMatchResult= getWorkflowMatchedCacheResultMsg(key,activity.getId());
        		needSelected= getWorkflowNeedSelectPeopleNode(key, activity.getId());
        	}
        	if(needSelected!=1){//没有匹配过
        		String nodePartyTypeId= partyTypeId;
        		if( "1".equals(vjoin) || "2".equals(vjoin) || "3".equals(vjoin)){
        			nodePartyTypeId= partyTypeId+vjoin;
        		}
        		HumenNodeMatchInterface humenNodeMatchInterface= HumenNodeMatchManager.getHumenNodeMatchInterface(nodePartyTypeId);
	            WorkFlowAppExtendManager matchOrgMemberManager = WorkFlowAppExtendInvokeManager.getAppManager(nodePartyTypeId);
	            if(null!=matchOrgMemberManager && null==humenNodeMatchInterface){//给应用扩展留个接口
	                result= matchOrgMemberManager.findMatchedOrgMembers(partyId, context.getFormData());
	            }else{
	            	Map<String,Object> matchContext= createMatchContext(context, activity, party, actor, process);
	            	if(null!=humenNodeMatchInterface){
	            		WorkflowNodeUsersMatchResult matchResult= humenNodeMatchInterface.getMatchedUsers(partyId, matchContext);
	                	if(null!=matchResult){
	                		result= matchResult.getMembers();
	                		if("WFDynamicForm".equals(partyTypeId)){
	                			List<DynamicFormMasterInfo> dynamicFormMasters = matchResult.getDynamicFormMasters();
	                			context.setDynamicFormMasterIds(dynamicFormMasters == null ? "" : JSONUtil.toJSONString(dynamicFormMasters));
                				String  humenNodeMatchAlertMsg = matchResult.getHumenNodeMatchAlertMsg();
	                			if(Strings.isNotBlank(humenNodeMatchAlertMsg)){
	                				context.setHumenNodeMatchAlertMsg(activity.getId(), humenNodeMatchAlertMsg);
	                				//matchResult.setHumenNodeMatchAlertMsg(context.getHumenNodeMatchAlertMsg());
	                			}
	                		}
	                		putWorkflowMatchedCacheResult(key, activity.getId(), matchResult);
	                		if(Strings.isNotBlank(matchResult.getHumenNodeMatchAlertMsg())){
                				return result;
                			}
	                	}
	            	}
	            }
        	}
        	else{
        		result = cacheMatchResult.getMembers(); 
        		if(Strings.isNotBlank(cacheMatchResult.getHumenNodeMatchAlertMsg())){
        		    context.setHumenNodeMatchAlertMsg(activity.getId(), cacheMatchResult.getHumenNodeMatchAlertMsg());
        			return result;
        		}
        	}
        	if(null!=result && result.size()==1){//节点上只有1个人，则直接返回
                return result;
            }
            if(context.isFindReplaceNodeUser()){
                return result;
            }
            
            if(activity.isMultipleProcessMode()){//多人执行模式,以addition中的为准
                if (addition != null && !"".equals(addition.trim())) {
                    List<V3xOrgMember> aresult=  getUserListByAddition(addition,false);
                    return aresult;
                }
            }else if(activity.isSingleProcessMode()){//单人执行模式
                if(null==result || (null!=result && result.size()!=1)){//正常匹配没有找到人或多于1个人，则以addition中的为准
                    if (addition != null && !"".equals(addition.trim())) {
                        List<V3xOrgMember> aresult=  getUserListByAddition(addition,false);
                        return aresult;
                    }
                }
            }else{//全体和竞争执行
                if(null!= result && result.size()>0){
                    return result;
                }else{
                    if (addition != null && !"".equals(addition.trim())) {
                        List<V3xOrgMember> aresult=  getUserListByAddition(addition,false);
                        return aresult;
                    }
                }
            }
        }
        return result;
    }

	private Map<String, Object> createMatchContext(WorkflowBpmContext context, BPMHumenActivity activity,BPMParticipant party, BPMActor actor,BPMProcess process) {
		
	    String partyId = party.getId();
		String partyTypeId = party.getType().id;
		
		String sender = context.getStartUserId();//发起者
    	if(sender==null || "".equals(sender.trim()) ){
            if(context.getTheCase()!=null){
                sender = context.getTheCase().getStartUser();
            }else if(AppContext.getCurrentUser()!=null){
                sender = String.valueOf(AppContext.getCurrentUser().getId());
            }
            context.setStartUserId(sender);
            BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
            context.setStartAccountId(startActor.getParty().getAccountId());
        }
    	Long startUserId= Long.parseLong(context.getStartUserId());
    	Long startAccountId= Long.parseLong(context.getStartAccountId());
    	
    	Long currentUserId=null;
    	if(Strings.isNotBlank(context.getCurrentUserId())){
    		currentUserId= Long.parseLong(context.getCurrentUserId());
    	}else{
    		currentUserId= AppContext.getCurrentUser().getId();
    	}
    	
    	Long currentAccountId = null;
    	if(Strings.isNotBlank(context.getCurrentAccountId())){
    		currentAccountId = Long.parseLong(context.getCurrentAccountId());
    	}else{
    	    currentAccountId = AppContext.getCurrentUser().getLoginAccount();
    	}
    	
    	boolean includeChild= party.isIncludeChild();
    	
    	BPMSeeyonPolicy policy= activity.getSeeyonPolicy();
    	String rupStr= policy.getRup();
        boolean rup = false;
        //表单部门角色控件特殊处理(rup 为1时开启自动向上匹配， 其他表示未开启)
        if("FormField".equals(partyTypeId) && checkIsFormFieldRole(partyId) ||"Department_Role".equals(partyTypeId)){
        	if("1".equals(rupStr)){
        		rup = true;
        	}
        }else{
        	if( null==rupStr || "1".equals(rupStr) || "-1".equals(rupStr) || "null".equals(rupStr) || "undefined".equals(rupStr) || "".equals(rupStr.trim())){//自动向上查找
        		rup = true;
        	}
        }
        String pupStr= policy.getPup();
        boolean pup= false;
        if( null==pupStr || "1".equals(pupStr) || "-1".equals(pupStr) || "null".equals(pupStr) || "undefined".equals(pupStr) || "".equals(pupStr.trim())){//自动向上查找
        	pup= true;
        }
        String rScope = policy.getRscope();
        String matchScope = "1";//匹配范围，1发起者单位 2全集团 3发起者部門-包含子部门 4表单控件决定 5发起者上级单位的集团基准岗,6-不包含子部门
        String formField= "";
        try {
            matchScope = policy.getMatchScope();
            String formFieldName = policy.getFormField() ;
            
            // 看不懂~~~~~~~~~~~~~~
            formField= formFieldName.replaceAll("-", WorkflowUtil.replaceUUID);
            
        } catch (Exception e) {
            log.error("岗位匹配中获取匹配范围和表单域名发生异常！", e);
        }
    	String[] preNodeTypeAndId= null;
    	if("Node".equals(partyTypeId) || "Role".equals(partyTypeId)){
    		preNodeTypeAndId= getPreNodeTypeAndId(context, activity, actor);
    	}
    	Map<String, Object> formFiledValueMap = (Map<String, Object>)context.getBusinessData().get(EventDataContext.CTP_FORM_DATA);
    	Map<String,Object> matchContext= new HashMap<String, Object>();
    	matchContext.put("startUserId", startUserId);//发起人ID
    	matchContext.put("startAccountId", startAccountId);//发起人所在单位ID
    	matchContext.put("currentUserId", currentUserId);//当前处理人ID
    	matchContext.put("currentAccountId", currentAccountId);//当前处理人所在单位ID
    	matchContext.put("partyTypeId", partyTypeId);//节点参与者类型
    	matchContext.put("includeChild", includeChild);//是否包含子部门
    	matchContext.put("rup", rup);//角色是否自动向上查找
    	matchContext.put("pup", pup);//岗位是否自动向上查找
    	matchContext.put("rScope", rScope);//角色匹配范围
    	matchContext.put("matchScope", matchScope);//岗位匹配范围
    	matchContext.put("formField", formField);//岗位匹配范围，表单控件决定
    	matchContext.put("nodeName", activity.getName());//岗位匹配范围，表单控件决定
    	matchContext.put("nodeId", activity.getId());//岗位匹配范围，表单控件决定
    	
    	if(null!=preNodeTypeAndId){
    	    String prePartyType = preNodeTypeAndId[0];
			matchContext.put("prePartyType", prePartyType);
			matchContext.put("prePartyId", preNodeTypeAndId[1]);
			matchContext.put("prePartyRealTypeName", preNodeTypeAndId[2]);
			
			String mu = policy.getMu();
			Long batchId = (Long) context.getBusinessData("WORKITEM_BATCH_ID");
			if(batchId != null && V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(prePartyType)
			        && BPMSeeyonPolicy.NODE_MATCH_ALL.equals(mu)){
			    //获取上节点全部人员
			    List<String> members = new ArrayList<String>();
			    try {
			        List<WorkitemDAO> workItems = itemlist.getWorkItemByBatch(batchId, new Integer[]{WorkitemInfo.STATE_READY,WorkitemInfo.STATE_NEEDREDO_TOME,WorkitemInfo.STATE_SUSPENDED, WorkitemInfo.STATE_FINISHED}, null);
			        List<HistoryWorkitemBLOBDAO> finishWorkItems = itemlist.getFinishWorkItemByBatch(batchId);
			        
			        for(WorkitemDAO w : workItems){
			            if(w.getPerformer() != null)
			                members.add(w.getPerformer());
			        }
			        for(HistoryWorkitemBLOBDAO w : finishWorkItems){
			            if(w.getPerformer() != null)
                            members.add(w.getPerformer());
                    }
			        
			        matchContext.put("allPrePartyId", members);
                } catch (Exception e) {
                    log.error("获取上节点全部信息失败", e);
                }
			}
		}
        String formAppId= process.getStart().getSeeyonPolicy().getFormApp();
        matchContext.put("formAppId", formAppId);
        matchContext.put(ActionRunner.WF_DYNAMIC_FORM_KEY, context.getDynamicFormMasterIds());
    	matchContext.put(EventDataContext.CTP_FORM_DATA, formFiledValueMap);
    	if(null!=context.getBusinessData().get(EventDataContext.CTP_FORM_DATA_DEF)){
    		Map<String, WorkflowFormFieldBO> formFiledValueDefMap = (Map<String, WorkflowFormFieldBO>)context.getBusinessData().get(EventDataContext.CTP_FORM_DATA_DEF);
    		matchContext.put(EventDataContext.CTP_FORM_DATA_DEF, formFiledValueDefMap);
    	} 
    	return matchContext;
	}

	/**
     * 从addition中解析出来前台选择的人员ID
     * @param addition
     * @return
     */
    private List<V3xOrgMember> getUserListByAddition(String addition,boolean isValid) {
        List<V3xOrgMember> result = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> resultAll = new ArrayList<V3xOrgMember>();
        if (addition != null && !"".equals(addition.trim())) {
            String[] additionList = addition.split(",");
            for(String subAddition : additionList){
                String memberId = subAddition.trim();
                try {
                    V3xOrgMember user = orgManager.getMemberById(new Long(memberId));
                    if(user != null){
                        if(user.isValid()){
                            result.add(user);
                        }
                        resultAll.add(user);
                    }
                }
                catch (Exception e) {
                    log.warn("", e);
                }
            }
            //Collections.sort(result, CompareSortUser.CompareSortUserInstance);
            if(result.size()>0){
                return result;
            }else{
                if(!isValid){
                    return resultAll;
                }else{
                    return result;
                }
            }
        }
        return result;
    }
    
    /**
     * 从addition中解析出来前台选择的人员ID
     * @param addition
     * @return
     */
    private List<V3xOrgMember> getUserListByAddition(String[] additionList,boolean isValid) {
        List<V3xOrgMember> result = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> resultAll = new ArrayList<V3xOrgMember>();
        if(null!=additionList){
            for(String subAddition : additionList){
                String memberId = subAddition.trim();
                try {
                    V3xOrgMember user = orgManager.getMemberById(new Long(memberId));
                    if(user != null){
                        if(user.isValid()){
                            result.add(user);
                        }
                        resultAll.add(user);
                    }
                }
                catch (Exception e) {
                    log.warn("", e);
                }
            }
        }
        //Collections.sort(result, CompareSortUser.CompareSortUserInstance);
        if(result.size()>0){
            return result;
        }else{
            if(!isValid){
                return resultAll;
            }else{
                return result;
            }
        }
    }
    
    /**
     * 根据表单控件的值，查询到将要匹配到的人，返回List<User>
     * @param matchMesssage
     * @param formFiledValueMap
     * @return
     */
    public List<User> getUserListFormField(String partyId, Map<String, Object> formFiledValueMap,WorkflowBpmContext context,boolean rup, String rScope){
        List<V3xOrgMember> members = null;
        String partyTypeId= "FormField";
        Long startUserId= Long.parseLong(context.getStartUserId());
    	Long startAccountId= Long.parseLong(context.getStartAccountId());
    	Long currentUserId= Long.parseLong(context.getCurrentUserId());
    	Long currentAccountId= Long.parseLong(context.getCurrentAccountId());
        Map<String,Object> matchContext= new HashMap<String, Object>();
    	matchContext.put("startUserId", startUserId);//发起人ID
    	matchContext.put("startAccountId", startAccountId);//发起人所在单位ID
    	matchContext.put("currentUserId", currentUserId);//当前处理人ID
    	matchContext.put("currentAccountId", currentAccountId);//当前处理人所在单位ID
    	matchContext.put("partyTypeId", partyTypeId);//节点参与者类型
    	matchContext.put("rup", rup);//角色是否自动向上查找
    	matchContext.put("rScope", rScope);//角色匹配范围
    	matchContext.put(EventDataContext.CTP_FORM_DATA, formFiledValueMap);
    	Map<String,Object> formFiledValueMapDef = (Map<String,Object>)context.getBusinessData().get(EventDataContext.CTP_FORM_DATA_DEF);
    	matchContext.put(EventDataContext.CTP_FORM_DATA_DEF, formFiledValueMapDef);
    	try {
	    	WorkflowNodeUsersMatchResult matchResult= HumenNodeMatchManager.getHumenNodeMatchInterface(partyTypeId).getMatchedUsers(partyId, matchContext);
	    	if(null!=matchResult){
	    		members= matchResult.getMembers();
	    		List<User> users = WorkflowUtil.v3xOrgMemberToWorkflowUser(members,false,true);
	            return users;
	    	}
    	}catch(Throwable e){
    		 log.error("", e);
    	}
        return null;
    }
    
    @Override
    public List<User> getUserListByTypeAndId(String typeAndIds) {
        String[] values = typeAndIds.split("[|]");
        List<V3xOrgMember> members = null;
        try {
            if(values!=null && values.length==3 && values[0].equals(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT)){
                if("true".equals(values[1].trim())){
                        members = orgManager.getMembersByDepartment(Long.parseLong(values[2]), true);
                } else {
                    members = orgManager.getMembersByDepartment(Long.parseLong(values[2]), false);
                }
            } else {
                Set<V3xOrgMember> ms = orgManager.getMembersByTypeAndIds(typeAndIds);
                if(ms!=null && ms.size()>0){
                    members = new ArrayList<V3xOrgMember>();
                    if (ms != null && !ms.isEmpty()) {
                        for (V3xOrgMember m : ms) {
                            if (m != null && m.isValid()) {//对人员不可用这种情况进行处理
                                members.add(m);
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("人员匹配中，没有一个有效的Id："+typeAndIds, e);
        } catch (BusinessException e) {
            log.error("人员匹配出现异常，调用组织模型借口发生错误，getUserListByTypeAndId", e);
        }
        return WorkflowUtil.v3xOrgMemberToWorkflowUser(members,false,true);
    }

    /**
     * 解析表单控件
     * @param partyId
     * @return [类型， 表单字段名， 表单实际字段名]
     */
    private String[] parseFormField(String partyId){
        
      //3.5中matchMesssage就是formField的display,5.0中是FormField|display
        //因为3.5中只支持选人的表单控件，5.0中人、部门都支持。
        
        int index = partyId.indexOf("|");
        int firstIndex= partyId.indexOf("@");
        int sencondIndex= partyId.indexOf("#");
        int thirdIndex = partyId.lastIndexOf("#");
        String role = null;
        String entityType = "Member";
        String fieldDisplayName = partyId;//老的还是用DisplayName,新的用filedName
        if( firstIndex > -1 && sencondIndex > -1 ){
            entityType = partyId.substring(0, firstIndex);
            fieldDisplayName= partyId.substring(firstIndex+1,sencondIndex);
            if(sencondIndex != thirdIndex){
                role = partyId.substring(thirdIndex+1);
            }
        }else if( index > -1 ){//兼容5.0老格式,避免bug
            entityType = partyId.substring(0, index);
            fieldDisplayName = partyId.substring(index+1);
        }
        
        return new String[]{entityType, fieldDisplayName, role};
    }
    
    public OrgManager getProcessOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager processOrgManager) {
        this.orgManager = processOrgManager;
    }

    private BPMAbstractNode findPreviousNode(long caseId, BPMAbstractNode node) {
        if(node.getUpTransitions().size() > 1){
            return null;
        }
        
        BPMTransition tran = (BPMTransition)node.getUpTransitions().get(0);
        BPMAbstractNode previouNode = (BPMAbstractNode)tran.getFrom();
        if(previouNode instanceof BPMAndRouter){
            return findPreviousNode(caseId, previouNode);
        }
        else if(previouNode instanceof BPMStart){//发起者 
            return previouNode;
        }
        else if(previouNode instanceof BPMStatus){
            return null;
        }
        else{
            return previouNode;
        }
    }
    
    private String[] findWorkitemMemberId(long caseId,Long workitemId,BPMAbstractNode node,WorkflowBpmContext context,boolean flag){
        if(!(node instanceof BPMHumenActivity)){
            return null;
        }
        String previouNodeType = null;
        String previouNodeRealTypeName = "";
        String previouNodeId = null;
        
        String preNodeType = ((BPMActor)node.getActorList().get(0)).getType().id;
        String preNodeId = ((BPMActor)node.getActorList().get(0)).getParty().getId();
        boolean hasDepartment = false;
        //flag为true时，表示强制查询上一节点中的成员。
        if(!flag){
        	HumenNodeMatchInterface humenNodeMatchInterface= HumenNodeMatchManager.getHumenNodeMatchInterface(preNodeType);
        	if(null!=humenNodeMatchInterface){
        		String masterId = getDynamicFormMasterId(node, context);
    			
    			Map<String, Object> formFiledValueMap = (Map<String, Object>)context.getBusinessData().get(EventDataContext.CTP_FORM_DATA);
        		WorkflowOrgnazitionVO orgDefVo= humenNodeMatchInterface.findMatchedOrgVo(preNodeType, preNodeId, formFiledValueMap,masterId);
        		if(null!=orgDefVo && orgDefVo.getType().equals(OrgConstants.ORGENT_TYPE.Department)){
	                hasDepartment= true;
	                previouNodeType = V3xOrgEntity.ORGENT_TYPE_DEPARTMENT;
	                previouNodeId = orgDefVo.getId().toString();
	                previouNodeRealTypeName= orgDefVo.getRealTypeName();
	            }
        	}
        }
        if(!hasDepartment){
            String performer= null;
            if(workitemId!=-1){
                WorkItem workitem = getWorkItemById(workitemId.longValue());
                if(workitem != null){
                    performer = workitem.getPerformer();
                }
            }
            if(performer == null){
                performer = getLastPerformerOfActivity(caseId, node.getId());
                log.warn("无法确定workitem，根据case判断最后处理人");
            }
            if(performer != null){
                previouNodeType = V3xOrgEntity.ORGENT_TYPE_MEMBER;
                previouNodeId = performer;
            }
            else{
                return null;
            }
        }
        
        return new String[]{previouNodeType, previouNodeId,previouNodeRealTypeName};
    }

	private String getDynamicFormMasterId(BPMAbstractNode node, WorkflowBpmContext context) {
		String dyForm = (String) context.getTheCase().getDataMap().get(ActionRunner.WF_DYNAMIC_FORM_KEY);
		
		String masterId = "" ; 
		if (Strings.isNotEmpty(dyForm)) {
			List dfmis = null;
			try {
				dfmis = JSONUtil.parseJSONString(dyForm, List.class);
			} catch (Throwable e) {
				log.error("", e);
			}
			if(dfmis!=null){
				for (Object o : dfmis) {
					if (o instanceof Map) {
						DynamicFormMasterInfo dfmi = new DynamicFormMasterInfo();
						ParamUtil.mapToBean((Map) o, dfmi, false);
						List<String> nodeIds = dfmi.getNodeIds();
						if (nodeIds.contains(node.getId())) {
							masterId = dfmi.getMasterIds().get(0);
						}
					}
				}
			}
		}
		return masterId;
	}
    
    private static WorkItem getWorkItemById(long workitemId) {
        WorkItem wi = null;
        try {
            WorkItemManager wim = WAPIFactory.getWorkItemManager("Task_1");
            if(wim != null){
                wi = wim.getWorkItemOrHistory(workitemId);
            }
        } catch (BPMException ex) {
            log.error("", ex);
        }
        return wi;
    }
    
    private static String getLastPerformerOfActivity(long caseid, String nodeId){
        String lasePerformer = null;
        try {
            WorkItemManager wim = WAPIFactory.getWorkItemManager("Task_1");
            if(wim != null){
                lasePerformer = wim.getLastPerformerOfActivity(caseid, nodeId);
            }
        } catch (BPMException ex) {
            log.error("", ex);
        }
        return lasePerformer;
    }
    
    private String[] getPreNodeTypeAndId(WorkflowBpmContext context, BPMHumenActivity activity, BPMActor actor){
    	return getPreNodeTypeAndId(context, activity, actor, false);
    }
    
    /**
     * 获取到上一节点的类型和Id
     * @param context
     * @param activity
     * @param actor
     * @param flag true时，强制只查询节点中的最后一个处理人(竞争节点时找当前处理人)。
     * @return
     */
    private String[] getPreNodeTypeAndId(WorkflowBpmContext context, BPMHumenActivity activity, BPMActor actor, boolean flag){
        String sender = context.getStartUserId();//发起者
        BPMCase theCase = context.getTheCase();
        BPMParticipant party = actor.getParty();
        String preNodeType = null;
        String preNodeRealTypeName = null;
        String preNodeId = null;
        //partyId就是actor标签的partyType属性的值
        String partyId = party.getId();
        //partyTypeId就是actor标签的partyId属性的值
        String partyTypeId = party.getType().id;
        if(WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(partyId)){ //空节点，不用匹配
            return null; 
        }
        if (context != null && (partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_DYNAMIC_ROLE)
                || partyTypeId.equals(WorkFlowMatchUserManager.ORGREL_TYPE_ACCOUNT_ROLE)
                || partyTypeId.equals(V3xOrgEntity.ORGENT_TYPE_POST)
                || partyTypeId.equals(V3xOrgEntity.ORGREL_TYPE_DEP_POST)
                || partyTypeId.equals(V3xOrgEntity.ORGREL_TYPE_DEP_ROLE)
                || partyTypeId.equals(WorkFlowMatchUserManager.ORGENT_META_KEY_NODE))) {
            
            if (theCase != null && !"start".equals(context.getCurrentActivityId())) {
                String startUser = theCase.getStartUser();
                preNodeType = V3xOrgEntity.ORGENT_TYPE_MEMBER; 
                preNodeId = startUser;
                
                if (partyId.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSER)) {//上节点部门主管、上节点上级部门主管
                    BPMAbstractNode currentActivity = context.getProcess().getActivityById(context.getCurrentActivityId()); //当前处理者的节点
                    if(currentActivity == null || activity.getId().equals(currentActivity.getId())){
                        currentActivity = findPreviousNode(theCase.getId(), activity);//上一个节点
                    }
                    
                    if(currentActivity != null){
                        long workitemId = context.getCurrentWorkitemId();
                        
                        //上节点主管各部门/上节点分管各部门  和上节点汇报人 情况下直接查询最后一个处理人
                        if(partyId.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERMANAGEDEPTMEMBER)
                                || partyId.startsWith(WorkFlowMatchUserManager.ORGENT_META_KEY_NODEUSERLEADERDEPTMEMBER)
                                || partyId.endsWith(WorkFlowMatchUserManager.ReciprocalRoleReporter)){
                            flag = true;
                        }
                        
                        String[] preNode = findWorkitemMemberId(theCase.getId(),workitemId,currentActivity,context,flag);//上一个节点
                        if(preNode != null){
                            preNodeType = preNode[0]; 
                            preNodeId = preNode[1];
                            preNodeRealTypeName = preNode[2];
                            if("start".equals(preNodeId)){
                                preNodeId = startUser;
                            }
                        }
                        else{
                            if(currentActivity.getNodeType().equals(BPMAbstractNode.NodeType.start)){//上一节点为开始节点
                                preNodeType = V3xOrgEntity.ORGENT_TYPE_MEMBER; 
                                preNodeId = theCase.getStartUser();
                            }else{
                                return null;
                            }
                        }
                    }
                }
            }
            else {
                preNodeType = V3xOrgEntity.ORGENT_TYPE_MEMBER;
                preNodeId = sender;
            }
        }
        return new String[]{preNodeType, preNodeId,preNodeRealTypeName};
    }
    
    /**
     * 判断是不是表单角色控件
     * @param partyId
     * @return
     */
	private boolean checkIsFormFieldRole(String partyId) {
	    //partyId: Department@field0023#部门#DepManager
		boolean isRole = false;
		if(Strings.isBlank(partyId)){
			return isRole;
		}
		int firstIndex = partyId.indexOf("@");
		int sencondIndex = partyId.indexOf("#");
		int thirdIndex = partyId.lastIndexOf("#");
		String role = null;
		if(firstIndex > -1 && sencondIndex > -1 && sencondIndex != thirdIndex) {
			role = partyId.substring(thirdIndex + 1);
		} 
		if(Strings.isNotBlank(role)){
			isRole = true;
		}
		return isRole;
	}
}
