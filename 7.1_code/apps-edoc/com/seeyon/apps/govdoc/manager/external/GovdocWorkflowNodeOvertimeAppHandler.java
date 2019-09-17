/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.govdoc.manager.external;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt3Enum;
import com.seeyon.apps.collaboration.event.CollaborationProcessEvent;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.event.EdocFinishEvent;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.quartz.WorkflowNodeOvertimeAppHandler;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldComBean;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.ctp.workflow.wapi.PopResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.util.EdocInfo;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.MessageCommentParam;
import com.seeyon.v3x.edoc.workflow.event.EdocWorkflowEventListener;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import www.seeyon.com.utils.UUIDUtil;

/**
 * @author 
 *
 */
public class GovdocWorkflowNodeOvertimeAppHandler extends WorkflowNodeOvertimeAppHandler{
	private final static Log LOGGER = LogFactory.getLog(GovdocWorkflowNodeOvertimeAppHandler.class);

	public enum NodeOverTimeAutoRunCheckCode {
		normal, // 正常
		specialback, // 指定回退
		mainInSpecialback,
		flowexecute, // 流程决定分支走向的人
		isPreNewFlowFinish,//前面节点触发的子流程是否已经结束
		hasNewFlowOrHasBranchSelectPeople,//是否有子流程或者分支选人。
		hasNewFlow,//邦定了子流程
		hasBranchSelectPeople,//有分支选人
		exchangenode, // 交换类型
		opinionMustWrite,//意见必填
		checkDee,//校验dee任务
		allOtherFalse  //其他所有不能节点超期跳过的情况。
	}
	
	private GovdocLogManager govdocLogManager;
	private GovdocPubManager govdocPubManager;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocFormManager govdocFormManager;
	
	private UserMessageManager userMessageManager;
	private AffairManager affairManager;
	private OrgManager orgManager;
	private WorkflowApiManager wapi;
	private TemplateManager templateManager;
	private CollaborationApi collaborationApi;
	private PermissionManager permissionManager;
	private SuperviseManager superviseManager;
	private EnumManager enumManagerNew;
	private WorkTimeManager workTimeManager;
	private EdocStatManager edocStatManager;
	private CommentManager ctpCommentManager;
	private CAPFormManager capFormManager;
	private ConfigManager configManager;
	
	
	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}
	
	@Override
	public void transOverTimeProcessing(Long affairId, Long isAdvanceRemind,String hasExecuteActivity) throws BusinessException {

		LOGGER.info("QuartzManagerImpl.transNodeOverTimeQuartz,affairId:"+affairId+",isAdvanceRemind:"+isAdvanceRemind);
		String matchRequestToken= String.valueOf(UUIDLong.longUUID());
		try{
			CtpAffair affair = new CtpAffair();
			try {
				affair = affairManager.get(affairId);
			} catch (BusinessException e2) {
				LOGGER.error(e2.getMessage(),e2);
			}
			if(null!=affair && affair.getCompleteTime() == null){
				try{
					if(!AffairUtil.isAfffairValid(affair)){
						LOGGER.info("公文定时调度任务判断事项不可用，直接中断，affairId:"+affair.getId()+",应用ID："+affair.getObjectId());
						return;
					}
				}catch(Exception e){
					LOGGER.error("验证待办事项有效性时报错："+e,e);
				}


				EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId(), false);

				String[] links = this.getMessgaeLink(affair);
				String messageLink= links[1];
				String messageLinkdone= links[0];

				String key = null;
				if(Long.valueOf(0).equals(isAdvanceRemind)){
					key = "node.affair.advanceRemind.edoc"; //您有《{0}》事項需要處理！
				}else if(Long.valueOf(1).equals(isAdvanceRemind)){
					key = "node.affair.overTerm.edoc";   //《{0}》事項已超期！
				}
				Long memberId = affair.getMemberId();
				Long sendId = affair.getSenderId();
				V3xOrgMember currentMember= null;
				try {
					currentMember= orgManager.getMemberById(memberId);
				} catch (Throwable e) {
					LOGGER.warn(e.getMessage(),e);
					return;
				}

				ApplicationCategoryEnum appEnum  = ApplicationCategoryEnum.edoc;

				if(Long.valueOf(1).equals(isAdvanceRemind)){
					try{
						boolean isOvertopTime = affair.isCoverTime();
						if(!isOvertopTime){
							isOvertopTime = true;
							affair.setCoverTime(isOvertopTime);
							affairManager.updateAffair(affair);//更新事项处理状态
						}
					}catch(Throwable e1){
						LOGGER.error(e1.getMessage(),e1);
						return;
					}
					int dealTermType = affair.getDealTermType()==null ? 0 : affair.getDealTermType();
					if(dealTermType != 0) {
						BPMProcess bpmProcess= null;
						BPMActivity bpmActivity= null;
						BPMSeeyonPolicy bpmSeeyonPolicy= null;
						String _nodePolicy= null;
						String processId= null;
						String colSubject= "";
						try {
							colSubject= summary.getSubject();
							processId = summary.getProcessId();

							bpmProcess= wapi.getBPMProcess(processId);
							if(bpmProcess != null){
								bpmActivity= bpmProcess.getActivityById(affair.getActivityId().toString());
							}
							if(bpmActivity != null){
								bpmSeeyonPolicy= bpmActivity.getSeeyonPolicy();
							}
							_nodePolicy = affair.getNodePolicy();
						} catch (Throwable e) {
							LOGGER.warn(e.getMessage(),e);
							return;
						}

						/* boolean needStep = isInformNeedStep(_nodePolicy, bpmSeeyonPolicy);*/

						if( affair.getCompleteTime() == null){//不是知会节点，且获得了锁

							if( dealTermType == 1  && null != bpmProcess ){//1-转给指定人
								if(affair.getDealTermUserid().longValue()!=affair.getMemberId().longValue() && affair.getDealTermUserid().longValue()!=-2){
									//但转给人和被转给人不能是同一个人，否则不进行转给操作
									currentMember= orgManager.getMemberById(memberId);
									V3xOrgAccount currentOrgAccount= orgManager.getAccountById(currentMember.getOrgAccountId());
									List<V3xOrgMember> nextMembers= getMembers(processId, affair, currentMember, currentOrgAccount);
									Long dealTermUserId= affair.getDealTermUserid();
									V3xOrgMember nextMember= null;
									if(null!=nextMembers && !nextMembers.isEmpty()){//具体人员
										nextMember= nextMembers.get(0);
									}

									User user = new User();
									user.setId(currentMember.getId());
									user.setDepartmentId(currentMember.getOrgDepartmentId());
									user.setLoginAccount(currentMember.getOrgAccountId());
									user.setLoginName(currentMember.getLoginName());
									user.setName(currentMember.getName());
									AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);


									if(null!=nextMember && nextMember.isValid()){//接替人员可用才行 
										this.doReplacement(nextMembers,currentMember,currentOrgAccount,
												affair,bpmProcess,colSubject,processId,ApplicationCategoryEnum.edoc,sendId,messageLink);

										//更新当前待办人
										this.updateCurrentNodesInfo(summary);

										return;
									}else{//模板中指定的接替人员及其代理人都不可用，则发个消息给该待办事项的人员和代理人
										String notkey1= "";
										String notkey2= "";

										notkey1= "node.affair.overTerm.sysautoreplace1.not";
										notkey2= "node.affair.overTerm.sysautoreplace1.edoc.not";

										V3xOrgAccount nextOrgAccount= null;
										if(null==nextMember){
											notkey1 = notkey1+"1";
											if(Strings.isNotBlank(notkey2)){
												notkey2= notkey2+"1";
											}
										}else{
											nextOrgAccount= orgManager.getAccountById(nextMember.getOrgAccountId());
										}
										sendSysMessageForReplacement(currentMember,affair,nextMember,nextOrgAccount,messageLink,processId,appEnum,notkey1,notkey2);
										return;
									}
								}
							}else if( dealTermType== 2  && null!=bpmProcess ){//2-自动跳过
								//一、查询当前人员信息
								try {
									User user = new User();
									user.setId(currentMember.getId());
									user.setDepartmentId(currentMember.getOrgDepartmentId());
									user.setLoginAccount(currentMember.getOrgAccountId());
									user.setLoginName(currentMember.getLoginName());
									user.setName(currentMember.getName());
									AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);

									NodeAutoRunCaseBO nodeAutoRunCaseBO = this.canAutoRunCase(matchRequestToken,summary, affair,bpmSeeyonPolicy);
									NodeOverTimeAutoRunCheckCode checkCode = nodeAutoRunCaseBO.getCode();

									String notkey = "";

									key = "node.affair.overTerm.autoruncase.edoc";// 公文
									notkey = "node.affair.overTerm.autoruncase.edoc.not";

									if (NodeOverTimeAutoRunCheckCode.exchangenode.equals(checkCode)) {
										notkey = "node.affair.overTerm.autoruncase.edoc.not1";// 封发
									}


									boolean isFinishSuccess = true;
									if (NodeOverTimeAutoRunCheckCode.normal.equals(checkCode)) {// 能正常跳过

										String flow = nodeAutoRunCaseBO.getFlow();
										String dynamicFormMasterIds = nodeAutoRunCaseBO.getDynamicFormMasterIds();

										NodeOverTimeAutoRunCheckCode finishCheckCode = this.transSystemAutoRunCase(matchRequestToken,summary, affair, 
												bpmSeeyonPolicy, bpmActivity,user, flow);
										//成功处理
										if(finishCheckCode == null || NodeOverTimeAutoRunCheckCode.normal.equals(finishCheckCode)){
											String isStrongValidate = nodeAutoRunCaseBO.getIsStrongValidate();
											if("false".equals(isStrongValidate)){//非强制校验不通过记录日志
												wapi.saveMatchProcessLog(4,matchRequestToken, processId, bpmActivity, user, currentMember.getName());
											}else{
												if(bpmActivity != null){
													govdocLogManager.insertProcessLog(user, Long.parseLong(processId),
															Long.parseLong(bpmActivity.getId()),
															ProcessLogAction.processColl_SysAuto, currentMember.getName());
												}

											}
											if(bpmActivity != null){
												govdocLogManager.insertAppLog(user, GovdocAppLogAction.COLL_FLOW_NODE_RUNCASE_AUTOSYS.key(), colSubject, bpmActivity.getName(), user.getName());
											}
											// 三、给被跳过人发个消息提醒
											sendSysMessageForAutoRunCase(affair, messageLinkdone, processId, appEnum,
													currentMember, key);

											//给督办人发送消息
											sendMsgToSupervisor4AutoRunCase(affair, appEnum);
										}
										else{
											isFinishSuccess  = false;
										}
									} 
									else{
										isFinishSuccess  = false;
									}
									if(!isFinishSuccess){

										sendSysMessageForAutoRunCase(affair, messageLink, processId, appEnum,currentMember, notkey);
										wapi.saveMatchProcessLog(2,matchRequestToken, processId, bpmActivity, user, currentMember.getName());
									}
									return;
								} catch (Throwable e1) {
									LOGGER.warn(e1.getMessage(), e1);
									return;
								}
							}
						}
					} else {//dealTermType==0 仅消息提醒

						BPMProcess bpmProcess= null;
						BPMActivity bpmActivity= null;
						BPMSeeyonPolicy bpmSeeyonPolicy= null;
						String processId= null;
						try {
							processId = summary.getProcessId();

							bpmProcess= wapi.getBPMProcess(processId);
							if(bpmProcess != null){
								bpmActivity= bpmProcess.getActivityById(affair.getActivityId().toString());
							}
							if(bpmActivity != null){
								bpmSeeyonPolicy= bpmActivity.getSeeyonPolicy();
							}
						} catch (Throwable e) {
							LOGGER.warn(e.getMessage(),e);
							return;
						}

						//节点设置了超期后多次提醒
						if(bpmSeeyonPolicy != null && 
								Strings.isNotBlank(bpmSeeyonPolicy.getCycleRemindTime()) && !"0".equals(bpmSeeyonPolicy.getCycleRemindTime())
								&& !"null".equals(bpmSeeyonPolicy.getCycleRemindTime())) {

							//节点期限已到
							if(affair.getExpectedProcessTime()!=null && affair.getExpectedProcessTime().getTime()<=new Date().getTime()) {
								String jobName = "CycleRemind_" + affair.getObjectId() + "_" + affair.getActivityId(); 
								if(!QuartzHolder.hasQuartzJob(jobName, jobName)) {
									LOGGER.info("创建节点超期后多次提醒定时任务：" + DateUtil.get19DateAndTime() + " subject=" + affair.getSubject() + " jobName=" + jobName);
									Date startTime = affair.getExpectedProcessTime();
									//（自定义超期时间才做些处理）因超期时已发送了超期消息，故定时器开始时间是在超期后
									if(!"0".equals(String.valueOf(affair.getDeadlineDate()))) {
										startTime = Datetimes.addMinute(startTime, Integer.parseInt(bpmSeeyonPolicy.getCycleRemindTime()));
									}
									Map<String, String> datamap = new HashMap<String, String>(2);
									datamap.put("objectId", String.valueOf(affair.getObjectId()));
									datamap.put("activityId", String.valueOf(affair.getActivityId()));
									datamap.put("cycleRemindTimeMinutes", bpmSeeyonPolicy.getCycleRemindTime());
									try {
										QuartzHolder.newQuartzJob(jobName,startTime , "govdocNodeOverTimeCycleRemindJob", datamap);
									} catch(Exception e) {
										LOGGER.error("创建节点超期后多次提醒定时任务出错 affairId="+affairId + " 报错信息：" + e);
									}
								}
							}
						}

					}
				}
				//待办事项给督办发消息， 消息改造，只做协同
				if(Long.valueOf(1).equals(isAdvanceRemind)){
					sendMsgToSupervisor4Overtime(affair, appEnum, key);
				}
				sendSysMessageForAutoRunCase(affair,messageLink,null,appEnum,currentMember,key);
			}else{
				LOGGER.info("该待办已处理，完成时间不为空，不需要执行处理期限到操作。affairId:="+affairId);
			}
		}finally{
			wapi.removeAllWorkflowMatchLogCache(matchRequestToken);
			DBAgent.commit();
		}
	}

	/**
	 * 找人
	 * @param processId
	 * @param affair
	 * @param currentMember
	 * @param currentOrgAccount
	 * @return
	 */
	private List<V3xOrgMember> getMembers(String processId,CtpAffair affair,V3xOrgMember currentMember,V3xOrgAccount currentOrgAccount) {
		List<V3xOrgMember> nextMembers= new ArrayList<V3xOrgMember>();
		Long dealTermUserId= affair.getDealTermUserid();
		if(dealTermUserId!=-1){//具体人员
			V3xOrgMember nextMember= null;
			try {
				nextMember = orgManager.getMemberById(dealTermUserId);
				if(nextMember != null){
					nextMembers.add(nextMember);
				}
			} catch (Throwable e) {
				LOGGER.warn(e.getMessage(),e);
			}
		}else{//
			String activityId= affair.getActivityId().toString();
			nextMembers= wapi.getUserListForNodeReplace(processId,activityId,currentMember.getId(),currentOrgAccount.getId());
		}
		return nextMembers;
	}

	/**
	 * 节点替换- 给当前人和及代理人发系统提醒消息（指定给具体人员，释放锁资源）
	 * @param currentMember
	 * @param CtpAffair
	 * @param edocSummary
	 * @param nextMember
	 * @param nextOrgAccount
	 * @param messageLink
	 * @param processId
	 * @param appEnum
	 */
	private void sendSysMessageForReplacement(V3xOrgMember currentMember,
			CtpAffair ctpAffair,V3xOrgMember nextMember,
			V3xOrgAccount nextOrgAccount,String messageLink,String processId,
			ApplicationCategoryEnum appEnum,String colKey,String edocKey) {
		try {
			MessageContent msgContent = null;//代理人消息内容
			MessageReceiver msgReceiver = null;//代理人id
			MessageContent msgContent1 = null;//代理人消息内容
			MessageReceiver msgReceiver1= null;//代理人id
			String key= colKey;
			boolean isEdoc = ApplicationCategoryEnum.edoc.equals(appEnum)|| ApplicationCategoryEnum.edocRec.equals(appEnum)
					|| ApplicationCategoryEnum.edocSend.equals(appEnum) || ApplicationCategoryEnum.edocSign.equals(appEnum);

			if(isEdoc){
				key= edocKey;
			}
			if(null!=currentMember && currentMember.isValid()){//给本人发消息提醒
				if(null!=nextMember){
					msgContent = new MessageContent(key, ctpAffair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")").setImportantLevel(ctpAffair.getImportantLevel());
				}else{
					msgContent = new MessageContent(key, ctpAffair.getSubject()).setImportantLevel(ctpAffair.getImportantLevel());
				}
				msgReceiver = new MessageReceiver(ctpAffair.getId(), currentMember.getId(), messageLink, ctpAffair.getId().toString());
			}   
			//判断当前的代理人是否有效,给代理人发消息提醒
			Long agentId = WFComponentUtil.getAgentMemberId(ctpAffair.getTempleteId(), ctpAffair.getMemberId(), ctpAffair.getReceiveTime());
			if(null!= agentId){             
				V3xOrgMember currentMemberAgent= orgManager.getMemberById(agentId);
				if(null!=currentMemberAgent && currentMemberAgent.isValid()){//代理人员可用
					if(null!=nextMember){
						msgContent1 = new MessageContent(key, ctpAffair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")").setImportantLevel(ctpAffair.getImportantLevel()).add("col.agent");
					}else{
						msgContent1 = new MessageContent(key, ctpAffair.getSubject()).setImportantLevel(ctpAffair.getImportantLevel()).add("col.agent");
					}
					msgReceiver1 = new MessageReceiver(ctpAffair.getId(), currentMemberAgent.getId(), messageLink, ctpAffair.getId().toString());
				}
			}
			if(null!=msgReceiver){
				userMessageManager.sendSystemMessage(msgContent, appEnum, ctpAffair.getSenderId(), msgReceiver,ColMessageFilterEnum.overTime.key);
			}
			if(null!=msgReceiver1){
				userMessageManager.sendSystemMessage(msgContent1, appEnum, ctpAffair.getSenderId(), msgReceiver1,ColMessageFilterEnum.overTime.key);
			}
		}catch(Throwable e){
			LOGGER.error(e.getMessage(),e);
		}
	}



	/**
	 * 转给指定人：从currentMember转给nextMember
	 * @param nextMember
	 * @param nextOrgAccount
	 * @param currentMember
	 * @param currentOrgAccount
	 * @param CtpAffair
	 * @param bpmProcess
	 * @param edocSummary
	 * @param colSubject
	 * @param processId
	 * @param appEnum
	 * @param sendId
	 * @return
	 * @throws Exception
	 */
	private void doReplacement(
			List<V3xOrgMember> nextMembers1,
			V3xOrgMember currentMember,V3xOrgAccount currentOrgAccount,
			CtpAffair affair,BPMProcess bpmProcess,
			String colSubject,String processId,
			ApplicationCategoryEnum appEnum,Long sendId,String messageLink) {
		try{
			List<CtpAffair> affairs= affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
			List<V3xOrgMember> nextMembers= filterNextMembers(nextMembers1, affairs);
			if(null!=nextMembers && !nextMembers.isEmpty()){//找到人了
				Object[] result= wapi.replaceWorkItemMembers(true,affair.getMemberId(), processId, 
						affair.getSubObjectId(), affair.getActivityId().toString(), nextMembers,false);
				List<WorkItem> workitems= (List<WorkItem>)result[1];
				BPMHumenActivity bpmActivity= (BPMHumenActivity)result[2];
				List<CtpAffair> newAffairs= new ArrayList<CtpAffair>();

				for (int i=0;i<workitems.size();i++) {
					WorkItem workItem= workitems.get(i);

					CtpAffair newAffair = (CtpAffair)BeanUtils.cloneBean(affair);

					//一、替换v3x_affair中的member_id为dealTermUserId
					Long newPendingAffairMemberId = Long.parseLong(workItem.getPerformer());
					newAffair.setMemberId(newPendingAffairMemberId);
					newAffair.setId(UUIDLong.longUUID());
					newAffair.setCoverTime(false);
					newAffair.setSubObjectId(workItem.getId());
					newAffair.setReceiveTime(new Date());
					newAffair.setUpdateDate(new Date());
					newAffair.setDelete(false);
					newAffair.setDealTermUserid(-2l);//-2避免递归指定给某个人
					newAffair.setState(StateEnum.col_pending.getKey());
					newAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());
					newAffairs.add(newAffair);

					//bug OA-109611上一处理人：流程超期转给指定人，转后的人员列表里上一处理人显示的是上一节点的处理人，应该是转给我的人
					newAffair.setPreApprover(affair.getMemberId()); 

					newAffair.setOverWorktime(null);
					newAffair.setRunWorktime(null);
					newAffair.setOverTime(null);
					newAffair.setRunTime(null);
					V3xOrgMember nextMember= nextMembers.get(i);

					Long deadLine = affair.getDeadlineDate();
					Date createTime = newAffair.getReceiveTime();
					Date deadLineRunTime = null;
					try {
						if (deadLine != null && deadLine != 0){
							deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, nextMember.getOrgAccountId());
						}
					} catch (WorkTimeSetExecption e) {
						LOGGER.error("", e);
					}
					Long remindTime = affair.getRemindDate();
					Date advanceRemindTime = null;
					if (remindTime != null && remindTime != -1 && remindTime != 0 && deadLine > remindTime) {
						advanceRemindTime = workTimeManager.getRemindDate(deadLineRunTime, remindTime);
					}
					newAffair.setDealTermType(0);//仅消息提醒了
					newAffair.setExpectedProcessTime(deadLineRunTime);

					WFComponentUtil.affairExcuteRemind(newAffair, nextMember.getOrgAccountId(),deadLineRunTime,advanceRemindTime);

				}

				affairManager.saveAffairs(newAffairs);
				for (int i=0;i<newAffairs.size();i++) {
					CtpAffair newAffair= newAffairs.get(i);
					V3xOrgMember nextMember= nextMembers.get(i);
					sendMsgAndRecordLog( newAffair, nextMember,currentMember, currentOrgAccount, 
							messageLink,appEnum, affair, processId, bpmActivity, colSubject, sendId);
				}
			}else{
				BPMHumenActivity  bpmActivity= (BPMHumenActivity)bpmProcess.getActivityById(affair.getActivityId().toString());
				Map<Long,V3xOrgMember> memberIds= new HashMap<Long,V3xOrgMember>();
				for (V3xOrgMember nextMember : nextMembers1) {
					memberIds.put(nextMember.getId(),nextMember);
				}
				Set<Long> doneMemberIds= new HashSet<Long>();
				boolean hasPending= false;
				boolean hasDone= false;
				CtpAffair doneAffair= null;
				for(int i=0;i<affairs.size();i++){
					CtpAffair tempAffair= affairs.get(i);
					if(tempAffair.getId().longValue()!= affair.getId().longValue()){
						if(null!=memberIds.get(tempAffair.getMemberId()) && tempAffair.getState()==3){//有待办
							hasPending= true;
						}else if(null!=memberIds.get(tempAffair.getMemberId()) && tempAffair.getState()==4){//无待办
							hasDone=true;
							doneAffair= tempAffair;
							doneMemberIds.add(tempAffair.getMemberId());
						}
					}
				}
				if(!hasPending){//没有一个待办，则将原来的随机一个已办修改为待办
					doneAffair.setState(StateEnum.col_pending.getKey());
					doneAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());
					doneAffair.setFinish(false);
					doneAffair.setUpdateDate(new Date());
					affairManager.updateAffair(doneAffair);
					wapi.moveWorkitemHistoryToRun(doneAffair.getSubObjectId());
					V3xOrgMember nextMember= memberIds.get(doneAffair.getMemberId());
					//发一条提醒消息和记录流程日志
					sendMsgAndRecordLog( null, nextMember, currentMember, currentOrgAccount,
							messageLink, appEnum, affair, processId, bpmActivity, colSubject, sendId);
				}else{
					for (V3xOrgMember nextMember : nextMembers1) {
						if(!doneMemberIds.contains(nextMember.getId())){//将已办人员过滤掉
							//给每个待办人员发一条提醒消息和记录流程日志
							sendMsgAndRecordLog( null, nextMember, currentMember, currentOrgAccount,
									messageLink, appEnum, affair, processId, bpmActivity, colSubject, sendId);
						}
					}
				}

				wapi.replaceWorkItemMembers(false,affair.getMemberId(), processId, 
						affair.getSubObjectId(), affair.getActivityId().toString(), nextMembers1,false);
			}

			//更新当前待办人
			/*  
            if(Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(affair.getApp())){
            	WorkflowNodeOvertimeAppHandler handler = getSuperviseHandler(ApplicationCategoryEnum.collaboration);
            	if(handler != null){
            		handler.updateCurrentNodeInfos(affair.getObjectId(), newPendingAffairMemberId);
            	}
            }*/

			affair.setDelete(true);
			affair.setActivityId(-1l);
			affair.setSubObjectId(-1l);
			affair.setObjectId(-1l);
			affair.setTempleteId(-1l);
			affair.setMemberId(-1l);
			affairManager.updateAffair(affair); 





			super.getSession().flush();
		}catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	private void sendMsgAndRecordLog(CtpAffair newAffair,V3xOrgMember nextMember,
			V3xOrgMember currentMember,V3xOrgAccount currentOrgAccount,String messageLink,ApplicationCategoryEnum appEnum,
			CtpAffair affair,String processId,BPMHumenActivity bpmActivity,String colSubject,Long sendId) throws Exception{
		V3xOrgAccount nextOrgAccount= orgManager.getAccountById(nextMember.getOrgAccountId());
		//二、替换流程图中的:
		//1)<node/>标签的name属性
		//2)<actor/>标签的partyId为dealTermUserId，partyIdName为dealTermUserId对应的人员名称
		//  accountId为dealTermUserId对应的人员所在单位id
		//if("user".equals(partyType) && !"inform".equals(_nodePolicy) && !"zhihui".equals(_nodePolicy)){
		//需要替换流程中上述信息，否则不需要替换
		User user= new User();

		user.setId(currentMember.getId());
		user.setDepartmentId(currentMember.getOrgDepartmentId());
		user.setLoginAccount(currentMember.getOrgAccountId());
		user.setLoginName(currentMember.getLoginName());
		user.setName(currentMember.getName());

		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);

		boolean isEdoc = ApplicationCategoryEnum.edoc.equals(appEnum)|| ApplicationCategoryEnum.edocRec.equals(appEnum)
				|| ApplicationCategoryEnum.edocSend.equals(appEnum) || ApplicationCategoryEnum.edocSign.equals(appEnum);


		//三、给被替换人发个消息提醒:
		String key1= "node.affair.overTerm.sysautoreplace";
		if(isEdoc){
			key1= "node.affair.overTerm.sysautoreplace.edoc";
		}
		MessageContent msgContent1 = new MessageContent(key1, affair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")");
		MessageReceiver msgReceiver1 = new MessageReceiver(affair.getId(), currentMember.getId());
		//判断当前的代理人是否有效
		Long agentId = WFComponentUtil.getAgentMemberId(affair.getTempleteId(), currentMember.getId(), affair.getReceiveTime());
		MessageContent msgContent2= null;
		MessageReceiver msgReceiver2= null;
		if(null!= agentId){
			try {
				V3xOrgMember currentMemberAgent= orgManager.getMemberById(agentId);
				if(null!=currentMemberAgent && currentMemberAgent.isValid()){//代理人员可用
					String key2= "node.affair.overTerm.sysautoreplace.agent";
					if(isEdoc){
						key2= "node.affair.overTerm.sysautoreplace.edoc.agent";
					}
					msgContent2 = new MessageContent(key2, affair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")");
					msgReceiver2 = new MessageReceiver(affair.getId(), currentMemberAgent.getId());
				}
			} catch (Throwable e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		//五、写流程日志和应用日志
		govdocLogManager.insertProcessLog(user, Long.parseLong(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.replaceNode_SysAuto,currentOrgAccount.getShortName()+":"+currentMember.getName(),nextOrgAccount.getShortName()+":"+nextMember.getName());

		int appLogAction = 162;
		if(isEdoc){
			appLogAction = 350;
		}
		govdocLogManager.insertAppLog(user, appLogAction, colSubject,bpmActivity.getName(),currentMember.getName(),nextMember.getName());

		try {
			if(currentMember.isValid()){
				userMessageManager.sendSystemMessage(msgContent1, appEnum, sendId, msgReceiver1,ColMessageFilterEnum.overTime.key);
			}
			if( null!=msgContent2 && null!= msgReceiver2){//给代理人发消息
				userMessageManager.sendSystemMessage(msgContent2, appEnum, sendId, msgReceiver2,ColMessageFilterEnum.overTime.key);
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		if(null!=newAffair){
			try {
				//给替换人发个消息提醒:
				String key= "node.affair.overTerm.sysautoreplace1";
				if(isEdoc){
					key= "node.affair.overTerm.sysautoreplace1.edoc";
				}
				MessageContent msgContent3 = new MessageContent(key, newAffair.getSubject(),currentMember.getName()+"("+currentOrgAccount.getShortName()+")").setImportantLevel(newAffair.getImportantLevel());
				MessageReceiver msgReceiver3 = new MessageReceiver(newAffair.getId(), nextMember.getId(), messageLink, newAffair.getId().toString()); 
				Long nextMemberAgentId = MemberAgentBean.getInstance().getAgentMemberId(appEnum.key(), nextMember.getId());
				MessageContent msgContent4 = null;
				MessageReceiver msgReceiver4= null;
				if(null!= nextMemberAgentId){
					try {
						V3xOrgMember nextMemberAgent= orgManager.getMemberById(nextMemberAgentId);
						if(null!=nextMemberAgent && nextMemberAgent.isValid()){//代理人员可用
							String key2= "node.affair.overTerm.sysautoreplace1.agent";
							if(isEdoc){
								key2= "node.affair.overTerm.sysautoreplace1.edoc.agent";
							}
							msgContent4 = new MessageContent(key2, 
									newAffair.getSubject(),
									currentMember.getName()+"("+currentOrgAccount.getShortName()+")",
									nextMember.getName()+"("+nextOrgAccount.getShortName()+")",
									nextMember.getName()+"("+nextOrgAccount.getShortName()+")").setImportantLevel(newAffair.getImportantLevel());
							msgReceiver4 = new MessageReceiver(newAffair.getId(), nextMemberAgent.getId(), messageLink, newAffair.getId().toString()); 
						}
					} catch (Throwable e) {
						LOGGER.warn(e.getMessage(), e);
					}
				}
				if(nextMember.isValid()){
					userMessageManager.sendSystemMessage(msgContent3, appEnum, sendId, msgReceiver3,ColMessageFilterEnum.overTime.key);
				}
				if( null!=msgContent4 && null!= msgReceiver4){//给代理人发消息
					userMessageManager.sendSystemMessage(msgContent4, appEnum, sendId, msgReceiver4,ColMessageFilterEnum.overTime.key);
				}
			} catch (Throwable e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}


		if(Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(affair.getApp())){
			//给督办人员发送消息， 消息改造， 只做协同
			CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(affair.getObjectId());
			if (superviseDetail != null) {
				List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
				Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
				for (CtpSupervisor colSupervisor : colSupervisorSet) {
					Long colSupervisMemberId = colSupervisor.getSupervisorId();
					receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId));
				}
				if(Strings.isNotEmpty(receivers)){
					String memberName = orgManager.getMemberById(affair.getMemberId()).getName();
					MessageContent msgContent = new MessageContent("node.affair.overTerm.sysautoreplace2", affair.getSubject(), nextMember.getName()+"("+nextOrgAccount.getShortName()+")", memberName).setImportantLevel(affair.getImportantLevel());
					userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers,ColMessageFilterEnum.supervise.key);
				}
			}
		}
	}

	/**
	 * 过滤重复人员ID
	 * @param nextMembers
	 * @param affair
	 * @return
	 */
	private List<V3xOrgMember> filterNextMembers(List<V3xOrgMember> nextMembers,List<CtpAffair> affairs) {
		List<V3xOrgMember> result= new ArrayList<V3xOrgMember>();
		if( null!=nextMembers && !nextMembers.isEmpty()){
			Set<Long> memberIds= new HashSet<Long>();
			String subject= "";
			if(null!=affairs && !affairs.isEmpty()){
				for (CtpAffair temp_affair : affairs) { 
					if(temp_affair.getState()==3 || temp_affair.getState()==4){//已办和待办的人员
						memberIds.add(temp_affair.getMemberId());
					}
					if(Strings.isBlank(subject)){
						subject= temp_affair.getSubject();
					}
				}
			}
			LOGGER.info(subject+";"+memberIds);
			for (V3xOrgMember v3xOrgMember : nextMembers) {
				if(!memberIds.contains(v3xOrgMember.getId())){//去掉在该节点上已有的人员节点
					result.add(v3xOrgMember);
				}
			}
		}
		return result;
	}

	/**
	 * 转给指定人：从currentMember转给nextMember
	 * @author xuqw
	 * 
	 * @param nextMember
	 * @param nextOrgAccount
	 * @param currentMember
	 * @param currentOrgAccount
	 * @param CtpAffair
	 * @param bpmProcess
	 * @param edocSummary
	 * @param colSubject
	 * @param processId
	 * @param appEnum
	 * @param sendId
	 * @param currentUserMsg 被替代人发送消息
	 * @param currentAgentUserMsg 被替代人代理发送消息
	 * @param nextUserMsg 替代人发送消息
	 * @return nextAgentUsermsg 替代人代理发送消息
	 * @throws Exception
	 */
	private void doReplacement4Info(
			List<V3xOrgMember> nextMembers1,
			V3xOrgMember currentMember,V3xOrgAccount currentOrgAccount,
			CtpAffair affair,BPMProcess bpmProcess,
			String colSubject,String processId,
			ApplicationCategoryEnum appEnum,Long sendId,String messageLink, 
			String currentUserMsg, String currentAgentUserMsg, String nextUserMsg, String nextAgentUsermsg) {
		try{
			List<CtpAffair> affairs= affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
			List<V3xOrgMember> nextMembers= filterNextMembers(nextMembers1, affairs);
			if(null!=nextMembers && !nextMembers.isEmpty()){//找到人了
				Object[] result= wapi.replaceWorkItemMembers(true,affair.getMemberId(), processId, 
						affair.getSubObjectId(), affair.getActivityId().toString(), nextMembers,false);
				List<WorkItem> workitems= (List<WorkItem>)result[1];
				BPMHumenActivity bpmActivity= (BPMHumenActivity)result[2];
				List<CtpAffair> newAffairs= new ArrayList<CtpAffair>();

				for (int i=0;i<workitems.size();i++) {
					WorkItem workItem= workitems.get(i);

					CtpAffair newAffair = (CtpAffair)BeanUtils.cloneBean(affair);

					//一、替换v3x_affair中的member_id为dealTermUserId
					newAffair.setMemberId(Long.parseLong(workItem.getPerformer()));
					newAffair.setId(UUIDLong.longUUID());
					newAffair.setSubObjectId(workItem.getId());
					newAffair.setCoverTime(false);
					newAffair.setReceiveTime(new Date());
					newAffair.setUpdateDate(new Date());
					newAffair.setDelete(false);
					newAffair.setDealTermType(null);//仅消息提醒了
					newAffair.setDealTermUserid(-2l);//-2避免递归指定给某个人
					newAffair.setState(StateEnum.col_pending.getKey());
					newAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());

					V3xOrgMember nextMember= nextMembers.get(i);
					Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
					Long deadLine = affair.getDeadlineDate();
					try {
						//超期时间为相对时间，重新设置流程超期时间
						if (deadLine != null && deadLine != 0){
							Date deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, nextMember.getOrgAccountId());
							newAffair.setExpectedProcessTime(deadLineRunTime);
						}
					} catch (WorkTimeSetExecption e) {
						LOGGER.error("", e);
					}

					WFComponentUtil.affairExcuteRemind(newAffair, nextMember.getOrgAccountId());

					newAffairs.add(newAffair);
				}
				affairManager.saveAffairs(newAffairs);
				for (int i=0;i<newAffairs.size();i++) {
					CtpAffair newAffair= newAffairs.get(i);
					V3xOrgMember nextMember= nextMembers.get(i);
					sendMsgAndRecordLog(newAffair, nextMember, processId, bpmActivity, colSubject, sendId,
							nextMembers, affair, appEnum, nextUserMsg, currentOrgAccount, currentMember, 
							currentUserMsg, currentAgentUserMsg, messageLink, nextAgentUsermsg);
				}
			}else{//只发消息和记录日志
				BPMHumenActivity  bpmActivity= (BPMHumenActivity)bpmProcess.getActivityById(affair.getActivityId().toString());
				Map<Long,V3xOrgMember> memberIds= new HashMap<Long,V3xOrgMember>();
				for (V3xOrgMember nextMember : nextMembers1) {
					memberIds.put(nextMember.getId(),nextMember);
				}
				Set<Long> doneMemberIds= new HashSet<Long>();
				boolean hasPending= false;
				boolean hasDone= false;
				CtpAffair doneAffair= null;
				for(int i=0;i<affairs.size();i++){
					CtpAffair tempAffair= affairs.get(i);
					if(tempAffair.getId().longValue()!= affair.getId().longValue()){
						if(null!=memberIds.get(tempAffair.getMemberId()) && tempAffair.getState()==3){//有待办
							hasPending= true;
						}else if(null!=memberIds.get(tempAffair.getMemberId()) && tempAffair.getState()==4){//无待办
							hasDone=true;
							doneAffair= tempAffair;
							doneMemberIds.add(tempAffair.getMemberId());
						}
					}
				}
				if(!hasPending){//没有一个待办，则将原来的随机一个已办修改为待办
					doneAffair.setState(StateEnum.col_pending.getKey());
					doneAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());
					doneAffair.setFinish(false);
					doneAffair.setUpdateDate(new Date());
					affairManager.updateAffair(doneAffair);
					wapi.moveWorkitemHistoryToRun(doneAffair.getSubObjectId());
					V3xOrgMember nextMember= memberIds.get(doneAffair.getMemberId());
					//发一条提醒消息和记录流程日志
					sendMsgAndRecordLog(null, nextMember, processId, bpmActivity, colSubject, sendId,
							nextMembers, affair, appEnum, nextUserMsg, currentOrgAccount, currentMember, 
							currentUserMsg, currentAgentUserMsg, messageLink, nextAgentUsermsg);
				}else{
					for (V3xOrgMember nextMember : nextMembers1) {
						if(!doneMemberIds.contains(nextMember.getId())){//将已办人员过滤掉
							//给每个待办人员发一条提醒消息和记录流程日志
							sendMsgAndRecordLog(null, nextMember, processId, bpmActivity, colSubject, sendId,
									nextMembers, affair, appEnum, nextUserMsg, currentOrgAccount, currentMember, 
									currentUserMsg, currentAgentUserMsg, messageLink, nextAgentUsermsg);
						}
					}
				}
				wapi.replaceWorkItemMembers(false,affair.getMemberId(), processId, 
						affair.getSubObjectId(), affair.getActivityId().toString(), nextMembers1,false);
			}
			affair.setDelete(true);
			affair.setActivityId(-1l);
			affair.setSubObjectId(-1l);
			affair.setObjectId(-1l);
			affair.setTempleteId(-1l);
			affair.setMemberId(-1l);
			affairManager.updateAffair(affair);
			super.getSession().flush();
		}catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	/**
	 * 发消息和记日志
	 * @param newAffair
	 * @param nextMember
	 * @param processId
	 * @param bpmActivity
	 * @param colSubject
	 * @param sendId
	 * @param nextMembers
	 * @param affair
	 * @param appEnum
	 * @param nextUserMsg
	 * @param currentOrgAccount
	 * @param currentMember
	 * @param currentUserMsg
	 * @param currentAgentUserMsg
	 * @param messageLink
	 * @param nextAgentUsermsg
	 * @throws Exception
	 */
	private void sendMsgAndRecordLog(CtpAffair newAffair,V3xOrgMember nextMember,String processId,BPMHumenActivity bpmActivity,
			String colSubject,Long sendId,List<V3xOrgMember> nextMembers,CtpAffair affair,
			ApplicationCategoryEnum appEnum,String nextUserMsg,V3xOrgAccount currentOrgAccount,
			V3xOrgMember currentMember,String currentUserMsg,String currentAgentUserMsg,
			String messageLink,String nextAgentUsermsg) throws Exception {
		V3xOrgAccount nextOrgAccount= orgManager.getAccountById(nextMember.getOrgAccountId());
		//二、替换流程图中的:
		//1)<node/>标签的name属性
		//2)<actor/>标签的partyId为dealTermUserId，partyIdName为dealTermUserId对应的人员名称
		//  accountId为dealTermUserId对应的人员所在单位id
		//if("user".equals(partyType) && !"inform".equals(_nodePolicy) && !"zhihui".equals(_nodePolicy)){
		//需要替换流程中上述信息，否则不需要替换
		User user= new User();
		//        user.setId(nextMember.getId());
		//        user.setDepartmentId(nextMember.getOrgDepartmentId());
		//        user.setLoginAccount(nextMember.getOrgAccountId());
		//        user.setLoginName(nextMember.getLoginName());
		//        user.setName(nextMember.getName());
		//        user.setLocale(new Locale("zh","CN"));

		user.setId(currentMember.getId());
		user.setDepartmentId(currentMember.getOrgDepartmentId());
		user.setLoginAccount(currentMember.getOrgAccountId());
		user.setLoginName(currentMember.getLoginName());
		user.setName(currentMember.getName());

		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);

		//三、给被替换人发个消息提醒:
		String key1= "node.affair.overTerm.sysautoreplace";
		if(Strings.isNotBlank(currentUserMsg)){
			key1= currentUserMsg;
		}
		MessageContent msgContent1 = new MessageContent(key1, affair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")");
		MessageReceiver msgReceiver1 = new MessageReceiver(affair.getId(), currentMember.getId());
		//判断当前的代理人是否有效
		Long agentId = WFComponentUtil.getAgentMemberId(affair.getTempleteId(), currentMember.getId(), affair.getReceiveTime());
		MessageContent msgContent2= null;
		MessageReceiver msgReceiver2= null;
		if(null!= agentId){
			try {
				V3xOrgMember currentMemberAgent= orgManager.getMemberById(agentId);
				if(null!=currentMemberAgent && currentMemberAgent.isValid()){//代理人员可用
					String key2= "node.affair.overTerm.sysautoreplace.agent";
					if(Strings.isNotBlank(currentAgentUserMsg)){
						key2= currentAgentUserMsg;
					}
					msgContent2 = new MessageContent(key2, affair.getSubject(),nextMember.getName()+"("+nextOrgAccount.getShortName()+")");
					msgReceiver2 = new MessageReceiver(affair.getId(), currentMemberAgent.getId());
				}
			} catch (Throwable e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		//五、写流程日志和应用日志
		govdocLogManager.insertProcessLog(user, Long.parseLong(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.replaceNode_SysAuto,currentOrgAccount.getShortName()+":"+currentMember.getName(),nextOrgAccount.getShortName()+":"+nextMember.getName());
		govdocLogManager.insertAppLog(user, GovdocAppLogAction.COLL_FLOW_NODE_DEADLINE_2_POPLE.key(), colSubject,bpmActivity.getName(),currentMember.getName(),nextMember.getName());
		try {
			if(currentMember.isValid()){
				userMessageManager.sendSystemMessage(msgContent1, appEnum, sendId, msgReceiver1,ColMessageFilterEnum.overTime.key);
			}
			if( null!=msgContent2 && null!= msgReceiver2){//给代理人发消息
				userMessageManager.sendSystemMessage(msgContent2, appEnum, sendId, msgReceiver2,ColMessageFilterEnum.overTime.key);
			}
		} catch (Throwable e) {
			LOGGER.warn(e.getMessage(), e);
		}
		if(null!=newAffair){
			try {
				//给替换人发个消息提醒:
				String key= "node.affair.overTerm.sysautoreplace1";
				if(Strings.isNotBlank(nextUserMsg)){
					key= nextUserMsg;
				}
				MessageContent msgContent3 = new MessageContent(key, newAffair.getSubject(),currentMember.getName()+"("+currentOrgAccount.getShortName()+")").setImportantLevel(newAffair.getImportantLevel());
				MessageReceiver msgReceiver3 = new MessageReceiver(newAffair.getId(), nextMember.getId(), messageLink, newAffair.getId().toString()); 
				Long nextMemberAgentId = MemberAgentBean.getInstance().getAgentMemberId(appEnum.key(), nextMember.getId());
				MessageContent msgContent4 = null;
				MessageReceiver msgReceiver4= null;
				if(null!= nextMemberAgentId){
					try {
						V3xOrgMember nextMemberAgent= orgManager.getMemberById(nextMemberAgentId);
						if(null!=nextMemberAgent && nextMemberAgent.isValid()){//代理人员可用
							String key2= "node.affair.overTerm.sysautoreplace1.agent";
							if(null!=nextAgentUsermsg){
								key2= nextAgentUsermsg;
							}
							msgContent4 = new MessageContent(key2, 
									newAffair.getSubject(),
									currentMember.getName()+"("+currentOrgAccount.getShortName()+")",
									nextMember.getName()+"("+nextOrgAccount.getShortName()+")",
									nextMember.getName()+"("+nextOrgAccount.getShortName()+")").setImportantLevel(newAffair.getImportantLevel());
							msgReceiver4 = new MessageReceiver(newAffair.getId(), nextMemberAgent.getId(), messageLink, newAffair.getId().toString()); 
						}
					} catch (Throwable e) {
						LOGGER.warn(e.getMessage(), e);
					}
				}
				if(nextMember.isValid()){
					userMessageManager.sendSystemMessage(msgContent3, appEnum, sendId, msgReceiver3,ColMessageFilterEnum.overTime.key);
				}
				if( null!=msgContent4 && null!= msgReceiver4){//给代理人发消息
					userMessageManager.sendSystemMessage(msgContent4, appEnum, sendId, msgReceiver4,ColMessageFilterEnum.overTime.key);
				}
			} catch (Throwable e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}




	/**
	 * @author xuqw
	 * @param summary
	 * @param affair
	 * @param user
	 * @return
	 */


	public void sendMsg(Long summaryId,Long opinionId,String commentCont) throws BusinessException {
		User user = AppContext.getCurrentUser();
		ColSummary summary = collaborationApi.getColSummary(summaryId);
		List<CtpAffair> affairList = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, summaryId);
		Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
		for(CtpAffair affair : affairList){
			Long memberId = affair.getMemberId();
			Long senderId = affair.getSenderId();
			if(memberId.intValue() == senderId.intValue()){
				continue;
			}
			if(affair.getState() == StateEnum.col_pending.key()){
				receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.col.pending", affair.getId(), opinionId));
			}else{
				receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.col.done.detail", affair.getId(), opinionId));
			}
		}
		Integer importantLevel = affairList.get(0).getImportantLevel();
		String forwardMemberId = affairList.get(0).getForwardMember();
		int forwardMemberFlag = 0;
		String forwardMember = null;
		if(Strings.isNotBlank(forwardMemberId)){
			try {
				forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
				forwardMemberFlag = 1;
			}
			catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(commentCont));
		try {
			userMessageManager.sendSystemMessage(new MessageContent("col.addnote", summary.getSubject(), user.getName(), forwardMemberFlag, forwardMember, opinionContent).setImportantLevel(importantLevel),
					ApplicationCategoryEnum.collaboration, user.getId(), receivers,ColMessageFilterEnum.overTime.key);
		} catch (Exception e) {
			LOGGER.error("发起人增加附言消息提醒失败", e);
			throw new BusinessException(e);
		}
	}


	/**
	 * 获取协同督办人员
	 * @param summaryId
	 * @param affairId
	 * @return
	 * @throws BusinessException
	 */
	private Set<MessageReceiver> getSupervisors(Long summaryId, Long affairId) throws BusinessException{
		CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
		Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
		if (superviseDetail != null) {
			List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
			for (CtpSupervisor colSupervisor : colSupervisorSet) {
				Long colSupervisMemberId = colSupervisor.getSupervisorId();
				receivers.add(new MessageReceiver(affairId, colSupervisMemberId));
			}
		}
		return receivers;
	}

	/**
	 * 节点超期给督办人发送消息
	 */
	private void sendMsgToSupervisor4AutoRunCase(CtpAffair affair, ApplicationCategoryEnum appEnum){

		try{
			String key = "";
			if (appEnum.key() == ApplicationCategoryEnum.collaboration.getKey()) {  //協同
				key = "node.affair.overTerm.autoruncase2";
			} else {//公文
				key = "node.affair.overTerm.autoruncase2.edoc";
			}

			Set<MessageReceiver> receivers = getSupervisors(affair.getObjectId(), affair.getId());
			if(Strings.isNotEmpty(receivers)){
				String memberName = orgManager.getMemberById(affair.getMemberId()).getName();
				MessageContent msgContent = new MessageContent(key, affair.getSubject(), memberName).setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(msgContent, appEnum, affair.getSenderId(), receivers,ColMessageFilterEnum.supervise.key);
			}
		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}finally{}
	}

	/**
	 * 超期给督办发消息
	 */
	private void sendMsgToSupervisor4Overtime(CtpAffair affair, ApplicationCategoryEnum appEnum, String key) {

		try{
			Set<MessageReceiver> receivers = getSupervisors(affair.getObjectId(), affair.getId());
			if(Strings.isNotEmpty(receivers)){
				//String memberName = orgManager.getMemberById(affair.getMemberId()).getName();
				MessageContent msgContent = new MessageContent(key, affair.getSubject()).setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(msgContent, appEnum, affair.getSenderId(), receivers,ColMessageFilterEnum.supervise.key);
			}
		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}finally{
		}
	}

	/**
	 * 自动跳过和仅消息提醒
	 * @param CtpAffair
	 * @param messageLink
	 * @param edocSummary
	 * @param processId
	 * @param appEnum
	 * @param currentMmber
	 * @param key
	 */
	private void sendSysMessageForAutoRunCase(CtpAffair ctpAffair,String messageLink, String processId,ApplicationCategoryEnum appEnum,
			V3xOrgMember currentMmber,String key) {
		try{

			//节点提前提醒时间
			Long remindDate = ctpAffair.getRemindDate();
			String enumLabel = "";
			if(remindDate != null){
				CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.common_remind_time,String.valueOf(remindDate));
				enumLabel = ResourceUtil.getString(cei.getLabel());
			}

			//同时给该事项办理人员发一条提醒消息?
			MessageContent msgContent = null;
			MessageReceiver msgReceiver = null;
			Long agentId = null;
			if( null!=currentMmber && currentMmber.isValid() ){
				msgContent = new MessageContent(key, ctpAffair.getSubject(), enumLabel).setImportantLevel(ctpAffair.getImportantLevel());
				msgReceiver = new MessageReceiver(ctpAffair.getId(), currentMmber.getId(), messageLink, ctpAffair.getId().toString());
				//判断当前的代理人是否有效
				agentId = WFComponentUtil.getAgentMemberId(ctpAffair.getTempleteId(), currentMmber.getId(), ctpAffair.getReceiveTime());
			}
			MessageContent msgContent1 = null;//代理人消息内容
			MessageReceiver msgReceiver1= null;//代理人id
			if(null!= agentId){             
				V3xOrgMember currentMemberAgent= orgManager.getMemberById(agentId);
				if(null!=currentMemberAgent && currentMemberAgent.isValid()){//代理人员可用
					msgContent1 = new MessageContent(key, ctpAffair.getSubject(), enumLabel).setImportantLevel(ctpAffair.getImportantLevel()).add("col.agent");
					msgReceiver1 = new MessageReceiver(ctpAffair.getId(), currentMemberAgent.getId(), messageLink, ctpAffair.getId().toString());
				}
			}
			if(null!=msgReceiver){
				userMessageManager.sendSystemMessage(msgContent, appEnum, ctpAffair.getSenderId(), msgReceiver,ColMessageFilterEnum.overTime.key);
			}
			if(null!=msgReceiver1){
				userMessageManager.sendSystemMessage(msgContent1, appEnum, ctpAffair.getSenderId(), msgReceiver1,ColMessageFilterEnum.overTime.key);
			}
		}catch(Throwable e){
			LOGGER.error(e.getMessage(), e);
		}finally{
			if( null!= processId){
				// releaseLockForQuartz(processId,ctpAffair.getObjectId(),edocSummary,ctpAffair);
			}
		}
	}

	private String[] getMessgaeLink(CtpAffair affair) throws BusinessException {
		String[] links = new String[2];
		if(GovdocUtil.isGovdocWf(affair.getApp(), affair.getSubApp())) {
			links[0] = "message.link.govdoc.done.detail";
			links[1] = "message.link.govdoc.pending";
		} else {
			links[0] = "message.link.edoc.done";
			links[1] = "message.link.edoc.pending";
		}
		return links;
	}

	private void updateCurrentNodesInfo(Object object) throws BusinessException {
		EdocSummary summary = (EdocSummary)object;
		try {
			if(summary.getGovdocType() != 0){
				com.seeyon.apps.govdoc.helper.GovdocHelper.updateCurrentNodesInfo(summary, true);
			}else{
				EdocHelper.updateCurrentNodesInfo(summary, true);
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	private NodeOverTimeAutoRunCheckCode transSystemAutoRunCase(String matchRequestToken,Object object, CtpAffair affair, BPMSeeyonPolicy bpmSeeyonPolicy,BPMActivity bpmActivity, 
			User user, String workflowJson) throws BusinessException {

		EdocSummary summary = (EdocSummary)object;

		EdocOpinion signOpinion = new EdocOpinion();
		signOpinion.isDeleteImmediate = false;
		signOpinion.affairIsTrack = false;
		signOpinion.setIsHidden(false);
		signOpinion.setIdIfNew();
		String content= ResourceUtil.getString("node.affair.overTerm.sysautoruncase.opinion");//ResourceBundleUtil.getString(com.seeyon.ctp.common.usermessage.Constants.DEFAULT_MESSAGE_RESOURCE, Locale.getDefault(), "node.affair.overTerm.sysautoruncase.opinion");
		//signOpinion.setContent(content);
		signOpinion.setNodeId(Long.parseLong(bpmActivity.getId()));
		signOpinion.setEdocSummary(summary);
		signOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
		signOpinion.setOpinionType(EdocOpinion.OpinionType.sysAutoSignOpinion.ordinal());
		signOpinion.setAttribute(EdocOpinion.OpinionType.sysAutoSignOpinion.ordinal());
		if(Strings.isBlank(signOpinion.getPolicy())){
			signOpinion.setPolicy(affair.getNodePolicy());
		}
		boolean upd=false;
		Map<String,Object> namedParameter = new HashMap<String,Object>();
		if("qianfa".equals(bpmSeeyonPolicy.getId())){//here!!!!
			String issuerName = user.getName();
			try{
				issuerName = orgManager.getMemberById(affair.getMemberId()).getName();
			}catch(Throwable e){
				LOGGER.error("查找人员错误", e);
			}
			if(Strings.isNotBlank(summary.getIssuer())){
				String separator = ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources", "common.separator.label");
				issuerName+=separator+summary.getIssuer();
			}
			summary.setIssuer(issuerName);
			try {
				edocStatManager.updateElement(summary);
			} catch (Exception e) {
				throw new BusinessException(e);
			}
			namedParameter.put("issuer", issuerName);
			//如果有多人签发，则取最后一个签发节点审批的时间为签发时间
			summary.setSigningDate(new Date(System.currentTimeMillis()));
			namedParameter.put("signingDate", new Date(System.currentTimeMillis()));
			if(summary.getHasArchive() && summary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()){
				govdocPubManager.setArchiveIdToAffairsAndSendMessages(summary,affair,true);
			}
			upd=true;
		}
		signOpinion.setCreateUserId(affair.getMemberId());
		if(upd) {
			govdocSummaryManager.update(summary.getId(), namedParameter);
		}
		Map<String, Object> data = new HashMap<String, Object>();
		Comment comment = new Comment();
		comment.setAffairId(null!=affair?affair.getId():null);
		comment.setHidden(false);
		comment.setId(com.seeyon.ctp.util.UUIDLong.longUUID());
		comment.setContent(content);
		comment.setPid(0L);
		comment.setModuleType(ModuleType.edoc.getKey());
		comment.setModuleId(summary.getId());
		comment.setCreateId(affair.getMemberId());
		comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
		comment.setExtAtt3("");
		comment.setCtype(Comment.CommentType.comment.getKey());
		Long workItemId = affair.getSubObjectId();
       // DateSharedWithWorkflowEngineThreadLocal.setFinishWorkitemOpinionId(signOpinion.getId(), signOpinion.getIsHidden(), content, 2,false);
		MessageCommentParam mc = new MessageCommentParam(signOpinion.getId(), signOpinion.getIsHidden(), content, 2,false);

		AffairData affairData= createEdocAffairData(summary,affair,user);
		try {
			//finishWorkItem("edoc",affair, data, workItemId,bpmActivity,summary,affairData,workflowJson);

			WorkflowBpmContext wfContext = new WorkflowBpmContext();
			wfContext.setCurrentUserId(affair.getMemberId().toString());
			wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
			wfContext.setCurrentWorkitemId(workItemId);
			wfContext.setBusinessData(EventDataContext.CTP_FORM_DATA, data);
			wfContext.setCurrentActivityId(bpmActivity.getId());
			wfContext.setSysAutoFinishFlag(true);
			wfContext.setBusinessData(EdocWorkflowEventListener.OPERATION_TYPE, EdocWorkflowEventListener.AUTOSKIP);
			wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.AUTOSKIP);
			wfContext.setAppName("edoc");
			wfContext.setAppObject(summary);
			wfContext.setConditionsOfNodes(workflowJson);
			wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
			wfContext.setBusinessData(EdocWorkflowEventListener.COMMENT_MESSAGE, mc);
			wfContext.setBusinessData("comment", comment);
			wfContext.setFormAppId(affair.getFormAppId().toString());
//			wfContext.setFormData(formData);
//			wfContext.setMastrid(mastrid);

			String[] result = wapi.transFinishWorkItem(wfContext);
			//((com.seeyon.ctp.common.content.comment.CommentManager)AppContext.getBean("ctpCommentManager")).insertComment(comment);
		} catch (Exception e) {
			throw new BusinessException(e);
		}

		//协同立方
		Timestamp now = new Timestamp(System.currentTimeMillis());
		EdocFinishEvent event = new EdocFinishEvent(this,affair.getMemberId(),now,affair);
		EventDispatcher.fireEvent(event);
		updateCurrentNodesInfo(summary);
		return NodeOverTimeAutoRunCheckCode.normal;
	}
	/*
    private  String[] finishWorkItem(String appName, CtpAffair affair, Map<String, Object> fieldDataBaseMap,
            Long workItemId, BPMActivity bpmActivity, Object summary, AffairData affairData, String condition_json)
                    throws Exception {

        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setCurrentUserId(affair.getMemberId().toString());
        wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
        wfContext.setCurrentWorkitemId(workItemId);
        wfContext.setBusinessData(EventDataContext.CTP_FORM_DATA, fieldDataBaseMap);
        wfContext.setCurrentActivityId(bpmActivity.getId());
        wfContext.setSysAutoFinishFlag(true);
        wfContext.setBusinessData("operationType", EdocWorkflowEventListener.AUTOSKIP);
        wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.AUTOSKIP);
        wfContext.setAppName(appName);
        wfContext.setAppObject(summary);
        wfContext.setConditionsOfNodes(condition_json);
        wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        String[] result = wapi.transFinishWorkItem(wfContext);

        return result;
    }
	 */
	private AffairData createEdocAffairData(EdocSummary summary,CtpAffair affair,User user) {
		EdocInfo info =new EdocInfo(); 
		info.setSummary(summary);
		AffairData affairData = new AffairData();
		affairData.setModuleId(summary.getId());
		affairData.setSender(summary.getStartUserId());
		affairData.setIsSendMessage(false); //是否发消息
		// affairData.setResentTime(info.getSummary().getResentTime());//如协同colsummary
		affairData.setState(StateEnum.col_pending.key());//事项状态 - 协同业务中3为待办
		affairData.setSubject(affair.getSubject());                      //如协同colsummary
		affairData.setSubState(SubStateEnum.col_pending_unRead.key());//事项子状态 协同业务中11为协同-待办-未读
		affairData.setSummaryAccountId(info.getSummary().getOrgAccountId());//如协同colsummary.orgAccountId
		affairData.setTemplateId(info.getSummary().getTempleteId());//如协同colsummary
		affairData.setModuleId(summary.getId());
		affairData.setMemberId(user.getId());
		if(Strings.isBlank(summary.getAttachments())){
			affairData.setIsHasAttachment(false);
		}else{
			affairData.setIsHasAttachment(true);
		}
		if (summary.getFirstBody()!=null) {
			String contentType = summary.getFirstBody().getContentType();
			if(Strings.isBlank(contentType)){
				contentType = "HTML";
			}
			affairData.setContentType(contentType);
		}else {
			affairData.setBodyType(summary.getBodyType());
		}
		if(summary.getEdocType() == 0){
			affairData.setModuleType(ApplicationCategoryEnum.edocSend.key());
		}else if(summary.getEdocType() == 1){
			affairData.setModuleType(ApplicationCategoryEnum.edocRec.key());
		}else if(summary.getEdocType() == 2){
			affairData.setModuleType(ApplicationCategoryEnum.edocSign.key());
		}
		if(Strings.isNotBlank(summary.getUrgentLevel())){
			affairData.setImportantLevel(Integer.parseInt(summary.getUrgentLevel()));
		}
		if (summary.getDeadline() != null && summary.getDeadline().intValue() > 0) {
			affairData.setProcessDeadline(summary.getDeadline());
		}
		return affairData;
	}

	public NodeAutoRunCaseBO canAutoRunCase(String matchRequestToken,Object object,CtpAffair affair,BPMSeeyonPolicy bpmSeeyonPolicy) throws BusinessException {

		List<String> cannotMsgList= new ArrayList<String>();
		String autoSkipNodeId= affair.getActivityId().toString();
		EdocSummary summary = (EdocSummary)object;

        String category = "";
        if(summary.getGovdocType() == null || 0 == summary.getGovdocType()){
        	category = EdocEnum.edocType.sendEdoc.name();
            if(Integer.valueOf(ApplicationCategoryEnum.edocRec.getKey()).equals(affair.getApp())){
                category = EdocEnum.edocType.recEdoc.name();
            }else if(Integer.valueOf(ApplicationCategoryEnum.edocSign.getKey()).equals(affair.getApp())){
                category = EdocEnum.edocType.signReport.name();
            }
        }else{
        	category = GovdocEnum.govdocTypeEnum.govdocSend.name();
        	if(ApplicationSubCategoryEnum.edoc_jiaohuan.getKey() == affair.getSubApp()){
        		category = GovdocEnum.govdocTypeEnum.govdocExchange.name();
        	}else if(ApplicationSubCategoryEnum.edoc_qianbao.getKey() == affair.getSubApp()){
        		category = GovdocEnum.govdocTypeEnum.govdocSign.name();
        	}else if(ApplicationSubCategoryEnum.edoc_shouwen.getKey() == affair.getSubApp()){
        		category = GovdocEnum.govdocTypeEnum.govdocRec.name();
        	}
        }
		TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
		long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);

		boolean isExchangeNode= wapi.isExchangeNode(ApplicationCategoryEnum.edoc.name(),category, affair.getNodePolicy(), flowPermAccout);
		boolean isSepcial = Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(affair.getSubState()) || Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState());


		NodeAutoRunCaseBO  nodeAutoRunCaseBO = new NodeAutoRunCaseBO();
		NodeOverTimeAutoRunCheckCode checkCode = NodeOverTimeAutoRunCheckCode.normal;
		nodeAutoRunCaseBO.setMatchRequestToken(matchRequestToken);
		if(isSepcial){
			checkCode = NodeOverTimeAutoRunCheckCode.specialback;
			String msg= WorkflowMatchLogMessageConstants.PROCESS_AFFAIR_IN_SPECIAL_STEPBACK;
			cannotMsgList.add(msg);
		}else if(isExchangeNode){//自定义节点含有交换类型的本次不处理

			LOGGER.warn("该公文待办为公文交换类型节点，不允许执行自动跳过操作。edocSummaryId:="+affair.getObjectId()+";affairId:="+affair.getId());

			checkCode = NodeOverTimeAutoRunCheckCode.exchangenode;
			String msg= WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_EXCHANGENODE;
			cannotMsgList.add(msg);
		}else{
			//知会节点超期-自动跳过，有流程分支或选人选项，忽略并自动跳转
			if(!"inform".equals(affair.getNodePolicy()) && !"zhihui".equals(affair.getNodePolicy())) {

				//判断当前处理人员是否为当前流程节点的最后一个处理人
				boolean isExecuteFinished= wapi.isExecuteFinished(summary.getProcessId(), affair.getSubObjectId());
				if(isExecuteFinished){
                	String formAppId = summary.getFormAppid() == null ? null : summary.getFormAppid().toString();
                    String masterId = summary.getFormRecordid() == null ? null : summary.getFormRecordid().toString();
                    //如果含有当前节点之后含有分支，则不允许跳过
                    PopResult pr = isPop(matchRequestToken,WorkflowMatchLogMessageConstants.step5,affair, summary.getProcessId(), summary.getCaseId(),summary.getStartUserId(),formAppId, masterId,"edoc");
					if("true".equals(pr.getPopResult())){
						LOGGER.warn("该公文待办后面节点需要进行分支匹配或选择执行人或人员不可用，不允许执行自动跳过操作。edocSummaryId:="+affair.getObjectId()+";  affairId:="+affair.getId());
						checkCode = NodeOverTimeAutoRunCheckCode.flowexecute;
						String msg= pr.getMsg();
						if(Strings.isNotBlank(msg)){
							cannotMsgList.add(msg);
						}
					}

					nodeAutoRunCaseBO.setFlow(pr.getConditionsOfNodes());
					nodeAutoRunCaseBO.setDynamicFormMasterIds(pr.getDynamicFormMasterIds());
				}

			}
		}



		try {
			Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary.getTempleteId(),
					summary.getOrgAccountId(), templateManager);
			String nodePermissionPolicy = affair.getNodePolicy();
			EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnum(summary.getEdocType());
			Permission permission = permissionManager.getPermission(edocTypeEnum.name(), nodePermissionPolicy,
					flowPermAccountId);
			if (permission != null) {
				NodePolicy policy = permission.getNodePolicy();
				Integer opinionPolicy = policy.getOpinionPolicy();
				if (Integer.valueOf(1).equals(opinionPolicy)) {
					LOGGER.info("意见必须填写,不能重复跳过，  affairId:=" + affair.getId());
					checkCode = NodeOverTimeAutoRunCheckCode.opinionMustWrite;
					String msg= WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_OPINIONMUSTWRITE;
					cannotMsgList.add(msg);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}

		nodeAutoRunCaseBO.setCode(checkCode);
		wapi.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step5, matchRequestToken, autoSkipNodeId, "", cannotMsgList); 
		return nodeAutoRunCaseBO;

	}

	public PopResult isPop(String matchRequestToken,String validateStep,CtpAffair affair, String processId, Long caseId,Long startUserId,String formAppId,String materId,String appName) throws BPMException {
		WorkflowBpmContext wfContext = new WorkflowBpmContext();
		wfContext.setProcessId(processId);
		wfContext.setCaseId(caseId);
		wfContext.setCurrentActivityId(String.valueOf(affair.getActivityId()));
		wfContext.setCurrentWorkitemId(affair.getSubObjectId());
		wfContext.setFormData(formAppId);
		wfContext.setMastrid(materId);
		wfContext.setAppName(appName);
		wfContext.setStartUserId(String.valueOf(startUserId));
		wfContext.setCurrentUserId(String.valueOf(AppContext.getCurrentUser().getId()));
		wfContext.setBussinessId(String.valueOf(affair.getObjectId()));
		wfContext.setSysAutoFinishFlag(true);
		wfContext.setIsValidate(true);
		wfContext.setMatchRequestToken(matchRequestToken);
		wfContext.setAutoSkipNodeId(affair.getActivityId().toString());
		wfContext.setValidateStep(validateStep);
		OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
		try {
			wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
		} catch (BusinessException e) {
			LOGGER.error("",e);
		}
		wapi = (WorkflowApiManager)AppContext.getBean("wapi");
		PopResult pr= wapi.isPop(wfContext);
		//LOGGER.info("r0:="+result[0]+";r1:="+result[1]+";r2:="+result[2] + "; dynamicFormMasterIds=" + result[3]);
		return pr;
	}

	public class NodeAutoRunCaseBO {
		private NodeOverTimeAutoRunCheckCode code;
		private String flow;
		private String dynamicFormMasterIds;
		private String matchRequestToken;
		private String isStrongValidate;

		public String getIsStrongValidate() {
			return isStrongValidate;
		}
		public void setIsStrongValidate(String isStrongValidate) {
			this.isStrongValidate = isStrongValidate;
		}
		public NodeOverTimeAutoRunCheckCode getCode() {
			return code;
		}
		public void setCode(NodeOverTimeAutoRunCheckCode code) {
			this.code = code;
		}
		public String getFlow() {
			return flow;
		}
		public void setFlow(String flow) {
			this.flow = flow;
		}
		public String getMatchRequestToken() {
			return matchRequestToken;
		}
		public void setMatchRequestToken(String matchRequestToken) {
			this.matchRequestToken = matchRequestToken;
		}
		public String getDynamicFormMasterIds() {
			return dynamicFormMasterIds;
		}
		public void setDynamicFormMasterIds(String dynamicFormMasterIds) {
			this.dynamicFormMasterIds = dynamicFormMasterIds;
		}


	}
	public void transRepeatAffairProcessing(Long affairId, String commentId, String _policyName,Map<String, String> parameters) throws BusinessException {
		CtpAffair affair;
		try {
			affair = affairManager.get(affairId);
			if (!Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(affair.getState())) {
				LOGGER.info("事项已经不需要重复跳过，状态值不符合,affairid:" + affair.getId());
				return;
			}
			EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
			Comment c = null;

			String mergeDealType = parameters.get("mergeDealType");
			String agentId = parameters.get("_skipAgentId");
			if (Strings.isNotBlank(commentId) && !"0".equals(commentId)) {
				Long _commentId = Long.valueOf(commentId);
				c = ctpCommentManager.getComment(_commentId);
			}
			else { // 任意节点重复跳过的时候取任意一个非空节点的意见。

				List<Comment> comments = new ArrayList<Comment>();
				if (Strings.isBlank(agentId) || "0".equals(agentId)) {
					comments = ctpCommentManager.findCommentsList(ModuleType.edoc,
							Comment.CommentType.comment, affair.getObjectId(), affair.getMemberId());
				} else {// 如果是因代理处理而跳过，查找代理处理的评论
					comments = ctpCommentManager.findCommentsList(ModuleType.edoc,
							Comment.CommentType.comment, affair.getObjectId(), Long.parseLong(agentId));
				}

				if (Strings.isNotEmpty(comments)) {
					for(Iterator<Comment>  it = comments.iterator();it.hasNext();){
						Comment comment = it.next();
						if (CommentExtAtt3Enum.zcdb.getI18nLabel().equals(comment.getExtAtt3())) {
							it.remove();
						}
						if (Strings.isNotBlank(comment.getContent())
								&& !"collaboration.dealAttitude.rollback".equals(comment.getExtAtt3())
								&& !"collaboration.dealAttitude.cancelProcess".equals(comment.getExtAtt3())) {
							c = comment;
							break;
						}
					}

					if(c == null){
						c = comments.get(0);
					}
				}
			}
			//后续节点不管前一节点是否是代理人处理。
			if(c!=null){
				c.setExtAtt2(null);
			}
			autoRepeatSkipDealByObject(affair, summary, c, _policyName,mergeDealType,agentId,parameters);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
	}

	private void autoRepeatSkipDealByObject(CtpAffair affair,EdocSummary summary,Comment c,String _policyName ,String mergeDealType,String agentId,Map<String,String> parameters) throws BPMException{
		String matchRequestToken= String.valueOf(UUIDLong.longUUID());
		try{
			V3xOrgMember currentMember = orgManager.getMemberById(affair.getMemberId());
			User user= new User();
			user.setId(currentMember.getId());
			user.setDepartmentId(currentMember.getOrgDepartmentId());
			user.setLoginAccount(currentMember.getOrgAccountId());
			user.setLoginName(currentMember.getLoginName());
			user.setName(currentMember.getName());
			//二、以该affair运行流程，使流程流转到下一节点
			AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);

			boolean _canAutoDeal = false;
			BPMProcess bpmProcess= wapi.getBPMProcess(summary.getProcessId());
			BPMActivity bpmActivity = null;
			BPMSeeyonPolicy bpmSeeyonPolicy = null;
			if(bpmProcess != null){
				bpmActivity = bpmProcess.getActivityById(affair.getActivityId().toString());
			}
			if(bpmActivity != null){
				bpmSeeyonPolicy= bpmActivity.getSeeyonPolicy();
			}
			String formAppId= summary.getFormAppid()==null?null:summary.getFormAppid().toString();
			String masterId= summary.getFormRecordid()==null?null:summary.getFormRecordid().toString();

			PopResult pr = new PopResult();
			if(!"inform".equals(affair.getNodePolicy()) && !"zhihui".equals(affair.getNodePolicy())){
				pr = isPop(matchRequestToken,WorkflowMatchLogMessageConstants.step6,affair,summary.getProcessId(), summary.getCaseId(), summary.getStartMemberId(),formAppId,masterId,"collaboration");  
			}

			try{
				_canAutoDeal = canRepeatSkipAutoDeal(matchRequestToken,affair, summary, bpmSeeyonPolicy, bpmActivity, pr,c,parameters);
			} catch (Exception e) {
				LOGGER.error("", e);
			}

			if(_canAutoDeal){
				LOGGER.info("重复跳过条件判断都满足，可以跳过，执行处理逻辑");
				try {
					NodeOverTimeAutoRunCheckCode code = sysAutoRunCase4RepeatSkip(matchRequestToken,summary, affair, c,bpmSeeyonPolicy, bpmActivity
							,_policyName,mergeDealType,agentId,pr);
					if(code!=null && !NodeOverTimeAutoRunCheckCode.normal.equals(code)){ //处理过程中有异常情况。
						updateAffairState4RepeatAffair(affair,summary.getForwardMember());
					}
				     //发送自动跳过的事件
			        GovdocHelper.fireAutoSkipEvent(this);
				} catch (Exception e) {
					LOGGER.error("", e);
					updateAffairState4RepeatAffair(affair,summary.getForwardMember());
				}
			}else{

				LOGGER.info("重复跳过条件判断不满足，不能可以跳过，恢复为待办状态");
				updateAffairState4RepeatAffair(affair,summary.getForwardMember());
				wapi.saveMatchProcessLog(3,matchRequestToken, summary.getProcessId(), bpmActivity, user, currentMember.getName());
			}
		}catch (Throwable e) {
			LOGGER.error("", e);
			updateAffairState4RepeatAffair(affair,summary.getForwardMember());
		}finally{
			wapi.removeAllWorkflowMatchLogCache(matchRequestToken);
		}
	}
	/**
	 * B节点是核定、新闻审批、公告审批、封发（带交换类型的节点）时，不能跳过。<br>
	 * B节点有必填项时不能自动跳过。<br>
	 * B节点后有手动分支和非强制分支和选人不能跳过。强制分支不需要手动选择的可以跳过。<br>
	 * B节点有子流程不能跳过。<br>
	 * 对于被加签人员与加签人员同一个人时，和下一节点需要选人但又选择了自己的情况，不支持跳过<br>
	 * B节点处理意见必填时不能跳过<br> -当B节点意见必填时上一节点也是同一个人而且填写了意见也可以跳过
	 * @throws BusinessException 
	 */
	private boolean canRepeatSkipAutoDeal(String matchRequestToken,CtpAffair affair, EdocSummary summary, BPMSeeyonPolicy bpmSeeyonPolicy,
			BPMActivity bpmActivity, PopResult pr,Comment c,Map<String,String> parameters) throws BusinessException {
		List<String> cannotMsgList= new ArrayList<String>();
		
		String autoSkipNodeId= affair.getActivityId().toString();
		boolean result= true;
		try{

			String resonI18n = "collaboration.cant.skip.reson.";
			if(!parameters.isEmpty() && parameters.containsKey("_cantSkipResonMap")){
				String _skipMap = parameters.get("_cantSkipResonMap");
				HashMap parseJSONString = JSONUtil.parseJSONString(_skipMap,HashMap.class);
				if(!parseJSONString.isEmpty() && Strings.isNotBlank((String)parseJSONString.get(affair.getId()))){
					resonI18n =  resonI18n + parseJSONString.get(affair.getId());
					result = false;
					cannotMsgList.add(ResourceUtil.getString(resonI18n));

					LOGGER.info("不能重复跳过:affairId:" + affair.getId()+",reson:"+resonI18n);
					return result;
				}
			}

			String processId = summary.getProcessId();
			boolean isSepcial = Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey())
					.equals(affair.getSubState())
					|| Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState())
					|| Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
			if (isSepcial) {
				LOGGER.info("指定回退不能重复跳过:affairId:" + affair.getId());
				String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.PROCESS_AFFAIR_IN_SPECIAL_STEPBACK);
				cannotMsgList.add(msg);
				result= false;
			}
			if ("vouch".equalsIgnoreCase(affair.getNodePolicy()) || "bulletionaudit".equals(affair.getNodePolicy())
					|| "newsaudit".equals(affair.getNodePolicy())// B节点是核定、新闻审批、公告审批、封发（带交换类型的节点）时，不能跳过。
					|| "faxing".equals(affair.getNodePolicy()))// B节点是公文 分送节点
			{
				LOGGER.info("节点权限不能重复跳过:affairId:" + affair.getId() + ",nodepolicy:" + affair.getNodePolicy());
				if("vouch".equalsIgnoreCase(affair.getNodePolicy())){
					String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_VOUCH);
					cannotMsgList.add(msg);
				}else if("bulletionaudit".equals(affair.getNodePolicy())){
					String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_BULLETIONAUDIT);
					cannotMsgList.add(msg);
				}else if("newsaudit".equals(affair.getNodePolicy())){
					String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_NEWSAUDIT);
					cannotMsgList.add(msg);
				}else if("faxing".equals(affair.getNodePolicy())){
					String msg= "节点权限为<font color='blue'>分送</font>";
		        	cannotMsgList.add(msg);
				}
				result= false;
			}
			// 前面节点触发的子流程是否已经结束
			boolean isPreNewFlowFinish = true;
			// B节点有必填项时不能自动跳过。
			if (GovdocUtil.isForm(affair.getBodyType())) {// 表单类型的数据
				/* Map<String, String> ret = checkBindEventBatch(affair, summary);
               if (!ret.isEmpty() && !"true".equals(ret.get("success"))) {// dee校验不通过，不能自动跳过
                   cannotMsgList.add(ret.get("msg"));
                   return false;
               }
               else {*/
				/*关于表单校验规则和表单必填项对流程运转的影响（魏俊华/吉大军/孙老）：
                   1、无人自动跳过 （仅表单必填）
                   2、连续节点合并处理（表单必填&意见必填）
                   3、超期节点自动跳过（表单必填&意见必填&表单强制校验不满足）
                   4、批处理（表单必填&表单强制校验不满足)*/

				boolean isAffairReadOnly = AffairUtil.isFormReadonly(affair);

				if (!"inform".equals(affair.getNodePolicy()) && !isAffairReadOnly) {

					Set<String> fields = capFormManager.getNotNullableFields(affair.getFormAppId(), affair.getMultiViewStr());
					LOGGER.info("[getNotNullableFields],入参：FormAppid():" + summary.getFormAppid() + ",MultiViewStr:" + affair.getMultiViewStr());
					if (Strings.isNotEmpty(fields)) {
						LOGGER.info("表单必须填写，不能重复跳过:affairId:" + affair.getId());
						String msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FORM_FIELD_MUSTWRITE) + Strings.join(fields, ",");
						cannotMsgList.add(msg);
						result = false;
					}
				}

				isPreNewFlowFinish = !wapi.hasUnFinishedNewflow(summary.getProcessId(), String.valueOf(affair.getActivityId()));
				if (!isPreNewFlowFinish) {
					LOGGER.info("表单触发的子流程没有结束，不能重复跳过:affairId:" + affair.getId());
					String msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB_UNFINISHED);
					cannotMsgList.add(msg);
					result = false;
				}
				// }

			}
			// 判断当前处理人员是否为当前流程节点的最后一个处理人,是决定流程走向的人
			boolean isExecuteFinished = wapi.isExecuteFinished(summary.getProcessId(), affair.getSubObjectId());
			boolean isFromTemplate = false;
			if (summary != null) {
				isFromTemplate = summary.getTempleteId() != null && summary.getTempleteId().longValue() != -1;
			}

			// 当前跳过节点是否有子流程
			boolean hasNewflow = isFromTemplate && bpmSeeyonPolicy != null && "1".equals(bpmSeeyonPolicy.getNF());

			// 当前跳过的节点后面是否有分支或者需要选人或者人员不可用
			if(hasNewflow){
				String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB);
				cannotMsgList.add(msg);
				result= false;
			}
			else if (isExecuteFinished &&  "true".equals(pr.getPopResult())) {
				LOGGER.info("决定流程走向，并且需要选人或者有子流程，不能重复跳过，  affairId:=" + affair.getId());
				String msg= pr.getMsg();
				if(Strings.isNotBlank(msg)){
					cannotMsgList.add(msg);
				}
				result= false;
			}
			String configItem = collaborationApi.getPolicyByAffair(affair).getId();
			String category = EnumNameEnum.col_flow_perm_policy.name();
			if(ApplicationSubCategoryEnum.edoc_fawen.getKey() == summary.getGovdocType()){
				category = EnumNameEnum.edoc_new_send_permission_policy.name();
			}else if(ApplicationSubCategoryEnum.edoc_shouwen.getKey() == summary.getGovdocType()){
				category = EnumNameEnum.edoc_new_rec_permission_policy.name();
			}else if(ApplicationSubCategoryEnum.edoc_qianbao.getKey() == summary.getGovdocType()){
				category = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
			}
			Permission permission = null;
			try {
				Long accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary);
				permission = permissionManager.getPermission(category, configItem, accountId);
				if (permission != null) {
					Integer opinionPolicy = permission.getNodePolicy().getOpinionPolicy();
					Integer disAgredOpinionPlicy = permission.getNodePolicy().getDisAgreeOpinionPolicy();
					boolean _disAgredOpinionPlicy = null != c &&  Integer.valueOf(1).equals(disAgredOpinionPlicy) && CommentExtAtt1Enum.disagree.getI18nLabel().equals(c.getExtAtt1()) ;
					//LOG.error("AFFAIR.ID:"+affair.getId()+",disAgredOpinionPlicy:"+disAgredOpinionPlicy+",c.id:"+c.getId()+",c.getExtAtt1():"+c.getExtAtt1()+",END_disAgredOpinionPlicy:"+_disAgredOpinionPlicy+",t:"+(CommentExtAtt1Enum.disagree.getI18nLabel().equals(c.getExtAtt1())));
					if (Integer.valueOf(1).equals(opinionPolicy) || _disAgredOpinionPlicy) {
						boolean skipFlag =false;
						LOGGER.info("当处理人为同一个连续处理人时，只要上一次的意见存在就重复跳过，  affairId:=" + affair.getId());
						if(c !=null &&  c.getCreateId().equals(affair.getMemberId())  && Strings.isNotEmpty(c.getContent())){
							skipFlag = true;
						}
						if(skipFlag == false){
							String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_OPINIONMUSTWRITE);
							cannotMsgList.add(msg);
							result= false;
						}


					}
				}
			} catch (Exception e) {
				String msg= "报错了："+e.getMessage();
				cannotMsgList.add(msg);
				result= false;
				LOGGER.error(msg,e);
			}

			boolean isCircle = this.isCircleNode(processId, affair.getActivityId().toString());
			if(isCircle){
				LOGGER.info("该公文待办存在环形分支，不能重复跳过，  affairId:=" + affair.getId());
				String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_LINK_CIRCLE);
				cannotMsgList.add(msg);
				result= false;
			}



			//          CanBackgroundDealParam param = new CanBackgroundDealParam();
			//          param.setAffair(affair);
			//          CanBackgroundDealResult cr =  ColSelfUtil.publicCheckCanBackgroundDeal(param);
			//          if(!cr.isCan()){
			//              LOGGER.info("不能重复跳过，  affairId:=" + affair.getId()+",msg:"+cr.getMsg());
			//              cannotMsgList.add(cr.getMsg());
			//              result= false;
			//          }

			return result;
		}finally{
			wapi.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step6, matchRequestToken, autoSkipNodeId, "", cannotMsgList); 
		}
	}
	private  NodeOverTimeAutoRunCheckCode sysAutoRunCase4RepeatSkip(String matchRequestToken,EdocSummary summary, 
			CtpAffair affair, Comment c, BPMSeeyonPolicy bpmSeeyo1nPolicy, BPMActivity bpmActivity, 
			String _policyName, 
			String mergeDealType, String agentId,PopResult pr) throws Exception {

		Comment comment = null;
		Permission permission = getPermissionByAffair(summary, affair);
		String defaultAttitude = permission.getNodePolicy().getDefaultAttitude();
		String attitude = permission.getNodePolicy().getAttitude();

		boolean isHasOpinion = permissionManager.isContainsAction(permission, "Opinion");
		Long needCloneBaseCommentId = 0l;
		
		CtpAffair oldCtpAffair = new CtpAffair();
		String configCategory =null;
		String configItem1 = affair.getNodePolicy();
		if(summary.getEdocType() == 0){
			configCategory = EnumNameEnum.edoc_new_send_permission_policy.toString();
		}else if(summary.getEdocType() == 1){
			configCategory = EnumNameEnum.edoc_new_rec_permission_policy.toString();
		}else if(summary.getEdocType() == 1){
			configCategory = EnumNameEnum.edoc_new_change_permission_policy.toString();
		}else if(summary.getEdocType() == 1){
			configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.toString();
		}
		ConfigItem configItem = configManager.getConfigItemFromDB(configCategory, configItem1,affair.getOrgAccountId());
		List<FormAuthViewFieldBean> formAuthViewFieldBeans = govdocFormManager.getEditFieldByFormAndPermission(summary.getFormId(), configItem.getId());
		if(formAuthViewFieldBeans != null && !formAuthViewFieldBeans.isEmpty()){
			List<CtpAffair> ctpAffairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc,affair.getObjectId(),affair.getMemberId());
			for(CtpAffair ctpAffair:ctpAffairs){
				if(ctpAffair.getState() == StateEnum.col_done.key()){
					oldCtpAffair = ctpAffair;
					break;
				}
			}
			EdocOpinion edocOpinion = null;
			List doneFieldInput = new ArrayList();
			if(oldCtpAffair.getId() != null){
				List<EdocOpinion> edocOpinions = govdocCommentManager.findEdocOpinionByAffairId(oldCtpAffair.getId());
				if(edocOpinions != null && !edocOpinions.isEmpty()){
					edocOpinion = edocOpinions.get(0);
					for(EdocOpinion doneEdocOpinion : edocOpinions){
						doneFieldInput.add(doneEdocOpinion.getPolicy());
					}
				}
			}
			FormApi4Cap3 formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
			FormDataMasterBean formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
			for(FormAuthViewFieldBean frmAuthViewFieldBean : formAuthViewFieldBeans){
				Object value = formDataMasterBean.getFieldValue(frmAuthViewFieldBean.getFieldName());
				if((frmAuthViewFieldBean.getIsNotNull() == 1) && (value == null || "".equals(value))){
					throw new Exception();
				}
				if(edocOpinion != null){
					if(FormFieldComBean.FormFieldComEnum.EDOCFLOWDEALOPITION.getKey().equals(frmAuthViewFieldBean.getFormFieldBean().getInputType())
							&& !doneFieldInput.contains(frmAuthViewFieldBean.getFormFieldBean().getName())){
						edocOpinion.setId(UUIDUtil.getUUIDLong());
						edocOpinion.setPolicy(frmAuthViewFieldBean.getFormFieldBean().getName());
						edocOpinion.setNodeId(affair.getActivityId());
						edocOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
						govdocCommentManager.saveOpinionPo(edocOpinion);
					}
				}
			}
		}
		// 拷贝原始意见
		if (c != null) {

			needCloneBaseCommentId = c.getId();

			comment = c;
			comment.setId(UUIDLong.longUUID());
			comment.setExtAtt3(CommentExtAtt3Enum.repeat_auto_skip.getI18nLabel());
			comment.setRelateInfo(null);
			comment.setAttachList(null);
			if (affair != null) {
				comment.setAffairId(affair.getId());
				comment.setCreateId(affair.getMemberId());
			}
			comment.setCreateDate(new Date());
			comment.setCtype(Comment.CommentType.comment.getKey());
			String _attitude = getDisplayAttitude(comment, false, defaultAttitude, attitude);
			comment.setExtAtt1(_attitude);

			comment.setPraiseNumber(0);
			comment.setPraiseToComment(false);
			comment.setPraiseMemberIds(null);

			if (!isHasOpinion) {
				comment.setContent(null);
				comment.setHidden(false);
				comment.setShowToId(null);
			}

		}
		else {
			comment = new Comment();
			comment.setHidden(false);
			comment.setId(UUIDLong.longUUID());
			// 意见设置为自动跳过

			String _attitude = getDisplayAttitude(comment, true, defaultAttitude, attitude);
			comment.setExtAtt1(_attitude);

			comment.setCtype(Comment.CommentType.comment.getKey());
			comment.setPid(0L);
			comment.setPraiseNumber(0);
			comment.setPraiseToComment(false);
			comment.setPraiseToComment(false);
			comment.setModuleType(ModuleType.edoc.getKey());
			comment.setModuleId(summary.getId());
			comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
			if (affair != null) {
				comment.setAffairId(affair.getId());
				comment.setCreateId(affair.getMemberId());
			}
			comment.setExtAtt3(CommentExtAtt3Enum.repeat_auto_skip.getI18nLabel());

			needCloneBaseCommentId = comment.getId();
		}
		//       ColUtil.addOneReplyCounts(summary);

		if (null == comment.getForwardCount()) {
			comment.setForwardCount(0);
		}
		//如果因代理人处理过而跳过处理，将意见处显示代理人姓名
		if (Strings.isNotBlank(agentId) && !"0".equals(agentId)) {
			V3xOrgMember member = orgManager.getMemberById(Long.parseLong(agentId));
			if (member != null) {
				comment.setExtAtt2(member.getName());
			}
		}


		NodeOverTimeAutoRunCheckCode code =colAutoRun(matchRequestToken,summary, affair, bpmActivity, 
				comment, needCloneBaseCommentId,
				true, _policyName,mergeDealType,pr,null);
		return code;
	}

	private boolean isCircleNode(String processId, String activityId) throws BPMException {

		boolean ret = false;
		try{
			BPMProcess process = wapi.getBPMProcess(processId);
			if(process != null) {
				BPMActivity myNode = process.getActivityById(activityId);
				if(myNode != null) {
					List<BPMCircleTransition> circleList = myNode.getDownCirlcleTransitions();
					if(Strings.isNotEmpty(circleList) &&circleList.size()>0){
						ret = true;
					}
				}else {
					LOGGER.info("myNode：null");
				}
			} else {
				LOGGER.info("process：null");
			}

		} catch (Exception e){
			LOGGER.error("环形分支判断出错processId:"+processId+",activityId"+activityId,e);
		}
		return ret;
	}

	private Permission getPermissionByAffair(EdocSummary edocSummary, CtpAffair affair) throws BusinessException {
		String configItem = govdocWorkflowManager.getPolicyByAffair(affair, edocSummary.getProcessId()).getId();
		Long accountId = GovdocHelper.getFlowPermAccountId(edocSummary.getOrgAccountId(), edocSummary);
		Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), configItem,
				accountId);
		return permission;
	}

	/**
	 * 重复跳过节点的意见态度没有时，应该取默认态度；有时取上节点态度意见；
	 *
	 * @param comment
	 * @param lastNodeIsSender
	 * @param defaultAttitude 节点默认态度
	 * @param attitude 节点显示态度
	 * @throws BusinessException
	 */
	private String getDisplayAttitude(Comment comment, boolean lastNodeIsSender,String defaultAttitude,  String attitude)
			throws BusinessException {
		String _attitude = "";
		List<String> attitudeList = new ArrayList<String>();
		if (Strings.isBlank(defaultAttitude)) {//没有默认态度时,默认取第一个
			if (Strings.isNotBlank(attitude)) {
				attitudeList = Arrays.asList(attitude.split(","));
				defaultAttitude = attitudeList.get(0);
			}
		}
		try {
			if (Strings.isNotBlank(attitude)) {
				String commentAttitude = comment.getExtAtt1();
				if (!lastNodeIsSender && Strings.isNotBlank(commentAttitude) && attitudeList.contains(commentAttitude)) {//上节点有态度时,并且当前节点权限有该态度
					_attitude = commentAttitude;
				} else {
					_attitude = defaultAttitude;
				}
			} // 不显示态度
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		if(Strings.isNotBlank(_attitude)){
			if ("haveRead".equals(_attitude)) {
				_attitude = "collaboration.dealAttitude.haveRead";
			} else if ("agree".equals(_attitude)) {
				_attitude = "collaboration.dealAttitude.agree";
			} else if ("disagree".equals(_attitude)) {
				_attitude = "collaboration.dealAttitude.disagree";
			} 
		}
		return _attitude;
	}

	/**
	 *  公文自动跳过
	 */
	private NodeOverTimeAutoRunCheckCode colAutoRun(String matchRequestToken, EdocSummary summary,
			CtpAffair affair, BPMActivity bpmActivity, Comment comment,
			Long needCloneBaseCommentId, boolean isRepeatSkip, String policyName, String mergeDealType, PopResult pr,Map<String,Object> condtiontMap) throws Exception {
		boolean isSuccess = excuteDee(matchRequestToken, summary, affair, bpmActivity, isRepeatSkip, comment,pr);
		if (!isSuccess) {
			return NodeOverTimeAutoRunCheckCode.checkDee;
		}

		String last = "";
		if (pr != null) {
			last = pr.getLast();
		}
		if ("true".equals(last)) {
			String msg = executeWorkflowBeforeEvent("BeforeProcessFinished", affair, pr);
			if (Strings.isNotEmpty(msg)) {
				LOGGER.error("affair.id:" + affair.getId() + msg);
				return NodeOverTimeAutoRunCheckCode.allOtherFalse;
			}
		}


		// 更新表单初始值,只会节点权限 & 加签只读不赋值表单初始值
		if (GovdocUtil.isForm(summary.getBodyType()) && !AffairUtil.isFormReadonly(affair) && !"inform".equals(affair.getNodePolicy())) {
			try {
				// 更新表单初始值
				String rightId = govdocFormManager.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
				capFormManager.procDefaultValue(summary.getFormAppid(), summary.getFormRecordid(), rightId, summary.getId(),null);
				LOGGER.info("表单初始值赋值：formAppId:" + summary.getFormAppid() + "getFormRecordid:" + summary.getFormRecordid() + ",rightId:" + rightId
						+ ",summary.getId():" + summary.getId() + ",affairid:" + affair.getId());
			} catch (Exception e) {
				throw new BusinessException("设置表单初始值异常", e);
			}
		}
		ctpCommentManager.insertComment(comment);
		//在跳过流程之前 刷新动态标题
		//       checkEdocSubject(summary,affair);
		Long workItemId = affair.getSubObjectId();
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
		AffairData affairData = EdocHelper.getAffairData(summary,AppContext.getCurrentUser());
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> globalMap = new HashMap<String, Object>();

		String[] nextMemberNodes = finishWorkItem(comment, "edoc", affair, data, workItemId, bpmActivity,
				summary, affairData, needCloneBaseCommentId, globalMap, pr, condtiontMap);
		String memberAndPolicys = nextMemberNodes[0];

		if (isRepeatSkip) {
			Long memberId = affair.getMemberId();
			String membername = "";
			if (memberId != null) {
				V3xOrgMember m = orgManager.getMemberById(memberId);
				if (m != null) {
					membername = m.getName();
				}
			}
			// 与{0,choice,1#发起人|2#({3})处理人}相同，重复跳过人员：{1}，送往人员：{2}
			String sender = ResourceUtil.getString("collaboration.process.autoskip.log.sender");
			String dealer = ResourceUtil.getString("collaboration.process.autoskip.log.dealer");
			String anyDealer = ResourceUtil.getString("collaboration.process.autoskip.log.anydealer");
			String subpfs = ResourceUtil.getString("collaboration.process.autoskip.log.subpfs"); // 子流程触发设置跳过发起人第一次处理
			String display = "";
			if (BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue().equals(mergeDealType)) {
				display = sender;
			} else if (BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue().equals(mergeDealType)) {
				display = dealer;
			} else if (BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue().equals(mergeDealType)) {
				display = anyDealer;
			} else if (BPMSeeyonPolicySetting.MergeDealType.SUB_PROCESS_FSENDER.getValue().equals(mergeDealType)) {
				display = subpfs;
			}
			User user = AppContext.getCurrentUser();
			String _memberAndPolicys = Strings.isBlank(memberAndPolicys) ? ResourceUtil.getString("collaboration.process.autoskip.log.no") : memberAndPolicys;

			govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()), Long.parseLong(bpmActivity.getId()), ProcessLogAction.autoskip, user.getName(), _memberAndPolicys);

			govdocLogManager.insertAppLog(user, GovdocAppLogAction.REPEAT_SKIP.getKey(), summary.getSubject(), membername, _memberAndPolicys, policyName);

		}
		// 调用表单万能方法,更新状态，触发子流程等
		if (GovdocUtil.isForm(summary.getBodyType())) {
			try {
				List<Comment> commentList = this.ctpCommentManager.getCommentList(ModuleType.collaboration, affair.getObjectId());

				// 协同V5 OA-119937 合并处理后，表单审核状态查询结果错误了
				if ("vouch".equalsIgnoreCase(affair.getNodePolicy())) {
					summary.setVouch(CollaborationEnum.vouchState.pass.ordinal());
				}
				if ("formaudit".equals(affair.getNodePolicy())) {
					summary.setAudited(true);
				}

				capFormManager.updateDataState(summary, affair, ColHandleType.finish, commentList);

			} catch (Exception e) {
				throw new BusinessException("更新表单相关信息异常", e);
			}
		}

		// 流程处理事件通知
		CollaborationProcessEvent event = new CollaborationProcessEvent(this);
		event.setSummaryId(summary.getId());
		event.setAffair(affair);
		event.setComment(comment);
		event.setSenderId(summary.getStartMemberId());
		event.setUserId(AppContext.currentUserId());
		event.setTemplateId(summary.getTempleteId());
		event.setBodyType(summary.getBodyType());
		EventDispatcher.fireEvent(event);
		LOGGER.info("流程结束触发表单高级事件1：colSummary.getState=" + summary.getState());
		GovdocHelper.updateCurrentNodesInfo(summary,true);
		DBAgent.update(summary);

		// 发送自动跳过的事件
		return NodeOverTimeAutoRunCheckCode.normal;
	}

	private boolean excuteDee(String matchRequestToken, EdocSummary summary, CtpAffair affair, BPMActivity bpmActivity, boolean isRepeatSkip,Comment comment,PopResult pr) throws BusinessException {
		if(!String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())){
			return true;
		}
		boolean isSucess = true;
		//       if(collaborationFormBindEventListener != null){
		//           
		//           String attitude = "";
		//           String content = "";
		//           if(comment != null){
		//               attitude = comment.getExtAtt1();
		//               content = comment.getContent();
		//           }
		//           Map<String,String> ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(),affair.getFormAppId(), affair.getMultiViewStr(), summary.getId()
		//                   ,ColHandleType.finish,attitude,content,pr.getCurrentNodeLast());
		//           
		//           if(!"true".equals(ret.get("success"))){
		//               String  msg = ret.get("msg");
		//               List<String> msgs = new ArrayList<String>();
		//               msgs.add(msg);
		//               String step = WorkflowMatchLogMessageConstants.step5;
		//               int state =2;
		//               if(isRepeatSkip){
		//                   step = WorkflowMatchLogMessageConstants.step6;
		//                   state = 3 ;
		//               }
		//               wapi.putWorkflowMatchLogToCache(step, matchRequestToken, affair.getActivityId().toString(), "", msgs); 
		//               wapi.saveMatchProcessLog(state,matchRequestToken, summary.getProcessId(), bpmActivity, AppContext.getCurrentUser(), AppContext.currentUserName());
		//               isSucess = false;   
		//               LOGGER.info("DEE执行失败 - affair.id:"+affair.getId()+",msg:"+msg);
		//           }
		//       }
		return isSucess;
	}
	public String executeWorkflowBeforeEvent(String event,CtpAffair affair,PopResult pr) {

		WorkflowBpmContext context = new WorkflowBpmContext();
		context.setFormData(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
		context.setMastrid(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
		context.setProcessTemplateId(affair.getProcessId() == null ? null : String.valueOf(affair.getProcessId()));
		context.setCurrentActivityId(affair.getActivityId() == null ? null : String.valueOf(affair.getActivityId()));
		context.setBussinessId(affair.getObjectId() == null ? null : String.valueOf(affair.getObjectId()));
		context.setAffairId(String.valueOf(affair.getId()));
		context.setProcessId(affair.getProcessId());
		String appName = "";
		if(Integer.valueOf(ModuleType.collaboration.ordinal()).equals(affair.getApp())){
			appName = "collaboration";
		}else if(Integer.valueOf(ModuleType.edoc.ordinal()).equals(affair.getApp())){
			appName = "edoc";
		}
		context.setAppName(appName);
		context.setCurrentNodeLast(pr.getCurrentNodeLast());

		String msg = wapi.executeWorkflowBeforeEvent(event, context);
		return msg;

	}
	/**
	 * 检查是否需要动态刷新标题，如果需要那么久刷新标题
	 */
	private void checkEdocSubject(EdocSummary summary, CtpAffair affair) throws Exception{
		if(summary.getTempleteId() != null){
			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
			if(null != ctpTemplate) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("templateColSubject", ctpTemplate.getColSubject());
				params.put("templateWorkflowId", ctpTemplate.getWorkflowId());
				params.put("template", ctpTemplate);
				//colManager.checkCollSubject(colSummary,affair,params);
			}
		}

	}
	//overFlag 是否是超期跳过的属性
	private String[] finishWorkItem(Comment c, String appName, CtpAffair affair,
			Map<String, Object> fieldDataBaseMap, Long workItemId, BPMActivity bpmActivity, Object summary,
			AffairData affairData,
			Long needCloneBaseCommentId, Map<String, Object> globalMap, PopResult pr,Map<String,Object> condtiontMap) throws Exception{
		WorkflowBpmContext wfContext = new WorkflowBpmContext();
		wfContext.setCurrentUserId(affair.getMemberId().toString());
		wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
		wfContext.setCurrentWorkitemId(workItemId);
		wfContext.setBusinessData(EventDataContext.CTP_FORM_DATA, fieldDataBaseMap);
		wfContext.setCurrentActivityId(bpmActivity.getId());
		wfContext.setSysAutoFinishFlag(true);
		wfContext.setBusinessData("operationType", GovdocWorkflowEventListener.AUTOSKIP);
		wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, GovdocWorkflowEventListener.AUTOSKIP);
		wfContext.setAppName(appName);
		wfContext.setAppObject(summary);
		wfContext.setConditionsOfNodes(pr.getConditionsOfNodes());
		wfContext.setDynamicFormMasterIds(pr.getDynamicFormMasterIds());

		if (summary instanceof EdocSummary) {
			EdocSummary cs = (EdocSummary) summary;
			String formAppId = cs.getFormAppid() == null ? null : cs.getFormAppid().toString();
			String masterId = cs.getFormRecordid() == null ? null : cs.getFormRecordid().toString();
			wfContext.setFormData(formAppId);
			wfContext.setMastrid(masterId);
			wfContext.setVersion("2.0");
		}
		wfContext.setBusinessData("comment", c);
		//overFlag //设置是否是超期的流程 //设置应用类型是协同还是公文
		if(condtiontMap != null ){
			wfContext.getBusinessData().putAll(condtiontMap);
		}
		wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
		wfContext.setBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT, affair);
		wfContext.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
		wfContext.setBusinessData(GovdocWorkflowEventListener.CURRENT_OPERATE_COMMENT_ID, needCloneBaseCommentId);
		if ("collaboration.affair.skipExtended".equals(c.getExtAtt3())) {
			wfContext.setBusinessData(GovdocWorkflowEventListener.AFFAIR_SUB_STATE, String.valueOf(SubStateEnum.col_done_overtimeskip.getKey()));
		}

		String[] result = wapi.transFinishWorkItem(wfContext);

		//同步消息
		userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affair.getId());
		return result;
	}
	@Override
	public void updateAffairState4RepeatAffair(CtpAffair affair, String forwardMember) {
		affair.setState(StateEnum.col_pending.getKey());
		try {
			affairManager.updateAffair(affair);
		} catch (Throwable e1) {
			LOGGER.error("",e1);
		}
	}

	public void setEdocStatManager(EdocStatManager edocStatManager) {
		this.edocStatManager = edocStatManager;
	}
	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}
	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}
	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setCtpCommentManager(CommentManager ctpCommentManager) {
		this.ctpCommentManager = ctpCommentManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	
}
