package com.seeyon.v3x.edoc.webmodel;

import java.util.Date;

public class EdocSuperviseDealModel {
	private long dealUser;
	private String dealLine;
	private Date reveiveDate;
	private Date dealDate;
	private String dealDays;
	private String efficiency;
	private int hastened;
	private String policyName;
	private boolean isOverTime;
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public Date getDealDate() {
		return dealDate;
	}
	public void setDealDate(Date dealDate) {
		this.dealDate = dealDate;
	}
	public String getDealDays() {
		return dealDays;
	}
	public void setDealDays(String dealDays) {
		this.dealDays = dealDays;
	}
	public String getDealLine() {
		return dealLine;
	}
	public void setDealLine(String dealLine) {
		this.dealLine = dealLine;
	}
	public long getDealUser() {
		return dealUser;
	}
	public void setDealUser(long dealUser) {
		this.dealUser = dealUser;
	}
	public String getEfficiency() {
		return efficiency;
	}
	public void setEfficiency(String efficiency) {
		this.efficiency = efficiency;
	}
	public int getHastened() {
		return hastened;
	}
	public void setHastened(int hastened) {
		this.hastened = hastened;
	}
	public Date getReveiveDate() {
		return reveiveDate;
	}
	public void setReveiveDate(Date reveiveDate) {
		this.reveiveDate = reveiveDate;
	}
	public boolean isOverTime() {
		return isOverTime;
	}
	public void setOverTime(boolean isOverTime) {
		this.isOverTime = isOverTime;
	}
}
