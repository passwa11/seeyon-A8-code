package com.seeyon.v3x.edoc.domain;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

/**
 * The persistent class for the edoc_summary database table.
 * 请修改这个类的同学注意，如果是新增公文元素（不包括意见元素），必须要在get方法上加@Column(name = "***")的注解，不然会导致必填项判断不到新增的公文元素
 * @author BEA Workshop Studio
 */
public class EdocSummary extends BasePO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1593346713271077837L;
	//default serial version id, required for serializable classes.
	//private static final Log  LOGGER     = CtpLogFactory.getLog(EdocSummary.class);

	/**
	 * 标志位, 共100位，采用枚举的自然顺序
	 */
	public static enum INENTIFIER_INDEX {
		HAS_ATTACHMENTS, // 是否有附件
	};

	private String identifier;
	protected static final int INENTIFIER_SIZE = 20;

	private Boolean hasArchive = false;
	private Long archiveId;



	//private Long id = null;

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
	private java.sql.Date date1;//date1
	private java.sql.Date date10;
	private java.sql.Date date2;
	private java.sql.Date date3;
	private java.sql.Date date4;
	private java.sql.Date date5;
	private java.sql.Date date6;
	private java.sql.Date date7;
	private java.sql.Date date8;
	private java.sql.Date date9;
	private java.sql.Date date11;
	private java.sql.Date date20;
	private java.sql.Date date12;
	private java.sql.Date date13;
	private java.sql.Date date14;
	private java.sql.Date date15;
	private java.sql.Date date16;
	private java.sql.Date date17;
	private java.sql.Date date18;
	private java.sql.Date date19;
	private Long deadline=-1L;
	private java.util.Date deadlineDatetime;
	private Double decimal1;//decimal1
	private Double decimal10;
	private Double decimal2;
	private Double decimal3;
	private Double decimal4;
	private Double decimal5;
	private Double decimal6;
	private Double decimal7;
	private Double decimal8;
	private Double decimal9;
	private Double decimal11;
	private Double decimal20;
	private Double decimal12;
	private Double decimal13;
	private Double decimal14;
	private Double decimal15;
	private Double decimal16;
	private Double decimal17;
	private Double decimal18;
	private Double decimal19;
	private String docMark;//doc_mark
	private String docMark2;//doc_mark2
	private String docType;//doc_type
	private int edocType;
	private Long formId;
	private Integer integer1;//integer1
	private Integer integer10;
	private Integer integer2;
	private Integer integer3;
	private Integer integer4;
	private Integer integer5;
	private Integer integer6;
	private Integer integer7;
	private Integer integer8;
	private Integer integer9;
	private Integer integer11;
	private Integer integer20;
	private Integer integer12;
	private Integer integer13;
	private Integer integer14;
	private Integer integer15;
	private Integer integer16;
	private Integer integer17;
	private Integer integer18;
	private Integer integer19;
	private String issuer;
	private Integer keepPeriod;//keep_period
	private String keywords;  //keyword
	private String list1;
	private String list10;
	private String list2;
	private String list3;
	private String list4;
	private String list5;
	private String list6;
	private String list7;
	private String list8;
	private String list9;
	private String list11;
	private String list20;
	private String list12;
	private String list13;
	private String list14;
	private String list15;
	private String list16;
	private String list17;
	private String list18;
	private String list19;
	private String printUnit;//print_unit
	private String printUnitId;
	private String printer;
	private String processId;
	private String reportTo;//report_to
	private String reportToId;
	private String reportTo2;
	private String reportToId2;
	private String secretLevel;//secret_level
	private String unitLevel; //公文级别
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
	private Integer _state;
	private int state = EdocConstant.flowState.run.ordinal();
	private String subject;
	private String dynamicSubject;
	
	private String text1;//text1
	private String text10;
	private String text2;
	private String text3;
	private String text4;
	private String text5;
	private String text6;
	private String text7;
	private String text8;
	private String text9;
	private String text11;
	private String text12;
	private String text13;
	private String text14;
	private String text15;
	private String urgentLevel;//urgent_level
	private String varchar1;  //对应于string1----其余的类似
	private String varchar10;
	private String varchar2;
	private String varchar3;
	private String varchar4;
	private String varchar5;
	private String varchar6;
	private String varchar7;
	private String varchar8;
	private String varchar9;
	private String varchar11;
	private String varchar20;
	private String varchar12;
	private String varchar13;
	private String varchar14;
	private String varchar15;
	private String varchar16;
	private String varchar17;
	private String varchar18;
	private String varchar19;
	private String varchar21;
	private String varchar22;
	private String varchar23;
	private String varchar24;
	private String varchar25;
	private String varchar26;
	private String varchar27;
	private String varchar28;
	private String varchar29;
	private String varchar30;
	private java.sql.Timestamp sendTime;
	/**
	 * 父协同ID
	 */
	private java.lang.Long parentformSummaryid;

	/**
	 * 能否修改正文
	 */
	private java.lang.Boolean canEdit = true;
	/**
	 * 是否能归档
	 */
	private java.lang.Boolean canArchive = false;
	/**
	 * 关联项目ID
	 */
	private java.lang.Long projectId;
	/**
	   *  能否编辑附件 
	   */
	private java.lang.Boolean canEditAttachment = false;
	/**
	   *  是否能修改 
	   */
	private java.lang.Boolean canModify = false;
	/**
	   *  是否能转发 
	   */
	private java.lang.Boolean canForward = false;
	/**
	   * 预归档
	   */
	private String advancePigeonhole;
	/**
	   *  发起人 
	   */
	private java.lang.Long startMemberId;
	/**
	 * 公文类型
	 */
	private Integer govdocType;
	private Integer jianbanType = 1;
	private Long fontSize;
	private String govcode;
	private boolean canDueReminder;
	/**
	 * @Field edocSecretLevel:公文流程密级
	 */
	private Integer edocSecretLevel;
	//G6 V5.7 流转状态
	private int transferStatus;
	private String signPerson;//签收人
	private Long signAccountId;
	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-10 [修改功能：公文库]start*/
	private Boolean toEdocLibFlag = false;//是否要在公文库中显示	
	private String toEdocLibType = "";//什么情况入的公文库：send 发行时默认文件进入公文库；    quickSend快速发文时默认文件不进入公文库 ； end 发文办结 
	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-10 [修改功能：公文库]end*/
	/*客开 项目名称：G6 580 省级专版  新增字段 signReply 与 sendDate   chenyq*/
	private java.sql.Timestamp sendDate;//送文日期
	private String publicInfo;
	private String receiveUnit;
	private String receiveUnitId;
	private String checkPerson;
	
	//add by rz 2017-09-21[添加批示意见元素] 
	private java.util.Date proxyDate;//批示编号的时间
	private Integer pishiNo;//批示编号
	private String pishiName;//批示领导编号
	private String pishiYear;//批示年号
	private String pishiLeaderName;//批示年号
	private java.lang.String awakeDate;//督办时间
	private java.lang.Boolean canAutostopflow = false;//是否自动终止流程 
	private Long formAppid;//表单ID
	private Long formRecordid;//主表记录id
	private String bodyType;//正文类型
	private Long exchangeSendAffairId;//分送的affairId
	/**
	   *  审核通过/审核不通过
	   */
	private java.lang.Boolean audited = false;
	/**
	   *  是否核定0：默认值;1核定通过;2核定不能过 
	   */
	private java.lang.Integer vouch = EdocConstant.vouchState.defaultValue.ordinal();
	
	//是否续办 (只为在保存续办模板的时候使用,没有在数据库新增字段)
	private Boolean isCustomDealWith;
	private String customDealWithNode;//续办节点
	private String customDealWithMemberId;//续办人员id
	private String customDealWithMemberName;//续办人员名

	/**
	   *  督办人员Id串
	   */
	private java.lang.String supervisorsId;
	
	//额外属性，规范不允许使用，额外属性以Map方式存储
    private DataContainer extraAttr = null;
    /**
     * 是否自动更新标题
     */
    private java.lang.Boolean updateSubject = false;
    //是否合并处理
    private Boolean canMergeDeal;
    //与前一任处理节点相同时，合并处理
    private Boolean canAnyMerge;
    /**
     *  重复次数 
     */
    private java.lang.Integer resentTime;
    /**
     *  新流程类型 
     */
    private java.lang.Integer newflowType;
    /**
     *  附件归档ID 
     */
    private java.lang.Long attachmentArchiveId;
    
	private Long workitemId;//兼容老公文交换数据添加的一个字段(没有映射到数据库)
    
    public boolean isFinished() {
		return EdocConstant.flowState.terminate.ordinal() == this.getState() 
				|| EdocConstant.flowState.finish.ordinal() == this.getState();
	}
    /**
     * 合并处理类型
     */
    private java.lang.String mergeDealType;
    
    /*********** v7.1sp1 新增字段 strat  ************************/
    
    /**
     * 流程超期处理类型(0-自动终止，1-自动撤销)
     */
    private java.lang.Integer processTermType;
    
    /**
     *  提醒周期 
     */
    private java.lang.Long remindInterval;
    
    /**
     * 拟办意见
     */
    private String dealSuggestion;
    
    /**
     * 是否自动发起
     */
   private Boolean autoRun = false;
    /*********** v7.1sp1 新增字段 end  ************************/
    
    
    /**
     * 返回扩展属性Map
     * @return 扩展属性Map
     */
    @SuppressWarnings("rawtypes")
	public Map getExtraMap() {
        return getExtraDataContainer();
    }

    public DataContainer getExtraDataContainer() {
        if(extraAttr==null){
            extraAttr = new DataContainer();
        }
        return extraAttr;
    }
    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（int型）
     */
    public void putExtraAttr(String key, int value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（long型）
     */
    public void putExtraAttr(String key, long value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（short型）
     */
    public void putExtraAttr(String key, short value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（byte型）
     */
    public void putExtraAttr(String key, byte value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（float型）
     */
    public void putExtraAttr(String key, float value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（double型）
     */
    public void putExtraAttr(String key, double value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（boolean型）
     */
    public void putExtraAttr(String key, boolean value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（char型）
     */
    public void putExtraAttr(String key, char value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（String型）
     */
    public void putExtraAttr(String key, String value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（java.util.Date型）
     */
    public void putExtraAttr(String key, Date value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（BasePO型）
     */
    public void putExtraAttr(String key, BasePO value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一条属性及它的值
     * @param key 属性名
     * @param value 属性值（java.util.List型）
     */
    public void putExtraAttr(String key, List<? extends Object> value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一个字符串数组
     * @param key 属性名
     * @param value 属性值（String数组类型）
     */
    public void putExtraAttr(String key, String[] value) {
        getExtraDataContainer().add(key, value);
    }

    /**
     * 向数据集中添加一个整形数组
     * @param key 属性名
     * @param value 属性值（int数组类型）
     */
    public void putExtraAttr(String key, int[] value) {
        getExtraDataContainer().add(key, value);
    }
	
	public java.sql.Timestamp getSendTime() {
		return sendTime;
	}
	public void setSendTime(java.sql.Timestamp sendTime) {
		this.sendTime = sendTime;
	}


	public Map<String, String> superviseMap = new HashMap<String, String>();

	@Column(name = "phone")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Map<String, String> getSuperviseMap() {
		return superviseMap;
	}
	public void setSuperviseMap(Map<String, String> superviseMap) {
		this.superviseMap = superviseMap;
	}

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
	private Integer processType = 1;//lijl添加,处理类型(1办文,2阅文),edoc_type的子类型
	@SuppressWarnings("unused")
	private String edocTypeEnum;//lijl添加,将edocType转化成枚举值recEdoc

	private java.sql.Date receiptDate;//签收日期
	private java.sql.Date registrationDate;//登记日期
	private String auditor;//审核人
	private String review;//复核人
	private String undertaker;//承办人
	private String undertakenoffice;//承办机构
	private String undertakenofficeId;//承办机构ID
	private String undertakerDep; //承办人部门
	private String undertakerAccount;//承办人单位
	private Integer exchangeType = 0;//交换表的type
	private Integer fromType = 0;//公文数据来源类型

	private Boolean isQuickSend = false ; //是否为快速发文，默认为否
	private String from;

	@Column(name = "undertakerDep")
	public String getUndertakerDep() {
		return undertakerDep;
	}
	public void setUndertakerDep(String undertakerDep) {
		this.undertakerDep = undertakerDep;
	}
	@Column(name = "undertakerAccount")
	public String getUndertakerAccount() {
		return undertakerAccount;
	}
	public void setUndertakerAccount(String undertakerAccount) {
		this.undertakerAccount = undertakerAccount;
	}
	public Boolean getIsQuickSend() {
		return isQuickSend;
	}
	public void setIsQuickSend(Boolean isQuickSend) {
		this.isQuickSend = isQuickSend;
	}
	@Column(name = "receiptDate")
	public java.sql.Date getReceiptDate() {
        return receiptDate;
    }
    public void setReceiptDate(java.sql.Date receiptDate) {
        this.receiptDate = receiptDate;
    }
    public java.sql.Date getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(java.sql.Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    @Column(name = "auditor")
    public String getAuditor() {
        return auditor;
    }
    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }
    @Column(name = "review")
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
    @Column(name = "undertaker")
    public String getUndertaker() {
        return undertaker;
    }
    public void setUndertaker(String undertaker) {
    	//数据库长度500，限制166个字符
    	if(Strings.isNotBlank(undertaker) && undertaker.length() < 166){
    		this.undertaker = undertaker;
    	}
    }



    /**
	 * 流程是否超期
	 */
	private java.lang.Boolean coverTime = false;

	public Integer getProcessType() {
		return processType;
	}
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}
	public Long getSubEdocType() {
		return subEdocType;
	}
	public void setSubEdocType(Long subEdocType) {
		this.subEdocType = subEdocType;
	}
		/*public String getCperson() {
			return cperson;
		}
		public void setCperson(String cperson) {
			this.cperson = cperson;
		}
		*/
	    @Column(name = "party")
		public String getParty() {
			return party;
		}
		public void setParty(String party) {
			this.party = party;
		}
		@Column(name = "administrative")
		public String getAdministrative() {
			return administrative;
		}
		public void setAdministrative(String administrative) {
			this.administrative = administrative;
		}
		@Column(name = "filesm")
		public String getFilesm() {
			if(!Strings.isBlank(filesm)){
				return filesm.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>");
			}
			return filesm;
		}
		public String getEdocTypeEnum() {
			return EdocEnum.getEdocAppName(edocType);
		}
		public void setFilesm(String filesm) {
			this.filesm = filesm;
		}
		@Column(name = "filefz")
		public String getFilefz() {
			return filefz;
		}

		public void setFilefz(String filefz) {
			this.filefz = filefz;
		}
		public String getGovdocTypeEnum() {
			if(govdocType < 1){
				return EdocEnum.getEdocAppName(edocType);
			}else{
				return GovdocEnum.getGovdocAppName(govdocType);
			}
		}

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

	@SuppressWarnings("unused")
	private String forwardMember; //转发时，原公文的发起人name (不进行持久化)

    public String getForwardMember() {
		return null; //tanmf屏蔽的，有问题找谭敏锋
	}
    @Deprecated
	public void setForwardMember(String forwardMember) {
		//this.forwardMember = forwardMember;  //tanmf屏蔽的，有问题找谭敏锋
	}

    public String getCurrentNodesInfo() {
		return currentNodesInfo==null?"":currentNodesInfo;
	}
	public void setCurrentNodesInfo(String currentNodesInfo) {
		this.currentNodesInfo = currentNodesInfo;
	}
	public Long getAffairId() {
        return affairId;
    }
    public void setAffairId(Long affairId) {
        this.affairId = affairId;
    }
    public V3xOrgMember getStartMember() {
		return startMember;
	}

	public void setStartMember(V3xOrgMember startMember) {
		this.startMember = startMember;
	}

	private boolean finished=false;

	public boolean getFinished()
	{
		finished=completeTime!=null;
		return this.finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished=finished;
		if(finished)
		{
			setCompleteTime(new Timestamp(System.currentTimeMillis()));
		}
	}

/////////////////////////非数据库字段数据 结束///////////////////////////

	public Long getAdvanceRemind() {
		return advanceRemind;
	}

	public void setAdvanceRemind(Long advanceRemind) {
		this.advanceRemind = advanceRemind;
	}

	public String getWorkflowRule() {
		return workflowRule;
	}

	public void setWorkflowRule(String workflowRule) {
		this.workflowRule = workflowRule;
	}

	public Long getTempleteId() {
		return templeteId;
	}

	public void setTempleteId(Long templeteId) {
		this.templeteId = templeteId;
	}

    public EdocSummary() {
    }

    public Boolean getHasArchive()
    {
    	return this.hasArchive == null || !this.hasArchive? false : true;
    }
    public void setHasArchive(Boolean hasArchive)
    {
    	this.hasArchive=hasArchive;
    }

    public Boolean getIsunit()
    {
    	if(this.isunit==null){this.isunit=false;}
    	return this.isunit;
    }
    public void setIsunit(Boolean isunit)
    {
    	this.isunit=isunit;
    }

	public String getIdentifier() {
		return IdentifierUtil.newIdentifier(this.identifier, INENTIFIER_SIZE,
				'0');
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isHasAttachments() {
		return IdentifierUtil.lookupInner(identifier,
				EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1');
	}

	public void setHasAttachments(boolean hasAttachments) {
		this.identifier = IdentifierUtil.update(this.getIdentifier(),
				EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), hasAttachments ? '1' : '0');
	}

    public void setEdocOpinion(EdocOpinion senderOpinion)
    {
    	this.senderOpinion=senderOpinion;
    }
	public EdocOpinion getSenderOpinion() {
		if (senderOpinion != null) {
			return senderOpinion;
		}
		if (this.getEdocOpinions() != null) {
			for (EdocOpinion opinion : edocOpinions) {
				if (opinion.getOpinionType() == EdocOpinion.OpinionType.senderOpinion.ordinal()) {
					//只取最初的附言
					if(senderOpinion==null)
					{
						senderOpinion=opinion;
					}
					else
					{
						if(senderOpinion.getCreateTime().after(opinion.getCreateTime()))
						{
							senderOpinion=opinion;
						}
					}
				}
			}
		}
		return senderOpinion;
	}

	public int getCanTrack() {
		return this.canTrack;
	}
	public void setCanTrack(int canTrack) {
		this.canTrack = canTrack;
	}

	public Long getCaseId() {
		return this.caseId;
	}
	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}

	public String getComment() {
		return this.comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public java.sql.Timestamp getCompleteTime() {
		return this.completeTime;
	}
	public void setCompleteTime(java.sql.Timestamp completeTime) {
		this.completeTime = completeTime;
	}

	@Column(name = "copies")
	public Integer getCopies() {
		return this.copies;
	}
	public void setCopies(Integer copies) {
		this.copies = copies;
	}

	@Column(name = "copies2")
	public Integer getCopies2() {
		return this.copies2;
	}
	public void setCopies2(Integer copies2) {
		this.copies2 = copies2;
	}

	@Column(name = "copy_to")
	public String getCopyTo() {
		return this.copyTo;
	}
	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	public String getCopyToId() {
		return this.copyToId;
	}
	public void setCopyToId(String copyToId) {
		this.copyToId = copyToId;
	}

	@Column(name = "copy_to2")
	public String getCopyTo2() {
		return this.copyTo2;
	}
	public void setCopyTo2(String copyTo2) {
		this.copyTo2 = copyTo2;
	}

	public String getCopyToId2() {
		return this.copyToId2;
	}
	public void setCopyToId2(String copyToId2) {
		this.copyToId2 = copyToId2;
	}

	@Column(name = "create_person")
	public String getCreatePerson() {
		return this.createPerson;
	}
	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	public java.sql.Timestamp getCreateTime() {
		return this.createTime;
	}
	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}

	public java.sql.Timestamp getPackTime() {
		return this.packTime;
	}
	public void setPackTime(java.sql.Timestamp packTime) {
		this.packTime = packTime;
	}

	@Column(name = "date1")
	public java.sql.Date getDate1() {
		return this.date1;
	}
	public void setDate1(java.sql.Date date1) {
		this.date1 = date1;
	}

	@Column(name = "date10")
	public java.sql.Date getDate10() {
		return this.date10;
	}
	public void setDate10(java.sql.Date date10) {
		this.date10 = date10;
	}

	@Column(name = "date2")
	public java.sql.Date getDate2() {
		return this.date2;
	}
	public void setDate2(java.sql.Date date2) {
		this.date2 = date2;
	}

	@Column(name = "date3")
	public java.sql.Date getDate3() {
		return this.date3;
	}
	public void setDate3(java.sql.Date date3) {
		this.date3 = date3;
	}

	@Column(name = "date4")
	public java.sql.Date getDate4() {
		return this.date4;
	}
	public void setDate4(java.sql.Date date4) {
		this.date4 = date4;
	}

	@Column(name = "date5")
	public java.sql.Date getDate5() {
		return this.date5;
	}
	public void setDate5(java.sql.Date date5) {
		this.date5 = date5;
	}

	@Column(name = "date6")
	public java.sql.Date getDate6() {
		return this.date6;
	}
	public void setDate6(java.sql.Date date6) {
		this.date6 = date6;
	}

	@Column(name = "date7")
	public java.sql.Date getDate7() {
		return this.date7;
	}
	public void setDate7(java.sql.Date date7) {
		this.date7 = date7;
	}

	@Column(name = "date8")
	public java.sql.Date getDate8() {
		return this.date8;
	}
	public void setDate8(java.sql.Date date8) {
		this.date8 = date8;
	}

	@Column(name = "date9")
	public java.sql.Date getDate9() {
		return this.date9;
	}
	public void setDate9(java.sql.Date date9) {
		this.date9 = date9;
	}

	@Column(name = "date11")
	public java.sql.Date getDate11() {
		return this.date11;
	}
	public void setDate11(java.sql.Date date11) {
		this.date11 = date11;
	}

	@Column(name = "date20")
	public java.sql.Date getDate20() {
		return this.date20;
	}
	public void setDate20(java.sql.Date date20) {
		this.date20 = date20;
	}

	@Column(name = "date12")
	public java.sql.Date getDate12() {
		return this.date12;
	}
	public void setDate12(java.sql.Date date12) {
		this.date12 = date12;
	}

	@Column(name = "date13")
	public java.sql.Date getDate13() {
		return this.date13;
	}
	public void setDate13(java.sql.Date date13) {
		this.date13 = date13;
	}

	@Column(name = "date14")
	public java.sql.Date getDate14() {
		return this.date14;
	}
	public void setDate14(java.sql.Date date14) {
		this.date14 = date14;
	}

	@Column(name = "date15")
	public java.sql.Date getDate15() {
		return this.date15;
	}
	public void setDate15(java.sql.Date date15) {
		this.date15 = date15;
	}

	@Column(name = "date16")
	public java.sql.Date getDate16() {
		return this.date16;
	}
	public void setDate16(java.sql.Date date16) {
		this.date16 = date16;
	}

	@Column(name = "date17")
	public java.sql.Date getDate17() {
		return this.date17;
	}
	public void setDate17(java.sql.Date date17) {
		this.date17 = date17;
	}

	@Column(name = "date18")
	public java.sql.Date getDate18() {
		return this.date18;
	}
	public void setDate18(java.sql.Date date18) {
		this.date18 = date18;
	}

	@Column(name = "date19")
	public java.sql.Date getDate19() {
		return this.date19;
	}
	public void setDate19(java.sql.Date date19) {
		this.date19 = date19;
	}

	public Long getDeadline() {
		return this.deadline;
	}
	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}

	@Column(name = "decimal1")
	public Double getDecimal1() {
		return this.decimal1;
	}
	public void setDecimal1(Double decimal1) {
		this.decimal1 = decimal1;
	}

	@Column(name = "decimal10")
	public Double getDecimal10() {
		return this.decimal10;
	}
	public void setDecimal10(Double decimal10) {
		this.decimal10 = decimal10;
	}

	@Column(name = "decimal2")
	public Double getDecimal2() {
		return this.decimal2;
	}
	public void setDecimal2(Double decimal2) {
		this.decimal2 = decimal2;
	}

	@Column(name = "decimal3")
	public Double getDecimal3() {
		return this.decimal3;
	}
	public void setDecimal3(Double decimal3) {
		this.decimal3 = decimal3;
	}

	@Column(name = "decimal4")
	public Double getDecimal4() {
		return this.decimal4;
	}
	public void setDecimal4(Double decimal4) {
		this.decimal4 = decimal4;
	}

	@Column(name = "decimal5")
	public Double getDecimal5() {
		return this.decimal5;
	}
	public void setDecimal5(Double decimal5) {
		this.decimal5 = decimal5;
	}

	@Column(name = "decimal6")
	public Double getDecimal6() {
		return this.decimal6;
	}
	public void setDecimal6(Double decimal6) {
		this.decimal6 = decimal6;
	}

	@Column(name = "decimal7")
	public Double getDecimal7() {
		return this.decimal7;
	}
	public void setDecimal7(Double decimal7) {
		this.decimal7 = decimal7;
	}

	@Column(name = "decimal8")
	public Double getDecimal8() {
		return this.decimal8;
	}
	public void setDecimal8(Double decimal8) {
		this.decimal8 = decimal8;
	}

	@Column(name = "decimal9")
	public Double getDecimal9() {
		return this.decimal9;
	}
	public void setDecimal9(Double decimal9) {
		this.decimal9 = decimal9;
	}

	@Column(name = "decimal11")
	public Double getDecimal11() {
		return this.decimal11;
	}
	public void setDecimal11(Double decimal11) {
		this.decimal11 = decimal11;
	}

	@Column(name = "decimal20")
	public Double getDecimal20() {
		return this.decimal20;
	}
	public void setDecimal20(Double decimal20) {
		this.decimal20 = decimal20;
	}

	@Column(name = "decimal12")
	public Double getDecimal12() {
		return this.decimal12;
	}
	public void setDecimal12(Double decimal12) {
		this.decimal12 = decimal12;
	}

	@Column(name = "decimal13")
	public Double getDecimal13() {
		return this.decimal13;
	}
	public void setDecimal13(Double decimal13) {
		this.decimal13 = decimal13;
	}

	@Column(name = "decimal14")
	public Double getDecimal14() {
		return this.decimal14;
	}
	public void setDecimal14(Double decimal14) {
		this.decimal14 = decimal14;
	}

	@Column(name = "decimal15")
	public Double getDecimal15() {
		return this.decimal15;
	}
	public void setDecimal15(Double decimal15) {
		this.decimal15 = decimal15;
	}

	@Column(name = "decimal16")
	public Double getDecimal16() {
		return this.decimal16;
	}
	public void setDecimal16(Double decimal16) {
		this.decimal16 = decimal16;
	}

	@Column(name = "decimal17")
	public Double getDecimal17() {
		return this.decimal17;
	}
	public void setDecimal17(Double decimal17) {
		this.decimal17 = decimal17;
	}

	@Column(name = "decimal18")
	public Double getDecimal18() {
		return this.decimal18;
	}
	public void setDecimal18(Double decimal18) {
		this.decimal18 = decimal18;
	}

	@Column(name = "decimal19")
	public Double getDecimal19() {
		return this.decimal19;
	}
	public void setDecimal19(Double decimal19) {
		this.decimal19 = decimal19;
	}

	@Column(name = "doc_mark")
	public String getDocMark() {
		return this.docMark;
	}
	public void setDocMark(String docMark) {
		
	    String _docMark = docMark;
		if(Strings.isNotBlank(_docMark)){
			_docMark = _docMark.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
		}
		
		this.docMark = _docMark;
	}

	@Column(name = "doc_mark2")
	public String getDocMark2() {
		
		if(Strings.isNotBlank(docMark2)){
			docMark2 = docMark2.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
		}
		
		return this.docMark2;
	}
	public void setDocMark2(String docMark2) {
		this.docMark2 = docMark2;
	}

	@Column(name = "doc_type")
	public String getDocType() {
		return this.docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}

	public int getEdocType() {
		return this.edocType;
	}
	public void setEdocType(int edocType) {
		this.edocType = edocType;
	}

	public Long getFormId() {
		return this.formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}

	@Column(name = "integer1")
	public Integer getInteger1() {
		return this.integer1;
	}
	public void setInteger1(Integer integer1) {
		this.integer1 = integer1;
	}

	@Column(name = "integer10")
	public Integer getInteger10() {
		return this.integer10;
	}
	public void setInteger10(Integer integer10) {
		this.integer10 = integer10;
	}

	@Column(name = "integer2")
	public Integer getInteger2() {
		return this.integer2;
	}
	public void setInteger2(Integer integer2) {
		this.integer2 = integer2;
	}

	@Column(name = "integer3")
	public Integer getInteger3() {
		return this.integer3;
	}
	public void setInteger3(Integer integer3) {
		this.integer3 = integer3;
	}

	@Column(name = "integer4")
	public Integer getInteger4() {
		return this.integer4;
	}
	public void setInteger4(Integer integer4) {
		this.integer4 = integer4;
	}

	@Column(name = "integer5")
	public Integer getInteger5() {
		return this.integer5;
	}
	public void setInteger5(Integer integer5) {
		this.integer5 = integer5;
	}

	@Column(name = "integer6")
	public Integer getInteger6() {
		return this.integer6;
	}
	public void setInteger6(Integer integer6) {
		this.integer6 = integer6;
	}

	@Column(name = "integer7")
	public Integer getInteger7() {
		return this.integer7;
	}
	public void setInteger7(Integer integer7) {
		this.integer7 = integer7;
	}

	@Column(name = "integer8")
	public Integer getInteger8() {
		return this.integer8;
	}
	public void setInteger8(Integer integer8) {
		this.integer8 = integer8;
	}

	@Column(name = "integer9")
	public Integer getInteger9() {
		return this.integer9;
	}
	public void setInteger9(Integer integer9) {
		this.integer9 = integer9;
	}

	@Column(name = "integer11")
	public Integer getInteger11() {
		return this.integer11;
	}
	public void setInteger11(Integer integer11) {
		this.integer11 = integer11;
	}

	@Column(name = "integer20")
	public Integer getInteger20() {
		return this.integer20;
	}
	public void setInteger20(Integer integer20) {
		this.integer20 = integer20;
	}

	@Column(name = "integer12")
	public Integer getInteger12() {
		return this.integer12;
	}
	public void setInteger12(Integer integer12) {
		this.integer12 = integer12;
	}

	@Column(name = "integer13")
	public Integer getInteger13() {
		return this.integer13;
	}
	public void setInteger13(Integer integer13) {
		this.integer13 = integer13;
	}

	@Column(name = "integer14")
	public Integer getInteger14() {
		return this.integer14;
	}
	public void setInteger14(Integer integer14) {
		this.integer14 = integer14;
	}

	@Column(name = "integer15")
	public Integer getInteger15() {
		return this.integer15;
	}
	public void setInteger15(Integer integer15) {
		this.integer15 = integer15;
	}

	@Column(name = "integer16")
	public Integer getInteger16() {
		return this.integer16;
	}
	public void setInteger16(Integer integer16) {
		this.integer16 = integer16;
	}

	@Column(name = "integer17")
	public Integer getInteger17() {
		return this.integer17;
	}
	public void setInteger17(Integer integer17) {
		this.integer17 = integer17;
	}

	@Column(name = "integer18")
	public Integer getInteger18() {
		return this.integer18;
	}
	public void setInteger18(Integer integer18) {
		this.integer18 = integer18;
	}

	@Column(name = "integer19")
	public Integer getInteger19() {
		return this.integer19;
	}
	public void setInteger19(Integer integer19) {
		this.integer19 = integer19;
	}

	@Column(name = "issuer")
	public String getIssuer() {
		return this.issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Column(name = "keep_period")
	public Integer getKeepPeriod() {
		return this.keepPeriod;
	}
	public void setKeepPeriod(Integer keepPeriod) {
		this.keepPeriod = keepPeriod;
	}

	@Column(name = "keyword")
	public String getKeywords() {
		return this.keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Column(name = "list1")
	public String getList1() {
		return this.list1;
	}
	public void setList1(String list1) {
		this.list1 = list1;
	}

	@Column(name = "list10")
	public String getList10() {
		return this.list10;
	}
	public void setList10(String list10) {
		this.list10 = list10;
	}

	@Column(name = "list2")
	public String getList2() {
		return this.list2;
	}
	public void setList2(String list2) {
		this.list2 = list2;
	}

	@Column(name = "list3")
	public String getList3() {
		return this.list3;
	}
	public void setList3(String list3) {
		this.list3 = list3;
	}

	@Column(name = "list4")
	public String getList4() {
		return this.list4;
	}
	public void setList4(String list4) {
		this.list4 = list4;
	}

	@Column(name = "list5")
	public String getList5() {
		return this.list5;
	}
	public void setList5(String list5) {
		this.list5 = list5;
	}

	@Column(name = "list6")
	public String getList6() {
		return this.list6;
	}
	public void setList6(String list6) {
		this.list6 = list6;
	}

	@Column(name = "list7")
	public String getList7() {
		return this.list7;
	}
	public void setList7(String list7) {
		this.list7 = list7;
	}

	@Column(name = "list8")
	public String getList8() {
		return this.list8;
	}
	public void setList8(String list8) {
		this.list8 = list8;
	}

	@Column(name = "list9")
	public String getList9() {
		return this.list9;
	}
	public void setList9(String list9) {
		this.list9 = list9;
	}

	@Column(name = "list11")
	public String getList11() {
		return this.list11;
	}
	public void setList11(String list11) {
		this.list11 = list11;
	}

	@Column(name = "list20")
	public String getList20() {
		return this.list20;
	}
	public void setList20(String list20) {
		this.list20 = list20;
	}

	@Column(name = "list12")
	public String getList12() {
		return this.list12;
	}
	public void setList12(String list12) {
		this.list12 = list12;
	}

	@Column(name = "list13")
	public String getList13() {
		return this.list13;
	}
	public void setList13(String list13) {
		this.list13 = list13;
	}

	@Column(name = "list14")
	public String getList14() {
		return this.list14;
	}
	public void setList14(String list14) {
		this.list14 = list14;
	}

	@Column(name = "list15")
	public String getList15() {
		return this.list15;
	}
	public void setList15(String list15) {
		this.list15 = list15;
	}

	@Column(name = "list16")
	public String getList16() {
		return this.list16;
	}
	public void setList16(String list16) {
		this.list16 = list16;
	}

	@Column(name = "list17")
	public String getList17() {
		return this.list17;
	}
	public void setList17(String list17) {
		this.list17 = list17;
	}

	@Column(name = "list18")
	public String getList18() {
		return this.list18;
	}
	public void setList18(String list18) {
		this.list18 = list18;
	}

	@Column(name = "list19")
	public String getList19() {
		return this.list19;
	}
	public void setList19(String list19) {
		this.list19 = list19;
	}

	@Column(name = "print_unit")
	public String getPrintUnit() {
		return this.printUnit;
	}
	public void setPrintUnit(String printUnit) {
		this.printUnit = printUnit;
	}

	@Column(name = "printer")
	public String getPrinter() {
		return this.printer;
	}
	public void setPrinter(String printer) {
		this.printer = printer;
	}

	public String getProcessId() {
		return this.processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	@Column(name = "report_to")
	public String getReportTo() {
		return this.reportTo;
	}
	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}

	public String getReportToId() {
		return this.reportToId;
	}
	public void setReportToId(String reportToId) {
		this.reportToId = reportToId;
	}

	@Column(name = "report_to2")
	public String getReportTo2() {
		return this.reportTo2;
	}
	public void setReportTo2(String reportTo2) {
		this.reportTo2 = reportTo2;
	}

	public String getReportToId2() {
		return this.reportToId2;
	}
	public void setReportToId2(String reportToId2) {
		this.reportToId2 = reportToId2;
	}

	@Column(name = "secret_level")
	public String getSecretLevel() {
		return this.secretLevel;
	}
	public void setSecretLevel(String secretLevel) {
		this.secretLevel = secretLevel;
	}

	@Column(name = "send_to")
	public String getSendTo() {
		return this.sendTo;
	}
	public void setSendTo(String sendTo) {
		this.sendTo = sendTo;
	}

	public String getSendToId() {
		return this.sendToId;
	}
	public void setSendToId(String sendToId) {
		this.sendToId = sendToId;
	}

	@Column(name = "send_to2")
	public String getSendTo2() {
		return this.sendTo2;
	}
	public void setSendTo2(String sendTo2) {
		this.sendTo2 = sendTo2;
	}

	public String getSendToId2() {
		return this.sendToId2;
	}
	public void setSendToId2(String sendToId2) {
		this.sendToId2 = sendToId2;
	}

	@Column(name = "send_type")
	public String getSendType() {
		return this.sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	@Column(name = "send_unit")
	public String getSendUnit() {
		return this.sendUnit;
	}
	public void setSendUnit(String sendUnit) {
		this.sendUnit = sendUnit;
	}

	public String getSendUnitId() {
		return this.sendUnitId;
	}
	public void setSendUnitId(String sendUnitId) {
		this.sendUnitId = sendUnitId;
	}

	@Column(name = "send_unit2")
	public String getSendUnit2() {
		return this.sendUnit2;
	}
	public void setSendUnit2(String sendUnit2) {
		this.sendUnit2 = sendUnit2;
	}

	public String getSendUnitId2() {
		return this.sendUnitId2;
	}
	public void setSendUnitId2(String sendUnitId2) {
		this.sendUnitId2 = sendUnitId2;
	}

	@Column(name = "serial_no")
	public String getSerialNo() {
		return this.serialNo;
	}
	public void setSerialNo(String serialNo) {
		
	    String _serialNo = serialNo;
		if(Strings.isNotBlank(_serialNo)){
			_serialNo = _serialNo.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
		}
		this.serialNo = _serialNo;
	}

	@Column(name = "signing_date")
	public java.sql.Date getSigningDate() {
		return this.signingDate;
	}
	public void setSigningDate(java.sql.Date signingDate) {
		this.signingDate = signingDate;
	}
	public void setSigningDate(java.util.Date signingDate) {
		this.signingDate = new java.sql.Date(signingDate.getTime());
	}

	public java.sql.Timestamp getStartTime() {
		return this.startTime;
	}
	public void setStartTime(java.sql.Timestamp startTime) {
		this.startTime = startTime;
	}

	public Long getStartUserId() {
		return this.startUserId;
	}
	public void setStartUserId(Long startUserId) {
		this.startUserId = startUserId;
	}

	public int getState() {
		return this.state;
	}

	/**
	 * {@link com.seeyon.v3x.collaboration.Constant.flowstate}
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}
	//仅仅供归档时候调用
	public String getSubjectA()
	{
		if(this.isunit)
		{
			return this.subject+ ResourceUtil.getString("edoc.label.archive.send", "A");//[发文A]
		}
		else
		{
			return this.subject;
		}
	}
	//仅供归档时候调用
	public String getSubjectB()
	{
		return this.subject+ ResourceUtil.getString("edoc.label.archive.send", "B");//[发文B];
	}

	@Column(name = "subject")
	public String getSubject() {
		return this.subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name = "text1")
	public String getText1() {
		return this.text1;
	}
	public void setText1(String text1) {
		this.text1 = text1;
	}

	@Column(name = "text10")
	public String getText10() {
		return this.text10;
	}
	public void setText10(String text10) {
		this.text10 = text10;
	}

	@Column(name = "text2")
	public String getText2() {
		return this.text2;
	}
	public void setText2(String text2) {
		this.text2 = text2;
	}

	@Column(name = "text3")
	public String getText3() {
		return this.text3;
	}
	public void setText3(String text3) {
		this.text3 = text3;
	}

	@Column(name = "text4")
	public String getText4() {
		return this.text4;
	}
	public void setText4(String text4) {
		this.text4 = text4;
	}

	@Column(name = "text5")
	public String getText5() {
		return this.text5;
	}
	public void setText5(String text5) {
		this.text5 = text5;
	}

	@Column(name = "text6")
	public String getText6() {
		return this.text6;
	}
	public void setText6(String text6) {
		this.text6 = text6;
	}

	@Column(name = "text7")
	public String getText7() {
		return this.text7;
	}
	public void setText7(String text7) {
		this.text7 = text7;
	}

	@Column(name = "text8")
	public String getText8() {
		return this.text8;
	}
	public void setText8(String text8) {
		this.text8 = text8;
	}

	@Column(name = "text9")
	public String getText9() {
		return this.text9;
	}
	public void setText9(String text9) {
		this.text9 = text9;
	}

	@Column(name = "urgent_level")
	public String getUrgentLevel() {
		return this.urgentLevel;
	}
	public void setUrgentLevel(String urgentLevel) {
		this.urgentLevel = urgentLevel;
	}

	@Column(name = "string1")
	public String getVarchar1() {
		return this.varchar1;
	}
	public void setVarchar1(String varchar1) {
		this.varchar1 = varchar1;
	}

	@Column(name = "string10")
	public String getVarchar10() {
		return this.varchar10;
	}
	public void setVarchar10(String varchar10) {
		this.varchar10 = varchar10;
	}

	@Column(name = "string2")
	public String getVarchar2() {
		return this.varchar2;
	}
	public void setVarchar2(String varchar2) {
		this.varchar2 = varchar2;
	}

	@Column(name = "string3")
	public String getVarchar3() {
		return this.varchar3;
	}
	public void setVarchar3(String varchar3) {
		this.varchar3 = varchar3;
	}

	@Column(name = "string4")
	public String getVarchar4() {
		return this.varchar4;
	}
	public void setVarchar4(String varchar4) {
		this.varchar4 = varchar4;
	}

	@Column(name = "string5")
	public String getVarchar5() {
		return this.varchar5;
	}
	public void setVarchar5(String varchar5) {
		this.varchar5 = varchar5;
	}

	@Column(name = "string6")
	public String getVarchar6() {
		return this.varchar6;
	}
	public void setVarchar6(String varchar6) {
		this.varchar6 = varchar6;
	}

	@Column(name = "string7")
	public String getVarchar7() {
		return this.varchar7;
	}
	public void setVarchar7(String varchar7) {
		this.varchar7 = varchar7;
	}

	@Column(name = "string8")
	public String getVarchar8() {
		return this.varchar8;
	}
	public void setVarchar8(String varchar8) {
		this.varchar8 = varchar8;
	}

	@Column(name = "string9")
	public String getVarchar9() {
		return this.varchar9;
	}
	public void setVarchar9(String varchar9) {
		this.varchar9 = varchar9;
	}

	@Column(name = "string11")
	public String getVarchar11() {
		return this.varchar11;
	}
	public void setVarchar11(String varchar11) {
		this.varchar11 = varchar11;
	}

	@Column(name = "string20")
	public String getVarchar20() {
		return this.varchar20;
	}
	public void setVarchar20(String varchar20) {
		this.varchar20 = varchar20;
	}

	@Column(name = "string12")
	public String getVarchar12() {
		return this.varchar12;
	}
	public void setVarchar12(String varchar12) {
		this.varchar12 = varchar12;
	}

	@Column(name = "string13")
	public String getVarchar13() {
		return this.varchar13;
	}
	public void setVarchar13(String varchar13) {
		this.varchar13 = varchar13;
	}

	@Column(name = "string14")
	public String getVarchar14() {
		return this.varchar14;
	}
	public void setVarchar14(String varchar14) {
		this.varchar14 = varchar14;
	}

	@Column(name = "string15")
	public String getVarchar15() {
		return this.varchar15;
	}
	public void setVarchar15(String varchar15) {
		this.varchar15 = varchar15;
	}

	@Column(name = "string16")
	public String getVarchar16() {
		return this.varchar16;
	}
	public void setVarchar16(String varchar16) {
		this.varchar16 = varchar16;
	}

	@Column(name = "string17")
	public String getVarchar17() {
		return this.varchar17;
	}
	public void setVarchar17(String varchar17) {
		this.varchar17 = varchar17;
	}

	@Column(name = "string18")
	public String getVarchar18() {
		return this.varchar18;
	}
	public void setVarchar18(String varchar18) {
		this.varchar18 = varchar18;
	}

	@Column(name = "string19")
	public String getVarchar19() {
		return this.varchar19;
	}
	public void setVarchar19(String varchar19) {
		this.varchar19 = varchar19;
	}

	//bi-directional many-to-one association to EdocBody
	public java.util.Set<EdocBody> getEdocBodies() {
		if (this.edocBodies == null){
			this.edocBodies = new HashSet<EdocBody>();
			List<EdocBody> bodys = GovdocHelper.getEdocBodys(this.getId());
			if(Strings.isNotEmpty(bodys)) {
				edocBodies.addAll(bodys);
			}
		}
		return this.edocBodies;
	}
	public void setEdocBodies(java.util.Set<EdocBody> edocBodies) {
		this.edocBodies = edocBodies;
	}

	public String getEdocBodiesJs()
	{
		StringBuilder sb=new StringBuilder();

		sb.append("\r\n var contentOfficeId=new Properties();\r\n");
		if (this.edocBodies != null && this.edocBodies.size() > 0)
		{
			EdocBody eb=null;
			Iterator<EdocBody> itor=edocBodies.iterator();
			for(int i=0;i<edocBodies.size();i++)
			{
				eb=itor.next();
				if(eb != null){
				    if(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML.equals(eb.getContentType()))
	                {
	                    continue;
	                }
	                if(eb.getContent()==null)continue;
	                sb.append("contentOfficeId.put('").append(eb.getContentNo().toString()).append("','").append(eb.getContent()).append("');\r\n");
				}
			}
		}
		return sb.toString();
	}

	public EdocBody getFirstBody() {
		if (this.edocBodies == null || this.edocBodies.size() == 0) {
			return GovdocHelper.getFirstBody(this.getId());
		}
		EdocBody eb=null;
		EdocBody teb=null;
		Iterator<EdocBody> it=this.edocBodies.iterator();
		//兼容老公文，做了回退或者待发列表发送后，edocbody会新增出现多条记录，确保正文内容最新修改的
		Timestamp  tempDate = null;
		while(it.hasNext())
		{
			eb=it.next();
			if(eb != null && eb.getContentNo()==0){
				if(tempDate == null || (tempDate != null && tempDate.before(eb.getLastUpdate()))){
					tempDate = eb.getLastUpdate();
					teb = eb;
				}
			}
		}
		return teb;
	}


	public EdocBody getBody(int bodyNum) {
		if (this.edocBodies == null || this.edocBodies.size() == 0)
			return null;
		EdocBody eb=null,ebTemp;
		Iterator<EdocBody> it=this.edocBodies.iterator();
		while(it.hasNext())
		{
			ebTemp=it.next();
			if(ebTemp != null && ebTemp.getContentNo()==bodyNum)
			{
				eb=ebTemp;
				break;
			}
		}
		return eb;
	}

	//bi-directional many-to-one association to EdocOpinion
	public java.util.Set<EdocOpinion> getEdocOpinions() {
		if(this.edocOpinions==null){this.edocOpinions=new HashSet<EdocOpinion>();}
		return this.edocOpinions;
	}
	public void setEdocOpinions(java.util.Set<EdocOpinion> edocOpinions) {
		this.edocOpinions = edocOpinions;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.toString();
	}
	/*删除处理意见，不包括发起人意见*/
	public void delDowithOpinon()
	{
		java.util.Set<EdocOpinion> opinions=getEdocOpinions();
		for(EdocOpinion op:opinions)
		{
			if(op.getOpinionType()!=EdocOpinion.OpinionType.senderOpinion.ordinal())
			{
				opinions.remove(op);
			}
		}
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

	public java.sql.Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(java.sql.Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Boolean getWorklfowTimeout() {
		return worklfowTimeout;
	}

	public void setWorklfowTimeout(Boolean worklfowTimeout) {
		this.worklfowTimeout = worklfowTimeout;
	}

	public void checkSendUnitData()
	{
		//校验sendUnitId数据是否正确
        if(!Strings.isBlank(getSendUnitId()))
        {
        	if(getSendUnitId().indexOf("|")<0){setSendUnitId("Account|"+getSendUnitId());}
        }
        if(!Strings.isBlank(getSendUnitId2()))
        {
           	if(getSendUnitId2().indexOf("|")<0){setSendUnitId2("Account|"+getSendUnitId2());}
        }
	}

	@Column(name = "important_level")
	public Integer getImportantLevel() {
		return importantLevel;
	}

	public void setImportantLevel(Integer importantLevel) {
		this.importantLevel = importantLevel;
	}

	@Column(name = "varchar21")
	public String getVarchar21() {
		return varchar21;
	}

	public void setVarchar21(String varchar21) {
		this.varchar21 = varchar21;
	}

	@Column(name = "varchar22")
	public String getVarchar22() {
		return varchar22;
	}

	public void setVarchar22(String varchar22) {
		this.varchar22 = varchar22;
	}

	@Column(name = "varchar23")
	public String getVarchar23() {
		return varchar23;
	}

	public void setVarchar23(String varchar23) {
		this.varchar23 = varchar23;
	}

	@Column(name = "varchar24")
	public String getVarchar24() {
		return varchar24;
	}

	public void setVarchar24(String varchar24) {
		this.varchar24 = varchar24;
	}

	@Column(name = "varchar25")
	public String getVarchar25() {
		return varchar25;
	}

	public void setVarchar25(String varchar25) {
		this.varchar25 = varchar25;
	}

	@Column(name = "varchar26")
	public String getVarchar26() {
		return varchar26;
	}

	public void setVarchar26(String varchar26) {
		this.varchar26 = varchar26;
	}

	@Column(name = "varchar27")
	public String getVarchar27() {
		return varchar27;
	}

	public void setVarchar27(String varchar27) {
		this.varchar27 = varchar27;
	}

	@Column(name = "varchar28")
	public String getVarchar28() {
		return varchar28;
	}

	public void setVarchar28(String varchar28) {
		this.varchar28 = varchar28;
	}

	@Column(name = "varchar29")
	public String getVarchar29() {
		return varchar29;
	}

	public void setVarchar29(String varchar29) {
		this.varchar29 = varchar29;
	}

	@Column(name = "varchar30")
	public String getVarchar30() {
		return varchar30;
	}

	public void setVarchar30(String varchar30) {
		this.varchar30 = varchar30;
	}

	@Column(name = "text11")
	public String getText11() {
		return text11;
	}

	public void setText11(String text11) {
		this.text11 = text11;
	}

	@Column(name = "text12")
	public String getText12() {
		return text12;
	}

	public void setText12(String text12) {
		this.text12 = text12;
	}

	@Column(name = "text13")
	public String getText13() {
		return text13;
	}

	public void setText13(String text13) {
		this.text13 = text13;
	}

	@Column(name = "text14")
	public String getText14() {
		return text14;
	}

	public void setText14(String text14) {
		this.text14 = text14;
	}

	@Column(name = "text15")
	public String getText15() {
		return text15;
	}

	public void setText15(String text15) {
		this.text15 = text15;
	}
	public Long getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}
	public String getPrintUnitId() {
		return printUnitId;
	}
    private int isRetreat;//是否被退回

    public int getIsRetreat() {
		return isRetreat;
	}

	public void setIsRetreat(int isRetreat) {
		this.isRetreat = isRetreat;
	}
	public void setPrintUnitId(String printUnitId) {
		this.printUnitId = printUnitId;
	}
	public void bind(EdocRegister e) {
		  this.setIdIfNew();
		  this.setIdentifier(e.getIdentifier()==null?"00000000000000000000":e.getIdentifier());
		  this.setEdocType(e.getEdocType());
		  this.setCreatePerson(e.getCreateUserName());
		  this.setCreateTime(e.getCreateTime()==null?new java.sql.Timestamp(new java.util.Date().getTime()) :e.getCreateTime());
		  this.setSendUnit(e.getSendUnit()==null?"":e.getSendUnit());
		  
		  Long rSendUnitId = e.getSendUnitId();
		  if(rSendUnitId != null){
		      this.setSendUnitId("Account|" + rSendUnitId);
		  }
		  
		  if(e.getSendType()!=null){
			  this.setSendType(e.getSendType());
		  }
		  //branches_a8_v350_r_gov GOV-653 于荒津修改签发人显示-1问题 start
		  this.setIssuer(e.getIssuer());
		  //branches_a8_v350_r_gov GOV-653 于荒津修改签发人显示-1问题 end
		  this.setSubject(e.getSubject()==null?"":e.getSubject());
		  if(e.getDocType()!=null){
			  this.setDocType(e.getDocType());
		  }
		  this.setDocMark(e.getDocMark()==null?"":e.getDocMark());
		  this.setSerialNo(e.getSerialNo()==null?"":e.getSerialNo());
		  if(e.getSecretLevel()!=null){
			  this.setSecretLevel(e.getSecretLevel());
		  }
		  if(e.getUrgentLevel()!=null){
			  this.setUrgentLevel(e.getUrgentLevel());
		  }
		  if(e.getKeepPeriod()!=null && "null".equals(e.getKeepPeriod())){
			  this.setKeepPeriod(Integer.valueOf(e.getKeepPeriod()));
		  }
		  this.setSendTo(e.getSendTo()==null?"":e.getSendTo());
		  this.setSendToId(e.getSendToId()==null?"":e.getSendToId());
		  this.setCopyTo(e.getCopyTo()==null? "":e.getCopyTo());
		  this.setCopyToId(e.getCopyToId()==null?"":e.getCopyToId());
		  this.setKeywords(e.getKeywords()==null?"":e.getKeywords());
		  this.setCopies(e.getCopies());
		  this.setState(EdocConstant.flowState.run.ordinal());
		  this.setOrgAccountId(e.getOrgAccountId());
		  this.setDocType("1");
		  this.setSendType("1");
	}

	public void setSendDepartment(String sendDepartment) {
		this.sendDepartment = sendDepartment;
	}
	@Column(name = "send_department")
	public String getSendDepartment() {
		return sendDepartment;
	}

	public void setSendDepartment2(String sendDepartment2) {
		this.sendDepartment2 = sendDepartment2;
	}
	@Column(name = "send_department2")
	public String getSendDepartment2() {
		return sendDepartment2;
	}

	public void setSendDepartmentId(String sendDepartmentId) {
		this.sendDepartmentId = sendDepartmentId;
	}
	public String getSendDepartmentId() {
		return sendDepartmentId;
	}
	public void setSendDepartmentId2(String sendDepartmentId2) {
		this.sendDepartmentId2 = sendDepartmentId2;
	}
	public String getSendDepartmentId2() {
		return sendDepartmentId2;
	}
	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}
	public String getAttachments() {
		return attachments;
	}
	public Long getOverTime() {
		return overTime;
	}
	public void setOverTime(Long overTime) {
		this.overTime = overTime;
	}
	public Long getRunTime() {
		return runTime;
	}
	public void setRunTime(Long runTime) {
		this.runTime = runTime;
	}
	public Long getOverWorkTime() {
		return overWorkTime;
	}
	public void setOverWorkTime(Long overWorkTime) {
		this.overWorkTime = overWorkTime;
	}
	public Long getRunWorkTime() {
		return runWorkTime;
	}
	public void setRunWorkTime(Long runWorkTime) {
		this.runWorkTime = runWorkTime;
	}

	public java.lang.Boolean getCoverTime() {
		return coverTime;
	}
	public void setCoverTime(java.lang.Boolean coverTime) {
		this.coverTime = coverTime;
	}
	
	/**
	 * 
	 * @Author      : xuqiangwei
	 * @Date        : 2015年1月11日下午11:42:31
	 * @param object
	 * @param summary
	 * @param affair
	 * @return 返回解析到数组的第几个
	 */
	public int make(Object[] object, EdocSummary summary, CtpAffair affair) {
		int n = 0;
		summary.setId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		summary.setStartUserId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
        summary.setCaseId(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
        summary.setCompleteTime((Timestamp)object[n++]);
        summary.setSubject((String)object[n++]);
        summary.setSecretLevel((String)object[n++]);
        summary.setIdentifier((String)object[n++]);
        summary.setDocMark((String)object[n++]);
        summary.setSerialNo((String)object[n++]);
        summary.setCreateTime((Timestamp)object[n++]);
        summary.setSendTo((String)object[n++]);
        summary.setIssuer((String)object[n++]);
        summary.setSigningDate(trans2SqlDate(object[n++]));
        summary.setDeadline(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
        summary.setDeadlineDatetime((java.util.Date)object[n++]);
        summary.setStartTime((Timestamp)object[n++]);
        summary.setCopies(object[n] == null? null : ((Number)object[n]).intValue());
        n++;
        summary.setCreatePerson((String)object[n++]);
        summary.setSendUnit((String)object[n++]);
        summary.setSendDepartment((String)object[n++]);
        summary.setHasArchive((Boolean)object[n++]);
        summary.setProcessId((String)object[n++]);
        
        summary.setCaseId(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
        summary.setUrgentLevel((String)object[n++]);
        summary.setTempleteId(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
        summary.setState(object[n] == null? null : ((Number)object[n]).intValue());
        n++;
        summary.setCopyTo((String)object[n++]);
        summary.setReportTo((String)object[n++]);
        summary.setArchiveId(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
        summary.setEdocType(object[n] == null? null : ((Number)object[n]).intValue());
        n++;
        summary.setDocMark2((String)object[n++]);
        summary.setSendTo2((String)object[n++]);
        summary.setDocType((String)object[n++]);
        summary.setSendType((String)object[n++]);
        summary.setIsQuickSend((Boolean)object[n++]);
        summary.setCurrentNodesInfo((String)object[n++]);
        
        affair.setId(object[n] == null? null : ((Number)object[n]).longValue());
        n++;
		affair.setState(object[n] == null? null : ((Number)object[n]).intValue());
		n++;
		affair.setSubState(object[n] == null? null : ((Number)object[n]).intValue());
		n++;
		affair.setTrack(object[n] == null? null : ((Number)object[n]).intValue());
		n++;
		affair.setHastenTimes(object[n] == null? null : ((Number)object[n]).intValue());
		n++;
		affair.setCoverTime((Boolean)object[n++]);
		affair.setRemindDate(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setDeadlineDate(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setReceiveTime((Timestamp)object[n++]);
		affair.setCompleteTime((Timestamp)object[n++]);
		affair.setCreateDate((Timestamp)object[n++]);
		affair.setMemberId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setBodyType((String)object[n++]);
		affair.setTransactorId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setUpdateDate((Timestamp)object[n++]);
		affair.setExtProps(((String)object[n++]));
		String nodePolicy=(String)object[n++];
		if(nodePolicy != null){nodePolicy=nodePolicy.replaceAll(new String(new char[]{(char)160}), " ");}
		affair.setNodePolicy(nodePolicy);
		affair.setSubject(summary.getSubject());
		
		affair.setSubObjectId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setExpectedProcessTime((Timestamp)object[n++]);
		affair.setActivityId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setFromId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		affair.setBackFromId(object[n] == null? null : ((Number)object[n]).longValue());
		n++;
		return n;
	}
	public void make(Object[] object, EdocSummary summary)
	{
		int n = 0;
		summary.setId((Long)object[n++]);
		summary.setStartUserId((Long)object[n++]);
        summary.setCaseId((Long)object[n++]);
        summary.setCompleteTime((Timestamp)object[n++]);
        summary.setSubject((String)object[n++]);
        summary.setSecretLevel((String)object[n++]);
        summary.setIdentifier((String)object[n++]);
        summary.setDocMark((String)object[n++]);
        summary.setSerialNo((String)object[n++]);
        summary.setCreateTime((Timestamp)object[n++]);
        summary.setSendTo((String)object[n++]);
        summary.setIssuer((String)object[n++]);
        summary.setSigningDate(trans2SqlDate(object[n++]));
        summary.setDeadline((Long)object[n++]);
        summary.setDeadlineDatetime((java.util.Date)object[n++]);
        summary.setStartTime((Timestamp)object[n++]);
        summary.setCopies((Integer)object[n++]);
        summary.setCreatePerson((String)object[n++]);
        summary.setSendUnit((String)object[n++]);
        summary.setSendDepartment((String)object[n++]);
        summary.setHasArchive((Boolean)object[n++]);
        summary.setProcessId((String)object[n++]);
        summary.setCaseId((Long)object[n++]);
        summary.setUrgentLevel((String)object[n++]);
        summary.setTempleteId((Long)object[n++]);
        summary.setState((Integer)object[n++]);
        summary.setCopyTo((String)object[n++]);
        summary.setReportTo((String)object[n++]);
        summary.setArchiveId((Long)object[n++]);
        summary.setEdocType((Integer)object[n++]);
        summary.setDocMark2((String)object[n++]);
        summary.setSendTo2((String)object[n++]);
        summary.setDocType((String)object[n++]);
        summary.setSendType((String)object[n++]);
        summary.setCurrentNodesInfo((String)object[n++]);
        summary.setIsQuickSend((Boolean)object[n++]);

	}
	
	
	private static Date trans2SqlDate(Object date){
        Date ret = null;
        if(date != null){
            if(date instanceof Date){
                ret = (Date)date;
            }else{
                ret = new Date(((java.util.Date)date).getTime());
            }
        }
        return ret;
    }
	
	public java.util.Date getDeadlineDatetime() {
		return deadlineDatetime;
	}
	public void setDeadlineDatetime(java.util.Date deadlineDatetime) {
		this.deadlineDatetime = deadlineDatetime;
	}

	private Set<EdocSendRecord> edocSendRecords;

	public Set<EdocSendRecord> getEdocSendRecords() {
		return edocSendRecords;
	}
	public void setEdocSendRecords(Set<EdocSendRecord> edocSendRecords) {
		this.edocSendRecords = edocSendRecords;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}

	public void setUndertakenoffice(String undertakenoffice) {
        this.undertakenoffice = undertakenoffice;
    }
	
	@Column(name = "undertakenoffice")
	public String getUndertakenoffice() {
        return undertakenoffice;
    }
	
	public void setUndertakenofficeId(String undertakenofficeId) {
        this.undertakenofficeId = undertakenofficeId;
    }
	
	@Column(name = "undertakenoffice_id")
	public String getUndertakenofficeId() {
        return undertakenofficeId;
    }
	
	@Column(name = "unit_level")
    public String getUnitLevel() {
        return unitLevel;
    }
    public void setUnitLevel(String unitLevel) {
        this.unitLevel = unitLevel;
    }
    /**
     *  父协同ID 
     */
    public java.lang.Long getParentformSummaryid () {
      return parentformSummaryid;
    }

    /**
     *  父协同ID 
     * @param _parentformSummaryid the PARENTFORM_SUMMARYID value
     */
    public void setParentformSummaryid (java.lang.Long _parentformSummaryid) {
      this.parentformSummaryid = _parentformSummaryid;
    }
    /**
     *  能否修改正文 
     */
    public java.lang.Boolean getCanEdit () {
      return canEdit == null ? false : canEdit;
    }

    /**
     *  能否修改正文 
     * @param _canEdit the CAN_EDIT value
     */
    public void setCanEdit (java.lang.Boolean _canEdit) {
      this.canEdit = _canEdit;
    }
    /**
     *  是否能归档 
     */
    public java.lang.Boolean getCanArchive () {
      return canArchive == null ? false : canArchive;
    }
    /**
     *  是否能归档 
     * @param _canArchive the CAN_ARCHIVE value
     */
    public void setCanArchive (java.lang.Boolean _canArchive) {
      this.canArchive = _canArchive;
    }


    /**
     *  关联项目ID 
     */
    public java.lang.Long getProjectId () {
      return projectId;
    }

    /**
     *  关联项目ID 
     * @param _projectId the PROJECT_ID value
     */
    public void setProjectId (java.lang.Long _projectId) {
      this.projectId = _projectId;
    }
    /**
     *  能否编辑附件 
     */
    public java.lang.Boolean getCanEditAttachment () {
      return canEditAttachment == null ? false : canEditAttachment;
    }

    /**
     *  能否编辑附件 
     * @param _canEditAttachment the CAN_EDIT_ATTACHMENT value
     */
    public void setCanEditAttachment (java.lang.Boolean _canEditAttachment) {
      this.canEditAttachment = _canEditAttachment;
    }
    /**
     *  是否能修改 流程
     */
    public java.lang.Boolean getCanModify () {
      return canModify == null ? false : canModify;
    }

    /**
     *  是否能修改 流程
     * @param _canModify the CAN_MODIFY value
     */
    public void setCanModify (java.lang.Boolean _canModify) {
      this.canModify = _canModify;
    }
    /**
     *  是否能转发 
     */
    public java.lang.Boolean getCanForward () {
      return canForward == null ? false : canForward;
    }

    /**
     *  是否能转发 
     * @param _canForward the CAN_FORWARD value
     */
    public void setCanForward (java.lang.Boolean _canForward) {
      this.canForward = _canForward;
    }
    public String getAdvancePigeonhole() {
  		return advancePigeonhole;
	}
	
	public void setAdvancePigeonhole(String advancePigeonhole) {
		this.advancePigeonhole = advancePigeonhole;
	}
	public Integer getGovdocType() {
		if (govdocType == null){
			return 0;
		}
		return govdocType;
	}
	public void setGovdocType(Integer govdocType) {
		if(govdocType == null){
			this.govdocType = 0;
		}else{
			this.govdocType = govdocType;
		}
	}
	/**
	 * 发起人
	 */
	public java.lang.Long getStartMemberId() {
		return startUserId;
	}

	/**
	 * 发起人
	 * 
	 * @param _startMemberId
	 *            the START_MEMBER_ID value
	 */
	public void setStartMemberId(java.lang.Long _startMemberId) {
		this.startMemberId = _startMemberId;
	}
	public Integer getJianbanType() {
		return jianbanType;
	}
	public void setJianbanType(Integer jianbanType) {
		this.jianbanType = jianbanType;
	}
	/**
	   *  是否周期提醒 
	   */
	  public java.lang.Boolean getCanDueReminder () {
	    return canDueReminder;
	  }

	  /**
	   *  是否周期提醒 
	   * @param _canDueReminder the CAN_DUE_REMINDER value
	   */
	  public void setCanDueReminder (java.lang.Boolean _canDueReminder) {
	    this.canDueReminder = _canDueReminder;
	  }
	public java.lang.Long get_parentformSummaryid() {
		return parentformSummaryid;
	}
	public void set_parentformSummaryid(java.lang.Long _parentformSummaryid) {
		this.parentformSummaryid = _parentformSummaryid;
	}
	public java.lang.Boolean get_canEdit() {
		return canEdit;
	}
	public void set_canEdit(java.lang.Boolean _canEdit) {
		this.canEdit = _canEdit;
	}
	public java.lang.Boolean get_canArchive() {
		return canArchive;
	}
	public void set_canArchive(java.lang.Boolean _canArchive) {
		this.canArchive = _canArchive;
	}
	public java.lang.Long get_projectId() {
		return projectId;
	}
	public void set_projectId(java.lang.Long _projectId) {
		this.projectId = _projectId;
	}
	public java.lang.Boolean get_canModify() {
		return canModify;
	}
	public void set_canModify(java.lang.Boolean _canModify) {
		this.canModify = _canModify;
	}
	public java.lang.Boolean get_canForward() {
		return canForward;
	}
	public void set_canForward(java.lang.Boolean _canForward) {
		this.canForward = _canForward;
	}
	public java.lang.Long get_startMemberId() {
		return startMemberId;
	}
	public void set_startMemberId(java.lang.Long _startMemberId) {
		this.startMemberId = _startMemberId;
	}
	public boolean is_canDueReminder() {
		return canDueReminder;
	}
	public void set_canDueReminder(boolean _canDueReminder) {
		this.canDueReminder = _canDueReminder;
	}
	public Integer getEdocSecretLevel() {
		return edocSecretLevel;
	}
	public void setEdocSecretLevel(Integer edocSecretLevel) {
		this.edocSecretLevel = edocSecretLevel;
	}
	public int getTransferStatus() {
		return transferStatus;
	}
	public void setTransferStatus(int transferStatus) {
		this.transferStatus = transferStatus;
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
	public Boolean getToEdocLibFlag() {
		if (toEdocLibFlag == null) {
			return false;
		}
		return toEdocLibFlag;
	}
	public void setToEdocLibFlag(Boolean toEdocLibFlag) {
		this.toEdocLibFlag = toEdocLibFlag;
	}
	public String getToEdocLibType() {
		return toEdocLibType;
	}
	public void setToEdocLibType(String toEdocLibType) {
		this.toEdocLibType = toEdocLibType;
	}
	public java.sql.Timestamp getSendDate() {
		return sendDate;
	}
	public void setSendDate(java.sql.Timestamp sendDate) {
		this.sendDate = sendDate;
	}
	public String getPublicInfo() {
		return publicInfo;
	}
	public void setPublicInfo(String publicInfo) {
		this.publicInfo = publicInfo;
	}
	public String getReceiveUnit() {
		return receiveUnit;
	}
	public void setReceiveUnit(String receiveUnit) {
		this.receiveUnit = receiveUnit;
	}
	public String getReceiveUnitId() {
		return receiveUnitId;
	}
	public void setReceiveUnitId(String receiveUnitId) {
		this.receiveUnitId = receiveUnitId;
	}
	public String getCheckPerson() {
		return checkPerson;
	}
	public void setCheckPerson(String checkPerson) {
		this.checkPerson = checkPerson;
	}
	public java.util.Date getProxyDate() {
		return proxyDate;
	}
	public void setProxyDate(java.util.Date proxyDate) {
		this.proxyDate = proxyDate;
	}
	public Integer getPishiNo() {
		return pishiNo;
	}
	public void setPishiNo(Integer pishiNo) {
		this.pishiNo = pishiNo;
	}
	public String getPishiName() {
		return pishiName;
	}
	public void setPishiName(String pishiName) {
		this.pishiName = pishiName;
	}
	public String getPishiYear() {
		return pishiYear;
	}
	public void setPishiYear(String pishiYear) {
		this.pishiYear = pishiYear;
	}
	public String getPishiLeaderName() {
		return pishiLeaderName;
	}
	public void setPishiLeaderName(String pishiLeaderName) {
		this.pishiLeaderName = pishiLeaderName;
	}
	public void setEdocTypeEnum(String edocTypeEnum) {
		this.edocTypeEnum = edocTypeEnum;
	}
	public void setSenderOpinion(EdocOpinion senderOpinion) {
		this.senderOpinion = senderOpinion;
	}
	public Long getFontSize() {
		return fontSize;
	}
	public void setFontSize(Long fontSize) {
		this.fontSize = fontSize;
	}
	public String getGovcode() {
		return govcode;
	}
	public void setGovcode(String govcode) {
		this.govcode = govcode;
	}
	public java.lang.String getAwakeDate() {
		return awakeDate;
	}
	
	public void setAwakeDate(java.lang.String _awakeDate) {
		this.awakeDate = _awakeDate;
	}

	/**
	 * 是否自动终止流程
	 */
	public java.lang.Boolean getCanAutostopflow() {
		return canAutostopflow;
	}

	/**
	 * 是否自动终止流程
	 * 
	 * @param _canAutostopflow
	 *            the CAN_AUTOSTOPFLOW value
	 */
	public void setCanAutostopflow(java.lang.Boolean _canAutostopflow) {
		this.canAutostopflow = _canAutostopflow;
	}
	public Long getFormAppid() {
		return formAppid;
	}
	public void setFormAppid(Long formAppid) {
		this.formAppid = formAppid;
	}
	public Long getFormRecordid() {
		return formRecordid;
	}
	public void setFormRecordid(Long formRecordid) {
		this.formRecordid = formRecordid;
	}
	public String getBodyType() {
		return bodyType==null ? "" : bodyType;
	}
	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}
	/**
	 * 是否核定0：默认值;1核定通过;2核定不能过
	 */
	public java.lang.Integer getVouch() {
		return vouch;
	}

	/**
	 * 是否核定0：默认值;1核定通过;2核定不能过
	 * 
	 * @param _vouch
	 *            the IS_VOUCH value
	 */
	public void setVouch(java.lang.Integer _vouch) {
		this.vouch = _vouch;
	}
	/**
	 * 审核通过/审核不通过
	 */
	public java.lang.Boolean isAudited() {
		if (audited == null) {
			return false;
		}
		return audited;
	}

	/**
	 * 审核通过/审核不通过
	 * 
	 * @param _audited
	 *            the IS_AUDITED value
	 */
	public void setAudited(java.lang.Boolean _audited) {
		this.audited = _audited;
	}
	public Boolean getIsCustomDealWith() {
		return isCustomDealWith;
	}
	public void setIsCustomDealWith(Boolean isCustomDealWith) {
		this.isCustomDealWith = isCustomDealWith;
	}
	public String getCustomDealWithNode() {
		return customDealWithNode;
	}
	public void setCustomDealWithNode(String customDealWithNode) {
		this.customDealWithNode = customDealWithNode;
	}
	public String getCustomDealWithMemberId() {
		return customDealWithMemberId;
	}
	public void setCustomDealWithMemberId(String customDealWithMemberId) {
		this.customDealWithMemberId = customDealWithMemberId;
	}
	public String getCustomDealWithMemberName() {
		return customDealWithMemberName;
	}
	public void setCustomDealWithMemberName(String customDealWithMemberName) {
		this.customDealWithMemberName = customDealWithMemberName;
	}
	public java.lang.String getSupervisorsId() {
		return supervisorsId;
	}
	public void setSupervisorsId(java.lang.String _supervisorsId) {
		this.supervisorsId = _supervisorsId;
	}
	public java.lang.Boolean getUpdateSubject() {
		return updateSubject;
	}

	public void setUpdateSubject(java.lang.Boolean updateSubject) {
		this.updateSubject = updateSubject;
	}
	
	public Boolean getCanAnyMerge() {
        if (canAnyMerge == null) {
            return Boolean.FALSE;
        }
        return canAnyMerge;
    }

    public void setCanAnyMerge(Boolean canAnyMerge) {
        this.canAnyMerge = canAnyMerge;
    }

	public Boolean getCanMergeDeal() {
		if (canMergeDeal == null) {
			return Boolean.FALSE;
		}
		return canMergeDeal;
	}

	public void setCanMergeDeal(Boolean isMergeDeal) {
		this.canMergeDeal = isMergeDeal;
	}

	/**
	 * 重复次数
	 */
	public java.lang.Integer getResentTime() {
		return resentTime;
	}

	/**
	 * 重复次数
	 * 
	 * @param _resentTime
	 *            the RESENT_TIME value
	 */
	public void setResentTime(java.lang.Integer _resentTime) {
		this.resentTime = _resentTime;
	}

	/**
	 * 新流程类型
	 */
	public java.lang.Integer getNewflowType() {
		if (newflowType == null) {
			return EdocConstant.NewflowType.main.ordinal();
		} else {
			return newflowType;
		}
	}

	/**
	 * 新流程类型
	 * 
	 * @param _newflowType
	 *            the NEWFLOW_TYPE value
	 */
	public void setNewflowType(java.lang.Integer _newflowType) {
		this.newflowType = _newflowType;
	}

	public Long getExchangeSendAffairId() {
		return exchangeSendAffairId;
	}

	public void setExchangeSendAffairId(Long exchangeSendAffairId) {
		this.exchangeSendAffairId = exchangeSendAffairId;
	}

	public java.lang.Long getAttachmentArchiveId() {
		return attachmentArchiveId;
	}

	public void setAttachmentArchiveId(java.lang.Long attachmentArchiveId) {
		this.attachmentArchiveId = attachmentArchiveId;
	}

	public String getDynamicSubject() {
		return dynamicSubject;
	}
	public void setDynamicSubject(String dynamicSubject) {
		this.dynamicSubject = dynamicSubject;
	}
	public Long getWorkitemId() {
		return workitemId;
	}
	public void setWorkitemId(Long workitemId) {
		this.workitemId = workitemId;
	}
	public Integer getExchangeType() {
		return exchangeType;
	}
	public void setExchangeType(Integer exchangeType) {
		this.exchangeType = exchangeType;
	}
	public Integer getFromType() {
		if(fromType == null) {
			fromType = GovdocEnum.GovdocFromTypeEnum.inner.ordinal();
		}
		return fromType;
	}
	public void setFromType(Integer fromType) {
		this.fromType = fromType;
	}

	public java.lang.String getMergeDealType() {
		if(Strings.isBlank(mergeDealType)){
			Map<String,String> map = new HashMap<String,String>();
			if(getCanMergeDeal()){
				map.put("PRE_DEAL_MERGE", "3");
			}
			if(getCanAnyMerge()){
				map.put("START_MERGE", "2");
				map.put("DEAL_MERGE", "4");
			}
			return JSONUtil.toJSONString(map);
		}
		return mergeDealType;
	}

	public void setMergeDealType(java.lang.String mergeDealType) {
		this.mergeDealType = mergeDealType;
	}
	public java.lang.Integer getProcessTermType() {
		return processTermType;
	}

	public void setProcessTermType(java.lang.Integer processTermType) {
		this.processTermType = processTermType;
	}

	public java.lang.Long getRemindInterval () {
		return remindInterval;
	}

	public void setRemindInterval (java.lang.Long remindInterval) {
		this.remindInterval = remindInterval;
	}

	public String getDealSuggestion() {
		return dealSuggestion;
	}

	public void setDealSuggestion(String dealSuggestion) {
		this.dealSuggestion = dealSuggestion;
	}

	public Boolean getAutoRun() {
		if(newflowType != null 
				&& (newflowType == EdocConstant.NewflowType.child.ordinal()
				|| newflowType == EdocConstant.NewflowType.auto.ordinal())){
			return true;
		}else{
			return false;
		}
		
	}
	public void setAutoRun(Boolean autoRun) {
		this.autoRun = autoRun;
	}
	
}
