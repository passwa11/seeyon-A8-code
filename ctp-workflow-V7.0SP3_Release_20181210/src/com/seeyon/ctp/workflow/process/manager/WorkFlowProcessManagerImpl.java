package com.seeyon.ctp.workflow.process.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.workflow.designer.manager.WorkFlowDesignerManager;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.ProcessTemplateManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.ProcessEngine;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.BPMWorkItemList;

public class WorkFlowProcessManagerImpl implements WorkFlowProcessManager {

    private ProcessManager processManager;

    private ProcessTemplateManager processTemplateManager;

    private CaseManager caseManager;

    private BPMWorkItemList itemlist;
    
    private WorkflowApiManager wapi;
    
    private SubProcessManager subProcessManager;
    
    private WorkFlowDesignerManager workFlowDesignerManager;

    @Override
    public String stepBack(String workitemId, String caseId, String processId, String nodeId, String targetNodeId, String userId)
            throws BPMException {
        ProcessEngine engine = WAPIFactory.getProcessEngine("Engine_1");
        DataContainer container = new DataContainer();
        boolean success = true;
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentUserId(String.valueOf(AppContext.currentUserId()));
        context.setCurrentUserName(String.valueOf(AppContext.currentUserName()));
        context.setCurrentAccountId(String.valueOf(AppContext.getCurrentUser().getAccountId()));
        context.setCurrentAccountName(String.valueOf(AppContext.getCurrentUser().getAccountId()));
        context.setCurrentWorkitemId(Long.parseLong(workitemId));
        context.setProcessId(processId);
        context.setCurrentActivityId(nodeId);
        if(targetNodeId!=null && !targetNodeId.equals(nodeId)){
            context.setSelectTargetNodeId(targetNodeId);
        }
        context.setCaseId(Long.parseLong(caseId));
        engine.stepBack(context);
        container.add("success", success);
        return container.getJson();
    }

    @Override
    public List<Map<String, String>> getAllParentNodes(String processId, String nodeId) throws BPMException {
        BPMProcess process = processManager.getRunningProcess(processId);
        List<BPMHumenActivity> nodes = WorkflowUtil.findAllAncestorHumenActivitys(process.getActivityById(nodeId));
        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        if(nodes!=null && nodes.size()>0){
            for(BPMHumenActivity node : nodes){
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", node.getId());
                map.put("name", node.getName());
                results.add(map);
            }
        }
        return results;
    }

    public ProcessManager getProcessManager() {
        return processManager;
    }

    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    public ProcessTemplateManager getProcessTemplateManager() {
        return processTemplateManager;
    }

    public void setProcessTemplateManager(ProcessTemplateManager processTemplateManager) {
        this.processTemplateManager = processTemplateManager;
    }

    public CaseManager getCaseManager() {
        return caseManager;
    }

    public void setCaseManager(CaseManager caseManager) {
        this.caseManager = caseManager;
    }

    public BPMWorkItemList getItemlist() {
        return itemlist;
    }

    public void setItemlist(BPMWorkItemList itemlist) {
        this.itemlist = itemlist;
    }

    public WorkflowApiManager getWapi() {
        return wapi;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public SubProcessManager getSubProcessManager() {
        return subProcessManager;
    }

    public void setSubProcessManager(SubProcessManager subProcessManager) {
        this.subProcessManager = subProcessManager;
    }

    public WorkFlowDesignerManager getWorkFlowDesignerManager() {
        return workFlowDesignerManager;
    }

    public void setWorkFlowDesignerManager(WorkFlowDesignerManager workFlowDesignerManager) {
        this.workFlowDesignerManager = workFlowDesignerManager;
    }
}
