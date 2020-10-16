package com.seeyon.apps.govdoc.vo;

public class GovdocRepealVO extends GovdocBaseVO {
	private String summaryIdStr;
	private String affairIdStr;
	private String repealComment;
	private String trackWorkflowType;
	private String isWFTrace;
	private String extAtt1;

	public String getSummaryIdStr() {
		return summaryIdStr;
	}

	public void setSummaryIdStr(String summaryIdStr) {
		this.summaryIdStr = summaryIdStr;
	}

	public String getAffairIdStr() {
		return affairIdStr;
	}

	public void setAffairIdStr(String affairIdStr) {
		this.affairIdStr = affairIdStr;
	}

	public String getRepealComment() {
		return repealComment;
	}

	public void setRepealComment(String repealComment) {
		this.repealComment = repealComment;
	}

	public String getTrackWorkflowType() {
		return trackWorkflowType;
	}

	public void setTrackWorkflowType(String trackWorkflowType) {
		this.trackWorkflowType = trackWorkflowType;
	}

	public String getExtAtt1() {
		return extAtt1;
	}

	public void setExtAtt1(String extAtt1) {
		this.extAtt1 = extAtt1;
	}

	public String getIsWFTrace() {
		return isWFTrace;
	}

	public void setIsWFTrace(String isWFTrace) {
		this.isWFTrace = isWFTrace;
	}

}
