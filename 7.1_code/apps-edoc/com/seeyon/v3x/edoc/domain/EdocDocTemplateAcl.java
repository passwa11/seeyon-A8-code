package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.v3x.common.domain.BaseModel;

public class EdocDocTemplateAcl extends BaseModel implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -2392527732213754939L;
	private Long templateId;
	private Long depId;
	private String depType;
	
	public Long getDepId() {
		return depId;
	}
	public void setDepId(Long depId) {
		this.depId = depId;
	}
	public String getDepType() {
		return depType;
	}
	public void setDepType(String depType) {
		this.depType = depType;
	}
	public Long getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}
	public String getType(){
		return V3xOrgEntity.ORGENT_TYPE_DEPARTMENT;
	}
	
	public EdocDocTemplateAcl(){
		
	}
	
	public EdocDocTemplateAcl(Long id,Long templateId,Long depId,String depType){
		this.id = id;
		this.templateId = templateId;
		this.depId = depId;
		this.depType = depType;
	}
}
