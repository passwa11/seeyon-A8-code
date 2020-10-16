package com.seeyon.apps.wpstrans.po;

import java.sql.Timestamp;

import com.seeyon.ctp.common.po.BasePO;

public class WpsTransRecord extends BasePO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long objectId;
	private Long sourceFileId;
	private Long destFileId;
	private String subject;
	private Integer app;
	private Long affairId;
	private Long memberId;
	private Timestamp createDate;
	private Timestamp updateDate;
	private Integer status;
	private String message;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public Long getSourceFileId() {
		return sourceFileId;
	}
	public void setSourceFileId(Long sourceFileId) {
		this.sourceFileId = sourceFileId;
	}
	public Long getDestFileId() {
		return destFileId;
	}
	public void setDestFileId(Long destFileId) {
		this.destFileId = destFileId;
	}
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public Timestamp getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}
	public Integer getApp() {
		return app;
	}
	public void setApp(Integer app) {
		this.app = app;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

}
