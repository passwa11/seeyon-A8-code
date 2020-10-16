package com.seeyon.apps.govdoc.po;

public class JointlyIssyedVO {
	private String orgType;
	private String orgId;
	private String orgName;
	private boolean isSendFlow;//是否是被触发的协办单位
	private long summaryId;
	private int summaryState;
	private long exchangeDetailId;
	private long affairId=-1;
	private String lDate = "";
	private int affairApp;
	private String processId;
	public long getAffairId() {
		return affairId;
	}

	public void setAffairId(long affairId) {
		this.affairId = affairId;
	}
	
	/**
	 * 流程所属单位
	 */
	private String summary_unit;

	private String subject;
	private String type_str;//流程类型，协办，主办
	
	private int state;//状态，可撤销，可重发；
	private String oper_str = "";//可操作的str
	
	private boolean sendUserFlag;//本人是否联合发文人
	
	public boolean isSendUserFlag() {
		return sendUserFlag;
	}

	public void setSendUserFlag(boolean sendUserFlag) {
		this.sendUserFlag = sendUserFlag;
	}

	public long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(long summaryId) {
		this.summaryId = summaryId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getType_str() {
		return type_str;
	}

	public void setType_str(String type_str) {
		this.type_str = type_str;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getExchangeDetailId() {
		return exchangeDetailId;
	}

	public void setExchangeDetailId(long exchangeDetailId) {
		this.exchangeDetailId = exchangeDetailId;
	}

	public String getOper_str() {
		return oper_str;
	}

	public void setOper_str(String oper_str) {
		this.oper_str = oper_str;
	}

	public int getSummaryState() {
		return summaryState;
	}

	public void setSummaryState(int summaryState) {
		this.summaryState = summaryState;
	}

	public String getSummary_unit() {
		return summary_unit;
	}

	public void setSummary_unit(String summary_unit) {
		this.summary_unit = summary_unit;
	}

	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public boolean isSendFlow() {
		return isSendFlow;
	}

	public void setSendFlow(boolean isSendFlow) {
		this.isSendFlow = isSendFlow;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getlDate() {
		return lDate;
	}

	public void setlDate(String lDate) {
		this.lDate = lDate;
	}

	public int getAffairApp() {
		return affairApp;
	}

	public void setAffairApp(int affairApp) {
		this.affairApp = affairApp;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

}
