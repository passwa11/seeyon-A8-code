package com.seeyon.v3x.edoc.manager.statistics;

public class StatContent {

	private String statContentId;
	private String statContentName;
	public StatContent(String statContentId, String statContentName) {
		this.statContentId = statContentId;
		this.statContentName = statContentName;
	}
	public String getStatContentId() {
		return statContentId;
	}
	public void setStatContentId(String statContentId) {
		this.statContentId = statContentId;
	}
	public String getStatContentName() {
		return statContentName;
	}
	public void setStatContentName(String statContentName) {
		this.statContentName = statContentName;
	}

	
}
