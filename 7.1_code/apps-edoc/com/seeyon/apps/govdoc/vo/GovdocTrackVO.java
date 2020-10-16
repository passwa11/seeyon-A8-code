package com.seeyon.apps.govdoc.vo;

public class GovdocTrackVO {

	private Long summaryId;
	private Long affairId;
	private Integer trackType;
	private Integer state;
	private Long startMemberId;
	private String zdgzrStr;
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public Integer getTrackType() {
		return trackType;
	}
	public void setTrackType(Integer trackType) {
		this.trackType = trackType;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Long getStartMemberId() {
		return startMemberId;
	}
	public void setStartMemberId(Long startMemberId) {
		this.startMemberId = startMemberId;
	}
	public String getZdgzrStr() {
		return zdgzrStr;
	}
	public void setZdgzrStr(String zdgzrStr) {
		this.zdgzrStr = zdgzrStr;
	}
	
}
