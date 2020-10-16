/*
 * Created on 2004-11-9
 *
 */
package net.joinwork.bpm.engine.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.engine.enums.ConditionType;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.engine.listener.ExecuteListenerList;
import com.seeyon.ctp.workflow.engine.log.Recorder;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.util.WorkflowUtil;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMConRouter;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMObject;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.log.BPMCaseLog;
import net.joinwork.bpm.engine.wapi.CaseDetailLog;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import net.joinwork.bpm.task.WorkitemInfo;

/**
 * @author dinghong
 */
public class Interpreter {
    private static Log                   log                     = CtpLogFactory.getLog(Interpreter.class);

    private String                       domain;

    private WorkflowBpmContext           context;

    private Map<String, BPMAbstractNode> returnBackNodeMap       = Collections
                                                                         .synchronizedMap(new HashMap<String, BPMAbstractNode>());

    private String                       currentReturnBackNodeId = "";

    private BPMProcess                   process;

    private BPMCase                      theCase;

    private ExecuteListenerList          listener;

    private Recorder                     recorder;

    public Interpreter(String domain, WorkflowBpmContext context, BPMProcess process, BPMCase theCase,
            ExecuteListenerList listener, Recorder recorder) {
        this.process = process;
        this.theCase = theCase;
        this.listener = listener;
        this.domain = domain;
        this.recorder = recorder;
        this.context = context;
    }

    public void AddReadyStatus(String statusId) throws BPMException {
        BPMStatus status;
        if (statusId == null) {
            status = (BPMStatus) process.getStart();
        } else {
            status = process.getStatusById(statusId);
        }
        if (status == null) {
            log.error("status(id=" + statusId + ") can not found in process(id=" + process.getId() + ")");
            throw new BPMException(BPMException.EXCEPTION_CODE_STATUS_NOT_EXITE_IN_PROCESS, new Object[] { statusId,
                    process.getIndex() });
        }
        addReadyStatus(status);
    }

    /**
     * start或end节点就绪
     * @param status
     * @throws BPMException
     */
    private void addReadyStatus(BPMStatus status) throws BPMException {
        recorder.onNodeReady(status);
        theCase.addReadyStatus(status.getId());
        if (status.getBPMObjectType() == ObjectName.BPMEnd) {
            List list = theCase.getReadyActivityList();
            List informList= theCase.getReadyInformActivityList();
            if (list != null) {
                List clonList = new ArrayList();
                clonList.addAll(list);
                if( null != informList && !informList.isEmpty() ){
                    clonList.addAll(informList);
                }
                for (int i = 0; i < clonList.size(); i++) {
                    ReadyNode readyNode = ((ReadyNode) clonList.get(i));
                    String readyActivityId = readyNode.getId();
                    BPMActivity activity = process.getActivityById(readyActivityId);
                    //join节点被删除的防护
                    if (activity == null && readyActivityId.length() >= 4 && "join".equals(readyNode.getName())) {
                        theCase.getReadyActivityList().remove((ReadyNode) clonList.get(i));
                        theCase.getReadyInformActivityList().remove((ReadyNode) clonList.get(i));
                        continue;
                    }
                    boolean isInform = ObjectName.isInformObject(activity);
                    if (!isInform) {
                        removeActivity(readyActivityId);
                    }
                }
            }
            list = theCase.getReadyStatusList();
            if (list != null) {
                List clonList = new ArrayList();
                clonList.addAll(list);
                for (int i = 0; i < clonList.size(); i++) {
                    String readyStatusId = (String) clonList.get(i);
                    removeStatus(readyStatusId, null);
                }
            }
            theCase.setEndStatusId(status.getId());
            return;
        } else {
            List list = status.getDownTransitions();
            BPMAbstractNode activity = null;
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    BPMTransition trans = (BPMTransition) list.get(i);
                    activity = transition(trans);
                    if (activity != null) {
                        break;
                    }
                }
            } else {
                // 没有下游迁移，状态结束
                removeStatus(status.getId(), null);
                return;
            }
        }
    }

    /**
     * 从case中的readyStatusList删除状态节点
     * @param statusId
     * @param nextLinkId
     * @throws BPMException
     */
    public void removeStatus(String statusId, String nextLinkId) throws BPMException {
        BPMStatus status = process.getStatusById(statusId);
        if (RemoveReadyStatus(status)) {
            recorder.onNodeRemoved(status);
        }
        return;
    }

    public void _removeStatus(String statusId, String nextLinkId) throws BPMException {
        BPMStatus status = process.getStatusById(statusId);
        if (_RemoveReadyStatus(status)) {
            /*log.debug("ready status(id=" + status.getId() + ") "
                    + status.getName() + " remove");*/
            recorder.onNodeRemoved(status);
        }
    }

    /**
     * @param activity
     */
    private void finishActivity(BPMActivity activity, int statusId, String theStepBackNodeId,boolean isNodeFinished) throws BPMException {
        List list = activity.getUpTransitions();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                BPMTransition trans = (BPMTransition) list.get(i);
                BPMObject object = (BPMObject) trans.getFrom();
                if (ObjectName.isStatusObject(object)) {
                    removeStatus(object.getId(), trans.getId());
                }
            }
        }
        if(isNodeFinished){
            recorder.onNodeFinished(activity);
            context.setActivateNode(activity);
            listener.onActivityFinished(domain, context);
        }
        if (statusId== WorkItem.STATE_NEEDREDO_TOME) {//提交至上一回退节点
            BPMActivity theStepBackActivity = process.getActivityById(theStepBackNodeId);
            if (null == theStepBackActivity) {
                throw new BPMException(BPMException.EXCEPTION_CODE_STATUS_NOT_EXITE_IN_PROCESS, new Object[] {
                        theStepBackNodeId, process.getIndex() });
            } else {
                //                this.addReadyActivity(theStepBackActivity);//to do!会产生重复待办，给流程增加一种状态：等待状态,workitem_run中也设置一个状态字段，只有状态字段有值的才可以提交.
                log.debug("提交至上一回退节点:回退节点名称=" + theStepBackActivity.getName() + ";回退节点标识="
                        + theStepBackActivity.getId());
                //更改节点statusId状态为"就绪"状态
                recorder.onNodeReady(theStepBackActivity);
                context.setActivateNode(theStepBackActivity);
                context.setSelectTargetNodeId(theStepBackNodeId);
                listener.onActivityWaitingToReady(domain, context);
                int stepCount= theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)==null?0:Integer.valueOf(String.valueOf(theCase.getDataMap().get(ActionRunner.STEPBACK_COUNT)));
                theCase.getDataMap().put(ActionRunner.STEPBACK_COUNT,stepCount-1);
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
            }
        } else {//正常提交至下一节点
            boolean isInformNode = ObjectName.isInformObject(activity);//当前节点是否为知会节点
            boolean nextIsInformAdded = false;// 知会节点加签(知会)
            if (isInformNode) {
                final BPMHumenActivity findFirstDirectHumen = NodeUtil.findFirstDirectHumen(activity);
                if (findFirstDirectHumen != null) {
                    nextIsInformAdded = findFirstDirectHumen.isAdded();// 知会节点处理时再知会
                }
            }
            if (!isInformNode || nextIsInformAdded) {
                log.info("正常提交至下一节点,"+activity.getId());
                goNext(activity,null);
            }
        }
    }

    /**
     * 修复节点状态和数据
     * @param node
     * @param endNode
     * @throws BPMException
     */
    public void recoverRecursiveForStepBack(BPMAbstractNode node, BPMAbstractNode endNode,List<String> nodeLists,Set<String> passNodes) throws BPMException {
        if(passNodes.contains(node.getId())){
            return;
        }
        passNodes.add(node.getId());
        //如果是结束结点，不删除。
        if (node instanceof BPMEnd) {
            return;
        }
        if (node.equals(endNode)) {
            removeActivity(node.getId(), true, true);
            return;
        }
        if (ObjectName.isStatusObject(node)) {
            _removeStatus(node.getId(), null);
        } else {
            removeActivity(node.getId(), true, true);
            //cancelSubProcess
            nodeLists.add(node.getId());
            recorder.clearLogDetail(node);
        }
        WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", "false");
        WorkflowUtil.putNodeConditionToContext(context, node, "isPass", "success");
        List<BPMTransition> transitions = node.getDownTransitions();
        if (transitions == null || transitions.size() == 0) {
            return;
        }
        for (int i = 0; i < transitions.size(); i++) {
            if (transitions.get(i) == null) {
                continue;
            }
            BPMAbstractNode child = transitions.get(i).getTo();
            recoverRecursiveForStepBack(child, endNode,nodeLists,passNodes);
        }
    }

    /**
     *
     */
    private boolean RemoveReadyStatus(BPMStatus status) throws BPMException {
        if (theCase.removeReadyStatus(status.getId()) != null) {
            List list = status.getDownTransitions();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    BPMTransition trans = (BPMTransition) list.get(i);
                    BPMActivity activity = (BPMActivity) trans.getTo();
                    //知会节点不进行删除操作
                    BPMSeeyonPolicy policy = activity.getSeeyonPolicy();
                    if (null != policy) {
                        if (!ObjectName.isInformObject(activity)) {
                            removeActivity(activity.getId(), false, false);
                        }
                    }
                }
            }
            return true;
        } else
            return false;
    }

    /**
     *
     */
    private boolean _RemoveReadyStatus(BPMStatus status) throws BPMException {
        if (theCase.removeReadyStatus(status.getId()) != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean RemoveReadyActivity(BPMActivity activity) throws BPMException {
        return RemoveReadyActivity(activity, false);

    }

    private boolean RemoveReadyActivity(BPMActivity activity, boolean clearAddition) throws BPMException {
        //theCase.removeReadyActivity(activity.getId());
        if (clearAddition) {
            List<BPMActor> actors = activity.getActorList();
            if (actors != null) {
                for (int i = 0; i < actors.size(); i++) {
                    actors.get(i).getParty().setAddition("");
                }
            }
            context.getNodeAdditionMap().remove(activity.getId());
            //return true;
        }
        //return false;
        if (theCase.removeReadyActivity(activity.getId()) != null) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * @param activity
     * @return
     */
    private void runActivity(BPMActivity activity) throws BPMException {
        if(activity == null){
            return;
        }
        if (activity.getFinishMode() == BPMAbstractNode.FINISHMODE_NUMBER) {//join节点
            //当前节点是join节点
            if (activity.getNodeType() == BPMAbstractNode.NodeType.join) {
                //兼容旧流程，主要是split->join或者join->join，并且当前节点的UpTransitions.size()==1的情况
                BPMAndRouter joinNode = (BPMAndRouter) activity;
                int upLinks = joinNode.getUpTransitions().size();
                if (upLinks == 1) {
                    BPMTransition tran = (BPMTransition) joinNode.getUpTransitions().get(0);
                    boolean isSplitOrJoin = ObjectName.isAndRouterObject(tran.getFrom());
                    if (isSplitOrJoin) {
                        FinishActivity(activity, -1, null,true);
                        return;
                    }
                }
                int finishNumber = joinNode.getFinishNumber(this.theCase,context);
                if (finishNumber > 0) {
                    addReadyActivity(joinNode, finishNumber);
                } else {
                    FinishActivity(joinNode, -1, null,true);
                }
                return;
            }
        }
        recorder.onNodeRun(activity);
        List list = activity.getUpTransitions();
        for (int i = 0; i < list.size(); i++) {
            BPMTransition trans = (BPMTransition) list.get(i);
            BPMObject object = (BPMObject) trans.getFrom();
            if (ObjectName.isStatusObject(object)) {
                removeStatus(object.getId(), trans.getId());
            }
        }
        goNext(activity,null);
    }

    /**
     * @param activity
     * @return seeyon. private -> public
     */
    public void addReadyActivity(BPMActivity activity) throws BPMException {
        
        log.info("addReadyActivity activity(id=" + activity.getId() + "),nodeName:"+ activity.getName());
        
        recorder = new Recorder(theCase);
        recorder.SetReadyNode(activity, null);
        recorder.onNodeReady(activity);
        context.setActivateNode(activity);
        boolean isSendMessage = context.isSendMessage();//临时保存下外面传进来的
        if (listener.onActivityReady(domain, context, true,false) == false) {
            finishActivity(activity, -1, null,true);
        } else {
            ReadyNode node = new ReadyNode(activity);
            node.setNum(activity.getFinishNumber(theCase));
            theCase.addReadyActivity(node);
        }
        context.setSendMessage(isSendMessage);//还原有的值
    }

    public void addReadyActivity(BPMActivity activity, int finishNumber) throws BPMException {
        recorder.SetReadyNode(activity, null);
        recorder.onNodeReady(activity);
        context.setActivateNode(activity);
        boolean isSendMessage = context.isSendMessage();//临时保存下外面传进来的
        if (listener.onActivityReady(domain, context, true,false) == false) {
            finishActivity(activity, -1, null,true);
        } else {
            theCase.removeReadyActivity(activity.getId());
            ReadyNode node = new ReadyNode(activity);
            node.setNum(finishNumber);
            theCase.addReadyActivity(node);
        }
        context.setSendMessage(isSendMessage);//还原有的值
    }

    /**
     * @param activity
     * @return seeyon. private -> public
     */
    public void addReadyActivity(BPMActivity activity, boolean isSendMessage,boolean isUseAdditon) throws BPMException {
        /*log.debug("activity(id=" + activity.getId() + ")  "
                + activity.getName() + " ready");*/
        recorder = new Recorder(theCase);
        recorder.SetReadyNode(activity, null);
        recorder.onNodeReady(activity);
        context.setActivateNode(activity);
        boolean isSendMessage1 = context.isSendMessage();//临时保存下外面传进来的
        if (listener.onActivityReady(domain, context, isSendMessage,isUseAdditon) == false) {
            finishActivity(activity, -1, null,true);
        } else {
            ReadyNode node = new ReadyNode(activity);
            node.setNum(activity.getFinishNumber(theCase));
            theCase.addReadyActivity(node);
        }
        context.setSendMessage(isSendMessage1);//还原有的值
    }

    public void addReadyActivity(List<BPMActivity> activity,boolean isChange) throws BPMException {
        if(null!=activity && !activity.isEmpty()){
            for (int i = 0; i < activity.size(); i++) {
                BPMActivity myActivity= activity.get(i);
                if(isChange){
                    List<BPMHumenActivity> parents= WorkflowUtil.findAllParentHumenActivitys(myActivity);
                    boolean isCanActivate= true;
                    if(null!=parents && !parents.isEmpty()){
                        for (BPMHumenActivity bpmHumenActivity : parents) {
                            if( WorkflowUtil.isThisState(theCase, bpmHumenActivity.getId(), 0,1,2) && !ObjectName.isInformObject(bpmHumenActivity)){
                                isCanActivate= false;
                                break;
                            }
                        }
                    }
                    if(isCanActivate){
                        addReadyActivity(myActivity);
                        recorder.onNodeReady(myActivity);
                        if(ObjectName.isInformObject(myActivity)){
                            goNext(myActivity, null);
                        }
                    }
                }else{
                    addReadyActivity(myActivity);
                    recorder.onNodeReady(myActivity);
                }
            }
        }
    }

    private void goNext(BPMActivity activity, String nextLinkId) throws BPMException {
        List list = activity.getDownTransitions();
        if (list == null){
            log.info(AppContext.currentUserName()+",goNext没有找到向下流转的线,ActivityID:"+activity.getId()+",NodeName:"+activity.getBPMAbstractNodeName());
            return;
        }
        else{
            log.info(AppContext.currentUserName()+",goNext,currentActivityID:"+activity.getId()+",currentNodeName:"+activity.getBPMAbstractNodeName()+",nextLinkId"+nextLinkId);
        }
        //BPMActivity nextActivity = null;
        if (nextLinkId != null && !"".equals(nextLinkId.trim())) {
            for (int i = 0; i < list.size(); i++) {
                BPMTransition trans = (BPMTransition) list.get(i);
                if (nextLinkId.equals(trans.getId())) {
                    transition(trans);
                    return;
                }
            }
            throw new BPMException(BPMException.EXCEPTION_CODE_LINK_NOT_EXITE, new Object[] { nextLinkId,
                    activity.getId(), process.getId() });
        }
        BPMTransition OtherTrans = null;
        // 条件节点
        if (activity.getBPMObjectType() == ObjectName.BPMConRouter) {
            // 找到缺省transition
            /*for (int i = 0; i < list.size(); i++) {
                BPMTransition trans = (BPMTransition) list.get(i);
                if (trans.getCondition() == null
                        || trans.getCondition().equals("")) {
                    OtherTrans = trans;
                    break;
                }
            }*/
            if (list != null && list.size() != 0) {
                OtherTrans = (BPMTransition) list.get(0);
            }
            /*// 条件计算
            for (int i = 0; i < list.size(); i++) {
                BPMTransition trans = (BPMTransition) list.get(i);
                String condition = trans.getCondition();
                if (condition != null && !condition.equals("")) {
                    if (ActionRunner.getConditionValue(domain, process,
                            theCase, trans, condition)) {
                        transition(trans);
                        return;
                    }

                }
            }*/
            if (OtherTrans != null)
                transition(OtherTrans);
        } else {
            for (int i = 0; i < list.size(); i++) {
                BPMTransition trans = (BPMTransition) list.get(i);
                if (transition(trans) != null){
                    if (activity.getSplitMode() == BPMAbstractNode.SPLITMODE_XOR){
                        return;
                    }
                }
            }
        }

    }

    /**
     * 删除该分支之后的所有节点(isDelete=true)
     * @param trans
     * @throws BPMException 
     */
    private BPMAbstractNode deleteTransition(BPMTransition trans) throws BPMException {
        BPMAbstractNode returnNode = null;
        BPMAbstractNode toNode = trans.getTo();
        if (toNode.getNodeType() == BPMAbstractNode.NodeType.end) {//结束节点,则有问题
            //do nothing
            returnNode = toNode;
        } else if (toNode.getNodeType() == BPMAbstractNode.NodeType.split) {//split节点
            //找到split节点对应的join节点,将这之间的节点全部删除掉
        	WorkflowUtil.putNodeConditionToContext(context, toNode, "isDelete", "true");
            List downList = toNode.getDownTransitions();
            for (Object object : downList) {
                BPMTransition b = (BPMTransition) object;
                deleteTransition(b);
            }
        } else if (toNode.getNodeType() == BPMAbstractNode.NodeType.join) {//join节点
            //判断是否可以穿过该join节点,不可穿过则返回该join节点
            List upList = toNode.getUpTransitions();
            boolean isCanDeleteJoin = true;
            for (Object object : upList) {
                BPMTransition b = (BPMTransition) object;
                if (!b.getId().equals(trans.getId())) {
                    BPMAbstractNode fromNode = b.getFrom();
                    String isDelete= WorkflowUtil.getNodeConditionFromContext(context,fromNode,"isDelete");
                    if ("false".equals(isDelete)) {
                        isCanDeleteJoin = false;
                    }
                }
            }
            if (isCanDeleteJoin) {
            	WorkflowUtil.putNodeConditionToContext(context, toNode, "isDelete", "true");
                deleteTransition((BPMTransition) (toNode.getDownTransitions().get(0)));
            } else {
                returnNode = toNode;
            }
        } else if (toNode.getNodeType() == BPMAbstractNode.NodeType.humen) {//humen节点
            //将该节点的isDelete属性设置为true
        	WorkflowUtil.putNodeConditionToContext(context, toNode, "isDelete", "true");
            returnNode = deleteTransition((BPMTransition) (toNode.getDownTransitions().get(0)));
        }
        if (null != returnNode && returnNode.getNodeType() == BPMAbstractNode.NodeType.end) {//结束节点
            throw new BPMException(BPMException.EXCEPTION_CODE_SYSTEM_ERROR);
        }
        return returnNode;
    }

    private BPMAbstractNode transition(BPMTransition trans) throws BPMException {
        log.info("transition：linkId:" + trans.getId() + ",fromNodeName:"+ trans.getFrom().getName()+",toNodeName:"+trans.getTo().getName());
        recorder.onTransition(trans);
        BPMAbstractNode fromActivity = (BPMAbstractNode) trans.getFrom();
        if (ObjectName.isStatusObject(trans.getTo())) {
            //知会节点的下一节点为结束节点时，不能执行removeNode()
            Boolean isRemoveNode = (trans.getTo().getBPMObjectType() == ObjectName.BPMEnd && ObjectName.isInformObject(fromActivity));

            if (!isRemoveNode) {
                removeNode(fromActivity);
            }
            addReadyStatus((BPMStatus) trans.getTo());
            return trans.getTo();
        }
        BPMActivity nextActivity = (BPMActivity) trans.getTo();
        
        
        boolean isDeleteNextNode = isDelete(nextActivity); 
        boolean isDeleteFromNode = isDelete(fromActivity); 
        
        //增加校验逻辑，如果是自动分支或者手动分支，但是Context上下文中没有相关的信息，则说明分支信息丢失，则直接抛出异常，防止错误数据继续流转！
        if(!isDeleteFromNode){
            //开始节点被删除，说明是被删除节点的后续节点，这个时候没有分支信息传递到后台，也不需要校验，只需要校验最开始的即可
            checkDataIntegrity(trans, nextActivity);
        }
        
        boolean isInformNextNode = ObjectName.isInformObject(nextActivity);
        boolean isInformFromNode = ObjectName.isInformObject(fromActivity);
        if (nextActivity.getStartMode() == BPMAbstractNode.STARTMODE_AUTO || isNotPass(fromActivity)
                || isDeleteNextNode || isDeleteFromNode) {
            if (isInformFromNode && !(isNotPass(fromActivity) || isDelete(nextActivity) || isDelete(fromActivity))) {

            } else {
                removeNode(trans.getFrom());
            }
            if (isNotPass(fromActivity)) {
            	WorkflowUtil.putNodeConditionToContext(context, nextActivity, "isPass", "failure");
            }
            
           /* log.info("nextActivity.id:"+nextActivity.getId()+",nextActivity.name:"+nextActivity.getName()+",fromActivity.id:"+fromActivity.getId()
            +",fromActivity.name:"+fromActivity.getName()+",_isDeleteNextNode:"+_isDeleteNextNode+",_isDeleteFromNode:"+_isDeleteFromNode);
            */
            if (isDeleteNextNode || isDeleteFromNode) {
            	WorkflowUtil.putNodeConditionToContext(context, nextActivity, "isDelete", "true");
            }

            if (nextActivity.getStartMode() == BPMAbstractNode.STARTMODE_AUTO) {
                List upTrans = nextActivity.getUpTransitions();
                for (int i = 0; i < upTrans.size(); i++) {
                    BPMTransition upTran = (BPMTransition) upTrans.get(i);
                    BPMAbstractNode parentNode = upTran.getFrom();
                    if (isNotDelete(parentNode)) {
                    	WorkflowUtil.putNodeConditionToContext(context, nextActivity, "isDelete", "false");
                    }
                    if (isNotPass(parentNode) && isDelete(parentNode)) {
                    	WorkflowUtil.putNodeConditionToContext(context, nextActivity, "isPass", "failure");
                    }
                }
            }

            //如果是split，且后续子节点isDelete都是true，则split的isDelete也置为true
            if ("split".equals(nextActivity.getName())) {
                List<BPMHumenActivity> subs = BPMProcess.findDirectHumenChildrenCascade(nextActivity);
                if (subs != null && subs.size() > 0) {
                    boolean isDelete = true;
                    for (BPMHumenActivity sub : subs) {
                    	String _isDelete= WorkflowUtil.getNodeConditionFromContext(context, sub, "isDelete");
                        if ("false".equals(_isDelete)) {
                            isDelete = false;
                            break;
                        }
                    }
                    if (isDelete){
                    	WorkflowUtil.putNodeConditionToContext(context, nextActivity, "isDelete", "true");
                    }
                }
            }
            boolean isNextNodeSplit =  WorkflowUtil.isSplit(nextActivity);
            
            
            if(isDeleteNextNode && isNextNodeSplit){//性能优化：如果是split节点不满足条件了，则找到对应的join节点，将这连个节点之前的所有节点置成删除标志
                Map<String,String> splitJoinMap= context.getSplitJoinMap();
                String joinNodeId= splitJoinMap.get(nextActivity.getId());
                BPMActivity joinNode= process.getActivityById(joinNodeId);
                Map<String,String> passNodes= new HashMap<String, String>();
                
                initAllNodesNotPassed(nextActivity,joinNode,passNodes);
                
                WorkflowUtil.putNodeConditionToContext(context, joinNode, "isDelete", "true");
                
                runActivity(joinNode); 
                
            }else{
                runActivity(nextActivity);
            }
            return nextActivity;
        } else if (isInformNextNode) {
            if (isActive(nextActivity)) {
                return null;
            }
            addReadyActivity(nextActivity);
            goNext(nextActivity, null);
            return nextActivity;
        } else {
            if (ObjectName.isStatusObject(trans.getFrom())) {
                addReadyActivity(nextActivity);
                return null;
            } else {
                if (isActive(nextActivity)) {
                    return null;
                }
                addReadyActivity(nextActivity);
                return nextActivity;
            }
        }
    }
    /**
     * 增加校验逻辑，如果是自动分支或者手动分支，但是Context上下文中没有相关的信息，则说明分支信息丢失，则直接抛出异常，防止错误数据继续流转！
     * @param trans
     * @param nextActivity
     * @throws BPMException
     */
    private void checkDataIntegrity(BPMTransition trans, BPMActivity nextActivity) throws BPMException {
        int conditionType = trans.getConditionType();
        if(nextActivity.getNodeType().equals(BPMAbstractNode.NodeType.join) || nextActivity.getNodeType().equals(BPMAbstractNode.NodeType.split)  ){
            return;
        }
        if (ConditionType.isAutoConditionType(conditionType) || conditionType == ConditionType.handCondition.key()) {
            
            Map<String, Map<String,String>> myMap= context.getNodeConditionChangeInfoMap();
            if(myMap.get(nextActivity.getId()) == null || myMap.get(nextActivity.getId()).get("isDelete") == null){
                
             // 知会节点进行知会操作， 有节点状态是不对的
                if(!WorkflowUtil.isThisState(this.theCase, nextActivity.getId(), CaseDetailLog.STATE_READY)){
                    
                    log.info(AppContext.currentUserName()+",没有获取到预提交执行的分支匹配结果，流程不能向下流程，请稍后重试！,trans.id:"+trans.getId()
                    +",nextActivity.id:"+nextActivity.getId()
                    +",processId:"+context.getProcessId()
                    +",conditionMap:"+myMap);
                    
                    throw new BPMException("流程分支数据不完整，接请稍后重试！");
                }
            }
        }
    }

    /**
     * 判断节点是否已被激活。
     * @param activity 节点
     * @return 被激活（在激活列表中或已办）返回<tt>true</tt>，否则返回<tt>false</tt>
     */
    private boolean isActive(BPMActivity activity) {
        final String id = activity.getId();
        // 在列表中
        boolean b = this.theCase.getReadyActivityById(id) != null;
        if (b)
            return true;
        // 不在列表中，有可能已办，查日志
        List<BPMCaseLog> caseLogs = theCase.getCaseLogList();
        int state = 0;
        for (BPMCaseLog step : caseLogs) {
            List<CaseDetailLog> frames = step.getDetailLogList();
            if (frames == null) {
                continue;
            }
            for (CaseDetailLog frame : frames) {
                String nodeId = frame.nodeId;
                if (nodeId.equals(id)) {
                    if (state != 6 || frame.getState() == 2) {
                        state = frame.getState();
                        break;
                    }
                }
            }
        }
        if (state == 3 || state == 6) {//已办 || 终止
            return true;
        }
        return false;
    }

    private boolean isNotDelete(BPMAbstractNode parentNode) {
        if ("start".equals(parentNode.getId()) || "end".equals(parentNode.getId()))
            return true;
        String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parentNode, "isDelete");
        return "false".equalsIgnoreCase(isDelete);
    }

    private boolean isNotPass(BPMAbstractNode fromActivity) {
        if ("start".equals(fromActivity.getId()) || "end".equals(fromActivity.getId()))
            return false;
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, fromActivity, "isPass");
        String isDelete= WorkflowUtil.getNodeConditionFromContext(context, fromActivity, "isDelete");
        return "failure".equalsIgnoreCase(isPass) && "true".equals(isDelete);
    }

    private boolean isDelete(BPMAbstractNode nextActivity) {
        if ("start".equals(nextActivity.getId()) || "end".equals(nextActivity.getId()))
            return false;
        String isDelete= WorkflowUtil.getNodeConditionFromContext(context, nextActivity, "isDelete");
        return "true".equalsIgnoreCase(isDelete);
    }

    /**
     * @param node
     */
    private void removeNode(BPMAbstractNode node) throws BPMException {
        if (ObjectName.isStatusObject(node)) {
            removeStatus(node.getId(), null);
        } else {
            removeActivity(node.getId(), false, false);
        }
    }

    /**
     * @param node
     */
    private void removeNodeSelf(BPMAbstractNode node) throws BPMException {
        if (ObjectName.isStatusObject(node))
            removeStatus(node.getId(), null);
        else
            removeActivity(node.getId(), true);
    }

    /**
     * @param activityId
     * @param statusId
     * @param nextActivityId
     * @throws BPMException
     */
    public void removeActivity(String activityId) throws BPMException {
        removeActivity(activityId, false);
    }

    public void removeActivity(String activityId, boolean clearLog) throws BPMException {
        removeActivity(activityId, clearLog, true);
    }

    public void removeActivity(String activityId, boolean clearLog, boolean clearAddition) throws BPMException {
        BPMActivity activity = process.getActivityById(activityId);
        String nodeId= theCase.removeReadyActivity(activityId);
        //清楚流程动态匹配表中存储的底表的值
     
        if(null==activity){
            log.warn("processId:="+process.getId()+";nodeId:="+activityId);
            return;
        }
        if (clearAddition) {
            List<BPMActor> actors = activity.getActorList();
            if (actors != null) {
                for (int i = 0; i < actors.size(); i++) {
                    actors.get(i).getParty().setAddition("");
                }
            }
            context.getNodeAdditionMap().remove(activity.getId());
        }
        context.setActivateNode(activity);
        if(Strings.isNotBlank(nodeId)){//待办节点
            if (!clearLog && !clearAddition) {
                recorder.onNodeRemoved(activity);
            } else {
                recorder.clearLogDetail(activity);
                recorder.onNodeRemoved(activity);
            }
            listener.onActivityRemove(domain, context);
        }else{//已办和分支没满足未激活节点
            if (clearAddition && clearLog) {
                recorder.clearLogDetail(activity);
                recorder.onNodeRemoved(activity);
                if(activity.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                    if("false".equals(WorkflowUtil.getNodeConditionFromContext(context, activity, "isDelete"))){//已办
                        listener.onActivityRemove(domain, context);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param activity
     * @param statusId
     * @param theStepBackNodeId
     * @throws BPMException
     */
    public void FinishActivity(BPMActivity activity, int statusId, String theStepBackNodeId,boolean isNodeFinished) throws BPMException {
        if (activity == null) {
            throw new BPMException(BPMException.EXCEPTION_CODE_READYACTIVITY_NOT_EXITE_IN_CASE, new Object[] {
                    theCase.getId(), "" });
        }
        String activityId = activity.getId();
        if(isNodeFinished){
            theCase.removeReadyActivity(activityId);
        }
        finishActivity(activity, statusId, theStepBackNodeId,isNodeFinished);
    }

    /**
     * 记录暂存待办日志
     * @param activity
     * @throws BPMException
     */
    public void zcdbActivity(BPMActivity activity) throws BPMException {
        recorder.onNodeZcdb(activity);
    }

    private void removeRecursive(BPMAbstractNode node) throws BPMException {
        removeRecursive(node, false, null, true);
    }

    /**
     * 删除指定结点及其所有子孙节点
     *
     * @param node
     * @throws BPMException
     */
    private void removeRecursive(BPMAbstractNode node, boolean clearAddition, String isDelete, boolean stepBackAll)
            throws BPMException {
        //如果是结束结点，不删除。
        if (node instanceof BPMEnd) {
            return;
        }

        boolean _stepBackAll = stepBackAll;
        String _isDelete= WorkflowUtil.getNodeConditionFromContext(context, node, "isDelete");
        if ("true".equalsIgnoreCase(_isDelete)) {
            _stepBackAll = true;
        }

        if (ObjectName.isStatusObject(node)) {
            _removeStatus(node.getId(), null);
        } else {
            if (NodeUtil.isEnableDelete(node,theCase))
                removeActivity(node.getId(), true, clearAddition);
        }

        if (isDelete != null) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", isDelete);
        }
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, node, "isPass");
        if ("failure".equals(isPass)) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isPass", "success");
        }

        boolean isBPMAndRouter = (node instanceof BPMAndRouter);
        if (!isBPMAndRouter && !_stepBackAll) {

        } else {
            List<BPMTransition> transitions = node.getDownTransitions();
            if (transitions == null || transitions.size() == 0)
                return;
            for (int i = 0; i < transitions.size(); i++) {
                if (transitions.get(i) == null)
                    continue;
                BPMAbstractNode child = transitions.get(i).getTo();
                removeRecursive(child, clearAddition, isDelete, _stepBackAll);
            }
        }
    }

    /**
     * 删除指定结点及其所有子孙节点
     *
     * @param node
     * @throws BPMException
     */
    private void removeRecursiveNew(BPMAbstractNode node, boolean clearAddition, String isDelete, boolean stepBackAll,Map<String,String> nodes)
            throws BPMException {
        //如果是结束结点，不删除。
        if(null!= nodes.get(node.getId())){
            return;
        }
        nodes.put(node.getId(), node.getId());
        if (node instanceof BPMEnd) {
            return;
        }
        
        boolean _stepBackAll = stepBackAll;
        String _isDelete= WorkflowUtil.getNodeConditionFromContext(context, node, "isDelete");
        if ("true".equalsIgnoreCase(_isDelete)) {
            _stepBackAll = true;
        }

        if (ObjectName.isStatusObject(node)) {
            _removeStatus(node.getId(), null);
        } else {
            if(!WorkflowUtil.isThisState(theCase, node.getId(), 1)){
              //if(NodeUtil.isEnableDelete(node))
                removeActivity(node.getId(), true, clearAddition);
            }
        }

        if (isDelete != null) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", isDelete);
        }
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, node, "isPass");
        if ("failure".equals(isPass)) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isPass", "success");
        }

        boolean isBPMAndRouter = (node instanceof BPMAndRouter);
        if (!isBPMAndRouter && !_stepBackAll) {

        } else {
            List<BPMTransition> transitions = node.getDownTransitions();
            if (transitions == null || transitions.size() == 0)
                return;
            for (int i = 0; i < transitions.size(); i++) {
                if (transitions.get(i) == null)
                    continue;
                BPMAbstractNode child = transitions.get(i).getTo();
                removeRecursiveNew(child, clearAddition, isDelete, _stepBackAll,nodes);
            }
        }
    }

    /**
     * 删除指定结点及其所有子孙节点
     *
     * @param node
     * @throws BPMException
     */
    private void removeRecursiveNew(BPMAbstractNode node, boolean clearAddition, String isDelete, boolean stepBackAll,
            BPMAbstractNode endNode,Map<String,String> nodeIds) throws BPMException {
        //如果是结束结点，不删除。
        if (node instanceof BPMEnd) {
            return;
        }
        if(null!=nodeIds.get(node.getId())){
            return;
        }
        nodeIds.put(node.getId(), node.getId());
        if (node.equals(endNode)) {
            return;
        }
        if (ObjectName.isStatusObject(node)) {
            _removeStatus(node.getId(), null);
        } else {
            //if(NodeUtil.isEnableDelete(node))
            removeActivity(node.getId(), true, clearAddition);
        }

        if (isDelete != null) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", isDelete);
        }
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, node, "isPass");
        if ("failure".equals(isPass)) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isPass", "success");
        }

        boolean isBPMAndRouter = (node instanceof BPMAndRouter);
        if (!isBPMAndRouter && !stepBackAll) {

        } else {
            List<BPMTransition> transitions = node.getDownTransitions();
            if (transitions == null || transitions.size() == 0)
                return;
            // 排序，需要先处理知会的节点，删除节点放到后面，否则会影响isEnableDelete中isAllInformNode的判断
            List<BPMAbstractNode> transitions2 = new LinkedList<BPMAbstractNode>();
            for (int i = 0; i < transitions.size(); i++) {
                if (transitions.get(i) == null)
                    continue;
                BPMAbstractNode child = transitions.get(i).getTo();
                if (WorkflowUtil.isInformNode(child)) {
                    transitions2.add(0, child);
                } else {
                    transitions2.add(child);
                }
            }
            for (BPMAbstractNode child : transitions2) {
                removeRecursiveNew(child, clearAddition, isDelete, stepBackAll, endNode,nodeIds);
            }
        }
    }

    /**
     * @deprecated
     * 删除指定结点及其所有子孙节点
     *
     * @param node
     * @throws BPMException
     */
    private void removeRecursive(BPMAbstractNode node, boolean clearAddition, String isDelete, boolean stepBackAll,
            BPMAbstractNode endNode) throws BPMException {
        //如果是结束结点，不删除。
        if (node instanceof BPMEnd) {
            return;
        }
        if (node.equals(endNode)) {
            return;
        }
        if (ObjectName.isStatusObject(node)) {
            _removeStatus(node.getId(), null);
        } else {
            //if(NodeUtil.isEnableDelete(node))
            removeActivity(node.getId(), true, clearAddition);
        }

        if (isDelete != null) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", isDelete);
        }
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, node, "isPass");
        if ("failure".equals(isPass)) {
        	WorkflowUtil.putNodeConditionToContext(context, node, "isPass", "success");
        }

        boolean isBPMAndRouter = (node instanceof BPMAndRouter);
        if (!isBPMAndRouter && !stepBackAll) {

        } else {
            List<BPMTransition> transitions = node.getDownTransitions();
            if (transitions == null || transitions.size() == 0)
                return;
            // 排序，需要先处理知会的节点，删除节点放到后面，否则会影响isEnableDelete中isAllInformNode的判断
            List<BPMAbstractNode> transitions2 = new LinkedList<BPMAbstractNode>();
            for (int i = 0; i < transitions.size(); i++) {
                if (transitions.get(i) == null)
                    continue;
                BPMAbstractNode child = transitions.get(i).getTo();
                if (WorkflowUtil.isInformNode(child)) {
                    transitions2.add(0, child);
                } else {
                    transitions2.add(child);
                }
            }
            for (BPMAbstractNode child : transitions2) {
                removeRecursive(child, clearAddition, isDelete, stepBackAll, endNode);
            }
        }
    }

    /**
     * 删除指定结点下所有子孙节点
     *
     * @param node
     * @throws BPMException
     */
    public void removeRecursiveExceptSelf(BPMAbstractNode node, boolean stepBackAll) throws BPMException {
        //如果是结束结点，不删除。
        if (node instanceof BPMEnd) {
            return;
        }
        Map<String,String> nodes= new HashMap<String,String>();
        nodes.put(node.getId(), node.getId());
        List<BPMTransition> transitions = node.getDownTransitions();
        for (int i = 0; i < transitions.size(); i++) {
            if (transitions.get(i) == null)
                continue;
            BPMAbstractNode child = transitions.get(i).getTo();
            //removeRecursive(child, true,"false", stepBackAll);
            removeRecursiveNew(child, true, "false", stepBackAll,nodes);
        }
    }

    public void removeAllAdditions(String isDelete) throws BPMException {
        List<BPMAbstractNode> activity = process.getActivitiesList();
        if (activity != null) {
            for (BPMAbstractNode node : activity) {
            	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", isDelete);
                if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                    List<BPMActor> actors = node.getActorList();
                    BPMActor actor = actors.get(0);
                    actor.getParty().setAddition("");
                    actor.getParty().setRaddition("");
                    context.getNodeAdditionMap().remove(node.getId());
                    context.getNodeRAdditionMap().remove(node.getId());
                }
            }
        }
    }

    /**
     * withdrawActivity()
     * 递归计算出本次回退的状态标志。
     * 注意：第一次调用本方法时currBackNode节点只能为humen(非知会节点)，后续递归调用只能是humen(知会节点)
     * @param currBackNode 人工活动节点(只能为知会或非知会)
     * @return 0: 正常回退 1：需要撤消整个流程 -1:不允许回退
     */
    public int withdrawActivity(BPMActivity currBackNode,Map<String,String> nodeIds) throws BPMException {
        //获得当前回退节点类型
        BPMAbstractNode.NodeType currBackNodeNodeType = currBackNode.getNodeType();
        //当前回退节点的为humen之外结点时，不允许回退
        if (!currBackNodeNodeType.equals(BPMAbstractNode.NodeType.humen)) {
            return -1;
        }
        //if (log.isInfoEnabled()) {
            //log.info("currentReturnBackNodeId:=" + currentReturnBackNodeId);
        //}
        //最原始的回退节点进入该方法时，对将要回退到的所有节点的有效状态进行判断
        if (null == currentReturnBackNodeId || "".equals(currentReturnBackNodeId)) {
            NodeUtil.returnBackNode.set(currBackNode);
            returnBackNodeMap.put(currentReturnBackNodeId, currBackNode);
            currentReturnBackNodeId = currBackNode.getId();
            //int result= isAllHumenNodeValid(currBackNode);
            //Map resultMap= Utils.isAllHumenNodeValid(currBackNode);
            //String result_str= String.valueOf(resultMap.get("result"));
            //if("1".equals(result_str) || "-1".equals(result_str)){//如果返回结果为1或-1，则不需要再继续后面的运算了。
            //int result= Integer.parseInt(result_str);
            //return result;
            //}
        }
        //确保NodeUtil.returnBackNode中确实是当前最初的回退节点
        BPMAbstractNode bpmThreadLocal = NodeUtil.returnBackNode.get();
        if (bpmThreadLocal != null) {
            if (!bpmThreadLocal.getId().trim().equals(currentReturnBackNodeId)) {
                NodeUtil.returnBackNode.set(returnBackNodeMap.get(currentReturnBackNodeId));
            }
        }
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();

        //获得当前回退节点的所有指向它的up线(肯定只有一条up线)
        BPMTransition currBackNodeUpLinks = (BPMTransition) currBackNode.getUpTransitions().get(0);
        //通过up线获得指向当前回退节点的from节点
        BPMAbstractNode fromNodeOfcurrBackNode = currBackNodeUpLinks.getFrom();

        //humen->humen(回退节点)
        if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
            BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity) fromNodeOfcurrBackNode;
            //        	if(!"normal".equals(fromHumenNodeOfcurrBackNode.isValid())){
            //        		return -1;
            //        	}
            String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
            //计算出当前回退节点的from节点是否为知会节点
            boolean currFromHumenNodeIsInformNode = currFromHumenNodePolicy.equals(informActivityPolicy)
                    || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
            boolean isAutoSkip= WorkflowUtil.isAutoSkip(theCase,fromHumenNodeOfcurrBackNode);
            if (currFromHumenNodeIsInformNode || isAutoSkip) {//humen(知会)->humen(回退节点)
                //清除当前回退节点的ready状态信息
                removeCurrentNode(currBackNode);
                //则继续往回退
                int result = withdrawActivity(fromHumenNodeOfcurrBackNode,nodeIds);
                return result;
            } else {//humen(非知会)->humen(回退节点)
                    //清除当前回退节点、前回退节点from节点的ready状态信息
                removeCurrentNode(currBackNode);
                removeCurrentNode(fromHumenNodeOfcurrBackNode);
                //不需要继续回退，只需为该from节点产生待办任务事项
                addReadyActivity((BPMActivity) fromHumenNodeOfcurrBackNode, false,true);
                return 0;
            }
        }

        //join->humen(回退节点)
        if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
            //清除当前回退节点的ready状态信息
            removeCurrentNode(currBackNode);
            //则继续往回退(join的扩散处理),处理完后要返回处理结果对象【唯一入口】和【唯一出口】
            Map tempMap = withdrawActivityOfJoin((BPMActivity) fromNodeOfcurrBackNode,
                    (BPMActivity) fromNodeOfcurrBackNode);
            Map<String, BPMActivity> subDesNodeSetTmp = (Map<String, BPMActivity>) tempMap.get("subDesNodeSet");
            Map<String, List<BPMActivity>> subRelationInfoSplitMapTmp = (Map<String, List<BPMActivity>>) tempMap
                    .get("subRelationInfoSplitMap");
            //必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
            if (subDesNodeSetTmp.size() == 1) {//如果最终为都归集到了split节点，则穿过该split节点
                Iterator<String> iter = subDesNodeSetTmp.keySet().iterator();
                String key = iter.next();
                BPMActivity desSplitNode = subDesNodeSetTmp.get(key);
                if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                    //删除desSplitNode节点及其子节点的ready状态信息，直到对应的join节点为止
                    BPMAndRouter join = NodeUtil.findJoin(process, desSplitNode);
                    removeCurrentNode((BPMActivity) join);
                    removeRecursiveNew(desSplitNode, true, "false", true, join,nodeIds);
                    //由于可以穿过join节点currentJoinNode对应的split节点desSplitNode，且回到第一个Join节点fromNodeOfcurrBackNode，
                    //后面如果再遇到其它join节点，则为一次全新的运算，
                    //因此后继续以split节点desSplitNode为起点，调用withdrawActivityOfSplit方法进行递归回退
                    int result1 = withdrawActivityOfSplit(desSplitNode,nodeIds);
                    return result1;
                }
            } else {//如果最终为没有都归集到了split节点，则不穿过该split节点
                Iterator<String> iter = subDesNodeSetTmp.keySet().iterator();
                for (; iter.hasNext();) {
                    String key = iter.next();
                    BPMActivity desSplitNode = subDesNodeSetTmp.get(key);
                    if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                        List<BPMActivity> lastInfoList = subRelationInfoSplitMapTmp.get(key);
                        if (lastInfoList != null) {
                            for (Iterator iterator = lastInfoList.iterator(); iterator.hasNext();) {
                                BPMActivity bpmActivity = (BPMActivity) iterator.next();
                                //递归为该知会节点bpmActivity节点产生待办任务事项
                                addReadyActivity(bpmActivity,false,true);
                                goNext(bpmActivity,null);
                            }
                        }
                    }
                }
                //分支回退特殊处理
                int delCount = 0;
                for (int i = 0; i < fromNodeOfcurrBackNode.getUpTransitions().size(); i++) {
                    BPMAbstractNode parentNode = ((BPMTransition) fromNodeOfcurrBackNode.getUpTransitions().get(i))
                            .getFrom();
                    String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parentNode, "isDelete");
                    if ("true".equals(isDelete)) {
                        delCount++;
                    }
                }
                if (delCount > 0) {
                    addReadyActivity((BPMActivity) fromNodeOfcurrBackNode,false,true);
                    ReadyNode readyNode = theCase.getReadyActivityById(fromNodeOfcurrBackNode.getId());
                    if (readyNode != null) {
                        //第一个父节点处理时，没有减1
                        readyNode.setNum(readyNode.getNum() - delCount + 1);
                    }
                }
                return 0;
            }
        }

        //split->humen(回退节点)
        if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
            //清除当前回退节点的ready状态信息
            removeCurrentNode(currBackNode);
            //获得该split节点对应的join节点
            BPMAndRouter join = NodeUtil.findJoin(process, fromNodeOfcurrBackNode);
            //删除该split节点对应的join节点ready状态
            removeCurrentNode((BPMActivity) join);
            //将该split节点及其下面所有子孙中的ready的都删掉
            removeRecursiveNew(fromNodeOfcurrBackNode, true, "false", true, join,nodeIds);
            int result = withdrawActivityOfSplit(fromNodeOfcurrBackNode,nodeIds);
            return result;
        }

        //from节点为start节点
        if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.start)) {
            //则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
            return 1;
        }
        return 0;
    }

    /**
     * withdrawActivityOfSplit()
     * 对一次遇到split节点后，递归调用该方法进行回退，直到遇到humen(非知会)为止，或者遇到join节点调用withdrawActivityOfJoin()进行处理。
     * 注意：第一调用该方法时,fromNodeOfcurrBackNode必须为split节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
     * @param fromNodeOfcurrBackNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
     * @return 返回回退结果：0: 正常回退 1：需要撤消整个流程 -1:不允许回退
     * @throws BPMException
     */
    private int withdrawActivityOfSplit(BPMAbstractNode fromNodeOfcurrBackNode,Map<String,String> nodeIds) throws BPMException {
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
        //获得该fromNodeOfcurrBackNode节点的所有指向它的up线(肯定只有一条up线)
        BPMTransition fromNodeOfcurrBackNodeUpLinks = (BPMTransition) fromNodeOfcurrBackNode.getUpTransitions().get(0);
        //通过up线获得指向该fromNodeOfcurrBackNode节点的from节点
        BPMAbstractNode fromNodeOfcurrSplitNode = fromNodeOfcurrBackNodeUpLinks.getFrom();
        //humen->split->split->humen(回退节点)
        if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
            BPMHumenActivity fromHumenNodeOfSplitNode = (BPMHumenActivity) fromNodeOfcurrSplitNode;
            //			if(!"normal".equals(fromHumenNodeOfSplitNode.isValid())){
            //	       		return -1;
            //	       	}
            String currFromHumenNodePolicy = fromHumenNodeOfSplitNode.getSeeyonPolicy().getId();
            //计算出当前回退节点的from节点是否为知会节点
            boolean currFromHumenNodeIsInformNode = currFromHumenNodePolicy.equals(informActivityPolicy)
                    || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
            boolean isAutoSkip= WorkflowUtil.isAutoSkip(theCase,fromHumenNodeOfSplitNode);
            if (currFromHumenNodeIsInformNode || isAutoSkip) {//humen(知会)->split->split->humen(回退节点)
                //删除该fromNodeOfcurrBackNode节点的ready状态
                removeCurrentNode((BPMActivity) fromNodeOfcurrBackNode);
                //则继续往回退
                int result = withdrawActivityOfSplit(fromHumenNodeOfSplitNode,nodeIds);
                return result;
            } else {//humen(非知会)->split->split->humen(回退节点)
                    //保留当前非知会节点的Actor信息
                BPMAbstractNode splitNode = NodeUtil.findDirectSplitNodeForSplit(fromNodeOfcurrBackNode);
                if (splitNode != null && !fromNodeOfcurrBackNode.getId().equals(splitNode.getId())) {
                    //获得该split节点对应的join节点
                    BPMAndRouter join = NodeUtil.findJoin(process, splitNode);
                    //删除该split节点对应的join节点ready状态
                    removeCurrentNode((BPMActivity) join);
                    //将该split节点及其下面所有子孙中的ready的都删掉
                    removeRecursiveNew(splitNode, true, "false", true, join,nodeIds);
                }
                removeCurrentNode((BPMActivity) fromNodeOfcurrBackNode);
                //删除该humen(非知会)节点的ready状态
                removeCurrentNode(fromHumenNodeOfSplitNode);
                //不需要继续回退，只需为该from节点产生待办任务事项
                addReadyActivity((BPMActivity) fromHumenNodeOfSplitNode,false,true);
                return 0;
            }
        }
        //split->split->split->humen(回退节点)
        if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
            //删除split节点及其子节点的ready状态信息
            BPMAndRouter join = NodeUtil.findJoin(process, fromNodeOfcurrSplitNode);
            removeCurrentNode((BPMActivity) join);
            //将a及其下面所有子孙中的ready的都删掉
            removeRecursiveNew(fromNodeOfcurrSplitNode, true, "false", true, join,nodeIds);
            removeCurrentNode((BPMActivity) fromNodeOfcurrBackNode);
            int result = withdrawActivityOfSplit(fromNodeOfcurrSplitNode,nodeIds);
            return result;
        }
        //join->split->split->humen(回退节点)
        if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
            removeCurrentNode((BPMActivity) fromNodeOfcurrBackNode);
            //则继续往回退(join的扩散处理),处理完后要返回处理结果对象【唯一入口】和【唯一出口】
            Map tempMap = withdrawActivityOfJoin((BPMActivity) fromNodeOfcurrSplitNode,
                    (BPMActivity) fromNodeOfcurrSplitNode);
            Map<String, BPMActivity> subDesNodeSetTmp = (Map<String, BPMActivity>) tempMap.get("subDesNodeSet");
            Map<String, List<BPMActivity>> subRelationInfoSplitMapTmp = (Map<String, List<BPMActivity>>) tempMap
                    .get("subRelationInfoSplitMap");
            //必须要对join节点进行评估计算(收缩处理)，看是否需要穿透其对应的split节点
            if (subDesNodeSetTmp.size() == 1) {//如果最终为都归集到了split节点，则穿过该split节点
                Iterator<String> iter = subDesNodeSetTmp.keySet().iterator();
                String key = iter.next();
                BPMActivity desSplitNode = subDesNodeSetTmp.get(key);
                if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                    //删除desSplitNode节点及其子节点的ready状态信息，直到对应的join节点为止
                    BPMAndRouter join = NodeUtil.findJoin(process, desSplitNode);
                    removeCurrentNode((BPMActivity) join);
                    removeRecursiveNew(desSplitNode, true, "false", true, join,nodeIds);
                    //由于可以穿过join节点fromNodeOfcurrSplitNode对应的split节点desSplitNode，且回到第一个Join节点fromNodeOfcurrSplitNode，
                    //后面如果再遇到其它join节点，则为一次全新的运算，
                    //因此后继续以split节点desSplitNode为起点，调用withdrawActivityOfSplit方法进行递归回退
                    int result1 = withdrawActivityOfSplit(desSplitNode,nodeIds);
                    return result1;
                }
            } else {//如果最终为没有都归集到了split节点，则不穿过该split节点
                Iterator<String> iter = subDesNodeSetTmp.keySet().iterator();
                for (; iter.hasNext();) {
                    String key = iter.next();
                    BPMActivity desSplitNode = subDesNodeSetTmp.get(key);
                    if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                        List<BPMActivity> lastInfoList = subRelationInfoSplitMapTmp.get(key);
                        if (lastInfoList != null) {
                            for (Iterator iterator = lastInfoList.iterator(); iterator.hasNext();) {
                                BPMActivity bpmActivity = (BPMActivity) iterator.next();
                                //递归为该知会节点bpmActivity节点产生待办任务事项
                                addReadyActivity(bpmActivity,false,true);
                                goNext(bpmActivity,null);
                            }
                        }
                    }
                }
                //分支回退特殊处理
                int delCount = 0;
                for (int i = 0; i < fromNodeOfcurrSplitNode.getUpTransitions().size(); i++) {
                    BPMAbstractNode parentNode = ((BPMTransition) fromNodeOfcurrSplitNode.getUpTransitions().get(i))
                            .getFrom();
                    String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parentNode, "isDelete");
                    if ("true".equals(isDelete)) {
                        delCount++;
                    }
                }
                if (delCount > 0) {
                    addReadyActivity((BPMActivity) fromNodeOfcurrSplitNode,false,true);
                    ReadyNode readyNode = theCase.getReadyActivityById(fromNodeOfcurrSplitNode.getId());
                    if (readyNode != null) {
                        //第一个父节点处理时，没有减1
                        readyNode.setNum(readyNode.getNum() - delCount + 1);
                    }
                }
                return 0;
            }
        }
        //start->split->split->humen(回退节点)
        if (fromNodeOfcurrSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.start)) {
            //则不再回退，返回1，表示撤消流程，变为待发，让ColHelper根据1进行流程撤销的业务处理
            return 1;
        }
        return 0;
    }

    /**
     * withdrawActivityOfJoin()
     * 对一次遇到join节点后，递归调用该方法进行回退，直到遇到与之对应的split节点或humen(非知会)为止。
     * 注意：第一调用该方法时,firstJoinNode和currentJoinNode必须为同一个join节点对象，后面递归过程currentJoinNode可以为join/split/humen(知会)
     * @param firstJoinNode 第一次调用该方法传入的join节点对象
     * @param currentJoinNode 递归调用过程中传入的节点对象，可以为join/split/humen(知会)
     * @return Map:
     *         <"subDesNodeSet",本次递归遇到的没能穿过的节点集合(由1个split节点或多个humen(非知会)组成)>
     *         <"subRelationInfoSplitMap",本次递归遇到没能穿过的split节点指向的所有humen(非知会)节点列表>
     * @throws BPMException
     */
    private Map withdrawActivityOfJoin(BPMActivity firstJoinNode, BPMActivity currentJoinNode) throws BPMException {
        //为每个currentJoinNode节点定义2个临时存储递归过程运算的结果信息subDesNodeSet和subRelationInfoSplitMap
        Map<String, BPMActivity> subDesNodeSet = new HashMap<String, BPMActivity>();
        Map<String, List<BPMActivity>> subRelationInfoSplitMap = new HashMap<String, List<BPMActivity>>();
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();

        //循环遍历节点的所有up线
        List links_ba = currentJoinNode.getUpTransitions();
        for (Iterator iterator = links_ba.iterator(); iterator.hasNext();) {
            BPMTransition upLink = (BPMTransition) iterator.next();
            BPMAbstractNode fromNode = upLink.getFrom();
            String fromNodeIsDelete= WorkflowUtil.getNodeConditionFromContext(context, fromNode, "isDelete");
            if ("false".equals(fromNodeIsDelete)) {//from节点没有被删除
                //对from节点的类型进行判断
                BPMAbstractNode.NodeType fromNodeType = fromNode.getNodeType();
                if (fromNodeType.equals(BPMAbstractNode.NodeType.join)) {//join->join
                    //将currentJoinNode的状态删除
                    removeCurrentNode(currentJoinNode);
                    //则以fromNode节点为当前回退节点继续回退（递归调用）
                    Map tempMap = withdrawActivityOfJoin(firstJoinNode, (BPMActivity) fromNode);
                    //将fromNode节点递归调用返回的运算结果合并到currentJoinNode节点的运算结果中
                    Map<String, BPMActivity> subDesNodeSetTmp = (Map<String, BPMActivity>) tempMap.get("subDesNodeSet");
                    Map<String, List<BPMActivity>> subRelationInfoSplitMapTmp = (Map<String, List<BPMActivity>>) tempMap
                            .get("subRelationInfoSplitMap");
                    subDesNodeSet.putAll(subDesNodeSetTmp);
                    if (subRelationInfoSplitMapTmp.size() > 0) {
                        Iterator<String> iterSplitTmp = subRelationInfoSplitMapTmp.keySet().iterator();
                        while (iterSplitTmp.hasNext()) {
                            String splitIdTmp = iterSplitTmp.next();
                            List<BPMActivity> lastInfoList = subRelationInfoSplitMap.get(splitIdTmp);
                            if (lastInfoList != null) {
                                lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
                            } else {
                                subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
                            }
                        }
                    }
                }
                if (fromNodeType.equals(BPMAbstractNode.NodeType.humen)) {//humen->join
                    BPMHumenActivity fromNodeOfJoinNode = (BPMHumenActivity) fromNode;
                    String currFromHumenNodePolicy = fromNode.getSeeyonPolicy().getId();
                    //计算出当前回退节点的from节点是否为知会节点
                    boolean currFromHumenNodeIsInformNode = currFromHumenNodePolicy.equals(informActivityPolicy)
                            || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
                    boolean isAutoSkip= WorkflowUtil.isAutoSkip(theCase,fromNode);
                    if (currFromHumenNodeIsInformNode || isAutoSkip) {//humen(知会)->join
                        //将currentJoinNode的状态删除
                        removeCurrentNode(currentJoinNode);
                        //则以fromNodeOfJoinNode节点为当前回退节点继续回退（递归调用）
                        Map tempMap = withdrawActivityOfJoin(firstJoinNode, fromNodeOfJoinNode);
                        //将fromNodeOfJoinNode节点递归调用返回的运算结果合并到currentJoinNode节点的运算结果中
                        Map<String, BPMActivity> subDesNodeSetTmp = (Map<String, BPMActivity>) tempMap
                                .get("subDesNodeSet");
                        Map<String, List<BPMActivity>> subRelationInfoSplitMapTmp = (Map<String, List<BPMActivity>>) tempMap
                                .get("subRelationInfoSplitMap");
                        subDesNodeSet.putAll(subDesNodeSetTmp);
                        if (subRelationInfoSplitMapTmp.size() > 0) {
                            Iterator<String> iterSplitTmp = subRelationInfoSplitMapTmp.keySet().iterator();
                            while (iterSplitTmp.hasNext()) {
                                String splitIdTmp = iterSplitTmp.next();
                                List<BPMActivity> lastInfoList = subRelationInfoSplitMap.get(splitIdTmp);
                                if (lastInfoList != null) {
                                    lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
                                } else {
                                    subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
                                }
                            }
                        }
                    } else {//humen(非知会)->join
                            //将currentJoinNode节点及其下面所有子孙中的ready的都删掉
                        Map<String,String> nodes= new HashMap<String,String>();
                        removeRecursiveNew(currentJoinNode, true, "false", true,nodes);
                        //将fromNodeOfJoinNode节点ready的删掉
                        removeCurrentNode(fromNodeOfJoinNode);
                        //不需要继续回退，只需为该fromNodeOfJoinNode节点产生待办任务事项
                        addReadyActivity(fromNodeOfJoinNode, false,true);
                        //将该非知会节点fromNodeOfJoinNode对象作为一个终点对象保存到subDesNodeSet中
                        subDesNodeSet.put(fromNodeOfJoinNode.getId() + "", fromNodeOfJoinNode);
                    }
                }
                if (fromNodeType.equals(BPMAbstractNode.NodeType.split)) {//split--中间穿透过一些节点--->join
                    //清除当前回退节点的ready状态信息
                    removeCurrentNode(currentJoinNode);
                    //将该非知会节点对象作为一个终点对象保存到subDesNodeSet中
                    subDesNodeSet.put(fromNode.getId() + "", (BPMActivity) fromNode);
                    List<BPMActivity> lastInfoList = subRelationInfoSplitMap.get(fromNode.getId());
                    if (lastInfoList == null) {
                        lastInfoList = new ArrayList<BPMActivity>();
                    }
                    //注意：currentJoinNode肯定为知会节点，才会出现fromNode为split节点
                    lastInfoList.add(currentJoinNode);
                    subRelationInfoSplitMap.put(fromNode.getId() + "", lastInfoList);
                }
            }
        }
        //分支回退特殊处理 
        if (currentJoinNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
            //还没回到第一个Join节点firstJoinNode，则做如下处理
            if (!firstJoinNode.getId().equals(currentJoinNode.getId())) {
                if (subDesNodeSet.size() == 1) {
                    //如果当前join节点currentJoinNode通过递归回退后，最终为都归集到了split节点，则穿过该split节点
                    Iterator<String> iter = subDesNodeSet.keySet().iterator();
                    String key = iter.next();
                    BPMActivity desSplitNode = subDesNodeSet.get(key);
                    if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                        //删除desSplitNode节点的ready状态信息
                        removeCurrentNode(desSplitNode);
                        //由于可以穿过join节点currentJoinNode对应的split节点desSplitNode，且还没回到第一个Join节点firstJoinNode，
                        //则清除currentJoinNode的运算结果，因为这个currentJoinNode节点的运算信息留着无意义，
                        //如果留着，则会对及其它join节点的递归运算判断产生影响
                        subDesNodeSet.remove(key);
                        subRelationInfoSplitMap.remove(key);
                        //由于可以穿过split节点，则继续以desSplitNode节点为当前回退节点继续回退（递归调用）
                        Map tempMap = withdrawActivityOfJoin(firstJoinNode, desSplitNode);
                        //将desSplitNode节点递归调用返回的运算结果合并到currentJoinNode节点的运算结果中
                        Map<String, BPMActivity> subDesNodeSetTmp = (Map<String, BPMActivity>) tempMap
                                .get("subDesNodeSet");
                        Map<String, List<BPMActivity>> subRelationInfoSplitMapTmp = (Map<String, List<BPMActivity>>) tempMap
                                .get("subRelationInfoSplitMap");
                        subDesNodeSet.putAll(subDesNodeSetTmp);
                        if (subRelationInfoSplitMapTmp.size() > 0) {
                            Iterator<String> iterSplitTmp = subRelationInfoSplitMapTmp.keySet().iterator();
                            while (iterSplitTmp.hasNext()) {
                                String splitIdTmp = iterSplitTmp.next();
                                List<BPMActivity> lastInfoList = subRelationInfoSplitMap.get(splitIdTmp);
                                if (lastInfoList != null) {
                                    lastInfoList.addAll(subRelationInfoSplitMapTmp.get(splitIdTmp));
                                } else {
                                    subRelationInfoSplitMap.put(splitIdTmp, subRelationInfoSplitMapTmp.get(splitIdTmp));
                                }
                            }
                        }
                    }
                } else {//如果最终为没有都归集到了split节点，则不穿过该split节点
                    Iterator<String> iter = subDesNodeSet.keySet().iterator();
                    for (; iter.hasNext();) {
                        String key = iter.next();
                        BPMActivity desSplitNode = subDesNodeSet.get(key);
                        if (desSplitNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
                            List<BPMActivity> lastInfoList = subRelationInfoSplitMap.get(key);
                            if (lastInfoList != null) {
                                for (Iterator iterator = lastInfoList.iterator(); iterator.hasNext();) {
                                    BPMActivity bpmActivity = (BPMActivity) iterator.next();
                                    //递归为该知会节点bpmActivity节点产生待办任务事项
                                    addReadyActivity(bpmActivity,false,true);
                                    goNext(bpmActivity,null);
                                }
                            }
                        }
                    }
                    subRelationInfoSplitMap.clear();
                    //分支回退特殊处理
                    int delCount = 0;
                    for (int i = 0; i < links_ba.size(); i++) {
                        BPMAbstractNode parentNode = ((BPMTransition) links_ba.get(i)).getFrom();
                        String isDelete= WorkflowUtil.getNodeConditionFromContext(context, parentNode, "isDelete");
                        if ("true".equals(isDelete)) {
                            delCount++;
                        }
                    }
                    if (delCount > 0) {
                        addReadyActivity(currentJoinNode,false,true);
                        ReadyNode readyNode = theCase.getReadyActivityById(currentJoinNode.getId());
                        if (readyNode != null) {
                            //第一个父节点处理时，没有减1
                            readyNode.setNum(readyNode.getNum() - delCount + 1);
                        }
                    }
                }
            }
        }
        //返回currentJoinNode的递归运算结果
        Map tempMap = new HashMap();
        tempMap.put("subDesNodeSet", subDesNodeSet);
        tempMap.put("subRelationInfoSplitMap", subRelationInfoSplitMap);
        return tempMap;
    }

    /**
     * 删除节点操作
     * @param a当前节点
     * @return
     * @throws BPMException
     */
    public int deleteActivity(BPMActivity a, boolean frontad) throws BPMException {
        removeCurrentNode(a);
        if (frontad) {
            goNext(a,null);
        }
        return 0;
    }

    public void removeCurrentNode(BPMActivity activity) throws BPMException {
        theCase.removeReadyActivity(activity.getId());
        //log.debug("ready activity(id=" + activity.getId() + ")" + activity.getName() + "remove");
        recorder.clearLogDetail(activity);
        recorder.onNodeRemoved(activity);
        context.setActivateNode(activity);
        listener.onActivityRemove(domain, context);
    }

    /**
     * stop activtiy Jincm add 2007-04-28
     * @param activity
     */
    private void _takeBackActivity1(BPMActivity activity, String statusId, String nextLinkId) throws BPMException {
        String isPass= WorkflowUtil.getNodeConditionFromContext(context, activity, "isPass");
        if ("failure".equals(isPass)) {
        	WorkflowUtil.putNodeConditionToContext(context, activity, "isPass", "success");
        }
        recorder = new Recorder(theCase);
        recorder.SetReadyNode(activity, null);
        recorder.onNodeReady(activity);
        listener.onActivityTackBack(domain, context);
        ReadyNode node = new ReadyNode(activity);
        node.setNum(activity.getFinishNumber(theCase));
        if (theCase.getReadyActivityById(node.getId()) == null) {
            theCase.addReadyActivity(node);
        }
    }

    /**
     * 2004-04-28 Jincm add
     * @param activity
     * @param statusId
     * @param nextLinkId
     * @throws BPMException
     */
    public void _takeBackActivity(BPMActivity activity, String statusId, String nextLinkId) throws BPMException {
        if (activity == null) {
            throw new BPMException("READYACTIVITY_NOT_EXITE_IN_CASE", new Object[] { theCase.getId(), null });
        }
        List trans = activity.getDownTransitions();
        BPMTransition downTran = (BPMTransition) trans.get(0);
        BPMAbstractNode nextNode = downTran.getTo();
        if (nextNode.getBPMObjectType() == 2 && nextNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {//join节点
            if (theCase.getReadyActivityById(activity.getId()) == null) {
                BPMActivity nextActivity = (BPMActivity) nextNode;
                if (theCase.getReadyActivityById(nextActivity.getId()) == null) {
                    ReadyNode node = new ReadyNode(nextActivity);
                    node.setNum(1);
                    theCase.addReadyActivity(node);
                    _takeBackActivity1(activity, statusId, nextLinkId);
                    removeRecursiveExceptSelf((BPMActivity) nextNode, true);
                } else {
                    ReadyNode node = theCase.getReadyActivityById(nextActivity.getId());
                    node.setNum(node.getNum() + 1);
                    _takeBackActivity1(activity, statusId, nextLinkId);
                }
            } else {
                _takeBackActivity1(activity, statusId, nextLinkId);
            }
        } else {
            _takeBackActivity1(activity, statusId, nextLinkId);
            removeRecursiveExceptSelf(activity, true);
        }
    }

    /**
     * 唤醒一系列的节点
     * @param nodes 将要被唤醒的节点
     * @throws BPMException 抛出异常
     */
    public void awakeActivity(BPMProcess process, List<BPMActivity> nodes, List<WorkitemInfo> workitem)
            throws BPMException {
        if (nodes != null && nodes.size() > 0 && workitem != null && workitem.size() > 0) {
            Map<String, List<WorkitemInfo>> itemMap = new HashMap<String, List<WorkitemInfo>>();
            for (WorkitemInfo item : workitem) {
                List<WorkitemInfo> itemList = itemMap.get(item.getActivityId());
                if (itemList == null) {
                    itemList = new ArrayList<WorkitemInfo>();
                }
                itemList.add(item);
                itemMap.put(item.getActivityId(), itemList);
            }
            //开始修改流程实例并调用事件接口
            int len = nodes.size();
            recorder = new Recorder(theCase);
            for (BPMActivity activity : nodes) {
                ReadyNode readyNode = new ReadyNode(activity);
                readyNode.setNum(len);
                theCase.addReadyActivity(readyNode);
                String isPass= WorkflowUtil.getNodeConditionFromContext(context, activity, "isPass");
                if ("failure".equals(isPass)) {
                	WorkflowUtil.putNodeConditionToContext(context, activity, "isPass", "success");
                }
                recorder.SetReadyNode(activity, null);
                recorder.onNodeReady(activity);
                listener.onActivityAwakeToReady(domain, context, itemMap.get(activity.getId()));
            }
        }
    }

    /**
     * 删除所有节点的状态
     * @param isDelete
     * @throws BPMException
     */
    public void removeActivityForStepToStartNode() throws BPMException {
        List<BPMAbstractNode> activity = process.getActivitiesList();
        if (activity != null) {
            for (BPMAbstractNode node : activity) {
            	WorkflowUtil.putNodeConditionToContext(context, node, "isDelete", "false");
                if(node.getNodeType().equals(BPMAbstractNode.NodeType.humen)){
                    removeActivity(node.getId(), true, true);
                }
            }
        }
    }
    
    /**
     * 设置split和join之间的所有节点为分支不通过状态
     * @param nextActivity
     * @param joinNode
     * @param passNodes
     */
    private void initAllNodesNotPassed(BPMAbstractNode parent,
            BPMActivity joinNode, Map<String, String> passNodes) throws BPMException {
        if(null==parent || null== joinNode || null!=passNodes.get(parent.getId()) || parent.getId().equals(joinNode.getId())){//节点已走过或者到了终点join节点了
            return;
        }
        passNodes.put(parent.getId(), parent.getId());
        List<BPMTransition> downs= parent.getDownTransitions();
        if(downs!=null && !downs.isEmpty()){
            for (BPMTransition down : downs) {
                BPMAbstractNode toNode= down.getTo();
                recorder.onNodeRun(toNode);
                removeNode((BPMActivity)toNode);
                WorkflowUtil.putNodeConditionToContext(context, toNode, "isDelete", "true");
                initAllNodesNotPassed(toNode, joinNode, passNodes);
            }
        }
    }
}

/**
 * 节点工具类。封装常用的节点操作。
 * 
 * @author wangwy
 * 
 */
class NodeUtil {
    public static ThreadLocal<BPMAbstractNode> returnBackNode = new ThreadLocal<BPMAbstractNode>();

    /**
     * 找到Split节点对应的Join节点。 如果传入的是Human节点，返回对应分支的Join节点，没有对应Join则返回null。
     * 
     * @param process
     * @param split
     *            split节点
     * @return
     */
    public static BPMAndRouter findJoin(BPMProcess process, BPMAbstractNode split) {
        BPMAbstractNode node = split;
        // 算法查找
        // 查找所有直接后续节点都通过的Join节点。
        List<BPMTransition> links = node.getDownTransitions();
        if (links == null)
            return null;

        Set<BPMAbstractNode> set = new HashSet<BPMAbstractNode>();
        Map<String,String> passedNodes= new HashMap<String, String>();
        for (BPMTransition link : links) {
            BPMAbstractNode to = link.getTo();
            Set<BPMAbstractNode> allNext = getAllNextNodes(to,passedNodes);
            if (set.size() == 0) {
                set.addAll(allNext);
            } else {
                set.retainAll(allNext);
            }
        }
        if (set.size() > 0) {
        	if(set.size() >1){
	            // 找出路径中第一个Join节点
	            BPMAndRouter firstJoin = null;
	            for (BPMAbstractNode n : set) {
	                if (isJoin(n)) {
	                    BPMAndRouter join2 = (BPMAndRouter) n;
	                    if (firstJoin == null) {
	                        firstJoin = join2;
	                    }
	                    firstJoin = (passThrough(firstJoin, join2)) ? firstJoin : join2;
	                }
	            }
	            return firstJoin;
        	}else{
        		BPMAbstractNode n= set.iterator().next();
        		if(isJoin(n)){
        			return (BPMAndRouter) n;
        		}
        	}
        }
        return null;
    }

    /**
     * 获得指定节点下得直接split节点
     * @param fromNodeOfcurrBackNode
     * @return
     */
    public static BPMAbstractNode findDirectSplitNodeForSplit(BPMAbstractNode fromNodeOfcurrBackNode) {
        if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {
            return fromNodeOfcurrBackNode;
        } else if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
            List<BPMTransition> downs = fromNodeOfcurrBackNode.getDownTransitions();
            BPMTransition down = downs.get(0);
            BPMAbstractNode toNode = down.getTo();
            return findDirectSplitNodeForSplit(toNode);
        } else if (fromNodeOfcurrBackNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
            List<BPMTransition> downs = fromNodeOfcurrBackNode.getDownTransitions();
            BPMTransition down = downs.get(0);
            BPMAbstractNode toNode = down.getTo();
            return findDirectSplitNodeForSplit(toNode);
        }
        return null;
    }

    /**
     * 判断节点1后续节点是否包含节点2。
     * 
     * @param split
     * @param join
     * @return
     */
    private static boolean passThrough(BPMAbstractNode split, BPMAbstractNode join) {
        List<BPMTransition> links = split.getDownTransitions();
        for (BPMTransition link : links) {
            BPMAbstractNode to = link.getTo();
            if (to instanceof BPMEnd)
                break;
            if (to.equals(join))
                return true;
            if (passThrough(to, join))
                return true;
        }
        return false;
    }

    public static boolean isJoin(BPMAbstractNode node) {
        if (node == null)
            return false;
        return (node instanceof BPMAndRouter) && !((BPMAndRouter) node).isStartAnd();
    }

    public static boolean isSplit(BPMAbstractNode node) {
        if (node == null)
            return false;
        return (node instanceof BPMAndRouter) && ((BPMAndRouter) node).isStartAnd();
    }

    /**
     * 取得节点的所有后续节点。
     * 
     * @param node
     * @return
     */
    private static Set<BPMAbstractNode> getAllNextNodes(BPMAbstractNode node,Map<String,String> passedNodes) {
        Set<BPMAbstractNode> result = new HashSet<BPMAbstractNode>();
        if(null!= passedNodes.get(node.getId())){
            return result;
        }
        passedNodes.put(node.getId(), node.getId());
        List<BPMTransition> links = node.getDownTransitions();
        for (BPMTransition link : links) {

            BPMAbstractNode to = link.getTo();
            result.add(to);
            if (!(to instanceof BPMEnd))
                result.addAll(getAllNextNodes(to,passedNodes));
        }
        return result;
    }

    /**
     * 回退节点与此知会节点是否是并发节点，如果是返回true
     * 
     * @param activity 知会节点 
     * 
     * @return
     */
    public static boolean isSyndNode(BPMAbstractNode activity) {
        List<BPMTransition> upTransitions = activity.getUpTransitions();
        List<BPMTransition> downTransitions = activity.getDownTransitions();
        if (upTransitions != null && !upTransitions.isEmpty()) {
            BPMAbstractNode parent = upTransitions.get(0).getFrom();
            BPMAbstractNode child = downTransitions.get(0).getTo();
            if (child.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
                List<BPMHumenActivity> lists = findDirectHumenChildrenCascade(parent, child);
                for (BPMHumenActivity b : lists) {
                    if (b.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
                        BPMAbstractNode returnBackNode = NodeUtil.returnBackNode.get();
                        if (returnBackNode != null && b.getId().equals(returnBackNode.getId()))
                            return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 当前结点是否是并发结点，并且是否全是知会节点。如果满足这两个条件，返回true
     * 
     * @param activity
     * @return
     */
    public static boolean isAllInformNode(BPMAbstractNode activity,BPMCase theCase) {
        List<BPMTransition> upTransitions = activity.getUpTransitions();
        List<BPMTransition> downTransitions = activity.getDownTransitions();
        if (upTransitions != null && !upTransitions.isEmpty()) {
            BPMAbstractNode parent = upTransitions.get(0).getFrom();
            BPMAbstractNode child = downTransitions.get(0).getTo();
            if (child.getNodeType().equals(BPMAbstractNode.NodeType.join)) {
                List<BPMHumenActivity> lists = findDirectHumenChildrenCascade(parent, child);
                for (BPMHumenActivity b : lists) {
                    if (b.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {
                        BPMHumenActivity humenNode = (BPMHumenActivity) b;
                        String isDelete= WorkflowUtil.getNodeConditionFromCase(theCase, humenNode, "isDelete");
                        if (!WorkflowUtil.isInformNode(b) && "false".equals(isDelete))
                            return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 查找当前节点下到指定的节点之间所有的人工节点
     * 
     * @param current_node
     * @param split_node
     * @return
     */
    public static List<BPMHumenActivity> findDirectHumenChildrenCascade(BPMAbstractNode current_node,
            BPMAbstractNode split_node) {
        List<BPMHumenActivity> result = new ArrayList<BPMHumenActivity>();
        List<BPMTransition> down_links = current_node.getDownTransitions();
        for (BPMTransition down_link : down_links) {
            BPMAbstractNode _node = down_link.getTo();
            if (_node instanceof BPMHumenActivity) {
                BPMHumenActivity node = (BPMHumenActivity) _node;
                result.add(node);
                if (WorkflowUtil.isInformNode(node)) {
                    List<BPMHumenActivity> children = findDirectHumenChildrenCascade(_node, split_node);
                    result.addAll(children);
                }
            } else if ((_node instanceof BPMAndRouter || _node instanceof BPMConRouter)
                    && !_node.getId().equals(split_node.getId())) {
                List<BPMHumenActivity> children = findDirectHumenChildrenCascade(_node, split_node);
                result.addAll(children);
            } else if (_node instanceof BPMEnd) {
                return new ArrayList<BPMHumenActivity>(0);
            }
        }
        return result;
    }

    /**
     * 判断当前是知会节点时，是否可以操作。如果当前节点是知会，它的并发节点全是知会，返回true，如果不是知会，直接返回true。 
     * 
     * @return
     */

    public static boolean isEnableOperate(BPMAbstractNode node,BPMCase theCase) {
        if (WorkflowUtil.isInformNode(node)) {
            if (NodeUtil.isAllInformNode(node,theCase)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 判断当前是知会节点时，是否可以删除。如果回退节点和此知会节点是并发节点，返回true，否则不是并发节点，如果当前节点是知会，它的并发节点全是知会，返回true，如果不是知会，直接返回true。 
     * 
     * @return
     */
    public static boolean isEnableDelete(BPMAbstractNode node,BPMCase theCase) {
        if (WorkflowUtil.isInformNode(node)) {
            if (NodeUtil.isSyndNode(node)) {
                return true;
            } else {
                if (NodeUtil.isAllInformNode(node,theCase)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return true;
        }
    }

    /**
     * 查找第一个后续人工节点,无后续人工节点则返回null
     */
    public static BPMHumenActivity findFirstDirectHumen(BPMAbstractNode current_node) {
        List<BPMTransition> down_links = current_node.getDownTransitions();
        for (BPMTransition down_link : down_links) {
            BPMAbstractNode _node = down_link.getTo();
            if (_node instanceof BPMHumenActivity) {
                return (BPMHumenActivity) _node;
            } else if (_node instanceof BPMAndRouter || _node instanceof BPMConRouter) {
                return findFirstDirectHumen(_node);
            } else if (_node instanceof BPMEnd) {
                return null;
            }
        }
        return null;
    }
}