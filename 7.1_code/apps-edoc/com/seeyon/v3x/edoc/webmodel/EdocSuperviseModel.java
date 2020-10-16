package com.seeyon.v3x.edoc.webmodel;

import java.util.Date;

public class EdocSuperviseModel {
	
	private Long id;
	private String secretLevel;
	private String title;
	private String edocType;
	private String appName;
	private String sender;
	private String caseLogXML;
	private String caseProcessXML;
	private String caseWorkItemLogXML;
	private String process_desc_by;
	private String supervisor;
	private Date startDate;
	private Date endDate;
	private String description;
	private Integer count;
	private Integer remindModel;
	private Boolean hasWorkflow;
	private Long caseId;
	private Integer actorId;
	private String content;
	private Long edocId;
	private Integer status;
	private Long deadline;
	private Date deadlineDatetime;
	private Date deadlineDisplay;
	private Boolean isRed = false;//督办日期是否超期
	private Boolean workflowTimeout = false;//流程超期
    private String bodyType;
    private Boolean hasAttachment;
    private String urgentLevel;      //重要程度
    //G6 V1.0 SP1后续功能_流程期限--start
    private Date startTime; //公文发起时间
    private Date createTime;//公文创建时间
    //G6 V1.0 SP1后续功能_流程期限--end
    
	/**
	 * @return the workflowTimeout
	 */
	public Boolean getWorkflowTimeout() {
		return workflowTimeout;
	}
	/**
	 * @param workflowTimeout the workflowTimeout to set
	 */
	public void setWorkflowTimeout(Boolean workflowTimeout) {
		this.workflowTimeout = workflowTimeout;
	}
	/**
	 * @return the deadline
	 */
	public Long getEdocId() {
		return edocId;
	}
	public void setEdocId(Long edocId) {
		this.edocId = edocId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEdocType() {
		return edocType;
	}
	public void setEdocType(String edocType) {
		this.edocType = edocType;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getSecretLevel() {
		return secretLevel;
	}
	public void setSecretLevel(String secretLevel) {
		this.secretLevel = secretLevel;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getRemindModel() {
		return remindModel;
	}
	public void setRemindModel(Integer remindModel) {
		this.remindModel = remindModel;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSupervisor() {
		return supervisor;
	}
	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}
	public String getCaseLogXML() {
		return caseLogXML;
	}
	public void setCaseLogXML(String caseLogXML) {
		this.caseLogXML = caseLogXML;
	}
	public String getCaseProcessXML() {
		return caseProcessXML;
	}
	public void setCaseProcessXML(String caseProcessXML) {
		this.caseProcessXML = caseProcessXML;
	}
	public String getCaseWorkItemLogXML() {
		return caseWorkItemLogXML;
	}
	public void setCaseWorkItemLogXML(String caseWorkItemLogXML) {
		this.caseWorkItemLogXML = caseWorkItemLogXML;
	}
	public String getProcess_desc_by() {
		return process_desc_by;
	}
	public void setProcess_desc_by(String process_desc_by) {
		this.process_desc_by = process_desc_by;
	}
	public Boolean getHasWorkflow() {
		return hasWorkflow;
	}
	public void setHasWorkflow(Boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Long getCaseId() {
		return caseId;
	}
	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}
	public Integer getActorId() {
		return actorId;
	}
	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getDeadline() {
		return deadline;
	}
	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}
	/**
	 * @return the isRed
	 */
	public Boolean getIsRed() {
		return isRed;
	}
	/**
	 * @param isRed the isRed to set
	 */
	public void setIsRed(Boolean isRed) {
		this.isRed = isRed;
	}
	public String getBodyType() {
		return bodyType;
	}
	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}
	public Boolean getHasAttachment() {
		return hasAttachment;
	}
	public void setHasAttachment(Boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}
	public String getUrgentLevel() {
		return urgentLevel;
	}
	public void setUrgentLevel(String urgentLevel) {
		this.urgentLevel = urgentLevel;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getDeadlineDatetime() {
		return deadlineDatetime;
	}
	public void setDeadlineDatetime(Date deadlineDatetime) {
		this.deadlineDatetime = deadlineDatetime;
	}
	public Date getDeadlineDisplay() {
		return deadlineDisplay;
	}
	public void setDeadlineDisplay(Date deadlineDisplay) {
		this.deadlineDisplay = deadlineDisplay;
	}


}
