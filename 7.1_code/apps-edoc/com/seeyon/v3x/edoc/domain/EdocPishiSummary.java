package com.seeyon.v3x.edoc.domain;

import java.util.Date;

/**
 * 代领导批示
 * @author 53454
 *
 */
public class EdocPishiSummary extends EdocSummary {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 应用
	 */
	private java.lang.Integer affairApp;
	/**
	 * 子应用ID
	 */
	private java.lang.Integer subApp;
	/**
	 * 当前affairId
	 */
	private java.lang.Long affairId;
	/**
	 * 当前affairId
	 */
	private java.lang.Long summaryId;
	/**
	 * 对应workitem_id
	 */
	private java.lang.Long subObjectId;
	/**
	 * 是否有附件
	 */
    private Boolean hasAtt = false;
    /**
     * 流程超期
     */
	private Boolean affairIsCoverTime = false;
	/**
	 * 处理期限（节点期限）
	 */
	private String affairDeadLineName;
	
	public java.lang.Integer getAffairApp() {
		return affairApp;
	}
	public void setAffairApp(java.lang.Integer affairApp) {
		this.affairApp = affairApp;
	}
	public java.lang.Integer getSubApp() {
		return subApp;
	}
	public void setSubApp(java.lang.Integer subApp) {
		this.subApp = subApp;
	}
	public java.lang.Long getAffairId() {
		return affairId;
	}
	public void setAffairId(java.lang.Long affairId) {
		this.affairId = affairId;
	}
	public java.lang.Long getSubObjectId() {
		return subObjectId;
	}
	public void setSubObjectId(java.lang.Long subObjectId) {
		this.subObjectId = subObjectId;
	}
	public java.lang.Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(java.lang.Long summaryId) {
		this.summaryId = summaryId;
	}
	public Boolean getHasAtt() {
		return hasAtt;
	}
	public void setHasAtt(Boolean hasAtt) {
		this.hasAtt = hasAtt;
	}
	public Boolean getAffairIsCoverTime() {
		return affairIsCoverTime;
	}
	public void setAffairIsCoverTime(Boolean affairIsCoverTime) {
		this.affairIsCoverTime = affairIsCoverTime;
	}
	public String getAffairDeadLineName() {
		return affairDeadLineName;
	}
	public void setAffairDeadLineName(String affairDeadLineName) {
		this.affairDeadLineName = affairDeadLineName;
	}
	
}
