/*
 * Created on 2004-11-11
 *
 */
package com.seeyon.ctp.workflow.engine.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.event.BPMEvent;
import com.seeyon.ctp.workflow.event.Event;
import com.seeyon.ctp.workflow.exception.BPMException;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流执行监听列表</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-31 下午11:06:28
 */
public class ExecuteListenerList {
    
    private static Log log = CtpLogFactory.getLog(ExecuteListenerList.class);

    /**
     * 默认构造函数
     */
    public ExecuteListenerList() {
    }

    /**
     * 工作流监听器列表
     */
    private List<ExecuteListener> list = new ArrayList<ExecuteListener>();

    /**
     * @param list the list to set
     */
    public void setList(List<ExecuteListener> list) {
        this.list = list;
    }

    /**
     * 节点就绪事件
     * @param domain
     * @param context
     * @param isSendMessage
     * @return
     * @throws BPMException
     */
    public boolean onActivityReady(String domain,WorkflowBpmContext context,boolean isSendMessage,boolean isUseAddition) throws BPMException {
        context.setSendMessage(isSendMessage);
        ActionRunner.runNodeAction(domain, context, BPMEvent.ACTIVITY_STARTED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityReady(domain,context,isUseAddition) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例撤销事件
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onCaseCancel(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_CANCELED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseCancel(domain, context,false) == false){
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例挂起事件
     * @param domain
     * @param process
     * @param theCase
     */
    public boolean onCaseSuspend(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_SUSPEND);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseSuspend(domain, context) == false){
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例恢复事件接口
     * @param domain
     * @param process
     * @param theCase
     */
    public boolean onCaseResume(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_RESUME);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseResume(domain, context) == false)
                return false;
        }
        return true;
    }

    /**
     * 将节点对应的"挂起状态"的任务事项更改为"就绪状态"
     * @param domain
     * @param context
     */
    public boolean onActivityWaitingToReady(String domain, WorkflowBpmContext context) throws BPMException {
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityWaitingToReady(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从"就绪"到"挂起"
     * @param domain
     * @param context
     */
    public boolean onActivityReadyToWaiting(String domain, WorkflowBpmContext context) throws BPMException {
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityReadyToWaiting(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从"已办"到"就绪"
     * @param domain
     * @param context
     */
    public boolean onActivityDoneToReady(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runNodeAction(domain, context, Event.ACTIVITY_CANCELED);//回退动作
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityDoneToReady(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例初始化事件
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onCaseInitialized(String domain, WorkflowBpmContext context) throws BPMException {
        log.debug("执行流程初始化监听实现(sprint1重构)：listener.onCaseInitialized");
        //执行流程启动时的动作脚本(集成扩展点)
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_STARTED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseInitialized(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例完成事件
     * @param domain 工作流引擎域名称
     * @param context 工作流上下文对象
     * @return
     * @throws BPMException
     */
    public boolean onCaseFinish(String domain, WorkflowBpmContext context) throws BPMException {
        //触发流程结束动作脚本
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_FINISHED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseFinish(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程节点完成事件
     * @param domain 工作流引擎域名称
     * @param context 工作流上下文对象
     * @throws BPMException
     */
    public boolean onActivityFinished(String domain, WorkflowBpmContext context) throws BPMException {
        //触发流程节点后置动作脚本
        ActionRunner.runNodeAction(domain, context, BPMEvent.ACTIVITY_FINISHED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityFinished(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程节点取消事件
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityRemove(String domain,WorkflowBpmContext context) throws BPMException {
        ActionRunner.runNodeAction(domain, context, BPMEvent.ACTIVITY_CANCELED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityRemove(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程节点取回事件接口
     * @param domain
     * @param context
     */
    public boolean onActivityTackBack(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runNodeAction(domain, context, BPMEvent.ACTIVITY_TACKBACK);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityTackBack(domain, context) == false) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 流程实例唤醒事件接口
     * @param domain
     * @param context
     * @return
     * @throws BPMException
     */
    public boolean onActivityAwakeToReady(String domain, WorkflowBpmContext context, List<WorkitemInfo> items) throws BPMException {
        ActionRunner.runNodeAction(domain, context, BPMEvent.WORKITEM_AWAKE_TO_READY);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onActivityAwakeToReady(domain, context, items) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程实例终止事件接口
     * @param domain
     * @param context
     * @throws BPMException
     */
    public boolean onCaseStop(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_STOPPED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseStop(domain, context) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 直接回退给发起者
     * @param domain
     * @param context
     * @throws BPMException
     */
    public boolean onCaseStepDirectToStartNode(String domain, WorkflowBpmContext context) throws BPMException {
        ActionRunner.runProcessAction(domain, context, BPMEvent.PROCESS_CANCELED);
        for (int i = 0; i < list.size(); i++) {
            ExecuteListener listener = (ExecuteListener) list.get(i);
            if (listener.onCaseCancel(domain, context,true) == false) {
                return false;
            }
        }
        return true;
    }

}
