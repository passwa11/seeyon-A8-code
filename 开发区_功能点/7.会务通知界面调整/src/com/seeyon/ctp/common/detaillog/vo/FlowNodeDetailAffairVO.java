/**
 * $Author 翟锋$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.common.detaillog.vo;

import java.util.Date;


/**
 * @author zhaifeng
 *
 */
public class FlowNodeDetailAffairVO {
	/**
	 * affairId
	 */
    private Long id;
    private Long processId;
    /**
     *  是否超期
     */
    private Boolean coverTime;
    /**
     *  人员ID
     */
    private Long memberId;
    /**
     *  发送人ID
     */
    private Long senderId;
    /**
     *  标题
     */
    private String subject;
    /**
     *  应用
     */
    private Integer app;
    /**
     *  主应用ID
     */
    private Long objectId;
    /**
     *  对应workitem_id
     */
    private Long subObjectId;
    /**
     *  状态
     */
    private Integer state;
    /**
     *  子状态
     */
    private Integer subState;
    /**
     *  催办次数
     */
    private Integer hastenTimes;
    /**
     *  提醒时间
     */
    private Long remindDate;
    /**
     *  节点期限
     */
    private Long deadlineDate;
    /**
     *  是否到期提醒
     */
    private Boolean dueRemind;
    /**
     *  创建时间
     */
    private Date createDate;
    /**
     *  接受时间
     */
    private Date receiveTime;
    /**
     *  完成时间
     */
    private Date completeTime;
    /**
     *  提醒周期
     */
    private Integer remindInterval;
    /**
     *  是否被删除
     */
    private Boolean delete;
    /**
     *  归档ID
     */
    private Long archiveId;
    /**
     *  是否跟踪
     */
    private Integer track;
    /**
     *  属性
     */
    private String addition;
    /**
     *  扩展属性
     */
    private String extProps;
    /**
     *  更新时间
     */
    private Date updateDate;
    /**
     *  是否结束
     */
    private Boolean finish;
    /**
     *  正文类型
     */
    private String bodyType;
    /**
     *  重要程度
     */
    private Integer importantLevel;
    /**
     *  重复次数
     */
    private Integer resentTime;
    /**
     *  转发人
     */
    private String forwardMember;
    /**
     *  标识符
     */
    private String identifier;
    /**
     *  代理人
     */
    private Long transactorId;
    /**
     *  节点权限
     */
    private String nodePolicy;
    /**
     *  工作流节点Id
     */
    private Long activityId;
    /**
     *  表单应用id
     */
    private Long formAppId;

    private String formViewOperation;

    /**
     *  模板ID
     */
    private Long templeteId;
    /**
     *  //加签、知会、会签等操作，来源人的Id
     */
    private Long fromId;
    /**
     *  按工作时间计算超期时间
     */
    private Long overWorktime;
    /**
     *  按工作时间计算运行时间
     */
    private Long runWorktime;
    /**
     *  按自然时间计算超期时间
     */
    private Long overTime;
    /**
     *  按自然时间计算运行时间
     */
    private Long runTime;
    /**
     *  处理期限到处理类型（0-仅消息提醒;1-转给指定人;2-自动跳过）
     */
    private Integer dealTermType;
    /**
     *  处理期限到处理类型1的辅助值1：userid
     */
    private Long dealTermUserid;
    /**
     *  子应用ID
     */
    private Integer subApp;
    /**
     *  首次查看时间
     */
    private Date firstViewDate;

    private Long entityId;
    //发起或处理者
    private String handler;
    //节点名称
    private String policyName;
    //处理时间
    private Date finishDate;
    //处理期限
    private String deadline;
    //处理时长
    private String dealTime;
    //超期时长
    private String deadlineTime;
    //处理状态
    private String stateLabel;
    //引用名称(类型)
    private String appName;
    //是否有附件
    private Boolean hasAttsFlag;
    //状态的名称
    private String subStateName;

    private String meetingNature;

    // 已处理人数/总人数(会议、调查字段)
    private String processingProgress;

    // 已处理人数(会议、调查字段)
    private Integer processedNumber;

    //总人数(会议、调查字段)
	private Integer totalNumber;

	//会议地点(会议字段)
	private String placeOfMeeting;

	//主持人(会议字段)
	private String theConferenceHost;
	//是否表单授权
	private boolean isAuthority;
	//流程期限（转换后的名称）
	private String processPeriodName;
	//流程期限
    private Long processPeriod;
	/*private String link;//链接URL
	private int openType;//链接方式
*/
	//公文-公文文号
	private String edocMark;
	//发文-发文单位
	private String sendUnit;
	/**
	 * 是否有资源权限
	 */
	private boolean hasResPerm;
	private boolean meetingVideoConf;

	//回退人的id
	private Long backFromId;

	/**
	 * 回退人的id
	 * @return
	 */
	public Long getBackFromId() {
        return backFromId;
    }
    public void setBackFromId(Long backFromId) {
        this.backFromId = backFromId;
    }
    /*public void setOpenType(int openType) {
        this.openType = openType;
    }*/
    public boolean isMeetingVideoConf() {
        return meetingVideoConf;
    }
    public void setMeetingVideoConf(boolean meetingVideoConf) {
        this.meetingVideoConf = meetingVideoConf;
    }
	/*public void setLink(String link) {
		this.link = link;
	}*/


	public String getMeetingNature() {
		return meetingNature;
	}
	public void setMeetingNature(String meetingNature) {
		this.meetingNature = meetingNature;
	}
	/*public Integer getOpenType() {
		return openType;
	}

	public String getLink() {
		return link;
	}*/
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    public Boolean getCoverTime() {
        return coverTime;
    }
    public void setCoverTime(Boolean coverTime) {
        this.coverTime = coverTime;
    }
    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public Integer getApp() {
        return app;
    }
    public void setApp(Integer app) {
        this.app = app;
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
    public Integer getState() {
        return state;
    }
    public void setState(Integer state) {
        this.state = state;
    }
    public Integer getSubState() {
        return subState;
    }
    public void setSubState(Integer subState) {
        this.subState = subState;
    }
    public Integer getHastenTimes() {
        return hastenTimes;
    }
    public void setHastenTimes(Integer hastenTimes) {
        this.hastenTimes = hastenTimes;
    }
    public Long getRemindDate() {
        return remindDate;
    }
    public void setRemindDate(Long remindDate) {
        this.remindDate = remindDate;
    }
    public Long getDeadlineDate() {
        return deadlineDate;
    }
    public void setDeadlineDate(Long deadlineDate) {
        this.deadlineDate = deadlineDate;
    }
    public Boolean getDueRemind() {
        return dueRemind;
    }
    public void setDueRemind(Boolean dueRemind) {
        this.dueRemind = dueRemind;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public Date getReceiveTime() {
        return receiveTime;
    }
    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
    public Date getCompleteTime() {
        return completeTime;
    }
    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
    public Integer getRemindInterval() {
        return remindInterval;
    }
    public void setRemindInterval(Integer remindInterval) {
        this.remindInterval = remindInterval;
    }
    public Boolean getDelete() {
        return delete;
    }
    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
    public Long getArchiveId() {
        return archiveId;
    }
    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }
    public Integer getTrack() {
        return track;
    }
    public void setTrack(Integer track) {
        this.track = track;
    }
    public String getAddition() {
        return addition;
    }
    public void setAddition(String addition) {
        this.addition = addition;
    }
    public String getExtProps() {
        return extProps;
    }
    public void setExtProps(String extProps) {
        this.extProps = extProps;
    }
    public Date getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    public Boolean getFinish() {
        return finish;
    }
    public void setFinish(Boolean finish) {
        this.finish = finish;
    }
    public String getBodyType() {
        return bodyType;
    }
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }
    public Integer getImportantLevel() {
        return importantLevel;
    }
    public void setImportantLevel(Integer importantLevel) {
        this.importantLevel = importantLevel;
    }
    public Integer getResentTime() {
        return resentTime;
    }
    public void setResentTime(Integer resentTime) {
        this.resentTime = resentTime;
    }
    public String getForwardMember() {
        return forwardMember;
    }
    public void setForwardMember(String forwardMember) {
        this.forwardMember = forwardMember;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Long getTransactorId() {
        return transactorId;
    }
    public void setTransactorId(Long transactorId) {
        this.transactorId = transactorId;
    }
    public String getNodePolicy() {
        return nodePolicy;
    }
    public void setNodePolicy(String nodePolicy) {
        this.nodePolicy = nodePolicy;
    }
    public Long getActivityId() {
        return activityId;
    }
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }
    public Long getFormAppId() {
        return formAppId;
    }
    public void setFormAppId(Long formAppId) {
        this.formAppId = formAppId;
    }
    public Long getTempleteId() {
        return templeteId;
    }
    public void setTempleteId(Long templeteId) {
        this.templeteId = templeteId;
    }
    public Long getFromId() {
        return fromId;
    }
    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }
    public Long getOverWorktime() {
        return overWorktime;
    }
    public void setOverWorktime(Long overWorktime) {
        this.overWorktime = overWorktime;
    }
    public Long getRunWorktime() {
        return runWorktime;
    }
    public void setRunWorktime(Long runWorktime) {
        this.runWorktime = runWorktime;
    }
    public Long getOverTime() {
        return overTime;
    }
    public void setOverTime(Long overTime) {
        this.overTime = overTime;
    }
    public Long getRunTime() {
        return runTime;
    }
    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }
    public Integer getDealTermType() {
        return dealTermType;
    }
    public void setDealTermType(Integer dealTermType) {
        this.dealTermType = dealTermType;
    }
    public Long getDealTermUserid() {
        return dealTermUserid;
    }
    public void setDealTermUserid(Long dealTermUserid) {
        this.dealTermUserid = dealTermUserid;
    }
    public Integer getSubApp() {
        return subApp;
    }
    public void setSubApp(Integer subApp) {
        this.subApp = subApp;
    }
    public Long getEntityId() {
        return entityId;
    }
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    public String getHandler() {
        return handler;
    }
    public void setHandler(String handler) {
        this.handler = handler;
    }
    public String getPolicyName() {
        return policyName;
    }
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Date getFinishDate() {
        return finishDate;
    }
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }
    public String getDeadline() {
        return deadline;
    }
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
    public String getDealTime() {
        return dealTime;
    }
    public void setDealTime(String dealTime) {
        this.dealTime = dealTime;
    }
    public String getDeadlineTime() {
        return deadlineTime;
    }
    public void setDeadlineTime(String deadlineTime) {
        this.deadlineTime = deadlineTime;
    }
    public String getStateLabel() {
        return stateLabel;
    }
    public void setStateLabel(String stateLabel) {
        this.stateLabel = stateLabel;
    }

    public Boolean getHasAttsFlag() {
        return hasAttsFlag;
    }

    public void setHasAttsFlag(Boolean hasAttsFlag) {
        this.hasAttsFlag = hasAttsFlag;
    }

    public String getSubStateName() {
        return subStateName;
    }

    public void setSubStateName(String subStateName) {
        this.subStateName = subStateName;
    }

	public String getProcessingProgress() {
		return processingProgress;
	}

	public void setProcessingProgress(String processingProgress) {
		this.processingProgress = processingProgress;
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

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

    public boolean isAuthority() {
        return isAuthority;
    }

    public void setAuthority(boolean isAuthority) {
        this.isAuthority = isAuthority;
    }

    public String getProcessPeriodName() {
        return processPeriodName;
    }

    public void setProcessPeriodName(String processPeriodName) {
        this.processPeriodName = processPeriodName;
    }

    public Long getProcessPeriod() {
        return processPeriod;
    }

    public void setProcessPeriod(Long processPeriod) {
        this.processPeriod = processPeriod;
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
	public boolean isHasResPerm() {
		return hasResPerm;
	}
	public void setHasResPerm(boolean hasResPerm) {
		this.hasResPerm = hasResPerm;
	}
    public Date getFirstViewDate() {
		return firstViewDate;
	}

	public Long getProcessId() {
        return processId;
    }
    public void setProcessId(Long processId) {
        this.processId = processId;
    }
    public void setFirstViewDate(Date firstViewDate) {
		this.firstViewDate = firstViewDate;
	}
    public String getFormViewOperation() {
        return formViewOperation;
    }
    public void setFormViewOperation(String formViewOperation) {
        this.formViewOperation = formViewOperation;
    }
}
