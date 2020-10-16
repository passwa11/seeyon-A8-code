package com.seeyon.v3x.edoc.quartz;

import java.util.Map;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.usermessage.MessageReceiver;

public class ProcessMsgParamBO {
	private String subject;
    private String messageSentLink;
    private String messagePendingLink;
    private ApplicationCategoryEnum appEnum ;
    private Long processSenderId;
    private Integer importantLevel ;//信息报送没有重要程度
    
    //以下四个属性应用不设置，组件统一设置。
	private int forwardMemberFlag;
	private String forwardMember;
	private Map<Long, MessageReceiver> receiverMap ;
	private Map<Long, MessageReceiver> receiverAgentMap ;
	private Map<Long, MessageReceiver> doneReceiverMap ;
	private Map<Long, MessageReceiver> trackReceiverMap ;
	
	
	
	
    public Map<Long, MessageReceiver> getReceiverMap() {
		return receiverMap;
	}
	public void setReceiverMap(Map<Long, MessageReceiver> receiverMap) {
		this.receiverMap = receiverMap;
	}
	public Map<Long, MessageReceiver> getReceiverAgentMap() {
		return receiverAgentMap;
	}
	public void setReceiverAgentMap(Map<Long, MessageReceiver> receiverAgentMap) {
		this.receiverAgentMap = receiverAgentMap;
	}
	public int getForwardMemberFlag() {
		return forwardMemberFlag;
	}
	public void setForwardMemberFlag(int forwardMemberFlag) {
		this.forwardMemberFlag = forwardMemberFlag;
	}
	public String getForwardMember() {
		return forwardMember;
	}
	public void setForwardMember(String forwardMember) {
		this.forwardMember = forwardMember;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getMessageSentLink() {
		return messageSentLink;
	}
	public void setMessageSentLink(String messageSentLink) {
		this.messageSentLink = messageSentLink;
	}
	public String getMessagePendingLink() {
		return messagePendingLink;
	}
	public void setMessagePendingLink(String messagePendingLink) {
		this.messagePendingLink = messagePendingLink;
	}
	public ApplicationCategoryEnum getAppEnum() {
		return appEnum;
	}
	public void setAppEnum(ApplicationCategoryEnum appEnum) {
		this.appEnum = appEnum;
	}
	public Long getProcessSenderId() {
		return processSenderId;
	}
	public void setProcessSenderId(Long processSenderId) {
		this.processSenderId = processSenderId;
	}
	public Integer getImportantLevel() {
		return importantLevel;
	}
	public void setImportantLevel(Integer importantLevel) {
		this.importantLevel = importantLevel;
	}
	public Map<Long, MessageReceiver> getDoneReceiverMap() {
		return doneReceiverMap;
	}
	public Map<Long, MessageReceiver> getTrackReceiverMap() {
		return trackReceiverMap;
	}
	public void setDoneReceiverMap(Map<Long, MessageReceiver> doneReceiverMap) {
		this.doneReceiverMap = doneReceiverMap;
	}
	public void setTrackReceiverMap(Map<Long, MessageReceiver> trackReceiverMap) {
		this.trackReceiverMap = trackReceiverMap;
	}
    
    
}
