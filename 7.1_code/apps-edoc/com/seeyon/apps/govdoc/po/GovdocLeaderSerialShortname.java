package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;


/**
 *
 *  领导批示编号简称
 */
public class GovdocLeaderSerialShortname extends BasePO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 978568287284108471L;
	
	//领导id
	private Long leaderId;
	//领导名
	private String leaderName;
	//编号简称
	private String shortName;
	//是否可用
	private Boolean isUsable;
	//单位id
	private Long orgAccountId;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(Long leaderId) {
		this.leaderId = leaderId;
	}
	public String getLeaderName() {
		return leaderName;
	}
	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public Boolean getIsUsable() {
		return isUsable;
	}
	public void setIsUsable(Boolean isUsable) {
		this.isUsable = isUsable;
	}
	public Long getOrgAccountId() {
		return orgAccountId;
	}
	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}
	
	
	
}