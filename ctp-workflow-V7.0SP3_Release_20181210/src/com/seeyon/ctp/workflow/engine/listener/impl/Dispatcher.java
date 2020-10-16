/*
 * Created on 2004-5-18
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.seeyon.ctp.workflow.engine.listener.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.workflow.engine.enums.ProcessStateEnum;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.engine.listener.ExecuteListener;
import com.seeyon.ctp.workflow.engine.log.Recorder;
import com.seeyon.ctp.workflow.event.BPMEvent;
import com.seeyon.ctp.workflow.event.Event;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.po.SubProcessRunning;
import com.seeyon.ctp.workflow.po.WorkitemDAO;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.User;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.BPMWorkItemList;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流内部事件接口任务事项调度实现类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-27 上午12:50:52
 */
public class Dispatcher implements ExecuteListener {
    
    private static Log         log           = CtpLogFactory.getLog(Dispatcher.class);
    private BPMWorkItemList    itemlist      = null;
    private ProcessManager        processManager= null;
    private WorkFlowMatchUserManager workflowMatchUserManager= null; 
    private SubProcessManager subProcessManager= null;

    /**
     * @param subProcessManager the subProcessManager to set
     */
    public void setSubProcessManager(SubProcessManager subProcessManager) {
        this.subProcessManager = subProcessManager;
    }


    /**
     * @param workflowMatchUserManager the workflowMatchUserManager to set
     */
    public void setWorkflowMatchUserManager(WorkFlowMatchUserManager workflowMatchUserManager) {
        this.workflowMatchUserManager = workflowMatchUserManager;
    }


    /**
     * @param itemlist the itemlist to set
     */
    public void setItemlist(BPMWorkItemList itemlist) {
        this.itemlist = itemlist;
    }

    public Dispatcher() {
    }
    
    @Override
    public boolean onActivityReady(String engineDomain, WorkflowBpmContext context,boolean isUseAdditonUserId) throws BPMException {
        BPMHumenActivity humenActivity = check((BPMActivity) context.getActivateNode());
        if (humenActivity == null) {
            log.info("- onActivityReady - humenActivity is null ,id:"+(context.getActivateNode() == null ? -1:context.getActivateNode().getId()));
            return true;
        }
        String seeyonPolicyName= humenActivity.getSeeyonPolicy().getName();
        if(humenActivity.getSeeyonPolicy().getName().equals(humenActivity.getSeeyonPolicy().getId())){
            seeyonPolicyName= BPMSeeyonPolicy.getShowName(humenActivity.getSeeyonPolicy().getId());
        }
        String name = humenActivity.getName()+"("+seeyonPolicyName+")";
        if(context.getNextMembers()==null){
            StringBuffer nextMembers= new StringBuffer();
            nextMembers.append(name);
            context.setNextMembers(nextMembers);
            
            StringBuffer nextMembers1= new StringBuffer();
            nextMembers1.append(humenActivity.getName());
            context.setNextMembersWithoutPolicyInfo(nextMembers1);
        }else{
            StringBuffer nextMembers= context.getNextMembers();
            nextMembers.append(",").append(name);
            context.setNextMembers(nextMembers);
            
            StringBuffer nextMembers1= context.getNextMembersWithoutPolicyInfo();
            nextMembers1.append(",").append(humenActivity.getName());
            context.setNextMembersWithoutPolicyInfo(nextMembers1);
        }
        
        BPMCase theCase = context.getTheCase();
        BPMProcess process = context.getProcess();
        List<BPMActor> actors = humenActivity.getActorList();
        if (actors == null || actors.size() == 0) {
            log.warn("没有Actor!!" + process.getId() + "," + theCase.getId() + "," + humenActivity.getId());
            return false;
        }
        BPMActor actor = actors.get(0);
        BPMParticipant party = actor.getParty();
        List<User> actorsList = humenActivity.getCandidateList();
        boolean isUserNode = false;
        if (actorsList == null || actorsList.isEmpty()) {
            List<V3xOrgMember> members= workflowMatchUserManager.getUserList(engineDomain, humenActivity, context,isUseAdditonUserId);
            //partyTypeId就是actor标签的partyType属性的值
            String partyTypeId = party.getType().id;
            String partyId = party.getId();
            if ("user".equals(partyTypeId) || WorkFlowMatchUserManager.ORGENT_META_KEY_SEDNER.equals(partyId)){
                isUserNode = true;
                actorsList= WorkflowUtil.v3xOrgMemberToWorkflowUser(members, true);
            }else{
                actorsList= WorkflowUtil.v3xOrgMemberToWorkflowUser(members, isUseAdditonUserId);
            }
        }
        if (actorsList == null || actorsList.isEmpty()) {
            log.info("没有匹配到人!!" + process.getId() + "," + theCase.getId() + "," + humenActivity.getId() + ","
                    + ((BPMActor) humenActivity.getActorList().get(0)).getParty().getId());
            
            boolean isBlankNode = WorkflowUtil.isBlankNode(humenActivity);
            boolean isJoinNode = WorkflowUtil.isJoin(humenActivity);
            boolean isSplitNode = WorkflowUtil.isSplit(humenActivity);
            boolean isManualSelect = "0".equals(humenActivity.getSeeyonPolicy().getNa());
            if(!isBlankNode && !isJoinNode && !isSplitNode && !isUserNode && isManualSelect){
                //由上节点选择执行人的这种情况。
                log.info(AppContext.currentUserName()+",没有发现执行人，请稍后重试.nodeId:"+humenActivity.getId()+",processId:"+process.getId());
                throw new BPMException ("人员匹配执行失败，请重试");
            }
            
            return false;
        }
        else{
            log.info("匹配到人,processId:"+ process.getId() + ", caseId:" + theCase.getId() + ",A:" + humenActivity.getId() + ",size:"
                    + actorsList.size());
        }
        
        //记录前端选人日志
        String cacheKey = context.getMatchRequestToken();
        Map<String, Map<String, Object>> selectPeopleParams = context.getNeedSelectPeopleNodeMap();
        workflowMatchUserManager.cacheManualSelectLog(cacheKey, process, selectPeopleParams);
        
        Object[] result = null;
        boolean fireIt= true;
        if("2.0".equals(context.getVersion())){
            result = itemlist.generateWorkItems(actorsList, humenActivity, process.getId(),theCase.getId(), engineDomain);
            fireIt= false;
        }else{
            result = itemlist.addWorkItems(actorsList, humenActivity, process.getId(),theCase.getId(), engineDomain);
            fireIt= true;
        }        
        List<String> actorStr = (List<String>) result[0];
        List<WorkitemDAO> workitems = (List<WorkitemDAO>) result[1];
        //(主要是回退的问题)
        WorkflowUtil.putNodeAdditionToContext(context, humenActivity.getId(), party, "addition",  StringUtils.join(actorStr, ","));
        WorkflowUtil.putNodeAdditionToContext(context, humenActivity.getId(), party, "raddition",  StringUtils.join(actorStr, ","));
        WorkflowUtil.putContextDynamicFormToCase(context, theCase, humenActivity.getId());
        WorkitemDAO workitem0 = workitems.get(0);
        EventDataContext edCtx= ActionRunner.RunItemEvent(BPMEvent.WORKITEM_ASSIGNED, context,workitem0,workitems,fireIt);
        if(!fireIt){
            edCtx.setWorkitemDaoList(workitems);
            context.getEventDataContextList().add(edCtx);
        }
        return true;
    }

    private BPMHumenActivity check(BPMActivity activity) {
        if (activity.getBPMObjectType() == ObjectName.BPMHumenActivity && !ObjectName.isSuperNode(activity)) {
            BPMHumenActivity humenActivity = (BPMHumenActivity) activity;
            return humenActivity;
        }
        return null;
    }

    @Override
    public boolean onActivityRemove(String engineDomain, WorkflowBpmContext context) throws BPMException {
        BPMHumenActivity humenActivity = check((BPMActivity) context.getActivateNode());
        if (humenActivity == null){
            return true;
        }
        //删除有效的可执行itemList数，modify by jincm
        humenActivity.setCandidateList(null);
        //if("2.0".equals(context.getVersion())){
        if(context.isBatchCancel()){
            context.getWillDeleteNodes().add(humenActivity.getId());
            List list = itemlist.getItemsByActivity(engineDomain, context.getTheCase().getId(), humenActivity);
            context.getWillDeleteWorkItems().addAll(list);
        }else{
            List list = itemlist.cancelItemByActivity(engineDomain, context.getTheCase().getId(), humenActivity);
            if (list != null && !list.isEmpty()){
                WorkitemInfo item = (WorkitemInfo) list.get(0);
                ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,item,list);
            }
        }
        //还原超期替换节点相关信息
        BPMActor actor = (BPMActor)humenActivity.getActorList().get(0);
        if(!"user".equals(actor.getParty().getType().id)){
        	WorkflowUtil.recoverNodeBakUserInfo(humenActivity, context);
        }
        
        BPMCase theCase = context.getTheCase();
        WorkflowUtil.removeWFDynamicFormMasterIds(context, theCase, humenActivity.getId());
        
        return true;
    }

    @Override
    public boolean onCaseFinish(String domain, WorkflowBpmContext context) throws BPMException {
    	if(!context.isStartFinished()){//流程一发起就结束，肯定不会有子流程，下面的逻辑不用执行
	        SubProcessRunning subProcessRunning= subProcessManager.getAffinedMainflow(context.getProcessId());
	        if( null!= subProcessRunning ){
	        	if(subProcessRunning.getFlowRelateType()==1){
		            List<Long> nextNodeIds = processManager.getLatterActivityIds(subProcessRunning.getMainProcessId(),subProcessRunning.getMainNodeId());
		            context.setSubProcess(true);
		            context.setMainCaseId(String.valueOf(subProcessRunning.getMainCaseId()));
		            context.setMainNextNodeIds(nextNodeIds);
	        	}
	            subProcessRunning.setIsFinished(1);
	            subProcessManager.updateSubProcessRunning(subProcessRunning);
	        }
    	}
        ActionRunner.RunItemEvent(Event.PROCESS_FINISHED, context,null,null);
        WorkflowUtil.putWorkflowBPMContextToCase(context, context.getTheCase());
        //更新流程状态为完成
        if(context.isStartFinished()){
            processManager.saveRunningProcessWithState(context.getProcess(),ProcessStateEnum.processState.finished.ordinal(),context.getTheCase());
        }else{
            if(!context.isProcessChanged()){
                processManager.finishProcessState(context.getProcessId());
            }
        }
        return true;
    }

    @Override
    public boolean onCaseCancel(String engineDomain, WorkflowBpmContext context,boolean isDeleteItem) throws BPMException {
        if(isDeleteItem){
            List list = itemlist.cancelItemByCase(engineDomain, context.getTheCase().getId()); 
        }
        if(context.isSubProcess()){//子流程
            ActionRunner.RunItemEvent(Event.SUB_PROCESS_CANCELED, context, null, null);
        }
        else{//主流程
            ActionRunner.RunItemEvent(Event.PROCESS_CANCELED, context, null, null);
        }
        return true;
    }

    @Override
    public boolean onCaseResume(String engineDomain, WorkflowBpmContext context) throws BPMException {
        List list = itemlist.suspendItemByCase(engineDomain, context.getTheCase().getId(), false);
        for (int i = 0; i < list.size(); i++) {
            WorkitemInfo item = (WorkitemInfo) list.get(i);
            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_RESUME, context,item,null);
        }
        return true;
    }

    @Override
    public boolean onCaseSuspend(String engineDomain, WorkflowBpmContext context) throws BPMException {
        List list = itemlist.suspendItemByCase(engineDomain, context.getTheCase().getId(), true);
        for (int i = 0; i < list.size(); i++) {
            WorkitemInfo item = (WorkitemInfo) list.get(i);
            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_SUSPEND, context,item,null);
        }
        return true;
    }

    @Override
    public boolean onActivityWaitingToReady(String domain, WorkflowBpmContext context) throws BPMException {
        log.debug("从\"挂起状态\"到\"就绪状态\"");
        boolean isNodeToMeReady= false;
        List<WorkitemInfo> workitems= itemlist.updateActivityItemsToReadyStatus(domain, context.getProcessId(), context.getActivateNode().getId());
        List<WorkitemInfo> workitems41= new ArrayList<WorkitemInfo>();
        List<WorkitemInfo> workitems7= new ArrayList<WorkitemInfo>();
        boolean isZCDB= false;
        for (WorkitemInfo w : workitems) {
            if(w.getState()==41){
                isNodeToMeReady= true;
                workitems41.add(w);
            }else if(w.getState()==7){
                workitems7.add(w);
                if(w.getActionState().endsWith("26,23") && !isZCDB){
                    isZCDB= true;
                }
            }
        }
        Recorder recorder = new Recorder(context.getTheCase());
        if(isNodeToMeReady){
            recorder.onNodeToMeReady(context.getActivateNode());
            ActionRunner.RunItemEvent(Event.WORKITEM_WAIT_TO_41, context, null, workitems41);
        }else{
            if(isZCDB){
                recorder.zcdbActivity((BPMActivity)context.getActivateNode());
                recorder.onNodeZcdb(context.getActivateNode());
            }else{
                recorder.onNodeReady(context.getActivateNode());
            }
            ActionRunner.RunItemEvent(Event.WORKITEM_WAIT_TO_READY, context, null, workitems7);
        }
        return true;
    }

    @Override
    public boolean onActivityReadyToWaiting(String domain, WorkflowBpmContext context) throws BPMException {
        log.debug("从\"就绪状态\"到\"挂起状态\"");
        Recorder recorder = new Recorder(context.getTheCase());
        recorder.onNodeSuspend(context.getActivateNode());
        //List<WorkitemInfo> workitems= itemlist.upateItemsForWaitingStatus(domain, context.getProcessId(), context.getCurrentActivityId());
        List<WorkitemInfo> workitems= itemlist.upateItemsForWaitingStatus(domain, context.getProcessId(),context.getSubObjectIds());
        ActionRunner.RunItemEvent(Event.WORKITEM_READY_TO_WAIT, context, null, workitems);
        return true;
    }

    @Override
    public boolean onActivityDoneToReady(String domain, WorkflowBpmContext context) throws BPMException {
        log.debug("从\"完成状态\"到\"就绪状态\"");
        Recorder recorder = new Recorder(context.getTheCase());
        boolean isReadyStatus= false;
        if ("0".equals(context.getSubmitStyleAfterStepBack())) {//流程重走方式
            isReadyStatus= true;
            recorder.onNodeReady(context.getActivateNode());
            BPMProcess process= context.getProcess();
            BPMCase theCase= context.getTheCase();
            BPMHumenActivity humenActivity= (BPMHumenActivity)context.getActivateNode();
            List<BPMActor> actors = humenActivity.getActorList();
            List<V3xOrgMember> members= workflowMatchUserManager.getUserList(domain, humenActivity, context,true);
            List<User> actorsList= WorkflowUtil.v3xOrgMemberToWorkflowUser(members, true);
            Object[] result = itemlist.addWorkItems(actorsList, humenActivity,process.getId(),theCase.getId(), domain);
            List<String> actorStr = (List<String>) result[0];
            List<WorkitemDAO> workitems = (List<WorkitemDAO>) result[1];
            BPMActor actor = actors.get(0);
            actor.getParty().setAddition(StringUtils.join(actorStr, ","));//(主要是回退的问题)
            WorkitemDAO workitem0 = workitems.get(0);
            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_ASSIGNED, context,workitem0,workitems);
        } else if ("1".equals(context.getSubmitStyleAfterStepBack())) {//直接提交给我方式
            isReadyStatus= false;
            recorder.onNodeToMeReady(context.getActivateNode());
            List<WorkitemInfo> workitems= itemlist.changeItemsFromFinishToReady(domain, context.getProcessId(), context.getActivateNode().getId(),
                    context.getCurrentActivityId(),isReadyStatus);
            ActionRunner.RunItemEvent(Event.WORKITEM_DONE_TO_READY, context, null, workitems);
        }
        return true;
    }

    @Override
    public boolean onActivityAwakeToReady(String domain, WorkflowBpmContext context, List<WorkitemInfo> items) throws BPMException {
        log.debug("从\"完成或终止状态\"到\"就绪状态\"");
        Recorder recorder = new Recorder(context.getTheCase());
        recorder.onNodeReady(context.getActivateNode());
        List<WorkitemInfo> workitems = items;
        ActionRunner.RunItemEvent(Event.WORKITEM_AWAKE_TO_READY, context, null, workitems);
        return true;
    }

    @Override
    public boolean onCaseInitialized(String domain, WorkflowBpmContext context) throws BPMException {
        if(context.isSubProcess()){//子流程
            ActionRunner.RunItemEvent(Event.SUB_PROCESS_STARTED, context, null, null);
        }
        else{//主流程
            ActionRunner.RunItemEvent(Event.PROCESS_STARTED, context, null, null);
        }
        return true;
    }

    @Override
    public boolean onActivityFinished(String domain, WorkflowBpmContext context) throws BPMException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onActivityTackBack(String domain, WorkflowBpmContext context) throws BPMException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onCaseStop(String domain, WorkflowBpmContext context) throws BPMException {
        SubProcessRunning subProcessRunning= subProcessManager.getAffinedMainflow(context.getProcessId());
        if( null!= subProcessRunning ){
        	if(subProcessRunning.getFlowRelateType()==1){
	            List<Long> nextNodeIds = processManager.getLatterActivityIds(subProcessRunning.getMainProcessId(),subProcessRunning.getMainNodeId());
	            context.setSubProcess(true);
	            context.setMainCaseId(String.valueOf(subProcessRunning.getMainCaseId()));
	            context.setMainNextNodeIds(nextNodeIds);
        	}
        	subProcessRunning.setIsFinished(1);
            subProcessManager.updateSubProcessRunning(subProcessRunning);
        }
        ActionRunner.RunItemEvent(Event.PROCESS_FINISHED, context,null,null);
        //更新流程状态为完成
        processManager.finishProcessState(context.getProcessId());
        return true;
    }


    /**
     * @param processManager the processManager to set
     */
    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

}
