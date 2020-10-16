package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSupervisor extends BaseModel implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7063241492222307831L;
	private Long superviseId;
	private Integer permission;
	private Long supervisorId;
	private Boolean isDelete;
	public Boolean getIsDelete() {
		return isDelete;
	}
	public void setIsDelete(Boolean isDelete) {
		this.isDelete = isDelete;
	}
	public Integer getPermission() {
		return permission;
	}
	public void setPermission(Integer permission) {
		this.permission = permission;
	}
	public Long getSuperviseId() {
		return superviseId;
	}
	public void setSuperviseId(Long superviseId) {
		this.superviseId = superviseId;
	}
	public Long getSupervisorId() {
		return supervisorId;
	}
	public void setSupervisorId(Long supervisorId) {
		this.supervisorId = supervisorId;
	}

}
