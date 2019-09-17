package com.seeyon.v3x.edoc.domain;

import java.util.List;

public class EdocStatCondObj {
		
	private int year;
	private int season;
	private int month;
	private int periodType;
	private int groupType; //分组条件
	private List<Long> deptIds;// 部门id
	private long domainId;// 单位id	
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getSeason() {
		return season;
	}
	
	public void setSeason(int season) {
		this.season = season;
	}
	
	public int getMonth() {
		return month;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}
	
	public int getPeriodType() {
		return periodType;
	}
	
	public void setPeriodType(int periodType) {
		this.periodType = periodType;
	}
	
	public int getGroupType() {
		return groupType;
	}
	
	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}
	
	public List<Long> getDeptIds() {
		return deptIds;
	}
	
	public void setDeptIds(List<Long> deptId) {
		this.deptIds = deptId;
	}
	
	public long getDomainId() {
		return domainId;
	}
	
	public void setDomainId(long domainId) {
		this.domainId = domainId;
	}

}
