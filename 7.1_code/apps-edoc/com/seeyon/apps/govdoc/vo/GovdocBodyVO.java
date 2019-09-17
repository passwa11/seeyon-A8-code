package com.seeyon.apps.govdoc.vo;

import java.util.Date;
import java.util.List;

import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;

public class GovdocBodyVO {
	
	private Long moduleId = -1L;
	private String mainbodyTypeListJSONStr;
	
	//套红签收日期格式
	private boolean taohongriqiSwitch;
	private String officeOcxUploadMaxSize;
	
	private Integer templateContentType;
	private String defaultBodyType;
	private String bodyTypeText;
	private Boolean isHideContentType;
	private Integer contentType;
	private String bodyType;
	private String content;
	//公文模板是否有正文类型，-1表示没有，0表示有设置类型
	private String contentT;
	private Long fileId;
	private String fileName;
	private Date createDate;
	private Date modifyDate;
	
	private String pdfFileId;
	private String ofdFileId;
	private Long bindTHTemplateId;
	private boolean isBindTHTemplete;

	private CtpContentAll formContent;
	private CtpContentAll bodyContent;
	private CtpContentAll pdfContent;
	private CtpContentAll ofdContent;
	
	//快速发文时需要查询可使用的模板
	private List<EdocDocTemplate> taohongList;
	
	/**
	 * 正文
	 * 
	 * @param newBodyType
	 * @param content
	 * @param contentDataId
	 * @param createDate
	 */
	public void setFormContent(MainbodyType newBodyType, String content, Long contentDataId, Date createDate) {
		this.formContent = new CtpContentAll();
		this.formContent.setNewId();
		this.formContent.setContent(content);
		this.formContent.setContentDataId(contentDataId);
		this.formContent.setContentType(newBodyType.getKey());
		this.formContent.setModuleType(ModuleType.edoc.getKey());
		this.formContent.setContentTemplateId(0L);
		this.formContent.setModuleTemplateId(-1L);
		this.formContent.setCreateDate(createDate);
		this.formContent.setModifyDate(null);
		this.formContent.setModifyId(null);
		this.formContent.setSort(0);
	}
	
	/**
	 * 正文
	 * 
	 * @param newBodyType
	 * @param content
	 * @param contentDataId
	 * @param createDate
	 */
	public void setBodyContent(Integer contentType, String content, Date createDate) {
		this.bodyContent = new CtpContentAll();
		this.bodyContent.setNewId();
		this.bodyContent.setContent(content);
		this.bodyContent.setContentType(contentType);
		this.bodyContent.setModuleType(ModuleType.edoc.getKey());
		this.bodyContent.setContentTemplateId(0L);
		this.bodyContent.setModuleTemplateId(-1L);
		this.bodyContent.setCreateDate(createDate);
		this.bodyContent.setModifyDate(null);
		this.bodyContent.setModifyId(null);
		this.bodyContent.setSort(1);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Boolean getIsHideContentType() {
		return isHideContentType;
	}

	public void setIsHideContentType(Boolean isHideContentType) {
		this.isHideContentType = isHideContentType;
	}

	public CtpContentAll getBodyContent() {
		return bodyContent;
	}

	public void setBodyContent(CtpContentAll bodyContent) {
		this.bodyContent = bodyContent;
	}

	public CtpContentAll getFormContent() {
		return formContent;
	}

	public void setFormContent(CtpContentAll formContent) {
		this.formContent = formContent;
	}

	public CtpContentAll getPdfContent() {
		return pdfContent;
	}

	public void setPdfContent(CtpContentAll pdfContent) {
		this.pdfContent = pdfContent;
	}

	public CtpContentAll getOfdContent() {
		return ofdContent;
	}

	public void setOfdContent(CtpContentAll ofdContent) {
		this.ofdContent = ofdContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPdfFileId() {
		return pdfFileId;
	}

	public void setPdfFileId(String pdfFileId) {
		this.pdfFileId = pdfFileId;
	}

	public String getOfdFileId() {
		return ofdFileId;
	}

	public void setOfdFileId(String ofdFileId) {
		this.ofdFileId = ofdFileId;
	}

	public String getBodyTypeText() {
		return bodyTypeText;
	}

	public void setBodyTypeText(String bodyTypeText) {
		this.bodyTypeText = bodyTypeText;
	}

	public Integer getContentType() {
		return contentType;
	}

	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	public String getContentT() {
		return contentT;
	}

	public void setContentT(String contentT) {
		this.contentT = contentT;
	}

	public Long getBindTHTemplateId() {
		return bindTHTemplateId;
	}

	public void setBindTHTemplateId(Long bindTHTemplateId) {
		this.bindTHTemplateId = bindTHTemplateId;
	}

	public boolean isBindTHTemplete() {
		return isBindTHTemplete;
	}

	public void setBindTHTemplete(boolean isBindTHTemplete) {
		this.isBindTHTemplete = isBindTHTemplete;
	}

	public String getDefaultBodyType() {
		return defaultBodyType;
	}

	public void setDefaultBodyType(String defaultBodyType) {
		this.defaultBodyType = defaultBodyType;
	}

	public boolean getTaohongriqiSwitch() {
		return taohongriqiSwitch;
	}

	public void setTaohongriqiSwitch(boolean taohongriqiSwitch) {
		this.taohongriqiSwitch = taohongriqiSwitch;
	}

	public List<EdocDocTemplate> getTaohongList() {
		return taohongList;
	}

	public void setTaohongList(List<EdocDocTemplate> taohongList) {
		this.taohongList = taohongList;
	}

	public String getMainbodyTypeListJSONStr() {
		return mainbodyTypeListJSONStr;
	}

	public void setMainbodyTypeListJSONStr(String mainbodyTypeListJSONStr) {
		this.mainbodyTypeListJSONStr = mainbodyTypeListJSONStr;
	}

	public Integer getTemplateContentType() {
		return templateContentType;
	}

	public void setTemplateContentType(Integer templateContentType) {
		this.templateContentType = templateContentType;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public String getOfficeOcxUploadMaxSize() {
		return officeOcxUploadMaxSize;
	}

	public void setOfficeOcxUploadMaxSize(String officeOcxUploadMaxSize) {
		this.officeOcxUploadMaxSize = officeOcxUploadMaxSize;
	}

}
