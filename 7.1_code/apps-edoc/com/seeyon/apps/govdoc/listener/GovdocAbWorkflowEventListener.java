package com.seeyon.apps.govdoc.listener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.joinwork.bpm.engine.wapi.WorkItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.helper.GovdocMessageHelper;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public abstract class GovdocAbWorkflowEventListener {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocAbWorkflowEventListener.class);

	/** 回退  **/
    public static final Integer WITHDRAW = AbstractEventListener.WITHDRAW;
    /** 取回  **/
    public static final Integer TAKE_BACK = AbstractEventListener.TAKE_BACK;
    /** 知会 **/
    public static final Integer ADD_INFORM = AbstractEventListener.ADD_INFORM;
    /** 会签  **/
    public static final Integer COL_ASSIGN = AbstractEventListener.COL_ASSIGN;
    /** 加签 **/
    public static final Integer INSERT = AbstractEventListener.INSERT;
    /** 减签  **/
    public static final Integer DELETE = AbstractEventListener.DELETE;
    /** 分配任务 **/
    public static final Integer ASSIGN = AbstractEventListener.ASSIGN;
    /** 终止 **/
    public static final Integer STETSTOP = AbstractEventListener.STETSTOP;
    /** 正常处理 **/
    public static final Integer COMMONDISPOSAL = AbstractEventListener.COMMONDISPOSAL;
    /** 撤销 **/
    public static final Integer CANCEL = AbstractEventListener.CANCEL;
    /** 暂存待办 **/
    public static final Integer ZCDB = AbstractEventListener.ZCDB;
    /**默认删除操作: 督办替换节点，自动流程复合节点的单人执行 **/
    public static final Integer AUTODELETE = AbstractEventListener.AUTODELETE;
    /** 自动跳过 **/
    public static final Integer AUTOSKIP = AbstractEventListener.AUTOSKIP;
    /** 指定回退流程重走 **/
    public static final Integer SPECIAL_BACK_RERUN = AbstractEventListener.SPECIAL_BACK_RERUN;
    /** 指定回退提交回退者 **/
    public static final Integer SPECIAL_BACK_SUBMITTO = AbstractEventListener.SPECIAL_BACK_SUBMITTO;
    /** 操作类型  **/
    public static final String OPERATION_TYPE = AbstractEventListener.OPERATION_TYPE;
    /** 执行模式: 竞争执行 **/	
    public static final String PROCESS_MODE_COMPETITION = AbstractEventListener.PROCESS_MODE_COMPETITION;
    /** 执行模式: 单人执行 **/
    public static final String PROCESS_MODE_SINGLE = "single";
	
	protected static DocApi docApi = (DocApi) AppContext.getBean("docApi");
	protected static WorkflowApiManager wapi = (WorkflowApiManager) AppContext.getBean("wapi");
	protected static AffairManager affairManager  = (AffairManager) AppContext.getBean("affairManager");
	protected static AttachmentManager attachmentManager  = (AttachmentManager) AppContext.getBean("attachmentManager");
	protected static FileManager fileManager  = (FileManager) AppContext.getBean("fileManager");
	protected static OrgManager orgManager  = (OrgManager) AppContext.getBean("orgManager");
	protected static TemplateManager templateManager  = (TemplateManager) AppContext.getBean("templateManager");
	protected static CtpTrackMemberManager trackManager  = (CtpTrackMemberManager) AppContext.getBean("trackManager");
	protected static WorkTimeManager workTimeManager  = (WorkTimeManager) AppContext.getBean("workTimeManager");
	protected static ProcessLogManager processLogManager  = (ProcessLogManager) AppContext.getBean("processLogManager");
	protected static AppLogManager appLogManager  = (AppLogManager) AppContext.getBean("appLogManager");
	protected static UserMessageManager userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
	
	public abstract String getSubAppName();
	
	public abstract boolean onProcessStarted(EventDataContext context);
	public abstract boolean onProcessFinished(EventDataContext context);
	public abstract boolean onProcessCanceled(EventDataContext context);
	public abstract boolean onWorkitemAssigned(EventDataContext context);
	public abstract boolean onWorkflowAssigned(List<EventDataContext> contextList);
	public abstract boolean onWorkitemFinished(EventDataContext context);
	public abstract boolean onWorkitemCanceled(EventDataContext context);
	public abstract boolean onWorkitemTakeBack(EventDataContext context);
	public abstract boolean onWorkitemStoped(EventDataContext context);
	public abstract boolean onWorkitemWaitToLastTimeStatus(EventDataContext context);
	public abstract boolean onWorkitemWaitToReady(EventDataContext context);
	public abstract boolean onWorkitemReadyToWait(EventDataContext context);
	public abstract boolean onWorkitemDoneToReady(EventDataContext context);
	public abstract boolean onSubProcessStarted(EventDataContext context);
	public abstract boolean onSubProcessCanceled(EventDataContext context);

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
	
	/**
     * 设置Affair的运行时长，超时时长，按工作时间设置的运行时长，按工作时间设置的超时时长。
     * @param affair
     * @throws BusinessException
     */
	protected void setTime2Affair(CtpAffair affair,EdocSummary summary) throws BusinessException{
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
		    	deadline = workTimeManager.getDealWithTimeValue(DateUtil.currentTimestamp(), affair.getExpectedProcessTime(), orgAccountId);
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
    
	public void affairExcuteRemind(CtpAffair affair, Long summaryAccountId,Date deadLineRunTime) {
		if(GovdocUtil.isGovdocWf(affair.getApp(), affair.getSubApp()) || GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
            //超期提醒
            try {
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
                         
                         LOGGER.info("___EDOC___创建定时任务：activityId:"+affair.getActivityId()+",objectId："+affair.getObjectId()+",事项名："+affair.getId()+",定时任务name:"+name+",预计执行时间:"+deadLineRunTime);

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
	
    //通过workitemId得到affair
    protected CtpAffair eventData2ExistingAffair(EventDataContext eventData) throws BusinessException{
    	int operationType = DateSharedWithWorkflowEngineThreadLocal.getOperationType(); 
    	
    	WorkItem workitem =  eventData.getWorkItem();
    	CtpAffair affair = null;
        if(null != eventData.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT)) {
        	affair = (CtpAffair)eventData.getBusinessData(GovdocWorkflowEventListener.CTPAFFAIR_CONSTANT);
        } else {
             affair = affairManager.getAffairBySubObjectId(workitem.getId());
         }
         
         if(affair == null) {}
         else{
        	 switch(operationType) {
          	case 1 :  //回退
          	case 2 : //取回
          		affair.setState(StateEnum.col_stepBack.key());
              	affair.setSubState(SubStateEnum.col_normal.key());
              	break;
          	case 10 : //撤销
          		affair.setState(StateEnum.col_cancel.key());
              	affair.setSubState(SubStateEnum.col_normal.key());
              	break;
          	case 9 : //正常处理
          	case 13: //自动跳过
          		affair.setState(StateEnum.col_done.key());
              	affair.setSubState(SubStateEnum.col_normal.key());
          		break;
          	case 8 : //终止
          		affair.setState(StateEnum.col_done.key());
              	affair.setSubState(SubStateEnum.col_done_stepStop.key());
              	affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
              	break;
          }
         }         
         return affair;
    }
    
    public void logInfo4WorkItemFinished(CtpAffair affair, User user, Timestamp now) {
		StringBuilder sb = new StringBuilder();
		sb.append("事项ID：").append(affair.getId());
		sb.append("，事项标题：").append(affair.getSubject());
		sb.append("，WorkItemFinished处理协同时间：").append(now);
		if (null == user) {
			sb.append("，处理客户端：pc");
		} else {
			sb.append("，处理客户端：").append(user.isFromM1() ? "m1" : "pc");
			sb.append("，当前用户：").append(user.getName());
		}		
		if (user != null && !user.getId().equals(affair.getMemberId())) {
			List<Long> ownerIds = MemberAgentBean.getInstance().getAgentToMemberId(ModuleType.collaboration.getKey(), user.getId());
			if (Strings.isNotEmpty(ownerIds) && ownerIds.contains(affair.getMemberId())) {
				sb.append("代理校验：True");
			} else {
				sb.append("代理校验：False");
			}
		}
	}
    
    public void getReceiver(boolean isSendMessage, CtpAffair affair, int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1) {
		if (GovdocUtil.isGovdocWf(affair.getApp(), affair.getSubApp())) {
			String pendingLink = "message.link.govdoc.pending";
			Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), affair.getMemberId(), affair.getTempleteId());
			if (agentMemberId != null) {
				receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), pendingLink, affair.getId().toString()));
				receivers1.add(new MessageReceiver(affair.getId(), agentMemberId, pendingLink, affair.getId().toString()));
			} else {
				receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), pendingLink, affair.getId().toString()));
			}
		} else if (GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
			String pendingLink = "message.link.govdoc.pending";
			if (isSendMessage) {
                Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), affair.getMemberId());
                if (agentMemberId != null) {
                    receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), pendingLink, affair.getId().toString()));
                    receivers1.add(new MessageReceiver(affair.getId(), agentMemberId, pendingLink, affair.getId().toString()));
                } else {
                    receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), pendingLink, affair.getId().toString()));
                }
            }
		}
	}
    
    public void sendMessage(CtpAffair affair,int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1,
            V3xOrgMember sender, Object[] subjects, Integer importantLevel, String bodyContent, String bodyType,
            Date bodyCreateDate, Long[] userInfoData) {
    	Integer systemMessageFilterParam = GovdocMessageHelper.getSystemMessageFilterParam(affair).key;
        if (GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
            ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
            if(userInfoData.length==2 && userInfoData[1]!=null) {//登记的时候是代理人进行登记的。
                try {
                    String agentToName ="";
                    Long agentToId = userInfoData[1];
                    try{
                        agentToName = orgManager.getMemberById(agentToId).getName();
                    }catch(Exception e){
                        LOGGER.error("获取代理人名字抛出异常",e);
                    }
                    userMessageManager.sendSystemMessage(MessageContent.get("edoc.send", subjects[0], agentToName,app).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel).add("edoc.agent.deal",sender.getName()),
                            appEnum, agentToId, receivers, systemMessageFilterParam);
                    if(receivers1 != null && receivers1.size() != 0){
                        userMessageManager.sendSystemMessage(MessageContent.get("edoc.send", subjects[0], agentToName,app).setBody(bodyContent, bodyType, bodyCreateDate).add("edoc.agent.deal",sender.getName()).add("col.agent").setImportantLevel(importantLevel),
                                appEnum, agentToId, receivers1, systemMessageFilterParam);
                    }
                } catch (Exception e) {
                    LOGGER.error("发起公文消息提醒失败!", e);
                }
            }else{
                try {
                    userMessageManager.sendSystemMessage(MessageContent.get("edoc.send", subjects[0], sender.getName(),app).setBody(bodyContent, bodyType, bodyCreateDate).setImportantLevel(importantLevel),
                            appEnum, sender.getId(), receivers, systemMessageFilterParam);
                } catch (Exception e) {
                    LOGGER.error("发起公文消息提醒失败!", e);
                }
                if(receivers1 != null && receivers1.size() != 0){
                    try {
                        userMessageManager.sendSystemMessage(MessageContent.get("edoc.send", subjects[0], sender.getName(),app).setBody(bodyContent, bodyType, bodyCreateDate).add("col.agent").setImportantLevel(importantLevel),
                                appEnum, sender.getId(), receivers1, systemMessageFilterParam);
                    } catch (Exception e) {
                        LOGGER.error("发起公文消息提醒失败!", e);
                    }
                }
            }
        }
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
                List<CtpAffair> affairs = affairManager.getByConditions(null, nameParameters);
                affair4Message.addAll(affairs);
                ids.clear();
            }
        }
        return affair4Message;
    }
	
    public void updateAffairBySubObject(List<Long> workItems,Integer state,Integer subState,Long objectId) throws BusinessException {
        int MaxCommitNumber = 300;
        int length = workItems.size();
        List<Long> workitemIds = new ArrayList<Long>();
        int i = 0;
        Map<String, Object> nameParameters = new HashMap<String, Object>();
        nameParameters.put("updateDate", new Date());
        if(state != null) {
            nameParameters.put("state", state);
        }
        if(subState != null) {
            nameParameters.put("subState", subState);
        }
        for (Long workItem : workItems) {
            i++;
            workitemIds.add(workItem);
            if(i % MaxCommitNumber == 0 || i == length) {
            	Object[][] wheres = new Object[][] {
            		{"objectId",objectId},
            		{"subObjectId",workitemIds}};
                affairManager.update(nameParameters, wheres);
//                DBAgent.bulkUpdate(sql.toString(), nameParameters);
                workitemIds = new ArrayList<Long>();
            }
        }
    }

    public void updateStepBackAffair(List<Long> workItems, Integer state, Integer subState,Long objectId) {
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

    
}
