package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocArchiveModifyLog extends BaseModel  implements Serializable{
	//default serial version id, required for serializable classes.
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6603717569283592630L;
	/**
	 * 
	 */
	private Long summaryId;
	private Long userId;
	private String updatePerson;
	private int modifyContent;
	private int modifyForm;
	private int modifyAtt;
	private Date updateTime;
	
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUpdatePerson() {
		return updatePerson;
	}
	public void setUpdatePerson(String updatePerson) {
		this.updatePerson = updatePerson;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public int getModifyContent() {
		return modifyContent;
	}
	public void setModifyContent(int modifyContent) {
		this.modifyContent = modifyContent;
	}
	public int getModifyForm() {
		return modifyForm;
	}
	public void setModifyForm(int modifyForm) {
		this.modifyForm = modifyForm;
	}
	public int getModifyAtt() {
		return modifyAtt;
	}
	public void setModifyAtt(int modifyAtt) {
		this.modifyAtt = modifyAtt;
	}
	
	
}
