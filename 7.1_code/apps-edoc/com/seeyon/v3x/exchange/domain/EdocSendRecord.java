package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.common.taglibs.functions.Functions;

public class EdocSendRecord extends BaseModel implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 618559615378721041L;

	/** 公文交换状态：待发送 */
	public final static int Exchange_iStatus_Tosend = 0;
	
	/** 公文交换状态：已发送 */
	public final static int Exchange_iStatus_Sent = 1;
	
	/** 公文交换状态（发文记录）：已回退  */
	public final static int Exchange_iStatus_Send_StepBacked = 2;
	
	/** 公文交换状态（发文记录）：退回后生成新数据  */
	public final static int Exchange_iStatus_Send_New_StepBacked = 3;
	
	/** 公文交换状态（发文记录）：撤销后生成新数据  */
	public final static int Exchange_iStatus_Send_New_Cancel = 4;
	
	/** 公文交换状态：已发送-被删除 */
	public final static int Exchange_iStatus_Send_Delete = 5;
	
	/** 公文交换状态：待发送-被删除 */
	public final static int Exchange_iStatus_ToSend_Delete = 6;
	
	/** 公文交换单位类型：部门交换  exchangeType */
	public final static int Exchange_Send_iExchangeType_Dept = 0;
	
	/** 公文交换类型：内部单位交换 */
	public final static int Exchange_Send_iExchangeType_Org = 1;
	
	/** 公文交换类型：外部单位交换 */
	public final static int Exchange_Send_iExchangeType_ExternalOrg = 2;
	
	/** 公文交换执行类型：0竞争执行 1指定执行 */
	public final static int Exchange_Assign_To_All = 0;
	
	/** 公文交换执行类型： 1指定执行 */
	public final static int Exchange_Assign_To_Member = 1;
	
	/** 公文交换原发文类型： 0是补发，撤销，退回的新生成数据 */
	public final static int Exchange_Base_NO = 0;
	
	/** 公文交换原发文类型：1第一次发送生成的交换数据 */
	public final static int Exchange_Base_YES = 1;	
	
	private String subject; // 公文标题
	private String docType; // 公文种类
	private String docMark; // 公文文号	
	private String secretLevel; // 公文密级
	private String urgentLevel; // 紧急程度 
	private String sendUnit; // 发文单位
	private String issuer; // 签发人
	private Date issueDate; // 公文发起时间
	private Integer copies; // 印发份数
	private long edocId; // 公文ID	
	private long sendUserId; // 发文人  
	private Integer assignType;//交换执行类型 0竞争执行 1指定人执行 
	private Integer isBase; //是否公文交换原发文 1是公文交换原发文   0非公文交换原发文，如补发,撤销或退回数据
	private Timestamp sendTime; // 交换签发日期	
	private long exchangeOrgId; // 交换单位ID|交换部门ID
	private Long exchangeAccountId = null;//交换单位，部门交换时部门所在单位，单位交换时和exchangeOrgId相同
	private int exchangeType; // 交换类型：单位交换|部门交换
	private Integer exchangeMode =0;//交换方式：区分往外发送的方式（内部公文交换又叫致远交换、书生公文交换）
	private Timestamp createTime; // 创建时间
	private int status; // 状态[待发送|已发送]
	private Set<EdocSendDetail> sendDetails;
	private String sendedTypeIds;

	private String stepBackInfo;// 回退说明
	private Integer isTurnRec;//是否为转收文类型(0 不是 ，1 是)
	
	//送往单位
	private String sendedNames;
	
	public String getSendedNames() {
		return sendedNames;
	}

	public void setSendedNames(String sendedNames) {
		this.sendedNames = sendedNames;
	}

	public Integer getIsTurnRec() {
		return isTurnRec;
	}

	public void setIsTurnRec(Integer isTurnRec) {
		this.isTurnRec = isTurnRec;
	}

	public String getStepBackInfo() {
		return stepBackInfo;
	}

	public void setStepBackInfo(String stepBackInfo) {
		this.stepBackInfo = stepBackInfo;
	}

	// 用于页面排序需求
	private List<EdocSendDetail> sendDetailList;
	
	private Integer contentNo;
	
	public Integer getIsBase() {
		return isBase;
	}

	public void setIsBase(Integer isBase) {
		this.isBase = isBase;
	}

	public Integer getContentNo()
	{
		return this.contentNo;
	}
	public void setContentNo(Integer contentNo)
	{
		this.contentNo=contentNo;
	}
	/**
	 * 此函数是为了兼容历史数据，以前没用sendedTypeIds字段，后增加的；
	 * sendedTypeIds有值的时候优先使用
	 * @return
	 */	
	public String getSendEntityNames()
	{
	    if(Strings.isNotBlank(sendedNames)){
	        return sendedNames;
	    }
	    
		if(sendedTypeIds!=null && !"".equals(sendedTypeIds))
		{
			return Functions.showOrgEntities(sendedTypeIds,"、");
		}
		StringBuilder str=new StringBuilder("");
		for(EdocSendDetail ed:sendDetails)
		{
			if(str.length() > 0){
			    str.append(V3xOrgEntity.ORG_ID_DELIMITER);
			}
			str.append(ed.getRecOrgName());
		}
		return str.toString();
	}
	
	// 不用持久化，页面显示  发送到单位的名字
	private String sendNames;
	private String sendUserNames;
	private String keywords;
	
	// 公文交换的单位名字显示，即exchangeOrgId对应的名字
	private String exchangeOrgName;
	
	public String getExchangeOrgName() {
		return exchangeOrgName;
	}

	public void setExchangeOrgName(String exchangeOrgName) {
		this.exchangeOrgName = exchangeOrgName;
	}

	public String getSendUserNames() {
		return sendUserNames;
	}

	public void setSendUserNames(String sendUserNames) {
		this.sendUserNames = sendUserNames;
	}

	public String getSendNames() {
		return sendNames;
	}

	public void setSendNames(String sendNames) {
		this.sendNames = sendNames;
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
	
	public Integer getCopies() {
		return copies;
	}
	
	public void setCopies(Integer copies) {
		this.copies = copies;
	}
	
	public long getEdocId() {
		return edocId;
	}
	
	public void setEdocId(long edocId) {
		this.edocId = edocId;
	}
	 
	public long getSendUserId() {
		return sendUserId;
	}
	
	public void setSendUserId(long sendUserId) {
		this.sendUserId = sendUserId;
	}
	
	public Timestamp getSendTime() {
		return sendTime;
	}
	
	public void setSendTime(Timestamp sendTime) {
		this.sendTime = sendTime;
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
	
	public Set<EdocSendDetail> getSendDetails() {		
		return sendDetails;
	}
	
	public void setSendDetails(Set<EdocSendDetail> sendDetails) {
		this.sendDetails = sendDetails;
	}

	public List<EdocSendDetail> getSendDetailList() {
		return sendDetailList;
	}

	public void setSendDetailList(List<EdocSendDetail> sendDetailList) {
		this.sendDetailList = sendDetailList;
	}
	public String getSendedTypeIds() {
		return sendedTypeIds;
	}
	public void setSendedTypeIds(String sendedTypeIds) {
		this.sendedTypeIds = sendedTypeIds;
	}
	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Integer getAssignType() {
		return assignType;
	}

	public void setAssignType(Integer assignType) {
		this.assignType = assignType;
	}

	public Integer getExchangeMode() {
		return exchangeMode;
	}

	public void setExchangeMode(Integer exchangeMode) {
		this.exchangeMode = exchangeMode;
	}

    public Long getExchangeAccountId() {
        return exchangeAccountId;
    }

    public void setExchangeAccountId(Long exchangeAccountId) {
        this.exchangeAccountId = exchangeAccountId;
    }

	
}
