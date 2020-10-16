/*
 * Created on 2004-11-8
 *
 */
package net.joinwork.bpm.engine.execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.engine.enums.ProcessStateEnum;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.engine.listener.ExecuteListenerList;
import com.seeyon.ctp.workflow.engine.log.Recorder;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.util.WorkflowUtil;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.wapi.CaseInfo;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流引擎执行类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-3 上午08:58:00
 */
public class BPMExecute {

    private static Log          log = CtpLogFactory.getLog(BPMExecute.class);
    private ProcessManager      processes;
    private CaseManager         cases;
    private ExecuteListenerList listener;
    private String              domain;
    private WorkflowBpmContext  context;

    public BPMExecute(String domain, WorkflowBpmContext context, ProcessManager processes, CaseManager cases,
            ExecuteListenerList executListnerList) {
        this.processes = processes;
        this.cases = cases;
        this.listener = executListnerList;
        this.domain = domain;
        this.context = context;
    }

    /**
     * 发起流程实例
     * @return 流程实例id
     * @throws BPMException
     */
    public String[] StartCase(BPMProcess process, boolean isNew) throws BPMException {
        String[] result = new String[5];
        long caseId = -1l;
        if (process == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_START_PROCESS_NOT_FOUND,
                    new Object[] { context.getProcessId() });
        }
        String processId = process.getId();
        //创建流程实例
        BPMCase theCase = cases.createCase(process, context.getStartUserId(), context.getCaseName(),
                context.isSubProcess());
        //获得流程开始节点
        BPMStatus status;
        if (context.getStartStatusId() == null) {
            status = process.getStart();
            context.setStartStatusId(status.getId());
        } else {
            status = process.getStatusById(context.getStartStatusId());
        }
        if (status == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_STATUS_NOT_EXITE_IN_PROCESS, new Object[] {
                    context.getStartStatusId(), process.getIndex() });
        }
        log.debug("开始节点名称：" + status.getName());
        //将业务流程类型标识实例化到流程实例对象中，以后流程流转需要用到
        theCase.getDataMap().put(ActionRunner.SYSDATA_APPNAME, context.getAppName());
        String dynamicFormMasterIds  = context.getDynamicFormMasterIds();
        if(Strings.isNotEmpty(dynamicFormMasterIds)){
        	theCase.getDataMap().put(ActionRunner.WF_DYNAMIC_FORM_KEY,dynamicFormMasterIds);
        }
        context.setCurrentWorkitemId(-1l);
        context.setCurrentActivityId(context.getStartStatusId());
        context.setActivateNode(status);
        context.setProcess(process);
        context.setTheCase(theCase);

        //记录流程启动日志
        Recorder recorder = new Recorder(theCase);
        //执行流程启动时的监听器(集成扩展点)
        if (listener.onCaseInitialized(domain, context)) {
            recorder.StartCase(context.getStartStatusId(), status, null);

            Map<String,String> splitJoinMap= WorkflowUtil.findSplitJoinMap(process);
            context.setSplitJoinMap(splitJoinMap);
            //流程流转
            Interpreter interpreter = new Interpreter(domain, context, process, theCase, listener, recorder);
            interpreter.AddReadyStatus(context.getStartStatusId());
            
            WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
            if (theCase.isFinished()) {
                recorder.CaseFinished(status);
                context.setStartFinished(true);
                listener.onCaseFinish(domain, context);
            }else{//保存流程定义模版到数据库
                if (isNew) {
                    if(!context.isAddFirstNode()){
                    	processId = processes.saveRunningProcessXml(process.getId(),context.getProcessXml());
                    }else{
                    	processId = processes.saveRunningProcess(process,theCase);
                    }
                } else {
                    processId = processes.updateRunningProcess(process,ProcessStateEnum.processState.running.ordinal(),theCase);
                }
            }
            //保存流程实例数据
            cases.addCase(theCase);
            caseId = theCase.getId();
        }
        if (caseId == -1) {
            throw new BPMException(BPMException.EXCEPTION_CODE_SYSTEM_ERROR, new Object[] { context.getStartStatusId(),
                    caseId });
        }
        result[0] = String.valueOf(caseId);
        result[1] = processId;
        if (null != context.getNextMembers()) {
            result[2] = context.getNextMembers().toString();
            result[3] = context.getNextMembersWithoutPolicyInfo().toString();
        } else {
            result[2] = "";
            result[3] = "";
        }
        result[4] = String.valueOf(context.isStartFinished());
        return result;
    }

    /**
     * 节点流转
     * @param userId
     * @param process
     * @param theCase
     * @param activity
     * @param statusId
     * @param theStepBackNodeId
     * @throws BPMException
     */
    public void FinishActivity(String userId, BPMProcess process, BPMCase theCase, BPMActivity activity,
            int statusId, String theStepBackNodeId,int unFinisheditemNum) throws BPMException {
        log.info(AppContext.currentUserName()+"-FinishActivity : P:"+process.getId()+",A:"+activity.getId()+",statusId:"+statusId+",theStepBackNodeId:"+theStepBackNodeId+",unFinisheditemNum:"+unFinisheditemNum);
        Recorder recorder = new Recorder(theCase);
        boolean isNodeFinished= false;
        if(ObjectName.isInformObject(activity)){
        	if(unFinisheditemNum==0){
                recorder.FinishActivity(activity, theStepBackNodeId, userId);
                isNodeFinished= true; 
            }
        }else{
            recorder.FinishActivity(activity, theStepBackNodeId, userId);
            isNodeFinished= true;
        }
        Map<String,String> splitJoinMap= WorkflowUtil.findSplitJoinMap(process);
        context.setSplitJoinMap(splitJoinMap);
        
        Interpreter interpreter = new Interpreter(domain, context, process, theCase, listener, recorder);
        interpreter.FinishActivity(activity, statusId, theStepBackNodeId,isNodeFinished);
        
        
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        boolean isFirstTimeFinished= theCase.getState()!=CaseInfo.STATE_FINISHED;
        boolean isUpdateFinishProcess= false;
        if (theCase.isFinished()) {
            if(!ObjectName.isInformObject(activity)){
                recorder.CaseFinished(activity);
                listener.onCaseFinish(domain, context);
                isUpdateFinishProcess= true;
            }else if(isFirstTimeFinished){
            	recorder.CaseFinished(activity);
                listener.onCaseFinish(domain, context);
                isUpdateFinishProcess= true;
            }
        }
      
        boolean  isProcessChanged = context.isProcessChanged();
        log.info(AppContext.currentUserName()+",[FinishActivity]:"+isUpdateFinishProcess+",isProcessChanged:"+isProcessChanged);
        
        if(isUpdateFinishProcess){
        	processes.updateRunningProcess(context.getProcess(), ProcessStateEnum.processState.finished.ordinal(),context.getTheCase());
        }else if(isProcessChanged){
            processes.updateRunningProcess(process,theCase);
        }
        cases.save(theCase);
    }

    /**
     * 终止流程
     * @param process
     * @param theCase
     * @param activity
     * @throws BPMException
     */
    public void StopActivity(BPMProcess process, BPMCase theCase, BPMActivity activity) throws BPMException {
        Recorder recorder = new Recorder(theCase);
        recorder.StopActivity(activity);
        recorder.onNodeStoped(activity);
        List<ReadyNode> readyActivityList = theCase.getReadyActivityList();
        if (readyActivityList != null && !readyActivityList.isEmpty()) {
            for (ReadyNode node : readyActivityList) {
                BPMActivity readyActivity = process.getActivityById(node.getId());
                if (readyActivity != null && !activity.getId().equals(readyActivity.getId())) {
                    Recorder recorder0 = new Recorder(theCase);
                    recorder0.StopActivity(readyActivity);
                    recorder0.onNodeStoped(readyActivity);
                }
            }
            readyActivityList.clear();
        }
        List<ReadyNode> informList= theCase.getReadyInformActivityList();
        if (informList != null && !informList.isEmpty()) {
            for (ReadyNode node : informList) {
                BPMActivity readyActivity = process.getActivityById(node.getId());
                if (readyActivity != null && !activity.getId().equals(readyActivity.getId())) {
                    Recorder recorder0 = new Recorder(theCase);
                    recorder0.StopActivity(readyActivity);
                    recorder0.onNodeStoped(readyActivity);
                }
            }
            informList.clear();
        }
        List readyStatusList = theCase.getReadyStatusList();
        if (readyStatusList != null) {
            readyStatusList.clear();
        }
        Recorder recorder1 = new Recorder(theCase);
        recorder1.CaseFinished(activity);
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        processes.updateRunningProcess(process,theCase);
        cases.stop(theCase);
        listener.onCaseStop(domain, context);
    }

    /**
     * 暂存待办
     * @param process
     * @param theCase
     * @param activity
     * @throws BPMException
     */
    public void zcdbActivity(BPMProcess process, BPMCase theCase, BPMActivity activity) throws BPMException {
        Recorder recorder = new Recorder(theCase);
        recorder.zcdbActivity(activity);
        new Interpreter(domain, context, process, theCase, listener, recorder).zcdbActivity(activity);
    }

    /**
     * 取回流程
     * @param process
     * @param theCase
     * @param activity
     * @throws BPMException
     */
    public void _takeBackActivity(BPMProcess process, BPMCase theCase, BPMActivity activity) throws BPMException {
        Recorder recorder = new Recorder(theCase);
        new Interpreter(domain, context, process, theCase, listener, recorder)._takeBackActivity(activity, null, null);
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        processes.updateRunningProcess(process,theCase);
    }

    /**
     * 唤醒一个流程实例
     * @param theCase 将要被唤醒的流程实例
     * @param process 当前流程实例对流的工作流
     * @param nodes 将要被唤醒的节点
     * @throws BPMException 抛出异常
     */
    public void awakeCase(BPMCase theCase, BPMProcess process, List<BPMActivity> nodes, List<WorkitemInfo> workitems)
            throws BPMException {
        if (theCase == null || process == null) {
            return;
        }
        theCase.setState(CaseInfo.STATE_RUNNING);
        Recorder recorder = new Recorder(theCase);
        //Interpreter上应该添加一个表示流程唤醒的方法。
        new Interpreter(domain, context, process, theCase, listener, recorder).awakeActivity(process, nodes, workitems);
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        if (theCase.getState() == CaseInfo.STATE_FINISHED) {
            listener.onCaseFinish(domain, context);
        }
        if(theCase.isFinished()){
        	processes.updateRunningProcess(context.getProcess(), ProcessStateEnum.processState.finished.ordinal(),context.getTheCase());
        }else{
        	 processes.updateRunningProcess(process,theCase);
        }
    }

    /**
     * 撤销流程实例
     * @param theCase
     * @param process
     * @return 1:流程不存在（已完成） 0:流程存在，可以撤消
     * @throws BPMException
     */
    public int CancelCase(BPMCase theCase, BPMProcess process) throws BPMException {
        if (process == null) {
            return -1;
        }
        Recorder recorder = new Recorder(theCase);
        new Interpreter(domain, context, process, theCase, listener, recorder).removeAllAdditions("false");
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        cases.cancel(theCase);
        //processes.updateRunningProcess(process);
        processes.updateRunningProcess(process,ProcessStateEnum.processState.finished.ordinal(),theCase);
        context.setProcessId(process.getId());
        listener.onCaseCancel(domain, context);
        return 0;
    }

    /**
     * 此方法可以去掉
    	 * @param userId
    	 * @param caseId
    	 */
    public void SuspendCase(String userId, long caseId, boolean isSuspend) throws BPMException {
        BPMCase theCase = getCase(caseId);
        if (isSuspend && theCase.getState() != CaseInfo.STATE_RUNNING) {
            return;
        }
        if (!isSuspend && theCase.getState() != CaseInfo.STATE_SUSPEND) {
            return;
        }
        BPMProcess process = getProcess(theCase);
        cases.suspend(theCase, isSuspend);
        if (isSuspend) {
            listener.onCaseSuspend(domain, context);
        } else {
            listener.onCaseResume(domain, context);
        }
    }

    private BPMCase getCase(long caseId) throws BPMException {
        BPMCase theCase = cases.getCase(caseId);
        if (theCase == null)
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        return theCase;
    }

    private BPMProcess getProcess(BPMCase theCase) throws BPMException {
        BPMProcess process = processes.getRunningProcess(theCase.getProcessIndex());

        if (process == null)
            throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN,
                    new String[] { theCase.getProcessId() });
        return process;
    }

    //seeyon >>>
    /* 回退流程
     * Edit by James Hu
     */
    public int withdrawActivity(BPMProcess process, BPMCase theCase, BPMActivity activity) throws BPMException {

        Recorder recorder = new Recorder(theCase);
        Map<String, String> nodeIds= new HashMap<String,String>();
        
     // 需要设置配对信息
        Map<String,String> splitJoinMap= WorkflowUtil.findSplitJoinMap(process);
        context.setSplitJoinMap(splitJoinMap);
        
        int result = new Interpreter(domain, context, process, theCase, listener, recorder).withdrawActivity(activity,nodeIds);
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        if (result != 1) {//如果是撤销流程，在这里做下面处理没有意义，且当有空节点时，会出现撤销不成功的问题。
            cases.save(theCase);
        }
        processes.updateRunningProcess(process,theCase);
        return result;
    }

    /*
     * 删除节点
     * Edit by jincm
     */
    public int deleteActivity(String userId, BPMProcess process, BPMCase theCase, BPMActivity activity, boolean frontad)
            throws BPMException {

        Recorder recorder = new Recorder(theCase);
        int result = new Interpreter(domain, context, process, theCase, listener, recorder).deleteActivity(activity,
                frontad);

        //当流程实例中不存在待办节点时，完成流程实例
        List readyActivityList = theCase.getReadyActivityList();
        //List informList= theCase.getReadyInformActivityList();
        if ((readyActivityList == null || readyActivityList.size() == 0) && frontad) {
            theCase.setState(CaseInfo.STATE_FINISHED);
        }

        if (frontad) {
            cases._save(theCase, frontad);
        }

        if (theCase.getState() == CaseInfo.STATE_FINISHED) {
            listener.onCaseFinish(domain, context);
        }
        return result;
    }

    /**
     * 增加就绪节点
     * @param userId
     * @param process
     * @param theCase
     * @param activity
     * @throws BPMException
     */
    public void addReadyActivity(String userId, BPMProcess process, BPMCase theCase, List<BPMActivity> activity,boolean isChange)
            throws BPMException {

        Recorder recorder = new Recorder(theCase);
        new Interpreter(domain, context, process, theCase, listener, recorder).addReadyActivity(activity,isChange);
        cases.save(theCase);
    }

    public CaseManager getCases() {
        return cases;
    }

    public void setCases(CaseManager cases) {
        this.cases = cases;
    }

    /**
     * 高级回退
     * @param selectTargetNodeId
     * @return
     * @throws BPMException
     */
    public List<String> recoverRecursiveForStepBack(BPMProcess process, BPMCase theCase, BPMAbstractNode fromNode,
            BPMAbstractNode toNode) throws BPMException {
        Recorder recorder = new Recorder(theCase);
        Interpreter interpreter = new Interpreter(domain, context, process, theCase, listener, recorder);
        List<String> nodeLists= new ArrayList<String>(); 
        Set<String> passNodes= new HashSet<String>();
        interpreter.recoverRecursiveForStepBack(fromNode, toNode,nodeLists,passNodes);
        return nodeLists;
    }

    public String[] startCaseToMe(BPMActivity theStepBackActivity,BPMCase theCase) throws BPMException {
        log.debug("提交至上一回退节点:回退节点名称=" + theStepBackActivity.getName() + ";回退节点标识="
                + theStepBackActivity.getId());
        context.setActivateNode(theStepBackActivity);
        context.setTheCase(theCase);
        listener.onActivityWaitingToReady(domain, context);
        String[] result= new String[5];
        result[0] = String.valueOf(theCase.getId());
        result[1] = theCase.getProcessId();
        String seeyonPolicyName= theStepBackActivity.getSeeyonPolicy().getName();
        if(theStepBackActivity.getSeeyonPolicy().getName().equals(theStepBackActivity.getSeeyonPolicy().getId())){
            seeyonPolicyName= BPMSeeyonPolicy.getShowName(theStepBackActivity.getSeeyonPolicy().getId());
        }
        String name = theStepBackActivity.getName()+"("+seeyonPolicyName+")";
        if(context.getNextMembers()==null){
            StringBuffer nextMembers= new StringBuffer();
            nextMembers.append(name);
            context.setNextMembers(nextMembers);
            
            StringBuffer nextMembers1= new StringBuffer();
            nextMembers1.append(theStepBackActivity.getName());
            context.setNextMembersWithoutPolicyInfo(nextMembers1);
        }else{
            StringBuffer nextMembers= context.getNextMembers();
            nextMembers.append(",").append(name);
            context.setNextMembers(nextMembers);
            
            StringBuffer nextMembers1= context.getNextMembersWithoutPolicyInfo();
            nextMembers1.append(",").append(theStepBackActivity.getName());
            context.setNextMembersWithoutPolicyInfo(nextMembers1);
        }
        if (null != context.getNextMembers()) {
            result[2] = context.getNextMembers().toString();
            result[3] = context.getNextMembersWithoutPolicyInfo().toString();
        } else {
            result[2] = "";
            result[3] = "";
        }
        result[4]= String.valueOf(false);
        return result;
    }

    /**
     * 直接回退流程到发起者
     * @param theCase
     * @param process
     * @return
     * @throws BPMException
     */
    public int stepCaseToStarter(BPMCase theCase, BPMProcess process) throws BPMException {
        if (process == null) {
            return -1;
        }
        Recorder recorder = new Recorder(theCase);
        new Interpreter(domain, context, process, theCase, listener, recorder).removeActivityForStepToStartNode();
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        cases.updateCase(theCase);
        processes.updateRunningProcess(process,theCase);
        listener.onCaseStepDirectToStartNode(domain, context);
        return 0;
    }

}
