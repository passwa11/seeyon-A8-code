package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSuperviseRemind extends BaseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2263535332094060035L;
	private Long superviseId;
	private Long senderId;
	private Long receiverId;
	private Date sendTime;
	private String content;
	private Integer mode;
	private Boolean isSended;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getMode() {
		return mode;
	}
	public void setMode(Integer mode) {
		this.mode = mode;
	}
	public Long getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(Long receiverId) {
		this.receiverId = receiverId;
	}
	public Long getSenderId() {
		return senderId;
	}
	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public Long getSuperviseId() {
		return superviseId;
	}
	public void setSuperviseId(Long superviseId) {
		this.superviseId = superviseId;
	}
	public Boolean getIsSended() {
		return isSended;
	}
	public void setIsSended(Boolean isSended) {
		this.isSended = isSended;
	}
	
	
}
