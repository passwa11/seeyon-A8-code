package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;


/**
 * 单位人员与上级领导对应关系
 */
public class EdocUserLeaderRelation extends BasePO {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5856168374335462085L;
	/**
	 *  人员id
	 */
	private java.lang.Long userId;
	/**
	 *  人员姓名
	 */
	private java.lang.String userName;
	/**
	 *  领导id
	 */
	private java.lang.String leaderId;
	/**
	 *  领导姓名
	 */
	private java.lang.String leaderName;
	public java.lang.Long getId() {
		return id;
	}
	public void setId(java.lang.Long id) {
		this.id = id;
	}
	public java.lang.Long getUserId() {
		return userId;
	}
	public void setUserId(java.lang.Long userId) {
		this.userId = userId;
	}
	public java.lang.String getUserName() {
		return userName;
	}
	public void setUserName(java.lang.String userName) {
		this.userName = userName;
	}
	public java.lang.String getLeaderId() {
		return leaderId;
	}
	public void setLeaderId(java.lang.String leaderId) {
		this.leaderId = leaderId;
	}
	public java.lang.String getLeaderName() {
		return leaderName;
	}
	public void setLeaderName(java.lang.String leaderName) {
		this.leaderName = leaderName;
	}







}