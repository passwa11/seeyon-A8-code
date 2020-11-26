/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.listener;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.MessageCommentParam;
import com.seeyon.apps.collaboration.bo.SendCollResult;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationAffairsAssignedEvent;
import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.event.CollaborationReceivetimeChangeEvent;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.manager.ColPubManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColSelfUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.trace.enums.WFTraceConstants;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.enums.WorkflowBugTypeEnum;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;
import net.joinwork.bpm.engine.wapi.WorkItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mujun
 *
 */
public class WorkFlowEventListener extends AbstractEventListener {

    private static Log log = LogFactory.getLog(WorkFlowEventListener.class);
    

    public static final String COLSUMMARY_CONSTANT = "ColSummary";
    
    public static final String CTPAFFAIR_CONSTANT = "CtpAffair";
    public static final String AFFAIR_SUB_STATE = "subState";
    public static final String WF_APP_GLOBAL = "WF_APP_GLOBAL"; //流程和应用穿透传参的KEY
    public static final String WF_APP_GLOBAL_REPEAT_AFFMAP = "WF_APP_GLOBAL_REPEAT_AFFMAP"; //重复跳过的相关数据
    public static final String WF_ALL_VALID_AFFAIRS = "WF_ALL_VALID_AFFAIRS"; //重复跳过的相关数据
    public static final String BUSINESS_DATA ="BusinessData";//WorkflowBpmContext.BusinessData
    public static final String CANCELED_MIDTOAID_MAP = "WORKITEMCANCELED_MEMBERIDTOAFFAIRID";  //被权限的事项的人员ID-事项ID
    public static final String CANCELED_MIDTOARRAY_MAP = "WORKITEMCANCELED_MEMBERIDTOARRAY"; //被权限的事项的人员ID-数组
    public static final String CANCELED_AFFAIRS = "CANCELED_AFFAIRS";
    public static final String ASSIGNED_AFFAIRS = "ASSIGNED_AFFAIRS";//生产的事项
   
    /**
     * 当前操作affairId
     */
    public static final String CURRENT_OPERATE_AFFAIR_ID= "CURRENT_OPERATE_AFFAIR_ID";
    /**
     * 当前操作MEMBER
     */
    public static final String CURRENT_OPERATE_MEMBER_ID= "CURRENT_OPERATE_MEMBER_ID";
   
    public static final String CURRENT_OPERATE_COMMENT_CONTENT= "CURRENT_OPERATE_COMMENT_CONTENT";//当前操作comment
    public static final String CURRENT_OPERATE_COMMENT_EXTATT1= "CURRENT_OPERATE_COMMENT_EXTATT1";//当前操作comment.extAtt1
    public static final String CURRENT_OPERATE_COMMENT_MESSAGE= "CURRENT_OPERATE_COMMENT_MESSAGE"; //messagecomment对象
    /**
     * 当前操作comment
     */
    public static final String CURRENT_OPERATE_COMMENT_ID= "CURRENT_OPERATE_COMMENT_ID";
    /**
     * 当前操作summaryId
     */
    public static final String CURRENT_OPERATE_SUMMARY_ID= "CURRENT_OPERATE_SUMMARY_ID";
    /**
     * 当前操作是否跟踪流程
     */
    public static final String CURRENT_OPERATE_TRACK_FLOW= "CURRENT_OPERATE_TRACK_FLOW";
    
    /**
     * 当前操作是否需要发送消息
     */
    public static final String CURRENT_OPERATE_NEED_SEND_MESSAGE= "CURRENT_OPERATE_NEED_SEND_MESSAGE";
    
    /**
     * 是否为管理员回退
     */
    public static final String IS_ADMIN_STEP_BACK = "IS_ADMIN_STEP_BACK";

    private ColManager            colManager;
    private AffairManager         affairManager;
    private SuperviseManager      superviseManager;
    private WorkTimeManager       workTimeManager;
    private OrgManager            orgManager;
    private UserMessageManager    userMessageManager;
    private ColMessageManager     colMessageManager;
    private CtpTrackMemberManager trackManager;
    private TemplateManager       templateManager;
    private ColPubManager         colPubManager;
    private AttachmentManager     attachmentManager;
    private WorkflowApiManager    wapi;
    private CAPFormManager capFormManager;
    private MainbodyManager contentManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
    private DocApi docApi;
    private FileManager fileManager;
    private AppLogManager appLogManager;
    private ProcessLogManager processLogManager;
    private CommentManager ctpCommentManager;

    public void setCtpCommentManager(CommentManager ctpCommentManager) {
        this.ctpCommentManager = ctpCommentManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public ProcessLogManager getProcessLogManager() {
        return processLogManager;
    }

    public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	
	public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
	
	public void setContentManager(MainbodyManager contentManager) {
		this.contentManager = contentManager;
	}
	
	
	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }



	/**
     * @return the wapi
     */
    public WorkflowApiManager getWapi() {
        return wapi;
    }


    /**
     * @param wapi the wapi to set
     */
    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public ColMessageManager getColMessageManager() {
        return colMessageManager;
    }

    public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setColPubManager(ColPubManager colPubManager) {
        this.colPubManager = colPubManager;
    }

    public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
    
    public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
    
    @Override
    public String getModuleName() {
        return "collaboration";
    }

    /**
     * 就绪到等待状态
     * @param context
     * @return
     */
    public boolean onWorkitemReadyToWait(EventDataContext context) {
		try {
			/*List<WorkItem> workitems = context.getWorkitemLists();
			
			Long currentAffairID = (Long) context.getBusinessData(CURRENT_OPERATE_AFFAIR_ID);
			*/
			CtpAffair affair = (CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT);

			if (affair == null) {
				log.error("onWorkitemReadyToWait affair is null" + CURRENT_OPERATE_AFFAIR_ID);
				return false;
			}

			affair.setState(StateEnum.col_stepBack.getKey());
			// 如果当前状态是已经指定回退被回退节点再次指定回退
			if (affair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()) {
				affair.setSubState(SubStateEnum.col_pending_specialBackCenter.getKey());
			} else {
				affair.setSubState(SubStateEnum.col_pending_specialBack.getKey());
			}
			//affair.setBackFromId(affair.getMemberId());

			affair.setUpdateDate(new java.util.Date());
			this.affairManager.updateAffair(affair);

			ColSummary summary = (ColSummary) context.getAppObject();
			summary.setSubState(CollaborationEnum.SubState.SpecialBack.ordinal());
			colManager.updateColSummary(summary);
			
			//删除原来的定时任务（超期提醒、提前提醒）
            if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
            	QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
            }
            if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0 )|| affair.getExpectedProcessTime() != null) {
                QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                QuartzHolder.deleteQuartzJob("DeadLine"  + affair.getObjectId() + "_" + affair.getActivityId());
            }
            //指定回退-提交给我（回退节点删除多次消息提醒任务）
            ColUtil.deleteCycleRemindQuartzJob(affair, false);

		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

    //取回
    @Override
    public boolean onWorkitemTakeBack(EventDataContext context) {
    	CtpAffair affair;
		try {
			affair = eventData2ExistingAffair(context);
			if (affair == null){
			    return false;
			}
			//在ColManagerImpl 中 transTakeBack方法也有更新affair的语句，是否要将本监听事件中的更新affair语句删掉 ？
//			affair.setState(StateEnum.col_takeBack.key());
//			affair.setSubState(SubStateEnum.col_normal.key());
//			Timestamp now = new Timestamp(System.currentTimeMillis());
//			affair.setUpdateDate(now);
//			affairManager.updateAffair(affair);
		} catch (BusinessException e) {
			log.error("", e);
		}

        return true;
    }
    /**
     * 事项结束
     */
    public boolean onWorkitemFinished(EventDataContext eventData) {
        try {
            Map<String,Object> OverTimecondtiontMap = new HashMap<String,Object>();
            OverTimecondtiontMap.put("overFlag",eventData.getBusinessData("overFlag"));//设置是否是超期的流程
            Boolean __hasExeWorkItemFinishedOuter =  (Boolean)eventData.getBusinessData("__hasExeWorkItemFinishedOuter");
            String messageRule = eventData.getMessageRule();
            eventData.setBusinessData("messageRule", messageRule);
            if(__hasExeWorkItemFinishedOuter != null && __hasExeWorkItemFinishedOuter){
                log.info(AppContext.currentUserLoginName()+",workfloweventliserter.onWorkitemFinished return!");
                return true;
            }
            
            log.info(AppContext.currentUserLoginName()+",workfloweventliserter.onWorkitemFinished start....  ");
            
            CtpAffair affair = (CtpAffair)eventData.getBusinessData(CTPAFFAIR_CONSTANT);
            String subState = (String)eventData.getBusinessData(AFFAIR_SUB_STATE);
            if (affair == null){
                affair = affairManager.getAffairBySubObjectId( eventData.getWorkItem().getId());
            }
            if(affair == null) {
                log.error("事项处理WorkFlowEventListener.onWorkitemFinished 获取affair为空，workitemid:"+eventData.getWorkItem().getId()  );
                throw new RuntimeException();
            }
            boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());

            User user = AppContext.getCurrentUser();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            //记录日志
            logInfo4WorkItemFinished(affair, user, now);


            affair.setCompleteTime(now);
            affair.setUpdateDate(now);
            affair.setState(StateEnum.col_done.key());
            if(Strings.isNotBlank(subState)){
                affair.setSubState(Integer.valueOf(subState));
            }else{
                affair.setSubState(SubStateEnum.col_normal.key());
            }
            
            //通过AI智能处理的要标记
            String aiProcessingFlag = (String) eventData.getBusinessData("aiProcessingFlag");
            if("true".equals(aiProcessingFlag)) {
            	affair.setAiProcessing(true);
            }
            
            //判断代理人
            if(AppContext.getCurrentUser()!= null && !affair.getMemberId().equals(AppContext.getCurrentUser().getId())){
                affair.setTransactorId(AppContext.getCurrentUser().getId());
            }
            //设置运行时长，超时时长等
            ColSummary summary = null;
            if(null!= eventData.getAppObject()){
                summary = (ColSummary)eventData.getAppObject();
            }else{
                summary = colManager.getColSummaryById(affair.getObjectId());
            }

            ColUtil.setTime2Affair(affair,summary);
            affairManager.updateAffair(affair);

            //处理协同时，删除节点超期后多次提醒定时任务
            ColUtil.deleteCycleRemindQuartzJob(affair, true);
            
            Long summaryId = affair.getObjectId();
            int operationType = (Integer) ((Integer) eventData.getBusinessData().get("operationType")==null ? -1:eventData.getBusinessData().get("operationType"));
            if(operationType == 8){
                return true;
            }else{
                if(!isSepicalBackedSubmit){//指定回退提交在外层发消息
                    Comment c=  (Comment)eventData.getBusinessData().get("comment");
                    if(c!=null){
                    	OverTimecondtiontMap.put("messageRule", messageRule);
                    	OverTimecondtiontMap.put("currentNodeLast",eventData.getBusinessData("currentNodeLast"));
                        colMessageManager.workitemFinishedMessage(c,affair, summaryId,OverTimecondtiontMap);
                    }
                }
            }
            if(Strings.isNotBlank(eventData.getGoNextMsg())){//当前节点中的待办人、流程的跟踪人收到消息：节点处理率达到模板设置要求，可继续流转
            	//TODO
            }
        } catch (Throwable e) {
            log.error("事项处理WorkFlowEventListener.onWorkitemFinished：", e);
            throw new RuntimeException(e);
        }
        log.info("workfloweventliserter.onWorkitemFinished end....");
        return true;
    }

    private void logInfo4WorkItemFinished(CtpAffair affair, User user, Timestamp now) {
        StringBuilder sb = new StringBuilder();
        sb.append("事项ID：").append(affair.getId());
        sb.append("，WorkItemFinished处理协同时间：").append(now);
        if (null == user) {
            sb.append("，处理客户端：pc");
        } else {
            sb.append("，处理客户端：").append(user.isFromM1() ? "m1" : "pc");
            sb.append("，当前用户：").append(user.getName());
            if (!user.getId().equals(affair.getMemberId())) {
                List<Long> ownerIds = MemberAgentBean.getInstance().getAgentToMemberId(ModuleType.collaboration.getKey(), user.getId());
                if (Strings.isNotEmpty(ownerIds) && ownerIds.contains(affair.getMemberId())) {
                    sb.append("代理校验：True");
                } else {
                    sb.append("代理校验：False");
                }
            }
        }
        log.info(sb.toString());
    }
    
    /**
     * 设置Summary的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     */
    public void setTime2Summary(ColSummary summary) throws BusinessException {
        if(summary == null){
            return ;
        }
        //工作日计算运行时间和超期时间。
        Long orgAccountId = summary.getOrgAccountId();
        Date deadLine = summary.getDeadlineDatetime();
        //超期工作时间
        Long overWorkTime = 0L;
        //设置了处理期限才进行计算,没有设置处理期限的话,默认为0;
        Long overTime = 0L;
        if(deadLine!=null){
        	long ow = workTimeManager.getDealWithTimeValue(deadLine,new Date(),orgAccountId);
        	overWorkTime =  ow >0 ? ow: null ;

        	Long o = (System.currentTimeMillis() - deadLine.getTime())/(60*1000);
            overTime = o >0 ? o : null;

        }
        if(summary.isCoverTime()!=null && summary.isCoverTime()){
            if(Long.valueOf(0).equals(overTime)) overTime = 1l;
            if(Long.valueOf(0).equals(overWorkTime)) overWorkTime = 1l;
        }

        //自然日计算运行时间和超期时间
        long runTime = 0L;
        long runWorkTime =0L;
        Date startDate = summary.getCreateDate();
        if(null != startDate){
        	runTime = (System.currentTimeMillis() - startDate.getTime())/(60*1000);
        	runWorkTime = workTimeManager.getDealWithTimeValue(startDate,new Date(),orgAccountId);
        	runWorkTime = runWorkTime/(60*1000);
        }

        summary.setOverTime(overTime);
        summary.setOverWorktime(overWorkTime);
        summary.setRunTime(runTime == 0l ? 1: runTime);
        summary.setRunWorktime(runWorkTime == 0l ? 1 : runWorkTime);
    }

    /**
     * 流程结束
     * @param context
     * @return
     */
    @Override
    public boolean onProcessFinished(EventDataContext context) {
    	ColSummary colSummary = null;
    	//colSummary 是否是从外部传入的
    	boolean fromOuter = true;
        try {
        	
    		if(context.getAppObject()!= null){
    			colSummary = (ColSummary)context.getAppObject();
    		}
            
    		if (colSummary == null) {
                fromOuter = true;
                if (context.getBusinessData(COLSUMMARY_CONSTANT) != null) {
                    colSummary = (ColSummary) context.getBusinessData(COLSUMMARY_CONSTANT);
                }
            }
    		
            if(colSummary == null){
                colSummary = colManager.getColSummaryByProcessId(Long.valueOf(context.getProcessId()));
                fromOuter = false;
            }

			if(context.getBusinessData(COLSUMMARY_CONSTANT) == null){
			    
			    // 没有就塞进去， 其他地方会用到， 比如  WorkflowColExtendImpl.transDoSomethingAffterProcessFinish
			    context.setBusinessData(COLSUMMARY_CONSTANT, colSummary);
			}
			
			CtpAffair businessAffair = (CtpAffair)context.getBusinessData(CTPAFFAIR_CONSTANT);
            if(businessAffair != null){
                // 目前是为超级节点准备的
                businessAffair.setFinish(true);
            }
            
            Integer operationType = (Integer) (context.getBusinessData().get(OPERATION_TYPE)==null ? -1:context.getBusinessData().get(OPERATION_TYPE));
            CollaborationEnum.flowState summaryState = WorkFlowEventListener.STETSTOP.equals(operationType) ? CollaborationEnum.flowState.terminate : CollaborationEnum.flowState.finish;
            colSummary.setState(summaryState.ordinal());
            setTime2Summary(colSummary);
            colManager.transSetFinishedFlag(colSummary);
            
            Map<String, Object> columns2 = new HashMap<String, Object>();
            columns2.put("summaryState", colSummary.getState());
            columns2.put("finish", true);
            affairManager.update(columns2, new Object[][] {{"objectId",colSummary.getId()}});
            
            User user = AppContext.getCurrentUser();
            
            /****************************************归档开始*****************************************/
            if(Strings.isNotBlank(colSummary.getAdvancePigeonhole()) && AppContext.hasPlugin("doc")){
            	JSONObject jo;
    			try {
    				jo = new JSONObject(colSummary.getAdvancePigeonhole());
    				String archiveFolder = jo.optString(ColConstant.COL_ARCHIVEFIELDID,"");//表单控件字段
    				String archiveFieldValue = jo.optString(ColConstant.COL_ARCHIVEFIELDVALUE, "");
    				String isCereateNew = jo.optString(ColConstant.COL_ISCEREATENEW,"");
    				String archiveText = jo.optString("archiveText", "");
    				String archiveAll = jo.optString("archiveAll", "");
    				boolean isCreateFloder = "true".equals(isCereateNew);
    				CtpAffair affair = affairManager.getSenderAffair(colSummary.getId());
    				if(affair==null){
    					affair = (CtpAffair) context.getBusinessData(CTPAFFAIR_CONSTANT);
    				}
    				Long archievId  = colSummary.getArchiveId();
    				
    				String StrArchiveFolder =  "";
    				Long realFolderId = archievId;
    				/****************************************表单归档文件夾移動开始*****************************************/
    				if(Strings.isNotBlank(archiveFolder)){
    					log.info("archiveFolder="+archiveFolder);
    				    try {
    				        StrArchiveFolder = capFormManager.getMasterFieldValue(colSummary.getFormAppid(),colSummary.getFormRecordid(),archiveFolder,true).toString();
                        } catch (SQLException e) {
                            log.error("",e);
                        }
    				    log.info("StrArchiveFolder="+StrArchiveFolder);
    				    if(Strings.isNotBlank(StrArchiveFolder)){
    				    	realFolderId = docApi.getPigeonholeFolder(archievId, StrArchiveFolder, isCreateFloder);//真实归档的路径
    				    	if(realFolderId == null){
                                log.warn("表单高级归档，没有勾选表单不存在时自动创建目录, 需要创建的目录 : " + StrArchiveFolder);
                                StrArchiveFolder = null;
                            }
    				    }
    				    
    				    if(Strings.isBlank(StrArchiveFolder)){
    				        StrArchiveFolder = "Temp";
    				    	//归档到Temp下面
    				    	realFolderId = docApi.getPigeonholeFolder(archievId, StrArchiveFolder, true);//真实归档的路径
    				    }
    				    if(null==realFolderId){
    				    	log.error("归档路径为null,导致归档不成功!!!");
    				    }
    				    //历史数据中设置的是否可以归档全部。历史数据没有archiveAll。
    				    boolean historyCanArchiveAll = Strings.isBlank(archiveAll) && !"true".equals(archiveText);
    				    boolean canArchiveAll = "true".equals(archiveAll) || historyCanArchiveAll;
    				    //不一样时才移动
    				    if(!StrArchiveFolder.equals(archiveFieldValue) && canArchiveAll && null!=realFolderId){
                            docApi.updatePigehole(Long.parseLong(context.getCurrentUserId()), affair.getId(), ApplicationCategoryEnum.form.key());
                            docApi.moveWithoutAcl(Long.parseLong(context.getCurrentUserId()), affair.getId(), realFolderId);
                            
                            //同步归档路径
                            jo.put(ColConstant.COL_ARCHIVEFIELDVALUE, StrArchiveFolder);
                            colSummary.setAdvancePigeonhole(jo.toString());
                            ColUtil.addOneReplyCounts(colSummary);
                            if(!fromOuter){
                            	colManager.updateColSummary(colSummary);
                            }
    				    }
    				}
    				log.info("realFolderId="+realFolderId);
    				/****************************************表单套红正文归档开始*****************************************/
    				if("true".equals(archiveText)){
    					String archiveTextName = jo.optString("archiveTextName", "");
    					String multipleArchiveTextName = jo.optString("multipleArchiveTextName", "");
    					String archiveKeyword = jo.optString("archiveKeyword", "");
    					taoHongContentPigeonhole(colSummary, archiveTextName, multipleArchiveTextName, archiveKeyword, user, realFolderId);
    				}
    				/****************************************表单套红正文归档结束*****************************************/
    				/****************************************表单归档文件夾移動結束*****************************************/   
    				    
    				
    			} catch (JSONException e) {
    				log.error("",e);
    			}
            }
            /****************************************附件归档开始*****************************************/
            //附件归档,流程结束更新
            if(colSummary.getAttachmentArchiveId() != null &&  AppContext.hasPlugin("doc")){
            	
            	List<Attachment> attachment = attachmentManager.getByReference(colSummary.getId(),colSummary.getId());//标题区附件
            	List<Attachment> formAttachments = capFormManager.getAllFormAttsByModuleId(colSummary.getId(),colSummary.getFormAppid(),colSummary.getFormRecordid());//表单控件里面的附件,不包含套红正文的附件
            	
    			if(Strings.isNotEmpty(formAttachments)){//归档表单控件中的附件
    				for(Attachment _formAtt : formAttachments){
    				    
    				    if(_formAtt.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()){
        				    V3XFile file = fileManager.getV3XFile(_formAtt.getFileUrl());
        					docApi.attachmentPigeonhole(file, colSummary.getAttachmentArchiveId(), user.getId(),
        							user.getLoginAccount(), false, "", PigeonholeType.edoc_account.ordinal());
    				    }
    				}
    			}
    			if(Strings.isNotEmpty(attachment)){//归档标题区附件
    				for(Attachment _Att : attachment){
    				    if(_Att.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()){
    				        V3XFile file = fileManager.getV3XFile(_Att.getFileUrl());
    				        docApi.attachmentPigeonhole(file, colSummary.getAttachmentArchiveId(), user.getId(), user.getLoginAccount(), true, "", PigeonholeType.edoc_account.ordinal());
    				    }
    				}
    			}
            }
            
            /****************************************附件归档结束*****************************************/
            //更新督办状态为已办结
            superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised,colSummary.getId(),SuperviseEnum.EntityType.summary);
            
            //清空消息跟踪设置表中的数据, 最后一个节点清空了数据后， 其他跟踪人收不到消息
            //trackManager.deleteTrackMembers(colSummary.getId());
            
            //TODO 更新表单动态表状态为已办结
            String mainCaseId = context.getMainCaseId();
            List<Long> nodeIds = context.getMainNextNodeIds();
            //发送消息 如果是子流程且主流程受约束，则给下节点待办发消息可处理。
            if(Strings.isNotBlank(mainCaseId) && Strings.isDigits(mainCaseId)){
                this.colMessageManager.sendNextPendingNodeMessage(Long.parseLong(mainCaseId), nodeIds);
            }
            if(!WorkFlowEventListener.STETSTOP.equals(operationType)){
	            //流程正常结束通知
                CollaborationFinishEvent finishEvent = new CollaborationFinishEvent(this);
                finishEvent.setSummaryId(colSummary.getId());
                CtpAffair affair = (CtpAffair)context.getBusinessData(CTPAFFAIR_CONSTANT);
                if(affair!=null){
                    log.info("流程结束触发表单高级事件：affair.getsubject="+affair.getSubject()+";"+affair.getId()+";summaryID="+colSummary.getId());
                    finishEvent.setAffairId(affair.getId());
                    finishEvent.setBodyType(affair.getBodyType());
                }
                
                if(Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(colSummary.getNewflowType())){
                   if(Strings.isNotBlank(colSummary.getProcessId())){
                       Long mainProcessId = wapi.getMainProcessIdBySubProcessId(Long.valueOf(colSummary.getProcessId()));
                       finishEvent.setMainProcessId(mainProcessId);
                   }
                }
                finishEvent.setAffair(affair);
                finishEvent.setSummary(colSummary);
                
                EventDispatcher.fireEventAfterCommit(finishEvent);
                
            }
        } catch (NumberFormatException e) {
           log.error("", e);
        } catch (BusinessException e) {
        	log.error("", e);
        }

        return true;
    }

    /**
     * 流程终止
     */
	public boolean onWorkitemStoped(EventDataContext context) {
		WorkItem workitem = (WorkItem) context.getWorkItem();
		CtpAffair affair = (CtpAffair)context.getBusinessData(CTPAFFAIR_CONSTANT);
		try {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ColSummary colSummary = (ColSummary)context.getAppObject();
			if(colSummary == null){
			    colSummary = colManager.getColSummaryByProcessId(Long.valueOf(context.getProcessId()));   
			}
			colSummary.setState(CollaborationEnum.flowState.terminate.ordinal());
			colSummary.setFinishDate(now);
			colManager.updateColSummary(colSummary);
			if (affair == null) {
				affair = affairManager.getAffairBySubObjectId(Long
						.valueOf(workitem.getId()));
			}
			//终止时不给待发送事项发消息
            List<CtpAffair> trackingAndPendingAffairs = affairManager.getTrackAndPendingAffairs(
                            affair.getObjectId(), affair.getApp());
            // 指定回退
            List<CtpAffair> pendingAffairs = affairManager.getAffairs(colSummary.getId());
            boolean isInspecialBack=false;
            if (!CollectionUtils.isEmpty(pendingAffairs)) {
                for (CtpAffair ctpAffair : pendingAffairs) {
                    if (ctpAffair.getSubState() == SubStateEnum.col_pending_specialBack.getKey()
                            || ctpAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
                            || ctpAffair.getSubState() == SubStateEnum.col_pending_specialBackCenter.getKey()) {
                        trackingAndPendingAffairs.add(ctpAffair);
                        isInspecialBack = true;
                    }
                }
            }
            if(isInspecialBack){
            	CtpAffair _senderAffair = affairManager.getSenderAffair(colSummary.getId());
            	if(_senderAffair != null && _senderAffair.getTrack().intValue() != TrackEnum.no.ordinal()){
            		_senderAffair.setTrack(TrackEnum.no.ordinal());
            		affairManager.updateAffair(_senderAffair);
            	}
            }
            //流程终止时发送消息
            Object businessData = context.getBusinessData("isAutoStop");
            //设置了超期自动终止的在workflowProcess*ColHanlder里面发了 消息 这里就不需要在发送消息了
            if(!(null != businessData && "1".equals((String)businessData))){
                MessageCommentParam mc = (MessageCommentParam)context.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_MESSAGE);
                List<Long[]> pushMsgMemberList = (List<Long[]>) context.getBusinessData("pushMsgMemberList");
                
            	this.colMessageManager.terminateCancel(workitem, affair, trackingAndPendingAffairs,mc,pushMsgMemberList);
            }
            
            affair.setSummaryState(colSummary.getState());
            ColUtil.setTime2Affair(affair,colSummary);
			Map<String, Object> columns = new HashMap<String, Object>();
			columns.put("state", StateEnum.col_done.key());
			columns.put("subState", SubStateEnum.col_done_stepStop.key());
			columns.put("completeTime", now);
			columns.put("updateDate", now);
			columns.put("finish", true);
			columns.put("overWorktime", affair.getOverWorktime());
			columns.put("runWorktime", affair.getRunWorktime());
			columns.put("overTime", affair.getOverTime());
			columns.put("runTime", affair.getRunTime());
			columns.put("coverTime", affair.isCoverTime());
			 //判断代理人
            if(!affair.getMemberId().equals(AppContext.getCurrentUser().getId())){
            	columns.put("transactorId", AppContext.getCurrentUser().getId());
            }

			List<CtpAffair> affairsByObjectIdAndState = affairManager.getAffairs(affair.getObjectId(), StateEnum.col_pending);
			// 根据app判断，避免终止时更新待发送事项的状态
			//affairManager.update(columns,new Object[][] {{"objectId", affair.getObjectId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
			
			
			
			Map<String,Map<String,Object>> valueMap = new HashMap<String,Map<String,Object>>();
			Map<String,List<Long>> whereMap = new HashMap<String,List<Long>>();
			
			for(CtpAffair _m : affairsByObjectIdAndState){
				
				String reTime = _m.getReceiveTime() == null ? ""  : _m.getReceiveTime().toString();
				String expectTime = _m.getExpectedProcessTime() == null ? "" : _m.getExpectedProcessTime().toString();
				String key = reTime + expectTime;
				
				if(valueMap.get(key) == null){
					_m.setSummaryState(colSummary.getState());
					ColUtil.setTime2Affair(_m, colSummary);
					Map<String,Object> v = new HashMap<String,Object>();
					v.put("overWorktime", _m.getOverWorktime());
					v.put("runWorktime", _m.getRunWorktime());
					v.put("overTime", _m.getOverTime());
					v.put("runTime", _m.getRunTime());
					v.put("summaryState", _m.getSummaryState());
					v.put("coverTime", _m.isCoverTime());
					
					valueMap.put(key,v);
				}
				
				List<Long> ids = whereMap.get(key);
				if(ids == null){
					ids = new ArrayList<Long>();
				}
				ids.add(_m.getId());
				whereMap.put(key, ids);
			}
			
			Set<String> s = whereMap.keySet();
			if(Strings.isNotEmpty(s)){
				for(String key : s){
					Map<String,Object> v = valueMap.get(key);
					columns.put("overWorktime", v.get("overWorktime"));
					columns.put("runWorktime", v.get("runWorktime"));
					columns.put("overTime", v.get("overTime"));
					columns.put("runTime", v.get("runTime"));
					columns.put("summaryState", v.get("summaryState"));
					columns.put("coverTime", v.get("coverTime"));
					
					List<Long> affairIds = whereMap.get(key);
					
					if(Strings.isNotEmpty(affairIds)){
						List<Long>[] affairIdsArray = Strings.splitList(affairIds, 900);
						for(List<Long> ids : affairIdsArray){
							affairManager.update(columns, new Object[][] {{"id",ids}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
						}
						//设置主动终止
						Map<String, Object> column = new HashMap<String, Object>();
						column.put("subState", SubStateEnum.col_done_activeStepStop.key());
						affairManager.update(affair.getId(), column);
					}
				}
			}
			
			/*for(CtpAffair ctpAffairM : affairsByObjectIdAndState){
				if(null == ctpAffairM.getDeadlineDate()){
					columns.put("overWorktime",0L);
					affairManager.update(columns, new Object[][] {{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
				}
				if(null != ctpAffairM.getDeadlineDate()){
					if(affair.getDeadlineDate() != null && affair.getDeadlineDate().equals(ctpAffairM.getDeadlineDate())){
						columns.put("overWorktime",affair.getOverWorktime());
					}else {
						long ctpAffairMOverWorkTime = affair.getRunWorktime()-ctpAffairM.getDeadlineDate();
						columns.put("overWorktime",ctpAffairMOverWorkTime > 0 ? ctpAffairMOverWorkTime : 0L);
					}
					affairManager.update(columns, new Object[][] {{"id", ctpAffairM.getId()}, {"state", StateEnum.col_pending.key()},{"app", affair.getApp()}});
				}

			}
			*/
			

			//指定回退给发起人,在待发中
            Map<String, Object> columns2 = new HashMap<String, Object>();
            columns2.put("state", StateEnum.col_sent.key());
            columns2.put("finish", true);
            columns2.put("updateDate", now);
            columns2.put("summaryState", colSummary.getState());
            affairManager.update(columns2, new Object[][]{{"objectId", affair.getObjectId()}, {"state", StateEnum.col_waitSend.key()},{"app",affair.getApp()}});

            //删除节点超期后多次提醒定时任务
            ColUtil.deleteCycleRemindQuartzJob(affair, false);
            
		} catch (BusinessException e) {
		    log.error("更新affair事项出错  " + e.getMessage(), e);
		}

		return true;
	}
	
	@Override
	public boolean onWorkflowAssigned(List<EventDataContext> contextList) {
	    
	    boolean isNotEmptyContextList = Strings.isNotEmpty(contextList);
	    log.info(AppContext.currentUserName()+" in onWorkflowAssigned ,contextList.size:"+(isNotEmptyContextList ? contextList.size() : 0));
	    
		if (isNotEmptyContextList) {
		    @SuppressWarnings("unchecked")
            Map<String,Object> global = (Map<String,Object>)contextList.get(0).getBusinessData(WF_APP_GLOBAL);
		    if(global == null){
		        global = new HashMap<String,Object>();
		    }
		    Long processId= null;
		    String processIdStr= contextList.get(0).getProcessId();
		    Map<String,String> repeatMap = new HashMap<String,String>();
			AffairData affairData = (AffairData) contextList.get(0).getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
			// 非内容组件封装事件执行
			if (affairData == null && Strings.isNotBlank(processIdStr) && Strings.isDigits(processIdStr)){
				processId= Long.parseLong(processIdStr);
				try {
					ColSummary summary = colManager.getColSummaryByProcessId(processId);
					if(null!=summary){
						affairData= ColUtil.getAffairData(summary); 
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
			if (affairData == null) {
				return true;
			}
			Timestamp now = DateUtil.currentTimestamp();

			Boolean isCover = false;
			//回退生成
			ColSummary summary = null;
			Object summaryObj = contextList.get(0).getBusinessData(COLSUMMARY_CONSTANT);
			if (summaryObj != null) {
				summary = (ColSummary) summaryObj;
			}else{
				try {
					summary = colManager.getColSummaryById(affairData.getModuleId());
				} catch (Exception e) {
					log.error("", e);
				}
			}
			
			//自动发起的重新构建哈发消息的标题
			if(null != summary.getAutoRun() && summary.getAutoRun()){
				String subject = ResourceUtil.getString("collaboration.newflow.fire.subject",summary.getSubject());
				affairData.setSubject(subject);
			}else{
				affairData.setSubject(summary.getSubject());
			}
			
			CtpAffair currentAffair = null;
			Object currentAffairObj = contextList.get(0).getBusinessData(CTPAFFAIR_CONSTANT);
			if (currentAffairObj != null) {
				currentAffair = (CtpAffair) currentAffairObj;
			}
			
			isCover = summary.isCoverTime();

			// 控制是否发送协同发起消息
			Boolean isSendMessageContext = contextList.get(0).isSendMessage();
			Boolean isSendMessage = true;
			if (isSendMessageContext != null && !isSendMessageContext) {
				isSendMessage = false;
			}

			List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
			List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();

			
			Long currentMemberId = 0L;
			
			Object obj = contextList.get(0).getBusinessData(CURRENT_OPERATE_MEMBER_ID);
			if(obj != null){
				currentMemberId = (Long)obj;
			}else{
				currentMemberId = null != AppContext.getCurrentUser() ? AppContext.getCurrentUser().getId() : null;
			}
			
			
			
			Set<Long>  currentNodesInfoSet = new HashSet<Long>();
			
			Map<String,Object>  bugReportMap = new  HashMap<String,Object>();
			String bugReportInvalidMemberName = ""; //异常上报不可用的人员名字。
			
			try {
				//重复自动跳过的事项
				List<Long> isRepeatAutoSkipAffairIds = new ArrayList<Long>();
				//创建List存放affairid与skipAgentId的对应关系列表
				List<Map<String,String>> autoSkipArray = new ArrayList<Map<String,String>>();
                Set<Long> colDoneMemberIds = new HashSet<Long>();
                Set<Long> preDoneMemberIds = new HashSet<Long>();
              
                
                Map<String,String> cantAutoSkipReson = new HashMap<String,String>();
                List<CtpAffair> doneAffairList = new ArrayList<CtpAffair>();
                boolean processCanAnyDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,summary);
                boolean processCanPreDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,summary);
                boolean processCanStartMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,summary);
                boolean processCanMerge = processCanAnyDealMerge || processCanPreDealMerge || processCanStartMerge;
                boolean hasLoadDoneAffair = false;
                boolean hasLoadPreDealAffair = false;
				for (EventDataContext context : contextList) {
					List<WorkItem> workitems = context.getWorkitemLists();
					Long deadline = null;
					Long remindTime = null;
					int dealTermType = 0;
					long dealTermUserId = -1;
					Date nodeDeaLineRunTime = null;
					log.info("节点期限:"+context.getDealTerm());
					if (ColUtil.isNotBlank(context.getDealTerm())) {
						if(context.getDealTerm().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d$")){
							nodeDeaLineRunTime = DateUtil.parse(context.getDealTerm(), "yyyy-MM-dd HH:mm");
						}else if(context.getDealTerm().matches("^-?\\d+$")){
							log.info("计算节点期限入参：now="+now+"__deadline="+deadline);
							deadline = Long.parseLong(context.getDealTerm());
							if (null != deadline && 0 != deadline) {
								nodeDeaLineRunTime = workTimeManager.getCompleteDate4Nature(now, deadline, affairData.getSummaryAccountId());
							}
							log.info("转换后的节点期限："+nodeDeaLineRunTime);
						}else if(context.getDealTerm().startsWith("field")){//表单字段流程期限
			            	Map<String,Object> formData = (Map<String, Object>) context.getBusinessData("CTP_FORM_DATA");
			            	Object date = null;
			            	if(formData != null) {
			            	    date = formData.get(context.getDealTerm());
			            	    
	                            log.info("节点期限选择的表单字段:"+context.getDealTerm()+","+"fieldValue:"+date);
	                            
	                            if(date != null) {
	                                if(date instanceof Date){
	                                    nodeDeaLineRunTime = (Date)date;
	                                } else if(date instanceof String){
	                                    nodeDeaLineRunTime = Strings.isBlank((String)date) ? null : Datetimes.parse(date.toString());
	                                }
	                            }
			            	}
			            }
						if (ColUtil.isNotBlank(context.getDealTermType())) {
							dealTermType = Integer.parseInt(context
									.getDealTermType().trim());
						}
						if (ColUtil.isNotBlank(context.getDealTermUserId()) && ColUtil.isLong(context.getDealTermUserId())) {
							dealTermUserId = Long.parseLong(context
									.getDealTermUserId().trim());
						}
					}
					if (ColUtil.isNotBlank(context.getRemindTerm())) {
						remindTime = Long.parseLong(context.getRemindTerm()
								.trim());
					}

					List<CtpAffair> affairs = new ArrayList<CtpAffair>(workitems.size());
					Long activetyId = null;
					int affairCountOneNode = workitems.size();
					
					
					boolean isListPighole  =  String.valueOf(SubStateEnum.col_done_pighone.getKey()).equals(context.getBusinessData(WorkFlowEventListener.AFFAIR_SUB_STATE));
				    boolean isListDelete   =  String.valueOf(SubStateEnum.col_done_delete.getKey()).equals(context.getBusinessData(WorkFlowEventListener.AFFAIR_SUB_STATE));
				       
					
					// 设置事项为待办和协同待办未读
					//1.MemberId
					//2.加签
					//3.回退
					//4.是否要标志，WCW。
					boolean isCompetition = PROCESS_MODE_COMPETITION.equals(context.getProcessMode())  && affairCountOneNode > 1;
					boolean isSelectPeople = context.isSelectPeople();
					boolean isSystemAdd = context.isSystemAdd();
					boolean isOrderExecuteAdd = String.valueOf(ChangeType.OrderExecuteAdd.getKey()).equals(context.getAddedFromType());
					boolean isNeedAutoSkip  =  !(isListPighole || isListDelete); //这2种情况不重复跳过
					boolean isCanAutoDeal = false;
					
					boolean isBacked = false;
					//回退的情况下覆盖fromId，显示的时候通过subState来区分,设置回退的人员id
					int operationType = (Integer) ((Integer) context.getBusinessData().get(OPERATION_TYPE)==null ? 12:context.getBusinessData().get(OPERATION_TYPE));
					//设置回退人的id
					if(operationType==WITHDRAW||operationType==SPECIAL_BACK_RERUN){
						isBacked = true;
					}
					
					boolean isAddNode = ColUtil.isNotBlank(context.getAddedFromId());
					boolean isModifyWorkflowModel = context.isModifyWorkflowModel();

					if(!isModifyWorkflowModel ){
					    Object  isModifyWorkflowModelObj = context.getBusinessData().get(BackgroundDealParamBO.EXTPARAM_IS_MODIFYWORKFLOW_MODEL);
					    if(isModifyWorkflowModelObj != null){
                            isModifyWorkflowModel = (Boolean) isModifyWorkflowModelObj;
                        }
                    }
					//节点因为 什么条件符合合并处理
					String mergeDealType = "";
					//节点合并处理设置
					String nodeMergeDealType = context.getMergeDealType();
					boolean nodeMerge4Process =  true;
					if(Strings.isNotBlank(nodeMergeDealType)){
						nodeMerge4Process = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.PROCESS_MERGE.getValue());
					}
					//默认不合并处理
					boolean canAnyDealMerge = false;
	                boolean canPreDealMerge = false;
	                boolean canStartMerge = false;
	                boolean canMerge = false;
					
	                //按照流程设置匹配是否合并处理
	                if(nodeMerge4Process){
	                	canAnyDealMerge = processCanAnyDealMerge;
	                	canPreDealMerge = processCanPreDealMerge;
	                	canStartMerge = processCanStartMerge;
	                	canMerge = processCanMerge;
	                }else {//按照节点设置是否合并处理
	                	boolean noMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.NO_MERGE.getValue());
	                	if(!noMerge){
	                		canMerge = true;
	                		canStartMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue());	
	                		canPreDealMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue());	
	                		canAnyDealMerge = nodeMergeDealType.contains(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue());
	                	}
	                		
	                }
					
					
					//设置与前面任一节点合并时读取已办事项
					if(canAnyDealMerge && !hasLoadDoneAffair){
						//已办事项
		            	List<StateEnum> states = new ArrayList<StateEnum>();
		            	states.add(StateEnum.col_done);
		                doneAffairList = affairManager.getAffairs(summary.getId(),states);
		                for (CtpAffair ctpAffair : doneAffairList) {
		                    colDoneMemberIds.add(ctpAffair.getMemberId());
		                }
		                if(currentAffair!=null 
		                		&&(Integer.valueOf(StateEnum.col_pending.getKey()).equals(currentAffair.getState()) 
		                		||Integer.valueOf(StateEnum.col_pending_repeat_auto_deal.getKey()).equals(currentAffair.getState())
		                		|| Integer.valueOf(StateEnum.col_done.getKey()).equals(currentAffair.getState()) )){
		                	colDoneMemberIds.add(currentAffair.getMemberId());
		                }
		                hasLoadDoneAffair = true;
					}
					
					//设置了与上一步相同时合并处理并且没有读取数据时读取上一步处理人
					if(canPreDealMerge && !hasLoadPreDealAffair && currentAffair!=null ){
						//上一节点处理人的affair
						Long activityId = currentAffair.getActivityId();
						//前面已经查过的不再查询
						if(Strings.isNotEmpty(doneAffairList)){
							for(CtpAffair ctpAffair : doneAffairList){
								if(activityId.equals(ctpAffair.getActivityId())){
									preDoneMemberIds.add(ctpAffair.getMemberId());
								}
							}
						}else{//没查过重新查询一次
							List<Integer> states = new ArrayList<Integer>();
							states.add(StateEnum.col_done.getKey());
							List<CtpAffair> preAffairList = affairManager.getSimpleAffairsByObjectIdAndActivityId(summary.getId(), activityId,states);
							for (CtpAffair ctpAffair : preAffairList) {
								if(Integer.valueOf(StateEnum.col_done.getKey()).equals(ctpAffair.getState())){
									preDoneMemberIds.add(ctpAffair.getMemberId());
								}
							}
						}
						//过滤发起人
						if(null!=activityId){
							preDoneMemberIds.add(currentAffair.getMemberId());
						}
						hasLoadPreDealAffair = true;
					}
					
					for (WorkItem workitem : workitems) {
						Long memberId = Long.parseLong(workitem.getPerformer());
						if(!"zhihui".equals(context.getPolicyId())&&!"inform".equals(context.getPolicyId()) && currentNodesInfoSet.size() < 10){//知会 节点不算待办
								currentNodesInfoSet.add(memberId);
						}
						
						CtpAffair affair = new CtpAffair();
						affair.setPreApprover(currentMemberId); 
						affair.setIdIfNew();
						affair.setTrack(0);
						affair.setDelete(false);
						affair.setSubObjectId(Long.valueOf(workitem.getId()));
						affair.setMemberId(memberId);
						
						affair.setSenderId(affairData.getSender());
						affair.setSubject(summary.getSubject());
						affair.setAutoRun(summary.getAutoRun());
						
						affair.setMatchDepartmentId(workitem.getMatchDepartmentId());
						affair.setMatchPostId(workitem.getMatchPostId());
						affair.setMatchAccountId(workitem.getMatchOrgAccountId());
						affair.setMatchRoleId(workitem.getMatchRoleId());
						affair.setNodeName(context.getNodeName());
						//68121  & 68671
						String policyId = context.getPolicyId();
						if(policyId != null){policyId=policyId.replaceAll(new String(new char[]{(char)160}), " ");}
						affair.setNodePolicy(policyId);
						//设置加签、知会、会签的人员id
						affair.setFromId(ColUtil.isNotBlank(context.getAddedFromId()) ? Long.valueOf(context.getAddedFromId()) : null);
						affair.setFromType(ColUtil.isNotBlank(context.getAddedFromType()) ? Integer.valueOf(context.getAddedFromType()) : null);

						//设置回退人的id
						if(operationType==WITHDRAW||operationType==SPECIAL_BACK_RERUN){
							affair.setBackFromId(AppContext.getCurrentUser().getId());
						}
						AffairUtil.setHasAttachments(affair, affairData
								.getIsHasAttachment() == null ? false
								: affairData.getIsHasAttachment());
						AffairUtil.setFormReadonly(affair,
								"1".equals(context.getfR()));
						
                        //定义一个代理id，代表此memberId的协同代理id
                        Long agentId = null;
                        boolean isAgentSkip = false;
                        if (canMerge) {// 如果有合并处理，根据下一个执行人获取其对该协同的代理人id
                           // agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.getKey(), memberId, summary.getTempleteId());
                        }
                        Long proxyMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.getKey(), memberId, summary.getTempleteId(), affair.getSenderId());
                        affair.setProxyMemberId(proxyMemberId);

                        isCanAutoDeal = false;
                        boolean  isSubProcessSkipFSender = context.isSubProcessSkipFSender();
                        if(context.isSystemAdd()){//子流程第一个节点是否能合并处理
                            if(isSubProcessSkipFSender){
                                isCanAutoDeal = true;
                                mergeDealType = BPMSeeyonPolicySetting.MergeDealType.SUB_PROCESS_FSENDER.getValue();
                            }
                        }
                        //与发起人相同时合并处理
                        if(!isCanAutoDeal && canStartMerge){
                        	Long senderId = affairData.getSender();
                        	if ((senderId.equals(memberId) || senderId.equals(agentId))){
                        		isAgentSkip = currentMemberId.equals(agentId);
                        		isCanAutoDeal = true;
                        		mergeDealType = BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue();
                        	}
                        }
                        StringBuilder sb = new StringBuilder();
                        //上一节点处理过合并处理
                        if (!isCanAutoDeal && canPreDealMerge && !preDoneMemberIds.isEmpty()) {
                        	isCanAutoDeal = preDoneMemberIds.contains(memberId);
                            if (!isCanAutoDeal && agentId != null) {//如果不能自动跳过，判断是否有代理
                                // 如果有代理，判断代理是否在前面处理人中
                                isCanAutoDeal = preDoneMemberIds.contains(agentId);
                                if (isCanAutoDeal) {// 如果因为前面有代理处理过而合并，设置标记为true
                                    isAgentSkip = true;
                                }
                            }
                            if(isCanAutoDeal){
                            	mergeDealType = BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue();
                            }
                            sb.append("___________________preDoneMemberIds______contains:" + isCanAutoDeal);
                        }
                        //判定当前处理人员是否与在前面节点是否能查询到(前面已经处理过时合并处理)
                        if (!isCanAutoDeal && canAnyDealMerge && !colDoneMemberIds.isEmpty()) {
                        	isCanAutoDeal = colDoneMemberIds.contains(memberId);
                            if (!isCanAutoDeal && agentId != null) {// 如果不能自动跳过，判断是否有代理
                                // 如果有代理，判断代理是否在前面处理人中
                                isCanAutoDeal = colDoneMemberIds.contains(agentId);
                                if (isCanAutoDeal) {// 如果因为前面有代理处理过而合并，设置标记为true
                                    isAgentSkip = true;
                                }
                            }
                            if(isCanAutoDeal){
                            	mergeDealType = BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue();
                            }
                            sb.append("___________________colDoneMemberIds______contains:" + isCanAutoDeal);
                        }

                        if (!isCanAutoDeal && canMerge) {
                                sb.append("isCanAutoDeal：").append(isCanAutoDeal).append("\r\n");
                                sb.append("affairId:").append(affair.getId()).append("\r\n");
                                sb.append("PARAMETER:").append("\r\n");
                                sb.append("isAddNode:").append(isAddNode).append("\r\n");
                                sb.append("isSelectPeople:").append(isSelectPeople).append("\r\n");
                                sb.append("isSystemAdd:").append(isSystemAdd).append("\r\n");
                                sb.append("isBacked:").append(isBacked).append("\r\n");
                                sb.append("isCompetition:").append(isCompetition).append("\r\n");
                                sb.append("!isNeedAutoSkip:").append(!isNeedAutoSkip).append("\r\n");
                                sb.append("processAnyDealMerge:").append(processCanAnyDealMerge).append("\r\n");
                                sb.append("processPreDealMerge:").append(processCanPreDealMerge).append("\r\n");
                                sb.append("processMergeStart:").append(processCanStartMerge).append("\r\n");
                                sb.append("nodeAnyDealMerge:").append(canAnyDealMerge).append("\r\n");
                                sb.append("nodePreDealMerge:").append(canPreDealMerge).append("\r\n");
                                sb.append("nodeMergeStart:").append(canStartMerge).append("\r\n");
                                sb.append("isModifyWorkflowModel:").append(isModifyWorkflowModel).append("\r\n");
                                sb.append("isOrderExecuteAdd:").append(isOrderExecuteAdd).append("\r\n");
                                log.info(sb.toString());
                        }
                        if (canMerge || isCanAutoDeal) {
                        	String affairIdStr = String.valueOf(affair.getId());
                            if (isCompetition) {
                                cantAutoSkipReson.put(affairIdStr, "isCompetition");
                            }
                            else if (isSelectPeople) {
                                cantAutoSkipReson.put(affairIdStr, "isSelectPeople");
                            }
                            else if (isSystemAdd && !isSubProcessSkipFSender) {
                                cantAutoSkipReson.put(affairIdStr, "isSystemAdd");
                            }
                            else if (isBacked) {
                                cantAutoSkipReson.put(affairIdStr, "isBacked");
                            }
                            else if (isAddNode) {
                                cantAutoSkipReson.put(affairIdStr, "isAddNode");
                            }
                            else if (!isNeedAutoSkip) {
                                cantAutoSkipReson.put(affairIdStr, "isNeedAutoSkip");
                            }
                            else if (isModifyWorkflowModel) {
                                cantAutoSkipReson.put(affairIdStr, "isModifyWorkflowModel");
                            }else if(isOrderExecuteAdd){
                            	cantAutoSkipReson.put(affairIdStr, "isOrderExecuteAdd");
                            }

                            if (isCanAutoDeal) {
                                // 创建jsonObject存放affairId 与skipAgentId的对应关系
                                isRepeatAutoSkipAffairIds.add(affair.getId());
                                Map<String,String> skipMap = new HashMap<String,String>();
                                skipMap.put("affairId", affair.getId().toString());
                                if (isAgentSkip) {
                                    // 因代理处理而进行自动跳过
                                    skipMap.put("skipAgentId", agentId.toString());
                                }
                                else {// 非代理跳过，存入0
                                    skipMap.put("skipAgentId", "0");
                                }
                                skipMap.put("mergeDealType", mergeDealType);
                                Long currentCommentId = 0l;
                                Object commentObj = contextList.get(0).getBusinessData(CURRENT_OPERATE_COMMENT_ID);
                                // 与前面节点重复处理的时候在定时任务中取意见，不在这个地方取意见。
                                if (commentObj != null && BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue().equals(mergeDealType)) {
                                    currentCommentId = (Long) commentObj;
                                }
                                skipMap.put("_commentId", String.valueOf(currentCommentId));
                                // 将对应关系存入jsonArray
                                autoSkipArray.add(skipMap);
                            }
                        }
						
						affair.setState( isCanAutoDeal ? StateEnum.col_pending_repeat_auto_deal.getKey() : affairData.getState());
						affair.setSubState(isBacked ? SubStateEnum.col_pending_Back.key() : SubStateEnum.col_pending_unRead.key());
						
						// 协同的ID
						affair.setObjectId(affairData.getModuleId());
						affair.setDeadlineDate(deadline);
						
						affair.setFormRelativeQueryIds(context.getQueryIds());
						affair.setFormRelativeStaticIds(context.getStatisticsIds());
						
						String dr = context.getDR();
						affair.setRelationDataId(Strings.isBlank(dr) ? null : Long.valueOf(dr));
						
						
						affair.setDealTermType(dealTermType);
						affair.setDealTermUserid(dealTermUserId);
						affair.setRemindDate(remindTime);
						affair.setReceiveTime(now);
						affair.setUpdateDate(now);
						affair.setApp(affairData.getModuleType());
						if (Strings.equals(affair.getApp(),ApplicationCategoryEnum.collaboration.key())) {
							affair.setSubApp(affairData.getTemplateId() == null ? ApplicationSubCategoryEnum.collaboration_self.key()
									: ApplicationSubCategoryEnum.collaboration_tempate.key());
						}
						affair.setCreateDate(affairData.getCreateDate() == null ? now : affairData.getCreateDate());
						affair.setExpectedProcessTime(nodeDeaLineRunTime);
						affair.setTempleteId(affairData.getTemplateId());
						affair.setImportantLevel(affairData.getImportantLevel());
						affair.setResentTime(affairData.getResentTime());
						affair.setForwardMember(affairData.getForwardMember());
						affair.setBodyType(affairData.getContentType());
						activetyId = Long.parseLong(workitem.getActivityId());
						affair.setActivityId(activetyId);
						affair.setProcessId(workitem.getProcessId());
						affair.setCaseId(workitem.getCaseId());
						affair.setOrgAccountId(affairData.getOrgAccountId());
						
						if (ColUtil.isForm(affairData.getContentType())) {
							if (ColUtil.isNotBlank(context.getFormApp())) {
								affair.setFormAppId(Long.valueOf(context.getFormApp()));
							}
							
							affair.setFormRecordid(summary.getFormRecordid());
							
							//设置视图信息
							affair.setMultiViewStr(context.getFormViewOperation());
						}
						// 三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
						affair.setFinish(false);
						affair.setCoverTime(false);
						affair.setDueRemind(false);
						// 设置流程期限
						if (affairData.getProcessDeadlineDatetime() != null) {
							AffairUtil.addExtProperty(affair,
									AffairExtPropEnums.processPeriod,
									affairData.getProcessDeadlineDatetime());
						}
						affair.setSummaryState(summary.getState());
						affair.setProcessDeadlineTime(summary.getDeadlineDatetime());
						
						String messageRuleId = context.getMessageRule();
						affair.setMessageRuleId(messageRuleId);
						affairs.add(affair);

						if (isSendMessage && !isCanAutoDeal) {
							colMessageManager.getReceiver(affair, affair.getApp(),receivers, receivers1);
						}
						
						V3xOrgMember member = orgManager.getMemberById(affair.getMemberId());
					    if(member != null && !member.isValid()) {
					    	if(Strings.isBlank(bugReportInvalidMemberName)) {
					    		bugReportInvalidMemberName = member.getName();
					    	}else {
					    		bugReportInvalidMemberName +=","+member.getName();
					    	}
					    	if(bugReportMap.isEmpty()) {
					    		
					    		bugReportMap.put("appName", "collaboration");
					    		bugReportMap.put("processId", affair.getProcessId());
					    		bugReportMap.put("caseId", affair.getCaseId() == null ? "" : String.valueOf(affair.getCaseId()));
					    		bugReportMap.put("workitemId", affair.getSubObjectId() == null ? "" : String.valueOf(affair.getSubObjectId()));
					    		bugReportMap.put("bugType", String.valueOf(WorkflowBugTypeEnum.SUBSEQUENT_NODES_UNAVAILABLE.getKey()));
					    	}
					    	//在affair中设置异常处理的标志
					    	AffairUtil.setBugReport(affair, true);
					    }
						
					}


					// 控制是否发送消息
					affairData.setIsSendMessage(isSendMessage);

					if (affairData.getAffairList() != null) {
						affairData.getAffairList().addAll(affairs);
					} else {
						affairData.setAffairList(affairs);
					}

					// *******************定时任务计算*******************
					CtpAffair affair = affairs.get(0);
					Date advanceRemindTime = null;
					if (remindTime != null
							&& !Long.valueOf(-1).equals(remindTime) && affair.getExpectedProcessTime()!=null) {
						advanceRemindTime = workTimeManager.getRemindDate(
								nodeDeaLineRunTime, remindTime);
					}
					// 提前提醒，超期提醒
					ColUtil.affairExcuteRemind4Node(affair,
							affairData.getSummaryAccountId(),
							nodeDeaLineRunTime, advanceRemindTime);
					// *******************定时任务计算*******************
				}

				contextList.get(0).setBusinessData(ASSIGNED_AFFAIRS, affairData.getAffairList());
				affairData.setCurrentMemberId(currentMemberId);
				saveListMap(affairData, now, isCover, receivers, receivers1);

				if(Strings.isNotBlank(bugReportInvalidMemberName)) {
					bugReportMap.put("bugDesc", ResourceUtil.getString("workflow.bugreport.node.invalid.alert.js",bugReportInvalidMemberName));
					bugReportMap.put("affair", affairData.getAffairList().get(0));
					bugReportMap.put("bugMemberId",currentMemberId);
					saveBugReportByNewThread(bugReportMap);
				}
				
				//创建重复处理定时任务
				if(Strings.isNotEmpty(isRepeatAutoSkipAffairIds)){
					String policyName = "";
					if(currentAffair!=null){
						policyName = ColUtil.getPolicyByAffair(currentAffair).getName();
					}
					String skipJsonString = JSONUtil.toJSONString(autoSkipArray);
					
					repeatMap.put("_policyName", policyName);
					repeatMap.put("count", "0");
					repeatMap.put("_isAffairAutotSkip", "1");
					repeatMap.put("skipJsonString", skipJsonString);//将jsonArray转换为字符串传递
					repeatMap.put("_cantSkipResonMap",JSONUtil.toJSONString(cantAutoSkipReson));
					
					global.put(WF_APP_GLOBAL_REPEAT_AFFMAP, repeatMap);
				}
				
				if(summaryObj==null){//工作流修改流程增加的节点，需要单独更新
					ColUtil.updateCurrentNodesInfo(summary);
					colManager.updateColSummary(summary);
				}
				
				CollaborationAffairsAssignedEvent affairsAssigned = new CollaborationAffairsAssignedEvent(this);
				affairsAssigned.setAffairs(affairData.getAffairList());
				affairsAssigned.setSummaryId(summary.getId());
				affairsAssigned.setCurrentAffair(currentAffair);
				EventDispatcher.fireEventAfterCommit(affairsAssigned);
			
			} catch (Exception e) {
				log.error(BPMException.EXCEPTION_CODE_DATA_FORMAT_ERROR, e);
				throw new RuntimeException(e);
			}
			
			//发送自动跳过的事件
			ColSelfUtil.fireAutoSkipEvent(this, (Map<String, String>) global.get(WorkFlowEventListener.WF_APP_GLOBAL_REPEAT_AFFMAP));
		}
		

		return true;
	 }
	
	private void saveBugReportByNewThread(final Map<String, Object> bugReportMap) {

		Thread t = new Thread(new Runnable() {
			public void run() {
				
				Map<String,Object> params = new HashMap<String, Object>();
				params.putAll(bugReportMap);
				try {
					wapi.saveWorkflowBugReport(params);
				} catch (BPMException e) {
					log.error("",e);
				}
			}
		});
		t.start();

	}



	/**
	 * 回退 工作流回调方法
	 */
	@Override
    public boolean onWorkitemCanceled(EventDataContext context) {

		Long summaryId = null;
		Object reMeToReGoOperationType = context.getBusinessData().get("_ReMeToReGo_operationType");
		boolean isReToRego = false;
		if(reMeToReGoOperationType != null){
			isReToRego = true;
		}
		
	    Integer type =  (Integer)context.getBusinessData().get(OPERATION_TYPE);
        int operationType = (type == null ? 12 : type);
    	WorkItem workitem =  context.getWorkItem();
    	if(workitem == null){
        	operationType = 12;
    	}else{
	        if((operationType == 9 || operationType == 11) && !PROCESS_MODE_COMPETITION.equals(context.getProcessMode())){
	        	operationType = 12;
	        }
    	}
    	
    	if(isReToRego){
    		operationType = (Integer)reMeToReGoOperationType;
    	}
    	//log.info("工作流回调，事项被取消onWorkitemCanceled，operationType："+operationType+",getProcessMode:"+context.getProcessMode()+",workitem.id:"+workitem.getId());
    	Timestamp now = new Timestamp(System.currentTimeMillis());
    	List<String> normalStepBackTargetNodes = context.getNormalStepBackTargetNodes();
    	try{
    	    //指定回退：流程重走 | 取回 | 回退
    		if(operationType == TAKE_BACK || operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN){
    			List<WorkItem> workItems = context.getWorkitemLists();
    			log.info("[onWorkitemCanceled]进入分支1,workItems.size():"+workItems.size());
    			if(Strings.isNotEmpty(workItems)){
    			    CtpAffair affair = (CtpAffair)context.getBusinessData(CTPAFFAIR_CONSTANT);
    				if(affair == null){
    				    affair = affairManager.getAffairBySubObjectId(workItems.get(0).getId());
    				}
    				if (affair == null){
    					log.info("====affair is null========workItems.get(0).getId():"+workItems.get(0).getId());
    				    return false;
    				}
    				summaryId = affair.getObjectId();
    				int maxCommitNumber = 300;
    				int length = workItems.size();
    				List<Long> workitemIds = new ArrayList<Long>();
    				List<Long> cancelAffairIds4Doc = new ArrayList<Long>(); //需要取消归档的
    				List<CtpAffair> cancelAffairs = new ArrayList<CtpAffair>();
    				int i = 0;
    				int state = operationType == TAKE_BACK ? StateEnum.col_takeBack.key() : StateEnum.col_stepBack.key();
    				List<CtpAffair>  affairs = (List<CtpAffair>)context.getBusinessData(WF_ALL_VALID_AFFAIRS);
    				if(Strings.isEmpty(affairs)){
    				    affairs =  affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, affair.getObjectId());
    				}
    				Map<Long,CtpAffair> m = new HashMap<Long,CtpAffair>();
    				for(CtpAffair af : affairs){
    					if(af.getSubObjectId()!=null){
    						m.put(af.getSubObjectId(), af);
    					}
    				}
    				Object  currentNodeId  = context.getBusinessData().get(CURRENT_OPERATE_AFFAIR_ID);
    				List<Long> traceList = new ArrayList<Long>();//流程追溯消息使用
    				Map<Long,Long> canceledMemberIdToAffairId = new HashMap<Long,Long>();
    				Map<Long,Long[]> canceledMemberIdToAarray = new HashMap<Long,Long[]>();
    				for (WorkItem workItem0 : workItems) {
    					if(m.keySet().contains(workItem0.getId())) {
    						CtpAffair af = m.get(workItem0.getId());
    						
    						if(af.getArchiveId() != null){
    						    cancelAffairIds4Doc.add(af.getId());
    						}
    						
    						cancelAffairs.add(af);

    						//1.被回退不產生追溯数据
    						//2.待办节点不产生追溯数据
    						//3.暂存代办节点产生数据
    						//4.回退节点自己  && 已办节点产生追溯数据
    						boolean isDoneNode = Integer.valueOf(StateEnum.col_done.getKey()).equals(af.getState());
    						boolean isZCDBNode = Integer.valueOf(StateEnum.col_pending.key()).equals(af.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(af.getSubState());
    						boolean isBackNode = af.getId().equals((Long)currentNodeId);
    						if(isReToRego){
    							Object reMeToReGoStepBackAffair = context.getBusinessData().get("_ReMeToReGo_stepBackAffair");
    							if(reMeToReGoStepBackAffair != null){
    								isBackNode = af.getId().equals(((CtpAffair)reMeToReGoStepBackAffair).getId());
    							}
    						}
    						boolean isBackedNode = af.getActivityId() == null ? true : normalStepBackTargetNodes.contains(String.valueOf(af.getActivityId())) ||String.valueOf(af.getActivityId()).equals(context.getSelectTargetNodeId()) ;
    						
    						if((operationType == WITHDRAW || operationType == SPECIAL_BACK_RERUN)
    								&& "1".equals(context.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW))
    								&& !isBackedNode
    								&& (isDoneNode || isBackNode || isZCDBNode) ){

    							
    				            String isCircleBack = (String)context.getBusinessData("isCircleBack");
    				            WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.step_back_normal;
    				            if(operationType != WITHDRAW){
    				            	trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_normal;
    				            	if("1".equals(isCircleBack)){
    				            		trackType = WorkflowTraceEnums.workflowTrackType.circle_step_back_normal;
    				            	}
    				            }

    							traceList.add(af.getId());
    						}
    						
    						canceledMemberIdToAffairId.put(af.getMemberId(), af.getId());
    						Long[] arr = new Long[2];
    						arr[0] = af.getId() ;
    						arr[1] = Long.valueOf(af.getState());
    						
    						canceledMemberIdToAarray.put(af.getMemberId(), arr);
    					}
    					workitemIds.add((long)workItem0.getId());
    					i++;
    					if(i % maxCommitNumber == 0 || i == length){
    						
    						Map<String,Object> params=new HashMap<String, Object>();
    						params.put("state", state);
    						params.put("subState", SubStateEnum.col_normal.key());
    						params.put("updateDate", now);
                            params.put("backFromId",AppContext.getCurrentUser().getId());
    						
    						Object[][] wheres = new Object[][] {
								{"objectId",affair.getObjectId()},
								{"subObjectId",workitemIds}};
    						
    						affairManager.update(params, wheres);
    						
    						//打印日志
    						logCancelIds(workitemIds);
    						
    						workitemIds = new ArrayList<Long>();
    					}
    				}
    				context.setBusinessData(CANCELED_MIDTOAID_MAP, canceledMemberIdToAffairId);
                    context.setBusinessData(CANCELED_MIDTOARRAY_MAP, canceledMemberIdToAarray);
                    context.setBusinessData(CANCELED_AFFAIRS,cancelAffairs);

    				context.setBusinessData(WFTraceConstants.WFTRACE_AFFAIRIDS,traceList);//发消息用
    				context.setBusinessData(WFTraceConstants.WFTRACE_TYPE,WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey());
				    
    				if (!CollectionUtils.isEmpty(cancelAffairIds4Doc)) {
				    	if(docApi != null){
				    		Long userId = 0l;
				    		if(AppContext.getCurrentUser()!=null){
				    			userId = AppContext.getCurrentUser().getId();
				    		} 
				    		docApi.deleteDocResources(userId , cancelAffairIds4Doc);
				    	}
			        }
				    if(Strings.isNotEmpty(cancelAffairs)){
				    	ColUtil.deleteQuartzJobForNodes(cancelAffairs);
				    	// 删除中间流程的跟踪表数据 by zhengchao at 2019年5月29日10点09分
				    	List<Long> affIds = new ArrayList<Long>();
				    	for (CtpAffair aff : cancelAffairs) {
				    		affIds.add(aff.getId());
						}
				    	trackManager.deleteTrackMembersByAffairIds(affIds);
				    }
		    		
    				return false;
    			}
    		}
    		CtpAffair affair = eventData2ExistingAffair(context);
    		if (affair == null){
    			log.info("工作流回调，事项被取消onWorkitemCanceled，affair is null");
    			return false;
    		}
    		boolean executeSingleUpdate = true;
    		
    		
    		boolean isCompetition = PROCESS_MODE_COMPETITION.equals(context.getProcessMode())  || Strings.isNotBlank(context.getGoNextMsg());
    		boolean operationSo = (operationType == COMMONDISPOSAL 
        			|| operationType == ZCDB 
        			|| operationType == AUTOSKIP 
        			|| operationType == SPECIAL_BACK_SUBMITTO || Strings.isNotBlank(context.getGoNextMsg()));
    		

            StringBuilder logInfo = new StringBuilder();
            logInfo.append("记录删除竞争事项的日志：");
            logInfo.append("affair.id:").append(affair.getId());
            logInfo.append(",affair.getObjectId():").append(affair.getObjectId());
            logInfo.append(",operationType:").append(operationType);
            logInfo.append(",affair.getActivityId():").append(affair.getActivityId());
            logInfo.append(",subObjectId:").append(affair.getSubObjectId());
            logInfo.append(",workItemId:").append(workitem.getId());
    		
    		if(isCompetition && !operationSo){
    			log.info(logInfo);
    		}
    		//竞争执行
    		if(isCompetition &&  operationSo){
    			
				if(isCompetition){
				    context.getBusinessData().put("IsProcessCompetion", "1");
				}
    			log.info("___IN Competition:"+logInfo);
    			List<CtpAffair> affairs = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
    			
    			
    			if(!affairs.isEmpty()){
    				log.info("___IN Competition Not Empty:size:"+affairs.size());
    				StringBuilder hql=new StringBuilder();
    				hql.append("update CtpAffair set state=:state,subState=:subState,updateDate=:updateDate "
    						+ " where  objectId=:objectId and activityId=:activityId and app=:app  "
    						+ " AND subObjectId<>:subObjectId "
    						+ " AND state <> :notState ");
    				
    				Map<String,Object> params=new HashMap<String, Object>();
					params.put("state", StateEnum.col_competeOver.key());
					params.put("subState", SubStateEnum.col_normal.key());
					params.put("updateDate", new Date());
					params.put("app", affair.getApp());
					params.put("objectId", affair.getObjectId());
					params.put("app", affair.getApp());
					params.put("activityId", affair.getActivityId());
					params.put("subObjectId", workitem.getId());
					params.put("notState", StateEnum.col_done.key());
					
					
					affairManager.update(hql.toString(), params);

					//给在竞争执行中被取消的affair发送消息提醒，按比例执行的时候，已办的时候不发送被竞争掉的消息
					for(Iterator<CtpAffair> it = affairs.iterator() ; it.hasNext();) {
						CtpAffair a  =  it.next();
						if(Integer.valueOf(StateEnum.col_done.key()).equals(a.getState())) {
							it.remove();
						}
					}
                    colMessageManager.transCompetitionCancel(workitem, affairs,affair);
    			}
    			
    			return true;
    		}
    		else if(operationType == 8){
    			throw new UnsupportedOperationException(affair.getId() + ", " + affair.getSubject());
    		}
    		else if(operationType == 12){
    			//删除被替换的所有affair事项
    			List<CtpAffair> affairs = new ArrayList<CtpAffair>();
    			if(context.getWorkitemLists() != null){
    				affairs = this.superviseCancel(context.getWorkitemLists(),now);
    				executeSingleUpdate = false;
    			}

    			if(affairs.isEmpty()){
    				affairs.add(affair);
    			}
    			//2013-1-8给在督办中被删除的affair发送消息提醒
    			colMessageManager.superviseDelete(workitem, affairs);
    			
    			
    			ColSummary summary  = null;
    			Object appObject = context.getAppObject();
    			if(null != appObject){
    				summary = (ColSummary)appObject;
    			}
    			if(summary == null){
    				summary = colManager.getColSummaryById(affair.getObjectId());
    			}
				if(summary == null){
					return true;
				}
				
				String currentNodeInfo = summary.getCurrentNodesInfo();
				
				if(Strings.isNotBlank(currentNodeInfo) && Strings.isNotEmpty(affairs)){
					for(CtpAffair a : affairs){
						currentNodeInfo = currentNodeInfo.replace(String.valueOf(a.getMemberId()), "");
					}
					summary.setCurrentNodesInfo(currentNodeInfo);
					colManager.updateColSummary(summary);
				}
    		}

    		if(executeSingleUpdate){
    			/**Map<String,Object> parmas = new HashMap<String,Object>();
    			parmas.put("updateDate", now);
    			log.info("executeSingleUpdate: affairId:"+affair.getId());
    			affairManager.update(affair.getId(), parmas);*/
    		}
    		
    		if(Strings.isNotBlank(context.getGoNextMsg())){//当前节点中的待办人、流程的跟踪人收到消息：节点处理率达到模板设置要求，可继续流转
            	//TODO
            }
    	}catch(BusinessException e){
    		log.error("", e);
    	}
        return true;

	}

	private void logCancelIds(List<Long> workitemIds) {
		StringBuilder sbItems = new StringBuilder();
		if(Strings.isNotEmpty(workitemIds)){
			for(Long wid : workitemIds){
				if(sbItems.length() != 0){
					sbItems.append(",");
				}
				sbItems.append(wid);
			}
		}
		
		log.info("[onWorkitemCanceled],实际设置为Cancel状态的数据Id:"+sbItems);
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
				StringBuilder hql=new StringBuilder();
				hql.append("update CtpAffair as a set a.state=:state,a.subState=:subState,a.updateDate=:updateDate,a.delete=1 where a.subObjectId in (:subObjectIds)");
				Map<String,Object> params=new HashMap<String, Object>();
				params.put("state", StateEnum.col_itemRemoved.key());
				params.put("subState", SubStateEnum.col_normal.key());
				params.put("updateDate", now);
				params.put("subObjectIds", ids);
				DBAgent.bulkUpdate(hql.toString(), params);
				/*DBAgent.bulkUpdate("update " + CtpAffair.class.getName() + " set state=?,subState=?,updateDate=?,delete=1 where subObjectId in (?)", StateEnum.col_cancel.key(),SubStateEnum.col_normal.key(),now,ids);*/
				List<CtpAffair> affairs = affairManager.getByConditions(null,nameParameters);
				affair4Message.addAll(affairs);
				ids.clear();
			}
		}
		return affair4Message;
	}
    //通过workitemId得到affair
    protected CtpAffair eventData2ExistingAffair(EventDataContext eventData) throws BusinessException{
        WorkItem workitem =  eventData.getWorkItem();
       // int operationType = (Integer) ((Integer) eventData.getBusinessData().get(OPERATION_TYPE)==null ? -1:eventData.getBusinessData().get(OPERATION_TYPE));
        CtpAffair affair = null;
        if( null!=eventData.getBusinessData(CTPAFFAIR_CONSTANT) ){
            affair = (CtpAffair)eventData.getBusinessData(CTPAFFAIR_CONSTANT);
        }else{
            affair = affairManager.getAffairBySubObjectId(workitem.getId());
        }
       /* if(affair == null){
        	//TODO getLog().warn("不能通过workitem取到affair，workitem id："+workitem.getId());
        }*/
        /*switch(operationType){
        	case 1 :
        		affair.setState(StateEnum.col_stepBack.key());
            	affair.setSubState(SubStateEnum.col_normal.key());
            	break;
        	case 2 :
        		affair.setState(StateEnum.col_takeBack.key());
            	affair.setSubState(SubStateEnum.col_normal.key());
            	break;
        	case 10 :
        		affair.setState(StateEnum.col_cancel.key());
            	affair.setSubState(SubStateEnum.col_normal.key());
            	break;
        	case 9 :
        		affair.setState(StateEnum.col_done.key());
            	affair.setSubState(SubStateEnum.col_normal.key());
            	break;
        	case 13:
        		affair.setState(StateEnum.col_done.key());
            	affair.setSubState(SubStateEnum.col_normal.key());
        		break;
        	case 8 :
        		affair.setState(StateEnum.col_done.key());
            	affair.setSubState(SubStateEnum.col_done_stepStop.key());
            	affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            	break;
        }*/
        return affair;
    }
    //指定回退：直接提交给我
    @Override
    public boolean onWorkitemDoneToReady(EventDataContext context) {
        List<WorkItem> workItems = context.getWorkitemLists();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try {
            List<Long> cancelAffairIds4Doc = new ArrayList<Long>(); //需要取消归档的
            for (WorkItem wi : workItems) {
                CtpAffair affair = affairManager.getAffairBySubObjectId(wi.getId());
                affair.setState(StateEnum.col_pending.key());
                affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
                if(affair.getArchiveId() != null){
                    cancelAffairIds4Doc.add(affair.getId());
                    affair.setArchiveId(null);
                }
                affair.setUpdateDate(now);
                affair.setCompleteTime(null);
                affair.setReceiveTime(now);
                //设置回退人的id用来在待办栏目显示
                affair.setBackFromId(AppContext.getCurrentUser().getId());
                //将超期状态置为不超期，设置新的定时任务来重新计算超期状态
                affair.setCoverTime(false);
                affair.setDelete(false);
                affair.setPreApprover(null == AppContext.getCurrentUser()? null : AppContext.getCurrentUser().getId());
                
                //删除原来的定时任务（超期提醒、提前提醒）
                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                	QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
                }
                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0 )|| affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                    QuartzHolder.deleteQuartzJob("DeadLine"  + affair.getObjectId() + "_" + affair.getActivityId());
                }
                //指定回退-提交给我（回退节点删除多次消息提醒任务）
                ColUtil.deleteCycleRemindQuartzJob(affair, false);
                
                //设置新的定时任务
                Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), (ColSummary)context.getAppObject());
                Long deadLine = affair.getDeadlineDate();
                Date createTime = affair.getReceiveTime() == null ? affair.getCreateDate() : affair.getReceiveTime();
                Date deadLineRunTime = null;
                try {
                    if (deadLine != null && deadLine != 0){
                        deadLineRunTime = workTimeManager.getCompleteDate4Nature(new Date(createTime.getTime()), deadLine, accountId);
                        affair.setExpectedProcessTime(deadLineRunTime);
                    }
                } catch (WorkTimeSetExecption e) {
                  log.error("", e);
                }
                affairManager.updateAffair(affair);
                ColUtil.affairExcuteRemind(affair, accountId);
                
                CollaborationReceivetimeChangeEvent event = new CollaborationReceivetimeChangeEvent(this);
                event.setAffairId(affair.getId());
                EventDispatcher.fireEventAfterCommit(event);
                // 发送消息
//                try {
//                    Set<MessageReceiver> receiversPending = new HashSet<MessageReceiver>();
//                    receiversPending.add(new MessageReceiver(affair.getId(), affair.getMemberId(),
//                            "message.link.col.pending", affair.getId(), null));
//                    Integer importantLevel = ColUtil.getImportantLevel(affair);
//                    String content = ResourceUtil.getString("collaboration.appointStepBack.msgToSend",
//                            AppContext.currentUserName(), affair.getSubject());
//                    MessageContent messageContentSent = new MessageContent(content);
//                    messageContentSent.setImportantLevel(affair.getImportantLevel());
//                    userMessageManager.sendSystemMessage(messageContentSent, ApplicationCategoryEnum.collaboration,
//                            AppContext.getCurrentUser().getId(), receiversPending, importantLevel);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
            if (!CollectionUtils.isEmpty(cancelAffairIds4Doc)) {
                if(docApi != null){
                    Long userId = 0l;
                    if(AppContext.getCurrentUser()!=null){
                        userId = AppContext.getCurrentUser().getId();
                    } 
                    docApi.deleteDocResources(userId , cancelAffairIds4Doc);
                }
            }
        } catch (BusinessException e) {
        	log.error("", e);
        }
        return true;
    }

    @Override
    public boolean onWorkitemWaitToReady(EventDataContext context) {
        try {
            ColSummary summary = (ColSummary)context.getAppObject();
            summary.setSubState(CollaborationEnum.SubState.Normal.ordinal());
            colManager.updateColSummary(summary);
            List<WorkItem> workItems = context.getWorkitemLists();
            
            List<CtpAffair> effactAffairs = new ArrayList<CtpAffair>(workItems.size());
            
            for (WorkItem wi : workItems) {
                CtpAffair effactAffair = transDeal4SepcailSubmit(wi,SubStateEnum.col_pending_unRead,summary);
                
                if(effactAffair != null) {
                    effactAffairs.add(effactAffair);
                }
            }
            
            List<CtpAffair> assignAffairs = (List<CtpAffair>) context.getBusinessData(ASSIGNED_AFFAIRS);
            if(assignAffairs == null) {
                assignAffairs = new ArrayList<CtpAffair>();
            }
            assignAffairs.addAll(effactAffairs);
            
            context.setBusinessData(ASSIGNED_AFFAIRS, assignAffairs);
            
        } catch (Exception e) {
            log.error("",e);
        }
        return true;
    }

	/* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.event.AbstractEventListener#onSubProcessStarted(com.seeyon.ctp.workflow.event.EventDataContext)
     */
    @Override
    public boolean onSubProcessStarted(EventDataContext context) {
        try {
            String processTemplateId = context.getProcessTemplateId();
            String sendId = context.getStartUserId();
            //处理人的事项数据
            AffairData affairData = (AffairData) context.getBusinessData(EventDataContext.CTP_AFFAIR_DATA);
            CtpTemplate templete = this.templateManager.getCtpTemplateByWorkFlowId(Long.parseLong(processTemplateId));
            if (templete == null) {
                log.error("发起新流程失败，原因：触发的表单模板已被删除。NewflowRunningId=" + processTemplateId);
                return false;
            }
            if(templete.getState() == 1){
                //记录流程日志并且发送消息-0发起人和触发节点
                processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(affairData.getProcessId()), -1L, ProcessLogAction.triggerTaskFail, templete.getSubject(), ResourceUtil.getString("collaboration.templete.stop"));
               //发起人和触发节点发送消息
                Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                receivers.add(new MessageReceiver(templete.getId(), affairData.getSender()));
                receivers.add(new MessageReceiver(templete.getId(), affairData.getMemberId()));
                MessageContent templeteContent = new MessageContent("collaboration.msg.workflow.tempalte.stop", affairData.getSubject(),templete.getSubject()).setImportantLevel(affairData.getImportantLevel());
                userMessageManager.sendSystemMessage(templeteContent, ApplicationCategoryEnum.collaboration, V3xOrgEntity.CONFIG_SYSTEM_AUTO_TRIGGER_ID, receivers);

            }
            Map<String,Object> ext = new HashMap<String,Object>();
            ext.put(EventDataContext.CTP_WORKFLOW_SUBPROCESS_SKIP_FSENDER, context.isSubProcessSkipFSender());
            SendCollResult sendCollResult = this.colPubManager.transSendColl(ColConstant.SendType.child, templete.getId(),
                    Long.parseLong(sendId), affairData.getFormRecordId(),affairData.getModuleId(),ext);
            //子流程的已发事项
            CtpAffair childSenderAffair = sendCollResult.getSentAffair();
            ColSummary newSummary = sendCollResult.getSummary();
            wapi.updateSubProcessRunning(context.getSubProcessRunningId(), newSummary.getProcessId(),
                    newSummary.getCaseId(), context.getStartUserId(), context.getStartUserName());
            //触发新流程，发送系统消息 ： 来自《主流程标题》的子流程《子流程标题》已经发起
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            //主流程的已发事项
            CtpAffair senderAffair = affairManager.getSenderAffair(affairData.getModuleId());
            Integer importantLevel = ColUtil.getImportantLevel(senderAffair);
            boolean isCanViewByMainFlow = false;
            Object canViewByMainFlowObject = context
                    .getBusinessData(EventDataContext.CTP_SUB_WORKFLOW_CAN_VIEW_BY_MAIN_FLOW);
            if (null != canViewByMainFlowObject) {
                isCanViewByMainFlow = (Boolean) canViewByMainFlowObject;
            }
            List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelToList(senderAffair.getSenderId());//得到谁代理我了

            if (isCanViewByMainFlow) {//能被主流程查看，就能打开连接
                Object relativeProcessId = context.getBusinessData(EventDataContext.CTP_WORKFLOW_MAIN_PROCESSID);
                Long relativeProcessIdl = relativeProcessId == null?  0l : Long.valueOf(relativeProcessId.toString());
                receivers.add(new MessageReceiver(childSenderAffair.getId(), senderAffair.getSenderId(),
                        "message.link.col.done.newflow", childSenderAffair.getId(), 0, affairData.getModuleId(),newSummary.getProcessId(),relativeProcessIdl));
               //给代理人发消息
                for(AgentModel am:agentModelList){
                	Long agentId=am.getAgentId();//代理人id
                	receivers.add(new MessageReceiver(childSenderAffair.getId(), agentId,
                            "message.link.col.done.newflow", childSenderAffair.getId(), 0, affairData.getModuleId(),newSummary.getProcessId(),relativeProcessIdl));
                }
            } else {
                receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getSenderId()));
                //给代理人发消息
                for(AgentModel am:agentModelList){
                	Long agentId=am.getAgentId();//代理人id
                	receivers.add(new MessageReceiver(senderAffair.getId(), agentId));
                }
            }
            String mesSubject = newSummary.getSubject();
            if(null !=  newSummary.getAutoRun() && newSummary.getAutoRun()){
            	mesSubject = ResourceUtil.getString("collaboration.newflow.fire.subject",mesSubject);
            }
            //重要程度图标，不需要转换
            MessageContent content = new MessageContent("collaboration.msg.workflow.new.start", affairData.getSubject(),
            		mesSubject).setImportantLevel(senderAffair.getImportantLevel());
            if (null != newSummary.getTempleteId()) {
            	content.setTemplateId(newSummary.getTempleteId());
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, Long.parseLong(sendId), receivers,importantLevel);
            return true;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean onProcessStarted(EventDataContext context) {
        return true;
    }
   
    
	private void saveListMap(AffairData affairData,Date receiveTime,Boolean isCover,List<MessageReceiver> receivers,List<MessageReceiver> receivers1) {
        if (affairData == null){
            return;
        }
        try {
            List<CtpAffair> affairList = affairData.getAffairList();
            Boolean isSendMessage = affairData.getIsSendMessage();
            if (affairList == null || affairList.isEmpty()){
                return;
            }
            CtpAffair aff = affairList.get(0);
          //  log.info("--------saveAllForceFlush------");
            DBAgent.savePatchAll(affairList);
           /* if(affairList.size() <= 50){
            }
            else{
            	DBAgent.saveAllForceFlush(affairList);
            }*/
            
          

            // 生成事项消息提醒
            if (isSendMessage) {
                CollaborationAffairAssignedMsgEvent msg = new CollaborationAffairAssignedMsgEvent(this);
                msg.setAffairData(affairData);
                msg.setReceivers(receivers);
                msg.setReceivers1(receivers1);
                msg.setReceiveTime(receiveTime);
                EventDispatcher.fireEventAfterCommit(msg);
            }

            //发送流程超期消息
            if(isCover != null && isCover){
                colMessageManager.transSendMsg4ProcessOverTime(aff, receivers, receivers1);
            }

            // 在此调用CallBack
            if (affairList.size() == 0){
                return;
            }
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException();
        }
    }
	




	    /* (non-Javadoc)
	     * @see com.seeyon.ctp.workflow.event.AbstractEventListener#onSubProcessCanceled(com.seeyon.ctp.workflow.event.EventDataContext)
	     */
	    @Override
	    public boolean onSubProcessCanceled(EventDataContext context) {
	        long subCaseId= context.getCaseId();
	        User user= AppContext.getCurrentUser();
	        String operationType= (String)context.getBusinessData(OPERATION_TYPE);
	        try {
                colManager.recallNewflowSummary(subCaseId, user, operationType);
            } catch (Throwable e) {
                log.error("子流程撤销发生异常",e);
            }
            return true;
	    }
	    /**
	     * 将指定回退状态 17 改为 16
	     * 参加bug：OA-39879
	     */
		@Override
		public boolean onWorkitemWaitToLastTimeStatus(EventDataContext context) {

	        try {
	            List<WorkItem> workItems = context.getWorkitemLists();
	            ColSummary summary = (ColSummary)context.getAppObject();
	            
	            summary.setSubState(CollaborationEnum.SubState.SpecialBack.ordinal());
	            colManager.updateColSummary(summary);
	            
	            for (WorkItem wi : workItems) {
	                transDeal4SepcailSubmit(wi,SubStateEnum.col_pending_specialBacked,summary);
	            }
	        } catch (Exception e) {
	          log.error("",e);
	        }
	        return true;

		}
       private CtpAffair transDeal4SepcailSubmit(WorkItem wi,SubStateEnum subState,ColSummary summary){
           
    	   // 发送消息
           try {
        	   Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(),summary);
               Timestamp now = new Timestamp(System.currentTimeMillis());
               CtpAffair affair = affairManager.getAffairBySubObjectId(wi.getId());
               if(Integer.valueOf(SubStateEnum.col_pending_specialBack.key()).equals(affair.getSubState())){
            	   //复合节点只有回退事项设置状态，其他的同一个节点下的事项不设置状态
            	   affair.setSubState(SubStateEnum.col_pending_unRead.key());
            	   affair.setBackFromId(null);
               }
               else if(SubStateEnum.col_pending_specialBacked.equals(subState)){
            	   affair.setSubState(SubStateEnum.col_pending_specialBacked.key());
               }
               else{
            	   affair.setPreApprover(AppContext.getCurrentUser() == null ? null : AppContext.getCurrentUser().getId());
                   affairManager.updateAffair(affair);
                   
            	   return affair;
               }
               affair.setState(StateEnum.col_pending.key());
               affair.setUpdateDate(now);
               affair.setReceiveTime(now);
               affair.setPreApprover(AppContext.getCurrentUser() == null ? null : AppContext.getCurrentUser().getId());
               Long deadLine = affair.getDeadlineDate();
               try {
            	   //超期时间为相对时间，重新设置流程超期时间
                   if (deadLine != null && deadLine != 0){
                	   Date deadLineRunTime = workTimeManager.getCompleteDate4Nature(now, deadLine, accountId);
                       affair.setExpectedProcessTime(deadLineRunTime);
                       if (now.after(deadLineRunTime)) {
                    	   affair.setCoverTime(true);
                       } else {
                    	   affair.setCoverTime(false);
                       }
                   }
               } catch (WorkTimeSetExecption e) {
                 log.error("", e);
               }
               affairManager.updateAffair(affair);
               //删除原来的定时任务（超期提醒、提前提醒）
               if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                   QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                   QuartzHolder.deleteQuartzJob("Remind"  + affair.getObjectId() + "_" + affair.getActivityId());
               }
               if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                   QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                   QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
               }
               
               //指定回退-提交给我（回退节点删除多次消息提醒任务）
               ColUtil.deleteCycleRemindQuartzJob(affair, false);
               
               //设置新的定时任务
               ColUtil.affairExcuteRemind(affair, accountId);

               Set<MessageReceiver> receiversPending = new HashSet<MessageReceiver>();
               receiversPending.add(new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.col.pending", affair.getId(), ""));

               Set<MessageReceiver> receiversPendingAgent = new HashSet<MessageReceiver>();
               Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getReceiveTime(), affair.getMemberId(), affair.getTempleteId());
               if (agentMemberId != null) {
                   receiversPendingAgent.add(new MessageReceiver(affair.getId(), agentMemberId,"message.link.col.pending", affair.getId(), ""));
               }

               Integer importantLevel = ColUtil.getImportantLevel(affair);

               String name = orgManager.getMemberById(affair.getSenderId()).getName();
               MessageContent content = new MessageContent("collaboration.appointStepBack.send",name,affair.getSubject()).setImportantLevel(affair.getImportantLevel());
               if (null != affair.getTempleteId()) {
            	   content.setTemplateId(affair.getTempleteId());
               }
               userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration,AppContext.getCurrentUser().getId(), receiversPending, importantLevel);

               if(receiversPendingAgent.size()>0){
            	   MessageContent agentContent = new MessageContent("collaboration.appointStepBack.send",name,affair.getSubject()).add("col.agent").setImportantLevel(affair.getImportantLevel());
            	   if (null != affair.getTempleteId()) {
            		   agentContent.setTemplateId(affair.getTempleteId());
                   }
                   userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration,
                           AppContext.getCurrentUser().getId(), receiversPendingAgent, importantLevel);
               }
               
               CollaborationReceivetimeChangeEvent event = new CollaborationReceivetimeChangeEvent(this);
               event.setAffairId(affair.getId());
               EventDispatcher.fireEventAfterCommit(event);
               
               return affair;
           } catch (Exception e) {
              log.error("",e);
           }
           
           return null;
       }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.workflow.event.AbstractEventListener#onProcessCanceled(com.seeyon.ctp.workflow.event.EventDataContext)
     */
    @Override
    public boolean onProcessCanceled(EventDataContext context) {
        try {
            Long affairId = null;
            if(null!=context.getBusinessData(CURRENT_OPERATE_AFFAIR_ID)){
                affairId= (Long)context.getBusinessData(CURRENT_OPERATE_AFFAIR_ID);
            }
            String repealComment= "";
            if(null!=context.getBusinessData(CURRENT_OPERATE_COMMENT_CONTENT)){
                repealComment= (String)context.getBusinessData(CURRENT_OPERATE_COMMENT_CONTENT);
            }
            String extAtt1= "";
            if(null!=context.getBusinessData(CURRENT_OPERATE_COMMENT_EXTATT1)){
                extAtt1= (String)context.getBusinessData(CURRENT_OPERATE_COMMENT_EXTATT1);
            }
            
            Long summaryId= null;
            if(null!=context.getBusinessData(CURRENT_OPERATE_SUMMARY_ID)){
                summaryId= (Long)context.getBusinessData(CURRENT_OPERATE_SUMMARY_ID);
            }
            String trackWorkflowType= "";
            if(null!=context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW)){
                trackWorkflowType= (String)context.getBusinessData(CURRENT_OPERATE_TRACK_FLOW);
            }
            if(affairId == null){//没有值，这从workitem中来查
                if( null!= context.getCurrentWorkitemId() ){
                    CtpAffair currentAffair= affairManager.getAffairBySubObjectId(context.getCurrentWorkitemId());
                    affairId= currentAffair.getId();
                    summaryId= currentAffair.getObjectId();
                }
            }
            
            Integer operationType =  (Integer)context.getBusinessData().get(OPERATION_TYPE) ;
            Boolean needSendMessage =  (Boolean)context.getBusinessData().get(CURRENT_OPERATE_NEED_SEND_MESSAGE) ;
          
            colManager.transRepalBackground(summaryId,affairId, repealComment,trackWorkflowType,operationType,needSendMessage,extAtt1,context.getBusinessData());
        
        } catch (Exception e) {
            log.error("",e);
        }
        return true;
    }
    private String makeSubject(String archiveName, ColSummary summary) throws BusinessException {
        if (Strings.isNotBlank(archiveName)) {
            archiveName = capFormManager.getCollSubjuet(summary.getFormAppid(), archiveName, summary.getFormRecordid(), false);
        }
        return archiveName;
    }

	@Override
	public boolean onWorkitemAwakeToReady(EventDataContext context) {
		try{
			String processIdStr= context.getProcessId();
			List<WorkItem> workItems = context.getWorkitemLists();
			if(Strings.isNotEmpty(workItems)){
				Long processId= Long.parseLong(processIdStr);
				ColSummary summary= colManager.getColSummaryByProcessId(processId);
				boolean dealFlag = true;
				summary.setState(CollaborationEnum.flowState.run.ordinal());
                summary.setFinishDate(null);//设置完成时间
				int maxCommitNumber = 300;
				int length = workItems.size();
                int i = 0;
				List<Long> workitemIds = new ArrayList<Long>();
                Map<Long,CtpAffair> m = new HashMap<Long,CtpAffair>();
                for (WorkItem workItem0 : workItems) {
                    workitemIds.add((long)workItem0.getId());
                }
                if(dealFlag && workitemIds.size() >0){
                    List<CtpAffair> affairList = colManager.getAffairsBySubObjects(workitemIds);
                    List<Long> affairIds = new ArrayList<Long>();
                    List<CtpAffair> datelineAffair = new ArrayList<CtpAffair>();
                    if(affairList != null){
                        for (CtpAffair ctpAffair :affairList){
                        	Long deadline = ctpAffair.getDeadlineDate();
                        	if (deadline != null && 0 != deadline) {
                        		//跟新超期时间
                        		ctpAffair.setExpectedProcessTime(workTimeManager.getCompleteDate4Nature(DateUtil.currentTimestamp(), deadline, ctpAffair.getOrgAccountId()));
                        		ctpAffair.setCoverTime(Boolean.FALSE);
                        		datelineAffair.add(ctpAffair);
                        	}
                            affairIds.add(ctpAffair.getId());
                            List<Comment> commentList = this.ctpCommentManager.getCommentList(ModuleType.collaboration, ctpAffair.getObjectId());
                            //设置协同-事项-取回-意见
                            capFormManager.updateDataState4FlowRelive(summary,ctpAffair, ColHandleType.takeBack,commentList);

                        }
                    }
                    if (datelineAffair.size() > 0) {
                    	//跟新超期时间和超期状态
						affairManager.updateAffairs(datelineAffair);
					}
                    if(affairIds.size() >0){
                        //                        ctpCommentManager.deleteCommentByAffairIds(affairIds);//删除意见- 意见不删除
                        superviseManager.updateStatusByAffairIds(0,affairIds);//还原督办信息
                    }
                    superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervising,summary.getId(),SuperviseEnum.EntityType.summary);
                }
                workitemIds = new ArrayList<Long>();
                for (WorkItem workItem0 : workItems) {
                    workitemIds.add((long)workItem0.getId());
                    i++;
                    if(i % maxCommitNumber == 0 || i == length){

                        Map<String,Object> params=new HashMap<String, Object>();
                        params.put("state", StateEnum.col_pending.key());
                        params.put("subState", SubStateEnum.col_pending_unRead.key());//修改子状态为未读
                        params.put("receiveTime", new Date());
                        params.put("completeTime", null);
                        params.put("delete", false);
                        params.put("summaryState", summary.getState());

                        Object[][] wheres = new Object[][] {
                                {"objectId",summary.getId()},
                                {"subObjectId",workitemIds}};
                        affairManager.update(params, wheres);
//                        lili1发起协同:《发送罅隙2》
//                        colMessageManager.sendMessage();
                    }
                }
                Map<String,Object> params=new HashMap<String, Object>();
                params.put("summaryState", summary.getState());
                params.put("finish", false);
                Object[][] wheres = new Object[][] {{"objectId",summary.getId()}};
                affairManager.update(params, wheres);

				colManager.updateColSummary(summary);
                for (WorkItem workItem0 : workItems) {
//                    transDeal4SepcailSubmit(workItem0,SubStateEnum.col_pending_unRead,summary);
					Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(),summary);
					CtpAffair affair = affairManager.getAffairBySubObjectId(workItem0.getId());
					//删除原来的定时任务（超期提醒、提前提醒）
					if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
						QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
						QuartzHolder.deleteQuartzJob("Remind"  + affair.getObjectId() + "_" + affair.getActivityId());
					}
					if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
						QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
						QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
					}

					//指定回退-提交给我（回退节点删除多次消息提醒任务）
					ColUtil.deleteCycleRemindQuartzJob(affair, false);

					//设置新的定时任务
					ColUtil.affairExcuteRemind(affair, accountId);

					Set<MessageReceiver> receiversPending = new HashSet<MessageReceiver>();
					receiversPending.add(new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.col.pending", affair.getId(), ""));

					Set<MessageReceiver> receiversPendingAgent = new HashSet<MessageReceiver>();
					Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getReceiveTime(), affair.getMemberId(), affair.getTempleteId());
					if (agentMemberId != null) {
						receiversPendingAgent.add(new MessageReceiver(affair.getId(), agentMemberId,"message.link.col.pending", affair.getId(), ""));
					}

					Integer importantLevel = ColUtil.getImportantLevel(affair);

					String name = orgManager.getMemberById(affair.getSenderId()).getName();
					MessageContent content = new MessageContent("collaboration.appointStepBack.send",name,affair.getSubject()).setImportantLevel(affair.getImportantLevel());
					if (null != affair.getTempleteId()) {
						content.setTemplateId(affair.getTempleteId());
					}
					userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration,AppContext.getCurrentUser().getId(), receiversPending, importantLevel);

					if(receiversPendingAgent.size()>0){
						MessageContent agentContent = new MessageContent("collaboration.appointStepBack.send",name,affair.getSubject()).add("col.agent").setImportantLevel(affair.getImportantLevel());
						if (null != affair.getTempleteId()) {
							agentContent.setTemplateId(affair.getTempleteId());
						}
						userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration,
								AppContext.getCurrentUser().getId(), receiversPendingAgent, importantLevel);
					}

					CollaborationReceivetimeChangeEvent event = new CollaborationReceivetimeChangeEvent(this);
					event.setAffairId(affair.getId());
					EventDispatcher.fireEventAfterCommit(event);
                }

			}
		} catch (Exception e) {
            log.error("",e);
        }
        return true;
	}
	/**
	 * 
	 * @Title: taoHongContentPigeonhole   
	 * @Description: 套红正文归档
	 * @param colSummary
	 * @param archiveTextName
	 * @param multipleArchiveTextName
	 * @param archiveKeyword
	 * @param user
	 * @param realFolderId
	 * @throws BusinessException      
	 * @return: void  
	 * @date:   2019年7月24日 上午9:51:35
	 * @author: xusx
	 * @since   V7.1SP1	       
	 * @throws
	 */
	private void taoHongContentPigeonhole(ColSummary colSummary,String archiveTextName,String multipleArchiveTextName,String archiveKeyword,User user,Long realFolderId) throws BusinessException{
		//正文归档
		List<CtpContentAll> ctpContentAll = contentManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, colSummary.getId());
		
		String keyword = null;
		if (Strings.isNotBlank(archiveKeyword)) {
			keyword = capFormManager.getCollSubjuet(colSummary.getFormAppid(), archiveKeyword, colSummary.getFormRecordid(), false);
		}
		if (keyword != null && keyword.length() > 85) {//关键字截取
			keyword=keyword.substring(0, 85);
		}
		boolean isCap4 = capFormManager.isCAP4Form(colSummary.getFormAppid());
		if(!isCap4 && ctpContentAll.get(0).getContent()!=null && !"".equals(ctpContentAll.get(0).getContent())){
	
			taoHongContentPigeonhole(archiveTextName, colSummary, realFolderId, user, keyword, ctpContentAll.get(0).getContentDataId(),Long.parseLong(ctpContentAll.get(0).getContent()));

		}else if(isCap4 && Strings.isNotBlank(multipleArchiveTextName)){
			Map<String,String> multipleArchiveTextNameMap = (Map<String,String>)JSONUtil.parseJSONString(multipleArchiveTextName, Map.class);
			for (String formField : multipleArchiveTextNameMap.keySet()) {
				String taoHongAttachmentId = null;
				try {
					taoHongAttachmentId = capFormManager.getMasterFieldValue(colSummary.getFormAppid(),colSummary.getFormRecordid(),formField,true).toString();
                } catch (SQLException e) {
                    log.error("",e);
                }
				if(Strings.isNotBlank(taoHongAttachmentId)) {
					taoHongContentPigeonhole(multipleArchiveTextNameMap.get(formField), colSummary, realFolderId, user, keyword,colSummary.getId(),Long.valueOf(taoHongAttachmentId));
				}

			}
		}
	}
	private void taoHongContentPigeonhole(String archiveTextName,ColSummary colSummary,Long realFolderId,User user,String keyword,Long attachmentReference,Long attachmentSubReference) throws BusinessException{
		String archiveName = makeSubject(archiveTextName,colSummary);
		if (archiveName.length() > 85) {
			archiveName = archiveName.substring(0, 85) + ".doc";
		}
		List<Attachment> attachments = attachmentManager.getByReference(attachmentReference,attachmentSubReference);
		if(Strings.isNotEmpty(attachments)){
			V3XFile file = fileManager.getV3XFile(attachments.get(0).getFileUrl());
			file.setFilename(archiveName);
			if(null != realFolderId){
				docApi.attachmentPigeonhole(file, realFolderId, user.getId(), user.getLoginAccount(), true, keyword, PigeonholeType.edoc_account.ordinal());
			}
		}
	}
	
}
