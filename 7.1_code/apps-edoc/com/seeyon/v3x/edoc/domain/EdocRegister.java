package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.ctp.util.IdentifierUtil;

/**
 * 公文登记对象
 * @author 唐桂林 2011.09.27
 */
public class EdocRegister extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2987994269664372485L;
	public static final int AUTO_REGISTER = 1;
    /**
     * 
     */
    
    public static final int REGISTER_TYPE_BY_PAPER_REC_EDOC = 100; //新建纸质收文时，登记表中也插入数据并设置该状态，不在登记表中显示
    
    

	/**
     * 标志位, 共100位，采用枚举的自然顺序
     */
    protected static enum INENTIFIER_INDEX{
        HAS_ATTACHMENTS, // 是否有附件
    };

    /** 标志位 */
    private String identifier;

    /** 签收单ID */
    private Long recieveId = 0L;

    /** 来文公文ID */
    private Long edocId = 0L;

    /** 登记单类型 0发文登记 1收文登记 */
    private Integer edocType = 0;

    /** 登记方式 1电子公文登记 2纸质公文登记 3二维码公文登记 */
    private Integer registerType = 0;

    /** 创建人Id */
    private Long createUserId = 0L;

    /** 创建人名字 */
    private String createUserName;

    /** 创建时间 */
    private java.sql.Timestamp createTime;

    /** 修改时间 */
    private java.sql.Timestamp updateTime;

    /** 来文单位 */
    private String sendUnit;

    /** 来文单位id */
    private Long sendUnitId = 0L;

    /** 来文类型 1内部单位 2外部单位 */
    private Integer sendUnitType = 0;

    /** 成文单位 */
    private String edocUnit;

    /** 成文单位id */
    private String edocUnitId;

    /** 成文日期 发文最后一个签发节点的处理日期,如果没有签发节点,用封发日期 */
    private java.sql.Date edocDate;

    /** 登记人id */
    private Long registerUserId = 0L;

    /** 登记人 */
    private String registerUserName;

    /** 登记日期 */
    private java.sql.Date registerDate;

    /** 签发人id */
    private Long issuerId = 0L;

    /** 签发人 */
    private String issuer;

    /** 签发日期 */
    private java.sql.Date issueDate;

    /** 会签人 */
    private String signer;

    /** 分发人id */
    private Long distributerId = -1L;
    

	 /** 是否代理 */
    private boolean proxy;
    
    private Long proxyId;

    /** 代理人 */
    private String proxyName;
    
    private String proxyLabel;

    /** 代理人 Id*/
    private Long proxyUserId;
 
	/** 分发人 */
    private String distributer;

    /** 分发时间 */
    private java.sql.Date distributeDate;

    /** 分发状态 0草稿箱 1待分发 2已分发 */
    private Integer distributeState = 0;
    
    /** 分发关联公文id */
    private Long distributeEdocId = 0L;

	/** 标题 */
    private String subject;

    /** 公文类型-来自系统枚举值 */
    private String docType;

    /** 行文类型- 来自系统枚举值 */
    private String sendType;

    /** 来文字号 */
    private String docMark;

    /** 收文编号 */
    private String serialNo;

    /** 文件密级 */
    private String secretLevel;

    /** 紧急程度 */
    private String urgentLevel;

    /** 保密期限 */
    private String keepPeriod;

    /** 主送单位 */
    private String sendTo;

    /** 主送单位id */
    private String sendToId;

    /** 抄送单位 */
    private String copyTo;

    /** 主送单位id */
    private String copyToId;

    /** 主题词 */
    private String keywords;

    /** 印发份数 */
    private Integer copies;

   

	/** 附注 */
    private String noteAppend;

    /** 附件说明 */
    private String attNote;

    /** 登记状态 0草稿箱 1待登记 2已登记 3退回给签收 4被退回 5删除 */
    private Integer state = 0;

    /** 登记单位 */
    private Long orgAccountId = 0L;

    /** 签收时间  */
    private java.sql.Timestamp recTime;
    
    /** 交换机关类型 0部门 1单位(非数据库字段) */
    private int exchangeType;

    /** 交换机关id (非数据库字段) */
    private long exchangeOrgId;
    
    /** 是否有附件 */
    private boolean hasAttachments;
    
    /** 公文级别  */
    private String unitLevel;
    
    /** 送文日期  */
    private java.sql.Timestamp exchangeSendTime;
    
    /** 交换方式：区分往外发送的方式（内部公文交换又叫致远交换、书生公文交换）  */
	private Integer exchangeMode =0;

	/** 附件 */
    private List<Attachment> attachmentList = new ArrayList<Attachment>();

    /** 正文 */
    private RegisterBody registerBody = null;
    
    private Integer isRetreat = 0;//是否被退回
    
    private Integer autoRegister;//是否自动登记(V5-G6版本使用，但A8中也有这个字段)
    
    private Long recieveUserId;	//签收人id
    private String recieveUserName; //签收人名称
    
	public Long getRecieveUserId() {
		return recieveUserId;
	}

	public void setRecieveUserId(Long recieveUserId) {
		this.recieveUserId = recieveUserId;
	}

	public String getRecieveUserName() {
		return recieveUserName;
	}

	public void setRecieveUserName(String recieveUserName) {
		this.recieveUserName = recieveUserName;
	}

	public Integer getAutoRegister() {
		return autoRegister;
	}

	public void setAutoRegister(Integer autoRegister) {
		this.autoRegister = autoRegister;
	}

	public String getProxyLabel() {
		return proxyLabel;
	}

	public void setProxyLabel(String proxyLabel) {
		this.proxyLabel = proxyLabel;
	}

	public Integer getIsRetreat() {
		return isRetreat;
	}

	public void setIsRetreat(Integer isRetreat) {
		this.isRetreat = isRetreat;
	}

	public Long getProxyId() {
		return proxyId;
	}

	public void setProxyId(Long proxyId) {
		this.proxyId = proxyId;
	}

	//解析Long值
	private Long parseLongVal(HttpServletRequest request, String code, Long defualtValue){
	    
	    Long ret = defualtValue;
	    
	    String rValue = request.getParameter(code);
	    if(Strings.isNotBlank(rValue)){
	        ret = Long.parseLong(rValue);
	    }
	    
	    return ret;
	}
	
	//解析Integer值
    private Integer parseIntegerVal(HttpServletRequest request, String code, Integer defualtValue){
        
        Integer ret = defualtValue;
        
        String rValue = request.getParameter(code);
        if(Strings.isNotBlank(rValue)){
            ret = Integer.parseInt(rValue);
        }
        
        return ret;
    }
    
    //解析String 类型
    private String parseStringVal(HttpServletRequest request, String code, String defualtValue){
        
        String ret = defualtValue;
        
        String rValue = request.getParameter(code);
        if(Strings.isNotBlank(rValue)){
            ret = rValue;
        }
        
        return ret;
    }
	
	public void bind(HttpServletRequest request) {
        this.setId(parseLongVal(request, "id", -1L));
        this.setIdentifier(parseStringVal(request, "identifier", "00000000000000000000"));
        this.setEdocId(parseLongVal(request, "edocId", -1L));
        this.setEdocType(parseIntegerVal(request, "edocType", 1));
        this.setRecieveId(parseLongVal(request, "recieveId", -1L));
        this.setRegisterType(parseIntegerVal(request, "registerType", EdocNavigationEnum.RegisterType.ByAutomatic.ordinal()));
        this.setCreateUserId(parseLongVal(request, "createUserId", -1L));
        this.setCreateUserName(parseStringVal(request, "createUserName", ""));
        this.setCreateTime(request.getParameter("createTime") == null ? new java.sql.Timestamp(new java.util.Date().getTime()) : Timestamp.valueOf(request.getParameter("createTime")));
        this.setUpdateTime(request.getParameter("updateTime") == null ? null : Timestamp.valueOf(request.getParameter("updateTime")));
        this.setSendUnit(parseStringVal(request, "sendUnit", ""));
        this.setSendUnitId(parseLongVal(request, "sendUnitId", -1L));
    	this.setSendUnitType(parseIntegerVal(request, "sendUnitType", this.getSendUnitType()));
        this.setEdocUnit(parseStringVal(request, "edocUnit", ""));
        this.setEdocUnitId(parseStringVal(request, "edocUnitId", ""));        
        this.setRegisterUserId(parseLongVal(request, "registerUserId", -1L));
        this.setRegisterUserName(parseStringVal(request, "registerUserName", ""));
        this.setIssuerId(parseLongVal(request, "issuerId", -1L));
        this.setIssuer(parseStringVal(request, "issuer", ""));
        java.sql.Date date = null;
        if(Strings.isNotBlank(request.getParameter("edocDate"))) {
            date = new java.sql.Date(Datetimes.parseDatetime(request.getParameter("edocDate")+" 00:00:00").getTime());
        }
        this.setEdocDate(date);
        date = null;
        if(Strings.isNotBlank(request.getParameter("registerDate"))) {
            date = new java.sql.Date(Datetimes.parseDatetime(request.getParameter("registerDate")+" 00:00:00").getTime());
        }
        this.setRegisterDate(date);
        date = null;
        if(Strings.isNotBlank(request.getParameter("issueDate"))) {
            date = new java.sql.Date(Datetimes.parseDatetime(request.getParameter("issueDate")+" 00:00:00").getTime());
        }
        this.setIssueDate(date);
        date = null;
        if(Strings.isNotBlank(request.getParameter("distributeDate"))) {
            date = new java.sql.Date(Datetimes.parseDatetime(request.getParameter("distributeDate")+" 00:00:00").getTime());
        }
        this.setDistributeDate(date);
        this.setRecTime(Strings.isBlank(request.getParameter("recTime"))  ? null : Timestamp.valueOf(request.getParameter("recTime")));
        this.setSigner(parseStringVal(request, "signer", ""));
        this.setDistributerId(parseLongVal(request, "distributerId", -1L));
        this.setDistributer(parseStringVal(request, "distributer", ""));
        this.setDistributeState(parseIntegerVal(request, "distributeState", 0));
        this.setDistributeEdocId(parseLongVal(request, "distributeEdocId", -1L));
        this.setSubject(parseStringVal(request, "subject", ""));
        this.setDocType(parseStringVal(request, "docType", this.getDocType()));
        this.setSendType(parseStringVal(request, "sendType", this.getSendType()));
        this.setDocMark(parseStringVal(request, "docMark", ""));
        this.setSerialNo(parseStringVal(request, "serialNo", ""));
        this.setSecretLevel(parseStringVal(request, "secretLevel", this.getSecretLevel()));
        this.setUrgentLevel(parseStringVal(request, "urgentLevel", this.getUrgentLevel()));
    	this.setKeepPeriod(parseStringVal(request, "keepPeriod", this.getKeepPeriod()));
        this.setUnitLevel(parseStringVal(request, "unitLevel", null));
        this.setSendTo(parseStringVal(request, "sendTo", ""));
        this.setSendToId(parseStringVal(request, "sendToId", ""));
        this.setCopyTo(parseStringVal(request, "copyTo", ""));
        this.setCopyToId(parseStringVal(request, "copyToId", ""));
        this.setKeywords(parseStringVal(request, "keywords", ""));
        this.setCopies(parseIntegerVal(request, "registerCopies", null));
        this.setNoteAppend(parseStringVal(request, "noteAppend", ""));
        this.setAttNote(parseStringVal(request, "attNote", ""));
        this.setState(parseIntegerVal(request, "state", 1));
        this.setOrgAccountId(parseLongVal(request, "orgAccountId", -1L));
    }

    /**
     * @return distributeEdocId
     */
    public Long getDistributeEdocId() {
		return distributeEdocId;
	}

	/**
	 * @param distributeEdocId
	 */
	public void setDistributeEdocId(Long distributeEdocId) {
		this.distributeEdocId = distributeEdocId;
	}
    
    /**
     * @return the recTime
     */
    public java.sql.Timestamp getRecTime() {
        return recTime;
    }

    /**
     * @param recTime the recTime to set
     */
    public void setRecTime(java.sql.Timestamp recTime) {
        this.recTime = recTime;
    }

    /**
     * @return the exchangeType
     */
    public int getExchangeType() {
        return exchangeType;
    }

    /**
     * @param exchangeType the exchangeType to set
     */
    public void setExchangeType(int exchangeType) {
        this.exchangeType = exchangeType;
    }

    /**
     * @return the exchangeOrgId
     */
    public long getExchangeOrgId() {
        return exchangeOrgId;
    }

	  public Long getProxyUserId() {
			return proxyUserId;
		}

		public void setProxyUserId(Long proxyUserId) {
			this.proxyUserId = proxyUserId;
		}

    /**
     * @param exchangeOrgId the exchangeOrgId to set
     */
    public void setExchangeOrgId(long exchangeOrgId) {
        this.exchangeOrgId = exchangeOrgId;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the recieveId
     */
    public Long getRecieveId() {
        return recieveId;
    }

    /**
     * @param recieveId the recieveId to set
     */
    public void setRecieveId(Long recieveId) {
        this.recieveId = recieveId;
    }

    /**
     * @return the edocId
     */
    public Long getEdocId() {
        return edocId;
    }

    /**
     * @param edocId the edocId to set
     */
    public void setEdocId(Long edocId) {
        this.edocId = edocId;
    }

    /**
     * @return the edocType
     */
    public Integer getEdocType() {
        return edocType;
    }

    /**
     * @param edocType the edocType to set
     */
    public void setEdocType(Integer edocType) {
        this.edocType = edocType;
    }

    /**
     * @return the registerType
     */
    public Integer getRegisterType() {
        return registerType;
    }

    /**
     * @param registerType the registerType to set
     */
    public void setRegisterType(Integer registerType) {
        this.registerType = registerType;
    }

    /**
     * @return the createUserId
     */
    public Long getCreateUserId() {
        return createUserId;
    }

    /**
     * @param createUserId the createUserId to set
     */
    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    /**
     * @return the createUserName
     */
    public String getCreateUserName() {
        return createUserName;
    }

    /**
     * @param createUserName the createUserName to set
     */
    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    /**
     * @return the createTime
     */
    public java.sql.Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(java.sql.Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the updateTime
     */
    public java.sql.Timestamp getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(java.sql.Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return the sendUnit
     */
    public String getSendUnit() {
        return sendUnit;
    }

    /**
     * @param sendUnit the sendUnit to set
     */
    public void setSendUnit(String sendUnit) {
        this.sendUnit = sendUnit;
    }

    /**
     * @return the sendUnitId
     */
    public Long getSendUnitId() {
        return sendUnitId;
    }

    /**
     * @param sendUnitId the sendUnitId to set
     */
    public void setSendUnitId(Long sendUnitId) {
        this.sendUnitId = sendUnitId;
    }

    /**
     * @return the sendUnitType
     */
    public Integer getSendUnitType() {
        return sendUnitType;
    }

    /**
     * @param sendUnitType the sendUnitType to set
     */
    public void setSendUnitType(Integer sendUnitType) {
        this.sendUnitType = sendUnitType;
    }

    /**
     * @return the edocUnit
     */
    public String getEdocUnit() {
        return edocUnit;
    }

    /**
     * @param edocUnit the edocUnit to set
     */
    public void setEdocUnit(String edocUnit) {
        this.edocUnit = edocUnit;
    }

    /**
     * @return the edocUnitId
     */
    public String getEdocUnitId() {
        return edocUnitId;
    }

    /**
     * @param edocUnitId the edocUnitId to set
     */
    public void setEdocUnitId(String edocUnitId) {
        this.edocUnitId = edocUnitId;
    }

    /**
     * @return the edocDate
     */
    public java.sql.Date getEdocDate() {
        return edocDate;
    }

    /**
     * @param edocDate the edocDate to set
     */
    public void setEdocDate(java.sql.Date edocDate) {
        this.edocDate = edocDate;
    }

    /**
     * @return the registerUserId
     */
    public Long getRegisterUserId() {
        return registerUserId;
    }

    /**
     * @param registerUserId the registerUserId to set
     */
    public void setRegisterUserId(Long registerUserId) {
        this.registerUserId = registerUserId;
    }

    /**
     * @return the registerUserName
     */
    public String getRegisterUserName() {
        return registerUserName;
    }

    /**
     * @param registerUserName the registerUserName to set
     */
    public void setRegisterUserName(String registerUserName) {
        this.registerUserName = registerUserName;
    }

    /**
     * @return the registerDate
     */
    public java.sql.Date getRegisterDate() {
        return registerDate;
    }

    /**
     * @param registerDate the registerDate to set
     */
    public void setRegisterDate(java.sql.Date registerDate) {
        this.registerDate = registerDate;
    }

    /**
     * @return the signer
     */
    public String getSigner() {
        return signer;
    }

    /**
     * @param signer the signer to set
     */
    public void setSigner(String signer) {
        this.signer = signer;
    }

    /**
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * @return the distributerId
     */
    public Long getDistributerId() {
        return distributerId;
    }

    /**
     * @param distributerId the distributerId to set
     */
    public void setDistributerId(Long distributerId) {
        this.distributerId = distributerId;
    }

    /**
     * @return the distributer
     */
    public String getDistributer() {
        return distributer;
    }

    /**
     * @param distributer the distributer to set
     */
    public void setDistributer(String distributer) {
        this.distributer = distributer;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the docType
     */
    public String getDocType() {
        return docType;
    }

    /**
     * @param docType the docType to set
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }

    /**
     * @return the sendType
     */
    public String getSendType() {
        return sendType;
    }

    /**
     * @param sendType the sendType to set
     */
    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    /**
     * @return the docMark
     */
    public String getDocMark() {
        return docMark;
    }

    /**
     * @param docMark the docMark to set
     */
    public void setDocMark(String docMark) {
        this.docMark = docMark;
    }

    /**
     * @return the serialNo
     */
    public String getSerialNo() {
        return serialNo;
    }

    /**
     * @param serialNo the serialNo to set
     */
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    /**
     * @return the secretLevel
     */
    public String getSecretLevel() {
        return secretLevel;
    }

    /**
     * @param secretLevel the secretLevel to set
     */
    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

    /**
     * @return the urgentLevel
     */
    public String getUrgentLevel() {
        return urgentLevel;
    }

    /**
     * @param urgentLevel the urgentLevel to set
     */
    public void setUrgentLevel(String urgentLevel) {
        this.urgentLevel = urgentLevel;
    }

    /**
     * @return the keepPeriod
     */
    public String getKeepPeriod() {
        return keepPeriod;
    }

    /**
     * @param keepPeriod the keepPeriod to set
     */
    public void setKeepPeriod(String keepPeriod) {
        this.keepPeriod = keepPeriod;
    }

    /**
     * @return the sendTo
     */
    public String getSendTo() {
        return sendTo;
    }

    /**
     * @param sendTo the sendTo to set
     */
    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    /**
     * @return the sendToId
     */
    public String getSendToId() {
        return sendToId;
    }

    /**
     * @param sendToId the sendToId to set
     */
    public void setSendToId(String sendToId) {
        this.sendToId = sendToId;
    }

    /**
     * @return the copyTo
     */
    public String getCopyTo() {
        return copyTo;
    }

    /**
     * @param copyTo the copyTo to set
     */
    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }

    /**
     * @return the copyToId
     */
    public String getCopyToId() {
        return copyToId;
    }

    /**
     * @param copyToId the copyToId to set
     */
    public void setCopyToId(String copyToId) {
        this.copyToId = copyToId;
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


    /**
     * @return the noteAppend
     */
    public String getNoteAppend() {
        return noteAppend;
    }

    /**
     * @param noteAppend the noteAppend to set
     */
    public void setNoteAppend(String noteAppend) {
        this.noteAppend = noteAppend;
    }

    /**
     * @return the attNote
     */
    public String getAttNote() {
        return attNote;
    }

    /**
     * @param attNote the attNote to set
     */
    public void setAttNote(String attNote) {
        this.attNote = attNote;
    }

    /**
     * @return the state
     */
    public Integer getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * @return the orgAccountId
     */
    public Long getOrgAccountId() {
        return orgAccountId;
    }

    /**
     * @return the issuerId
     */
    public Long getIssuerId() {
        return issuerId;
    }

    /**
     * @param issuerId the issuerId to set
     */
    public void setIssuerId(Long issuerId) {
        this.issuerId = issuerId;
    }

    /**
     * @return the issueDate
     */
    public java.sql.Date getIssueDate() {
        return issueDate;
    }

    /**
     * @param issueDate the issueDate to set
     */
    public void setIssueDate(java.sql.Date issueDate) {
        this.issueDate = issueDate;
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

    /**
     * @param orgAccountId the orgAccountId to set
     */
    public void setOrgAccountId(Long orgAccountId) {
        this.orgAccountId = orgAccountId;
    }

    /**
     * @return the distributeDate
     */
    public java.sql.Date getDistributeDate() {
        return distributeDate;
    }

    /**
     * @param distributeDate the distributeDate to set
     */
    public void setDistributeDate(java.sql.Date distributeDate) {
        this.distributeDate = distributeDate;
    }

    /**
     * @return the distributeState
     */
    public Integer getDistributeState() {
        return distributeState;
    }

    /**
     * @param distributeState the distributeState to set
     */
    public void setDistributeState(Integer distributeState) {
        this.distributeState = distributeState;
    }

    /**
     * @return the attachmentList
     */
    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    /**
     * @param attachmentList the attachmentList to set
     */
    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    /**
     * @return the edocBodyList
     */
    public RegisterBody getRegisterBody() {
        return registerBody;
    }

    /**
     * @param edocBody the edocBody to set
     */
    public void setRegisterBody(RegisterBody registerBody) {
        this.registerBody = registerBody;
    }
    
    public boolean getHasAttachments() {
        return IdentifierUtil.lookupInner(identifier, EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1');
    }

    public void setHasAttachments(boolean hasAttachments) {
    	this.hasAttachments = hasAttachments;
        this.identifier = IdentifierUtil.update(this.getIdentifier(), EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), hasAttachments ? '1' : '0');
    }
    public String getUnitLevel() {
		return unitLevel;
	}

	public void setUnitLevel(String unitLevel) {
		this.unitLevel = unitLevel;
	}

	 public Integer getCopies() {
		return copies;
	}

	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	public java.sql.Timestamp getExchangeSendTime() {
		return exchangeSendTime;
	}

	public void setExchangeSendTime(java.sql.Timestamp exchangeSendTime) {
		this.exchangeSendTime = exchangeSendTime;
	}
	 public Integer getExchangeMode() {
		return exchangeMode;
	}

	public void setExchangeMode(Integer exchangeMode) {
		this.exchangeMode = exchangeMode;
	}

    
}
