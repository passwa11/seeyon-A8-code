package com.seeyon.apps.govdoc.vo;

import java.io.Serializable;

public class GovdocLockParam implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer sortNo;
	private Long ownerId;
	private String ownerName;
	private String lockTime;
	
	private Long summaryId;
	private Long fromRecordId;
	
	public Integer getSortNo() {
		return sortNo;
	}
	public void setSortNo(Integer sortNo) {
		this.sortNo = sortNo;
	}
	public Long getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getLockTime() {
		return lockTime;
	}
	public void setLockTime(String lockTime) {
		this.lockTime = lockTime;
	}
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public Long getFromRecordId() {
		return fromRecordId;
	}
	public void setFromRecordId(Long fromRecordId) {
		this.fromRecordId = fromRecordId;
	}
}
