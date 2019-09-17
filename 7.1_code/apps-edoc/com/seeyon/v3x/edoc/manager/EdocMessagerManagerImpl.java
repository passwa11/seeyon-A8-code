package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.joinwork.bpm.engine.wapi.WorkItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocMessagerManagerImpl extends  EdocManagerImpl implements EdocMessagerManager{
	private final static Log log = LogFactory.getLog(EdocMessagerManagerImpl.class);
	@Override 
    public void sendOperationTypeMessage(String messageDataListJSON,EdocSummary summary,CtpAffair affair){
		sendOperationTypeMessage(messageDataListJSON,summary,affair,null);
	}
	@Override 
    public void sendOperationTypeMessage(String messageDataListJSON,EdocSummary summary,CtpAffair affair,Long commentId) {
        List<Map<String, Object>> newMessageDataList = null;
        if (null == messageDataListJSON || "".equals(messageDataListJSON.trim())) {
            newMessageDataList = new ArrayList<Map<String, Object>>();
        } else {
            newMessageDataList = (List<Map<String, Object>>) JSONUtil.parseJSONString(messageDataListJSON);
        }
        User user = AppContext.getCurrentUser();
        for(Map<String, Object> map:newMessageDataList){
           // if(user.getId().toString().equals(map.get("handlerId").toString())){  ---因为要考虑到代理人，所以注释掉这里
                String operationType = map.get("operationType").toString();
                String partyNames = map.get("partyNames").toString();
                List<String> names = Arrays.asList(partyNames.split("[,]"));
                List<String> partyNameList = new ArrayList<String>();
                for (String name : names) {
                	if (!partyNameList.contains(name)) {
                		partyNameList.add(name);
                	}
                }
                String processLogParam = map.get("processLogParam").toString();
                if(commentId == null){
                    if("insertPeople".equals(operationType)){//加签
                    	this.insertPeopleMessage(affairManager, userMessageManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertPeople, processLogParam);
                    }else if("deletePeople".equals(operationType)){//减签
                        this.deletePeopleMessage(affairManager, orgManager, userMessageManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.deletePeople, processLogParam);
                    }else if("colAssign".equals(operationType)){//当前会签
                        this.colAssignMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.colAssign, processLogParam);
                    }else if("addInform".equals(operationType)){
                         this.addInformMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.inform, processLogParam);
                    }else if("addPassRead".equals(operationType)){//传阅
                    	this.addPassReadMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                         processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.passRound, processLogParam);
                    }else if("addMoreSign".equals(operationType)){//多级会签
    	               	 this.moreSignMessage(affairManager, userMessageManager, orgManager, partyNameList, summary, affair);
    	                 processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.addMoreSign, processLogParam);
    	            }
                }else{
                    if("insertPeople".equals(operationType)){//加签
                    	this.insertPeopleMessage(affairManager, userMessageManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.insertPeople,commentId, processLogParam);
                    }else if("deletePeople".equals(operationType)){//减签
                        this.deletePeopleMessage(affairManager, orgManager, userMessageManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.deletePeople, commentId,processLogParam);
                    }else if("colAssign".equals(operationType)){//当前会签
                        this.colAssignMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.colAssign,commentId, processLogParam);
                    }else if("addInform".equals(operationType)){
                         this.addInformMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.inform,commentId, processLogParam);
                    }else if("addPassRead".equals(operationType)){//传阅
                    	this.addPassReadMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                         processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.passRound,commentId, processLogParam);
                    }else if("addMoreSign".equals(operationType)){//多级会签
    	               	 this.moreSignMessage(affairManager, userMessageManager, orgManager, partyNameList, summary, affair);
    	                 processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),affair.getActivityId(), ProcessLogAction.addMoreSign, commentId,processLogParam);
    	            }
                }
        }
    }
	
	 //加签消息提醒
    public Boolean insertPeopleMessage(AffairManager affairManager, UserMessageManager userMessageManager,
            OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair) {
        Integer importantLevel = affair.getImportantLevel();
        User user = AppContext.getCurrentUser();
        try {
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
            List<Long> memberIds = new ArrayList<Long>();
            for (CtpAffair trackingAffair : trackingAffairList) {
            	if (!memberIds.contains(trackingAffair.getMemberId())) {
            		receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",
                        trackingAffair.getId().toString()));
            		memberIds.add(trackingAffair.getMemberId());
            	}
            }
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if(Strings.isNotBlank(affair.getForwardMember())) {
            	forwardMember = affair.getForwardMember();
                forwardMemberFlag = 1;
            }
            Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
            if(!user.getId().equals(affair.getMemberId() )){
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId() );
                String proxyName = member.getName();
                userMessageManager.sendSystemMessage(new MessageContent("edoc.addAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), affair.getApp() ,forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName()).setImportantLevel(importantLevel), 
	                ApplicationCategoryEnum.edoc, 
	                affair.getMemberId() , 
	                receivers, 
	                systemMessageFilterParam);
            }else{
                userMessageManager.sendSystemMessage(new MessageContent("edoc.addAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp() ,forwardMemberFlag,forwardMember).setImportantLevel(importantLevel), 
                    ApplicationCategoryEnum.edoc, 
                    user.getId(), 
                    receivers, 
                    systemMessageFilterParam);
            }
        } catch (Exception e) {
            log.error("send message failed", e);
            return false;
        }
        return true;
    }
	//减签消息提醒
    public Boolean deletePeopleMessage(AffairManager affairManager, OrgManager orgManager, 
            UserMessageManager userMessageManager, List<String> partyNames, EdocSummary summary, CtpAffair affair) {
        Integer importantLevel = affair.getImportantLevel();
        User user = AppContext.getCurrentUser();
        try {
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
            List<Long> memberIds = new ArrayList<Long>();
            for (CtpAffair trackingAffair : trackingAffairList) {
            	 if(!memberIds.contains(trackingAffair.getMemberId())) {
            		 memberIds.add(trackingAffair.getMemberId());
            		 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
            	 }
                 
            }
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if(Strings.isNotBlank(affair.getForwardMember())){
            	forwardMember = affair.getForwardMember();
                forwardMemberFlag = 1;
            }
           
            Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
            
            if(!user.getId().equals(affair.getMemberId())){
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                userMessageManager.sendSystemMessage(new MessageContent("edoc.decreaseAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), affair.getApp() ,forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName()).setImportantLevel(importantLevel), 
	                affair.getApp(), 
	                affair.getMemberId(), 
	                receivers, 
	                systemMessageFilterParam);
            }else{
                userMessageManager.sendSystemMessage(new MessageContent("edoc.decreaseAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), affair.getApp(), forwardMember).setImportantLevel(importantLevel), 
                    ApplicationCategoryEnum.edoc, 
                    user.getId(), 
                    receivers, 
                    systemMessageFilterParam);
            }
        } catch (Exception e) {
            log.error("send message failed", e);
            return false;
        }
        return true;
    }
    
	
	 //知会消息提醒
	    public Boolean addInformMessage(UserMessageManager userMessageManager, AffairManager affairManager,
	            OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair) {
	        Integer importantLevel = affair.getImportantLevel();
	        User user = AppContext.getCurrentUser();
	        try {
	        	Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
	            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	                List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
	                List<Long> memberIds = new ArrayList<Long>();
	                for (CtpAffair trackingAffair : trackingAffairList) {
	                	 if(!memberIds.contains(trackingAffair.getMemberId())) {
	                		 memberIds.add(trackingAffair.getMemberId());
	                		 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
	                	 }
	                     
	                }
	                int forwardMemberFlag = 0;
	                String forwardMember = null;
	                if(Strings.isNotBlank(affair.getForwardMember())) {
	                	forwardMember = affair.getForwardMember();
	                	forwardMemberFlag = 1;
	                }
	                if(!user.getId().equals(affair.getMemberId())){
	                    V3xOrgMember member = null;
	                    member = getMemberById(orgManager, affair.getMemberId());
	                    String proxyName = member.getName();
	                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addInform", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), affair.getApp() ,forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName()).setImportantLevel(importantLevel), 
	                    		affair.getApp(), 
	                    		affair.getMemberId(), 
	                    		receivers, 
	                    		systemMessageFilterParam);
	                }else{
	                    userMessageManager.sendSystemMessage(new MessageContent("edoc.addInform", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), affair.getApp(), forwardMember).setImportantLevel(importantLevel), 
	                    		affair.getApp(), 
	                    		user.getId(), 
	                    		receivers, 
	                    		systemMessageFilterParam);
	                }
	        } catch (Exception e) {
	            log.error("send message failed", e);
	            return false;
	        }
	        return true;
	    }
	    
	    public V3xOrgMember getMemberById(OrgManager orgManager, Long memberId){
	        V3xOrgMember member = null;
	        try {
	            member = orgManager.getEntityById(V3xOrgMember.class, memberId);
	        } catch (BusinessException e) {
	            log.error("获取协同消息提醒对应人员失败", e);
	            return null;
	        }
	        return member;
	    }
	    
	    //会签消息提醒
	    public Boolean colAssignMessage(UserMessageManager userMessageManager, AffairManager affairManager,
	            OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair) {
	        Integer importantLevel = affair.getImportantLevel();
	        User user = AppContext.getCurrentUser();
	        try {
	            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
	                List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
	                List<Long> memberIds = new ArrayList<Long>();
	                for (CtpAffair trackingAffair : trackingAffairList) {
	                	 if(!memberIds.contains(trackingAffair.getMemberId())) {
	                		 memberIds.add(trackingAffair.getMemberId());
	                		 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
	                	 }
	                     
	                }
	                int forwardMemberFlag = 0;
	                String forwardMember = null;
	                if(Strings.isNotBlank(affair.getForwardMember())) {
                    	forwardMember = affair.getForwardMember();
                        forwardMemberFlag = 1;
	                }
	                
	                Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
	                if(!user.getId().equals(affair.getMemberId())){
	                    V3xOrgMember member = null;
	                    member = getMemberById(orgManager,affair.getMemberId());
	                    String proxyName = member.getName();
	                    userMessageManager.sendSystemMessage(new MessageContent("edoc.colAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), affair.getApp() ,forwardMemberFlag, forwardMember)
	                    .add("col.agent.deal", user.getName()).setImportantLevel(importantLevel), affair.getApp(), affair.getMemberId(), receivers, systemMessageFilterParam);
	                }else{
	                    userMessageManager.sendSystemMessage(new MessageContent("edoc.colAssign", summary.getSubject(), user.getName(),StringUtils.join(partyNames.iterator(), ","),affair.getApp(),  forwardMemberFlag, forwardMember)
	                        .setImportantLevel(importantLevel), affair.getApp(), user.getId(), receivers, systemMessageFilterParam);
	                }
	        } catch (Exception e) {
	            log.error("send message failed", e);
	            return false;
	        }
	        return true;
	    }
   //传阅消息提醒
	public  Boolean addPassReadMessage(UserMessageManager userMessageManager, AffairManager affairManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		User user = AppContext.getCurrentUser();
		ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
		Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
		try {
            if (partyNames != null) {
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                List<Long> memberIds = new ArrayList<Long>();
                for (CtpAffair trackingAffair : trackingAffairList) {
                	 if(!memberIds.contains(trackingAffair.getMemberId())) {
                		 memberIds.add(trackingAffair.getMemberId());
                		 receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",trackingAffair.getId().toString()));
                	 }
                     
                }
                if(affair.getMemberId().longValue() != user.getId().longValue()){
    				V3xOrgMember member = null;
    				member = getMemberById(orgManager, affair.getMemberId());
    			    String proxyName = member.getName();
    			    userMessageManager.sendSystemMessage(new MessageContent("edoc.addPassRead", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
    				.add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
                }else{
                	userMessageManager.sendSystemMessage(new MessageContent("edoc.addPassRead", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (Exception e) {
            log.error("send message failed", e);
        }
		return true;
	}
	//多级会签消息提醒
	public  Boolean moreSignMessage(AffairManager affairManager, UserMessageManager userMessageManager,
			OrgManager orgManager, List<String> partyNames, EdocSummary summary, CtpAffair affair){
		User user = AppContext.getCurrentUser();
		ApplicationCategoryEnum appEnum=ApplicationCategoryEnum.valueOf(affair.getApp());
		Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
		try {
            if (partyNames != null) {
            	List<Long> msgMemberIdList = new ArrayList<Long>();
            	Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                List<CtpAffair> trackingAffairList = affairManager.getValidTrackAffairs(summary.getId());
                for (CtpAffair trackingAffair : trackingAffairList) {
                	if(!msgMemberIdList.contains(trackingAffair.getMemberId())) {
                		msgMemberIdList.add(trackingAffair.getMemberId());
                		receivers.add(new MessageReceiver(trackingAffair.getId(), trackingAffair.getMemberId(),"message.link.edoc.done",
                			trackingAffair.getId().toString()));
                	}
                }
                msgMemberIdList.add(affair.getMemberId());
                if(affair.getMemberId().longValue() != user.getId().longValue()){
                	V3xOrgMember member = null;
                	member = getMemberById(orgManager, affair.getMemberId());
                	String proxyName = member.getName();
                	userMessageManager.sendSystemMessage(new MessageContent("edoc.addMoreAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","),affair.getApp())
                			.add("edoc.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel()), appEnum, affair.getMemberId(), receivers,systemMessageFilterParam);
                }else{
                	userMessageManager.sendSystemMessage(new MessageContent("edoc.addMoreAssign", summary.getSubject(), user.getName(), 
                			StringUtils.join(partyNames.iterator(), ","),affair.getApp()).setImportantLevel(affair.getImportantLevel()), appEnum, user.getId(), receivers,systemMessageFilterParam);
                }
            }
        } catch (Exception e) {
            log.error("send message failed", e);
            return false;
        }
		return true;
	}
	
	//给在督办中被删除的affair发送消息提醒
    public Boolean superviseDelete(WorkItem workitem, List<CtpAffair> affairs) {
        if(affairs == null || affairs.isEmpty()){
            return true;
        }
        User user = AppContext.getCurrentUser();
        String userName = "";
        if (user != null) {
            userName = user.getName();
        }
        Integer importmentLevel = affairs.get(0).getImportantLevel();
        try{
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            CtpAffair firstAffair = affairs.get(0);
            for(CtpAffair affair : affairs){
           
                receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId()));
            }
            Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(firstAffair).key;
            userMessageManager.sendSystemMessage(new MessageContent("edoc.delete", firstAffair.getSubject(), userName, firstAffair.getApp()).setImportantLevel(importmentLevel), 
            		ApplicationCategoryEnum.valueOf(firstAffair.getApp()), 
            		firstAffair.getSenderId(), 
            		receivers,
            		systemMessageFilterParam);
        } catch (Exception e) {
            log.error("发送消息异常", e);
        }
        return true;
    }
    
    
    public List<MessageReceiver> getSuperviseReceiver(Long summaryId,Set<Long> filterMembers,CtpAffair senderAffair){
        List<MessageReceiver> receivers = new  ArrayList<MessageReceiver>();
			 CtpSuperviseDetail	superviseDetail = superviseManager.getSupervise(summaryId);
        if(superviseDetail != null){
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for(CtpSupervisor colSupervisor : colSupervisorSet){
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!filterMembers.contains(colSupervisMemberId)) {
                    filterMembers.add(colSupervisMemberId);
                    receivers.add(new MessageReceiver(senderAffair.getId(), colSupervisMemberId,"message.link.edoc.supervise.detail", summaryId));
                }
            }
        }
        return receivers;
    }

	@Override
	public void sendMsg4Receivers(EdocSummary summary, CtpAffair currentAffair,
			List<Object> m, String messageLinkSent,
			List<MessageReceiver> receivers, Set<Long> filterMemberIds) throws BusinessException {
	        User user = AppContext.getCurrentUser();
	        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
	        for(Iterator<MessageReceiver> it = receivers.iterator();it.hasNext();){
	            MessageReceiver r =  it.next();
	            //收消息的代理人
	            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(),r.getReceiverId());
	            if(agentId!=null && !user.getId().equals(agentId) && !filterMemberIds.contains(agentId)){
	                   MessageReceiver ra =  new MessageReceiver(r.getReferenceId(), agentId, r.getLinkType());
	                   ra.setLinkParam(r.getLinkParam());
	                   agentReceivers.add(ra);
	            }
	            if(user.getId().equals(r.getReceiverId())){
	                it.remove();
	            }
	        }
	        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(currentAffair).key;
	        if(!user.getId().equals(currentAffair.getMemberId())){
	            if(Strings.isNotEmpty(agentReceivers)){
	                MessageContent mc1 = new MessageContent((String)m.get(0),(String)m.get(1),(String)m.get(2),(String)m.get(3));
	                userMessageManager.sendSystemMessage(mc1.setImportantLevel(summary.getImportantLevel()).add("col.agent.deal", user.getName()).add("col.agent"),ApplicationCategoryEnum.edoc, currentAffair.getMemberId(), agentReceivers, receivers,systemMessageFilterParam);
	            }
	            MessageContent mc2 = new MessageContent((String)m.get(0),(String)m.get(1),(String)m.get(2),(String)m.get(3));
	            userMessageManager.sendSystemMessage(mc2.setImportantLevel(summary.getImportantLevel()).add("col.agent.deal", user.getName()),ApplicationCategoryEnum.edoc, currentAffair.getMemberId(), receivers,receivers,systemMessageFilterParam);
	        }else{
	            if(Strings.isNotEmpty(agentReceivers)){
	                MessageContent mc3 = new MessageContent((String)m.get(0),(String)m.get(1),(String)m.get(2),(String)m.get(3));
	                userMessageManager.sendSystemMessage(mc3.setImportantLevel(summary.getImportantLevel()).add("col.agent"),ApplicationCategoryEnum.edoc, currentAffair.getSenderId(), agentReceivers, receivers,systemMessageFilterParam);
	            }
	            MessageContent mc4 = new MessageContent((String)m.get(0),(String)m.get(1),(String)m.get(2),(String)m.get(3));
	            userMessageManager.sendSystemMessage(mc4.setImportantLevel(summary.getImportantLevel()),ApplicationCategoryEnum.edoc,currentAffair.getMemberId(), receivers, receivers,systemMessageFilterParam);
	        }
	    }
	 public void transSendMsg4ProcessOverTime(CtpAffair affair,List<MessageReceiver> receivers, List<MessageReceiver> agentReceivers){
	        
	        String forwardMemberId = affair.getForwardMember();
	        int forwardMemberFlag = 0;
	        String forwardMember = null;
	        if(Strings.isNotBlank(forwardMemberId)){
	            try {
	                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
	                forwardMemberFlag = 1;
	            }catch (Exception e) {
	                log.error("", e);
	            }
	        }
	        
	        String msgKey = "process.summary.overTerm.edoc";
	        String title = affair.getSubject();
	        Integer importantLevel = affair.getImportantLevel();
	        Long senderId = affair.getSenderId();
	        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(affair).key;
	        if(Strings.isNotEmpty(receivers)){
	            try {
	                userMessageManager.sendSystemMessage(MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel), ApplicationCategoryEnum.valueOf(affair.getApp()), senderId, receivers,systemMessageFilterParam);
	            } catch (BusinessException e) {
	                log.error("", e);
	            }
	        }
	        
	        if(Strings.isNotEmpty(agentReceivers)){
	            try {
	                userMessageManager.sendSystemMessage(MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent"),  ApplicationCategoryEnum.valueOf(affair.getApp()), senderId, agentReceivers,systemMessageFilterParam);
	            } catch (BusinessException e) {
	              log.error("", e);
	            }
	        }
	    } 
		
}
