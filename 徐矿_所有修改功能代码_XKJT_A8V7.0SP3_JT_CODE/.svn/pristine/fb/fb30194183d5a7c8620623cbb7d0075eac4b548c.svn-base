/**
 *
 */
package com.seeyon.v3x.edoc.workflow.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.trace.dao.TraceDao;
import com.seeyon.apps.collaboration.trace.enums.WFTraceConstants;
import com.seeyon.apps.collaboration.trace.enums.WorkflowTraceEnums;
import com.seeyon.apps.collaboration.trace.manager.TraceWorkflowDataManager;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.event.EdocAffairsAssignedEvent;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.v3x.common.web.util.ThreadLocalUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.event.EdocWorkflowFinishEvent;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkHistoryManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocMessagerManager;
import com.seeyon.v3x.edoc.manager.EdocStatManager;
import com.seeyon.v3x.edoc.manager.EdocSuperviseManager;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.MessageCommentParam;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * @author wangwei
 *
 */
public class EdocWorkflowEventListener extends AbstractEventListener {

    private static final Log LOGGER = LogFactory.getLog(EdocWorkflowEventListener.class);

    public static final String EdocSummaryConstant = "EdocSummary";
    public static final String CTP_AFFAIR = "CTP_AFFAIR";  //当前事项
    public static final String COMMENT_ID = "COMMENT_ID";//意见ID
    public static final String COMMENT_MESSAGE = "COMMENT_MESSAGE";//意见ID
    public static final String WORKITEMCANCELED_MEMBERIDTOAFFAIRID = "WORKITEMCANCELED_MEMBERIDTOAFFAIRID";
    public static final String WORKITEMCANCELED_MEMBERIDTOARRAY = "WORKITEMCANCELED_MEMBERIDTOARRAY";
    public static final String PUSHMSG_MEMBERLIST = "PUSHMSG_MEMBERLIST"; // 消息推送的人员信息
    public static final String ASSIGNED_AFFAIRS = "ASSIGNED_AFFAIRS";//生产的事项
    public static final String OPERATION_TYPE = "operationType" ; //操作类型
    
    private WorkTimeManager workTimeManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private UserMessageManager userMessageManager;
    private ProcessLogManager processLogManager ;
    private AppLogManager appLogManager ;
    private TemplateManager       templateManager;
    private TraceWorkflowDataManager edocTraceWorkflowManager;
    private TraceDao traceDao;
    private DocApi          docApi;
    private EdocMarkHistoryManager edocMarkHistoryManager;
    private CtpTrackMemberManager trackManager;
    
    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }
    
    public CtpTrackMemberManager getTrackManager() {
      return trackManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
      this.trackManager = trackManager;
    }

	public TraceDao getTraceDao() {
		return traceDao;
	}

	public void setTraceDao(TraceDao traceDao) {
		this.traceDao = traceDao;
	}


	public void setEdocTraceWorkflowManager(TraceWorkflowDataManager edocTraceWorkflowManager) {
		this.edocTraceWorkflowManager = edocTraceWorkflowManager;
	}


	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

    public EdocMarkHistoryManager getEdocMarkHistoryManager() {
		return edocMarkHistoryManager;
	}

	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}

	/**
     * 公文消息发送接口
     */
    private EdocMessagerManager edocMessagerManager;
    /**
     * @return the edocMessagerManager
     */
    public EdocMessagerManager getEdocMessagerManager() {
        return edocMessagerManager;
    }
    /**
     * @param edocMessagerManager the edocMessagerManager to set
     */
    public void setEdocMessagerManager(EdocMessagerManager edocMessagerManager) {
        this.edocMessagerManager = edocMessagerManager;
    }

    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }

    private EdocManager edocManager;

    public WorkTimeManager getWorkTimeManager() {
        return workTimeManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

   

    public UserMessageManager getUserMessageManager() {
        return userMessageManager;
    }
    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }
    public OrgManager getOrgManager() {
        return orgManager;
    }
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
    public AffairManager getAffairManager() {
        return affairManager;
    }
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    /**
     * 直接提交给我，提交后回调函数：将退回人状态挂起->待办
     * 将指定回退状态 17 改为 16
     * 参加bug：OA-39879
     */
    @Override
    public boolean onWorkitemWaitToLastTimeStatus(EventDataContext context) {
        try {
            EdocSummary summary = (EdocSummary)context.getAppObject();
            List<WorkItem> workItems = context.getWorkitemLists();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            //Set<MessageReceiver> targetReceivers = new HashSet<MessageReceiver>();
            for (WorkItem wi : workItems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(wi.getId());
                affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
                affair.setState(StateEnum.col_pending.key());
                affair.setUpdateDate(now);

                //删除原来的定时任务（超期提醒、提前提醒）
                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                }
                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0)||affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                }
                //设置新的定时任务
                Long accountId = EdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary , templateManager);
                Long deadLine = affair.getDeadlineDate();
                Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                Date deadLineRunTime = null;
                try {
                    if (deadLine != null && deadLine != 0){
                        deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                        affair.setExpectedProcessTime(deadLineRunTime);
                    }
                } catch (WorkTimeSetExecption e) {
                	LOGGER.error("", e);
                }
                affairManager.updateAffair(affair);
                ColUtil.affairExcuteRemind(affair, accountId);

                /** 激活节点：消息接收人员  **/
                String senderName = orgManager.getMemberById(affair.getSenderId()).getName();
                MessageContent sendContent = new MessageContent("edoc.appointStepBack.send", senderName, affair.getSubject());
                sendContent.setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                	sendContent.setTemplateId(affair.getTempleteId());
                }
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString());
                userMessageManager.sendSystemMessage(sendContent, app, affair.getSenderId(), receiver,EdocMessageHelper.getSystemMessageFilterParam(affair).key);
            }

            
        } catch (Exception e) {
          LOGGER.error("",e);
        }
        return true;
    }

    @Override
    public boolean onWorkitemWaitToReady(EventDataContext context) {
        try {
            //提交时传递的summay
        	EdocSummary summary = (EdocSummary)context.getAppObject();
        	//发起指定回退人的nodeid
        	String activityId = context.getNodeId();
        	Set<Long> currentNodesInfoSet = new HashSet<Long>();
        	//更新发起指定回退人的状态
        	//List<CtpAffair> targetAffair = affairManager.getAffairsByActivityId(Long.parseLong(activityId));
        	//Set<MessageReceiver> targetReceivers = new HashSet<MessageReceiver>();
        	List<WorkItem> workitems = context.getWorkitemLists();
        	for (WorkItem workItem : workitems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(workItem.getId());
                if(affair.getSubState() == SubStateEnum.col_pending_specialBack.getKey()){
                    affair.setSubState(SubStateEnum.col_pending_unRead.getKey());
                }else if(affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderReGo.getKey()){
                    affair.setSubState(SubStateEnum.col_pending_ZCDB.getKey());
                }else{
                 	  continue;
                }
                /**************加上当前待办人**************/
                if(!"zhihui".equals(context.getPolicyId())&&!"inform".equals(context.getPolicyId()) && currentNodesInfoSet.size() < 10){//知会 节点不算待办
					currentNodesInfoSet.add(affair.getMemberId());
                }
				/**************加上当前待办人**************/
                
                try {
                	Timestamp now = new Timestamp(System.currentTimeMillis());
                	affair.setReceiveTime(now);
                	affair.setUpdateDate(now);
                	affair.setPreApprover(null !=  AppContext.getCurrentUser()? AppContext.getCurrentUser().getId() :null);
                    
                    //targetReceivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString()));
                  //删除原来的定时任务（超期提醒、提前提醒）
                    if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                        QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                    }
                    if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0)||affair.getExpectedProcessTime() != null) {
                        QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                    }
                    //设置新的定时任务
                    Long accountId = EdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary , templateManager);
                    Long deadLine = affair.getDeadlineDate();
                    Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                    Date deadLineRunTime = null;
                    try {
                        if (deadLine != null && deadLine != 0){
                            deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                            affair.setExpectedProcessTime(deadLineRunTime);
                            if (now.after(deadLineRunTime)) {
                         	   affair.setCoverTime(true);
                            } else {
                         	   affair.setCoverTime(false);
                            }
                        }
                    } catch (WorkTimeSetExecption e) {
                    	LOGGER.error("", e);
                    }
                    affairManager.updateAffair(affair);
                    ColUtil.affairExcuteRemind(affair, accountId);
                } catch (BusinessException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                /** 激活节点：消息接收人员  **/
                String senderName = orgManager.getMemberById(affair.getSenderId()).getName();
                MessageContent messageContentSent = new MessageContent("edoc.appointStepBack.send", senderName, affair.getSubject());
                messageContentSent.setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                	messageContentSent.setTemplateId(affair.getTempleteId());
                }
                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString());
                userMessageManager.sendSystemMessage(messageContentSent, app, affair.getSenderId(), receiver, EdocMessageHelper.getSystemMessageFilterParam(affair).key);
                break;
        	}
        	
        	if(null!=summary && Strings.isNotEmpty(currentNodesInfoSet)){//正常处理生成
            	EdocHelper.setCurrentNodesInfo(summary,Strings.join(currentNodesInfoSet, ";"));
            	edocManager.updateCurrentNodeUser(summary.getId(),summary.getCurrentNodesInfo());
			}
        	
		} catch (BusinessException e) {
			LOGGER.error("",e);
		}
        return true;
    }

    @Override
    public String getModuleName() {
        return "edoc";
    }

    @Override
    public boolean onProcessStarted(EventDataContext context) {
        return true;
    }
    @Override
    public boolean onProcessFinished(EventDataContext context) {
        EdocManager edocManager = (EdocManager)AppContext.getBean("edocManager");
        AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        EdocSuperviseManager edocSuperviseManager = (EdocSuperviseManager)AppContext.getBean("edocSuperviseManager");
        EdocStatManager edocStatManager= (EdocStatManager)AppContext.getBean("edocStatManager");
        EdocSummary summary= (EdocSummary)context.getAppObject();
        //当流程中只有知会节点时，发送流程后会视为流程结束，调用该方法
        if(summary == null){
            summary = edocManager.getSummaryByProcessId(context.getProcessId());
        }
        if(summary == null){
            return true;
        }
        try {
        	Map<String, Object> businessData = context.getBusinessData();
            int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE)==null ? COMMONDISPOSAL : (Integer)businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
	    	int summaryState = STETSTOP.equals(operationType) ? CollaborationEnum.flowState.terminate.ordinal() : CollaborationEnum.flowState.finish.ordinal();
	        //edocManager.setFinishedFlag(summary.getId(), summaryState);
	        //affairManager.updateFinishFlag(summary.getId());
		    //更新督办设置
	        edocSuperviseManager.updateBySummaryId(summary.getId());
	        setTime2Summary(summary);
	        summary.setFinished(true);
	        summary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            summary.setState(summaryState);
            edocManager.transSetFinishedFlag(summary);
           
            
            Map<String, Object> columns2 = new HashMap<String, Object>();
            columns2.put("summaryState", summary.getState());
            columns2.put("finish", true);
            affairManager.update(columns2, new Object[][] {{"objectId",summary.getId()}});

            try{
                edocStatManager.updateFlowState(summary.getId(), summaryState);
            }catch(Exception e){
                LOGGER.error("更新公文统计流程状态错误 summaryId="+summary.getId(),e);
            }
        } catch(Exception e) {
        	LOGGER.error("",e);
        }

        /**
         * 预归档目录
         */
        if(summary.getState() == CollaborationEnum.flowState.finish.ordinal()){ //流程结束不是终止
            //发文：流程结束，  如果设置了预归档目录，则直接归档到该目录中
            Long affairId=(Long)context.getBusinessData("affairId");
            if(affairId != null){
                CtpAffair affair;
                try {
                    affair = affairManager.get(affairId);
                    if(summary.getEdocType() == 0){  //发文
                        //if(summary.getArchiveId()!= null  && !summary.getHasArchive() && summary.getTempleteId()!=null){
                        if(summary.getArchiveId()!= null  && !summary.getHasArchive()) {
                            edocManager.pigeonholeAffair("", affair, summary.getId(),summary.getArchiveId(),false);
                            summary.setHasArchive(true);
                        }
                    }
                    //发文：发文在归档并且流程结束后在已办、已发列表中删除
                    if(summary.getHasArchive() && summary.getArchiveId() != null){
                        edocManager.setArchiveIdToAffairsAndSendMessages(summary,affair,true);
                        try{
		        	    	User user = AppContext.getCurrentUser();
		        		    String params = summary.getSubject() ;
		        		    Long activityId = affair.getActivityId();
		        		    if(activityId==null){
		        		    	BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);//当前节点
		        		    	if(bPMActivity != null)
		        		    		activityId = Long.valueOf(bPMActivity.getId());
		        		    }
		        		    if(activityId != null){
		        		    	processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), activityId.longValue(), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()),params);
		        		    }else {
		        		    	processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()),params);
		        		    }
		        		    appLogManager.insertLog(user, AppLogAction.Edoc_PingHole, user.getName() ,summary.getSubject()) ;
		        		    }catch(Exception e){
		        	    	LOGGER.error("公文自动归档，记录流程日志",e);
		        	    }
                    }
                    
                    EdocWorkflowFinishEvent finishEvent = new EdocWorkflowFinishEvent(this);
                    finishEvent.setSummaryId(summary.getId());
                    EventDispatcher.fireEvent(finishEvent);
                } catch (BusinessException e) {
                	LOGGER.error("",e);
                }
            }
        }

        //清空消息跟踪设置表中的数据
        try{
          trackManager.deleteTrackMembers(summary.getId());
        }catch(Exception e){
        	LOGGER.error("清空消息跟踪设置表中的数据抛出异常",e);
        }
        
        return true;
    }

    /**
     * 设置Summary的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     */
    public void setTime2Summary(EdocSummary summary) throws BusinessException {
        if(summary == null){
            return ;
        }
        //工作日计算运行时间和超期时间。
        Long orgAccountId = summary.getOrgAccountId();
        Date startDate = summary.getCreateTime();
        Long deadLine = summary.getDeadline();
        long runWorkTime = workTimeManager.getDealWithTimeValue(startDate,new Date(),orgAccountId);
        runWorkTime = runWorkTime/(60*1000);
        Long workDeadline = workTimeManager.convert2WorkTime(deadLine, orgAccountId);
        //超期工作时间
        Long overWorkTime = 0L;
        //设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
        if(workDeadline!=null&&workDeadline!=0){
            long ow = runWorkTime - workDeadline;
            overWorkTime =  ow >0 ? ow: null ;
        }
        //自然日计算运行时间和超期时间
        Long runTime = (System.currentTimeMillis() - startDate.getTime())/(60*1000);
        Long overTime = 0L;
        if( deadLine!= null &&  deadLine!=0){
            Long o = runTime - deadLine;
            overTime = o >0 ? o : null;
        }
        summary.setOverTime(overTime);
        summary.setOverWorkTime(overWorkTime);
        summary.setRunTime(runTime);
        summary.setRunWorkTime(runWorkTime);
    }
    public ProcessLogManager getProcessLogManager() {
		return processLogManager;
	}
	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	//取回
    @Override
    public boolean onWorkitemTakeBack(EventDataContext context) {
        return true;//在controller中更新当前affair为待办状态，这里不需要做其他操作。
    }

    /**
     * 响应任务事项workitem取消事件
     * @param context EventDataContext
     * @return
     */
    @Override
    public boolean onProcessCanceled(EventDataContext context){
        return true;//在controller中更新当前affair为待办状态，这里不需要做其他操作。
    }
   


    @Override
    /**
     * 指定回退到普通/发起节点，退回人状态处理(待办->挂起)
     * 改状态; 不发消息;
     */
    public boolean onWorkitemReadyToWait(EventDataContext context) {
        try {
            List<WorkItem> workitems = context.getWorkitemLists();
            String currentAffairID =  (String)(context.getBusinessData("CRRENTAFFAIRID")+"");
            for (WorkItem workItem : workitems) {
                CtpAffair affair = this.affairManager.getAffairBySubObjectId(workItem.getId());
                if (!String.valueOf(affair.getId()).equals(currentAffairID))
                    continue;
                affair.setState(StateEnum.col_pending.getKey());
                if(affair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()) { //16
                	affair.setSubState(SubStateEnum.col_pending_specialBackCenter.getKey());//17如果是被退回的数据。
                }else if(affair.getSubState() == SubStateEnum.col_pending_ZCDB.getKey()) {//13
                	affair.setSubState(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());//19暂存代办的数据
                }else{
                	affair.setSubState(SubStateEnum.col_pending_specialBack.getKey());//15
                }
                affair.setUpdateDate(new java.util.Date());
                this.affairManager.updateAffair(affair);
            }
           
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     * 指定回退到普通节点时，被退回人的处理(已办-待办)
     * 改状态; 删定时任务; 增定时任务; 不发消息;
     */
    @Override
    public boolean onWorkitemDoneToReady(EventDataContext context) {
        try {
            List<WorkItem> workitems = context.getWorkitemLists();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            
            
            for (WorkItem workItem : workitems) {
            	
            	
            	
            	 CtpAffair affair = affairManager.getAffairBySubObjectId(workItem.getId());
            	 
            	 EdocSummary s = edocManager.getEdocSummaryById(affair.getObjectId(), false);
                 Long accountId = EdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), s, templateManager);
                 
                 affair.setState(StateEnum.col_pending.key());
                 affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
                 affair.setArchiveId(null);
                 affair.setUpdateDate(now);
                 affair.setCompleteTime(null);
                 affair.setReceiveTime(now);
                 affair.setBackFromId(AppContext.getCurrentUser().getId());
                 affair.setPreApprover(AppContext.getCurrentUser().getId());
                 
                 //设置回退人的id用来在待办栏目显示
                 affair.setBackFromId(AppContext.getCurrentUser().getId());
                 //将超期状态置为不超期，设置新的定时任务来重新计算超期状态
                 affair.setCoverTime(false);
                 affair.setDelete(false);
                 //超期时间
                 Long deadLine = affair.getDeadlineDate();
                 Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                 Date deadLineRunTime = null;
                 try {
                     if (deadLine != null && deadLine != 0){
                         deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                         affair.setExpectedProcessTime(deadLineRunTime);
                     }
                 } catch (WorkTimeSetExecption e) {
                	 LOGGER.error("", e);
                 }
                 affairManager.updateAffair(affair);
                 //删除原来的定时任务（超期提醒、提前提醒）
                 if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                 	QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                 }
                 if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0)||affair.getExpectedProcessTime() != null) {
                     QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                 }
                 //设置新的定时任务
                 ColUtil.affairExcuteRemind(affair, accountId);
            }
            
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }


    //终止
    @Override
    public boolean onWorkitemStoped(EventDataContext context) {

        WorkItem workitem = context.getWorkItem();
        CtpAffair affair = (CtpAffair)context.getBusinessData(CTP_AFFAIR);
        
        AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
//      终止时不给待发送事项发消息
        List<CtpAffair> trackingAndPendingAffairs = null;
        try {
        Timestamp now = new Timestamp(System.currentTimeMillis());
		EdocSummary edocSummary;
		edocSummary = edocManager.getEdocSummaryByProcessId(Long.valueOf(context.getProcessId()));
		edocSummary.setState(CollaborationEnum.flowState.terminate.ordinal());
		edocSummary.setCompleteTime(now);
		//终止时更新summary
		try {
			edocManager.update(edocSummary);
		} catch (Exception e) {
			LOGGER.error("",e);
		}
		if (affair == null) {
			affair = affairManager.getAffairBySubObjectId(Long
					.valueOf(workitem.getId()));
		}
        setTime2Affair(affair,edocSummary);
        long overWorktime=0L;
        long overTime=0L;
        if(null !=affair.getOverWorktime()){
        	overWorktime=affair.getOverWorktime();
        }
        if(null != affair.getOverTime()){
        	overTime =affair.getOverTime();
        }
        Map<String, Object> columns = new HashMap<String, Object>();
        columns.put("state", StateEnum.col_done.key());
        columns.put("subState", SubStateEnum.col_done_stepStop.key());
        columns.put("completeTime", now);
        columns.put("updateDate", now);
        columns.put("finish", true);
    	columns.put("overWorktime", overWorktime);
		columns.put("runWorktime", affair.getRunWorktime());
		columns.put("overTime", overTime);
		columns.put("runTime", affair.getRunTime());

        try {
            trackingAndPendingAffairs = affairManager.getTrackAndPendingAffairs(affair.getObjectId(),affair.getApp());
        } catch (BusinessException e) {
            LOGGER.error("公文流程终止时，获得待办事项出错  " + e.getMessage());
        }
        	//根据app判断，避免终止时更新待发送事项的状态
            //affairManager.update(columns, new Object[][]{{"objectId", affair.getObjectId()}, {"state", StateEnum.col_pending.key()},{"app",affair.getApp()}});
        List<CtpAffair> affairsByObjectIdAndState = affairManager.getAffairs(affair.getObjectId(), StateEnum.col_pending);
		for(int count = affairsByObjectIdAndState.size(),a = 0;a<count; a++){
			CtpAffair ctpAffairM = affairsByObjectIdAndState.get(a);
			if(null == ctpAffairM.getDeadlineDate()){
				columns.put("overWorktime",0L);
				affairManager.update(columns, new Object[][] {{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
			}
			if(null != ctpAffairM.getDeadlineDate()){
				if(ctpAffairM.getDeadlineDate().equals(affair.getDeadlineDate())){
					columns.put("overWorktime",affair.getOverWorktime());
				}else {
					long ctpAffairMOverWorkTime = affair.getRunWorktime()-ctpAffairM.getDeadlineDate();
					columns.put("overWorktime",ctpAffairMOverWorkTime > 0 ? ctpAffairMOverWorkTime : 0L);
				}
				affairManager.update(columns, new Object[][] {{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
			}

		}

        //指定回退给发起人,在待发中
        Map<String, Object> columns2 = new HashMap<String, Object>();
        columns2.put("state", StateEnum.col_sent.key());
        columns.put("subState", SubStateEnum.col_normal.key());
        columns2.put("finish", true);
        columns2.put("updateDate", now);
        columns2.put("summaryState", edocSummary.getState());
        affairManager.update(columns2, new Object[][]{{"objectId", affair.getObjectId()}, {"state", StateEnum.col_waitSend.key()},{"app",affair.getApp()}});

        } catch (BusinessException e) {
            LOGGER.error("更新affair事项出错  " + e.getMessage(), e);
        }
        LOGGER.info("公文流程终止，发送消息。。。" + affair.getId());
        MessageCommentParam mc = (MessageCommentParam)context.getBusinessData(EdocWorkflowEventListener.COMMENT_MESSAGE);
        
        EdocMessageHelper.terminateCancel(affairManager, orgManager, userMessageManager, workitem, affair, trackingAndPendingAffairs,mc,context.getBusinessData());

        return true;
    }

    @Override
    // 事项取消
    public boolean onWorkitemCanceled(EventDataContext context) {
        // 流程撤销、指定回退发起者流程重走：这块不需要处理，affair都在EdocController通过调用affairManager批量处理了。
        // 流程取回和流程回退、指定回退普通节点流程重走（注意：回退和指定回退是不一样的）这个方法要单独实现
        try {
        	Map<String, Object> businessData = context.getBusinessData();
            int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE)==null ? AUTODELETE : (Integer)businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
            WorkItem workitem =  context.getWorkItem();
           
            String processMode = context.getProcessMode();
            if(operationType==AUTOSKIP && PROCESS_MODE_COMPETITION.equals(processMode)) {
	    		operationType = COMMONDISPOSAL;
	    	}
            CtpAffair affair = eventData2ExistingAffair(context);
            if (affair == null)
                return false;
            List<Long> cancelAffairIds = new ArrayList<Long>();
            long workItemId = workitem.getId();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            /******************  正常处理、暂存待办、竞争执行 ********************/
            if((operationType == COMMONDISPOSAL || operationType == ZCDB || operationType == SPECIAL_BACK_SUBMITTO) && PROCESS_MODE_COMPETITION.equals(processMode)) {
                List<CtpAffair> affairs = new ArrayList<CtpAffair>();
                List<CtpAffair> sendAffairs = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
                if(!sendAffairs.isEmpty())affairs.addAll(sendAffairs);

                if(!affairs.isEmpty()) {
                	StringBuffer hql = new StringBuffer();
					hql.append("UPDATE " + CtpAffair.class.getName() + " SET state=:state,subState=:subState,updateDate=:updateDate WHERE app=:app AND objectId=:objectId AND activityId=:activityId AND subObjectId<>:subObjectId");
					Map<String,Object> params=new HashMap<String, Object>();
					params.put("state", StateEnum.col_competeOver.key());
					params.put("subState", SubStateEnum.col_normal.key());
					params.put("updateDate", now);
					params.put("app", affair.getApp());
					params.put("objectId", affair.getObjectId());
					params.put("subObjectId", workItemId);
					params.put("activityId", affair.getActivityId());
					DBAgent.bulkUpdate(hql.toString(), params);
					//给在竞争执行中被取消的affair发送消息提醒
                    EdocMessageHelper.competitionCancel(affairManager, orgManager, userMessageManager, workitem, affairs);
                }
                //竞争执行，有人暂存待办，那么当前待办人就只有 暂存待办这个人了
                if(operationType == ZCDB){
                	StringBuffer hql = new StringBuffer();
                	hql.append("from " + CtpAffair.class.getName() + " WHERE app=:app AND objectId=:objectId AND activityId=:activityId AND subObjectId=:subObjectId");
					Map<String,Object> params=new HashMap<String, Object>();
					params.put("app", affair.getApp());
					params.put("objectId", affair.getObjectId());
					params.put("subObjectId", workItemId);
					params.put("activityId", affair.getActivityId());
					List<CtpAffair> afs = DBAgent.find(hql.toString(), params);
					if(Strings.isNotEmpty(afs)){
						CtpAffair zcdbAf = afs.get(0);
						EdocSummary summary=(EdocSummary)context.getAppObject();
						String currentNodesInfo = zcdbAf.getMemberId() +"";

						summary.setCurrentNodesInfo(currentNodesInfo);
					}
                }
            }
            /****************** 退回或是取回 ********************/
            else if(operationType == WITHDRAW || operationType == TAKE_BACK) {

            	int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
                List<WorkItem> workItems = context.getWorkitemLists();
                //更新affair状态
                AffairData affairData = (AffairData)context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
                List<CtpAffair>  affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.valueOf(affair.getApp()), affair.getObjectId());
                Map<Long,CtpAffair> m = new HashMap<Long,CtpAffair>();
                for(CtpAffair af : affairs){
                    if(af.getSubObjectId()!=null){
                        m.put(af.getSubObjectId(), af);
                    }
                }

                List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
                List<Long> workItemList = new ArrayList<Long>(workItems.size());

				List<String> normalStepBackTargetNodes = context.getNormalStepBackTargetNodes();
				Object currentNodeId  = context.getBusinessData().get("currentAffairId");
				List<Long> _traceList = new ArrayList<Long>();
				
				int maxCommitNumber = 300;
				int length = workItems.size();
				int i = 0;
				Map<Long,Long> canceledMemberIdToAffairId = new HashMap<Long,Long>();
                for(WorkItem item : workItems) {
                    workItemList.add(item.getId());
                    if(m.keySet().contains(item.getId())) {
                        CtpAffair af = m.get(item.getId());
                        cancelAffairIds.add(af.getId());
                        cancelAffairs.add(af);
                        boolean isDoneNode = Integer.valueOf(StateEnum.col_done.getKey()).equals(af.getState());
                        boolean isZCDBNode = Integer.valueOf(StateEnum.col_pending.key()).equals(af.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(af.getSubState());
						boolean isBackNode = af.getId().equals((Long)currentNodeId);
						boolean isBackedNode = af.getActivityId() == null ? true : normalStepBackTargetNodes.contains(String.valueOf(af.getActivityId()));

						if(operationType == WITHDRAW){
    							traceDao.deleteDynamicOldDataByAffair(af);
    					}

						if(operationType == WITHDRAW
								&& "1".equals(context.getBusinessData("isNeedTraceWorkflow"))
								&& !isBackedNode
								&& (isDoneNode || isBackNode)|| isZCDBNode ){
							edocTraceWorkflowManager.createStepBackTrackData(af,af.getSenderId(),AppContext.getCurrentUser().getId() ,WorkflowTraceEnums.workflowTrackType.step_back_normal);
							_traceList.add(af.getId());
						}
                        //加入threadlocal，发消息时使用
						canceledMemberIdToAffairId.put(af.getMemberId(), af.getId());
                    }
//                    if(operationType == TAKE_BACK) {//取回
//                    	this.updateAffairBySubObject(workItemList, state, SubStateEnum.col_normal.key(),affair.getObjectId());
//                    } else{
//                    	this.updateStepBackAffair(workItemList, state, SubStateEnum.col_normal.key(),affair.getObjectId());
//                    }
                    i++;
                    Map<String, Object> nameParameters = new HashMap<String, Object>();
                    nameParameters.put("updateDate", new Date());
                    nameParameters.put("state", state);
                    nameParameters.put("subState", SubStateEnum.col_normal.key());
                    if(i % maxCommitNumber == 0 || i == length) {
                    	Object[][] wheres = new Object[][] {
                    		{"objectId",affair.getObjectId()},
                    		{"subObjectId",workItemList}};
                        affairManager.update(nameParameters, wheres);
                        workItemList = new ArrayList<Long>();
                    }
                }
                context.setBusinessData(WORKITEMCANCELED_MEMBERIDTOAFFAIRID, canceledMemberIdToAffairId);
                context.setBusinessData(WFTraceConstants.WFTRACE_AFFAIRIDS, _traceList);
                context.setBusinessData(WFTraceConstants.WFTRACE_TYPE, WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey());
                //更新当前处理人信息
				EdocSummary summary=(EdocSummary)context.getAppObject();
				EdocHelper.deleteAffairsNodeInfo(summary,cancelAffairs);

            }
            /****************** 默认删除操作: 督办替换节点，自动流程复合节点的单人执 ********************/
            else if(operationType == AUTODELETE) {
                //删除被替换的所有affair事项
                List<CtpAffair> affairs = new ArrayList<CtpAffair>();
                if(context.getWorkitemLists() != null){
                	List<WorkItem> workitems=context.getWorkitemLists();
                	affairs = this.superviseCancel(workitems,now);
                }
                if(affairs.isEmpty()){
                    affairs.add(affair);
                }
                //2013-1-8给在督办中被删除的affair发送消息提醒
                //OA-50240 流程已经变更，原流程节点还能从消息进入处理页面，还能进行终止等操作
                edocMessagerManager.superviseDelete(workitem, affairs);
                //替换节点后更新当前待办人信息
                EdocSummary summary = (EdocSummary)context.getAppObject();
                if(summary==null){
                	summary=edocManager.getEdocSummaryById(affair.getObjectId(), false);
                }
                EdocHelper.updateCurrentNodesInfo(summary, true);
            }
            /****************** 指定回退普通节点流程重走 ********************/
            else if (operationType == SPECIAL_BACK_RERUN) {
            	EdocSummary summary = (EdocSummary)context.getAppObject();
            	List<WorkItem> workItems = context.getWorkitemLists();
                if(Strings.isNotEmpty(workItems)) {
                	CtpAffair firstAffair = affairManager.getAffairBySubObjectId(workItems.get(0).getId());
    				if (firstAffair == null){
    					LOGGER.info("====firstAffair is null========workItems.get(0).getId():"+workItems.get(0).getId());
    				    return false;
    				}

    				int MaxCommitNumber = 300;
    				int length = workItems.size();
    				List<Long> workitemIds = new ArrayList<Long>();
    				List<Long> activityIds = new ArrayList<Long>();
    				List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
    				int i = 0;
    				int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
    				ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
    				List<CtpAffair>  affairs = affairManager.getValidAffairs(app, affair.getObjectId());
    				Map<Long,CtpAffair> m = new HashMap<Long,CtpAffair>();
    				for(CtpAffair af : affairs){
    					if(af.getSubObjectId()!=null){
    						m.put(af.getSubObjectId(), af);
    					}
    				}

                    Map<Long,Long> canceledMemberIdToAffairId = new HashMap<Long,Long>();
                    Map<Long,Long[]> canceledMemberIdToAarray = new HashMap<Long,Long[]>();
    				List<Long> workitemIds1 = new ArrayList<Long>();
	                for (WorkItem workItem : workItems) {
	                	CtpAffair af = m.get(workItem.getId());
	                	if(m.keySet().contains(workItem.getId())) {
    						cancelAffairIds.add(af.getId());
    						cancelAffairs.add(af);
    						canceledMemberIdToAffairId.put(af.getMemberId(), af.getId());
    						Long[] arr = new Long[2];
    						arr[0] = af.getId() ;
    						arr[1] = Long.valueOf(af.getState());
    						canceledMemberIdToAarray.put(af.getMemberId(), arr);
    					}

	                	if(af != null){
	                		workitemIds1.add((long) workItem.getId());
	                	}
	                }
	                
	                context.setBusinessData(WORKITEMCANCELED_MEMBERIDTOAFFAIRID, canceledMemberIdToAffairId);
                    context.setBusinessData(WORKITEMCANCELED_MEMBERIDTOARRAY, canceledMemberIdToAarray);
                    
	                if(Strings.isNotEmpty(workitemIds1)){
	                	List<Long>[]  subList= Strings.splitList(workitemIds1, 1000);
	                	for(List<Long> list : subList){
	                		
	                		StringBuffer hql=new StringBuffer();
	                		hql.append("update CtpAffair as affair set state=:state,subState=:subState,updateDate=:updateDate where objectId=:objectId and subObjectId in (:subObjectIds) ");
	                		Map<String,Object> params=new HashMap<String, Object>();
	                		params.put("state", state);
	                		params.put("subState", SubStateEnum.col_normal.key());
	                		params.put("updateDate", now);
	                		params.put("objectId", affair.getObjectId());
	                		params.put("subObjectIds", list);
	                		DBAgent.bulkUpdate(hql.toString(), params);
	                	}
	                	
	                }
					
	                //更新当前处理人信息
					EdocHelper.deleteAffairsNodeInfo(summary,cancelAffairs);
                }
            }
            if (!CollectionUtils.isEmpty(cancelAffairIds) && AppContext.hasPlugin("doc")) {
                docApi.deleteDocResources(AppContext.getCurrentUser().getId(), cancelAffairIds);
	        }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    protected List<CtpAffair> superviseCancel(List<WorkItem> workitems,Timestamp now) throws BusinessException{
        List<CtpAffair> affair4Message = new ArrayList<CtpAffair>();
        if(workitems == null || workitems.size()==0)
            return affair4Message;
        List<Long> ids = new ArrayList<Long>();
        Map<String,Object> nameParameters = new HashMap<String,Object>();
        for(int i=0;i<workitems.size();i++){
            ids.add((long)((WorkItem)workitems.get(i)).getId());
            //防止in超长，300个一更新，事务上会有问题
            if((i+1) % 300 == 0 || i == workitems.size()-1){
            	nameParameters.put("subObjectId", ids);
                StringBuffer hql=new StringBuffer();
                hql.append("update CtpAffair as a set a.state=:state,a.subState=:subState,a.updateDate=:updateDate,a.delete=1 where a.subObjectId in (:subObjectIds)");
                Map<String,Object> params=new HashMap<String, Object>();
                params.put("state", StateEnum.col_cancel.key());
                params.put("subState", SubStateEnum.col_normal.key());
                params.put("updateDate", now);
                params.put("subObjectIds", ids);
                DBAgent.bulkUpdate(hql.toString(), params);
                /*DBAgent.bulkUpdate("update " + CtpAffair.class.getName() + " set state=?,subState=?,updateDate=?,delete=1 where subObjectId in (?)", StateEnum.col_cancel.key(),SubStateEnum.col_normal.key(),now,ids);*/
                List<CtpAffair> affairs = affairManager.getByConditions(null, nameParameters);
                affair4Message.addAll(affairs);
                ids.clear();
            }
        }
        return affair4Message;
    }

    @Override
    public boolean onWorkitemFinished(EventDataContext context) {

        AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        CtpAffair affair = null;
		try {
			affair = eventData2ExistingAffair(context);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
        //设置运行时长，超时时长等
        EdocSummary summary = null;
        try{
	        if(null!= context.getAppObject()){
	            summary = (EdocSummary)context.getAppObject();
	        }else{
	            summary = edocManager.getEdocSummaryById(affair.getObjectId(),false);
	        }
	        affair.setCompleteTime(now);
	        setTime2Affair(affair,summary);
        } catch(Exception e) {
        	LOGGER.error("",e);
        }
        
        Map<String, Object> businessData = context.getBusinessData();
        int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE)==null ? AUTODELETE : (Integer)businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);
        
        //更新当前处理人信息
        List<CtpAffair> affairs=new ArrayList<CtpAffair>();
        affairs.add(affair);
        EdocHelper.deleteAffairsNodeInfo(summary,affairs );
        try {
        	affair.setState(StateEnum.col_done.key());
            affairManager.updateAffair(affair);
        } catch (BusinessException e) {
        	LOGGER.error("",e);
        }


        Long summaryId = affair.getObjectId();
        if(operationType == STETSTOP) {//终止
            return false;
        }else{
            boolean isHasAtt = false;
            if(context.getBusinessData("isAddAttachmentByOpinion")!=null){
                isHasAtt = Boolean.valueOf(String.valueOf(context.getBusinessData("isAddAttachmentByOpinion")));
            }
            boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
            if(!isSepicalBackedSubmit) {
            	//发送完成事项消息提醒
                MessageCommentParam mc = (MessageCommentParam)context.getBusinessData(COMMENT_MESSAGE);
                List<Long[]> pushMsgMemberList = (List<Long[]>)context.getBusinessData(PUSHMSG_MEMBERLIST); 
            	Boolean ok = EdocMessageHelper.workitemFinishedMessage(affairManager, orgManager, edocManager, userMessageManager, affair, summaryId, isHasAtt,mc,pushMsgMemberList);
            }
        }

        return false;
    }

    @Override
    public boolean onWorkitemAssigned(EventDataContext context) {
        /*context.getWorkItem().getId();//--->v3x_affair

        System.out
                .println("----------------------------------------------------------------------------------------------------------------");
        context.setAppName("test");
        return true;*/
        AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
        //非内容组件封装事件执行
        if (affairData == null)
            return true;
        //首页栏目的扩展字段设置--公文文号、发文单位等--start
        Object docMarkObject= context.getBusinessData("edoc_send_doc_mark");
        Object sendUnitObject= context.getBusinessData("edoc_send_send_unit");
        //OA-40584新建公文，设置督办，督办人替换节点后查看首页待办的数据，没有将发文单位显示出来
        if(docMarkObject==null){
        	docMarkObject=affairData.getBusinessData("edoc_send_doc_mark");
        }
        if(sendUnitObject==null){
        	sendUnitObject= affairData.getBusinessData("edoc_send_send_unit");
        }
        Map<String, Object> extParam = new HashMap<String, Object>();
        extParam.put(AffairExtPropEnums.edoc_edocMark.name(), docMarkObject); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), sendUnitObject);//发文单位
        //OA-43885 首页待办栏目下，待开会议的主持人名字改变后，仍显示之前的名称
        EdocSummary summary = (EdocSummary)context.getAppObject();

        Boolean isCover = false;
        if(null == summary){
        	summary=edocManager.getSummaryByProcessId(context.getProcessId());
        }
        if(summary != null){
            isCover = summary.getCoverTime();
        }
        extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), summary.getSendUnitId());//发文单位ID
        //首页栏目的扩展字段设置--公文文号、发文单位等--end
        List<WorkItem> workitems = context.getWorkitemLists();
        Set<Long> currentNodesInfoSet = new HashSet<Long>();
		Timestamp now = DateUtil.currentTimestamp();
		// CtpAffair affair = new CtpAffair();

        try {
            //控制是否发送协同发起消息
            Boolean _isSendMessage = context.isSendMessage();
            Boolean isSendMessage = true;
            if (_isSendMessage != null && !_isSendMessage)
                isSendMessage = false;

            Long deadline = null;
            Long remindTime = null;
            String dealTermType = null;
            String dealTermUserId = null;
            Date deadLineRunTime= null;
            if (!EdocUtil.isBlank(context.getDealTerm())) {
            	if(context.getDealTerm().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d$")){
                	// 直接把处理的具体时间set到affair表中
                	deadLineRunTime =  DateUtil.parse(context.getDealTerm(), "yyyy-MM-dd HH:mm");
                	//当自定义时间时，不设置deadline值，区分超期时间是具体时间点和相对时间 两种不同的方式
                	/*deadline = workTimeManager.getDealWithTimeValue(now, deadLineRunTime, affairData.getSummaryAccountId());
					deadline = deadline/1000/60;*/
				}else if(context.getDealTerm().matches("^-?\\d+$")){
            		 deadline = Long.parseLong(context.getDealTerm());
            	        if(deadline != null && deadline != 0){
                        	//按工作日进行设置时间
                        	deadLineRunTime = workTimeManager.getCompleteDate4Nature(now, deadline, affairData.getSummaryAccountId());
                        }
            	}
                if (null != context.getDealTermType() && !"".equals(context.getDealTermType().trim())) {
                    dealTermType = context.getDealTermType().trim();
                } else {
                    dealTermType = "0";
                }
                if (null != context.getDealTermUserId() && !"".equals(context.getDealTermUserId().trim())) {
                    dealTermUserId = context.getDealTermUserId();
                } else {
                    dealTermUserId = "-1";
                }
            }

            if (Strings.isNotBlank(context.getRemindTerm()) && !("undefined").equals(context.getRemindTerm()) && !("null").equals(context.getRemindTerm())) {
                remindTime = Long.parseLong(context.getRemindTerm());
            }

            List<CtpAffair> affairs = new ArrayList<CtpAffair>(workitems.size());
            Long activityId = null;
            for (WorkItem workitem : workitems) {
                Long memberId;
                try {
                    memberId = Long.parseLong(workitem.getPerformer());
                } catch (Exception e) {
                    memberId = 123456L;
                }
                /**************加上当前待办人**************/
                if(!"zhihui".equals(context.getPolicyId())&&!"inform".equals(context.getPolicyId()) && currentNodesInfoSet.size() < 10){//知会 节点不算待办
					currentNodesInfoSet.add(memberId);
                }
				/**************加上当前待办人**************/

				CtpAffair affair = new CtpAffair();
				affair.setPreApprover(affairData.getMemberId());
                affair.setIdIfNew();

                ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
                affair.setApp(app.key());
                //设置subApp
                if(ApplicationCategoryEnum.edocRec.equals(app) && null != summary.getProcessType()){
                	if(2 == summary.getProcessType().intValue()){
                		affair.setSubApp(ApplicationSubCategoryEnum.edocRecRead.getKey());
                	}else if(1== summary.getProcessType().intValue()){
                		affair.setSubApp(ApplicationSubCategoryEnum.edocRecHandle.getKey());
                	}
                }

                affair.setTrack(0);
                affair.setDelete(false);
                affair.setSubObjectId(Long.valueOf(workitem.getId()));
                affair.setMemberId(memberId);
                //设置事项为待办和协同待办未读
                affair.setState(affairData.getState());
                affair.setSubState(SubStateEnum.col_pending_unRead.key());
                affair.setSenderId(affairData.getSender());
                affair.setSubject(affairData.getSubject());
                String nodePolicy=context.getPolicyId();
        		if(nodePolicy != null){nodePolicy=nodePolicy.replaceAll(new String(new char[]{(char)160}), " ");}
        		affair.setNodePolicy(nodePolicy);
                AffairUtil.setHasAttachments(affair,affairData.getIsHasAttachment()==null ? false:affairData.getIsHasAttachment());
                //协同的ID
                affair.setObjectId(affairData.getModuleId());
                affair.setDeadlineDate(deadline);
                try {
                    affair.setDealTermType(Integer.parseInt(dealTermType));
                } catch (Throwable e) {
                    affair.setDealTermType(0);
                }
                try {
                    affair.setDealTermUserid(Long.parseLong(dealTermUserId));
                } catch (Throwable e) {
                    affair.setDealTermUserid(-1l);
                }
                affair.setRemindDate(remindTime);
                if(null != remindTime && remindTime.equals(0L)){
                	affair.setRemindDate(null);
                }

                affair.setReceiveTime(now);
                affair.setUpdateDate(now);
                affair.setApp(affairData.getModuleType());//

                affair.setCreateDate(affairData.getCreateDate()==null?now:affairData.getCreateDate());
                //affair.sets(isSendMessage);
                affair.setTempleteId(affairData.getTemplateId());

                affair.setImportantLevel(affairData.getImportantLevel());
                affair.setResentTime(affairData.getResentTime());
                affair.setForwardMember(affairData.getForwardMember());
                
                affair.setProcessId(workitem.getProcessId());
                affair.setCaseId(workitem.getCaseId());
                
                affair.setOrgAccountId(summary.getOrgAccountId());
                
                //设置加签、知会、会签的人员id
                affair.setFromId(Strings.isNotBlank(context.getAddedFromId())? Long.valueOf(context.getAddedFromId()) : null);

                //回退的情况下覆盖fromId，显示的时候通过subState来区分,设置回退的人员id
                Map<String, Object> businessData = context.getBusinessData();
                int operationType = businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE)==null ? AUTODELETE : (Integer)businessData.get(EventDataContext.CTP_AFFAIR_OPERATION_TYPE);

                //回退、指定回退流程重走
				if(operationType==WITHDRAW||operationType==SPECIAL_BACK_RERUN){
					affair.setBackFromId(AppContext.getCurrentUser().getId());
				}

                affair.setBodyType(affairData.getContentType());
                //                affair.setNodePolicy(eventData.getN);
                activityId=Long.parseLong(workitem.getActivityId());
                affair.setActivityId(activityId);

                //回退导致新生成的事项
                /*if (!isSendMessage) {
                    DateSharedWithWorkflowEngineThreadLocal.addToAffairMap(memberId, affair.getId());
                }*/

                //三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
                affair.setFinish(false);
                affair.setCoverTime(false);
                affair.setDueRemind(true);
                //lijl添加if,OA-42474.开发---对外接口

                affair.setSummaryState(summary.getState());

                affair.setExpectedProcessTime(deadLineRunTime);
                AffairUtil.setExtProperty(affair, extParam);
                affairs.add(affair);
            }
            
            context.setBusinessData(ASSIGNED_AFFAIRS, affairs);
            
            affairData.setAffairList(affairs);
            affairData.setIsSendMessage(isSendMessage);

            saveListMap(affairData,now,isCover);


            if(null!=summary){//正常处理生成
            	EdocHelper.setCurrentNodesInfo(summary,Strings.join(currentNodesInfoSet, ";"));
            	edocManager.updateCurrentNodeUser(summary.getId(),summary.getCurrentNodesInfo());
			}
            
            List<CtpAffair> affairList = (List<CtpAffair>) ThreadLocalUtil.get("afffairList");
            if(affairList != null) {
            	affairList.addAll(affairData.getAffairList());
            }

        } catch (Exception e) {
            LOGGER.error(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
            // throw new BPMException(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
        }
        return true;
    }

    private void saveListMap(AffairData affairData,Date receiveTime,Boolean isCover) {
        if (affairData == null)
            return;

        try {
            Long senderId = affairData.getSender();
            List<CtpAffair> affairList = affairData.getAffairList();
            Boolean isSendMessage = affairData.getIsSendMessage();

            if (affairList == null || affairList.isEmpty())
                return;

            String subject = affairData.getSubject();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(affairData.getForwardMember())) {
            	forwardMember = affairData.getForwardMember();
            	forwardMemberFlag = 1;
            }

            Integer importantLevel = affairData.getImportantLevel();
            String bodyContent = affairData.getBodyContent();
            String bodyType = affairData.getContentType();
            Date bodyCreateDate = affairData.getBodyCreateDate();

            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            CtpAffair aff = affairList.get(0);
            int app = aff.getApp();

            CtpAffair senderAffair = affairManager.getSenderAffair(aff.getObjectId());
            Long[] userInfoData  = new Long[2];
            if(senderAffair!=null){
                userInfoData[0] = senderAffair.getMemberId();
                userInfoData[1] = senderAffair.getTransactorId();
            }
            DBAgent.saveAll(affairList);
            for (CtpAffair affair : affairList) {
                if (isSendMessage) {
                    getReceiver(affairData.getIsSendMessage(), affair, app, receivers, receivers1);
                }
                // 提前提醒，超期提醒
                affairExcuteRemind(affair, affairData.getSummaryAccountId(),affair.getExpectedProcessTime());
            }
            // 生成事项消息提醒
            if (isSendMessage) {
                V3xOrgMember sender = null;
                try {
                    sender = orgManager.getMemberById(senderId);
                } catch (Exception e1) {
                    LOGGER.error("", e1);
                    return;
                }
                //{1}发起协同:《{0}{2,choice,0|#1# (由{3}原发)}》
                Object[] subjects = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember };
                sendMessage(aff,app, receivers, receivers1, sender, subjects, importantLevel, bodyContent, bodyType, bodyCreateDate,userInfoData);
            }
            //发送流程超期消息
            if(isCover != null && isCover){
            	edocMessagerManager.transSendMsg4ProcessOverTime(aff, receivers, receivers1);
            }

            // 在此调用CallBack
            if (affairList == null || affairList.size() == 0)
                return;

            //          if(DateSharedWithWorkflowEngineThreadLocal.isNeedIndex()) {
            //              Affair affair0 = affairList.get(0);
            //              if (affair0.getApp() == ApplicationCategoryEnum.collaboration.key()) {
            //                  CallbackHandler callback = CallbackHandler.getCallbackHandler("ColIndex");
            //                  callback.invoke(affair0.getObjectId().toString());
            //              }
            //          }
        } catch (Exception e) {
        	LOGGER.error("",e);
        }
    }

    private void getReceiver(boolean isSendMessage, CtpAffair affair, int app, List<MessageReceiver> receivers,
            List<MessageReceiver> receivers1) {
        Long theMemberId = affair.getMemberId();
        if (app == ApplicationCategoryEnum.collaboration.key()) {
            if (isSendMessage) {
                Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(
                        ApplicationCategoryEnum.collaboration.key(), theMemberId, affair.getTempleteId());
                if (agentMemberId != null) {
                	MessageReceiver rec = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending",
                            affair.getId().toString());
                	if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec.setTrack(true);
                	}
                    receivers.add(rec);
                    MessageReceiver rec1 = new MessageReceiver(affair.getId(), agentMemberId, "message.link.col.pending",
                            affair.getId().toString());
                    if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec1.setTrack(true);
                	}
                    receivers1.add(rec1);
                } else {
                	MessageReceiver rec = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending",
                            affair.getId().toString());
                	if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec.setTrack(true);
                	}
                    receivers.add(rec);
                }
            }
        } else if (app == ApplicationCategoryEnum.edocSend.key() || app == ApplicationCategoryEnum.edocRec.key()
                || app == ApplicationCategoryEnum.edocSign.key()) {
            if (isSendMessage) {
                Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                        theMemberId);
                if (agentMemberId != null) {
                	MessageReceiver rec = new MessageReceiver(affair.getId(), affair.getMemberId(),
                            "message.link.edoc.pending", affair.getId().toString());
                	if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec.setTrack(true);
                	}
                    receivers.add(rec);
                    MessageReceiver rec1 = new MessageReceiver(affair.getId(), agentMemberId, "message.link.edoc.pending",
                            affair.getId().toString());
                    if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec1.setTrack(true);
                	}
                    receivers1.add(rec1);
                } else {
                	MessageReceiver rec = new MessageReceiver(affair.getId(), affair.getMemberId(),
                            "message.link.edoc.pending", affair.getId().toString());
                	if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                		rec.setTrack(true);
                	}
                    receivers.add(rec);
                }
            }
        }
    }

    private void sendMessage(CtpAffair affair,int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1,
            V3xOrgMember sender, Object[] subjects, Integer importantLevel, String bodyContent, String bodyType,
            Date bodyCreateDate, Long[] userInfoData) {
    	Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
    	//Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
        if(app == ApplicationCategoryEnum.collaboration.key()){
            try {
            	MessageContent content = MessageContent.get("col.send", subjects).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel);
            	if (null != affair && null != affair.getTempleteId()) {
            		content.setTemplateId(affair.getTempleteId());
            	}
                userMessageManager.sendSystemMessage(content,
                        ApplicationCategoryEnum.edoc, sender.getId(), receivers, systemMessageFilterParam);
            } catch (Exception e) {
                LOGGER.error("发起协同消息提醒失败!", e);
            }
            if(receivers1 != null && receivers1.size() != 0){
                try {
                	MessageContent content1 = MessageContent.get("col.send", subjects).setBody(bodyContent, bodyType, bodyCreateDate).add("col.agent").setImportantLevel(importantLevel);
                	if (null != affair && null != affair.getTempleteId()) {
                		content1.setTemplateId(affair.getTempleteId());
                	}
                    userMessageManager.sendSystemMessage(content1,
                            ApplicationCategoryEnum.edoc, sender.getId(), receivers1, systemMessageFilterParam);
                } catch (Exception e) {
                    LOGGER.error("发起协同消息提醒失败!", e);
                }
            }
        }else if(app == ApplicationCategoryEnum.edocSend.key()
                || app == ApplicationCategoryEnum.edocRec.key()
                || app == ApplicationCategoryEnum.edocSign.key()){

            ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(app);
            if(userInfoData.length==2 && userInfoData[1]!=null){//登记的时候是代理人进行登记的。
                try {
                    String agentToName ="";
                    Long agentToId = userInfoData[1];
                    try{
                        agentToName = orgManager.getMemberById(agentToId).getName();
                    }catch(Exception e){
                        LOGGER.error("获取代理人名字抛出异常",e);
                    }
                    MessageContent content = MessageContent.get("edoc.send", subjects[0], agentToName,app).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel).add("edoc.agent.deal",sender.getName());
                    if (null != affair && null != affair.getTempleteId()) {
                		content.setTemplateId(affair.getTempleteId());
                	}
                    userMessageManager.sendSystemMessage(content,
                            appEnum, agentToId, receivers, systemMessageFilterParam);
                    if(receivers1 != null && receivers1.size() != 0){
                    	MessageContent content1 = MessageContent.get("edoc.send", subjects[0], agentToName,app).setBody(bodyContent, bodyType, bodyCreateDate).add("edoc.agent.deal",sender.getName()).add("col.agent").setImportantLevel(importantLevel);
                    	if (null != affair && null != affair.getTempleteId()) {
                    		content1.setTemplateId(affair.getTempleteId());
                    	}
                        userMessageManager.sendSystemMessage(content1,
                                appEnum, agentToId, receivers1, systemMessageFilterParam);
                    }
                } catch (Exception e) {
                    LOGGER.error("发起公文消息提醒失败!", e);
                }
            }else{
                try {
                	MessageContent content = MessageContent.get("edoc.send", subjects[0], sender.getName(),app).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel);
                	if (null != affair && null != affair.getTempleteId()) {
                		content.setTemplateId(affair.getTempleteId());
                	}
                    userMessageManager.sendSystemMessage(content,
                            appEnum, sender.getId(), receivers, systemMessageFilterParam);
                } catch (Exception e) {
                    LOGGER.error("发起公文消息提醒失败!", e);
                }
                if(receivers1 != null && receivers1.size() != 0){
                    try {
                    	MessageContent content1 = MessageContent.get("edoc.send", subjects[0], sender.getName(),app).setBody(bodyContent, bodyType, bodyCreateDate).add("col.agent").setImportantLevel(importantLevel);
                    	if (null != affair && null != affair.getTempleteId()) {
                    		content1.setTemplateId(affair.getTempleteId());
                    	}
                        userMessageManager.sendSystemMessage(content1,
                                appEnum, sender.getId(), receivers1, systemMessageFilterParam);
                    } catch (Exception e) {
                        LOGGER.error("发起公文消息提醒失败!", e);
                    }
                }
            }
        }
    }

    private void affairExcuteRemind(CtpAffair affair, Long summaryAccountId,Date deadLineRunTime) {
        if (affair.getApp() == ApplicationCategoryEnum.collaboration.key()
                || affair.getApp() == ApplicationCategoryEnum.edoc.key()
                || affair.getApp() == ApplicationCategoryEnum.edocRec.key()
                || affair.getApp() == ApplicationCategoryEnum.edocSend.key()
                || affair.getApp() == ApplicationCategoryEnum.edocSign.key()) {
            //超期提醒
            try {
               /* Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                Long deadLine = affair.getDeadlineDate();*/
            	if(deadLineRunTime!=null){

                     Long affairId = affair.getId();
                     {
                         String name = "DeadLine" + affairId;

                         Map<String, String> datamap = new HashMap<String, String>(2);

                         datamap.put("isAdvanceRemind", "1");
                         datamap.put("affairId", String.valueOf(affairId));

                         
                         //增加30秒随机数
                     	int randomInOneMinte = (int)(Math.random()*30+1)*1000;
                        Date _runDate = new java.sql.Timestamp(deadLineRunTime.getTime()+randomInOneMinte);
                         
                         QuartzHolder.newQuartzJob(name, _runDate, "affairIsOvertopTimeJob", datamap);
                     }

                     Long remindTime = affair.getRemindDate();
                     if (remindTime != null && !Long.valueOf(0).equals(remindTime) && !Long.valueOf(-1).equals(remindTime)){
                   	     Date advanceRemindTime = workTimeManager.getRemindDate(deadLineRunTime, remindTime);//.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine - remindTime, summaryAccountId);

                         String name = "Remind" + affairId;

                         Map<String, String> datamap = new HashMap<String, String>(2);

                         datamap.put("isAdvanceRemind", "0");
                         datamap.put("affairId", String.valueOf(affairId));

                         QuartzHolder.newQuartzJob(name, advanceRemindTime, "affairIsOvertopTimeJob", datamap);
                     }
            	}
               
            } catch (Exception e) {
                LOGGER.error("获取定时调度器对象失败", e);
            }
        }
    }

    protected Log getLog(){
        return LOGGER;
    }

    @Override
	public boolean onWorkflowAssigned(List<EventDataContext> contextList) {
    	if (Strings.isNotEmpty(contextList)) {
    		ThreadLocalUtil.set("afffairList", new ArrayList<CtpAffair>());
    		boolean isStart = false;
			for (EventDataContext context : contextList) {
				if("true".equals(context.getBusinessData("isStart"))) {
					isStart = true;
				}
                this.onWorkitemAssigned(context);
            }
			
			List<CtpAffair> affairList = (List<CtpAffair>) ThreadLocalUtil.get("afffairList");
            if(affairList != null) {
            	EdocAffairsAssignedEvent event = new EdocAffairsAssignedEvent(this);
            	event.setAffairs(affairList);
            	event.setStart(isStart);
            	EventDispatcher.fireEventAfterCommit(event);
            }
		}
		return true;
    }


    /**
     * 设置Affair的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     * @throws BusinessException
     */
    public void setTime2Affair(CtpAffair affair,EdocSummary summary) throws BusinessException{
    	//工作日计算运行时间和超期时间。
    	long runWorkTime = 0L;
    	long orgAccountId = summary.getOrgAccountId();
		runWorkTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(),new Date(),orgAccountId);
		runWorkTime = runWorkTime/(60*1000);
		Long deadline = 0l;
		Long  workDeadline = 0l;
		if((affair.getExpectedProcessTime()!=null || affair.getDeadlineDate()!=null) &&  !Long.valueOf(0).equals(affair.getDeadlineDate())){
		    if (affair.getDeadlineDate()!= null) {
		    	deadline = affair.getDeadlineDate().longValue();
		    } else {
		    	deadline = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), affair.getExpectedProcessTime(), orgAccountId);
				deadline = deadline/1000/60;
		    }
		    workDeadline = workTimeManager.convert2WorkTime(deadline, orgAccountId);
		}
		//超期工作时间
		Long overWorkTime = 0L;
		//设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
		if(workDeadline!=null &&  workDeadline!=0){
			long ow = runWorkTime - workDeadline;
			overWorkTime =  ow >0 ? ow: 0l ;
		}
    	//自然日计算运行时间和超期时间
    	Long runTime = (System.currentTimeMillis() - affair.getReceiveTime().getTime())/(60*1000);
    	Long overTime = 0L;
    	if( affair.getDeadlineDate()!= null && affair.getDeadlineDate()!= 0){
    		Long o = runTime - affair.getDeadlineDate();
    		overTime = o >0 ? o : null;
    	}

    	//避免时间到了定时任务还没有执行。暂时不需要考虑是否在工作时间，因为定时任务那边也没有考虑，先保持一致。
    	if(null != affair.getExpectedProcessTime() && new Date().after(affair.getExpectedProcessTime())){  
    		affair.setCoverTime(true);
    	}
    	
    	if(affair.isCoverTime()!=null && affair.isCoverTime()){
    	    if(Long.valueOf(0).equals(overTime)) overTime = 1l;
    	    if(Long.valueOf(0).equals(overWorkTime)) overWorkTime = 1l;
    	}
    	affair.setOverTime(overTime);
    	affair.setOverWorktime(overWorkTime);
    	affair.setRunTime(runTime);
    	affair.setRunWorktime(runWorkTime);
    }


    private void updateAffairBySubObject(List<Long> workItems,Integer state,Integer subState,Long objectId) {
        int MaxCommitNumber = 300;
        int length = workItems.size();
        List<Long> workitemIds = new ArrayList<Long>();
        int i = 0;
        Map<String, Object> nameParameters = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder("update " + CtpAffair.class.getName() + " set updateDate=:updateDate");
        nameParameters.put("updateDate", new Date());
        if(state != null) {
            sql.append(",state=:state");
            nameParameters.put("state", state);
        }
        if(subState != null) {
            sql.append(",subState=:subState");
            nameParameters.put("subState", subState);
        }
        sql.append(" where objectId=:objectId and subObjectId in (:subObjectId)");
        nameParameters.put("objectId", objectId);
        for (Long workItem : workItems) {
            i++;
            workitemIds.add(workItem);
            if(i % MaxCommitNumber == 0 || i == length) {
                nameParameters.put("subObjectId", workitemIds);
                DBAgent.bulkUpdate(sql.toString(), nameParameters);
                workitemIds = new ArrayList<Long>();
            }
        }
    }

    private void updateStepBackAffair(List<Long> workItems, Integer state, Integer subState,Long objectId) {
        int MaxCommitNumber = 300;
        int length = workItems.size();
        List<Long> workitemIds = new ArrayList<Long>();
        int i = 0;
        Map<String, Object> nameParameters = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder("update CtpAffair as affair set updateDate=:updateDate");
        nameParameters.put("updateDate", new Date());
    	if(state != null) {
            sql.append(",state=:state");
            nameParameters.put("state", state);
        }
        if(subState != null) {
            sql.append(",subState=:subState");
            nameParameters.put("subState", subState);
        }
        sql.append(" where objectId=:objectId and subObjectId in (:subObjectId)");
        nameParameters.put("objectId", objectId);
        for (Long workItem : workItems) {
            i++;
            workitemIds.add(workItem);
            if(i % MaxCommitNumber == 0 || i == length) {
                nameParameters.put("subObjectId", workitemIds);
                DBAgent.bulkUpdate(sql.toString(), nameParameters);
                workitemIds = new ArrayList<Long>();
            }
        }
    }

    //通过workitemId得到affair
    protected CtpAffair eventData2ExistingAffair(EventDataContext eventData) throws BusinessException {
        
        CtpAffair affair = null;
        WorkItem workitem = eventData.getWorkItem();

        if (null != eventData.getBusinessData(CTP_AFFAIR)) {
            affair = (CtpAffair) eventData.getBusinessData(CTP_AFFAIR);
        }
        else {
            affair = affairManager.getAffairBySubObjectId(workitem.getId());
        }

        return affair;
    }

}
