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
package com.seeyon.apps.collaboration.quartz;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt3Enum;
import com.seeyon.apps.collaboration.event.CollaborationProcessEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.CanBackgroundDealParam;
import com.seeyon.apps.collaboration.util.CanBackgroundDealResult;
import com.seeyon.apps.collaboration.util.ColSelfUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.dee.api.CollaborationFormBindEventListener;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.quartz.WorkflowNodeOvertimeAppHandler;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.AttitudeManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.messageRule.ruleEnum.MessageRuleEnum.MessageRuleType;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.ctp.workflow.wapi.PopResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMCircleTransition;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author mujun
 *
 */
public class CollNodeOverTimeManagerImpl extends WorkflowNodeOvertimeAppHandler{
	private final static Log LOGGER = LogFactory.getLog(CollNodeOverTimeManagerImpl.class);
    
    private TemplateManager templateManager;
    private CollaborationApi collaborationApi;
	private SuperviseManager superviseManager = null;
	private EnumManager enumManagerNew = null;
	private WorkTimeManager workTimeManager = null;
	private ColManager colManager;
    private WorkflowApiManager           wapi;
    private CAPFormManager capFormManager;
    private CommentManager ctpCommentManager;
    private OrgManager orgManager;
    private AppLogManager appLogManager;
    private ProcessLogManager processLogManager;
    private AffairManager affairManager;
    private MainbodyManager ctpMainbodyManager;
    private UserMessageManager           userMessageManager;
    private ColMessageManager colMessageManager;
    private CollaborationFormBindEventListener collaborationFormBindEventListener;
    private PermissionManager permissionManager;
    private MessageRuleManager messageRuleManager;
    private AttitudeManager attitudeManager;
    private OrgManagerDirect orgManagerDirect;

    public void setCollaborationFormBindEventListener(
            CollaborationFormBindEventListener collaborationFormBindEventListener) {
        this.collaborationFormBindEventListener = collaborationFormBindEventListener;
    }



    public PermissionManager getPermissionManager() {
        return permissionManager;
    }



    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }



    public ColMessageManager getColMessageManager() {
        return colMessageManager;
    }
    public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }
    public MainbodyManager getCtpMainbodyManager() {
        return ctpMainbodyManager;
    }
    public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
        this.ctpMainbodyManager = ctpMainbodyManager;
    }
    public UserMessageManager getUserMessageManager() {
        return userMessageManager;
    }
    public AffairManager getAffairManager() {
        return affairManager;
    }
    public OrgManager getOrgManager() {
        return orgManager;
    }
    public AppLogManager getAppLogManager() {
        return appLogManager;
    }
    public ProcessLogManager getProcessLogManager() {
        return processLogManager;
    }
    public CommentManager getCtpCommentManager() {
        return ctpCommentManager;
    }
    public void setCtpCommentManager(CommentManager ctpCommentManager) {
        this.ctpCommentManager = ctpCommentManager;
    }

    
    
    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
    
    public WorkflowApiManager getWapi() {
        return wapi;
    }
    public ColManager getColManager() {
        return colManager;
    }
    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
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

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }


    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	
	public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
		this.messageRuleManager = messageRuleManager;
	}

    public void setAttitudeManager(AttitudeManager attitudeManager) {
		this.attitudeManager = attitudeManager;
	}
	public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
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
            		if(!ColUtil.isAfffairValid(affair)){
            			LOGGER.info("协同定时调度任务判断事项不可用，直接中断，affairId:"+affair.getId()+",应用ID："+affair.getObjectId());
            			return;
            		}
            	}catch(Exception e){
            		LOGGER.error("验证待办事项有效性时报错："+e,e);
            	}
            	
            	
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
             
                ColSummary summary = colManager.getColSummaryById(affair.getObjectId());

                String[] links = this.getMessgaeLink();
                String messageLink= links[1];
                String messageLinkdone= links[0];
                
                String key = null;
                if(Long.valueOf(0).equals(isAdvanceRemind)){
                    key = "node.affair.advanceRemind"; //您有《{0}》事項需要處理！
                }else if(Long.valueOf(1).equals(isAdvanceRemind)){
                    key = "node.affair.overTerm";   //《{0}》事項已超期！
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
                                                affair,bpmProcess,colSubject,processId,appEnum,sendId,messageLink);
                                	
                                        
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
                            }else if( dealTermType== 2   && null!=bpmProcess ){//2-自动跳过
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

									boolean isCircle = false;
									String notkey = "";
							

									key = "node.affair.overTerm.autoruncase";// 协同
									notkey = "collaboration.node.affair.overTerm.autoruncase.not";

									if (NodeOverTimeAutoRunCheckCode.vouchnode.equals(checkCode)) {
										notkey = "node.affair.overTerm.autoruncase.not.heding";// 核定节点
									}
									
									if(MainbodyType.getEnumByKey(Integer.valueOf(affair.getBodyType()))
									        .equals(MainbodyType.FORM)){
									    isCircle = this.isCircleNode(processId, affair.getActivityId().toString());
                                    }
									
									
									if(isCircle){
										String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_LINK_CIRCLE);
										wapi.putWorkflowMatchLogMsgToCache(WorkflowMatchLogMessageConstants.step5, matchRequestToken, affair.getActivityId().toString(), "", msg); 
									}
									
									boolean isFinishSuccess = true;
									if (NodeOverTimeAutoRunCheckCode.normal.equals(checkCode) && !isCircle) {// 能正常跳过


                                        //添加超期跳过的状态和-当前是公文还是协同
                                        Map<String,Object> condtiontMap = new HashMap<String,Object>();
                                        condtiontMap.put("overFlag",true);//设置是否是超期的流程
//                                        condtiontMap.put("appEnum",appEnum);//设置应用类型是协同还是公文--应该没有使用了，暂时先不管
                                        NodeOverTimeAutoRunCheckCode finishCheckCode = this.transSystemAutoRunCase(matchRequestToken, summary, affair,
                                                bpmSeeyonPolicy, bpmActivity, user, nodeAutoRunCaseBO.getPr(),condtiontMap);
										//成功处理
										if(finishCheckCode == null || NodeOverTimeAutoRunCheckCode.normal.equals(finishCheckCode)){
										    String isStrongValidate = nodeAutoRunCaseBO.getIsStrongValidate();
	                                        if("false".equals(isStrongValidate)){//非强制校验不通过记录日志
	                                            wapi.saveMatchProcessLog(4,matchRequestToken, processId, bpmActivity, user, currentMember.getName());
	                                        }else{
	                                            processLogManager.insertLog(user, Long.parseLong(processId),
	                                                    Long.parseLong(bpmActivity.getId()),
	                                                    ProcessLogAction.processColl_SysAuto, currentMember.getName());
	                                        }
	                                        appLogManager.insertLog(user, AppLogAction.Coll_Flow_Node_RunCase_AutoSys,
	                                                colSubject, bpmActivity.getName(), user.getName());
	                                        // 三、给被跳过人发个消息提醒
	                                        sendSysMessageForAutoRunCase(affair, messageLinkdone, processId, appEnum,
	                                                currentMember, key,summary);
										}
										else{
											isFinishSuccess  = false;
										}
									} 
									else{
                                        isFinishSuccess  = false;
                                    }
									if(!isFinishSuccess){

										// 不能自动跳过
										LOGGER.info("checkCode="+checkCode+",name="+(null != affair.getSubject()? affair.getSubject():"**"));
										if(NodeOverTimeAutoRunCheckCode.specialback.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not1";
										}else if(NodeOverTimeAutoRunCheckCode.hasNewFlow.equals(checkCode)){
											//邦定了新流程
											notkey ="collaboration.node.affair.overTerm.autoruncase.not2";
										}else if(NodeOverTimeAutoRunCheckCode.hasBranchSelectPeople.equals(checkCode)){
											//需要为后面的节点选择人员或分支条件
											notkey ="collaboration.node.affair.overTerm.autoruncase.not3";
										}else if(NodeOverTimeAutoRunCheckCode.isPreNewFlowFinish.equals(checkCode)){
											//前面节点触发的新流程还没有结束不允许执行自动跳过操作
											notkey ="collaboration.node.affair.overTerm.autoruncase.not4";
										}else if(NodeOverTimeAutoRunCheckCode.isFormMustWrite.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not5";
										}else if(NodeOverTimeAutoRunCheckCode.opinionMustWrite.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not6";
										}else if(NodeOverTimeAutoRunCheckCode.mainInSpecialback.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not8";
										}else if(NodeOverTimeAutoRunCheckCode.strongValidate.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not9";
										}else if(NodeOverTimeAutoRunCheckCode.checkDee.equals(checkCode)){
											notkey ="collaboration.node.affair.overTerm.autoruncase.not10";
										}else if(isCircle){
											//环形分支
											notkey = "collaboration.node.affair.overTerm.autoruncase.not7";
										}
										sendSysMessageForAutoRunCase(affair, messageLink, processId, appEnum,currentMember, notkey,summary);
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
                      //协同或表单
                        
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
                                if(!QuartzHolder.hasQuartzJob(jobName)) {
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
                                        QuartzHolder.newQuartzJob(jobName,jobName,startTime , "overTimeCycleRemindJob", datamap);
                                    } catch(Exception e) {
                                        LOGGER.error("创建节点超期后多次提醒定时任务出错 affairId="+affairId + " 报错信息：" + e);
                                    }
                                }
                            }
                        }
                    
                    }
                }
                //待办事项给督办发消息， 消息改造，只做协同
                if(Long.valueOf(0).equals(isAdvanceRemind) && Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(affair.getApp())){
                    sendMsgToSupervisor4Remind(affair,currentMember.getId());
                    sendSysMessageForAutoRunCase(affair,messageLink,null,appEnum,currentMember,key,summary);
                }else if(Long.valueOf(1).equals(isAdvanceRemind)){
                	//超期消息有消息规则按照消息规则的逻辑走
                	if(Strings.isNotBlank(affair.getMessageRuleId())){
                		sendNodeOverTimeMessageByMessageRule(affair, currentMember, messageLink, key,hasExecuteActivity);
                	}else{
                		sendMsgToSupervisor4Overtime(affair, appEnum);
                		sendSysMessageForAutoRunCase(affair,messageLink,null,appEnum,currentMember,key,summary);
                	}

                }
            }else{
                LOGGER.info("该待办已处理，完成时间不为空，不需要执行处理期限到操作。affairId:="+affairId);
            }
        }finally{
            wapi.removeAllWorkflowMatchLogCache(matchRequestToken);
            DBAgent.commit();
        }
    }

    /**
     * 
     * @Title: sendNodeOverTimeMessageByMessageRule   
     * @Description: 设置了超期提醒规则，发送超期消息
     * @param affair
     * @param currentMemeber
     * @param messageLink
     * @param contentKey      
     * @return: void  
     * @date:   2019年2月19日 下午2:28:35
     * @author: xusx
     * @since   V7.1	       
     * @throws
     */
    private void sendNodeOverTimeMessageByMessageRule(CtpAffair affair,V3xOrgMember currentMemeber,String messageLink,String contentKey,String hasExecuteActivity){
    	try {
    		
    		List<CtpAffair> allAffair = affairManager.getAffairs(affair.getObjectId());
			ColSummary summary = colManager.getColSummaryById(affair.getObjectId());

			String messageRule = affair.getMessageRuleId();
			//超期提醒
			List<MessageRuleVO> messageRules = messageRuleManager.getMeesageByIds(messageRule, MessageRuleType.overtimeNotice,null);
			if(Strings.isNotEmpty(messageRules)){
				for (MessageRuleVO messageRuleVO : messageRules) {
					//系统预置的走原来的逻辑给当前待办人和督办人发超期消息
					if(Long.valueOf("1").equals(messageRuleVO.getCreater())){
						sendMsgToSupervisor4Overtime(affair,ApplicationCategoryEnum.collaboration);//超期给督办发消息
	                	sendSysMessageForAutoRunCase(affair,messageLink,null,ApplicationCategoryEnum.collaboration,currentMemeber,contentKey,summary);//给当前待办人发送超期消息
					}else if(Strings.isBlank(hasExecuteActivity) || !hasExecuteActivity.contains(affair.getActivityId().toString())){
						
						//非系统默认走消息规则发送超期消息
						colMessageManager.sendMessageByMessageRule(messageRuleVO, summary, affair, allAffair, ColMessageFilterEnum.overTime,affair.getMemberId());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
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
     * @param ctpAffair
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
            Long agentId = ColUtil.getAgentMemberId(ctpAffair.getTempleteId(), ctpAffair.getMemberId(), ctpAffair.getReceiveTime());
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
     * @param nextMembers1
     * @param currentMember
     * @param currentOrgAccount
     * @param affair
     * @param bpmProcess
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
            List<V3xOrgMember> nextMembers= filterNextMembers(nextMembers1, affairs, affair);
            
            
            
            
            /*System.out.println(affair.getId());
            System.out.println("=====");
            
            for(CtpAffair a : affairs){
                System.out.println(a.getId() + " - " + a.getMemberId());
            }
            System.out.println("-----");
            for(V3xOrgMember m : nextMembers1){
                System.out.println(m.getId() + " - " + m.getName());
            }
            System.out.println("~~~~~");
            for(V3xOrgMember m : nextMembers){
                System.out.println(m.getId() + " - " + m.getName());
            }
            System.out.println("-----");*/
            
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
                    
                    //System.out.println(newAffair.getId() + " - " + nextMember.getId() + " - " + nextMember.getName());
                    
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
                    if (remindTime != null && remindTime != -1 && remindTime != 0 && deadLine != null && deadLine != 0 && deadLine > remindTime) {
                        advanceRemindTime = workTimeManager.getRemindDate(deadLineRunTime, remindTime);
                    }
                    newAffair.setDealTermType(0);//仅消息提醒了
                    newAffair.setExpectedProcessTime(deadLineRunTime);
                    
                    ColUtil.affairExcuteRemind(newAffair, nextMember.getOrgAccountId(),deadLineRunTime,advanceRemindTime);
                    
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
            
            
            //System.out.println("==================================");
            
            
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
        Long agentId = ColUtil.getAgentMemberId(affair.getTempleteId(), currentMember.getId(), affair.getReceiveTime());
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
        processLogManager.insertLog(user, Long.parseLong(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.replaceNode_SysAuto,currentOrgAccount.getShortName()+":"+currentMember.getName(),nextOrgAccount.getShortName()+":"+nextMember.getName());
        
        int appLogAction = 162;
        if(isEdoc){
        	appLogAction = 350;
        }
        appLogManager.insertLog(user, appLogAction, colSubject,bpmActivity.getName(),currentMember.getName(),nextMember.getName());
       
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
     * @param affairs
     * @return
     */
    private List<V3xOrgMember> filterNextMembers(List<V3xOrgMember> nextMembers,List<CtpAffair> affairs, CtpAffair currentAffair) {
        List<V3xOrgMember> result= new ArrayList<V3xOrgMember>();
        if( null!=nextMembers && !nextMembers.isEmpty()){
            Set<Long> memberIds= new HashSet<Long>();
            String subject= "";
            if(null!=affairs && !affairs.isEmpty()){
                for (CtpAffair temp_affair : affairs) { 
                    
                    if(temp_affair.getId().equals(currentAffair.getId())){
                        // 当前这条affair 会被删除， 如果匹配到的人员有重复， 按照新匹配的人员来算
                        continue;
                    }
                    
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
    * @author xuqw
    * @param summaryId
    * @param opinionId
    * @param commentCont
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
     * 节点超期提醒给督办人发送消息
     */
     private void sendMsgToSupervisor4Remind(CtpAffair affair, Long currentMemberId){
         
         try{
             Set<MessageReceiver> receivers = getSupervisors(affair.getObjectId(), affair.getId());
             if(Strings.isNotEmpty(receivers)){
                 String memberName = orgManager.getMemberById(affair.getMemberId()).getName();
               //节点提前提醒时间
                 Long remindDate = affair.getRemindDate();
                 String enumLabel = "";
                 if(remindDate != null){
                	 Locale locale = orgManagerDirect.getMemberLocaleById(currentMemberId);
                     CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.common_remind_time,String.valueOf(remindDate),locale);
                     enumLabel = ResourceUtil.getString(cei.getLabel());
                 }
                 MessageContent msgContent = new MessageContent("node.affair.advanceRemind2", affair.getSubject(), enumLabel, memberName).setImportantLevel(affair.getImportantLevel());
                 userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers,ColMessageFilterEnum.supervise.key);
             }
         }catch(Throwable e){
             LOGGER.error(e.getMessage(), e);
         }finally{
         }
     }
     
     /**
      * 超期给督办发消息
      */
      private void sendMsgToSupervisor4Overtime(CtpAffair affair, ApplicationCategoryEnum appEnum){
          
          try{
              Set<MessageReceiver> receivers = getSupervisors(affair.getObjectId(), affair.getId());
              if(Strings.isNotEmpty(receivers)){
                  String memberName = orgManager.getMemberById(affair.getMemberId()).getName();
                  MessageContent msgContent = new MessageContent("node.affair.overTerm2", affair.getSubject(), memberName).setImportantLevel(affair.getImportantLevel());
                  userMessageManager.sendSystemMessage(msgContent, appEnum, affair.getSenderId(), receivers,ColMessageFilterEnum.supervise.key);
              }
          }catch(Throwable e){
              LOGGER.error(e.getMessage(), e);
          }finally{
          }
      }
    
    /**
     * 自动跳过和仅消息提醒
     * @param ctpAffair
     * @param messageLink
     * @param processId
     * @param appEnum
     * @param currentMmber
     * @param key
     */
    private void sendSysMessageForAutoRunCase(CtpAffair ctpAffair,String messageLink, String processId,ApplicationCategoryEnum appEnum,
            V3xOrgMember currentMmber,String key,ColSummary summary) {
        try{
            
            //节点提前提醒时间
            Long remindDate = ctpAffair.getRemindDate();
            String enumLabel = "";
            if(remindDate != null){
            	Locale locale = orgManagerDirect.getMemberLocaleById(currentMmber.getId());
                CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.common_remind_time,String.valueOf(remindDate), locale);
                enumLabel = ResourceUtil.getString(cei.getLabel());
            }
            
            //同时给该事项办理人员发一条提醒消息?
            MessageContent msgContent = null;
            MessageReceiver msgReceiver = null;
            Long agentId = null;
           String permissionLabel = "";
           Permission permission =  getPermissionByAffair(summary, ctpAffair);
           if(permission!=null) {
        	   permissionLabel = permission.getLabel();
           }
            if( null!=currentMmber && currentMmber.isValid() ){
                msgContent = new MessageContent(key, ctpAffair.getSubject(), enumLabel,permissionLabel).setImportantLevel(ctpAffair.getImportantLevel());
                msgReceiver = new MessageReceiver(ctpAffair.getId(), currentMmber.getId(), messageLink, ctpAffair.getId().toString());
                //判断当前的代理人是否有效
                agentId = ColUtil.getAgentMemberId(ctpAffair.getTempleteId(), currentMmber.getId(), ctpAffair.getReceiveTime());
            }
            MessageContent msgContent1 = null;//代理人消息内容
            MessageReceiver msgReceiver1= null;//代理人id
            if(null!= agentId){             
                V3xOrgMember currentMemberAgent= orgManager.getMemberById(agentId);
                if(null!=currentMemberAgent && currentMemberAgent.isValid()){//代理人员可用
                    msgContent1 = new MessageContent(key, ctpAffair.getSubject(), enumLabel,permissionLabel).setImportantLevel(ctpAffair.getImportantLevel()).add("col.agent");
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


    public ApplicationCategoryEnum getAppEnum() throws BusinessException {
        return ApplicationCategoryEnum.collaboration;
    }
    
    private String[] getMessgaeLink() throws BusinessException {
        String[] links = new String[2];
        links[0] = "message.link.col.done.detail";
        links[1] = "message.link.col.pending";
        return links;
    }
    
    private void updateCurrentNodesInfo(Object object) throws BusinessException {
        ColSummary summary  = (ColSummary)object;
        ColUtil.updateCurrentNodesInfo(summary); 
        colManager.updateColSummary(summary);
    }


    private NodeOverTimeAutoRunCheckCode transSystemAutoRunCase(String matchRequestToken, Object object, CtpAffair affair,
                                                                BPMSeeyonPolicy bpmSeeyonPolicy, BPMActivity bpmActivity, User user, PopResult pr,Map<String,Object> condtiontMap) throws BusinessException {
        ColSummary summary = (ColSummary) object;
        Comment comment = new Comment();
        comment.setHidden(false);
        comment.setId(UUIDLong.longUUID());
        //意见设置为自动跳过
        comment.setExtAtt3("collaboration.affair.skipExtended");
        comment.setPid(0L);
        comment.setPraiseNumber(0);
        comment.setPraiseToComment(false);
        comment.setPraiseToComment(false);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setCtype(0);
        
        
        comment.setDepartmentId(affair.getMatchDepartmentId());
        comment.setPostId(affair.getMatchPostId());
        comment.setAccountId(affair.getMatchAccountId());
        
        
        
		comment.setAffairId(affair.getId());
		comment.setCreateId(affair.getMemberId());
        try {
            
            ColUtil.addOneReplyCounts(summary);
            NodeOverTimeAutoRunCheckCode code = colAutoRun(matchRequestToken, summary, affair, bpmActivity
                    , comment, comment.getId(), false, null, "", pr,condtiontMap);

            return code;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new BusinessException(e);
        }
    }
   
    
    public NodeAutoRunCaseBO canAutoRunCase(String matchRequestToken,Object object, CtpAffair affair,BPMSeeyonPolicy bpmSeeyonPolicy) throws BusinessException {
        List<String> cannotMsgList= new ArrayList<String>();
        
        NodeAutoRunCaseBO  nodeAutoRunCaseBO = checkAutoRunCase(matchRequestToken,object, affair,bpmSeeyonPolicy,cannotMsgList);
        String autoSkipNodeId= affair.getActivityId().toString();
        wapi.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step5, matchRequestToken, autoSkipNodeId, "", cannotMsgList); 
        return nodeAutoRunCaseBO;
        
    }
   
    private NodeAutoRunCaseBO checkAutoRunCase(String matchRequestToken, Object object, CtpAffair affair, 
            BPMSeeyonPolicy bpmSeeyonPolicy, List<String> cannotMsgList) throws BusinessException {
        
        
        NodeAutoRunCaseBO nodeAutoRunCaseBO = new NodeAutoRunCaseBO();
        NodeOverTimeAutoRunCheckCode checkCode = NodeOverTimeAutoRunCheckCode.normal;
        nodeAutoRunCaseBO.setCode(checkCode);
        nodeAutoRunCaseBO.setMatchRequestToken(matchRequestToken);
        ColSummary summary = (ColSummary) object;

        boolean isSepcial = Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(affair.getSubState())
                || Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState());
        if (isSepcial) {
            nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.specialback);
            String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.PROCESS_AFFAIR_IN_SPECIAL_STEPBACK));
            cannotMsgList.add(msg);
            return nodeAutoRunCaseBO;
        }
      //意见必填
        String configItem = ColUtil.getPolicyByAffair(affair).getId();
        String category = EnumNameEnum.col_flow_perm_policy.name();
        Permission permission = null;
        try {
            Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            permission = permissionManager.getPermission(category, configItem, accountId);
            
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        if ("vouch".equals(affair.getNodePolicy()) || (permission!=null&&permission.getCustomNode())) {// 核定节点权限或者自定义节点权限不能自动跳过
            nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.vouchnode);
            LOGGER.warn("该协同待办为核定节点或者自定义节点权限，不允许执行自动跳过操作。colSummaryId:=" + affair.getObjectId() + ";  affairId:=" + affair.getId());
            String msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY,permission.getLabel());
            cannotMsgList.add(msg);
            return nodeAutoRunCaseBO;
        }
        boolean isForm = ColUtil.isForm(affair.getBodyType());
        if (isForm) {

          /*  Map<String, String> deeMap = checkBindEventBatch(affair, summary);
            if (!deeMap.isEmpty() && !"true".equals(deeMap.get("success"))) {// dee校验不通过，不能自动跳过
                cannotMsgList.add(deeMap.get("msg"));
                nodeAutoRunCaseBO.setCode(AutoRunCaseCheckCode.checkDee);
                return nodeAutoRunCaseBO;
            }
*/
            boolean isAffairReadOnly = AffairUtil.isFormReadonly(affair);
            if (!isAffairReadOnly && !"inform".equals(affair.getNodePolicy())) {
                
                // Set<String> fields = capFormManager.getNotNullableFields(summary.getFormAppid(), affair.getMultiViewStr());
                
                WorkflowApiManager wapi = (WorkflowApiManager) AppContext.getBean("wapi");
                WorkflowFormDataMapManager formDataManager = wapi.getWorkflowFormDataMapManager("form");
                
                Set<String> mastWriteBblockField = formDataManager.isFormMustWrite(summary.getFormAppid(), affair.getMultiViewStr());
                
                if (Strings.isNotEmpty(mastWriteBblockField)) {
                    nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.isFormMustWrite);
                    String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FORM_FIELD_MUSTWRITE)) + Strings.join(mastWriteBblockField, ",");
                    cannotMsgList.add(msg);
                    return nodeAutoRunCaseBO;
                }
            }

            
            // 校验表单校验规则
            Map<String, Object> ret = capFormManager.checkRule(summary.getId(), summary.getFormAppid(), summary.getFormRecordid(), affair.getMultiViewStr());
            
            String isPass = (String)ret.get("isPass");
            String isExsitNotStrongValidate = (String)ret.get("isExsitNotStrongValidate");
            
            LOGGER.error("[checkRule]节点超期自动跳过,传入参数：formMasterDataId：" + summary.getFormRecordid() + ", formAppID:" + summary.getFormAppid() + ",summaryId:" + summary.getId()
                +",isPass:"+isPass+",isExsitNotStrongValidate:"+isExsitNotStrongValidate);
            
            if ("false".equals(isPass)) {// 强制校验
                nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.strongValidate);
                String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_CAN_AUTOSKIP_ISSTRONGVALIDATE));
                cannotMsgList.add(msg);
                return nodeAutoRunCaseBO;
            }
            else if ("true".equals(isExsitNotStrongValidate)) {
                nodeAutoRunCaseBO.setIsStrongValidate("false");
                String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_CAN_AUTOSKIP_ISNOTSTRONGVALIDATE));
                cannotMsgList.add(msg);
            }
        }
        // 判断当前处理人员是否为当前流程节点的最后一个处理人
        boolean isExecuteFinished = wapi.isExecuteFinished(summary.getProcessId(), affair.getSubObjectId());
        // 前面节点触发的子流程是否已经结束
        boolean isPreNewFlowFinish = !wapi.hasUnFinishedNewflow(summary.getProcessId(), String.valueOf(affair.getActivityId()));

        // 当前跳过节点是否有子流程
        boolean isFromTemplate = summary.getTempleteId() != null && summary.getTempleteId().longValue() != -1;
        boolean hasNewflow = isFromTemplate && bpmSeeyonPolicy != null && "1".equals(bpmSeeyonPolicy.getNF());

        if (hasNewflow) {
            nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.hasNewFlow);
            String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB));
            cannotMsgList.add(msg);
            return nodeAutoRunCaseBO;
        }

        if (!isPreNewFlowFinish) {
            nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.isPreNewFlowFinish);
            LOGGER.warn("该协同待办前面节点触发的新流程还没有结束不允许执行自动跳过操作。colSummaryId:=" + summary.getId() + ";  affairId:=" + affair.getId());
            String msg = ResourceUtil.getString(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_FLOWSUB_UNFINISHED));
            cannotMsgList.add(msg);
            return nodeAutoRunCaseBO;
        }
        
        
        PopResult pr =  new  PopResult ();
        if (isExecuteFinished) {// 是决定流程走向的人

            // 知会节点超期-自动跳过，有流程分支或选人选项，忽略并自动跳转
            if (!"inform".equals(affair.getNodePolicy()) && !"zhihui".equals(affair.getNodePolicy())) {
                String formAppId = summary.getFormAppid() == null ? null : summary.getFormAppid().toString();
                String masterId = summary.getFormRecordid() == null ? null : summary.getFormRecordid().toString();
                // 当前跳过的节点后面是否有分支或者需要选人或者人员不可用
                
                pr = isPop(matchRequestToken, ResourceUtil.getString(WorkflowMatchLogMessageConstants.step5), affair, summary.getProcessId(), summary.getCaseId(),
                        summary.getStartMemberId(), formAppId, masterId, "collaboration");
                LOGGER.info("isPreNewFlowFinish:=" + isPreNewFlowFinish + ";hasNewflow=" + hasNewflow);

                if ("true".equals(pr.getPopResult())) {
                    LOGGER.warn("该协同待办需要触发新流程或者后面节点需要进行分支匹配、选择执行人或人员不可用，不允许执行自动跳过操作。colSummaryId:=" + summary.getId() + ";  affairId:=" + affair.getId());
                    checkCode = NodeOverTimeAutoRunCheckCode.hasBranchSelectPeople;
                    String msg = pr.getMsg();
                    if (ResourceUtil.getString("collaboration.workflow.mainInSpecial.title ").equals(msg)) {
                        checkCode = NodeOverTimeAutoRunCheckCode.mainInSpecialback;
                    }
                    if (Strings.isBlank(msg)) {
                        msg = ResourceUtil.getString("coll.summary.validate.lable23");
                    }
                    nodeAutoRunCaseBO.setCode(checkCode);
                    cannotMsgList.add(msg);
                    
                    return nodeAutoRunCaseBO;

                }
            }
        }
        nodeAutoRunCaseBO.setPr(pr);
        
        //意见必填
        try {
            if (permission != null) {
                Integer opinionPolicy = permission.getNodePolicy().getOpinionPolicy();

                if (Integer.valueOf(1).equals(opinionPolicy)) {
                    LOGGER.info("意见必须填写,不能重复跳过，  affairId:=" + affair.getId());
                    nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.opinionMustWrite);
                    String msg = ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY_OPINIONMUSTWRITE);
                    cannotMsgList.add(msg);
                    return nodeAutoRunCaseBO;
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        
        CanBackgroundDealParam param = new CanBackgroundDealParam();
        param.setAffair(affair);
        CanBackgroundDealResult cr =  ColSelfUtil.publicCheckCanBackgroundDeal(param);
        if(!cr.isCan()){
            nodeAutoRunCaseBO.setCode(NodeOverTimeAutoRunCheckCode.allOtherFalse);
            cannotMsgList.add(cr.getMsg());
            return nodeAutoRunCaseBO;
        }
        
        return nodeAutoRunCaseBO;
    }

    public String executeWorkflowBeforeEvent(String event,CtpAffair affair,PopResult pr,Comment comment) {
        String msg = ColUtil.executeWorkflowBeforeEvent(affair, event, pr.getCurrentNodeLast(), comment);
        return msg;
        
    }

    /**
     * 检查是否需要动态刷新标题，如果需要那么久刷新标题
     */
    private void checkCollSubject(ColSummary colSummary, CtpAffair affair) throws Exception{
        if(colSummary.getTempleteId() != null){
            CtpTemplate ctpTemplate = templateManager.getCtpTemplate(colSummary.getTempleteId());
            if(null != ctpTemplate) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("templateColSubject", ctpTemplate.getColSubject());
                params.put("templateWorkflowId", ctpTemplate.getWorkflowId());
                params.put("template", ctpTemplate);
                colManager.checkCollSubject(colSummary,affair,params);
            }
        }

    }

    /**
     *  协同自动跳过
     */
    private NodeOverTimeAutoRunCheckCode colAutoRun(String matchRequestToken, ColSummary colSummary,
                                                    CtpAffair affair, BPMActivity bpmActivity, Comment comment,
                                                    Long needCloneBaseCommentId, boolean isRepeatSkip, String policyName, String mergeDealType, PopResult pr,Map<String,Object> condtiontMap) throws Exception {
        boolean isSuccess = excuteDee(matchRequestToken, colSummary, affair, bpmActivity, isRepeatSkip, comment,pr);
        if (!isSuccess) {
            return NodeOverTimeAutoRunCheckCode.checkDee;
        }

        String last = "";
        if (pr != null) {
            last = pr.getLast();
        }
        if ("true".equals(last)) {
            String msg = executeWorkflowBeforeEvent("BeforeProcessFinished", affair, pr,comment);
            if (Strings.isNotEmpty(msg)) {
                LOGGER.error("affair.id:" + affair.getId() + msg);
                return NodeOverTimeAutoRunCheckCode.allOtherFalse;
            }
        }


        // 更新表单初始值,只会节点权限 & 加签只读不赋值表单初始值
        if (ColUtil.isForm(colSummary.getBodyType()) && !AffairUtil.isFormReadonly(affair) && !"inform".equals(affair.getNodePolicy())) {
            try {
                // 更新表单初始值
                String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
                capFormManager.procDefaultValue(colSummary.getFormAppid(), colSummary.getFormRecordid(), rightId, colSummary.getId(),null);
                LOGGER.info("表单初始值赋值：formAppId:" + colSummary.getFormAppid() + "getFormRecordid:" + colSummary.getFormRecordid() + ",rightId:" + rightId
                        + ",summary.getId():" + colSummary.getId() + ",affairid:" + affair.getId());
            } catch (Exception e) {
                throw new BusinessException("设置表单初始值异常", e);
            }
        }
        ctpCommentManager.insertComment(comment);
        //在跳过流程之前 刷新动态标题
        checkCollSubject(colSummary,affair);
        Long workItemId = affair.getSubObjectId();
        AffairData affairData = ColUtil.getAffairData(colSummary);
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> globalMap = new HashMap<String, Object>();

        String[] nextMemberNodes = finishWorkItem(comment, "collaboration", affair, data, workItemId, bpmActivity,
                colSummary, affairData, needCloneBaseCommentId, globalMap, pr, condtiontMap);
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

            processLogManager.insertLog(user, Long.parseLong(colSummary.getProcessId()), Long.parseLong(bpmActivity.getId()), ProcessLogAction.autoskip, display, _memberAndPolicys);

            appLogManager.insertLog(user, 106, colSummary.getSubject(), membername, _memberAndPolicys, policyName);

        }
        // 调用表单万能方法,更新状态，触发子流程等
        if (ColUtil.isForm(colSummary.getBodyType())) {
            try {
                List<Comment> commentList = this.ctpCommentManager.getCommentList(ModuleType.collaboration, affair.getObjectId());

                // 协同V5 OA-119937 合并处理后，表单审核状态查询结果错误了
                if ("vouch".equalsIgnoreCase(affair.getNodePolicy())) {
                    colSummary.setVouch(CollaborationEnum.vouchState.pass.ordinal());
                }
                if ("formaudit".equals(affair.getNodePolicy())) {
                    colSummary.setAudited(true);
                }

                capFormManager.updateDataState(colSummary, affair, ColHandleType.finish, commentList);

            } catch (Exception e) {
                throw new BusinessException("更新表单相关信息异常", e);
            }
        }

        LOGGER.info("流程结束触发表单高级事件1：colSummary.getState=" + colSummary.getState().intValue());
        ColUtil.updateCurrentNodesInfo(colSummary);
        DBAgent.update(colSummary);
        // 流程处理事件通知
        CollaborationProcessEvent event = new CollaborationProcessEvent(this);
        event.setSummaryId(colSummary.getId());
        event.setAffair(affair);
        event.setComment(comment);
        event.setSenderId(colSummary.getStartMemberId());
        event.setUserId(AppContext.currentUserId());
        event.setTemplateId(colSummary.getTempleteId());
        event.setBodyType(colSummary.getBodyType());
        EventDispatcher.fireEventAfterCommit(event);
        

        // 发送自动跳过的事件
        return NodeOverTimeAutoRunCheckCode.normal;
    }

    private boolean excuteDee(String matchRequestToken, ColSummary colSummary, CtpAffair affair, BPMActivity bpmActivity, boolean isRepeatSkip,Comment comment,PopResult pr) throws BusinessException {
        if(!String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())){
            return true;
        }
        boolean isSucess = true;
        if(collaborationFormBindEventListener != null){
            
            String attitude = "";
            String content = "";
            if(comment != null){
                attitude = comment.getExtAtt1();
                content = comment.getContent();
            }
            Map<String,String> ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(),affair.getFormAppId(), affair.getMultiViewStr(), colSummary.getId()
                    ,ColHandleType.finish,attitude,content,pr.getCurrentNodeLast());
            
            if(!"true".equals(ret.get("success"))){
                String  msg = ret.get("msg");
                List<String> msgs = new ArrayList<String>();
                msgs.add(msg);
                String step = WorkflowMatchLogMessageConstants.step5;
                int state =2;
                if(isRepeatSkip){
                    step = WorkflowMatchLogMessageConstants.step6;
                    state = 3 ;
                }
                wapi.putWorkflowMatchLogToCache(step, matchRequestToken, affair.getActivityId().toString(), "", msgs); 
                wapi.saveMatchProcessLog(state,matchRequestToken, colSummary.getProcessId(), bpmActivity, AppContext.getCurrentUser(), AppContext.currentUserName());
                isSucess = false;   
                LOGGER.info("DEE执行失败 - affair.id:"+affair.getId()+",msg:"+msg);
            }
        }
        return isSucess;
    }

    public void transRepeatAffairProcessing(Long affairId, String commentId, String _policyName,Map<String, String> parameters) throws BusinessException {
        CtpAffair affair;
        try {
            affair = affairManager.get(affairId);
            if (!Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(affair.getState())) {
                LOGGER.info("事项已经不需要重复跳过，状态值不符合,affairid:" + affair.getId());
                return;
            }
            ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
            Comment c = null;

            String mergeDealType = parameters.get("mergeDealType");
            String agentId = parameters.get("_skipAgentId");
            
            boolean needComment = true;
            if (Strings.isNotBlank(commentId) && !"0".equals(commentId)) {
            	needComment = false;
                Long _commentId = Long.valueOf(commentId);
                c = ctpCommentManager.getComment(_commentId);
                if (!c.getCreateId().equals(affair.getMemberId())) {//当前意见不是affair对应的人员的
                	needComment = true;
                	c = null;
                }
            }
            if (needComment) { // 任意节点重复跳过的时候取任意一个非空节点的意见。
                
                List<Comment> comments = new ArrayList<Comment>();
                if (Strings.isBlank(agentId) || "0".equals(agentId)) {
                    comments = ctpCommentManager.findCommentsList(ModuleType.collaboration,
                            Comment.CommentType.comment, affair.getObjectId(), affair.getMemberId());
                } else {// 如果是因代理处理而跳过，查找代理处理的评论
                    comments = ctpCommentManager.findCommentsList(ModuleType.collaboration,
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
								&& !"collaboration.dealAttitude.cancelProcess".equals(comment.getExtAtt3())
								&& !CommentExtAtt1Enum.disagree.name().equals(comment.getExtAtt1())) {
							c = comment;
							break;
						}
                	}
                    
                    if(c == null){
                        c = comments.get(0);
                        if(c!= null && ("collaboration.dealAttitude.rollback".equals(c.getExtAtt3())
								|| "collaboration.dealAttitude.cancelProcess".equals(c.getExtAtt3()))){
							c = null;
						}
                    }
                }
            }
            //后续节点不管前一节点是否是代理人处理。
            if(c!=null){
                c.setExtAtt2(null);
                c.setHasWfOperation(false);
            }
            autoRepeatSkipDealByObject(affair, summary, c, _policyName,mergeDealType,agentId,parameters);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
    }
        private void autoRepeatSkipDealByObject(CtpAffair affair,ColSummary summary,Comment c,String _policyName ,String mergeDealType,String agentId,Map<String,String> parameters) throws BPMException{
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

        @Override
        public void updateAffairState4RepeatAffair(CtpAffair affair,String forwardMember) {
            affair.setState(StateEnum.col_pending.getKey());
            try {
                affairManager.updateAffair(affair);
                
                try{
                    sendMsg4RepeatAutoSkip(affair,forwardMember);
                } catch (Exception e1) {
                    LOGGER.error("",e1);
                }
        
            } catch (Throwable e1) {
                LOGGER.error("",e1);
            }
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
            
            PopResult pr = wapi.isPop(wfContext);
           // LOGGER.info("r0:="+result[0]+";r1:="+result[1]+";r2:="+result[2] + "; dynamicFormMasterIds=" + result[3]);
            return pr;
        }


        private void sendMsg4RepeatAutoSkip(CtpAffair affair,String forwardMember) throws BusinessException {
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            
            colMessageManager.getReceiver(affair, ApplicationCategoryEnum.collaboration.key(), receivers, receivers1);
            
             //设置正文内容，用来发送邮件的时候显示正文内容
             String  _content = "";
             List<CtpContentAll> contentList=ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, affair.getObjectId());
             if(contentList.size()>0){
                 CtpContentAll content=contentList.get(0);
                _content = content.getContent();
             }
            V3xOrgMember sender = orgManager.getMemberById(affair.getSenderId());
            
            
            String forwardMemberId = forwardMember;
            int forwardMemberFlag = 0;
            String forwardMemberName = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMemberName = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
            
            Integer  messageFilter = ColUtil.getImportantLevel(affair);
            
            Object[] subjects = new Object[] { affair.getSubject(), sender.getName(), forwardMemberFlag, forwardMemberName, 0,"",0 };
            try {
                MessageContent content = MessageContent.get("col.send", subjects).setBody(Strings.toText(_content), affair.getBodyType(), affair.getReceiveTime()).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers, messageFilter);
            } catch (Exception e) {
                LOGGER.error("发起协同消息提醒失败!", e);
            }
                
             
            if(receivers1.size() != 0){
               try {
                   MessageContent content1 = MessageContent.get("col.send", subjects).setBody(_content, affair.getBodyType(), affair.getReceiveTime()).add("col.agent").setImportantLevel(affair.getImportantLevel());
                   if(null != affair.getTempleteId()) {
                       content1.setTemplateId(affair.getTempleteId());
                   }
                   userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers1, messageFilter);
               } catch (Exception e) {
                   LOGGER.error("发起协同消息提醒失败!", e);
               }
            }
        }
    //overFlag 是否是超期跳过的属性
    private String[] finishWorkItem(Comment c, String appName, CtpAffair affair,
                                    Map<String, Object> fieldDataBaseMap, Long workItemId, BPMActivity bpmActivity, Object summary,
                                    AffairData affairData,
                                    Long needCloneBaseCommentId, Map<String, Object> globalMap, PopResult pr,Map<String,Object> condtiontMap ) throws Exception {

        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setCurrentUserId(affair.getMemberId().toString());
        wfContext.setCurrentAccountId(orgManager.getMemberById(affair.getMemberId()).getOrgAccountId().toString());
        wfContext.setCurrentWorkitemId(workItemId);
        wfContext.setBusinessData(EventDataContext.CTP_FORM_DATA, fieldDataBaseMap);
        wfContext.setCurrentActivityId(bpmActivity.getId());
        wfContext.setSysAutoFinishFlag(true);
        wfContext.setBusinessData("operationType", WorkFlowEventListener.AUTOSKIP);
        wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, WorkFlowEventListener.AUTOSKIP);
        wfContext.setAppName(appName);
        wfContext.setAppObject(summary);
        wfContext.setConditionsOfNodes(pr.getConditionsOfNodes());
        wfContext.setDynamicFormMasterIds(pr.getDynamicFormMasterIds());
        
        if (summary instanceof ColSummary) {
            ColSummary cs = (ColSummary) summary;
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
        wfContext.setBusinessData(WorkFlowEventListener.CTPAFFAIR_CONSTANT, affair);
        wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
        wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_ID, needCloneBaseCommentId);
        if ("collaboration.affair.skipExtended".equals(c.getExtAtt3())) {
            wfContext.setBusinessData(WorkFlowEventListener.AFFAIR_SUB_STATE, String.valueOf(SubStateEnum.col_done_overtimeskip.getKey()));
        }
        wfContext.setBusinessData(WorkFlowEventListener.WF_APP_GLOBAL, globalMap);
        wfContext.setBussinessId(String.valueOf(affair.getObjectId()));
        wfContext.setAffairId(String.valueOf(affair.getId()));

        wfContext.setCurrentNodeLast(pr.getCurrentNodeLast());
        wfContext.setBusinessData("currentNodeLast", pr.getCurrentNodeLast());
        String[] result = wapi.transFinishWorkItem(wfContext);

        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affair.getId());


        return result;
    }
      
    private Permission getPermissionByAffair(ColSummary colSummary, CtpAffair affair) throws BusinessException {
        String configItem = ColUtil.getPolicyByAffair(affair).getId();
        Long accountId = ColUtil.getFlowPermAccountId(colSummary.getOrgAccountId(), colSummary);
        Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), configItem,
                accountId);
        return permission;
    }
    
    private  NodeOverTimeAutoRunCheckCode sysAutoRunCase4RepeatSkip(String matchRequestToken,ColSummary colSummary, 
            CtpAffair affair, Comment c, BPMSeeyonPolicy bpmSeeyo1nPolicy, BPMActivity bpmActivity, 
             String _policyName, 
            String mergeDealType, String agentId,PopResult pr) throws Exception {
        
        Comment comment = null;
        Permission permission = getPermissionByAffair(colSummary, affair);
        String defaultAttitude = permission.getNodePolicy().getDefaultAttitude();
        String attitude = permission.getNodePolicy().getAttitude();
        DetailAttitude detailAttitude = permission.getNodePolicy().getDatailAttitude();
        
        Long needCloneBaseCommentId = 0l;

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
            // 合并节点将态度置空，取默认态度
            comment.setExtAtt1(null);
            String _attitude = getDisplayAttitude(comment, false, defaultAttitude, attitude, detailAttitude);
            comment.setExtAtt1(_attitude);
            comment.setExtAtt4(attitudeManager.getAttitudeCodeByPermission(_attitude, permission));

            comment.setPraiseNumber(0);
            comment.setPraiseToComment(false);
            comment.setPraiseMemberIds(null);
            
            // V7.1SP1-F1-12 审批意见，流程节点设置合并处理后，不显示同样的审批意见（出现过多的审批处理记录）
            comment.setContent(null);
            comment.setRichContent(null);
            comment.setPraiseToSummary(false); // 点赞信息也不需要复制
            // 合并处理后，没有了审批意见，所以也不需要隐藏
            comment.setHidden(false);
            comment.setShowToId(null);
            
        }
        else {
            comment = new Comment();
            comment.setHidden(false);
            comment.setId(UUIDLong.longUUID());
            // 意见设置为自动跳过

            String _attitude = getDisplayAttitude(comment, true, defaultAttitude, attitude, detailAttitude);
            comment.setExtAtt1(_attitude);

            comment.setCtype(Comment.CommentType.comment.getKey());
            comment.setPid(0L);
            comment.setPraiseNumber(0);
            comment.setPraiseToComment(false);
            comment.setModuleType(ModuleType.collaboration.getKey());
            comment.setModuleId(colSummary.getId());
            comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
            if (affair != null) {
                comment.setAffairId(affair.getId());
                comment.setCreateId(affair.getMemberId());
            }
            comment.setExtAtt3(CommentExtAtt3Enum.repeat_auto_skip.getI18nLabel());

            needCloneBaseCommentId = comment.getId();
        }
        ColUtil.addOneReplyCounts(colSummary);

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
        if(affair != null){
			comment.setDepartmentId(affair.getMatchDepartmentId());
			comment.setPostId(affair.getMatchPostId());
			comment.setAccountId(affair.getMatchAccountId());
		}

        
        NodeOverTimeAutoRunCheckCode code =colAutoRun(matchRequestToken,colSummary, affair, bpmActivity, 
                 comment, needCloneBaseCommentId,
                true, _policyName,mergeDealType,pr,null);
        return code;
    }

    /**
     * 重复跳过节点的意见态度没有时，应该取默认态度；有时取上节点态度意见；
     *
     * @param comment
     * @param lastNodeIsSender
     * @param defaultAttitude 节点权限设置的默认态度
     * @param attitude 节点显示态度
     * @throws BusinessException
     */
    private String getDisplayAttitude(Comment comment, boolean lastNodeIsSender,String defaultAttitude,  String attitude, DetailAttitude detailAttitude)
            throws BusinessException {
    	//可能存在节点权限设置的默认态度,没有在显示态度中.比如:默认态度为已阅,节点态度为:同意\不同意.可能就会取到haveRead
    	String nodeDefaultAttitude = "";//默认态度
        String _attitude = "";
        List<String> attitudeList = new ArrayList<String>();
        if (Strings.isNotBlank(attitude)) {
        	List<String> attitudes = Arrays.asList(attitude.split(","));
        	if (null != detailAttitude) {
        		for (String att : attitudes) {
        			String attitudeValue = "";
        			if ("haveRead".equals(att)) {
        				attitudeValue = detailAttitude.getHaveRead();
        			} else if ("agree".equals(att)) {
        				attitudeValue = detailAttitude.getAgree();
        			} else if ("disagree".equals(att)) {
        				attitudeValue = detailAttitude.getDisagree();
        			}
        			attitudeList.add(attitudeValue);
        			if (att.equals(defaultAttitude)) {
        				nodeDefaultAttitude = attitudeValue;
        			}
        		}
			}
        	if (Strings.isBlank(nodeDefaultAttitude) && attitudeList.size() > 0) {//没有默认态度时,默认取第一个
        		nodeDefaultAttitude = attitudeList.get(0);
        	}
        }
        try {
        	if (Strings.isNotBlank(attitude)) {
        		String commentAttitude = comment.getExtAtt1();
        		if (!lastNodeIsSender && Strings.isNotBlank(commentAttitude) && attitudeList.contains(commentAttitude)) {//上节点有态度时,并且当前节点权限有该态度
        			_attitude = commentAttitude;
        		} else {
        			_attitude = nodeDefaultAttitude;
        		}
            } // 不显示态度
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return _attitude;
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
    private boolean canRepeatSkipAutoDeal(String matchRequestToken,CtpAffair affair, ColSummary summary, BPMSeeyonPolicy bpmSeeyonPolicy,
            BPMActivity bpmActivity, PopResult pr,Comment c,Map<String,String> parameters) throws BusinessException {
        List<String> cannotMsgList= new ArrayList<String>();
        String autoSkipNodeId= affair.getActivityId().toString();
        boolean result= true;
        try{
            
            String resonI18n = "collaboration.cant.skip.reson.";
            if(!parameters.isEmpty() && parameters.containsKey("_cantSkipResonMap")){
                String _skipMap = parameters.get("_cantSkipResonMap");
                HashMap<String,String> parseJSONString = JSONUtil.parseJSONString(_skipMap,HashMap.class);
                if(!parseJSONString.isEmpty() && Strings.isNotBlank(parseJSONString.get(String.valueOf(affair.getId())))){
                    resonI18n =  resonI18n + parseJSONString.get(String.valueOf(affair.getId()));
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
            String configItem = ColUtil.getPolicyByAffair(affair).getId();

            String category = EnumNameEnum.col_flow_perm_policy.name();
            Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            Permission permission = permissionManager.getPermission(category, configItem, accountId);	
        	
        	if (permission!=null && !permission.getCanBackgroundDeal()) {// B节点是核定、新闻审批、公告审批、系统自定节点时，不能跳过。
        		LOGGER.info("节点权限不能重复跳过:affairId:" + affair.getId() + ",nodepolicy:" + affair.getNodePolicy());
        		//节点权限为{0}
        		cannotMsgList.add(ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_POLICY, permission.getLabel()));
        		
        		result = false;
        	}
        	
            // 前面节点触发的子流程是否已经结束
            boolean isPreNewFlowFinish = true;
            // B节点有必填项时不能自动跳过。
            if (ColUtil.isForm(affair.getBodyType())) {// 表单类型的数据
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
            try {
                if (permission != null) {
                	Integer opinionPolicy = permission.getNodePolicy().getOpinionPolicy();
                    Integer disAgredOpinionPlicy = permission.getNodePolicy().getDisAgreeOpinionPolicy();
                    boolean _disAgredOpinionPlicy = null != c &&  Integer.valueOf(1).equals(disAgredOpinionPlicy) && CommentExtAtt1Enum.disagree.getI18nLabel().equals(c.getExtAtt1()) ;
                    //LOG.error("AFFAIR.ID:"+affair.getId()+",disAgredOpinionPlicy:"+disAgredOpinionPlicy+",c.id:"+c.getId()+",c.getExtAtt1():"+c.getExtAtt1()+",END_disAgredOpinionPlicy:"+_disAgredOpinionPlicy+",t:"+(CommentExtAtt1Enum.disagree.getI18nLabel().equals(c.getExtAtt1())));
                    if (Integer.valueOf(1).equals(opinionPolicy) || _disAgredOpinionPlicy) {
                        boolean skipFlag =false;
                        LOGGER.info("当处理人为同一个连续处理人时，只要上一次的意见存在就重复跳过，  affairId:=" + affair.getId());
                       
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
                LOGGER.info("该协同待办存在环形分支，不能重复跳过，  affairId:=" + affair.getId());
                String msg= ResourceUtil.getString(WorkflowMatchLogMessageConstants.NODE_BIND_LINK_CIRCLE);
                cannotMsgList.add(msg);
                result= false;
            }
            
            
           
           CanBackgroundDealParam param = new CanBackgroundDealParam();
           param.setAffair(affair);
           param.setComment(c);
           CanBackgroundDealResult cr =  ColSelfUtil.publicCheckCanBackgroundDeal(param);
           if(!cr.isCan()){
               LOGGER.info("不能重复跳过，  affairId:=" + affair.getId()+",msg:"+cr.getMsg());
               cannotMsgList.add(cr.getMsg());
               result= false;
           }
                
            return result;
        }finally{
            wapi.putWorkflowMatchLogToCache(WorkflowMatchLogMessageConstants.step6, matchRequestToken, autoSkipNodeId, "", cannotMsgList); 
        }
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
}
