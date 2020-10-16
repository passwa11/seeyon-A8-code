/*
 * Created on 2004-11-11
 *
 */
package net.joinwork.bpm.engine.execute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.engine.enums.ProcessStateEnum;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.engine.listener.ExecuteListenerList;
import com.seeyon.ctp.workflow.event.BPMEvent;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.event.WorkflowEventExecute;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.ProcessDefManager;
import com.seeyon.ctp.workflow.manager.ProcessManager;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.ProcessTemplateManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.po.CaseRunDAO;
import com.seeyon.ctp.workflow.po.HistoryCaseRunDAO;
import com.seeyon.ctp.workflow.po.HistoryWorkitemDAO;
import com.seeyon.ctp.workflow.po.SubProcessRunning;
import com.seeyon.ctp.workflow.po.WorkitemDAO;
import com.seeyon.ctp.workflow.supernode.manager.WorkflowSuperNodeManager;
import com.seeyon.ctp.workflow.util.BPMChangeUtil;
import com.seeyon.ctp.workflow.util.ReadyObjectUtil;
import com.seeyon.ctp.workflow.util.WorkflowEventConstants;
import com.seeyon.ctp.workflow.util.WorkflowNodeBranchLogUtil;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.BPMChangeMergeVO;
import com.seeyon.ctp.workflow.vo.BPMChangeMessageVO;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;
import com.seeyon.ctp.workflow.xml.StringXMLElement;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMStart;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.definition.ReadyObject;
import net.joinwork.bpm.engine.wapi.CaseInfo;
import net.joinwork.bpm.engine.wapi.CaseLog;
import net.joinwork.bpm.engine.wapi.ProcessEngine;
import net.joinwork.bpm.engine.wapi.ProcessObject;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkItemManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.BPMWorkItemList;
import net.joinwork.bpm.task.WorkitemInfo;
import net.joinwork.bpm.task.log.Recorder;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流引擎接口实现类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-2 下午02:46:04
 */
public class ProcessEngineImpl implements ProcessEngine {

    private final static Log       log = CtpLogFactory.getLog(ProcessEngineImpl.class);
    /** 多人按顺序执行 key **/
    public  final static String    IS_ORDER_EXECUTE = "isOrderExecute"; 
    
    /** 多人按顺序执行 手动添加人员key **/
    public  final static String    MULTI_ADD_MEMBER = "addMember"; 
    private ExecuteListenerList    listener;
    private String                 domain;
    private ProcessManager         processManager;
    private BPMWorkItemList        itemlist;
    private ProcessTemplateManager processTemplateManager;
    private ProcessOrgManager      processOrgManager;
    private SubProcessManager      subProcessManager;
    private WorkItemManager workItemManager;
    private WorkFlowMatchUserManager workFlowMatchUserManager;
    private WorkflowSuperNodeManager workflowSuperNodeManager;

    public void setWorkflowSuperNodeManager(WorkflowSuperNodeManager workflowSuperNodeManager) {
        this.workflowSuperNodeManager = workflowSuperNodeManager;
    }
    
    public WorkItemManager getWorkItemManager() {
		return workItemManager;
	}

	public void setWorkItemManager(WorkItemManager workItemManager) {
		this.workItemManager = workItemManager;
	}

	public void setSubProcessManager(SubProcessManager subProcessManager) {
        this.subProcessManager = subProcessManager;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(ExecuteListenerList listener) {
        this.listener = listener;
    }

    /**
     * @param processOrgManager the processOrgManager to set
     */
    public void setProcessOrgManager(ProcessOrgManager processOrgManager) {
        this.processOrgManager = processOrgManager;
    }

    /**
     * @param itemlist the itemlist to set
     */
    public void setItemlist(BPMWorkItemList itemlist) {
        this.itemlist = itemlist;
    }

    private CaseManager            caseManager;
    private String                 lic              = "lic";
    private CacheAccessable        cacheFactory     = CacheFactory.getInstance(ProcessEngineImpl.class);
    private CacheMap               updateProcessMap = null;
    private Map<Long, ReadyObject> readyAddedMap;
    private LockManager            lockManager      = null;

    public ProcessEngineImpl() {
        try {
            if (!cacheFactory.isExist("updateProcessMap")) {
                updateProcessMap = cacheFactory.createMap("updateProcessMap");
            } else {
                updateProcessMap = cacheFactory.getMap("updateProcessMap");
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
            updateProcessMap = cacheFactory.createMap("updateProcessMap");
        }
    }

    public Map<Long, ReadyObject> getReadyAddedMap() {
        return readyAddedMap;
    }

    public void setReadyAddedMap(Map<Long, ReadyObject> readyAddedMap) {
        this.readyAddedMap = readyAddedMap;
    }

    public Map getUpdateProcessMap() {
        return updateProcessMap.toMap();
    }

    public void setUpdateProcessMap(Map updateProcessMap) {
        this.updateProcessMap.replaceAll(updateProcessMap);
    }

    public void setCaseManager(CaseManager caseManager) {
        this.caseManager = caseManager;
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getDomain()
      */
    public String getDomain() {
        return domain;
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#setDomain(java.lang.String)
      */
    public void setDomain(String domain) throws BPMException {
        this.domain = domain;
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#cancelCase(java.lang.String, int)
      */

    private int cancelCase(BPMCase theCase, BPMProcess process, WorkflowBpmContext context) throws BPMException {
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
            int result = execute.CancelCase(theCase, process);
            return result;
        } catch (BPMException e) {
            throw e;
        } catch (Exception e) {
            throw new BPMException(e);
        }
    }

    /**
     * 启动流程
     */
    public String[] runCase(WorkflowBpmContext context) throws BPMException {
        try {
	        WorkflowUtil.putCaseToWorkflowBPMContext(null,context);
	        String processId = context.getProcessId();
	        boolean isNew = false;
	        if (Strings.isBlank(processId) || "-1".equals(processId.trim())) {
	            processId = String.valueOf(WorkflowUtil.getTableKey());
	            isNew = true;
	        }
	        context.setProcessId(processId);
	        String processXml= context.getProcessXml();
	        BPMProcess process =  workFlowMatchUserManager.getBPMProcessFromCacheRequestScope(context.getMatchRequestToken());
	        if(null==process){
	        	process = BPMProcess.fromXML(processXml);
	        }
	       
	        /**
	         * 这段代码要放在获取分支/选人信息之前,最好放在代码的最前放！！
	         * 否则流程重走（cancelNodes4ReG0）的时候Interpreter.removeActivity会清除掉context.NodeAdditionMap里面的所有信息，而节点选人的数据正好存在这里面
	         */
	        if(context.isToReGo()){
                BPMCase theCase = caseManager.getCase(context.getCaseId());
                context.setTheCase(theCase); 
                cancelNodes4ReG0(context, theCase, process, process.getStart(),context.getAppName());
                context.setTheCase(null);
            }
	        
	        String popNodeSelected = context.getSelectedPeoplesOfNodes();
            Map<String, Map<String,Object>> addition = WorkflowUtil.getPopNodeSelectedValues(popNodeSelected);
            setActivityManualSelect(process, addition,context);
            log.info("节点选人信息:="+popNodeSelected);
            
            //复合节点变串签模式
            if(addition != null && !addition.isEmpty()){
                BPMChangeUtil.mergeOneToMany(context,process, addition);
                //串签执行以后，将这个字符值设置为一个无意义的值，避免同一次操作触发了子流程，子流程也取到相同的Context对象，然后也执行了串签操作，然后报错
                context.setSelectedPeoplesOfNodes(popNodeSelected.replaceAll(IS_ORDER_EXECUTE, ""));
                if(context.isProcessChanged()){
                    processXml = process.toXML();
                }
            }
	        
	        process = processManager.saveOrUpdateProcessByXML(process, processId, null, null, context.getStartUserId(),context.getStartUserName(),context.getStartAccountId());
	        processXml= WorkflowUtil.initStartAndProcessIdInfo(processXml,process);
	        context.setProcessXml(processXml);
	        
	       
	       
	        if(context.isAddFirstNode()){//需要在第一个节点之后自动增加自身节点
	            processManager.changeProcess4Newflow(process, context);
	        }
	        
	        String formAppId= process.getStart().getSeeyonPolicy().getFormApp();
	        String rightId = "";//WorkflowUtil.getFormRightId(currentActivity);
	        WorkflowUtil.doInitFormDataForCache(context, formAppId, rightId, false);
	        
	      
            
	        String popNodeCondition = context.getConditionsOfNodes();
	        Map<String, String> condition= WorkflowUtil.getPopNodeConditionValues(popNodeCondition, addition);
            setActivityIsDelete(process, condition,context);
            log.info("节点选分支信息:="+condition);
	        context.setNeedSelectPeopleNodeMap(addition);
	        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        
            String[] result1 = execute.StartCase(process, isNew);
            if(!context.getEventDataContextList().isEmpty()){
                ActionRunner.RunItemEvent(BPMEvent.WORKFLOW_WORKITEM_ASSIGNED,context.getEventDataContextList());
            }
            context.setCaseId(Long.valueOf(result1[0]));
            boolean hasSubprocess = false;
            //拷贝生成子流程
            if(null!=context.getProcessTemplateId() && !"".equals(context.getProcessTemplateId().trim())){
                hasSubprocess = processManager
                .copySubProcessFromSettingToRunning(processId, context.getProcessTemplateId(), context.getCaseId(),
                        context.getBussinessId(), context.getAppName());
            }
            String[] result = new String[8];
            result[0] = result1[0];
            result[1] = result1[1];
            result[2] = result1[2];
            result[3] = String.valueOf(hasSubprocess);
            result[4] = result1[3];
            result[5] = result1[4];
            List<String> policy = processManager.getWorkflowUsedPolicyIds(process);
            result[6] = Strings.join(policy, ",");
            result[7] = process.getStart().getSeeyonPolicy().getDR();
            
            WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.Start.name(), context);
            if(context.isStartFinished()){
                WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.ProcessFinished.name(), context);
            }
            
            return result;
        } catch (Throwable e) {
            log.error("runCase:启动流程出现错误", e);
            throw new BPMException(e);
        }
    }

    /**
     * 
     */
    public String[] runCaseFromTemplate(WorkflowBpmContext context) throws BPMException {
        String processTempateId = context.getProcessTemplateId();
        String matchRequestToken= context.getMatchRequestToken();
        BPMProcess process= workFlowMatchUserManager.getBPMProcessFromCacheRequestScope(matchRequestToken);
        if(null==process){
        	String processXml = processTemplateManager.selectProcessTempateXml(processTempateId);//从流程模版表中获得
        	context.setProcessXml(processXml);
        }else{//走缓存
        	String processXml = workFlowMatchUserManager.getProcessXmlFromCacheRequestScope(matchRequestToken);
        	context.setProcessXml(processXml);
        }
        context.setProcessId(null);
        return runCase(context);
    }

    /**
     * 为节点设置选择的人员
     * @param process
     * @param manualMap
     * @throws BPMException
     */
    private static void setActivityManualSelect(BPMProcess process, Map<String, Map<String,Object>> manualMap,WorkflowBpmContext context)
            throws BPMException {
        try {
            Iterator iter = manualMap.keySet().iterator();

            while (iter.hasNext()) {
                String nodeId = (String) iter.next();
                Map<String,Object> nodeInfo = manualMap.get(nodeId);
                String[] manualSelect = (String[])nodeInfo.get("pepole");
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
                    WorkflowUtil.putNodeAdditionToContext(context, nodeId, actor.getParty(), "addition", actorStr);
                }
            }
            WorkflowNodeBranchLogUtil.printLogNodePersonSelect(process, manualMap);
        } catch (Exception e) {
            
            log.error("动态设置节点人员异常", e);
            
            throw new BPMException("Dynamically set node exception", e);
        }
    }

    /**
     * 为节点设置是否为删除节点
     * @param process
     * @param condition
     * @throws BPMException
     */
    public static void setActivityIsDelete(BPMProcess process, Map<String, String> condition,WorkflowBpmContext context) throws BPMException {
        try {
            Iterator iter = condition.keySet().iterator();
            while (iter.hasNext()) {
                String nodeId = (String) iter.next();
                String isDelete = condition.get(nodeId);
                BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(nodeId);
                if (activity == null)
                    continue;
                WorkflowUtil.putNodeConditionToContext(context, activity, "isDelete", isDelete);
            }
        } catch (Exception e) {
            throw new BPMException("Dynamically set Node to delete error"/*"动态设置节点是否进行假删除异常"*/, e);
        }
    }

    /**
     * @param processManager the processManager to set
     */
    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    /**
     * 撤销流程
     */
    public int cancelCase(WorkflowBpmContext context) throws BPMException {
        long caseId = context.getCaseId();
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
            log.debug("接口名称：撤销流程");
            log.debug("流程定义模版标识caseId：" + caseId);
            BPMCase theCase = getCase(caseId);
            context.setTheCase(theCase);
            int theCaseState = theCase.getState();
            if (theCaseState == CaseInfo.STATE_FINISHED || theCaseState == CaseInfo.STATE_CANCEL) {
                return -1;
            }
            WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
            BPMProcess process = (BPMProcess) getProcessRunningById(theCase.getProcessId());
            //判断是否存在已经流程结束的子流程
            if(!context.isSubProcess()){
	            String[] canCancel = subProcessManager.canCancelSubProcess(process.getId(), null);
	            if (!"1".equals(canCancel[1])) {
	                log.debug("子流程已结束，主流程不允许做撤销操作！");
	                return 3;
	            }
            }
            int result = execute.CancelCase(theCase, process);
            if (result == 0) {
                //NF 正常回退，收回上节点触发的新流程
                context.setBusinessData("sub_operationType", "repeal");
                if(!context.isSubProcess()){
                	cancelSubProcess(context, process.getId(), null);
                }
            }
            context.setProcessId(process.getId());
            
            String appName= theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
            context.setAppName(appName);
            String formAppId= process.getStart().getSeeyonPolicy().getFormApp();
            context.setFormAppId(formAppId);
            
          //表单操作权限
            String rightId = "";//WorkflowUtil.getFormRightId(currentActivity);
            WorkflowUtil.doInitFormDataForCache(context, formAppId, rightId);
            WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.Cancel.name(), context);
            return result;
        } catch (BPMException e) {
            throw e;
        } catch (Exception e) {
            throw new BPMException(e);
        }
    }

    /**
     * 取回流程
     */
    public int takeBack(WorkflowBpmContext context) throws BPMException {
        long workitemId = context.getCurrentWorkitemId();
        WorkitemInfo workitem = itemlist.getWorkItemOrHistory(workitemId);
        BPMProcess process = getProcess(workitem.getProcessId(),context.getMatchRequestToken());
        BPMCase theCase = getCase(workitem.getCaseId());
        WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
        String activityId = workitem.getActivityId();
        BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(activityId);
        context.setTheCase(theCase);
        context.setProcess(process);
        context.setActivateNode(activity);
        context.setCurrentWorkitemId(workitemId);
        context.setCurrentActivityId(activityId);
        context.setCaseId(theCase.getId());
        log.debug("接口名称：取回流程");
        log.debug("流程实例标识caseId：" + workitem.getCaseId());
        log.debug("流程定义模版标识processId：" + process.getId());
        log.debug("当前任务事项标识workitemId：" + workitemId);
        log.debug("当前流程节点标识：" + activityId);
        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_TAKEBACK, context, workitem, null);
        Recorder.takeBackItem(null, workitem);
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        execute._takeBackActivity(process, theCase, activity);
        if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
            itemlist.cancelItems(domain, context);
            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,context.getWillDeleteWorkItems().get(0),context.getWillDeleteWorkItems());
        }
        itemlist.takeBackItem(workitem);
        WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
        updateCase(theCase);
        if (theCase.getState() == CaseInfo.STATE_FINISHED) {
            listener.onCaseFinish(domain, context);
        }
        //撤销子流程
        List<String> nodeIdList = new ArrayList<String>();
        nodeIdList.add(activityId);
        context.setBusinessData("sub_operationType", "takeBack");
        cancelSubProcess(context, process.getId(), nodeIdList);
        context.setProcessId(process.getId());
        WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.TakeBack.name(), context);
        return 0;
    }

    /**
     * 终止流程
     * @param workitemId 任务事项id
     * @throws BPMException
     */
    public void stopCase(WorkflowBpmContext context) throws BPMException {
        long workitemId = context.getCurrentWorkitemId();
        WorkitemInfo workitem = itemlist.getWorkItemOrHistory(workitemId);
        if (workitem == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_EXITE,
                    new Object[] { new Long(workitemId) });
        }
        if (workitem.getState() != WorkItem.STATE_READY && workitem.getState() != WorkItem.STATE_NEEDREDO 
                && workitem.getState() != WorkItem.STATE_NEEDREDO_TOME && workitem.getState() != WorkItem.STATE_SUSPENDED) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_READY_STATE, new Object[] { new Long(
                    workitemId) });
        }
        BPMProcess process = getProcess(workitem.getProcessId(),context.getMatchRequestToken());
        BPMHumenActivity activity = (BPMHumenActivity) process.getActivityById(workitem.getActivityId());
        BPMCase theCase = getCase(workitem.getCaseId());
        WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
        context.setProcessId(workitem.getProcessId());
        context.setProcess(process);
        context.setTheCase(theCase);
        context.setCurrentWorkitemId(workitemId);
        context.setActivateNode(activity);
        log.debug("接口方法：终止流程");
        log.debug("流程实例标识caseId：" + theCase.getId());
        log.debug("流程定义模版标识processId：" + process.getId());
        log.debug("当前任务事项标识workitemId：" + workitemId);
        log.debug("当前流程节点标识activityId：" + activity.getId());
        theCase.setLastPerformer(workitem.getPerformer());
        itemlist.stopItem(workitem.getId(), theCase.getId());
        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_STOP, context, workitem, null);
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        execute.StopActivity(process, theCase, activity);
        
        
        String appName= theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
        context.setAppName(appName);
        String formAppId= process.getStart().getSeeyonPolicy().getFormApp();
        context.setFormAppId(formAppId);
        String rightId = "";//WorkflowUtil.getFormRightId(currentActivity);
        WorkflowUtil.doInitFormDataForCache(context, formAppId, rightId);        
        WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.Stop.name(), context);
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getCaseProcess(java.lang.String, int)
      */
    public ProcessObject getCaseProcess(Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Object[] { new Long(caseId) });
        }
        return processManager.getRunningProcess(theCase.getProcessIndex());
    }

    public BPMProcess updateRunningProcess(BPMProcess process,BPMCase theCase,WorkflowBpmContext context) throws BPMException {
        try {
        	WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
            processManager.updateRunningProcess(process,theCase);
        } catch (BPMException e) {
            throw new BPMException(BPMException.EXCEPTION_CODE_START_PROCESS_NOT_FOUND,
                    new Object[] { process.getId() });
        }
        return process;
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getCaseList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date, int, int, int)
      */

    public List getCaseList(String userId, String processId, String processName, String caseName, String startUser,
            Date startDateAfter, Date startDateBefor, int state, int begin, int length) throws BPMException {
        return caseManager.getCaseList(processId, processName, caseName, startUser, state, begin, length);
    }

    @SuppressWarnings("deprecation")
    public List getHistoryCaseList(String userId, String processId, String processName, String caseName,
            String startUser, Date finishDateAfter, Date finishDateBefor, int state, int begin, int length)
            throws BPMException {
        return caseManager.getHistoryCaseList(processId, processName, caseName, startUser, state, begin, length);
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getCaseInfo(java.lang.String, int)
      */
    public CaseInfo getCaseInfo(String userId, Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCaseInfo(caseId);
        if (theCase == null)
            return null;
        return theCase.createCaseInfo();
    }

    public BPMCase getCase(Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        }
        return theCase;
    }

    public void updateCase(BPMCase theCase) throws BPMException {
        caseManager.save(theCase);
    }

    /**
     * 暂存待办
     */
    public void temporaryPending(WorkflowBpmContext context) throws BPMException {
        String userId = context.getCurrentUserId();
        Long itemId = context.getCurrentWorkitemId();
        WorkitemInfo workitem = itemlist.getWorkItemOrHistory(itemId);
        if (workitem == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_EXITE, new Object[] { new Long(itemId) });
        }
        int state = workitem.getState();
        /*if (workitem.getState() != WorkItem.STATE_READY && workitem.getState() != WorkItem.STATE_NEEDREDO) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_READY_STATE,
                    new Object[] { new Long(itemId) });
        }*/
        BPMCase theCase = getCase(workitem.getCaseId());
        String appName= theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
        context.setAppName(appName);
        WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
        
        String cacheKey = context.getMatchRequestToken();
        if(context.getCeMevo()!=null){
            //有加签等信息的时候不要从缓存中中取， 节点会重复...
            cacheKey = "";
        }
        BPMProcess process = getProcess(workitem.getProcessId(), cacheKey);
        if (process == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN,
                    new Object[] { workitem.getProcessId() });
        }
        WorkFlowAppExtendManager workFlowAppExtendManager = WorkFlowAppExtendInvokeManager.getAppManager(appName);
        //合并加签节点
        if(context.getCeMevo()!=null){
        	if(!context.isMobile()){
        		BPMChangeUtil.mergeProcessAndChangeMessage(process, context.getCeMevo());
        	}
        	context.setProcessChanged(true);
        	boolean isCol ="collaboration".equals(context.getAppName());
        	if(workFlowAppExtendManager != null && isCol){
        		List<Map<String,String>> list = WorkflowUtil.getNodeMemberInfos(process);
        		workFlowAppExtendManager.processNodesChanged(workitem.getProcessId(),context.getAppObject(), list);
        	}
        }
        
        context.setTheCase(theCase);
        context.setProcess(process);
        context.setAppName(appName);
        if(Strings.isBlank(context.getStartAccountId())){
            BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
            context.setStartAccountId(startActor.getParty().getAccountId());
        }
        String readyObjectJson = context.getReadyObjectJson();
        if (null != readyObjectJson && !"".equals(readyObjectJson.trim())) {
        	context.setProcessChanged(true);
            saveAcitivityModify(process, workitem.getActivityId(), context, theCase,false);
        }
        BPMActivity activity = process.getActivityById(workitem.getActivityId());
        context.setActivateNode(activity);
        log.debug("接口方法：暂存待办");
        log.debug("流程实例标识caseId：" + theCase.getId());
        log.debug("流程定义模版标识processId：" + process.getId());
        log.debug("当前任务事项标识workitemId：" + itemId);
        log.debug("当前流程节点标识activityId：" + activity.getId());
        Recorder.zcdbItem(null, workitem);
        Date date = new Date(System.currentTimeMillis());
        workitem.setUpdateDate(date);
        itemlist.updateItem(workitem);
        boolean isProcessSaved= false;
        if(null!=activity){
            boolean isCopyNode= isCopyNode((BPMHumenActivity)activity);
            BPMActor actor = (BPMActor)activity.getActorList().get(0);
            BPMParticipant party = actor.getParty();
            String partyTypeId = party.getType().id;
            if(isCopyNode && !"user".equals(partyTypeId)){
                //用于节点复制
            	WorkflowUtil.putNodeAdditionToContext(context, activity.getId(), party, "raddition", 
                		WorkflowUtil.getNodeAdditionFromContext(context, activity.getId(), party, "addition"));
            }
            BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
            int unFinishedNum = 0;
            if (workitem.getItemNum() == 1) {
                execute.zcdbActivity(process, theCase, activity);
            } else {
                List<WorkitemDAO> sameBatchItemList = itemlist.getWorkItemByBatch(workitem.getBatch(),
                        new Integer[]{WorkitemInfo.STATE_READY,WorkitemInfo.STATE_NEEDREDO_TOME,WorkitemInfo.STATE_SUSPENDED}, workitem);
                unFinishedNum = sameBatchItemList.size();
                if (unFinishedNum == 0 || (activity.isSingleProcessMode() || activity.isCompetitionProcessMode())) {
                    if (!sameBatchItemList.isEmpty() && sameBatchItemList.size() > 0) {
                        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context, workitem, sameBatchItemList);
                        itemlist.canceItems(sameBatchItemList, workitem);
                    }
//                    if (activity.isSingleProcessMode() || activity.isCompetitionProcessMode()) {
//                        List<BPMActor> actors = activity.getActorList();
//                        BPMActor actor = actors.get(0);
//                        actor.getParty().setAddition(workitem.getPerformer());//(主要是回退的问题)
//                    }
                    if(isCopyNode && (activity.isSingleProcessMode() || activity.isCompetitionProcessMode())){
                        actor.getParty().setRaddition(workitem.getPerformer()); //用于节点复制
                    }
                }
                execute.zcdbActivity(process, theCase, activity);
            }
            if(ObjectName.isInformObject(activity)){//复合知会节点，特殊处理下
                execute.FinishActivity(context.getCurrentUserId(), process, theCase, activity, state,workitem.getNextStatus(),1);
                isProcessSaved= true;
            }
            if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                itemlist.cancelItems(domain, context);
                ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,workitem,context.getWillDeleteWorkItems());
            }
            if(!context.getEventDataContextList().isEmpty()){
                ActionRunner.RunItemEvent(BPMEvent.WORKFLOW_WORKITEM_ASSIGNED,context.getEventDataContextList());
            }
            WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
            updateCase(theCase);
        }
        if(context.isProcessChanged() && !isProcessSaved){
        	if(theCase.isFinished()){
            	processManager.updateRunningProcess(context.getProcess(), ProcessStateEnum.processState.finished.ordinal(),context.getTheCase());
        	}else{
        		processManager.updateRunningProcess(process,theCase);
        	}
        }
    }

    /**
     * 如果该流程实例存在待添加的节点，将其激活
     * @param process
     * @param userId
     * @throws BPMException
     */
    public void saveAcitivityModify(BPMProcess process, String userId, WorkflowBpmContext context, BPMCase theCase,boolean isChange)
            throws BPMException {
        try {
            ReadyObject readyObject = ReadyObjectUtil.parseToReadyObject(context.getReadyObjectJson(), process);
            if (readyObject != null) {
                Boolean hasPendingObject = false;
                List<BPMActivity> activityList = readyObject.getActivityList();
                if (activityList != null && !activityList.isEmpty()) {
                    hasPendingObject = true;
                }
                List<BPMActivity> preDelActivityList = readyObject.getPreDelActivityList();
                if (preDelActivityList != null && !preDelActivityList.isEmpty()) {
                    for (BPMActivity preDelActivity : preDelActivityList) {
                        if (hasPendingObject) {
                            preDelActivity.setFrontadFlag(false);
                        }
                        //待优化
                        deleteActivity(userId, process, theCase, preDelActivity, preDelActivity.isFrontadFlag(),
                                context);
                    }
                }
                if (hasPendingObject) {
                    addReadyActivity(userId, process, theCase, activityList, context,isChange);
                    //待优化
                    if (readyObject.isSaveTheCaseFlag()) {
                        getCaseManager().save(theCase);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    /**
     * @return
     */
    public ExecuteListenerList getListener() {
        return listener;
    }

    public BPMProcess getProcess(String processIndex,String matchRequestToken) throws BPMException {
    	BPMProcess process = workFlowMatchUserManager.getBPMProcessFromCacheRequestScope(matchRequestToken);
    	if(null==process){
    		process = processManager.getRunningProcess(processIndex);
    	}
        return process;
    }

    public BPMProcess getProcessRunningById(String processId) throws BPMException {
        return this.getProcessRunningById(processId, true);
    }

    public BPMProcess getHisProcessRunningById(String processId) throws BPMException {
        return processManager.getHisRunningProcess(processId);
    }

    public BPMProcess getProcessRunningById(String processId, boolean isFromCache) throws BPMException {
        //log.info("processId:="+processId);
        //log.info("isFromCache:="+isFromCache);
        if (isFromCache) { //从缓存中读，如果没有就读数据库
            BPMProcess process = processManager.getRunningProcess(processId);
            if (process == null) {
                throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN);
            }

            return process;
        } else {
            //缓存事实上也是从数据库中读取的，这里可以略去不要。但是为了格式不变，仍旧使用缓存读取的方式。
            //    		return new ProcessInRunningDataUtil(database).getProcess(processId);
            return processManager.getRunningProcess(processId);
        }
    }

    /**
     * @return
     */
    public CaseManager getCaseManager() {
        return caseManager;
    }

    /* (non-Javadoc)
      * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getCaseLogList(java.lang.String, int)
      */
    public List getCaseLogList(String userId, Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        }
        return theCase.getCaseLogList();
    }

    public String getCaseLogXML(String userId, Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        }
        List list = theCase.getCaseLogList();
        StringXMLElement rootNode = new StringXMLElement("caseLog");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                CaseLog caseLog = (CaseLog) list.get(i);
                caseLog.toXML(rootNode);
            }
        }
        return rootNode.toString();
    }

    public String getHisCaseLogXML(String userId, Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getHisCase(caseId);
        if (theCase == null)
            theCase = caseManager.getHisHistoryCase(caseId);
        if (theCase == null)
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        List list = theCase.getCaseLogList();

        StringXMLElement rootNode = new StringXMLElement("caseLog");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                CaseLog caseLog = (CaseLog) list.get(i);
                caseLog.toXML(rootNode);
            }
        }

        return rootNode.toString();
    }

    public String getCaseProcessXML(String processId) throws BPMException {
        /*BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null)
            theCase = caseManager.getHistoryCase(caseId);
        if (theCase == null)
            throw new BPMException(
                    BPMException.EXCEPTION_CODE_CASE_NOT_EXITE,
                    new Integer[]{new Integer(caseId)});*/

        BPMProcess process = processManager.getRunningProcess(processId);
        if (process == null) {
            return null;
        }

        StringXMLElement rootNode = new StringXMLElement("processes");
        process.toXML(rootNode);
        return rootNode.toString();
    }

    public String getHisCaseProcessXML(String processId) throws BPMException {
        /*BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null)
            theCase = caseManager.getHistoryCase(caseId);
        if (theCase == null)
            throw new BPMException(
                    BPMException.EXCEPTION_CODE_CASE_NOT_EXITE,
                    new Integer[]{new Integer(caseId)});*/

        BPMProcess process = processManager.getHisRunningProcess(processId);
        if (process == null) {
            return null;
        }

        StringXMLElement rootNode = new StringXMLElement("processes");
        process.toXML(rootNode);
        return rootNode.toString();
    }

    public String getCaseProcessXML(String userId, Long caseId) throws BPMException {
        BPMCase theCase = caseManager.getCase(caseId);
        if (theCase == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Long[] { new Long(caseId) });
        }
        BPMProcess process = processManager.getRunningProcess(theCase.getProcessIndex());
        StringXMLElement rootNode = new StringXMLElement("processes");
        process.toXML(rootNode);
        return rootNode.toString();
    }

    public List getNextHumenActivitis(Long caseId, String fromNodeId) throws BPMException {
        BPMProcess process = (BPMProcess) getCaseProcess(caseId);
        if (process == null)
            return null;
        List list = new ArrayList();
        process.getSubProcessIds(list);
        List subProcessList = null;
        ProcessDefManager processDef = WAPIFactory.getProcessDefManager(domain);
        for (int i = 0; i < list.size(); i++) {
            String subProcessId = (String) list.get(i);
            BPMProcess subProcess = (BPMProcess) processDef.getProcessInReady(subProcessId);
            if (subProcess == null)
                throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN);
            if (subProcessList == null)
                subProcessList = new ArrayList();
            subProcessList.add(subProcess);

        }
        return process.getNextHumenActivities(fromNodeId, subProcessList, 0);
    }

    //seeyon>>>

    /*
      * Edit by James Hu
      */

    private int withdrawActivity(BPMProcess process, BPMCase theCase, BPMActivity activity, WorkflowBpmContext context)
            throws BPMException {
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
            int result = execute.withdrawActivity(process, theCase, activity);
            return result;
        } catch (BPMException e) {
            throw e;
        } catch (Exception e) {
            throw new BPMException(e);
        }

    }

    /*
     * Edit by jincm
     */

    public int deleteActivity(String userId, BPMProcess process, BPMCase theCase, BPMActivity activity,
            boolean frontad, WorkflowBpmContext context) throws BPMException {
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
            int result = execute.deleteActivity(userId, process, theCase, activity, frontad);
            return result;
        } catch (BPMException e) {
            throw e;
        } catch (Exception e) {
            throw new BPMException(e);
        }

    }

    /**
     * 使指定的activity列表处于ready状态
     *
     * @param userId
     * @param process
     * @param theCase
     * @param activity
     * @return
     * @throws BPMException
     */
    public void addReadyActivity(String userId, BPMProcess process, BPMCase theCase, List<BPMActivity> activity,
            WorkflowBpmContext context,boolean isChange) throws BPMException {
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
            execute.addReadyActivity(userId, process, theCase, activity,isChange);
        } catch (BPMException e) {
            throw e;
        } catch (Exception e) {
            throw new BPMException(e);
        }
    }

    //seeyon>>>

    public List<BPMParticipant> getParticipantByActivity(String userId, BPMCase theCase, BPMActivity activity)
            throws BPMException {
        //return processPool.getRunningProcess(theCase.getProcessIndex());
        //theCase.getReadyActivityList()

        List<BPMParticipant> result = new ArrayList<BPMParticipant>();
        List<BPMActor> actors = activity.getActorList();
        for (BPMActor actor : actors) {
            BPMParticipant participant = actor.getParty();
            result.add(participant);
        }

        return result;
    }

    /*
     * 更新修改中的process,不进行持久化
     * @see net.joinwork.bpm.engine.wapi.ProcessEngine#updateModifyingProcess(net.joinwork.bpm.definition.BPMProcess)
     */
    public void updateModifyingProcess(BPMProcess process) throws BPMException {
        Long processId = Long.parseLong(process.getId());
        //更新流程修改次数
        process.setModifyNum((process.getModifyNum() + 1).toString());
        if (updateProcessMap == null) {
            updateProcessMap = cacheFactory.createMap("updateProcessMap");
            updateProcessMap.put(processId, process);
        } else {
            BPMProcess modifyingProcess = (BPMProcess) updateProcessMap.get(processId);
            if (modifyingProcess == null) {
                updateProcessMap.put(processId, process);
            } else {
                updateProcessMap.remove(processId);
                updateProcessMap.put(processId, process);
            }
        }
        //应用锁 修改 by wusb 2010-11-3
        if (lockManager.check(new Long(process.getModifyUser()), processId))
            lockManager.lock(new Long(process.getModifyUser()), processId);
    }

    /*
     * 删除修改中的process,进行持久化
     * @see net.joinwork.bpm.engine.wapi.ProcessEngine#updateModifyingProcess(net.joinwork.bpm.definition.BPMProcess)
     */
    public void delModifyingProcess(String processId, String userId) throws BPMException {
        Long _processId = Long.parseLong(processId);
        //删除应用锁，by wusb 2010-11-3
        lockManager.unlock(_processId);
        if (updateProcessMap != null) {
            BPMProcess modifyingProcess = (BPMProcess) updateProcessMap.get(_processId);
            if (modifyingProcess != null) {
                String modifyUser = modifyingProcess.getModifyUser();
                if (modifyUser.equals(userId)) {
                    updateProcessMap.remove(_processId);
                    //必须该流程处于修改中，才有可能存在待添加的节点
                    if (readyAddedMap != null) {
                        ReadyObject readyObject = (ReadyObject) readyAddedMap.get(_processId);
                        if (readyObject != null) {
                            readyAddedMap.remove(_processId);
                        }
                    }
                }
            }
        }
    }

    /*
     * 检查修改中的process是否更当前process相同
     * @see net.joinwork.bpm.engine.wapi.ProcessEngine#updateModifyingProcess(net.joinwork.bpm.definition.BPMProcess)
     */
    public synchronized String checkModifyingProcess(String processId, String userId) throws BPMException {
        Long _processId = Long.parseLong(processId);
        String modifyUserId = null;
        //取出锁的拥有者，没有返回null by wusb 2010-11-3
        String tempUserId = getLockObject(_processId);
        if (tempUserId != null && !tempUserId.equals(userId)) {
            modifyUserId = tempUserId;
        }
        if (updateProcessMap == null) {
            return modifyUserId;
        }
        BPMProcess modifyingProcess = (BPMProcess) updateProcessMap.get(_processId);
        if (modifyingProcess != null && !modifyingProcess.getModifyUser().equals(userId)) {
            modifyUserId = modifyingProcess.getModifyUser();
        }
        if (modifyUserId != null && !"".equals(modifyUserId)) {
            modifyUserId = processOrgManager.getProcessModifyUserId(modifyUserId);
        }
        return modifyUserId;
    }

    //取出锁的拥有者  by wusb 2010-11-3
    private String getLockObject(Long processId) {
        List<Lock> locks = lockManager.getLocks(processId);
        if (locks != null && !locks.isEmpty()) {
            Lock lk = locks.get(0);
            if (lk != null) {
                return String.valueOf(lk.getOwner());
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.joinwork.bpm.engine.wapi.ProcessEngine#getCaseProcessXML(java.lang.String, int)
     */
    public String getModifyingProcessXML(String userId, String processId) throws BPMException {
        BPMProcess process = (BPMProcess) updateProcessMap.get(Long.parseLong(processId));
        StringXMLElement rootNode = new StringXMLElement("processes");
        process.toXML(rootNode);
        return rootNode.toString();
    }

    public void addReadyActivityMap(String processId, ReadyObject readyObject) throws BPMException {
        Long _processId = Long.parseLong(processId);
        if (readyAddedMap != null) {
            ReadyObject _readyObject = (ReadyObject) readyAddedMap.get(_processId);
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
            } else {
                readyAddedMap.put(_processId, readyObject);
            }
        }
    }

    public ReadyObject getReadyObject(String processId, String userId) throws BPMException {
        ReadyObject readyObject = null;
        if (readyAddedMap != null) {
            ReadyObject _readyObject = (ReadyObject) readyAddedMap.get(Long.parseLong(processId));
            if (_readyObject != null && _readyObject.userId.equals(userId)) {
                readyObject = _readyObject;
                readyAddedMap.remove(Long.parseLong(processId));
            }
        }
        return readyObject;
    }

    //seeyon<<<

    public void addHisProcess(String processId) throws BPMException {
        processManager.addHisProcess(processId);
    }

    public void saveHisCase(BPMCase case1) throws BPMException {
        caseManager.saveHisCase(case1);
    }

    public void deleteCase(BPMCase theCase) throws BPMException {
        if (theCase != null) {
            caseManager.remove(theCase);
        }
    }

    public void deleteProcess(String processId) throws BPMException {
        this.processManager.deleteRunningProcess(processId);
    }

    @Override
    public String[] finishWorkItem(WorkflowBpmContext context) throws BPMException {
        String appName = context.getAppName();
        String currentUserId = context.getCurrentUserId();
        Long currentWorkitemId = context.getCurrentWorkitemId();
        boolean sysAutoFinishFlag = context.isSysAutoFinishFlag();
        
        StringBuilder sb = new StringBuilder();
        sb.append(AppContext.currentUserName()+",W_I:="+currentWorkitemId);
        sb.append("; W_N:="+context.getSelectedPeoplesOfNodes());
        sb.append("; W_C:="+context.getConditionsOfNodes());
        sb.append("; W_M:="+context.getChangeMessageJSON());
        log.info(sb);
        
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        try {
        	context.setMatchRequestToken(WorkflowUtil.getWorkflowMatchRequestToken(context.getConditionsOfNodes()));
        	Long workItemId = context.getCurrentWorkitemId();
            WorkItem workitem = workItemManager.getWorkItemOrHistory(workItemId);//itemlist.getWorkItemById(context.getCurrentWorkitemId());
            /*if (workitem == null) {
                throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_EXITE,
                        new Object[] { context.getCurrentWorkitemId() });
            }*/
            context.setCurrentActivityId(workitem.getActivityId());
            
            //相对角色匹配上节点所有人员使用
            context.setBusinessData("WORKITEM_BATCH_ID", workitem.getBatch());
            
            BPMCase theCase = getCase(workitem.getCaseId());
            if (theCase == null) {
                throw new BPMException(BPMException.EXCEPTION_CODE_CASE_NOT_EXITE, new Object[] { "" });
            }
            WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
            appName= theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
            context.setAppName(appName);
            String processId = theCase.getProcessId();
            context.setStartUserId(theCase.getStartUser());
            context.setProcessId(processId);
            
            String cacheKey = context.getMatchRequestToken();
            if(context.getCeMevo()!=null){
                //有加签等信息的时候不要从缓存中中取， 节点会重复...
                cacheKey = "";
            }
            BPMProcess process = getProcess(theCase.getProcessId(), cacheKey);
            
            if (process == null) {
                throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN,
                        new Object[] { theCase.getProcessIndex() });
            }
            String formAppId= process.getStart().getSeeyonPolicy().getFormApp();
            String rightId = "";//WorkflowUtil.getFormRightId(currentActivity);
           
            
            BPMActivity myNode= process.getActivityById(workitem.getActivityId());

            context.setTheCase(theCase);
            context.setProcess(process);
            
            //判断是否需要流程重走
            int state = workitem.getState();
            /**
             * 这段代码要放在获取分支/选人信息之前,最好放在代码的最前放！！
             * 否则流程重走（cancelNodes4ReG0）的时候Interpreter.removeActivity会清除掉context.NodeAdditionMap里面的所有信息，而节点选人的数据正好存在这里面
             */
            if(context.isToReGo()){
                cancelNodes4ReG0(context, theCase, process, myNode,context.getAppName());
                workitem.setNextStatus(null);
                state = WorkItem.STATE_READY;
            }
            
            WorkflowUtil.doInitFormDataForCache(context, formAppId, rightId, false);
            
            String dynamicFormMasterIds = context.getDynamicFormMasterIds();
            if (Strings.isNotEmpty(dynamicFormMasterIds)) {
                theCase.getDataMap().put(ActionRunner.WF_DYNAMIC_FORM_KEY, dynamicFormMasterIds);
            }else{
                //前端不需要选人的动态审批路径需要在这里设置，要不然表单条件变动后就会出问题
                dynamicFormMasterIds = (String) theCase.getDataMap().get(ActionRunner.WF_DYNAMIC_FORM_KEY); 
                if(Strings.isNotEmpty(dynamicFormMasterIds)){
                    context.setDynamicFormMasterIds(dynamicFormMasterIds);
                }
            }
            
            //合并加签节点
            WorkFlowAppExtendManager workFlowAppExtendManager = WorkFlowAppExtendInvokeManager.getAppManager(appName);
            if(context.getCeMevo()!=null){
            	BPMChangeUtil.mergeProcessAndChangeMessage(process, context.getCeMevo());
            	context.setProcessChanged(true);
            	boolean isCol ="collaboration".equals(context.getAppName());
            	if(workFlowAppExtendManager != null && isCol){
            		List<Map<String,String>> list = WorkflowUtil.getNodeMemberInfos(process);
            		workFlowAppExtendManager.processNodesChanged(processId,context.getAppObject(), list);
            	}
            }
            if(Strings.isBlank(context.getStartAccountId())){
                BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
                context.setStartAccountId(startActor.getParty().getAccountId());
            }
            String readyObjectJson = context.getReadyObjectJson();
            if (null != readyObjectJson && !"".equals(readyObjectJson.trim())) {
            	context.setProcessChanged(true);
                saveAcitivityModify(process, currentUserId, context, theCase,false);
            }

            String popNodeSelected = context.getSelectedPeoplesOfNodes();
            Map<String, Map<String,Object>> selectPeopleParams = WorkflowUtil.getPopNodeSelectedValues(popNodeSelected);
            setActivityManualSelect(process, selectPeopleParams,context);
            
            String popNodeCondition = context.getConditionsOfNodes();
            Map<String, String> nodeIdToIsDeleteMap = WorkflowUtil.getPopNodeConditionValues(popNodeCondition, selectPeopleParams);
            setActivityIsDelete(process, nodeIdToIsDeleteMap,context);
            
            //复合节点变串签模式
            if(selectPeopleParams != null && !selectPeopleParams.isEmpty()){
                BPMChangeUtil.mergeOneToMany(context,process, selectPeopleParams);
                //串签执行以后，将这个字符值设置为一个无意义的值，避免同一次操作触发了子流程，子流程也取到相同的Context对象，然后也执行了串签操作，然后报错
                context.setSelectedPeoplesOfNodes(popNodeSelected.replaceAll(IS_ORDER_EXECUTE, ""));
            }
            context.setNeedSelectPeopleNodeMap(selectPeopleParams);
            BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
            context.setStartAccountId(startActor.getParty().getAccountId());
            
            if(null==myNode){
                //运行任务事项完成事件的动作脚本
                ActionRunner.RunItemEvent(BPMEvent.WORKITEM_FINISHED, context, workitem, null);
                if (sysAutoFinishFlag) {//自动跳过标志
                    Recorder.autoFinishItem(context.getCurrentUserId(), workitem);
                } else {//人工处理完成标志
                    Recorder.finishItem(context.getCurrentUserId(), workitem);
                }
                //移到历史表中去
                itemlist.finishItem(workitem);
            }else{
                //删除环形分支线路
                deleteCircleLine(theCase, myNode);
                
                BPMHumenActivity activity = (BPMHumenActivity) myNode;
                boolean isCopyNode= isCopyNode(activity);
                BPMActor actor = (BPMActor)activity.getActorList().get(0);
                BPMParticipant party = actor.getParty();
                String partyTypeId = party.getType().id;
                if(isCopyNode && !"user".equals(partyTypeId)){
                    //用于节点复制
                	WorkflowUtil.putNodeAdditionToContext(context, activity.getId(), party, "raddition", 
                			WorkflowUtil.getNodeAdditionFromContext(context, activity.getId(), party, "addition"));
                }
                workitem.setFinisher(context.getCurrentUserId());
                theCase.setLastPerformer(workitem.getPerformer());
                //运行任务事项完成事件的动作脚本
                context.setActivateNode(activity);
                ActionRunner.RunItemEvent(BPMEvent.WORKITEM_FINISHED, context, workitem, null);
                if (sysAutoFinishFlag) {//自动跳过标志
                    Recorder.autoFinishItem(context.getCurrentUserId(), workitem);
                } else {//人工处理完成标志
                    Recorder.finishItem(context.getCurrentUserId(), workitem);
                }
                //移到历史表中去
                itemlist.finishItem(workitem);
                if (workitem.getItemNum() == 1) {//当前节点上只有一个任务事项，则流转到下一个节点
                    execute.FinishActivity(context.getCurrentUserId(), process, theCase, activity, state,
                            workitem.getNextStatus(),0);
                } else {//当前节点上有多个任务事项
                    List<WorkitemDAO> sameBatchItemList = itemlist.getWorkItemByBatch(workitem.getBatch(),
                            new Integer[]{WorkitemInfo.STATE_READY,WorkitemInfo.STATE_NEEDREDO_TOME,WorkitemInfo.STATE_SUSPENDED}, workitem);//该节点上所有待办任务事项列表
                    int unFinishedNum = sameBatchItemList.size();//该节点上待办的任务事项总数
                    // 该节点上待办的任务事项总数=0 or 单人执行模式  or 竞争执行模式
                    if (unFinishedNum == 0 || (activity.isSingleProcessMode() || activity.isCompetitionProcessMode())) {
                        if (!sameBatchItemList.isEmpty() && sameBatchItemList.size() > 0) {
                            //取消这些未办理的任务事项，执行任务事项取消事件对应的动作脚本
                            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context, workitem, sameBatchItemList);
                            itemlist.canceItems(sameBatchItemList, workitem);
                            
                            //其他人都被取消了
                            unFinishedNum = 0;
                        }
//                        if (activity.isSingleProcessMode() || activity.isCompetitionProcessMode()) {
//                            List<BPMActor> actors = activity.getActorList();
//                            BPMActor actor = actors.get(0);
//                            actor.getParty().setAddition(workitem.getPerformer());//(主要是回退的问题)
//                        }
                        if(isCopyNode && (activity.isSingleProcessMode() || activity.isCompetitionProcessMode())){
                            actor.getParty().setRaddition(workitem.getPerformer()); //用于节点复制
                            WorkflowUtil.putNodeAdditionToContext(context, activity.getId(), party, "raddition", workitem.getPerformer());
                        }
                        execute.FinishActivity(context.getCurrentUserId(), process, theCase, activity, state,
                                workitem.getNextStatus(),unFinishedNum);
                    }else if(ObjectName.isInformObject(activity)){//复合知会节点，特殊处理下
                        execute.FinishActivity(context.getCurrentUserId(), process, theCase, activity, state,
                                workitem.getNextStatus(),unFinishedNum);
                    } else {
                    	WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
                        if(context.isProcessChanged()){
                            updateRunningProcess(process,theCase,context);  
                        }
                        updateCase(theCase);
                    }
                }
                //触发子流程
                if(Strings.isNotBlank(context.getSimulationId())){
                    //流程仿真不触发子流程
                }else {
                    triggerSubProcess(context);
                }
            }
            if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                itemlist.cancelItems(domain, context);
                ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,workitem,context.getWillDeleteWorkItems());
            }
            if(!context.getEventDataContextList().isEmpty()){
                log.info(AppContext.currentUserName()+" WORKFLOW_WORKITEM_ASSIGNED  : context.getEventDataContextList().size:"+context.getEventDataContextList().size());
                ActionRunner.RunItemEvent(BPMEvent.WORKFLOW_WORKITEM_ASSIGNED,context.getEventDataContextList());
            }
            else{
                log.info(AppContext.currentUserName()+"Finish : context.getEventDataContextList().isEmpty,P:"+process.getId()+",A"+workitem.getActivityId()+",W:"+workitem.getId());
            }
            String[] result = new String[3];
            if (null != context.getNextMembers()) {
                result[0] = context.getNextMembers().toString();
                result[1] = context.getNextMembersWithoutPolicyInfo().toString();
            } else {
                result[0] = "";
                result[1] = "";
            }
            
            List<String> policyIdlists = processManager.getWorkflowUsedPolicyIds(process);
            String policyIds = Strings.join(policyIdlists, ",");
            result[2] = policyIds;
            
            
            WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.FinishWorkitem.name(), context);
            if(theCase.isFinished() && !ObjectName.isInformObject(myNode)){
                //流程实例完成
            	if(myNode!=null && !ObjectName.isInformObject(myNode)){
            		WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.ProcessFinished.name(), context);
            	}
            }
            return result;
        } catch (Throwable e) {
        	log.error("",e);
        	throw new BPMException(e.getMessage(), e);
        }
    }

    
	private void deleteCircleLine(BPMCase theCase, BPMAbstractNode myNode) {
		Set<String> clines =  ( Set<String>)theCase.getDataMap().get(ActionRunner.WF_CIRCLR_LINES);
		List<BPMCircleTransition> circles = myNode.getUpCircleTransitions();
		if(Strings.isNotEmpty(circles) && Strings.isNotEmpty(clines)){
			for(BPMCircleTransition c:circles){
				if(clines.contains(c.getId())){
					clines.remove(c.getId());
				}
			}
		}
		theCase.getDataMap().put(ActionRunner.WF_CIRCLR_LINES, clines);
	}

    /**
     * 是否为C复制节点
     * @param activity
     * @return
     */
    private boolean isCopyNode(BPMHumenActivity activity) {
        if(Strings.isNotBlank(activity.getPasteTo()) 
                && !"null".equals(activity.getPasteTo()) 
                && !"undefined".equals(activity.getPasteTo())){
            return true;
        }
        return false;
    }

    @Override
    public String[] stepBack(WorkflowBpmContext context) throws BPMException {
        String[] returnResult = new String[3];
        String activityId = context.getCurrentActivityId();
        Long workitemId = context.getCurrentWorkitemId();
        String selectTargetNodeId = context.getSelectTargetNodeId();
        log.debug("接口名称：回退流程");
        log.debug("流程当前回退节点activityId：" + activityId);
        log.debug("对应的任务事项标识currentWorkitemId：" + workitemId);
        log.debug("指定回退到的节点标识selectTargetNodeId：" + selectTargetNodeId);
        WorkitemInfo workitem = itemlist.getWorkItemOrHistory(context.getCurrentWorkitemId());
        if (workitem == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_EXITE,
                    new Object[] { context.getCurrentWorkitemId() });
        }
        BPMCase theCase = getCase(workitem.getCaseId());
        if (theCase instanceof HistoryCaseRunDAO) {
            returnResult[0] = "-1";
            return returnResult;
        }
        WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
        BPMProcess process = processManager.getRunningProcess(theCase.getProcessId());
        if (process == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_PROCESS_NOT_EXITE_IN_RUN,
                    new Object[] { theCase.getProcessId() });
        }
        BPMActivity currentActivity = process.getActivityById(activityId.toString());
        if (null == currentActivity) {
            throw new BPMException(BPMException.EXCEPTION_CODE_READYACTIVITY_NOT_EXITE_IN_CASE,
                    new Object[] { activityId });
        }
        context.setProcessId(process.getId());
        log.debug("流程定义模版标识processId：" + process.getId());
        context.setTheCase(theCase);
        context.setProcess(process);
        context.setCaseId(theCase.getId());
        String appName= theCase.getData(ActionRunner.SYSDATA_APPNAME).toString();
        context.setAppName(appName);
        List<BPMTransition> trans = currentActivity.getUpTransitions();
        BPMTransition tran = trans.get(0);
        BPMAbstractNode parentNode = (BPMAbstractNode) tran.getFrom();
        if (parentNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
            List<BPMTransition> _trans = parentNode.getDownTransitions();
            for (int i = 0; i < _trans.size(); i++) {
                BPMTransition _tran = _trans.get(i);
                BPMAbstractNode parallelismNode = _tran.getTo();
                String isPass= WorkflowUtil.getNodeConditionFromContext(context, parallelismNode, "isPass");
                String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parallelismNode, "isDelete");
                if ("failure".equalsIgnoreCase(isPass) && "false".equals(isDelete)) {
                    returnResult[0] = "-2";
                    return returnResult;
                }
            }
        }
        BPMActor startActor = (BPMActor) process.getStart().getActorList().get(0);
        context.setStartAccountId(startActor.getParty().getAccountId());
        context.setStartUserId(theCase.getStartUser());
        context.setBusinessData("sub_operationType", "stepBack");
        if (null == selectTargetNodeId || "".equals(selectTargetNodeId.trim())) {//正常回退
            log.debug("本次为正常回退");
            return normalStepBack(process, theCase, currentActivity, context);
        } else {//回退到指定节点
            log.debug("本次为回退到指定节点");
            if ("start".equals(selectTargetNodeId.trim())) {
                if ("0".equals(context.getSubmitStyleAfterStepBack())) {//流程重走方式
                    //撤销流程
                    BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
                    int result = execute.stepCaseToStarter(theCase, process);
                    if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                        itemlist.cancelItems(domain, context);
                        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,workitem,context.getWillDeleteWorkItems());
                    }
                    if (result == 0) {
                        processManager.updateProcessState(theCase.getProcessId(),
                                ProcessStateEnum.processState.startNodeRego.ordinal());
                        returnResult[0] = String.valueOf(2);
                        returnResult[1] = process.getStart().getName(); //+ "("+ process.getStart().getSeeyonPolicy().getName() + ")";
                        returnResult[2] = process.getStart().getName();
                        context.setBusinessData("sub_operationType", "stepBack");
                        cancelSubProcess(context, process.getId(), null);
                        return returnResult;
                    }
                    returnResult[0] = String.valueOf(result);
                    return returnResult;
                } else if ("1".equals(context.getSubmitStyleAfterStepBack())) {//直接提交给我方式
                    //该节点上所有待办任务事项列表
                    List<WorkitemDAO> sameBatchItemList = itemlist.getWorkItemByBatch(workitem.getBatch(),
                            new Integer[]{
                        WorkitemInfo.STATE_READY,
                        WorkitemInfo.STATE_NEEDREDO_TOME,
                        WorkitemInfo.STATE_SUSPENDED}, workitem);
                    int unFinishedNum = sameBatchItemList.size();//该节点上待办的任务事项总数
                    // 该节点上待办的任务事项总数=0 or 单人执行模式  or 竞争执行模式
                    if (unFinishedNum == 0 || (currentActivity.isSingleProcessMode() || currentActivity.isCompetitionProcessMode())) {
                        if (!sameBatchItemList.isEmpty() && sameBatchItemList.size() > 0) {
                            //取消这些未办理的任务事项，执行任务事项取消事件对应的动作脚本
                            context.setActivateNode(currentActivity);
                            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context, workitem, sameBatchItemList);
                            itemlist.canceItems(sameBatchItemList, workitem);
                        }
                    }
                    if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                        itemlist.cancelItems(domain, context);
                        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,workitem,context.getWillDeleteWorkItems());
                    }
                    //设置本节点为挂起状态，以及回退节点为激活状态
                    context.setActivateNode(currentActivity);
                    List<String> workitems = new ArrayList<String>();
                    workitems.add(workitem.getId().toString());
                    context.setSubObjectIds(workitems);
                    listener.onActivityReadyToWaiting(domain, context);
                    processManager.updateProcessState(theCase.getProcessId(),
                            ProcessStateEnum.processState.startNodeTome.ordinal());
                    theCase.getDataMap().put(ActionRunner.TO_STARTNODE_ACTIVITYID, currentActivity.getId());
                    int stepCount = theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT) == null ? 0 : Integer
                            .valueOf(String.valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
                    theCase.getDataMap().put(ActionRunner.STEPBACK_COUNT, stepCount + 1);
                    Set<String> clines =  ( Set<String>)theCase.getDataMap().get(ActionRunner.WF_CIRCLR_LINES);
                    List<BPMCircleTransition> circles = currentActivity.getDownCirlcleTransitions();
                    if(Strings.isNotEmpty(circles)){
                    	for(BPMCircleTransition c:circles){
                    		if(c.getTo().getId().equals(selectTargetNodeId.trim())){
                    			if(clines == null){
                    				clines = new HashSet<String>();
                    			}
                    			clines.add(c.getId());
                    		}
                    	}
                    }
                    theCase.getDataMap().put(ActionRunner.WF_CIRCLR_LINES, clines);
                    WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
                    caseManager.save(theCase);
                    returnResult[0] = String.valueOf(2);
                    returnResult[1] = process.getStart().getName();// + "("+ process.getStart().getSeeyonPolicy().getName() + ")";
                    returnResult[2] = process.getStart().getName();
                    return returnResult;
                }
                returnResult[0] = String.valueOf(-2);
                return returnResult;
            } else {
                BPMActivity thePreTargetNode = process.getActivityById(selectTargetNodeId);
                if (null == thePreTargetNode) {
                    throw new BPMException(BPMException.EXCEPTION_CODE_READYACTIVITY_NOT_EXITE_IN_CASE,
                            new Object[] { selectTargetNodeId });
                }
                return seniorStepback(context, process, theCase, currentActivity, workitem, thePreTargetNode);
            }
        }
    }

    /**
     * 正常回退
     * @throws BPMException 
     */
    private String[] normalStepBack(BPMProcess process, BPMCase theCase, BPMActivity activity,
            WorkflowBpmContext context) throws BPMException {
        String[] returnResult = new String[4];
        Map resultMap = WorkflowUtil.isAllHumenNodeValid(activity,theCase);
        String result_str = (String) resultMap.get("result");
        List<SubProcessRunning> runList = new ArrayList<SubProcessRunning>();
        List<String> subProcessSubjects = null;
        if ("0".equals(result_str) || "1".equals(result_str)) {//正常回退或撤销
            Map normalNodes = (Map) resultMap.get("normal_nodes");
            if ("1".equals(result_str)) {//撤销，放入start节点信息
                normalNodes.put(process.getStart().getId(), process.getStart().getId());
            }
            int result = 0;
            List<String> nodeIdList = new ArrayList<String>();
            for (Object entryObj : normalNodes.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                nodeIdList.add(entry.getValue().toString());
            }
            List<String> nodeLists = null;
            if ("0".equals(result_str)) {//正常回退的话
                nodeLists = new ArrayList<String>();
                for (String thePreTargetNodeId : nodeIdList) {
                    BPMAbstractNode thePreTargetNode= process.getActivityById(thePreTargetNodeId);
                    Set<String> passNodes= new HashSet<String>();
                    boolean isSubprocessFinished = subProcessManager.isSubprocessFinished(process, theCase,
                            thePreTargetNode, activity, nodeLists,passNodes);
                }
            }
            if ("0".equals(result_str)) {//正常回退
                //判断是否存在已经流程结束的子流程
                String[] canCancel = subProcessManager.canCancelSubProcess(process.getId(), nodeIdList);
                if (!"1".equals(canCancel[1])) {
                    log.debug("子流程已结束，主流程不允许做取回操作！");
                    returnResult[0] = "-1";
                    return returnResult;
                }
                context.setNormalStepBackTargetNodes(nodeIdList);
                result = withdrawActivity(process, theCase, activity, context);
                if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                    itemlist.cancelItems(domain, context);
                    ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,context.getWillDeleteWorkItems().get(0),context.getWillDeleteWorkItems());
                }
                if(!context.getEventDataContextList().isEmpty()){//要产生的待办
                    ActionRunner.RunItemEvent(BPMEvent.WORKFLOW_WORKITEM_ASSIGNED,context.getEventDataContextList());
                }
            } else if ("1".equals(result_str)) {//撤销
                int theCaseState = theCase.getState();
                if (theCaseState == CaseInfo.STATE_FINISHED || theCaseState == CaseInfo.STATE_CANCEL) {//流程已结束，不允许撤销
                    returnResult[0] = "-1";
                    return returnResult;
                } else {//撤销流程
                    result = cancelCase(theCase, process, context);
                }
            }
            if (result == 0) {
                //NF 正常回退，收回上节点触发的新流程,撤销子流程
                if ("0".equals(result_str)) {//正常回退的话
                    context.setBusinessData("sub_operationType", "stepBack");
                    if(nodeLists!=null && nodeLists.size()>0){
                        cancelSubProcess(context, process.getId(), nodeLists);
                    }
                }else{//回退导致流程撤销
                    context.setBusinessData("sub_operationType", "stepBack");
                    cancelSubProcess(context, process.getId(), null);
                }
                returnResult[0] = result_str;
                Set<String> returnBackNodeSet = normalNodes.keySet();
                StringBuffer preMembers = new StringBuffer();
                int k = 0;
                for (String nodeId : returnBackNodeSet) {
                    String name = "";
                    if ("start".equals(nodeId)) {
                        BPMStart start = (BPMStart) process.getStart();
                        name = start.getName();// + "(" + start.getSeeyonPolicy().getName() + ")";
                    } else {
                        BPMAbstractNode hNode = process.getActivityById(nodeId);
                        name = hNode.getName() + "(" + hNode.getSeeyonPolicy().getName() + ")";
                    }
                    if (k == 0) {
                        preMembers.append(name);
                    } else {
                        preMembers.append(",").append(name);
                    }
                }
                returnResult[1] = preMembers.toString();
                returnResult[2] = preMembers.toString();
                returnResult[3] = Strings.join(returnBackNodeSet, ",");
                //正常回退才触发“回退后事件”
                WorkflowEventExecute.execute(WorkflowEventConstants.WorkflowEventEnum.StepBack.name(), context);
                return returnResult;
            } else {
                returnResult[0] = String.valueOf(result);
                returnResult[3] = "start";
                return returnResult;
            }
        } else {//返回不能回退提示信息
            log.debug("返回流程不能回退的原因(sprint1实现)");
            returnResult[0] = result_str;
            return returnResult;
        }
    }

    
    private List<String> getNodeIds(BPMProcess process, BPMAbstractNode startNode, List<String> nodeIds, BPMAbstractNode endNode) {
        if (process != null) {
            if (!nodeIds.contains(startNode.getId())) {
                nodeIds.add(startNode.getId());
            }
            else {
                return nodeIds;
            }

            if (startNode.getId().equals(endNode.getId())) {
                return nodeIds;
            }

            List<BPMTransition> list = startNode.getDownTransitions();
            if (list != null && list.size() > 0) {
                for (BPMTransition transition : list) {
                    getNodeIds(process, transition.getTo(), nodeIds, endNode);
                }
            }

        }
        return nodeIds;
    }
    public void cancelNodes4ReG0(WorkflowBpmContext  context, BPMCase theCase,BPMProcess process,BPMAbstractNode currentNode,String appName) throws BPMException{
    	
    	//1、删除子流程
    	BPMAbstractNode endNode = (BPMAbstractNode)(process.getEnds().get(0));
    	List<String> nodeIds = new ArrayList<String>();
    	getNodeIds(process,currentNode,nodeIds,endNode);
    	if(Strings.isNotEmpty(nodeIds)){
            cancelSubProcess(context, process.getId(), nodeIds);
        }
    	
    	//2、被回退节点之后的节点 - > 回退节点 的节点还原成删除状态
    	
        BPMTransition b = (BPMTransition) currentNode.getDownTransitions().get(0);
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        execute.recoverRecursiveForStepBack(process, theCase, b.getTo(), (BPMAbstractNode)(process.getEnds().get(0)));
       
        if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
            itemlist.cancelItems(domain, context);
            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,context.getWillDeleteWorkItems().get(0),context.getWillDeleteWorkItems());
        }
        
        appName = "form".equals(context.getAppName()) ? "collaboration" : appName;
        WorkFlowAppExtendManager workFlowAppExtendManager = WorkFlowAppExtendInvokeManager.getAppManager(appName);
        if(workFlowAppExtendManager != null){
        	try {
				workFlowAppExtendManager.transReMeToReGo(context);
			} catch (BusinessException e) {
				log.error("",e);
				throw new BPMException(e);
			}
        }
        
    	//3、被回退节点本身的状态清楚
        if(theCase!=null){
        	//int stepCount= theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)==null?0:Integer.valueOf(String.valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
        	//流程重走直接将所有的都清0
        	theCase.getDataMap().put(ActionRunner.STEPBACK_COUNT,0);
        }
       
    }   
    
    
    
    
    
    /**
     * 
     * 高级回退
     * @throws BPMException  
     */
    private String[] seniorStepback(WorkflowBpmContext context, BPMProcess process, BPMCase theCase,
            BPMActivity currentActivity, WorkitemInfo workitem, BPMActivity thePreTargetNode) throws BPMException {
        String[] returnResult = new String[3];
        if (null != context.getSubmitStyleAfterStepBack()) {
            if ("0".equals(context.getSubmitStyleAfterStepBack())) {//流程重走方式
                //是否有子流程已结束
                List<String> nodeLists = new ArrayList<String>();
                Set<String> passNodes= new HashSet<String>();
                boolean isSubprocessFinished = subProcessManager.isSubprocessFinished(process, theCase,
                        thePreTargetNode, currentActivity, nodeLists,passNodes);
                if (!isSubprocessFinished) {
                    BPMTransition b = (BPMTransition) thePreTargetNode.getDownTransitions().get(0);
                    BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
                    execute.recoverRecursiveForStepBack(process, theCase, b.getTo(), currentActivity);
                    context.setBusinessData("sub_operationType", "stepBack");
                    if(null!=nodeLists && nodeLists.size()>0){
                        cancelSubProcess(context, process.getId(), nodeLists);
                    }
                    //设置被回退节点为激活状态
                    context.setActivateNode(thePreTargetNode);
                    theCase.removeReadyActivity(thePreTargetNode.getId());
                    com.seeyon.ctp.workflow.engine.log.Recorder recorder= new com.seeyon.ctp.workflow.engine.log.Recorder(theCase);
                    recorder.clearLogDetail(thePreTargetNode);
                    listener.onActivityRemove(domain,context);
                    listener.onActivityReady(domain, context, false, true);
                    recorder.SetReadyNode(thePreTargetNode, null);
                    recorder.onNodeReady(thePreTargetNode);
                    ReadyNode node = new ReadyNode(thePreTargetNode);
                    node.setNum(thePreTargetNode.getFinishNumber(theCase));
                    theCase.addReadyActivity(node);
                    WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
                    caseManager.save(theCase);
                    processManager.updateRunningProcess(process,theCase);
                    if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                        itemlist.cancelItems(domain, context);
                        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,context.getWillDeleteWorkItems().get(0),context.getWillDeleteWorkItems());
                    }
                    if(!context.getEventDataContextList().isEmpty()){
                        ActionRunner.RunItemEvent(BPMEvent.WORKFLOW_WORKITEM_ASSIGNED,context.getEventDataContextList());
                    }
                    returnResult[0] = String.valueOf(2);
                    returnResult[1] = thePreTargetNode.getName() + "(" + thePreTargetNode.getSeeyonPolicy().getName()
                            + ")";
                    returnResult[2] = thePreTargetNode.getName();
                    return returnResult;
                } else {
                    returnResult[0] = String.valueOf(3);
                    return returnResult;
                }
            } else if ("1".equals(context.getSubmitStyleAfterStepBack())) {//直接提交给我方式
                List<String> nodeIdList = new ArrayList<String>();
                nodeIdList.add(thePreTargetNode.getId());
                Set<String> passNodes= new HashSet<String>();
                boolean isSubprocessFinished = subProcessManager.isSubprocessFinished(process, theCase,
                        thePreTargetNode, thePreTargetNode, null,passNodes);
                if (isSubprocessFinished) {
                    log.debug("子流程已结束，主流程不允许做回退操作！");
                    returnResult[0] = String.valueOf(3);
                    return returnResult;
                } else {
                    //TODO 撤销子流程:先不放开,如果这里放开,则指定回退选择页面校验逻辑要修改,有一定的工作量
                    //cancelSubProcess(context, context.getProcessId(), nodeIdList);
                    //该节点上所有待办任务事项列表
                    List<WorkitemDAO> sameBatchItemList = itemlist.getWorkItemByBatch(workitem.getBatch(),
                            new Integer[]{
                        WorkitemInfo.STATE_READY,
                        WorkitemInfo.STATE_NEEDREDO_TOME,
                        WorkitemInfo.STATE_SUSPENDED}, workitem);
                    int unFinishedNum = sameBatchItemList.size();//该节点上待办的任务事项总数
                    // 该节点上待办的任务事项总数=0 or 单人执行模式  or 竞争执行模式
                    if (unFinishedNum == 0 || (currentActivity.isSingleProcessMode() || currentActivity.isCompetitionProcessMode())) {
                        if (!sameBatchItemList.isEmpty() && sameBatchItemList.size() > 0) {
                            //取消这些未办理的任务事项，执行任务事项取消事件对应的动作脚本
                            context.setActivateNode(currentActivity);
                            ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context, workitem, sameBatchItemList);
                            itemlist.canceItems(sameBatchItemList, workitem);
                        }
                    }
                    if(!context.getWillDeleteWorkItems().isEmpty()){//要删除的节点集合和要删除的事项集合
                        itemlist.cancelItems(domain, context);
                        ActionRunner.RunItemEvent(BPMEvent.WORKITEM_CANCELED, context,workitem,context.getWillDeleteWorkItems());
                    }
                    //设置本节点为挂起状态，以及回退节点为激活状态
                    context.setActivateNode(currentActivity);
                    List<String> workitems = new ArrayList<String>();
                    workitems.add(workitem.getId().toString());
                    context.setSubObjectIds(workitems);
                    listener.onActivityReadyToWaiting(domain, context);
                    int stepCount = theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT) == null ? 0 : Integer
                            .valueOf(String.valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
                    theCase.getDataMap().put(ActionRunner.STEPBACK_COUNT, stepCount + 1);
                    
                    Set<String> clines =  ( Set<String>)theCase.getDataMap().get(ActionRunner.WF_CIRCLR_LINES);
                    List<BPMCircleTransition> circles = currentActivity.getDownCirlcleTransitions();
                    if(Strings.isNotEmpty(circles)){
                    	for(BPMCircleTransition c:circles){
                    		if(c.getTo().getId().equals(thePreTargetNode.getId())){
                    			if(clines == null){
                    				clines = new HashSet<String>();
                    			}
                    			clines.add(c.getId());
                    		}
                    	}
                    }
                    theCase.getDataMap().put(ActionRunner.WF_CIRCLR_LINES, clines);
                    
                    //设置被回退节点为激活状态
                    context.setActivateNode(thePreTargetNode);
                    listener.onActivityDoneToReady(domain, context);
                    ReadyNode node = new ReadyNode(thePreTargetNode);
                    node.setNum(thePreTargetNode.getFinishNumber(theCase));
                    theCase.addReadyActivity(node);
                    WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
                    caseManager.save(theCase);
                    returnResult[0] = String.valueOf(2);
                    returnResult[1] = thePreTargetNode.getName() + "(" + thePreTargetNode.getSeeyonPolicy().getName()
                            + ")";
                    returnResult[2] = thePreTargetNode.getName();
                    return returnResult;
                }
            } else {
                returnResult[0] = String.valueOf(-2);
                return returnResult;
            }
        } else {
            returnResult[0] = String.valueOf(-2);
            return returnResult;
        }
    }
    
    
    @Override
    public String[] addNode(String processId, String currentActivityId, String targetActivityId, String userId,
            int changeType, Map<Object, Object> message, String baseProcessXML, String baseReadyObjectJSON,
            String messageDataList, String changeMessageJSON, List<BPMHumenActivity> addHumanNodes) throws BPMException {

        String[] returnObj = new String[6];
        String processXML = "";
        String readyObjectJSON = "";
        String newChangeMessageJSON = "";
        long caseId = BPMChangeUtil.getLong("caseId", message);
        BPMCase theCase = null;
        if (caseId != 0) {
            theCase = getCase(caseId);
        }
        if (null== theCase && Strings.isNotBlank(processId)) {
            theCase = caseManager.getCaseOrHistoryCaseByProcessId(processId);
        }
        if (processId != null && !"".equals(processId) && currentActivityId != null && targetActivityId != null
                && message != null && message.size() > 0) {
            BPMProcess process = null;
            BPMAbstractNode targetActivity = null; 
            BPMAbstractNode currentActivity = null;
            ReadyObject readyObj = null;
            if (baseProcessXML != null && !"".equals(baseProcessXML)) {
                process = BPMProcess.fromXML(baseProcessXML);
            } else {
                process = getProcess(processId,"");//暂不能走缓存
            }
            if (baseReadyObjectJSON != null && !"".equals(baseReadyObjectJSON)) {
                readyObj = ReadyObjectUtil.parseToReadyObject(baseReadyObjectJSON, process);
            }
            List<Map<String, Object>> newMessageDataList = null;
            if (null == messageDataList || "".equals(messageDataList.trim())) {
                newMessageDataList = new ArrayList<Map<String, Object>>();
            } else {
                newMessageDataList = (List<Map<String, Object>>) JSONUtil.parseJSONString(messageDataList);
            }
            if ("start".equals(targetActivityId)) {
                targetActivity = process.getStart();
            } else {
                targetActivity = process.getActivityById(targetActivityId);
            }
            if (targetActivity == null) {
                processXML = process.toXML(theCase,true);
            }
            if (currentActivityId.equals(targetActivityId)) {
                currentActivity = targetActivity;
            } else {
                currentActivity = process.getActivityById(targetActivityId);
            }
            if (process != null && targetActivity != null && currentActivity != null) {
                BPMChangeMessageVO vo = new BPMChangeMessageVO();
                vo.setReadyObject(readyObj);
                vo.setMessage(message);
                vo.setTargetActivityId(targetActivityId);
                vo.setTargetActivity(targetActivity);
                vo.setCurrentActivityId(currentActivityId);
                vo.setCurrentActivity(currentActivity);
                vo.setProcessId(processId);
                vo.setProcess(process);
                vo.setChangeType(changeType);
                vo.setBaseXML(baseProcessXML);
                vo.setUserId(userId);
                vo.setMessageDataList(newMessageDataList);
                vo.setChangeMessageJSON(changeMessageJSON);
                //构造要添加的用户节点列表
                BPMChangeUtil.createFromMap(vo);
                //在BPMProcess对象中将要加签的节点添加进去，并更新节点间的关系
                vo = BPMChangeUtil.addNode(vo, theCase);
                if (vo.isSuccess()) {
                    processXML = vo.getProcess().toXML(theCase,true);
                    readyObjectJSON = ReadyObjectUtil.readyObjectToJSON(vo.getReadyObject());
                    // 强制更新num，否则处理时node.getNum()仍未改变
                    if (theCase != null) {
                        caseManager.save(theCase);
                    }
                }
                returnObj[2] = String.valueOf(vo.isSuccess());
                returnObj[3] = vo.getErrorMsg();
                
                if (null != vo.getMessageDataList() && vo.getMessageDataList().size() > 0) {
                    returnObj[4] = JSONUtil.toJSONString(vo.getMessageDataList());
                } else {
                    returnObj[4] = "";
                }
                
                if(vo.getCeMevo()!=null){
                    newChangeMessageJSON = BPMChangeMergeVO.toBPMChangeMergeVOJSON(vo.getCeMevo());
                }
                
                //返回新增节点列表
                if(addHumanNodes != null){
                    addHumanNodes.clear();
                    addHumanNodes.addAll(vo.getAddedActivityList());
                    
                    //多级会签列表, 这里顺序必须放在加签节点后面
                    if(Strings.isNotEmpty(vo.getMultiStageAsignCurrentUserNodeList())){
                        addHumanNodes.addAll(vo.getMultiStageAsignCurrentUserNodeList());
                    }
                }
            }
        }
        returnObj[0] = processXML;
        returnObj[1] = readyObjectJSON;
        returnObj[5] = newChangeMessageJSON;
        return returnObj;
    }

    @Override
    public String[] addNode(String processId, String currentActivityId, String targetActivityId, String userId,
            int changeType, Map<Object, Object> message, String baseProcessXML, String baseReadyObjectJSON,
            String messageDataList, String changeMessageJSON) throws BPMException {
        return addNode(processId, currentActivityId, targetActivityId, userId, changeType, message, baseProcessXML, baseReadyObjectJSON, messageDataList, changeMessageJSON, null);
    }

    @Override
    public String[] deleteNode(String processId, String currentActivityId, String userId, List<String> activityIdList,
            String baseProcessXML,String messageDataList, String changeMessageJSON, String summaryId,String affairId) throws BPMException {
        String[] result= new String[3];
        String resultXML = "";
        String messageDataStr= "";
        String newChangeMessageJSON = "";
        if (processId != null && !"".equals(processId) && currentActivityId != null) {
            BPMProcess process = null;
            BPMActivity currentActivity = null;
            if (baseProcessXML != null && !"".equals(baseProcessXML)) {
                process = BPMProcess.fromXML(baseProcessXML);
            } else {
                process = getProcess(processId,"");//暂不走缓存
            }
            currentActivity = process.getActivityById(currentActivityId);
            if (process != null && currentActivity != null) {
                List<Map<String, Object>> newMessageDataList = null;
                if (null == messageDataList || "".equals(messageDataList.trim())) {
                    newMessageDataList = new ArrayList<Map<String, Object>>();
                } else {
                    newMessageDataList = (List<Map<String, Object>>) JSONUtil.parseJSONString(messageDataList);
                }
                BPMChangeMessageVO vo = new BPMChangeMessageVO();
                vo.setCurrentActivityId(currentActivityId);
                vo.setCurrentActivity(currentActivity);
                vo.setProcessId(processId);
                vo.setProcess(process);
                vo.setChangeType(ChangeType.DeleteNode.getKey());
                vo.setBaseXML(baseProcessXML);
                vo.setUserId(userId);
                vo.setDeleteAcitivityIdList(activityIdList);
                vo.setMessageDataList(newMessageDataList);
                vo.setChangeMessageJSON(changeMessageJSON);
                List<String> deleteNodeIds= vo.getDeleteAcitivityIdList();
                List<String> partyNames = new ArrayList<String>();
                StringBuffer processLogActionParam = new StringBuffer() ;  
                for (int i=0;i< deleteNodeIds.size();i++) {
                    String nodeId= deleteNodeIds.get(i);
                    BPMActivity node= process.getActivityById(nodeId);
                    if(node != null){
                        partyNames.add(node.getName());
                        if(i == deleteNodeIds.size() -1){
                            processLogActionParam.append(node.getName()).append("(").
                            append(node.getSeeyonPolicy().getName()).append(")") ;                         
                        }else{
                            processLogActionParam.append(node.getName()).append("(").
                            append(node.getSeeyonPolicy().getName()).append(")").append(",") ;                     
                        }   
                    }
                }
                //在BPMProcess对象中将要加签的节点添加进去，并更新节点间的关系
                vo = BPMChangeUtil.deleteNode(vo);
                Map<String, Object> messageData = new HashMap<String, Object>();
                messageData.put("operationType","deletePeople");
                messageData.put("handlerId",vo.getUserId());
                messageData.put("summaryId",summaryId);
                messageData.put("affairId",affairId);
                messageData.put("processLogParam",processLogActionParam.toString());
                messageData.put("partyNames",StringUtils.join(partyNames.iterator(), ","));
                vo.getMessageDataList().add(messageData);
                if (null != vo.getMessageDataList() && vo.getMessageDataList().size() > 0) {
                    messageDataStr = JSONUtil.toJSONString(vo.getMessageDataList());
                } else {
                    messageDataStr = "";
                }
                BPMCase theCase= caseManager.getCaseOrHistoryCaseByProcessId(processId);
                resultXML = vo.getProcess().toXML(theCase,true);
                if(vo.getCeMevo()!=null){
                	newChangeMessageJSON = BPMChangeMergeVO.toBPMChangeMergeVOJSON(vo.getCeMevo());
                }
            }
        }
        result[0]= resultXML;
        result[1]= messageDataStr;
        result[2] = newChangeMessageJSON;
        return result;
    }

    @Override
    public List<BPMHumenActivity> preDeleteNode(String processId, String currentActivityId, String baseProcessXML)
            throws BPMException {
        List<BPMHumenActivity> humenList = new UniqueList<BPMHumenActivity>();
        if (processId != null && !"".equals(processId) && currentActivityId != null) {
            BPMProcess process = null;
            BPMActivity currentActivity = null;
            if (baseProcessXML != null && !"".equals(baseProcessXML)) {
                process = BPMProcess.fromXML(baseProcessXML);
            } else {
                process = getProcess(processId,"");
            }
            currentActivity = process.getActivityById(currentActivityId);
            if (currentActivity == null) {
                return humenList;
            }
            @SuppressWarnings("unchecked")
            List<BPMTransition> transitions = currentActivity.getDownTransitions();
            for (BPMTransition trans : transitions) {
                BPMAbstractNode child = trans.getTo();
                if (child.getNodeType() == BPMAbstractNode.NodeType.humen) {
                    humenList.add((BPMHumenActivity) child);
                } else if (child.getNodeType() == BPMAbstractNode.NodeType.join
                        || child.getNodeType() == BPMAbstractNode.NodeType.split) {
                    humenList.addAll(WorkflowUtil.getChildHumens((BPMActivity) child,false));
                }
            }
        }
        return humenList;
    }

    public LockManager getLockManager() {
        return lockManager;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Override
    public void awakeCase(WorkflowBpmContext context) throws BPMException {
        Long caseId = context.getCaseId();
        log.debug("接口名称：唤醒流程");
        log.debug("流程实例IdcaseId：" + caseId);
        BPMCase currentCase = getCase(caseId);
        if (currentCase == null) {
            return;
        }
        int currentCaseState = currentCase.getState();
        //运行、取消挂起状态的流程实例不可被唤醒
        if (currentCaseState == CaseInfo.STATE_RUNNING || currentCaseState == CaseInfo.STATE_CANCEL
                || currentCaseState == CaseInfo.STATE_SUSPEND) {
            return;
        }
        WorkflowUtil.putCaseToWorkflowBPMContext(currentCase, context);
        BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
        BPMProcess process = (BPMProcess) getProcessRunningById(currentCase.getProcessId());
        context.setTheCase(currentCase);
        context.setProcess(process);
        context.setProcessId(process.getId());
        List<WorkitemInfo> hisWorkitemDaos = new ArrayList<WorkitemInfo>();
        List<BPMActivity> nodes = new ArrayList<BPMActivity>();
        switch (currentCase.getState()) {
            case CaseInfo.STATE_STOP: {
                //找到所有被终止的workitem(由于处理后workitem没有及时转入workitem_history，还要去workitem_run中去查一下)
                List<WorkitemDAO> workitem = itemlist.getWorkItemList(null, caseId, null, new Integer[]{WorkItem.STATE_STOP});
                if (workitem != null && workitem.size() > 0) {
                    for (WorkitemDAO item : workitem) {
                        if (item != null) {
                            hisWorkitemDaos.add(item);
                        }
                    }
                }
                //找到所有终止的workitem
                List<HistoryWorkitemDAO> hisWorkitem = itemlist.getHistoryWorkitemList(null, null, null, caseId, null, new Integer[]{WorkItem.STATE_STOP});
                		//(null, null, null, null, caseId,
                        //null, null, new Integer[]{WorkItem.STATE_STOP});
                if (hisWorkitem != null && hisWorkitem.size() > 0) {
                    for (HistoryWorkitemDAO item : hisWorkitem) {
                        if (item != null) {
                            hisWorkitemDaos.add(item);
                        }
                    }
                }
                //获取将要被唤醒的节点
                Map<String, BPMActivity> nodeMap = new HashMap<String, BPMActivity>();
                if (hisWorkitemDaos != null && hisWorkitemDaos.size() > 0) {
                    for (WorkitemInfo hisItem : hisWorkitemDaos) {
                        if (hisItem != null) {
                            BPMActivity node = process.getActivityById(hisItem.getActivityId());
                            //将node的id作为key，放在一个Map里避免节点重复
                            if (node != null && nodeMap.get(node.getId()) == null) {
                                nodeMap.put(node.getId(), node);
                                nodes.add(node);
                            }
                        }
                    }
                }
                break;
            }
            case CaseInfo.STATE_FINISHED: {
                @SuppressWarnings("rawtypes")
                List ends = process.getEnds();
                if (ends != null && ends.size() > 0) {
                    BPMEnd end = (BPMEnd) ends.get(0);
                    List<BPMHumenActivity> parentNodes = WorkflowUtil.findAllParentHumenActivitys(end);
                    //查询要转为待办的工作项的时候，有循环sql，这个先这么搞吧。
                    for (BPMActivity node : parentNodes) {
                        if (node != null) {
                            List<WorkitemDAO> workitem = itemlist.getWorkItemList(null, caseId,node.getId(), null, new Integer[]{WorkItem.STATE_FINISHED});
                            if (workitem != null && workitem.size() > 0) {
                                if (workitem != null && workitem.size() > 0) {
                                    for (WorkitemDAO item : workitem) {
                                        if (item != null) {
                                            hisWorkitemDaos.add(item);
                                        }
                                    }
                                }
                            }
                            List<HistoryWorkitemDAO> hisWorkitem = itemlist.getHistoryWorkitemList(null, null, null, caseId, node.getId(), new Integer[]{WorkItem.STATE_FINISHED});
                            		//(null, null, null,
                                    //null, caseId, null, node.getId(),  new Integer[]{WorkItem.STATE_FINISHED});
                            if (hisWorkitem != null && hisWorkitem.size() > 0) {
                                for (HistoryWorkitemDAO item : hisWorkitem) {
                                    if (item != null) {
                                        hisWorkitemDaos.add(item);
                                    }
                                }
                            }
                            nodes.add(node);
                        }
                    }
                }
                break;
            }
        }
        if (hisWorkitemDaos != null && hisWorkitemDaos.size() > 0) {
            if (nodes.size() > 0) {
                execute.awakeCase(currentCase, process, nodes, hisWorkitemDaos);
            }
            //将workitem从已办表移到待办表
            itemlist.awakeItems(hisWorkitemDaos);
        }
        WorkflowUtil.putWorkflowBPMContextToCase(context, currentCase);
        updateAwakeCase(currentCase, process);
    }

    private void updateAwakeCase(BPMCase theCase, BPMProcess process) throws BPMException {
        if (theCase != null) {
            BPMCase awakeCase = new CaseRunDAO();
            awakeCase.copy(theCase);
            awakeCase.setUpdateDate(new Date(System.currentTimeMillis()));
            awakeCase.setState(CaseInfo.STATE_RUNNING);
            caseManager.remove(theCase);
            caseManager.addCase(awakeCase);
        }
    }

    /**
     * 触发子流程发起.
     * @param appName 应用类型：collabortion表示协同,edoc表示公文等
     * @param currentUserId 当前处理用户
     * @param currentWorkitemId 当前sub_object_id
     * @param currentAccountId 当前处理用户所属单位
     * @param popNodeSubFlow 触发子流程所需的相关数据。
     * @throws BPMException
     */
    private void triggerSubProcess(WorkflowBpmContext context) throws BPMException {
        String popNodeSubProcessJson = context.getPopNodeSubProcessJson();
        //如果子流程的相关数据位null，什么也不做
        if (popNodeSubProcessJson == null || "".equals(popNodeSubProcessJson.trim())) {
            return;
        }
        String hasNewflow = "false";
        JSONObject popNodeNewFlowObj = null;
        if (popNodeSubProcessJson != null && !"".equals(popNodeSubProcessJson)) {
            try {
                popNodeNewFlowObj = new JSONObject(popNodeSubProcessJson);
                hasNewflow = popNodeNewFlowObj.getString("hasNewflow");
            } catch (JSONException e) {
                log.error("子流程json数据解析错误", e);
                throw new BPMException("json data of the Sub-flow parase error，json=" + popNodeSubProcessJson, e);
            }
        }
        if ("true".equals(hasNewflow)) {
            JSONArray popNodeNewFlowAr = null;
            try {
                popNodeNewFlowAr = popNodeNewFlowObj.getJSONArray("newFlows");
            } catch (JSONException e1) {
                log.error("子流程json数据解析错误，解析完的json中找不到newFlows json数组", e1);
                throw new BPMException("json data of the Sub-flow parase error: cannot find attribute 'newFlows' in the json", e1);
            }
            if (popNodeNewFlowAr != null && popNodeNewFlowAr.length() > 0) {
                List<String> nodeIds = new ArrayList<String>();
                nodeIds.add(context.getCurrentActivityId());
                List<SubProcessRunning> subList = subProcessManager.getSubProcessRunningListByMainProcessId(
                        context.getProcessId(), nodeIds, null, null);
                if (subList != null && subList.size() > 0) {
                    Map<Long, SubProcessRunning> newflowsMap = new HashMap<Long, SubProcessRunning>();
                    for (SubProcessRunning sub : subList) {
                        newflowsMap.put(sub.getId(), sub);
                    }
                    for (int j = 0; j < popNodeNewFlowAr.length(); j++) {
                        Long newflowId = null;
                        User subStartUser = null;
                        Long caseId = -1L;
                        try {
                            JSONObject jsonNewFlowObj = popNodeNewFlowAr.getJSONObject(j);
                            String newflowIdStr = jsonNewFlowObj.getString("newFlowId");
                            String senderIdStr = jsonNewFlowObj.getString("newFlowSender");
                           // boolean isCanSkip = Boolean.valueOf(jsonNewFlowObj.getString("canSkip"));
                            newflowId = Long.parseLong(newflowIdStr);
                            subStartUser = processOrgManager.getUserById(senderIdStr,true);
                            if (null == subStartUser) {//找当前登录用户
                                subStartUser = processOrgManager.getCurrentUser();
                            }
                            if (null == subStartUser || null == newflowId) {
                                continue;
                            }
                            SubProcessRunning runFlow = newflowsMap.get(newflowId);
                            WorkflowBpmContext contextNew = new WorkflowBpmContext();
                            contextNew.setBusinessData(context.getBusinessData());
                            contextNew.setDebugMode(context.isDebugMode());
                            if (contextNew.getAppName() == null || "".equals(contextNew.getAppName())) {
                                contextNew.setAppName(context.getTheCase().getData(ActionRunner.SYSDATA_APPNAME)
                                        .toString());
                            }
                            contextNew.setCanSubProcessSkipFSender(runFlow.isSkipFSender());
                         //   contextNew.setCanSubProcessSkipFSender(isCanSkip);
                            contextNew.setProcessTemplateId(String.valueOf(runFlow.getSubProcessTempleteId()));
                            contextNew.setStartUserId(subStartUser.getId());
                            contextNew.setStartUserName(subStartUser.getName());
                            contextNew.setStartAccountId(subStartUser.getAccountId());
                            contextNew.setStartAccountName(subStartUser.getAccountShortName());
                            contextNew.setSubProcess(true);
                            contextNew.setAddFirstNode(true); // 是否需要在发起者之后自动增加一个自身节点
                            contextNew.setAppObject(context.getAppObject());
                            contextNew.setBusinessData(EventDataContext.CTP_SUB_WORKFLOW_CAN_VIEW_BY_MAIN_FLOW,
                                    runFlow.getIsCanViewByMainFlow());
                            contextNew.setBusinessData(EventDataContext.CTP_WORKFLOW_MAIN_PROCESSID, context.getProcessId());
                            contextNew.setSubProcessRunningId(newflowId);
                            contextNew.setMatchRequestToken(UUIDLong.longUUID()+"");
                            listener.onCaseInitialized(domain, contextNew);
                        } catch (Throwable e) {
                            log.error(" 子流程触发生异常!", e);
                            throw new BPMException("Trigger the sub-flow error", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 撤销子流程
     * 返回值是一个数组（
     * 第一个元素的值，0表示允许撤销，-1表示不允许撤销；
     * 第二个元素的值，如果允许撤销的话，是""，如果不允许撤销，表示第一个不允许撤销的子流程的caseId
     * ）
     * @param mainProcessId
     * @param nodeIds
     * @return
     * @throws BPMException
     */
    private String[] cancelSubProcess(WorkflowBpmContext context, String mainProcessId, List<String> nodeIds)
            throws BPMException {
        String[] resultArray = null;
//        List<SubProcessRunning> finishedSubProcessList = subProcessManager
//                .getFinishedSubProcess(mainProcessId, nodeIds);
        List<SubProcessRunning> subProcessList = subProcessManager.getSubProcessRunningListByMainProcessId(mainProcessId, nodeIds);
        if(null!=subProcessList && !subProcessList.isEmpty()){
        	for (SubProcessRunning subProcessRunning : subProcessList) {
				if(subProcessRunning.getIsFinished()==1){
					resultArray = new String[2];
		            resultArray[0] = "-1";
		            resultArray[1] = String.valueOf(subProcessRunning.getSubProcessCaseId());
		            return resultArray;
				}
			}
        	
        	for (SubProcessRunning running : subProcessList) {
                WorkflowBpmContext contextNew = new WorkflowBpmContext();
                contextNew.setCaseId(running.getSubProcessCaseId());
                contextNew.setDebugMode(context.isDebugMode());
                contextNew.setAppName(context.getAppName());
                contextNew.setStartUserId(context.getStartUserId());
                contextNew.setStartUserName(context.getStartUserName());
                contextNew.setStartAccountId(context.getStartAccountId());
                contextNew.setStartAccountName(context.getStartAccountName());
                contextNew.setSubProcess(true);
                contextNew.setMatchRequestToken(UUIDLong.longUUID()+"");
                contextNew.setBusinessData("operationType",context.getBusinessData("sub_operationType"));
                int recallResult = cancelCase(contextNew);
                if (recallResult == 0) {
                    //撤销子流程, 这里不能放入待发，置为已删除。
                    running.setMainCaseId(null);
                    running.setSubProcessCaseId(null);
                    running.setSubProcessProcessId(null);
                    running.setSubProcessSenderId(null);
                    running.setIsActivate(false);
                    running.setIsDelete(false);
                    running.setUpdateTime(new Date());
                    subProcessManager.updateSubProcessRunning(running);
                }
            }
        }
        resultArray = new String[2];
        resultArray[0] = "0";
        resultArray[1] = "1";
        return resultArray;
    }

    @Override
    public void readWorkItem(Long workitemId) throws BPMException {
    	WorkitemInfo workitem= itemlist.getWorkItemOrHistory(workitemId);
        if (workitem == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_WORKITEM_NOT_EXITE,
                    new Object[] { new Long(workitemId) });
        }
        Recorder.readItem(null, workitem);
        itemlist.updateItem(workitem);
    }

    /**
     * @param processTemplateManager the processTemplateManager to set
     */
    public void setProcessTemplateManager(ProcessTemplateManager processTemplateManager) {
        this.processTemplateManager = processTemplateManager;
    }

    @Override
    public String[] runCaseToMe(WorkflowBpmContext context) throws BPMException {
        String processId = context.getProcessId();
        long caseId = context.getCaseId();
        BPMCase theCase = getCase(caseId);
        String theStepBackNodeId = (String) theCase.getDataMap().get(ActionRunner.TO_STARTNODE_ACTIVITYID);
        BPMProcess process = processManager.getRunningProcess(processId);
        BPMActivity theStepBackActivity = process.getActivityById(theStepBackNodeId);
        if (null == theStepBackActivity) {
            throw new BPMException(BPMException.EXCEPTION_CODE_STATUS_NOT_EXITE_IN_PROCESS, new Object[] {
                    theStepBackNodeId, process.getIndex() });
        } else {
        	WorkflowUtil.putCaseToWorkflowBPMContext(theCase, context);
            BPMExecute execute = new BPMExecute(domain, context, processManager, caseManager, listener);
            context.setProcess(process);
            context.setSelectTargetNodeId(theStepBackActivity.getId());
            String[] result1 = execute.startCaseToMe(theStepBackActivity, theCase);
            processManager.updateProcessState(processId, ProcessStateEnum.processState.running.ordinal());
            theCase.getDataMap().put(ActionRunner.TO_STARTNODE_ACTIVITYID, "");
            deleteCircleLine(theCase, process.getStart());
            int stepCount = theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT) == null ? 0 : Integer.valueOf(String
                    .valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
            theCase.getDataMap().put(ActionRunner.STEPBACK_COUNT, stepCount - 1);
            WorkflowUtil.putWorkflowBPMContextToCase(context, theCase);
            caseManager.save(theCase);
            String[] result = new String[6];
            result[0] = result1[0];
            result[1] = result1[1];
            result[2] = result1[2];
            result[3] = result1[3];
            result[4] = result1[4];
            return result;
        }
    }


	public void setWorkFlowMatchUserManager(WorkFlowMatchUserManager workFlowMatchUserManager) {
		this.workFlowMatchUserManager = workFlowMatchUserManager;
	}
}
