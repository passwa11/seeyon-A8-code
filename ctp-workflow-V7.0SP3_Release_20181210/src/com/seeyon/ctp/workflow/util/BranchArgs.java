/**
 * 
 */
package com.seeyon.ctp.workflow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.bo.WorkflowFormFieldBO;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.engine.listener.ActionRunner;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.manager.SubProcessManager;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.manager.WorkflowFormDataMapInvokeManager;
import com.seeyon.ctp.workflow.po.SubProcessRunning;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.vo.ConditionMatchResultVO;
import com.seeyon.ctp.workflow.vo.NodePeopleMatchResultVO;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.vo.WorkflowMatchLogVO;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;
import com.seeyon.ctp.workflow.wapi.WorkflowNodeUsersMatchResult;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMAbstractNode.NodeType;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMStatus;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.wapi.CaseDetailLog;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * @Project/Product:A8
 * @Description:分支匹配算法
 * @Copyright: Copyright (c) 2011 Seeyon,All Rights Reserved.
 * @author: wangchw
 * @time:2011-09-20 下午02:07:46
 * @version:1.0
 */
public class BranchArgs {
    
    private static ProcessOrgManager processOrgManager= (ProcessOrgManager)AppContext.getBean("processOrgManager");
    
    private static WorkFlowMatchUserManager workFlowMatchUserManager= (WorkFlowMatchUserManager)AppContext.getBean("workflowMatchUserManager");

    private static Log log = CtpLogFactory.getLog(BranchArgs.class);

    /**
     * isCanPassWithJoin()
     * 判断是否可以穿过该join节点，这是一个递归判断过程
     * @param toNode join节点
     * @param current_node 当前处理节点
     * @param allNotSelectNodeList 在弹出页面所有没被选中的节点集合
     * @param allSelectNodeList 在弹出页面中所有被选中的节点集合
     * @param allInformNodeList 在弹出页面中所有被选中的知会节点集合
     * @param context 上下文信息
     * @return true：可以穿过；false：不可以穿过
     * @throws BPMException
     */
    private static boolean isCanPassWithJoin(Map<String,Boolean> canPassJoinNodes,BPMAbstractNode toNode, BPMAbstractNode current_node,
            Set<String> allNotSelectNodeList, Set<String> allSelectNodeList, Set<String> allInformNodeList,
            WorkflowBpmContext context,Map<String, ConditionMatchResultVO> condtionResult,BPMCase theCase) throws BPMException {
        //默认可以穿过
        boolean isCanPass = true;
        if(null!=canPassJoinNodes.get(toNode.getId())){
            return canPassJoinNodes.get(toNode.getId());
        }
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
        //获得该join节点的所有up线
        List<BPMTransition> joinUps = toNode.getUpTransitions();
        for (BPMTransition bpmTransition2 : joinUps) {
            BPMAbstractNode fromNode = bpmTransition2.getFrom();
            String nodeId = fromNode.getId();
            String isDelete= WorkflowUtil.getNodeConditionFromCase(theCase, fromNode, "isDelete");
            //System.out.println("nodeId:="+nodeId);
            //System.out.println("fromNodePolicy:="+fromNodePolicy);
            //System.out.println("isDelete:="+isDelete);
            if ("false".equals(isDelete) && !current_node.getId().equals(nodeId) && isCanPass) {//from节点没有被删除，且不是当前处理节点
                if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {//humen->split
                    BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity) fromNode;
                    String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
                    //计算出当前from节点是否为知会节点
                    boolean isInformNode = currFromHumenNodePolicy.equals(informActivityPolicy)
                            || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
                    //看是否被选中
                    if (allSelectNodeList.contains(nodeId)) {//选中
                        //判断fromNode节点是否为选中的知会节点
                    	if (allInformNodeList.contains(nodeId) && condtionResult.get(nodeId)==null) {// 是选中的知会节点,并且不需要选人的情况
                            //继续，对isCanPass=true没有影响
                            continue;
                        } else {//不是选中的知会节点，则肯定是选中的非知会节点(则不让穿过该join节点)
                            isCanPass = false;
                        }
                    } else if (allNotSelectNodeList.contains(nodeId)) {//没选中
                        if (isInformNode) {//是没选中的知会节点
                            //                          isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,context);
                            //继续，对isCanPass=true没有影响
                            continue;
                        } else {//是没选中的非知会节点(则不让穿过该join节点)
                                //继续，对isCanPass=true没有影响
                            continue;
                        }
                    } else {
                        if(null!= condtionResult && null!= condtionResult.get(nodeId)){
                            isCanPass= false;
                        }else{
                            //判断该humen节点是否已产生待办
                            boolean isDoing = false;
                            boolean isDone = false;
                            if (null != theCase) {
                                isDoing = WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_READY, CaseDetailLog.STATE_ZCDB);
                                isDone = WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_FINISHED,
                                        CaseDetailLog.STATE_CANCEL, CaseDetailLog.STATE_STOP);
                            }
                            //if (log.isInfoEnabled()) {
                                //log.info("isDoing:=" + isDoing);
                                //log.info("isDone:=" + isDone);
                            //}
                            if (isDoing) {//如果是待办状态
                                if (isInformNode) {//是知会节点
                                    continue;
                                } else {
                                    isCanPass = false;
                                }
                            } else if (isDone) {//如果是办已办状态
                                continue;
                            } else {//即没有产生待办，也没有产生已办
                                if (isInformNode) {//是知会节点
                                    //                              isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                                    isCanPass = isCanPassWithNonInformNode(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList,
                                            allSelectNodeList, allInformNodeList, context,condtionResult);
                                } else {//不是知会节点
                                    //                              isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,context); 2012-2-7
                                    isCanPass = isCanPassWithNonInformNode(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList,
                                            allSelectNodeList, allInformNodeList, context,condtionResult);
                                }
                            }
                        } 
                    }
                }
                if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {//join->join
                    //以fromNode为基础递归往后查找
                    isCanPass = isCanPassWithJoin(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList, allSelectNodeList,
                            allInformNodeList, context,condtionResult,theCase);
                }
                if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {//split->中间穿过一些知会节点->join
                    //以fromNode为基础递归往后查找
                    isCanPass = isCanPassWithJoin(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList, allSelectNodeList,
                            allInformNodeList, context,condtionResult,theCase);
                }
            }
            if (!isCanPass) {
                break;
            }
        }
        if(null==canPassJoinNodes.get(toNode.getId())){
            canPassJoinNodes.put(toNode.getId(),isCanPass);
        }
        return isCanPass;
    }

    /**
     * 判断该非知会节点是否可以被穿过
     * @param toNode
     * @param current_node
     * @param allNotSelectNodeList
     * @param allSelectNodeList
     * @param allInformNodeList
     * @param context
     * @return
     */
    private static boolean isCanPassWithNonInformNode(Map<String,Boolean> canPassJoinNodes,BPMAbstractNode toNode, BPMAbstractNode current_node,
            Set<String> allNotSelectNodeList, Set<String> allSelectNodeList, Set<String> allInformNodeList,
            WorkflowBpmContext context,Map<String, ConditionMatchResultVO> condtionResult) {

        if(null!=canPassJoinNodes.get(toNode.getId())){
            return canPassJoinNodes.get(toNode.getId());
        }
        BPMCase theCase = context.getTheCase();
        //默认可以穿过
        boolean isCanPass = true;
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
        //获得该join节点的所有up线
        List<BPMTransition> joinUps = toNode.getUpTransitions();
        if(null==joinUps){
            if(log.isErrorEnabled()){
                log.error("找不到UpTransitions流程ID:="+context.getProcessId()+"节点ID:="+toNode.getId()+"节点名称:="+toNode.getName());
            }
            return isCanPass;
        }
        for (BPMTransition bpmTransition2 : joinUps) {
            if(null==bpmTransition2){
                continue;
            }
            BPMAbstractNode fromNode = bpmTransition2.getFrom();
            if(null==fromNode){
                continue;
            }
            String nodeId = fromNode.getId();
            String isDelete = WorkflowUtil.getNodeConditionFromCase(theCase, fromNode, "isDelete");
            if (isCanPass) {
                if ("false".equals(isDelete) && !current_node.getId().equals(nodeId)) {//from节点没有被删除，且不是当前处理节点
                    if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)) {//humen->split
                        BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity) fromNode;
                        String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
                        //计算出当前from节点是否为知会节点
                        boolean isInformNode = currFromHumenNodePolicy.equals(informActivityPolicy)
                                || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
                        //看是否被选中
                        if (allSelectNodeList.contains(nodeId)) {//选中
                            //判断fromNode节点是否为选中的知会节点
                            if (allInformNodeList.contains(nodeId)) {//是选中的知会节点(则不让穿过该join节点)
                                isCanPass = false;
                            } else {//不是选中的知会节点，则肯定是选中的非知会节点(则不让穿过该join节点)
                                isCanPass = false;
                            }
                        } else if (allNotSelectNodeList.contains(nodeId)) {//没选中
                            if (isInformNode) {//是没选中的知会节点
                                //继续，对isCanPass=true没有影响
                                continue;
                            } else {//是没选中的非知会节点
                                    //继续，对isCanPass=true没有影响
                                continue;
                            }
                        } else {
                            if(null!= condtionResult && null!= condtionResult.get(nodeId)){
                                isCanPass= false;
                            }else{
                                //判断该humen节点是否已产生待办
                                boolean isDoing = false;
                                boolean isDone = false;
                                if (null != theCase) {
                                    isDoing = WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_READY,CaseDetailLog.STATE_ZCDB);
                                    isDone = WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_FINISHED,
                                            CaseDetailLog.STATE_CANCEL, CaseDetailLog.STATE_STOP);
                                }
                                //if (log.isInfoEnabled()) {
                                    //log.info("isDoing:=" + isDoing);
                                    //log.info("isDone:=" + isDone);
                                //}
                                if (isDoing) {//如果是待办状态
                                    if (isInformNode) {//是知会节点
                                        isCanPass = false;
                                    } else {
                                        isCanPass = false;
                                    }
                                } else if (isDone) {//如果是已办状态
                                    isCanPass = false;
                                } else {//即没有产生待办，也没有产生已办，递归继续
                                    isCanPass = isCanPassWithNonInformNode(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList,
                                            allSelectNodeList, allInformNodeList, context,condtionResult);
                                }
                            }
                        }
                    }
                    if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.join)) {//join->非知会节点
                        isCanPass = isCanPassWithNonInformNode(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList,
                                allSelectNodeList, allInformNodeList, context,condtionResult);
                    }
                    if (fromNode.getNodeType().equals(BPMAbstractNode.NodeType.split)) {//split->中间穿过一些知会节点->非知会节点
                        isCanPass = isCanPassWithNonInformNode(canPassJoinNodes,fromNode, current_node, allNotSelectNodeList,
                                allSelectNodeList, allInformNodeList, context,condtionResult);
                    }
                    if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){
                        isCanPass = false;
                    }
                } else if ("false".equals(isDelete) && current_node.getId().equals(nodeId)) {//是当前处理节点
                    isCanPass = false;
                }
            }
        }
        if(null==canPassJoinNodes.get(toNode.getId())){
            canPassJoinNodes.put(toNode.getId(),isCanPass);
        }
        return isCanPass;
    }

    /**
     * 找到该节点之后的所有人工活动节点
     * @param current_node
     * @param condtionResult
     * @param cpMatchResult
     * @param context
     * @param allNotSelectNodeList
     * @param allSelectNodeList
     * @param informNodeList
     * @param allInformNodeList
     * @return
     * @throws BPMException
     */
    private static List<BPMHumenActivity> findDirectHumenChildren(Set<String> passedJoinNodes,BPMAbstractNode current_node,
            Map<String, ConditionMatchResultVO> condtionResult, Map<String, ConditionMatchResultVO> condtionResultTemp, 
            Map<String, ConditionMatchResultVO> condtionResultTempAll,CPMatchResultVO cpMatchResult,
            WorkflowBpmContext context,Set<String> allSelectNodeList,
            Set<String> allInformNodeList, Set<String> currentSelectNodes, Set<String> currentSelectInformNodes,
            Set<String> allMustNotSelectNodeList,boolean isClearAddition,Map<String,Set<String>> cannotPassedNodes,Map<String,Boolean> autoSkipNodeMap) throws BPMException {
    	List<BPMHumenActivity> result = new ArrayList<BPMHumenActivity>();
    	if(passedJoinNodes.contains(current_node.getId())){
        	return result;
        }
    	BPMCase theCase= context.getTheCase();
        cpMatchResult.setCondtionMatchMap(condtionResult);
        String fromNodeId = current_node.getId();
        ConditionMatchResultVO upCondition = condtionResult.get(fromNodeId);//当前节点对应的分支条件

        List<BPMTransition> down_linksTemp = current_node.getDownTransitions();
        context.setAllNotSelectNodes(allMustNotSelectNodeList);
        Map<String,NodePeopleMatchResultVO> countResultMap= new HashMap<String,NodePeopleMatchResultVO>();
        List<BPMTransition> down_links= BranchArgs.getBPMTransitionBySort(down_linksTemp, context, isClearAddition, cpMatchResult, autoSkipNodeMap, upCondition, countResultMap);
        WorkflowNodeBranchLogUtil.initCacheBranchMatchResult();
        WorkflowNodeBranchLogUtil.initCacheFormFieldValue();
        /*String baseCurrentNode = "<span class='color_gray2'></span><br/>";
        String baseSender = "<span class='color_gray2'></span><br/>";*/
        
        String baseCurrentNode = "[" + ResourceUtil.getString("workflow.branch.currentnode") + "]\r";
        String baseSender = "[" + ResourceUtil.getString("workflow.branch.sender") + "]";
        String appName = context.getAppName();
        String formApp = context.getFormAppId();
        Map<String,WorkflowFormFieldBO> formFieldDefMap = (Map<String, WorkflowFormFieldBO>) context.getBusinessData(EventDataContext.CTP_FORM_DATA_DEF);
        //循环遍历每条down线
        log.info("TDS:="+down_linksTemp.size()+"DS:="+down_links.size());
        for (int i=down_links.size()-1;i>=0;i--) {//循环遍历每条down线
            BPMTransition down_link = down_links.get(i);
            String linkId = down_link.getId();
            BPMAbstractNode toNode = down_link.getTo();//获得down线的目的节点toNode
            String currentNodeId = toNode.getId();
            String currentCondition = down_link.getFormCondition();//获得down线上的条件表达式
            String conditionBase = down_link.getConditionBase();//获得down线上的条件的参考对象：当前节点或发起者
            String isForce = down_link.getIsForce();
            isForce= (isForce==null || "null".equals(isForce.trim()) || "".equals(isForce.trim()) || "undefined".equals(isForce.trim()))?"0":isForce;
            //String conditionDesc= down_link.getConditionTitle();
            String conditionDesc= down_link.getDesc();
            String conditionTitle = down_link.getConditionTitle();
           String[] branchExpressionResult =  null;
           if(Strings.isNotBlank(currentCondition)){
        	   branchExpressionResult = WorkflowUtil.branchTranslateBranchExpression(appName, formApp, currentCondition,formFieldDefMap);
           }
           if(branchExpressionResult!=null && branchExpressionResult.length>=2 
              && "true".equals(branchExpressionResult[0])){
        	   conditionTitle = branchExpressionResult[1];
        	   conditionTitle = conditionTitle.replaceAll("<span.*?>", "").replaceAll("</span>", "");
           }
            String conditionBaseTitle = "";
            if("currentNode".equals(conditionBase)){
            	conditionBaseTitle = baseCurrentNode;
            } else if("CurrentSender".equals(conditionBase)){
            	conditionBaseTitle = baseSender;
            } else if("start".equals(conditionBase)){
            	conditionBaseTitle = baseSender;
            }

            int conditionType = down_link.getConditionType();
            //兼容老代码的时候可能需要，但是新代码中暂时不需要。以后再来调整
//            currentCondition = BranchArgs.parseCondition(currentCondition, conditionBase, currentIsStart);
            boolean currentConditionIsHandle = false;
            boolean currentConditionMatchResult = false;
            boolean currentConditionHasBranch = false;
            boolean isDefaultShow= false;
            int currentPreConditionType= conditionType;
            if(null!=upCondition){
                currentPreConditionType= Integer.parseInt(upCondition.getConditionType());  
            }
            if(conditionType==2){
                isDefaultShow= true;
                currentPreConditionType= 20;//手工分支
                conditionTitle = null;
            }
            String preConditionType= null;
            String preConditionDesc= "";
            String preConditionTitle = "";
            if (null != upCondition) {
            	String upConditionDesc = upCondition.getConditionDesc();
            	String upConditionTitle = upCondition.getConditionTitle();
            	if(WorkflowUtil.isNotNull(conditionDesc)){
                    conditionDesc = (null==upConditionDesc || "".equals(upConditionDesc.trim())) ? 
                            Strings.toHTML(conditionDesc) : Strings.toHTML(upConditionDesc)+" <font class='color_black' style='font-style:italic;'>and</font> "+ Strings.toHTML(conditionDesc);
                }else{
                    conditionDesc = Strings.toHTML(upConditionDesc);
                }
            	if(WorkflowUtil.isNotNull(conditionTitle)){
            		conditionTitle = ((null==upConditionTitle || "".equals(upConditionTitle.trim()) || "null".equals(upConditionTitle.trim())) ? (conditionBaseTitle + conditionTitle) : upConditionTitle+" and\r " + conditionBaseTitle + conditionTitle);
            	}else{
            	    if(WorkflowUtil.isNotNull(upConditionTitle)){
            	        conditionTitle = conditionBaseTitle + upConditionTitle;
            	    }
            	}
            	boolean upConditionMatchResult = upCondition.isMatchResult();
                boolean upHasBranch = upCondition.isHasBranch();
                boolean upIsHandle = upCondition.isHand();
                preConditionDesc= upCondition.getConditionDesc();
                preConditionTitle=upCondition.getConditionTitle();
                preConditionType= upCondition.getConditionType();
                if(Strings.isNotBlank(upCondition.getPreConditionType()) && ( "30".equals(upCondition.getPreConditionType()) || "20".equals(upCondition.getPreConditionType()))){
                    if(!"2".equals(preConditionType)){
                        preConditionType= upCondition.getPreConditionType();
                    }else{
                        preConditionType= "20";
                    }
                }else if( Strings.isNotBlank(upCondition.getPreConditionType()) && "1".equals(upCondition.getPreConditionType())){
                    if(!"2".equals(preConditionType)){
                        preConditionType= upCondition.getPreConditionType();
                    }
                }
                //if("2".equals(preConditionType) && !"1".equals(isForce)){//前一分支条件或当前分支条件为手工分支条件
                if("2".equals(preConditionType) || "20".equals(preConditionType)){//含有手工分支
//                    if(conditionType==1 || conditionType==4){
//                        currentPreConditionType= 30;
//                    }else{
//                        isDefaultShow= true;
//                        currentPreConditionType= 20;//手工分支
//                    }
                    isDefaultShow= true;
                    currentPreConditionType= 20;//手工分支
                }
                if((("1".equals(preConditionType) || "4".equals(preConditionType)) && upIsHandle && !"2".equals(preConditionType) ) || "30".equals(preConditionType)){
                    currentPreConditionType= 30;//非强制自动分支条件
                }
                if (upIsHandle) {//可选
                    currentConditionHasBranch = true;
                    if (conditionType == 1 || conditionType == 4) {
                    	currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                conditionBase);
                    	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(toNode.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                        log.debug(currentNodeId+";currentCondition:="+currentConditionMatchResult);
                        if(!"1".equals(isForce)){//非强制分支条件
                            currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                            currentConditionIsHandle= true;
                        }else{//强制分支条件
                            if(currentConditionMatchResult){//分支条件满足
                                currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                                currentConditionIsHandle= true;
                            }else{
                                currentConditionIsHandle= false;
                            }
                        }
                    } else if (conditionType == 2) {
                        currentConditionMatchResult = upConditionMatchResult && false;
                        currentConditionIsHandle = true;
                        
                    } else if (conditionType == 3) {
                        currentConditionMatchResult = upConditionMatchResult && true;
                        currentConditionIsHandle = true;
                    }
                } else {//不可选
                    if (upConditionMatchResult) {
                        if (upHasBranch) {
                            currentConditionHasBranch = true;
                            if (conditionType == 1 || conditionType == 4) {
                                currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                        conditionBase);
                            	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(toNode.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                                currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                                currentConditionIsHandle = !"1".equals(isForce);
//                                conditionDesc= (null==upConditionDesc || "".equals(upConditionDesc.trim()))?conditionDesc:"("+upConditionDesc+") and ("+conditionDesc+")";
                            } else if (conditionType == 2) {
                                currentConditionMatchResult = upConditionMatchResult && false;
                                currentConditionIsHandle = true;
                            } else if (conditionType == 3) {
                                currentConditionMatchResult = upConditionMatchResult && true;
                                currentConditionIsHandle = false;
                            }
                        } else {
                            if (conditionType == 1 || conditionType == 4) {
                                currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                        conditionBase);
                            	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(toNode.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                                currentConditionMatchResult = true && currentConditionMatchResult;
                                currentConditionIsHandle = !"1".equals(isForce);
                                currentConditionHasBranch = true;
                            } else if (conditionType == 2) {
                                currentConditionMatchResult = true && false;
                                currentConditionIsHandle = true;
                                currentConditionHasBranch = true;
                            } else if (conditionType == 3) {
                                currentConditionMatchResult = true && true;
                                currentConditionIsHandle = false;
                                currentConditionHasBranch = false;
                            }
                        }
                    } else {
                        if (upHasBranch) {
                            currentConditionMatchResult = false;
                            currentConditionIsHandle = false;
                            currentConditionHasBranch = true;
                        } else {
                            currentConditionMatchResult = false;
                            currentConditionIsHandle = false;
                            currentConditionHasBranch = false;
                        }
                    }
                }
            } else {
                if(WorkflowUtil.isNotNull(conditionTitle)){
                    conditionTitle = conditionBaseTitle + conditionTitle;
                }
                if (conditionType == 1 || conditionType == 4) {
                    currentConditionHasBranch = true;
                    currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                            conditionBase);
                	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(toNode.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                    currentConditionIsHandle = !"1".equals(isForce);
                } else if (conditionType == 2) {
                    currentConditionMatchResult = false;
                    currentConditionIsHandle = true;
                    currentConditionHasBranch = true;
                } else if (conditionType == 3) {
                    currentConditionMatchResult = true;
                    currentConditionHasBranch = false;
                    currentConditionIsHandle = false;
                }
            }
            BPMSeeyonPolicy policy = toNode.getSeeyonPolicy();
            String nodeName = toNode.getName();
            NodeType nodeType = toNode.getNodeType();
            if (nodeType.equals(NodeType.humen)) {//人工活动节点(humen->humen)
                BPMHumenActivity hNode = (BPMHumenActivity) toNode;
//                if(!"normal".equals(hNode.isValid())){ //节点不可用
//                    cpMatchResult.getInvalidateActivityMap().put(hNode.getId(), hNode.getName());
//                }
                boolean isNew = condtionResult.get(hNode.getId()) == null;
                if (isNew) {
                    context.setAllNotSelectNodes(allMustNotSelectNodeList);
                    NodePeopleMatchResultVO nodePMRVO = new NodePeopleMatchResultVO();
                    //强制分支不满足条件时不判断后面的节点(正常匹配满足或者手工选择的情况下才判断后续的节点人员情况)
                    if(currentConditionMatchResult || currentConditionIsHandle){
                    	nodePMRVO = isNodeNeedSelectPeople(hNode, context,isClearAddition,cpMatchResult,currentConditionIsHandle,currentConditionMatchResult,autoSkipNodeMap,
                    			allMustNotSelectNodeList,allSelectNodeList,allInformNodeList);
                    }
                    String personStatus= hNode.isValid();
                    BPMActor actor = (BPMActor) hNode.getActorList().get(0);
                    BPMParticipant party = actor.getParty();
                    String partyTypeId= party.getType().id;
                    String partyId = party.getId();
                    if("user".equals(partyTypeId) && !"normal".equals(personStatus)){//走自动跳过
                    	boolean hasAgent= WorkflowUtil.hasAgent(partyId);
                    	if(!hasAgent){
                    		cpMatchResult.getInvalidateActivityMap().put(hNode.getId(), hNode.getName());
                    	}
                    }
                    ConditionMatchResultVO vo = new ConditionMatchResultVO();
                    vo.setConditionDesc(conditionDesc);
                    vo.setConditionTitle(conditionTitle);
                    if(WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(partyId)){//空节点
                        vo.setNodePolicy("");
                    }else{
                        vo.setNodePolicy(toNode.getSeeyonPolicy().getName());
                    }
                    if(currentPreConditionType==20){
                        conditionType= 2;
                    }
                    if(conditionType==3){
                        vo.setConditionDesc(preConditionDesc);
                        vo.setConditionTitle(preConditionTitle);
                    }
                    vo.setConditionType(String.valueOf(conditionType));
                    vo.setFromNodeId(fromNodeId);
                    vo.setId(linkId);
                    vo.setHand(currentConditionIsHandle);
                    vo.setHasBranch(currentConditionHasBranch);
                    vo.setNeedSelectPeople(nodePMRVO.isNeedSelectPeople());
                    vo.setPeoples(nodePMRVO.getPeoples());
                    vo.setCanAutoSkip(nodePMRVO.isCanAutoSkip());
                    vo.setNa(nodePMRVO.getNa());
                    vo.setNeedPeopleTag(nodePMRVO.isNeedPeopleTag());
                    vo.setMatchResult(currentConditionMatchResult);
                    vo.setDefaultShow(isDefaultShow);
                    vo.setPreConditionType(String.valueOf(currentPreConditionType));
                    String matchResultName= "";
                    if(conditionType==1 || conditionType==4){
                        if(currentConditionMatchResult){
                            matchResultName= MessageUtil.getString("workflow.commonpage.branch.sucess");
                        }else{
                            matchResultName= MessageUtil.getString("workflow.commonpage.branch.failed");
                        }
                    }else if(conditionType==2){
                        matchResultName= MessageUtil.getString("workflow.commonpage.branch.hand");
                    }else{
                        if(currentConditionIsHandle){
                            if( currentPreConditionType==30 ){
                                if(currentConditionMatchResult){
                                    matchResultName= MessageUtil.getString("workflow.commonpage.branch.sucess");
                                }else{
                                    matchResultName= MessageUtil.getString("workflow.commonpage.branch.failed");
                                }
                            }else{
                                matchResultName= MessageUtil.getString("workflow.commonpage.branch.hand");
                            }
                        }else{
                            if(currentConditionMatchResult){
                                matchResultName= MessageUtil.getString("workflow.commonpage.branch.sucess");
                            }else{
                                matchResultName= MessageUtil.getString("workflow.commonpage.branch.failed");
                            }
                        }
                    }
                    vo.setMatchResultName(matchResultName);
                    vo.setProcessMode(policy.getProcessMode());
                    String processModeName= MessageUtil.getString("workflow.commonpage.processmode."+policy.getProcessMode());
                    vo.setProcessModeName(processModeName);
                    vo.setToNodeId(currentNodeId);
                    vo.setToNodeIsInform(ObjectName.isInformObject(toNode)||vo.isCanAutoSkip());
                    vo.setToNodePartyType(partyTypeId);
                    vo.setToNodePolicyId(policy.getId());
                    vo.setCanOrderExecute(String.valueOf(policy.isOrderExecute()));
                    vo.setCanExecuteNormal(String.valueOf(policy.isOrderNormal()));
                    vo.setCanSelectOrgMember(String.valueOf(policy.canSelectOrgMember()));
                    
                    nodeName = hNode.getCustomName();
                    if(Strings.isBlank(nodeName) || "null".equals(nodeName)){
                        nodeName= hNode.getBPMAbstractNodeName();
                    }
                    vo.setToNodeName(nodeName);
                    WorkFlowAppExtendManager matchOrgMemberManager = WorkFlowAppExtendInvokeManager.getAppManager(partyTypeId);
                    if(!partyTypeId.equals(OrgConstants.ORGENT_TYPE.Account.name()) && !"Node".equals(partyTypeId) 
                            && !"FormField".equals(partyTypeId) && null==matchOrgMemberManager){ //岗位,加上单位简称 fix:OA-20300
                        if(Strings.isNotBlank(actor.getParty().getAccountId()) && Strings.isNotBlank(context.getCurrentAccountId())){
                            if(!actor.getParty().getAccountId().equals(context.getCurrentAccountId())){
                                V3xOrgAccount account= processOrgManager.getAccountById(actor.getParty().getAccountId());
                                if(account!=null){
                                    String acountShortName= account.getShortName();
                                    vo.setToNodeName(nodeName+"("+acountShortName+")");
                                }
                            }
                        }
                    }
                    if( "1".equals(preConditionType) || conditionType==1 || conditionType==2 || conditionType==4 || nodePMRVO.isNeedSelectPeople() || currentConditionIsHandle){
                        condtionResultTempAll.put(currentNodeId, vo);
                        cpMatchResult.setBackgroundPop(true);
                    }
                    if(currentConditionIsHandle){//能选分支
                        cpMatchResult.setPop(true);
                        //String canNotSkipMsg= "节点<"+toNode.getName()+">前面的分支条件需要人工进行选择。";
                        //workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(1,context.getMatchRequestToken(), context.getAutoSkipNodeId(),context.getAutoSkipNodeName(), canNotSkipMsg);
                        condtionResult.put(currentNodeId, vo);
                        cpMatchResult.setNeedSelectBranch(true);
                        
                        workFlowMatchUserManager.putWorkflowNeedSelectBranchNodeCacheMap(context.getMatchRequestToken(), currentNodeId, nodeName);
                    }else if(currentConditionMatchResult && nodePMRVO.isNeedSelectPeople()){//能选人
                        cpMatchResult.setPop(true);
                        WorkflowNodeUsersMatchResult matchResult=  workFlowMatchUserManager.getWorkflowMatchedCacheResultMsg(context.getMatchRequestToken(), currentNodeId);
                        String matchMsg="";
                        if(null!=matchResult){
                        	matchMsg= matchResult.getMatchRuleMsg();
                        }
                        //String canNotSkipMsg= "该节点后面的节点<font color='blue'>"+toNode.getName()+"</font>执行模式为<font color='blue'>"+processModeName+"</font>，";
                        String canNotSkipMsg= ResourceUtil.getString("workflow.label.msg.followNodeExeModelDes", toNode.getName(), processModeName);
                        if(Strings.isNotBlank(nodePMRVO.getCannotAutoSkipMsg())){
                        	canNotSkipMsg += nodePMRVO.getCannotAutoSkipMsg();
                        }else{
                          //"人员匹配逻辑为："+matchMsg+"需要选择人员。"
                        	canNotSkipMsg += ResourceUtil.getString("workflow.label.msg.matchLogic", matchMsg); 
                        }
                        workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), context.getAutoSkipNodeId(),context.getAutoSkipNodeName(), canNotSkipMsg);
                        condtionResult.put(currentNodeId, vo);
                    }else if(vo.isCanAutoSkip() && !context.getAutoSkipNodeId().equals(currentNodeId) && currentConditionMatchResult && !currentConditionIsHandle){
                    	boolean isMyDirectParentNode= WorkflowUtil.isMyDirectParentNode(theCase,toNode,context.getAutoSkipNodeId());
                    	if(isMyDirectParentNode){
                    		//String canNotSkipMsg= "该节点后面的节点<font color='blue'>"+toNode.getName()+"</font>设置了无人自动跳过，且通过无人自动跳过条件校验，不需要选择人员。";
                    		String canNotSkipMsg= ResourceUtil.getString("workflow.label.msg.noneedSelect4EmptySkip", toNode.getName());
                    		if(Strings.isNotBlank(context.getValidateStep())){
                    			workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(context.getValidateStep(),context.getMatchRequestToken(), context.getAutoSkipNodeId(),context.getAutoSkipNodeName(), canNotSkipMsg);
                    		}else{
                    			workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), context.getAutoSkipNodeId(),context.getAutoSkipNodeName(), canNotSkipMsg);
                    		}
                    	}
                    }
                    
                    if (currentConditionIsHandle || !currentConditionMatchResult) {//能选择或分支匹配为false
                        if (!currentConditionMatchResult && !currentConditionIsHandle) {//分支匹配为false，且不能选择分支
                            if (!allMustNotSelectNodeList.contains(currentNodeId)) {
                                allMustNotSelectNodeList.add(currentNodeId);
                            }
                            condtionResultTemp.put(currentNodeId, vo); 
                        }
                    } else {
                        allSelectNodeList.add(currentNodeId);
                        if (!ObjectName.isInformObject(hNode) && !vo.isCanAutoSkip()) {//非知会节点
                            currentSelectNodes.add(currentNodeId);
                        }
                    }
                    if (ObjectName.isInformObject(hNode) || vo.isCanAutoSkip()) {//知会节点
                        if(currentConditionMatchResult && !currentConditionIsHandle && nodePMRVO.isNeedSelectPeople()){//强制分支满足，但需要选人。
                            currentSelectInformNodes.add(currentNodeId);
                            allInformNodeList.add(currentNodeId);
                        }
                        if (currentConditionIsHandle || nodePMRVO.isNeedSelectPeople() || !currentConditionMatchResult) {//非强制分支、要选人、分支不满足
                            //不能闯过该知会节点
                        }else {//强制分支满足，不需要选人。
                            allInformNodeList.add(currentNodeId);
                            
                            //设置成自动跳过节点
                            boolean asBlankNode = BPMSeeyonPolicySetting.checkSettingIsValue(policy.getAsBlankNode(), 
                                    BPMSeeyonPolicySetting.NoMatchAutoSkip.AS_BLANK_NODE_YES.getValue());
                            
                            String backUpCurrentUserId= context.getCurrentUserId();
                            String backUpCurrentAccountId= context.getCurrentAccountId();
                            Long backUpCurrentWorkitemId= context.getCurrentWorkitemId();
                            
                            if((vo.isCanAutoSkip() && !asBlankNode) && !ObjectName.isInformObject(hNode)){
                                context.setCurrentUserId("-1");
                                context.setCurrentAccountId("-1");
                                context.setCurrentWorkitemId(-1l);
                            }
                            List<BPMHumenActivity> children = findDirectHumenChildren(passedJoinNodes,toNode, condtionResult, 
                                    condtionResultTemp, condtionResultTempAll,cpMatchResult, context,
                                    allSelectNodeList, allInformNodeList, currentSelectNodes,
                                    currentSelectInformNodes, allMustNotSelectNodeList,isClearAddition,cannotPassedNodes,autoSkipNodeMap);
                            if(!(null!=children && children.size()>0)){//没穿过改知会节点
                                currentSelectInformNodes.add(currentNodeId);
                            }
                            if((vo.isCanAutoSkip() && !asBlankNode) && !ObjectName.isInformObject(hNode)){
                                context.setCurrentUserId(backUpCurrentUserId);
                                context.setCurrentAccountId(backUpCurrentAccountId);
                                context.setCurrentWorkitemId(backUpCurrentWorkitemId);
                            }
                        }
                    }
                    result.add(hNode);
                }
            } else if (nodeType.equals(NodeType.split)) {//split节点(humen->split)或(humen->join)
                ConditionMatchResultVO vo = new ConditionMatchResultVO();
                vo.setConditionDesc(conditionDesc);
                vo.setConditionTitle(conditionTitle);
                if(currentPreConditionType==20){
                    conditionType= 2;
                }
                if(conditionType==3){
                    vo.setConditionDesc(preConditionDesc);
                    vo.setConditionTitle(preConditionTitle);
                }
                vo.setConditionType(String.valueOf(conditionType));
                vo.setFromNodeId(fromNodeId);
                vo.setId(linkId);
                vo.setHand(currentConditionIsHandle);
                vo.setNeedSelectPeople(false);
                vo.setMatchResult(currentConditionMatchResult);
                vo.setProcessMode(policy.getProcessMode());
                vo.setToNodeId(currentNodeId);
                vo.setToNodeName(nodeName);
                vo.setHasBranch(currentConditionHasBranch);
                vo.setDefaultShow(isDefaultShow);
                vo.setPreConditionType(String.valueOf(currentPreConditionType));
                condtionResult.put(currentNodeId, vo);
                //循环递归查找后续人工活动节点
                List<BPMHumenActivity> children = findDirectHumenChildren(passedJoinNodes,toNode, condtionResult, condtionResultTemp, 
                        condtionResultTempAll,cpMatchResult,
                        context, allSelectNodeList, allInformNodeList, currentSelectNodes,
                        currentSelectInformNodes, allMustNotSelectNodeList,isClearAddition,cannotPassedNodes,autoSkipNodeMap);
                result.addAll(children);
                condtionResult.remove(currentNodeId);
            } else if (nodeType.equals(NodeType.join)) {
                Map<String,Boolean> canPassJoinNodes= new HashMap<String, Boolean>();
                boolean isCanPass = isCanPassWithJoin(canPassJoinNodes,toNode, current_node, allMustNotSelectNodeList, allSelectNodeList,
                        allInformNodeList, context,condtionResult,theCase);
                if (isCanPass) {
                    ConditionMatchResultVO vo = new ConditionMatchResultVO();
                    vo.setConditionDesc(conditionDesc);
                    vo.setConditionTitle(conditionTitle);
                    if(currentPreConditionType==20){
                        conditionType= 2;
                    }
                    if(conditionType==3){
                        vo.setConditionDesc(preConditionDesc);
                        vo.setConditionTitle(preConditionTitle);
                    }
                    vo.setConditionType(String.valueOf(conditionType));
                    vo.setFromNodeId(fromNodeId);
                    vo.setId(linkId);
                    vo.setHand(currentConditionIsHandle);
                    vo.setNeedSelectPeople(false);
                    vo.setMatchResult(currentConditionMatchResult);
                    vo.setProcessMode(policy.getProcessMode());
                    vo.setToNodeId(currentNodeId);
                    vo.setToNodeName(nodeName);
                    vo.setHasBranch(currentConditionHasBranch);
                    vo.setDefaultShow(isDefaultShow);
                    vo.setPreConditionType(String.valueOf(currentPreConditionType));
                    condtionResult.put(currentNodeId, vo);
                    //循环递归查找后续人工活动节点
                    List<BPMHumenActivity> children = findDirectHumenChildren(passedJoinNodes,toNode, condtionResult,
                            condtionResultTemp,condtionResultTempAll, cpMatchResult,
                            context, allSelectNodeList, allInformNodeList, currentSelectNodes,
                            currentSelectInformNodes, allMustNotSelectNodeList,isClearAddition,cannotPassedNodes,autoSkipNodeMap);
                    result.addAll(children);
                    condtionResult.remove(currentNodeId);
                    Set<String> nodes= cannotPassedNodes.get(currentNodeId);
                    if(null!=nodes && null!=currentSelectInformNodes && !nodes.isEmpty() && !currentSelectInformNodes.isEmpty()){
                        currentSelectInformNodes.removeAll(nodes);
                    }
                    passedJoinNodes.add(toNode.getId());
                }else{//没有能穿过join的节点集合
                    Set<String> nodes= cannotPassedNodes.get(currentNodeId);
                    if(null==nodes){
                        nodes= new HashSet<String>();
                        nodes.add(current_node.getId());
                    }else{
                        nodes.add(current_node.getId());
                    }
                    cannotPassedNodes.put(currentNodeId, nodes);
                }
            } else if (nodeType.equals(NodeType.end)) {//结束节点
                allSelectNodeList.add(currentNodeId);
                currentSelectNodes.add(currentNodeId);
                return new ArrayList<BPMHumenActivity>(0);
            }
        }
        WorkflowNodeBranchLogUtil.printLogCacheBranchMatchResult();
        WorkflowNodeBranchLogUtil.printLogCacheFormFieldValue();
        return result;
    }

    private static List<BPMTransition> getBPMTransitionBySort(List<BPMTransition> down_linksTemp,WorkflowBpmContext context,boolean isClearAddition,CPMatchResultVO cpMatchResult,
    		Map<String,Boolean> autoSkipNodeMap,ConditionMatchResultVO upCondition,Map<String,NodePeopleMatchResultVO> countResultMap) throws BPMException {
        List<BPMTransition> nonInformNodes= new ArrayList<BPMTransition>();
        List<BPMTransition> informNodes= new ArrayList<BPMTransition>();
        List<BPMTransition> unPassedNodes= new ArrayList<BPMTransition>();//分支条件肯定不满足的节点集合
        List<BPMTransition> autoSkipNodes= new ArrayList<BPMTransition>();
        for (BPMTransition tran : down_linksTemp) {
            BPMAbstractNode currentActivity = tran.getTo();
            String preConditionType= null;
        	int conditionType = tran.getConditionType();
        	String currentCondition = tran.getFormCondition();//获得down线上的条件表达式
            String conditionBase = tran.getConditionBase();//获得down线上的条件的参考对象：当前节点或发起者
            String isForce = tran.getIsForce();
            isForce= (isForce==null || "null".equals(isForce.trim()) || "".equals(isForce.trim()) || "undefined".equals(isForce.trim()))?"0":isForce;
            boolean currentConditionIsHandle = false;
            boolean currentConditionMatchResult = false;
        	if (null != upCondition) {
            	boolean upConditionMatchResult = upCondition.isMatchResult();
                boolean upHasBranch = upCondition.isHasBranch();
                boolean upIsHandle = upCondition.isHand();
                preConditionType= upCondition.getConditionType();
                if(Strings.isNotBlank(upCondition.getPreConditionType()) && ( "30".equals(upCondition.getPreConditionType()) || "20".equals(upCondition.getPreConditionType()))){
                    if(!"2".equals(preConditionType)){
                        preConditionType= upCondition.getPreConditionType();
                    }else{
                        preConditionType= "20";
                    }
                }
                if (upIsHandle) {//可选
                    if (conditionType == 1 || conditionType == 4) {
                    	currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                conditionBase);
                    	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(currentActivity.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                        if(!"1".equals(isForce)){//非强制分支条件
                            currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                            currentConditionIsHandle= true;
                        }else{//强制分支条件
                            if(currentConditionMatchResult){//分支条件满足
                                currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                                currentConditionIsHandle= true;
                            }else{
                                currentConditionIsHandle= false;
                            }
                        }
                    } else if (conditionType == 2) {
                        currentConditionMatchResult = upConditionMatchResult && false;
                        currentConditionIsHandle = true;
                        
                    } else if (conditionType == 3) {
                        currentConditionMatchResult = upConditionMatchResult && true;
                        currentConditionIsHandle = true;
                    }
                } else {//不可选
                    if (upConditionMatchResult) {
                        if (upHasBranch) {
                            if (conditionType == 1 || conditionType == 4) {
                                currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                        conditionBase);
                            	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(currentActivity.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                                currentConditionMatchResult = upConditionMatchResult && currentConditionMatchResult;
                                currentConditionIsHandle = !"1".equals(isForce);
//                                conditionDesc= (null==upConditionDesc || "".equals(upConditionDesc.trim()))?conditionDesc:"("+upConditionDesc+") and ("+conditionDesc+")";
                            } else if (conditionType == 2) {
                                currentConditionMatchResult = upConditionMatchResult && false;
                                currentConditionIsHandle = true;
                            } else if (conditionType == 3) {
                                currentConditionMatchResult = upConditionMatchResult && true;
                                currentConditionIsHandle = false;
                            }
                        } else {
                            if (conditionType == 1 || conditionType == 4) {
                                currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                                        conditionBase);
                            	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(currentActivity.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                                currentConditionMatchResult = true && currentConditionMatchResult;
                                currentConditionIsHandle = !"1".equals(isForce);
                            } else if (conditionType == 2) {
                                currentConditionMatchResult = true && false;
                                currentConditionIsHandle = true;
                            } else if (conditionType == 3) {
                                currentConditionMatchResult = true && true;
                                currentConditionIsHandle = false;
                            }
                        }
                    } else {
                        if (upHasBranch) {
                            currentConditionMatchResult = false;
                            currentConditionIsHandle = false;
                        } else {
                            currentConditionMatchResult = false;
                            currentConditionIsHandle = false;
                        }
                    }
                }
            } else {
                if (conditionType == 1 || conditionType == 4) {
                    currentConditionMatchResult = ActionRunner.getConditionValue(context, currentCondition,
                            conditionBase);
                	WorkflowNodeBranchLogUtil.addCacheBranchMatchResult(currentActivity.getName(), conditionBase, String.valueOf(currentConditionMatchResult), currentCondition);
                    currentConditionIsHandle = !"1".equals(isForce);
                } else if (conditionType == 2) {
                    currentConditionMatchResult = false;
                    currentConditionIsHandle = true;
                } else if (conditionType == 3) {
                    currentConditionMatchResult = true;
                    currentConditionIsHandle = false;
                }
            }
        	if( !currentConditionMatchResult && !currentConditionIsHandle ){
            	unPassedNodes.add(tran);
            }else{
	            if(ObjectName.isInformObject(currentActivity)){//知会节点放到最后面
	                informNodes.add(tran);
	            }else{
	            	NodeType nodeType = currentActivity.getNodeType();
	            	if (nodeType.equals(NodeType.humen)) {//人工活动节点(humen->humen)
	            		BPMHumenActivity hNode = (BPMHumenActivity) currentActivity;
	                    BPMSeeyonPolicy policy= hNode.getSeeyonPolicy();
	                    if( "2".equals(policy.getNa())){//自动跳过节点
	                    	
	                    	boolean isSkipable= isAutoSkipNode(hNode, context, isClearAddition, currentConditionIsHandle, currentConditionMatchResult, autoSkipNodeMap);
	                    	if(isSkipable){
	                    		autoSkipNodes.add(tran);
	                		}else{
		                    	nonInformNodes.add(tran);
		                    }
	                    }else{
	                    	nonInformNodes.add(tran);
	                    }
	            	}else{
	            		nonInformNodes.add(tran);
	            	}
	            }
	        }
        }
        informNodes.addAll(nonInformNodes);
        autoSkipNodes.addAll(informNodes);
        autoSkipNodes.addAll(unPassedNodes);
        return autoSkipNodes;
    }
    
    /**
     * 判断节点是否需要选人
     * @param hactivity 人工活动节点
     * @return
     * @throws BPMException 
     */
    private static boolean isAutoSkipNode(BPMHumenActivity hactivity, 
    		WorkflowBpmContext context,boolean isClearAddition,
    		boolean currentConditionIsHandle,boolean currentConditionMatchResult,Map<String,Boolean> autoSkipNodeMap) throws BPMException {
        List<BPMActor> actors = hactivity.getActorList();
        BPMActor actor = actors.get(0);
        log.info("autoSkipNodeMap:="+autoSkipNodeMap);
        if (!"user".equals(actor.getType().id)//该节点直接是人，不用匹配
                && !WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(actor.getParty().getId())//空节点，不用匹配
        ) {
            if(isClearAddition){
                actor.getParty().setAddition("");
                BPMParticipant party = actor.getParty();
                WorkflowUtil.putNodeAdditionToContext(context, hactivity.getId(), party, "addition","");
            }
            WorkFlowMatchUserManager workFlowOrgManager = WAPIFactory.getWorkFlowOrgManager("Engine_1");
            context.setActivateNode(hactivity);
            List<V3xOrgMember> members = workFlowOrgManager.getUserList("Engine_1", hactivity, context,false);
            String copyFrom = hactivity.getCopyFrom();
            //是否为粘贴替换节点 
            //是否为粘贴替换节点 
            boolean isPasteToActivity = Strings.isNotBlank(copyFrom) && !"null".equals(copyFrom) && !"undefined".equals(copyFrom);
            if(isPasteToActivity){
                BPMHumenActivity copyFromNode= (BPMHumenActivity)context.getProcess().getActivityById(copyFrom);
                if(null!=copyFromNode){
	                String isDelete= WorkflowUtil.getNodeConditionFromContext(context, copyFromNode, "isDelete");
	                if("true".equals(isDelete)){
	                    isPasteToActivity= false;
	                }
                }else{
                	isPasteToActivity= false;
                }
            }
            //不需要匹配，有人就行:1. 该节点是后加的，如：加签;2. 不是模板(自由协同);3. 竞争执行 ;4. 全体执行
            if (members != null && !members.isEmpty()
                    && (hactivity.isCompetitionProcessMode() || hactivity.isAllProcessMode() || isPasteToActivity)) {
                return false;
            }
            else if(Strings.isNotBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))){
            	return false;
            }
            else {
                if (null!= members && members.size() != 1) {//当前匹配到不止一个人时（没有人或多于一个人时），在前端显示出流程人员选择页面
                    BPMSeeyonPolicy policy= hactivity.getSeeyonPolicy();
                    if( "2".equals(policy.getNa()) &&  members.isEmpty()){//自动跳过
                    	if(null!=autoSkipNodeMap.get(hactivity.getId()) && autoSkipNodeMap.get(hactivity.getId()).booleanValue()){ 
                    		return true;
                    	}else{
	                        //a.后面有分支条件 
	                        //b.后面有节点需要选人 
	                        //c.有表单必填项(表单应用特有) 
	                        //d.有触发子流程(表单应用特有)
                    	    //无人视为空节点
	                        boolean isInformNode= ObjectName.isInformObject(hactivity) || BPMSeeyonPolicySetting.checkSettingIsValue(policy.getAsBlankNode(), 
	                                        BPMSeeyonPolicySetting.NoMatchAutoSkip.AS_BLANK_NODE_YES.getValue());
	                        if(isInformNode){
	                            return true;
	                        }else{
	                        	if(currentConditionIsHandle || !currentConditionMatchResult){
	                        		return false;
	                        	}else{//满足条件才看是否可以自动跳过，否则不判断
	                        		return true;
	                        	}
	                        }
                    	}
                    }else {//由上节点选人
                        return false;
                    }
                }else{
                	return false;
                }
            }
        }
        return false;
    }

    /**
     * 解析分支表达式
     * @param currentCondition 分支表达式内容
     * @param conditionBase 分支表达式参考对象
     * @param currentIsStart 是否为开始节点
     * @return
     */
    public static String parseCondition(String currentCondition, String conditionBase, String currentIsStart) {
        if(currentCondition == null){
            return currentCondition;
        }
        currentCondition = currentCondition.replaceAll("isNotRole", "isnotrole").replaceAll("isRole", "isrole")
                .replaceAll("isPost", "ispost").replaceAll("isNotPost", "isNotpost");
        
        if ("start".equals(conditionBase) && !"true".equals(currentIsStart)) {//如果分支条件是基于发起者start，且当前处理节点不是发起节点，则作如下处理
            currentCondition = currentCondition.replace("[Level]", "[startlevel]")
                    .replace("[Account]", "[startaccount]").replace("Concurrent_Acunt", "Start_ConcurrentAcunt")
                    .replace("Concurrent_Levl", "Start_ConcurrentLevl")
                    .replace("Account,Concurrent_Acunt", "startaccount,Start_ConcurrentAcunt")
                    .replace("Level,Concurrent_Levl", "startlevel,Start_ConcurrentLevl");
            currentCondition = currentCondition.replaceAll("Department", "startdepartment")
                    .replaceAll("Post", "startpost").replaceAll("Level", "startlevel").replaceAll("team", "startTeam")
                    .replaceAll("secondpost", "startSecondpost").replaceAll("Account", "startaccount")
                    .replaceAll("standardpost", "startStandardpost").replaceAll("grouplevel", "startGrouplevel")
                    .replaceAll("Role", "startrole").replaceAll("ispost", "isStartpost")
                    .replaceAll("isNotpost", "isNotStartpost").replaceAll("isNotDep", "isNotStartDep")
                    .replaceAll("isDep", "isStartDep");
        }
        return currentCondition;
    }

    /**
     * 判断节点是否需要选人
     * @param hactivity 人工活动节点
     * @return
     * @throws BPMException 
     */
    private static NodePeopleMatchResultVO isNodeNeedSelectPeople(BPMHumenActivity hactivity, 
            WorkflowBpmContext context,boolean isClearAddition,CPMatchResultVO cpMatchResult,
            boolean currentConditionIsHandle,boolean currentConditionMatchResult,Map<String,Boolean> autoSkipNodeMap,
            Set<String> allMustNotSelectNodeList,Set<String> allSelectNodeList,Set<String> allInformNodeList) throws BPMException {
        NodePeopleMatchResultVO vo = new NodePeopleMatchResultVO();
        List<BPMActor> actors = hactivity.getActorList();
        BPMActor actor = actors.get(0);
        String nodeType= "";
        int matchState= 0;
        String matchMsg= "";
        boolean needSelectPeople= false;
        List<String> processMode= new ArrayList<String>();
        BPMSeeyonPolicy policy= hactivity.getSeeyonPolicy();
        String processModeName= MessageUtil.getString("workflow.commonpage.processmode."+policy.getProcessMode());
        if(Strings.isBlank(context.getAutoSkipNodeId())){
    		context.setAutoSkipNodeId(hactivity.getId());
        	context.setAutoSkipNodeName(hactivity.getName()); 
    	}
        if (!"user".equals(actor.getType().id)//该节点直接是人，不用匹配
                && !WorkFlowMatchUserManager.ORGENT_META_KEY_BlankNode.equals(actor.getParty().getId())//空节点，不用匹配
                && !ObjectName.isSuperNode(hactivity)//超级节点不用匹配
        ) {
            if(isClearAddition){
                actor.getParty().setAddition("");
                BPMParticipant party = actor.getParty();
                WorkflowUtil.putNodeAdditionToContext(context, hactivity.getId(), party, "addition","");
            }
            WorkFlowMatchUserManager workFlowOrgManager = WAPIFactory.getWorkFlowOrgManager("Engine_1");
            context.setActivateNode(hactivity);
            
            
            //匹配节点人员
            List<V3xOrgMember> members = workFlowOrgManager.getUserList("Engine_1", hactivity, context,false);
            
            
            String copyFrom = hactivity.getCopyFrom();
            //是否为粘贴替换节点 
            boolean isPasteToActivity = Strings.isNotBlank(copyFrom) && !"null".equals(copyFrom) && !"undefined".equals(copyFrom);
            if(isPasteToActivity){
                BPMHumenActivity copyFromNode= (BPMHumenActivity)context.getProcess().getActivityById(copyFrom);
                if(null!=copyFromNode){
	                String isDelete= WorkflowUtil.getNodeConditionFromContext(context, copyFromNode, "isDelete");
	                if("true".equals(isDelete)){
	                    isPasteToActivity= false;
	                }
                }else{
                	isPasteToActivity= false;
                }
            }
            WorkflowNodeUsersMatchResult matchResult= workFlowMatchUserManager.getWorkflowMatchedCacheResultMsg(context.getMatchRequestToken(), hactivity.getId());
            if(null!=matchResult){
            	matchMsg= matchResult.getMatchRuleMsg();
            	nodeType= matchResult.getNodeOrgType();
            	processMode= matchResult.getProcessMode();
            }
            List<User> users= null;
            //不需要匹配，有人就行:1. 该节点是后加的，如：加签;2. 不是模板(自由协同);3. 竞争执行 ;4. 全体执行
            if (members != null && !members.isEmpty()
                    && (hactivity.isCompetitionProcessMode() || hactivity.isAllProcessMode() || isPasteToActivity)) {
                vo.setNeedSelectPeople(false);
                if(members.size()==1){
                    users= WorkflowUtil.v3xOrgMemberToWorkflowUser(members, false, false);
                }
            } else {
                if (null!= members && members.size() != 1) {//当前匹配到不止一个人时（没有人或多于一个人时），在前端显示出流程人员选择页面
                	needSelectPeople= true;
                	if(Strings.isNotBlank(context.getAutoSkipNodeId())
                			&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))){
                		workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step0, context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), matchMsg);
            		}
                    if( "2".equals(policy.getNa()) &&  members.isEmpty()){//自动跳过
                    	List<String> canNotSkipMsgList= new ArrayList<String>();
                    	List<String> canNotSkipMsgList2= new ArrayList<String>();
                    	if(null!=autoSkipNodeMap.get(hactivity.getId()) && autoSkipNodeMap.get(hactivity.getId()).booleanValue()){ 
                    		vo.setCanAutoSkip(true);//可以自动跳过，当做空节点来处理，是否可穿透的逻辑由此来判断
                            vo.setNeedSelectPeople(false);
                            if(Strings.isNotBlank(context.getAutoSkipNodeId())
                            		&&( context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))){
	                        	WorkflowMatchLogVO myVo= workFlowMatchUserManager.getWorkflowMatchLogList(context.getMatchRequestToken(), hactivity.getId());
	                        	workFlowMatchUserManager.removeWorkflowMatchResult(context.getMatchRequestToken(),hactivity.getId());
	                        	//String canNotSkipMsg= "该节点后面的所有节点可以正常流转到达。";
	                        	String canNotSkipMsg= ResourceUtil.getString("workflow.label.msg.passNormal");
	                        	if(null!=myVo && null!=myVo.getWorkflowMatchMsgMap() && !myVo.getWorkflowMatchMsgMap().isEmpty()){
	                        		List<String> step0List= myVo.getWorkflowMatchMsgMap().get(WorkflowMatchLogMessageConstants.step0);
	                        		List<String> step1List= myVo.getWorkflowMatchMsgMap().get(WorkflowMatchLogMessageConstants.step1);
	                        		if(null!=step0List){
	                        			workFlowMatchUserManager.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step0,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), step0List);
	                        		}
	                        		if(null!=step1List){
	                        			workFlowMatchUserManager.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), step1List);
	                        		}else{
	                        			workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), canNotSkipMsg);
	                        		}
	                        	}else{
	                            	canNotSkipMsgList.add(canNotSkipMsg);
	                        	}
	                        	 //String canNotSkipMsg4= "<font color='red'>经过上面的校验，该节点允许自动跳过，不需要选择人员。</font>";
	                        	 String canNotSkipMsg4= "<font color='red'>" + ResourceUtil.getString("workflow.label.msg.passByChecking") + "</font>";
	                        	 canNotSkipMsgList.add(canNotSkipMsg4);
	                        	
	                        	workFlowMatchUserManager.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), canNotSkipMsgList);
                            }else{
                            	//String canNotSkipMsg4= "设置了无人自动跳过，且通过无人自动跳过条件校验，不需要选择人员。";
                            	String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.noneedSelect4EmptySkip2");
                            	vo.setCannotAutoSkipMsg(canNotSkipMsg4);
                            }
                    	}else{
	                        //a.后面有分支条件 
	                        //b.后面有节点需要选人 
	                        //c.有表单必填项(表单应用特有) 
	                        //d.有触发子流程(表单应用特有)
	                        boolean isInformNode= ObjectName.isInformObject(hactivity);
	                        boolean isAsBlankNode= BPMSeeyonPolicySetting.checkSettingIsValue(policy.getAsBlankNode(), BPMSeeyonPolicySetting.NoMatchAutoSkip.AS_BLANK_NODE_YES.getValue());
							if ((isInformNode || isAsBlankNode) && Strings.isBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))) {
	                            vo.setCanAutoSkip(true);//可以自动跳过，当做空节点来处理，是否可穿透的逻辑由此来判断
	                            vo.setNeedSelectPeople(false);
	                            if(Strings.isNotBlank(context.getAutoSkipNodeId()) 
	                            		&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))){
		                            if(isInformNode){
		                            	String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.infoNode");//"该节点是知会节点。";
		                            	canNotSkipMsgList.add(canNotSkipMsg4);
		                            }else if(isAsBlankNode){
		                                //"该节点被设置了无人自动跳过时当做空节点处理。"
		                            	String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.emptyAsBlank");
		                            	canNotSkipMsgList.add(canNotSkipMsg4);
		                            }
		                            //String canNotSkipMsg4= "<font color='red'>经过上面的校验，该节点允许自动跳过，不需要选择人员。</font>";
		                            String canNotSkipMsg4= "<font color='red'>" + ResourceUtil.getString("workflow.label.msg.passByChecking") + "</font>";;
	                                canNotSkipMsgList2.add(canNotSkipMsg4);
	                            }
	                        }else{
	                            boolean isCan= false;
	                            if(currentConditionIsHandle || !currentConditionMatchResult || Strings.isNotBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))){
	                            	if(Strings.isNotBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))){
	                            		vo.setCannotAutoSkipMsg(context.getHumenNodeMatchAlertMsgById(hactivity.getId()));
	                            	}
	                            	else if(currentConditionIsHandle){
		                            	if(Strings.isNotBlank(context.getAutoSkipNodeId()) 
		                            			&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))){
		                            		String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.thePrenodeNeedSelect");//"该节点前面的分支条件需要人工选择。";
			                            	canNotSkipMsgList.add(canNotSkipMsg4); 
			                            	
			                            	//String canNotSkipMsg41= "<font color='red'>经过上面的校验，该节点不允许自动跳过，需要选择人员。</font>";
			                            	String canNotSkipMsg41= "<font color='red'>" + ResourceUtil.getString("workflow.label.msg.notPassByChecking") + "</font>";;
			                                canNotSkipMsgList2.add(canNotSkipMsg41);
		                            	}
		                            	else{
		                            		//String canNotSkipMsg4= "虽然设置了无人自动跳过，由于前面的分支条件需要人工选择，不允许自动跳过，需要选择人员。";
		                            	    String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.cannotSkip4select");
			                                vo.setCannotAutoSkipMsg(canNotSkipMsg4);
		                            	}
	                            	}
	                                isCan= false;
	                            }else{//满足条件才看是否可以自动跳过，否则不判断
	                            	String parentAutoSkipNodeId= "";
	                            	String parentAutoSkipNodeName= "";
	                            	String parentValidateStep= "";
	                            	if(Strings.isNotBlank(context.getAutoSkipNodeId()) && !context.getAutoSkipNodeId().equals(hactivity.getId())){
	                            		parentAutoSkipNodeId= context.getAutoSkipNodeId();
	                            		parentAutoSkipNodeName= context.getAutoSkipNodeName();
	                            	}
	                            	if(Strings.isNotBlank(context.getValidateStep())){
	                            		parentValidateStep= context.getValidateStep();
	                            		context.setValidateStep("");
	                            	}
	                            	context.setAutoSkipNodeId(hactivity.getId());
	                            	context.setAutoSkipNodeName(hactivity.getName()); 
	                                String[] nodeCanAutoSkipArr= isNodeCanAutoSkip(hactivity,context,isClearAddition,cpMatchResult,autoSkipNodeMap,
	                                        allMustNotSelectNodeList,allSelectNodeList,allInformNodeList);
	                                if(null!=nodeCanAutoSkipArr && "false".equals(nodeCanAutoSkipArr[0])){
	                                	isCan= false;
	                                }else{
	                                	isCan= true;
	                                }
	                                String nodeCanAutoSkipMsg= nodeCanAutoSkipArr[1];
	                                if(Strings.isNotBlank(parentAutoSkipNodeId)){
	                                	context.setAutoSkipNodeId(parentAutoSkipNodeId);
		                            	context.setAutoSkipNodeName(parentAutoSkipNodeName); 
	                                }
	                                if(Strings.isNotBlank(parentValidateStep)){
	                                	context.setValidateStep(parentValidateStep);
	                                }
	                                if(Strings.isNotBlank(nodeCanAutoSkipMsg) && Strings.isNotBlank(context.getAutoSkipNodeId()) 
	                                		&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))
	                                		&& !isCan){
	                                	nodeCanAutoSkipMsg= ResourceUtil.getString("workflow.label.msg.thisNode")/*"该节点"*/+nodeCanAutoSkipMsg;
	                                	canNotSkipMsgList2.add(nodeCanAutoSkipMsg);
	                                }
	                                
	                                if(!isCan){
	                                	if(Strings.isNotBlank(context.getAutoSkipNodeId()) 
	                                			&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()) )){
	                                	    
	                                	    //经过上面的校验，该节点不允许自动跳过，需要选择人员。
		                                	String canNotSkipMsg4= "<font color='red'>" + ResourceUtil.getString("workflow.label.msg.notPassByChecking") + "</font>";
			                                canNotSkipMsgList2.add(canNotSkipMsg4);
		                                }else{
		                                	//String canNotSkipMsg4= "虽然设置了无人自动跳过，但没通过无人自动跳过条件校验，需要选择人员。";
		                                	String canNotSkipMsg4 = ResourceUtil.getString("workflow.label.msg.cannotSkip4select2");
			                                vo.setCannotAutoSkipMsg(canNotSkipMsg4);
		                                }
	                                }else{
	                                	if(Strings.isNotBlank(context.getAutoSkipNodeId())
	                                			&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()) )){
			                                //String canNotSkipMsg4= "<font color='red'>经过上面的校验，该节点允许无人自动跳过，不需要选择人员。</font>";
			                                String canNotSkipMsg4= "<font color='red'>" + ResourceUtil.getString("workflow.label.msg.passByChecking2") + "</font>";
			                                canNotSkipMsgList2.add(canNotSkipMsg4);
		                                }else{
		                                	//String canNotSkipMsg4= "设置了无人自动跳过，且通过无人自动跳过条件校验，不需要选择人员。";
		                                	String canNotSkipMsg4= ResourceUtil.getString("workflow.label.msg.noneedSelect4EmptySkip2");
		                                	vo.setCannotAutoSkipMsg(canNotSkipMsg4);
		                                } 
	                                }
	                            }
	                            if(!isCan){
	                            	if(Strings.isNotBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))){
	                            		vo.setNeedSelectPeople(false);
	                            	}else{
	                            		vo.setNeedSelectPeople(true);
	                            	}
	                                matchState= 1;
	                            }else{
	                                vo.setCanAutoSkip(true);//可以自动跳过，当做空节点来处理，是否可穿透的逻辑由此来判断
	                                vo.setNeedSelectPeople(false);
	                                vo.setNa("2");
	                                vo.setNeedPeopleTag(true);
	                            }
	                        }
	                        if(Strings.isNotBlank(context.getAutoSkipNodeId())
	                        		&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep()))){
	                        	workFlowMatchUserManager.putWorkflowMatchLogToCacheHead(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(),hactivity.getId(), hactivity.getName(), canNotSkipMsgList);
	                        	workFlowMatchUserManager.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step1,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), canNotSkipMsgList2);
	                        }
                    	}
                    }else {//由上节点选人
                    	vo.setNeedSelectPeople(true);
                    	matchState= 1;
                    }
                }
                users= WorkflowUtil.v3xOrgMemberToWorkflowUser(members, false, true);
            }
            if(vo.isCanAutoSkip()){
            	autoSkipNodeMap.put(hactivity.getId(), vo.isCanAutoSkip());
            }
            vo.setPeoples(users);
        }
        boolean isCannotPass= !currentConditionIsHandle && !currentConditionMatchResult;
        if(Strings.isNotBlank(context.getAutoSkipNodeId()) 
        		&& (context.getAutoSkipNodeId().equals(hactivity.getId()) || Strings.isNotBlank(context.getValidateStep())) 
        		&& ( isCannotPass|| needSelectPeople)){
	        if(null!=processMode){
	        	processMode.add(0,processModeName);
	        }else{
	        	processMode= new ArrayList<String>();
	        	processMode.add(processModeName);
	        }
	        workFlowMatchUserManager.putWorkflowMatchLogNodeTypeToCache(context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), nodeType);
	        workFlowMatchUserManager.putWorkflowMatchLogProcessModeToCache(context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), processMode);
	        workFlowMatchUserManager.putWorkflowMatchLogMatchStateToCache(context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), matchState);
	        if(!currentConditionIsHandle && !currentConditionMatchResult){
	        	String branchNotMatchMsg= ResourceUtil.getString("workflow.label.msg.notMatchBranch");//"该节点前的分支条件强制不满足，流程不再流转经过该节点。";
	        	workFlowMatchUserManager.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step3,context.getMatchRequestToken(), hactivity.getId(), hactivity.getName(), branchNotMatchMsg);
	        }
        }
        if(hactivity.getId().equals(context.getAutoSkipNodeId())){
        	context.setAutoSkipNodeId("");
        }
        return vo;
    }

    /**
     * 该节点是否自动跳过
     * @param hactivity
     * @param context
     * @param isClearAddition
     * @return
     */
    private static String[] isNodeCanAutoSkip(BPMHumenActivity hactivity, WorkflowBpmContext context,
            boolean isClearAddition,CPMatchResultVO cpMatchResult,Map<String,Boolean> autoSkipNodeMap,
            Set<String> allMustNotSelectNodeList,Set<String> allSelectNodeList,Set<String> allInformNodeList) {
    	String[] nodeCanAutoSkipArr= {"true",""};
        boolean result= true;
        String nodeMsg= "";
        BPMSeeyonPolicy policy= hactivity.getSeeyonPolicy();
        String nf= policy.getNF();
        String formAppId = context.getFormData();
        String formViewOperation = policy.getFormViewOperation();
        String policyId= policy.getId();
        
        //无人视为空节点
        boolean asBlankNode =  BPMSeeyonPolicySetting.checkSettingIsValue(policy.getAsBlankNode(), 
                BPMSeeyonPolicySetting.NoMatchAutoSkip.AS_BLANK_NODE_YES.getValue());
        
        //无人忽略表单必填项
        boolean ignoreFormBlank =  BPMSeeyonPolicySetting.checkSettingIsValue(policy.getIgnoreBlank(), 
                BPMSeeyonPolicySetting.NoMatchAutoSkip.IGNORE_FORM_BLANK_YES.getValue());
        
        
        boolean  hasFormData  = null!=context.getFormData() && Strings.isNotBlank(context.getFormData())  && !"null".equals(context.getFormData())  
        						&& !"undefined".equals(context.getFormData())  && !"-1".equals(context.getFormData()) ;
        
        boolean hasMasterId  = null!=context.getMastrid() && Strings.isNotBlank(context.getMastrid()) && !"null".equals(context.getMastrid())
                				&& !"undefined".equals(context.getMastrid())&& !"-1".equals(context.getMastrid());
        
        if(Strings.isNotBlank(context.getHumenNodeMatchAlertMsgById(hactivity.getId()))){
        	result = false;
        }
        else if(asBlankNode){//视为空节点
        	nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_ASBLANKNODE);
        }
        else if(("1".equals(nf) || "fengfa".equals(policyId))&& !asBlankNode){//触发新流程或公文封发节点
        	if("1".equals(nf) ){
        		nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB);
        	}else if("fengfa".equals(policyId)){
        		nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FENGFA);
        	}
        	result= false;
        }
        else if( hasFormData  || hasMasterId){//校验表单必填项
        	if(ignoreFormBlank){
        		nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NDOE_BIND_POLICY_FORMMUSTWRITE_INGORE);
        	}else if(WorkflowUtil.isLong(formAppId) && !ignoreFormBlank){
                Set<String> isFormMustWrite= WorkflowFormDataMapInvokeManager.getAppManager("form")
                        .isFormMustWrite(Long.parseLong(formAppId), formViewOperation);
                
                if(!isFormMustWrite.isEmpty()){
                	nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FORM_FIELD_MUSTWRITE)+Strings.join(isFormMustWrite, ",");
                    result= false;
                }else{
                	//前面节点触发的子流程是否已经结束
                	result= isPreSubFlowFinished(context);
                	if(!result){
                		nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB_UNFINISHED);
                	}
                }
            }
        }
        
        if(result && !asBlankNode){
        	List<BPMCircleTransition> circleLinks= hactivity.getDownCirlcleTransitions();
        	if(null!=circleLinks){
        		for (BPMCircleTransition bpmCircleTransition : circleLinks) {
					if(null!=bpmCircleTransition.getTo()){
						nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_LINK_CIRCLE);
						result= false;
						break;
					}
				}
        	}
        }
        if(result){//判断分支条件和是否选人
            BPMAbstractNode backUpNode= context.getActivateNode();
            String backUpCurrentActivityId= context.getCurrentActivityId();
            String backUpCurrentUserId= context.getCurrentUserId();
            String backUpCurrentAccountId= context.getCurrentAccountId();
            Long backUpCurrentWorkitemId= context.getCurrentWorkitemId();
            context.setActivateNode(hactivity);
            context.setCurrentActivityId(hactivity.getId());
            context.setCurrentUserId("-1");
            context.setCurrentAccountId("-1");
            context.setCurrentWorkitemId(-1l);
            Set<String> myCurrentSelectNodes= new HashSet<String>();
            myCurrentSelectNodes.add(hactivity.getId());
            CPMatchResultVO cpMatchResult1= new CPMatchResultVO();
            BPMProcess process= context.getProcess();
            BPMStatus startNode = process.getStart();
            
            cpMatchResult1.setAllNotSelectNodes(getNewSet(allMustNotSelectNodeList));
            cpMatchResult1.setAllSelectInformNodes(getNewSet(allInformNodeList)); 
            cpMatchResult1.setAllSelectNodes(getNewSet(allSelectNodeList));
            if(null!=cpMatchResult1.getAllNotSelectNodes()){
            	cpMatchResult1.getAllNotSelectNodes().addAll(getNewSet(cpMatchResult.getAllNotSelectNodes()));
            }else{
            	cpMatchResult1.setAllNotSelectNodes(getNewSet(cpMatchResult.getAllNotSelectNodes()));
            }
            if(null!=cpMatchResult1.getAllSelectInformNodes()){
            	cpMatchResult1.getAllSelectInformNodes().addAll(getNewSet(cpMatchResult.getAllSelectInformNodes()));
            }else{
            	cpMatchResult1.setAllSelectInformNodes(getNewSet(cpMatchResult.getAllSelectInformNodes()));
            }
            if(null!=cpMatchResult1.getAllSelectNodes()){
            	cpMatchResult1.getAllSelectNodes().addAll(getNewSet(cpMatchResult.getAllSelectNodes()));
            }else{
            	cpMatchResult1.setAllSelectNodes(getNewSet(cpMatchResult.getAllSelectNodes()));
            }
            cpMatchResult1.setCaseId(cpMatchResult.getCaseId());
            cpMatchResult1.setCurrentSelectInformNodes(getNewSet(cpMatchResult.getCurrentSelectInformNodes()));
            cpMatchResult1.setCurrentSelectNodes(getNewSet(cpMatchResult.getCurrentSelectNodes()));
            try {
                if(!asBlankNode){
                    Map<String, ConditionMatchResultVO> condtionResultAll= new HashMap<String, ConditionMatchResultVO>();
                    nodeMsg= BranchArgs.doMatchAll(cpMatchResult1,myCurrentSelectNodes,context,isClearAddition,context.getProcess(),startNode,condtionResultAll,autoSkipNodeMap);
                    String invalidateActivityMapStr= WorkflowUtil.getInvalidateActivityMapStr(cpMatchResult.getInvalidateActivityMap(),false);
                    if(Strings.isNotBlank(invalidateActivityMapStr)){
                    	cpMatchResult1.setPop(true);
                    	nodeMsg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.CHILIDNODE_HUMEN_INVALID) +invalidateActivityMapStr;
                    }
                    boolean isPop = cpMatchResult1.isPop();
                    //节点设置了，如果没有匹配到人视为空节点
                    result= !isPop;
                }
            } catch (BPMException e) {
                log.error("判断分支条件和是否选人", e);
            }
            context.setActivateNode(backUpNode);
            context.setCurrentActivityId(backUpCurrentActivityId);
            context.setCurrentUserId(backUpCurrentUserId);
            context.setCurrentAccountId(backUpCurrentAccountId);
            context.setCurrentWorkitemId(backUpCurrentWorkitemId);
        }
        nodeCanAutoSkipArr[0]= String.valueOf(result);
        nodeCanAutoSkipArr[1]= nodeMsg;
        return nodeCanAutoSkipArr;
    }
    
    /**
     * 前面的子流程是否已经结束
     * @param context
     * @return
     */
    private static boolean isPreSubFlowFinished(WorkflowBpmContext context) {
    	String popNodeSubProcessJson= context.getPopNodeSubProcessJson();
    	if(Strings.isNotBlank(popNodeSubProcessJson)){
    		String hasNewflow = "false";
    		try{
                JSONObject popNodeNewFlowObj= new JSONObject(popNodeSubProcessJson);
                hasNewflow = popNodeNewFlowObj.getString("hasNewflow");
                if ("true".equals(hasNewflow)) {
                	JSONArray popNodeNewFlowAr = popNodeNewFlowObj.getJSONArray("newFlows");
                	if (popNodeNewFlowAr != null && popNodeNewFlowAr.length() > 0) {
                		List<String> nodeIds = new ArrayList<String>();
                        nodeIds.add(context.getCurrentActivityId());
                		SubProcessManager      subProcessManager= (SubProcessManager)AppContext.getBean("subProcessManager");
                		List<SubProcessRunning> subList = subProcessManager.getSubProcessRunningListByMainProcessId(context.getProcessId(), nodeIds, null, null);
                        if (subList != null && subList.size() > 0) {
                        	for (SubProcessRunning sub : subList) {
                        		if(sub.getFlowRelateType()==1){
                        			return false;
                        		}
                        	}
                        }
                    }
                }
    		} catch (Throwable e) {
				log.error("",e);
			}
    	}
		return true;
	}
    
    private static Set<String> getNewSet(Set<String> oldSet) {
        Set<String> newSet= new HashSet<String>();
        if(null!=oldSet){
            for (String nodeId : oldSet) {
                newSet.add(nodeId);
            }
        }
        return newSet;
    }

    /**
     * 对一个节点的分支和人员进行匹配
     * @param cpMatchResult
     * @param currentActivity
     * @param context
     * @throws BPMException
     */
    public static void doMatch(Set<String> passedJoinNodes,CPMatchResultVO cpMatchResult, BPMAbstractNode currentActivity,
            WorkflowBpmContext context,boolean isClearAddition,Map<String, ConditionMatchResultVO> condtionResult,
            Map<String, ConditionMatchResultVO> condtionResultTemp,Map<String, ConditionMatchResultVO> condtionResultTempAll,Set<String> currentSelectNodes,
            Set<String> currentSelectInformNodes,List childs,Map<String,Set<String>> cannotPassedNodes,Map<String,Boolean> autoSkipNodeMap) throws BPMException {
        try {
            Set<String> allMustNotSelectNodeList = new HashSet<String>();
            if (null != cpMatchResult.getAllNotSelectNodes()) {
                allMustNotSelectNodeList.addAll(cpMatchResult.getAllNotSelectNodes());
            }
            Set<String> allSelectNodeList = new HashSet<String>();
            if (null != cpMatchResult.getAllSelectNodes()) {
                allSelectNodeList = cpMatchResult.getAllSelectNodes();
            }
            Set<String> allInformNodeList = new HashSet<String>();
            if (null != cpMatchResult.getAllSelectInformNodes()) {
                allInformNodeList = cpMatchResult.getAllSelectInformNodes();
            }
            List childs1= BranchArgs.findDirectHumenChildren(passedJoinNodes,currentActivity, condtionResult, condtionResultTemp,condtionResultTempAll, cpMatchResult, context,
                    allSelectNodeList, allInformNodeList, currentSelectNodes,
                    currentSelectInformNodes, allMustNotSelectNodeList,isClearAddition,cannotPassedNodes,autoSkipNodeMap);
            childs.addAll(childs1);
            cpMatchResult.setCondtionMatchMap(condtionResult);
            cpMatchResult.setCaseId(String.valueOf(context.getCaseId()));
            cpMatchResult.setProcessId(context.getProcessId());
            cpMatchResult.setWorkItemId(String.valueOf(context.getCurrentWorkitemId()));
            cpMatchResult.setAllNotSelectNodes(allMustNotSelectNodeList);
            cpMatchResult.setAllSelectNodes(allSelectNodeList);
            cpMatchResult.setAllSelectInformNodes(allInformNodeList);
            cpMatchResult.setCurrentSelectNodes(currentSelectNodes);
            cpMatchResult.setCurrentSelectInformNodes(currentSelectInformNodes);
            cpMatchResult.setAllSelectInformNodes(allInformNodeList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BPMException("beforeInvokeWorkFlow............An exception occurred", e);
        }
    }
    
    
   
    
    private static  HashMap<String,Object> splitCondition(HashMap<String,String> hash){
        HashMap<String,Object> result = new HashMap<String,Object>();
        Set<Map.Entry<String, String>> entry = hash.entrySet();
        List<String> keys = new ArrayList<String>();
        List<String> nodeNames = new ArrayList<String>();
        List<String> conditions = new ArrayList<String>();
        List<String> forces = new ArrayList<String>();
        List<String> links = new ArrayList<String>();
        String[] temp = null;
        String[] temp1 = null;
        String order = hash.get("order");
        if(order != null && order.indexOf("$")!=-1) {
            temp1 = WorkflowUtil.split(order,"$");
        }

        StringBuffer sb = new StringBuffer();
        if(temp1!=null && temp1.length>0) {
            for(String item:temp1){
                String value = hash.get(item);
                if(value!=null&&value.indexOf("↗")!=-1){
                    sb.append(item+":");
                    keys.add(item);
                    links.add(hash.get("linkTo"+item));
                    temp = value.split("↗");
                    if(temp != null){
                        nodeNames.add(temp[0]);
                        //temp[1] = temp[1].replaceAll("handCondition", "false");
                        conditions.add(temp[1]);
                        if(temp.length==3 && "1".equals(temp[2]))
                            forces.add("true");
                        else
                            forces.add("false");
                    }
                }
            }
        }else {
            for(Map.Entry<String, String> item:entry){
                if(item.getValue()!=null&&item.getValue().indexOf("↗")!=-1){
                    sb.append(item.getKey()+":");
                    keys.add(item.getKey());
                    links.add(hash.get("linkTo"+item.getKey()));
                    temp = item.getValue().split("↗");
                    if(temp != null){
                        nodeNames.add(temp[0]);
                        //temp[1] = temp[1].replaceAll("handCondition", "false");
                        conditions.add(temp[1]);
                        if(temp.length==3)
                            forces.add("true");
                        else
                            forces.add("false");
                    }
                }
            }
        }
        if(keys.size() > 0 && conditions.size() >0){
            result.put("allNodes", sb.toString());
            result.put("keys", keys);
            result.put("names", nodeNames);
            result.put("conditions", conditions);
            result.put("nodeCount", hash.get("nodeCount"));
            result.put("forces", forces);
            result.put("links", links);
        }
        return result;
    }
    
    /**
     * 
     * @param followUpMap
     * @param currentNode
     */
    private static void getFollowUpDirectInfo(Map followUpMap,
            BPMAbstractNode currentNode) {
        //获得当前节点的down线集合
        List<BPMTransition> downs= currentNode.getDownTransitions();
        if(downs!= null && downs.size()>0 ){
            for (BPMTransition bpmTransition : downs) {
                //获得down线指向的节点
                BPMAbstractNode toNode= bpmTransition.getTo();
                //获得当前节点的节点类型
                NodeType nodeType= toNode.getNodeType();
                //System.out.println("nodeType:="+nodeType);
                if( nodeType.equals(NodeType.split) || nodeType.equals(NodeType.join) ){//split节点或join节点
                    //继续查找后续人工活动节点
                    getFollowUpDirectInfo(followUpMap,toNode);
                }else if( nodeType.equals(NodeType.humen) ){//humen节点
                    String toNodePolicy = toNode.getSeeyonPolicy().getId();
                    BPMHumenActivity hactivity= (BPMHumenActivity)toNode;
                    if( "inform".equals(toNodePolicy) || "zhihui".equals(toNodePolicy) ){//知会节点
                        Map allInformNodes= null;
                        if( followUpMap.get("allInformNodes") != null ){
                            allInformNodes= (Map)followUpMap.get("allInformNodes");
                            if( allInformNodes.get(toNode.getId()) == null ){
                                allInformNodes.put(toNode.getId(), toNode);
                            }
                        }else{
                            allInformNodes= new HashMap();
                            allInformNodes.put(toNode.getId(), toNode);
                        }
                        followUpMap.put("allInformNodes", allInformNodes);
                    }else{//非知会节点
                        Map allNonInformNodes= null;
                        if( followUpMap.get("allNonInformNodes") != null ){
                            allNonInformNodes= (Map)followUpMap.get("allNonInformNodes");
                            if( allNonInformNodes.get(toNode.getId()) == null ){
                                allNonInformNodes.put(toNode.getId(), toNode);
                            }
                        }else{
                            allNonInformNodes= new HashMap();
                            allNonInformNodes.put(toNode.getId(), toNode);
                        }
                        followUpMap.put("allNonInformNodes", allNonInformNodes);
                    }
                    //判断该人工活动节点的执行人员是否为不可用
                    BPMActor actor = (BPMActor) hactivity.getActorList().get(0);
                    BPMParticipant party = actor.getParty();
                    String partyTypeId= party.getType().id;
                    if(!"normal".equals(hactivity.isValid()) && "user".equals(partyTypeId)){ //节点不可用
                        Map invalidateActivityMap= null;
                        if( followUpMap.get("invalidateActivityMap") != null ){
                            invalidateActivityMap= (Map)followUpMap.get("invalidateActivityMap");
                            invalidateActivityMap.put(hactivity.getId(), hactivity.getBPMAbstractNodeName());
                        }else{
                            invalidateActivityMap= new HashMap();
                            invalidateActivityMap.put(hactivity.getId(), hactivity.getBPMAbstractNodeName());
                        }
                        followUpMap.put("invalidateActivityMap", invalidateActivityMap);
                    }
                }
                //对down进行分支条件处理
                String currentCondition = bpmTransition.getFormCondition();
                Map allBranchConditions= null;
                if(followUpMap.get("allBranchConditions")==null){
                    allBranchConditions= new HashMap();
                }else{
                    allBranchConditions= (Map)followUpMap.get("allBranchConditions");
                }
                if(currentCondition != null && !"".equals(currentCondition.trim()) && !"null".equals(currentCondition.trim())){//有自动条件分支
                    allBranchConditions.put(bpmTransition.getId(), currentCondition);
                    followUpMap.put("allBranchConditions", allBranchConditions);
                }else if(bpmTransition.getConditionType()==2){//有手动选择分支
                    allBranchConditions.put(bpmTransition.getId(), "handCondition");
                    followUpMap.put("allBranchConditions", allBranchConditions);
                }
            }
        }
    }
    
  
    
    /**
     * isCanPassWithJoin()
     * 判断是否可以穿过该join节点，这是一个递归判断过程
     * @param toNode join节点
     * @param current_node 当前处理节点
     * @param allNotSelectNodeList 在弹出页面所有没被选中的节点集合
     * @param allSelectNodeList 在弹出页面中所有被选中的节点集合
     * @param informNodeList 在弹出页面中所有被选中的知会节点集合
     * @param context 上下文信息
     * @return true：可以穿过；false：不可以穿过
     * @throws BPMException
     */
    private static boolean isCanPassWithJoin(
            BPMAbstractNode toNode,
            BPMActivity current_node,
            List<String> allNotSelectNodeList,
            List<String> allSelectNodeList,
            List<String> informNodeList,
            List<String> allInformNodeList,
            Map context) throws BPMException {
        BPMCase theCase= null;
        if(context.get("case")!=null){
            theCase= (BPMCase)context.get("case");
        }
        //默认可以穿过
        boolean isCanPass= true;
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
        //获得该join节点的所有up线
        List<BPMTransition> joinUps= toNode.getUpTransitions();
        for (BPMTransition bpmTransition2 : joinUps) {
            BPMAbstractNode fromNode= bpmTransition2.getFrom();
            String nodeId= fromNode.getId();
            //String fromNodePolicy = fromNode.getSeeyonPolicy().getId();
            //String isDelete= fromNode.getSeeyonPolicy().getIsDelete();
            String isDelete= WorkflowUtil.getNodeConditionFromCase(theCase, fromNode, "isDelete");
            //System.out.println("nodeId:="+nodeId);
            //System.out.println("fromNodePolicy:="+fromNodePolicy);
            //System.out.println("isDelete:="+isDelete);
            if("false".equals(isDelete) && !current_node.getId().equals(nodeId) && isCanPass){//from节点没有被删除，且不是当前处理节点
                if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){//humen->split
                    BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity)fromNode;
                    String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
                    //计算出当前from节点是否为知会节点
                    boolean isInformNode = currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
                    //看是否被选中
                    if(allSelectNodeList.contains(nodeId)){//选中
                        //判断fromNode节点是否为选中的知会节点
                        if(allInformNodeList.contains(nodeId)){//是选中的知会节点
                            //继续，对isCanPass=true没有影响
                            continue;
                        }else{//不是选中的知会节点，则肯定是选中的非知会节点(则不让穿过该join节点)
                            isCanPass= false;
                        }
                    }else if(allNotSelectNodeList.contains(nodeId)){//没选中
                        if(isInformNode){//是没选中的知会节点
//                          isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,context);
                            //继续，对isCanPass=true没有影响
                            continue;
                        }else{//是没选中的非知会节点(则不让穿过该join节点)
                            //继续，对isCanPass=true没有影响
                            continue;
                        }
                    }else{
                        //判断该humen节点是否已产生待办
//                      boolean isDoing= isDoingWithHumen(theCase,nodeId);
//                      boolean isDone= isDoneWithHumen(theCase,nodeId);
                        boolean isDoing= false;
                        boolean isDone= false;
                        if(null!=theCase){
                            isDoing= WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_READY,CaseDetailLog.STATE_ZCDB);
                            isDone= WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_FINISHED,
                                    CaseDetailLog.STATE_CANCEL,CaseDetailLog.STATE_STOP);
                        }
                        //if(log.isInfoEnabled()){
                            //log.info("isDoing:="+isDoing);
                            //log.info("isDone:="+isDone);
                        //}
                        if(isDoing){//如果是待办状态
                            if(isInformNode){//是知会节点
                                continue;
                            }else{
                                isCanPass= false;
                            }
                        }else if(isDone){//如果是办已办状态
                            continue;
                        }else{//即没有产生待办，也没有产生已办
                            if(isInformNode){//是知会节点
//                              isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                                isCanPass= isCanPassWithNonInformNode(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                            }else{//不是知会节点
//                              isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,context); 2012-2-7
                                isCanPass= isCanPassWithNonInformNode(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                            }
                        }
                    }
                }
                if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){//join->join
                    //以fromNode为基础递归往后查找
                    isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                }
                if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){//split->中间穿过一些知会节点->join
                    //以fromNode为基础递归往后查找
                    isCanPass= isCanPassWithJoin(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                }
            }
            if(!isCanPass){
                break;
            }
        }
        return isCanPass;
    }
    
    /**
     * 判断该非知会节点是否可以被穿过
     * @param fromNode
     * @param current_node
     * @param allNotSelectNodeList
     * @param allSelectNodeList
     * @param informNodeList
     * @param context
     * @return
     */
    private static boolean isCanPassWithNonInformNode(BPMAbstractNode toNode,
            BPMActivity current_node, List<String> allNotSelectNodeList,
            List<String> allSelectNodeList, List<String> informNodeList,List<String> allInformNodeList,
            Map context) {
        BPMCase theCase= null;
        if(context.get("case")!=null){
            theCase= (BPMCase)context.get("case");
        }
        //默认可以穿过
        boolean isCanPass= true;
        //获得标识知会的两个常量
        String informActivityPolicy = BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId();
        String edocInformActivityPolicy = BPMSeeyonPolicy.EDOC_POLICY_ZHIHUI.getId();
        //获得该join节点的所有up线
        List<BPMTransition> joinUps= toNode.getUpTransitions();
        for (BPMTransition bpmTransition2 : joinUps) {
            BPMAbstractNode fromNode= bpmTransition2.getFrom();
            String nodeId= fromNode.getId();
            String isDelete= WorkflowUtil.getNodeConditionFromCase(theCase, fromNode, "isDelete");
            if(isCanPass){
                if("false".equals(isDelete) && !current_node.getId().equals(nodeId)){//from节点没有被删除，且不是当前处理节点
                    if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){//humen->split
                        BPMHumenActivity fromHumenNodeOfcurrBackNode = (BPMHumenActivity)fromNode;
                        String currFromHumenNodePolicy = fromHumenNodeOfcurrBackNode.getSeeyonPolicy().getId();
                        //计算出当前from节点是否为知会节点
                        boolean isInformNode = currFromHumenNodePolicy.equals(informActivityPolicy) || currFromHumenNodePolicy.equals(edocInformActivityPolicy);
                        //看是否被选中
                        if(allSelectNodeList.contains(nodeId)){//选中
                            //判断fromNode节点是否为选中的知会节点
                            if(allInformNodeList.contains(nodeId)){//是选中的知会节点(则不让穿过该join节点)
                                isCanPass= false;
                            }else{//不是选中的知会节点，则肯定是选中的非知会节点(则不让穿过该join节点)
                                isCanPass= false;
                            }
                        }else if(allNotSelectNodeList.contains(nodeId)){//没选中
                            if(isInformNode){//是没选中的知会节点
                                //继续，对isCanPass=true没有影响
                                continue;
                            }else{//是没选中的非知会节点
                                //继续，对isCanPass=true没有影响
                                continue;
                            }
                        }else{
                            //判断该humen节点是否已产生待办
                            boolean isDoing= false;
                            boolean isDone= false;
                            if(null!=theCase){
                                isDoing= WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_READY,CaseDetailLog.STATE_ZCDB);
                                isDone= WorkflowUtil.isThisState(theCase, nodeId, CaseDetailLog.STATE_FINISHED,
                                        CaseDetailLog.STATE_CANCEL,CaseDetailLog.STATE_STOP);
                            }
                            //if(log.isInfoEnabled()){
                                //log.info("isDoing:="+isDoing);
                                //log.info("isDone:="+isDone);
                            //}
                            if(isDoing){//如果是待办状态
                                if(isInformNode){//是知会节点
                                    isCanPass= false;
                                }else{
                                    isCanPass= false;
                                }
                            }else if(isDone){//如果是已办状态
                                isCanPass= false;
                            }else{//即没有产生待办，也没有产生已办，递归继续
                                isCanPass= isCanPassWithNonInformNode(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                            }
                        }
                    }
                    if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){//join->非知会节点
                        isCanPass= isCanPassWithNonInformNode(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                    }
                    if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){//split->中间穿过一些知会节点->非知会节点
                        isCanPass= isCanPassWithNonInformNode(fromNode, current_node, allNotSelectNodeList, allSelectNodeList, informNodeList,allInformNodeList,context);
                    }
                }else if("false".equals(isDelete) && current_node.getId().equals(nodeId) ){//是当前处理节点
                    isCanPass= false;
                }
            }
        }
        return isCanPass;
    }
    
    /**
     * 判断指定分支currentLinkId在processXml流程定义文件中，指定分支之前是否有可以自动跳过的节点(穿过知会)
     * @param currentTransaction
     * @return
     */
    public static String[] hasAutoSkipNodeBeforeSetCondition(BPMTransition currentTransaction) {
        String[] result= new String[2];
        if(null!=currentTransaction){
            BPMAbstractNode fromNode= currentTransaction.getFrom();
            NodeType nodeType= fromNode.getNodeType();//获得当前节点的节点类型
            if( nodeType.equals(NodeType.split) ){//split节点
                BPMTransition upOfSplit= (BPMTransition)fromNode.getUpTransitions().get(0);
                return hasAutoSkipNodeBeforeSetCondition(upOfSplit);
            }else if( nodeType.equals(NodeType.join) ){//join节点
                Map<String,Object> resultMap= hasAutoSkipNodeBeforeSetConditionForJoin(fromNode,0);
                result[0]= resultMap.get("resultBoolean").toString();
                result[1]= resultMap.get("autoSkipNodeName").toString();
                return result;
            }else if( nodeType.equals(NodeType.humen) ){//humen节点
                String fromNodePolicy = fromNode.getSeeyonPolicy().getId();
                BPMHumenActivity hactivity= (BPMHumenActivity)fromNode;
                if( "inform".equals(fromNodePolicy) || "zhihui".equals(fromNodePolicy) ){//知会节点
                    BPMTransition upOfSplit= (BPMTransition)fromNode.getUpTransitions().get(0);
                    return hasAutoSkipNodeBeforeSetCondition(upOfSplit);
                }else{//非知会节点
                    String dealType= fromNode.getSeeyonPolicy().getDealTermType();
                    String dealTerm= fromNode.getSeeyonPolicy().getdealTerm();//是否选择处理期限
                    if( null!=dealType && "2".equals(dealType.trim()) 
                            && null!=dealTerm && !"".equals(dealTerm) && !"0".equals(dealTerm) ){//自动跳过节点
                        result[0]= String.valueOf(true);
                        result[1]= fromNode.getName();
                        return result;
                    }else{//遇到非知会节点，但没有设置为自动跳过，则可以设置分支条件。
                        result[0]= String.valueOf(false);
                        result[1]= fromNode.getName();
                        return result;
                    }
                }
            }else if( nodeType.equals(NodeType.start) ){//start节点
                result[0]= String.valueOf(false);
                result[1]= fromNode.getName();
                return result;
            }
        }
        return result;
    }
    
    /**
     * 针对join节点做特殊处理
     * @param fromNode
     * @return
     */
    private static Map<String,Object> hasAutoSkipNodeBeforeSetConditionForJoin(
            BPMAbstractNode fromNode,int level) {
        Map<String,Object> resultMap= new HashMap<String, Object>();
        List<BPMTransition> upsOfJoins= fromNode.getUpTransitions();
        boolean result= false;
        boolean hasNonInformNode= false;
        for (BPMTransition bpmTransition : upsOfJoins) {
            if(!result){
                BPMAbstractNode preFromNode= bpmTransition.getFrom();
                NodeType nodeType= preFromNode.getNodeType();//获得当前节点的节点类型
                if( nodeType.equals(NodeType.join) ){//join节点
                    int newLevel= level+1;
                    resultMap= hasAutoSkipNodeBeforeSetConditionForJoin(preFromNode,newLevel);
                }else if( nodeType.equals(NodeType.humen) ){//humen节点
                    String fromNodePolicy = preFromNode.getSeeyonPolicy().getId();
                    BPMHumenActivity hactivity= (BPMHumenActivity)preFromNode;
                    if( "inform".equals(fromNodePolicy) || "zhihui".equals(fromNodePolicy) ){//知会节点
                        BPMTransition upOfInfo= (BPMTransition)preFromNode.getUpTransitions().get(0);
                        resultMap= hasAutoSkipNodeBeforeSetConditionOfInfo(upOfInfo,level);
                    }else{//非知会节点
                        String dealType= preFromNode.getSeeyonPolicy().getDealTermType();
                        String dealTerm= preFromNode.getSeeyonPolicy().getdealTerm();
                        if( null!=dealType && "2".equals(dealType.trim()) 
                                && null!=dealTerm && !"".equals(dealTerm) && !"0".equals(dealTerm) ){//自动跳过节点
                            resultMap.put("resultBoolean", true);
                            resultMap.put("splitNode", null);
                            resultMap.put("autoSkipNodeName", preFromNode.getName());
                        }else{
                            resultMap.put("resultBoolean", false);
                            resultMap.put("splitNode", null);
                            resultMap.put("autoSkipNodeName", preFromNode.getName());
                        }
                        hasNonInformNode= true;
                    }
                }
                result= (Boolean)resultMap.get("resultBoolean");
            }
        }
        if(!result && !hasNonInformNode){//都为知会节点，则继续往前查找
            Object splitObj= resultMap.get("splitNode");
            if( null!= splitObj ){
                BPMAbstractNode splitNode= (BPMAbstractNode)splitObj;
                BPMTransition preTransition= (BPMTransition)splitNode.getUpTransitions().get(0);
                if(level==0){//最外层
                    String[] returnResult= hasAutoSkipNodeBeforeSetCondition(preTransition);
                    resultMap.put("resultBoolean", returnResult[0]);
                    resultMap.put("autoSkipNodeName", returnResult[1]);
                }else{//类似知会
                    resultMap= hasAutoSkipNodeBeforeSetConditionOfInfo(preTransition,level);
                }
            }
        }
        return resultMap;
    }
    
    /**
     * 针对知会节点处理
     * @param currentTransaction
     * @return
     */
    private static Map<String,Object> hasAutoSkipNodeBeforeSetConditionOfInfo(BPMTransition currentTransaction,int level) {
        Map<String,Object> resultMap= new HashMap<String, Object>();
        resultMap.put("resultBoolean", false);
        resultMap.put("splitNode", null);
        resultMap.put("autoSkipNodeName", "");
        if(null!=currentTransaction){
            BPMAbstractNode fromNode= currentTransaction.getFrom();
            NodeType nodeType= fromNode.getNodeType();//获得当前节点的节点类型
            if( nodeType.equals(NodeType.split) ){//split节点
                resultMap.put("resultBoolean", false);
                resultMap.put("splitNode", fromNode);
                resultMap.put("autoSkipNodeName", fromNode.getName());
                return resultMap;
            }else if( nodeType.equals(NodeType.join) ){//join节点
                int newLevel= level+1;
                return hasAutoSkipNodeBeforeSetConditionForJoin(fromNode,newLevel);
            }else if( nodeType.equals(NodeType.humen) ){//humen节点
                String fromNodePolicy = fromNode.getSeeyonPolicy().getId();
                BPMHumenActivity hactivity= (BPMHumenActivity)fromNode;
                if( "inform".equals(fromNodePolicy) || "zhihui".equals(fromNodePolicy) ){//知会节点
                    BPMTransition upOfSplit= (BPMTransition)fromNode.getUpTransitions().get(0);
                    return hasAutoSkipNodeBeforeSetConditionOfInfo(upOfSplit,level);
                }else{//非知会节点
                    String dealType= fromNode.getSeeyonPolicy().getDealTermType();
                    String dealTerm= fromNode.getSeeyonPolicy().getdealTerm();
                    if( null!=dealType && "2".equals(dealType.trim()) 
                            && null!=dealTerm && !"".equals(dealTerm) && !"0".equals(dealTerm) ){//自动跳过节点
                        resultMap.put("resultBoolean", true);
                        resultMap.put("splitNode", null);
                        resultMap.put("autoSkipNodeName", fromNode.getName());
                        return resultMap;
                    }else{//遇到非知会节点，但没有设置为自动跳过，则可以设置分支条件。
                        resultMap.put("resultBoolean", false);
                        resultMap.put("splitNode", null);
                        resultMap.put("autoSkipNodeName", fromNode.getName());
                        return resultMap;
                    }
                }
            }else if( nodeType.equals(NodeType.start) ){//start节点
                resultMap.put("resultBoolean", false);
                resultMap.put("splitNode", null);
                resultMap.put("autoSkipNodeName", fromNode.getName());
                return resultMap;
            }
        }
        return resultMap;
    }

    public static String doMatchAll(CPMatchResultVO cpMatchResult, Set<String> myCurrentSelectNodes,
            WorkflowBpmContext context,boolean isClearAddition,BPMProcess process,BPMStatus startNode,
            Map<String, ConditionMatchResultVO> condtionResultAll,Map<String,Boolean> autoSkipNodeMap) throws BPMException {
    	String matchMsg= "";
        Map<String, ConditionMatchResultVO> condtionResult = new LinkedHashMap<String, ConditionMatchResultVO>();
        Map<String, ConditionMatchResultVO> condtionResultTemp = new LinkedHashMap<String, ConditionMatchResultVO>();
        Map<String, ConditionMatchResultVO> condtionResultTempAll = new LinkedHashMap<String, ConditionMatchResultVO>();
        Set<String> currentSelectNodes = new HashSet<String>();
        Set<String> currentSelectInformNodes = new HashSet<String>();
        Map<String,Set<String>> cannotPassedNodes= new HashMap<String, Set<String>>();
        List childs= new ArrayList();
        Set<String> passedJoinNodes= new HashSet<String>();
        for (String nodeId : myCurrentSelectNodes) {
            BPMAbstractNode currentActivity = null;
            if("start".equals(nodeId)){
                currentActivity= startNode;
            }else{
                currentActivity = (BPMHumenActivity) process.getActivityById(nodeId);
            }
            BranchArgs.doMatch(passedJoinNodes,cpMatchResult, currentActivity, context, isClearAddition,condtionResult,
                    condtionResultTemp,condtionResultTempAll,currentSelectNodes,currentSelectInformNodes,childs,cannotPassedNodes,autoSkipNodeMap);
            condtionResultAll.putAll(condtionResultTempAll);
        }
        matchMsg= "";
        if (currentSelectNodes.size() <= 0 && childs.size()>0) {
            if(currentSelectInformNodes.size()>0 && condtionResult.size()<=0){
                condtionResultTempAll.clear();
                condtionResultTemp.clear();
                matchMsg= BranchArgs.doMatchAll(cpMatchResult, currentSelectInformNodes, context, isClearAddition, process,null,condtionResultAll,autoSkipNodeMap);
            }else{
                cpMatchResult.setPop(true);
                if(condtionResult.size()<=0 && condtionResultTemp.size()>0){
                    cpMatchResult.setCondtionMatchMap(condtionResultTemp);
                    matchMsg= "后面的所有分支条件都不满足。";
                }else if(cpMatchResult.isNeedSelectBranch() && condtionResultTempAll.size()>0){
                    cpMatchResult.setCondtionMatchMap(condtionResultTempAll);
                    matchMsg= "后面有分支条件需要手动选择。";
                }
                if(currentSelectInformNodes.size()>0){
                    Map<String,String> hasPassedNodes= new HashMap<String,String>();
                    checkCurrentSelectInformNodes(currentSelectInformNodes,process,cpMatchResult.getCondtionMatchMap(),hasPassedNodes);
                    cpMatchResult.setCurrentSelectInformNodes(currentSelectInformNodes);
                }
            }
        }else{
        	if(cpMatchResult.isNeedSelectBranch()){
        		matchMsg= "需要选择分支条件";
        	}
        }
        if(!cpMatchResult.isPop()){
        	matchMsg= "后面的节点可以被流转到达，并能激活产生待办，不需要选人或选分支。";
        }else{
        	if(Strings.isBlank(matchMsg)){
        		matchMsg= "需要为后面的节点选择人员或分支条件";
        	}
        }
        if(cpMatchResult.isNeedSelectBranch() && condtionResultTempAll.size()>0){
            //if(!cpMatchResult.getAllNotSelectNodes().isEmpty()){
                removeParentConditionResult(process,cpMatchResult,condtionResultTempAll,myCurrentSelectNodes);
            //}
            cpMatchResult.setCondtionMatchMap(condtionResultTempAll);
        }
        return matchMsg;
    }

    /**
     * 删除已被穿过的节点前分支条件
     * @param process
     * @param cpMatchResult
     * @param condtionResultTempAll
     * @param myCurrentSelectNodes
     */
    private static void removeParentConditionResult(BPMProcess process,CPMatchResultVO cpMatchResult,
            Map<String, ConditionMatchResultVO> condtionResultTempAll,Set<String> myCurrentSelectNodes) {
        Set<String> allBranchNodes= condtionResultTempAll.keySet();
        if(!allBranchNodes.isEmpty()){
        	Set<String> passedNodeSet= new HashSet<String>();
            Set<String> allBranchNodesTemp= new HashSet<String>();
            for (String nodeId : allBranchNodes) {
                allBranchNodesTemp.add(nodeId);
            }
            for (String nodeId : allBranchNodesTemp) {
                BPMAbstractNode activity= process.getActivityById(nodeId);
                BPMTransition up= (BPMTransition)activity.getUpTransitions().get(0);
                BPMAbstractNode fromNode= up.getFrom();
                checkBack(passedNodeSet,fromNode,cpMatchResult,condtionResultTempAll,myCurrentSelectNodes);
            }
        }
    }

    /**
     * 向start节点方向递归
     * @param fromNode
     * @param cpMatchResult
     * @param condtionResultTempAll
     * @param myCurrentSelectNodes
     */
    private static void checkBack(Set<String> passedNodeSet,BPMAbstractNode fromNode, CPMatchResultVO cpMatchResult,
            Map<String, ConditionMatchResultVO> condtionResultTempAll, Set<String> myCurrentSelectNodes) {
        String fromNodeId= fromNode.getId();
        if(passedNodeSet.contains(fromNodeId)){
        	return;
        }
        passedNodeSet.add(fromNodeId);
        if(myCurrentSelectNodes.contains(fromNodeId) || "start".equals(fromNodeId)){//到了起始节点，则不再像开始节点方向查找
            return;
        }
        if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){//人工节点，则删除
            if(condtionResultTempAll.containsKey(fromNodeId)){
                condtionResultTempAll.remove(fromNodeId);
            }
            if(cpMatchResult.getAllNotSelectNodes().contains(fromNodeId)){//是前面没选中的节点，则删除
                condtionResultTempAll.remove(fromNodeId);
            }
            if(cpMatchResult.getAllSelectNodes().contains(fromNodeId)){
                condtionResultTempAll.remove(fromNodeId);
            }
        }
        List<BPMTransition> ups= fromNode.getUpTransitions();
        for (BPMTransition up : ups) {
            BPMAbstractNode fromNode1= up.getFrom();
            checkBack(passedNodeSet,fromNode1, cpMatchResult, condtionResultTempAll, myCurrentSelectNodes);
        }
    }

    private static Set<String> checkCurrentSelectInformNodes(Set<String> currentSelectInformNodes, BPMProcess process,
            Map<String, ConditionMatchResultVO> conditionMap,Map<String,String> hasPassedNodes) {
        Set<String> nodes= conditionMap.keySet();
        for (String nodeId : nodes) {
            BPMAbstractNode informActivity= process.getActivityById(nodeId);
            findForward(informActivity, hasPassedNodes, currentSelectInformNodes);
        }
        return currentSelectInformNodes;
    }

    private static void findForward(BPMAbstractNode informActivity,Map<String,String> hasPassedNodes,Set<String> currentSelectInformNodes) {
        if(currentSelectInformNodes.size()<=0){
            return;
        }
        List<BPMTransition> ups= informActivity.getUpTransitions();
        for (BPMTransition bpmTransition : ups) {
            BPMAbstractNode fromNode= bpmTransition.getFrom();
            if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.humen)){//人工节点
                if(hasPassedNodes.containsKey(fromNode.getId())){
                    continue;
                }else{
                    hasPassedNodes.put(fromNode.getId(), fromNode.getId());
                }
                if(currentSelectInformNodes.contains(fromNode.getId())){
                    currentSelectInformNodes.remove(fromNode.getId());
                }else{
                    findForward(fromNode,hasPassedNodes,currentSelectInformNodes);
                }
            }else if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.join)){//join
                findForward(fromNode,hasPassedNodes,currentSelectInformNodes);
            }else if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.split)){//split
                findForward(fromNode,hasPassedNodes,currentSelectInformNodes);
            }else if(fromNode.getNodeType().equals(BPMAbstractNode.NodeType.start)){//start
                return;
            }
        }
    }
    
    
}