package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSuperviseDetail extends BaseModel implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6595077051776831872L;
	private Long edocId;
	private Date startDate;
	private Date endDate;
	private Integer status;
	private Long senderId;
	private String supervisors;
	private String description;
	private Integer count;
	private Integer remindMode;	
	private String scheduleProp;
	private Set<EdocSupervisor> edocSupervisors;
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getSupervisors() {
		return supervisors;
	}
	public void setSupervisors(String supervisors) {
		this.supervisors = supervisors;
	}
	public Long getEdocId() {
		return edocId;
	}
	public void setEdocId(Long edocId) {
		this.edocId = edocId;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Integer getRemindMode() {
		return remindMode;
	}
	public void setRemindMode(Integer remindMode) {
		this.remindMode = remindMode;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Set<EdocSupervisor> getEdocSupervisors() {
		return edocSupervisors;
	}
	public void setEdocSupervisors(Set<EdocSupervisor> edocSupervisors) {
		this.edocSupervisors = edocSupervisors;
	}
	public Long getSenderId() {
		return senderId;
	}
	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}
	public String getScheduleProp() {
		return scheduleProp;
	}
	public void setScheduleProp(String scheduleProp) {
		this.scheduleProp = scheduleProp;
	}
	
	
}
