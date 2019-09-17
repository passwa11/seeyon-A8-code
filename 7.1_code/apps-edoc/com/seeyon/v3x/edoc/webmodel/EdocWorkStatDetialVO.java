package com.seeyon.v3x.edoc.webmodel;

import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.ctp.util.DateUtil;

/**
 * 工作统计穿透VO
 * @author zhangdong
 *
 */
public class EdocWorkStatDetialVO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long summaryId;
	private String subject;
	private String docMark;
	private String serialNo;
	private Long startUserId;
	private String startUserName;
	private Timestamp startTime;
	private String startTimeView;
	private Integer state;
	private String isFinishView;
	private Boolean hasArchive;
	private String hasArchiveView;
	private Long archiveId;
	private String archiveName;
	private String sendUnit;
	private String sendDepartment;
	private String undertakenoffice;
	private String unitLevel;
	private String unitLevelName;
	private String issuer;
	private String signingDate;
	private String currentNodesInfo;
	private Date completeTime;
	private String completeTimeView;
	private Date deadlineDatetime;
	private Long deadline;
	private Boolean coverTime;
	private String coverTimeView;//是否超期
	private Boolean worklfowTimeout = false;
	private String deadlineTimeView;//流程期限
	private String deadlineOverView;//超期时长
	private String registrationDate;//登记时间
	private String receipDateView;//签收日期
	private Date receipDate;//签收日期
	private Long memberId;
	private Long orgAccountId;
	private Long templeteId;
	//xuker add v5.7区分新老公文。0代表老公文，1代表新发文，2代表新收文，4代表新交换
	private Integer govdocType;
	//xuker add v5.7 发起人的affairid 用于统计列表穿透
	private Long affairId;
	
	private String operDept;
	
	private String maxOverPerson;
	private String maxNodePolicy;
	public void setListValue(Object[] object) {
		int i = 0;
		this.setSummaryId((Long)object[i++]);
		this.setSubject((String)object[i++]);
		this.setDocMark((String)object[i++]);
		this.setSerialNo((String)object[i++]);
		this.setState((Integer)object[i++]);
		this.setStartUserId((Long)object[i++]);
		this.setDeadlineDatetime((Date)object[i++]);
		this.setDeadline((Long)object[i++]);
		this.setHasArchive((Boolean)object[i++]);		
		this.setUnitLevel((String)object[i++]);
		this.setIssuer((String)object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setStartTime((Timestamp)object[i++]);
		Object archivedId = object[i++];
		Object templeteId = object[i++];
		Object signingDate = object[i++];
		Object completeTime = object[i++]; 
		Object registrationDate = object[i++];
		this.setArchiveId(archivedId==null ? null : (Long)archivedId);
		this.setTempleteId(templeteId==null ? null : (Long)templeteId);
		Object receipDateObj = object[i++];
		if(receipDateObj != null) {
			this.setReceipDate(new Timestamp(((Date)receipDateObj).getTime()));	
		}
		Date startTime = this.getStartTime();
		Date receipDate = this.getReceipDate();
		if(receipDate == null){
		    this.setReceipDateView("");
		}else {
		    this.setReceipDateView(DateUtil.format(receipDate, "yyyy-MM-dd"));
        }
		if(startTime == null){
		    this.setStartTimeView("");
		}else {
		    this.setStartTimeView(DateUtil.format(startTime, "yyyy-MM-dd"));
        }
		this.setSigningDate(signingDate==null ? null : DateUtil.format((Date)signingDate, "yyyy-MM-dd"));
		this.setCompleteTime(completeTime==null ? null :(Date)completeTime);
		this.setCompleteTimeView(completeTime==null ? null :DateUtil.format((Date)completeTime, "yyyy-MM-dd"));
		this.setRegistrationDate(registrationDate==null ? this.getStartTimeView() : DateUtil.format((Date)registrationDate, "yyyy-MM-dd"));
		this.setGovdocType((Integer)object[i++]);
	}
	
	public String getMaxOverPerson() {
		return maxOverPerson;
	}

	public void setMaxOverPerson(String maxOverPerson) {
		this.maxOverPerson = maxOverPerson;
	}

	public String getMaxNodePolicy() {
		return maxNodePolicy;
	}

	public void setMaxNodePolicy(String maxNodePolicy) {
		this.maxNodePolicy = maxNodePolicy;
	}

	public String getOperDept() {
		return operDept;
	}

	public void setOperDept(String operDept) {
		this.operDept = operDept;
	}

	public String getCompleteTimeView() {
		return completeTimeView;
	}

	public void setCompleteTimeView(String completeTimeView) {
		this.completeTimeView = completeTimeView;
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
	public String getStartTimeView() {
		return startTimeView;
	}
	public void setStartTimeView(String startTimeView) {
		this.startTimeView = startTimeView;
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
	public String getUndertakenoffice() {
		return undertakenoffice;
	}
	public void setUndertakenoffice(String undertakenoffice) {
		this.undertakenoffice = undertakenoffice;
	}
	public String getUnitLevel() {
		return unitLevel;
	}
	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}
	public String getUnitLevelName() {
		return unitLevelName;
	}
	public void setUnitLevelName(String unitLevelName) {
		this.unitLevelName = unitLevelName;
	}
	public String getStartUserName() {
		return startUserName;
	}
	public void setStartUserName(String startUserName) {
		this.startUserName = startUserName;
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
	public Date getCompleteTime() {
		return completeTime;
	}
	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}
	public String getCurrentNodesInfo() {
		return currentNodesInfo;
	}
	public void setCurrentNodesInfo(String currentNodesInfo) {
		this.currentNodesInfo = currentNodesInfo;
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
	public Boolean getWorklfowTimeout() {
		return worklfowTimeout;
	}
	public void setWorklfowTimeout(Boolean worklfowTimeout) {
		this.worklfowTimeout = worklfowTimeout;
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
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getDeadlineDatetime() {
		return deadlineDatetime;
	}

	public void setDeadlineDatetime(Date deadlineDatetime) {
		this.deadlineDatetime = deadlineDatetime;
	}

	public Long getDeadline() {
		return deadline;
	}

	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}

	public Long getOrgAccountId() {
		return orgAccountId;
	}

	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}

	public Long getTempleteId() {
		return templeteId;
	}

	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Integer getGovdocType() {
		return govdocType;
	}

	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
	}

	public Long getAffairId() {
		return affairId;
	}

	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}

	public String getReceipDateView() {
		return receipDateView;
	}

	public void setReceipDateView(String receipDateView) {
		this.receipDateView = receipDateView;
	}

	public Date getReceipDate() {
		return receipDate;
	}

	public void setReceipDate(Date receipDate) {
		this.receipDate = receipDate;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	
}
