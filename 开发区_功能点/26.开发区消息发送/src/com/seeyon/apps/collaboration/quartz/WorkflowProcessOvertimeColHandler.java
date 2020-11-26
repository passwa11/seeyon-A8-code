package com.seeyon.apps.collaboration.quartz;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.dee.api.CollaborationFormBindEventListener;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.quartz.ProcessAutoStopRepealCaseBO;
import com.seeyon.ctp.common.affair.quartz.WorkflowProcessOvertimeAppHandler;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.batch.exception.BatchException;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.lock.manager.LockState;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.NoSuchQuartzJobBeanException;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.trace.enums.WFTraceConstants;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class WorkflowProcessOvertimeColHandler implements WorkflowProcessOvertimeAppHandler {
	private static Log LOG = CtpLogFactory.getLog(WorkflowProcessOvertimeColHandler.class);
	private ColManager colManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private AppLogManager appLogManager;
    private ProcessLogManager processLogManager;
    private CommentManager     ctpCommentManager;
    private WorkflowApiManager           wapi;
    private UserMessageManager userMessageManager;
    private LockManager			lockManager;
    private CollaborationFormBindEventListener collaborationFormBindEventListener;
    private TemplateManager templateManager;
    private SuperviseManager superviseManager;
    private CAPFormManager capFormManager;
    private MessageRuleManager messageRuleManager;
    private FormApi4Cap4 formApi4Cap4;
    private MobileMessageManager mobileMessageManager;
    private FormApi4Cap3 formApi4Cap3;
    private ColMessageManager colMessageManager;
    
    
    public MobileMessageManager getMobileMessageManager() {
		return mobileMessageManager;
	}

	public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
		this.mobileMessageManager = mobileMessageManager;
	}

	public FormApi4Cap4 getFormApi4Cap4() {
		return formApi4Cap4;
	}

	public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
		this.formApi4Cap4 = formApi4Cap4;
	}
	public MessageRuleManager getMessageRuleManager() {
		return messageRuleManager;
	}

	public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
		this.messageRuleManager = messageRuleManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public AppLogManager getAppLogManager() {
		return appLogManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public ProcessLogManager getProcessLogManager() {
		return processLogManager;
	}

	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}
	private EnumManager enumManagerNew;
	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}

	public CommentManager getCtpCommentManager() {
		return ctpCommentManager;
	}

	public void setCtpCommentManager(CommentManager ctpCommentManager) {
		this.ctpCommentManager = ctpCommentManager;
	}

	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}

	public ColManager getColManager() {
		return colManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	
	public void setCollaborationFormBindEventListener(
			CollaborationFormBindEventListener collaborationFormBindEventListener) {
		this.collaborationFormBindEventListener = collaborationFormBindEventListener;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public CAPFormManager getCapFormManager() {
		return capFormManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}
	public void setColMessageManager(ColMessageManager colMessageManager) {
		this.colMessageManager = colMessageManager;
	}

	@Override
	public void updateProcessOvertimeInfo(Object summaryobj) throws BusinessException {
	   
		ColSummary summary = (ColSummary)summaryobj;
		
		Boolean isOvertopTime = summary.isCoverTime();
        if(isOvertopTime != null && !isOvertopTime){
             summary.setCoverTime(true);
             colManager.updateColSummary(summary);
        }
        
       
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("processOverTime",true);
		affairManager.update(m, new Object[][]{{"objectId",summary.getId() }});
	}

	public ProcessMsgParamBO getMessageParam(Object summaryobj) throws BusinessException {
		
		ColSummary summary = (ColSummary)summaryobj;
		
		ProcessMsgParamBO messageBO = new ProcessMsgParamBO();
		messageBO.setSubject(summary.getSubject());
		messageBO.setMessageSentLink( "message.link.col.done.detail");
		messageBO.setMessagePendingLink("message.link.col.pending");
		messageBO.setAppEnum(ApplicationCategoryEnum.collaboration);
		messageBO.setProcessSenderId( summary.getStartMemberId());
		messageBO.setImportantLevel(summary.getImportantLevel());
		
		return messageBO;
	}

	@Override
	public boolean isNeedJobExcute(Object summaryobj) throws BusinessException {
		
		ColSummary summary = (ColSummary)summaryobj;
		
		boolean isGo = true;
		//协同被删除或者完成,不做提醒
		if(summary == null || summary.getFinishDate() != null) {
			isGo = false;
		}
		return isGo;
	}

	@Override
	public Object getSummaryObject(long summaryId) throws BusinessException {
		ColSummary summary = colManager.getColSummaryById(summaryId);
		return summary;
	}

	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.collaboration;
	}

	public void transStopProcess(Object summary,ProcessMsgParamBO messageBO) throws BusinessException {
		
		ColSummary s = (ColSummary)summary;
		User user = new User();
		CtpAffair sendAffair = affairManager.getSenderAffair(s.getId());
		if (sendAffair != null) {
			user = createCurrentUser(sendAffair.getSenderId());
		}
		//自由协同：流程期限到时自动终止处理
		Long processId = s.getProcessId() ==null?null:Long.valueOf(s.getProcessId());
		ProcessAutoStopRepealCaseBO stopCaseBO = checkAndupdateLockForQuartz(sendAffair,processId,11);
		if(stopCaseBO != null){
			try{
				if(stopCaseBO.getCanAutoStop()){

					stopCaseBO = this.canAutostopflow(summary);

					if(stopCaseBO.getCanAutoStop()){
						stopCaseBO = autoStopFlow(s,messageBO,user);
					}

				}
			}catch(Exception e){//工作流组件出错了
				stopCaseBO.setErrorMsg(ResourceUtil.getString("collaboration.auto.workflow.error"));
				stopCaseBO.setCanAutoStop(false);
				stopCaseBO.setCanReCreate(false);
				LOG.error("", e);
			}finally{//解除锁
				releaseLockForQuartz(sendAffair,Long.valueOf(s.getProcessId()),11);
			}
		}

		boolean sendMessageToDoneMember = true;
		//写流程日志和应用日志
		String processLogMsg = ResourceUtil.getString("collaboration.auto.stop.sucess.processlog");
		String appLogMsg = ResourceUtil.getString("collaboration.auto.stop.sucess.applog", s.getSubject());
		//发系统消息
		String msgKey = "process.summary.overTerm.stopflow";
		//发系统消息
		if(!stopCaseBO.getCanAutoStop()){
			//写流程日志和应用日志
			processLogMsg = ResourceUtil.getString("collaboration.auto.stop.error.processlog", s.getSubject(),stopCaseBO.getErrorMsg(),stopCaseBO.isCanReCreate()?1:0);
			appLogMsg = ResourceUtil.getString("collaboration.auto.stop.error.applog", s.getSubject(),stopCaseBO.getErrorMsg(),stopCaseBO.isCanReCreate()?1:0);
			//发系统消息
			msgKey = "process.summary.overTerm.stopflow.error";
			
			sendMessageToDoneMember = false;
		}
		sendMessageAndInsertLog(s,messageBO,user,ProcessLogAction.processAutoStop,AppLogAction.Coll_Flow_Auto_Stop,msgKey,processLogMsg,appLogMsg,sendMessageToDoneMember,new HashMap<String,Object>());
	}
	/**
	 * 逾期自动终止流程处理
	 * @throws BatchException 
	 */
	private ProcessAutoStopRepealCaseBO autoStopFlow(ColSummary summary,ProcessMsgParamBO messageBO,User user) throws BusinessException, BatchException{
		ProcessAutoStopRepealCaseBO autoStopFlow = new ProcessAutoStopRepealCaseBO();
		List<CtpAffair> curerntAffairs = affairManager.getAffairs(summary.getId(),StateEnum.col_pending);
		//CtpAffair currentAffair = affairManager.getSenderAffair(summary.getId());
		CtpAffair currentAffair = null;
		if(Strings.isNotEmpty(curerntAffairs)){
			currentAffair = curerntAffairs.get(0);
		}else {
			autoStopFlow.setCanAutoRepeal(false);
			autoStopFlow.setCanAutoStop(false);
			autoStopFlow.setCanReCreate(true);
			autoStopFlow.setErrorMsg(ResourceUtil.getString("workflow.stop.not.pending.alert.js"));
			CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
			//Date nextRunTime= new Date((System.currentTimeMillis()+24*60*60*1000));
			//Date nextRunTime= new Date((System.currentTimeMillis()+30*1000));
			Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
			reCreateColProcessDeadLineQuartz(sendAffair,nextRunTime);
		    
		    autoStopFlow.setNextRunTime(nextRunTime);
		    return autoStopFlow;
		}
		
        CtpAffair theStopAffair= new CtpAffair();
        if (currentAffair != null) {
            V3xOrgMember sender = orgManager.getMemberById(currentAffair.getSenderId());
            user.setId(sender.getId());
            user.setDepartmentId(sender.getOrgDepartmentId());
            user.setLoginAccount(sender.getOrgAccountId());
            user.setLoginName(sender.getLoginName());
            user.setName(sender.getName());
            try {
				theStopAffair = (CtpAffair) currentAffair.clone();
				theStopAffair.setId(currentAffair.getId());
			} catch (CloneNotSupportedException e) { 
				LOG.info("",e);
			}
            
        }
        //设置相关变量
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
        //保存终止时的意见
        Comment comment = new Comment();
        comment.setAffairId(null!=currentAffair?currentAffair.getId():null);
        comment.setHidden(false);
        comment.setId(UUIDLong.longUUID());
        String content= ResourceUtil.getString("collaboration.auto.stop.opinion");
        comment.setContent(content);
        comment.setPid(0L);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateId(summary.getStartMemberId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setExtAtt3(CommentExtAtt1Enum.stepStop.i18nLabel());
        comment.setCtype(Comment.CommentType.comment.getKey());
        
       
        theStopAffair.setSenderId(user.getId());//发起协同人员id
        theStopAffair.setMemberId(user.getId());//处理协同人员id
        theStopAffair.setObjectId(summary.getId());//协同id
        theStopAffair.setApp(ApplicationCategoryEnum.collaboration.ordinal());//协同类型
        theStopAffair.setSubApp(summary.getTempleteId() == null ? ApplicationSubCategoryEnum.collaboration_self.key() : ApplicationSubCategoryEnum.collaboration_tempate.key());
        //执行流程事件
        String errorMsg =  executeWorkflowBeforeEvent(theStopAffair, summary.getProcessId(), "BeforeStop",comment);
        if(Strings.isBlank(errorMsg)){
    		Map<String,String> ret = excuteDee(summary, theStopAffair,comment,ColHandleType.stepStop);
    		if(!"true".equals(ret.get("success"))){
    			errorMsg = ret.get("msg");
    		}
    	}
        if(Strings.isBlank(errorMsg)){
        	//终止工作流组件
        	WorkflowBpmContext wfContext = new WorkflowBpmContext();
        	wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID,theStopAffair.getMemberId());
        	wfContext.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_ID, comment.getId());
        	wfContext.setBusinessData(WorkFlowEventListener.OPERATION_TYPE, WorkFlowEventListener.STETSTOP);
        	wfContext.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT, summary);
        	wfContext.setBusinessData(WorkFlowEventListener.CTPAFFAIR_CONSTANT,theStopAffair);
        	wfContext.setBusinessData("isAutoStop", "1");
        	wfContext.setBussinessId(String.valueOf(summary.getId()));
        	
            Long formRecordId = summary.getFormRecordid();
            Long formAppId = summary.getFormAppid();
            wfContext.setMastrid(formRecordId == null ? null : String.valueOf(formRecordId));
            wfContext.setFormData(formAppId == null ? null : String.valueOf(formAppId));
            
            wfContext.setAppObject(summary);
            wfContext.setAppName("collaboration");
            wfContext.setBusinessData("operationType", WorkFlowEventListener.STETSTOP);
            wfContext.setCurrentUserId(String.valueOf(user.getId()));
        	
        	if (currentAffair != null && currentAffair.getSubObjectId() != null) {
        		wfContext.setCurrentWorkitemId(currentAffair.getSubObjectId());
        	}
        	
        	wapi.stopCase(wfContext);
        	
        	ctpCommentManager.insertComment(comment);
        	
        	
        	
        	//工作流中更新了状态信息，重新获取，表单会用state字段
            summary = colManager.getColSummaryById(currentAffair.getObjectId());
            
            //终止 流程结束时更新督办状态
            superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised,summary.getId(),SuperviseEnum.EntityType.summary);
            //调用表单万能方法,更新状态，触发子流程等
            if(ColUtil.isForm(summary.getBodyType())){
                try {
                   List<Comment> commentList = ctpCommentManager.getCommentList(ModuleType.collaboration, currentAffair.getObjectId()); 
                   capFormManager.updateDataState(summary,currentAffair,ColHandleType.stepStop,commentList);
                } catch (Exception e) {
                   LOG.error("更新表单相关信息异常",e);
                }
            }
            
            
            // 若做过指定回退的操作.做过回退发起者则被回退者的状态要从待发改为已发
            CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
            if (sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
                    || sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                sendAffair.setState(StateEnum.col_sent.getKey());
                //affair更新状态时需要更新此冗余字段
                sendAffair.setSummaryState(summary.getState());
                sendAffair.setUpdateDate(new java.util.Date());
                affairManager.updateAffair(sendAffair);
            }
            
            //记录操作时间
            affairManager.updateAffairAnalyzeData(currentAffair);
            ColUtil.addOneReplyCounts(summary);
            colManager.updateColSummary(summary);
            
            //流程正常结束通知
            CollaborationStopEvent stopEvent = new CollaborationStopEvent(this);
            stopEvent.setSummaryId(summary.getId());
            stopEvent.setUserId(user.getId());
            stopEvent.setSenderId(summary.getStartMemberId());
            stopEvent.setTemplateId(summary.getTempleteId());
            stopEvent.setBodyType(summary.getBodyType());
            stopEvent.setAffair(currentAffair);
            if(Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(summary.getNewflowType()) && Strings.isNotBlank(summary.getProcessId())){
                Long mainProcessId = wapi.getMainProcessIdBySubProcessId(Long.valueOf(summary.getProcessId()));
                stopEvent.setMainProcessId(mainProcessId);
            }
            EventDispatcher.fireEventAfterCommit(stopEvent);
            
            autoStopFlow.setCanAutoStop(true);
        }else{
        	
        	autoStopFlow.setErrorMsg(errorMsg);
        }
        
        return autoStopFlow;
	}
	@Override
	public Integer getMsgFilterKey(Object object) throws BusinessException {
		ColSummary summary = (ColSummary)object;
		return ColUtil.getImportantLevel(summary);
	}

    @Override
    public long getAdvanceRemind(Object object) {
        
        ColSummary summary = (ColSummary)object;
        Long ret = summary.getAdvanceRemind();
        if(ret == null){
            ret = 0l;
        }
        return ret;
    }

    @Override
    public ProcessAutoStopRepealCaseBO canAutostopflow(Object summary) throws BusinessException {
        ColSummary s = (ColSummary)summary;
        ProcessAutoStopRepealCaseBO autoStopRepealCaseBO = new ProcessAutoStopRepealCaseBO();
    	String[] result = wapi.canStopFlow(s.getCaseId()+"");
    	if("true".equals(result[0])){
    		autoStopRepealCaseBO.setCanAutoStop(true);
    	}else{
    		autoStopRepealCaseBO.setCanAutoStop(false);
    		autoStopRepealCaseBO.setErrorMsg(result[1]);
    	}
        return autoStopRepealCaseBO;
    }
    
    
    /**
	 * 流程是否可以自动撤销
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
    @Override
	public ProcessAutoStopRepealCaseBO canAutoRepealFlow(Object summary) throws BusinessException{
    	ColSummary s = (ColSummary)summary;
    	ProcessAutoStopRepealCaseBO autoStopRepealCaseBO = new ProcessAutoStopRepealCaseBO();
    	Map<String,String> param = new HashMap<String,String>();
    	param.put("summaryId", s.getId().toString());
    	Map<String,String> map = colManager.checkIsCanRepeal(param);
    	String msg = map.get("msg");
    	if(Strings.isNotBlank(msg)){
    		autoStopRepealCaseBO.setCanAutoRepeal(false);
    		autoStopRepealCaseBO.setErrorMsg(msg);
    		return autoStopRepealCaseBO;
    	}
    	String[] result = wapi.canRepeal(ApplicationCategoryEnum.collaboration.name(), String.valueOf(s.getProcessId()), "start");
    	if("true".equals(result[0])){
    		autoStopRepealCaseBO.setCanAutoRepeal(true);
    	}else{
    		autoStopRepealCaseBO.setCanAutoRepeal(false);
    		autoStopRepealCaseBO.setErrorMsg(result[1]);
    	}
        return autoStopRepealCaseBO;
	}
	
	/**
	 * 执行自动撤销
	 * @param summary
	 * @param messageBO
	 * @throws BusinessException
	 */
	public void transRepealProcess(Object summary, ProcessMsgParamBO messageBO) throws BusinessException{

		
		ColSummary s = (ColSummary)summary;
		User user = new User();
		CtpAffair senderAffair = affairManager.getSenderAffair(s.getId());
		if (senderAffair != null) {
			user = createCurrentUser(senderAffair.getSenderId());
		}
		Long processId = s.getProcessId() ==null?null:Long.valueOf(s.getProcessId());

		ProcessAutoStopRepealCaseBO repealCaseBO = checkAndupdateLockForQuartz(senderAffair,processId, 12);
		//获取到流程锁才往下走
		if(repealCaseBO != null){
			try{
				if(repealCaseBO.getCanAutoRepeal()){

					repealCaseBO = this.canAutoRepealFlow(summary);

					if(repealCaseBO.getCanAutoRepeal()){

						repealCaseBO = this.autoRepealFlow(s,messageBO,user);

					}

				}
			}catch(Exception e){//工作流组件出错了
				repealCaseBO.setCanReCreate(false);
				repealCaseBO.setErrorMsg(ResourceUtil.getString("collaboration.auto.workflow.error"));
				repealCaseBO.setCanAutoRepeal(false);
				LOG.error("", e);
			}finally{//解除锁
				releaseLockForQuartz(senderAffair,processId,12);
			}
		}

		
		boolean canSendToDoneMember = true;
		String processLogMsg = ResourceUtil.getString("collaboration.auto.repeal.sucess.processlog");
    	String appLogMsg = ResourceUtil.getString("collaboration.auto.repeal.sucess.applog", s.getSubject());
    	//发系统消息
    	String msgKey = "process.summary.overTerm.repealflow";
    	
		if(!repealCaseBO.getCanAutoRepeal()){
        	//写流程日志和应用日志
        	processLogMsg = ResourceUtil.getString("collaboration.auto.repeal.error.processlog", s.getSubject(),repealCaseBO.getErrorMsg(),repealCaseBO.isCanReCreate()?1:0);
        	appLogMsg = ResourceUtil.getString("collaboration.auto.repeal.error.applog", s.getSubject(),repealCaseBO.getErrorMsg(),repealCaseBO.isCanReCreate()?1:0);
        	//发系统消息
        	msgKey = "process.summary.overTerm.repealflow.error";
        	canSendToDoneMember = false;
		}
		
		sendMessageAndInsertLog(s,messageBO,user,ProcessLogAction.processAutoRepeal,AppLogAction.Coll_Flow_Auto_Repeal,msgKey,
		        processLogMsg,appLogMsg,canSendToDoneMember,repealCaseBO.getBusinessData());
	
	}
    
    /**
	 * 获取自动终止和撤销的标志
	 * @param summary
	 * @return
	 */
    @Override
	public int getAutoStopRepealFlag(Object summary){
    	ColSummary s = (ColSummary)summary;
    	if(s.getCanAutostopflow()){//兼容历史数据，兼容自动协同设置了超期自动终止的情况
    		return CollaborationEnum.processTermType.autoStop.ordinal();
    	}
		return s.getProcessTermType()==null?-1:s.getProcessTermType();
	}

    private ProcessAutoStopRepealCaseBO autoRepealFlow(ColSummary summary,ProcessMsgParamBO messageBO,User user) throws BusinessException, BatchException{
    	CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
    	String errorMsg = "";
    	Comment comment = new Comment();
    	comment.setContent(ResourceUtil.getString("collaboration.auto.repeal.opinion"));
    	errorMsg = executeWorkflowBeforeEvent(sendAffair, summary.getProcessId(), "BeforeCancel",comment);
    	
    	if(Strings.isBlank(errorMsg)){
    		Map<String,String> ret = excuteDee(summary, sendAffair,null,ColHandleType.repeal);
    		if(!"true".equals(ret.get("success"))){
    			errorMsg = ret.get("msg");
    		}
    	}
    	
    	ProcessAutoStopRepealCaseBO autoRepealCaseBo = new ProcessAutoStopRepealCaseBO();
    	if(Strings.isBlank(errorMsg)){
    		Integer isWFTrace = 0;
    		if(null!=summary.getTempleteId()){
    			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
    			if(null!=ctpTemplate){
    				isWFTrace = ctpTemplate.getCanTrackWorkflow();
    			}
    		}
    		
    		Map<String,Object> tempMap=new HashMap<String, Object>();
    		tempMap.put("affairId", sendAffair.getId().toString());
    		tempMap.put("summaryId", summary.getId().toString());
    		tempMap.put("repealComment", ResourceUtil.getString("collaboration.auto.repeal.opinion"));
    		//流程会用到这个参数
    		tempMap.put("isWFTrace", null==isWFTrace?"":isWFTrace.toString());
    		tempMap.put("needSendMessage", Boolean.FALSE);
    		
    		//撤销
    		Map<String,Object> result = colManager.transRepealPublic(tempMap);
    		errorMsg = (String) result.get("result");
    		autoRepealCaseBo.setBusinessData(result);
    	}
	    if(Strings.isNotBlank(errorMsg)){
		  autoRepealCaseBo.setErrorMsg(errorMsg);
	    }else{
		  autoRepealCaseBo.setCanAutoRepeal(true);
	    }
	    return autoRepealCaseBo;
    }
    
    private void sendMessageAndInsertLog(ColSummary summary,ProcessMsgParamBO messageBO,User user,
								    		ProcessLogAction processLogAction,AppLogAction appLogAction,String msgKey,
								    		String processLogMsg,String appLogMsg,boolean canSendToDoneMember,Map<String,Object> businessData) throws BusinessException{
    	
    	processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), 1l, processLogAction,processLogMsg);
		appLogManager.insertLog(user, appLogAction, appLogMsg);
		
		String subject = messageBO.getSubject();
		Integer importantLevel = messageBO.getImportantLevel();
		Long processSenderId = messageBO.getProcessSenderId();
		int forwardMemberFlag = messageBO.getForwardMemberFlag();
		String forwardMember = messageBO.getForwardMember();
		Map<Long, MessageReceiver> receiverMap = messageBO.getReceiverMap();
		Map<Long, MessageReceiver> doneReceiverMap = messageBO.getDoneReceiverMap();
		Map<Long, MessageReceiver> trackReceiverMap = messageBO.getTrackReceiverMap();
		Map<Long, MessageReceiver> receiverAgentMap  = messageBO.getReceiverAgentMap();
		
		int autoStopRepealFlag = getAutoStopRepealFlag(summary);
		boolean canRepeal = autoStopRepealFlag==CollaborationEnum.processTermType.autoRepeal.ordinal();
		
		boolean canTrack = false;
		if(null !=summary.getTempleteId()){
			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
			canTrack = Integer.valueOf(1).equals(ctpTemplate.getCanTrackWorkflow());
		}
		List<Long> alreadyTraceMes = new ArrayList<Long>();//已经发了追索消息的 就不需要发其他消息了  过滤用
		
		if(canTrack && canRepeal){
			//生成流程追溯的消息
			if(!trackReceiverMap.isEmpty()){
				//OA-138976 模板流程设置了到期后自动撤销并追溯流程，流程撤销后，
				//已办人收到撤销追溯的消息，点击穿透提示已被撤销，应该能穿透到查看详情页面
				//参考：colMessagemanager.sendMessage4Repeal
 
				List<Long> traeDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
				if(traeDataAffair == null){
                    traeDataAffair = Collections.emptyList();
                }
				
				Long sId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID);
				Long aId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID);;
				Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
				Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(trackReceiverMap.values());
				CtpAffair cloneAffair = affairManager.get(aId);
				List<MessageReceiver> needSendReceviers = new ArrayList<MessageReceiver>();
				for(MessageReceiver mr:receivers){
					Long referenceId = mr.getReferenceId();
					if (traeDataAffair.contains(referenceId)) {
						MessageReceiver receiver = new MessageReceiver(cloneAffair.getId(),mr.getReceiverId(),"message.link.col.traceRecord",ColOpenFrom.repealRecord.name(),cloneAffair.getId().toString(),type.intValue()+"");
						receiver.setTrack(true);
						needSendReceviers.add(receiver);
						alreadyTraceMes.add(mr.getReceiverId());
					}
				}
				MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember)
						.add("collaboration.summary.cancel.traceview").setImportantLevel(importantLevel);
				if (null != summary.getTempleteId()) {
					content.setTemplateId(summary.getTempleteId());
				}
				userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, processSenderId, needSendReceviers);
			}
		}else{
			if(canSendToDoneMember){
				receiverMap.putAll(doneReceiverMap);
			}
		}
		
		//给已发、已办和待办人发送消息
		if(!receiverMap.isEmpty()){
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(receiverMap.values());
			Iterator<MessageReceiver> _it = receivers.iterator();
			
			while(_it.hasNext()){
				MessageReceiver mr = _it.next();
				if(alreadyTraceMes.contains(mr.getReceiverId())){
					receivers.remove(mr);
				}
			}
			if(!receivers.isEmpty()){
				MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
				if (null != summary.getTempleteId()) {
					content.setTemplateId(summary.getTempleteId());
				}
				userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, processSenderId, receivers);
			}
		}
		
		//代理人发消息
		if(!receiverAgentMap.isEmpty()){
			Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(receiverAgentMap.values());
			MessageContent agentCount = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent");
			if (null != summary.getTempleteId()) {
				agentCount.setTemplateId(summary.getTempleteId());
			}
			userMessageManager.sendSystemMessage(agentCount, ApplicationCategoryEnum.collaboration, processSenderId, receiverAgents);
		}
    }
	
    
    
    /**
     * 
     * 获得锁(协同流程锁、表单锁和公文锁)，如果获取不到，则将定时任务向后推迟15分钟（默认规则）
     * @param processId
     * @param summaryId
     * @param affairId
     * @param formAppId
     * @param edocSummary
     * @return
     */
    private  ProcessAutoStopRepealCaseBO checkAndupdateLockForQuartz(CtpAffair affair,Long processId,int lockAction) {
    	ProcessAutoStopRepealCaseBO autoStopRepealCaseBO= new ProcessAutoStopRepealCaseBO();
    	autoStopRepealCaseBO.setCanAutoRepeal(true);
    	autoStopRepealCaseBO.setCanAutoStop(true);
    	try {
    		LOG.info("processId:="+processId+";lockOwer:="+affair.getMemberId()+";lockAction:="+lockAction);
            //流程锁
            String[] result= canLock(affair,processId,lockAction);
            autoStopRepealCaseBO.setErrorMsg(result[1]);
            if("false".equals(result[0])){
            	autoStopRepealCaseBO.setCanAutoRepeal(false);
            	autoStopRepealCaseBO.setCanAutoStop(false);
            	autoStopRepealCaseBO.setCanReCreate(true);
            	
            	Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
            	autoStopRepealCaseBO.setNextRunTime(nextRunTime);
            	
                reCreateColProcessDeadLineQuartz(affair,nextRunTime);
            }
        } catch (Throwable e) {
        	LOG.error(e.getMessage(),e);
        	autoStopRepealCaseBO.setCanAutoRepeal(false);
        	autoStopRepealCaseBO.setCanAutoStop(false);
        	autoStopRepealCaseBO.setCanReCreate(true);
        	autoStopRepealCaseBO.setErrorMsg(e.getMessage());
        }
        return autoStopRepealCaseBO;
    }

	private String[] canLock(CtpAffair affair,Long processId,int lockAction) {
		String lockMsg= "";
		Lock lock= null;
		Long invlidOwner= null;
		int invlidAction= 14;
		String invlidFrom= "";
		Long invlidTime = 0L;
        List<Lock> locks = lockManager.getLocks(processId);
        if(locks!=null && !locks.isEmpty()){
            for (Lock lk : locks) {
                if(lk!=null){
                	LOG.info("resourceid:="+lk.getResourceId()+";owner:="+lk.getOwner()+";action:="+lk.getAction());
                	invlidOwner= lk.getOwner();
                	invlidAction= lk.getAction();
                	invlidFrom= lk.getFrom();
                	invlidTime = lk.getLockTime();
                    if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                        lock = lk;
                    }else{//不是有效的锁解掉，不然后面lock不到锁，造成死循环
                    	lockManager.unlock(lk.getOwner(), lk.getResourceId(), lk.getAction());
                    	LOG.info("解掉无效的锁(resourceid:="+lk.getResourceId()+";owner:="+lk.getOwner()+";action:="+lk.getAction()+")");
                    }
                }
            }
        }
        boolean canGetLock = true;
        if(null!=lock){
        	if(lock.getAction()==15  || lock.getAction()==16 || lock.getAction()==-1){//别人只拥有除流程之外的锁，则当前人员可以申请到提交锁，否则不行
        		canGetLock= true;
            }else{
                String myAction= "";
                String myOwner= "";
                String from= "";
                Long lockTime = 0L;
                if(null!=lock){
                    myAction= String.valueOf(lock.getAction());
                    myOwner= String.valueOf(lock.getOwner());
                    from= lock.getFrom();
                    lockTime = lock.getLockTime();
                }
                if(Strings.isNotBlank(myOwner)){
                	canGetLock= false;
					try {
						V3xOrgMember lockMember = orgManager.getMemberById(Long.parseLong(myOwner));
						lockMsg= wapi.getLockMsg(myAction, lockMember.getName(), from,lockTime);//国际化处理
					}catch (Throwable e) {
						LOG.error("", e);
						canGetLock= true;
					}
                }else{
                	canGetLock= true;
                }
            }
        	
        	
        	if(canGetLock){
        		canGetLock = lockManager.lock(affair.getMemberId(), processId,lockAction, Lock.FROM_SYSTEM);
        	}
        }else{
        	canGetLock = lockManager.lock(affair.getMemberId(),processId,lockAction, Lock.FROM_SYSTEM);
        }
        if(!canGetLock && Strings.isBlank(lockMsg) && null!=invlidOwner){
        	try {
				V3xOrgMember lockMember = orgManager.getMemberById(invlidOwner);
				lockMsg= wapi.getLockMsg(invlidAction+"", lockMember.getName(), invlidFrom,invlidTime);//国际化处理
			}catch (Throwable e) {
				LOG.error("", e);
			}
        }
        String[] result= {String.valueOf(canGetLock),lockMsg};
        return result;
	}
    
    /**
     * 解锁
     * @param processId
     * @param summaryId
     * @param edocSummary
     */
    private void releaseLockForQuartz(CtpAffair affair,Long processId,int action) {
        try {
        	LOG.info("释放流程锁：类型"+action+",processId="+affair.getProcessId());
            lockManager.unlock(affair.getMemberId(),processId,action);
        } catch (Throwable e) {
        	LOG.error(e.getMessage(),e);
        }
    }
    /**
     * 重新创建定时任务，延迟15分钟
     * @param affairId
     * @param type
     * @throws NoSuchQuartzJobBeanException 
     * @throws Exception 
     * @throws Throwable
     */
    private void reCreateColProcessDeadLineQuartz(CtpAffair affair,Date nextRunTime) throws BusinessException{
     //Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
   	 Map<String, String> datamap = new HashMap<String, String>(3);
     datamap.put("appType", String.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
     datamap.put("isAdvanceRemind", "1");
     datamap.put("objectId", String.valueOf(affair.getObjectId()));
     String jobName = "ColProcessDeadLine" + affair.getObjectId() ;
     if(!QuartzHolder.hasQuartzJob(jobName, jobName)){
    	 QuartzHolder.deleteQuartzJob(jobName);
	 }
     QuartzHolder.newQuartzJob(jobName, nextRunTime, "processCycRemindQuartzJob", datamap);
     LOG.info("由于流程终止出错，流程到期终止处理的定时任务[ColProcessDeadLine" 
    		 + affair.getObjectId()+"]向后推迟执行，下次执行时间为："
    		 +Datetimes.formatDatetimeWithoutSecond(nextRunTime)+"，新的定时任务名称为："+jobName);
    }
    
    private void reCreateColProcessDeadLineQuartz(CtpAffair affair) throws BusinessException{
        Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
        reCreateColProcessDeadLineQuartz(affair,nextRunTime);
    }
    /**
     * 执行流程事件
     * @param ctpAffair
     * @return
     */
    private String executeWorkflowBeforeEvent(CtpAffair ctpAffair,String processId,String event,Comment comment){
		String msg = ColUtil.executeWorkflowBeforeEvent(ctpAffair, event, "", comment);
    	return msg;
    }
    
    private Map<String,String> excuteDee(ColSummary colSummary, CtpAffair affair,Comment comment,ColHandleType handleType) throws BusinessException{
    	Map<String,String> ret = new HashMap<String,String>();
    	
    	String attitude = "";
        String content = "";
        if(comment != null){
            attitude = comment.getExtAtt1();
            content = comment.getContent();
        }
        
    	if(collaborationFormBindEventListener != null && String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())){
    		ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(),affair.getFormAppId(), affair.getMultiViewStr(), colSummary.getId(),
                    handleType,attitude,content,"");
        }
        return ret;
    }
    
    private User createCurrentUser(Long memeberId) throws BusinessException{
    	V3xOrgMember currentMember= orgManager.getMemberById(memeberId);
    	User user = new User();
		user.setId(currentMember.getId());
		user.setDepartmentId(currentMember.getOrgDepartmentId());
		user.setLoginAccount(currentMember.getOrgAccountId());
		user.setLoginName(currentMember.getLoginName());
		user.setName(currentMember.getName());
		user.setLocale(AppContext.getLocale());
		AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
		return user;
    }

	@Override
	public Long getProcessRemindInterval(Object summary) {
		ColSummary s = (ColSummary) summary;
		return s.getRemindInterval();
	}

	public void transSendCycMessage(Object summary, ProcessMsgParamBO messageBO,String messageRuleId,List<CtpAffair> affairList,CtpAffair senderAffair,Long messageRuleSenderId) throws BusinessException{
	    ColSummary s = (ColSummary)summary;
		if(Strings.isBlank(messageRuleId)){
			oldSendProcessOverTimeMessage(summary, messageBO);
		}else{
			sendProcessOverTimeMessageByMessageRule(summary, messageRuleId, affairList, messageBO,senderAffair,messageRuleSenderId);
		}
		String jobName = "ColProcessDeadLine" + s.getId() ;
		String groupName = "ColProcessDeadLine" + s.getId() ;
		if(!QuartzHolder.hasQuartzJob(jobName, jobName)){
			Map<String, String> datamap = new HashMap<String, String>(3);
			Long remindInterval = s.getRemindInterval();
			Date startTime = new Date();
			if(null!=remindInterval && remindInterval.intValue()>0) {
				startTime = Datetimes.addMinute(startTime, remindInterval.intValue());
				datamap.put("appType", String.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
				datamap.put("isAdvanceRemind", "1");
				datamap.put("objectId", String.valueOf(s.getId()));
				QuartzHolder.newQuartzJob(groupName, jobName+startTime.getTime(), startTime, "processCycRemindQuartzJob", datamap);
			}
		}
		
		
	}
	
	/**
	 * 
	 * @Title: oldSendProcessOverTimeMessage   
	 * @Description: 以前的超期发送消息
	 * @param summary
	 * @param messageBO
	 * @throws BusinessException      
	 * @return: void  
	 * @date:   2019年2月19日 下午2:33:23
	 * @author: xusx
	 * @since   V7.1	       
	 * @throws
	 */
	private void oldSendProcessOverTimeMessage(Object summary, ProcessMsgParamBO messageBO)  throws BusinessException{
		ColSummary s = (ColSummary) summary;
		String msgKey = "process.summary.overTerm";
		String subject = messageBO.getSubject();
		Integer importantLevel = messageBO.getImportantLevel();
		Long processSenderId = messageBO.getProcessSenderId();
		int forwardMemberFlag = messageBO.getForwardMemberFlag();
		String forwardMember = messageBO.getForwardMember();
		Map<Long, MessageReceiver> receiverMap = messageBO.getReceiverMap();
		Map<Long, MessageReceiver> receiverAgentMap  = messageBO.getReceiverAgentMap();
		Integer messageFilterParam = ColMessageFilterEnum.overTime.key;
		
		if(!receiverMap.isEmpty()){
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(receiverMap.values());
			MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
			if (null != s.getTempleteId()) {
				content.setTemplateId(s.getTempleteId());
			}
			userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, processSenderId, receivers,messageFilterParam);
		}
		if(!receiverAgentMap.isEmpty()){
			Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(receiverAgentMap.values());
			MessageContent agentContent = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent");
			if (null != s.getTempleteId()) {
				agentContent.setTemplateId(s.getTempleteId());
			}
			userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, processSenderId, receiverAgents,messageFilterParam);
		}
	}
	
	/**
	 * 
	 * @Title: sendProcessOverTimeMessageByMessageRule   
	 * @Description: 消息规则发送超期消息
	 * @param object
	 * @param messageRuleId
	 * @param affairList
	 * @param messageBO
	 * @throws BusinessException      
	 * @return: void  
	 * @date:   2019年2月19日 下午2:33:52
	 * @author: xusx
	 * @since   V7.1	       
	 * @throws
	 */
	private void sendProcessOverTimeMessageByMessageRule(Object object, String messageRuleId,List<CtpAffair> affairList,ProcessMsgParamBO messageBO,CtpAffair sendAffair,Long messageRuleSenderId) throws BusinessException{
		if(Strings.isBlank(messageRuleId)){
			return;
		}
		List<MessageRuleVO>  ruleVOs = messageRuleManager.getMessageRuleByIdList(messageRuleId);
		if(Strings.isEmpty(ruleVOs)){
			return;
		}
		
		
		ColSummary summary = (ColSummary)object; 
		for (MessageRuleVO messageRule : ruleVOs) {
			//系统预制的走以前的逻辑发送超期消息
			if(Long.valueOf("1").equals(messageRule.getCreater())){
				oldSendProcessOverTimeMessage(summary, messageBO);
			}else{
				
				colMessageManager.sendMessageByMessageRule(messageRule, summary, null, affairList, ColMessageFilterEnum.overTime,messageRuleSenderId);
				
				//自定义消息默认给发起人发送消息
				if(sendAffair!=null){
					ColSummary s = (ColSummary) summary;
					String msgKey = "process.summary.overTerm";
					String subject = messageBO.getSubject();
					Integer importantLevel = messageBO.getImportantLevel();
					Long processSenderId = messageBO.getProcessSenderId();
					int forwardMemberFlag = messageBO.getForwardMemberFlag();
					String forwardMember = messageBO.getForwardMember();
					Integer messageFilterParam = ColMessageFilterEnum.overTime.key;
					String messageSentLink = messageBO.getMessageSentLink();
					
					Set<MessageReceiver> sendReceivers = new HashSet<MessageReceiver>();
					MessageReceiver receiver = new MessageReceiver(sendAffair.getId(), sendAffair.getMemberId(),messageSentLink,sendAffair.getId().toString());
					sendReceivers.add(receiver);
					MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
					if (null != s.getTempleteId()) {
						content.setTemplateId(s.getTempleteId());
					}
					userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, processSenderId, sendReceivers,messageFilterParam);
				}
	
			}
		}

	}
	
	@Override
	public void dealProcessCycRemind(Long objectId, Long isAdvanceRemind, Long appType,String messageRuleId) throws BusinessException {

		try{
			LOG.info("____开始执行定时任务ProcessCycRemind，objectId"+objectId+",isAdvanceRemind："+isAdvanceRemind+",appType:"+appType);
			Object object = this.getSummaryObject(objectId);
			
			boolean isGo = this.isNeedJobExcute(object);
			if(!isGo){
				LOG.info("流程到期，但是流程已经被处理，或者被撤销，不再执行定时任务：objectId"+objectId+",isAdvanceRemind:"+isAdvanceRemind+",appType:"+appType);
				return;
			}
			
			//更新summary和affair的超期字段
			if(Long.valueOf(1).equals(isAdvanceRemind)){
				this.updateProcessOvertimeInfo(object);
			}
			
			ProcessMsgParamBO messageBO = this.getMessageParam(object);
			String title = messageBO.getSubject();
			String messageSentLink = messageBO.getMessageSentLink();
			String messagePendingLink = messageBO.getMessagePendingLink();
			ApplicationCategoryEnum appEnum = messageBO.getAppEnum();
			Long sendId = messageBO.getProcessSenderId();
			Integer importantLevel = messageBO.getImportantLevel();
			
			CtpAffair senderAffair = null;
			
			List<StateEnum> states = new ArrayList<StateEnum>();
			states.add(StateEnum.col_sent);
			states.add(StateEnum.col_pending);
			if(Strings.isNotBlank(messageRuleId)){
				states.add(StateEnum.col_done);
			}
			List<CtpAffair> affairList = affairManager.getAffairs(objectId, states);
			if(affairList == null){
				return;//协同被撤销或者回退到发起人,不做提醒
			}
			
			Iterator<CtpAffair> iterator = affairList.iterator();
			while(iterator.hasNext()){
				CtpAffair affair = iterator.next();
				if (Integer.valueOf(ApplicationCategoryEnum.stepBackData.getKey()).equals(affair.getApp())) {//排除回退到发起人的数据
					iterator.remove();
				}
			}

			if(affairList.size() == 0){
				return;
			}
			
			Long messageRuleSenderId = null;
	        String forwardMemberId = "";
	        if(affairList.size()>0) forwardMemberId = affairList.get(0).getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if(Strings.isNotBlank(forwardMemberId)){
				try {
					forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
					forwardMemberFlag = 1;
				}catch (Exception e) {
				    LOG.error("", e);
				}
			}
			try{
				Map<Long, MessageReceiver> receiverMap = new HashMap<Long, MessageReceiver>();
				Map<Long, MessageReceiver> receiverAgentMap = new HashMap<Long, MessageReceiver>();
				for(CtpAffair affair : affairList){
		        	Long memberId = affair.getMemberId();
		            Long affairId = affair.getId();
		            if(affair.getState() == StateEnum.col_pending.getKey()){
		            	receiverMap.put(memberId, new MessageReceiver(affairId, memberId,messagePendingLink,affairId.toString()));
		            	messageRuleSenderId = memberId;
		            	//判断当前的代理人是否有效，给代理人消息提醒
	                    Long agentId = WFComponentUtil.getAgentMemberId(affair.getTempleteId(), memberId, affair.getReceiveTime());
		            	if(agentId != null) {
		            		receiverAgentMap.put(memberId, new MessageReceiver(affairId, agentId,messagePendingLink,affairId.toString()));
		            	}
		            }else if(affair.getState() == StateEnum.col_sent.getKey()){
		            		senderAffair = affair;
		            		receiverMap.put(memberId, new MessageReceiver(affairId, memberId,messageSentLink,affairId.toString()));
		        	}
		        }
		        //督办人
		    	CtpSuperviseDetail detail = superviseManager.getSupervise(objectId);
		    	if(detail!=null){
    		    	List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
    		        if(Strings.isNotEmpty(supervisors)) {
    	            	for(CtpSupervisor supervisor : supervisors){
    			    		Long colSupervisMemberId = supervisor.getSupervisorId();
    			    		if(affairList.size()>0){
    			    		    //督办需要找到已发的affairId
    			    		    CtpAffair _affair = null;
    			    		    for(CtpAffair c : affairList){
    			    		        if(c.getState().equals(StateEnum.col_waitSend.getKey())
    			    		                || c.getState().equals(StateEnum.col_sent.getKey())){
    			    		            
    			    		            _affair = c;
    			    		            break;
    			    		        }
    			    		    }
    			    			if(_affair == null){
    			    			    _affair = affairList.get(0);
    			    			}
    			    			receiverMap.put(colSupervisMemberId, new MessageReceiver(_affair.getId(), colSupervisMemberId, messageSentLink, _affair.getId().toString(),""));
    			    		}
    			    	}
    		    	}
		    	}
		    	
		    	messageBO.setForwardMember(forwardMember);
		    	messageBO.setForwardMemberFlag(forwardMemberFlag);
		    	messageBO.setReceiverAgentMap(receiverAgentMap);
		    	messageBO.setReceiverMap(receiverMap);
		    	
				if((Long.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(appType) || Long.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(appType)) && isAdvanceRemind !=0 ){
					int autoStopRepealFlag = this.getAutoStopRepealFlag(object);
					if(autoStopRepealFlag==CollaborationEnum.processTermType.autoStop.ordinal()){//自动终止
						
						Map<Long, MessageReceiver> doneReceiverMap = new HashMap<Long, MessageReceiver>();
						//给已办人发消息
						List<CtpAffair> doneAffair = affairManager.getAffairs(objectId, StateEnum.col_done);
						if(Strings.isNotEmpty(doneAffair)){
							for(CtpAffair affair:doneAffair){
								Long memberId = affair.getMemberId();
					            Long affairId = affair.getId();
					            doneReceiverMap.put(memberId, new MessageReceiver(affairId, memberId,messageSentLink,affairId.toString()));
							}
						}
						messageBO.setDoneReceiverMap(doneReceiverMap);
						//执行自动终止
						this.transStopProcess(object,messageBO);
						
					}else if(autoStopRepealFlag==CollaborationEnum.processTermType.autoRepeal.ordinal()){//自动撤销
						
						Map<Long, MessageReceiver> doneReceiverMap = new HashMap<Long, MessageReceiver>();
						Map<Long, MessageReceiver> trackReceiverMap = new HashMap<Long, MessageReceiver>();
						//给已办人发消息
						//修改：暂存待办也会产生追索数据。
						List<StateEnum> stateEnum = new ArrayList<StateEnum>();
						stateEnum.add(StateEnum.col_done);
						stateEnum.add(StateEnum.col_pending);
						List<CtpAffair> doneAffair = affairManager.getAffairs(objectId, stateEnum);
						if(Strings.isNotEmpty(doneAffair)){
							for(CtpAffair affair:doneAffair){
								if(affair.getState().intValue() == StateEnum.col_done.getKey()){
									Long memberId = affair.getMemberId();
									Long affairId = affair.getId();
									doneReceiverMap.put(memberId, new MessageReceiver(affairId, memberId,messageSentLink,affairId.toString()));
									trackReceiverMap.put(memberId, new MessageReceiver(affairId, memberId,"message.link.col.done.detail",affairId.toString()));
								}else{
									if(affair.getSubState().intValue() ==  SubStateEnum.col_pending_ZCDB.getKey()){
										Long memberId = affair.getMemberId();
										Long affairId = affair.getId();
										trackReceiverMap.put(memberId, new MessageReceiver(affairId, memberId,"message.link.col.done.detail",affairId.toString()));
									}
								}
							}
						}
						messageBO.setDoneReceiverMap(doneReceiverMap);
						messageBO.setTrackReceiverMap(trackReceiverMap);
						//执行自动终止
						
						this.transRepealProcess(object,messageBO);
						
					}else{
						this.transSendCycMessage(object, messageBO,messageRuleId,affairList,senderAffair,messageRuleSenderId);
					}
				}else{
					CtpEnumItem cei = enumManagerNew.getEnumItem(EnumNameEnum.common_remind_time,String.valueOf(this.getAdvanceRemind(object)));
					String enumLabel = ResourceUtil.getString(cei.getLabel());
					String msgKey = null;
					if(isAdvanceRemind == 0){ //提前提醒
						msgKey = "process.summary.advanceRemind";
						
						if(appType == ApplicationCategoryEnum.info.getKey()){
						    msgKey = "infosend.process.summary.advanceRemind.info";
						}
					}
					else{ //超期提醒
						msgKey = "process.summary.overTerm";
						
						if(appType == ApplicationCategoryEnum.info.getKey()){
                            msgKey = "infosend.process.summary.overTerm.info";
                        }
					}
					
					if(appType == ApplicationCategoryEnum.edoc.getKey()){
						msgKey += ".edoc";
					}
					
					Integer messageFilterParam = null;
					if(appType == ApplicationCategoryEnum.collaboration.getKey()){//协同超期提醒
					    messageFilterParam = ColMessageFilterEnum.overTime.key;
					}else if(appType == ApplicationCategoryEnum.edoc.getKey()){
					    //messageFilterParam = GovdocMessageHelper.getSystemMessageFilterParam(edocSummary).key;
					}
					
					if(!receiverMap.isEmpty()){
						Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(receiverMap.values());
						userMessageManager.sendSystemMessage(MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember,enumLabel).setImportantLevel(importantLevel), appEnum, sendId, receivers,messageFilterParam);
					}
					
					if(!receiverAgentMap.isEmpty()){
						Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(receiverAgentMap.values());
                        userMessageManager.sendSystemMessage(MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember,enumLabel).setImportantLevel(importantLevel).add("col.agent"), appEnum, sendId, receiverAgents, messageFilterParam);
					}
				}
			}
			catch(Exception e){
				LOG.error("", e);
			}
		}
		catch(Exception e){
			//绑定的定时任务事项已经不存在或被删除
			LOG.error("", e);
			return;
		}
	
	}
}
