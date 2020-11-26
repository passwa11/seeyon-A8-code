package com.seeyon.apps.collaboration.bo;

public class FormLockParam {
	private Integer affairState;
	private Long formAppId;
	private Long formRecordId ;
	private String rightId;
	private String nodePolicy;
	private Boolean affairReadOnly;
	private Long affairId ;
	
	
	
	
	
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public String getRightId() {
		return rightId;
	}
	public void setRightId(String rightId) {
		this.rightId = rightId;
	}
	public String getNodePolicy() {
		return nodePolicy;
	}
	public void setNodePolicy(String nodePolicy) {
		this.nodePolicy = nodePolicy;
	}
	public Integer getAffairState() {
		return affairState;
	}
	public void setAffairState(Integer affairState) {
		this.affairState = affairState;
	}
	public Long getFormAppId() {
		return formAppId;
	}
	public void setFormAppId(Long formAppId) {
		this.formAppId = formAppId;
	}
	public Long getFormRecordId() {
		return formRecordId;
	}
	public void setFormRecordId(Long formRecordId) {
		this.formRecordId = formRecordId;
	}
	public Boolean getAffairReadOnly() {
		return affairReadOnly;
	}
	public void setAffairReadOnly(Boolean affairReadOnly) {
		this.affairReadOnly = affairReadOnly;
	}
	
	
}
