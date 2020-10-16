package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocRecieveRecord extends BaseModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3908150799045335949L;

	/** 公文交换状态：待签收 */
	public final static int Exchange_iStatus_Torecieve = 0;
	
	/** 公文交换状态：已签收 */
	public final static int Exchange_iStatus_Recieved = 1;
	
	/** 公文交换状态：已登记 */
	public final static int Exchange_iStatus_Registered = 2;
	
	/** 公文交换状态：登记保存待发 */
	public final static int Exchange_iStatus_RegisterToWaitSend = 10;
	
	/** 公文交换状态（收文回执）：已回退/取回 */
	public final static int Exchange_iStatus_Receive_StepBacked = 3;
	
	/** 公文交换状态：被退回到退件箱  */
	public final static int Exchange_iStatus_Receive_Retreat = 4;	
	
	/** 公文交换状态：已签收-被删除 */
	public final static int Exchange_iStatus_Receive_Delete = 5;	
	
	/** 公文交换状态：待签收-被删除 */
	public final static int Exchange_iStatus_ToReceive_Delete = 6;	
	
	/** 没被退回 */
	public final static int Receive_Retreat_No = 0;
	
	/** 被退回过 */
	public final static int Receive_Retreat_Yes = 1;
	
	/** 交换帐号类型：手工添加的外部单位（无法交换） */
	public final static int Exchange_Receive_iAccountType_Default = 0;
	
	/** 交换帐号类型：单位帐号(exchangeType值域) */
	public final static int Exchange_Receive_iAccountType_Org = 1;
	
	/** 交换帐号类型：部门帐号(exchangeType值域) */
	public final static int Exchange_Receive_iAccountType_Dept = 2;
	
	/** 交换帐号类型：人员帐号(exchangeType值域) */
	public final static int Exchange_Receive_iAccountType_Person = 3;
	
	private String subject; // 公文标题
	private String docType; // 公文种类
	private String docMark; // 公文文号
	private String secretLevel; // 公文密级
	private String urgentLevel; // 紧急程度
	private String keepPeriod; // 保管期限
	private String sendUnit; // 发文单位
	private String sender;   //发文人/送文人
	private String issuer; // 签发人
	private Date issueDate; // 签发日期
	private String sendTo; // 公文“主送”单位
	private String copyTo; // 公文“抄送”单位
	private String reportTo; // 公文“抄报”单位
	private String remark; // 备考,或纸质文件说明
	private long edocId; // 公文记录ID
	private Long reciveEdocId;//收文记录ID

	private long fileId; // 公文交换XML文件（外部交换）
	private boolean fromInternal; // 公文是否来自内部交换（同一套系统）
	private String replyId; // 回执ID（发文时带过来的，用于回执公文的标识）	
	private String recNo;	// 签收编号
	private long recUserId; // 签收人
	private String recUser; //签收人名称,只用于前台显示,无须持久化
	private String recAccountName; //签收单位名称，同上
	private Integer copies; //签收数量，同上
	private Timestamp recTime; // 签收时间	
	private long registerUserId; // 登记人ID
	private String registerName; //登记人姓名,同上
	private Timestamp registerTime; //登记时间
	private long exchangeOrgId; // 单位ID或部门ID	
	private int exchangeType; // 交换类型	
	private Timestamp createTime; // 收文记录创建时间	
	private int status; // 待签收，已签收（已回执，待登记），已登记
	private Integer contentNo;
	private String stepBackInfo;// 回退信息
	private Integer sendUnitType;//来文类型
	private Integer isRetreat;//是否被退回 
	private Integer isTurnRec;//是否为转收文类型(0 不是 ，1 是)
	private Integer exchangeMode =0;
	
	 public Integer getExchangeMode() {
		return exchangeMode;
	}

	public void setExchangeMode(Integer exchangeMode) {
		this.exchangeMode = exchangeMode;
	}
	 /**
	 * 内部公文交换
	 */
	public final static int ExchangeMode_Internal=0;
	/**
	 * 书生公文交换
	 */
	public final static int ExchangeMode_Sursen=1;
	
	/** 是否代理 */
    private boolean proxy;

    /** 代理人 */
    private String proxyName;
    
    /** 代理人 Id*/
    private Long proxyUserId;

    /** */
    private String proxyLabel;
	
    public Integer getIsTurnRec() {
		return isTurnRec;
	}

	public void setIsTurnRec(Integer isTurnRec) {
		this.isTurnRec = isTurnRec;
	}
	public String getProxyLabel() {
		return proxyLabel;
	}
	public void setProxyLabel(String proxyLabel) {
		this.proxyLabel = proxyLabel;
	}
	public Long getProxyUserId() {
		return proxyUserId;
	}
	public void setProxyUserId(Long proxyUserId) {
		this.proxyUserId = proxyUserId;
	}
	public boolean isProxy() {
		return proxy;
	}
	public void setProxy(boolean proxy) {
		this.proxy = proxy;
	}
	public String getProxyName() {
		return proxyName;
	}
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}
	public Integer getIsRetreat() {
		return isRetreat;
	}
	public void setIsRetreat(Integer isRetreat) {
		this.isRetreat = isRetreat;
	}
	

	public Integer getSendUnitType() {
		return sendUnitType;
	}

	public void setSendUnitType(Integer sendUnitType) {
		this.sendUnitType = sendUnitType;
	}

	public String getStepBackInfo() {
		return stepBackInfo;
	}
	public void setStepBackInfo(String stepBackInfo) {
		this.stepBackInfo = stepBackInfo;
	}
	public Integer getContentNo()
	{
		return this.contentNo;
	}
	public void setContentNo(Integer contentNo)
	{
		this.contentNo=contentNo;
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
	
	public String getSendUnit() {
		return sendUnit;
	}
	
	public void setSendUnit(String sendUnit) {
		this.sendUnit = sendUnit;
	}
	
	public String getIssuer() {
		return issuer;
	}
	
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	
	public Date getIssueDate() {
		return issueDate;
	}
	
	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
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
	
	public String getReportTo() {
		return reportTo;
	}
	
	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}
	
	public String getKeepPeriod() {
		return keepPeriod;
	}
	
	public void setKeepPeriod(String keepPeriod) {
		this.keepPeriod = keepPeriod;
	}
	
	public String getRecNo() {
		return recNo;
	}
	
	public void setRecNo(String recNo) {
		this.recNo = recNo;
	}
	
	public long getRecUserId() {
		return recUserId;
	}
	
	public void setRecUserId(long recUserId) {
		this.recUserId = recUserId;
	}
	
	public Timestamp getRecTime() {
		return recTime;
	}
	
	public void setRecTime(Timestamp recTime) {
		this.recTime = recTime;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public long getEdocId() {
		return edocId;
	}
	
	public void setEdocId(long edocId) {
		this.edocId = edocId;
	}
	
	public long getFileId() {
		return fileId;
	}
	
	public void setFileId(long fileId) {
		this.fileId = fileId;
	}
	
	public boolean getFromInternal() {
		return fromInternal;
	}
	
	public void setFromInternal(boolean fromInternal) {
		this.fromInternal = fromInternal;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}	
	
	public String getReplyId() {
		return replyId;
	}
	
	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}	
	
	public long getExchangeOrgId() {
		return exchangeOrgId;
	}
	
	public void setExchangeOrgId(long exchangeOrgId) {
		this.exchangeOrgId = exchangeOrgId;
	}	
	
	public int getExchangeType() {
		return exchangeType;
	}
	
	public void setExchangeType(int exchangeType) {
		this.exchangeType = exchangeType;
	}
	
	public long getRegisterUserId() {
		return registerUserId;
	}
	
	public void setRegisterUserId(long registerUserId) {
		this.registerUserId = registerUserId;
	}
	
	public Timestamp getRegisterTime() {
		return registerTime;
	}
	
	public void setRegisterTime(Timestamp registerTime) {
		this.registerTime = registerTime;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecUser() {
		return recUser;
	}

	public void setRecUser(String recUser) {
		this.recUser = recUser;
	}

	public String getRecAccountName() {
		return recAccountName;
	}

	public void setRecAccountName(String recAccountName) {
		this.recAccountName = recAccountName;
	}

	public Integer getCopies() {
		return copies;
	}

	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	public String getRegisterName() {
		return registerName;
	}

	public void setRegisterName(String registerName) {
		this.registerName = registerName;
	}
	public void setReciveEdocId(Long reciveEdocId) {
		this.reciveEdocId = reciveEdocId;
	}
	public Long getReciveEdocId() {
		return reciveEdocId;
	}
	
}
