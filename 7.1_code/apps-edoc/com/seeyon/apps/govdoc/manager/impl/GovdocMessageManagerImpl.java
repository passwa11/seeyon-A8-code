package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocMessageHelper;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.ocip.util.OrgUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * 新公文消息管理类
 * 
 * @author 唐桂林
 * 
 */
public class GovdocMessageManagerImpl implements GovdocMessageManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocMessageManagerImpl.class);

	private GovdocSummaryManager govdocSummaryManager;
	protected GovdocLogManager govdocLogManager;
	private OrgManager orgManager;
	private UserMessageManager userMessageManager;
	private AffairManager affairManager;
	private CtpTrackMemberManager trackManager;
	private SuperviseManager superviseManager;
	private GovdocPishiManager govdocPishiManager;

	@Override
	public void sendMessage(AffairData affairData, List<MessageReceiver> receivers, List<MessageReceiver> receivers1, Date bodyCreateDate) throws BusinessException {
		try {
			List<CtpAffair> affairList = affairData.getAffairList();
			if (affairList == null) {
				return;
			}
			User user = AppContext.getCurrentUser();
			Long senderId = affairData.getSender();
			String subject = affairData.getSubject();
			String forwardMemberId = affairData.getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
			List<Long>  receiversIds = new ArrayList<Long>();
			List<MessageReceiver> receivers2=new ArrayList<MessageReceiver>(); //代录人
			CtpAffair aff = affairList.get(0);
			//add by rz 2018-08-30 [存在代录人的情况 终止时需要给所有代录人发送消息] satrt
			if(aff.getApp()==ApplicationCategoryEnum.edoc.getKey()){
				List<Long> list = null;
				try {
					list = govdocPishiManager.getEdocUserLeaderId(aff.getMemberId());
				} catch (Exception e1) {
					LOGGER.error(e1);
				}
				if(list!=null&&!list.isEmpty()){
					for(Long userId:list){
						if(!userId.equals(user.getId())){
							//receivers.add(new MessageReceiver(aff.getId(), aff.getMemberId(), "message.link.govdoc.pending", aff.getId().toString()));
							MessageReceiver messageReceiver = new MessageReceiver(aff.getId(), userId, "message.link.govdoc.pending", aff.getId().toString());
							messageReceiver.setForceSMS(affairData.isSmsAlert());
							receivers2.add(messageReceiver);
						}
					}
				}
			}
			//add by rz 2018-08-30 [存在代录人的情况 终止时需要给所有代录人发送消息] end
			Integer importantLevel = WFComponentUtil.getImportantLevel(aff);
			String bodyContent = affairData.getBodyContent();
			String bodyType = affairData.getContentType();
			int app = aff.getApp();
			V3xOrgMember sender = null;
			sender = orgManager.getMemberById(senderId);
			Object[] subjects = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember, 0,"",0,aff.getSubApp() };
			if (app == ApplicationCategoryEnum.edoc.key()) {
				Map<Long, CtpAffair> transAffairs = new HashMap<Long, CtpAffair>();
				for (CtpAffair a : affairList) {
					if (a.getFromId() != null && a.getFromType() != null) {
						transAffairs.put(a.getId(), a);
					}
				}
	
				/*
				 *  {1}发起{9,choice,1#发文|2#收文|3#签报|4#收文}:《{0}{2,choice,0#|1# (由{3}原发)}》 {4,choice,0#|1#（由{5}}{6,choice,0#|1#加签|2#知会|3#会签|4#减签|5#多级会签|6#传阅|7#移交}{4,choice,0#|1#给你）}
				 */
				for (MessageReceiver r : receivers) {
					MessageContent mContent = null;
					CtpAffair affair = transAffairs.get(r.getReferenceId());
					if (affair != null) {
						String memberName = null;
						if(affair.getPreApprover() !=null){
							V3xOrgMember member = orgManager.getMemberById(affair.getPreApprover());
							memberName = member.getName();
						}else{
							memberName = user.getName();
						}
						Object[] transObjs = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember, 1, memberName, affair.getFromType(),aff.getSubApp()  };
						mContent = MessageContent.get("govdoc.send", transObjs);
					} else {
						mContent = MessageContent.get("govdoc.send", subjects);
					}
					mContent.setBody(bodyContent, bodyType, bodyCreateDate);
					mContent.setImportantLevel(aff.getImportantLevel());
					r.setForceSMS(affairData.isSmsAlert());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, sender.getId(), r, importantLevel);
					receiversIds.add(r.getReceiverId());
				}
				if (receivers1 != null) {
					for (MessageReceiver r : receivers1) {
						if(receiversIds.contains(r.getReceiverId())){
							continue;
						}
						MessageContent mContent = null;
						CtpAffair affair = transAffairs.get(r.getReferenceId());
						if (affair != null) {
							V3xOrgMember member = null;
							if(!user.getId().equals(aff.getMemberId()) ){
								member = orgManager.getMemberById(aff.getMemberId());
							}else{
								member = orgManager.getMemberById(affair.getFromId());
							}
							Object[] transObjs = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember, 1, member.getName(), affair.getFromType(),aff.getSubApp()  };
							mContent = MessageContent.get("govdoc.send", transObjs);
						} else {
							subjects[1] = sender.getName();
							mContent = MessageContent.get("govdoc.send", subjects);
						}

						mContent.setBody(bodyContent, bodyType, bodyCreateDate);
						mContent.add("col.agent");
						mContent.setImportantLevel(aff.getImportantLevel());
						r.setForceSMS(affairData.isSmsAlert());
						userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, sender.getId(), r, importantLevel);
						receiversIds.add(r.getReceiverId());
					}
				}
				if (receivers2 != null) {
					for (MessageReceiver r : receivers1) {
						if(receiversIds.contains(r.getReceiverId())){
							continue;
						}
						MessageContent mContent = null;
						CtpAffair affair = transAffairs.get(r.getReferenceId());
						if (affair != null) {
							V3xOrgMember member = null;
							if(!user.getId().equals(aff.getMemberId())){
								member = orgManager.getMemberById(aff.getMemberId());
							}else{
								member = orgManager.getMemberById(affair.getFromId());
							}
							Object[] transObjs = new Object[] { subject, sender.getName(), forwardMemberFlag, forwardMember, 1, member.getName(), affair.getFromType(),aff.getSubApp()  };
							mContent = MessageContent.get("govdoc.send", transObjs);
						} else {
							subjects[1] = sender.getName();
							mContent = MessageContent.get("govdoc.send", subjects);
						}

						mContent.setBody(bodyContent, bodyType, bodyCreateDate);
						mContent.add("col.agent");
						mContent.setImportantLevel(aff.getImportantLevel());
						r.setForceSMS(affairData.isSmsAlert());
						userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, sender.getId(), r, importantLevel);
					}
				}
			}
		} catch(Exception e) {
			LOGGER.error("公文发送消息出错", e);
		}
	}
	
	@Override
	public Boolean sendFinishMessage(Comment comment, CtpAffair affair, Long summaryId) throws BusinessException {
		try {
			Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
			User user = AppContext.getCurrentUser();
			V3xOrgMember theMember = orgManager.getMemberById(affair.getMemberId());
			List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
			List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
			trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, affair.getMemberId());
			List<Long[]> pushMemberIds = comment.getPushMessageToMembersList();// DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
			if (trackingAffairList.isEmpty() && pushMemberIds.isEmpty()) {
				return true;
			}
	
			String forwardMemberId = trackingAffairList.isEmpty() ? null : trackingAffairList.get(0).getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
			// 检查代录关系
			String pishiFlag = govdocPishiManager.checkLeaderPishi(user.getId(), affair.getMemberId());
			
			String opinionId = String.valueOf(comment.getId());// String.valueOf(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionId());
			String opinionContentAll = comment.getContent();// DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion();
			int opinionAttitude = 0;
			if ("collaboration.dealAttitude.haveRead".equals(comment.getExtAtt1())) {
				opinionAttitude = 1;
			} else if ("collaboration.dealAttitude.agree".equals(comment.getExtAtt1())) {
				opinionAttitude = 2;
			} else if ("collaboration.dealAttitude.disagree".equals(comment.getExtAtt1())) {
				opinionAttitude = 3;
			}
			// int opinionAttitude =
			// DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionAttitude();
			// 0:意见被隐藏; -1：意见内容为空; 1: 无态度有内容（内容前面加“意见：”）; 2: 有态度有内容
			int opinionType = comment.isHidden() ? 0 : Strings.isBlank(opinionContentAll) ? -1 : opinionAttitude == -1 ? 1 : 2;
			// -1:无附件或者意见被隐藏; 1: 无态度且无内容（内容前面加“意见：”）; 2:有态度或有内容，有附件
			String relateAttr = String.valueOf(comment.getRelateInfo());
			boolean relate = false;
			if (Strings.isEmpty(relateAttr) || "[]".equals(relateAttr) || "null".equals(relateAttr)) {
				relate = true;
			}
			int opinionAtt = (relate || opinionType == 0) ? -1 : (Strings.isBlank(opinionContentAll) && opinionAttitude == -1 ? 1 : 2);
			// 有内容，有附件：减少4个字节
			int deviation = opinionAtt == 2 ? -4 : 0;
			String opinionContent = UserMessageUtil.getComment4Message(opinionContentAll, deviation);
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			// TODO 如果是发起者，意见对发起者隐藏要做单独处理
			Set<Long> members = new HashSet<Long>();
			for (CtpAffair affair1 : trackingAffairList) {
				if (!(affair1.getState() != null && (affair1.getState() == StateEnum.col_waitSend.key() || affair1.getState() == StateEnum.col_sent.key())
						&& affair1.getApp() != null && affair1.getApp() == ApplicationCategoryEnum.edoc.getKey())) {
					if (affair1.isDelete()) {
						continue;
					}
				}
				Long affairId = affair1.getId();
				Long recieverMemberId = affair1.getMemberId();
				Long transactorId = affair1.getTransactorId();
				if (transactorId != null && GovdocHelper.isGovdocProxy(recieverMemberId, transactorId)) {
					recieverMemberId = transactorId;
				}
				String messageUrl = "message.link.govdoc.pending";
				if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair1.getState())) {
					messageUrl = "message.link.govdoc.waitSend";
				}
				if (!recieverMemberId.equals(user.getId())) {
					members.add(recieverMemberId);
					receivers.add(new MessageReceiver(affairId, recieverMemberId, messageUrl, affairId, opinionId));
				}
			}
			for (Long[] push : pushMemberIds) {
				if (!members.contains(push[1])) {
					MessageReceiver receiver = new MessageReceiver(push[0], push[1], "message.link.govdoc.pending", push[0], opinionId);
					receiver.setAt(true);
					receivers.add(receiver);
				}
			}
			MessageContent mContent = new MessageContent("govdoc.opinion.deal", theMember.getName(), affair.getSubject(), forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAttitude, opinionAtt, (comment.getPraiseToSummary() != null && comment.getPraiseToSummary()) ? 1 : 0);
			mContent.setImportantLevel(affair.getImportantLevel());
			mContent.setBody(opinionContentAll, Constants.EDITOR_TYPE_HTML, new Date());
			if (!affair.getMemberId().equals(user.getId())) {
				if(!members.contains(affair.getMemberId())) {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.govdoc.done.detail", affair.getId(), opinionId));
				}
				String proxyName = user.getName();
				if("pishi".equals(pishiFlag)){
					//mContent.add("col.pishi.deal", proxyName);
				}else{ 
					mContent.add("col.agent.deal", proxyName);
				}
			}
			userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, theMember.getId(), receivers, importantLevel);
		} catch(Exception e) {
			LOGGER.error("公文发送提交消息出错", e);
		}
		return true;
	}

	@Override
	public Boolean sendMessage4Zcdb(CtpAffair zcdbAffair, Comment opinion) throws BusinessException {
		try {
			Integer importantLevel = WFComponentUtil.getImportantLevel(zcdbAffair);
			User user = AppContext.getCurrentUser();
	
			// 获取跟踪人
			List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(zcdbAffair.getObjectId());
			List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(zcdbAffair.getObjectId(), null);
			trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, zcdbAffair.getMemberId());

			// 检查代录关系
			String pishiFlag = govdocPishiManager.checkLeaderPishi(user.getId(), zcdbAffair.getMemberId());
			
			List<Long[]> pushMemberIds = opinion.getPushMessageToMembersList();
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			Set<Long> members = new HashSet<Long>();
			for (CtpAffair _affair : trackingAffairList) {
				if (!user.getId().equals(_affair.getMemberId()) && !_affair.isDelete()) {
					Long memberId = _affair.getMemberId();
					String messageUrl = "message.link.govdoc.pending";
					if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(_affair.getState())) {
						messageUrl = "message.link.govdoc.waitSend";
					}
					if (!members.contains(_affair.getMemberId())) {
						receivers.add(new MessageReceiver(_affair.getId(), memberId, messageUrl, _affair.getId().toString(), opinion.getId()));
						members.add(_affair.getMemberId());
					}
				}
			}
			for (Long[] push : pushMemberIds) {
				if (!user.getId().equals(push[1]) && !members.contains(push[1])) {
					receivers.add(new MessageReceiver(push[0], push[1], "message.link.govdoc.pending", push[0], opinion.getId()));
				}
			}
			String forwardMemberId = zcdbAffair.getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
	
			// 0-意见隐藏，-1内容为空，1有内容
			int opinionType = Strings.isTrue(opinion.isHidden()) ? 0 : Strings.isBlank(opinion.getContent()) ? -1 : 1;
			// -1:无附件或意见被隐藏; 1: 无内容（内容前面加“意见：”）; 2:有内容，有附件
			// TODO 附件。暂时提交的时候没有保存附件。
			int opinionAtt = ("[]".equals(opinion.getRelateInfo()) || opinionType == 0) ? -1 : (Strings.isBlank(opinion.getContent()) ? 1 : 2);
			// 有内容，有附件：减少4个字节
			int deviation = opinionAtt == 2 ? -4 : 0;
			String opinionContent = UserMessageUtil.getComment4Message(opinion.getContent(), deviation);
	
			Integer praiseType = 0;
			if (null == opinion.getPraiseToSummary()) {
				praiseType = 0;
			} else {
				praiseType = opinion.getPraiseToSummary() ? 1 : 0;
			}
			if (!user.getId().equals(zcdbAffair.getMemberId())) {
				V3xOrgMember member = null;
				member = orgManager.getMemberById(zcdbAffair.getMemberId());
				String proxyName = member.getName();
				MessageContent mContent = new MessageContent("govdoc.opinion.zcdb", zcdbAffair.getSubject(), proxyName, forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAtt, praiseType);
				mContent.setImportantLevel(zcdbAffair.getImportantLevel());
				if("pishi".equals(pishiFlag)){
					mContent.add("govdoc.pishi.deal", user.getName());
				}else{
					mContent.add("col.agent.deal", user.getName());
				}
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, zcdbAffair.getMemberId(), receivers, importantLevel);
			} else {
				MessageContent mContent = new MessageContent("govdoc.opinion.zcdb", zcdbAffair.getSubject(), user.getName(), forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAtt, praiseType);
				mContent.setImportantLevel(zcdbAffair.getImportantLevel());
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
			}
		} catch(Exception e) {
			LOGGER.error("公文发送暂存待办消息出错", e);
		}
		return true;
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
    public boolean sendMessage4Transfer(User user, EdocSummary summary, List<CtpAffair> affairs,CtpAffair oldAffair, Comment opinion) throws BusinessException {
		try {
	        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        List<String> names = new ArrayList<String>();
	        Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
	        //@发送消息需要排除的人员集合
	        Set <Long> members = new HashSet<Long>();
	        for(CtpAffair c : affairs){
	            names.add(orgManager.getMemberById(c.getMemberId()).getName());
	            receivers.add(new MessageReceiver(c.getId(), c.getMemberId(), "message.link.govdoc.pending", c.getId(), ""));
	            members.add(c.getMemberId());
	            //代理人
	            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),c.getMemberId());
	            if (agentId!=null) {
	            	receivers1.add(new MessageReceiver(c.getId(), agentId, "message.link.govdoc.pending", c.getId(), ""));
	            	members.add(agentId);
	            }
	        }
	       
	        String bodyType = summary.getBodyType();
	        Date bodyCreateDate = summary.getCreateTime();
	        Integer important = summary.getImportantLevel();
	        String forwardMemberId = summary.getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
			
			V3xOrgMember sender = orgManager.getMemberById(summary.getStartMemberId());
			//移交前的人员
			V3xOrgMember oldAffairMember = orgManager.getMemberById(oldAffair.getMemberId());
	
			Object[] transObjs = new Object[] { summary.getSubject(), sender.getName(), forwardMemberFlag, forwardMember, 1, oldAffairMember.getName(), ChangeType.Transfer.getKey()};
			//跟踪人
	        Set<MessageReceiver> trackRecievers = getTrackReceivers(user, summary.getId());
	        
	      //需要删除的
	        Set<MessageReceiver> removeTrackRecievers = new HashSet<MessageReceiver>();
	        for (MessageReceiver receiver : trackRecievers) { 
	        	//过滤移交的人的多余消息
	            if (affairs.get(0).getMemberId().equals(receiver.getReceiverId())) {  
	            	removeTrackRecievers.add(receiver);
	            }
	            members.add(receiver.getReceiverId());
	        } 
	        trackRecievers.removeAll(removeTrackRecievers);
	
	        //获取@人员列表
	        List<Long[]> pushMemberIds = opinion.getPushMessageToMembersList();
	        for(Long[] push :pushMemberIds ){
	            if(!user.getId().equals(push[1]) && !members.contains(push[1])){
	            	trackRecievers.add(new MessageReceiver(push[0], push[1],"message.link.govdoc.pending",push[0], opinion.getId()));
	            }
	        }
	        String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(opinion.getContent()), 0);
	        Integer praiseType =0;
	        if(null == opinion.getPraiseToSummary()) {
	            praiseType = 0; 
	        } else {
	            praiseType = opinion.getPraiseToSummary() ? 1 : 0;
	        }
	        if(Strings.isNotBlank(opinionContent)){
	        	opinionContent = ResourceUtil.getString("collaboration.node.affair.transfer", opinionContent,praiseType);
	        }
	        List<Map> list_attach = opinion.getAttachList();
	        if(Strings.isNotEmpty(list_attach) && list_attach.size() >= 1) {
	        	opinionContent = ResourceUtil.getString("collaboration.node.affair.transfer1", opinionContent);
	        }
	        
	        
			//代理人处理时   发送消息
	        if(!user.getId().equals(oldAffair.getMemberId())) {
	        	MessageContent mContent = new MessageContent("govdoc.transfer", transObjs).add("col.agent.deal", user.getName()).setBody("", bodyType, bodyCreateDate);
	        	mContent.setImportantLevel(important);
	        	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, summary.getImportantLevel());
	        	if (receivers1.size() > 0) {
	        		mContent = new MessageContent("govdoc.transfer", transObjs);
	        		mContent.add("col.agent.deal", user.getName()).add("col.agent").setBody("", bodyType, bodyCreateDate);
	        		mContent.setImportantLevel(important);
	            	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers1, summary.getImportantLevel());
	            }
	        	if(Strings.isNotEmpty(trackRecievers)) {
	        		mContent = new MessageContent("govdoc.node.affair.transfer", summary.getSubject(), oldAffairMember.getName(), StringUtils.join(names.iterator(), ","), opinionContent);
	        		mContent.add("col.agent.deal", user.getName());
	        		mContent.setImportantLevel(summary.getImportantLevel());
	                userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), trackRecievers, summary.getImportantLevel());
	            }
	        } else {
	        	MessageContent mContent = new MessageContent("govdoc.transfer", transObjs).setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
	        	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, summary.getImportantLevel());
	        	
	        	if (receivers1.size() > 0) {
	        		mContent =  new MessageContent("govdoc.transfer", transObjs).add("col.agent").setBody("", bodyType, bodyCreateDate);
	        		mContent.setImportantLevel(important);
	            	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers1, summary.getImportantLevel());
	            }
	        	if(Strings.isNotEmpty(trackRecievers)) {
	        		mContent = new MessageContent("govdoc.node.affair.transfer", summary.getSubject(), user.getName(), StringUtils.join(names.iterator(), ","), opinionContent);
	        		mContent.setImportantLevel(summary.getImportantLevel());
	                userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), trackRecievers, summary.getImportantLevel());
	            }
	        }
		} catch(Exception e) {
			LOGGER.error("公文发送移交消息出错", e);
		}
        return true;
    }
	

	@Override
	@SuppressWarnings("unchecked")
	public void sendMessage4Repeal(List<CtpAffair> affairs, CtpAffair currentAffair, String repealComment, boolean trackFlag) throws BusinessException {
		try {
			User user = AppContext.getCurrentUser();
			if (user == null) {
				return;
			}
			Long userId = user.getId();
			if (userId == null) {
				return;
			}
			String name = OrgHelper.showMemberNameOnly(Long.valueOf(user.getId()));
			Integer importantLevel = WFComponentUtil.getImportantLevel(affairs.get(0));
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			// 代理人集合
			List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
			// 代录人集合
			List<MessageReceiver> receivers2 = new ArrayList<MessageReceiver>();
			Set<Long> receiverIds = new HashSet<Long>(); // 同一个人在这个流程中出现多次，只收一条消息就够了
			for (CtpAffair affair1 : affairs) {
				Long agentMemberId = null;
				if (affair1.isDelete() || receiverIds.contains(affair1.getMemberId())) {
					continue;
				}
				// 如果当前用户是撤销人时，不发送消息
				if (AppContext.getCurrentUser().getId().equals(affair1.getMemberId())) {
					continue;
				}
				if (affair1.getState() == StateEnum.col_waitSend.key()) {
					receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.govdoc.waitSend", affair1.getId().toString()));
				} else {
					receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
					// 给代理人发送消息
					agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), affair1.getMemberId());
					// 如果是代理人处理时 不给代理人发消息
					if (user.getId().equals(agentMemberId)) {
						continue;
					}
					if (agentMemberId != null) {
						receivers1.add(new MessageReceiver(affair1.getId(), agentMemberId));
					}
				}
				receiverIds.add(affair1.getMemberId());
			}
			String forwardMemberId = affairs.get(0).getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
			// 获取督办人，给督办人发送消息(不给督办人的代理人发送消息)
			Map<Long, MessageReceiver> _superviseBySummaryId = getSuperviseBySummaryId(currentAffair.getObjectId(), currentAffair.getId(), receiverIds);
			receivers.addAll(_superviseBySummaryId.values());
			Set<Long> supervisekeySet = _superviseBySummaryId.keySet();
			// 如果没有附言或者
			int messageFlag = 0;
			if (Strings.isNotBlank(repealComment)) {
				messageFlag = 1;
			}
			// 判断当前是什么时候的撤销，有待办、已发、发起者，如果是待办则显示“意见”，如果是其他则显示“附言”
			String messageLink = "edoc.summary.cancel";
			if (currentAffair != null && currentAffair.getState().intValue() == StateEnum.col_pending.getKey()) {
				messageLink = "edoc.summary.cancelPending";
			}
			boolean outuser = false;
			/** OCIP 1.0-公文交换开始 */
			if (AppContext.hasPlugin("ocip")) {
				V3xOrgEntity entity = orgManager.getEntity("Member|" + user.getId());
				outuser = OrgUtil.isPlatformEntity(entity);
			}
			/** OCIP 1.0-公文交换开始 */
			
			// 代理人处理时发消息
			if (userId != null && !user.isAdmin() && !userId.equals(currentAffair.getMemberId()) && !outuser) {
				V3xOrgMember member = orgManager.getMemberById(currentAffair.getMemberId());
				if (null != member) {
					String proxyName = member.getName();
					MessageContent mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.add("col.agent.deal", name).setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers, importantLevel);
					// 给代理人发消息
					if (receivers1 != null && receivers1.size() != 0) {
						mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag);
						mContent.add("col.agent.deal", name);
						mContent.add("col.agent");
						mContent.setImportantLevel(currentAffair.getImportantLevel());
						userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers1, importantLevel);
					}
					// 给代录人发消息
					if (receivers2 != null && receivers2.size() != 0) {
						mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag);
						mContent.add("col.agent.deal", name);
						mContent.setImportantLevel(currentAffair.getImportantLevel());
						userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers2, importantLevel);
					}
				}
			} else {
				// 当前人 处理时 发消息
				if (!trackFlag || outuser) {
					MessageContent mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers, importantLevel);
				} else {
					List<Long> traeDataAffair = (List<Long>) DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affair");
					Long aId = (Long) DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affairId");
					Integer type = (Integer) DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_traceType");
					Iterator<MessageReceiver> _it = receivers.iterator();
					Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
					Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
					CtpAffair cloneAffair = affairManager.get(aId);
					while (_it.hasNext()) {
						MessageReceiver mr = _it.next();
						Long referenceId = mr.getReferenceId();
						if (traeDataAffair!=null && traeDataAffair.contains(referenceId) && !supervisekeySet.contains(mr.getReceiverId())) {
							new_receivers.add(new MessageReceiver(cloneAffair.getId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.repealRecord.name(), cloneAffair.getId().toString(), type.intValue() + ""));
						} else {
							old_receivers.add(mr);
						}
					}
					MessageContent mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, old_receivers, importantLevel);
					
					mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.add("edoc.summary.cancel.traceview");
					mContent.setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, new_receivers, importantLevel);
				}
				
				// 给代理人发消息
				if (receivers1 != null && receivers1.size() != 0) {
					MessageContent mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.add("col.agent");
					mContent.setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers1, importantLevel);
				}
				
				// 给代录人发消息
				if (receivers2 != null && receivers2.size() != 0) {
					MessageContent mContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag);
					mContent.setImportantLevel(currentAffair.getImportantLevel());
					userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers2, importantLevel);
				}
			}
		} catch(Exception e) {
			LOGGER.error("公文发送撤销消息出错", e);
		}
	}

	@Override
    @SuppressWarnings("unchecked")
	public boolean sendStepBackMessage(List<CtpAffair> allTrackAffairLists, CtpAffair affair, Long summaryId,Comment signOpinion,boolean traceFlag, boolean msg2Sender) throws BusinessException {
		try {
			String messageLink = "message.link.govdoc.pending";
	    	String messageLinkDone = "message.link.govdoc.done.detail";
	    	String messageLinkSent = "message.link.govdoc.sended";
	    	String messageLinkWait = "message.link.govdoc.waitSend";
	    	String messageLinkTrace = "message.link.govdoc.traceRecord";
            if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
            	messageLink = "message.link.edoc.pending";
            	messageLinkDone = "message.link.edoc.done";
            	messageLinkSent = "message.link.edoc.sended";
            	messageLinkWait = "message.link.edoc.waitSend";
            	messageLinkTrace = "message.link.edoc.traceRecord";
            }
            
	    	Integer importantLevel = affair.getImportantLevel();
			User user = AppContext.getCurrentUser();
	        String userName = "";
	        if (user != null) {
	            userName = user.getName();
	        }
	        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
	    	//给所有待办事项发起协同被回退消息提醒
	    	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	    	Set<Long> filterMember = new HashSet<Long>();
	    	
	    	//1、发起者一定发送
	    	if(affair.getSubApp()!=ApplicationSubCategoryEnum.edoc_jiaohuan.key() && !filterMember.contains(affair.getMemberId())) {
	    		CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
	    		if(null != sendAffair.getSubState() && sendAffair.getSubState() == SubStateEnum.col_waitSend_stepBack.getKey()) {
	    			//如果是回退或者撤銷，指定回退調整的連接地址用新的Link
	    			receivers.add(new MessageReceiver(affair.getId(), sendAffair.getMemberId(), messageLinkWait, sendAffair.getId().toString()));
		        	filterMember.add(affair.getSenderId());
	    		}else {
	    			if(affair.getState() != StateEnum.col_pending.getKey()){
		    			receivers.add(new MessageReceiver(affair.getId(), sendAffair.getMemberId(), messageLinkSent, sendAffair.getId().toString()));
			        	filterMember.add(affair.getSenderId());
	    			}
	    		}        	
	    	}
	    	
	    	//2.当前节点父节点。
	    	Map<Long, Long> affairMap = DateSharedWithWorkflowEngineThreadLocal.getAffairMap();
	    	for(Long key : affairMap.keySet()){
	    		Long affId = affairMap.get(key);
	    		if(!filterMember.contains(key)){
	    			receivers.add(new MessageReceiver(affId, key, messageLink, affId.toString()));
	    			filterMember.add(key);
	    		}
	    		Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),key);
				if(agentId!=null && !filterMember.contains(agentId) && user.getId().longValue()!= agentId.longValue()){
					receivers.add(new MessageReceiver(affId,agentId, messageLink, affId.toString()));
					filterMember.add(agentId);
				}
	    	}
	    	//3、跟踪事项
	    	for(CtpAffair _affair : allTrackAffairLists){
	    		if(!filterMember.contains(_affair.getMemberId()) && user.getId().longValue() != _affair.getMemberId().longValue()){
					receivers.add(new MessageReceiver(_affair.getId(), _affair.getMemberId()));
					Long transactorId = _affair.getTransactorId();
					if(transactorId!=null && !filterMember.contains(transactorId) && user.getId().longValue()!= transactorId.longValue()){
						receivers.add(new MessageReceiver(_affair.getId(), _affair.getTransactorId()));
						filterMember.add(_affair.getTransactorId());
					}
	    			filterMember.add(_affair.getMemberId());
	    		}
	    	}
	    	//4、交换节点回退时的affair
	    	Map<Long, Long> senderAffair = DateSharedWithWorkflowEngineThreadLocal.getAffairDistributeMap();
	    	for(Long key : senderAffair.keySet()){
	    		Long affId = senderAffair.get(key);
	    		//不给回退人自己发消息
	    		if(key != null && key.longValue()!=user.getId().longValue() && !filterMember.contains(key)){
		    		receivers.add(new MessageReceiver(affId, key, messageLinkDone, affId.toString()));
	    			filterMember.add(key);
		    	}
	    	}
	    	
	    	//回退的时候其他影响的节点，比如兄弟节点。
	    	Map<Long, Long> allStepBackAffectAffairMap = DateSharedWithWorkflowEngineThreadLocal.getAllStepBackAffectAffairMap();
	    	for(Long key : allStepBackAffectAffairMap.keySet()){
	    		//不给已发的人重复发
	    		if(!filterMember.contains(key) && user.getId().longValue() != key.longValue()){
	    			Long affairId = allStepBackAffectAffairMap.get(key);
	    			receivers.add(new MessageReceiver(affairId, key));
					filterMember.add(key);
					
					//代理
	        		Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),key);
	        		if(agentId!=null && !filterMember.contains(agentId) && user.getId().longValue()!= agentId.longValue()){
	        			receivers.add(new MessageReceiver(affairId, agentId));
						filterMember.add(agentId);
	        		}
	    		}
	    	}
	        	
	    	int opinionType = 1;
	    	String opinionContent = UserMessageUtil.getComment4Message(signOpinion.getContent());
	    	//回退时 意见加了附件，则回退消息中加附字
	    	/*if(signOpinion.isHasAtt()){
	    		opinionContent += EdocHelper.getOpinionAttStr();
	    	}*/

	    	Integer systemMessageFilterParam = GovdocMessageHelper.getSystemMessageFilterParam(affair).key;
	    	if(affair.getMemberId().longValue() != user.getId().longValue()) {
	    		V3xOrgMember member = getMemberById(affair.getMemberId());
	    		String proxyName = member==null ? "" : member.getName();
			    MessageContent mContent = new MessageContent("edoc.stepback", affair.getSubject(), proxyName ,affair.getApp(), opinionType, opinionContent);
			    mContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
			    if(!opinionContent.contains("代录)")){
				    mContent.add("edoc.agent.deal", user.getName());
			    }
			    mContent.setImportantLevel(importantLevel);
				userMessageManager.sendSystemMessage(mContent, appEnum, affair.getMemberId(), receivers, systemMessageFilterParam);
	        } else {
	        	if(traceFlag) {
	        		List<Long> _hasTraceDataAffair = (List<Long>)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affair");
	            	Long sId =  (Long)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_summaryId");
	     		   	Long aId =  (Long)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affairId");
	            	Integer type =  (Integer)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_traceType");
	            	Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
	     		   	Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
	            	Iterator<MessageReceiver> it = receivers.iterator();
	            	while(it.hasNext()) {
	            		MessageReceiver mr = it.next();
	            		if(_hasTraceDataAffair.contains(mr.getReferenceId())) {
	            			if(Strings.isBlank(mr.getLinkType())) {
	            				if(null != aId) {
	            				    //静态数据连接
	            					new_receivers.add(new MessageReceiver(aId,mr.getReceiverId(),messageLinkTrace,ColOpenFrom.stepBackRecord.name(),aId.toString(),sId.toString(),type+""));
	            				} else {
	            					new_receivers.add(new MessageReceiver(mr.getReferenceId(),mr.getReceiverId(),messageLinkTrace,ColOpenFrom.stepBackRecord.name(),mr.getReferenceId().toString(),summaryId.toString(),type+""));
	            				}
	            			} else {
	            				old_receivers.add(mr);	
	            			}
	            		} else {
	            			old_receivers.add(mr);
	            		}
	            	}
	            	MessageContent mContent = new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent);
	            	mContent.setImportantLevel(importantLevel);
	            	mContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
					userMessageManager.sendSystemMessage(mContent, appEnum, user.getId(), old_receivers, systemMessageFilterParam);
					
					mContent =new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent);
					mContent.add("edoc.summary.cancel.traceview");
					mContent.setImportantLevel(importantLevel);
					mContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource"); 
					userMessageManager.sendSystemMessage(mContent, appEnum, user.getId(), new_receivers, systemMessageFilterParam);
	        	} else {
	        		MessageContent mContent = new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent);
	        		mContent.setImportantLevel(importantLevel);
	        		mContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
	        		userMessageManager.sendSystemMessage(mContent, appEnum, user.getId(), receivers, systemMessageFilterParam);
	        	}
	        }
		} catch(Exception e) {
			LOGGER.error("公文发送回退消息出错", e);
		}
        return true;
    }

	@Override
    @SuppressWarnings("unchecked")
	public void sendAppointStepBackMsg(GovdocDealVO dealVo) throws BusinessException {
		try {
	    	Map<String, Object> msgMap = new HashMap<String, Object>();
	    	msgMap.put("summary", dealVo.getSummary());
	    	msgMap.put("allAvailableAffairs", dealVo.getValidAffairList());
	    	msgMap.put("submitStyle", dealVo.getSubmitStyle());
	    	msgMap.put("selectTargetNodeId", dealVo.getSelectTargetNodeId());
	    	msgMap.put("selectTargetNodeName", dealVo.getSelectTargetNodeName());
	    	msgMap.put("currentAffair", dealVo.getAffair());
	    	msgMap.put("sendAffair", dealVo.getSenderAffair());
	    	
	    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
	    	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	    	UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
	    	EdocSummary summary =  (EdocSummary)msgMap.get("summary");
	    	List<CtpAffair> allAvailableAffairs =  (List<CtpAffair>)msgMap.get("allAvailableAffairs");
	    	String submitStyle =  (String)msgMap.get("submitStyle");
	    	String selectTargetNodeId =  (String)msgMap.get("selectTargetNodeId");
	    	String selectTargetNodeName =  (String)msgMap.get("selectTargetNodeName");
	    	CtpAffair currentAffair =  (CtpAffair)msgMap.get("currentAffair");
	    	CtpAffair sendAffair =  (CtpAffair)msgMap.get("sendAffair");
	    	if (summary == null || Strings.isBlank(selectTargetNodeId))   return;
	    	String messageLinkWait = "message.link.govdoc.waitSend";
	        String messageLinkSent = "message.link.govdoc.sended";
	        String messageLinkDone = "message.link.govdoc.done.detail";
	        String messageLinkPending = "message.link.govdoc.pending";
	        if(GovdocUtil.isGovdocWfOld(currentAffair.getApp(), currentAffair.getSubApp())) {
	        	messageLinkPending = "message.link.edoc.pending";
	        	messageLinkDone = "message.link.edoc.done";
	        	messageLinkSent = "message.link.edoc.sended";
	        	messageLinkWait = "message.link.edoc.waitSend";
            }
	        
	        if(sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
	        		|| sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
	    		messageLinkSent = messageLinkWait;
	    	}
	
	        Long summaryId = summary.getId();
	        V3xOrgMember member = orgManager.getMemberById(currentAffair.getMemberId());
	        
	        //{0}回退公文《{1}》 --用于被退回节点，被影响的待办节点--
	        List<Object> backLabel = new ArrayList<Object>();
	        backLabel.add(0, "edoc.appointStepBack.msgToSend");
	        backLabel.add(1, member.getName());
	        backLabel.add(2, summary.getSubject());
	        backLabel.add(3, null);
	        
	        //{0}回退公文《{1}》，此公文将从你的已办中取消     --用于受影响的已办节点--
	        List<Object> backDoneDisappearLabel = new ArrayList<Object>();
	        backDoneDisappearLabel.add(0, "edoc.appointStepBack.msgToDone");
	        backDoneDisappearLabel.add(1, member.getName());
	        backDoneDisappearLabel.add(2, summary.getSubject());
	        backDoneDisappearLabel.add(3, null);
	        
	        //{0}公文《{1}》，此公文将你的待办中取消
	        // MessageContent mcBackPendingDisappear = new MessageContent(ResourceUtil.getString("collaboration.appointStepBack.msgToPending", m.getName()), summary.getSubject());
	        
	        //{0}回退公文《{1}》至{2}  --用于发退回消息给发起人，跟踪人，督办人--
	        String selectTargetNodeMemberName = selectTargetNodeName;
	        if("start".equals(selectTargetNodeId)){
	            V3xOrgMember sm = orgManager.getMemberById(summary.getStartUserId());
	            selectTargetNodeMemberName = sm.getName();
	        }
	        List<Object> backToMemberLabel = new ArrayList<Object>();
	        backToMemberLabel.add(0, "edoc.appointStepBack.backSendermsgToSend");
	        backToMemberLabel.add(1, member.getName());
	        backToMemberLabel.add(2, summary.getSubject());
	        backToMemberLabel.add(3, selectTargetNodeMemberName);
	        
	        List<CtpAffair> sentAffairs =  new ArrayList<CtpAffair>();
	        List<CtpAffair> doneAffairs =  new ArrayList<CtpAffair>();
	        List<CtpAffair> pendingAffairs =  new ArrayList<CtpAffair>();
	        /** 定义人员过滤器，避免消息重复 **/
	        Set<Long> filterMemberIds = new HashSet<Long>();
	        //提交方式
	        int way = Integer.parseInt(submitStyle);
	        if("start".equals(selectTargetNodeId)) {//指定回退给发起节点
	        	switch (way) {
					case 0:/** 流程重走 **/
						if (Strings.isNotEmpty(allAvailableAffairs)) {
	                        for (CtpAffair ctpAffair : allAvailableAffairs) {
	                            switch (StateEnum.valueOf(ctpAffair.getState())) {
	                                case col_sent:
	                                case col_waitSend:
	                                    filterMemberIds.add(ctpAffair.getMemberId());
	                                    sentAffairs.add(ctpAffair);
	                                    break;
	                                case col_done:
	                                    if(!filterMemberIds.contains(ctpAffair.getMemberId()) && !ctpAffair.getMemberId().equals(ctpAffair.getSenderId())){
	                                        filterMemberIds.add(ctpAffair.getMemberId());
	                                        doneAffairs.add(ctpAffair);
	                                    }
	                                    break;
	                                // 若是直接提交给我待办节点暂存待办下若跟踪可以收到消息提醒
	                                // 流程重走待办需要收到消息
	                                case col_pending:
	                                    if(!filterMemberIds.contains(ctpAffair.getMemberId()) && !ctpAffair.getMemberId().equals(ctpAffair.getSenderId()) ){
	                                        filterMemberIds.add(ctpAffair.getMemberId());
	                                        pendingAffairs.add(ctpAffair);
	                                    }
	                                    break;
	                                default:
	                                    break;
	                            }
	                        }  
	                    }
						/** 指定退回到发起节点，流程重走，给所有已办节点发消息 **/
						GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backDoneDisappearLabel, messageLinkSent, messageLinkDone, doneAffairs, filterMemberIds);
	                    /** 指定退回到发起节点，流程重走，给所有待办/在办节点发消息 **/
						GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkDone, pendingAffairs, filterMemberIds);
	                    break;
					
					case 1:/** 提交回退者 **/
	                    //发起者
	                    if (Strings.isNotEmpty(allAvailableAffairs)) {
	                        for (CtpAffair ctpAffair : allAvailableAffairs) {
	                            switch (StateEnum.valueOf(ctpAffair.getState())) {
	                                case col_sent:
	                                case col_waitSend:
	                                    filterMemberIds.add(ctpAffair.getMemberId());
	                                    sentAffairs.add(ctpAffair);
	                                    break;
	                                default:
	                                    break;
	                            }
	                        }  
	                    }
	                    /** 指定退回到发起节点，提交回退者，给待办/在办跟踪节点发消息 **/
	                    List<CtpAffair> trackAffairs =  getTrackingAffairList(filterMemberIds, summaryId);
	                    GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backToMemberLabel, messageLinkSent, messageLinkPending, trackAffairs, filterMemberIds);
						break;
					
					default:
						break;
	        	}
				/** 指定退回到发起节点，给发起人发送消息 **/
	        	GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkDone, sentAffairs, filterMemberIds);
	            /** 指定退回到发起节点，给督办人发送消息 **/
	        	GovdocMessageHelper.sendMsg4Receivers(userMessageManager, summary, currentAffair, backToMemberLabel, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds);
	            /** 指定退回到发起节点，给当前人的代理人或者被代理人发送消息 **/
	            //sendCurrentAndAgent(userMessageManager, summary,currentAffair,messageLinkDone,filterMemberIds);
	        } else {//指定回退给普通节点
	            //这个地方重新查询了一下，目的是让例如被回退的节点的消息连接是可用的。而不是无效的。
	            allAvailableAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, summaryId);
	            List<CtpAffair> targetAffair = new ArrayList<CtpAffair>();
	            for (CtpAffair ctpAffair : allAvailableAffairs) {
	                if(selectTargetNodeId.equals(String.valueOf(ctpAffair.getActivityId()))){
	                    filterMemberIds.add(ctpAffair.getMemberId());
	                    targetAffair.add(ctpAffair);
	                }
	            }
	            
	            switch (way) {
					case 0:/** 流程重走 **/
						List<MessageReceiver> dones = new ArrayList<MessageReceiver>();
	                    List<MessageReceiver> pendings = new ArrayList<MessageReceiver>();
	                    Map<Long, Long[]>  map = DateSharedWithWorkflowEngineThreadLocal.getAllSepcialStepBackCanceledAffairMap();
	                    for(Long memberId : map.keySet()) {
	                        if(!filterMemberIds.contains(memberId)) {
	                            Long[] value = map.get(memberId);
	                            if(value!= null && value.length == 2) {
	                                Long affairId = value[0];
	                                if(Long.valueOf(StateEnum.col_done.getKey()).equals(value[1])){
	                                    dones.add(new MessageReceiver(affairId, memberId, messageLinkDone,affairId, null));
	                                }else if(Long.valueOf(StateEnum.col_pending.getKey()).equals(value[1])){
	                                    pendings.add(new MessageReceiver(affairId, memberId, messageLinkPending,affairId, null));
	                                }
	                            }
	                        }
	                    }
	                    /** 指定退回到一般节点，流程重走，给受影响的待办/在办节点发消息 **/
	                    GovdocMessageHelper.sendMsg4Receivers(userMessageManager, summary, currentAffair, backLabel, pendings, filterMemberIds);
	                    /** 指定退回到一般节点，流程重走，给受影响的已办节点发消息 **/
	                    GovdocMessageHelper.sendMsg4Receivers(userMessageManager, summary, currentAffair, backDoneDisappearLabel, dones, filterMemberIds);
	                    break;
	                    
					case 1:/** 提交回退者 **/
						break;
	            }
	            
	            /** 指定退回到普通节点，给被退回节点(复合节点可能是多个人)发送消息 **/
	            GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkPending, targetAffair, filterMemberIds);
	            /** 指定退回到一般节点，给待办/在办/已办跟踪节点发消息 **/
	            List<CtpAffair> trackAffairs = getTrackingAffairList(filterMemberIds, summaryId);
	            GovdocMessageHelper.sendMsg4Affairs(userMessageManager, summary, currentAffair, backToMemberLabel, messageLinkSent, messageLinkPending, trackAffairs, filterMemberIds);
	            /** 指定退回到一般节点，给督办人发消息 **/
	            GovdocMessageHelper.sendMsg4Receivers(userMessageManager, summary, currentAffair, backToMemberLabel, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds);
	        }
		} catch(Exception e) {
			LOGGER.error("公文发送指定回退消息出错", e);
		}
    }
    
    @Override
    public Boolean sendStepstopMsg(WorkItem workitem, CtpAffair currentAffair, List<CtpAffair> trackingAndPendingAffairs) throws BusinessException {
    	try {
	        if(currentAffair == null){
	            return false;
	        }
	        if(currentAffair.getSenderId().equals(currentAffair.getMemberId())
	                && (currentAffair.getSubObjectId() == null || currentAffair.getSubObjectId().longValue() == -1)) {//从已发中终止，作为系统自动终止的一个标志
	        	LOGGER.debug("协同自动终止逾期自由流程，不需要再发终止消息!");
	        } else {
	            Integer importantLevel = WFComponentUtil.getImportantLevel(currentAffair);
	            User user = AppContext.getCurrentUser();
	            String forwardMemberId = currentAffair.getForwardMember();
	            String opinionId = String.valueOf(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionId());
	            String opinionContent = UserMessageUtil.getComment4Message(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion());
	            int opinionType = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionHidden() ? 0 : Strings.isBlank(opinionContent) ? -1 : 1;
	            int forwardMemberFlag = 0;
	            String forwardMember = null;
	            if(forwardMemberId != null){
		            V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
					if(fMember != null) {
						forwardMember = fMember.getName();
						forwardMemberFlag = 1;
					}
	            }
	            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	            List<Long> members = new ArrayList<Long>();
	            for (CtpAffair affair : trackingAndPendingAffairs) {
	                //当前处理者不需要收到终止消息提醒
	                if(!user.getId().equals(affair.getMemberId()) && affair.getSenderId().longValue() != affair.getMemberId().longValue() && !affair.isDelete()){
	                    receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.govdoc.done.detail", affair.getId(), opinionId));
	                    members.add(affair.getMemberId());  
	                }
	            }
	            CtpAffair senderAffair = affairManager.getSenderAffair(currentAffair.getObjectId());
	            if(!members.contains(senderAffair.getMemberId())) {
	            	//公文签收流程的发起人affair有is_delete为删除状态，终止时给签收流程发起者发送消息不能链接
		            if(currentAffair.getSubApp().intValue()!=ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {
			        	receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getMemberId(), "message.link.govdoc.done.detail", senderAffair.getId(), opinionId));
		            } else {
		            	receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getMemberId()));
			        }
	            	members.add(senderAffair.getMemberId());
	        	}
	            // 督办者发送消息
	            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), currentAffair.getObjectId());
	            if (superviseDetail != null) {
	                List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
	                for (CtpSupervisor colSupervisor : colSupervisorSet) {
	                    Long colSupervisMemberId = colSupervisor.getSupervisorId();
	                    if (!members.contains(colSupervisMemberId) && !colSupervisMemberId.equals(user.getId())) {
	                    	receivers.add(new MessageReceiver(currentAffair.getId(), colSupervisMemberId));
	                    	members.add(colSupervisMemberId);
	                    }
	                }
	            }
	            List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
	            for(Long[] push :pushMemberIds) {
	                if(!members.contains(push[1])) {
	                    receivers.add(new MessageReceiver(push[0], push[1],"message.link.col.done",push[0], opinionId));
	                    members.add(push[1]);
	                }
	            }
	            
	            String memberName = OrgHelper.showMemberNameOnly(Long.valueOf(user.getId()));
	            V3xOrgMember m = orgManager.getMemberById(currentAffair.getMemberId());
	            if(!user.isAdmin() && !user.getId().equals(currentAffair.getMemberId())) {  //由代理人终止
	                V3xOrgMember member = getMemberById(currentAffair.getMemberId());
	                if(member != null){
	                	MessageContent mContent = new MessageContent("govdoc.terminate", currentAffair.getSubject(), member.getName(), forwardMemberFlag, forwardMember, opinionType, opinionContent, currentAffair.getSubApp());
	                	mContent.add("col.agent.deal", user.getName());
		                mContent.setImportantLevel(currentAffair.getImportantLevel());
		                userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, m.getId(), receivers, importantLevel);
	                }	                	                
	            } else {
	            	MessageContent mContent = new MessageContent("govdoc.terminate", currentAffair.getSubject(),  memberName, forwardMemberFlag, forwardMember, opinionType, opinionContent,currentAffair.getSubApp());
	            	mContent.setImportantLevel(currentAffair.getImportantLevel());
	                userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);                    
	            }
	        }
    	} catch(Exception e) {
			LOGGER.error("公文发送终止消息出错", e);
		}
        return true;
	}
	
    @Override
	public Boolean sendTakeBackMsg(List<CtpAffair> pendingAffairList, CtpAffair affair, Long summaryId) throws BusinessException {
    	try {
			Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
			User user = AppContext.getCurrentUser();
			String userName = "";
			Long userId = null;
			if (user != null) {
				userName = user.getName();
				userId = user.getId();
			}
			if (userId == null) {
				return false;
			}
			
			Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
			V3xOrgMember member = null;
			Long memberId = null;
			Set<Long> utilList = new HashSet<Long>();
	
			Map<Long, Long> allStepBackAffectAffairMap = DateSharedWithWorkflowEngineThreadLocal.getAllStepBackAffectAffairMap();
			if (allStepBackAffectAffairMap != null) {
				for (Long key : allStepBackAffectAffairMap.keySet()) {
					memberId = key;
					if (user.getId().equals(memberId)) {
						continue;
					}
					member = orgManager.getMemberById(memberId);
					if (!utilList.contains(memberId)) {
						utilList.add(memberId);
						receivers1.add(new MessageReceiver(allStepBackAffectAffairMap.get(key), memberId));
					}
				}
			}
			String forwardMemberId = affair.getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
			if (!userId.equals(affair.getMemberId())) {
				member = orgManager.getMemberById(affair.getMemberId());
				String proxyName = member.getName();
				MessageContent mContent = new MessageContent("govdoc.takeback", affair.getSubject(), proxyName, forwardMemberFlag, forwardMember);
				mContent.add("col.agent.deal", userName);
				mContent.setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers1, importantLevel);
			} else {
				MessageContent mContent = new MessageContent("govdoc.takeback", affair.getSubject(), userName, forwardMemberFlag, forwardMember);
				mContent.setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers1, importantLevel);
			}
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
			for (CtpAffair trackingAffair : trackingAffairList) {
				// 当前用户设置了跟踪是不会发消息的
				if (userId != null && userId.equals(trackingAffair.getMemberId())) {
					continue;
				}
				String messageUrl = "message.link.govdoc.done.detail";
				if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
					messageUrl = "message.link.govdoc.waitSend";
				}
				if (!utilList.contains(trackingAffair.getMemberId()) && !trackingAffair.isDelete()) {
					receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(), messageUrl, trackingAffair.getId().toString()));
				}
			}
	
			// 督办人
			CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summaryId);
			if (superviseDetail != null) {
				List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
				for (CtpSupervisor colSupervisor : colSupervisorSet) {
					Long colSupervisMemberId = colSupervisor.getSupervisorId();
					if (!utilList.contains(colSupervisMemberId)) {
						receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, "message.link.govdoc.done.detail", affair.getId()));
						utilList.add(colSupervisMemberId);
					}
				}
			}
			if (userId != null && !userId.equals(affair.getMemberId())) {
				member = orgManager.getMemberById(affair.getMemberId());
				String proxyName = member.getName();
				MessageContent mContent = new MessageContent("govdoc.takeback", affair.getSubject(), proxyName, forwardMemberFlag, forwardMember);
				mContent.add("col.agent.deal", userName);
				mContent.setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel);
			} else {
				MessageContent mContent = new MessageContent("govdoc.takeback", affair.getSubject(), userName, forwardMemberFlag, forwardMember);
				mContent.setImportantLevel(affair.getImportantLevel());
				userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, userId, receivers, importantLevel);
			}
    	} catch(Exception e) {
			LOGGER.error("公文发送取回消息出错", e);
		}
		return true;
	}

    @SuppressWarnings("deprecation")
	@Override
 	public Boolean sendMessage4ModifyBodyOrAtt(EdocSummary summary, Long memberId,int type) throws BusinessException {
    	try {
	 		 User user = AppContext.getCurrentUser();
	         Integer importantLevel = summary.getImportantLevel();
	         Set<MessageReceiver> receivers = getMessageReceiver(user, summary.getId());
	         String forwardMemberId = summary.getForwardMember();
	         int forwardMemberFlag = 0;
	         String forwardMember = null;
	         if(Strings.isNotBlank(forwardMemberId)){
	        	 V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
	 			if(fMember != null) {
	 				forwardMember = fMember.getName();
	 				forwardMemberFlag = 1;
	 			}
	         }                      
	         //代理人处理时发消息
	         if(user.getId() != null && !user.isAdmin() && !user.getId() .equals(memberId)) {
	             V3xOrgMember member = getMemberById(memberId);
	             if(member != null){
	            	 String proxyName = member.getName();
		             //给节点中的人员和督办人员发送消息
		             MessageContent mContent = new MessageContent("col.modifyBodyOrAtt", summary.getSubject(), proxyName, forwardMemberFlag, forwardMember,type);
		             mContent.add("col.agent.deal", user.getName());
		             mContent.setImportantLevel(summary.getImportantLevel());
		             userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
	             }	            
	         } else {
	             //当前人 处理时 发消息
	        	 MessageContent mContent = new MessageContent("col.modifyBodyOrAtt", summary.getSubject(), user.getName(), forwardMemberFlag, forwardMember,type);
	        	 mContent.setImportantLevel(summary.getImportantLevel());
	             userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
	         }
    	} catch(Exception e) {
			LOGGER.error("公文发送修改正文或附件消息出错", e);
		}
         return true;
 	}
    
    @Override
	public void sendMsg4ProcessOverTime(CtpAffair affair, List<MessageReceiver> receivers, List<MessageReceiver> agentReceivers) throws BusinessException {
    	try {
			String forwardMemberId = affair.getForwardMember();
			int forwardMemberFlag = 0;
			String forwardMember = null;
			if (Strings.isNotBlank(forwardMemberId)) {
				V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
				if(fMember != null) {
					forwardMember = fMember.getName();
					forwardMemberFlag = 1;
				}
			}
	
			String msgKey = "process.summary.overTerm.edoc";
			String title = affair.getSubject();
			Integer importantLevel = affair.getImportantLevel();
			Long senderId = affair.getSenderId();
			ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.edoc;
			if (Strings.isNotEmpty(receivers)) {
				MessageContent mContent = MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
				userMessageManager.sendSystemMessage(mContent, appEnum, senderId, receivers, WFComponentUtil.getImportantLevel(affair));
			}
	
			if (Strings.isNotEmpty(agentReceivers)) {
				MessageContent mContent = MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent");
				userMessageManager.sendSystemMessage(mContent, appEnum, senderId, agentReceivers, WFComponentUtil.getImportantLevel(affair));
			}
    	} catch(Exception e) {
			LOGGER.error("公文发送超期消息出错", e);
		}
	}

    @Override
    public Boolean sendSuperviseDelete(WorkItem workitem, List<CtpAffair> affairs) throws BusinessException {
    	try {
	        if(affairs == null || affairs.isEmpty()){
	            return true;
	        }
	        User user = AppContext.getCurrentUser();
	        String userName = "";
	        if (user != null) {
	            userName = user.getName();
	        }
	        Integer importmentLevel = affairs.get(0).getImportantLevel();
	        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        CtpAffair firstAffair = affairs.get(0);
	        for(CtpAffair affair : affairs) {
	            receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId()));
	        }
	        Integer systemMessageFilterParam = GovdocMessageHelper.getSystemMessageFilterParam(firstAffair).key;
	        MessageContent mContent = new MessageContent("edoc.delete", firstAffair.getSubject(), userName, firstAffair.getApp()).setImportantLevel(importmentLevel);
	        userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.valueOf(firstAffair.getApp()),  firstAffair.getSenderId(),  receivers, systemMessageFilterParam);
    	} catch(Exception e) {
			LOGGER.error("公文发送删除督办消息出错", e);
		}
        return true;
    }
    
    @Override
    public void sendPushMessage4SenderNote(Comment comment) throws BusinessException {
    	try {
	    	List<CtpAffair> list = new UniqueList<CtpAffair>();
	        User user = AppContext.getCurrentUser();
	        Long summaryId = comment.getModuleId();
	        List<CtpAffair> affairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, summaryId);
	        if(affairList == null || affairList.size()<1){
	            return;
	        }
	        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        Set<MessageReceiver> receiversAgent = new HashSet<MessageReceiver>();//代理人
	        Set<Long> filterMemberIds = new HashSet<Long>();
	        for(CtpAffair affair : affairList){
	            Long memberId = affair.getMemberId();
	            Long senderId = affair.getSenderId();
	            if(memberId.intValue() == senderId.intValue()){
	                continue;
	            }
	            
	            if(filterMemberIds.contains(memberId)){
	            	continue;
	            }
	            else{
	            	filterMemberIds.add(memberId);
	            }
	            
	            if(affair.getState() == StateEnum.col_pending.key()){
	                receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.govdoc.pending", affair.getId(), comment.getId()));
	            }else{
	                receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.govdoc.done.detail", affair.getId(), comment.getId()));
	            }
	            //判断新增附言发送人员是否为外单位
	            V3xOrgMember dMember = orgManager.getMemberById(memberId);
				if (OrgUtil.isPlatformEntity(dMember)) {
					CtpAffair addAffair = affair;
					addAffair.setSubject(comment.getContent());
					addAffair.setTempleteId(comment.getId());
					addAffair.setAddition(comment.getTitle());
					list.add(addAffair);
				}
	
	            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),affair.getMemberId());
	            if (null != agentId && !affair.getMemberId().equals(agentId)) {
	            	V3xOrgMember  memberAgent = getMemberById(affair.getMemberId());
	            	if(null != memberAgent){
	            		if(affair.getState() == StateEnum.col_pending.key()){
	            			receiversAgent.add(new MessageReceiver(affair.getId(), agentId, "message.link.govdoc.pending", affair.getId(), comment.getId()));
	            		}else{
	            			receiversAgent.add(new MessageReceiver(affair.getId(), agentId, "message.link.govdoc.done.detail", affair.getId(), comment.getId()));
	            		}
	            	}
	            }            
	        }
	        Integer importantLevel = WFComponentUtil.getImportantLevel(affairList.get(0));
	        String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(comment.getContent()));
	    	MessageContent mContent = new MessageContent("edoc.addnote", affairList.get(0).getSubject(), user.getName(),  ApplicationCategoryEnum.edoc.getKey(), opinionContent);
	    	mContent.setImportantLevel(affairList.get(0).getImportantLevel());
	    	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
	        
	        if(!receiversAgent.isEmpty()) {
	        	mContent = new MessageContent("edoc.addnote", affairList.get(0).getSubject(), user.getName(), ApplicationCategoryEnum.edoc.getKey(), opinionContent);
	        	mContent.add("col.agent").setImportantLevel(affairList.get(0).getImportantLevel());
	        	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc.getKey(), user.getId(), receiversAgent, importantLevel);
	        }
    	} catch(Exception e) {
			LOGGER.error("公文发送附言消息出错", e);
		}
    }

    @Override
	public void sendPushMessage4Reply(Comment comment) throws BusinessException {
    	try {
	    	//用于存储提醒外系统人员意见回复事务
	        List<CtpAffair> list = new UniqueList<CtpAffair>();
	        User user = AppContext.getCurrentUser();
	        Long summaryId = comment.getModuleId();
	        CtpCommentAll comAll = DBAgent.get(CtpCommentAll.class, comment.getPid());
	        String memberName = orgManager.getMemberById(comAll.getCreateId()).getName();
	        String commentContent = UserMessageUtil.getComment4Message(Strings.toText(comment.getContent()));
	        int commentType = Strings.isTrue(comment.isHidden()) ? 0 : Strings.isBlank(commentContent) ? -1 : 1;

	        List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
	        
	        EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
	       
	        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
	        trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, user.getId());
	        List<Long[]> pushMemberIds = comment.getPushMessageToMembersList();
	        
	        if(!(comment.isPushMessage() != null && comment.isPushMessage())){
	        	pushMemberIds = new ArrayList<Long[]>();
	        }
	        
	        //不用给任何人提供消息.
	        if (pushMemberIds.isEmpty() && trackingAffairList.isEmpty() && trackingAffairList.size()<=0){
	            return;
	        }
	        //取得消息的接受者。
	        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        Set<Long> members = new HashSet<Long>();
	        
	        String subject = Strings.isBlank(comment.getTitle())? summary.getSubject() : comment.getTitle();
	
	        String messageUrl = "";
	        //定义一个Affair,主要取转发相关的信息.
	        Integer importantLevel = summary.getImportantLevel();
	        String forwardMemberId = summary.getForwardMember();
	        for (CtpAffair taffair : trackingAffairList) {
	            if (taffair.isDelete()) {
	                continue;
	            }
	            if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(taffair.getState())) {
	                messageUrl = "message.link.govdoc.pending";
	            } else {
	                messageUrl = "message.link.govdoc.done.detail";
	            }
	            Long affairId = taffair.getId();
	            Long recieverMemberId = taffair.getMemberId();
	            Long transactorId = taffair.getTransactorId();
	            if (transactorId != null && GovdocHelper.isGovdocProxy(recieverMemberId, transactorId))
	                recieverMemberId = transactorId;
	            if (!recieverMemberId.equals(user.getId()) && !members.contains(recieverMemberId)) {
	                members.add(recieverMemberId);
	                MessageReceiver trackReceiver = new MessageReceiver(affairId, recieverMemberId, messageUrl, affairId, comment.getId());
	                trackReceiver.setTrack(true);
	                if (comAll.getCreateId().equals(recieverMemberId)) {
	                	trackReceiver.setReply(true);
	                }
	                receivers.add(trackReceiver);
	            }
	            //判断人员是不是提醒外系统人员,用于修改正文协同交换提醒
	            V3xOrgMember dMember = orgManager.getMemberById(recieverMemberId);
				if (OrgUtil.isPlatformEntity(dMember)) {
					list.add(taffair);
				}
	        }
	        for (Long[] push : pushMemberIds) {
	            if (!members.contains(push[1]) && !push[1].equals(user.getId())) {
	                MessageReceiver receiver = new MessageReceiver(push[0], push[1], "message.link.govdoc.pending", push[0], comment.getId());
	                receiver.setAt(true);
	                receivers.add(receiver);
	                //判断人员是不是提醒外系统人员,用于修改正文协同交换提醒
		            V3xOrgMember dMember = orgManager.getMemberById(push[1]);
					if (OrgUtil.isPlatformEntity(dMember)) {
						CtpAffair commentAffair =affairManager.get(push[0]);
						commentAffair.setMemberId(push[1]);
						list.add(commentAffair);
					}
	                members.add(push[1]);
	            }
	        }
	        if (receivers.isEmpty()) {
	            return;
	        }
	        int forwardMemberFlag = 0;
	        String forwardMember = null;
	        if (Strings.isNotBlank(forwardMemberId)) {
	            try {
	                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
	                forwardMemberFlag = 1;
	            } catch (Exception e) {
	            	LOGGER.error("", e);
	            }
	        }
	        if(Strings.isNotBlank(comment.getExtAtt2())) {
	        	String proxyName = "";
	        	V3xOrgMember theMember = orgManager.getEntityById(V3xOrgMember.class,comment.getCreateId());
	            if(theMember != null) {
	                proxyName = theMember.getName();
	            }
	            MessageContent mContent = new MessageContent("edoc.reply", subject, proxyName, memberName, forwardMemberFlag, forwardMember,commentType, commentContent);
	            mContent.add("edoc.agent.reply", user.getName());
	            mContent.setImportantLevel(summary.getImportantLevel());
	            userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, comment.getCreateId(), receivers,importantLevel);
	        } else {
	        	MessageContent mContent = new MessageContent("edoc.reply", subject, user.getName(), memberName,forwardMemberFlag, forwardMember,commentType, commentContent);
	        	if(commentType != 0){
		        	mContent.add("govdoc.opinion", "："+ comment.getContent());;
	        	}
	        	mContent.setImportantLevel(summary.getImportantLevel());
	        	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
	        }
    	} catch(Exception e) {
			LOGGER.error("公文发送震荡回复消息出错", e);
		}
    }
    
	@Override
	public void sendMessage4ExchangeFail(Long receiverId,String subject,String messageInfo) throws BusinessException {
		try {
			MessageReceiver receiver = new MessageReceiver(-1L,receiverId);
			MessageContent msgContent = new MessageContent("govdoc.exchange.fail.message",subject,messageInfo);
			V3xOrgMember member = orgManager.getMemberById(receiverId);
			V3xOrgMember adminMember = orgManager.getAdministrator(member.getOrgAccountId());
			userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, adminMember.getId(), receiver);
		} catch(Exception e) {
			LOGGER.error("公文发送交换消息出错", e);
		}
	}
	
	 @Override
	 public Boolean sendMessage4SubProcessRepeal(List<CtpAffair> affairs, EdocSummary summary, CtpSuperviseDetail detail, String repealComment) throws BusinessException {
		 try {
			 String key = "edoc.newflow.callback";
			 User user= AppContext.getCurrentUser();
			 if(null!=detail) {
				 //发送消息 - 给被撤销的督办人
				 this.sendMessage2Supervisor(detail.getId(), summary.getSubject(), key, user.getId(), user.getName(), repealComment, null);
			 }
			 Integer importantLevel = summary.getImportantLevel();
			 Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			 if(affairs != null && affairs.size() > 0){
				 for(CtpAffair affair1 : affairs) {
					 if(affair1.isDelete()) {
						 continue;
					 }
					 if(user.getId().equals(affair1.getMemberId())){continue;}
					 receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
				 }
				 MessageContent mContent = new MessageContent(key, summary.getSubject(), user.getName(), repealComment).setImportantLevel(importantLevel);
				 userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, WFComponentUtil.getImportantLevel(affairs.get(0)));
			 }
		 } catch(Exception e) {
			 LOGGER.error("公文发送督办消息出错", e);
		 }
		 return true;
	 }
    
	/**
     * 获取修改正文和附件的时候需要发送的人
     * @param user
     * @param affairManager
     * @param summary
     * @param receivers1 代理人集合
     * @return
     * @throws BusinessException
     */
    private Set<MessageReceiver> getMessageReceiver(User user, Long summaryId) throws BusinessException {
    	//用于存储提醒外系统人员超期事务
        List<CtpAffair> list = new UniqueList<CtpAffair>();
    	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        //获取当前事项的affair集合
        List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summaryId);
        //节点人和跟踪节点需要发送消息的人员集合
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
        //代理人发消息集合
        List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
        
        for(CtpAffair affair : affairs){
            if(affair.isDelete()) continue;
          //取回的不应该收到消息
            if(Integer.valueOf(StateEnum.col_takeBack.getKey()).equals(affair.getState())){
            	continue;
            }
            //获取代理人
            Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),affair.getMemberId());
            //如果当然用户是代理人，则不给当然代理人发送消息，且需要给被代理人发送消息
            if (user.getId().equals(agentMemberId)){
                if (!receiversMap.containsKey(affair.getMemberId())){
                    receiversMap.put(affair.getMemberId(), new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.govdoc.pending", affair.getId().toString(),""));
                    //判断人员是不是提醒外系统人员,用于修改正文协同交换提醒
		            V3xOrgMember dMember = orgManager.getMemberById(affair.getMemberId());
					if (OrgUtil.isPlatformEntity(dMember)) {
						list.add(affair);
					}
                }
                continue;
            }
            //不给当前操作用户发送消息,如果当前用户有代理人则给代理人发送消息
            if(user.getId().equals(affair.getMemberId())){
                //给代理人发送消息
                if (agentMemberId != null && !receiversMap.containsKey(agentMemberId)) {
                    receivers1.add(new MessageReceiver(affair.getId(),agentMemberId));
                }
                continue;
            }
            String messageUrl = "";
            if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
                messageUrl = "message.link.govdoc.pending";
            } else if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                messageUrl = "message.link.govdoc.waitSend";
            } else {
                messageUrl = "message.link.govdoc.done.detail";
            }
            //给已发节点发送消息
            Long sendId = affair.getSenderId();
            if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()) && !receiversMap.containsKey(affair.getSenderId())){
                receiversMap.put(sendId, new MessageReceiver(affair.getId(), sendId, messageUrl, affair.getId().toString(),""));
                //判断人员是不是提醒外系统人员,用于修改正文协同交换提醒
	            V3xOrgMember dMember = orgManager.getMemberById(sendId);
				if (OrgUtil.isPlatformEntity(dMember)) {
					//对于提醒发起人，标识是提醒发起人
					CtpAffair ocipAffair = affair;
					ocipAffair.setApp(-100);//表明是提醒发起人标识
					list.add(affair);
				}
            } else if(!receiversMap.containsKey(affair.getMemberId())) {
                receiversMap.put(affair.getMemberId(), new MessageReceiver(affair.getId(), affair.getMemberId(), messageUrl, affair.getId().toString()));
                //判断人员是不是提醒外系统人员,用于修改正文协同交换提醒
	            V3xOrgMember dMember = orgManager.getMemberById(affair.getMemberId());
				if (OrgUtil.isPlatformEntity(dMember)) {
					list.add(affair);
				}
                //给代理人发送消息
                if (agentMemberId != null && !receiversMap.containsKey(agentMemberId)) {
                    receivers1.add(new MessageReceiver(affair.getId(),agentMemberId));
                }
            } 
        }
        //督办人
        receivers.addAll(receiversMap.values());
        return receivers;
    }
    
	
	/**
	 * 根据当前事项查询出督办人并添加到消息对象中
	 * @param summaryId
	 * @param affairId 给当前的
	 * @param receiverIds
	 * @return
	 * @throws BusinessException
	 */
	private Map<Long, MessageReceiver> getSuperviseBySummaryId(Long summaryId, Long affairId, Set<Long> receiverIds) throws BusinessException {
		// 督办人
		Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
		CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summaryId);
		if (superviseDetail != null) {
			List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
			for (CtpSupervisor colSupervisor : colSupervisorSet) {
				Long colSupervisMemberId = colSupervisor.getSupervisorId();
				// 只给督办人发送消息，不给督办人的代理人发送消息。（见测试用例）
				if (!receiverIds.contains(colSupervisMemberId) && !receiversMap.containsKey(colSupervisMemberId)) {
					receiversMap.put(colSupervisMemberId, new MessageReceiver(affairId, colSupervisMemberId, "message.link.govdoc.done.detail", affairId.toString()));
				}
			}
		}
		return receiversMap;
	}

    private V3xOrgMember getMemberById(Long memberId) {
        V3xOrgMember member = null;
        try {
            member = orgManager.getEntityById(V3xOrgMember.class, memberId);
        } catch (BusinessException e) {
            LOGGER.error("获取协同消息提醒对应人员失败", e);
            return null;
        }
        return member;
    }
	
	// 查找哪些设置了部分跟踪但是没有跟踪当前用户的Affair,从trackAffairs中移除，然后返回跟踪事项列表
	private List<CtpAffair> getTrackAffairExcludePart(List<CtpAffair> trackAffairs, List<CtpTrackMember> trackMembers, Long currentMemberId) {
		for (Iterator<CtpAffair> it = trackAffairs.iterator(); it.hasNext();) {
			CtpAffair affair = it.next();
			boolean partTrack = Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack()); // 设置的是部分跟踪
			boolean isTrackCurrentMemebr = false; // 设置部分跟踪的时候是否跟踪了当前的用户
			for (CtpTrackMember colTrackMember : trackMembers) {
				if (affair.getId().equals(colTrackMember.getAffairId())) {
					partTrack = true;
					if (colTrackMember.getTrackMemberId().equals(currentMemberId)) {
						isTrackCurrentMemebr = true;
					}
				}
			}
			// 设置了部分跟踪但是没有跟踪当前用户的
			if (partTrack && !isTrackCurrentMemebr) {
				it.remove();
			}
		}
		return trackAffairs;
	}

    /**
     * 给当前人的代理人或者被代理人发送消息
     * @param summary
     * @param affair
     * @param messageLinkDone
     * @param filterMemberIds
     */
    @SuppressWarnings("unused")
	private void sendCurrentAndAgent(EdocSummary summary,CtpAffair affair,String messageLinkDone,Set<Long> filterMemberIds) throws BusinessException{
        User user = AppContext.getCurrentUser();
        //给被代理人发送消息
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        //给代理人发消息
        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
        //判断是否是代理人
        boolean hasProxy = false;
        Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),affair.getMemberId());
        V3xOrgMember member = null;
        if (!user.getId().equals(affair.getMemberId()) && !filterMemberIds.contains(affair.getMemberId()) && agentId!=null) {
            hasProxy = true;
            member = getMemberById(affair.getMemberId());
            receivers.add(new MessageReceiver(affair.getId(),affair.getMemberId(), messageLinkDone,affair.getId(), ""));
            filterMemberIds.add(affair.getMemberId());
        }
        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if(Strings.isNotBlank(forwardMemberId)){
        	V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
			if(fMember != null) {
				forwardMember = fMember.getName();
				forwardMemberFlag = 1;
			}
        }
        //代理人处理,给被代理人发送消息
        if(hasProxy) {
        	if(member != null){
        		MessageContent mContent = new MessageContent("collaboration.appointStepBack.msgToSend", member.getName(), affair.getSubject(),forwardMemberFlag, forwardMember);
            	mContent.add("col.agent.reply", user.getName());
            	mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getSenderId(), receivers, GovdocHelper.getImportantLevel(summary));   
        	}        	
        } else if (agentId!=null && !filterMemberIds.contains(agentId)) { //给当前人的代理人发送消息
            agentReceivers.add(new MessageReceiver(affair.getId(),agentId, messageLinkDone,affair.getId(), null));
            filterMemberIds.add(agentId);
            MessageContent mContent = new MessageContent("collaboration.appointStepBack.msgToSend",user.getName(), affair.getSubject(),forwardMemberFlag, forwardMember);
            mContent.add("col.agent");
            mContent.setImportantLevel(affair.getImportantLevel());
            userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getSenderId(), agentReceivers, GovdocHelper.getImportantLevel(summary));
        }
    }
    
	private List<MessageReceiver> getSuperviseReceiver(Long summaryId,Set<Long> filterMembers,CtpAffair senderAffair) throws BusinessException{
        List<MessageReceiver> receivers = new  ArrayList<MessageReceiver>();
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
        if(superviseDetail != null) {
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!filterMembers.contains(colSupervisMemberId)) {
                    filterMembers.add(colSupervisMemberId);
                    receivers.add(new MessageReceiver(senderAffair.getId(), colSupervisMemberId,"message.link.col.supervise", summaryId));
                }
            }
        }
        return receivers;
    }
    
    private List<CtpAffair> getTrackingAffairList(Set<Long> filterMemberIds,Long summaryId) throws BusinessException {
        // 设置了获取跟踪的affair
        List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, AppContext.currentUserId());
        for(Iterator<CtpAffair> it = trackingAffairList.iterator();it.hasNext();){
            CtpAffair a = it.next();
            if(!filterMemberIds.contains(a.getMemberId())){
                filterMemberIds.add(a.getMemberId());
            } else {
                it.remove();
            }
        }
        return trackingAffairList;
    }
    
	/**
     * 获取跟踪人消息接收人员
     * @param user
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    private Set<MessageReceiver> getTrackReceivers(User user, Long summaryId) throws BusinessException {
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summaryId);
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = "message.link.govdoc.done.detail";
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
                messageUrl = "message.link.govdoc.waitSend";
            } 
            if (!trackingAffair.isDelete()) {
                receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
            }
        }
        return receivers;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void sendOperationTypeMessage(String messageDataListJSON, EdocSummary summary, CtpAffair affair,Long commentId) throws BusinessException {
		try {
			List<Map<String, Object>> newMessageDataList = null;
			if (null == messageDataListJSON || "".equals(messageDataListJSON.trim())) {
				newMessageDataList = new ArrayList<Map<String, Object>>();
			} else {
				newMessageDataList = (List<Map<String, Object>>) JSONUtil.parseJSONString(messageDataListJSON);
			}
			User user = AppContext.getCurrentUser();
			for (Map<String, Object> map : newMessageDataList) {
				if (user.getId().toString().equals(map.get("handlerId").toString())) {
					String operationType = map.get("operationType").toString();
					String partyNames = map.get("partyNames").toString();
					List<String> partyNameList = Arrays.asList(partyNames.split("[,]"));
					String processLogParam = map.get("processLogParam").toString();
	                if(commentId == null){
	                    if("insertPeople".equals(operationType)) {//加签
	                    	this.insertPeopleMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertPeople, processLogParam);
	                    } else if("deletePeople".equals(operationType)) {//减签
	                        this.deletePeopleMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.deletePeople, processLogParam);
	                    } else if("colAssign".equals(operationType)) {//当前会签
	                        this.colAssignMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.colAssign, processLogParam);
	                    } else if("addInform".equals(operationType)) {
	                         this.addInformMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.inform, processLogParam);
	                    } else if("addPassRead".equals(operationType)) {//传阅
	                    	this.addPassReadMessage(partyNameList, summary, affair);
	                         govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.passRound, processLogParam);
	                    } else if("addMoreSign".equals(operationType)) {//多级会签
	                    	this.moreSignMessage(partyNameList, summary, affair);
	                    	govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.addMoreSign, processLogParam);
	                    } else if("insertCustomDealWith".equals(operationType)) {//续办
	                    	this.insertCustomDealWithMessage(partyNameList, summary, affair);
	                    	govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertCustomDealWith, processLogParam);
	                    }
	                } else {
	                    if("insertPeople".equals(operationType)) {//加签
	                    	this.insertPeopleMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertPeople,commentId, processLogParam);
	                    } else if("deletePeople".equals(operationType)) {//减签
	                        this.deletePeopleMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.deletePeople, commentId,processLogParam);
	                    } else if("colAssign".equals(operationType)) {//当前会签
	                        this.colAssignMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.colAssign,commentId, processLogParam);
	                    } else if("addInform".equals(operationType)) {
	                         this.addInformMessage(partyNameList, summary, affair);
	                        govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.inform,commentId, processLogParam);
	                    } else if("addPassRead".equals(operationType)) {//传阅
	                    	this.addPassReadMessage(partyNameList, summary, affair);
	                         govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.passRound,commentId, processLogParam);
	                    } else if("addMoreSign".equals(operationType)) {//多级会签
	                    	this.moreSignMessage(partyNameList, summary, affair);
	                    	govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.addMoreSign, commentId,processLogParam);
	                    } else if("insertCustomDealWith".equals(operationType)) {//续办
	                    	this.insertCustomDealWithMessage(partyNameList, summary, affair);
	                    	govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertCustomDealWith, commentId,processLogParam);
	                    }
	                }
				}
			}
		} catch(Exception e) {
			LOGGER.error("公文发送流程变更消息出错", e);
		}
	}
	   
    //加签消息提醒
    private Boolean insertPeopleMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
    	if(summary == null || affair == null){
    		return false;
    	}
    	String messageUrlWait = "message.link.govdoc.waitSend";
    	String messageUrlDone = "message.link.govdoc.done.detail";
    	String messageUrlSupervise = "message.link.govdoc.supervise";
        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageUrlDone = "message.link.edoc.done";
        	messageUrlWait = "message.link.edoc.waitSend";
        	messageUrlSupervise = "message.link.edoc.supervise.detail";
        }

        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        
        User user = AppContext.getCurrentUser();
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summary.getId());
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = messageUrlDone;
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
            	messageUrl = messageUrlWait;
            } 
            if (!trackingAffair.isDelete()) {
            	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
            }
        }
        //督办人
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
        if(superviseDetail != null) {
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!members.contains(colSupervisMemberId)) {
                    receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, messageUrlSupervise, summary.getId()));
                }
            }
        }
        if(!user.getId().equals(affair.getMemberId())) {
            V3xOrgMember member = null;
            member = getMemberById(affair.getMemberId() );
            if(member != null){
            	String proxyName = member.getName();
                MessageContent mContent = new MessageContent("edoc.addAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
                mContent.add("edoc.agent.deal", user.getName());
                mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getMemberId() , receivers, importantLevel);
            }            
        } else {
        	MessageContent mContent = new MessageContent("edoc.addAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
        	mContent.setImportantLevel(affair.getImportantLevel());
            userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
        }
        return true;
    }
    
    //减签消息提醒
    private Boolean deletePeopleMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
    	if(summary == null || affair == null) {
    		return false;
    	}
    	String messageLinkWait = "message.link.govdoc.waitSend";
    	String messageLinkDone = "message.link.govdoc.done.detail";
    	String messageUrlSupervise = "message.link.govdoc.supervise";
        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageLinkWait = "message.link.edoc.waitSend";
        	messageLinkDone = "message.link.edoc.done";
        	messageUrlSupervise = "message.link.edoc.supervise.detail";
        }
        
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summary.getId());
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = messageLinkDone;
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
                messageUrl = messageLinkWait;
            } 
            if (!trackingAffair.isDelete()) {
            	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
            }
        }
        
        //督办人
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
        if(superviseDetail != null){
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!members.contains(colSupervisMemberId)) {
                    receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, messageUrlSupervise, summary.getId()));
                }
            }
        }
        
        if(!user.getId().equals(affair.getMemberId())) {
            V3xOrgMember member = getMemberById(affair.getMemberId());
            if(member != null){
            	String proxyName = member.getName();
                MessageContent mContent = new MessageContent("edoc.decreaseAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),  ApplicationCategoryEnum.edoc.getKey());
                mContent.add("edoc.agent.deal", user.getName());
                mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel);
            }         
        } else {
        	MessageContent mContent = new MessageContent("edoc.decreaseAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
        	mContent.setImportantLevel(affair.getImportantLevel());
        	userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
        }
        return true;
    }
    
    //会签消息提醒
    private Boolean colAssignMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
    	if(summary == null || affair == null){
    		return false;
    	}
    	String messageLinkWait = "message.link.govdoc.waitSend";
    	String messageLinkDone = "message.link.govdoc.done.detail";
        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageLinkWait = "message.link.edoc.waitSend";
        	messageLinkDone = "message.link.edoc.done";
        }
        
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        
        //跟踪的人
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summary.getId());
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = messageLinkDone;
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
                messageUrl = messageLinkWait;
            }
            
            if (!trackingAffair.isDelete()) {
            	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(), messageUrl, trackingAffair.getId().toString()));
            }
        }
        //督办人
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
        if(superviseDetail != null){
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!members.contains(colSupervisMemberId)) {
                    receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, messageLinkDone, affair.getId()));
                }
            }
        }
        
        if(!user.getId().equals(affair.getMemberId())) {
            V3xOrgMember member = getMemberById(affair.getMemberId());
            if(member != null){
            	String proxyName = member.getName();
                MessageContent mContent = new MessageContent("edoc.colAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
                mContent.add("edoc.agent.deal", user.getName());
                mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel);
            }            
        } else {
        	MessageContent mContent = new MessageContent("edoc.colAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
        	mContent.setImportantLevel(affair.getImportantLevel());
            userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
        }
        return true;
    }
    
    //知会消息提醒
    private Boolean addInformMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
    	if(summary == null || affair == null){
    		return false;
    	}
    	String messageUrlWait = "message.link.govdoc.waitSend";
    	String messageUrlDone = "message.link.govdoc.done.detail";
        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageUrlDone = "message.link.edoc.done";
        	messageUrlWait = "message.link.edoc.waitSend";
        }
        
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summary.getId());
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = messageUrlDone;
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
            	messageUrl = messageUrlWait;
            }
            if (!trackingAffair.isDelete()) {
            	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
            }
        }
        
        //督办人
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
        if(superviseDetail != null){
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!members.contains(colSupervisMemberId)) {
                    receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, messageUrlDone, affair.getId()));
                }
            }
        }
        if(!user.getId().equals(affair.getMemberId())){
            V3xOrgMember member = null;
            member = getMemberById(affair.getMemberId());
            if(member != null){
            	String proxyName = member.getName();
                MessageContent mContent = new MessageContent("edoc.addInform", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
                mContent.add("edoc.agent.deal", user.getName());
                mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel);
            }            
        } else {
        	MessageContent mContent = new MessageContent("edoc.addInform", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
        	mContent.setImportantLevel(affair.getImportantLevel());
        	userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
        }

        return true;
    }
    
    //传阅消息提醒
 	private  Boolean addPassReadMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
 		User user = AppContext.getCurrentUser();
 		Integer importantLevel = WFComponentUtil.getImportantLevel(affair);

 		if (partyNames != null) {
 			String messageUrlDone = "message.link.govdoc.done.detail";
 	        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
 	        	messageUrlDone = "message.link.edoc.done";
 	        }
 	        
         	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
             List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
             List<Long> memberIds = new ArrayList<Long>();
             for (CtpAffair trackingAffair : trackingAffairList) {
             	 if(!memberIds.contains(trackingAffair.getMemberId()) && trackingAffair.getMemberId().longValue() != user.getId().longValue()) {
             		 memberIds.add(trackingAffair.getMemberId());
             		 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(), messageUrlDone, trackingAffair.getId().toString()));
             	 }
             }
             if(affair.getMemberId().longValue() != user.getId().longValue()) {
                 V3xOrgMember member = getMemberById(affair.getMemberId());
                 if(member != null){
                	 String proxyName = member.getName();
                     MessageContent mContent = new MessageContent("edoc.addPassRead", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),  ApplicationCategoryEnum.edoc.getKey());
                     mContent.add("edoc.agent.deal", user.getName());
                     mContent.setImportantLevel(affair.getImportantLevel());
                     userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel);                 
                 }                 
             } else {
            	MessageContent mContent = new MessageContent("edoc.addPassRead", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
            	mContent.setImportantLevel(affair.getImportantLevel());
            	userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
             }
         }

 		return true;
 	}
 	
	//多级会签消息提醒
	private  Boolean moreSignMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
		User user = AppContext.getCurrentUser();
		
    	String messageUrlDone = "message.link.govdoc.done.detail";
        if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageUrlDone = "message.link.edoc.done";
        }
        
		Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
		
        if (partyNames != null) {
        	List<Long> msgMemberIdList = new ArrayList<Long>();
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
            for (CtpAffair trackingAffair : trackingAffairList) {
            	if(!msgMemberIdList.contains(trackingAffair.getMemberId())) {
            		msgMemberIdList.add(trackingAffair.getMemberId());
            		receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(), messageUrlDone, trackingAffair.getId().toString()));
            	}
            }
            msgMemberIdList.add(affair.getMemberId());
            if(affair.getMemberId().longValue() != user.getId().longValue()) {
                V3xOrgMember member = getMemberById(affair.getMemberId());
                if(member != null){
                	String proxyName = member.getName();
                    MessageContent mContent = new MessageContent("edoc.addMoreAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
                    mContent.add("edoc.agent.deal", user.getName());
                    mContent.setImportantLevel(affair.getImportantLevel());
                    userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, affair.getMemberId(), receivers, importantLevel); 
                }                                
            } else { 
            	MessageContent mContent = new MessageContent("edoc.addMoreAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
            	mContent.setImportantLevel(affair.getImportantLevel());
            	userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
            }
        }
		return true;
	}
	
    //加签消息提醒
    private Boolean insertCustomDealWithMessage(List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException {
    	if(summary == null || affair == null){
    		return false;
    	}
    	String messageUrlWait = "message.link.govdoc.waitSend";
    	String messageUrlDone = "message.link.govdoc.done.detail";
    	String messageUrlSupervise = "message.link.govdoc.supervise";
    	if(GovdocUtil.isGovdocWfOld(affair.getApp(), affair.getSubApp())) {
        	messageUrlDone = "message.link.edoc.done";
        	messageUrlWait = "message.link.edoc.waitSend";
        	messageUrlSupervise = "message.link.edoc.supervise.detail";
        }
        
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        
        User user = AppContext.getCurrentUser();
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(members, summary.getId());
        Boolean pishiFlag = false;
        Map<String, Object> map = AffairUtil.getExtProperty(affair);
        if(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null){
        	pishiFlag = true;
        }
        for (CtpAffair trackingAffair : trackingAffairList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if(user.getId().equals(trackingAffair.getMemberId()) || trackingAffair.isDelete()){
                continue;
            }
            String messageUrl = messageUrlDone;
            if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
            	messageUrl = messageUrlWait;
            }
            if (!trackingAffair.isDelete()) {
            	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
            }
        }

        //督办人
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
        if(superviseDetail != null){
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!members.contains(colSupervisMemberId)) {
                    receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, messageUrlSupervise, summary.getId()));
                }
            }
        }
        if(!user.getId().equals(affair.getMemberId())) {
            V3xOrgMember member = getMemberById(affair.getMemberId() );
            if(member != null){
            	String proxyName = member.getName();
                MessageContent mContent = new MessageContent("edoc.customDealWith", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
                if(pishiFlag){
                	mContent.add("edoc.pishi.deal", user.getName());
                }else{
                    mContent.add("edoc.agent.deal", user.getName());
                }
                mContent.setImportantLevel(affair.getImportantLevel());
                userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, affair.getMemberId() , receivers, importantLevel);
            }   
        } else {
        	MessageContent mContent = new MessageContent("edoc.customDealWith", summary.getSubject(), user.getName(),  StringUtils.join(partyNames.iterator(), ","), ApplicationCategoryEnum.edoc.getKey());
        	mContent.setImportantLevel(affair.getImportantLevel());
        	userMessageManager.sendSystemMessage(mContent,  ApplicationCategoryEnum.edoc, user.getId(), receivers, importantLevel);
        }
        return true;
    }
	
   
    private void sendMessage2Supervisor(Long superviseId, String summarySubject, String messageKey, long userId, String userName, String repealComment, String forwardMemberId) throws BusinessException {
		List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
		List<CtpSupervisor> colSupervisors = superviseManager.getSupervisors(superviseId);
		for (CtpSupervisor colSupervisor : colSupervisors) {
			MessageReceiver receiver = new MessageReceiver(superviseId, colSupervisor.getSupervisorId());
			receivers.add(receiver);
		}
		MessageContent msgContent = null;
		int forwardMemberFlag = 0;
		String forwardMember = null;
		if (Strings.isNotBlank(forwardMemberId)) {
			V3xOrgMember fMember = getMemberById(Long.parseLong(forwardMemberId));
			if(fMember != null) {
				forwardMember = fMember.getName();
				forwardMemberFlag = 1;
			}
		}
		if ("collaboration.summary.cancel".equals(messageKey)) {
			int messageFlag = 0;
			if (Strings.isNotBlank(repealComment)) {
				messageFlag = 1;
			}
			if (repealComment == null) {
				repealComment = "";
			}
			msgContent = new MessageContent(messageKey, summarySubject, userName, repealComment, forwardMemberFlag, forwardMember, messageFlag);
		} else if ("col.stepback".equals(messageKey)) {
			int messageFlag = 0;
			if (Strings.isNotBlank(repealComment)) {
				messageFlag = 1;
			}
			msgContent = new MessageContent(messageKey, summarySubject, userName, forwardMemberFlag, forwardMember, repealComment, messageFlag);
		} else {
			msgContent = new MessageContent(messageKey, summarySubject, userName, repealComment);
		}
		userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, userId, receivers);
	}
    
    @Override
    public void sendCommentAddPraiseMsg(GovdocBaseVO baseVo) throws BusinessException {
    	String _title = baseVo.getSummary().getSubject();
    	CtpCommentAll c = baseVo.getCommentAll();
    	V3xOrgMember _member = baseVo.getMember();
    	User sender = baseVo.getCurrentUser();
    	String memberName = _member == null ? "" : _member.getName();
		MessageContent mc = new MessageContent("collaboration.opinion.deal.praise", sender.getName(), _title.replaceAll("&nbsp;", " "), memberName);
		Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
		MessageReceiver mr = new MessageReceiver(c.getAffairId(), c.getCreateId(), "message.link.govdoc.pending", c.getAffairId(), c.getId());
		receivers.add(mr);
		userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.edoc, sender.getId(), receivers);
    }
    
    /**
     * 给督办人员和当前节点所有人发消息
     * @param summaryId
     * @param memberId
     * @param activityId
     * @param message
     * @throws BusinessException
     */
	public void sendSubmitConfirmMessage(Long summaryId, Long memberId, Long activityId, String message) throws  BusinessException{
	    Long userId = AppContext.currentUserId();

        //督办人
        List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId,null);
        trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, memberId);
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        if(Strings.isNotEmpty(trackingAffairList)){
            for(CtpAffair affair : trackingAffairList){
                receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), ""));
            }
        }

        //当前节点所有人
        List<CtpAffair> currentNodeMmebers = affairManager.getAffairsByObjectIdAndNodeId(summaryId, activityId);
        if(Strings.isNotEmpty(currentNodeMmebers)){
            for(CtpAffair affair : currentNodeMmebers){
                receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), ""));
            }
        }

        if(Strings.isNotEmpty(receivers)){
            userMessageManager.sendSystemMessage(new MessageContent(message), ApplicationCategoryEnum.edoc, userId, receivers);
        }
    }
    
    public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setTrackManager(CtpTrackMemberManager trackManager) {
		this.trackManager = trackManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}

}
