/**
* $Author: tanmf $
* $Rev: 49738 $
* $Date:: 2015-06-02 16:15:00#$:
*
* Copyright (C) 2012 Seeyon, Inc. All rights reserved.
*
* This software is the proprietary information of Seeyon, Inc.
* Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.json.JSONObject;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.lock.manager.LockState;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.dao.IProcessRunningDao;
import com.seeyon.ctp.workflow.engine.enums.ProcessStateEnum;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.ProcessDefManager;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.ProcessTemplateManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.manager.WorkflowFormDataMapInvokeManager;
import com.seeyon.ctp.workflow.po.HisProcessInRunningDAO;
import com.seeyon.ctp.workflow.po.ProcessInRunningBLOBDAO;
import com.seeyon.ctp.workflow.po.ProcessInRunningDAO;
import com.seeyon.ctp.workflow.po.SubProcessRunning;
import com.seeyon.ctp.workflow.po.SubProcessSetting;
import com.seeyon.ctp.workflow.util.BPMChangeUtil;
import com.seeyon.ctp.workflow.util.ReadyObjectUtil;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.User;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMParticipantType;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTimeActivity;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.definition.ReadyObject;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.ReadyNode;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkItemManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
 */
public class ProcessManagerImpl implements ProcessManager {

    private static Log log = CtpLogFactory.getLog(ProcessManagerImpl.class);

    public ProcessManagerImpl() {
    }

    private IProcessRunningDao processRunningDao;

    private CaseManager        caseManager;

    private WorkItemManager    workItemManager = null;
    
    private EnumManager enumManagerNew;

    private SubProcessManager  subProcessManager;

    private LockManager        lockManager     = null;
    
    /**
     * @param lockManager the lockManager to set
     */
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    private ProcessOrgManager processOrgManager = null;

    /**
     * @param processOrgManager the processOrgManager to set
     */
    public void setProcessOrgManager(ProcessOrgManager processOrgManager) {
        this.processOrgManager = processOrgManager;
    }

    public void setSubProcessManager(SubProcessManager subProcessManager) {
        this.subProcessManager = subProcessManager;
    }

    /**
     * @return the workItemManager
     */
    public WorkItemManager getWorkItemManager() {
        return workItemManager;
    }

    /**
     * @param workItemManager the workItemManager to set
     */
    public void setWorkItemManager(WorkItemManager workItemManager) {
        this.workItemManager = workItemManager;
    }

    /**
     * @param caseManager the caseManager to set
     */
    public void setCaseManager(CaseManager caseManager) {
        this.caseManager = caseManager;
    }

    private ProcessTemplateManager processTemplateManager = null;

    /**
     * @param processTemplateManager the processTemplateManager to set
     */
    public void setProcessTemplateManager(ProcessTemplateManager processTemplateManager) {
        this.processTemplateManager = processTemplateManager;
    }

    /**
     * @param processRunningDao the processRunningDao to set
     */
    public void setProcessRunningDao(IProcessRunningDao processRunningDao) {
        this.processRunningDao = processRunningDao;
    }

    /**
     * 根据processId从wf_process_running表中查询process信息
     * @param processId 流程模版ID
     * @return BPMProcess
     * @throws BPMException
     */
    public BPMProcess getRunningProcess(String processId) throws BPMException {
        return processRunningDao.getProcess(processId);
    }
    public BPMProcess getRunningProcessHis(String processId) throws BPMException {
    	IProcessRunningDao processRunningDao = (IProcessRunningDao)AppContext.getBean("IProcessRunningDaoFK");
    	if(processRunningDao != null){
    		return processRunningDao.getProcess(processId);
    	}
    	return null;
    }
    public ProcessInRunningDAO getProcessInRunningDAO(String processId) throws BPMException{
    	return getProcessInRunningDAO(processId, false);
    }

    public ProcessInRunningDAO getProcessInRunningDAO(String processId, boolean isHistoryFlag) throws BPMException {
    	  if(isHistoryFlag){
    	      IProcessRunningDao processRunningDaoFK = (IProcessRunningDao)AppContext.getBean("IProcessRunningDaoFK");
    	      if(processRunningDaoFK!=null){
    	          return processRunningDaoFK.getProcessInRunningDAO(processId);
    	      }
    	  }
        return processRunningDao.getProcessInRunningDAO(processId);
    }

    /**
     * 根据processId从wf_his_process_running表中查询process信息
     * @param processId 流程模版ID
     * @return BPMProcess
     * @throws BPMException
     */
    public BPMProcess getHisRunningProcess(String processId) throws BPMException {
        return processRunningDao.getHisProcess(processId);
    }

    public HisProcessInRunningDAO getHisProcessInRunningDAO(String processId) throws BPMException{
    	return getHisProcessInRunningDAO(processId, false);
    }

    public HisProcessInRunningDAO getHisProcessInRunningDAO(String processId, boolean isHistoryFlag) throws BPMException {
        if(isHistoryFlag){
            IProcessRunningDao processRunningDaoFK = (IProcessRunningDao)AppContext.getBean("IProcessRunningDaoFK");
              if(processRunningDaoFK!=null){
                  return processRunningDaoFK.getHisProcessInRunningDAO(processId);
              }
    	  }
        return processRunningDao.getHisProcessInRunningDAO(processId);
    }

    /**
     * 新增
     */
    public String saveRunningProcess(BPMProcess process,BPMCase theCase) throws BPMException {
        String processId = process.getId();
        process.setIndex(processId);
        process.setCreateDate(new Date());
        process.setUpdateDate(new Date());
        processRunningDao.addProcess(process,theCase);
        return processId;
    }

    /**
     * 新增
     */
    public String saveRunningProcessXml(String processXml) throws BPMException {
        if (Strings.isBlank(processXml)) {
            throw new BPMException("ProcessXml is NULL!");
        }
        return processRunningDao.addProcessXml(processXml);
    }
    
    /**
     * 新增
     */
    public String saveRunningProcessXml(String id,String processXml) throws BPMException {
    	if (Strings.isBlank(processXml)) {
            throw new BPMException("ProcessXml is NULL!");
        }
        return processRunningDao.addProcessXml(id,processXml);
    }

    /**
     * 更新
     */
    public String updateRunningProcess(BPMProcess process,BPMCase theCase) throws BPMException {
        String processId = process.getId();
        process.setUpdateDate(new Date());
        process.setCreateDate(process.getCreateDate());
        processRunningDao.updateProcess(process,theCase);
        return processId;
    }

    @Override
    public void updateRunningProcessXml(String processId, String processXml) throws BPMException {
    	if (Strings.isBlank(processXml)) {
            throw new BPMException("ProcessXml is NULL!");
        }
        processRunningDao.updateProcessXml(processId, processXml);
    }

    public void finishProcessState(String processId) throws BPMException {
        processRunningDao.updateProcessState(processId, ProcessStateEnum.processState.finished.ordinal());
        subProcessManager.updateSubprocessRunningFinished(processId,1);
    }
    
    public int getProcessState(String processId) throws BPMException {
    	return getProcessState(processId, false);
    }

    public int getProcessState(String processId, boolean isHistoryFlag) throws BPMException {
        if(isHistoryFlag){
            IProcessRunningDao processRunningDaoFK = (IProcessRunningDao)AppContext.getBean("IProcessRunningDaoFK");
            if(processRunningDaoFK!=null){
                return processRunningDaoFK.getProcessRunningState(processId);
            }
      	}
  		  return processRunningDao.getProcessRunningState(processId);
    }

    public void addHisProcess(String processId) throws BPMException {
        processRunningDao.addHisProcess(processId);
    }

    public void deleteRunningProcess(String processId) throws BPMException {
        processRunningDao.deleteProcess(processId);
    }

    @Override
    public String  getWorkflowNodesInfo(String processId, CtpEnumBean nodePermissionPolicy) throws BPMException {
    	return getWorkflowNodesInfoAndDR( processId, nodePermissionPolicy)[0];
    }
    
    public String[] getWorkflowNodesInfoAndDR(String processId, CtpEnumBean nodePermissionPolicy) throws BPMException {
        try {
            ProcessInRunningDAO processPojo = getProcessInRunningDAO(processId);
            BPMProcess process = null;
            if (null != processPojo) {
                process = processPojo.getProcess();
            }
            if (process == null) {
                String processXml = processTemplateManager.selectProcessTempateXml(processId);
                process = BPMProcess.fromXML(processXml);
            }
            if (process == null) {
                return null;
            }
            String processXml= process.toXML(null, true);
            process= BPMProcess.fromXML(processXml);
            
            return this.getWorkflowNodesInfoAndDR(process, nodePermissionPolicy);
        }
        catch (Throwable e) {
            throw new BPMException(e);
        }
    }
    
    public String[] getWorkflowNodesInfoAndDR(BPMProcess process, CtpEnumBean nodePermissionPolicy) throws BPMException {
        try {
        	String[] infos = new String[2];
        	
            if (process == null) {
                return null;
            }

            WorkflowUtil.changeFormFieldNodeName(null,process,null);
            String isShowShortName = process.getIsShowShortName();
            Boolean showName = false;
            if ("true".equals(isShowShortName)) {
                showName = true;
            }
            BPMStatus start = process.getStart();
            Map<String, String> historyMap = new HashMap<String, String>();
            StringBuilder sb = new StringBuilder();
            String sp = ResourceUtil.getString("common.separator.label");
            Map<String,Integer> currentCountMap= new HashMap<String, Integer>();
            currentCountMap.put("currentCount", 0);
            getNodeInfo(nodePermissionPolicy, start, showName, 10, currentCountMap, historyMap, sb, sp);
            infos[0] = sb.toString();
            infos[1] = start.getSeeyonPolicy().getDR();
            return infos;
        } catch (Throwable e) {
        	log.error("", e);
            throw new BPMException(e);
        }
    }

    /**
     * 获取一个流程节点的节点信息
     * @param nodePermissionPolicy
     * @param o
     * @param showName
     * @param level
     * @param count
     * @param historyMap
     * @param sb
     * @param sp
     * @throws BPMException 
     */
    private int getNodeInfo(CtpEnumBean nodePermissionPolicy, BPMAbstractNode sourceNode, Boolean showName, int level, Map<String,Integer> currentCountMap,
            Map<String, String> historyMap, StringBuilder sb, String sp) throws BPMException {
        List<BPMTransition> ts = sourceNode.getDownTransitions();
        if (ts != null) {
            for (int i=ts.size()-1;i>=0;i--) {
            	BPMTransition t= ts.get(i);
            	BPMAbstractNode childNode= t.getTo();
            	int count= currentCountMap.get("currentCount");
                if (count >= level) {
                    return count;
                }
                if (null != historyMap.get(childNode.getId())) {
                    return count;
                }
            	String nodeType = childNode.getNodeType().name().trim().toLowerCase();
                if (!java.util.regex.Pattern.matches("start|split|join|end", nodeType)) {
                    List<BPMActor> actors = childNode.getActorList();
                    String seeyonPolicy = childNode.getSeeyonPolicy().getId();
                    for (BPMActor actor : actors) {
                        String type = actor.getParty().getType().id;
                        String id = actor.getParty().getId();
                        
                        String[] nodeInfo = actor.getNodeInfo();
                        String name = nodeInfo[0];
                        String shortname = nodeInfo[1];
                        
                        if (("Role".equals(type) || WorkFlowMatchUserManager.ORGENT_META_KEY_NODE.equals(type) ) && "BlankNode".equals(id)) {
                            seeyonPolicy = null;
                        }
                        String policy = seeyonPolicy;
                        if (count > 0) {
                            sb.append(sp);
                        }
                        
                        if (Strings.isNotBlank(name)) {
                            
                            String customName = childNode.getCustomName();
                            if(Strings.isNotEmpty(customName)){
                                sb.append(customName);
                            }else{
                                sb.append(name);
                            }
                        } else {//所选组织实体被删除
                            sb.append(ResourceUtil.getString("workflow.node.notExist"));
                        }
                        
                        String key = null;
                        if (null != nodePermissionPolicy) {
                            key = nodePermissionPolicy.getItemLabel(policy);
                        }
                        String label = "";
                        if (key == null) {
                            if("inform".equals(policy)){
                                policy= "zhihui";
                            }else if("zhihui".equals(policy)){
                                policy= "inform";
                            }
                            if (null != nodePermissionPolicy) {
                            	key = nodePermissionPolicy.getItemLabel(policy);
                            }
                            if(key== null){
                                label = policy;
                            }else{
                                label = ResourceUtil.getString(key);
                            }
                        } else {
                            label = ResourceUtil.getString(key);
                        }
                        
                        if (!"Account".equals(type) && Strings.isNotBlank(shortname) && !"null".equalsIgnoreCase(shortname)
                                && !"undefined".equalsIgnoreCase(shortname)) {
                            sb.append("(").append(shortname).append(")");
                        }
                        
                        if (Strings.isNotBlank(label)) {
                            sb.append("(").append(label).append(")");
                        }
                        count++;
                    }
                    historyMap.put(childNode.getId(), childNode.getId());
                }
                currentCountMap.put("currentCount", count);
            }
            
            for (int i=ts.size()-1;i>=0;i--) {
            	BPMTransition t= ts.get(i);
            	
            	BPMAbstractNode childNode= t.getTo();
            	getNodeInfo(nodePermissionPolicy, childNode, showName, level, currentCountMap, historyMap, sb, sp);
            }
        }
        return 0;
    }

    @Override
    public List<String> getWorkflowUsedPolicyIds(BPMProcess bpmProcess) throws BPMException {
        List<String> returnList = new ArrayList<String>();
        if (null != bpmProcess.getActivitiesList()) {
            List<BPMAbstractNode> nodes = (List<BPMAbstractNode>) bpmProcess.getActivitiesList();
            for (BPMAbstractNode bpmAbstractNode : nodes) {
                BPMSeeyonPolicy policy = bpmAbstractNode.getSeeyonPolicy();
                if (null != policy) {
                    if (!returnList.contains(policy.getId())) {
                        returnList.add(policy.getId());
                    }
                }
            }
        }
        return returnList;
    }

    @Override
    public String[] superviseUpdateProcess(String processId, String activityId, int operationType,
            Map<String, Object> flowData, BPMSeeyonPolicy policy, String[] selecteNodeIdArr, String[] _peopleArr,
            String caseId, String processXml, String orginalReadyObjectJson, String userId, String userName,
            boolean isForm, String oldProcessLogJson,String isProIncludeChild,String nodeName) throws BPMException {
        BPMProcess process = null;
        ReadyObject orginalReadyObject = null;
        if (null != processXml && !"".equals(processXml.trim()) && !"null".equals(processXml.trim())
                && !"undefined".equals(processXml.trim())) {
            try {
                process = BPMProcess.fromXML(processXml);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        if (null == process) {
            process = getRunningProcess(processId);
        }
        if (null == process) {
            throw new BPMException("query process error, [processId = " + processId + "],BPMProcess is null!");
        }
        if (null != orginalReadyObjectJson && !"".equals(orginalReadyObjectJson.trim())) {
            orginalReadyObject = ReadyObjectUtil.parseToReadyObject(orginalReadyObjectJson.trim(), process);
        }
        BPMCase theCase= caseManager.getCase(Long.parseLong(caseId));
        if(null==theCase){
            theCase= caseManager.getHisCase(Long.parseLong(caseId));
        }
        BPMActivity acitvity = process.getActivityById(activityId);
        boolean isNodePolicyChange = false;
        boolean isOperationNameChange = false;
        boolean isProcessModeChange = false;
        boolean isDeadTermChange = false;
        boolean isRemindTimeCahange = false;
        boolean isCycleRemindTimeCahange = false;
        boolean isDealTermActionChange = false;
        String oldName = acitvity.getName();
        
        String oldNodePolicy = acitvity.getSeeyonPolicy().getName();
        String oldOperationName = acitvity.getSeeyonPolicy().getFormViewOperation();
        
        String oldProcessMode = acitvity.getSeeyonPolicy().getProcessMode();
        String oldDeadTerm = acitvity.getSeeyonPolicy().getdealTerm();
        String oldRemindTime = acitvity.getSeeyonPolicy().getRemindTime();
        String oldCycleRemindTime = acitvity.getSeeyonPolicy().getCycleRemindTime();
        String oldDealTermAction = acitvity.getSeeyonPolicy().getDealTermType();
        String oldDealTermUserName = acitvity.getSeeyonPolicy().getDealTermUserName();
        String oldDealTermUserId = acitvity.getSeeyonPolicy().getDealTermUserId();
        //督办时前台传过来的operationType在会签多个时不准确，后台重新计算一下，如果当前节点在readyAddedMap中存在，则新会签的节点也要加入
        int type = 0;
        if (null != flowData.get("type")) {
            type = Integer.parseInt(flowData.get("type").toString());
        }
        String isShowShortName = "";
        if (null != flowData.get("isShowShortName")) {
            isShowShortName = flowData.get("isShowShortName").toString();
        }
        List<Map<String, Object>> peoples = new ArrayList<Map<String, Object>>();
        if (null != flowData.get("people")) {
            peoples = (List<Map<String, Object>>) flowData.get("people");
        }
        if ((operationType == 1 && flowData.get("type") != null && type == FLOWTYPE_COLASSIGN) || operationType == 5) {
            try {
                if (orginalReadyObject != null) {
                    List<BPMActivity> readyActivityList = orginalReadyObject.getActivityList();
                    if (readyActivityList != null) {
                        boolean containCurrentNode = false;
                        for (BPMActivity activity : readyActivityList) {
                            if (activityId.equals(activity.getId())) {
                                containCurrentNode = true;
                                break;
                            }
                        }
                        if (containCurrentNode) {
                            if (operationType == 1) {
                                operationType = 2;
                            } else if (operationType == 5) {
                                operationType = 6;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        String[] result = new String[5];
        String[] myresult = new String[2];
        if (operationType == 1) {//待办之后添加新节点
            myresult = addNewActivity(process, activityId, peoples, type, userId, Long.valueOf(caseId), false,
                    isShowShortName, orginalReadyObject,theCase);
        } else if (operationType == 2) {//已办之后,未激活节点之前
            myresult = addNewActivity(process, activityId, peoples, type, userId, Long.valueOf(caseId), true,
                    isShowShortName, orginalReadyObject,theCase);
        } else if (operationType == 3) {//删除未激活节点
            myresult = delNoActivationNode(process, activityId, peoples, userId, orginalReadyObject,theCase);
        } else if (operationType == 4) {//删除之前,对下节点进行人员匹配
           // Map<String, String[]> manualMap = new HashMap<String, String[]>();
            int i = 0;
            for (String nodeId : selecteNodeIdArr) {
                if (nodeId != null && !"".equals(nodeId)) {
                    String[] people = _peopleArr[i].split(",");
                  //  manualMap.put(nodeId, people);
                    i++;
                }
            }
//            if (manualMap != null && !manualMap.isEmpty()) {
//                setActivityManualSelect(process, manualMap);
//            }
            //处理分支
            if (flowData.get("condition") != null) {
                setActivityIsDelete(process, (Map<String, String>) flowData.get("condition"));
            }
            //删除待办节点
            myresult = delNoActivationNode1(process, activityId, peoples, userId, caseId, orginalReadyObject,theCase);
        } else if (operationType == 5) {//替换未激活节点
            String processXmlNew = replaceActivity(process, activityId, peoples, userId,theCase);
            String orginalReadyObjectJsonNew = "";
            if (null != orginalReadyObject) {
                orginalReadyObjectJsonNew = ReadyObjectUtil.readyObjectToJSON(orginalReadyObject);
            }
            myresult = new String[] { processXmlNew, orginalReadyObjectJsonNew };
        } else if (operationType == 6) {//替换待办节点
            myresult = replaceActivity1(process, activityId, peoples, userId, caseId, orginalReadyObject,theCase);
        } else if (operationType == 7) {
            isNodePolicyChange = !policy.getId().equals(acitvity.getSeeyonPolicy().getId());
            if (isForm) {
                isOperationNameChange = !oldOperationName.equals(policy.getFormViewOperation());
            }
            isProcessModeChange = !oldProcessMode.equals(policy.getProcessMode());
            isDeadTermChange = !oldDeadTerm.equals(policy.getdealTerm());
            isRemindTimeCahange = !oldRemindTime.equals(policy.getRemindTime());
            isCycleRemindTimeCahange = !oldCycleRemindTime.equals(policy.getCycleRemindTime());
            if(Strings.isBlank(oldDeadTerm) && "0".equals(policy.getdealTerm())){
                //防护：处理期限：old:空，new:0;其实都是“无”
                isDeadTermChange = false;
            }
            if(Strings.isBlank(oldRemindTime) && "0".equals(policy.getRemindTime())){
                //防护：提前提醒时间：old:空，new:0;其实都是“无”
                isRemindTimeCahange = false;
            }
            if(Strings.isBlank(oldCycleRemindTime) && "0".equals(policy.getCycleRemindTime())){
                //防护：提前提醒时间：old:空，new:0;其实都是“无”
            	isCycleRemindTimeCahange = false;
            }
            //指定人修改了也算改变
            isDealTermActionChange = !oldDealTermAction.equals(policy.getDealTermType()) || !oldDealTermUserId.equals(policy.getDealTermUserId());
            if(Strings.isBlank(oldDealTermAction) && "0".equals(policy.getDealTermType()) || ("null".equals(oldDealTermUserId) && Strings.isBlank(policy.getDealTermUserId()))){
                //防护：处理策略：old:空，new:0; 0表示仅消息提醒
                if(isDeadTermChange)
                    isDealTermActionChange = true;
                else
                    isDealTermActionChange = false;
            }
            //设置节点属性
            String processXmlNew = setActivityPolicy(process, activityId, policy, userId, isForm,isProIncludeChild,nodeName,theCase);
            String orginalReadyObjectJsonNew = "";
            if (null != orginalReadyObject) {
                orginalReadyObjectJsonNew = ReadyObjectUtil.readyObjectToJSON(orginalReadyObject);
            }
            myresult = new String[] { processXmlNew, orginalReadyObjectJsonNew };
        }
        //myresult[0] = Strings.escapeJavascript(myresult[0]);
        //流程日志
        List<Map<String, Object>> newProcessLogList = null;
        try {
            if (null == oldProcessLogJson || "".equals(oldProcessLogJson.trim())) {
                newProcessLogList = new ArrayList<Map<String, Object>>();
            } else {
                newProcessLogList = (List<Map<String, Object>>) JSONUtil.parseJSONString(oldProcessLogJson);
            }
            switch (operationType) {
                //增加节点
                case 1:
                case 2:
                    for (Map<String, Object> p : peoples) {
                        BPMSeeyonPolicy bpmSeeyonPolicy = (BPMSeeyonPolicy) p.get("bpmSeeyonPolicy");
                        Map<String, Object> pLog = new HashMap<String, Object>();
                        pLog.put("processId", process.getId());
                        pLog.put("activityId", p.get("activityId"));
                        pLog.put("actionId", ProcessLogAction.addNode.key());
                        pLog.put("actionUserId", userId);
                        pLog.put("param0", bpmSeeyonPolicy.getName());
                        pLog.put("param1", p.get("name"));
                        pLog.put("param2", getDeadTerm(bpmSeeyonPolicy.getdealTerm()));
                        pLog.put("param3", getProcessModeName(bpmSeeyonPolicy.getProcessMode()));
                        newProcessLogList.add(pLog);
                    }
                    break;
                //删除节点
                case 3:
                case 4:
                    Map<String, Object> pLog = new HashMap<String, Object>();
                    pLog.put("processId", process.getId());
                    pLog.put("activityId", 1l);
                    pLog.put("actionId", ProcessLogAction.deleteNode.key());
                    pLog.put("actionUserId", userId);
                    pLog.put("param0", acitvity.getSeeyonPolicy().getName());
                    pLog.put("param1", acitvity.getName());
                    newProcessLogList.add(pLog);
                    break;
                //节点替换
                case 5:
                case 6:
                    BPMActivity acitvitys = process.getActivityById(activityId);
                    for (Map<String, Object> p : peoples) {
                        Map<String, Object> psLog = new HashMap<String, Object>();
                        psLog.put("processId", process.getId());
                        psLog.put("activityId", 1l);
                        psLog.put("actionId", ProcessLogAction.replaceNode.key());
                        psLog.put("actionUserId", userId);
                        psLog.put("param0", acitvitys.getSeeyonPolicy().getName());
                        psLog.put("param1", p.get("name"));
                        psLog.put("actionDesc", ResourceUtil.getString("workflow.processLog.action.desc5", userName, oldName, p.get("name")));
                        newProcessLogList.add(psLog);
                    }
                    break;
                //设置节点属性
                case 7:
                    //增加表单绑定日志
                    if (isForm) {
                        if (isOperationNameChange) {
                            
                            BPMActivity currentActivity = process.getActivityById(activityId);
                            String summarySubject = "";
                            try {
                                summarySubject = WorkFlowAppExtendInvokeManager.getAppManager(
                                        ApplicationCategoryEnum.collaboration.name()).getSummarySubject(processId);
                            } catch (Throwable e) {
                                log.error(e.getMessage(), e);
                            }
                            
                            StringBuilder operationNames = new StringBuilder();
                            
                            if (isOperationNameChange) {
                                String operationId = policy.getBindFormOption()[1];
                                String operationName = WorkflowFormDataMapInvokeManager.getAppManager("form")
                                        .getLatestOperationName(policy.getFormApp(), operationId);
                                operationNames.append(operationName);
                            }
                        
                            Map<String, Object> psLog = new HashMap<String, Object>();
                            psLog.put("processId", process.getId());
                            psLog.put("activityId", activityId);
                            psLog.put("actionId", ProcessLogAction.nodeproperties.key());
                            psLog.put("actionUserId", userId);
                            psLog.put("param0", userName);
                            psLog.put("param1", summarySubject);
                            psLog.put("param2", currentActivity.getName());
                            psLog.put("param3", operationNames.toString());
                            newProcessLogList.add(psLog);
                        }
                    }
                    if (isNodePolicyChange) {
                        if (!newProcessLogList.isEmpty() && newProcessLogList.size() > 0) {
                            boolean isAdd = false;
                            //新增新节点
                            for (Map<String, Object> psLog : newProcessLogList) {
                                if (psLog.get("activityId").toString().equals(activityId)
                                        && Integer.parseInt(psLog.get("actionId").toString()) == ProcessLogAction.changeNodePolicy
                                                .ordinal()) {
                                    psLog.put("param0", acitvity.getSeeyonPolicy().getName());
                                    isAdd = true;
                                    break;
                                }
                            }
                            // 更改新/老节点
                            if (!isAdd) {
                                Map<String, Object> psLog = new HashMap<String, Object>();
                                psLog.put("processId", process.getId());
                                psLog.put("activityId", activityId);
                                psLog.put("actionId", ProcessLogAction.changeNodePolicy.key());
                                psLog.put("actionUserId", userId);
                                psLog.put("param0", userName);
                                psLog.put("param1", acitvity.getName());
                                psLog.put("param2", oldNodePolicy);
                                psLog.put("param3", acitvity.getSeeyonPolicy().getName());
                                newProcessLogList.add(psLog);
                            }
                        } else { // 第一次更改老节点
                            Map<String, Object> psLog = new HashMap<String, Object>();
                            psLog.put("processId", process.getId());
                            psLog.put("activityId", activityId);
                            psLog.put("actionId", ProcessLogAction.changeNodePolicy.key());
                            psLog.put("actionUserId", userId);
                            psLog.put("param0", userName);
                            psLog.put("param1", acitvity.getName());
                            psLog.put("param2", oldNodePolicy);
                            psLog.put("param3", acitvity.getSeeyonPolicy().getName());
                            newProcessLogList.add(psLog);
                        }
                    }
                    
                    if(isProcessModeChange){
                        Map<String, Object> psLog = new HashMap<String, Object>();
                        psLog.put("processId", process.getId());
                        psLog.put("activityId", activityId);
                        psLog.put("actionId", ProcessLogAction.processMode.getKey());
                        psLog.put("actionUserId", userId);
                        psLog.put("param0", userName);
                        psLog.put("param1", acitvity.getName());
                        psLog.put("param2", getProcessModeName(oldProcessMode));
                        psLog.put("param3",getProcessModeName(acitvity.getSeeyonPolicy().getProcessMode()));
                        newProcessLogList.add(psLog);
                    }
                    if(isDeadTermChange || isRemindTimeCahange || isCycleRemindTimeCahange || isDealTermActionChange){
                        Map<String, Object> psLog = new HashMap<String, Object>();
                        psLog.put("processId", process.getId());
                        psLog.put("activityId", activityId);
                        psLog.put("actionUserId", userId);
                        psLog.put("param0", userName);
                        psLog.put("param1", acitvity.getName());
                        if(isDealTermActionChange){
                            psLog.put("actionId", ProcessLogAction.nodeLimitTime.getKey());
                            if(isDeadTermChange && isRemindTimeCahange){
                                psLog.put("actionDesc", ResourceUtil.getStringByParams("workflow.processLog.action.desc1", 
                                        userName, acitvity.getName(), getDeadTerm(oldDeadTerm), getDeadTerm(acitvity.getSeeyonPolicy().getdealTerm()),
                                        getRemindTime(oldRemindTime), getRemindTime(acitvity.getSeeyonPolicy().getRemindTime()), 
                                        getDealTermActionName(oldDealTermAction, oldDealTermUserName), 
                                        getDealTermActionName(acitvity.getSeeyonPolicy().getDealTermType(), acitvity.getSeeyonPolicy().getDealTermUserName())));
                            } else if(isDeadTermChange){
                                psLog.put("actionDesc", ResourceUtil.getStringByParams("workflow.processLog.action.desc2", 
                                        userName, acitvity.getName(), getDeadTerm(oldDeadTerm), getDeadTerm(acitvity.getSeeyonPolicy().getdealTerm()),
                                        getDealTermActionName(oldDealTermAction, oldDealTermUserName), 
                                        getDealTermActionName(acitvity.getSeeyonPolicy().getDealTermType(), acitvity.getSeeyonPolicy().getDealTermUserName())));
                            } else if(isRemindTimeCahange){
                                psLog.put("actionDesc", ResourceUtil.getStringByParams("workflow.processLog.action.desc3", 
                                        userName, acitvity.getName(), getRemindTime(oldRemindTime), getRemindTime(acitvity.getSeeyonPolicy().getRemindTime()), 
                                        getDealTermActionName(oldDealTermAction, oldDealTermUserName), 
                                        getDealTermActionName(acitvity.getSeeyonPolicy().getDealTermType(), acitvity.getSeeyonPolicy().getDealTermUserName())));
                            } else {
                                psLog.put("actionDesc", ResourceUtil.getStringByParams("workflow.processLog.action.desc4", 
                                        userName, acitvity.getName(), getDealTermActionName(oldDealTermAction, oldDealTermUserName), 
                                        getDealTermActionName(acitvity.getSeeyonPolicy().getDealTermType(), acitvity.getSeeyonPolicy().getDealTermUserName())));
                            }
                        } else {
                            if(isDeadTermChange && isRemindTimeCahange){
                                psLog.put("actionId", ProcessLogAction.nodeLimitTime.getKey());
                                psLog.put("param2", getDeadTerm(oldDeadTerm));
                                psLog.put("param3", getDeadTerm(acitvity.getSeeyonPolicy().getdealTerm()));
                                psLog.put("param4", getRemindTime(oldRemindTime));
                                psLog.put("param5", getRemindTime(acitvity.getSeeyonPolicy().getRemindTime()));
                            } else if(isDeadTermChange){
                                psLog.put("actionId", ProcessLogAction.deadTerm.getKey());
                                psLog.put("param2", getDeadTerm(oldDeadTerm));
                                psLog.put("param3", getDeadTerm(acitvity.getSeeyonPolicy().getdealTerm()));
                            } else if(isRemindTimeCahange){
                                psLog.put("actionId", ProcessLogAction.remindTime.getKey());
                                psLog.put("param2", getRemindTime(oldRemindTime));
                                psLog.put("param3", getRemindTime(acitvity.getSeeyonPolicy().getRemindTime()));
                            } else if(isCycleRemindTimeCahange){
                                psLog.put("actionId", ProcessLogAction.remindTime.getKey());
                                psLog.put("param2", getRemindTime(oldCycleRemindTime));
                                psLog.put("param3", getRemindTime(acitvity.getSeeyonPolicy().getCycleRemindTime()));
                            }
                        }
                        newProcessLogList.add(psLog);
                    }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        
        String case_log_xml = caseManager.getCaseLogXml(theCase)[0];
        String case_workitem_log_xml = workItemManager.getCaseWorkItemLogXml(caseId);
        result[0] = myresult[0];
        result[1] = case_log_xml;
        result[2] = case_workitem_log_xml;
        if (null == myresult[1] || "".equals(myresult[1].trim())) {
            if (null != orginalReadyObjectJson) {
                result[3] = orginalReadyObjectJson.trim();
            } else {
                result[3] = "";
            }
        } else {
            result[3] = myresult[1];
        }
        if (null != newProcessLogList && newProcessLogList.size() > 0) {
            result[4] = JSONUtil.toJSONString(newProcessLogList);
        } else {
            result[4] = "";
        }
        return result;
    }
    
    private String getProcessModeName(String processModeValue){
        if("single".equals(processModeValue)){
            return ResourceUtil.getString("workflow.commonpage.processmode.single");
        } else if("multiple".equals(processModeValue)){
            return ResourceUtil.getString("workflow.commonpage.processmode.multiple");
        } else if("all".equals(processModeValue)){
            return ResourceUtil.getString("workflow.commonpage.processmode.all");
        } else if("competition".equals(processModeValue)){
            return ResourceUtil.getString("workflow.commonpage.processmode.competition");
        } else {
            return processModeValue;
        }
    }
    
    /**
     * 获取处理期限
     * @param deadTerm
     * @return
     */
    private String getDeadTerm(String deadTerm){
        if ( Strings.isBlank(deadTerm) || "undefined".equalsIgnoreCase(deadTerm)  || "null".equalsIgnoreCase(deadTerm)) {
            deadTerm = "0";
        }
        String regExp= "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";
        if(deadTerm.matches(regExp)){//具体日期：yyyy-MM-dd HH:mm
            return deadTerm;
        } 
        CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.collaboration_deadline, deadTerm);
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }
    
    /**
     * 获取提前提醒时间
     * @param remindTime
     * @return
     */
    public String getRemindTime(String remindTime) {
        if (remindTime == null || "".equals(remindTime) || "undefined".equalsIgnoreCase(remindTime)) {
            remindTime = "-1";
        }
        CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.common_remind_time, remindTime);
        if(cei == null){//无
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        return ResourceUtil.getString(cei.getLabel());
    }
    
    /**
     * 获取处理策略的动作名称
     * @param dealTermAction
     * @return
     */
    public String getDealTermActionName(String dealTermAction, String dealTermUserName){
        String dealTermActionName = "";
        if(Strings.isBlank(dealTermAction) || "undefined".equalsIgnoreCase(dealTermAction)){
            return ResourceUtil.getString("collaboration.project.nothing.label");
        }
        if("0".equals(dealTermAction)){
            dealTermActionName = ResourceUtil.getString("workflow.designer.node.deadline.arrived.do0");
        } else if("1".equals(dealTermAction)){
            dealTermActionName = ResourceUtil.getString("workflow.designer.node.deadline.arrived.do1") + dealTermUserName;
        } else if("2".equals(dealTermAction)){
            dealTermActionName = ResourceUtil.getString("workflow.designer.node.deadline.arrived.do2");
        }
        return dealTermActionName;
    }

    /**
     * 
     * @param process
     * @param conditionR
     * @throws BPMException
     */
    private void setActivityIsDelete(BPMProcess process, Map<String, String> condition) throws BPMException {
        try {
            Iterator iter = condition.keySet().iterator();
            while (iter.hasNext()) {
                String nodeId = (String) iter.next();
                String isDelete = condition.get(nodeId);
                BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(nodeId);
                if (activity == null) {
                    continue;
                }
                BPMSeeyonPolicy seeyonPolicy = (BPMSeeyonPolicy) activity.getSeeyonPolicy();
                seeyonPolicy.setIsDelete(isDelete);
            }
        } catch (Throwable e) {
            log.error("动态设置节点是否进行假删除异常" + e.getMessage(), e);
            throw new BPMException("Dynamically set Node to delete error"/*"动态设置节点是否进行假删除异常"*/, e);
        }
    }

    /**
     * 设置待办节点属性
     * @param process
     * @param activityId
     * @param policy
     * @param userId
     * @param isForm
     * @return
     * @throws BPMException
     */
    private void setActivityPolicy(BPMProcess process, String activityId, BPMSeeyonPolicy policy,
            boolean isForm,String isProIncludeChild,String nodeName) throws BPMException {
        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            //获取原seeyonpolicy中的表单信息
            BPMSeeyonPolicy oldPolicy = currentActivity.getSeeyonPolicy();
            policy.setFormApp(oldPolicy.getFormApp());
            if (!isForm) {
                policy.setFormViewOperation(oldPolicy.getFormViewOperation());
                policy.setForm("");
                policy.setOperationName("");
            }else{
                BPMActor actor= (BPMActor)currentActivity.getActorList().get(0);
                String partyTypeId = actor.getParty().getType().id;
                if("Post".equals(partyTypeId)){
                    actor.getParty().setIncludeChild("true".equals(isProIncludeChild));
                }
            }
            policy.setNF(oldPolicy.getNF());
            currentActivity.setSeeyonPolicy(policy);
            currentActivity.setDesc(policy.getDesc());
            String pasteTo = currentActivity.getPasteTo();
            if(Strings.isNotBlank(pasteTo) && !"null".equals(pasteTo) && !"undefined".equals(pasteTo)){
                String[] pasteActivityIds = pasteTo.split(",");
                for (String pasteActivityId : pasteActivityIds) {
                    BPMActivity pastedActivity = process.getActivityById(pasteActivityId);
                    if(pastedActivity != null){
                        pastedActivity.getSeeyonPolicy().setProcessMode(currentActivity.getSeeyonPolicy().getProcessMode());
                        pastedActivity.getSeeyonPolicy().setMatchScope(currentActivity.getSeeyonPolicy().getMatchScope());
                        pastedActivity.getSeeyonPolicy().setFormField(currentActivity.getSeeyonPolicy().getFormField());
                        BPMActor actor= (BPMActor)currentActivity.getActorList().get(0);
                        BPMActor pastedActor = (BPMActor)pastedActivity.getActorList().get(0);
                        pastedActor.getParty().setIncludeChild(actor.getParty().isIncludeChild());
                        pastedActivity.getSeeyonPolicy().setRup(currentActivity.getSeeyonPolicy().getRup());
                        pastedActivity.getSeeyonPolicy().setPup(currentActivity.getSeeyonPolicy().getPup());
                        pastedActivity.getSeeyonPolicy().setNa(currentActivity.getSeeyonPolicy().getNa());
                    }
                }
            }
            if(Strings.isNotBlank(nodeName)){
                currentActivity.setName(nodeName);
            }
            
        } catch (Throwable e) {
            log.error("替换节点操作异常" + e.getMessage(), e);
            throw new BPMException("set node policy error"/*"替换节点操作异常"*/, e);
        }
    }

    /**
     * 设置待办节点属性
     * @param process
     * @param activityId
     * @param policy
     * @param userId
     * @param isForm
     * @return
     * @throws BPMException
     */
    private String setActivityPolicy(BPMProcess process, String activityId, BPMSeeyonPolicy policy, String userId,
            boolean isForm,String isProIncludeChild,String nodeName,BPMCase theCase) throws BPMException {
        try {
            
            setActivityPolicy(process, activityId, policy, isForm, isProIncludeChild, nodeName);
            
            return transProcess2XML(process, userId, theCase, true);
            
        } catch (Throwable e) {
            log.error("替换节点操作异常" + e.getMessage(), e);
            throw new BPMException("set node policy error"/*"替换节点操作异常"*/, e);
        }
    }

    /**
     * 替换待办节点
     * @param process
     * @param activityId
     * @param people
     * @param userId
     * @param caseId
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    private String[] replaceActivity1(BPMProcess process, String activityId, List<Map<String, Object>> people,
            String userId, String caseId, ReadyObject _readyObject,BPMCase theCase) throws BPMException {
        List<BPMActivity> activityList = new ArrayList<BPMActivity>();
        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            _readyObject = setPreDelActivity(_readyObject, process, caseId, currentActivity, userId, false);
            List<BPMActor> actorList = new ArrayList<BPMActor>();
            BPMActor userActor = null;
            for (int i = 0; i < people.size(); i++) {
                Map<String, Object> party = people.get(i);
                userActor = createActor(party);
                userActor.getParty().getName();
                actorList.add(userActor);
                if (!"user".equals(party.get("type"))) {
                    currentActivity.getSeeyonPolicy().setProcessMode("all");
                }else{
                    currentActivity.getSeeyonPolicy().setProcessMode("single");
                }
            }
            currentActivity.setActorList(actorList);
            String actorName = "";
            if(userActor!=null){
                BPMParticipant party = userActor.getParty();
                if(party != null){
                    actorName = party.getName();
                }
            }
            currentActivity.setName(actorName);
            currentActivity.setCustomName("");
            WorkflowUtil.clearCopyNodeProperty(process, activityId);
            activityList.add(currentActivity);
            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);

            ProcessDefManager pdm = WAPIFactory.getProcessDefManager("Engine_1");
            pdm.saveOrUpdateProcessInReady(process);

            ReadyObject readyObject = new ReadyObject();
            readyObject.setActivityList(activityList);
            readyObject.setCaseId(caseId + "");
            readyObject.setProcessId(process.getId());
            readyObject.setUserId(userId + "");
            readyObject.setSaveTheCaseFlag(false);
            _readyObject = mergeReadyObject(readyObject, _readyObject);
            process.setModifyUser(userId);
            String processXml = process.toXML(theCase, true);
            String readyObjectJson = ReadyObjectUtil.readyObjectToJSON(_readyObject);
            String[] result = new String[] { processXml, readyObjectJson };
            return result;
        } catch (Throwable e) {
            log.error("替换节点操作异常" + e.getMessage(), e);
            throw new BPMException("replace node error"/*"替换节点操作异常"*/, e);
        }
    }

    /**
     * 替换节点，并返回新生成节点ID
     * 
     * @param process
     * @param activityId
     * @param people
     * @return
     * @throws BPMException
     *
     * @Author      : xuqw
     * @Date        : 2016年6月21日下午2:30:31
     *
     */
    private String replaceActivity(BPMProcess process, String activityId, 
            List<Map<String, Object>> people) throws BPMException{

        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            List<BPMActor> actorList = new ArrayList<BPMActor>();
            BPMActor userActor = null;
            for (int i = 0; i < people.size(); i++) {
                Map<String, Object> party = people.get(i);
                userActor = createActor(party);
                userActor.getParty().getName();
                actorList.add(userActor);
                if (!"user".equals(party.get("type"))) {
                    currentActivity.getSeeyonPolicy().setProcessMode("all");
                }else{
                    currentActivity.getSeeyonPolicy().setProcessMode("single");
                }
            }
            currentActivity.setActorList(actorList);
            String actorName = "";
            if(userActor!=null){
                BPMParticipant party = userActor.getParty();
                if(party != null){
                    actorName = party.getName();
                }
            }
            currentActivity.setName(actorName);
            currentActivity.setCustomName("");
            WorkflowUtil.clearCopyNodeProperty(process, activityId);
            String id= String.valueOf(UUIDLong.longUUID());
            List<BPMTransition> upList= currentActivity.getUpTransitions();
            List<BPMTransition> downList= currentActivity.getDownTransitions();
            currentActivity.setId(id);
            if(upList!=null && upList.size()>0){
                BPMTransition up= upList.get(0);
                up.setTo(currentActivity);
            }
            if(downList!=null && downList.size()>0){
                BPMTransition down= downList.get(0);
                down.setFrom(currentActivity);
            }
            
            return id;
        } catch (Exception e) {
            log.error("替换节点操作异常" + e.getMessage(), e);
            throw new BPMException("replace node error"/*"替换节点操作异常"*/, e);
        }
    
    }
    
    /**
     * 替换未激活节点
     * @param process
     * @param activityId
     * @param people
     * @param userId
     * @return
     * @throws BPMException
     */
    private String replaceActivity(BPMProcess process, String activityId, List<Map<String, Object>> people,
            String userId,BPMCase theCase) throws BPMException {
        try {
            replaceActivity(process, activityId, people);
            
            return transProcess2XML(process, userId, theCase, true);
        } catch (Exception e) {
            log.error("替换节点操作异常" + e.getMessage(), e);
            throw new BPMException("replace node error"/*"替换节点操作异常"*/, e);
        }
    }
    
    
    /**
     * 更新流程修改时间，并转换成XML字符串
     * 
     * @param process
     * @param userId
     * @param theCase
     * @return
     *
     * @Author      : xuqw
     * @Date        : 2016年6月21日下午2:19:38
     *
     */
    private String transProcess2XML(BPMProcess process, String userId, BPMCase theCase, boolean isNeedFindName){
        Date now = new Date(System.currentTimeMillis());
        process.setUpdateDate(now);
        process.setModifyUser(userId);
        String processXml = process.toXML(theCase, isNeedFindName);
        return processXml;
    }
    
    /**
     * 添加新节点--待办节点之后增加节点
     * @param process
     * @param activityId
     * @param people
     * @param type
     * @param userId
     * @param caseId
     * @param isPending
     * @param isShowShortName
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    private String[] addNewActivity(BPMProcess process, String activityId, List<Map<String, Object>> people, int type,
            String userId, Long caseId, boolean isPending, String isShowShortName, ReadyObject _readyObject,BPMCase theCase)
            throws BPMException {
        String readObjectJson = "";
        try {
            if ((type == FLOWTYPE_SERIAL || people.size() == 1) && type != FLOWTYPE_COLASSIGN) {
                //串发
                readObjectJson = addSerial(process, activityId, people, new BPMSeeyonPolicy("shenpi", "审批"), isPending,
                        caseId, false, isShowShortName, userId, _readyObject);
            } else if (type == FLOWTYPE_PARALLEL) {
                //并发
                readObjectJson = addParellel(process, activityId, people, new BPMSeeyonPolicy("shenpi", "审批"),
                        isPending, caseId, false, isShowShortName, userId, _readyObject);
            } else {
                //会签
                readObjectJson = colAssign(process, activityId, people, null, caseId, isPending, isShowShortName,
                        userId, _readyObject);
            }
            process.setModifyUser(userId);
            String processXml = process.toXML(theCase,true);
            String[] result = new String[] { processXml, readObjectJson };
            return result;
        } catch (Throwable e) {
            log.error("增加新节点异常 [activityId = " + activityId + "]" + e.getMessage(), e);
            throw new BPMException("add node error[activityId = " + activityId + "]", e);
        }
    }

    /**
     * 添加串发新节点
     * @param process
     * @param activityId
     * @param people
     * @param policy
     * @param isPending
     * @param caseId
     * @param isFormReadonly
     * @param isShowShortName
     * @param userId
     * @return
     * @throws BPMException
     */
    private String addSerial(BPMProcess process, String activityId, List<Map<String, Object>> people,
            BPMSeeyonPolicy policy, boolean isPending, Long caseId, boolean isFormReadonly, String isShowShortName,
            String userId, ReadyObject _readyObject) throws BPMException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<BPMActivity> activityList = new ArrayList<BPMActivity>();
        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            List downTransitions = currentActivity.getDownTransitions();
            BPMTransition tran = (BPMTransition) downTransitions.get(0);
            BPMAbstractNode childNode = (BPMAbstractNode) tran.getTo();
            boolean isJoin = (childNode instanceof BPMAndRouter) && !((BPMAndRouter) childNode).isStartAnd();
            //添加的第一个结�点
            BPMAbstractNode firstNode = null;
            //添加的最后一个结�点
            BPMAbstractNode lastNode = null;
            BPMAbstractNode previousNode = currentActivity;
            boolean isAddReady = true;
            String formApp = "";
            String formViewOperation = "";
            if (currentActivity.getNodeType().equals(BPMAbstractNode.NodeType.join)) {//当前节点为join节点加签时，需要进行特殊处。
                List<BPMHumenActivity> parents = getParentHumens(currentActivity);
                if (parents.size() > 0) {
                    BPMHumenActivity myRealParentNode = parents.get(0);
                    BPMSeeyonPolicy mySeeyonPolicy = myRealParentNode.getSeeyonPolicy();
                    formApp = mySeeyonPolicy.getFormApp();
                    formViewOperation = mySeeyonPolicy.getFormViewOperation();
                }
            } else {
                BPMSeeyonPolicy seeyonPolicy = currentActivity.getSeeyonPolicy();
                if (seeyonPolicy != null) {
                    formApp = seeyonPolicy.getFormApp();
                    formViewOperation = seeyonPolicy.getFormViewOperation();
                }
            }
            for (int i = 0; i < people.size(); i++) {
                Map<String, Object> party = people.get(i);
                BPMAbstractNode userNode = new BPMHumenActivity(WorkflowUtil.getTableKey() + "", (String) party.get("name"));
                BPMActor userActor = createActor(party);
                userNode.addActor(userActor);
                party.put("activityId", userNode.getId());

                if (party.get("bpmSeeyonPolicy") != null) {
                    userNode.setSeeyonPolicy((BPMSeeyonPolicy) party.get("bpmSeeyonPolicy"));
                } else {
                    userNode.setSeeyonPolicy(new BPMSeeyonPolicy(policy));
                }
                userNode.getSeeyonPolicy().setFormApp(formApp);
                userNode.getSeeyonPolicy().setFormViewOperation(formViewOperation);
                userNode.getSeeyonPolicy().setFR(isFormReadonly ? "1" : "");

                if (!"user".equals(party.get("type"))) {
                    BPMSeeyonPolicy _policy = userNode.getSeeyonPolicy();
                    _policy.setProcessMode("all");
                    
                    if("Post".equals(party.get("type"))) {
                    	try {//集团基准岗匹配范围是全集团
                    		String partyAccountId = (String) party.get("accountId");
                    		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(partyAccountId));
                    		if(account != null && account.isGroup()) {
                    			userNode.getSeeyonPolicy().setMatchScope("2");
                    		}
                    	} catch(Exception e) {
                    		log.error("加签操作获取集团开岗位出错", e);
                    	}
                    }
                }

                if (firstNode == null) {
                    firstNode = userNode;
                }
                BPMTransition userLink1 = new BPMTransition(previousNode, userNode);
                process.addChild(userNode);
                process.addLink(userLink1);
                previousNode = userNode;
                if (isAddReady) {
                    activityList.add((BPMActivity) userNode);
                    isAddReady = false;
                }
            }
            lastNode = previousNode;
            if (downTransitions != null) {
                for (int i = 0; i < downTransitions.size(); i++) {
                    BPMTransition trans = (BPMTransition) downTransitions.get(i);
                    BPMAbstractNode to = trans.getTo();
                    BPMTransition userLink1 = new BPMTransition(lastNode, to);
                    copyCondition(trans, userLink1);
                    process.addLink(userLink1);
                    process.removeLink(trans);
                    currentActivity.removeDownTransition(trans);
                }
            }

            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            if ("false".equals(isShowShortName)) {
                if ("false".equals(process.getIsShowShortName())) {
                    process.setIsShowShortName(isShowShortName);
                }
            } else {
                process.setIsShowShortName(isShowShortName);
            }
            //将加入的人设为ready状�态
            BPMCase theCase = null;
            if (isPending) {
                theCase = caseManager.getCase(caseId);
                if (theCase == null) {
                    throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
                }
                ReadyObject readyObject = new ReadyObject();
                readyObject.setActivityList(activityList);
                readyObject.setCaseId(caseId + "");
                readyObject.setProcessId(process.getId());
                readyObject.setUserId(userId + "");

                boolean saveTheCaseFlag = false;
                if (isJoin && theCase != null) {
                    ReadyNode node = theCase.getReadyActivityById(childNode.getId());
                    if (node != null) {
                        saveTheCaseFlag = true;
                    }
                }
                readyObject.setSaveTheCaseFlag(saveTheCaseFlag);
                ReadyObject readyObjectNew = mergeReadyObject(readyObject, _readyObject);
                String readyObjectJson = ReadyObjectUtil.readyObjectToJSON(readyObjectNew);
                return readyObjectJson;
            }
        } catch (Throwable e) {
            log.error("增加节点串发操作异常" + e.getMessage(), e);
            throw new BPMException("do adding serial nodes action error", e);
        }
        return "";
    }

    /**
     * 得到activiey的父节点,如果不是人工节点递归读取
     * @param activity
     * @return
     */
    private List<BPMHumenActivity> getParentHumens(BPMActivity activity) {
        List<BPMHumenActivity> humenList = new ArrayList();
        List<BPMTransition> transitions = activity.getUpTransitions();
        for (BPMTransition tran : transitions) {
            BPMAbstractNode parent = tran.getFrom();
            if (parent.getNodeType() == BPMAbstractNode.NodeType.humen) {
                humenList.add((BPMHumenActivity) parent);
            } else if (parent.getNodeType() == BPMAbstractNode.NodeType.join
                    || parent.getNodeType() == BPMAbstractNode.NodeType.split) {
                humenList.addAll(getParentHumens((BPMActivity) parent));
            }
        }
        return humenList;
    }

    /**
     * 
     * @param party
     * @return
     */
    private BPMActor createActor(Map<String, Object> party) {
        String partyId = (String) party.get("id");
        String partyName = (String) party.get("name");
        String partyType = (String) party.get("type");
        String partyAccountId = (String) party.get("accountId");
        BPMParticipantType type = new BPMParticipantType(partyType);
        String roleName = "roleadmin";
        BPMActor userActor = new BPMActor(partyId, partyName, type, roleName, BPMActor.CONDITION_OR, false,
                partyAccountId);
        userActor.getParty().setIncludeChild(Boolean.parseBoolean(party.get("includeChild").toString()));
        return userActor;
    }

    /**
     * 
     * @param target
     * @param to
     */
    private static void copyCondition(BPMTransition target, BPMTransition to) {
        to.setConditionBase(target.getConditionBase());
        to.setConditionId(target.getConditionId());
        to.setConditionTitle(target.getConditionTitle());
        to.setConditionType(target.getConditionType());
        to.setFormCondition(target.getFormCondition());
        to.setIsForce(target.getIsForce());
    }

    /**
     * 添加并发新节点
     * @param process
     * @param activityId
     * @param people
     * @param policy
     * @param isPending
     * @param caseId
     * @param isFormReadonly
     * @param isShowShortName
     * @param userId
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    public String addParellel(BPMProcess process, String activityId, List<Map<String, Object>> people,
            BPMSeeyonPolicy policy, boolean isPending, Long caseId, boolean isFormReadonly, String isShowShortName,
            String userId, ReadyObject _readyObject) throws BPMException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        List<BPMActivity> activityList = new ArrayList<BPMActivity>();
        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            BPMSeeyonPolicy seeyonPolicy = currentActivity.getSeeyonPolicy();
            String formApp = "";
            String formViewOperation = "";
            if (seeyonPolicy != null) {
                formApp = seeyonPolicy.getFormApp();
                formViewOperation = seeyonPolicy.getFormViewOperation();
            }
            List downTransitions = currentActivity.getDownTransitions();
            BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);

            BPMAbstractNode childNode = (BPMAbstractNode) nextTrans.getTo();
            boolean isJoin = (childNode instanceof BPMAndRouter) && !((BPMAndRouter) childNode).isStartAnd();

            BPMAndRouter split = null;
            BPMAndRouter join = null;
            //查看下一结点是否有split类型�jiedian节点
            for (int i = 0; i < downTransitions.size(); i++) {
                BPMTransition trans = (BPMTransition) downTransitions.get(i);
                if (trans.getTo() instanceof BPMAndRouter) {
                    BPMAndRouter to = (BPMAndRouter) trans.getTo();
                    if (to.isStartAnd()) {
                        split = to;
                    }
                }
            }

            //如果有split结点，遍历找到join结点
            if (split != null) {
                boolean foundJoin = false;
                BPMAbstractNode node = split;
                while (!foundJoin) {
                    BPMTransition trans = (BPMTransition) node.getDownTransitions().get(0);
                    node = trans.getTo();
                    if (node instanceof BPMAndRouter) {
                        BPMAndRouter andNode = (BPMAndRouter) node;
                        if (!andNode.isStartAnd()) {
                            if (split.getParallelismNodeId().equals(andNode.getParallelismNodeId())) {
                                foundJoin = true;
                                join = andNode;
                            }
                        }
                    }
                }
            }

            //如果没有split结点，新建split和join
            if (split == null) {
                String splitId = WorkflowUtil.getTableKey() + "";
                String joinId = WorkflowUtil.getTableKey() + "";
                split = new BPMAndRouter(splitId, "split");
                join = new BPMAndRouter(joinId, "join");
                split.setStartAnd(true);
                join.setStartAnd(false);
                String relevancyId = WorkflowUtil.getTableKey() + "";
                split.setParallelismNodeId(relevancyId);
                join.setParallelismNodeId(relevancyId);
                process.addChild(split);
                process.addChild(join);

                BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
                //如果后面是结束结点或分支结点，split和join之间不设结点，join直接连到结束结点�
                if (!((nextNode instanceof BPMHumenActivity) || (nextNode instanceof BPMTimeActivity))) {
                    BPMTransition trans1 = new BPMTransition(currentActivity, split);
                    BPMTransition trans2 = new BPMTransition(join, nextNode);
                    process.addLink(trans1);
                    process.addLink(trans2);
                    copyCondition(nextTrans, trans2);
                    process.removeLink(nextTrans);
                    currentActivity.removeDownTransition(nextTrans);
                }
                //如果后面不是结束结点，将下一结点纳入split/join之中，join之后连接nextNode.nextNode
                else {
                    BPMTransition trans1 = new BPMTransition(currentActivity, split);
                    BPMTransition trans2 = new BPMTransition(join, nextNode);
                    process.addLink(trans1);
                    process.addLink(trans2);
                    copyCondition(nextTrans, trans2);
                    process.removeLink(nextTrans);
                    currentActivity.removeDownTransition(nextTrans);
                }
            } else {
                String splitId = WorkflowUtil.getTableKey() + "";
                String joinId = WorkflowUtil.getTableKey() + "";
                split = new BPMAndRouter(splitId, "split");
                join = new BPMAndRouter(joinId, "join");
                split.setStartAnd(true);
                join.setStartAnd(false);
                String relevancyId = WorkflowUtil.getTableKey() + "";
                split.setParallelismNodeId(relevancyId);
                join.setParallelismNodeId(relevancyId);

                process.addChild(split);
                process.addChild(join);

                BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
                BPMTransition trans1 = new BPMTransition(currentActivity, split);
                BPMTransition trans2 = new BPMTransition(join, nextNode);

                process.addLink(trans1);
                process.addLink(trans2);
                copyCondition(nextTrans, trans2);
                process.removeLink(nextTrans);
                currentActivity.removeDownTransition(nextTrans);
            }

            //向split、join之间添加新节点
            int pSize= people.size()-1;
            for (int i = pSize; i >= 0; i--) {
                Map<String, Object> party = people.get(i);

                BPMAbstractNode userNode = new BPMHumenActivity(WorkflowUtil.getTableKey() + "", (String) party.get("name"));
                BPMActor userActor = createActor(party);

                userNode.addActor(userActor);
                party.put("activityId", userNode.getId());
                if (party.get("bpmSeeyonPolicy") != null) {
                    userNode.setSeeyonPolicy((BPMSeeyonPolicy) party.get("bpmSeeyonPolicy"));
                } else {
                    userNode.setSeeyonPolicy(new BPMSeeyonPolicy(policy));
                }
                userNode.getSeeyonPolicy().setFormApp(formApp);
                userNode.getSeeyonPolicy().setFormViewOperation(formViewOperation);
                userNode.getSeeyonPolicy().setFR(isFormReadonly ? "1" : "");

                if (!"user".equals(party.get("type")) && !"user".equals(party.get("Type"))) {
                    BPMSeeyonPolicy _policy = userNode.getSeeyonPolicy();
                    _policy.setProcessMode("all");
                    
                    if("Post".equals(party.get("type")) || "Post".equals(party.get("Type"))) {
                    	try {//集团基准岗匹配范围是全集团
                    		String partyAccountId = (String) party.get("accountId");
                    		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(partyAccountId));
                    		if(account != null && account.isGroup()) {
                    			userNode.getSeeyonPolicy().setMatchScope("2");
                    		}
                    	} catch(Exception e) {
                    		log.error("加签操作获取集团开岗位出错", e);
                    	}
                    }
                }

                BPMTransition userLink1 = new BPMTransition(split, userNode);
                BPMTransition userLink2 = new BPMTransition(userNode, join);

                process.addChild(userNode);
                process.addLink(userLink1);
                process.addLink(userLink2);
                activityList.add((BPMActivity) userNode);
            }

            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            if ("false".equals(isShowShortName)) {
                if ("false".equals(process.getIsShowShortName())) {
                    process.setIsShowShortName(isShowShortName);
                }
            } else {
                process.setIsShowShortName(isShowShortName);
            }

            //将加入的人设为ready状�态
            BPMCase theCase = null;
            if (isPending) {
                theCase = caseManager.getCase(caseId);
                if (theCase == null) {
                    throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
                }
                ReadyObject readyObject = new ReadyObject();
                readyObject.setActivityList(activityList);
                readyObject.setCaseId(caseId + "");
                readyObject.setProcessId(process.getId());
                readyObject.setUserId(userId + "");

                boolean saveTheCaseFlag = false;
                if (isJoin && theCase != null) {
                    ReadyNode node = theCase.getReadyActivityById(childNode.getId());
                    if (node != null) {
                        saveTheCaseFlag = true;
                    }
                }
                readyObject.setSaveTheCaseFlag(saveTheCaseFlag);
                ReadyObject readyObjectNew = mergeReadyObject(readyObject, _readyObject);
                String readyObjectJson = ReadyObjectUtil.readyObjectToJSON(readyObjectNew);
                return readyObjectJson;
            }
        } catch (Throwable e) {
            log.error("增加节点并发操作异常" + e.getMessage(), e);
            throw new BPMException("do adding concurrence nodes error", e);
        }
        return "";
    }

    /**
     * 会签
     * @param process
     * @param activityId
     * @param people
     * @param workItem
     * @param caseId
     * @param isPending
     * @param isShowShortName
     * @param userId
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    private String colAssign(BPMProcess process, String activityId, List<Map<String, Object>> people,
            WorkItem workItem, Long caseId, boolean isPending, String isShowShortName, String userId,
            ReadyObject _readyObject) throws BPMException {
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        try {
            BPMActivity a = process.getActivityById(activityId);
            String formApp = "";
            String formViewOperation = "";
            BPMSeeyonPolicy seeyonPolicy = a.getSeeyonPolicy();
            if (seeyonPolicy != null) {
                formApp = seeyonPolicy.getFormApp();
                formViewOperation = seeyonPolicy.getFormViewOperation();
            }

            List links_ba = a.getUpTransitions();
            List links_ac = a.getDownTransitions();
            BPMTransition link_ba = (BPMTransition) links_ba.get(0);
            BPMTransition link_ac = (BPMTransition) links_ac.get(0);
            BPMAbstractNode b = link_ba.getFrom();
            BPMAbstractNode c = link_ac.getTo();

            boolean bIsSplit = (b instanceof BPMAndRouter) && ((BPMAndRouter) b).isStartAnd();
            boolean cIsJoin = (c instanceof BPMAndRouter) && !((BPMAndRouter) c).isStartAnd();

            BPMAndRouter split = null;
            BPMAndRouter join = null;

            if (bIsSplit && cIsJoin) {
                split = (BPMAndRouter) b;
                join = (BPMAndRouter) c;
                BPMSeeyonPolicy virtualNodePolicy = null;
                if(null!= seeyonPolicy){
                    virtualNodePolicy = new BPMSeeyonPolicy(seeyonPolicy);
                }else{
                    virtualNodePolicy= new BPMSeeyonPolicy(BPMSeeyonPolicy.SEEYON_POLICY_COLLABORATE);
                }
                virtualNodePolicy.setFormApp(formApp);
                virtualNodePolicy.setFormViewOperation(formViewOperation);
                virtualNodePolicy.setAdded(false);
                join.setSeeyonPolicy(virtualNodePolicy);
                split.setSeeyonPolicy(virtualNodePolicy);
            } else {
                String splitId = WorkflowUtil.getTableKey() + "";
                String joinId = WorkflowUtil.getTableKey() + "";
                split = new BPMAndRouter(splitId, "split");
                join = new BPMAndRouter(joinId, "join");
                BPMSeeyonPolicy virtualNodePolicy = null;
                if(null!= seeyonPolicy){
                    virtualNodePolicy = new BPMSeeyonPolicy(seeyonPolicy);
                }else{
                    virtualNodePolicy= new BPMSeeyonPolicy(BPMSeeyonPolicy.SEEYON_POLICY_COLLABORATE);
                }
                virtualNodePolicy.setFormApp(formApp);
                virtualNodePolicy.setFormViewOperation(formViewOperation);
                virtualNodePolicy.setAdded(true);
                join.setSeeyonPolicy(virtualNodePolicy);
                split.setSeeyonPolicy(virtualNodePolicy);
                String relevancyId = WorkflowUtil.getTableKey() + "";
                split.setParallelismNodeId(relevancyId);
                join.setParallelismNodeId(relevancyId);
                split.setStartAnd(true);
                join.setStartAnd(false);
                process.addChild(split);
                process.addChild(join);

                for (int i = a.getUpTransitions().size() - 1; i >= 0; i--) {
                    BPMTransition b_a = (BPMTransition) a.getUpTransitions().get(i);
                    process.removeLink(b_a);
                    BPMTransition link_split_a = new BPMTransition(split, b_a.getTo());
                    //复制分支条件
                    copyCondition(link_ba, link_split_a);
                    process.addLink(link_split_a);
                }

                BPMTransition link_b_split = new BPMTransition(b, split);

                process.addLink(link_b_split);

                for (int i = a.getDownTransitions().size() - 1; i >= 0; i--) {
                    BPMTransition a_c = (BPMTransition) a.getDownTransitions().get(i);
                    process.removeLink(a_c);
                    BPMTransition link_a_join = new BPMTransition(a, join);
                    process.addLink(link_a_join);
                }
                BPMTransition link_join_c = new BPMTransition(join, c);
                copyCondition(link_ac, link_join_c);
                process.addLink(link_join_c);
            }

            BPMCase theCase = caseManager.getCase(caseId);
            if (theCase == null) {
                throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
            }
            //应用类型
            String appName = theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
            List<BPMActivity> added = new ArrayList<BPMActivity>();
            //split -> d -> join
            //向split、join之间添加新结节点
            for (int i = (people.size() - 1); i >= 0; i--) {
                Map<String, Object> party = people.get(i);
                BPMAbstractNode d = new BPMHumenActivity(WorkflowUtil.getTableKey() + "", (String) party.get("name"));
                BPMActor userActor = createActor(party);
                d.addActor(userActor);
                party.put("activityId", d.getId());
                BPMTransition link_split_d = new BPMTransition(split, d);
                BPMTransition link_d_join = new BPMTransition(d, join);

                //begin old code yangzd
                /*BPMSeeyonPolicy policy = null;
                if(flowData.getSeeyonPolicy() != null){
                    policy = flowData.getSeeyonPolicy();
                    policy = new BPMSeeyonPolicy(policy);
                }else{
                    policy = new BPMSeeyonPolicy("collaboration","协同");
                }*/
                //end old code
                //change  begin yangzd  --------->会签的与当前节点的权限相同
                BPMSeeyonPolicy policy = null;
                //change end
                //如果是公文，当前会签后的节点权限是‘会签'
                if ("edoc".equals(appName)) {
                    policy = new BPMSeeyonPolicy("huiqian", "会签");
                } else {
                    //如果是协同，当前会签后的节点权限同当前节点
                    policy = new BPMSeeyonPolicy(a.getSeeyonPolicy());
                }
                if (workItem != null) {
                    policy.setAddedFromId(workItem.getPerformer());
                }
                if (party.get("bpmSeeyonPolicy") != null) {
                    d.setSeeyonPolicy((BPMSeeyonPolicy) party.get("bpmSeeyonPolicy"));
                } else {
                    d.setSeeyonPolicy(policy);
                }
                if ("user".equals(party.get("type"))) {
                	d.getSeeyonPolicy().setProcessMode("single");
                } else {
                	d.getSeeyonPolicy().setProcessMode("all");
                	if("Post".equals(party.get("type"))) {
                    	try {//集团基准岗匹配范围是全集团
                    		String partyAccountId = (String) party.get("accountId");
                    		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(partyAccountId));
                    		if(account != null && account.isGroup()) {
                    			d.getSeeyonPolicy().setMatchScope("2");
                    		}
                    	} catch(Exception e) {
                    		log.error("加签操作获取集团开岗位出错", e);
                    	}
                    }
                }
                d.getSeeyonPolicy().setFormApp(formApp);
                d.getSeeyonPolicy().setFormViewOperation(formViewOperation);
                if(isPending){
                	d.getSeeyonPolicy().setAdded(true);
                }else{
                	d.getSeeyonPolicy().setAdded(false);
                }
                if (!"user".equals(party.get("type"))) {
                    BPMSeeyonPolicy _policy = d.getSeeyonPolicy();
                    if (!"competition".equals(_policy.getProcessMode())) {
                        _policy.setProcessMode("all");
                    }
                }
                added.add((BPMActivity) d);
                process.addChild(d);
                //复制分支条件
                copyCondition(link_ba, link_split_d);
                process.addLink(link_split_d);
                process.addLink(link_d_join);
            }

            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            if ("false".equals(isShowShortName)) {
                if ("false".equals(process.getIsShowShortName()))
                    process.setIsShowShortName(isShowShortName);
            } else {
                process.setIsShowShortName(isShowShortName);
            }
            //将加入的人设为ready状态
            if (isPending) {
                //pe.addReadyActivity(user.getId() + "", process, theCase, added);
                ReadyObject readyObject = new ReadyObject();
                readyObject.setActivityList(added);
                readyObject.setCaseId(caseId + "");
                readyObject.setProcessId(process.getId());
                readyObject.setUserId(userId + "");
                //修改join的num
                boolean saveTheCaseFlag = false;
                if (bIsSplit && cIsJoin && theCase != null) {
                    ReadyNode node = theCase.getReadyActivityById(join.getId());
                    if (node != null) {
                        saveTheCaseFlag = true;
                    }
                }
                readyObject.setSaveTheCaseFlag(saveTheCaseFlag);
                ReadyObject readyObjectNew = mergeReadyObject(readyObject, _readyObject);
                String readyObjectJson = ReadyObjectUtil.readyObjectToJSON(readyObjectNew);
                return readyObjectJson;
            }
        } catch (Throwable e) {
            log.error("会签操作异常" + e.getMessage(), e);
            throw new BPMException("do countersign action error", e);
        }
        return "";
    }

    /**
     * 删除未激活节点
     * @param process
     * @param activityId
     * @param people
     * @param userId
     * @param orginalReadyObject
     * @return
     * @throws BPMException
     */
    private String[] delNoActivationNode(BPMProcess process, String activityId, List<Map<String, Object>> people,
            String userId, ReadyObject orginalReadyObject,BPMCase theCase) throws BPMException {
        try {
        	WorkflowUtil.clearCopyNodeProperty(process, activityId);
            BPMActivity currentActivity = process.getActivityById(activityId);
            List<BPMTransition> linksDown = currentActivity.getDownTransitions();
            List<BPMTransition> linksUp = currentActivity.getUpTransitions();

            List<BPMCircleTransition> clinksDown = currentActivity.getDownCirlcleTransitions();
            List<BPMCircleTransition> clinksUp = currentActivity.getUpCircleTransitions();
            
            BPMTransition upTran = (BPMTransition) linksUp.get(0);
            BPMTransition downTran = (BPMTransition) linksDown.get(0);
            BPMAbstractNode parentActivity = (BPMAbstractNode) upTran.getFrom();
            BPMAbstractNode childActivity = (BPMAbstractNode) downTran.getTo();

            if (childActivity.getBPMObjectType() == ObjectName.BPMEnd) {
                process.removeLink(upTran);
                process.removeLink(downTran);
                process.removeChild(currentActivity);
                process.addLink(new BPMTransition(parentActivity, childActivity));
            } else {
                boolean parentIsSplit = (parentActivity instanceof BPMAndRouter)
                        && ((BPMAndRouter) parentActivity).isStartAnd();
                boolean parentIsJoin = (parentActivity instanceof BPMAndRouter)
                        && !((BPMAndRouter) parentActivity).isStartAnd();
                boolean childIsSplit = (childActivity instanceof BPMAndRouter)
                        && ((BPMAndRouter) childActivity).isStartAnd();
                boolean childIsJoin = (childActivity instanceof BPMAndRouter)
                        && !((BPMAndRouter) childActivity).isStartAnd();
                boolean parentIsHumen = parentActivity instanceof BPMHumenActivity;
                boolean childIsHumen = childActivity instanceof BPMHumenActivity;

                if ((!parentIsSplit && !childIsJoin) || parentIsHumen || childIsHumen
                        || (parentIsSplit && childIsSplit) || (parentIsJoin && childIsJoin)) {
                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                    BPMTransition newTran = new BPMTransition(parentActivity, childActivity);
                    process.addLink(newTran);
                } else if (parentIsSplit && childIsJoin && parentActivity.getDownTransitions().size() == 2) {
                    BPMTransition _splitParentTran = (BPMTransition) parentActivity.getUpTransitions().get(0);
                    BPMAbstractNode _parentActivity = (BPMAbstractNode) _splitParentTran.getFrom();
                    BPMAbstractNode _splitChildActivity = null;
                    List<BPMTransition> _splitChildTranList = parentActivity.getDownTransitions();
                    for (int i = 0; i < _splitChildTranList.size(); i++) {
                        BPMTransition tran = _splitChildTranList.get(i);
                        if (!(tran.getId()).equals(upTran.getId())) {
                            _splitChildActivity = (BPMAbstractNode) tran.getTo();
                            process.removeLink(tran);
                        }
                    }

                    BPMTransition _joinChildTran = (BPMTransition) childActivity.getDownTransitions().get(0);
                    BPMAbstractNode _joinchildActivity = (BPMAbstractNode) _joinChildTran.getTo();
                    BPMAbstractNode _joinParentActivity = null;
                    List<BPMTransition> _joinUpTranList = childActivity.getUpTransitions();
                    for (int i = 0; i < _joinUpTranList.size(); i++) {
                        BPMTransition tran = _joinUpTranList.get(i);
                        if (!(tran.getId()).equals(downTran.getId())) {
                            _joinParentActivity = (BPMAbstractNode) tran.getFrom();
                            process.removeLink(tran);
                        }
                    }

                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                    process.removeChild(parentActivity);
                    process.removeChild(childActivity);
                    process.removeLink(_splitParentTran);
                    process.removeLink(_joinChildTran);

                    process.addLink(new BPMTransition(_parentActivity, _splitChildActivity));
                    process.addLink(new BPMTransition(_joinParentActivity, _joinchildActivity));
                } else {
                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                }
            }
            if(Strings.isNotEmpty(clinksUp)){
            	for(BPMCircleTransition clink : clinksUp){
            		process.removeClink(clink);
            	}
            }
            if(Strings.isNotEmpty(clinksDown)){
            	for(BPMCircleTransition clink : clinksDown){
            		process.removeClink(clink);
            	}
            }
            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            process.setModifyUser(userId);
            if (orginalReadyObject != null) {
                List<BPMActivity> readyActivityList = orginalReadyObject.getActivityList();
                for (int i = 0; i < readyActivityList.size(); i++) {
                    BPMActivity node = readyActivityList.get(i);
                    if (node.getId().equals(activityId)) {
                        readyActivityList.remove(i);
                        break;
                    }
                }
            }
            String orginalReadyObjectJson = ReadyObjectUtil.readyObjectToJSON(orginalReadyObject);
            String processXml = process.toXML(theCase,true);
            String[] result = new String[] { processXml, orginalReadyObjectJson };
            return result;
        } catch (Throwable e) {
            log.error("删除节点操作异常" + e.getMessage(), e);
            throw new BPMException("do delete node action error", e);
        }
    }

    /**
     * 
     * @param process
     * @param manualMap
     * @throws BPMException
     */
    private void setActivityManualSelect(BPMProcess process, Map<String, String[]> manualMap) throws BPMException {
        try {
            Iterator iter = manualMap.keySet().iterator();

            while (iter.hasNext()) {
                String nodeId = (String) iter.next();
                String[] manualSelect = manualMap.get(nodeId);
                String actorStr = "";
                int i = 0;
                for (String selectorId : manualSelect) {
                    if (i == 0) {
                        actorStr = selectorId + ",";
                    } else {
                        actorStr += selectorId + ",";
                    }
                    i++;
                }
                BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(nodeId);
                if (activity != null) {
                    List<BPMActor> actors = activity.getActorList();
                    BPMActor actor = actors.get(0);
                    actor.getParty().setAddition(actorStr);
                }
            }

        } catch (Throwable e) {
            log.error("动态设置节点人员异常" + e.getMessage(), e);
            throw new BPMException("do dynamic setting node's staff error", e);
        }
    }

    /**
     * 删除待办节点
     * @param process
     * @param activityId
     * @param people
     * @param userId
     * @param caseId
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    private String[] delNoActivationNode1(BPMProcess process, String activityId, List<Map<String, Object>> people,
            String userId, String caseId, ReadyObject _readyObject,BPMCase theCase) throws BPMException {
        try {
            BPMActivity currentActivity = process.getActivityById(activityId);
            List<BPMTransition> linksDown = currentActivity.getDownTransitions();
            List<BPMTransition> linksUp = currentActivity.getUpTransitions();

            List<BPMCircleTransition> clinksDown = currentActivity.getDownCirlcleTransitions();
            List<BPMCircleTransition> clinksUp = currentActivity.getUpCircleTransitions();
            
            BPMTransition upTran = (BPMTransition) linksUp.get(0);
            BPMTransition downTran = (BPMTransition) linksDown.get(0);
            BPMAbstractNode parentActivity = (BPMAbstractNode) upTran.getFrom();
            BPMAbstractNode childActivity = (BPMAbstractNode) downTran.getTo();
            //将加入的人设为ready状�态
            List<BPMActivity> readyActivityList = new ArrayList<BPMActivity>();
            if (childActivity.getBPMObjectType() != ObjectName.BPMEnd
                    && ((childActivity instanceof BPMAndRouter) && ((BPMAndRouter) childActivity).isStartAnd() || childActivity instanceof BPMHumenActivity)) {
                List<BPMTransition> _linksDown = null;
                if ((childActivity instanceof BPMAndRouter) && ((BPMAndRouter) childActivity).isStartAnd()) {
                    _linksDown = childActivity.getDownTransitions();
                } else {
                    _linksDown = linksDown;
                }
                for (BPMTransition tran : _linksDown) {
                    readyActivityList.add((BPMActivity) tran.getTo());
                }

                ReadyObject readyObject = new ReadyObject();
                readyObject.setActivityList(readyActivityList);
                readyObject.setCaseId(caseId + "");
                readyObject.setProcessId(process.getId());
                readyObject.setUserId(userId);
                readyObject.setSaveTheCaseFlag(false);
                _readyObject = mergeReadyObject(readyObject, _readyObject);
            }

            if (childActivity.getBPMObjectType() == ObjectName.BPMEnd) {
                _readyObject = setPreDelActivity(_readyObject, process, caseId, currentActivity, userId, true);
                process.removeLink(upTran);
                process.removeLink(downTran);
                process.removeChild(currentActivity);
                BPMTransition newTran = new BPMTransition(parentActivity, childActivity);
                process.addLink(newTran);
            } else {
                boolean parentIsSplit = (parentActivity instanceof BPMAndRouter)
                        && ((BPMAndRouter) parentActivity).isStartAnd();
                boolean parentIsJoin = (parentActivity instanceof BPMAndRouter)
                        && !((BPMAndRouter) parentActivity).isStartAnd();
                boolean childIsSplit = (childActivity instanceof BPMAndRouter)
                        && ((BPMAndRouter) childActivity).isStartAnd();
                boolean childIsJoin = (childActivity instanceof BPMAndRouter)
                        && !((BPMAndRouter) childActivity).isStartAnd();
                boolean parentIsHumen = parentActivity instanceof BPMHumenActivity;
                boolean childIsHumen = childActivity instanceof BPMHumenActivity;

                if ((!parentIsSplit && !childIsJoin) || parentIsHumen || childIsHumen
                        || (parentIsSplit && childIsSplit) || (parentIsJoin && childIsJoin)) {
                    _readyObject = setPreDelActivity(_readyObject, process, caseId, currentActivity, userId, false);
                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                    BPMTransition newTran = new BPMTransition(parentActivity, childActivity);
                    process.addLink(newTran);
                } else if (parentIsSplit && childIsJoin && parentActivity.getDownTransitions().size() == 2) {
                    _readyObject = setPreDelActivity(_readyObject, process, caseId, currentActivity, userId, false);
                    BPMTransition _splitParentTran = (BPMTransition) parentActivity.getUpTransitions().get(0);
                    BPMAbstractNode _parentAbstractNode = (BPMAbstractNode) _splitParentTran.getFrom();
                    BPMTransition _joinChildTran = (BPMTransition) childActivity.getDownTransitions().get(0);
                    BPMAbstractNode _joinchildActivity = (BPMAbstractNode) _joinChildTran.getTo();
                    List<BPMTransition> _splitChildTranList = parentActivity.getDownTransitions();
                    BPMAbstractNode siblingActivity = null;
                    for (int i = 0; i < _splitChildTranList.size(); i++) {
                        BPMTransition tran = _splitChildTranList.get(i);
                        if (!(tran.getId()).equals(upTran.getId())) {
                            siblingActivity = tran.getTo();
                        }
                    }
                    //兄弟节点也要删除，比如：没有选中的分支
                    String isDelete= WorkflowUtil.getNodeConditionFromCase(theCase, siblingActivity, "isDelete");
                    boolean delSiblingActivity = "true".equalsIgnoreCase(isDelete);
                    //删除兄弟节点上面的线
                    process.removeLink((BPMTransition) siblingActivity.getUpTransitions().get(0));
                    //删除兄弟节点，并把split前的节点和join后的节点连接起来
                    if (delSiblingActivity) {
                        process.removeChild(siblingActivity);
                        // 删除兄弟节点下面的线, 直到join节点
                        deleteFromNodeToAnotherNode(process, siblingActivity, childActivity);
                        BPMAbstractNode childActivityChild = ((BPMTransition) childActivity.getDownTransitions().get(0))
                                .getTo();
                        _readyObject = setPreDelActivity(_readyObject, process, caseId, (BPMActivity) childActivity,
                                userId, childActivityChild.getBPMObjectType() == ObjectName.BPMEnd);
                        process.addLink(new BPMTransition(_parentAbstractNode, _joinchildActivity));
                        //激活下一节点
                        List<BPMActivity> readyActivityList1 = new ArrayList<BPMActivity>();
                        if ((_joinchildActivity instanceof BPMAndRouter)
                                && ((BPMAndRouter) _joinchildActivity).isStartAnd()) {
                            List<BPMTransition> _linksDown1 = _joinchildActivity.getDownTransitions();
                            for (BPMTransition tran : _linksDown1) {
                                readyActivityList1.add((BPMActivity) tran.getTo());
                            }
                        } else if (_joinchildActivity instanceof BPMHumenActivity) {
                            readyActivityList1.add((BPMActivity) _joinchildActivity);
                        }
                        ReadyObject readyObject = new ReadyObject();
                        readyObject.setActivityList(readyActivityList1);
                        readyObject.setCaseId(caseId + "");
                        readyObject.setProcessId(process.getId());
                        readyObject.setUserId(userId);
                        readyObject.setSaveTheCaseFlag(false);
                        _readyObject = mergeReadyObject(readyObject, _readyObject);
                    } else {
                        //兄弟分支的最末一个节点
                        BPMAbstractNode siblingActivityEndNode = null;
                        List<BPMTransition> _childActivityUpTransition = childActivity.getUpTransitions();
                        for (BPMTransition transition : _childActivityUpTransition) {
                            if (!transition.equals(downTran)) {
                                siblingActivityEndNode = transition.getFrom();
                                break;
                            }
                        }
                        List<BPMTransition> siblingActivityEndNodeDowns = siblingActivityEndNode.getDownTransitions();
                        process.removeLink((BPMTransition) siblingActivityEndNodeDowns.get(0));
                        // 把split前的节点和兄弟节点连接
                        // 把join后的节点和兄弟分支的最末一个节点连接
                        process.addLink(new BPMTransition(_parentAbstractNode, siblingActivity));
                        process.addLink(new BPMTransition(siblingActivityEndNode, _joinchildActivity));
                    }
                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                    process.removeChild(parentActivity);
                    process.removeChild(childActivity);
                    process.removeLink(_splitParentTran);
                    process.removeLink(_joinChildTran);
                } else {
                    //pe.deleteActivity(userId, process, theCase, currentActivity, true);
                    _readyObject = setPreDelActivity(_readyObject, process, caseId, currentActivity, userId, false);
                    process.removeLink(upTran);
                    process.removeLink(downTran);
                    process.removeChild(currentActivity);
                }
            }
            if(Strings.isNotEmpty(clinksUp)){
            	for(BPMCircleTransition clink : clinksUp){
            		process.removeClink(clink);
            	}
            }
            if(Strings.isNotEmpty(clinksDown)){
            	for(BPMCircleTransition clink : clinksDown){
            		process.removeClink(clink);
            	}
            }
            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            process.setModifyUser(userId);
            String processXml = process.toXML(theCase, true);
            String readyObjectJson = ReadyObjectUtil.readyObjectToJSON(_readyObject);
            String[] result = new String[] { processXml, readyObjectJson };
            return result;
        } catch (Throwable e) {
            log.error("删除节点操作异常" + e.getMessage(), e);
            throw new BPMException("do delete node action error", e);
        }
    }

    /**
     * 
     * @param orginalReadyObject
     * @param process
     * @param caseId
     * @param activity
     * @param userId
     * @param frontadFlag
     * @return
     * @throws BPMException
     */
    private ReadyObject setPreDelActivity(ReadyObject _readyObject, BPMProcess process, String caseId,
            BPMActivity activity, String userId, boolean frontadFlag) throws BPMException {
        if (_readyObject != null) {
            List<BPMActivity> reDelActivitys = _readyObject.getPreDelActivityList();
            if (reDelActivitys != null) {
                reDelActivitys.remove(activity);
            }

            List<BPMActivity> activitys = _readyObject.getActivityList();
            if (activitys != null) {
                activitys.remove(activity);
            }
        }
        List<BPMActivity> list = new ArrayList<BPMActivity>();
        if (!frontadFlag)
            activity.setFrontadFlag(frontadFlag);
        list.add(activity);
        ReadyObject readyObject = new ReadyObject();
        readyObject.setPreDelActivityList(list);
        readyObject.setCaseId(caseId + "");
        readyObject.setProcessId(process.getId());
        readyObject.setUserId(userId);
        readyObject.setSaveTheCaseFlag(false);
        ReadyObject readyObjectNew = mergeReadyObject(readyObject, _readyObject);
        return readyObjectNew;
    }

    /**
     * 
     * @param process
     * @param fromNode
     * @param toNode
     */
    private void deleteFromNodeToAnotherNode(BPMProcess process, BPMAbstractNode fromNode, BPMAbstractNode toNode) {
        List<BPMTransition> downs = fromNode.getDownTransitions();
        for (int i = 0; i < downs.size(); i++) {
            BPMTransition down = downs.get(i);
            process.removeLink(down);
            if (!down.getTo().equals(toNode)) {
                deleteFromNodeToAnotherNode(process, down.getTo(), toNode);
            }
        }
    }

    /**
     * 合并新老ReadyObject对象
     * @param processId
     * @param readyObject
     * @param _readyObject
     * @return
     * @throws BPMException
     */
    private ReadyObject mergeReadyObject(ReadyObject readyObject, ReadyObject _readyObject) throws BPMException {
        if (_readyObject != null) {
            //待激活的新添加节点
            List<BPMActivity> _readyActivityList = _readyObject.getActivityList();
            List<BPMActivity> readyActivityList = readyObject.getActivityList();
            if (_readyActivityList == null) {
                if (readyActivityList != null) {
                    _readyObject.setActivityList(readyActivityList);
                }
            } else {
                if (readyActivityList != null) {
                    _readyActivityList.addAll(readyActivityList);
                    _readyObject.setActivityList(_readyActivityList);
                }
            }
            //待删除待办节点
            List<BPMActivity> _readyDelActivityList = _readyObject.getPreDelActivityList();
            List<BPMActivity> readyDelActivityList = readyObject.getPreDelActivityList();
            if (_readyDelActivityList == null) {
                if (readyDelActivityList != null) {
                    _readyObject.setPreDelActivityList(readyDelActivityList);
                }
            } else {
                if (readyDelActivityList != null) {
                    _readyDelActivityList.addAll(readyDelActivityList);
                    _readyObject.setPreDelActivityList(_readyDelActivityList);
                }
            }
            //是否需要更新theCase
            boolean isSaveCase = readyObject.isSaveTheCaseFlag();
            if (isSaveCase) {
                _readyObject.setSaveTheCaseFlag(isSaveCase);
            }
            return _readyObject;
        } else {
            return readyObject;
        }
    }

    @Override
    public void updateProcessState(String processId, int state) throws BPMException {
        processRunningDao.updateProcessState(processId, state);
    }

    @Override
    public BPMProcess saveOrUpdateProcessByXML(BPMProcess process, String processId, Map addition, Map condition,String startUserId,String startUserName,String startUserLoginAccountId) throws BPMException {
        boolean isNewProcess = (processId == null);
        if (isNewProcess) {
            processId = UUIDLong.longUUID() + "";
        }
        BPMStatus start = process.getStart();
        start.setName(startUserName);
        BPMActor startUserActor = new BPMActor(startUserId, startUserName, new BPMParticipantType("user"), "roleadmin",
                BPMActor.CONDITION_OR, false, startUserLoginAccountId);
        List<BPMActor> actorList = new ArrayList<BPMActor>();
        actorList.add(startUserActor);
        start.setActorList(actorList);

        //根据xml更新process
        process.setId(processId);
        process.setIndex(processId);
        process.setName(processId);
        Date now = new Date(System.currentTimeMillis());
        if (isNewProcess) {
            process.setCreateDate(now);
            process.setUpdateDate(now);
        } else {
            process.setUpdateDate(now);
        }
        return process;
    }

    @Override
    public void changeProcess4Newflow(BPMProcess process,WorkflowBpmContext context) throws BPMException {
        try {
            String startUserId= context.getStartUserId();
            String startUserName= context.getStartUserName();
            String startUserLoginAccountId= context.getStartAccountId();
            BPMStatus startNode = process.getStart();
            startNode.getSeeyonPolicy().setNF("1");
            @SuppressWarnings("rawtypes")
            List downTransitions = startNode.getDownTransitions();
            String formApp = "";
            String formViewOperation = "";
            BPMSeeyonPolicy seeyonPolicy = startNode.getSeeyonPolicy();
            if (seeyonPolicy != null) {
                formApp = seeyonPolicy.getFormApp();
                formViewOperation = seeyonPolicy.getFormViewOperation();
            }
            BPMAbstractNode userNode = new BPMHumenActivity(UUIDLong.longUUID() + "", startUserName);
            BPMParticipantType type = new BPMParticipantType("user");
            String roleName = "roleadmin";
            BPMActor userActor = new BPMActor(startUserId + "", startUserName, type, roleName,
                    BPMActor.CONDITION_OR, false, startUserLoginAccountId);
            WorkflowUtil.putNodeAdditionToContext(context, userNode.getId(), userActor.getParty(), "addition", startUserId);
            userNode.addActor(userActor);
            //设置节点的SeeyonPolicy属性
            BPMSeeyonPolicy policy = new BPMSeeyonPolicy("collaboration", "协同");
            policy.setFormApp(formApp);
            policy.setFormViewOperation(formViewOperation);
            policy.setProcessMode("single");
            policy.setSystemAdd("1");//标识为系统添加的节点
            policy.setDR(process.getStart().getSeeyonPolicy().getDR());
            userNode.setSeeyonPolicy(policy);

            BPMTransition userLink = new BPMTransition(startNode, userNode);
            process.addChild(userNode);
            process.addLink(userLink);
            if (downTransitions != null) {
                for (int i = 0; i < downTransitions.size(); i++) {
                    BPMTransition trans = (BPMTransition) downTransitions.get(i);
                    BPMAbstractNode to = trans.getTo();
                    BPMTransition userLink1 = new BPMTransition(userNode, to);
                    process.addLink(userLink1);
                    //如果原来有分支，复制到新的link中
                    userLink1.setConditionType(trans.getConditionType());
                    userLink1.setConditionBase(trans.getConditionBase());
                    userLink1.setConditionId(trans.getConditionId());
                    userLink1.setConditionTitle(trans.getConditionTitle());
                    userLink1.setFormCondition(trans.getFormCondition());
                    userLink1.setIsForce(trans.getIsForce());
                    userLink1.setDesc(trans.getDesc());
                    process.removeLink(trans);
                    startNode.removeDownTransition(trans);
                }
            }
            process.setUpdateDate(new Date());
            String isShowShortName = "false";
            if ("false".equals(isShowShortName)) {
                if ("false".equals(process.getIsShowShortName())) {
                    process.setIsShowShortName(isShowShortName);
                }
            } else {
                process.setIsShowShortName(isShowShortName);
            }
        } catch (Exception e) {
            
            // 触发流程在开始节点后面加签开始节点操作异常
            throw new BPMException("add StartNode after the StartNode when starting process error", e);
        }
    }

    /**
     * 将子流程设置数据添加到子流程运行表中.
     * @param context
     * @throws BPMException
     */
    @Override
    public boolean copySubProcessFromSettingToRunning(String processId, String processTemplateId, long caseId,
            String bussinessId, String appName) throws BPMException {
        String caseIdString = String.valueOf(caseId);
        List<String> nodeIds = null;
        List<SubProcessSetting> subSettingList = subProcessManager.getAllSubProcessSettingByTemplateId(
                processTemplateId, nodeIds);
        if (subSettingList != null && subSettingList.size() > 0) {
            List<SubProcessRunning> subRunningList = new ArrayList<SubProcessRunning>();
            for (SubProcessSetting subSet : subSettingList) {
                SubProcessRunning subRun = new SubProcessRunning();
                subRun.setId(UUIDLong.longUUID());
                subRun.setSubject(subSet.getSubject());
                subRun.setBussinessId(bussinessId);
                subRun.setBussinessType(appName);
                subRun.setConditionBase(subSet.getConditionBase());
                subRun.setConditionTitle(subSet.getConditionTitle());
                subRun.setCreateTime(new Date(System.currentTimeMillis()));
                subRun.setFlowRelateType(subSet.getFlowRelateType());
                subRun.setIsActivate(false);
                subRun.setIsCanViewByMainFlow(subSet.getIsCanViewByMainFlow());
                subRun.setIsCanViewMainFlow(subSet.getIsCanViewMainFlow());
                subRun.setIsDelete(false);
                subRun.setIsForce(subSet.getIsForce());
                subRun.setMainCaseId(caseId);
                subRun.setMainNodeId(subSet.getNodeId());
                subRun.setMainProcessId(processId);
                subRun.setMainTempleteId(subSet.getTempleteId());
                subRun.setTriggerCondition(subSet.getTriggerCondition());
                subRun.setSubProcessSettingId(subSet.getId());
                subRun.setSubProcessTempleteId(subSet.getNewflowTempleteId());
                subRun.setSubProcessSender(subSet.getNewflowSender());
                subRunningList.add(subRun);
            }
            subProcessManager.saveSubProcessRunning(subRunningList);
            return true;
        }else{
            return false;
        }
    }

    @Override 
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action) throws BPMException {
        return this.lockWorkflowProcess(processId, userId, isLock, action, "");
    }
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action,String from) throws BPMException {
    	return lockWorkflowProcess(processId, userId, isLock, action, from, false);
    }
    @Override 
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action,String from,boolean useNowexpirationTime) throws BPMException {
        if(null==processId
                || "".equals(processId.trim())
                || "-1".equals(processId.trim())
                || "0".equals(processId.trim())
                || "null".equals(processId.trim())
                || "undefined".equals(processId.trim())
                || null==userId || "".equals(userId.trim())){
            return new String[]{"true","",""};
        }
        Long _processId = Long.parseLong(processId);
        String[] result= new String[3];
        result[2]= "";
        com.seeyon.ctp.common.authenticate.domain.User currentUser= AppContext.getCurrentUser();
        if (lockManager.check(new Long(userId), _processId)){
        	//自己的对当前资源的锁
            List<Lock> lks = getLockObject(_processId, userId);
            //其他人对当前资源的锁
            List<Lock> otherLocks = getOtherLocks(_processId,userId);
            if( null==otherLocks || otherLocks.isEmpty() ){
	            if(action==0 || action==1 || action==2){//督办、管理员、发起人修改流程的操作
	                if(lks.size()>0){
	                    Lock lock= lks.get(0);
	                    long myOwnerInt= lock.getOwner();
	                    int myActionInt= lock.getAction();
	                    String myOwner= String.valueOf(myOwnerInt);
	                    String myAction= String.valueOf(myActionInt);

	                    //重入锁判断
	                    if(!(userId.equals(String.valueOf(myOwnerInt))
	                            && myAction.equals(String.valueOf(action))
	                            && (Strings.isBlank(from) || lock.getFrom().equals(from)))){

	                        result[0]= "false";
	                        User user= processOrgManager.getUserById(myOwner,false);
	                        result[1]= getLockMsg(myAction, user.getName(), lock.getFrom(),lock.getLockTime());//国际化处理
	                        result[2]= user.getName();
	                        return result;
	                    }
	                }
	                if(isLock){
	                	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
	                	result[0] = String.valueOf(lockState);
		                if(lockState){
		                	result[1] = "";//国际化处理
		                }else{
		                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
		                }
	                }else{
	                	result[0]= "true";
		                result[1]= "";//国际化处理
	                }
	                return result;
	            }else{//其他操作
	                for(int i=0;i<lks.size();i++){
	                    Lock lock= lks.get(i);
	                    long myOwnerInt= lock.getOwner();
	                    int myActionInt= lock.getAction();
	                    String myOwner= String.valueOf(myOwnerInt);
	                    String myAction= String.valueOf(myActionInt);
	                    boolean isUnitOrGroupAdmin= (currentUser.isAdministrator() ||  currentUser.isGroupAdmin());//集团管理员、单位管理员

	                    if( isUnitOrGroupAdmin && myOwner.equals(userId)){//管理员可以再已加锁基础上再加终止和撤销流程锁

	                    }else{//非管理员
	                        if( myActionInt==0 || myActionInt==1 || myActionInt==2 || myActionInt==20){
	                            result[0]= "false";
	                            User user= processOrgManager.getUserById(myOwner,false);
	                            result[1]= getLockMsg(myAction, user.getName(), lock.getFrom(),lock.getLockTime());//国际化处理
	                            result[2]= user.getName();
	                            return result;
	                        }
	                    }
	                }
	                if(isLock){
	                	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
	                	result[0] = String.valueOf(lockState);
		                if(lockState){
		                	result[1] = "";//国际化处理
		                }else{
		                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
		                }
	                }else{
	                	result[0]= "true";
		                result[1]= "";//国际化处理
	                }
	                return result;
	            }
            }else{//别人也占有锁
            	boolean isCanGet= true;
            	int otherAction= 0;
                long otherOwner= -1;
                String otherFrom = "";
                Long otherLockTime = 0L;
            	for (Lock lock : otherLocks) {
					otherAction= lock.getAction();
                    otherOwner= lock.getOwner();
                    otherFrom = lock.getFrom();
                    otherLockTime = lock.getLockTime();
                    
                    
                    // 允许重入锁
                    String myOwner= String.valueOf(otherOwner);
                    if(myOwner.equals(userId) && otherAction == action){
                        continue;
                    }
                    
					if(otherAction==0 || otherAction==1 || otherAction==2 || otherAction==20){//0:督办修改流程、1:单位管理员修改流程、2:发起者修改流程
						if( action==0 || action==1 || action==2 || action==20
								|| action==3 || action==4 || action==5 || action==6  || action==7 || action==8
								|| action==9 || action==10 || action==11 || action==12  || action==13
								|| action==14
								){
							isCanGet= false;
						}
					} else if(otherAction==3 || otherAction==4 || otherAction==5 || otherAction==6  || otherAction==7 || otherAction==8){//:加签、4:减签、5:当前会签、6:知会、7:传阅、8:多级会签
						if( action==0 || action==1 || action==2 || action==20
								|| action==9 || action==10 || action==11 || action==12  || action==13
								){
							isCanGet= false;
						}
					} else if(otherAction==9 || otherAction==10 || otherAction==11 || otherAction==12  || otherAction==13){//9：回退、10：指定回退、11：终止、12：撤销、13：取回
						if( action==0 || action==1 || action==2 || action==20
								|| action==3 || action==4 || action==5 || action==6  || action==7 || action==8
								|| action==9 || action==10 || action==11 || action==12  || action==13
								|| action==14){
							isCanGet= false;
						}
					} else if(otherAction==14 ){//流程提交锁
						if( action==0 || action==1 || action==2 || action==20
								|| action==9 || action==10 || action==11 || action==12  || action==13
								|| action==14){
							isCanGet= false;
						}
					} else if(otherAction==15  || otherAction==16 || otherAction==-1){//流程之外的锁

					}
					if(!isCanGet){
						break;
					}
				}
            	if(!isCanGet){
            		String myAction= String.valueOf(otherAction);
            		String myOwner= String.valueOf(otherOwner);
					User user= processOrgManager.getUserById(myOwner,false);
                    result[0]= "false";
                    result[1]= getLockMsg(myAction, user.getName(), otherFrom,otherLockTime);//国际化处理
                    result[2]= user.getName();
                    return result;
				}else{
					if(isLock){
	                	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
	                	result[0] = String.valueOf(lockState);
		                if(lockState){
		                	result[1] = "";//国际化处理
		                }else{
		                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
		                }
	                }else{
	                	result[0]= "true";
		                result[1]= "";//国际化处理
	                }
                    return result;
				}
            }
        }else{
            //取出锁的拥有者，没有返回null
            Lock[] lks = getLockObjects(_processId);
            if(null!=lks){
                if(action==14){//提交申请
                    if(null==lks[1] && null!=lks[0]){//别人只拥有加签之类的锁，则当前人员可以申请到提交锁，否则不行
                        if(!isLock){//校验锁，则通过
                            result[0]= "true";
                            result[1]= "";//国际化处理
                            return result;
                        }else{//加锁，则加上提交锁
                        	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
        	                result[0] = String.valueOf(lockState);
        	                if(lockState){
        	                	result[1] = "";//国际化处理
        	                }else{
        	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
        	                }
                            return result;
                        }
                    }else if(null!=lks[1]
                                       &&
                            (lks[1].getAction()==15  || lks[1].getAction()==16 || lks[1].getAction()==-1)
                            ){//别人只拥有除流程之外的锁，则当前人员可以申请到提交锁，否则不行
                        if(!isLock){//校验锁，则通过
                            result[0]= "true";
                            result[1]= "";//国际化处理
                            return result;
                        }else{//加锁，则加上提交锁
                        	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
        	                result[0] = String.valueOf(lockState);
        	                if(lockState){
        	                	result[1] = "";//国际化处理
        	                }else{
        	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
        	                }
                            return result;
                        }
                    }else{
                        Lock locker = getLockInfo(lks);
                        String myAction = locker == null ? "" : String.valueOf(locker.getAction());
                        String myOwner = locker == null ? "" : String.valueOf(locker.getOwner());
                        String myFrom = locker == null ? "" : String.valueOf(locker.getFrom());
                        Long myLockTime = locker == null ? 0:locker.getLockTime();
                        
                     // 允许重入锁
                        if(myOwner.equals(userId) && myAction.equals(String.valueOf(action))){
                            myOwner = null;
                        }
                        
                        if(Strings.isNotBlank(myOwner)){
                            User user= processOrgManager.getUserById(myOwner,false);
                            result[0]= "false";
                            result[1]= getLockMsg(myAction, user.getName(), myFrom,myLockTime);//国际化处理
                            result[2]= user.getName();
                            return result;
                        }else{
                            log.warn("_processId:="+_processId+";userId:="+userId+";action:="+action+";isLock:="+isLock+"myOwner:="+myOwner+";myAction:="+myAction);
                            if(!isLock){//校验锁，则通过
                                result[0]= "true";
                                result[1]= "";//国际化处理
                                return result;
                            }else{//加锁，则加上提交锁
                                lockManager.unlock(_processId);
                                boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
            	                result[0] = String.valueOf(lockState);
            	                if(lockState){
            	                	result[1] = "";//国际化处理
            	                }else{
            	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
            	                }
                                return result;
                            }
                        }
                    }
                }else if(action==3 || action==4 || action==5
                        || action==6  || action==7 || action==8){
                    if( lks[0]==null && null!=lks[1] && (lks[1].getAction()==14 || lks[1].getAction()==15  || lks[1].getAction()==16 || lks[1].getAction()==-1)){
                        if(!isLock){//校验锁，则通过
                            result[0]= "true";
                            result[1]= "";//国际化处理
                            return result;
                        }else{//加锁，则加上提交锁
                        	boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
        	                result[0] = String.valueOf(lockState);
        	                if(lockState){
        	                	result[1] = "";//国际化处理
        	                }else{
        	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
        	                }
                            return result;
                        }
                    }else{
                    	Lock locker = getLockInfo(lks);
                        String myAction= locker == null ? "" :  String.valueOf(locker.getAction());
                        String myOwner= locker == null ? "" :  String.valueOf(locker.getOwner());
                        String myFrom = locker == null ? "" :  String.valueOf(locker.getFrom());
                        Long myLockTime = locker == null ? 0 : locker.getLockTime();

                        if(Strings.isNotBlank(myOwner)){
                            result[0]= "false";
                            User user= processOrgManager.getUserById(myOwner,false);
                            result[1]= getLockMsg(myAction, user.getName(), myFrom,myLockTime);//国际化处理
                            result[2]= user.getName();
                            return result;
                        }else{
                            log.warn("_processId:="+_processId+";userId:="+userId+";action:="+action+";isLock:="+isLock+"myOwner:="+myOwner+";myAction:="+myAction);
                            if(!isLock){//校验锁，则通过
                                result[0]= "true";
                                result[1]= "";//国际化处理
                                return result;
                            }else{//加锁，则加上提交锁
                                lockManager.unlock(_processId);
                                boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
            	                result[0] = String.valueOf(lockState);
            	                if(lockState){
            	                	result[1] = "";//国际化处理
            	                }else{
            	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
            	                }
                                return result;
                            }
                        }
                    }
                } else{
                	Lock locker = getLockInfo(lks);
                    String myAction= locker == null ? "" :  String.valueOf(locker.getAction());
                    String myOwner= locker == null ? "" :  String.valueOf(locker.getOwner());
                    String myFrom = locker == null ? "" :  String.valueOf(locker.getFrom());
                    Long myLockTime = locker == null ? 0 : locker.getLockTime();
                    if(Strings.isNotBlank(myOwner)){
                        result[0]= "false";
                        User user= processOrgManager.getUserById(myOwner,false);
                        result[1]= getLockMsg(myAction, user.getName(), myFrom,myLockTime);//国际化处理
                        result[2]= user.getName();
                        return result;
                    }else{
                        log.warn("_processId:="+_processId+";userId:="+userId+";action:="+action+";isLock:="+isLock+"myOwner:="+myOwner+";myAction:="+myAction);
                        if(!isLock){//校验锁，则通过
                            result[0]= "true";
                            result[1]= "";//国际化处理
                            return result;
                        }else{//加锁，则加上提交锁
                            lockManager.unlock(_processId);
                            boolean lockState = lockManager.lock(new Long(userId), _processId,action,from,useNowexpirationTime);
        	                result[0] = String.valueOf(lockState);
        	                if(lockState){
        	                	result[1] = "";//国际化处理
        	                }else{
        	                	result[1]= ResourceUtil.getString("workflow.lock.error");//加锁失败
        	                }
                            return result;
                        }
                    }
                }
            }
            return result;
        }
    }

    
    private List<Lock> getOtherLocks(Long processId,String userId) {
    	List<Lock> mylocks= new ArrayList<Lock>();
        List<Lock> locks = lockManager.getLocks(processId);
        if(locks!=null && !locks.isEmpty()){
            for (Lock lk : locks) {
                if(lk!=null && lk.getOwner()!= Long.parseLong(userId)){
                    if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                        mylocks.add(lk);
                    }
                }
            }
        }
        return mylocks;
	}

	/**
     * 获取占用锁人员信息
     * 
     * @param lks
     * @return
     *
     * @Author      : xuqw
     * @Date        : 2016年6月23日下午8:36:55
     *
     */
    private Lock getLockInfo(Lock[] lks){
        Lock myLock = null;
        if(null!=lks[1]){
        	myLock = lks[1];
        }else if(null!=lks[0]){
        	myLock = lks[0];
        }
        
        return myLock;
    }
    
    /**
     * 
     * 锁提示信息
     * 
     * @param action
     * @param userName
     * @param from
     * @return
     *
     * @Author      : xuqw
     * @Date        : 2016年6月23日下午8:25:22
     *
     */
    public String getLockMsg(String action, String userName, String from,Long lockTime){
        String ret = null;
        String fromName = "";
        String lockTimeStr = "";
        if(Strings.isNotBlank(from)){
            if(Constants.login_sign.pc.toString().equals(from)//电脑端
                    || Constants.login_sign.phone.toString().equals(from)//移动端
                    || Constants.login_sign.wechat.toString().equals(from)//微信端
                    || Lock.FROM_SYSTEM.equals(from)){ //系统锁
                fromName = ResourceUtil.getString("workflow.lock.from." + from);
            }
        }
        if(null!=lockTime){
        	lockTimeStr = Datetimes.format(new Date(lockTime), Datetimes.datetimeStyle);
        }
        ret = ResourceUtil.getString("workflow.lock.action."+action, userName,fromName,lockTimeStr);//国际化处理
        
        return ret;
    }

    @Override
    public String[] releaseWorkFlowProcessLock(String processId, String userId) throws BPMException {
        
        return releaseWorkFlowProcessLock(processId,userId,null);
    }
    
    @Override
    public String[] releaseWorkFlowProcessLock(String processId, String userId,String from) throws BPMException {
        if(null==processId 
                || "".equals(processId.trim()) 
                || "-1".equals(processId.trim())
                || "0".equals(processId.trim()) 
                || "null".equals(processId.trim()) 
                || "undefined".equals(processId.trim())
                || null==userId || "".equals(userId.trim())){
            return new String[]{"true","",""};
        }
        Long _processId = Long.parseLong(processId);
        String[] result= new String[2];
        //只能解开自己加的锁
        //List<Lock> lks = getLockObject(_processId,userId);
        List<Lock> lks = getAllLockObject(_processId,userId);
        if(null!=lks && !lks.isEmpty()){
            for (Lock lk : lks) {
            	if(Strings.isNotBlank(from) ){
            		if(from.equals(lk.getFrom())){
            			lockManager.unlock(lk.getOwner(),lk.getResourceId(),lk.getAction());
            		}
            	}
            	else{
            		lockManager.unlock(lk.getOwner(),lk.getResourceId(),lk.getAction());
            	}
            }
        }
        result[0]= "true";
        result[1]= "";//国际化处理
        return result;
    }
    
    
    
    /**
     * 取出锁的拥有者
     * @param processId
     * @return
     */
    private Lock getLockObject(Long processId){
        List<Lock> locks = lockManager.getLocks(processId);
        if(locks!=null && !locks.isEmpty()){
            Lock lk = locks.get(0);
            if(lk!=null){
                if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                    return lk;
                }
            }
        }
        return null;
    }
    

    private List<Lock> getLockObject(Long processId,String userId) {
        List<Lock> mylocks= new ArrayList<Lock>();
        List<Lock> locks = lockManager.getLocks(processId);
        if(locks!=null && !locks.isEmpty()){
            for (Lock lk : locks) {
                if(lk!=null && lk.getOwner()== Long.parseLong(userId)){
                    if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                        mylocks.add(lk);
                    }
                }
            }
        }
        return mylocks;
    }
    
    private Lock[] getLockObjects(Long _processId) {
        Lock[] returnObject= new Lock[2];
        List<Lock> locks = lockManager.getLocks(_processId);
        if(locks!=null && !locks.isEmpty()){
            for (Lock lk : locks) {
                if(lk!=null){
                    if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                        if(lk.getAction()==3 || lk.getAction()==4 || lk.getAction()==5 
                                || lk.getAction()==6  || lk.getAction()==7 || lk.getAction()==8 ){//加签减签相关操作
                            returnObject[0]= lk;
                        }else{
                            returnObject[1]= lk;
                        }
                    }
                }
            }
        }
        return returnObject;
    }
    
    

    @Override
    public List<Long> getLatterActivityIds(String mainProcessId, String mainNodeId) throws BPMException {
        BPMProcess process= getRunningProcess(mainProcessId);
        BPMActivity activity= process.getActivityById(mainNodeId);
        List<Long> result = new ArrayList<Long>(); 
        if(activity != null){
            List downTransitions = activity.getDownTransitions();
            getLatterNodeIdHelper(downTransitions, result);
        }
        return result;
    }
    
    //  getLatterActivityIds辅助方法
    private static void getLatterNodeIdHelper(List downTransitions, List<Long> result)throws BPMException{
        if (downTransitions != null){
            for (int i = 0; i < downTransitions.size(); i++){
                BPMTransition trans = (BPMTransition) downTransitions.get(i);
                BPMAbstractNode to = trans.getTo();
                if(to.getNodeType().equals(BPMAbstractNode.NodeType.end)){ 
                    return;
                }
                else if(to.getNodeType().equals(BPMAbstractNode.NodeType.split) || to.getNodeType().equals(BPMAbstractNode.NodeType.join)){
                    getLatterNodeIdHelper(to.getDownTransitions(), result);
                }
                else{
                    result.add(Long.parseLong(to.getId()));
                }
            }
        }
    }

    @Override
    public String[] releaseWorkFlowProcessLock(String processId, String userId, int action) throws BPMException {
        if(null==processId 
                || "".equals(processId.trim()) 
                || "-1".equals(processId.trim())
                || "0".equals(processId.trim()) 
                || "null".equals(processId.trim()) 
                || "undefined".equals(processId.trim())
                || null==userId || "".equals(userId.trim())){
            return new String[]{"false","params error for unlock"/*解锁参数错误.*/,""};
        }
        Long _processId = Long.parseLong(processId);
        String[] result= new String[2];
        //获得当前用户拥有的该资源的所有锁
        //List<Lock> lks= getLockObject(_processId,userId);
        List<Lock> lks = getAllLockObject(_processId,userId);
        if(null!=lks){
            for (Lock lk : lks) {
                String myAction= String.valueOf(lk.getAction());
                if(action== Integer.parseInt(myAction)){
                    lockManager.unlock(lk.getOwner(),lk.getResourceId(),lk.getAction());
                }
            }
        }
        result[0]= "true";
        result[1]= "";//国际化处理
        return result;
    }
    
    private List<Lock> getAllLockObject(Long processId,String userId) {
        List<Lock> mylocks= new ArrayList<Lock>();
        List<Lock> locks = lockManager.getLocks(processId);
        if(locks!=null && !locks.isEmpty()){
            for (Lock lk : locks) {
                if(lk!=null && lk.getOwner()== Long.parseLong(userId)){
                	mylocks.add(lk);
                }
            }
        }
        return mylocks;
    }

    @Override
    public String updateRunningProcess(BPMProcess process, int finished,BPMCase theCase) throws BPMException {
        String processId = process.getId();
        process.setUpdateDate(new Date());
        process.setCreateDate(process.getCreateDate());
        processRunningDao.updateProcess(process,finished,theCase);
        return processId;
    }

    @Override
    public void saveRunningProcessWithState(BPMProcess process, int ordinal,BPMCase theCase) throws BPMException {
        //processRunningDao.updateProcessState(processId, ProcessStateEnum.processState.finished.ordinal());
        processRunningDao.saveRunningProcessWithState(process,ordinal,theCase);
    }
    
    @Override
	public String freeAddNode(String workflowXml, String orgJson, String currentNodeId, String type,
			String currentUserId, String currentUserName, String currentAccountId, String currentAccountName,
			String defaultPolicyId, String defaultPolicyName,List<BPMHumenActivity> addHumanNodes) throws BPMException {
		BPMProcess process= null;
		if(Strings.isBlank(workflowXml)){
			process= WorkflowUtil.createEmptyProcess(defaultPolicyId, defaultPolicyName, currentUserId, currentUserName, currentAccountId);
		}else{
			process= BPMProcess.fromXML(workflowXml);
		}
		BPMAbstractNode currentActivity = null;
		if(Strings.isBlank(currentNodeId)){
			currentActivity= process.getStart();
		}else{
			currentActivity= process.getActivityById(currentNodeId);
		}
		List<BPMHumenActivity> nodeList= BPMChangeUtil.createBPMHumenActivityList(orgJson, defaultPolicyId, defaultPolicyName);
		if(addHumanNodes != null){
		    addHumanNodes.clear();
		    addHumanNodes.addAll(nodeList);
        }		
		
		if(Strings.isNotBlank(orgJson)){
			if("0".equals(type)){//0：串发
				BPMChangeUtil.serialAddNode(process, currentActivity, nodeList);
			}else if("1".equals(type)){//1：并发
				if(nodeList.size()==1){
					BPMChangeUtil.serialAddNode(process, currentActivity, nodeList);
				}else{
					BPMChangeUtil.parallelAddNode(process, currentActivity, nodeList);
				}
			}else if("2".equals(type)){//2：会签
				BPMChangeUtil.assignNode(process, currentActivity, nodeList);
			}
		}
		
		String processXml= process.toXML(null,true);
		
		return processXml;
	}
    
    /**
	 * 
	 * @param process
	 * @param currentNode
	 * @return
	 */
	public String freeDeleteNode(String workflowXml, String currentNodeId) throws BPMException {
		BPMProcess process= BPMProcess.fromXML(workflowXml);
		BPMAbstractNode currentNode= process.getActivityById(currentNodeId);
		//不是人工节点不允许减签
        if (currentNode instanceof BPMHumenActivity) {
            //人中节点只可能有一个父节点，也只可能有一个子节点
            BPMTransition aup = (BPMTransition) currentNode.getUpTransitions().get(0);
            BPMTransition adown = (BPMTransition) currentNode.getDownTransitions().get(0);
            BPMAbstractNode parent = aup.getFrom();
            BPMAbstractNode child = adown.getTo();
            //如果父节点是split且子节点是join
            if ((parent instanceof BPMAndRouter) && (child instanceof BPMAndRouter)) {
                BPMAndRouter parent1 = (BPMAndRouter) parent;
                BPMAndRouter child1 = (BPMAndRouter) child;
                if ((parent1.isStartAnd() == true) && (child1.isStartAnd() == false)) {
                    if (parent.getDownTransitions().size() >= 3) {
                        //如果split存在3个或以上的子节点，删除当前节点和up线、down线就可以
                        freeDeleteNode(process, currentNode, false);
                    } else {
                        //如果split存在两个或以下的子节点，删除当前节点和up线、down线
                        //同时要删除split节点和join节点，及split节点的up线，join节点的down线
                        //同时建立split的父节点、join节点的子节点与当前节点的并行节点的关系
                        //也即先减掉当前节点然后不建立父子节点关联，然后减掉split和join节点建立父子节点关联
                        
                        freeDeleteNode(process, currentNode, false);
                        freeDeleteNode(process, parent, true);
                        freeDeleteNode(process, child, true);
                    }
                } else {
                    freeDeleteNode(process, currentNode, true);
                }
            } else {
                freeDeleteNode(process, currentNode, true);
            }
        }
        
        String processXml= process.toXML(null,true);
        
        return processXml;
	}
	
	/**
	 * 
	 * @param process
	 * @param currentNode
	 * @param b
	 */
	private static void freeDeleteNode(BPMProcess process, BPMAbstractNode currentNode, boolean createPCLFlag) {
		BPMTransition aup = (BPMTransition) currentNode.getUpTransitions().get(0);
        BPMTransition adown = (BPMTransition) currentNode.getDownTransitions().get(0);
        if (createPCLFlag) {
            //并建立父节点与子节点的关系
            BPMAbstractNode parent = aup.getFrom();
            BPMAbstractNode child = adown.getTo();
            BPMTransition parentChild = new BPMTransition(parent, child);
            //复制分支条件
            copyCondition(adown, parentChild);
            process.addLink(parentChild);
        }
        //直接删除节点，并且删除up线和down线
        adown.getTo().removeUpTransition(adown);
        currentNode.removeDownTransition(adown);
        process.removeLink(adown);
        aup.getFrom().removeDownTransition(aup);
        currentNode.removeUpTransition(aup);
        process.removeLink(aup);
        process.removeChild(currentNode);
	}
    
    /**
	 * 
	 * @param workflowXml
	 * @param currentNodeId
	 * @param oneOrgJson
	 */
	public String[] freeReplaceNode(String workflowXml, String currentNodeId, String oneOrgJson,String defaultPolicyId,String defaultPolicyName,BPMCase theCase) 
			throws BPMException {
		try{
			if(Strings.isNotBlank(oneOrgJson)){
				
				BPMProcess process= BPMProcess.fromXML(workflowXml);
				
				List<Map<String, Object>> people = new ArrayList<Map<String, Object>>();
				
				JSONObject oneOrgJsonObj= new JSONObject(oneOrgJson);
				
				String entityId = oneOrgJsonObj.getString("id");
				String entityType= oneOrgJsonObj.getString("entityType");
                String type = processOrgManager.getUserTypeByField(entityType);
                String entityName = oneOrgJsonObj.getString("name");
                String accountId = oneOrgJsonObj.getString("accountId");
                String accountShortName = oneOrgJsonObj.getString("accountName");
                String includeChildStr= oneOrgJsonObj.getString("includeChild");
                boolean includeChild = true;
                BPMSeeyonPolicy bpmSeeyonPolicy = null;
                if (includeChildStr != null && "false".equals(includeChildStr)) {
                    includeChild = false;
                }
                bpmSeeyonPolicy = new BPMSeeyonPolicy(defaultPolicyId, defaultPolicyName);
                Map<String, Object> party = new HashMap<String, Object>();
                party.put("type", type);
                party.put("id", entityId);
                party.put("name", entityName);
                party.put("accountId", accountId);
                party.put("accountShortName", accountShortName);
                party.put("includeChild", includeChild);
                party.put("bpmSeeyonPolicy", bpmSeeyonPolicy);
                
                people.add(party);
				
                String newId = replaceActivity(process, currentNodeId, people);
				
				String processXml = transProcess2XML(process, "", theCase, true);
				
				return new String[]{processXml, newId};
			}else{
				return new String[]{workflowXml, currentNodeId};
			}
		}catch(Throwable e){
    		throw new BPMException("",e);
    	}
	}

	@Override
	public String freeChangeNodeProperty(String workflowXml, String currentNodeId, String nodePropertyJson
	        , boolean updateAll, List<String> updateNodesList,BPMCase thecase) throws BPMException {
		try{
			if(Strings.isNotBlank(nodePropertyJson)){
				JSONObject nodePropertyJsonObj= new JSONObject(nodePropertyJson);
				String policyId= nodePropertyJsonObj.optString("policyId");//节点权限ID
				String policyName= nodePropertyJsonObj.optString("policyName");//节点权限名称
				String dealTerm= nodePropertyJsonObj.optString("dealTerm");//超期时间
				String remindTime= nodePropertyJsonObj.optString("remindTime");//提醒时间
				String processMode= nodePropertyJsonObj.optString("processMode");//执行模式
				String matchScope= nodePropertyJsonObj.optString("matchScope");//匹配范围
				String desc= nodePropertyJsonObj.optString("desc");//节点描述
				String dealTermType= nodePropertyJsonObj.optString("dealTermType");//超期处理类型
				String dealTermUserId= nodePropertyJsonObj.optString("dealTermUserId");//超期转指定人ID
				String dealTermUserName= nodePropertyJsonObj.optString("dealTermUserName");//超期转指定人名称
				String includeChild= nodePropertyJsonObj.optString("includeChild");// includeChild:true/false,是否包含子部门
				String rup= nodePropertyJsonObj.optString("rup");//角色是否自动向上查找
				String pup= nodePropertyJsonObj.optString("pup");//岗位是否自动向上查找
				String na= nodePropertyJsonObj.optString("na");//无人是否自动跳过
				
				String formApp= nodePropertyJsonObj.optString("formApp");//表单视图ID(自由流程默认传空字符串)
				String formViewOperation= nodePropertyJsonObj.optString("formViewOperation");//表单视图ID(自由流程默认传空字符串)
				String formField= nodePropertyJsonObj.optString("formField");//表单字段信息(自由流程默认传空字符串)
				String tolerantModel= nodePropertyJsonObj.optString("tolerantModel");//超级节点容错处理模式(自由流程默认传空字符串)
				
		        BPMProcess process= BPMProcess.fromXML(workflowXml);
		        
		        Map<String, BPMActivity> activityMap = new HashMap<String, BPMActivity>();
		        List<String> nodeList = null;
		        boolean updatNodesIsEmpty = Strings.isNotEmpty(updateNodesList);
		        if(updateAll){
		            List<BPMActivity> activities = process.getActivitiesList();
		            nodeList = new ArrayList<String>(activities.size());
		            for(BPMActivity a : activities){
		                String aId = a.getId();
		                if(updatNodesIsEmpty){
		                    if(updateNodesList.contains(aId)){
		                        nodeList.add(aId);
		                    }
		                }else{
		                    nodeList.add(aId);
		                }
		                activityMap.put(aId, a);
		            }
		        }else {
		            nodeList = new ArrayList<String>(1);
                    nodeList.add(currentNodeId);
                    
                    List<BPMActivity> activities = process.getActivitiesList();
                    for(BPMActivity a : activities){
                    	if(a.getId().equals(currentNodeId)) {
                    		activityMap.put(currentNodeId, a);
                    		break;
                    	}
                    }
                }
		        
		        for(String nodeId : nodeList) {
		        	BPMSeeyonPolicy policy = null;
		        	if(activityMap.get(nodeId) != null) {
		        		policy = activityMap.get(nodeId).getSeeyonPolicy();
		        		policy.setId(policyId);
		        		policy.setName(policyName);
		        	} else {
		        		policy = new BPMSeeyonPolicy();
		        		policy.setId(policyId);
		                policy.setName(policyName);
		                policy.setdealTerm(dealTerm);
		                policy.setRemindTime(remindTime);
		                policy.setProcessMode(processMode);
		                policy.setMatchScope(matchScope);
		                policy.setDesc(desc);
		                policy.setDealTermType(dealTermType);
		                policy.setDealTermUserId(dealTermUserId);
		                policy.setDealTermUserName(dealTermUserName);
		                policy.setRup(rup);
		                policy.setPup(pup);
		                policy.setNa(na);
		                
		                policy.setFormField(formField);
		                policy.setFormApp(formApp);
		                policy.setFormViewOperation(formViewOperation);
		                policy.setForm("");
		                policy.setOperationName("");
		                policy.setTolerantModel(tolerantModel);
		        	}
		            
		            setActivityPolicy(process, nodeId, policy, false, includeChild, "");
		        }
		        
	            return transProcess2XML(process, "", thecase, true);
			}
			return workflowXml;
		}catch(Throwable e){
    		throw new BPMException("",e);
    	}	
	}

	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	
	public List<ProcessInRunningBLOBDAO> getProcessInRunningBLOBDAOList(int begin, int length, long upgradeTime)
	        throws BPMException
    {
        List list = this.processRunningDao.getProcessInRunningBLOBDAOList(begin, length, upgradeTime);
        return list;
    }

	@Override
	public String[] updateLockExpirationTime(String processId, String userId, int action, String loginPlatform)
			throws BPMException {
        if(Strings.isNotBlank(processId)){
            List<Lock> plocks = getLockObject(Long.valueOf(processId),userId);
            if(Strings.isEmpty(plocks)){
            	return null;
            }
            //工作流只有一种锁取第一个即可
        	Lock lock =  plocks.get(0);
            if(loginPlatform.equals(lock.getFrom()) && lock.getAction()==action){
            	lockManager.updateLockExpirationTime(lock.getResourceId(), lock.getAction(),System.currentTimeMillis());
            }
        }
		return null;
	}
}
