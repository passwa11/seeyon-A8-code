package com.seeyon.apps.govdoc.vo;


import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.filemanager.manager.AttachmentEditHelper;

public class GovdocCommentVO extends GovdocBaseVO {
	
	private String modifyContent;
	private String pishiYears; 
	private String pishinos; 
	private String proxydates; 
	private Long editOpinionId; 
	private AttachmentEditHelper attachmentEditHelper; 
	private Long affairId; 
	private String HASSAVEDATTACHMENT; 
	private String isEditAttachment; 
	private String clearAttr;
	private boolean hasEditAtt = false;
	private Boolean returnType = true;
	private String outInfo;
	
	private HttpServletRequest request;
	
	public String getModifyContent() {
		return modifyContent;
	}
	public void setModifyContent(String modifyContent) {
		this.modifyContent = modifyContent;
	}
	public String getPishiYears() {
		return pishiYears;
	}
	public void setPishiYears(String pishiYears) {
		this.pishiYears = pishiYears;
	}
	public String getPishinos() {
		return pishinos;
	}
	public void setPishinos(String pishinos) {
		this.pishinos = pishinos;
	}
	public String getProxydates() {
		return proxydates;
	}
	public void setProxydates(String proxydates) {
		this.proxydates = proxydates;
	}
	public Long getEditOpinionId() {
		return editOpinionId;
	}
	public void setEditOpinionId(Long editOpinionId) {
		this.editOpinionId = editOpinionId;
	}
	public AttachmentEditHelper getAttachmentEditHelper() {
		return attachmentEditHelper;
	}
	public void setAttachmentEditHelper(AttachmentEditHelper attachmentEditHelper) {
		this.attachmentEditHelper = attachmentEditHelper;
	}
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public String getHASSAVEDATTACHMENT() {
		return HASSAVEDATTACHMENT;
	}
	public void setHASSAVEDATTACHMENT(String hASSAVEDATTACHMENT) {
		HASSAVEDATTACHMENT = hASSAVEDATTACHMENT;
	}
	public String getIsEditAttachment() {
		return isEditAttachment;
	}
	public void setIsEditAttachment(String isEditAttachment) {
		this.isEditAttachment = isEditAttachment;
	}
	public String getClearAttr() {
		return clearAttr;
	}
	public void setClearAttr(String clearAttr) {
		this.clearAttr = clearAttr;
	}
	public boolean isHasEditAtt() {
		return hasEditAtt;
	}
	public void setHasEditAtt(boolean hasEditAtt) {
		this.hasEditAtt = hasEditAtt;
	}
	public Boolean getReturnType() {
		return returnType;
	}
	public void setReturnType(Boolean returnType) {
		this.returnType = returnType;
	}
	public String getOutInfo() {
		return outInfo;
	}
	public void setOutInfo(String outInfo) {
		this.outInfo = outInfo;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	} 
	
}
