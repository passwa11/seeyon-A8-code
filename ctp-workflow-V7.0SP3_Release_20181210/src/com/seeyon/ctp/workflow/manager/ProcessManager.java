/**
 * Author: wangchw
 * Rev:ProcessManager.java
 * Date: 20122012-9-7下午10:07:16
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.po.HisProcessInRunningDAO;
import com.seeyon.ctp.workflow.po.ProcessInRunningBLOBDAO;
import com.seeyon.ctp.workflow.po.ProcessInRunningDAO;

import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
*/
public interface ProcessManager {

    public static final int FLOWTYPE_SERIAL       = 1;
    public static final int FLOWTYPE_PARALLEL     = 2;
    public static final int FLOWTYPE_MULTIPLE     = 3;
    public static final int FLOWTYPE_COLASSIGN    = 4;
    public static final int FLOWTYPE_NEXTPARALLEL = 5;

    /**
     * 保存processXml到数据库中
     * @param process
     * @return
     * @throws BPMException
     */
    public String saveRunningProcessXml(String processXml) throws BPMException;

    /**
     * 根据processId从wf_process_running表中查询process信息
     * @param processId 流程模版ID
     * @return BPMProcess
     * @throws BPMException
     */
    public BPMProcess getRunningProcess(String processId) throws BPMException;
    /**
     * 根据processId从分库的表wf_process_running中查询process信息
     * @param processId 流程模版ID
     * @return BPMProcess
     * @throws BPMException
     */
    public BPMProcess getRunningProcessHis(String processId) throws BPMException ;
    /**
     * 
     * @param processId
     * @return
     * @throws BPMException
     */
    public ProcessInRunningDAO getProcessInRunningDAO(String processId) throws BPMException;
    public ProcessInRunningDAO getProcessInRunningDAO(String processId, boolean isHistoryFlag) throws BPMException;

    /**
     * 根据processId从wf_his_process_running表中查询process信息
     * @param processId 流程模版ID
     * @return BPMProcess
     * @throws BPMException
     */
    public BPMProcess getHisRunningProcess(String processId) throws BPMException;

    public HisProcessInRunningDAO getHisProcessInRunningDAO(String processId) throws BPMException;

    public HisProcessInRunningDAO getHisProcessInRunningDAO(String processId, boolean isHistoryFlag) throws BPMException;
    
    public String[] getWorkflowNodesInfoAndDR(String processId, CtpEnumBean nodePermissionPolicy) throws BPMException ;
    public String[] getWorkflowNodesInfoAndDR(BPMProcess process, CtpEnumBean nodePermissionPolicy) throws BPMException;
    /**
     * 新增流程定义BPMProcess
     * @param process
     * @return
     * @throws BPMException
     */
    public String saveRunningProcess(BPMProcess process,BPMCase theCase) throws BPMException;

    /**
     * 更新流程定义BPMProcess
     * @param process
     * @return
     * @throws BPMException
     */
    public String updateRunningProcess(BPMProcess process,BPMCase theCase) throws BPMException;

    public void finishProcessState(String processId) throws BPMException;

    public int getProcessState(String processId) throws BPMException;

    public int getProcessState(String processId, boolean isHistoryFlag) throws BPMException;

    public void addHisProcess(String processId) throws BPMException;

    public void deleteRunningProcess(String processId) throws BPMException;

    /**
     * 更新Xml到数据库中
     * @param processId
     * @param processXml
     */
    public void updateRunningProcessXml(String processId, String processXml) throws BPMException;

    /**
     * 
     * @param processId
     * @return
     * @throws BPMException
     */
    public String getWorkflowNodesInfo(String processId, CtpEnumBean nodePermissionPolicy) throws BPMException;

    /**
     * 获得被使用的节点权限名称
     * @param bpmProcess
     * @return
     * @throws BPMException
     */
    public List<String> getWorkflowUsedPolicyIds(BPMProcess bpmProcess) throws BPMException;

    /**
     * 督办更新流程内容
     * @param processId
     * @param activityId
     * @param operationType
     * @param flowData
     * @param policy
     * @param selecteNodeIdArr
     * @param _peopleArr
     * @param caseId
     * @param processXml
     * @param orginalReadyObjectJson
     * @param userId
     * @param userName
     * @param isForm
     * @param oldProcessLogJson
     * @return
     * @throws BPMException
     */
    public String[] superviseUpdateProcess(String processId, String activityId, int operationType,
            Map<String, Object> flowData, BPMSeeyonPolicy policy, String[] selecteNodeIdArr, String[] _peopleArr,
            String caseId, String processXml, String orginalReadyObjectJson, String userId, String userName,
            boolean isForm, String oldProcessLogJson, String isProIncludeChild,String nodeName) throws BPMException;
    public String[] releaseWorkFlowProcessLock(String processId, String userId,String loginFrom) throws BPMException;
    /**
     * 更新流程为指定状态
     * @param processId
     * @param state
     * @throws BPMException
     */
    public void updateProcessState(String processId, int state) throws BPMException;

    /**
     * 新流程改变发起节点
     * 原则：1、原模板流程开始节点更新为所选人员，同时设置NF属性为1，标记为是新流程。
     *       2、开始节点后加签一个节点，执行人为新流程设置所选中的人员
     * @param process
     * @param member
     * @throws BPMException
     */
    public void changeProcess4Newflow(BPMProcess process, WorkflowBpmContext context) throws BPMException;

    /**
     * 如果processId为null则为添加模板，否则为更新,根据XML生成/更新流程
     * @param process
     * @param processId
     * @param addition
     * @param condition
     * @param startUserId
     * @param startUserName
     * @param startUserLoginAccountId
     * @return
     * @throws BPMException
     */
    public BPMProcess saveOrUpdateProcessByXML(BPMProcess process, String processId, Map addition, Map condition,
            String startUserId, String startUserName, String startUserLoginAccountId) throws BPMException;

    /**
     * 将子流程设置数据添加到子流程运行表中.
     * @param processId
     * @param processTemplateId
     * @param caseId
     * @param bussinessId
     * @param appName
     * @throws BPMException
     */
    public boolean copySubProcessFromSettingToRunning(String processId, String processTemplateId, long caseId,
            String bussinessId, String appName) throws BPMException;

    /**
     * 流程加锁接口
     * @param processId
     * @param userId
     * @return
     * @throws BPMException
     */
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action) throws BPMException;
    
    /**
     * 流程加锁接口
     * @param processId
     * @param userId
     * @param isLock
     * @param action
     * @param from 来自哪个端（WXT表示微协同端）
     * @return
     * @throws BPMException
     */
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action,String from) throws BPMException;
    /**
     * 
     * @param processId
     * @param userId
     * @param isLock
     * @param action
     * @param from
     * @param useNowexpirationTime  是否使用当前时间更新激活时间
     * @return
     * @throws BPMException
     */
    public String[] lockWorkflowProcess(String processId, String userId,boolean isLock,int action,String from,boolean useNowexpirationTime) throws BPMException ;
    /**
     * 流程解锁接口
     * @param processId
     * @param userId
     * @return
     * @throws BPMException
     */
    public String[] releaseWorkFlowProcessLock(String processId, String userId) throws BPMException;

    /**
     * 
     * @param mainProcessId
     * @param mainNodeId
     * @return
     */
    public List<Long> getLatterActivityIds(String mainProcessId, String mainNodeId) throws BPMException;

    /**
     * 流程解锁接口
     * @param processId
     * @param currentUserId
     * @param action
     * @return
     * @throws BPMException
     */
    public String[] releaseWorkFlowProcessLock(String processId, String currentUserId, int action) throws BPMException;

    /**
     * 
     * @param process
     * @param finished
     * @param theCase
     * @return
     * @throws BPMException
     */
    public String updateRunningProcess(BPMProcess process, int finished,BPMCase theCase) throws BPMException;

    /**
     * 
     * @param process
     * @param ordinal
     * @param theCase
     * @throws BPMException
     */
    public void saveRunningProcessWithState(BPMProcess process, int ordinal,BPMCase theCase) throws BPMException;
    
    /**
     * 
     * @param workflowXml
     * @param orgJson
     * @param currentNodeId
     * @param type
     * @param currentUserId
     * @param currentUserName
     * @param currentAccountId
     * @param currentAccountName
     * @param defaultPolicyId
     * @param defaultPolicyName
     * @return
     * @throws BPMException
     */
    public String freeAddNode(String workflowXml,String orgJson,String currentNodeId,String type,String currentUserId,String currentUserName,
			String currentAccountId,String currentAccountName,String defaultPolicyId,String defaultPolicyName, List<BPMHumenActivity> addHumanNodes) throws BPMException;
    
    /**
     * 
     * @param workflowXml
     * @param currentNodeId
     * @return
     * @throws BPMException
     */
    public String freeDeleteNode(String workflowXml, String currentNodeId) throws BPMException;
    
    /**
     * 
     * @param workflowXml
     * @param currentNodeId
     * @param oneOrgJson
     * @param defaultPolicyId
     * @param defaultPolicyName
     * @return
     * @throws BPMException
     */
    public String[] freeReplaceNode(String workflowXml, String currentNodeId, String oneOrgJson,String defaultPolicyId,String defaultPolicyName,BPMCase theCase) 
    		throws BPMException;

    /**
     * 
     * @param workflowXml
     * @param currentNodeId
     * @param nodePropertyJson
     * @param updateAll 是否更新全部节点
     * @return
     * @throws BPMException
     */
	public String freeChangeNodeProperty(String workflowXml, String currentNodeId
	        , String nodePropertyJson, boolean updateAll, List<String> updateNodesList,BPMCase theCase) throws BPMException;

	/**
	 * 
	 * @param id
	 * @param processXml
	 * @return
	 */
	public String saveRunningProcessXml(String id, String processXml)throws BPMException;
	
	/**
	 * 
	 * @param action
	 * @param userName
	 * @param from
	 * @return
	 */
	public String getLockMsg(String action, String userName, String from,Long lockTime);
	
	
	public abstract List<ProcessInRunningBLOBDAO> getProcessInRunningBLOBDAOList(int paramInt1, int paramInt2, long paramLong)
	        throws BPMException;
    
    /**
     *设置当前流程中超时自动解锁，更新lock对象的expirationTime为当前时间 
     * @param processId
     * @param userId
     * @param action
     * @param loginPlatform
     * @return
     * @throws BPMException
     */
    public String[] updateLockExpirationTime(String processId, String userId,int action,String loginPlatform) throws BPMException ;


}
