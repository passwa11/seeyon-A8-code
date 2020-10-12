package com.seeyon.apps.meetingroom.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class MeetingRoomOccupancyVO implements Serializable{

	private static final long serialVersionUID = 7908037045540048378L;

    /** 开始使用时间 */
    private Date startDatetime;
    /** 结束使用时间 */
    private Date endDatetime;
    /** 申请说明，用途 */
    private String description;
    /** 申请人姓名 */
    private String appPerName;
    /** 申请时间  某天 */
    private Set<String> appDate;
    /** 申请状态 */
    private Integer status;
    /** 申请ID*/
    private Long appId;
    /** 是否结束使用 */
    private Boolean finish;
    
	public Date getStartDatetime() {
		return startDatetime;
	}
	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}

	public Date getEndDatetime() {
		return endDatetime;
	}
	public void setEndDatetime(Date endDatetime) {
		this.endDatetime = endDatetime;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAppPerName() {
		return appPerName;
	}
	public void setAppPerName(String appPerName) {
		this.appPerName = appPerName;
	}
	
	public Set<String> getAppDate() {
		return appDate;
	}
	public void setAppDate(Set<String> appDate) {
		this.appDate = appDate;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public Boolean isFinish() {
		return this.finish;
	}

	public void setFinish(Boolean finish) {
		this.finish = finish;
	}
}
