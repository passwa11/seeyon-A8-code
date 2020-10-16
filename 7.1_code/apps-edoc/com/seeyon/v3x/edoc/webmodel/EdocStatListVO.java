package com.seeyon.v3x.edoc.webmodel;

import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;

/**
 * 公文统计穿透VO
 * @author tanggl
 *
 */
public class EdocStatListVO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1653574204688242281L;
	/**
	 * 
	 */
	
	private Long summaryId;
	private Integer govdocType;
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
	private String archiveAllName;
	private String sendUnit;
	private String sendDepartment;
	private String undertakenoffice;
	private String unitLevel;
	private String unitLevelName;
	private String issuer;
	private String signingDate;
	private String currentNodesInfo;
	private Date completeTime;
	private Date deadlineDatetime;
	private Long deadline;
	private Boolean coverTime;
	private String coverTimeView;//是否超期
	private Boolean worklfowTimeout = false;
	private String deadlineTimeView;//流程期限
	private String deadlineOverView;//超期时长
	private String registrationDate;//登记时间
	private Long orgAccountId;
	private Long templeteId;
	private String bodyType;
	
	public void setListValue(Object[] object) {
		int i = 0;
		this.setSummaryId((Long)object[i++]);
		this.setGovdocType((Integer)object[i++]);
		this.setSubject((String)object[i++]);
		this.setDocMark((String)object[i++]);
		this.setSerialNo((String)object[i++]);
		this.setState((Integer)object[i++]);
		this.setStartUserId((Long)object[i++]);
		this.setDeadlineDatetime((Date)object[i++]);
		this.setDeadline((Long)object[i++]);
		this.setHasArchive((Boolean)object[i++]);		
		this.setSendDepartment((String)object[i++]);
		this.setUnitLevel((String)object[i++]);
		this.setIssuer((String)object[i++]);
		this.setCoverTime((Boolean)object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setStartTime((Timestamp)object[i++]);
		Object archivedId = object[i++];
		Object templeteId = object[i++];
		Object signingDate = object[i++];
		Object completeTime = object[i++]; 
		Object registrationDate = object[i++];
		int index=i++;
		if(object.length>=index+1){
			Object bodyType = object[index];
			if(bodyType!=null && !"".equals(bodyType.toString())){
				int bodyTypeValue=GovdocUtil.getContentType(bodyType.toString());
				this.setBodyType(String.valueOf(bodyTypeValue));
			}
		}
		this.setArchiveId(archivedId==null ? null : (Long)archivedId);
		this.setTempleteId(templeteId==null ? null : (Long)templeteId);
		
		Date startTime = this.getStartTime();
		if(startTime == null){
		    this.setStartTimeView("");
		}else {
		    this.setStartTimeView(Datetimes.format(startTime, "yyyy-MM-dd"));
        }
		
		this.setSigningDate(signingDate==null ? null : DateUtil.format((Date)signingDate, "yyyy-MM-dd"));
		this.setCompleteTime(completeTime==null ? null : (Date)completeTime);
		this.setRegistrationDate(registrationDate==null ? this.getStartTimeView() : DateUtil.format((Date)registrationDate, "yyyy-MM-dd"));
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

	public Integer getGovdocType() {
		return govdocType;
	}

	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
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

	public String getArchiveAllName() {
		return archiveAllName;
	}

	public void setArchiveAllName(String archiveAllName) {
		this.archiveAllName = archiveAllName;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}
}
