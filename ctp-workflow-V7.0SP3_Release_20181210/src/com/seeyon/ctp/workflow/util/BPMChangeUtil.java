/**
 * $Author: wangchw $
 * $Rev: 40343 $
 * $Date:: 2014-09-11 14:23:31#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.workflow.util;

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
import org.json.JSONObject;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.engine.enums.ConditionType;
import com.seeyon.ctp.workflow.engine.enums.FlowType;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.ProcessOrgManager;
import com.seeyon.ctp.workflow.vo.BPMAssignNodeMessageVO;
import com.seeyon.ctp.workflow.vo.BPMChangeMergeVO;
import com.seeyon.ctp.workflow.vo.BPMChangeMessageVO;
import com.seeyon.ctp.workflow.vo.BPMNewLinkVO;
import com.seeyon.ctp.workflow.vo.BPMNewNodeVO;
import com.seeyon.ctp.workflow.vo.User;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMAndRouter;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMEnd;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMParticipantType;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.definition.BPMTransition;
import net.joinwork.bpm.definition.ObjectName;
import net.joinwork.bpm.definition.ReadyObject;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.execute.ReadyNode;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkItemManager;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * <p>Title: T4工作流流程修改工具类</p>
 * <p>Description: 工作流-流程修改信息类（主要用于加签、减签、会签、知会等）</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class BPMChangeUtil {

	public final static Log logger = CtpLogFactory.getLog(BPMChangeUtil.class);
	private static OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	
    public static void createFromMap(BPMChangeMessageVO vo) throws BPMException {
        try {
            Map<Object, Object> map = vo.getMessage();
            String userId = vo.getUserId();
            //获取到当前节点的activity，这个activity是当前节点的id
            BPMAbstractNode targetActivity = vo.getTargetActivity();
            //将要添加到工作流中的节点
            List<BPMHumenActivity> nodeList = new ArrayList<BPMHumenActivity>(0);
            if (targetActivity == null) {
                return;
            }
            //表单应用名
            String formApp = "";
            String formViewOperation = "";
            //数据关联ID
            String DR = "";
            //表单只读标记，1只读,""默认
            String FR = "";
            //工作流activity的权限
            BPMSeeyonPolicy currentPolicy = targetActivity.getSeeyonPolicy(); 
            if (currentPolicy != null) {
                formApp = currentPolicy.getFormApp();
                formViewOperation = currentPolicy.getFormViewOperation();
                DR = currentPolicy.getDR();
                FR = currentPolicy.getFR();
            }
            //用户类型
            String[] types = getArray("userType", map);
            //用户id
            String[] ids = getArray("userId", map);
            //用户部门
            String[] userExcludeChildDepartment = getArray("userExcludeChildDepartment", map);
            //用户名
            String[] names = getArray("userName", map);
            //单位id
            String[] accountIds = getArray("accountId", map);
            //单位简称，在A8的FlowData中接收的这个参数，放入Party的accountShortname属性，但是后面并没有用到
            //String[] accountShortNames = getArray("accountShortname", map);
            //节点权限id
            String[] policyId = getArray("policyId", map);
            //节点权限名
            String[] policyName = getArray("policyName", map);
            //加签类型（串行、并行、还是与下一节点并行）
            int flowType = getInt("flowType", map);
            //是加签还是知会、会签
            int changeType = vo.getChangeType();
            //节点处理模式表示是单人执行还是多人执行
            String[] node_process_mode = getStringArray("node_process_mode", map);
            //表单类型
            String fromType = String.valueOf(changeType);
            //提前提醒时间
            String remindTime = getString("remindTime", map);
            //处理期限
            String dealTerm = getString("dealTerm", map);
            
            String backToMe= getString("backToMe", map);
            if (ChangeType.AddInform.getKey() == changeType) {//知会
                flowType = FlowType.Parallel.getKey();
                policyId = new String[] { BPMSeeyonPolicy.SEEYON_POLICY_INFORM.getId() };
                String zhihui = ResourceUtil.getString("node.policy.zhihui");
                policyName = new String[] {zhihui};
            }else if (ChangeType.PassRead.getKey() == changeType){//传阅只存在并发
                flowType = FlowType.Parallel.getKey();
                policyId = new String[] { BPMSeeyonPolicy.EDOC_POLICY_YUEDU.getId() };
                
                String yuedu = ResourceUtil.getString("node.policy.yuedu");
                policyName = new String[] { yuedu};
            }else if(ChangeType.MultistageAsign.getKey() == changeType){//多级会签
                //fromType= getString("fromType", map);
            	backToMe= "2";
            }
            if("1".equals(backToMe) || "2".equals(backToMe)){
            	String currentUserId= getString("currentUserId", map);
                String performer= getString("performer", map);
                String currentUserName= getString("currentUserName", map);
                String workitemId= getString("workitemId", map);
                WorkItemManager   workItemManager= (WorkItemManager)AppContext.getBean("workItemManager");
                WorkItem workitem = workItemManager.getWorkItemOrHistory(Long.parseLong(workitemId));
                //多级会签应该把原节点加进去，而不是把当前登录人员加进去，因为有可能是代理人在处理
                String affairMemberId= workitem.getPerformer();
                if(!affairMemberId.equals(currentUserId)){
                    ProcessOrgManager   processOrgManager= (ProcessOrgManager)AppContext.getBean("processOrgManager");
                    User myUser= processOrgManager.getUserById(affairMemberId, false);
                    if(null!=myUser){
                        currentUserId= affairMemberId;
                    }else{
                        myUser= processOrgManager.getUserById(currentUserId, false);
                    }
                    currentUserName= myUser.getName();
                }
                BPMActor currentBPMActor= (BPMActor)targetActivity.getActorList().get(0);
                String currentLoginAccountId= getString("currentLoginAccountId", map);
                BPMSeeyonPolicy seeyonPolicy = new BPMSeeyonPolicy(currentPolicy.getId(), currentPolicy.getName());
                String currentNodeType= "user";
                BPMHumenActivity userNode = new BPMHumenActivity(UUIDLong.longUUID() + "", currentUserName);
                userNode.setFromType(fromType);
                BPMParticipantType type = new BPMParticipantType(currentNodeType);
                BPMActor userActor = new BPMActor(currentUserId, currentUserName, type, "roleadmin",
                        BPMActor.CONDITION_OR, currentBPMActor.isIncludeChild(), currentLoginAccountId);
                userActor.getParty().setIncludeChild(currentBPMActor.getParty().isIncludeChild());
                userNode.addActor(userActor);
                seeyonPolicy.setFormApp(formApp);
                seeyonPolicy.setFormViewOperation(formViewOperation);
                
                seeyonPolicy.setProcessMode("single");
                seeyonPolicy.setAdded(true);
                seeyonPolicy.setAddedFromId(userId);
                seeyonPolicy.setFR(FR);
                if("2".equals(backToMe)){
                	seeyonPolicy.setdealTerm(dealTerm);
                	seeyonPolicy.setRemindTime(remindTime);
                }else if("1".equals(backToMe)){
                	seeyonPolicy.setdealTerm("0");
                	seeyonPolicy.setRemindTime("0"); 
                }
                seeyonPolicy.setDR(DR);
                userNode.setSeeyonPolicy(seeyonPolicy);
                List<BPMHumenActivity> list= new ArrayList<BPMHumenActivity>();
                list.add(userNode);
                vo.setMultiStageAsignCurrentUserNodeList(list);
            }
            //form操作是否是只读
            boolean isFormOperationReadonly = "1".equals(getString("formOperationPolicy", map));
            if(ChangeType.Assign.getKey() == changeType){//当前会签
                isFormOperationReadonly= false;
            }
            //是否显示名称简写
            String isShowShortName = getString("isShowShortName", map);
            vo.setIsShowShortName(isShowShortName);
            //获取当前流程实例Id
            long caseId = getLong("caseId", map);
            long summaryId = getLong("summaryId", map);
            long affairId= getLong("affairId", map);
            vo.setCaseId(caseId);
            vo.setSummaryId(summaryId);
            vo.setAffairId(affairId);
            nodeList = createActivityList(userId, ids, types, names, accountIds, policyId, policyName,
                    node_process_mode, dealTerm, remindTime, isFormOperationReadonly, fromType,
                    userExcludeChildDepartment, formApp, formViewOperation,DR);
            vo.setAddedActivityList(nodeList);
            vo.setFlowType(flowType);
        } catch (Exception e) {
            //throw new BPMException("构造将要被加签的节点时出现异常！流程id是：" + vo.getProcessId(), e);
            throw new BPMException("An exception occurs when constructing the nodes to be signed! the processId is " + vo.getProcessId(), e);
        }
    }

    public static BPMChangeMessageVO addNode(BPMChangeMessageVO vo,BPMCase theCase) throws BPMException{
        try{
            BPMProcess process = vo.getProcess();
            List<BPMHumenActivity> nodeList = vo.getAddedActivityList();
            if(process==null){
                return vo;
            }
            if(Strings.isEmpty(nodeList)){
                return vo;
            }
            //获取到当前节点的activity，这个activity是当前节点的id
            BPMAbstractNode targetActivity = vo.getTargetActivity();
            if(targetActivity==null){
                return vo;
            }
            //是否显示单位简写
            if("false".equals(process.getIsShowShortName())){
                process.setIsShowShortName(vo.getIsShowShortName());
            }
            //设置修改者名称
            process.setModifyUser(vo.getUserId());
            //设置修改时间
            Date now = new Date(System.currentTimeMillis());
            process.setUpdateDate(now);
            //是加签还是知会、会签
            int changeType = vo.getChangeType();
            //是并行加还是串行、与下一节点并行加
            int flowType = vo.getFlowType();
            String operationType= "insertPeople";
            Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
            if(ChangeType.AddNode.getKey()==changeType){//加签
                operationType= "insertPeople";
                List<BPMHumenActivity> myNodeList = vo.getMultiStageAsignCurrentUserNodeList();
                if(flowType == FlowType.Serial.getKey()){
                    //如果是串行加签,逻辑很容易理解
                	if(null!=myNodeList && !myNodeList.isEmpty()){
                    	serialAddNode(vo, process, targetActivity, myNodeList);
                    }
                    resultMap = serialAddNode(vo, process, targetActivity, nodeList);
                } else if(flowType == FlowType.Parallel.getKey()){
                	if(null!=myNodeList && !myNodeList.isEmpty()){
                    	serialAddNode(vo, process, targetActivity, myNodeList);
                    }
                    //加签的节点数多余一个时，是并行加签；如果只有一个，按照串行加签的逻辑添加节点即可。
                    if(nodeList!=null && nodeList.size()>1){
                        resultMap = parallelAddNode(vo, process, targetActivity, nodeList);
                    } else {
                        resultMap = serialAddNode(vo, process, targetActivity, nodeList);
                    }
                } else if(flowType == FlowType.NextParallel.getKey()){
                    //与下一节点并行加签
                    resultMap = nextParallelAddNode(vo, process, targetActivity, nodeList);
                }
            } else if(ChangeType.AddInform.getKey()==changeType){//知会
                operationType= "addInform";
                if(nodeList.size()>=2){
                    //知会的节点多余一个时，是并行加签
                    resultMap = parallelAddNode(vo, process, targetActivity, nodeList);
                } else if(nodeList.size()==1) {
                    //知会的节点只有一个时，是串行加签
                    resultMap = serialAddNode(vo, process, targetActivity, nodeList);
                }
            } else if(ChangeType.Assign.getKey()==changeType){//当前会签
                operationType= "colAssign";
                resultMap = assignNode(vo, true,theCase);
            } else if(ChangeType.MultistageAsign.getKey()==changeType){//多级会签
                operationType= "addMoreSign";
                List<BPMHumenActivity> myNodeList = vo.getMultiStageAsignCurrentUserNodeList();
                serialAddNode(vo, process, targetActivity, myNodeList);
                if(nodeList!=null && nodeList.size()>1){
                    resultMap = parallelAddNode(vo, process, targetActivity, nodeList);
                } else {
                    resultMap = serialAddNode(vo, process, targetActivity, nodeList);
                }
            } else if(ChangeType.PassRead.getKey()==changeType){//传阅
                operationType= "addPassRead";
                if(nodeList.size()>=2){
                    resultMap = parallelAddNode(vo, process, targetActivity, nodeList);
                } else if(nodeList.size()==1) {
                    resultMap = serialAddNode(vo, process, targetActivity, nodeList);
                }
            }
            if(resultMap.size()>0){
                Map<String, Object> messageData = new HashMap<String, Object>();
                messageData.put("operationType",operationType);
                messageData.put("handlerId",vo.getUserId());
                messageData.put("summaryId",vo.getSummaryId());
                messageData.put("affairId",vo.getAffairId());
                List<String> memAndPolicyName = resultMap.get("memAndPolicyName");
                List<String> partyNames = resultMap.get("partyNames");
                messageData.put("processLogParam",StringUtils.join(memAndPolicyName.iterator(), ","));
                messageData.put("partyNames",StringUtils.join(partyNames.iterator(), ","));
                messageData.put("formOperationPolicy",getString("formOperationPolicy", vo.getMessage()));
                vo.getMessageDataList().add(messageData);
            }
            return vo;
        } catch(Exception e){
            //throw new BPMException("修改流程时出现异常！流程id是："+vo.getProcessId(),e);
            throw new BPMException("There was an exception when editing the process!, the processId is"+vo.getProcessId(),e);
        }
    }

    public static BPMChangeMessageVO deleteNode(BPMChangeMessageVO vo) throws BPMException {
        //分离预减签和减签 预减签只需要取得所有的直接后续节点即可，可调用findDirectHumenChildrenCascade
        BPMProcess process = vo.getProcess();
        List<String> deleteActivityIdList = vo.getDeleteAcitivityIdList();
        try {
            for (String activityId : deleteActivityIdList) {
                if (activityId != null && !"".equals(activityId)) {
                    BPMActivity activity = process.getActivityById(activityId);
                    //不是人工节点不允许减签
                    if (activity instanceof BPMHumenActivity) {
                        //人中节点只可能有一个父节点，也只可能有一个子节点
                        BPMTransition aup = (BPMTransition) activity.getUpTransitions().get(0);
                        BPMTransition adown = (BPMTransition) activity.getDownTransitions().get(0);
                        BPMAbstractNode parent = aup.getFrom();
                        BPMAbstractNode child = adown.getTo();
                        //如果父节点是split且子节点是join
                        if ((parent instanceof BPMAndRouter) && (child instanceof BPMAndRouter)) {
                            BPMAndRouter parent1 = (BPMAndRouter) parent;
                            BPMAndRouter child1 = (BPMAndRouter) child;
                            if ((parent1.isStartAnd() == true) && (child1.isStartAnd() == false)) {
                                if (parent.getDownTransitions().size() >= 3) {
                                    //如果split存在3个或以上的子节点，删除当前节点和up线、down线就可以
                                    deleteNode(vo, process, activity, false);
                                } else {
                                    //如果split存在两个或以下的子节点，删除当前节点和up线、down线
                                    //同时要删除split节点和join节点，及split节点的up线，join节点的down线
                                    //同时建立split的父节点、join节点的子节点与当前节点的并行节点的关系
                                    //也即先减掉当前节点然后不建立父子节点关联，然后减掉split和join节点建立父子节点关联
                                    deleteNode(vo, process, activity, false);
                                    deleteNode(vo, process, parent, true);
                                    deleteNode(vo, process, child, true);
                                }
                            } else {
                                deleteNode(vo, process, activity, true);
                            }
                        } else {
                            deleteNode(vo, process, activity, true);
                        }
                    }
                }
            }
            
            //清除复制粘贴标记
            List list = process.getActivitiesList();
            for(Object node : list){
            	BPMAbstractNode _node = (BPMAbstractNode)node;
            	if(deleteActivityIdList.contains(_node.getCopyFrom()) || deleteActivityIdList.contains(_node.getPasteTo())){
            		_node.setCopyFrom("");
            		_node.setPasteTo("");
            		_node.setCopyNumber("");
            	}
            }
            
        } catch (Exception e) {
        	logger.error("减签操作异常",e);
            throw new BPMException("Delete node error", e);
        }
        return vo;
    }

    //该方法只针对串行节点的减签，减掉当前节点后，createPCLFlag=true的话就建立其父节点和子节点间的关联
    private static void deleteNode(BPMChangeMessageVO vo, BPMProcess process, BPMAbstractNode activity, boolean createPCLFlag) {
        BPMTransition aup = (BPMTransition) activity.getUpTransitions().get(0);
        BPMTransition adown = (BPMTransition) activity.getDownTransitions().get(0);
        
        List<BPMCircleTransition> circleUps = activity.getUpCircleTransitions();
        List<BPMCircleTransition> circleDowns = activity.getDownCirlcleTransitions();
        
        if (createPCLFlag) {
            //并建立父节点与子节点的关系
            BPMAbstractNode parent = aup.getFrom();
            BPMAbstractNode child = adown.getTo();
            BPMTransition parentChild = new BPMTransition(parent, child);
            //复制分支条件
            copyCondition(adown, parentChild);
            process.addLink(parentChild);
            vo.addAddedLink(parentChild);
        }
        //直接删除节点，并且删除up线和down线
        adown.getTo().removeUpTransition(adown);
        activity.removeDownTransition(adown);
        process.removeLink(adown);
        
        aup.getFrom().removeDownTransition(aup);
        activity.removeUpTransition(aup);
        process.removeLink(aup);
        
        if(Strings.isNotEmpty(circleUps)){
        	for(Iterator<BPMCircleTransition> it = circleUps.iterator();it.hasNext();){
        		BPMCircleTransition up = it.next();
        		process.removeClink(up);
        	}
        }
        if(Strings.isNotEmpty(circleDowns)){
        	for(Iterator<BPMCircleTransition> it = circleDowns.iterator();it.hasNext();){
        		BPMCircleTransition down = it.next();
        		process.removeClink(down);
        	}
        }
        
        process.removeChild(activity);
        
        vo.addDeleteLink(aup);
        vo.addDeleteLink(adown);
        vo.addDeleteNode(activity);
    }

    public static int getInt(String key, Map<Object, Object> map) {
        int result = 0;
        String results = getString(key, map);
        try {
            result = Integer.parseInt(results);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    public static long getLong(String key, Map<Object, Object> map) {
        long result = 0;
        String results = getString(key, map);
        try {
            result = Long.parseLong(results);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    public static String getString(String key, Map<Object, Object> map) {
        String result = null;
        if (map == null) {
            return result;
        }
        Object object = map.get(key);
        if (object != null) {
            if (object instanceof String) {
                result = (String) object;
            } else {
                result = object.toString();
            }
        }
        return result;
    }

    public static String[] getStringArray(String key, Map<Object, Object> map) {
        String[] result = null;
        if (map == null) {
            return result;
        }
        Object object = map.get(key);
        if (object != null) {
            if (object instanceof String) {
                result = new String[1];
                result[0] = (String) object;
            } else if (object instanceof String[]) {
                result = (String[]) object;
            } else if (object instanceof List) {
                @SuppressWarnings("rawtypes")
                List list = (List) object;
                if (Strings.isNotEmpty(list)) {
                    result = new String[list.size()];
                    for (int i = 0, len = list.size(); i < len; i++) {
                        result[i] = String.valueOf(list.get(i));
                    }
                } else {
                    result = new String[0];
                }
            } else {
                result = new String[1];
                result[0] = object.toString();
            }
        }
        return result;
    }

    /**
     * 串行加签
     * @param vo
     * @param process
     * @param targetActivity
     * @param nodeList
     * @return
     * @throws BPMException
     */
    private static Map<String,List<String>> serialAddNode(BPMChangeMessageVO vo, BPMProcess process, BPMAbstractNode targetActivity,
            List<BPMHumenActivity> nodeList) throws BPMException {
        try {
            List<String> memAndPolicyName = new ArrayList<String>();
            List<String> partyNames = new ArrayList<String>();
            //链式添加父子关系，第0个元素的父节点是当前节点，第n个元素的父节点是第n-1个节点。
            BPMAbstractNode previousNode = targetActivity;
            for (int i = 0; i < nodeList.size(); i++) {
                if (i > 0) {
                    previousNode = nodeList.get(i - 1);
                }
                //创建连接（把当前节点和加签节点的关系添加上去
                BPMActivity userNode = nodeList.get(i);
                BPMTransition userLink = new BPMTransition(previousNode, userNode);
                process.addChild(userNode);
                process.addLink(userLink);
                memAndPolicyName.add(userNode.getName() + "(" + userNode.getSeeyonPolicy().getName() + ")");
                partyNames.add(userNode.getName());
                vo.addAddedNode(userNode);
                vo.addAddedLink(userLink);
            }
            //得到最终节点
            BPMAbstractNode finalNode = nodeList.get(nodeList.size() - 1);
            //把下一节点的父节点设置为List的最后一个元素
            @SuppressWarnings("rawtypes")
            List downTransitions = targetActivity.getDownTransitions();
            if (downTransitions != null) {
                for (int i = 0; i < downTransitions.size(); i++) {
                    BPMTransition oldLink = (BPMTransition) downTransitions.get(i);
                    BPMAbstractNode to = oldLink.getTo();
                    BPMTransition newLink = new BPMTransition(finalNode, to);
                    copyCondition(oldLink, newLink);
                    process.addLink(newLink);
                    process.removeLink(oldLink);
                    targetActivity.removeDownTransition(oldLink);
                    oldLink.getTo().removeUpTransition(oldLink);
                    vo.addAddedLink(newLink);
                    vo.addDeleteLink(oldLink);
                }
            }
            Map<String,List<String>> returnMap= new HashMap<String, List<String>>();
            returnMap.put("memAndPolicyName", memAndPolicyName);
            returnMap.put("partyNames", partyNames);
            return returnMap;
        } catch (Throwable e) {
            throw new BPMException(e.getMessage(), e);
        }
    }

    /**
     * 并行加签
     * @param vo
     * @param process
     * @param targetActivity
     * @param nodeList
     * @return
     * @throws BPMException
     */
    private static Map<String,List<String>> parallelAddNode(BPMChangeMessageVO vo, BPMProcess process, BPMAbstractNode targetActivity,
            List<BPMHumenActivity> nodeList) throws BPMException{
        try{
            List<String> memAndPolicyName = new ArrayList<String>();
            List<String> partyNames = new ArrayList<String>();
            //并行加签的话，新建一个split、join节点，其他节点的父节点都是split，子节点都是join。
            @SuppressWarnings("rawtypes")
            List downTransitions = targetActivity.getDownTransitions();
            //创建split节点和join节点
            BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);
            BPMAndRouter split = null;
            BPMAndRouter join = null;
            String splitId = UUIDLong.longUUID() + "";
            String joinId = UUIDLong.longUUID() + "";
            split = new BPMAndRouter(splitId, "split");
            join = new BPMAndRouter(joinId, "join");
            split.setStartAnd(true);
            join.setStartAnd(false);
            String relevancyId = UUIDLong.longUUID() + "";
            split.setParallelismNodeId(relevancyId);
            join.setParallelismNodeId(relevancyId);

            process.addChild(split);
            process.addChild(join);
            vo.addAddedNode(split);
            vo.addAddedNode(join);

            //建立当前节点与split节点、join节点与下一节点之间的关系，并删除旧的关系
            BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
            BPMTransition trans1 = new BPMTransition(targetActivity, split);
            BPMTransition trans2 = new BPMTransition(join, nextNode);

            copyCondition(nextTrans, trans2);

            process.addLink(trans1);
            process.addLink(trans2);
            vo.addAddedLink(trans1);
            vo.addAddedLink(trans2);

            process.removeLink(nextTrans);
            targetActivity.removeDownTransition(nextTrans);
            nextTrans.getTo().removeUpTransition(nextTrans);
            vo.addDeleteLink(nextTrans);

            //建立将要被加签的节点与join、split节点之间的关系
            int ilen = nodeList.size()-1;
            for (int i = ilen; i >=0; i--) {
                BPMHumenActivity userNode = nodeList.get(i);
                BPMTransition userLink1 = new BPMTransition(split, userNode);
                BPMTransition userLink2 = new BPMTransition(userNode, join);

                process.addChild(userNode);
                process.addLink(userLink1);
                process.addLink(userLink2);
                vo.addAddedNode(userNode);
                vo.addAddedLink(userLink1);
                vo.addAddedLink(userLink2);
                memAndPolicyName.add(userNode.getName() + "(" + userNode.getSeeyonPolicy().getName() + ")");
                partyNames.add(userNode.getName());
            }
            Map<String,List<String>> returnMap= new HashMap<String, List<String>>();
            returnMap.put("memAndPolicyName", memAndPolicyName);
            returnMap.put("partyNames", partyNames);
            return returnMap;
        }catch(Throwable e){
            throw new BPMException(e.getMessage(), e);
        }
    }

    /**
     * 与下一节点并行加签
     * @param vo
     * @param process
     * @param targetActivity
     * @param nodeList
     * @return
     * @throws BPMException
     */
    private static Map<String,List<String>> nextParallelAddNode(BPMChangeMessageVO vo, BPMProcess process, BPMAbstractNode targetActivity,
            List<BPMHumenActivity> nodeList)  throws BPMException {
    	List<BPMHumenActivity> myNodeList = vo.getMultiStageAsignCurrentUserNodeList();
    	boolean isBackToMe= false;
    	if(null!=myNodeList && !myNodeList.isEmpty()){
    		isBackToMe= true;
        }
    	BPMHumenActivity meNode= null;
    	int backToMeType= 0;
        List<String> memAndPolicyName = new ArrayList<String>();
        List<String> partyNames = new ArrayList<String>();
        Map<String,List<String>> returnMap= new HashMap<String, List<String>>();
        try{
          //与下一节点并行加签的话，逻辑上比较简单，但是实际操作时如果后面的节点是split节点时，稍显复杂一些
            //总的来说就是类似下一节点的当前会签，但是又不完全相同
            @SuppressWarnings("rawtypes")
            List downTransitions = targetActivity.getDownTransitions();
            BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);
            BPMAndRouter split = null;
            BPMAndRouter join = null;
            //查看下一节点的节点类型
            BPMAbstractNode childNode = nextTrans.getTo();
            BPMAbstractNode.NodeType nodeType = childNode.getNodeType();
            if (nodeType == BPMAbstractNode.NodeType.end || nodeType == BPMAbstractNode.NodeType.join) {
                //下一节点是end节点，什么都不操作。
                vo.setSuccess(false);
                String errorMsg= ResourceUtil.getString("workflow.nextNode.is.specialNode");
                vo.setErrorMsg(errorMsg);
                return returnMap;
            }else if (nodeType == BPMAbstractNode.NodeType.split) {
                //如果下一节点是split节点的话，就不需要创建split和join节点了，寻找join节点的逻辑稍显复杂
                split = (BPMAndRouter) childNode;
                join = findJoinOfSplit(process, split);
            } else if (nodeType == BPMAbstractNode.NodeType.humen) {
                //如果下一节点是普通节点，就创建一个join、split节点对，并修改当前节点、下一节点与join、split节点之间的关系
                String splitId = UUIDLong.longUUID() + "";
                String joinId = UUIDLong.longUUID() + "";
                split = new BPMAndRouter(splitId, "split");
                join = new BPMAndRouter(joinId, "join");
                split.setStartAnd(true);
                join.setStartAnd(false);
                String relevancyId = UUIDLong.longUUID() + "";
                split.setParallelismNodeId(relevancyId);
                join.setParallelismNodeId(relevancyId);
                process.addChild(split);
                process.addChild(join);
                vo.addAddedNode(split);
                vo.addAddedNode(join);

                BPMTransition nextChildTrans = (BPMTransition) childNode.getDownTransitions().get(0);
                BPMAbstractNode nextChildNode = nextChildTrans.getTo();
                BPMTransition trans1 = new BPMTransition(targetActivity, split);
                BPMTransition trans2 = new BPMTransition(split, childNode);
                BPMTransition trans3 = new BPMTransition(childNode, join);
                BPMTransition trans4 = new BPMTransition(join, nextChildNode);
                copyCondition(nextTrans, trans2);
                copyCondition(nextChildTrans, trans4);

                process.addLink(trans1);
                process.addLink(trans2);
                process.addLink(trans3);
                process.addLink(trans4);
                vo.addAddedLink(trans1);
                vo.addAddedLink(trans2);
                vo.addAddedLink(trans3);
                vo.addAddedLink(trans4);

                process.removeLink(nextTrans);
                targetActivity.removeDownTransition(nextTrans);
                childNode.removeUpTransition(nextTrans);
                vo.addDeleteLink(nextTrans);

                process.removeLink(nextChildTrans);
                childNode.removeDownTransition(nextChildTrans);
                nextChildNode.removeUpTransition(nextChildTrans);
                vo.addDeleteLink(nextChildTrans);
            }
            
            if(isBackToMe){
            	if(nodeList.size()>1){
                    backToMeType= 2;
            	}else{
            		backToMeType= 1;
            	}
            	meNode= myNodeList.get(0);
            } 
            if(backToMeType==1){
            	BPMHumenActivity userNode = nodeList.get(0);
                BPMTransition userLink1 = new BPMTransition(split, userNode);
                BPMTransition userLink2 = new BPMTransition(userNode, meNode);
                BPMTransition userLink3 = new BPMTransition(meNode, join);
                
                process.addChild(userNode);
                process.addChild(meNode);
                process.addLink(userLink1);
                process.addLink(userLink2);
                process.addLink(userLink3);
                vo.addAddedNode(userNode);
                vo.addAddedNode(meNode);
                vo.addAddedLink(userLink1);
                vo.addAddedLink(userLink2);
                vo.addAddedLink(userLink3);
                memAndPolicyName.add(userNode.getName() + "(" + userNode.getSeeyonPolicy().getName() + ")");
                partyNames.add(userNode.getName());
                
                memAndPolicyName.add(meNode.getName() + "(" + meNode.getSeeyonPolicy().getName() + ")");
                partyNames.add(meNode.getName());
            }else if(backToMeType==2){
            	BPMAndRouter split1 = null;
                BPMAndRouter join1 = null;
                String splitId1 = UUIDLong.longUUID() + "";
                String joinId1 = UUIDLong.longUUID() + "";
                split1 = new BPMAndRouter(splitId1, "split");
                join1 = new BPMAndRouter(joinId1, "join");
                split1.setStartAnd(true);
                join1.setStartAnd(false);
                String relevancyId = UUIDLong.longUUID() + "";
                split1.setParallelismNodeId(relevancyId);
                join1.setParallelismNodeId(relevancyId);
                process.addChild(split1);
                process.addChild(join1);
                vo.addAddedNode(split1);
                vo.addAddedNode(join1);
                
                BPMTransition userLink22 = new BPMTransition(join1, meNode);
                process.addLink(userLink22);
                process.addChild(meNode);
                vo.addAddedLink(userLink22);
                vo.addAddedNode(meNode);
                memAndPolicyName.add(meNode.getName() + "(" + meNode.getSeeyonPolicy().getName() + ")");
                partyNames.add(meNode.getName());
                
                
                //将要被加签的节点与split、join节点建立关系
	            int ilen = nodeList.size()-1;
	            for (int i = ilen; i >= 0; i--) {
	                BPMHumenActivity userNode = nodeList.get(i);
	                BPMTransition userLink1 = new BPMTransition(split1, userNode);
	                BPMTransition userLink2 = new BPMTransition(userNode, join1);
	
	                process.addChild(userNode);
	                process.addLink(userLink1);
	                process.addLink(userLink2);
	                vo.addAddedNode(userNode);
	                vo.addAddedLink(userLink1);
	                vo.addAddedLink(userLink2);
	                memAndPolicyName.add(userNode.getName() + "(" + userNode.getSeeyonPolicy().getName() + ")");
	                partyNames.add(userNode.getName());
	            }
	            
	            BPMTransition userLink1 = new BPMTransition(split, split1);
                BPMTransition userLink2 = new BPMTransition(meNode, join);
                
                process.addLink(userLink1);
                process.addLink(userLink2);
                vo.addAddedLink(userLink1);
                vo.addAddedLink(userLink2);
                
            }else{
	            //将要被加签的节点与split、join节点建立关系
	            int ilen = nodeList.size()-1;
	            for (int i = ilen; i >= 0; i--) {
	                BPMHumenActivity userNode = nodeList.get(i);
	                BPMTransition userLink1 = new BPMTransition(split, userNode);
	                BPMTransition userLink2 = new BPMTransition(userNode, join);
	
	                process.addChild(userNode);
	                process.addLink(userLink1);
	                process.addLink(userLink2);
	                vo.addAddedNode(userNode);
	                vo.addAddedLink(userLink1);
	                vo.addAddedLink(userLink2);
	                memAndPolicyName.add(userNode.getName() + "(" + userNode.getSeeyonPolicy().getName() + ")");
	                partyNames.add(userNode.getName());
	            }
            }
            returnMap.put("memAndPolicyName", memAndPolicyName);
            returnMap.put("partyNames", partyNames);
            return returnMap;
        }catch(Throwable e){
            throw new BPMException(e);
        }
    }

    /**
     * 与指定节点并行
     * @param vo
     * @param process
     * @param targetActivity
     * @param nodeList
     * @param isAsignNode
     * @return
     */
    private static Object[] parallelWithTagetAddNode(BPMChangeMessageVO vo, BPMProcess process, BPMAbstractNode targetActivity,
            List<BPMHumenActivity> nodeList, boolean isAsignNode) {
        Object[] object= new Object[2];
        List<String> memAndPolicyName = new ArrayList<String>();
        List<String> partyNames = new ArrayList<String>();
        Map<String,List<String>> returnMap= new HashMap<String, List<String>>();
        BPMAssignNodeMessageVO anvo = new BPMAssignNodeMessageVO();
        if (nodeList == null || nodeList.size() <= 0) {
            object[0]= anvo;
            object[1]= returnMap;
            return object;
        }
        @SuppressWarnings("rawtypes")
        List links_ba = targetActivity.getUpTransitions();
        @SuppressWarnings("rawtypes")
        List links_ac = targetActivity.getDownTransitions();
        BPMTransition link_ba = (BPMTransition) links_ba.get(0);
        BPMTransition link_ac = (BPMTransition) links_ac.get(0);
        BPMAbstractNode b = link_ba.getFrom();//a是当前节点，b是父节点 ，c是子节点
        BPMAbstractNode c = link_ac.getTo();
        boolean bIsSplit = (b instanceof BPMAndRouter) && ((BPMAndRouter) b).isStartAnd();
        boolean cIsJoin = (c instanceof BPMAndRouter) && !((BPMAndRouter) c).isStartAnd();
        anvo.setbIsSplit(bIsSplit);
        anvo.setcIsJoin(cIsJoin);

        BPMAndRouter split = null;
        BPMAndRouter join = null;

        if (bIsSplit && cIsJoin) {
            //如果当前节点子节点和父节点分别是join、split节点，那么沿用该join节点和split节点
            split = (BPMAndRouter) b;
            join = (BPMAndRouter) c;
        } else {
            //如果不是if里面的情况，那么创建join节点和split节点
            String splitId = UUIDLong.longUUID() + "";
            String joinId = UUIDLong.longUUID() + "";
            split = new BPMAndRouter(splitId, "split");
            join = new BPMAndRouter(joinId, "join");
            String relevancyId = UUIDLong.longUUID() + "";
            split.setParallelismNodeId(relevancyId);
            join.setParallelismNodeId(relevancyId);
            split.setStartAnd(true);
            join.setStartAnd(false);
            process.addChild(split);
            process.addChild(join);
            vo.addAddedNode(split);
            vo.addAddedNode(join);

            //建议join节点与当前节点的父节点之间的关系
            BPMTransition link_b_split = new BPMTransition(b, split);
            process.addLink(link_b_split);
            vo.addAddedLink(link_b_split);
            //建立split节点与当前节点子节点之间的关系
            BPMTransition link_join_c = new BPMTransition(join, c);
            copyCondition(link_ac, link_join_c);
            process.addLink(link_join_c);
            vo.addAddedLink(link_join_c);
            
            //删除当前节点和父节点的关系，建立当前节点和split节点之间的关系
            for (int i = targetActivity.getUpTransitions().size() - 1; i >= 0; i--) {
                BPMTransition b_a = (BPMTransition) targetActivity.getUpTransitions().get(i);
                process.removeLink(b_a);
                b_a.getFrom().removeDownTransition(b_a);
                b_a.getTo().removeUpTransition(b_a);
                vo.addDeleteLink(b_a);
                BPMTransition link_split_a = new BPMTransition(split, b_a.getTo());
                //复制分支条件
                copyCondition(link_ba, link_split_a);
                process.addLink(link_split_a);
                vo.addAddedLink(link_split_a);
            }

            //删除当前节点和子节点的关系，建立当前节点和join节点之间的关系
            for (int i = targetActivity.getDownTransitions().size() - 1; i >= 0; i--) {
                BPMTransition a_c = (BPMTransition) targetActivity.getDownTransitions().get(i);
                process.removeLink(a_c);
                a_c.getFrom().removeDownTransition(a_c);
                a_c.getTo().removeUpTransition(a_c);
                vo.addDeleteLink(a_c);
                BPMTransition link_a_join = new BPMTransition(targetActivity, join);
                process.addLink(link_a_join);
                vo.addAddedLink(link_a_join);
            }
        }
        anvo.setJoin(join);
        anvo.setSplit(split);
        //下面的逻辑和A8原有逻辑不相同，A8中公文和协同会签时分别有不同的节点权限，现在修改为通通自己传进来
        //建立会签的节点和split和join节点之间的关系
        List<BPMActivity> added = new ArrayList<BPMActivity>();
        int ilen= nodeList.size()-1;
        for (int i=ilen;i>=0;i--) {
            BPMHumenActivity d= nodeList.get(i);
            if (d != null) {
                BPMTransition link_split_d = new BPMTransition(split, d);
                BPMTransition link_d_join = new BPMTransition(d, join);
                if (isAsignNode) {
                    //如果是会签的话才需要这些操作
                    BPMSeeyonPolicy seeyonPolicy = d.getSeeyonPolicy();
                    if (d != null && !"competition".equals(seeyonPolicy.getProcessMode())) {
                        seeyonPolicy.setProcessMode("all");
                    }
                    //复制分支条件
                    copyCondition(link_ba, link_split_d);
                }
                process.addLink(link_split_d);
                process.addLink(link_d_join);
                process.addChild(d);
                vo.addAddedLink(link_split_d);
                vo.addAddedLink(link_d_join);
                vo.addAddedNode(d);
                added.add((BPMActivity) d);
                memAndPolicyName.add(d.getName() + "(" + d.getSeeyonPolicy().getName() + ")");
                partyNames.add(d.getName());
            }
        }
        anvo.setAdded(added);
        returnMap.put("memAndPolicyName", memAndPolicyName);
        returnMap.put("partyNames", partyNames);
        object[0]= anvo;
        object[1]= returnMap;
        return object;
    }

    /**
     * 当前会签
     * @param vo
     * @param isPending
     * @param theCase
     * @return
     * @throws BPMException
     */
    private static  Map<String,List<String>>  assignNode(BPMChangeMessageVO vo, boolean isPending, BPMCase theCase) throws BPMException {
        try{
            Map<String,List<String>> returnMap= new HashMap<String, List<String>>();
            BPMProcess process = vo.getProcess();
            BPMAbstractNode targetActivity = vo.getTargetActivity();
            List<BPMHumenActivity> nodeList = vo.getAddedActivityList();

            //如果将要添加的节点数量为0，那么什么都不操作。
            if (Strings.isEmpty(nodeList)) {
                return returnMap;
            }
            //修改只读标记
            String fr = targetActivity.getSeeyonPolicy().getFR();
            if("1".equals(fr)){
            	//如果当前节点是只读，那么当前会签的节点都弄成只读
	            for(BPMHumenActivity node : nodeList){
	            	node.getSeeyonPolicy().setFR("1");
	            }
            }
            //将添加的节点与目标节点并行
            Object[] rObj= parallelWithTagetAddNode(vo, process, targetActivity, nodeList, true);
            BPMAssignNodeMessageVO assignVo = (BPMAssignNodeMessageVO)rObj[0];
            returnMap= (Map<String,List<String>>)rObj[1];

            //节点添加的逻辑在这里就已经完毕了，下面是添加后需要产生待办工作项的逻辑。
            //将加入的人设为ready状态
            if (isPending && vo.getCurrentActivityId().equals(vo.getTargetActivityId())) {
                //A8中下面一句就是注释掉的，不知道为什么，现在先搬过来，也注释掉。
                //pe.addReadyActivity(user.getId() + "", process, theCase, added);
                //ReadyObject readyObject = new ReadyObject();
                ReadyObject readyObject = vo.getReadyObject();
                if (readyObject == null) {
                    readyObject = new ReadyObject();
                }
                if (readyObject.getActivityList() == null) {
                    readyObject.setActivityList(new ArrayList<BPMActivity>());
                }
                readyObject.getActivityList().addAll(assignVo.getAdded());
                readyObject.setCaseId(String.valueOf(vo.getCaseId()));
                readyObject.setProcessId(process.getId());
                readyObject.setUserId(vo.getUserId());
                //修改join的num
                boolean saveTheCaseFlag = false;
                if (assignVo.isbIsSplit() && assignVo.iscIsJoin() && theCase != null) {
                    ReadyNode node = theCase.getReadyActivityById(assignVo.getJoin().getId());
                    if (node != null) {
                        node.setNum(node.getNum() + nodeList.size());
                        saveTheCaseFlag = true;
                    }
                }
                readyObject.setSaveTheCaseFlag(saveTheCaseFlag);
                vo.setReadyObject(readyObject);
            }
            return returnMap;
        }catch(Throwable e){
            throw new BPMException(e.getMessage(),e);
        }
    }

    public static String[] getArray(String key, Map<Object, Object> map) {
        String[] result = null;
        if (map == null) {
            return result;
        }
        Object object = map.get(key);
        if (object != null) {
            if (object instanceof String[]) {
                result = (String[]) object;
            } else if (object instanceof List) {
                @SuppressWarnings("rawtypes")
                List list = (List) object;
                result = new String[list.size()];
                int index = 0;
                for (Object o : list) {
                    if (o != null) {
                        if (o instanceof String) {
                            result[index] = (String) o;
                            index++;
                        }else if(o instanceof Boolean){
                            result[index] = String.valueOf(o);
                            index++;
                        }else if(o instanceof Long){
                            result[index] = String.valueOf(o);
                            index++;
                        }
                    }
                }
            } else {
                result = new String[] { object.toString() };
            }
        }
        return result;
    }

    private static String getUserTypeByField(String userTypeFieldName) {
        if (StringUtils.isBlank(userTypeFieldName) || ProcessOrgManager.ORGENT_TYPE_MEMBER.equals(userTypeFieldName)) {
            return "user";
        }
        return userTypeFieldName;
    }

    private static void copyCondition(BPMTransition from, BPMTransition to) {
        to.setConditionBase(from.getConditionBase());
        to.setConditionId(from.getConditionId());
        to.setConditionTitle(from.getConditionTitle());
        to.setConditionType(from.getConditionType());
        to.setFormCondition(from.getFormCondition());
        to.setIsForce(from.getIsForce());
    }

    /**
     * 找到Split节点对应的join节点。
     * @param process
     * @param split
     * @return
     */
    private static BPMAndRouter findJoinOfSplit(BPMProcess process, BPMAndRouter split) {
        BPMAndRouter join = null;
        BPMAbstractNode node = split;
        // 算法查找
        // 查找所有直接后续节点都通过的Join节点。
        @SuppressWarnings("unchecked")
        List<BPMTransition> links = node.getDownTransitions();
        if (links == null)
            return null;

        Set<BPMAbstractNode> set = new HashSet<BPMAbstractNode>();
        for (BPMTransition link : links) {
            BPMAbstractNode to = link.getTo();
            Set<BPMAbstractNode> allNext = getAllNextNodes(to);
            if (set.size() == 0) {
                set.addAll(allNext);
            } else {
                set.retainAll(allNext);
            }
        }
        if (set.size() > 0) {
            // 找出路径中第一个Join节点
            BPMAndRouter firstJoin = null;
            for (BPMAbstractNode n : set) {
                if (isJoinNode(n)) {
                    BPMAndRouter join2 = (BPMAndRouter) n;
                    if (firstJoin == null) {
                        firstJoin = join2;
                    }
                    firstJoin = (passThrough(firstJoin, join2)) ? firstJoin : join2;
                }
            }
            return firstJoin;
        }
        //下面是原来的两种判断方式，依据ParallelismNodeId查找，算法查找有效后可删除；如果ParallelismNodeId有效，优先使用按节点下溯查找
        //按节点下溯查找
        boolean foundJoin = false;
        while (!foundJoin) {
            BPMTransition trans = (BPMTransition) node.getDownTransitions().get(0);
            node = trans.getTo();
            if (node instanceof BPMAndRouter) {
                BPMAndRouter andNode = (BPMAndRouter) node;
                if (isJoinNode(node)) {
                    if (split.getParallelismNodeId().equals(andNode.getParallelismNodeId())) {
                        foundJoin = true;
                        join = andNode;
                        return join;
                    }
                }
            }
        }

        // 遍历所有activity
        @SuppressWarnings("unchecked")
        List<BPMAbstractNode> activityList = process.getActivitiesList();
        for (int i = 0; i < activityList.size(); i++) {
            node = activityList.get(i);
            if (node instanceof BPMAndRouter) {
                BPMAndRouter andRouter = (BPMAndRouter) node;
                String parallelismSplitId = split.getParallelismNodeId();
                String parallelismJoinId = andRouter.getParallelismNodeId();
                if (parallelismSplitId.equalsIgnoreCase(parallelismJoinId) && !split.getId().equals(andRouter.getId())) {
                    join = andRouter;
                    break;
                }
            }
        }
        return join;
    }

    /**
     * 取得节点的所有后续节点。
     * @param node
     * @return
     */
    private static Set<BPMAbstractNode> getAllNextNodes(BPMAbstractNode node) {
        Set<BPMAbstractNode> result = new HashSet<BPMAbstractNode>();
        @SuppressWarnings("unchecked")
        List<BPMTransition> links = node.getDownTransitions();
        for (BPMTransition link : links) {

            BPMAbstractNode to = link.getTo();
            result.add(to);
            if (!(to instanceof BPMEnd)) {
                result.addAll(getAllNextNodes(to));
            }
        }
        return result;
    }

    /**
     * 判断节点1后续节点是否包含节点2。
     * @param split
     * @param join
     * @return
     */
    private static boolean passThrough(BPMAbstractNode split, BPMAbstractNode join) {
        @SuppressWarnings("unchecked")
        List<BPMTransition> links = split.getDownTransitions();
        if (links == null)
            return false;
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

    //    private static boolean isSplitNode(BPMAbstractNode node) {
    //        return (node instanceof BPMAndRouter) && ((BPMAndRouter) node).isStartAnd();
    //    }

    private static boolean isJoinNode(BPMAbstractNode node) {
        return (node instanceof BPMAndRouter) && !((BPMAndRouter) node).isStartAnd();
    }

    private static List<BPMHumenActivity> createActivityList(String userId, String[] ids, String[] types,
            String[] names, String[] accountIds,String[] policyId, String[] policyName, String[] node_process_mode,
            String dealTerm, String remindTime, boolean isFormOperationReadonly, String fromType,
            String[] userExcludeChildDepartment, String formApp, String formViewOperation,String DR) {
        List<BPMHumenActivity> nodeList = null;
        if (ids != null && ids.length > 0) {
            nodeList = new ArrayList<BPMHumenActivity>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                String type = getUserTypeByField(types[i]);
                String name = "";
                if(null!=names){
                    name = names[i];
                }
                String accountId = accountIds[i];
                //String accountShortName = accountShortNames[i];
                String roleName = "roleadmin";
                //创建节点
                BPMHumenActivity userNode = new BPMHumenActivity(UUIDLong.longUUID() + "", name);
                String tempPolicyId = null, tempPolicyName = null, processMode = null;
                if (policyId != null && policyId.length <= i) {
                    tempPolicyId = policyId[0];
                } else {
                    if(null != policyId){
                        tempPolicyId = policyId[i];
                    }
                }
                if (policyName != null && policyName.length <= i) {
                    tempPolicyName = policyName[0].trim();
                } else {
                    if(null != policyName){
                        tempPolicyName = policyName[i].trim();
                    }
                }
                BPMSeeyonPolicy seeyonPolicy = new BPMSeeyonPolicy(tempPolicyId, tempPolicyName);
                if (node_process_mode != null ){
                    if(node_process_mode.length <= i){
                        processMode = node_process_mode[0];
                    }else{
                        processMode = node_process_mode[i];
                    }
                }else{
                    processMode = "all";
                }
                if (Strings.isNotBlank(processMode) && !"user".equals(type)) {
                    seeyonPolicy.setProcessMode(processMode);
                }
                if("Post".equals(type)) {
                	try {//集团基准岗匹配范围是全集团
                		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(accountId));
                		if(account != null && account.isGroup()) {
                			seeyonPolicy.setMatchScope("2");
                		}
                	} catch(Exception e) {
                		logger.error("加签操作获取集团开岗位出错", e);
                	}
                }
                seeyonPolicy.setdealTerm(dealTerm);
                seeyonPolicy.setRemindTime(remindTime);
                seeyonPolicy.setFormApp(formApp);
                seeyonPolicy.setFormViewOperation(formViewOperation);
                seeyonPolicy.setDR(DR);
                //设置处理模式，all表示全体执行
                if (Strings.isBlank(seeyonPolicy.getProcessMode())) {
                    seeyonPolicy.setProcessMode("all");
                }
                //如果是具体人的话，设置为单人。
                if ("user".equals(type)) {//具体人的话，为单人执行
                    seeyonPolicy.setProcessMode("single");
                }

                //传阅和知会非人节点应该是全体执行
                if (seeyonPolicy != null && "inform".equals(seeyonPolicy.getId()) && !"user".equals(type)) {
                       seeyonPolicy.setProcessMode("all");
                }
                //表明该节点是被加签或者会签或者知会的
                seeyonPolicy.setAdded(true);
                //表明该节点是被谁添加的,workitem的performer也是工作流BPMActivity节点中seeyonPolicy的dealTermUserId属性。
                seeyonPolicy.setAddedFromId(userId);
                //设置表单只读标记，1只读,""默认
                seeyonPolicy.setFR(isFormOperationReadonly ? "1" : "");
                userNode.setSeeyonPolicy(seeyonPolicy);
                //多级会签增加,自动增加自己加节点标识（但是怎么会设置成表单类型这么一个属性呢？这表示数据来源类型）
                userNode.setFromType(fromType);
                //给活动节点创建一个角色，然后添加到活动节点中。
                BPMParticipantType bpmType = new BPMParticipantType(type);
                BPMActor userActor = new BPMActor(id, name, bpmType, roleName, BPMActor.CONDITION_OR, false, accountId);
                if (userExcludeChildDepartment != null && userExcludeChildDepartment.length > i
                        && userExcludeChildDepartment[i] != null && "true".equals(userExcludeChildDepartment[i])) {
                    userActor.getParty().setIncludeChild(false);
                }else{
                    userActor.getParty().setIncludeChild(true);
                }
                userNode.addActor(userActor);
                nodeList.add(userNode);
            }
        }
        return nodeList;
    }
    
    public static BPMNewNodeVO nodeToVO(BPMAbstractNode node){
    	BPMNewNodeVO result = null;
    	if(node!=null){
    		result = new BPMNewNodeVO();
    		result.setId(node.getId());
    		result.setName(node.getName());
    		if(node instanceof BPMHumenActivity){
	    		result.setPolId(node.getSeeyonPolicy().getId());
	    		result.setPolName(node.getSeeyonPolicy().getName());
	    		result.setPrmd(node.getSeeyonPolicy().getProcessMode());
	    		result.setDltm(node.getSeeyonPolicy().getdealTerm());
	    		result.setRdtm(node.getSeeyonPolicy().getRemindTime());
	    		result.setFormApp(node.getSeeyonPolicy().getFormApp());
	    		result.setDR(node.getSeeyonPolicy().getDR());
	    		
	    		result.setFormViewOperation(node.getSeeyonPolicy().getFormViewOperation());
	    		
	    		result.setFr(node.getSeeyonPolicy().getFR());
	    		result.setFromType(node.getFromType());
	    		BPMActor userActor = (BPMActor)node.getActorList().get(0);
	    		result.setEleId(userActor.getId());
	    		result.setEleName(userActor.getName());
	    		result.setEleType(userActor.getType().id);
	    		result.setRname(userActor.getRoleId());
	    		result.setAccId(userActor.getParty().getAccountId());
	    		result.setInclch(String.valueOf(userActor.getParty().isIncludeChild()));
	    		result.setAdded(node.getSeeyonPolicy().isAdded());
	    		result.setAddedFromId(node.getSeeyonPolicy().getAddedFromId());
    		}else{
    			BPMAndRouter joinOrSplit = (BPMAndRouter)node;
    			result.setStartAnd(String.valueOf(joinOrSplit.isStartAnd()));
        		result.setParall(joinOrSplit.getParallelismNodeId());
    		}
    	}
    	return result;
    }
    
    
    private static BPMTransition nodeToLink(BPMAbstractNode from, BPMAbstractNode to){
        BPMTransition result = new BPMTransition();
        result.setId(String.valueOf(UUIDLong.longUUID()));
        result.setName("");
        result.setConditionType(ConditionType.noCondition.getKey());
        result.setIsForce("");
        result.setFormCondition("");
        result.setConditionId("");
        result.setConditionBase("");
        result.setConditionTitle("");
        result.setFrom(from);
        result.setTo(to);
        return result;
    }
    
    
    public static void mergeOneToMany(WorkflowBpmContext context,BPMProcess process, Map<String, Map<String, Object>> selectPeoples) {
    	
    	Map<String, Map<String, String>> orderExecuteMember = (Map<String, Map<String, String>>) context.getBusinessData(EventDataContext.ORDEREXECUTE_MEMBER_NODE);
    	if(null==orderExecuteMember){
    		orderExecuteMember = new HashMap<String, Map<String,String>>();
    	}
    	
    	for (String nodeId : selectPeoples.keySet()) {
            Map<String, Object> selectNodePeopleInfos = selectPeoples.get(nodeId);
            String isOrderExcute = (String) selectNodePeopleInfos.get("isOrderExecute");
            if ("true".equals(isOrderExcute)) {
                String[] peoples = (String[]) selectNodePeopleInfos.get("pepole");
                
                List<BPMAbstractNode> newNodes = new ArrayList<BPMAbstractNode>();
                BPMAbstractNode originalActivity = process.getActivityById(nodeId);
                
              //设置节点名称， 被替换后后面就找不到了
                selectNodePeopleInfos.put("nodeName", originalActivity.getName());
                Map<String,String> orderExecuteNode = orderExecuteMember.get(nodeId);
                if (peoples != null && peoples.length > 0) {
                    for (String personId : peoples) {
                    	
                    	//流程仿真需要获取上一次仿真节点的id，否则会导致分支匹配不成功
                    	String newNodeId = "";
                    	if(orderExecuteNode!=null && !orderExecuteNode.isEmpty()){
                    		newNodeId = orderExecuteNode.get(personId);
                    	}else{
                    		orderExecuteNode = new HashMap<String,String>();
                    	}
                    	
                        BPMActivity newNode = memberIdToNode(personId,newNodeId, originalActivity);
                        if(null!=newNode){
                        	newNode.setFromType(String.valueOf(ChangeType.OrderExecuteAdd.getKey()));
                        	newNodes.add(newNode);
                        	orderExecuteNode.put(personId, newNode.getId());
                        }
                    }
                }
                
                if(Strings.isNotEmpty(newNodes)){
                    replaceOneNodeToMany(process, newNodes, originalActivity);
                    context.setProcessChanged(true);
                    //将替换的第一节点加入节点选择信息。否则分支选择情况校验的时候会有问题
                    WorkflowUtil.putNodeConditionToContext(context, newNodes.get(0), "isDelete", "false");
                }
                
                orderExecuteMember.put(nodeId, orderExecuteNode);
            }
        }
    	
    	context.setBusinessData(EventDataContext.ORDEREXECUTE_MEMBER_NODE, orderExecuteMember);
    }
    
    /**
     * 
     * @param process
     * @param newNodes
     * @param orginalNode
     */
    @SuppressWarnings("unchecked")
    public static void replaceOneNodeToMany(BPMProcess process,List<BPMAbstractNode> newNodes,BPMAbstractNode orginalNode){
        
        if(Strings.isEmpty(newNodes)){
            return;
        }
        
        BPMAbstractNode from = null; 
        //1、找到原始节点的线，删除
      
        List<BPMTransition> downLines = orginalNode.getDownTransitions();
        List<BPMTransition> upLines = orginalNode.getUpTransitions();
        
        BPMTransition fromLine = upLines.get(0);
        BPMTransition toLine = downLines.get(0);
        
        from = fromLine.getFrom();
        
        //process.removeLink(fromLine);
        //process.removeLink(toLine);
        
        orginalNode.removeUpTransition(fromLine);
        orginalNode.removeDownTransition(toLine);
        
        //2、删除原始节点
        process.removeChild(orginalNode);
        
        //3.按顺序构建新的节点之间的连线，添加到流程中    eg.  old-new1- new2 - new3 - old1
        for(int i = 0; i < newNodes.size(); i++){
            BPMAbstractNode newNode = newNodes.get(i);
            BPMTransition link = null;
            if(i == 0){
                link = fromLine;
                //重新拼接
                link.setTo(newNode);
                newNode.addUpTransition(link);
            }else{
                link = nodeToLink(from,newNode);
                process.addLink(link);
            }
            from = newNode;
        }
        
        //从新拉线
        toLine.setFrom(from);
        from.addDownTransition(toLine);
        //process.addLink(toLine);
        
        //4、添加新节点到流程
        for(BPMAbstractNode newNode : newNodes){
            process.addChild(newNode);
        }
    }
    
    /**
     * 移除指定节点
     * 
     * 串行时：移除当前节点和后面的线， 前面的线直接连接掉后面节点
     * 并行时：移除前后线
     * 并行且split和join节点中只有当前节点， 移除整个并发分支
     * 
     * @param process
     * @param node
     *
     * @Since A8-V5 7.0
     * @Author      : xuqw
     * @Date        : 2018年5月2日下午2:19:21
     *
     */
    public static void removeNode(BPMProcess process, BPMAbstractNode node){
        
        if(process == null || ObjectName.isAndRouterObject(node)){
            return;
        }
        
        List<BPMTransition> downLines = node.getDownTransitions();
        List<BPMTransition> upLines = node.getUpTransitions();
        
        BPMTransition fromLine = upLines.get(0);
        BPMTransition toLine = downLines.get(0);
        
        BPMAbstractNode from = fromLine.getFrom();
        BPMAbstractNode to = toLine.getTo();
        
        if(ObjectName.isAndRouterObject(from) && ObjectName.isAndRouterObject(to)){
            
            List<BPMTransition> splitDonwLines = from.getDownTransitions();
            if(splitDonwLines.size() == 1){
                //split和join之间只有当前节点了
                //这种情况应该不存在...
            }else if(splitDonwLines.size() == 2){
                
                //移除的时候, 当前并发分支只有两条了
                
                process.removeLink(fromLine);
                process.removeLink(toLine);
                process.removeChild(node);
                
                //现在split和join间只有一个节点了， 移除split和join
                BPMTransition splitFromLine = (BPMTransition) from.getUpTransitions().get(0);
                BPMTransition splitToLine = (BPMTransition) from.getDownTransitions().get(0);
                BPMAbstractNode splitToNode = splitToLine.getTo();
                BPMAbstractNode splitFromNode = splitFromLine.getFrom();
                if(Strings.isNotBlank(splitFromLine.getFormCondition())){
                    
                    process.removeLink(splitToLine);
                    from.removeUpTransition(splitFromLine);
                    process.removeChild(from);
                    
                    splitFromLine.setTo(splitToNode);
                    splitToNode.addUpTransition(splitFromLine);
                }else{
                    process.removeLink(splitFromLine);
                    from.removeDownTransition(splitToLine);
                    process.removeChild(from);
                    splitToLine.setFrom(splitFromNode);
                    splitFromNode.addDownTransition(splitToLine);
                }
                
                //join节点
                BPMTransition joinFromLine = (BPMTransition) to.getUpTransitions().get(0);
                BPMTransition joinToLine = (BPMTransition) to.getDownTransitions().get(0);
                BPMAbstractNode joinFromNode = joinFromLine.getFrom();
                
                process.removeLink(joinFromLine);
                to.removeDownTransition(joinToLine);
                process.removeChild(to);
                joinToLine.setFrom(joinFromNode);
                joinFromNode.addDownTransition(joinToLine);
                
            }else{
                //直接把自己移除， 前后线
                process.removeLink(fromLine);
                process.removeLink(toLine);
                process.removeChild(node);
            }
        }else{
            //普通移除
            process.removeLink(toLine);
            node.removeUpTransition(fromLine);
            process.removeChild(node);
            
            fromLine.setTo(to);
            to.addUpTransition(fromLine);
        }
    }
    
    
    public static BPMActivity memberIdToNode(String memberId,String newNodeId,BPMAbstractNode originalActivity){
        
        
        if(!Strings.isDigits(memberId)){
            return null;
        }
        V3xOrgMember member = null;
        try {
            member = orgManager.getMemberById(Long.valueOf(memberId));
        }catch (BusinessException e2) {
            logger.error(e2.getLocalizedMessage(),e2);
        }
        if(member == null){
            return null;
        }
        
    	
        BPMSeeyonPolicy  originalPolicy= originalActivity.getSeeyonPolicy();
    	
        if(Strings.isBlank(newNodeId)){
        	newNodeId = String.valueOf(UUIDLong.longUUID());
        }
        BPMActivity newNode =  new BPMHumenActivity(newNodeId,member.getName());
      
        
        
        BPMSeeyonPolicy newPolicy = new BPMSeeyonPolicy(originalPolicy.getId(), originalPolicy.getName());
        newPolicy.setProcessMode("single");
        newPolicy.setdealTerm(originalPolicy.getdealTerm());
        newPolicy.setDealTermType(originalPolicy.getDealTermType());
        newPolicy.setDealTermUserId(originalPolicy.getDealTermUserId());
        newPolicy.setDealTermUserName(originalPolicy.getDealTermUserName());
        newPolicy.setDesc(originalPolicy.getDesc());
        newPolicy.setAsBlankNode(originalPolicy.getAsBlankNode());
        newPolicy.setCycleRemindTime(originalPolicy.getCycleRemindTime());
        newPolicy.setIgnoreBlank(originalPolicy.getIgnoreBlank());
        newPolicy.setOperationm(originalPolicy.getOperationm());
        newPolicy.setOperationName(originalPolicy.getOperationName());
        newPolicy.setRemindTime(originalPolicy.getRemindTime());
        newPolicy.setFormApp(originalPolicy.getFormApp());
        newPolicy.setFormViewOperation(originalPolicy.getFormViewOperation());
        newPolicy.setFR(originalPolicy.getFR());
        newPolicy.setDR(originalPolicy.getDR());
        newPolicy.setMergeDealType(originalPolicy.getMergeDealType());
        newNode.setSeeyonPolicy(newPolicy);
		

		
		BPMParticipantType type = new BPMParticipantType("user");
		BPMActor newActor = new BPMActor(String.valueOf(member.getId()), member.getName(), type, "roleadmin", BPMActor.CONDITION_OR, false, String.valueOf(member.getOrgAccountId()));
		newActor.getParty().setIncludeChild(false);
		newNode.addActor(newActor);
    	return newNode;
    }
    public static BPMActivity voToNode(BPMNewNodeVO vo){
    	BPMActivity result = null;
    	if(WorkflowUtil.isNotNull(vo.getStartAnd())){
    		BPMAndRouter node = new BPMAndRouter(vo.getId(), vo.getName());
    		node.setStartAnd(Boolean.parseBoolean(vo.getStartAnd()));
    		node.setParallelismNodeId(vo.getParall());
    		BPMSeeyonPolicy policy = new BPMSeeyonPolicy("collaboration","协同");
    		policy.setAdded(true);
    		node.setSeeyonPolicy(policy);
    		result = node;
    	}else{
    		BPMHumenActivity node = new BPMHumenActivity(vo.getId(), vo.getName());
    		node.setFromType(vo.getFromType());
    		BPMSeeyonPolicy policy = new BPMSeeyonPolicy(vo.getPolId(), vo.getPolName());
    		policy.setProcessMode(vo.getPrmd());
    		policy.setdealTerm(vo.getDltm());
    		policy.setRemindTime(vo.getRdtm());
    		policy.setFormApp(vo.getFormApp());
    		policy.setFormViewOperation(vo.getFormViewOperation());
    		policy.setFR(vo.getFr());
    		policy.setDR(vo.getDR());
    		policy.setAdded(true); 
    		policy.setAddedFromId(vo.getAddedFromId());
    		if("Post".equals(vo.getEleType())) {
            	try {//集团基准岗匹配范围是全集团
            		V3xOrgAccount account = orgManager.getAccountById(Long.parseLong(vo.getAccId()));
            		if(account != null && account.isGroup()) {
            			policy.setMatchScope("2");
            		}
            	} catch(Exception e) {
            		logger.error("加签提交获取集团开岗位出错", e);
            	}
            }
    		node.setSeeyonPolicy(policy);
    		BPMParticipantType type = new BPMParticipantType(vo.getEleType());
    		BPMActor actor = new BPMActor(vo.getEleId(), vo.getEleName(), type, vo.getRname(), BPMActor.CONDITION_OR, false, vo.getAccId());
    		actor.getParty().setIncludeChild(Boolean.parseBoolean(vo.getInclch()));
    		node.addActor(actor);
    		result = node;
    	}
    	return result;
    }
    
    public static BPMNewLinkVO linkToVO(BPMTransition link){
    	BPMNewLinkVO result = null;
    	if(link!=null){
    		result = new BPMNewLinkVO();
    		result.setId(link.getId());
    		result.setName(link.getName());
    		result.setFrom(link.getFrom().getId());
    		result.setTo(link.getTo().getId());
    		result.setType(link.getConditionType());
    		result.setIsForce(link.getIsForce());
    		result.setCon(link.getFormCondition());
    		result.setCid(link.getConditionId());
    		result.setCbase(link.getConditionBase());
    		result.setCtitle(link.getConditionTitle());
    	}
    	return result;
    }
    
    public static BPMTransition voToLink(BPMNewLinkVO vo, BPMProcess process){
    	BPMAbstractNode from = null;
    	BPMAbstractNode to = null;
    	if("start".equals(vo.getFrom())){
    		from = process.getStart();
    	}else{
    		from = process.getActivityById(vo.getFrom());
    	}
    	if("end".equals(vo.getTo())){
    		to = (BPMAbstractNode)process.getEnds().get(0);
    	}else{
    		to = process.getActivityById(vo.getTo());
    	}
    	BPMTransition result = new BPMTransition();
    	result.setId(vo.getId());
    	result.setName(vo.getName());
    	result.setConditionType(vo.getType());
    	result.setIsForce(vo.getIsForce());
    	result.setFormCondition(vo.getCon());
    	result.setConditionId(vo.getCid());
    	result.setConditionBase(vo.getCbase());
    	result.setConditionTitle(vo.getCtitle());
    	result.setFrom(from);
    	result.setTo(to);
    	return result;
    }
    
    public static void mergeProcessAndChangeMessage(BPMProcess process, BPMChangeMergeVO vo){
        logger.info(AppContext.currentUserName()+",执行加签/会签逻辑[mergeProcessAndChangeMessage]。");
    	//删除连线
    	if(Strings.isNotEmpty(vo.getDelLIds())){
    		for(String id : vo.getDelLIds()){
    			BPMTransition link = process.getLinkById(id);
    			if(link!=null){
    				link.getFrom().removeDownTransition(link);
    				link.getTo().removeUpTransition(link);
    				process.removeLink(link);
    			}
    		}
    	}
    	//删除节点
    	if(Strings.isNotEmpty(vo.getDelNIds())){
    		List<String> delIds = vo.getDelNIds();
    		for(String id : delIds){
    			BPMActivity node = process.getActivityById(id);
    			if(node!=null){
    				process.removeChild(node);
    			}
    		}
    		
    		//清除复制粘贴标记
            List list = process.getActivitiesList();
            for(Object node : list){
            	BPMAbstractNode _node = (BPMAbstractNode)node;
            	if(delIds.contains(_node.getCopyFrom()) || delIds.contains(_node.getPasteTo())){
            		_node.setCopyFrom("");
            		_node.setPasteTo("");
            		_node.setCopyNumber("");
            	}
            }
    	}
    	//添加节点
    	if(Strings.isNotEmpty(vo.getNodes())){
    		for(BPMNewNodeVO nvo : vo.getNodes()){
    			BPMActivity node = voToNode(nvo);
    			if(node!=null){
    				process.addChild(node);
    			}
    		}
    	}
    	//添加分支
    	if(Strings.isNotEmpty(vo.getLinks())){
    		for(BPMNewLinkVO lvo : vo.getLinks()){
    			BPMTransition link = voToLink(lvo, process);
    			if(link!=null){
    				process.addLink(link);
    			}
    		}
    	}
    }
    
    /**
     * 串行加签
     * @param vo
     * @param process
     * @param targetActivity
     * @param nodeList
     * @return
     * @throws BPMException
     */
    public static BPMProcess serialAddNode(BPMProcess process, BPMAbstractNode currentActivity,List<BPMHumenActivity> nodeList) throws BPMException {
        try {
        	List<BPMTransition> downs= currentActivity.getDownTransitions();
        	BPMTransition down= downs.get(0);
        	BPMAbstractNode toNode= down.getTo();
        	
            BPMAbstractNode previousNode = currentActivity;
            for (int i = 0; i < nodeList.size(); i++) {
                if (i > 0) {
                    previousNode = nodeList.get(i - 1);
                }
                BPMActivity userNode = nodeList.get(i);
                BPMTransition userLink = new BPMTransition(previousNode, userNode);
                process.addChild(userNode);
                process.addLink(userLink);
            }

            BPMAbstractNode finalNode = nodeList.get(nodeList.size() - 1);
            
            BPMTransition newLink = new BPMTransition(finalNode, toNode);
            
            copyCondition(down, newLink);
            
            process.addLink(newLink);
            process.removeLink(down);
            
            currentActivity.removeDownTransition(down);
            toNode.removeUpTransition(down);
            
            return process;
        } catch (Throwable e) {
            throw new BPMException(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param orgJson
     * @param defaultPolicyId
     * @param defaultPolicyName
     * @return
     * @throws BPMException
     */
    public static List<BPMHumenActivity> createBPMHumenActivityList(String orgJson,String defaultPolicyId,String defaultPolicyName) throws BPMException {
    	try{
	    	JSONArray orgJsonArray= new JSONArray(orgJson);
	    	int length= orgJsonArray.length();
	    	List<BPMHumenActivity> nodeList = new ArrayList<BPMHumenActivity>(length);
	    	for(int i=0;i<length;i++){
	    		JSONObject jsonObj= orgJsonArray.getJSONObject(i);
	    		String id= jsonObj.optString("id");
				String name= jsonObj.optString("name");
				String entityType= jsonObj.optString("entityType");
				String accountId= jsonObj.optString("accountId");
				String accountName= jsonObj.optString("accountName");
				String includeChildStr = jsonObj.optString("includeChild", "");
				
				boolean includeChild = true;
                if (includeChildStr != null && "false".equals(includeChildStr)) {
                    includeChild = false;
                }
				
				String type= getUserTypeByField(entityType);
				
				BPMHumenActivity userNode = new BPMHumenActivity(UUIDLong.longUUID() + "", name);
				BPMSeeyonPolicy seeyonPolicy = new BPMSeeyonPolicy(defaultPolicyId, defaultPolicyName);
	            if ("user".equals(type)) {//具体人的话，为单人执行
	                seeyonPolicy.setProcessMode("single");
	              //快速选人缺失单位信息, 前端传过来的单位ID不靠谱
                    if(WorkflowUtil.isLong(id)){
                        long userId = Long.valueOf(id);
                        V3xOrgMember v3xMember = orgManager.getMemberById(userId);
                        if(v3xMember != null){
                            accountId = v3xMember.getOrgAccountId().toString();
                        }
                    }
	            }else if ("inform".equals(seeyonPolicy.getId())) {
                    seeyonPolicy.setProcessMode("all");
	            }else{
	                //新建自由协同， 选择的部门需要全部执行
	                seeyonPolicy.setProcessMode("all");
	            }
	            seeyonPolicy.setFR("");
	            userNode.setSeeyonPolicy(seeyonPolicy);
	            BPMParticipantType bpmType = new BPMParticipantType(type);
	            String roleName= "roleadmin";
	            BPMActor userActor = new BPMActor(id, name, bpmType, roleName, BPMActor.CONDITION_OR, false, accountId);
	            userActor.getParty().setIncludeChild(includeChild);
	            
	            userNode.addActor(userActor);
	            nodeList.add(userNode);
	    	}
	    	return nodeList;
    	}catch(Throwable e){
    		throw new BPMException("",e);
    	}
    }

    /**
     * 
     * @param process
     * @param currentActivity
     * @param nodeList
     */
	public static BPMProcess parallelAddNode(BPMProcess process, BPMAbstractNode currentActivity,List<BPMHumenActivity> nodeList) throws BPMException {
		try{
            @SuppressWarnings("rawtypes")
            List downTransitions = currentActivity.getDownTransitions();
            //创建split节点和join节点
            BPMTransition nextTrans = (BPMTransition) downTransitions.get(0);
            BPMAndRouter split = null;
            BPMAndRouter join = null;
            String splitId = UUIDLong.longUUID() + "";
            String joinId = UUIDLong.longUUID() + "";
            split = new BPMAndRouter(splitId, "split");
            join = new BPMAndRouter(joinId, "join");
            split.setStartAnd(true);
            join.setStartAnd(false);
            String relevancyId = UUIDLong.longUUID() + "";
            split.setParallelismNodeId(relevancyId);
            join.setParallelismNodeId(relevancyId);

            process.addChild(split);
            process.addChild(join);

            //建立当前节点与split节点、join节点与下一节点之间的关系，并删除旧的关系
            BPMAbstractNode nextNode = (BPMAbstractNode) ((BPMTransition) downTransitions.get(0)).getTo();
            BPMTransition trans1 = new BPMTransition(currentActivity, split);
            BPMTransition trans2 = new BPMTransition(join, nextNode);

            copyCondition(nextTrans, trans2);

            process.addLink(trans1);
            process.addLink(trans2);

            process.removeLink(nextTrans);
            currentActivity.removeDownTransition(nextTrans);
            nextTrans.getTo().removeUpTransition(nextTrans);

            //建立将要被加签的节点与join、split节点之间的关系
            int ilen = nodeList.size()-1;
            for (int i = ilen; i >=0; i--) {
                BPMHumenActivity userNode = nodeList.get(i);
                BPMTransition userLink1 = new BPMTransition(split, userNode);
                BPMTransition userLink2 = new BPMTransition(userNode, join);

                process.addChild(userNode);
                process.addLink(userLink1);
                process.addLink(userLink2);
            }
            return process;
        }catch(Throwable e){
            throw new BPMException(e.getMessage(), e);
        }
	}

	/**
	 * 
	 * @param process
	 * @param currentActivity
	 * @param nodeList
	 * @throws BPMException
	 */
	public static BPMProcess assignNode(BPMProcess process, BPMAbstractNode currentActivity,List<BPMHumenActivity> nodeList) throws BPMException {
		try{
            //修改只读标记
            String fr = currentActivity.getSeeyonPolicy().getFR();
            if("1".equals(fr)){
            	//如果当前节点是只读，那么当前会签的节点都弄成只读
	            for(BPMHumenActivity node : nodeList){
	            	node.getSeeyonPolicy().setFR("1");
	            }
            }
            @SuppressWarnings("rawtypes")
            List links_ba = currentActivity.getUpTransitions();
            @SuppressWarnings("rawtypes")
            List links_ac = currentActivity.getDownTransitions();
            BPMTransition link_ba = (BPMTransition) links_ba.get(0);
            BPMTransition link_ac = (BPMTransition) links_ac.get(0);
            BPMAbstractNode b = link_ba.getFrom();//a是当前节点，b是父节点 ，c是子节点
            BPMAbstractNode c = link_ac.getTo();
            boolean bIsSplit = (b instanceof BPMAndRouter) && ((BPMAndRouter) b).isStartAnd();
            boolean cIsJoin = (c instanceof BPMAndRouter) && !((BPMAndRouter) c).isStartAnd();

            BPMAndRouter split = null;
            BPMAndRouter join = null;

            if (bIsSplit && cIsJoin) {
                //如果当前节点子节点和父节点分别是join、split节点，那么沿用该join节点和split节点
                split = (BPMAndRouter) b;
                join = (BPMAndRouter) c;
            } else {
                //如果不是if里面的情况，那么创建join节点和split节点
                String splitId = UUIDLong.longUUID() + "";
                String joinId = UUIDLong.longUUID() + "";
                split = new BPMAndRouter(splitId, "split");
                join = new BPMAndRouter(joinId, "join");
                String relevancyId = UUIDLong.longUUID() + "";
                split.setParallelismNodeId(relevancyId);
                join.setParallelismNodeId(relevancyId);
                split.setStartAnd(true);
                join.setStartAnd(false);
                process.addChild(split);
                process.addChild(join);

                //建议join节点与当前节点的父节点之间的关系
                BPMTransition link_b_split = new BPMTransition(b, split);
                process.addLink(link_b_split);
                //建立split节点与当前节点子节点之间的关系
                BPMTransition link_join_c = new BPMTransition(join, c);
                copyCondition(link_ac, link_join_c);
                process.addLink(link_join_c);
                
                //删除当前节点和父节点的关系，建立当前节点和split节点之间的关系
                for (int i = currentActivity.getUpTransitions().size() - 1; i >= 0; i--) {
                    BPMTransition b_a = (BPMTransition) currentActivity.getUpTransitions().get(i);
                    process.removeLink(b_a);
                    b_a.getFrom().removeDownTransition(b_a);
                    b_a.getTo().removeUpTransition(b_a);
                    BPMTransition link_split_a = new BPMTransition(split, b_a.getTo());
                    //复制分支条件
                    copyCondition(link_ba, link_split_a);
                    process.addLink(link_split_a);
                }

                //删除当前节点和子节点的关系，建立当前节点和join节点之间的关系
                for (int i = currentActivity.getDownTransitions().size() - 1; i >= 0; i--) {
                    BPMTransition a_c = (BPMTransition) currentActivity.getDownTransitions().get(i);
                    process.removeLink(a_c);
                    a_c.getFrom().removeDownTransition(a_c);
                    a_c.getTo().removeUpTransition(a_c);
                    BPMTransition link_a_join = new BPMTransition(currentActivity, join);
                    process.addLink(link_a_join);
                }
            }
            //下面的逻辑和A8原有逻辑不相同，A8中公文和协同会签时分别有不同的节点权限，现在修改为通通自己传进来
            //建立会签的节点和split和join节点之间的关系
            int ilen= nodeList.size()-1;
            for (int i=ilen;i>=0;i--) {
                BPMHumenActivity d= nodeList.get(i);
                if (d != null) {
                    BPMTransition link_split_d = new BPMTransition(split, d);
                    BPMTransition link_d_join = new BPMTransition(d, join);
                    //如果是会签的话才需要这些操作
                    BPMSeeyonPolicy seeyonPolicy = d.getSeeyonPolicy();
                    if (d != null && !"competition".equals(seeyonPolicy.getProcessMode())) {
                        seeyonPolicy.setProcessMode("all");
                    }
                    //复制分支条件
                    copyCondition(link_ba, link_split_d);
                    process.addLink(link_split_d);
                    process.addLink(link_d_join);
                    process.addChild(d);
                }
            }
            return process;
        }catch(Throwable e){
            throw new BPMException(e.getMessage(),e);
        }
	}

}
