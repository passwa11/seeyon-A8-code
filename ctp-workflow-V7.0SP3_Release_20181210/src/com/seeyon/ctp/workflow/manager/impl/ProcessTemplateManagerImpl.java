/**
 * Author: wangchw
 * Rev: ProcessTemplateManagerImpl.java
 * Date: 20122012-7-30下午01:25:07
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemInitializer;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.template.vo.CtpTemplateVO;
import com.seeyon.ctp.organization.bo.MemberRole;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.bo.ProcessTemplateNodeSimpleBO;
import com.seeyon.ctp.workflow.dao.IProcessTemplateDao;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.ProcessTemplateManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.manager.WFProcessPropertyManager;
import com.seeyon.ctp.workflow.po.ProcessTemplateCacheActivity;
import com.seeyon.ctp.workflow.po.ProcessTemplateCacheObject;
import com.seeyon.ctp.workflow.po.ProcessTemplete;
import com.seeyon.ctp.workflow.po.SubProcessSetting;
import com.seeyon.ctp.workflow.po.WFProcessProperty;
import com.seeyon.ctp.workflow.util.NodeReplaceControlUtil;
import com.seeyon.ctp.workflow.util.WorkflowEventConstants;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.WFPropertyBean;

import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;

/**
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流流程定义模版管理manager层</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-30 下午01:25:07
*/
public class ProcessTemplateManagerImpl implements ProcessTemplateManager,SystemInitializer {
    
    private final static Log   logger    = CtpLogFactory.getLog(ProcessTemplateManagerImpl.class);

    private IProcessTemplateDao processTemplateDao = null;
    
    private SubProcessManager subProcessManager = null;
    
    private WFProcessPropertyManager wfProcessPropertyManager;
    
    private CacheMap<Long, ProcessTemplateCacheObject> ProcessTemplateCache = null;
  
    private Set<String> roleCacheSet = new HashSet<String>();
    
    private OrgManager orgManager;

    public void setSubProcessManager(SubProcessManager subProcessManager) {
        this.subProcessManager = subProcessManager;
    }

    private static ProcessOrgManager processOrgManager;
    public static ProcessOrgManager getProcessOrgManager() {
        if (processOrgManager == null) {
            try {
                processOrgManager = (ProcessOrgManager) AppContext.getBean("processOrgManager");
            } catch (Throwable e) {
                //log.error(e.getMessage(), e);
            }
        }
        return processOrgManager;
    }
    /**
     * @param processTemplateDao the processTemplateDao to set
     */
    public void setProcessTemplateDao(IProcessTemplateDao processTemplateDao) {
        this.processTemplateDao = processTemplateDao;
    }

    /**
     * @param wfProcessPropertyManager the wfProcessPropertyManager to set
     */
    public void setWfProcessPropertyManager(WFProcessPropertyManager wfProcessPropertyManager) {
        this.wfProcessPropertyManager = wfProcessPropertyManager;
    }
    
    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    private class ProcessTempleteCacheInitThread extends Thread{

		@Override
		public void run() {
			try {
				boolean isContinue= true;
				int pageSize= 100;
				int page= 0;
				FlipInfo flipInfo= new FlipInfo();
		        flipInfo.setNeedTotal(false);
				while(isContinue){
					page ++;
			        flipInfo.setPage(page);
			        flipInfo.setSize(pageSize+1);
			        List<ProcessTemplete> templates = processTemplateDao.selectProcessTemplateList(flipInfo);
			        for (ProcessTemplete t : templates) {
		                ProcessTemplateCache.put(t.getId(), new ProcessTemplateCacheObject(t));
		            }
			        int resultSize= templates.size();
			        if(resultSize<=pageSize){
			        	isContinue= false;
		            }
				}
	            logger.info("加载ProcessTemplateCacheObject: " + ProcessTemplateCache.size());
	            
	            CacheFactory.resetAllStatistics();
	        }
	        catch (BPMException e) {
	            logger.error("", e);
	        }
		}
    	
    }

    public void initialize(){
        CacheAccessable factory = CacheFactory.getInstance(ProcessTemplateManager.class); 
        ProcessTemplateCache = factory.createMap("ProcessTemplateCache");
        ProcessTempleteCacheInitThread thread= new ProcessTempleteCacheInitThread();
        thread.start();
    }
    
    private void updateCache(ProcessTemplete p){
    	if(null!=ProcessTemplateCache){
    		ProcessTemplateCache.put(p.getId(), new ProcessTemplateCacheObject(p));
    	}
    }
    
    private void updateCache(Long id, String processName, String processXml){
    	if(null!=ProcessTemplateCache){
    		ProcessTemplateCache.put(id, new ProcessTemplateCacheObject(id, processName, processXml));
    	}
    }
    
    private void updateCache(Long id, String processName, BPMProcess process){
    	if(null!=ProcessTemplateCache){
    		ProcessTemplateCache.put(id, new ProcessTemplateCacheObject(id, processName, process));
    	}
    }
    
    public void destroy(){
    }

    public String insertProcessTemplate(String processName,String processXml,String createUser, long appId) throws BPMException{
        BPMProcess process= BPMProcess.fromXML(processXml);
        if(null==process){
            throw new BPMException("processXml is null!"); 
        }
        //state作为备用字段，暂时不对外暴露（1表示已发布，0表示未发布）
        int state = 1;
        String id = processTemplateDao.insertProcessTemplate(processName, processXml, createUser, appId, state);
        this.updateCache(new Long(id), processName, process);
        
        return id;
    }

	public String getBPMActivityDesc(Long processTemplateId, String nodeId) {
		String desc = "";
		ProcessTemplateCacheObject cacheObject = ProcessTemplateCache.get(processTemplateId);
		if (cacheObject != null) {
		    ProcessTemplateCacheActivity cacheActivity = cacheObject.getActivityMap().get(nodeId);
			if (cacheActivity != null) {
				desc = cacheActivity.getDesc();
			}
		}
		return desc;
	}
    @Override
    public String selectProcessTempateXml(String processTempateId) throws BPMException {
        String processXml= processTemplateDao.selectProcessTempateXml(processTempateId);
        if(processXml!=null && processXml.startsWith("<processes>")){//老数据
            BPMProcess process= BPMProcess.fromXML(processXml);
            processXml= process.toXML(null,true);
        }
        return processXml;
    }

    @Override
    public List<ProcessTemplete> selectProcessTemplateList() throws BPMException{
        return new ArrayList();
    }

    @Override
    public List<ProcessTemplete> selectProcessTemplateList(boolean formFlag, Long appId)
            throws BPMException {
        //暂时查询所有state=1的模版
        return processTemplateDao.selectProcessTemplateList(formFlag, appId, null);
    }

    @Override
    public ProcessTemplete selectProcessTempate(String processId) throws BPMException {
        ProcessTemplete t= processTemplateDao.selectProcessTempate(processId);
        return t;
    }

    @Override
    public ProcessTemplete selectProcessTemplateById(Long templateId) throws BPMException {
        return processTemplateDao.selectProcessTemplateById(templateId);
    }

    @Override
    public List<ProcessTemplete> selectProcessTemplateByIdList(List<Long> templateId) throws BPMException {
        return processTemplateDao.selectProcessTemplateByIdList(templateId);
    }

    @Override
    public List<ProcessTemplete> selectProcessTemplateByOldIdList(List<Long> templateId) throws BPMException {
        return processTemplateDao.selectProcessTemplateByOldIdList(templateId);
    }

    @Override
    public List<ProcessTemplete> selectProcessTemplateList(Long appId, Integer state) throws BPMException {
        return processTemplateDao.selectProcessTemplateList(true, appId, state);
    }

    @Override
    public long insertProcessTemplate(String moduleType, String processName, String processXml, String workflowRule,
            String createUser, long formId, long batchId, int state, String process_subsetting) throws BPMException {
        long id= processTemplateDao.insertProcessTemplate(moduleType,processName, processXml, workflowRule,null,createUser, formId, batchId,state);
        if(state==1){
        	this.updateCache(id, processName, processXml);
        }
        //创建子流程
        if(process_subsetting!=null && process_subsetting.trim().length()>0){
            List<SubProcessSetting> subProcessList = WorkflowUtil.createSubSettingFromStringArray(process_subsetting);
            if(subProcessList!=null && subProcessList.size()>0){
                Date date = new Date();
                for(SubProcessSetting setting : subProcessList){
                    setting.setTempleteId(id);
                    setting.setCreateTime(date);
                    setting.setId(UUIDLong.longUUID());
                }
                subProcessManager.saveSubProcessSetting(subProcessList);
            }
        }
        return id;
    }
    
    @Override
    public long insertProcessTemplate(String moduleType, String processName, String processXml, String workflowRule,
            String createUser, long formId, long batchId, int state, String process_subsetting, String processEventJson) throws BPMException {
        long id= processTemplateDao.insertProcessTemplate(moduleType,processName, processXml, workflowRule,null,createUser, formId, batchId,state);
        if(state==ProcessTemplete.STATE_PUBLISH){
        	this.updateCache(id, processName, processXml);
        }
        //创建子流程
        if(process_subsetting!=null && process_subsetting.trim().length()>0){
            List<SubProcessSetting> subProcessList = WorkflowUtil.createSubSettingFromStringArray(process_subsetting);
            if(subProcessList!=null && subProcessList.size()>0){
                Date date = new Date();
                for(SubProcessSetting setting : subProcessList){
                    setting.setTempleteId(id);
                    setting.setCreateTime(date);
                    setting.setId(UUIDLong.longUUID());
                }
                subProcessManager.saveSubProcessSetting(subProcessList);
            }
        }
        saveWorkflowProcessProperty(id, processEventJson);
        return id;
    }
    
    public void saveWorkflowProcessProperty(long processTemplateId, String processEventJson) throws BPMException {
        if(WorkflowUtil.isNotBlank(processEventJson) && !"{}".equals(processEventJson)){
            Map<String, String> processEventMap = (Map<String, String>)JSONUtil.parseJSONString(processEventJson);
            if(!processEventMap.isEmpty()){
                WFProcessProperty processProperty = new WFProcessProperty();
                processProperty.setNewId();
                processProperty.setProcessId(processTemplateId);
                processProperty.setType(WorkflowEventConstants.TEMPLAE_TYPE);
                WFPropertyBean propertyBean = new WFPropertyBean();
                propertyBean.setProcessIdOrTemplateId(processTemplateId);
                String exentExt = processEventMap.get("event_ext");
                processEventMap.remove("event_ext");
                for(Map.Entry<String, String> entry:processEventMap.entrySet()){
                    String nodeId = entry.getKey();
                    String eventString = entry.getValue();
                    Map<String, String> workflowEventMap = new LinkedHashMap<String, String>();
                    if(Strings.isNotBlank(eventString)){
                        String[] eventArray = eventString.split("\\|");
                    	for (String s : eventArray) {
                    		String[] arr = s.split("=");
                    		workflowEventMap.put(arr[0], arr[1]);
                    	}
                    }
                    if("global".equals(nodeId)){
                        propertyBean.add(WorkflowEventConstants.WORKFLOWEVENT, JSONUtil.toJSONString(workflowEventMap));
                    } else {
                        propertyBean.add(nodeId, WorkflowEventConstants.WORKFLOWEVENT, JSONUtil.toJSONString(workflowEventMap));
                    }
                }
                processProperty.setValue(propertyBean.toJson());
                processProperty.setEventExt(exentExt);
                wfProcessPropertyManager.insertProcessProperty(processProperty);
            }
        }
    }

    @Override
    public void saveImportedProcessTemplate(String moduleType, Long templateId, String processName, String processXml,
            String workflowRule, String createUser, long formId, long batchId, int state, String process_subsetting)
            throws BPMException {
        processTemplateDao.saveImportedProcessTemplate(moduleType, templateId, processName, processXml, workflowRule, null,createUser, formId, batchId,state);
        this.updateCache(templateId, processName, processXml);
        //创建子流程
        if(process_subsetting!=null && process_subsetting.trim().length()>0){
            List<SubProcessSetting> subProcessList = WorkflowUtil.createSubSettingFromStringArray(process_subsetting);
            if(subProcessList!=null && subProcessList.size()>0){
                Date date = new Date();
                for(SubProcessSetting setting : subProcessList){
                    setting.setTempleteId(templateId);
                    setting.setCreateTime(date);
                    setting.setId(UUIDLong.longUUID());
                }
                subProcessManager.saveSubProcessSetting(subProcessList);
            }
        }
    }

    @Override
    public void saveImportedProcessTemplate(String moduleType, List<ProcessTemplete> templeteList) throws BPMException {
        processTemplateDao.saveImportedProcessTemplate(moduleType, templeteList);
        for (ProcessTemplete processTemplete : templeteList) {
            this.updateCache(processTemplete);
        }
    }

    @Override
	public int updateProcessTemplate(List<ProcessTemplete> templateList)
			throws BPMException {
        for (ProcessTemplete processTemplete : templateList) {
            String processXml= processTemplete.getWorkflow();
            processTemplete.setWorkflow(WorkflowUtil.replaceAscii160ToAscii36(processXml));
            
            this.updateCache(processTemplete);
        }
		return processTemplateDao.updateProcessTemplate(templateList);
	}

	@Override
    public long updateProcessTemplate(String moduleType, String processName, String processXml, String workflowRule,
            String modifyUser, long formId, long batchId, int state, long id, String process_subsetting) throws BPMException {
        return updateProcessTemplate(moduleType, processName, processXml, workflowRule, modifyUser, formId, batchId,
                state, id, process_subsetting, null);
    }
	
	public void updateProcessTemplateXMLAndEvents(long id,String processXML,String events) throws BPMException {
	    if(Strings.isBlank(processXML) && Strings.isBlank(events)){
	        return;
	    }
	   
        
        //修改流程图
        ProcessTemplete pt= processTemplateDao.selectProcessTempate(String.valueOf(id));
        pt.setWorkflow(processXML);
        pt.setModifyDate(new java.sql.Timestamp(System.currentTimeMillis()));
        pt.setModifyUser(String.valueOf(AppContext.currentUserId()));
        processTemplateDao.updateProcessTemplate(pt);
        
        //更新缓存
        this.ProcessTemplateCache.remove(id);
        this.updateCache(pt.getId(),pt.getProcessName(),processXML);
        
        
        //保存事件
        
        wfProcessPropertyManager.deleteProcessPropertyByProcessId(id);
        saveWorkflowProcessProperty(id,events);
	}
	
	@Override
    public long updateProcessTemplate(String moduleType, String processName, String processXml, String workflowRule,
            String modifyUser, long formId, long batchId, int state, long id, String process_subsetting, String processEventJson) throws BPMException {
        long newId= 0;
        ProcessTemplete oldPT= processTemplateDao.selectProcessTempate(String.valueOf(id));
        //是否有修改过流程， 如果修改过流程的话，那么子流程绑定数据才跟着一起修改
        boolean isChangeProcess = false;
        if(null!=oldPT){
            String newProcessXml = oldPT.getWorkflow();
            if(WorkflowUtil.isNotNull(processXml)){
                isChangeProcess = true;
                newProcessXml = processXml;
            }
            //是否修改了标题
            boolean changeTitleFlag = false;
            if(oldPT.getProcessName() != null && processName != null && !oldPT.getProcessName().equals(processName.trim())){
                changeTitleFlag = true;
            }
            if(oldPT.getState() == ProcessTemplete.STATE_DRAFT){//草稿状态
                //直接更新原草稿状态的记录
                if(WorkflowUtil.isNotNull(processName)){
                    oldPT.setProcessName(processName);
                }
                if(WorkflowUtil.isNotNull(processXml)){
                    oldPT.setWorkflow(processXml);
                }
                if(WorkflowUtil.isNotNull(workflowRule)){
                    oldPT.setWorkflowRule(workflowRule);
                }
                oldPT.setModifyUser(modifyUser);
                oldPT.setModifyDate(new Timestamp(System.currentTimeMillis()));
                oldPT.setAppId(formId);
                oldPT.setBatchId(batchId);
                oldPT.setState(state);
                if(state==1){
                	this.updateCache(oldPT);
                }
                processTemplateDao.updateProcessTemplate(oldPT);
                newId = id;
                if(changeTitleFlag){
                    //如果标题被更新，并且当前模版作为别的流程的子流程，那么更新子流程绑定数据
                    List<SubProcessSetting> subSettingList = subProcessManager.getAllSubProcessSettingByNewflowTempleteId(String.valueOf(newId));
                    if(subSettingList!=null && subSettingList.size()>0){
                        for(SubProcessSetting set : subSettingList){
                            set.setSubject(processName);
                        }
                        subProcessManager.updateSubProcessSetting(subSettingList);
                    }
                }
            }else if(oldPT.getState() == ProcessTemplete.STATE_PUBLISH){//已发布状态
                List<Long> oldTemplateIdList = new ArrayList<Long>();
                oldTemplateIdList.add(id);
                List<ProcessTemplete> processTemplateList = processTemplateDao.selectProcessTemplateByOldIdList(oldTemplateIdList);
                if(isChangeProcess){
                    //流程修改才删除流程事件信息，后面会重新插入
                    if(processTemplateList != null && !processTemplateList.isEmpty()){
                        for (ProcessTemplete processTemplete : processTemplateList) {
                            //删除流程事件信息
                            WFProcessProperty deleteProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplete.getId());
                            if(null != deleteProcessProperty){
                                wfProcessPropertyManager.deleteProcessProperty(deleteProcessProperty);
                            }
                        }
                    }
                }
                //删除历史记录
                this.ProcessTemplateCache.remove(id);
                processTemplateDao.deleteProcessTemplateByOldId(id);
                //新增一条新的草稿状态的记录
                newId = processTemplateDao.insertProcessTemplate(moduleType, processName, newProcessXml, workflowRule,id,modifyUser, formId, batchId, state);
                if(state==1){
                	this.updateCache(newId,processName,newProcessXml);
                }
                if(changeTitleFlag){
                    //如果标题被更新，并且当前模版作为别的流程的子流程，那么更新子流程绑定数据
                    List<SubProcessSetting> subSettingList = subProcessManager.getAllSubProcessSettingByNewflowTempleteId(String.valueOf(newId));
                    if(subSettingList!=null && subSettingList.size()>0){
                        for(SubProcessSetting set : subSettingList){
                            set.setSubject(processName);
                        }
                        subProcessManager.updateSubProcessSetting(subSettingList);
                    }
                }
                //添加草稿记录后，如果流程没有修改，那么没有设置子流程，此时需要将老模版的子流程绑定数据添加进来
                //如果流程修改了，那么就没有必要拷贝老模版的子流程了，后面会添加的。
                if(!isChangeProcess){
                    List<SubProcessSetting> subSettingList = subProcessManager.getAllSubProcessSettingByTemplateId(String.valueOf(oldPT.getId()), null);
                    if(subSettingList!=null && subSettingList.size()>0){
                        List<SubProcessSetting> newSubSettingList = new ArrayList<SubProcessSetting>(subSettingList.size());
                        for(SubProcessSetting set : subSettingList){
                            SubProcessSetting newSet = new SubProcessSetting();
                            newSet.setId(UUIDLong.longUUID());
                            newSet.setTempleteId(newId);
                            newSet.setConditionBase(set.getConditionBase());
                            newSet.setConditionTitle(set.getConditionTitle());
                            newSet.setCreateTime(set.getCreateTime());
                            newSet.setFlowRelateType(set.getFlowRelateType());
                            newSet.setIsCanViewByMainFlow(set.getIsCanViewByMainFlow());
                            newSet.setIsCanViewMainFlow(set.getIsCanViewMainFlow());
                            newSet.setIsForce(set.getIsForce());
                            newSet.setNewflowSender(set.getNewflowSender());
                            newSet.setNewflowTempleteId(set.getNewflowTempleteId());
                            newSet.setNodeId(set.getNodeId());
                            newSet.setSubject(set.getSubject());
                            newSet.setTriggerCondition(set.getTriggerCondition());
                            newSubSettingList.add(newSet);
                        }
                        subProcessManager.saveSubProcessSetting(newSubSettingList);
                    }
                    
                    //流程没有修改的情况，需要更新流程事件中的processId
                    if(processTemplateList != null && !processTemplateList.isEmpty()){
                        for (ProcessTemplete processTemplete : processTemplateList) {
                            WFProcessProperty processProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplete.getId());
                            if(processProperty != null){
                                 WFPropertyBean propertyBean = new WFPropertyBean();
                                 propertyBean.fromJson(processProperty.getValue());
                                 //主要为更新processIdOrTemplateId
                                 propertyBean.setProcessIdOrTemplateId(newId);
                                 processProperty.setValue(propertyBean.toJson());
                                 processProperty.setProcessId(newId);
                                 wfProcessPropertyManager.updateProcessProperty(processProperty);
                            }
                        }
                    } else {
                        WFProcessProperty processProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(oldPT.getId());
                        if(processProperty != null){
                            WFProcessProperty newProcessProperty = new WFProcessProperty();
                            newProcessProperty.setNewId();
                            newProcessProperty.setProcessId(newId);
                            newProcessProperty.setType(WorkflowEventConstants.TEMPLAE_TYPE);
                            WFPropertyBean propertyBean = new WFPropertyBean();
                            propertyBean.fromJson(processProperty.getValue());
                            //主要为更新processIdOrTemplateId
                            propertyBean.setProcessIdOrTemplateId(newId);
                            newProcessProperty.setValue(propertyBean.toJson());
                            wfProcessPropertyManager.insertProcessProperty(newProcessProperty);
                        }
                    }
                }
                
            }else if(oldPT.getState() == ProcessTemplete.STATE_DELETE){//待删除状态
                //更新原始记录为已发布状态
                oldPT.setModifyUser(modifyUser);
                oldPT.setModifyDate(new Timestamp(System.currentTimeMillis()));
                oldPT.setState(ProcessTemplete.STATE_PUBLISH);
                this.updateCache(oldPT);
                processTemplateDao.updateProcessTemplate(oldPT);
                //新增一条新的草稿状态的记录
                newId= processTemplateDao.insertProcessTemplate(moduleType, processName, newProcessXml, workflowRule,id,modifyUser, formId, batchId, state);
                if(state == ProcessTemplete.STATE_PUBLISH){
                	this.updateCache(newId,processName,newProcessXml);
                }
            }
        }else{
            throw new BPMException("cannot find ProcessTemplete by id("+ id +")!");
        }
        //更新的时候不需要删除老的子流程，只需要创建新的子流程
        //subProcessManager.deleteSubProcessSettingByMainTempleteId(id);
        //创建子流程
        //删除掉模版以前绑定的子流程，更新新绑定的子流程的主流程模版号。
        if(isChangeProcess){
            subProcessManager.deleteSubProcessSettingByMainTempleteId(newId);
            if(process_subsetting!=null && process_subsetting.trim().length()>0){
                String newProcessXml = oldPT.getWorkflow();
                if(WorkflowUtil.isNotNull(processXml)){
                    newProcessXml = processXml;
                }
                BPMProcess process = BPMProcess.fromXML(newProcessXml);
                //保存子流程之前，先看子流程绑定的节点id是否在流程中存在，若是不存在的话，则不去保存子流程
                if(process!=null){
                    List<SubProcessSetting> subProcessList = WorkflowUtil.createSubSettingFromStringArray(process_subsetting);
                    if(subProcessList!=null && subProcessList.size()>0){
                        List<SubProcessSetting> needCreateList = new ArrayList<SubProcessSetting>(subProcessList.size());
                        Date date = new Date();
                        for(SubProcessSetting setting : subProcessList){
                            if(process.getActivityById(setting.getNodeId())!=null){
                                setting.setTempleteId(newId);
                                setting.setCreateTime(date);
                                setting.setId(UUIDLong.longUUID());
                                needCreateList.add(setting);
                            }
                        }
                        subProcessManager.saveSubProcessSetting(needCreateList);
                    }
                }
            }
            saveWorkflowProcessProperty(newId, processEventJson);
        }
        return newId;
    }
    

    @Override
    public void updateProcessState(List<String> processIdList, int state) throws BPMException {
        processTemplateDao.updateProcessState(processIdList, state);
    }

    @Override
    public void deleteWorkflowTemplate(long batchId, long id) throws BPMException {
        ProcessTemplete oldPT= processTemplateDao.selectProcessTempate(String.valueOf(id));
        if(null!=oldPT){
            if(oldPT.getState()==ProcessTemplete.STATE_DRAFT){//草稿状态
                //直接删除
                processTemplateDao.deleteWorkflowTemplate(oldPT);
                subProcessManager.deleteSubProcessSettingByMainTempleteId(oldPT.getId());
            }else if(oldPT.getState()==ProcessTemplete.STATE_PUBLISH){//已发布状态
                //更新为待删除状态
                oldPT.setState(ProcessTemplete.STATE_TO_DELETE);
                oldPT.setBatchId(batchId);
                oldPT.setModifyDate(new Timestamp(System.currentTimeMillis()));
                processTemplateDao.updateProcessTemplate(oldPT);
                //删除草稿状态的记录
                processTemplateDao.deleteProcessTemplateByOldId(id);
            }
            //else if(oldPT.getState()==ProcessTemplete.STATE_TO_DELETE){//待删除状态
                //do nothing
            //}
            this.ProcessTemplateCache.remove(oldPT.getId());
        }
    }
    
    
    /**
     * 把新的模板信息同步到老的， 模板修改的时候是生成一条新的模板
     * @Author      : xuqw
     * @Date        : 2016年2月21日下午1:07:22
     * @param processTemplete
     * @param batchId
     * @throws BPMException
     */
    private void syncProcessTemplate(ProcessTemplete processTemplete, long batchId) throws BPMException{
      //草稿状态
        Long oldId= processTemplete.getOldTemplateId();
        if(null!=oldId && oldId!=0 && oldId!=-1){
            ProcessTemplete oldPt= processTemplateDao.selectProcessTempate(String.valueOf(oldId));
            if(null!=oldPt){
                //更新processTemplete的值到oldPt中去
                oldPt.setBatchId(batchId);
                oldPt.setProcessName(processTemplete.getProcessName());
                oldPt.setWorkflow(processTemplete.getWorkflow());
                oldPt.setWorkflowRule(processTemplete.getWorkflowRule());
                this.updateCache(oldPt);
                processTemplateDao.updateProcessTemplate(oldPt);
                //删除该草稿状态的记录
                processTemplateDao.deleteWorkflowTemplate(processTemplete);
                this.ProcessTemplateCache.remove(processTemplete.getId());
                //删除掉模版以前绑定的子流程，更新新绑定的子流程的主流程模版号。
                subProcessManager.deleteSubProcessSettingByMainTempleteId(processTemplete.getOldTemplateId());
                List<SubProcessSetting> subProcessSettingList2 = subProcessManager.getAllSubProcessSettingByTemplateId(String.valueOf(processTemplete.getId()), null);
                if(subProcessSettingList2!=null && subProcessSettingList2.size()>0){
                    for(SubProcessSetting setting : subProcessSettingList2){
                        setting.setTempleteId(processTemplete.getOldTemplateId());
                    }
                    subProcessManager.updateSubProcessSetting(subProcessSettingList2);
                }
                //如果当前模版作为别的流程的子流程，那么更新子流程绑定数据
                List<SubProcessSetting> subSettingList = subProcessManager.getAllSubProcessSettingByNewflowTempleteId(String.valueOf(oldPt.getId()));
                if(subSettingList!=null && subSettingList.size()>0){
                    for(SubProcessSetting set : subSettingList){
                        set.setSubject(oldPt.getProcessName());
                    }
                    subProcessManager.updateSubProcessSetting(subSettingList);
                }
               /* WFProcessProperty oldProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(oldPt.getId());
                WFProcessProperty newProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplete.getId());
                if(null != newProcessProperty){
                    if(null != oldProcessProperty){
                        WFPropertyBean propertyBean = new WFPropertyBean();
                        propertyBean.fromJson(newProcessProperty.getValue());
                        //主要为更新processIdOrTemplateId
                        propertyBean.setProcessIdOrTemplateId(oldPt.getId());
                        oldProcessProperty.setValue(propertyBean.toJson());
                        wfProcessPropertyManager.updateProcessProperty(oldProcessProperty);
                        wfProcessPropertyManager.deleteProcessProperty(newProcessProperty);
                    } else {
                        newProcessProperty.setProcessId(oldId);
                        wfProcessPropertyManager.updateProcessProperty(newProcessProperty);
                    }
                } else {
                    if(null != oldProcessProperty){
                        wfProcessPropertyManager.deleteProcessProperty(oldProcessProperty);
                    }
                }*/
            }else{
                //更新已发布状态
                processTemplete.setBatchId(batchId);
                processTemplete.setState(ProcessTemplete.STATE_PUBLISH);
                processTemplete.setModifyDate(new Timestamp(System.currentTimeMillis()));
                this.updateCache(processTemplete); 
                processTemplateDao.updateProcessTemplate(processTemplete);
            }
        }else{
            //更新已发布状态
            processTemplete.setBatchId(batchId);
            processTemplete.setState(ProcessTemplete.STATE_PUBLISH);
            processTemplete.setModifyDate(new Timestamp(System.currentTimeMillis()));
            this.updateCache(processTemplete); 
            processTemplateDao.updateProcessTemplate(processTemplete);
        }
    }
    
    @Override
    public ProcessTemplete mergeWorkflowTemplate(long templateId) throws BPMException{
        
        ProcessTemplete newPt= processTemplateDao.selectProcessTempate(String.valueOf(templateId));
        if(newPt != null){
            syncProcessTemplate(newPt, -1);
        }
        return newPt;
    } 
    

    @Override
    public List<ProcessTemplete> saveWorkflowTemplates(long batchId) throws BPMException {
        List<ProcessTemplete> pts= processTemplateDao.getProcessTemplateListByBatchId(batchId);
        for (ProcessTemplete processTemplete : pts) {
            int mystate= processTemplete.getState();
            if(mystate== ProcessTemplete.STATE_DRAFT){
                syncProcessTemplate(processTemplete, batchId);
            }else if(mystate== ProcessTemplete.STATE_TO_DELETE){//待删除状态
                
                //更新为删除状态
                processTemplete.setState(ProcessTemplete.STATE_DELETE);
                processTemplateDao.updateProcessTemplate(processTemplete);
                
                this.ProcessTemplateCache.remove(processTemplete.getId());
                
                //删除绑定的子流程信息。
                subProcessManager.deleteSubProcessSettingByMainTempleteId(processTemplete.getId());
                WFProcessProperty deleteProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplete.getId());
                if(null != deleteProcessProperty){
                    wfProcessPropertyManager.deleteProcessProperty(deleteProcessProperty);
                }
            }
        }
        return pts;
    }

    @Override
    public List<ProcessTemplete> deleteWorkflowTemplates(long batchId) throws BPMException {
        List<ProcessTemplete> pts= processTemplateDao.getProcessTemplateListByBatchId(batchId);
        for (ProcessTemplete processTemplete : pts) {
            int mystate= processTemplete.getState();
            if(mystate==ProcessTemplete.STATE_DRAFT){//草稿状态
                processTemplateDao.deleteWorkflowTemplate(processTemplete);
                //删除绑定的子流程信息。
                subProcessManager.deleteSubProcessSettingByMainTempleteId(processTemplete.getId());
            }
            //else if(mystate==ProcessTemplete.STATE_PUBLISH){//已发布状态
                //do nothing
            //}
            else if(mystate==ProcessTemplete.STATE_TO_DELETE){//待删除状态
                processTemplete.setBatchId(batchId);
                processTemplete.setState(ProcessTemplete.STATE_PUBLISH);
                processTemplete.setModifyDate(new Timestamp(System.currentTimeMillis()));
                processTemplateDao.updateProcessTemplate(processTemplete);
            }
        }
        return pts;
    }

    @Override
    public void deleteWorkflowTemplate(long processTemplateId) throws BPMException {
        processTemplateDao.deleteWorkflowTemplate(processTemplateId);
      //删除流程事件数据
        WFProcessProperty deleteProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplateId);
        if(null != deleteProcessProperty){
            wfProcessPropertyManager.deleteProcessProperty(deleteProcessProperty);
        }
    }

    @Override
    public void deleteJunkDataOfWorkflowTemplates(long formId) throws BPMException {
        List<ProcessTemplete> pts= processTemplateDao.getProcessTemplateListByAppId(formId);
        for (ProcessTemplete processTemplete : pts) {
            int mystate= processTemplete.getState();
            if(mystate== ProcessTemplete.STATE_DRAFT){//草稿状态
                processTemplateDao.deleteWorkflowTemplate(processTemplete);
                this.ProcessTemplateCache.remove(processTemplete.getId());
                //删除绑定的子流程信息。
                subProcessManager.deleteSubProcessSettingByMainTempleteId(processTemplete.getId());
                //删除流程事件数据
                WFProcessProperty deleteProcessProperty = wfProcessPropertyManager.getTemplateProcessPropertyByProcessId(processTemplete.getId());
                if(null != deleteProcessProperty){
                    wfProcessPropertyManager.deleteProcessProperty(deleteProcessProperty);
                }
            }else if(mystate== ProcessTemplete.STATE_TO_DELETE){//待删除状态
                processTemplete.setModifyDate(new Timestamp(System.currentTimeMillis()));
                processTemplete.setState(ProcessTemplete.STATE_PUBLISH);
                this.updateCache(processTemplete);
                processTemplateDao.updateProcessTemplate(processTemplete);
            }
        }
    }

    @Override
    public void updateProcessTemplate(ProcessTemplete pt) throws BPMException {
        processTemplateDao.updateProcessTemplate(pt);
        if(pt.getState() == ProcessTemplete.STATE_PUBLISH){
        	this.updateCache(pt);
        }
    }

	@Override
	public List<CtpTemplateVO> getCtpTemplateByOrgIdsAndCategory(Long currentUserId,boolean isLeave,Long memberId,
			Long accountId, List<String> ids, boolean blur,
			ApplicationCategoryEnum... types) throws BusinessException {
		Long preInvokeTime= NodeReplaceControlUtil.getInvokeControlMap(currentUserId);
    	if(null!=preInvokeTime){
    	    
    	    //"系统中流程模板比较多，上次查询还没完成，请在系统空闲时再进行查找!"
    		throw new BPMException(ResourceUtil.getString("workflow.label.dialog.lastTemplateFindNotEnd")); 
    	}
    	NodeReplaceControlUtil.putInvokeControlMap(currentUserId, System.currentTimeMillis());
		List list= processTemplateDao.getCtpTemplateByOrgIdsAndCategory(currentUserId,isLeave,memberId, accountId, ids, blur, types);
		NodeReplaceControlUtil.removeInvokeControlMap(currentUserId);
		return list;
	}
	
	@Override
    public List<CtpTemplateAuth> getCtpTemplateAuths(Long moduleId, Integer moduleType) throws BusinessException {
       return processTemplateDao.getCtpTemplateAuths(moduleId, moduleType,null);
    }
	
	public List<CtpTemplateVO> getCtpTemplateByOrgIdsAndCategory(Long currentUserId, Long accountId, String orgId, ApplicationCategoryEnum... types) throws BusinessException{
	    List<Long> matchWorkflowIds = new ArrayList<Long>();
	    
	    Collection<ProcessTemplateCacheObject> processTemplateCacheObjects = this.ProcessTemplateCache.values();
        for (ProcessTemplateCacheObject p : processTemplateCacheObjects) {
            if(p.isContainsOrgId(orgId)){
                matchWorkflowIds.add(p.getId());
            }
        }
        
        if(!matchWorkflowIds.isEmpty()){
            return this.processTemplateDao.getCtpTemplateByOrgIdsAndCategory(accountId, matchWorkflowIds, types);
        }
        
        return Collections.emptyList();
	}
	
    public Map<String, String> getTemplateProcessNodeIdAndName(Long processTemplateId) {
        Map<String, String> id2name = new HashMap<String, String>();
        ProcessTemplateCacheObject pt = ProcessTemplateCache.get(processTemplateId);
        if (pt != null) {
            Map<String, ProcessTemplateCacheActivity> nodes = pt.getActivityMap();
            for (String id : nodes.keySet()) {
                ProcessTemplateCacheActivity n = nodes.get(id);
                id2name.put(id, n.getCustomName());
            }
        }
        return id2name;
    }
	
	public List<ProcessTemplateNodeSimpleBO> getProcessTemplateNodeSimpleBOsByOrgIds(List<String> orgIds){
	    
	    List<ProcessTemplateNodeSimpleBO> retList = new ArrayList<ProcessTemplateNodeSimpleBO>();
	    
        Collection<ProcessTemplateCacheObject> processTemplateCacheObjects = this.ProcessTemplateCache.values();
        
        for (ProcessTemplateCacheObject p : processTemplateCacheObjects) {
            Map<String,ProcessTemplateCacheActivity> nodeMap =  p.getActivityMap();
            Set<String> nodeIds =  nodeMap.keySet();
            
            if(Strings.isNotEmpty(nodeIds)){
               for(String nodeId : nodeIds){
                   ProcessTemplateCacheActivity cacheActivity = nodeMap.get(nodeId);
                   if(cacheActivity == null) {
                       continue;
                   }
                   String partyId = cacheActivity.getPartyId();
                   if(orgIds.contains(partyId)){
                       ProcessTemplateNodeSimpleBO bo = new ProcessTemplateNodeSimpleBO();
                       bo.setId(p.getId());
                       bo.setNodeId(nodeId);
                       bo.setNodeDeadLine(cacheActivity.getDeadline());
                       bo.setPermissionName(BPMSeeyonPolicy.getShowName(cacheActivity.getPermissionId()));
                       bo.setPermissionId(cacheActivity.getPermissionId());
                       try {
                        
                           if(Strings.isNotEmpty(cacheActivity.getCustomName())){
                               bo.setNodeName(cacheActivity.getCustomName());
                           }
                           if(Strings.isBlank(bo.getNodeName())){
                               String newNodeName = getNodeName(cacheActivity.getPartyId(),cacheActivity.getPartyType(),String.valueOf(AppContext.currentAccountId()));
                               if(Strings.isNotBlank(newNodeName)){
                                   bo.setNodeName(newNodeName);
                               }
                           }
                        } catch (Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        } 
                 
                       bo.setPartyId(cacheActivity.getPartyId());
                       bo.setProcessName(p.getProcessName());  
                       retList.add(bo);
                   }
               }
            }
        }
        return retList;
	}

	
	private String getNodeName(String partyId,String partyType,String accountId) throws NumberFormatException, BusinessException{
	    String name = "";
	    if(ProcessOrgManager.ORGENT_TYPE_ROLE.equals(partyId) || "Node".equals(partyId)){ //角色
            Long roleAccountId= AppContext.getCurrentUser().getAccountId();
            if(WorkflowUtil.isLong(accountId)){
                V3xOrgAccount roleAccount= orgManager.getAccountById(Long.valueOf(accountId));
                if(null!=roleAccount){
                    roleAccountId= roleAccount.getId();
                }
            }
            name = processOrgManager.getRoleShowNameByName(partyId,roleAccountId);
        }
        else {
            ProcessOrgManager pOrgManager = getProcessOrgManager();
            if (pOrgManager != null) {
                String[] result = pOrgManager.getNameByEntity(partyType, partyId, "");
                if (null != result) {
                    name = result[0];
                }
            }
        }
	    return name;
	}

    public List<Long> getProcessTemplateIdBySuperNodeId(String superNodeId) {

        if (Strings.isEmpty(superNodeId)) {
            return null;
        }

        Collection<ProcessTemplateCacheObject> processTemplateCacheObjects = this.ProcessTemplateCache.values();
        List<Long> rets = new ArrayList<Long>();
        for (ProcessTemplateCacheObject p : processTemplateCacheObjects) {

            Map<String, ProcessTemplateCacheActivity> nodeMap = p.getActivityMap();
            Set<String> nodeIds = nodeMap.keySet();

            if (Strings.isNotEmpty(nodeIds)) {
                for (String nodeId : nodeIds) {
                    ProcessTemplateCacheActivity cacheActivity = nodeMap.get(nodeId);
                    if (Strings.isNotEmpty(cacheActivity.getPartyId()) && cacheActivity.getPartyId().equals(superNodeId)) {
                        rets.add(p.getId());
                        break;
                    }
                }
            }
        }
        return rets;
    }
	
    @Override
    public List<ProcessTemplateNodeSimpleBO> getProcessTemplateNodeSimpleBOsByIds(Long userId,Long accountId,List<Long> processTemplateIds) {

        List<ProcessTemplateNodeSimpleBO> retList = new ArrayList<ProcessTemplateNodeSimpleBO>();
        
        Collection<ProcessTemplateCacheObject> processTemplateCacheObjects = this.ProcessTemplateCache.values();
        
        for (ProcessTemplateCacheObject p : processTemplateCacheObjects) {
            
            if(!processTemplateIds.contains(p.getId())){
                continue;
            }
            
            Map<String,ProcessTemplateCacheActivity> nodeMap =  p.getActivityMap();
            Set<String> nodeIds =  nodeMap.keySet();
            
            if(Strings.isNotEmpty(nodeIds)){
               for(String nodeId : nodeIds){
                   ProcessTemplateCacheActivity cacheActivity = nodeMap.get(nodeId);
                   
                   if(cacheActivity != null && "Node".equals(cacheActivity.getPartyType())){
                       
                        boolean isRole = isRole(userId, cacheActivity.getPartyId());
                        if(isRole){
                            ProcessTemplateNodeSimpleBO bo = new ProcessTemplateNodeSimpleBO();
                            bo.setId(p.getId());
                            bo.setNodeId(nodeId);
                            bo.setNodeDeadLine(cacheActivity.getDeadline());
                            bo.setPermissionName(BPMSeeyonPolicy.getShowName(cacheActivity.getPermissionId()));
                            bo.setPermissionId(cacheActivity.getPermissionId());
                            try {
                                bo.setNodeName(cacheActivity.getCustomName());
                                if(Strings.isBlank(bo.getNodeName())){
                                    String newNodeName = getNodeName(cacheActivity.getPartyId(),cacheActivity.getPartyType(),String.valueOf(AppContext.currentAccountId()));
                                    if(Strings.isNotBlank(newNodeName)){
                                        bo.setNodeName(newNodeName);
                                    }
                                }
                             } catch (Exception e) {
                                 logger.error(e.getLocalizedMessage(), e);
                             } 
                      
                            bo.setPartyId(cacheActivity.getPartyId());
                            bo.setProcessName(p.getProcessName());
                            retList.add(bo);
                        }
                   }
               }
            }
            
            
        }
        
        //每次清除缓存，避免角色更新了，这个地方不刷新
        roleCacheSet.clear();
        return retList;
    }
    
    
    private boolean isRole(long memberId, String partyId) {

        String roleId = partyId.replaceAll("NodeUserSuperAccount", "").replaceAll("SenderSuperAccount", "").replaceAll("NodeUserLeaderDep", "").replaceAll("NodeUserManageDep", "")
                .replaceAll("NodeUserSuperDept", "").replaceAll("SenderLeaderDep", "").replaceAll("SenderManageDep", "").replaceAll("SenderSuperDept", "").replaceAll("NodeUser", "")
                .replaceAll("Sender", "");
        boolean isRole = false;
        try {
            if (Strings.isNotEmpty(roleId)) {
                if (roleCacheSet.isEmpty() ) {
                    List<MemberRole> roles = orgManager.getMemberRoles(memberId, V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
                    if (Strings.isNotEmpty(roles)) {
                        for (MemberRole memberRole : roles) {
                            V3xOrgRole role = memberRole.getRole();
                            if (role != null) {
                                roleCacheSet.add(role.getName());
                                roleCacheSet.add(String.valueOf(role.getId()));
                            }
                        }
                    }
                    logger.info("roleCacheSet:" + roleCacheSet.toString());
                }
                
                if (roleCacheSet.contains(roleId)) {
                    isRole = true;
                }
            }
        } catch (BusinessException e) {
            logger.error("" + e.getLocalizedMessage(), e);
        }
        return isRole;
    }
	@Override
	public void deleteWorkflowTemplateByIds(List<Long> ids) throws BPMException {
		if(Strings.isEmpty(ids)){
			return;
		}
		processTemplateDao.deleteProcessTemplateByIds(ids);
	}
}
