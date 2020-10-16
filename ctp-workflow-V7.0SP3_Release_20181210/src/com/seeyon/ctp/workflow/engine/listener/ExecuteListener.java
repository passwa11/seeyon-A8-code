/*
 * Created on 2004-11-11
 *
 */
package com.seeyon.ctp.workflow.engine.listener;
import java.util.List;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.WorkitemInfo;

import com.seeyon.ctp.workflow.exception.BPMException;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流内部事件接口</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-27 上午12:23:41
 */
public interface ExecuteListener {
    
    /**
     * 流程实例初始化事件接口
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
    public boolean onCaseInitialized(String domain,WorkflowBpmContext context) throws BPMException ;
    
    /**
     * 流程实例完成事件接口
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
    public boolean onCaseFinish(String domain,WorkflowBpmContext context)throws BPMException ;
    
    /**
     * 流程节点完成事件
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
    public boolean onActivityFinished(String domain,WorkflowBpmContext context) throws BPMException ;
    
    /**
     * 流程节点取消事件
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
    public boolean onActivityRemove(String domain,WorkflowBpmContext context)throws BPMException ;
        
    /**
     * 流程节点就绪事件
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
//	public boolean onActivityReady(String domain,WorkflowBpmContext context)throws BPMException ;
	
	/**
     * 流程节点就绪事件
     * @param domain 工作流域名称
     * @param context 工作流上下文对象
     * @param isUseAdditonUserId 是否使用addition中的人员id生成待办事项
     * @return true表示成功;false表示失败
     * @throws BPMException 工作流异常对象
     */
	public boolean onActivityReady(String engineDomain, WorkflowBpmContext context,boolean isUseAdditonUserId) throws BPMException;
	
	/**
     * 从"挂起状态"到"就绪状态"
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityWaitingToReady(String domain,WorkflowBpmContext context) throws BPMException ;
    
    /**
     * 从"就绪状态"到"挂起状态"
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityReadyToWaiting(String domain,WorkflowBpmContext context) throws BPMException ;
    
    /**
     * 从"完成状态"到"就绪状态"
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityDoneToReady(String domain,WorkflowBpmContext context) throws BPMException ;
	
    /**
     * 流程实例撤销事件接口
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
	public boolean onCaseCancel(String domain,WorkflowBpmContext context,boolean isDeleteItem)throws BPMException ;
	
	/**
     * 流程实例挂起事件接口
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onCaseSuspend(String domain, WorkflowBpmContext context)throws BPMException ;
	
    /**
     * 流程实例恢复事件接口
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
	public boolean onCaseResume(String domain, WorkflowBpmContext context)throws BPMException ;
	
	/**
	 * 节点取回事件接口
	 * @param domain
	 * @param context
	 * @return
	 * @throws BPMException
	 */
	public boolean onActivityTackBack(String domain, WorkflowBpmContext context) throws BPMException;
    
    /**
     * 节点唤醒事件接口
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityAwakeToReady(String domain, WorkflowBpmContext context, List<WorkitemInfo> items) throws BPMException;

	/**
	 * 流程终止事件接口
	 * @param domain
	 * @param context
	 * @return
	 * @throws BPMException
	 */
    public boolean onCaseStop(String domain, WorkflowBpmContext context) throws BPMException ;   
}
