package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;

public class QwqpEdocFormFileRelation extends BasePO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4825789635313045696L;

	private Long id;
	private Long fileId; 
	private Long formId;
	private boolean doubleForm = true;
	private String fileType;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	public boolean getDoubleForm() {
		return doubleForm;
	}
	public void setDoubleForm(boolean doubleForm) {
		this.doubleForm = doubleForm;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
}
