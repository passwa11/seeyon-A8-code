package com.seeyon.ctp.form.po;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.v3x.common.domain.BaseModel;

public class FromPrintBind extends BaseModel  implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long fileUrl;//打印单文件Id
	private String fileName;//打印单文件名
	private Timestamp fileCreateTime;//创建时间
	private Long unitId;//单位Id
	private Long edocXsnId;//公文单Id
	
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFileUrl() {
		return this.fileUrl;
	}
	public void setFileUrl(Long fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getFileName() {
		return this.fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Timestamp getFileCreateTime() {
		return this.fileCreateTime;
	}
	public void setFileCreateTime(Timestamp fileCreateTime) {
		this.fileCreateTime = fileCreateTime;
	}
	public Long getUnitId() {
		return this.unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}
	public Long getEdocXsnId() {
		return this.edocXsnId;
	}
	public void setEdocXsnId(Long edocXsnId) {
		this.edocXsnId = edocXsnId;
	}
	
}
