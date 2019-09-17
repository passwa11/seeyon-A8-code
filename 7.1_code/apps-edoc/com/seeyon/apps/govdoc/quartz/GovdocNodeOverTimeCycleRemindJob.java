package com.seeyon.apps.govdoc.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;

/**
 * 节点超期后多次提醒触发器
 * @author 唐桂林
 *
 */
public class GovdocNodeOverTimeCycleRemindJob implements QuartzJob {

	private final static Log LOGGER = LogFactory.getLog(GovdocNodeOverTimeCycleRemindJob.class);
	
	private AffairManager affairManager;
	private OrgManager orgManager;
	private UserMessageManager userMessageManager;
	
	@Override
	public void execute(Map<String, String> parameters) {
		String objectId = parameters.get("objectId");
		String activityId = parameters.get("activityId");
		String cycleMinutes = parameters.get("cycleRemindTimeMinutes");

		LOGGER.info("执行节点超期后多次提醒定时任务 时间：" + DateUtil.get19DateAndTime() + " objectId=" + objectId + " activityId=" + activityId);
		try {

			List<CtpAffair> affairList = affairManager.getAffairsByObjectIdAndNodeId(Long.valueOf(objectId), Long.valueOf(activityId));

			boolean isStopCycleJob = false;
			String subject = "";
			Long senderId = null;

			String messageLink = null;
			List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
			List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
			V3xOrgMember member = null;
			V3xOrgMember agent = null;
			boolean hasPending = false;
			Date now = new Date();
			if (Strings.isNotEmpty(affairList)) {
				for (CtpAffair bean : affairList) {
				    
				    boolean isOverTime  = bean.getExpectedProcessTime()!=null && now.after(bean.getExpectedProcessTime()); //超期了
				    if(!isOverTime){
				        break;
				    }
					
				    boolean isPending  = bean.getState() != null && bean.getState().intValue() == StateEnum.col_pending.key() ;
				    
				    if (isPending) {
						hasPending = true;
						member = orgManager.getMemberById(bean.getMemberId());

						if (member != null && member.isValid()) {
							if(GovdocUtil.isGovdocWfOld(bean.getApp(), bean.getSubApp())) {
				            	messageLink = "message.link.edoc.pending";
				            } else {
				            	messageLink = "message.link.govdoc.pending";	
				            }
							
							receivers.add(new MessageReceiver(bean.getId(), member.getId(), messageLink, bean.getId().toString()));

							// 判断当前的代理人是否有效
							Long agentId = WFComponentUtil.getAgentMemberId(bean.getTempleteId(), member.getId(), bean.getReceiveTime());

							if (agentId != null) {
								agent = orgManager.getMemberById(agentId);
								if (agent != null && agent.isValid()) {
									agentReceivers.add(new MessageReceiver(bean.getId(), agent.getId(), messageLink, bean.getId().toString()));
								}
							}

							if (Strings.isBlank(subject)) {
								subject = bean.getSubject();
							}
							if (senderId == null) {
								senderId = bean.getSenderId();
							}
							if (!isStopCycleJob) {
								isStopCycleJob = true;
							}
						}
					}
				}
			}

			MessageContent msgContent = null;

			// 创建下一次的定时任务：
			if (hasPending) {
			    
			    if (Strings.isNotEmpty(receivers)) {
			        msgContent = MessageContent.get("node.affair.overTerm", subject);
			        userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, senderId, receivers, ColMessageFilterEnum.overTime.key);
			    }
			    
			    if (Strings.isNotEmpty(agentReceivers)) {
			        msgContent = MessageContent.get("node.affair.overTerm.agent", subject);
			        userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, senderId, agentReceivers, ColMessageFilterEnum.overTime.key);
			    }

				String jobName = "CycleRemind_" + objectId + "_" + activityId;
				if(!Strings.isDigits(cycleMinutes)){
					return;
				}
				Date startTime = Datetimes.addMinute(new Date(), Integer.parseInt(cycleMinutes));
				try {
					String jobN = jobName+Math.random();
					QuartzHolder.newQuartzJob(jobN, startTime, "govdocNodeOverTimeCycleRemindJob", parameters);
				} catch (Throwable e) {
					LOGGER.error(e.getMessage(), e);
				} finally {

				}

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

	}
	
	

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

}
