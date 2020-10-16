package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocElementFlowPermAcl extends BaseModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3571683190414114335L;
	private Long flowPermId;
//	private Long elementId;
	private Integer access;	
	
	private EdocElement edocElement;
	
	public static enum ACCESS_STATE{read,edit}; 
	
	public EdocElement getEdocElement() {
		return edocElement;
	}
	public void setEdocElement(EdocElement edocElement) {
		this.edocElement = edocElement;
	}
	public Integer getAccess() {
		return access;
	}
	public void setAccess(Integer access) {
		this.access = access;
	}

//	public Long getElementId() {
//		return elementId;
//	}
//	public void setElementId(Long elementId) {
//		this.elementId = elementId;
//	}
	public Long getFlowPermId() {
		return flowPermId;
	}
	public void setFlowPermId(Long flowPermId) {
		this.flowPermId = flowPermId;
	}
	public EdocElementFlowPermAcl(Long flowPermId, Long access, EdocElement edocElement) {
		super();
		this.flowPermId = flowPermId;
		this.access = access.intValue();
		this.edocElement = edocElement;		
	}
	public EdocElementFlowPermAcl(Long flowPermId, Integer access, EdocElement edocElement) {
		super();
		this.flowPermId = flowPermId;
		this.access = access;
		this.edocElement = edocElement;		
	}
	public EdocElementFlowPermAcl() {
		super();		
	}
	
}
