package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;

public class QwqpEdocSendFormRelation extends BasePO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5314061292330690951L;
	
	private Long id;
	private Long fileId;
	private Long edocId;
	private String fileType;
	private String signString;
	private String opinions;
	
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
	public Long getEdocId() {
		return edocId;
	}
	public void setEdocId(Long edocId) {
		this.edocId = edocId;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getSignString() {
		return signString;
	}
	public void setSignString(String signString) {
		this.signString = signString;
	}
	public String getOpinions() {
		return opinions;
	}
	public void setOpinions(String opinions) {
		this.opinions = opinions;
	}
}
