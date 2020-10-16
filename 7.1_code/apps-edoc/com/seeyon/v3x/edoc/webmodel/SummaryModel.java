/**
 * Author : xuqw
 *   Date : 2014年12月6日 下午5:43:13
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 
 * 注意：这个类的耦合有点高，包括收发文登记薄JSP页面展示的列，及edoc_register_condition表保存的收发文登记薄查询条件
 * 
 * 
 * <p>Title       : EdocSummary相关的VO</p>
 * <p>Description : EdocSummary相关的VO， 原有的EdocSummaryModel对象太复杂，重新编写</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class SummaryModel {

    private String identifier;
    protected static final int INENTIFIER_SIZE = 20;
    
    private Boolean hasArchive = false;
    private Long archiveId;
    
  //自然日计算时间
    private Long overTime;
    private Long runTime;
    //工作日计算时间
    private Long overWorkTime;
    private Long runWorkTime;
    
    private Integer importantLevel; //重要程度
    private Boolean isunit = false;
    private int canTrack;
    private Long caseId;
    private String comment;
    private java.sql.Timestamp completeTime;
    private Integer copies;
    private Integer copies2;//copies2
    private String copyTo;
    private String copyToId;
    private String copyTo2;//copy_to2
    private String copyToId2;
    //取公文单上的数据，如果公文单上的数据为空，则自动取发起节点为创建人.
    private String createPerson;//create_person
    //startTime是可以用户录入的，createTime是系统自动生成的
    private java.sql.Timestamp createTime;//createdate
    private java.sql.Timestamp packTime;//packdate
    private Long deadline=-1L;
    private java.util.Date deadlineDatetime;
    private String docMark;//doc_mark
    private String docMark2;//doc_mark2
    private String docType;//doc_type
    private int edocType;
    private Long formId;
    private String issuer;
    private Integer keepPeriod;//keep_period
    private String keywords;  //keyword
    private String printUnit;//print_unit
    private String printUnitId;
    private String printer;
    private String processId;
    private String reportTo;//report_to
    private String reportToId;
    private String reportTo2;
    private String reportToId2;
    private String secretLevel;//secret_level
    private String sendTo;//send_to
    private String sendToId;
    private String sendTo2;//send_to2
    private String sendToId2;
    private String sendType;
    private String sendUnit;
    private String sendUnit2;//send_unit2
    private String sendUnitId;
    private String sendUnitId2;
    private String sendDepartment;
    private String sendDepartment2;
    private String sendDepartmentId;
    private String sendDepartmentId2;
    private String attachments;
    private String serialNo;
    private java.sql.Date signingDate;
    private java.sql.Timestamp startTime;
    private Long startUserId;
    private int state = EdocConstant.flowState.run.ordinal();
    private String subject;
    private String urgentLevel;//urgent_level
    
    /**
     * wangwei   start
     */
    private String filesm; //附件说明
    private String filefz;//附注
    private String phone; //联系电话
    //private String cperson;//会签人
    private String party;//党务机关
    private String administrative;//政务机关
    private Long subEdocType;
    private Long processType;//lijl添加,处理类型(1办文,2阅文),edoc_type的子类型
    private String edocTypeEnum;//lijl添加,将edocType转化成枚举值recEdoc
    
    private java.sql.Date receiptDate;//签收日期
    private java.sql.Date registrationDate;//登记日期
    private String auditor;//审核人
    private String review;//复核人
    private String undertaker;//承办人
    private String undertakerDep; //承办人部门
    private String undertakerAccount;//承办人单位
    
    private Boolean isQuickSend = false ; //是否为快速发文，默认为否
    private String from;
    /**
     * 流程是否超期
     */
    private java.lang.Boolean coverTime = false;
    /**
     * wangwei  end
     */
    private java.util.Set<EdocBody> edocBodies;
    private java.util.Set<EdocOpinion> edocOpinions;
    
    private Boolean worklfowTimeout = false;//流程超期，不持久化到数据库
    
    private java.sql.Timestamp updateTime;

    private Long templeteId;    
    private String workflowRule;
    private Long advanceRemind = 0L;
    /*非直接数据库映射字段,保存时候放到一起保存 edocOpinions里面根据类型判断是发起附言还是处理附言*/
    private EdocOpinion senderOpinion;
    /////////////////////////非数据库字段数据 开始///////////////////////////
    /**
     * 该属性只是作为前端显示用�
     */
    private V3xOrgMember startMember;
    
    private Long orgAccountId;//单位Id
    
    private Long orgDepartmentId;//部门Id
    
    private Long affairId;//在收文中查看关联的发文时，记录发文对应的affairId
    
    private String currentNodesInfo; //当前待办人
    private boolean finished=false;
    
    private String departmentName;//发文部门
    private String sender;      //分发人(发文)
    private java.sql.Timestamp recieveDate;//来文日期(签收时间)
    private String signer;  //增加 会签人  (收文登记簿需要)
    private String registerUserName;    //登记人
    private java.sql.Date registerDate; //登记时间
    private String distributer; //分发人
    
    /** 来文类型 1内部单位 2外部单位 */
    private Integer sendUnitType;
    
    private Long id = null;
    private Integer affairState = null;
    private boolean hasAttsFlag = false;//是否有附件标识
    private String undertakenoffice = null;//承办机构
    private java.sql.Timestamp recTime = null;//签收时间
    private String recieveUserName = null;//签收人
	private String unitLevel;// 页面显示公文级别
	private java.sql.Timestamp sendTime; // 送文时间          
	private String exchangeMode;
	

	/**
     * 将EdocSummaryModel对象转换成当前对象
     * @Author      : xuqiangwei
     * @Date        : 2014年12月9日上午1:55:31
     * @param edocSummaryModel
     */
    public void trans2SummaryModel(EdocSummaryModel edocSummaryModel){

        //防护操作
        if(edocSummaryModel == null){
            return;
        }
        
        if(edocSummaryModel.getSummary() == null){
            edocSummaryModel.setSummary(new EdocSummary());
        }
        
        EdocSummary summary = edocSummaryModel.getSummary();
        
        this.setSecretLevel(summary.getSecretLevel());
        this.setKeepPeriod(summary.getKeepPeriod());
        this.setUrgentLevel(summary.getUrgentLevel());
        
        this.setDocMark(summary.getDocMark());
        this.setSubject(summary.getSubject());
        this.setSigningDate(summary.getSigningDate());
        this.setCreatePerson(summary.getCreatePerson());
        this.setCopies(summary.getCopies());
        this.setSendUnit(summary.getSendUnit());
        this.setSender(edocSummaryModel.getSender());
        this.setSendTo(summary.getSendTo());
        this.setCopyTo(summary.getCopyTo());
        this.setIssuer(summary.getIssuer());
        this.setCreateTime(summary.getCreateTime());
        this.setDepartmentName(edocSummaryModel.getDepartmentName());
        this.setSerialNo(summary.getSerialNo());
        this.setReview(summary.getReview());
        this.setRecieveDate(edocSummaryModel.getRecieveDate());
        this.setSigner(edocSummaryModel.getSigner());
        this.setKeywords(summary.getKeywords());
        this.setRegisterUserName(edocSummaryModel.getRegisterUserName());
        this.setRegisterDate(edocSummaryModel.getRegisterDate());
        this.setDistributer(edocSummaryModel.getDistributer());
        this.setUndertaker(summary.getUndertaker());
        this.setUndertakerDep(summary.getUndertakerDep());
        this.setUndertakerAccount(summary.getUndertakerAccount());
        this.setSendUnitType(edocSummaryModel.getSendUnitType());
        this.setAffairId(edocSummaryModel.getAffairId());
        this.setEdocType(summary.getEdocType());
        this.setId(summary.getId());
        this.setAffairState(edocSummaryModel.getState());
        this.setHasAttsFlag(summary.isHasAttachments());
        this.setUndertakenoffice(summary.getUndertakenoffice());
        this.setRegistrationDate(summary.getRegistrationDate());
        this.setRecTime(edocSummaryModel.getRecTime());
        this.setRecieveUserName(edocSummaryModel.getRecieveUserName());
        this.setStartTime(summary.getStartTime());
        // 发文页面显示公文级别
        this.setUnitLevel(summary.getUnitLevel());
        // 发文页面显示送文时间
        this.setSendTime(summary.getSendTime());
        // 发文显示交换方式
        this.setExchangeMode(edocSummaryModel.getExchangeMode());
    }
    
    public void setStartTime(java.sql.Timestamp startTime) {
        this.startTime = startTime;
    }
    
    public java.sql.Timestamp getStartTime() {
        return startTime;
    }
    
    public void setRecieveUserName(String recieveUserName) {
        this.recieveUserName = recieveUserName;
    }
    
    public String getRecieveUserName() {
        return recieveUserName;
    }
    
    public void setRecTime(java.sql.Timestamp recTime) {
        this.recTime = recTime;
    }
    
    public java.sql.Timestamp getRecTime() {
        return recTime;
    }
    
    public void setRegistrationDate(java.sql.Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public java.sql.Date getRegistrationDate() {
        return registrationDate;
    }
    
    public void setUndertakenoffice(String undertakenoffice) {
        this.undertakenoffice = undertakenoffice;
    }
    
    public String getUndertakenoffice() {
        return undertakenoffice;
    }
    
    public void setHasAttsFlag(boolean hasAttsFlag) {
        this.hasAttsFlag = hasAttsFlag;
    }
    
    public boolean isHasAttsFlag() {
        return hasAttsFlag;
    }
    
    public void setAffairState(Integer affairState) {
        this.affairState = affairState;
    }
    
    public Integer getAffairState() {
        return affairState;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setEdocType(int edocType) {
        this.edocType = edocType;
    }
    
    public int getEdocType() {
        return edocType;
    }
    
    public void setAffairId(Long affairId) {
        this.affairId = affairId;
    }
    
    public Long getAffairId() {
        return affairId;
    }
    
    public void setSendUnitType(Integer sendUnitType) {
        this.sendUnitType = sendUnitType;
    }
    
    public Integer getSendUnitType() {
        return sendUnitType;
    }
    
    public void setUndertakerAccount(String undertakerAccount) {
        this.undertakerAccount = undertakerAccount;
    }
    
    public String getUndertakerAccount() {
        return undertakerAccount;
    }
    
    public void setUndertakerDep(String undertakerDep) {
        this.undertakerDep = undertakerDep;
    }
    
    public String getUndertakerDep() {
        return undertakerDep;
    }
    
    public void setUndertaker(String undertaker) {
        this.undertaker = undertaker;
    }
    
    public String getUndertaker() {
        return undertaker;
    }
    
    public void setDistributer(String distributer) {
        this.distributer = distributer;
    }
    
    public String getDistributer() {
        return distributer;
    }
    
    public void setRegisterDate(java.sql.Date registerDate) {
        this.registerDate = registerDate;
    }
    
    public java.sql.Date getRegisterDate() {
        return registerDate;
    }
    
    public void setRegisterUserName(String registerUserName) {
        this.registerUserName = registerUserName;
    }
    
    public String getRegisterUserName() {
        return registerUserName;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setSigner(String signer) {
        this.signer = signer;
    }
    
    public String getSigner() {
        return signer;
    }
    
    public java.sql.Timestamp getRecieveDate() {
        return recieveDate;
    }
    
    public void setRecieveDate(java.sql.Timestamp recieveDate) {
        this.recieveDate = recieveDate;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
    
    public String getSerialNo() {
        return serialNo;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setCreateTime(java.sql.Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public java.sql.Timestamp getCreateTime() {
        return createTime;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }
    
    public String getCopyTo() {
        return copyTo;
    }
    
    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }
    
    public String getSendTo() {
        return sendTo;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSendUnit(String sendUnit) {
        this.sendUnit = sendUnit;
    }
    
    public String getSendUnit() {
        return sendUnit;
    }
    
    public void setCopies(Integer copies) {
        this.copies = copies;
    }
    
    public Integer getCopies() {
        return copies;
    }
    
    public void setUrgentLevel(String urgentLevel) {
        this.urgentLevel = urgentLevel;
    }
    
    public String getUrgentLevel() {
        return urgentLevel;
    }
    
    public void setKeepPeriod(Integer keepPeriod) {
        this.keepPeriod = keepPeriod;
    }
    
    public Integer getKeepPeriod() {
        return keepPeriod;
    }
    
    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }
    
    public String getSecretLevel() {
        return secretLevel;
    }
    
    public void setDocMark(String docMark) {
        this.docMark = docMark;
    }
    
    public String getDocMark() {
        return docMark;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSigningDate(java.sql.Date signingDate) {
        this.signingDate = signingDate;
    }
    
    public java.sql.Date getSigningDate() {
        return signingDate;
    }
    
    public void setCreatePerson(String createPerson) {
        this.createPerson = createPerson;
    }
    
    public String getCreatePerson() {
        return createPerson;
    }
    public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}
	public java.sql.Timestamp getSendTime() {
		return sendTime;
	}

	public void setSendTime(java.sql.Timestamp sendTime) {
		this.sendTime = sendTime;
	}

	public String getExchangeMode() {
		return exchangeMode;
	}

	public void setExchangeMode(String exchangeMode) {
		this.exchangeMode = exchangeMode;
	}
	
}
