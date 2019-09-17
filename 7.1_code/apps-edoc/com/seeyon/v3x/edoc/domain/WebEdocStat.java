package com.seeyon.v3x.edoc.domain;

import java.util.Date;

public class WebEdocStat {
	
	private long id ;
	private String docType ;//公文的种类
	private String secretLevel ;
	private String docMark ;
	private Date createDate ;
	private String issUser ;
	private String sendTo ;
	private String remark ;
	private String subject ;
	private String serialNo ;//内部文号
	private Date recviverDate;//登记日期
	private String account ; //发文单位 
	private String createUser ;//建文人
	private Date archivedTime ;//归档时间
	private String edocType ;//公文的类型
	private String logicalPath;  //归档逻辑路径。
	private String archiveName;  //归档文件件名称
	private Long archiveId; //归档路径ID
	public Long getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}
	public String getLogicalPath() {
		return logicalPath;
	}
	public void setLogicalPath(String logicalPath) {
		this.logicalPath = logicalPath;
	}
	public String getArchiveName() {
		return archiveName;
	}
	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}
	
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public Date getArchivedTime() {
		return archivedTime;
	}
	public void setArchivedTime(Date archivedTime) {
		this.archivedTime = archivedTime;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public String getDocMark() {
		return docMark;
	}
	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getEdocType() {
		return edocType;
	}
	public void setEdocType(String edocType) {
		this.edocType = edocType;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getIssUser() {
		return issUser;
	}
	public void setIssUser(String issUser) {
		this.issUser = issUser;
	}
	public Date getRecviverDate() {
		return recviverDate;
	}
	public void setRecviverDate(Date recviverDate) {
		this.recviverDate = recviverDate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSecretLevel() {
		return secretLevel;
	}
	public void setSecretLevel(String secretLevel) {
		this.secretLevel = secretLevel;
	}
	public String getSendTo() {
		return sendTo;
	}
	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

}
