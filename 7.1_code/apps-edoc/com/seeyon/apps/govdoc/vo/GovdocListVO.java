package com.seeyon.apps.govdoc.vo;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.ctp.util.Strings;

import java.sql.Timestamp;
import java.util.Date;

public class GovdocListVO {

	private Long summaryId;
	private String identifier;
	private String subject;//列头-标题
	private Integer importantLevel;//列头-紧急程序(标题图标)
	private Long startUserId;
	private String startUserName;//列头-发起
	private String createPerson;//列头-拟稿人/登记人
	private Date registerDate;//列头-登记日期
	private String issuer;//列头-签发人
	private Date signingDate;//列头-签发日期
	private Integer contentType;//公文正文类型
	
	private Timestamp createTime;//列头-发起时间（待发中使用，实际上是创建时间）
	private Timestamp startTime;//列头-发起时间
	private Timestamp completeTime;
	
	private Long summaryDeadline;
	private Date summaryDeadlineDatetime;
	private String summaryDeadLineName ;  //列头-流程期限
	private Boolean summaryIsCoverTime;//列头-公文流程是否超期
	private Integer summaryState;//公文流程状态
	private Integer summaryTransferStatus;//公文交换状态
	private String summaryTransferStatusName;
	private Long orgAccountId;
	private Long orgDepartmentId;
	private Boolean isQuickSend;
	private Long caseId;
	private String processId;
	private Long templeteId;
	private Boolean hasArchive;
	private String hasArchiveTxt;//列头-是否归档
	private Long archiveId;
	private String archiveName;//列头-归档路径
	private Integer govdocType;//列头-公文分类
	private Integer edocType;//列头-公文分类
	private String docMark;//列头-公文文号
	private String docMark2;
	private String serialNo;//列头-内部文号
	private Integer hastenTimes = 0;//列头-催办次数
	private Long exchangeSendAffairId = -1L;//列头-分送状态
	
	private String currentNodesInfo;//列头-当前待办人
	
	private String printUnit = "";//印发单位
	private String printer = "";//打印人
	private Integer copies = 0;//列头-印发份数
	private Integer copies2 = 0;
	private String phone = "";//联系电话
	private String auditor = "";//审核人
	private String review = "";//复核人
	private String undertaker = "";//承办人
	private String undertakenoffice = "";//承办机构
	private String receiveUnit = "";//接收单位
	private String signPerson = "";//签收人
	private String signMark = "";//签收编号
	private Date packTime;//分送日期
	private Date receiptDate;//签收日期
	private Date registrationDate;//登记日期

	private Integer edocSecretLevel;
	private String secretLevel;//列头-密级
	private String urgentLevel;//列头-紧急程度
	private String unitLevel;//列头-公文级别
	private Integer keepPeriod;//列头-保密期限
	private String keepPeriodTxt;//列头-流程期限
	private String docType;//列头-公文种类
	private String sendType;//列头-行文类型	
	
	private String sendTo = "";//列头-主送单位
	private String sendTo2 = "";
	private String sendToId = "";
	private String sendToId2 = "";
	private String copyTo = "";//列头-抄送单位
	private String copyTo2 = "";
	private String copyToId = "";
	private String copyToId2 = "";
	private String reportTo = "";//列头-抄报单位
	private String reportTo2 = "";
	private String reportToId = "";
	private String reportToId2 = "";
	private String sendUnit = "";//列头-发文单位/来文单位(公文查询列表中该字段名显示有问题)
	private String sendUnit2 = "";
	private String sendUnitId = "";
	private String sendUnitId2 = "";
	private String sendDepartment = "";//列头-发文部门/来文部门
	private String sendDepartment2 = "";
	private String sendDepartmentId = "";
	private String sendDepartmentId2 = "";
	
	//affair开头表示从ctp_affair中取的数据
	private Long affairId;
	private Integer affairState;
	private Integer affairSubState;//列头-状态(待发)/列头-处理状态(待办)
	private Integer affairTrack;//列头-是否跟踪
	private Long affairMemberId;
	private Long affairTransactorId;
	private String affairNodePolicy;
	private String affairIdentifier;
	private Long affairObjectId;
	private Long affairSubObjectId;
	private Long affairActivityId;
	private Long affairArchiveId;
	private String affairSubject;
	private String affairForwardMember;
	private Long affairBackFromId;
	private Integer affairApp;
	private Integer affairSubApp;
	private Boolean affairFinish;

	private Integer affairHastenTimes = 0;
	private Boolean affairIsCoverTime;
	private Long affairRemindDate;
	private Long affairDeadlineDate;
	private String affairDeadLineName;//列头-处理期限（节点期限）
	private Date affairReceiveTime;
	private Date affairCompleteTime;//列头-处理时间
	private Date affairCreateDate;
	private Date affairUpdateDate;
	private Date affairExpectedProcessTime;//列头-剩余处理时间
	private Long affairPreApprover;
	/** 办理剩余时间（=处理期限（自然日）-（现在的日-收到待办日-非工作日）） */
	private int[] surplusTime;//列头-办理剩余时间
	
	//回退列表需要展示的数据
	private String backOpinion;
	private Long exchangeDetailId;
	private String exchangeSendUnitName;
	private String exchangeRecUnitName;
	
	//待发送列表需要显示的数据
	private String preUserName;
	private String preNodePolicyName;
	private String preTime;
	private String currentNodePolicyName;
	
	//是否有附件
    private Boolean hasAtt = false;
    
    //是否代理
    private Boolean proxy = false;

    //代理人
    private String  proxyName;
    //被代理人自己处理
    private Boolean agentDeal = false;
    //领导批示编号展示字段
    private String leaderCommondNo;
    //公文来源 chenyq 20180821
    private Integer fromType;
    
     // 是否自动发起
    private Boolean autoRun = false;
	
    /**
     * 公文待办/已办/已发/待发列表/公文查询数据回填
     * @param object
     * @param hasDeduplication 是否包括Clob字段
     */
	public void toObject(Object[] object, boolean hasDeduplication) {
        int n = 0;
        this.setSummaryId((Long)object[n++]);
        this.setSubject((String)object[n++]);
        this.setIdentifier((String)object[n++]);
        this.setGovdocType((Integer)object[n++]);
        this.setSummaryState((Integer)object[n++]);
        this.setIsQuickSend((Boolean)object[n++]);
        this.setTempleteId((Long)object[n++]);
        this.setArchiveId((Long)object[n++]);
        this.setProcessId((String)object[n++]);
        this.setCaseId((Long)object[n++]);
        this.setStartUserId((Long)object[n++]);//11
        this.setStartTime((Timestamp)object[n++]);
        this.setCreateTime((Timestamp)object[n++]);
        this.setStartTime(this.getCreateTime());//兼容文单中有拟稿日期，start_time存的格式为2019-02-19 00:00:00
        this.setCompleteTime((Timestamp)object[n++]);
        this.setSummaryDeadline((Long)object[n++]);
        this.setSummaryDeadlineDatetime((Date)object[n++]);
        this.setSummaryIsCoverTime((Boolean)object[n++]);
        this.setDocMark((String)object[n++]);
        this.setSerialNo((String)object[n++]);
        this.setSignMark((String)object[n++]);
        this.setDocType((String)object[n++]);//21
        this.setSendType((String)object[n++]);
        this.setSecretLevel((String)object[n++]);
        this.setUrgentLevel((String)object[n++]);
        this.setUnitLevel((String)object[n++]);
        this.setKeepPeriod((Integer)object[n++]);
        this.setCopies((Integer)object[n++]);
        this.setIssuer((String)object[n++]);
        this.setSigningDate((Date)object[n++]);
        this.setCreatePerson((String)object[n++]);
        this.setPrinter((String)object[n++]);//31
        this.setPhone((String)object[n++]);
        this.setOrgAccountId((Long)object[n++]);
        this.setExchangeSendAffairId((Long)object[n++]);
        this.setUndertaker((String)object[n++]);
        this.setUndertakenoffice((String)object[n++]);
        String bodyType = (String)object[n++];
        if(Strings.isNotBlank(bodyType)) {
        	this.setContentType(GovdocUtil.getContentType(bodyType));
        }
        this.setHasArchive((Boolean)object[n++]);
        //增加签收时间和登记时间
        this.setSignPerson((String)object[n++]);
        this.setReceiptDate((Date)object[n++]);
        this.setRegistrationDate((Date)object[n++]);
        this.setFromType((Integer)object[n++]);
        Object autoRun = object[n++];
        if(null != autoRun 
        		&& (Integer)autoRun == EdocConstant.NewflowType.child.ordinal()
        		&& (Integer)autoRun == EdocConstant.NewflowType.auto.ordinal()){
            this.setAutoRun(true);
        }
        //公文主表大字段
    	this.setCurrentNodesInfo((String)object[n++]);//41
    	this.setSendTo((String)object[n++]);
    	this.setCopyTo((String)object[n++]);
    	this.setReportTo((String)object[n++]);
    	this.setPrintUnit((String)object[n++]);
    	this.setSendUnit((String)object[n++]);
    	this.setSendDepartment((String)object[n++]);
    	//Affair表字段
        this.setAffairId((Long)object[n++]);   
        this.setAffairIdentifier((String)object[n++]);
        this.setAffairSubject((String)object[n++]);
        this.setAffairApp((Integer)object[n++]);
        Object o = object[n++];
        if(null != o){
        	 this.setAffairSubApp((Integer)o);
        }
        this.setAffairState((Integer)object[n++]);
        this.setAffairSubState((Integer)object[n++]);
        o = object[n++];
        if(null != o){
        	this.setAffairFinish((Boolean)o);
        }
        this.setAffairObjectId((Long)object[n++]);
        this.setAffairSubObjectId((Long)object[n++]);
        this.setAffairActivityId((Long)object[n++]);
        this.setAffairNodePolicy((String)object[n++]);
        this.setAffairMemberId((Long)object[n++]);
        this.setAffairTransactorId((Long)object[n++]);
        this.setAffairHastenTimes((Integer)object[n++]);
        this.setAffairIsCoverTime((Boolean)object[n++]);
        this.setAffairRemindDate((Long)object[n++]);
        this.setAffairDeadlineDate((Long)object[n++]);
        this.setAffairReceiveTime((Date)object[n++]);
        this.setAffairCompleteTime((Date)object[n++]);
        this.setAffairCreateDate((Date)object[n++]);
        this.setAffairUpdateDate((Date)object[n++]);
        this.setAffairExpectedProcessTime((Date)object[n++]);
        //客开 项目名称： [修改功能：解决公文回退后，紧急程度图标丢失！] 作者：fzc 修改日期：2018-5-9 start
        n++;
        if (Strings.isNotEmpty(this.getUrgentLevel())) {
            this.setImportantLevel(Integer.valueOf(this.getUrgentLevel()));//importantLevel
        }else{
            this.setImportantLevel(1);//importantLevel
        }
        //客开 项目名称： [修改功能：解决公文回退后，紧急程度图标丢失！] 作者：fzc 修改日期：2018-5-9 end
        this.setAffairArchiveId((Long)object[n++]);
        this.setAffairTrack((Integer)object[n++]);
        this.setAffairBackFromId((Long)object[n++]);
        this.setAffairPreApprover((Long)object[n++]);
    }
	
	/**
	 * 公文登记簿
	 * @param object
	 */
	public int toRegister(Object[] object) {
        int n = 0;
        this.setSummaryId((Long)object[n++]);
        this.setSubject((String)object[n++]);
        this.setIdentifier((String)object[n++]);
        this.setGovdocType((Integer)object[n++]);
        this.setEdocType((Integer)object[n++]);
        this.setSummaryState((Integer)object[n++]);
        this.setSummaryTransferStatus((Integer)object[n++]);
        this.setSendType((String)object[n++]);
        this.setDocType((String)object[n++]);
        this.setUnitLevel((String)object[n++]);//10
        this.setUrgentLevel((String)object[n++]);
        this.setSecretLevel((String)object[n++]);
        this.setEdocSecretLevel((Integer)object[n++]);
        this.setKeepPeriod((Integer)object[n++]);
        this.setCreateTime((Timestamp)object[n++]);
        this.setCompleteTime((Timestamp)object[n++]);
        this.setPackTime((Timestamp)object[n++]);
        this.setReceiptDate((Date)object[n++]);
        this.setRegistrationDate((Date)object[n++]);
        this.setStartTime((Timestamp)object[n++]);//20
        this.setStartUserId((Long)object[n++]);
        this.setOrgAccountId((Long)object[n++]);
        this.setOrgDepartmentId((Long)object[n++]);
        this.setDocMark((String)object[n++]);
        this.setDocMark2((String)object[n++]);
        this.setSerialNo((String)object[n++]);
        this.setCopies((Integer)object[n++]);
        this.setCopies2((Integer)object[n++]);
        this.setCreatePerson((String)object[n++]);//30
        this.setReview((String)object[n++]);
        this.setIssuer((String)object[n++]);
        this.setSignPerson((String)object[n++]);
        this.setSigningDate((Date)object[n++]);
        String bodyType = (String)object[n++];
        if(Strings.isNotBlank(bodyType)) {
        	this.setContentType(GovdocUtil.getContentType(bodyType));
        }
        this.setUndertaker((String)object[n++]);
        this.setExchangeSendAffairId((Long)object[n++]);
        return n;
	}
	
	/**
	 * 公文登记簿clob字段单独获取(暂无用)
	 * @param object
	 */
	public int toRegisterClob(Object[] object, int n) {
		this.setSendTo((String)object[n++]);
		this.setSendTo2((String)object[n++]);
		this.setSendToId((String)object[n++]);
		this.setSendToId2((String)object[n++]);
		
		this.setCopyTo((String)object[n++]);
		this.setCopyTo2((String)object[n++]);
		this.setCopyToId((String)object[n++]);
		this.setCopyToId2((String)object[n++]);
		
		this.setReportTo((String)object[n++]);
		this.setReportTo2((String)object[n++]);
		this.setReportToId((String)object[n++]);
		this.setReportToId2((String)object[n++]);
		
		this.setSendUnit((String)object[n++]);
		this.setSendUnitId((String)object[n++]);
		this.setSendUnit2((String)object[n++]);
		this.setSendUnitId2((String)object[n++]);
		
		this.setSendDepartment((String)object[n++]);
		this.setSendDepartmentId((String)object[n++]);
		this.setSendDepartment2((String)object[n++]);
		this.setSendDepartmentId2((String)object[n++]);
		
		this.setCurrentNodesInfo((String)object[n++]);
		return n;
	}
	
	public void toExchangeFallback(Object[] object) {
		int i = 0;
		this.setSummaryId((Long) object[i++]);
		this.setDocMark((String) object[i++]);
		this.setCompleteTime((Timestamp)object[i++]);
		this.setSubject((String) object[i++]);
		this.setBackOpinion((String) object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setSendUnit((String) object[i++]);
		this.setExchangeRecUnitName((String) object[i++]);
	}
	
	public void toExchangeSignDone(Object[] object) {
		int i = 0;
		this.setSummaryId((Long) object[i++]);
		this.setIdentifier((String) object[i++]);
		String bodyType = (String) object[i++];
		if (Strings.isNotBlank(bodyType)) {
			this.setContentType(GovdocUtil.getContentType(bodyType));
		}
		this.setAffairId((Long) object[i++]);
		this.setSubject((String) object[i++]);
		this.setDocMark((String) object[i++]);
        this.setSummaryState((Integer)object[i++]);
        this.setAffairApp((Integer)object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setSignPerson((String) object[i++]);
		this.setReceiptDate((Date)object[i++]);
		this.setSendUnit((String) object[i++]);
	}
	
	public void toExObject(Object[] object) {
		int i = 0;
		this.setSummaryId((Long) object[i++]);
		this.setOrgAccountId((Long)object[i++]);
		this.setSendUnit((String) object[i++]);
		this.setSignPerson((String) object[i++]);
		this.setExchangeRecUnitName((String) object[i++]);
		this.setReceiptDate((Date)object[i++]);
	}
	
	public void toOldExObject(Object[] object) {
		int i = 0;
		this.setSummaryId((Long) object[i++]);
		this.setSendUnit((String) object[i++]);
		this.setSignPerson(GovdocOrgHelper.getMemberNameById((Long) object[i++]));
		this.setReceiptDate((Date) object[i++]);
	}

    public String getSummaryTransferStatusName() {
        return summaryTransferStatusName;
    }

    public void setSummaryTransferStatusName(String summaryTransferStatusName) {
        this.summaryTransferStatusName = summaryTransferStatusName;
    }

    public Long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getImportantLevel() {
		return importantLevel;
	}

	public void setImportantLevel(Integer importantLevel) {
		this.importantLevel = importantLevel;
	}

	public Long getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(Long startUserId) {
		this.startUserId = startUserId;
	}

	public String getStartUserName() {
		return startUserName;
	}

	public void setStartUserName(String startUserName) {
		this.startUserName = startUserName;
	}

	public String getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public Date getSigningDate() {
		return signingDate;
	}

	public void setSigningDate(Date signingDate) {
		this.signingDate = signingDate;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Timestamp completeTime) {
		this.completeTime = completeTime;
	}

	public Long getSummaryDeadline() {
		return summaryDeadline;
	}

	public void setSummaryDeadline(Long summaryDeadline) {
		this.summaryDeadline = summaryDeadline;
	}

	public Date getSummaryDeadlineDatetime() {
		return summaryDeadlineDatetime;
	}

	public void setSummaryDeadlineDatetime(Date summaryDeadlineDatetime) {
		this.summaryDeadlineDatetime = summaryDeadlineDatetime;
	}

	public String getSummaryDeadLineName() {
		return summaryDeadLineName;
	}

	public void setSummaryDeadLineName(String summaryDeadLineName) {
		this.summaryDeadLineName = summaryDeadLineName;
	}

	public Boolean getSummaryIsCoverTime() {
		return summaryIsCoverTime;
	}

	public void setSummaryIsCoverTime(Boolean summaryIsCoverTime) {
		this.summaryIsCoverTime = summaryIsCoverTime;
	}

	public Integer getSummaryState() {
		return summaryState;
	}

	public void setSummaryState(Integer summaryState) {
		this.summaryState = summaryState;
	}

	public Integer getSummaryTransferStatus() {
		return summaryTransferStatus;
	}

	public void setSummaryTransferStatus(Integer summaryTransferStatus) {
		this.summaryTransferStatus = summaryTransferStatus;
	}

	public Long getOrgAccountId() {
		return orgAccountId;
	}

	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}

	public Long getOrgDepartmentId() {
		return orgDepartmentId;
	}

	public void setOrgDepartmentId(Long orgDepartmentId) {
		this.orgDepartmentId = orgDepartmentId;
	}

	public Boolean getIsQuickSend() {
		return isQuickSend;
	}

	public void setIsQuickSend(Boolean isQuickSend) {
		this.isQuickSend = isQuickSend;
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

	public Long getTempleteId() {
		return templeteId;
	}

	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
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

	public Integer getGovdocType() {
		return govdocType;
	}

	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
	}

	public Integer getEdocType() {
		return edocType;
	}

	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}

	public String getDocMark() {
		return docMark;
	}

	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}

	public String getDocMark2() {
		return docMark2;
	}

	public void setDocMark2(String docMark2) {
		this.docMark2 = docMark2;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public Integer getHastenTimes() {
		return hastenTimes;
	}

	public void setHastenTimes(Integer hastenTimes) {
		this.hastenTimes = hastenTimes;
	}

	public Long getExchangeSendAffairId() {
		return exchangeSendAffairId;
	}

	public void setExchangeSendAffairId(Long exchangeSendAffairId) {
		this.exchangeSendAffairId = exchangeSendAffairId;
	}

	public String getCurrentNodesInfo() {
		return currentNodesInfo;
	}

	public void setCurrentNodesInfo(String currentNodesInfo) {
		this.currentNodesInfo = currentNodesInfo;
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

	public String getSendTo() {
		return sendTo;
	}

	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}

	public String getSendTo2() {
		return sendTo2;
	}

	public void setSendTo2(String sendTo2) {
		this.sendTo2 = sendTo2;
	}

	public String getSendToId() {
		return sendToId;
	}

	public void setSendToId(String sendToId) {
		this.sendToId = sendToId;
	}

	public String getSendToId2() {
		return sendToId2;
	}

	public void setSendToId2(String sendToId2) {
		this.sendToId2 = sendToId2;
	}

	public String getCopyTo() {
		return copyTo;
	}

	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	public String getCopyTo2() {
		return copyTo2;
	}

	public void setCopyTo2(String copyTo2) {
		this.copyTo2 = copyTo2;
	}

	public String getCopyToId() {
		return copyToId;
	}

	public void setCopyToId(String copyToId) {
		this.copyToId = copyToId;
	}

	public String getCopyToId2() {
		return copyToId2;
	}

	public void setCopyToId2(String copyToId2) {
		this.copyToId2 = copyToId2;
	}

	public String getReportTo() {
		return reportTo;
	}

	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}

	public String getReportTo2() {
		return reportTo2;
	}

	public void setReportTo2(String reportTo2) {
		this.reportTo2 = reportTo2;
	}

	public String getReportToId() {
		return reportToId;
	}

	public void setReportToId(String reportToId) {
		this.reportToId = reportToId;
	}

	public String getReportToId2() {
		return reportToId2;
	}

	public void setReportToId2(String reportToId2) {
		this.reportToId2 = reportToId2;
	}

	public String getSendUnit2() {
		return sendUnit2;
	}

	public void setSendUnit2(String sendUnit2) {
		this.sendUnit2 = sendUnit2;
	}

	public String getSendUnitId() {
		return sendUnitId;
	}

	public void setSendUnitId(String sendUnitId) {
		this.sendUnitId = sendUnitId;
	}

	public String getSendUnitId2() {
		return sendUnitId2;
	}

	public void setSendUnitId2(String sendUnitId2) {
		this.sendUnitId2 = sendUnitId2;
	}

	public String getSendDepartment2() {
		return sendDepartment2;
	}

	public void setSendDepartment2(String sendDepartment2) {
		this.sendDepartment2 = sendDepartment2;
	}

	public String getSendDepartmentId() {
		return sendDepartmentId;
	}

	public void setSendDepartmentId(String sendDepartmentId) {
		this.sendDepartmentId = sendDepartmentId;
	}

	public String getSendDepartmentId2() {
		return sendDepartmentId2;
	}

	public void setSendDepartmentId2(String sendDepartmentId2) {
		this.sendDepartmentId2 = sendDepartmentId2;
	}

	public String getPrintUnit() {
		return printUnit;
	}

	public void setPrintUnit(String printUnit) {
		this.printUnit = printUnit;
	}

	public String getPrinter() {
		return printer;
	}

	public void setPrinter(String printer) {
		this.printer = printer;
	}

	public Integer getCopies() {
		return copies;
	}

	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	public Integer getCopies2() {
		return copies2;
	}

	public void setCopies2(Integer copies2) {
		this.copies2 = copies2;
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

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
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

	public String getReceiveUnit() {
		return receiveUnit;
	}

	public void setReceiveUnit(String receiveUnit) {
		this.receiveUnit = receiveUnit;
	}

	public String getSignPerson() {
		return signPerson;
	}

	public void setSignPerson(String signPerson) {
		this.signPerson = signPerson;
	}

	public String getSignMark() {
		return signMark;
	}

	public void setSignMark(String signMark) {
		this.signMark = signMark;
	}

	public Date getPackTime() {
		return packTime;
	}

	public void setPackTime(Date packTime) {
		this.packTime = packTime;
	}

	public Date getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(Date receiptDate) {
		this.receiptDate = receiptDate;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Integer getEdocSecretLevel() {
		return edocSecretLevel;
	}

	public void setEdocSecretLevel(Integer edocSecretLevel) {
		this.edocSecretLevel = edocSecretLevel;
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

	public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}

	public Integer getKeepPeriod() {
		return keepPeriod;
	}

	public void setKeepPeriod(Integer keepPeriod) {
		this.keepPeriod = keepPeriod;
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

	public Long getAffairId() {
		return affairId;
	}

	public void setAffairId(Long affairId) {
		this.affairId = affairId;
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

	public Integer getAffairTrack() {
		return affairTrack;
	}

	public void setAffairTrack(Integer affairTrack) {
		this.affairTrack = affairTrack;
	}

	public Long getAffairMemberId() {
		return affairMemberId;
	}

	public void setAffairMemberId(Long affairMemberId) {
		this.affairMemberId = affairMemberId;
	}

	public Long getAffairTransactorId() {
		return affairTransactorId;
	}

	public void setAffairTransactorId(Long affairTransactorId) {
		this.affairTransactorId = affairTransactorId;
	}

	public String getAffairNodePolicy() {
		return affairNodePolicy;
	}

	public void setAffairNodePolicy(String affairNodePolicy) {
		this.affairNodePolicy = affairNodePolicy;
	}

	public String getAffairIdentifier() {
		return affairIdentifier;
	}

	public void setAffairIdentifier(String affairIdentifier) {
		this.affairIdentifier = affairIdentifier;
	}

	public Long getAffairObjectId() {
		return affairObjectId;
	}

	public void setAffairObjectId(Long affairObjectId) {
		this.affairObjectId = affairObjectId;
	}

	public Long getAffairSubObjectId() {
		return affairSubObjectId;
	}

	public void setAffairSubObjectId(Long affairSubObjectId) {
		this.affairSubObjectId = affairSubObjectId;
	}

	public Long getAffairActivityId() {
		return affairActivityId;
	}

	public void setAffairActivityId(Long affairActivityId) {
		this.affairActivityId = affairActivityId;
	}

	public Long getAffairArchiveId() {
		return affairArchiveId;
	}

	public void setAffairArchiveId(Long affairArchiveId) {
		this.affairArchiveId = affairArchiveId;
	}

	public String getAffairSubject() {
		return affairSubject;
	}

	public void setAffairSubject(String affairSubject) {
		this.affairSubject = affairSubject;
	}

	public String getAffairForwardMember() {
		return affairForwardMember;
	}

	public void setAffairForwardMember(String affairForwardMember) {
		this.affairForwardMember = affairForwardMember;
	}

	public Long getAffairBackFromId() {
		return affairBackFromId;
	}

	public void setAffairBackFromId(Long affairBackFromId) {
		this.affairBackFromId = affairBackFromId;
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

	public Boolean getAffairFinish() {
		return affairFinish;
	}

	public void setAffairFinish(Boolean affairFinish) {
		this.affairFinish = affairFinish;
	}

	public Integer getAffairHastenTimes() {
		return affairHastenTimes;
	}

	public void setAffairHastenTimes(Integer affairHastenTimes) {
		this.affairHastenTimes = affairHastenTimes;
	}

	public Boolean getAffairIsCoverTime() {
		return affairIsCoverTime;
	}

	public void setAffairIsCoverTime(Boolean affairIsCoverTime) {
		this.affairIsCoverTime = affairIsCoverTime;
	}

	public Long getAffairRemindDate() {
		return affairRemindDate;
	}

	public void setAffairRemindDate(Long affairRemindDate) {
		this.affairRemindDate = affairRemindDate;
	}

	public Long getAffairDeadlineDate() {
		return affairDeadlineDate;
	}

	public void setAffairDeadlineDate(Long affairDeadlineDate) {
		this.affairDeadlineDate = affairDeadlineDate;
	}

	public String getAffairDeadLineName() {
		return affairDeadLineName;
	}

	public void setAffairDeadLineName(String affairDeadLineName) {
		this.affairDeadLineName = affairDeadLineName;
	}

	public Date getAffairReceiveTime() {
		return affairReceiveTime;
	}

	public void setAffairReceiveTime(Date affairReceiveTime) {
		this.affairReceiveTime = affairReceiveTime;
	}

	public Date getAffairCompleteTime() {
		return affairCompleteTime;
	}

	public void setAffairCompleteTime(Date affairCompleteTime) {
		this.affairCompleteTime = affairCompleteTime;
	}

	public Date getAffairCreateDate() {
		return affairCreateDate;
	}

	public void setAffairCreateDate(Date affairCreateDate) {
		this.affairCreateDate = affairCreateDate;
	}

	public Date getAffairUpdateDate() {
		return affairUpdateDate;
	}

	public void setAffairUpdateDate(Date affairUpdateDate) {
		this.affairUpdateDate = affairUpdateDate;
	}

	public Date getAffairExpectedProcessTime() {
		return affairExpectedProcessTime;
	}

	public void setAffairExpectedProcessTime(Date affairExpectedProcessTime) {
		this.affairExpectedProcessTime = affairExpectedProcessTime;
	}

	public int[] getSurplusTime() {
		return surplusTime;
	}

	public void setSurplusTime(int[] surplusTime) {
		this.surplusTime = surplusTime;
	}

	public Boolean getHasAtt() {
		return hasAtt;
	}

	public void setHasAtt(Boolean hasAtt) {
		this.hasAtt = hasAtt;
	}

	public String getHasArchiveTxt() {
		return hasArchiveTxt;
	}

	public void setHasArchiveTxt(String hasArchiveTxt) {
		this.hasArchiveTxt = hasArchiveTxt;
	}

	public String getKeepPeriodTxt() {
		return keepPeriodTxt;
	}

	public void setKeepPeriodTxt(String keepPeriodTxt) {
		this.keepPeriodTxt = keepPeriodTxt;
	}

	public Boolean getProxy() {
		return proxy;
	}

	public void setProxy(Boolean proxy) {
		this.proxy = proxy;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public Boolean getAgentDeal() {
		return agentDeal;
	}

	public void setAgentDeal(Boolean agentDeal) {
		this.agentDeal = agentDeal;
	}

	public Integer getContentType() {
		return contentType;
	}

	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	public String getBackOpinion() {
		return backOpinion;
	}

	public void setBackOpinion(String backOpinion) {
		this.backOpinion = backOpinion;
	}

	public String getExchangeSendUnitName() {
		return exchangeSendUnitName;
	}

	public void setExchangeSendUnitName(String exchangeSendUnitName) {
		this.exchangeSendUnitName = exchangeSendUnitName;
	}

	public String getExchangeRecUnitName() {
		return exchangeRecUnitName;
	}

	public void setExchangeRecUnitName(String exchangeRecUnitName) {
		this.exchangeRecUnitName = exchangeRecUnitName;
	}

	public Long getExchangeDetailId() {
		return exchangeDetailId;
	}

	public void setExchangeDetailId(Long exchangeDetailId) {
		this.exchangeDetailId = exchangeDetailId;
	}

	public String getPreUserName() {
		return preUserName;
	}

	public void setPreUserName(String preUserName) {
		this.preUserName = preUserName;
	}

	public String getPreNodePolicyName() {
		return preNodePolicyName;
	}

	public void setPreNodePolicyName(String preNodePolicyName) {
		this.preNodePolicyName = preNodePolicyName;
	}

	public String getPreTime() {
		return preTime;
	}

	public void setPreTime(String preTime) {
		this.preTime = preTime;
	}

	public String getCurrentNodePolicyName() {
		return currentNodePolicyName;
	}

	public void setCurrentNodePolicyName(String currentNodePolicyName) {
		this.currentNodePolicyName = currentNodePolicyName;
	}

	public String getLeaderCommondNo() {
		return leaderCommondNo;
	}

	public void setLeaderCommondNo(String leaderCommondNo) {
		this.leaderCommondNo = leaderCommondNo;
	}

	public Integer getFromType() {
		return fromType;
	}

	public void setFromType(Integer fromType) {
		this.fromType = fromType;
	}

	public Boolean getAutoRun() {
		return autoRun;
	}

	public void setAutoRun(Boolean autoRun) {
		this.autoRun = autoRun;
	}

	public Long getAffairPreApprover() {
		return affairPreApprover;
	}

	public void setAffairPreApprover(Long affairPreApprover) {
		this.affairPreApprover = affairPreApprover;
	}
	
}
