package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSuperviseLog extends BaseModel implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4799928052029504282L;
	private Long superviseId;
	private Integer type;
	private String sender;
	private String receiver;
	private Date sendTime;
	private Integer mode;
	private String content;
	private Long parallelismId;
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
	public Long getParallelismId() {
		return parallelismId;
	}
	public void setParallelismId(Long parallelismId) {
		this.parallelismId = parallelismId;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public Long getSuperviseId() {
		return superviseId;
	}
	public void setSuperviseId(Long superviseId) {
		this.superviseId = superviseId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	
	
}
