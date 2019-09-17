package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;


/**
 *
 *  批示编号表 
 */
public class EdocLeaderPishiNo extends BasePO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5754000821723625994L;
	/*[IDTCODE MARKER BEGIN]*/

	// fields
	/**
	 *  ID 
	 */
	private java.lang.Long id;
	/**
	 *  领导ID 
	 */
	private java.lang.Long leaderId;
	/**
	 *  领导的sortId 
	 */
	private java.lang.Long  leaderSortId;
	/**
	 *  公文id
	 */
	private java.lang.Long summaryId;
	/**
	 *   
	 */
	private java.lang.Long affairId;
	/**
	 * 创建时间
	 */
	private java.util.Date createTime;
	/**
	 * 批示编号的时间
	 */
	private java.util.Date proxyDate;

	/**
	 * 批示编号
	 */
	private java.lang.Integer pishiNo;

	/**
	 * 批示领导编号
	 */
	private java.lang.String pishiName;

	/**
	 * 批示年号
	 */
	private java.lang.String pishiYear;
	/**
	 * 编号是否释放
	 */
	private Boolean isRelease = false;

	public java.lang.Long getId() {
		return id;
	}

	public void setId(java.lang.Long id) {
		this.id = id;
	}

	public java.lang.Long getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(java.lang.Long leaderId) {
		this.leaderId = leaderId;
	}

	public java.lang.Long getLeaderSortId() {
		return leaderSortId;
	}

	public void setLeaderSortId(java.lang.Long leaderSortId) {
		this.leaderSortId = leaderSortId;
	}

	public java.lang.Long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(java.lang.Long summaryId) {
		this.summaryId = summaryId;
	}

	public java.lang.Long getAffairId() {
		return affairId;
	}

	public void setAffairId(java.lang.Long affairId) {
		this.affairId = affairId;
	}

	public java.util.Date getProxyDate() {
		return proxyDate;
	}

	public void setProxyDate(java.util.Date proxyDate) {
		this.proxyDate = proxyDate;
	}

	public java.lang.Integer getPishiNo() {
		return pishiNo;
	}

	public void setPishiNo(java.lang.Integer pishiNo) {
		this.pishiNo = pishiNo;
	}

	public java.lang.String getPishiName() {
		return pishiName;
	}

	public void setPishiName(java.lang.String pishiName) {
		this.pishiName = pishiName;
	}

	public java.lang.String getPishiYear() {
		return pishiYear;
	}

	public void setPishiYear(java.lang.String pishiYear) {
		this.pishiYear = pishiYear;
	}

	public java.util.Date getcreateTime() {
		return createTime;
	}

	public void setcreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}

	public Boolean getIsRelease() {
		return isRelease;
	}

	@SuppressWarnings("null")
	public void setIsRelease(Boolean isRelease) {
		if(isRelease == null){
			this.isRelease = false;
		}else{
			this.isRelease = isRelease;
		}
	}



	/*[IDTCODE MARKER END]*/

}