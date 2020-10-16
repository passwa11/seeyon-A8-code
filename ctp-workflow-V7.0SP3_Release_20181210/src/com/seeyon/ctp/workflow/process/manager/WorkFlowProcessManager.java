package com.seeyon.ctp.workflow.process.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.NodeAuthorityVO;

public interface WorkFlowProcessManager {
    
    /**
     * 回退节点
     * @param workitemId
     * @param processId
     * @param nodeId
     * @param targetNodeId
     * @param userId
     * @return
     * @throws BPMException
     */
    public String stepBack(String workitemId, String caseId, String processId, String nodeId, String targetNodeId, String userId) throws BPMException;
    
    /**
     * 获取当前节点的父节点
     * @param processId
     * @param nodeId
     * @return
     * @throws BPMException
     */
    public List<Map<String, String>> getAllParentNodes(String processId, String nodeId) throws BPMException;
    
}
