package com.seeyon.apps.govdoc.manager.external;

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
import java.util.UUID;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocStatManager;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
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
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.trace.enums.WFTraceConstants;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.quartz.ProcessMsgParamBO;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class GovdocWorkflowProcessOvertimeHandler implements WorkflowProcessOvertimeAppHandler {
	
	private static Log LOGGER = CtpLogFactory.getLog(GovdocWorkflowProcessOvertimeHandler.class);
	
	private OrgManager orgManager;
    private EdocManager edocManager;
    private EdocSummaryManager  edocSummaryManager;
    private AffairManager affairManager;
   	private WorkflowApiManager           wapi;
    private UserMessageManager userMessageManager;
    private LockManager			lockManager;
    private TemplateManager templateManager;
    private SuperviseManager superviseManager;
    private AppLogManager appLogManager;
    private ProcessLogManager processLogManager;
    private GovdocManager govdocManager;
    private CommentManager     ctpCommentManager;
    private GovdocFormManager govdocFormManager;
    private GovdocStatManager govdocStatManager;
    private EnumManager enumManagerNew;
    
    @Override
	public void updateProcessOvertimeInfo(Object summary) throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		
	    Boolean isOvertopTime = s.getCoverTime();
        if(isOvertopTime != null && !isOvertopTime){
        	edocSummaryManager.updateEdocSummaryCoverTime(s.getId(), true);
        }
	}

	public ProcessMsgParamBO getMessageParam(Object summary) throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		
		ProcessMsgParamBO messageBO = new ProcessMsgParamBO();
		messageBO.setSubject(s.getSubject());
		
		String messageLink = "message.link.govdoc.pending";
		String messageLinkDone = "message.link.govdoc.done.detail";
		if(s.getGovdocType()!=null && s.getGovdocType().intValue()==0) {
			messageLink = "message.link.edoc.pending";
			messageLinkDone = "message.link.edoc.done";
		}
		messageBO.setMessageSentLink(messageLinkDone);
		messageBO.setMessagePendingLink(messageLink);
		
		ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.edoc;
		//将以下代码注释 消息类别有问题
//		if (s.getGovdocType().intValue() == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
//			appEnum = ApplicationCategoryEnum.govdocSend;
//		} else if (s.getGovdocType().intValue() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
//			appEnum = ApplicationCategoryEnum.govdocRec;
//		} else if (s.getGovdocType().intValue() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
//			appEnum = ApplicationCategoryEnum.govdocExchange;
//		} else if (s.getGovdocType().intValue() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
//			appEnum = ApplicationCategoryEnum.govdocSign;
//		}
		messageBO.setAppEnum(appEnum);
		messageBO.setProcessSenderId( s.getStartUserId());
		messageBO.setImportantLevel(s.getImportantLevel());
		
		return messageBO;
	}

	@Override
	public boolean isNeedJobExcute(Object summary) throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		
		boolean isGo = true;
		//协同被删除或者完成,不做提醒
		if(s == null || s.getCompleteTime() != null) {
			isGo = false;
		}
		return isGo;
	}

	@Override
	public Object getSummaryObject(long summaryId) throws BusinessException {
		EdocSummary s = edocManager.getEdocSummaryById(summaryId, false);
		return s;
	}

	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}


	@Override
	public Integer getMsgFilterKey(Object object) throws BusinessException {
		EdocSummary s = (EdocSummary)object;
		return EdocMessageHelper.getMsgFilterParamBySummary(s);
	}

    @Override
    public long getAdvanceRemind(Object summary) {
        EdocSummary s = (EdocSummary)summary;
        Long ret = s.getAdvanceRemind();
        if(ret == null){
            ret = 0l;
        }
        return ret;
    }

	@Override
	public ProcessAutoStopRepealCaseBO canAutostopflow(Object summary)
			throws BusinessException {
		EdocSummary s = (EdocSummary) summary;
		ProcessAutoStopRepealCaseBO autoStopRepealCaseBO = new ProcessAutoStopRepealCaseBO();
		String[] result = wapi.canStopFlow(s.getCaseId() + "");
		if ("true".equals(result[0])) {
			autoStopRepealCaseBO.setCanAutoStop(true);
		} else {
			autoStopRepealCaseBO.setCanAutoStop(false);
			autoStopRepealCaseBO.setErrorMsg(result[1]);
		}
		return autoStopRepealCaseBO;
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
	
	/*private String[] canLock(CtpAffair affair,Long processId,int lockAction) {
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
                	LOGGER.info("resourceid:="+lk.getResourceId()+";owner:="+lk.getOwner()+";action:="+lk.getAction());
                	invlidOwner= lk.getOwner();
                	invlidAction= lk.getAction();
                	invlidFrom= lk.getFrom();
                	invlidTime = lk.getLockTime();
                    if(LockState.effective_lock.equals(lockManager.isValid(lk))){
                        lock = lk;
                    }else{//不是有效的锁解掉，不然后面lock不到锁，造成死循环
                    	lockManager.unlock(lk.getOwner(), lk.getResourceId(), lk.getAction());
                    	LOGGER.info("解掉无效的锁(resourceid:="+lk.getResourceId()+";owner:="+lk.getOwner()+";action:="+lk.getAction()+")");
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
						LOGGER.error("", e);
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
				LOGGER.error("", e);
			}
        }
        String[] result= {String.valueOf(canGetLock),lockMsg};
        return result;
	}*/
	
	/**
     * 重新创建定时任务，延迟15分钟
     * @param affairId
     * @param type
     * @throws NoSuchQuartzJobBeanException 
     * @throws Exception 
     * @throws Throwable
     */
    private void reCreateColProcessDeadLineQuartz(CtpAffair affair,Date nextRunTime ) throws BusinessException{
	   //  Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
	   	 Map<String, String> datamap = new HashMap<String, String>(3);
	     datamap.put("appType", String.valueOf(ApplicationCategoryEnum.edoc.getKey()));
	     datamap.put("isAdvanceRemind", "1");
	     datamap.put("objectId", String.valueOf(affair.getObjectId()));
	     String jobName = "ColProcessDeadLine" + affair.getObjectId() ;
	     if(!QuartzHolder.hasQuartzJob(jobName, jobName)){
	    	 QuartzHolder.deleteQuartzJob(jobName);
		 }
	     QuartzHolder.newQuartzJob(jobName, nextRunTime, "processCycRemindQuartzJob", datamap);
	     LOGGER.info("由于流程终止出错，流程到期终止处理的定时任务[ColProcessDeadLine" 
	    		 + affair.getObjectId()+"]向后推迟15分钟执行，下次执行时间为："
	    		 +Datetimes.formatDatetimeWithoutSecond(nextRunTime)+"，新的定时任务名称为："+jobName);
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
    /*private  ProcessAutoStopRepealCaseBO checkAndupdateLockForQuartz(CtpAffair affair,Long processId,int lockAction) {
    	ProcessAutoStopRepealCaseBO autoStopRepealCaseBO= new ProcessAutoStopRepealCaseBO();
    	autoStopRepealCaseBO.setCanAutoRepeal(true);
    	autoStopRepealCaseBO.setCanAutoStop(true);
    	try {
    		LOGGER.info("processId:="+processId+";lockOwer:="+affair.getMemberId()+";lockAction:="+lockAction);
            //流程锁
            String[] result= canLock(affair,processId,lockAction);
            autoStopRepealCaseBO.setErrorMsg(result[1]);
            if("false".equals(result[0])){
            	autoStopRepealCaseBO.setCanAutoRepeal(false);
            	autoStopRepealCaseBO.setCanAutoStop(false);
            	autoStopRepealCaseBO.setCanReCreate(true);
                reCreateColProcessDeadLineQuartz(affair);
            }
        } catch (Throwable e) {
        	LOGGER.error(e.getMessage(),e);
        	autoStopRepealCaseBO.setCanAutoRepeal(false);
        	autoStopRepealCaseBO.setCanAutoStop(false);
        	autoStopRepealCaseBO.setCanReCreate(true);
        	autoStopRepealCaseBO.setErrorMsg(e.getMessage());
        }
        return autoStopRepealCaseBO;
    }*/
	private void reCreateColProcessDeadLineQuartz(CtpAffair affair) throws BusinessException{
        Date nextRunTime= new Date((System.currentTimeMillis()+15*60*1000));
        reCreateColProcessDeadLineQuartz(affair,nextRunTime);
    }
    /**
	 * 逾期自动终止流程处理
     * @throws BusinessException 
	 * @throws BatchException 
	 */
	private ProcessAutoStopRepealCaseBO autoStopFlow(EdocSummary summary,ProcessMsgParamBO messageBO,User user,CtpAffair currentAffair) throws BusinessException{
		ProcessAutoStopRepealCaseBO autoStopFlow = new ProcessAutoStopRepealCaseBO();
		List<CtpAffair> curerntAffairs = affairManager.getAffairs(summary.getId(),StateEnum.col_pending);
		
		if(Strings.isNotEmpty(curerntAffairs)){
			currentAffair = curerntAffairs.get(0);
		}
		else {
			autoStopFlow.setCanAutoRepeal(false);
			autoStopFlow.setCanAutoStop(false);
			autoStopFlow.setCanReCreate(true);
			autoStopFlow.setErrorMsg(ResourceUtil.getString("workflow.stop.not.pending.alert.js"));
			CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
			Date nextRunTime= new Date((System.currentTimeMillis()+24*60*60*1000));
		    reCreateColProcessDeadLineQuartz(sendAffair,nextRunTime);
		}
		CtpAffair theStopAffair= new CtpAffair();
        if (currentAffair != null) {
        	try {
        		V3xOrgMember sender = orgManager.getMemberById(currentAffair.getSenderId());
                user.setId(sender.getId());
                user.setDepartmentId(sender.getOrgDepartmentId());
                user.setLoginAccount(sender.getOrgAccountId());
                user.setLoginName(sender.getLoginName());
                user.setName(sender.getName());
				theStopAffair = (CtpAffair) currentAffair.clone();
				theStopAffair.setId(currentAffair.getId());
			} catch (CloneNotSupportedException e) { 
				LOGGER.info("",e);
			}
        	theStopAffair.setSenderId(user.getId());//发起协同人员id
            theStopAffair.setMemberId(user.getId());//处理协同人员id
            theStopAffair.setObjectId(summary.getId());//协同id
        }
		GovdocDealVO dealVo = new GovdocDealVO();
        dealVo.setCurrentDate(DateUtil.currentDate());
        dealVo.setCurrentUser(user);
        dealVo.setAffair(theStopAffair);
        Comment comment = new Comment();
        dealVo.setComment(comment);
        dealVo.setSummary(summary);
        
        comment.setAffairId(null!=currentAffair?currentAffair.getId():null);
        comment.setHidden(false);
        comment.setId(com.seeyon.ctp.util.UUIDLong.longUUID());
        String content= ResourceUtil.getString("govdoc.auto.stop.opinion");
        comment.setContent(content);
        comment.setPid(0L);
        comment.setModuleType(ModuleType.edoc.getKey());
        comment.setModuleId(summary.getId());
        comment.setCreateId(summary.getStartMemberId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setExtAtt3(CommentExtAtt1Enum.stepStop.i18nLabel());
        comment.setCtype(Comment.CommentType.comment.getKey());
        String errorMsg =  executeWorkflowBeforeEvent(theStopAffair, summary.getProcessId(), "BeforeStop");
        if(Strings.isBlank(errorMsg)){
			// 终止工作流组件
			WorkflowBpmContext wfContext = new WorkflowBpmContext();
			wfContext.setAppName(ApplicationCategoryEnum.edoc.name());
			wfContext.getBusinessData().put("subAppName", GovdocWorkflowTypeEnum.formedoc.name());
			wfContext.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, 8);
			wfContext.setCurrentUserId(String.valueOf(user.getId()));
			wfContext.setBusinessData("isAutoStop", "1");
			wfContext.setBussinessId(String.valueOf(summary.getId()));
			
	        theStopAffair.setSenderId(user.getId());//发起公文人员id
	        theStopAffair.setMemberId(user.getId());//处理公文人员id
        	wfContext.setBusinessData("CtpAffair",theStopAffair);
        	
			Long formRecordId = summary.getFormRecordid();
			Long formAppId = summary.getFormAppid();
			wfContext.setMastrid(formRecordId == null ? null : String.valueOf(formRecordId));
			wfContext.setFormData(formAppId == null ? null : String.valueOf(formAppId));

			//wfContext.setAppObject(summary);
			wfContext.setBusinessData("operationType", AbstractEventListener.STETSTOP);
			wfContext.setCurrentUserId(String.valueOf(user.getId()));

			if (currentAffair != null) {
				wfContext.setCurrentWorkitemId(currentAffair.getSubObjectId());
			}

			
			wapi.stopCase(wfContext);
			
			ctpCommentManager.insertComment(comment);
			// 调用表单万能方法,更新状态，触发子流程等
			govdocFormManager.updateDataState(currentAffair, summary, ColHandleType.stepStop);
			
			// 终止 流程结束时更新督办状态
			superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised, summary.getId(), SuperviseEnum.EntityType.govdoc);
            
            //记录操作时间
            affairManager.updateAffairAnalyzeData(currentAffair);

    		// 若做过指定回退的操作.做过回退发起者则被回退者的状态要从待发改为已发
    		CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
    		if (sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
    				|| sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
    			sendAffair.setState(StateEnum.col_sent.getKey());
    			sendAffair.setUpdateDate(new java.util.Date());
    			affairManager.updateAffair(sendAffair);
    		}
    		
    		summary.setState(EdocConstant.flowState.terminate.ordinal());
    		govdocStatManager.saveOrUpdateEdocStat(summary, user);
         // 流程正常结束通知
    		CollaborationStopEvent stopEvent = new CollaborationStopEvent(this);
    		stopEvent.setSummaryId(summary.getId());
    		stopEvent.setUserId(user.getId());
    		EventDispatcher.fireEvent(stopEvent);
            
            autoStopFlow.setCanAutoStop(true);
			
        }else{
        	autoStopFlow.setErrorMsg(errorMsg);
        }
        
        return autoStopFlow;
        
	}
	
	/**
     * 执行流程事件
     * @param ctpAffair
     * @return
     */
    private String executeWorkflowBeforeEvent(CtpAffair ctpAffair,String processId,String event){
    	WorkflowBpmContext  c = new WorkflowBpmContext();
    	
    	Long formRecordId = ctpAffair.getFormRecordid();
    	Long formAppId = ctpAffair.getFormAppId();
    	c.setFormData(null!=formAppId ? String.valueOf(formAppId) : "");
    	c.setMastrid(null!=formRecordId ? String.valueOf(formRecordId) : "");
    	c.setFormAppId(null!=formAppId ? String.valueOf(formAppId) : "");
    	c.setFormViewOperation(ctpAffair.getMultiViewStr());
    	
		c.setProcessId(processId);
		c.setProcessTemplateId(processId);
		c.setCaseId(ctpAffair.getCaseId());
		c.setCurrentActivityId(String.valueOf(ctpAffair.getActivityId()));
		Long workItemId = ctpAffair.getSubObjectId();
		c.setCurrentWorkitemId( null!=workItemId ? workItemId:0L);	
		
		c.setBussinessId(String.valueOf(ctpAffair.getObjectId()));
		c.setCurrentUserId(String.valueOf(ctpAffair.getMemberId()));
		c.setAffairId(String.valueOf(ctpAffair.getId()));
		c.setAppName(ApplicationCategoryEnum.edoc.name());
		
		
		c.setMatchRequestToken(UUID.randomUUID()+"");
		
				
		String executeWorkflowBeforeEvent = wapi.executeWorkflowBeforeEvent(event,c);
		if(Strings.isNotBlank(executeWorkflowBeforeEvent)){
			return executeWorkflowBeforeEvent;
		}
    	return "";
    }
	
	public void transStopProcess(Object summary, ProcessMsgParamBO messageBO)
			throws BusinessException {

		EdocSummary s = (EdocSummary)summary;
		User user = new User();
		CtpAffair sendAffair = affairManager.getSenderAffair(s.getId());
		if (sendAffair != null) {
			user = createCurrentUser(sendAffair.getSenderId());
		}
		//自由协同：流程期限到时自动终止处理
		//Long processId = s.getProcessId() ==null?null:Long.valueOf(s.getProcessId());
		//ProcessAutoStopRepealCaseBO stopCaseBO = checkAndupdateLockForQuartz(sendAffair,processId,11);
		ProcessAutoStopRepealCaseBO stopCaseBO= new ProcessAutoStopRepealCaseBO();
		stopCaseBO.setCanAutoRepeal(true);
		stopCaseBO.setCanAutoStop(true);
		try{
			//if(stopCaseBO.getCanAutoStop()){
			stopCaseBO = this.canAutostopflow(summary);
				
			if(stopCaseBO.getCanAutoStop()){
				stopCaseBO = autoStopFlow(s,messageBO,user,sendAffair);
			}
				
			//}
		}catch(Exception e){//工作流组件出错了
			stopCaseBO.setErrorMsg(ResourceUtil.getString("govdoc.auto.workflow.error"));
			stopCaseBO.setCanAutoStop(false);
			stopCaseBO.setCanReCreate(false);
			LOGGER.error("", e);
		}finally{//解除锁
			releaseLockForQuartz(sendAffair,Long.valueOf(s.getProcessId()),11);
		}
		
		
		boolean sendMessageToDoneMember = true;
		//写流程日志和应用日志
		String processLogMsg = ResourceUtil.getString("govdoc.auto.stop.sucess.processlog");
		String appLogMsg = ResourceUtil.getString("govdoc.auto.stop.sucess.applog", s.getSubject());
		//发系统消息
		String msgKey = "process.summary.overTerm.stopflow.edoc";
		//发系统消息
		if(!stopCaseBO.getCanAutoStop()){
			//写流程日志和应用日志
			processLogMsg = ResourceUtil.getString("govdoc.auto.stop.error.processlog", s.getSubject(),stopCaseBO.getErrorMsg(),stopCaseBO.isCanReCreate()?1:0);
			appLogMsg = ResourceUtil.getString("govdoc.auto.stop.error.applog", s.getSubject(),stopCaseBO.getErrorMsg(),stopCaseBO.isCanReCreate()?1:0);
			//发系统消息
			msgKey = "process.summary.overTerm.stopflow.edoc.error";
			
			sendMessageToDoneMember = false;
		}
		sendMessageAndInsertLog(s,messageBO,user,ProcessLogAction.processAutoStop,AppLogAction.Coll_Flow_Auto_Stop,msgKey,processLogMsg,appLogMsg,sendMessageToDoneMember,new HashMap<String,Object>());
	
	}
	
	/**
     * 解锁
     * @param processId
     * @param SUMMARYID
     * @param edocSummary
     */
    private void releaseLockForQuartz(CtpAffair affair,Long processId,int action) {
        try {
        	LOGGER.info("释放流程锁：类型"+action+",processId="+affair.getProcessId());
            lockManager.unlock(affair.getMemberId(),processId,action);
        } catch (Throwable e) {
        	LOGGER.error(e.getMessage(),e);
        }
    }
	
	private void sendMessageAndInsertLog(EdocSummary summary, ProcessMsgParamBO messageBO, User user,
			ProcessLogAction processLogAction, AppLogAction appLogAction, String msgKey, String processLogMsg,
			String appLogMsg, boolean canSendToDoneMember, Map<String, Object> businessData) throws BusinessException {

		processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), 1l, processLogAction, processLogMsg);
		appLogManager.insertLog(user, appLogAction, appLogMsg);

		String subject = messageBO.getSubject();
		Integer importantLevel = messageBO.getImportantLevel();
		Long processSenderId = messageBO.getProcessSenderId();
		int forwardMemberFlag = messageBO.getForwardMemberFlag();
		String forwardMember = messageBO.getForwardMember();
		Map<Long, MessageReceiver> receiverMap = messageBO.getReceiverMap();
		Map<Long, MessageReceiver> doneReceiverMap = messageBO.getDoneReceiverMap();
		Map<Long, MessageReceiver> trackReceiverMap = messageBO.getTrackReceiverMap();
		Map<Long, MessageReceiver> receiverAgentMap = messageBO.getReceiverAgentMap();

		int autoStopRepealFlag = getAutoStopRepealFlag(summary);
		boolean canRepeal = autoStopRepealFlag == CollaborationEnum.processTermType.autoRepeal.ordinal();

		boolean canTrack = false;
		if (null != summary.getTempleteId()) {
			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
			canTrack = Integer.valueOf(1).equals(ctpTemplate.getCanTrackWorkflow());
		}
		List<Long> alreadyTraceMes = new ArrayList<Long>();// 已经发了追索消息的
															// 就不需要发其他消息了 过滤用

		if (canTrack && canRepeal) {
			// 生成流程追溯的消息
			if (!trackReceiverMap.isEmpty()) {
				// OA-138976 模板流程设置了到期后自动撤销并追溯流程，流程撤销后，
				// 已办人收到撤销追溯的消息，点击穿透提示已被撤销，应该能穿透到查看详情页面
				// 参考：colMessagemanager.sendMessage4Repeal

				List<Long> traeDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
				if (traeDataAffair == null) {
					traeDataAffair = Collections.emptyList();
				}

				Long sId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID);
				Long aId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID);
				;
				Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
				Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(trackReceiverMap.values());
				CtpAffair cloneAffair = affairManager.get(aId);
				List<MessageReceiver> needSendReceviers = new ArrayList<MessageReceiver>();
				for (MessageReceiver mr : receivers) {
					Long referenceId = mr.getReferenceId();
					if (traeDataAffair.contains(referenceId)) {
						MessageReceiver receiver = new MessageReceiver(cloneAffair.getId(), mr.getReceiverId(),
								"message.link.col.traceRecord", ColOpenFrom.repealRecord.name(),
								cloneAffair.getId().toString(), type.intValue() + "");
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
				userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edoc, processSenderId,
						needSendReceviers);
			}
		} else {
			if (canSendToDoneMember) {
				receiverMap.putAll(doneReceiverMap);
			}
		}

		// 给已发、已办和待办人发送消息
		if (!receiverMap.isEmpty()) {
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(receiverMap.values());
			Iterator<MessageReceiver> _it = receivers.iterator();

			while (_it.hasNext()) {
				MessageReceiver mr = _it.next();
				if (alreadyTraceMes.contains(mr.getReceiverId())) {
					receivers.remove(mr);
				}
			}
			if (!receivers.isEmpty()) {
				MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember)
						.setImportantLevel(importantLevel);
				if (null != summary.getTempleteId()) {
					content.setTemplateId(summary.getTempleteId());
				}
				userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edoc, processSenderId,
						receivers);
			}
		}

		// 代理人发消息
		if (!receiverAgentMap.isEmpty()) {
			Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(receiverAgentMap.values());
			MessageContent agentCount = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember)
					.setImportantLevel(importantLevel).add("col.agent");
			if (null != summary.getTempleteId()) {
				agentCount.setTemplateId(summary.getTempleteId());
			}
			userMessageManager.sendSystemMessage(agentCount, ApplicationCategoryEnum.edoc, processSenderId,
					receiverAgents);
		}
	}

	@Override
	public ProcessAutoStopRepealCaseBO canAutoRepealFlow(Object summary)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public void transRepealProcess(Object summary, ProcessMsgParamBO messageBO)
			throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAutoStopRepealFlag(Object summary) {
		EdocSummary es = (EdocSummary)summary;
		if(es.getCanAutostopflow()){//兼容历史数据，兼容自动协同设置了超期自动终止的情况
    		return CollaborationEnum.processTermType.autoStop.ordinal();
    	}
		return es.getProcessTermType()==null?-1:es.getProcessTermType();
	}

	@Override
	public Long getProcessRemindInterval(Object summary) {
		// TODO Auto-generated method stub
		return null;
	}

	public void transSendCycMessage(Object summary, ProcessMsgParamBO messageBO)
			throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		String msgKey = "process.summary.overTerm.edoc";
		String subject = messageBO.getSubject();
		Integer importantLevel = messageBO.getImportantLevel();
		Long processSenderId = messageBO.getProcessSenderId();
		int forwardMemberFlag = messageBO.getForwardMemberFlag();
		String forwardMember = messageBO.getForwardMember();
		Map<Long, MessageReceiver> receiverMap = messageBO.getReceiverMap();
		Map<Long, MessageReceiver> receiverAgentMap  = messageBO.getReceiverAgentMap();
		Integer messageFilterParam = null;//ColMessageFilterEnum.overTime.key;
		
		if(!receiverMap.isEmpty()){
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(receiverMap.values());
			MessageContent content = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
			if (null != s.getTempleteId()) {
				content.setTemplateId(s.getTempleteId());
			}
			userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edoc, processSenderId, receivers,messageFilterParam);
		}
		if(!receiverAgentMap.isEmpty()){
			Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(receiverAgentMap.values());
			MessageContent agentContent = MessageContent.get(msgKey, subject, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent");
			if (null != s.getTempleteId()) {
				agentContent.setTemplateId(s.getTempleteId());
			}
			userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.edoc, processSenderId, receiverAgents,messageFilterParam);
		}
		String jobName = "ColProcessDeadLine" + s.getId() ;
		if(!QuartzHolder.hasQuartzJob(jobName, jobName)){
			Map<String, String> datamap = new HashMap<String, String>(3);
			Long remindInterval = s.getRemindInterval();
			Date startTime = new Date();
			if(null!=remindInterval && remindInterval.intValue()>0) {
				startTime = Datetimes.addMinute(startTime, remindInterval.intValue());
				datamap.put("appType", String.valueOf(ApplicationCategoryEnum.edoc.getKey()));
				datamap.put("isAdvanceRemind", "1");
				datamap.put("objectId", String.valueOf(s.getId()));
				QuartzHolder.newQuartzJob(jobName+startTime.getTime(), startTime, "processCycRemindQuartzJob", datamap);
			}
		}
		
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}

	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
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

	public GovdocManager getGovdocManager() {
		return govdocManager;
	}

	public void setGovdocManager(GovdocManager govdocManager) {
		this.govdocManager = govdocManager;
	}

	@Override
	public void dealProcessCycRemind(Long objectId, Long isAdvanceRemind, Long appType,String messageRuleId) throws BusinessException {

		try{
			LOGGER.info("____开始执行定时任务ProcessCycRemind，objectId"+objectId+",isAdvanceRemind："+isAdvanceRemind+",appType:"+appType);
			Object object = this.getSummaryObject(objectId);
			
			boolean isGo = this.isNeedJobExcute(object);
			if(!isGo){
				LOGGER.info("流程到期，但是流程已经被处理，或者被撤销，不再执行定时任务：objectId"+objectId+",isAdvanceRemind:"+isAdvanceRemind+",appType:"+appType);
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
			
			List<StateEnum> states = new ArrayList<StateEnum>();
			states.add(StateEnum.col_sent);
			states.add(StateEnum.col_pending);
		
			List<CtpAffair> affairList = affairManager.getAffairs(objectId, states);
			if(affairList == null){
				return;//协同被撤销或者回退到发起人,不做提醒
			}
			
	        String forwardMemberId = "";
	        if(affairList.size()>0) forwardMemberId = affairList.get(0).getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if(Strings.isNotBlank(forwardMemberId)){
				try {
					forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
					forwardMemberFlag = 1;
				}catch (Exception e) {
				    LOGGER.error("", e);
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
		            	
		            	//判断当前的代理人是否有效，给代理人消息提醒
	                    Long agentId = WFComponentUtil.getAgentMemberId(affair.getTempleteId(), memberId, affair.getReceiveTime());
		            	if(agentId != null) {
		            		receiverAgentMap.put(memberId, new MessageReceiver(affairId, agentId,messagePendingLink,affairId.toString()));
		            	}
		            }else{
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
						List<StateEnum> stateEnum = new ArrayList();
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
						this.transSendCycMessage(object, messageBO);
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
					    //messageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(edocSummary).key;
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
				LOGGER.error("", e);
			}
		}
		catch(Exception e){
			//绑定的定时任务事项已经不存在或被删除
			LOGGER.error("", e);
			return;
		}	
	}
	
	public void setEnumManagerNew(EnumManager enumManager) {
		this.enumManagerNew = enumManager;
	}
   	public GovdocStatManager getGovdocStatManager() {
		return govdocStatManager;
	}
	public void setGovdocStatManager(GovdocStatManager govdocStatManager) {
		this.govdocStatManager = govdocStatManager;
	}
	public GovdocFormManager getGovdocFormManager() {
		return govdocFormManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public CommentManager getCtpCommentManager() {
		return ctpCommentManager;
	}
	public void setCtpCommentManager(CommentManager ctpCommentManager) {
		this.ctpCommentManager = ctpCommentManager;
	}
	public AffairManager getAffairManager() {
   		return affairManager;
   	}
   	public void setAffairManager(AffairManager affairManager) {
   		this.affairManager = affairManager;
   	}
	public EdocManager getEdocManager() {
		return edocManager;
	}
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}	
	public EdocSummaryManager getEdocSummaryManager() {
		return edocSummaryManager;
	}
	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}
}
