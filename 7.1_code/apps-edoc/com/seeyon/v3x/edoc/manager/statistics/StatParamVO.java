package com.seeyon.v3x.edoc.manager.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StatParamVO {

	private int dimensionType;
	private List<String> times;
	private Map<Integer,List<Long>> orgs;
	private Map hasChirdOrgs;	//包含子部门的
	
	private int timeType;
	private Date starttime;
	private Date endtime;
	private long accountId;	//为了解决统计用外单位授权的文单所发的公文
	private long userId;
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public Map getHasChirdOrgs() {
		return hasChirdOrgs;
	}
	public long getAccountId() {
		return accountId;
	}
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	public void setHasChirdOrgs(Map hasChirdOrgs) {
		this.hasChirdOrgs = hasChirdOrgs;
	}
	
	public Map<Integer, List<Long>> getOrgs() {
		return orgs;
	}
	public void setOrgs(Map<Integer, List<Long>> orgs) {
		this.orgs = orgs;
	}
		public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public int getDimensionType() {
		return dimensionType;
	}
	public void setDimensionType(int dimensionType) {
		this.dimensionType = dimensionType;
	}
	public int getTimeType() {
		return timeType;
	}
	public void setTimeType(int timeType) {
		this.timeType = timeType;
	}
	public List<String> getTimes() {
		return times;
	}
	public void setTimes(List<String> times) {
		this.times = times;
	}
}








