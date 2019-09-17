package com.seeyon.apps.govdoc.vo;

import java.util.Date;

public class GovdocQueryVO {

	private int edocType;	//公文类型,收文,发文,签报
	private String subject;	//公文标题
	private String keywords;
	private String docMark;
	private String signMark;//签收编号
	private String serialNo;	//内部文号 (收文编号)
	private String docType;		//公文种类
	private String sendType;	//行文类型
	private String createPerson;	//建文人
	private Date createTimeB;	//建文日期
	private Date createTimeE;	//建文日期
	private String sendUnit;	//发文单位
	private String sendUnitId;	//发文单位Id
	private String sendDepartment;//发文部门
	private String sendDepartmentId;//发文部门ID
	private String sendTo;	//主送单位
	private String sendToId;	//主送单位
	private String issuer;	//签发人
	private Date signingDateA;	//签发日期
	private Date signingDateB;	//签发日期

	private String nodePolicy; //节点权限
	private Date createDateB;	//发文日期
	private Date createDateE;	//发文日期
	private String edocUnit; //成文单位
	private Date recieveDateB; //收文  签收时间
	private Date recieveDateE;
	private Date registerDateB;//收文 登记时间
	private Date registerDateE;
	private Date receiveTimeB; //到达时间
	private Date receiveTimeE;
	private String secretLevel; //文件密级
	private String urgentLevel; //紧急程度
	private String startMemberName; //发起人
	private Date expectprocesstimeB;
	private Date expectprocesstimeE;
	private String classification;
	private String phone; //联系电话
	private String auditor; //审核人
	private String printer; //打印人
	private String undertaker; //承办人
	private String undertakenoffice; //承办机构
	private String undertakenofficeId; //承办机构
	private Integer copies; //印发份数
	private String printUnit; //印发单位
	private String printUnitId; //印发单位
	private Integer keepPeriod; //保密期限
	private String review; //复核人
	private Date packdateB; //封发日期
	private Date packdateE; //封发日期
	private String unitLevel; //公文级别
	private String signPerson;
	private Long signAccountId;
	
	private String party; //党务机关
    private String administrative; //政务机关
	private String isGourpBy;//同一流程只显示最后一条
	
    private int sendQueryTimeType;//发文登记簿——查询时间选择，原来的时间为交换日期，新增签发日期、拟文日期

	public int getEdocType() {
		return edocType;
	}

	public void setEdocType(int edocType) {
		this.edocType = edocType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDocMark() {
		return docMark;
	}

	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}

	public String getSignMark() {
		return signMark;
	}

	public void setSignMark(String signMark) {
		this.signMark = signMark;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public String getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	public Date getCreateTimeB() {
		return createTimeB;
	}

	public void setCreateTimeB(Date createTimeB) {
		this.createTimeB = createTimeB;
	}

	public Date getCreateTimeE() {
		return createTimeE;
	}

	public void setCreateTimeE(Date createTimeE) {
		this.createTimeE = createTimeE;
	}

	public String getSendUnit() {
		return sendUnit;
	}

	public void setSendUnit(String sendUnit) {
		this.sendUnit = sendUnit;
	}

	public String getSendUnitId() {
		return sendUnitId;
	}

	public void setSendUnitId(String sendUnitId) {
		this.sendUnitId = sendUnitId;
	}

	public String getSendDepartment() {
		return sendDepartment;
	}

	public void setSendDepartment(String sendDepartment) {
		this.sendDepartment = sendDepartment;
	}

	public String getSendDepartmentId() {
		return sendDepartmentId;
	}

	public void setSendDepartmentId(String sendDepartmentId) {
		this.sendDepartmentId = sendDepartmentId;
	}

	public String getSendTo() {
		return sendTo;
	}

	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}

	public String getSendToId() {
		return sendToId;
	}

	public void setSendToId(String sendToId) {
		this.sendToId = sendToId;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public Date getSigningDateA() {
		return signingDateA;
	}

	public void setSigningDateA(Date signingDateA) {
		this.signingDateA = signingDateA;
	}

	public Date getSigningDateB() {
		return signingDateB;
	}

	public void setSigningDateB(Date signingDateB) {
		this.signingDateB = signingDateB;
	}

	public String getNodePolicy() {
		return nodePolicy;
	}

	public void setNodePolicy(String nodePolicy) {
		this.nodePolicy = nodePolicy;
	}

	public Date getCreateDateB() {
		return createDateB;
	}

	public void setCreateDateB(Date createDateB) {
		this.createDateB = createDateB;
	}

	public Date getCreateDateE() {
		return createDateE;
	}

	public void setCreateDateE(Date createDateE) {
		this.createDateE = createDateE;
	}

	public String getEdocUnit() {
		return edocUnit;
	}

	public void setEdocUnit(String edocUnit) {
		this.edocUnit = edocUnit;
	}

	public Date getRecieveDateB() {
		return recieveDateB;
	}

	public void setRecieveDateB(Date recieveDateB) {
		this.recieveDateB = recieveDateB;
	}

	public Date getRecieveDateE() {
		return recieveDateE;
	}

	public void setRecieveDateE(Date recieveDateE) {
		this.recieveDateE = recieveDateE;
	}

	public Date getRegisterDateB() {
		return registerDateB;
	}

	public void setRegisterDateB(Date registerDateB) {
		this.registerDateB = registerDateB;
	}

	public Date getRegisterDateE() {
		return registerDateE;
	}

	public void setRegisterDateE(Date registerDateE) {
		this.registerDateE = registerDateE;
	}

	public Date getReceiveTimeB() {
		return receiveTimeB;
	}

	public void setReceiveTimeB(Date receiveTimeB) {
		this.receiveTimeB = receiveTimeB;
	}

	public Date getReceiveTimeE() {
		return receiveTimeE;
	}

	public void setReceiveTimeE(Date receiveTimeE) {
		this.receiveTimeE = receiveTimeE;
	}

	public String getSecretLevel() {
		return secretLevel;
	}

	public void setSecretLevel(String secretLevel) {
		this.secretLevel = secretLevel;
	}

	public String getUrgentLevel() {
		return urgentLevel;
	}

	public void setUrgentLevel(String urgentLevel) {
		this.urgentLevel = urgentLevel;
	}

	public String getStartMemberName() {
		return startMemberName;
	}

	public void setStartMemberName(String startMemberName) {
		this.startMemberName = startMemberName;
	}

	public Date getExpectprocesstimeB() {
		return expectprocesstimeB;
	}

	public void setExpectprocesstimeB(Date expectprocesstimeB) {
		this.expectprocesstimeB = expectprocesstimeB;
	}

	public Date getExpectprocesstimeE() {
		return expectprocesstimeE;
	}

	public void setExpectprocesstimeE(Date expectprocesstimeE) {
		this.expectprocesstimeE = expectprocesstimeE;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAuditor() {
		return auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	public String getPrinter() {
		return printer;
	}

	public void setPrinter(String printer) {
		this.printer = printer;
	}

	public String getUndertaker() {
		return undertaker;
	}

	public void setUndertaker(String undertaker) {
		this.undertaker = undertaker;
	}

	public String getUndertakenoffice() {
		return undertakenoffice;
	}

	public void setUndertakenoffice(String undertakenoffice) {
		this.undertakenoffice = undertakenoffice;
	}

	public String getUndertakenofficeId() {
		return undertakenofficeId;
	}

	public void setUndertakenofficeId(String undertakenofficeId) {
		this.undertakenofficeId = undertakenofficeId;
	}

	public Integer getCopies() {
		return copies;
	}

	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	public String getPrintUnit() {
		return printUnit;
	}

	public void setPrintUnit(String printUnit) {
		this.printUnit = printUnit;
	}

	public String getPrintUnitId() {
		return printUnitId;
	}

	public void setPrintUnitId(String printUnitId) {
		this.printUnitId = printUnitId;
	}

	public Integer getKeepPeriod() {
		return keepPeriod;
	}

	public void setKeepPeriod(Integer keepPeriod) {
		this.keepPeriod = keepPeriod;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public Date getPackdateB() {
		return packdateB;
	}

	public void setPackdateB(Date packdateB) {
		this.packdateB = packdateB;
	}

	public Date getPackdateE() {
		return packdateE;
	}

	public void setPackdateE(Date packdateE) {
		this.packdateE = packdateE;
	}

	public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}

	public String getSignPerson() {
		return signPerson;
	}

	public void setSignPerson(String signPerson) {
		this.signPerson = signPerson;
	}

	public Long getSignAccountId() {
		return signAccountId;
	}

	public void setSignAccountId(Long signAccountId) {
		this.signAccountId = signAccountId;
	}

	public String getParty() {
		return party;
	}

	public void setParty(String party) {
		this.party = party;
	}

	public String getAdministrative() {
		return administrative;
	}

	public void setAdministrative(String administrative) {
		this.administrative = administrative;
	}

	public String getIsGourpBy() {
		return isGourpBy;
	}

	public void setIsGourpBy(String isGourpBy) {
		this.isGourpBy = isGourpBy;
	}

	public int getSendQueryTimeType() {
		return sendQueryTimeType;
	}

	public void setSendQueryTimeType(int sendQueryTimeType) {
		this.sendQueryTimeType = sendQueryTimeType;
	}

}
