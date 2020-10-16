package com.seeyon.v3x.edoc.webmodel;

import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.ctp.util.DateUtil;

public class EdocStatSummaryVO {
	
	private Long summaryId;
	private Long templeteId;
	private Integer edocType;
	private String subject;
	private String sendDepartmentId;
	private Integer state;
	private Long orgDepartmentId;
	private Long orgAccountId;
	
	private Date startTime;
	private Timestamp createTime;
	private Long deadline;
	private Date deadlineDatetime;
	
	private Long affairId;
	private Long objectId;
	//private Long orgDepartmentId;
	private Long memberDeptId;
	private Long affairMemberId;
	private Integer affairState;
	private Integer affairSubState;
	private Integer affairApp;
	private Integer affairSubApp;
	private String affairNodePolicy;
	/////////////////////////////////////////////////////////////
	private String docMark;
	private String serialNo;
	private Long startUserId;
	private String startTimeView;
	private Boolean hasArchive;
	private Long archiveId;
	private String archiveName;
	private String sendUnit;
	private String sendDepartment;
	private String undertakenoffice;
	private String unitLevel;
	private String unitLevelName;
	private String startUserName;
	private String issuer;
	private String signingDate;
	private String isFinishView;
	private String hasArchiveView;
	private Date completeTime;
	private String currentNodesInfo;
	private Boolean coverTime;
	private String coverTimeView;//是否超期
	private Boolean worklfowTimeout = false;
	private String deadlineTimeView;//流程期限
	private String deadlineOverView;//超期时长
	private String registrationDate;
	
	public void setSummaryValue(Object[] object) {
		int i = 0;
		this.setSummaryId((Long)object[i++]);
		this.setTempleteId((Long)object[i++]);
		this.setEdocType((Integer)object[i++]);
		this.setSubject((String)object[i++]);
		this.setSendDepartmentId((String)object[i++]);
		this.setState((Integer)object[i++]);
		this.setStartTime((Date)object[i++]);
		this.setStartUserId((Long)object[i++]);
		this.setOrgDepartmentId((Long)object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setAffairId(((Long)object[i++]));
		this.setObjectId(((Long)object[i++]));
		this.setAffairState((Integer)object[i++]);
		this.setAffairSubState((Integer)object[i++]);
		this.setAffairMemberId((Long)object[i++]);
		this.setAffairApp((Integer)object[i++]);
		this.setAffairSubApp((Integer)object[i++]);
		this.setAffairNodePolicy((String)object[i++]);
		this.setMemberDeptId((Long)object[i++]);
	}
	
	public void setListValue(Object[] object) {
		int i = 0;
		this.setSummaryId((Long)object[i++]);
		this.setSubject((String)object[i++]);
		this.setDocMark((String)object[i++]);
		this.setSerialNo((String)object[i++]);
		this.setState((Integer)object[i++]);
		this.setStartUserId((Long)object[i++]);
		this.setStartTime((Date)object[i++]);
		this.setDeadlineDatetime((Date)object[i++]);
		this.setDeadline((Long)object[i++]);
		this.setHasArchive((Boolean)object[i++]);
		Object archivedId = object[i++]; 
		if(archivedId != null) {
			this.setArchiveId((Long)archivedId);
		}
		this.setSendDepartment((String)object[i++]);
		this.setUnitLevel((String)object[i++]);
		this.setIssuer((String)object[i++]);
		Object signingDate = object[i++]; 
		if(signingDate != null) {
			this.setSigningDate(DateUtil.format((Date)signingDate, "yyyy-MM-dd"));
		}		
		Object completeTime = object[i++]; 
		if(completeTime != null) {
			this.setCompleteTime((Date)completeTime);
		}
		this.setCoverTime((Boolean)object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		Object templeteId = object[i++]; 
		if(templeteId != null) {
			this.setTempleteId((Long)templeteId);
		}
		Object registrationDate = object[i++]; 
		if(registrationDate != null) {
			this.setRegistrationDate(DateUtil.format((Date)registrationDate, "yyyy-MM-dd"));
		} else {
		    Date startTime = this.getStartTime();
		    if(startTime == null){
		        this.setRegistrationDate("");
		    }else {
		        this.setRegistrationDate(DateUtil.format(startTime, "yyyy-MM-dd"));
            }
		}
	}
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Long getTempleteId() {
		return templeteId;
	}
	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
	}
	public String getSendDepartmentId() {
		return sendDepartmentId;
	}
	public void setSendDepartmentId(String sendDepartmentId) {
		this.sendDepartmentId = sendDepartmentId;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public Integer getAffairState() {
		return affairState;
	}
	public void setAffairState(Integer affairState) {
		this.affairState = affairState;
	}
	public Integer getAffairSubState() {
		return affairSubState;
	}
	public void setAffairSubState(Integer affairSubState) {
		this.affairSubState = affairSubState;
	}
	public Long getAffairMemberId() {
		return affairMemberId;
	}
	public void setAffairMemberId(Long affairMemberId) {
		this.affairMemberId = affairMemberId;
	}
	
	public Integer getEdocType() {
		return edocType;
	}
	public Long getMemberDeptId() {
		return memberDeptId;
	}

	public void setMemberDeptId(Long memberDeptId) {
		this.memberDeptId = memberDeptId;
	}

	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}

	public Integer getAffairApp() {
		return affairApp;
	}

	public void setAffairApp(Integer affairApp) {
		this.affairApp = affairApp;
	}

	public Integer getAffairSubApp() {
		return affairSubApp;
	}

	public void setAffairSubApp(Integer affairSubApp) {
		this.affairSubApp = affairSubApp;
	}

	public String getDocMark() {
		return docMark;
	}

	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public Long getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(Long startUserId) {
		this.startUserId = startUserId;
	}

	public Boolean getHasArchive() {
		return hasArchive;
	}

	public void setHasArchive(Boolean hasArchive) {
		this.hasArchive = hasArchive;
	}

	public Long getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}

	public String getSendUnit() {
		return sendUnit;
	}

	public void setSendUnit(String sendUnit) {
		this.sendUnit = sendUnit;
	}

	public String getSendDepartment() {
		return sendDepartment;
	}

	public void setSendDepartment(String sendDepartment) {
		this.sendDepartment = sendDepartment;
	}

	public String getUnitLevelName() {
		return unitLevelName;
	}

	public void setUnitLevelName(String unitLevelName) {
		this.unitLevelName = unitLevelName;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getSigningDate() {
		return signingDate;
	}

	public void setSigningDate(String signingDate) {
		this.signingDate = signingDate;
	}

	public String getIsFinishView() {
		return isFinishView;
	}

	public void setIsFinishView(String isFinishView) {
		this.isFinishView = isFinishView;
	}

	public String getHasArchiveView() {
		return hasArchiveView;
	}

	public void setHasArchiveView(String hasArchiveView) {
		this.hasArchiveView = hasArchiveView;
	}

	public String getCurrentNodesInfo() {
		return currentNodesInfo;
	}

	public void setCurrentNodesInfo(String currentNodesInfo) {
		this.currentNodesInfo = currentNodesInfo;
	}

	public Date getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}

	public String getStartUserName() {
		return startUserName;
	}

	public void setStartUserName(String startUserName) {
		this.startUserName = startUserName;
	}

	public Boolean getCoverTime() {
		return coverTime;
	}

	public void setCoverTime(Boolean coverTime) {
		this.coverTime = coverTime;
	}

	public String getCoverTimeView() {
		return coverTimeView;
	}

	public void setCoverTimeView(String coverTimeView) {
		this.coverTimeView = coverTimeView;
	}

	public String getAffairNodePolicy() {
		return affairNodePolicy;
	}

	public void setAffairNodePolicy(String affairNodePolicy) {
		this.affairNodePolicy = affairNodePolicy;
	}

	public Date getDeadlineDatetime() {
		return deadlineDatetime;
	}

	public void setDeadlineDatetime(Date deadlineDatetime) {
		this.deadlineDatetime = deadlineDatetime;
	}

	public Boolean getWorklfowTimeout() {
		return worklfowTimeout;
	}

	public void setWorklfowTimeout(Boolean worklfowTimeout) {
		this.worklfowTimeout = worklfowTimeout;
	}

	public Long getDeadline() {
		return deadline;
	}

	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getDeadlineTimeView() {
		return deadlineTimeView;
	}

	public void setDeadlineTimeView(String deadlineTimeView) {
		this.deadlineTimeView = deadlineTimeView;
	}

	public String getDeadlineOverView() {
		return deadlineOverView;
	}

	public void setDeadlineOverView(String deadlineOverView) {
		this.deadlineOverView = deadlineOverView;
	}

	public Long getOrgDepartmentId() {
		return orgDepartmentId;
	}

	public void setOrgDepartmentId(Long orgDepartmentId) {
		this.orgDepartmentId = orgDepartmentId;
	}

	public Long getOrgAccountId() {
		return orgAccountId;
	}

	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}

	public String getStartTimeView() {
		return startTimeView;
	}

	public void setStartTimeView(String startTimeView) {
		this.startTimeView = startTimeView;
	}

	public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}

	public String getUndertakenoffice() {
		return undertakenoffice;
	}

	public void setUndertakenoffice(String undertakenoffice) {
		this.undertakenoffice = undertakenoffice;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	
}
