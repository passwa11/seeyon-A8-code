package com.seeyon.v3x.edoc.manager.statistics;

import java.util.List;
import java.util.Locale;

import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.permission.bo.Permission;

public class WorkflowNodeBean {
    private String nodeCode = "";
    private String nodeCodeId = "";
    private String workflowNode = "";
    
    public void execute(List<Permission> sendList, String[] sendCodes,String resource,Locale local){
        
        StringBuilder _workflowNode = new StringBuilder();
        StringBuilder _nodeCodeId = new StringBuilder();
        StringBuilder _nodeCode = new StringBuilder();
        
        for(Permission fp : sendList){
            for(String flow : sendCodes){
                if(String.valueOf(fp.getFlowPermId()).equals(flow)){
                    if(fp.getType() == 1){
                        _workflowNode.append(fp.getName());
                    }else{ 
                        String label = ResourceBundleUtil.getString(resource, local, fp.getLabel());
                        _workflowNode.append(label);
                    }
                    _workflowNode.append(StatConstants.LABEL_SIGN);
                    
                    _nodeCode.append(fp.getName()).append(StatConstants.SIGN);
                    _nodeCodeId.append(fp.getFlowPermId()).append(StatConstants.SIGN);
                    break;
                }
            }
        }
        if(_workflowNode.length() > 0){
            _workflowNode.deleteCharAt(_workflowNode.length() - 1);
        }
        
        workflowNode = _workflowNode.toString();
        nodeCodeId = _nodeCodeId.toString();
        nodeCode = _nodeCode.toString();
    }
    
    
    
    public String getNodeCode() {
        return nodeCode;
    }
    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }
    public String getNodeCodeId() {
        return nodeCodeId;
    }
    public void setNodeCodeId(String nodeCodeId) {
        this.nodeCodeId = nodeCodeId;
    }
    public String getWorkflowNode() {
        return workflowNode;
    }
    public void setWorkflowNode(String workflowNode) {
        this.workflowNode = workflowNode;
    }
    
}
