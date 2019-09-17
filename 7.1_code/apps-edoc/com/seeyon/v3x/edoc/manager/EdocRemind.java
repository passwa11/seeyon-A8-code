package com.seeyon.v3x.edoc.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.v3x.edoc.dao.EdocZcdbDao;
import com.seeyon.v3x.edoc.domain.EdocZcdb;
import com.seeyon.ctp.util.Strings;

public class EdocRemind implements QuartzJob {
	private final static Log LOGGER = LogFactory.getLog(EdocRemind.class);

	public void execute(Map<String, String> parameters) {
		this.execute(Long.valueOf(parameters.get("objectId")));
	}

	public void execute(JobExecutionContext datacontext)
			throws JobExecutionException {
		Long objectId = null;
		JobDetail jobDetail = datacontext.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();

		objectId = jobDataMap.getLongFromString("objectId");

		execute(objectId);
	}

	private void execute(Long objectId) {
		EdocZcdbDao edocZcdbDao = (EdocZcdbDao) AppContext
				.getBean("edocZcdbDao");
		UserMessageManager messageManager = (UserMessageManager) AppContext
				.getBean("userMessageManager");
		AffairManager affairManager = (AffairManager) AppContext
				.getBean("affairManager");

		try {
			String title = null;
			String messagePendingLink = null;
			ApplicationCategoryEnum appEnum = null;
			Long sendId = null;
			Integer importantLevel = null;

			CtpAffair affair = null;
			EdocZcdb edocZcdb = null;
			affair = affairManager.get(objectId);
			edocZcdb = edocZcdbDao.getEdocZcdbByAffairId(objectId);
			// 暂存待办提醒时间为空,不做提醒(并且公文状态必须是待办状态)
			if (affair == null || edocZcdb == null
					|| edocZcdb.getZcdbTime() == null ||affair.getState()!=StateEnum.col_pending.getKey())
				return;

			title = affair.getSubject();

			messagePendingLink = "message.link.edoc.pending";

			appEnum = ApplicationCategoryEnum.edoc;

			sendId = affair.getSenderId();
			importantLevel = affair.getImportantLevel();

			String msgKey = null;

			msgKey = "process.summary.advanceRemind.edoc";

			int forwardMemberFlag = 0;
			String forwardMember = null;
			if(Strings.isNotBlank(affair.getForwardMember())) {
				forwardMember = affair.getForwardMember();
				forwardMemberFlag = 1;
			}
			try {
				Map<Long, MessageReceiver> receiverMap = new HashMap<Long, MessageReceiver>();
				Map<Long, MessageReceiver> receiverAgentMap = new HashMap<Long, MessageReceiver>();

				Long memberId = affair.getMemberId();
				Long affairId = affair.getId();
				if (affair.getState() == StateEnum.col_pending.getKey()) {
					receiverMap.put(memberId, new MessageReceiver(affairId,
							memberId, messagePendingLink, affairId.toString()));
					// 给代理人消息提醒
					Long agentId = MemberAgentBean.getInstance()
							.getAgentMemberId(appEnum.key(), memberId);
					if (agentId != null)
						receiverAgentMap
								.put(memberId,
										new MessageReceiver(affairId, agentId,
												messagePendingLink, affairId
														.toString()));
				} 
				Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
				if (!receiverMap.isEmpty()) {
					Set<MessageReceiver> receivers = new HashSet<MessageReceiver>(
							receiverMap.values());
					messageManager.sendSystemMessage(
							MessageContent.get(msgKey, title,
									forwardMemberFlag, forwardMember)
									.setImportantLevel(importantLevel),
							appEnum, sendId, receivers,systemMessageFilterParam);
				}

				if (!receiverAgentMap.isEmpty()) {
					Set<MessageReceiver> receiverAgents = new HashSet<MessageReceiver>(
							receiverAgentMap.values());
					messageManager.sendSystemMessage(
							MessageContent.get(msgKey, title, forwardMemberFlag,forwardMember)
									.setImportantLevel(importantLevel)
									.add("col.agent"), appEnum, sendId,
							receiverAgents,systemMessageFilterParam);
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		} catch (Exception e) {
			// 绑定的定时任务事项已经不存在或被删除
			LOGGER.error("", e);
			return;
		}
	}
	
	
	static OrgManager orgManager;
	static OrgManager getOrgManager(){
		if(orgManager == null){
			orgManager = (OrgManager) AppContext.getBean("orgManager");
		}
		
		return orgManager;
	}

}
