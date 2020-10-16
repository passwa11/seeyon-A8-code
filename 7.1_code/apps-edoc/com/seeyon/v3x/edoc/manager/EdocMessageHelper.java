package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

import net.joinwork.bpm.engine.wapi.WorkItem;


public class EdocMessageHelper {
    
	private final static Log LOGGER = LogFactory.getLog(EdocMessageHelper.class);
	
	
	/**
	 * 查找哪些设置了部分跟踪但是没有跟踪当前用户的Affair,从trackAffairs中移除，然后返回跟踪事项列表
	 * 
	 * @param trackAffairs
	 * @param trackMembers
	 * @param currentMemberId
	 * @return
	 *
	 */
	public static List<CtpAffair> getTrackAffairExcludePart(List<CtpAffair> trackAffairs,List<CtpTrackMember> trackMembers,Long currentMemberId){
		
		for(Iterator<CtpAffair> it =trackAffairs.iterator();it.hasNext();){
			CtpAffair affair = it.next();
			boolean partTrack = Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack()); //设置的是部分跟踪
			boolean isTrackCurrentMemebr  = false; //设置部分跟踪的时候是否跟踪了当前的用户
			for(CtpTrackMember colTrackMember:trackMembers){
				if(affair.getId().equals(colTrackMember.getAffairId())){
					partTrack = true;
					if(colTrackMember.getTrackMemberId().equals(currentMemberId)){
						isTrackCurrentMemebr = true;
					}
				}
			}
			//设置了部分跟踪但是没有跟踪当前用户的
			if(partTrack && !isTrackCurrentMemebr){ 
				it.remove();
			}
		}
		return trackAffairs;
	}
	
	//工作项完成消息提醒
	public static Boolean workitemFinishedMessage(AffairManager affairManager, OrgManager orgManager, 
			EdocManager edocManager, UserMessageManager userMessageManager, CtpAffair affair, Long summaryId,
			    boolean isHasAtt){
		User user = AppContext.getCurrentUser();

        V3xOrgMember theMember = null;
        theMember = getMemberById(orgManager, affair.getMemberId());
        String name = theMember != null ? theMember.getName() : null;
        List<CtpAffair> trackingAffairLists = null;
        Boolean pishiFlag = false;
        Map<String, Object> map = AffairUtil.getExtProperty(affair);
        if(map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null){
        	pishiFlag = true;
        }
        try {
            trackingAffairLists = affairManager.getValidTrackAffairs(summaryId);
        } catch (BusinessException e1) {
            LOGGER.error("", e1);
        }
        List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
        if(trackingAffairLists.size()<=0 && pushMemberIds.isEmpty()){return true;}
        //yangzd 过滤掉重复的信息发送------发起人和处理人存在同一节点，都设置了跟踪时，会收到重复的系统消息22613
        List<CtpAffair> trackingAffairList=new ArrayList<CtpAffair>();
        trackingAffairList.addAll(trackingAffairLists);
        List<CtpTrackMember> trackMembers;
		try {
			trackMembers = edocManager.getColTrackMembersByObjectIdAndTrackMemberId(summaryId,null);
			//if(trackMembers != null && trackMembers.size() >0 ){
	            trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers,affair.getMemberId());
	        //}
	        if(trackingAffairList.isEmpty() && pushMemberIds.isEmpty()){
	        	return true;
	        }
		} catch (BusinessException e1) {
			LOGGER.error("", e1);
		}
        String opinionContent = UserMessageUtil.getComment4Message(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion());
        if(isHasAtt){
            opinionContent += EdocHelper.getOpinionAttStr();
        }
        int opinionAttitude = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionAttitude();
        int opinionType = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionHidden() ? 0 : Strings.isBlank(opinionContent) ? -1 : opinionAttitude == -1 ? 1: 2;
        
        //yangzd
        try{
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        	Set <Long> members = new HashSet<Long>();
	        for(CtpAffair affair1 : trackingAffairList){
	        	//设置了跟踪也不给自己发信息。 31509
	        	if(affair1.getMemberId().longValue()==user.getId()) continue;
	        
	            Long affairId = affair1.getId();
	            Long recieverMemberId = affair1.getMemberId();
		    	Long transactorId  =  affair1.getTransactorId();
		    	if(transactorId!=null && isEdocProxy(recieverMemberId,transactorId))
		    		recieverMemberId = transactorId;
		    	if(!members.contains(recieverMemberId)){
		    		members.add(recieverMemberId);
		    		receivers.add(new MessageReceiver(affairId, recieverMemberId,"message.link.edoc.done",affairId.toString()));
		    	}
	        }
	        for(Long[] push :pushMemberIds ){
	        	if(!members.contains(push[1])){
	        		receivers.add(new MessageReceiver(push[0], push[1],"message.link.edoc.done",push[0]));
	        	}
	        }
	        ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
	        Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
	        if(affair.getMemberId().longValue() != user.getId().longValue() && !pishiFlag){	
		    	receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(),"message.link.edoc.done",affair.getId()));
				String proxyName = user.getName();
				userMessageManager.sendSystemMessage(new MessageContent("edoc.deal", theMember.getName(),affair.getSubject(),affair.getApp(), opinionType, opinionContent, opinionAttitude, -1).add("edoc.agent.deal", proxyName).setImportantLevel(affair.getImportantLevel()), appEnum, theMember.getId(), receivers,systemMessageFilterParam);
		    }else{
		     	userMessageManager.sendSystemMessage(new MessageContent("edoc.deal",name,affair.getSubject(),affair.getApp(), opinionType, opinionContent, opinionAttitude, -1).setImportantLevel(affair.getImportantLevel()), appEnum,theMember.getId(),receivers,systemMessageFilterParam);
		    }
        } catch (Exception e) {
        	LOGGER.error("", e);
        }
		return true;
	}
	
	/**
     * 获取系统消息类型标识位 枚举
     * @param summary
     * @return MessageMark
     */
    public static EdocMessageFilterParamEnum getSystemMessageFilterParam(EdocSummary summary) {
        return getSystemMessageParam(null, summary.getEdocType(), Strings.isBlank(summary.getUrgentLevel()) ? null : Integer.valueOf(summary.getUrgentLevel()));
    }
    
    /**
     * 获取系统消息类型标识位 枚举
     * @param summary
     * @return MessageMark
     */
    public static EdocMessageFilterParamEnum getSystemMessageFilterParam(CtpAffair affair) {
        return getSystemMessageParam(affair.getApp(), -1, affair.getImportantLevel());
    }
    
    public static EdocMessageFilterParamEnum getSystemMessageFilterParam(Integer appEnum, Integer importantLevel) {
        return getSystemMessageParam(appEnum, -1, importantLevel);
    }
    
    /**
     * @param appEnum 当前参数与后面2个参数互斥，appEnum参数优先。
     * @param iEdocType 公文类型，已办根据summary查询消息用到
     * @param importantLevel 重要程度
     * @return 消息枚举
     */
    private static EdocMessageFilterParamEnum getSystemMessageParam(Integer appEnum, int iEdocType, Integer importantLevel) {
        if (appEnum != null) {
            if (Integer.valueOf(ApplicationCategoryEnum.edocRec.getKey()).equals(appEnum)) {
                iEdocType = EdocEnum.edocType.recEdoc.ordinal();
            } else if (Integer.valueOf(ApplicationCategoryEnum.edocSign.getKey()).equals(appEnum)) {
                iEdocType = EdocEnum.edocType.signReport.ordinal();
            }else{
                iEdocType = EdocEnum.edocType.sendEdoc.ordinal();
            }
        }
        
        if (iEdocType == EdocEnum.edocType.sendEdoc.ordinal()) {
            if (importantLevel == null) {
                return EdocMessageFilterParamEnum.sendQita;
            } else {
                switch (importantLevel) {
                    case 1:
                        return EdocMessageFilterParamEnum.sendPutong;
                    case 2:
                        return EdocMessageFilterParamEnum.sendPingji;
                    case 3:
                        return EdocMessageFilterParamEnum.sendJiaji;
                    case 4:
                        return EdocMessageFilterParamEnum.sendTeji;
                    case 5:
                        return EdocMessageFilterParamEnum.sendTeti;
                    default:
                        return EdocMessageFilterParamEnum.sendQita;
                }
            }
        } else if (iEdocType == EdocEnum.edocType.recEdoc.ordinal()) {
            if (importantLevel == null) {
                return EdocMessageFilterParamEnum.recQita;
            } else {
                switch (importantLevel) {
                    case 1:
                        return EdocMessageFilterParamEnum.recPutong;
                    case 2:
                        return EdocMessageFilterParamEnum.recPingji;
                    case 3:
                        return EdocMessageFilterParamEnum.recJiaji;
                    case 4:
                        return EdocMessageFilterParamEnum.recTeji;
                    case 5:
                        return EdocMessageFilterParamEnum.recTeti;
                    default:
                        return EdocMessageFilterParamEnum.recQita;
                }
            }

        } else if (iEdocType == EdocEnum.edocType.signReport.ordinal()) {
            if (importantLevel == null) {
                return EdocMessageFilterParamEnum.signQita;
            } else {
                switch (importantLevel) {
                    case 1:
                        return EdocMessageFilterParamEnum.signPutong;
                    case 2:
                        return EdocMessageFilterParamEnum.signPingji;
                    case 3:
                        return EdocMessageFilterParamEnum.signJiaji;
                    case 4:
                        return EdocMessageFilterParamEnum.signTeji;
                    case 5:
                        return EdocMessageFilterParamEnum.signTeti;
                    default:
                        return EdocMessageFilterParamEnum.signQita;
                }
            }
        }
        return EdocMessageFilterParamEnum.sendQita;
    }
	
	/**
	 * 判断是否给代理人发送消息.可能已经取消代理，或者代理过期了，这种情况就不发消息了
	 * @param affairMemberId  : affair的memberID
	 * @param affairTransactorId : affair.TransactorId affair的代理人的ID
	 * @return
	 */
	public static boolean  isEdocProxy(Long affairMemberId, Long affairTransactorId){
		//我设置了XX给我干活，返回他的Id
		Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.ordinal(), affairMemberId);
		return agentId.equals(affairTransactorId);
	}
	//给在竞争执行中被取消的affair发送消息提醒
	public static Boolean competitionCancel(AffairManager affairManager, OrgManager orgManager, 
			UserMessageManager userMessageManager, WorkItem workitem, List<CtpAffair> affairs){
		return competitionCancel(affairManager, orgManager, userMessageManager, workitem, affairs,null);
	}
	public static Boolean competitionCancel(AffairManager affairManager, OrgManager orgManager, 
			UserMessageManager userMessageManager, WorkItem workitem, List<CtpAffair> affairs, CtpAffair nowAffair){
		CtpAffair affair = affairs.get(0);
		User user = AppContext.getCurrentUser();
    	String userName = "";
    	if (user != null) {
            userName = user.getName();
        }
        Long agentId = null;
    	if(nowAffair != null && nowAffair.getMemberId() != user.getId()){
    		agentId	= MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),nowAffair.getMemberId());
    	}
		try{
			List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
			//需要去重复
			Set<Long> idSet = new HashSet<Long>();
			Boolean pishiFlag = false;
			for (CtpAffair affair2 : affairs) {
				if(affair2.getMemberId().equals(user.getId()) || idSet.contains(affair2.getMemberId())){
					continue;
				}
				Map<String, Object> map = AffairUtil.getExtProperty(affair2);
				if(map != null && map.get(AffairExtPropEnums.dailu_pishi_mark.toString()) != null){
					V3xOrgMember member = orgManager.getMemberById(affair2.getMemberId());
					pishiFlag= true;
				}
				idSet.add(affair2.getMemberId());
				receivers.add(new MessageReceiver(affair2.getId(), affair2.getMemberId()));
			}
			ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
			Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
			if(nowAffair != null && nowAffair.getMemberId() != null) {
				V3xOrgMember member = orgManager.getMemberById(nowAffair.getMemberId());
				if(agentId != null && agentId.toString().equals(user.getId().toString())){
					MessageContent mContent =new MessageContent("edoc.competition", affair.getSubject(), 
				    		member.getName(),affair.getApp()).setImportantLevel(affair.getImportantLevel());
					mContent.add("col.agent.deal", user.getName());
				    userMessageManager.sendSystemMessage(mContent, appEnum, affair.getSenderId(), receivers,systemMessageFilterParam);
				}else if(pishiFlag){
					MessageContent mContent =new MessageContent("edoc.competition", affair.getSubject(), 
				    		member.getName(),affair.getApp()).setImportantLevel(affair.getImportantLevel());
					mContent.add("govdoc.pishi.deal", user.getName());
				    userMessageManager.sendSystemMessage(mContent, appEnum, affair.getSenderId(), receivers,systemMessageFilterParam);
				}else{
				    userMessageManager.sendSystemMessage(new MessageContent("edoc.competition", affair.getSubject(), 
			    			userName,affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getSenderId(), receivers,systemMessageFilterParam);
				}
			}
		} catch (Exception e) {
			LOGGER.error("发送消息异常", e);
		}
		return true;		
	}
	/**
	 * 公文流程结束，给流程中的所有Affair发送消息
	 * @return
	 */
	public static void processFinishedAutoPigeonhole(AffairManager affairManager,UserMessageManager userMessageManager 
			,EdocSummary summary,CtpAffair affair,OrgManager orgManager,
			String pigeonholePath,ProcessLogManager processLogManager,AppLogManager appLogManager){
		String operName="";
    	List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
    	 EdocSuperviseManager edocSuperviseManage = (EdocSuperviseManagerImpl)AppContext.getBean("edocSuperviseManager");
    	List<CtpAffair> affairs = edocSuperviseManage.getALLAvailabilityAffairList(ApplicationCategoryEnum.edoc, EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType()), summary.getId(), false);
    	List<String> nameList = new ArrayList<String>();
    	for(CtpAffair tempAffair : affairs){      
                Long memberId = tempAffair.getMemberId();
                if(nameList.contains(memberId.toString()))continue;
                Long affId = tempAffair.getId();
                receivers.add(new MessageReceiver(affId, memberId,"message.link.edoc.done",affId.toString()));
                nameList.add(memberId.toString());
        }
    	User user = AppContext.getCurrentUser();
	    /*if(user.getAgentToId() != -1 && affair.getMemberId().longValue() != user.getId().longValue()){
			V3xOrgMember member = null;
		    try {
		       	member = orgManager.getEntityById(V3xOrgMember.class,user.getAgentToId());
		       	operName = member.getName();        				
		    } catch (BusinessException e) {
		    	log.error("公文归档，未发现制定人员信息："+user.getAgentToId(),e);
		    }
	    }else{*/
	    	operName = user.getName();        		     	
	    //}
	  //OA-35517 wangchw在签报已发中进行归档后，收到了流程结束公文从已发/已办中消失的消息
	   /* try{
		    MessageContent content=new MessageContent("edoc.pigeonhole.auto"+Functions.suffix(),summary.getSubject(),EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey(),pigeonholePath)
		    	.setImportantLevel(summary.getImportantLevel()); //国际化资源统一放在Usermessage下
		    userMessageManager.sendSystemMessage(content, EdocUtil.getAppCategoryByEdocType(summary.getEdocType()),user.getId(),receivers);
	    }
	    catch(Exception e){
	    	log.error("公文流程自动结束，发送删除已办，已发事项的消息",e);
	    }*/
	}
	

	//给在终止操作中被取消的affair发送消息提醒
	public static Boolean terminateCancel(AffairManager affairManager, OrgManager orgManager, UserMessageManager userMessageManager, 
			WorkItem workitem, CtpAffair currentAffair, List<CtpAffair> trackingAndPendingAffairs) {
        if(currentAffair.getSenderId().equals(currentAffair.getMemberId())
                && (currentAffair.getSubObjectId() == null || currentAffair.getSubObjectId().longValue() == -1)) {//从已发中终止，作为系统自动终止的一个标志
            LOGGER.debug("协同自动终止逾期自由流程，不需要再发终止消息!");
            return false;
        }
        String messageLinkWaitSend = "message.link.edoc.waitSend";
        String messageLinkSent = "message.link.edoc.sended";
        String messageLinkDone = "message.link.edoc.done";
		ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(currentAffair.getApp());
		User user = AppContext.getCurrentUser();
		try{
			String opinionId = String.valueOf(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionId());
			String opinionContent = UserMessageUtil.getComment4Message(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion());
	    	int opinionType = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionHidden() ? 0 : Strings.isBlank(opinionContent) ? -1 : 1;
	    	
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			List<Long> members = new ArrayList<Long>();
			/** 给跟踪人发终止消息 **/
			for (CtpAffair affair : trackingAndPendingAffairs) {
				//当前处理者不需要收到终止消息提醒
				if(!user.getId().equals(affair.getMemberId()) && affair.getSenderId().longValue() != affair.getMemberId().longValue() && !affair.isDelete()) {
					receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), messageLinkDone, affair.getId()));
					members.add(affair.getMemberId());
				}
			}
			/** 给发起人发终止消息 **/
			CtpAffair senderAffair = affairManager.getSenderAffair(currentAffair.getObjectId());
			if(senderAffair!=null && !members.contains(senderAffair.getMemberId())) {
				messageLinkSent = messageLinkWaitSend;
				receivers.add(new MessageReceiver(senderAffair.getId(), senderAffair.getMemberId(), messageLinkSent, senderAffair.getId()));
				members.add(senderAffair.getMemberId());
			}
			
			/** 给督办人发终止消息 **/
			SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
			
            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(currentAffair.getObjectId());
            
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
            
            /** 给消息推送人发终止消息 **/
            List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
            for(Long[] push :pushMemberIds) {
                if(!members.contains(push[1])) {
                    receivers.add(new MessageReceiver(push[0], push[1], messageLinkDone, push[0], opinionId));
                    members.add(push[1]);
                }
            }
			
			//从ThreadLocal中获取终止流程的Affair
			CtpAffair theStopAffair = DateSharedWithWorkflowEngineThreadLocal.getTheStopAffair();
            int importmentLevel = 1;
            if(null != currentAffair.getImportantLevel()){
            	importmentLevel=currentAffair.getImportantLevel();
            }
            Integer systemMessageFilterParam = getSystemMessageFilterParam(theStopAffair).key;
            if(theStopAffair != null){
                V3xOrgMember m = orgManager.getMemberById(theStopAffair.getMemberId());
                String memberName = Functions.showMemberNameOnly(theStopAffair.getMemberId());
                if(theStopAffair.getTransactorId() != null){ //由代理人终止
                    V3xOrgMember proxyM = orgManager.getMemberById(theStopAffair.getTransactorId());
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", currentAffair.getSubject(), memberName, currentAffair.getApp(), opinionType, opinionContent).add("edoc.agent.deal", proxyM.getName()).setImportantLevel(importmentLevel), appEnum, m.getId(), receivers,systemMessageFilterParam);
                } else{
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", currentAffair.getSubject(), memberName, currentAffair.getApp(), opinionType, opinionContent).setImportantLevel(importmentLevel), appEnum, m.getId(), receivers,systemMessageFilterParam);
                }
            } else{
                userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", currentAffair.getSubject(), EdocUtil.getAccountName(), currentAffair.getApp(), opinionType, opinionContent).setImportantLevel(importmentLevel), appEnum, user.getId(), receivers,systemMessageFilterParam);
            }
            
		} catch (Exception e) {
			LOGGER.error("发送消息异常", e);
		}
		return true;
	}
	
	//加签消息提醒
	public static Boolean insertPeopleMessage(AffairManager affairManager, UserMessageManager userMessageManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair) throws BusinessException{
		User user = AppContext.getCurrentUser();
		try {
            if (partyNames != null) {
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            	List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                for (CtpAffair trackingAffair : trackingAffairList) {
                	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",
                			trackingAffair.getId().toString()));
                }
                
                ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
                Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
                if(affair.getMemberId().longValue() != user.getId().longValue()){
    				V3xOrgMember member = null;
    				member = getMemberById(orgManager, affair.getMemberId());
    			    String proxyName = member != null ? member.getName() : null;
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
                    .add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
                }else{
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addAssign", summary.getSubject(), user.getName(), 
                    	StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
            return false;
        }
		return true;
	}
	
	//多级会签消息提醒
	public static Boolean addMoreSignMessage(AffairManager affairManager, UserMessageManager userMessageManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		User user = AppContext.getCurrentUser();
		try {
            if (partyNames != null) {
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            	List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                for (CtpAffair trackingAffair : trackingAffairList) {
                	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",
                			trackingAffair.getId().toString()));
                }
                
                ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
                Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
                if(affair.getMemberId().longValue() != user.getId().longValue()){
    				V3xOrgMember member = null;
    				member = getMemberById(orgManager, affair.getMemberId());
    				String proxyName = member != null ? member.getName() : null;
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addMoreAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
                    .add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
                }else{
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addMoreAssign", summary.getSubject(), user.getName(), 
                    	StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
            return false;
        }
		return true;
	}
	
	//减签消息提醒
	public static Boolean deletePeopleMessage(AffairManager affairManager, OrgManager orgManager, 
			UserMessageManager userMessageManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		User user = AppContext.getCurrentUser();
		try {
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
            for (CtpAffair trackingAffair : trackingAffairList) {
                 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
            }
            
            ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
            Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
            if(affair.getMemberId().longValue() != user.getId().longValue()){
				V3xOrgMember member = null;
				member = getMemberById(orgManager, affair.getMemberId());
				String proxyName = member != null ? member.getName() : null;
                userMessageManager.sendSystemMessage(new MessageContent("edoc.decreaseAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
                .add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
            }else{
                userMessageManager.sendSystemMessage(new MessageContent("edoc.decreaseAssign", summary.getSubject(), 
                		user.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
            return false;
        }
		return true;
	}
	
	//回退消息提醒
	//直接受到回退影响的节点，无论是否跟踪，都需要收到消息。包括{1.当前待办节点，2.当前节点的上一节点}
	//其它不受到影响的已办节点，若做了跟踪，也要受到消息，如果没有跟踪，不收到消息。
	public static Boolean stepBackMessage(AffairManager affairManager, OrgManager orgManager, 
			UserMessageManager userMessageManager, List<CtpAffair> allTrackAffairLists, CtpAffair affair, Long summaryId, EdocOpinion signOpinion,boolean traceFlag) {
		Integer importantLevel = affair.getImportantLevel();
		User user = AppContext.getCurrentUser();
        String userName = "";
        if (user != null) {
            userName = user.getName();
        }
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
        try {
        	//给所有待办事项发起协同被回退消息提醒
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        	Set<Long> filterMember = new HashSet<Long>();
        	
        	//1、发起者一定发送
        	if(!filterMember.contains(affair.getMemberId())){
        		CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
        		if(null != sendAffair.getSubState() && sendAffair.getSubState() == SubStateEnum.col_waitSend_stepBack.getKey()){
        			//如果是回退或者撤銷，指定回退調整的連接地址用新的Link
        			receivers.add(new MessageReceiver(affair.getId(), sendAffair.getMemberId(), "message.link.edoc.waitSend", sendAffair.getId().toString()));
    	        	filterMember.add(affair.getSenderId());
        		}else{
        			receivers.add(new MessageReceiver(affair.getId(), sendAffair.getMemberId(), "message.link.edoc.sended", sendAffair.getId().toString()));
    	        	filterMember.add(affair.getSenderId());
        		}
	        	
        	}
        	//2.当前节点父节点。
        	Map<Long, Long> affairMap = DateSharedWithWorkflowEngineThreadLocal.getAffairMap();
        	for(Long key : affairMap.keySet()){
        		Long affId = affairMap.get(key);
        		if(!filterMember.contains(key)){
        			receivers.add(new MessageReceiver(affId, key,"message.link.edoc.pending", affId.toString()));
        			filterMember.add(key);
        		}
        		Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),key);
				if(agentId!=null && !filterMember.contains(agentId) && user.getId().longValue()!= agentId.longValue()){
					receivers.add(new MessageReceiver(affId,agentId,"message.link.edoc.pending", affId.toString()));
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
        	
        	String opinionContent = UserMessageUtil.getComment4Message(signOpinion.getContent());
        	//回退时 意见加了附件，则回退消息中加附字
        	if(signOpinion.isHasAtt()){
        		opinionContent += EdocHelper.getOpinionAttStr();
        	}
        	
        	int opinionType = Strings.isTrue(signOpinion.getIsHidden()) ? 0 : Strings.isBlank(opinionContent) ? -1 : 1;
        	
        	Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
        	
        	if(affair.getMemberId().longValue() != user.getId().longValue()){
				V3xOrgMember member = null;
				member = getMemberById(orgManager, affair.getMemberId());
				String proxyName = member != null ? member.getName() : null;
				userMessageManager.sendSystemMessage(new MessageContent("edoc.stepback", affair.getSubject(), proxyName ,affair.getApp(), opinionType, opinionContent).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource")
				.add("edoc.agent.deal", user.getName()).setImportantLevel(importantLevel), appEnum,affair.getMemberId(), receivers, systemMessageFilterParam);
            }else{
            	if(traceFlag){
            		List<Long> _hasTraceDataAffair = (List<Long>)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affair");
                	Long sId =  (Long)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_summaryId");
         		   	Long aId =  (Long)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_affairId");
                	Integer type =  (Integer)DateSharedWithWorkflowEngineThreadLocal.getTraceDataMap().get("traceData_traceType");
                	Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
         		   	Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
                	Iterator<MessageReceiver> it = receivers.iterator();
                	while(it.hasNext()){
                		MessageReceiver mr = it.next();
                		if(_hasTraceDataAffair.contains(mr.getReferenceId())){
                			if(Strings.isBlank(mr.getLinkType())){
                				if(null != aId){
                				    //静态数据连接
                					new_receivers.add(new MessageReceiver(aId,mr.getReceiverId(),"message.link.edoc.traceRecord",ColOpenFrom.stepBackRecord.name(),aId.toString(),sId.toString(),type+""));
                				}else{
                					new_receivers.add(new MessageReceiver(mr.getReferenceId(),mr.getReceiverId(),"message.link.edoc.traceRecord",ColOpenFrom.stepBackRecord.name(),mr.getReferenceId().toString(),summaryId.toString(),type+""));
                				}
                			}else{
                				old_receivers.add(mr);	
                			}
                		}else{
                			old_receivers.add(mr);
                		}
                	}
                	userMessageManager.sendSystemMessage(new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent).setImportantLevel(importantLevel).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource"),
            				appEnum, user.getId(), old_receivers, systemMessageFilterParam);
                	userMessageManager.sendSystemMessage(new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent)
                	.add("edoc.summary.cancel.traceview").setImportantLevel(importantLevel).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource"),
            				appEnum, user.getId(), new_receivers, systemMessageFilterParam);
            	}else{
            		userMessageManager.sendSystemMessage(new MessageContent("edoc.stepback.1", affair.getSubject(), userName ,affair.getApp(), opinionType, opinionContent).setImportantLevel(importantLevel).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource"),
            				appEnum, user.getId(), receivers, systemMessageFilterParam);
            	}
            }
        	
        } catch (Exception e) {
            LOGGER.error("send message failed", e);
            return false;
        }
		return true;
	}
	

	//工作项终止消息提醒
	public static Boolean stepStopMessage(AffairManager affairManager, OrgManager orgManager, 
			EdocSummary summary, UserMessageManager userMessageManager, CtpAffair affair) {
		User user = AppContext.getCurrentUser();
		V3xOrgMember theMember = getMemberById(orgManager, affair.getMemberId());
		String name = theMember != null ? theMember.getName() : null;
		List<CtpAffair> trackingAffairList = null;
        try {
            trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
        } catch (BusinessException e1) {
            LOGGER.error("", e1);
        }
        
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
		Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
		
		try{
			Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
			for(CtpAffair affair1 : trackingAffairList){
				Long memberId = affair1.getMemberId();
				Long affairId = affair1.getId();
				receivers.add(new MessageReceiver(affairId, memberId,"message.link.edoc.done",affairId.toString()));
			}
			if(affair.getMemberId().longValue() != user.getId().longValue()){
				userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", summary.getSubject(), name, affair.getApp()).add("edoc.agent.deal", user.getName()).setImportantLevel(summary.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
			}else{
				userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", summary.getSubject(), name, affair.getApp()).setImportantLevel(summary.getImportantLevel()), appEnum, theMember.getId(), receivers,systemMessageFilterParam);
			}
		} catch (Exception e) {
			LOGGER.error("发送消息异常", e);
		}
		return true;
	}
	
	//管理员终止流程消息提醒
	public static Boolean adminStopMessage(AffairManager affairManager, OrgManager orgManager, 
			EdocSummary summary, UserMessageManager userMessageManager, CtpAffair affair) {
		User user = AppContext.getCurrentUser();
		List<CtpAffair> trackingAffairList = null;
        try {
            trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
        } catch (BusinessException e1) {
            LOGGER.error("", e1);
        }
        ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
        Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
        try{
        	Map<Long, MessageReceiver> messageReceiverMap = new HashMap<Long, MessageReceiver>();
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        for(CtpAffair affair1 : trackingAffairList){
	        	Long memberId = affair1.getMemberId();
	            Long affairId = affair1.getId();
	            messageReceiverMap.put(memberId, new MessageReceiver(affairId, memberId,"message.link.edoc.done",affairId.toString()));
	        }
	        for(Long key : messageReceiverMap.keySet()){
	        	receivers.add(messageReceiverMap.get(key));
	        }
     
            MessageContent msgContent = null;
            if(user.isAdministrator() || user.isGroupAdmin()){
                msgContent = new MessageContent("edoc.terminate", summary.getSubject(), user.getName(), affair.getApp());
                userMessageManager.sendSystemMessage(msgContent, appEnum, user.getId(), receivers,systemMessageFilterParam);
            }else if(affair.getMemberId().longValue() != user.getId().longValue()){//这里涉及到代理，目前未使用到，如使用到该条件需要修改。
				userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", summary.getSubject(), user.getName(),affair.getApp()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
		    }
            //消息改为“被单位管理员终止”
            userMessageManager.sendSystemMessage(new MessageContent("edoc.terminate", summary.getSubject(), user.getName(),affair.getApp()).setImportantLevel(summary.getImportantLevel()),appEnum,user.getId(),receivers,systemMessageFilterParam);
        } catch (Exception e) {
        	LOGGER.error("发送消息异常", e);
        }
		return true;
	}
	
	//取回消息提醒
	public static Boolean takeBackMessage(AffairManager affairManager, OrgManager orgManager, 
			UserMessageManager userMessageManager, List<CtpAffair> pendingAffairList, CtpAffair affair, Long summaryId){
		if(pendingAffairList == null || pendingAffairList.isEmpty()){
			return false;
		}
		User user = AppContext.getCurrentUser();
        String userName = "";
        if (user != null) {
            userName = user.getName();
        }
        ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
        Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
        
        try {
        	Integer importantLevel = pendingAffairList.get(0).getImportantLevel();
        	Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
            List<Long> list = new ArrayList<Long>();
            Map<Long, Long> allStepBackAffectAffairMap = DateSharedWithWorkflowEngineThreadLocal.getAllStepBackAffectAffairMap();
            if(allStepBackAffectAffairMap!=null){
	            for(Long key : allStepBackAffectAffairMap.keySet()){
	            	Long memberId = key;
	        		if(list.contains(memberId)){
	        			continue;
	        		}
	                receivers1.add(new MessageReceiver(allStepBackAffectAffairMap.get(memberId), memberId));
	                list.add(memberId);
	            }
            }
        	if(affair.getMemberId().longValue() != user.getId().longValue()){
				V3xOrgMember member = null;
				member = getMemberById(orgManager, affair.getMemberId());
			    String proxyName = member != null ? member.getName() : null;
				try {
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.takeback", affair.getSubject(), proxyName,affair.getApp())
                    .add("edoc.agent.deal", user.getName()).setImportantLevel(importantLevel), appEnum, affair.getMemberId(), receivers1,systemMessageFilterParam);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }else{
            	try {
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.takeback", affair.getSubject(), userName,affair.getApp()).setImportantLevel(importantLevel), appEnum, user.getId(), receivers1,systemMessageFilterParam);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();//---------------要放开
        	List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
            for (CtpAffair trackingAffair : trackingAffairList) {
            	if(list.contains(trackingAffair.getMemberId())){
        			continue;
        		}
            	list.add(trackingAffair.getMemberId());
                receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
            }
            if(affair.getMemberId().longValue() != user.getId().longValue()){
				V3xOrgMember member = null;
				member = getMemberById(orgManager, affair.getMemberId());
				String proxyName = member != null ? member.getName() : null;
                userMessageManager.sendSystemMessage(new MessageContent("edoc.takeback", affair.getSubject(), proxyName,affair.getApp())
                .add("edoc.agent.deal", user.getName()).setImportantLevel(importantLevel), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
            }else{
                userMessageManager.sendSystemMessage(new MessageContent("edoc.takeback", affair.getSubject(), userName,affair.getApp()).setImportantLevel(importantLevel), appEnum, user.getId(), receivers,systemMessageFilterParam);
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
        }
		return true;
	}
	
	//修改正文消息提醒
	//代理消息提醒未实现
	public static Boolean saveBodyMessage(AffairManager affairManager, UserMessageManager userMessageManager,
			OrgManager orgManager, EdocSummary summary){
		User user = AppContext.getCurrentUser();
    	Set<MessageReceiver> receivers = getBodyAttUpdateMessage(user,affairManager, summary);
    	
    	ApplicationCategoryEnum appEnum=EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
    	Integer systemMessageFilterParam = getSystemMessageFilterParam(summary).key;
    	try {
            userMessageManager.sendSystemMessage(new MessageContent("edoc.modifyBody", summary.getSubject(), user.getName(),EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey()).setImportantLevel(summary.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
		} catch (BusinessException e) {
			LOGGER.error("修改正文消息提醒失败", e);
		}
		return true;
	}
	
	public static int getMsgFilterParamBySummary(EdocSummary summary) {
		int msgFilterParma = 19;
        if(summary.getEdocType() ==1 ){
        	msgFilterParma = 20;
        }else if(summary.getEdocType() ==2 ){
        	msgFilterParma = 21;
        }
		return msgFilterParma;
	}
	public static Boolean updateAttachmentMessage(AffairManager affairManager, UserMessageManager userMessageManager,
			OrgManager orgManager, EdocSummary summary){
		User user = AppContext.getCurrentUser();
		// 发送系统消息
		List<CtpAffair> affairs;
        try {
        	List<StateEnum> stateList = new ArrayList<StateEnum>();
        	stateList.add(StateEnum.col_pending);
        	stateList.add(StateEnum.col_done);
        	stateList.add(StateEnum.col_sent);
        	affairs = affairManager.getAffairs(summary.getId(), stateList);
        }
        catch (BusinessException e1) {
            LOGGER.error("修改正文消息提醒失败", e1);
            return false;
        }
        
		Map<Long,MessageReceiver> member2Receiver = new HashMap<Long,MessageReceiver>();
		for (CtpAffair affair : affairs) {
			StateEnum state = StateEnum.valueOf(affair.getState());
			switch(state) {
				case col_sent:
					if(member2Receiver.get(affair.getMemberId()) == null && !affair.getMemberId().equals(user.getId()))
						member2Receiver.put(affair.getMemberId(),new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.sended", affair.getId()));
					break;
				case col_done:
					if(member2Receiver.get(affair.getMemberId()) == null && !affair.getMemberId().equals(user.getId()))
						member2Receiver.put(affair.getMemberId(),new MessageReceiver(summary.getId(), affair.getMemberId(), "message.link.edoc.done", affair.getId()));
					break;
				case col_pending:
					if (member2Receiver.get(affair.getMemberId()) == null && !affair.getMemberId().equals(user.getId())) {
						member2Receiver.put(affair.getMemberId(), new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId()));
					}
					break;
			}
		}
    	try {
    		SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
    		CtpSuperviseDetail detail = superviseManager.getSupervise(summary.getId());
    		if(detail != null) {
	    		List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
	    		if(Strings.isNotEmpty(supervisors)) {
	    			for(CtpSupervisor supervisor : supervisors) {
	    				Long personId = supervisor.getSupervisorId();
	    				if(member2Receiver.get(personId)==null && !personId.equals(user.getId()))
	    					member2Receiver.put(supervisor.getSupervisorId(), new MessageReceiver(summary.getId(), supervisor.getSupervisorId(), "message.link.edoc.supervise.detail", summary.getId()));
	    			}
	    		}
    		}
    		ApplicationCategoryEnum appEnum = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
    		Integer systemMessageFilterParam = getSystemMessageFilterParam(summary).key;
            userMessageManager.sendSystemMessage(new MessageContent("edoc_update_attachment", user.getName(), summary.getSubject(), appEnum.getKey()).setImportantLevel(summary.getImportantLevel()), appEnum,
            	user.getId(), member2Receiver.values(),systemMessageFilterParam);
		} catch (BusinessException e) {
			LOGGER.error("修改正文消息提醒失败", e);
		}
		return true;
	}
	private static Set<MessageReceiver> getBodyAttUpdateMessage(User user,AffairManager affairManager,EdocSummary summary){
		Long summaryId = summary.getId();
		List<CtpAffair> trackingAffairList = null;
        try {
            trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
    	Map<Long,MessageReceiver> receivers = new HashMap<Long,MessageReceiver>();
    	for(CtpAffair affair : trackingAffairList){
    		Long memberId = affair.getMemberId();
    		receivers.put(memberId,new MessageReceiver(affair.getId(), memberId, "message.link.edoc.done", affair.getId().toString()));
    	}
    	
    	List<CtpAffair> pendingAffair = null;
        try {
            pendingAffair = affairManager.getAffairs(summaryId, StateEnum.col_pending);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
    	for(CtpAffair affair1 : pendingAffair){
    		if(affair1.getMemberId().longValue() == user.getId().longValue())
    			continue;
    		receivers.put(affair1.getMemberId(),new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.edoc.pending", affair1.getId().toString()));
    	}
    	Set<MessageReceiver> ret= new HashSet<MessageReceiver>();
    	ret.addAll(receivers.values());
		return ret;
	}
	
	/**
	 * 暂存待办消息提醒
	 * @param userMessageManager
	 * @param orgManager
	 * @param affairManager
	 * @param affair
	 * @return
	 * @deprecated 公文暂存待办不发消息
	 */
	public static Boolean zcdbMessage(UserMessageManager userMessageManager, OrgManager orgManager,
	        AffairManager affairManager, CtpAffair affair,boolean isHasAtt){
		User user = AppContext.getCurrentUser();
		String key = "edoc.saveDraft";
		 List<CtpAffair> trackingAffairList = null;
		 try {
		 CtpTrackMemberManager trackManager=(CtpTrackMemberManager)AppContext.getBean("trackManager");
		 trackingAffairList = affairManager.getValidTrackAffairs(affair.getObjectId());
		 
	     List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(affair.getObjectId(),null);
		 
		 trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, affair.getMemberId());
		
        } catch (BusinessException e1) {
           LOGGER.error("", e1);
        }
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
        Set <Long> members = new HashSet<Long>();
    	for(CtpAffair _affair : trackingAffairList){
    		members.add(_affair.getMemberId());
    		Long memberId = _affair.getMemberId();
    		receivers.add(new MessageReceiver(_affair.getId(), memberId, "message.link.edoc.done", _affair.getId().toString()));
    	}
    	//推送
	   for(Long[] push :pushMemberIds ){
       		if(!members.contains(push[1])){
       			receivers.add(new MessageReceiver(push[0], push[1],"message.link.col.done",push[0]));
       		}
       } 
	    ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
   		Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
    	String attStr = "";
    	if(isHasAtt){
            attStr = EdocHelper.getOpinionAttStr();
        }
    	MessageContent msgContent = null;
    	
        try {
            msgContent = null;
            if(affair.getMemberId().longValue() != user.getId().longValue()){//代理人处理
            	V3xOrgMember member = getMemberById(orgManager, affair.getMemberId());
            	if(member != null){
            		msgContent = new MessageContent(key, affair.getSubject(), member.getName(),affair.getApp(),attStr);
            	}
            	if(msgContent != null){            		
            		msgContent.add("edoc.agent.deal", user.getName());
            	}
            }else{
            	msgContent = new MessageContent(key, affair.getSubject(), user.getName(),affair.getApp(),attStr);
            }
            msgContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
            userMessageManager.sendSystemMessage(msgContent.setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
        }
		return true;
	}
	
	//会签消息提醒
	public static Boolean colAssignMessage(UserMessageManager userMessageManager, AffairManager affairManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		User user = AppContext.getCurrentUser();
		ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
		try {
            if (partyNames != null) {
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            	List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                for (CtpAffair trackingAffair : trackingAffairList) {
                	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
                }
                Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
                if(affair.getMemberId().longValue() != user.getId().longValue()){
    				V3xOrgMember member = getMemberById(orgManager, affair.getMemberId());
    				if(member != null){	
    					userMessageManager.sendSystemMessage(new MessageContent("edoc.colAssign", summary.getSubject(), member.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp())
    							.add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
    				}
                }else{
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.colAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
        }
		return true;
	}
	
	//知会消息提醒
	public static Boolean addInformMessage(UserMessageManager userMessageManager, AffairManager affairManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		return _addInformMessage(userMessageManager, affairManager,
				orgManager, partyNames, summary, affair,"edoc.addInform");
	}
	public static Boolean addPassReadMessage(UserMessageManager userMessageManager, AffairManager affairManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		return _addInformMessage(userMessageManager, affairManager,
				orgManager, partyNames, summary, affair,"edoc.addPassRead");
	}

	//知会,传阅消息提醒
	private static Boolean _addInformMessage(UserMessageManager userMessageManager, AffairManager affairManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair,String msgLabel){
		User user = AppContext.getCurrentUser();
		ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
		try {
            if (partyNames != null) {
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            	List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                for (CtpAffair trackingAffair : trackingAffairList) {
                	receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
                }
                Integer systemMessageFilterParam = getSystemMessageFilterParam(affair).key;
                if(affair.getMemberId().longValue() != user.getId().longValue()){
    				V3xOrgMember member = null;
    				member = getMemberById(orgManager, affair.getMemberId());
    				String proxyName = member != null ? member.getName() : null;
                    userMessageManager.sendSystemMessage(new MessageContent(msgLabel, summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
                        .add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
                }else{
                    userMessageManager.sendSystemMessage(new MessageContent(msgLabel, summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
        }
		return true;
	}
	
	public static V3xOrgMember getMemberById(OrgManager orgManager, Long memberId){
		V3xOrgMember member = null;
		try {
	       	member = orgManager.getEntityById(V3xOrgMember.class, memberId);
	    } catch (BusinessException e) {
	    	LOGGER.error("获取公文消息提醒对应人员失败", e);
	    	return null;
	    }
		return member;
	}
	
	public static boolean sendBackDraftMessage(UserMessageManager userMessageManager,AffairManager affairManager,OrgManager orgManager,CtpAffair affair) {
		 if(affair == null)
			return false;
		String extProperty = (String)affair.getExtraAttr("sendBackAffairId");
		if(Strings.isBlank(extProperty))
			return false;
		String name = "";
		try {
			Long sendBackAffairId = Long.parseLong(extProperty);
			CtpAffair sendBackAffair = affairManager.get(sendBackAffairId);
			Integer systemMessageFilterParam = getSystemMessageFilterParam(sendBackAffair).key;
			
			Long sendBackMemberId = -1L;
			if(sendBackAffair != null) {
				sendBackMemberId = sendBackAffair.getMemberId();
				V3xOrgMember member = orgManager.getMemberById(sendBackMemberId);
				name = member.getName();
			}
			MessageReceiver receiver = new MessageReceiver(affair.getObjectId(),affair.getMemberId());
			MessageContent content = new MessageContent("edoc.sendBackDraft",affair.getSubject(),name);
			content.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
			userMessageManager.sendSystemMessage(content,ApplicationCategoryEnum.edocSend,sendBackMemberId,receiver,systemMessageFilterParam);
		}catch(Exception e) {
			LOGGER.error("", e);
			return false;
		} 
		return true;
	}
	//登记之后给签收人发消息
    public static void sendRegistMessage(UserMessageManager userMessageManager,OrgManager orgManager,User user,EdocRegister register,EdocRecieveRecord record){
    	Long regUserId = register.getRegisterUserId();
    	Long agentToId = null; //被代理人ID
		String agentToName= "";
		if(!Long.valueOf(user.getId()).equals(regUserId)){
			agentToId = regUserId;
			try{
				agentToName = orgManager.getMemberById(agentToId).getName();
			}catch(Exception e){
			    LOGGER.error("", e);
			}
		}
		if(agentToId != null){
			MessageContent msgContent=new MessageContent("exchange.edoc.register",agentToName,record.getSubject()).add("edoc.agent.deal", user.getName());
			msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
			MessageReceiver receiver=new MessageReceiver(record.getId(),record.getRecUserId(),"message.link.exchange.register.receive",record.getId().toString());
			try {
				userMessageManager.sendSystemMessage(msgContent,ApplicationCategoryEnum.edoc,agentToId,receiver,EdocMessageFilterParamEnum.exchange.key);
			} catch (BusinessException e) {
				LOGGER.error("", e);
			}
		}else{
			MessageContent msgContent=new MessageContent("exchange.edoc.register",user.getName(),record.getSubject());
			msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
			MessageReceiver receiver=new MessageReceiver(record.getId(),record.getRecUserId(),"message.link.exchange.register.receive",record.getId().toString());
			try {
				userMessageManager.sendSystemMessage(msgContent,ApplicationCategoryEnum.edoc,user.getId(),receiver,EdocMessageFilterParamEnum.exchange.key);
			} catch (BusinessException e) {
				LOGGER.error("", e);
			}	
		}
    }
    
    /**************** 指定回退消息 start ******************/
    /**
     * 指定回退给发起节点，提交的时候发消息
     * @param summary
     * @param nodeNameStr
     * @throws BusinessException
     */
    public static void transEdocPendingSpecialBackedMsg(EdocSummary summary, String nodeNameStr) throws BusinessException {
        transEdocPendingSpecialBackedMsg(summary, nodeNameStr, false);
    }
    
    /**
     * 
     * @param summary
     * @param nodeNameStr
     * @param isSpecialBackToSenderCancel 指定回退发起者：流程重走
     * @throws BusinessException
     */
    public static void transEdocPendingSpecialBackedMsg(EdocSummary summary, String nodeNameStr,boolean isSpecialBackToSenderCancel) throws BusinessException {
        if (summary == null)
            return;
        OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
        AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
        UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
        Long sendAffairId = null;
        Long summaryId = summary.getId();
        Set<Long> memberFilters = new HashSet<Long>();
        List<CtpAffair> trackingAffairList = getTrackingAffairList(memberFilters,summaryId);
        String messageLinkWaitSend = "message.link.edoc.waitSend";
        String messageLinkSent = "message.link.edoc.sended";
        String messageLinkDone = "message.link.edoc.done";
        String messageLinkPending = "message.link.edoc.pending";
        String messageSuperviseLink = "message.link.edoc.supervise.detail";//给督办人发消息的连接
        String resendLabel = "edoc.appointStepBack.resend";
        int app= ApplicationCategoryEnum.edoc.key();
        if(isSpecialBackToSenderCancel){
            resendLabel= "edoc.send";
            if(summary.getEdocType() == 0){
                app= ApplicationCategoryEnum.edocSend.key();
            }else if(summary.getEdocType() == 1){
                app= ApplicationCategoryEnum.edocRec.key();
            }else if(summary.getEdocType() == 2){
                app= ApplicationCategoryEnum.edocSign.key();
            }
        }
        ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(app);
        // 待办，已办
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectId", summary.getId());
        List<CtpAffair> allTrackAffairLists = affairManager.getValidAffairs(null, map);
        if (!CollectionUtils.isEmpty(allTrackAffairLists)) {
            Set<Long> receiversIds = new HashSet<Long>();
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            V3xOrgMember member = null;
            for (CtpAffair affair : allTrackAffairLists) {
                if (affair.getState() != StateEnum.col_sent.key()) {
                    // 待办的情况,要过滤掉指定回退者
                    if (affair.getState() == StateEnum.col_pending.key()) {
                        member = orgManager.getMemberById(affair.getMemberId());
                        if (member != null && nodeNameStr != null && nodeNameStr.indexOf(member.getName()) != -1) {
                            continue;
                        }
                    }
                    // 已办的情况,要过滤掉没有设置跟踪的情况
                    if ((affair.getState() == StateEnum.col_done.key() || affair.getState() == StateEnum.col_pending.key()) && !trackingAffairList.contains(affair)) {
                        continue;
                    }
                    //跟踪有可能是已发，待办，已办
                    String messageUrl = messageLinkSent;
                    if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                        messageUrl = messageLinkWaitSend;
                    } else if(Integer.valueOf(StateEnum.col_done.getKey()).equals(affair.getState())) {
                        messageUrl = messageLinkDone;
                    } else if(Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
                        messageUrl = messageLinkPending;
                    }
                    if (!receiversIds.contains(affair.getMemberId())) {
                        receiversIds.add(affair.getMemberId());
                        receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), messageUrl, affair.getId(), null));
                    }
                } else {
                    sendAffairId = affair.getId();
                }
            }
            Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
            if(isSpecialBackToSenderCancel){
                MessageContent messageContentSent = MessageContent.get(resendLabel, summary.getSubject(),  AppContext.currentUserName(),app);
                messageContentSent.setImportantLevel(summary.getImportantLevel());
                userMessageManager.sendSystemMessage(messageContentSent, appEnum, AppContext.getCurrentUser().getId(), receivers, systemMessageFilterParam);
            }else{
                String content = ResourceUtil.getString(resendLabel, AppContext.currentUserName(), summary.getSubject(), nodeNameStr);
                MessageContent messageContentSent = new MessageContent(content).setImportantLevel(summary.getImportantLevel());
                userMessageManager.sendSystemMessage(messageContentSent, ApplicationCategoryEnum.edoc, AppContext.getCurrentUser().getId(), receivers, systemMessageFilterParam);
            }
        }
        // 督办者
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summary.getId());
        
        if (superviseDetail != null) {
            Set<MessageReceiver> receivers4Sup = new HashSet<MessageReceiver>();
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for (CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                receivers4Sup.add(new MessageReceiver(sendAffairId, colSupervisMemberId, messageSuperviseLink, summary.getId()));
            }
            if(isSpecialBackToSenderCancel){
                MessageContent messageContentSent = MessageContent.get(resendLabel, summary.getSubject(),  AppContext.currentUserName(),app);
                messageContentSent.setImportantLevel(summary.getImportantLevel());
                userMessageManager.sendSystemMessage(messageContentSent, ApplicationCategoryEnum.edoc, AppContext.getCurrentUser().getId(), receivers4Sup,  EdocMessageFilterParamEnum.supervise.key);
            }else{
                MessageContent msgContent = new MessageContent(resendLabel, AppContext.currentUserName(), summary.getSubject(), nodeNameStr).setImportantLevel(summary.getImportantLevel());
                userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, AppContext.getCurrentUser().getId(), receivers4Sup, EdocMessageFilterParamEnum.supervise.key);
            }
        }
    }
    
    /**
     * 指定回退给普通节点，提交的时候发消息
     * @param summary
     * @param submit2NodeName
     * @param currentAffair
     * @throws BusinessException
     */
    public static void transSendSubmitMessage4SepicalBacked(EdocSummary summary, String submit2NodeName, CtpAffair currentAffair) throws BusinessException {
    	UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
    	OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
    	String messageLinkSent = "message.link.edoc.sended";
    	String messageLinkDone = "message.link.edoc.done";
        Long summaryId = summary.getId();
        V3xOrgMember member = orgManager.getMemberById(currentAffair.getMemberId());
        List<Object> submitMemberLabel = new ArrayList<Object>();
        submitMemberLabel.add(0, "edoc.appointStepBack.submit");
        submitMemberLabel.add(1, member.getName());
        submitMemberLabel.add(2, summary.getSubject());
        submitMemberLabel.add(3, submit2NodeName);
        /** 定义人员过滤器，避免消息重复 **/
        Set<Long> filterMemberIds = new HashSet<Long>();
        filterMemberIds.add(AppContext.currentUserId());
        /** 给跟踪节点发提交消息 **/
        List<CtpAffair> affairs = getTrackingAffairList(filterMemberIds,summaryId);
        sendMsg4Affairs(userMessageManager, summary,currentAffair, submitMemberLabel, messageLinkSent, messageLinkDone, affairs, filterMemberIds);
        /** 给督办人发提交消息 **/
        sendMsg4Receivers(userMessageManager, summary, currentAffair, submitMemberLabel, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair),filterMemberIds);
        
        /** 给消息推送发提交消息 **/
        List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
        if(pushMemberIds.isEmpty()){return;}
        User user = AppContext.getCurrentUser();
        Set <Long> members = new HashSet<Long>();
        for(CtpAffair a : affairs) {
            if (!a.isDelete()) {
            	members.add(a.getMemberId());
            }
        }
        try{
	        String opinionContent = UserMessageUtil.getComment4Message(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion());
	        if(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionUploadAtt()){
	            opinionContent += EdocHelper.getOpinionAttStr();
	        }
	        int opinionAttitude = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionAttitude();
	        int opinionType = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionHidden() ? 0 : Strings.isBlank(opinionContent) ? -1 : opinionAttitude == -1 ? 1: 2;
	        ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(currentAffair.getApp());
        	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	        for(Long[] push :pushMemberIds ){
	        	if(!members.contains(push[1])){
	        		receivers.add(new MessageReceiver(push[0], push[1],"message.link.edoc.done",push[0]));
	        	}
	        }
	        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
	        MessageContent mc = new MessageContent("edoc.deal",member.getName(),summary.getSubject(),currentAffair.getApp(), opinionType, opinionContent, opinionAttitude, -1).setImportantLevel(currentAffair.getImportantLevel());
	        if(currentAffair.getMemberId().longValue() != user.getId().longValue()){	
		    	receivers.add(new MessageReceiver(currentAffair.getId(), currentAffair.getMemberId(),"message.link.edoc.done",currentAffair.getId()));
				userMessageManager.sendSystemMessage(mc.add("edoc.agent.deal", user.getName()), appEnum, member.getId(), receivers,systemMessageFilterParam);
		    }else{
		     	userMessageManager.sendSystemMessage(mc, appEnum,member.getId(),receivers,systemMessageFilterParam);
		    }
        } catch (Exception e) {
        	LOGGER.error("", e);
        }
    }
    
    /**
     * 指定回退：发送消息
     * @param msgMap
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
	public static void appointStepBackMsg(Map<String, Object> msgMap) throws BusinessException {
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
    	String messageLinkWaitSend = "message.link.edoc.waitSend";
        String messageLinkSent = "message.link.edoc.sended";
        String messageLinkDone = "message.link.edoc.done";
        String messageLinkPending = "message.link.edoc.pending";
        if(sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
        		|| sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
    		messageLinkSent = messageLinkWaitSend;
    	}
        try {
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
	                    sendMsg4Affairs(userMessageManager, summary, currentAffair, backDoneDisappearLabel, messageLinkSent, messageLinkDone, doneAffairs, filterMemberIds);
	                    /** 指定退回到发起节点，流程重走，给所有待办/在办节点发消息 **/
	                    sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkDone, pendingAffairs, filterMemberIds);
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
	                    sendMsg4Affairs(userMessageManager, summary, currentAffair, backToMemberLabel, messageLinkSent, messageLinkPending, trackAffairs, filterMemberIds);
						break;
					
					default:
						break;
            	}
				/** 指定退回到发起节点，给发起人发送消息 **/
                sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkDone, sentAffairs, filterMemberIds);
                /** 指定退回到发起节点，给督办人发送消息 **/
                sendMsg4Receivers(userMessageManager, summary, currentAffair, backToMemberLabel, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds);
                /** 指定退回到发起节点，给当前人的代理人或者被代理人发送消息 **/
	            //sendCurrentAndAgent(userMessageManager, summary,currentAffair,messageLinkDone,filterMemberIds);
            } else {//指定回退给普通节点
                //这个地方重新查询了一下，目的是让例如被回退的节点的消息连接是可用的。而不是无效的。
            	ApplicationSubCategoryEnum subApp = EdocUtil.getSubAppCategoryByEdocType(summary.getEdocType());
                allAvailableAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, subApp, summaryId);
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
	                    sendMsg4Receivers(userMessageManager, summary, currentAffair, backLabel, pendings, filterMemberIds);
	                    /** 指定退回到一般节点，流程重走，给受影响的已办节点发消息 **/
	                    sendMsg4Receivers(userMessageManager, summary, currentAffair, backDoneDisappearLabel, dones, filterMemberIds);
	                    break;
	                    
					case 1:/** 提交回退者 **/
						break;
                }
                
                /** 指定退回到普通节点，给被退回节点(复合节点可能是多个人)发送消息 **/
                sendMsg4Affairs(userMessageManager, summary, currentAffair, backLabel, messageLinkSent, messageLinkPending, targetAffair, filterMemberIds);
                /** 指定退回到一般节点，给待办/在办/已办跟踪节点发消息 **/
                List<CtpAffair> trackAffairs = getTrackingAffairList(filterMemberIds, summaryId);
                sendMsg4Affairs(userMessageManager, summary, currentAffair, backToMemberLabel, messageLinkSent, messageLinkPending, trackAffairs, filterMemberIds);
                /** 指定退回到一般节点，给督办人发消息 **/
                sendMsg4Receivers(userMessageManager, summary, currentAffair, backToMemberLabel, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds);
            }
        }catch(BusinessException e ){
            LOGGER.error("指定回退发送消息异常", e);
        }
    }
    
    /**
     * 指定回退：获取指定回退督办人
     * @param summaryId
     * @param filterMembers
     * @param senderAffair
     * @return
     * @throws BusinessException
     */
    private static List<MessageReceiver> getSuperviseReceiver(Long summaryId, Set<Long> filterMembers,CtpAffair senderAffair) throws BusinessException{
    	SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
    	List<MessageReceiver> receivers = new  ArrayList<MessageReceiver>();
    	
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
    	
        if(superviseDetail != null){
        	String messageSuperviseLink = "message.link.edoc.supervise.detail";//给督办人发消息的连接
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!filterMembers.contains(colSupervisMemberId)) {
                    filterMembers.add(colSupervisMemberId);
                    receivers.add(new MessageReceiver(senderAffair.getId(), colSupervisMemberId, messageSuperviseLink, summaryId));
                }
            }
        }
        return receivers;
    }

	/**
	 * 指定回退：给相关节点发消息
	 * @param userMessageManager
	 * @param summary
	 * @param currentAffair
	 * @param label
	 * @param messageLinkSent
	 * @param messageLinkDone
	 * @param affairs
	 * @param filterMemberIds
	 * @throws BusinessException
	 */
    public static void sendMsg4Affairs(UserMessageManager userMessageManager, EdocSummary summary, CtpAffair currentAffair ,List<Object> label, String messageLinkSent, String messageLinkDone, List<CtpAffair> affairs,Set<Long> filterMemberIds) throws BusinessException {
        List<MessageReceiver> receivers= new  ArrayList<MessageReceiver>();
        String messageLink = messageLinkDone;
        for(CtpAffair a : affairs) {
            if (!a.isDelete()) {
            	messageLink = messageLinkDone;
            	if(a.getState() == StateEnum.col_sent.key() || a.getState() == StateEnum.col_waitSend.key()) {
            		messageLink = messageLinkSent;
            	}
                receivers .add(transMessageReceiver(a, messageLink));
            }
        }
        sendMsg4Receivers(userMessageManager, summary, currentAffair, label, receivers,filterMemberIds);
    }
    
    /**
     * 指定回退：给相关接收人发消息
     * @param userMessageManager
     * @param summary
     * @param currentAffair
     * @param label
     * @param receivers
     * @param filterMemberIds
     * @throws BusinessException
     */
    public static void sendMsg4Receivers(UserMessageManager userMessageManager, EdocSummary summary, 
            CtpAffair currentAffair, List<Object> label, List<MessageReceiver> receivers, Set<Long> filterMemberIds) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
        for(Iterator<MessageReceiver> it = receivers.iterator();it.hasNext();){
            MessageReceiver r =  it.next();
            //收消息的代理人
            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),r.getReceiverId());
            if(agentId!=null && !user.getId().equals(agentId) && !filterMemberIds.contains(agentId)){
               MessageReceiver ra =  new MessageReceiver(r.getReferenceId(), agentId, r.getLinkType());
               ra.setLinkParam(r.getLinkParam());
               agentReceivers.add(ra);
            }
            if(user.getId().equals(r.getReceiverId())){
                it.remove();
            }
        }
        
        Integer systemMessageFilterParam = getSystemMessageFilterParam(currentAffair).key;
        if(!user.getId().equals(currentAffair.getMemberId())){
            if(Strings.isNotEmpty(agentReceivers)){
                MessageContent mc1 = new MessageContent((String)label.get(0),(String)label.get(1),(String)label.get(2),(String)label.get(3));
                userMessageManager.sendSystemMessage(mc1.setImportantLevel(summary.getImportantLevel()).add("col.agent.deal", user.getName()).add("col.agent"),ApplicationCategoryEnum.edoc, currentAffair.getMemberId(), agentReceivers, systemMessageFilterParam);
            }
            MessageContent mc2 = new MessageContent((String)label.get(0),(String)label.get(1),(String)label.get(2),(String)label.get(3));
            userMessageManager.sendSystemMessage(mc2.setImportantLevel(summary.getImportantLevel()).add("col.agent.deal", user.getName()),ApplicationCategoryEnum.edoc, currentAffair.getMemberId(), receivers, systemMessageFilterParam);
        }else{
            if(Strings.isNotEmpty(agentReceivers)){
                MessageContent mc3 = new MessageContent((String)label.get(0),(String)label.get(1),(String)label.get(2),(String)label.get(3));
                userMessageManager.sendSystemMessage(mc3.setImportantLevel(summary.getImportantLevel()).add("col.agent"),ApplicationCategoryEnum.edoc, currentAffair.getSenderId(), agentReceivers,systemMessageFilterParam);
            }
            MessageContent mc4 = new MessageContent((String)label.get(0),(String)label.get(1),(String)label.get(2),(String)label.get(3));
            userMessageManager.sendSystemMessage(mc4.setImportantLevel(summary.getImportantLevel()),ApplicationCategoryEnum.edoc, currentAffair.getMemberId(), receivers, systemMessageFilterParam);
        }
    }
    
    /**
     * 指定回退：返回消息接收对象
     * @param ctpAffair
     * @param messageLink
     * @return
     */
    private static MessageReceiver transMessageReceiver(CtpAffair ctpAffair, String messageLink){
        return new MessageReceiver(ctpAffair.getId(), ctpAffair.getMemberId(), messageLink, ctpAffair.getId(), null);
    }
    
    /**
     * 指定回退：过滤跟踪节点
     * @param filterMemberIds
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    private static List<CtpAffair> getTrackingAffairList(Set<Long> filterMemberIds, Long summaryId) throws BusinessException {
    	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
    	CtpTrackMemberManager trackManager = (CtpTrackMemberManager)AppContext.getBean("trackManager");
        // 设置了获取跟踪的affair
        List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summaryId);
        
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        
        trackingAffairList = getTrackAffairExcludePart(trackingAffairList, trackMembers, AppContext.currentUserId());
        for(Iterator<CtpAffair> it = trackingAffairList.iterator();it.hasNext();){
            CtpAffair a = it.next();
            if(!filterMemberIds.contains(a.getMemberId())){
                filterMemberIds.add(a.getMemberId());
            }else{
                it.remove();
            }
        }
        return trackingAffairList;
    }
    /**************** 指定回退消息 end ******************/
    /**
     * 发送公文移交信息
     * @param affairManager
     * @param orgManager
     * @param userMessageManager
     * @param user
     * @param summary   公文
     * @param affairs	移交的事项
     * @param oldAffair 原affair
     * @param transferOpinion 移交意见
     * @return
     * @throws BusinessException
     */
    public static boolean sendMessage4EdocTransfer(AffairManager affairManager, OrgManager orgManager,UserMessageManager userMessageManager,User user, EdocSummary summary, List<CtpAffair> affairs,CtpAffair oldAffair, EdocOpinion transferOpinion) throws BusinessException{
        try {
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            List<String> names = new ArrayList<String>();
            Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
            String messageLinkSent = "message.link.edoc.sended";
            String messageLinkDone = "message.link.edoc.done";
            String messageLinkPending = "message.link.edoc.pending";
            //发送消息需要排除的人员集合
            Set <Long> members = new HashSet<Long>();
            for(CtpAffair c : affairs){
                names.add(orgManager.getMemberById(c.getMemberId()).getName());
                receivers.add(new MessageReceiver(c.getId(), c.getMemberId(), messageLinkPending, c.getId(), ""));
                members.add(c.getMemberId());
                //代理人
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),c.getMemberId());
                if (agentId!=null) {
                	receivers1.add(new MessageReceiver(c.getId(), agentId, messageLinkPending, c.getId(), ""));
                	members.add(agentId);
                }
            }
    		//移交前的人员
    		V3xOrgMember oldAffairMember = orgManager.getMemberById(oldAffair.getMemberId());

            Set<Long> memberFilters = new HashSet<Long>();
            memberFilters.add(user.getId());
            List<CtpAffair>  trackingAffairList = getTrackingAffairList(memberFilters,summary.getId());
            //跟踪人
            Set<MessageReceiver> trackRecievers = new HashSet<MessageReceiver>();
            for (CtpAffair trackingAffair : trackingAffairList) {
                //过滤无效事项
                if(trackingAffair.isDelete()){
                    continue;
                }
                String messageUrl = messageLinkDone;
                if(Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingAffair.getState())) {
                    messageUrl = messageLinkSent;
                } 
                if (!trackingAffair.isDelete()) {
                	trackRecievers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),messageUrl,trackingAffair.getId().toString()));
                }
            }
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
            
            Integer app = oldAffair.getApp();
            //获取推送消息人员列表
            List<Long[]> pushMemberIds = DateSharedWithWorkflowEngineThreadLocal.getPushMessageMembers();
            for(Long[] push :pushMemberIds ){
	        	if(!user.getId().equals(push[1]) && !members.contains(push[1])){
	        		trackRecievers.add(new MessageReceiver(push[0], push[1],messageLinkDone,push[0]));
	        	}
	        }
            String opinionContent = UserMessageUtil.getComment4Message(transferOpinion.getContent());
        	//回退时 意见加了附件，则回退消息中加附字
        	if(transferOpinion.isHasAtt()){
        		opinionContent += EdocHelper.getOpinionAttStr();
        	}
        	int opinionType = Strings.isTrue(transferOpinion.getIsHidden()) ? 0 : Strings.isBlank(opinionContent) ? -1 : 1;
            
            Integer important = summary.getImportantLevel();
            
            Integer systemMessageFilterParam = getSystemMessageFilterParam(oldAffair).key;
			//代理人处理时   发送消息
            if(!user.getId().equals(oldAffair.getMemberId())){
            	//xx移交公文《》给你，请及时处理！
            	userMessageManager.sendSystemMessage(new MessageContent("edoc.transfer", summary.getSubject(), oldAffairMember.getName() ,oldAffair.getApp(), opinionType, opinionContent,1).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource")
        				.add("edoc.agent.deal", user.getName()).setImportantLevel(important), oldAffair.getApp(),user.getId(), receivers, systemMessageFilterParam);
            	if (receivers1.size() > 0) {
            		userMessageManager.sendSystemMessage(new MessageContent("edoc.transfer", summary.getSubject(), oldAffairMember.getName() ,oldAffair.getApp(), opinionType, opinionContent,1).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource")
            				.add("edoc.agent.deal", user.getName()).setImportantLevel(important), oldAffair.getApp(),user.getId(), receivers1, systemMessageFilterParam);
                }
            	if(Strings.isNotEmpty(trackRecievers)){
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.node.affair.transfer2", summary.getSubject(), oldAffairMember.getName(), 
                            StringUtils.join(names.iterator(), ","), opinionType,opinionContent,oldAffair.getApp()).add("edoc.agent.deal", user.getName()).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource").setImportantLevel(important), 
                            oldAffair.getApp(), user.getId(), trackRecievers, summary.getImportantLevel());
                }
            } else {
            	userMessageManager.sendSystemMessage(new MessageContent("edoc.transfer", summary.getSubject(), oldAffairMember.getName() ,oldAffair.getApp(), opinionType, opinionContent,1).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource")
        				.setImportantLevel(important), oldAffair.getApp(),user.getId(), receivers, systemMessageFilterParam);
            	if (receivers1.size() > 0) {
            		userMessageManager.sendSystemMessage(new MessageContent("edoc.transfer", summary.getSubject(), oldAffairMember.getName() ,oldAffair.getApp(), opinionType, opinionContent,1).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource")
            				.setImportantLevel(important), oldAffair.getApp(),user.getId(), receivers1, systemMessageFilterParam);
                }
            	if(Strings.isNotEmpty(trackRecievers)){
                    userMessageManager.sendSystemMessage(new MessageContent("edoc.node.affair.transfer2", summary.getSubject(), oldAffairMember.getName(), 
                            StringUtils.join(names.iterator(), ","), opinionType,opinionContent,oldAffair.getApp()).setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource").setImportantLevel(important), 
                            oldAffair.getApp(), user.getId(), trackRecievers, summary.getImportantLevel());
                }
            	}
        } catch (BusinessException e) {
        	LOGGER.error("移交公文发送消息报错！", e);
        }
    	return true;
    	
    }
}