/*
 * Created on 2004-12-23
 *
 */
package com.seeyon.ctp.workflow.engine.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.dao.IWorkitemDao;
import com.seeyon.ctp.workflow.event.BPMEvent;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.event.RunEvent;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.po.WorkitemDAO;
import com.seeyon.ctp.workflow.script.WorkFlowScriptEvaluator;
import com.seeyon.ctp.workflow.util.WorkflowNodeBranchLogUtil;
import com.seeyon.ctp.workflow.util.WorkflowUtil;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMPolicies;
import net.joinwork.bpm.definition.BPMPolicy;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * 
 * <p>Title: 工作流（V3XWorkflow）</p>
 * <p>Description: 工作流流程、节点事件动作脚本执行器</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 北京致远协创软件有限公司</p>
 * <p>Author: wangchw
 * <p>Time: 2012-7-10 下午10:38:37
 */
public class ActionRunner {
    private static Log         log                     = CtpLogFactory.getLog(ActionRunner.class);
    public static final String       STEPBACK_COUNT          = "stepback_count";                      //指定回退的次数
    public static final String SYSDATA_APPNAME         = "appName";
    public static final String WF_CIRCLR_LINES         = "wf_circle_lines";
    public static final String TO_STARTNODE_ACTIVITYID = "to_startnode_activityid";             //指定回退用到
    public static final String WF_NODE_ADDITION_KEY = "wf_node_addition_key";
    public static final String WF_NODE_RADDITION_KEY = "wf_node_raddition_key";
    public static final String WF_NODE_CONDITION_CHANGE_KEY = "wf_node_condition_change_key";
    public static final String WF_DYNAMIC_FORM_KEY  = "WF_DYNAMIC_FORM_KEY"; //动态路径匹配表key的前缀，具体存的时候是key+底表ID
    
    public static final IWorkitemDao   workitemDao= (IWorkitemDao)AppContext.getBean("IWorkitemDao");

    /**
     * 根据EventId类型运行流程级别的动作脚本
     * @param domain
     * @param context
     * @param EventId
     * @throws BPMException
     */
    public static void runProcessAction(String domain, WorkflowBpmContext context, int EventId) throws BPMException {
        log.debug("执行流程启动时的动作脚本(sprint1实现)：ActionRunner.runProcessAction");
        if (EventId == BPMEvent.PROCESS_STARTED) {//流程启动事件，触发开始节点的节点后置事件动作脚本
            log.debug("流程启动了.............................");
            //            runAfterActivateNodeAction(domain, context, EventId);
            //            log.info("流程启动事件，触发开始节点的节点后置事件动作脚本");
        } else if (EventId == BPMEvent.PROCESS_FINISHED) {//流程结束事件，触发结束节点的节点前置事件动作脚本
            log.debug("流程结束了.............................");
            //            runBeforeActivateNodeAction(domain, context, EventId);
            //            log.info("流程结束事件，触发结束节点的节点前置事件动作脚本");
        } else if (EventId == BPMEvent.PROCESS_CANCELED) {//流程取消事件，触发开始节点的节点回退事件动作脚本
            log.debug("流程撤销了.............................");
            //            runNodeTakeBackAction(domain, context, EventId);
            //            log.info("流程取消事件，触发开始节点的节点回退事件动作脚本");
        } else if (EventId == BPMEvent.PROCESS_STOPPED) {//流程终止事件，触发开始节点的节点回退事件动作脚本
            log.debug("流程终止了.............................");
            //          runNodeTakeBackAction(domain, context, EventId);
            //          log.info("流程取消事件，触发开始节点的节点回退事件动作脚本");
        }
    }

    /**
     * 运行节点的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    public static void runNodeAction(String domain, WorkflowBpmContext context, int EventId) {
        if (null != context.getActivateNode()) {
            if (context.getActivateNode() instanceof BPMHumenActivity || context.getActivateNode() instanceof BPMStatus) {
                log.debug("--------------------- node action not implement---------------------------------");
                runBeforeActivateNodeAction(domain, context, EventId);
                runActivateNodeAction(domain, context, EventId);
                runAfterActivateNodeAction(domain, context, EventId);
                runNodeReturnBackAction(domain, context, EventId);
                runNodeCancelAction(domain, context, EventId);
                runNodeTakeBackAction(domain, context, EventId);
                runNodeStopAction(domain, context, EventId);
                runNodeWakeupAction(domain, context, EventId);
                runNodeWaitingAction(domain, context, EventId);
            }
        }
    }

    /**
     * 运行节点激活之前的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runBeforeActivateNodeAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- RunBeforeActivateNodeAction action not implement---------------------------------");
    }

    /**
     * 运行节点激活时的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runActivateNodeAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- RunActivateNodeAction action not implement---------------------------------");
    }

    /**
     * 运行节点激活之后的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runAfterActivateNodeAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- RunAfterActivateNodeAction action not implement---------------------------------");
    }

    /**
     * 运行节点回退的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runNodeReturnBackAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeReturnBackAction action not implement---------------------------------");
    }

    /**
     * 运行节点取消的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runNodeCancelAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeCancelAction action not implement---------------------------------");
    }

    /**
     * 运行节点取回的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runNodeTakeBackAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeTakeBackAction action not implement---------------------------------");
    }

    /**
     * 运行节点终止的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runNodeStopAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeStopAction action not implement---------------------------------");
    }

    /**
     * 运行节点唤醒的动作脚本
     * @param domain
     * @param context
     * @param EventId
     */
    private static void runNodeWakeupAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeWakeupAction action not implement---------------------------------");
    }

    private static void runNodeWaitingAction(String domain, WorkflowBpmContext context, int EventId) {
        log.debug("--------------------- runNodeWaitingAction action not implement---------------------------------");
    }

    /**
     * 运行任务事项事件监听接口
     * @param EventId
     * @param context
     * @param workitem
     * @param workitems
     * @throws BPMException
     */
    public static void RunItemEvent(int EventId, WorkflowBpmContext context, WorkItem workitem,
            List<? extends WorkitemInfo> workitems) throws BPMException {
        RunItemEvent(EventId, context, workitem, workitems, true);
    }
    
    
    /**
     * 运行任务事项事件监听接口
     * @param EventId
     * @param context
     * @param workitem
     * @param workitems
     * @throws BPMException
     */
    public static EventDataContext RunItemEvent(int EventId, WorkflowBpmContext context, WorkItem workitem,
            List<? extends WorkitemInfo> workitems,boolean fireIt) throws BPMException {
        try {
            EventDataContext edCtx = createEventDataContext(context, workitem, workitems);
            if(fireIt){
                RunEvent runEvent = BPMEvent.createRunEvent(EventId, edCtx);
                long beginTime= System.currentTimeMillis();
                runEvent.fireAppModule(); 
                long endTime= System.currentTimeMillis();
                //log.info("本次工作流监听事件回调耗时:"+(endTime-beginTime)+"ms");
            }
            return edCtx;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new BPMException(e);
        }
    }
    
    /**
     * 流程级事件回调
     * @param workflowWorkitemAssigned
     * @param eventDataContextList
     */
    public static void RunItemEvent(int EventId, List<EventDataContext> edCtxList) throws BPMException {
        try {
            //统一生成待办workitem
            int size= edCtxList.size();
            List<WorkitemDAO> workitems = new ArrayList<WorkitemDAO>();
            for(int i=0;i<size;i++){
                EventDataContext edc= edCtxList.get(i);
                workitems.addAll(edc.getWorkitemDaoList());
            }
            workitemDao.addItems(workitems);
            RunEvent runEvent = BPMEvent.createRunEvent(EventId, edCtxList);
            long beginTime= System.currentTimeMillis();
            runEvent.fireAppModule(); 
            long endTime= System.currentTimeMillis();
            //log.info("本次工作流监听事件回调耗时:"+(endTime-beginTime)+"ms");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new BPMException(e);
        }
    }

    /**
     * 创建事件上下文数据对象
     * @param context WorkflowBpmContext
     * @return
     */
    public static EventDataContext createEventDataContext(WorkflowBpmContext context, WorkItem workitem,
            List<? extends WorkitemInfo> workitems) {
        EventDataContext edCtx = new EventDataContext();
        BPMAbstractNode currentNode = null;
        if(null!=context){
            currentNode = context.getActivateNode();
            edCtx.setNormalStepBackTargetNodes(context.getNormalStepBackTargetNodes());
            edCtx.setStartAccountId(context.getStartAccountId());
            edCtx.setStartUserId(context.getStartUserId());
            edCtx.setCurrentUserId(context.getCurrentUserId());
            edCtx.setCurrentWorkitemId(context.getCurrentWorkitemId());
            edCtx.setCurrentUserName(context.getCurrentUserName());
            edCtx.setCaseId(context.getCaseId());
            edCtx.setProcessId(context.getProcessId());
            edCtx.setSubProcess(true);
            edCtx.setMainCaseId(context.getMainCaseId());
            edCtx.setMainNextNodeIds(context.getMainNextNodeIds());
            edCtx.setProcessTemplateId(context.getProcessTemplateId());
            String appName = context.getAppName();
            if(null==appName || "".equals(appName.trim())){
                appName=context.getTheCase().getData(ActionRunner.SYSDATA_APPNAME).toString();
            }
            edCtx.setAppName(appName);
            edCtx.setSendMessage(context.isSendMessage());
            edCtx.setSubProcess(context.isSubProcess());
            edCtx.setSubProcessRunningId(context.getSubProcessRunningId());
            edCtx.setStartUserName(context.getStartUserName());
            edCtx.setAppObject(context.getAppObject());
            edCtx.setBusinessData(context.getBusinessData());
            edCtx.setSubmitStyleAfterStepback(context.getSubmitStyleAfterStepBack());
            edCtx.setSelectTargetNodeId(context.getSelectTargetNodeId());
            if(Strings.isNotBlank(context.getSelectTargetNodeId()) && null!= context.getProcess()){
                if("start".equals(context.getSelectTargetNodeId())){
                    edCtx.setSelectTargetNodeName(context.getProcess().getStart().getName());
                }else{
                    edCtx.setSelectTargetNodeName(context.getProcess().getActivityById(context.getSelectTargetNodeId()).getName());
                }
            }
            edCtx.setAddFirstNode(context.isAddFirstNode());
            edCtx.setSubProcessSkipFSender(context.isCanSubProcessSkipFSender());
        }
        if (null != currentNode) {
            edCtx.setNodeId(currentNode.getId());
            edCtx.setNodeName(currentNode.getName());
            BPMSeeyonPolicy currentPolicy = currentNode.getSeeyonPolicy();
            edCtx.setPolicyId(currentPolicy.getId());
            edCtx.setAddedFromId(currentPolicy.getAddedFromId());
            edCtx.setAddedFromType(currentNode.getFromType());
            edCtx.setDealTerm(currentPolicy.getdealTerm());
            edCtx.setDealTermType(currentPolicy.getDealTermType());
            edCtx.setDealTermUserId(currentPolicy.getDealTermUserId());
            edCtx.setDealTermUserName(currentPolicy.getDealTermUserName());
            edCtx.setRemindTerm(currentPolicy.getRemindTime());
            edCtx.setDR(currentPolicy.getDR());
            edCtx.setFormViewOperation(currentPolicy.getFormViewOperation());
            edCtx.setFormApp(currentPolicy.getFormApp());
            edCtx.setfR(currentPolicy.getFR());
            edCtx.setProcessMode(currentPolicy.getProcessMode());
            edCtx.setQueryIds(currentPolicy.getQueryIds());
            edCtx.setStatisticsIds(currentPolicy.getStatisticsIds());
            if(null!=context.getNeedSelectPeopleNodeMap() && null!=context.getNeedSelectPeopleNodeMap().get(currentNode.getId()) && null!=context.getNeedSelectPeopleNodeMap().get(currentNode.getId()).get("pepole")){
                edCtx.setSelectPeople(true);
            }else{
                edCtx.setSelectPeople(false);
            }
            if(currentPolicy.getSystemAdd()!=null && "1".equals(currentPolicy.getSystemAdd())){
                edCtx.setSystemAdd(true);
            }else{
                edCtx.setSystemAdd(false);
            }
            edCtx.setMergeDealType(currentPolicy.getMergeDealType());
            
        }
        if (null != workitem) {
            edCtx.setWorkItem(workitem);
        }
        if (null != workitems) {
            List<WorkItem> list = new ArrayList<WorkItem>();
            Set<Long> filterRepeatIds= new HashSet<Long>();
            for (WorkitemInfo workitemInfo : workitems) {
            	if(!filterRepeatIds.contains(workitemInfo.getId())){
	                WorkitemInfo wi = new WorkitemInfo();
	                workitemInfo.clone2(wi);
	                list.add(wi);
	                filterRepeatIds.add(workitemInfo.getId());
            	}
            }
            edCtx.setWorkitemLists(list);
        }
        return edCtx;
    }

    private static BPMPolicy getPolicy(int EventId, BPMPolicies policies) {
        if (policies == null)
            return null;
        return policies.getPoliciesByEventId(EventId);
    }

    private static String getAction(BPMPolicy policy) {
        if (policy == null)
            return null;
        String action = policy.getAction();
        if (action == null || "".equals(action))
            return null;

        return action;
    }

    /**
     * 分支计算接口
     * @param context
     * @param condition
     * @param conditionBase
     * @return
     */
    public static boolean getConditionValue(WorkflowBpmContext context, String condition, String conditionBase) {
        boolean result = false;
        try {
            EventDataContext edCtx = createEventDataContext(context, null, null);
            edCtx.setConditionBase(conditionBase);
            AppContext.putThreadContext("workflow_condition_groovy", edCtx);
            if (null == condition) {
                condition = "";
            }
            if(condition.indexOf("<>")!=-1){
                if(condition.indexOf("include('text'")==-1 && condition.indexOf("exclude('text'")==-1 ){//升级上来的老分支条件
                    condition= condition.replaceAll("<>", "!=");
                }
            }
            //下面的这个替换主要是为了替换一些错误的分支条件，
            //分支条件中本来应该只有fieldName，但是有时又会有display，如果diaplay中有-，groovy运算会报错
            //客户bug：深圳市岁孚服装有限公司   _单选按钮作为分支条件判断，但是在流转提示分支都不满足_20131220021757 
            Pattern pattern = Pattern.compile("(compareField|compareDate|include|exclude)(\\s*\\(\\s*)('[-<>=!a-zA-Z]+')(\\s*,\\s*)((field\\d{4})|([^},()'\"]+))(\\s*,\\s*)((field\\d{4})|([^},()'\"]+)|'[^\\']*')(\\s*\\))");
            Matcher m = pattern.matcher(condition);
            StringBuffer sb = new StringBuffer();
            while(m.find()){
            	String m1 = m.group(1);
            	String m2 = m.group(2);
            	String m3 = m.group(3);
            	String m4 = m.group(4);
            	String m5 = m.group(5);//是第二个参数
            	String m6 = m.group(6);//不是null表示第二个参数是fieldName
            	String m7 = m.group(7);//不是null表示第二个参数是display
            	String m8 = m.group(8);
            	String m9 = m.group(9);//是第三个参数
            	String m10 = m.group(10);//不是null表示第三个参数是field开头，不用管
            	String m11 = m.group(11);//不是null表示第三个参数是display开头
            	String m12 = m.group(12);
            	if(m7!=null){
            		m5 = m7.replaceAll("-", WorkflowUtil.replaceUUID);
            	}
            	if(m11!=null){
            		m9 = m11.replaceAll("-", WorkflowUtil.replaceUUID);
            	}
            	m.appendReplacement(sb, m1+m2+m3+m4+m5+m8+m9+m12);
            }
            m.appendTail(sb);
            condition = sb.toString();
            //替换结束
            @SuppressWarnings("unchecked")
            Map<String, Object> formMap = (Map<String, Object>) edCtx.getBusinessData(EventDataContext.CTP_FORM_DATA);
            if (formMap == null) {
                formMap = new HashMap<String, Object>();
            }
            boolean need= condition.indexOf("&amp;")!=-1;
            while(need){
                condition= condition.replaceAll("&amp;","&");
                need= condition.indexOf("&amp;")!=-1;
            }
            condition= condition.replaceAll("&lt;&gt;", "!=").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            formMap.put("processId", context.getProcessId()); 
            
            //CAP3和CAP4区隔
            formMap.put("__isCAP4", context.isCAP4());
            
            Object resultObj= WorkFlowScriptEvaluator.eval(condition, formMap);
            WorkflowNodeBranchLogUtil.addCacheFormFieldValue(condition, formMap);
            if(null!=resultObj){
                result = (Boolean) resultObj;
            }
        } catch (Throwable e) {
            log.error("condition:=" + condition);
            log.error("conditionBase:=" + conditionBase);
            log.error("分支匹配计算失败", e);
        }
        log.debug("condition:=" + condition);
        log.debug("conditionBase:=" + conditionBase);
        log.debug("调用Groovy脚本引擎进行分支条件计算:=" + result);
        return result;
    }

}
