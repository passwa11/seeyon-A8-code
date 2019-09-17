package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocStat extends BaseModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1063809813278915439L;
	private long edocId; // 公文id
	private int edocType; // 公文类型
	private Integer processType = 1; // 办件阅件类型 1办件 2阅件
	private String subject; // 公文标题
	private String docType; //公文种类
	private String docMark=""; // 公文文号
	private long deptId; //收文部门id或发文部门id
	private Date createDate; //建文日期
	private String issuer;//签发人
	private String sendTo; //主送单位
	private String copyTo; //抄送单位
	private Integer copies;//份数
	private boolean isSent=false; // 是否已封发
	private boolean isArchived=false; // 是否已归档
	private int flowState=-1; //公文流转状态
	private long domainId; // 统计单位id<公文按单位进行统计>
	private long count=0L;
	private String year;
	private String month;
	private String remark; //备考	
	private Integer contentNo;
	private String serialNo ;//内部文号
	private Date archivedTime ;//归档时间
	private Long accountId ; //发文单位
	private Long orgDepartmentId;//发文部门，edoc_summary的org_department_id
	private Long createUserid ;//建文人
	private String day;
	private String quarter;
	private String sendType;
	private String unitLevel;
	private Long templeteId;
	
	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getEdocId() {
		return edocId;
	}
	
	public void setEdocId(long edocId) {
		this.edocId = edocId;
	}
	
	public int getEdocType() {
		return edocType;
	}
	
	public void setEdocType(int edocType) {
		this.edocType = edocType;
	}
	
	public Integer getProcessType() {
		return processType;
	}

	public void setProcessType(Integer processType) {
		this.processType = processType;
	}

	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getDocType() {
		return docType;
	}
	
	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	public String getDocMark() {
		return docMark;
	}
	
	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}
	
	public long getDeptId() {
		return deptId;
	}
	
	public void setDeptId(long deptId) {
		this.deptId = deptId;
	}

	public Date getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getIssuer() {
		return issuer;
	}
	
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	
	public String getSendTo() {
		return sendTo;
	}
	
	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}
	
	public String getCopyTo() {
		return copyTo;
	}
	
	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}
	
	public Integer getCopies() {
		return copies;
	}
	
	public void setCopies(Integer copies) {
		this.copies = copies;
	}
	
	public boolean getIsSent() {
		return isSent;
	}
	
	public void setIsSent(boolean isSent) {
		this.isSent = isSent;
	}
	
	public boolean getIsArchived() {
		return isArchived;
	}
	
	public void setIsArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}
	
	public int getFlowState() {
		return flowState;
	}
	
	public void setFlowState(int flowState) {
		this.flowState = flowState;
	}
	
	public long getDomainId() {
		return domainId;
	}
	
	public void setDomainId(long domainId) {
		this.domainId = domainId;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getContentNo() {
		return contentNo;
	}

	public void setContentNo(Integer contentNo) {
		this.contentNo = contentNo;
	}



	public Date getArchivedTime() {
		return archivedTime;
	}

	public void setArchivedTime(Date archivedTime) {
		this.archivedTime = archivedTime;
	}



	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getCreateUserid() {
		return createUserid;
	}

	public void setCreateUserid(Long createUserid) {
		this.createUserid = createUserid;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}

	public Long getTempleteId() {
		return templeteId;
	}

	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
	}

	public Long getOrgDepartmentId() {
		return orgDepartmentId;
	}

	public void setOrgDepartmentId(Long orgDepartmentId) {
		this.orgDepartmentId = orgDepartmentId;
	}

}
