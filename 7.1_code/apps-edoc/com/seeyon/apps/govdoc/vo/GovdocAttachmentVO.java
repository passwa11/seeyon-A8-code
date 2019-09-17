package com.seeyon.apps.govdoc.vo;

import java.io.Serializable;

public class GovdocAttachmentVO implements Serializable {
	private static final long serialVersionUID = -4892185379334235646L;

	private Integer category;
	private Integer type;
	private String filename;
	private String mimeType;
	private java.util.Date createdate;
	private Long size;
	private String description;
	private Long fileUrl;
	private String extension = null;
	private String icon = null;
	private Long genesisId;
	private int sort;
	private int from = 0;
	private Long reference = Long.valueOf(1L);
	private String officeTransformEnable = "disable";
	
	public static enum FromType {
		title, // 标题区
		form, // 正文区
		sender, // 附言区
		reply, // 处理区
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public java.util.Date getCreatedate() {
		return createdate;
	}

	public void setCreatedate(java.util.Date createdate) {
		this.createdate = createdate;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(Long fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Long getGenesisId() {
		return genesisId;
	}

	public void setGenesisId(Long genesisId) {
		this.genesisId = genesisId;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public Long getReference() {
		return reference;
	}

	public void setReference(Long reference) {
		this.reference = reference;
	}

	public String getOfficeTransformEnable() {
		return officeTransformEnable;
	}

	public void setOfficeTransformEnable(String officeTransformEnable) {
		this.officeTransformEnable = officeTransformEnable;
	}

}
