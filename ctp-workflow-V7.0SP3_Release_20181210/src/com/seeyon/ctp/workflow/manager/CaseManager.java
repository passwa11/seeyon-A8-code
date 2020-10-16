    /**
 * $Author: tanmf $
 * $Rev: 49624 $
 * $Date:: 2015-05-27 14:13:19#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.workflow.manager;

import java.util.List;
import java.util.Map;

import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.BPMCaseInfo;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

import com.seeyon.ctp.workflow.exception.BPMException;

/**
 * 
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
 */
public interface CaseManager {
    
    /**
     * 新增流程实例到wf_case_run或wf_case_history表中
     * @param theCase
     * @return
     * @throws BPMException
     */
    public long addCase(BPMCase theCase) throws BPMException;

    /**
     * 创建流程实例对象,但不保存到数据库表中
     * @param process 流程模版内容
     * @param startUserId 发起者ID
     * @param caseName 流程实例名称
     * @param isSubProcess 是否为子流程
     * @return BPMCase
     * @throws BPMException
     */
    public BPMCase createCase(BPMProcess process, String startUserId, String caseName, boolean isSubProcess)
            throws BPMException;

    /**
     * 根据流程实例ID查询流程实例信息,如果wf_case_run中没有,则到wf_case_history中去查询
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getCase(long caseId) throws BPMException;
    public BPMCase getCase(long caseId, boolean isHistoryFlag) throws BPMException;

    /**
     * 根据流程实例ID从wf_case_history中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHistoryCase(long caseId) throws BPMException;

    /**
     * 根据流程实例ID从wf_his_case_run中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHisCase(long caseId) throws BPMException;
    public BPMCase getHisCase(long caseId, boolean isHistoryFlag) throws BPMException;

    /**
     * 根据流程实例ID从wf_his_case_history中查询流程实例信息
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getHisHistoryCase(long caseId) throws BPMException;

    /**
     * 根据流程实例ID从wf_case_run中查询流程实例信息,不查大字段，速度快
     * @param caseId 流程实例ID
     * @return
     * @throws BPMException
     */
    public BPMCase getCaseInfo(long caseId) throws BPMException;

    /**
     * @param theCase
     */
    public void _save(BPMCase theCase, boolean isUpdateState) throws BPMException;

    /**
     * @param theCase
     */
    public void save(BPMCase theCase) throws BPMException;

    /**
     * @param theCase
     */
    public void cancel(BPMCase theCase) throws BPMException;

    public void stop(BPMCase theCase) throws BPMException;

    /**
     * @param theCase
     * @param isSuspend
     */
    public void suspend(BPMCase theCase, boolean isSuspend) throws BPMException;

    /**
     * @param processId
     * @return
     */
    public List<? extends BPMCase> getCaseByProcessId(String processId) throws BPMException;

    /**
     * 
     * @param processIndex
     * @return
     * @throws BPMException
     */
    public List<? extends BPMCase> getCaseByProcessIndex(String processIndex) throws BPMException;

    /**
     * 
     * @param theCase
     * @throws BPMException
     */
    public void remove(BPMCase theCase) throws BPMException;

    /**
     * 
     * @param case1
     * @throws BPMException
     */
    public void saveHisCase(BPMCase case1) throws BPMException;

    /**
     * 
     * @return
     * @throws BPMException
     */
    public List<BPMCaseInfo> getProcessCaseList() throws BPMException;
    
    
    public List<BPMCaseInfo> getHistoryCaseList(String processId, String processName, String caseName,
            String startUser, int state, int begin, int length) throws BPMException;
    
    public List<BPMCaseInfo> getCaseList(String processId, String processName, String caseName,
            String startUser, int state, int begin, int length) throws BPMException;
    
    public String[] getCaseLogXml(BPMCase theCase) throws BPMException;

    public void updateCase(BPMCase theCase)throws BPMException;

    public void deleteCase(long caseId)throws BPMException;
    public void deleteHistoryCasebyId(long caseId) throws BPMException;
    
    /**
     * 获得所有节点的状态
     * @param caseId
     * @return
     * @throws BPMException
     */
    public Map<String, String[]> getNodesStatus(long caseId) throws BPMException;
    
    /**
     * 通过processId获取case或historycase对象
     * @param processId
     * @return
     */
    public BPMCase getCaseOrHistoryCaseByProcessId(String processId)throws BPMException;
    
    /**
     * 
     * @param theCase
     * @throws BPMException
     */
	public void updateCaseOnly(BPMCase theCase) throws BPMException;
}
