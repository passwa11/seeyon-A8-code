package com.seeyon.ctp.common.affair.bo;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PendingRow implements Serializable {
	//zhou
	private String sendUserUnit;

	public String getSendUserUnit() {
		return sendUserUnit;
	}

	public void setSendUserUnit(String sendUserUnit) {
		this.sendUserUnit = sendUserUnit;
	}

	private Long id;//affairId

	private String subject  = "";//标题

	private String subjectHTML= "";//标题的HTML格式显示

	private String createMemberName= "";//发起人

	private Long createMemberId;//发起人

	private String createMemberAlt= "";//

	private Integer importantLevel;//重要程度

	private Boolean hasAttachments;//是否包含附件

	private String bodyType = "";//正文类别

	private List<String> extIcons;//扩展图标

	private String receiveTime = "";//接收时间/召开时间段

	private String completeTime= "";//会议结束时间

	private String edocMark = "";//公文文号(公文字段)

	private String sendUnit = "";//发文单位 (公文字段)

	private String placeOfMeeting = "";//会议地点(会议字段)

	private String theConferenceHost = "";//主持人(会议字段)

	private Long theConferenceHostId;//主持人ID

	private String processingProgress = "";//已处理人数/总人数(会议、调查字段)

	private Integer processedNumber = 0;//已处理人数(会议、调查字段)

	private Integer pendingNumber = 0;//待定人数(会议字段)

	private Integer unJoinNumber = 0;//不参加人数(会议、调查字段)

	private Integer totalNumber = 0;//总人数(会议、调查字段)

	private Integer replyCounts = 0;//回复条数(目前协同)

	private String meetingNature = "1";//会议方式 1普通会议 2视频会议

	private String categoryLabel = "";//分类标题

	private Integer openType;

	private String deadLine = "";//节点期限到期时间

	private Long deadLineDate;//节点期限  （未转化之前）

	private String link = "";//链接

	private Integer categoryOpenType;

	private String categoryLink = "";//分类链接

	private Integer applicationCategoryKey;

	private Integer applicationSubCategoryKey;

    private String policyName = "";//节点权限名称

    private Long objectId;//summaryId
    private Long subObjectId;

    private String fromName;//加签/会签人的 id

    private String backFromName;//回退、指定回退人的 id

    private Boolean agent;//代理

    private Boolean distinct; //是否将该行突出显示

    private Date createDate;

    private String currentNodesInfo;	//当前待办人

    private String meetingImpart; //是否是会议告知

    private String preApproverName;

    private Map<String, String> extParam;//待办栏目中：回复数、催办数、超期情况等信息


	public Map<String, String> getExtParam() {
		return extParam;
	}

	public void setExtParam(Map<String, String> extParam) {
		this.extParam = extParam;
	}

	public String getPreApproverName() {
		return preApproverName;
	}

	public void setPreApproverName(String preApproverName) {
		this.preApproverName = preApproverName;
	}

	/**
     *  状态
     */
    private Integer state;
    /**
     * 流程状态
     */
    private Integer summaryState;

	/**
     *  子状态
     */
    private Integer subState;
    /**
     * 流程状态是否超期 false 未超期 、  true 已超期
     */
    private boolean isOverTime;

    private boolean isShowClockIcon;
    // 处理期限
	private Boolean dealTimeout = false;

	// 首页处理时间显示       今日、明日、 yyyy-MM-dd
	private String dealLineName;
	// 更多页面接收时间显示     yyyy-MM-dd HH:ss
	private String receiveTimeAll;


	/**
     * 是否有资源权限
     * @return
     */
    private boolean hasResPerm;

    private String templateId;

    private Long activityId;
    private Long caseId;
    private String processId;
    private boolean isSpervisor;//当前登录人是否是督办人

    private String processDatetime;//流程期限时间点

    private Long memberId;   //当前处理人id
    //节点权限，不同意时，意见必填
    private Integer disAgreeOpinionPolicy;

    private Integer hastenTimes=0;

    //具体超期时间
    private String deadlineTime="";
    //具体距离超期
    private String nodeadlineTime="";

    //是否显示距离超期时间
    private Boolean showDealLineTime = false;

	public Boolean getShowDealLineTime() {
		return showDealLineTime;
	}

	public void setShowDealLineTime(Boolean showDealLineTime) {
		this.showDealLineTime = showDealLineTime;
	}

    /**
	 * 置顶时间
	 */
	private java.util.Date topTime;

    public java.util.Date getTopTime() {
    	//这里冗余两天的时间,避免时区的影响,入库是1970-01-01 00:00:00
    	Date date = new java.sql.Timestamp(144000000l); //"1970-01-03 00:00:00"
    	if (topTime!=null && topTime.after(date)) {//查询时判断是否为
			return topTime;
		}
    	return null;
	}

	public void setTopTime(java.util.Date topTime) {
		this.topTime = topTime;
	}

	public String getNodeadlineTime() {
		return nodeadlineTime;
	}

	public void setNodeadlineTime(String nodeadlineTime) {
		this.nodeadlineTime = nodeadlineTime;
	}

	public String getDeadlineTime() {
		return deadlineTime;
	}

	public void setDeadlineTime(String deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	public Integer getHastenTimes() {
		return hastenTimes;
	}

	public void setHastenTimes(Integer hastenTimes) {
		this.hastenTimes = hastenTimes;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public Long getCaseId() {
		return caseId;
	}

	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}


    public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getCurrentNodesInfo() {
		return currentNodesInfo;
	}

	public void setCurrentNodesInfo(String currentNodesInfo) {
		this.currentNodesInfo = currentNodesInfo;
	}

    public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getMeetingNature() {
		return meetingNature;
	}

	public void setMeetingNature(String meetingNature) {
		this.meetingNature = meetingNature;
	}

	public String getCategoryLabel() {
		return categoryLabel;
	}

	public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public Long getTheConferenceHostId() {
        return theConferenceHostId;
    }

    public void setTheConferenceHostId(Long theConferenceHostId) {
        this.theConferenceHostId = theConferenceHostId;
    }

    public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * 标题链接，直接用/*.do?method=**&...
	 *
	 * @param link
	 * @param openType 打开方式
	 */
	public void setLink(String link, Integer openType) {
		this.link = link;
		if(openType != 0){
            this.openType = openType;
        }
	}

	public Integer getOpenType() {
		return openType;
	}

	public String getCategoryLink() {
		return categoryLink;
	}

    public Integer getApplicationCategoryKey() {
		return applicationCategoryKey;
	}

	public void setOpenType(Integer openType) {
		this.openType = openType;
	}

	public void setCategoryLink(String categoryLink) {
		this.categoryLink = categoryLink;
	}

	public void setApplicationCategoryKey(Integer applicationCategoryKey) {
		this.applicationCategoryKey = applicationCategoryKey;
	}

	public void setApplicationSubCategoryKey(Integer applicationSubCategoryKey) {
		this.applicationSubCategoryKey = applicationSubCategoryKey;
	}

	public Integer getApplicationSubCategoryKey() {
		return applicationSubCategoryKey;
	}

	public String getSubject() {
    	return subject;
    }

    public void setSubject(String subject) {
    	this.subject = subject;
    }

    public String getSubjectHTML() {
    	return subjectHTML;
    }

    public void setSubjectHTML(String subjectHTML) {
    	this.subjectHTML = subjectHTML;
    }

    public String getCreateMemberName() {
    	return createMemberName;
    }

    public void setCreateMemberName(String createMemberName) {
    	this.createMemberName = createMemberName;
    }

    public Integer getImportantLevel() {
    	return importantLevel;
    }

    public void setImportantLevel(Integer importantLevel) {
    	this.importantLevel = importantLevel;
    }

    public Boolean getHasAttachments() {
    	return hasAttachments;
    }

    public void setHasAttachments(Boolean hasAttachments) {
    	this.hasAttachments = hasAttachments;
    }

    public String getBodyType() {
    	return bodyType;
    }

    public void setBodyType(String bodyType) {
    	this.bodyType = bodyType;
    }

    public void setExtIcons(List<String> extIcons) {
    	this.extIcons = extIcons;
    }
    public List<String> getExtIcons() {
		return extIcons;
	}

	public void addExtIcons(String extIcon) {
		if (this.extIcons == null) {
			this.extIcons = new ArrayList<String>();
		}

		this.extIcons.add(extIcon);
	}

    public String getReceiveTime() {
    	return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
    	this.receiveTime = receiveTime;
    }

    public String getEdocMark() {
    	return edocMark;
    }

    public void setEdocMark(String edocMark) {
    	this.edocMark = edocMark;
    }

    public String getSendUnit() {
    	return sendUnit;
    }

    public void setSendUnit(String sendUnit) {
    	this.sendUnit = sendUnit;
    }

    public String getPlaceOfMeeting() {
    	return placeOfMeeting;
    }

    public void setPlaceOfMeeting(String placeOfMeeting) {
    	this.placeOfMeeting = placeOfMeeting;
    }

    public String getTheConferenceHost() {
    	return theConferenceHost;
    }

    public void setTheConferenceHost(String theConferenceHost) {
    	this.theConferenceHost = theConferenceHost;
    }

    public String getProcessingProgress() {
    	return processingProgress;
    }

    public void setProcessingProgress(String processingProgress) {
    	this.processingProgress = processingProgress;
    }


    /**
	 * 类别链接，直接用/*.do?method=**&...
	 *
	 * @param label
	 *            直接输出的文本，做国际化
	 * @param link
	 *            如 "/collaboration.do?method=detail&from=Done&affairId=" +
	 *            affair.getId())
	 */
	public void setCategory(String label, String link) {
		this.categoryLabel = label;
		this.categoryLink = link;
	}

    public void setCategory(String label, String link, Integer openType) {
        this.categoryLabel = label;
        this.categoryLink = link;
        this.categoryOpenType = openType;
    }

    public Integer getCategoryOpenType() {
        return categoryOpenType;
    }

    public void setCategoryOpenType(Integer categoryOpenType) {
        this.categoryOpenType = categoryOpenType;
    }

    /**
	 *
	 * @param applicationCategoryKey
	 *            应用分类，参见{@link ApplicationCategoryEnum}
	 * @param link
	 */
	public void setCategory(Integer applicationCategoryKey, Integer applicationSubCategoryKey, String link) {
		this.applicationCategoryKey = applicationCategoryKey;
		this.applicationSubCategoryKey = applicationSubCategoryKey;
		this.categoryLink = link;
	}

	public void setCategory(int applicationCategoryKey, String link) {
		this.applicationCategoryKey = applicationCategoryKey;
		this.categoryLink = link;
	}

    public String getPolicyName() {
    	return policyName;
    }

    public void setPolicyName(String policyName) {
    	this.policyName = policyName;
    }

	public String getCreateMemberAlt() {
		return createMemberAlt;
	}

	public void setCreateMemberAlt(String createMemberAlt) {
		this.createMemberAlt = createMemberAlt;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public Long getSubObjectId() {
		return subObjectId;
	}

	public void setSubObjectId(Long subObjectId) {
		this.subObjectId = subObjectId;
	}

	public Boolean getAgent() {
		return agent;
	}

	public void setAgent(Boolean agent) {
		this.agent = agent;
	}

	public Boolean getDistinct() {
		return distinct;
	}

	public void setDistinct(Boolean distinct) {
		this.distinct = distinct;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getProcessedNumber() {
		return processedNumber;
	}

	public void setProcessedNumber(Integer processedNumber) {
		this.processedNumber = processedNumber;
	}

	public Integer getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(Integer totalNumber) {
		this.totalNumber = totalNumber;
	}

	public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public void setDeadLine(String deadLine) {
		this.deadLine = deadLine;
	}

	public String getDeadLine() {
		return deadLine;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromName() {
		return fromName;
	}

	public Long getCreateMemberId() {
		return createMemberId;
	}

	public void setCreateMemberId(Long createMemberId) {
		this.createMemberId = createMemberId;
	}

	public void setSubState(Integer subState) {
		this.subState = subState;
	}

	public Integer getSubState() {
		return subState;
	}

	public boolean isOverTime() {
		return isOverTime;
	}

	public void setOverTime(boolean isOverTime) {
		this.isOverTime = isOverTime;
	}

	public boolean isShowClockIcon() {
		return isShowClockIcon;
	}

	public void setShowClockIcon(boolean isShowClockIcon) {
		this.isShowClockIcon = isShowClockIcon;
	}

	public boolean isHasResPerm() {
		return hasResPerm;
	}

	public void setHasResPerm(boolean hasResPerm) {
		this.hasResPerm = hasResPerm;
	}

	public Long getDeadLineDate() {
		return deadLineDate;
	}

	public void setDeadLineDate(Long deadLineDate) {
		this.deadLineDate = deadLineDate;
	}

	public boolean isSpervisor() {
		return isSpervisor;
	}

	public void setSpervisor(boolean isSpervisor) {
		this.isSpervisor = isSpervisor;
	}

	public void setProcessDatetime(String processDatetime) {
		this.processDatetime = processDatetime;
	}

	public String getProcessDatetime() {
		return processDatetime;
	}

	public String getBackFromName() {
		return backFromName;
	}

	public void setBackFromName(String backFromName) {
		this.backFromName = backFromName;
	}

	public String getMeetingImpart() {
        return meetingImpart;
    }

	public void setMeetingImpart(String meetingImpart) {
        this.meetingImpart = meetingImpart;
    }

    public Boolean getDealTimeout() {
			return dealTimeout;
	}

	public void setDealTimeout(Boolean dealTimeout) {
			this.dealTimeout = dealTimeout;
	}
	public String getDealLineName() {
		return dealLineName;
	}

	public void setDealLineName(String dealLineName) {
		this.dealLineName = dealLineName;
	}

	public String getReceiveTimeAll() {
		return receiveTimeAll;
	}

	public void setReceiveTimeAll(String receiveTimeAll) {
		this.receiveTimeAll = receiveTimeAll;
	}

    public Integer getDisAgreeOpinionPolicy() {
        return disAgreeOpinionPolicy;
    }

    public void setDisAgreeOpinionPolicy(Integer disAgreeOpinionPolicy) {
        this.disAgreeOpinionPolicy = disAgreeOpinionPolicy;
    }

	public Integer getPendingNumber() {
		return pendingNumber;
	}

	public void setPendingNumber(Integer pendingNumber) {
		this.pendingNumber = pendingNumber;
	}

	public Integer getUnJoinNumber() {
		return unJoinNumber;
	}

	public void setUnJoinNumber(Integer unJoinNumber) {
		this.unJoinNumber = unJoinNumber;
	}

	public Integer getReplyCounts() {
		return replyCounts;
	}

	public void setReplyCounts(Integer replyCounts) {
		this.replyCounts = replyCounts;
	}

	public Integer getSummaryState() {
		return summaryState;
	}

	public void setSummaryState(Integer summaryState) {
		this.summaryState = summaryState;
	}
}
