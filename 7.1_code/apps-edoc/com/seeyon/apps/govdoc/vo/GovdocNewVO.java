package com.seeyon.apps.govdoc.vo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.WFInfo;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.organization.bo.V3xOrgMember;

/**
 * 公文新建界面/公文发送保存VO参数对象
 * @author tanggl
 *
 */
public class GovdocNewVO extends GovdocBaseVO {
	
	private List<ProcessLogDetail> processLogDetails;
	
	private boolean isNew = false;//是否新建公文
	
	private Long parentSummaryId;
	
	private Long caseId;//流程實例ID---caseId
	
    private boolean canEditColPigeonhole;//是否能编辑预归档路径
	
    private String senderOpinionContent;  
     
    private boolean canDeleteOriginalAtts = true;
    
    private boolean cloneOriginalAtts;
    
    private Long archiveId;
    
    private String archiveName = "";
    
    private String archiveAllName;
    
    private  Long projectId ;
    
    private Date createDate;
    
    private String deadLineDateTimeHidden;
    
    private String subjectForCopy;
    
    private Long attachmentArchiveId;
    
    private String advancePigeonhole;//预归档
    
    private CtpSuperviseDetail colSupervise;
    private List<Attachment> atts;//附件集合
    private List<ProjectBO> projectList;
    	
	private boolean parentWrokFlowTemplete; //父模板是流程模板
	
	private boolean parentTextTemplete;//父模板是格式模板
	
	private boolean parentColTemplete;//父模板是协同模板
	
	private boolean fromSystemTemplete;//父模板是来源于系统模板
	
	private String forShow;//用于督办的回显展现
	
	//跟踪信息
	private String forGZShow;//用于更总的回显展示
	private String forGZIds;//用于跟踪回显
	private Integer trackType;
	
	private String colSupervisors;
	private String superviseDate;
	private String colSupervisorNames;
	private String trackIds;	
	//流程ID
	private String processId;
	private boolean readOnly;
	private boolean fromTemplate;
	private String attListJSON;
	
	//调用模板的一些信息
	private boolean collSubjectNotEdit;
	private String collSubject;	
	private String temformParentId;	
	private String formtitle;	
	private String standardDuration;
	
	private String wfXMLInfo;
	
	private boolean sVisorsFromTemplate;
	private boolean noDepManager;
	
	public String unCancelledVisor;
	private boolean form;
	private boolean systemTemplate;
	private boolean resendFlag ;
	private boolean templeteHasDeadline;
	private boolean templeteHasRemind;
	private int hideContentType;
	private String formOperationId;
	private List<CtpEnumItem> secretLevelList;
	private String xml;
	private String workflowNodesInfo;
	private Long signSummaryId;
	private int fenbanFile;
	private List<CtpTemplateCategory> edocCategoryList;
	private String formList;
	private Long defaultFormId;
	private Long defaultCategoryId;
	private String disabledProjectId;
	private String forwardText;
	private int contentViewState;
	private long uuidlong;
	private Long zwModuleId;
	private String zwRightId;
	private boolean zwIsnew;
	private int zwViewState;
	private boolean isGovdocTemplate;
	private String forwardAffairId;
	private Long forwardSummaryId;
	private String govdocRelation1;
	private String govdocRelation2;
	private ContentViewRet contentContext;
	private boolean onlyViewWF;
	private Long formAppId;
	private boolean customSetTrack;
	private ConfigItem govdocViewConfigItem;
	private int allowCommentInForm;
	private String noselfflow;
	private boolean printSet;
	private String mainNum;
	private String copyNum;
	private boolean isGzEdition;
	private boolean isGovdocSystemTemplate;
	private PermissionVO defaultPermission;
	private Long originalContentId;
	private ContentConfig contentCfg;//正文类型
	private String editType;
	
	// 续办参数值
	private boolean customDealWith;
	private boolean customDealWithTemplate = false;
	private Integer returnPermissionsLength;
	private List<PermissionVO> permissions;
	private String customDealWithPermission;
	private String customDealWithMemberId;
	private List<Map<String,Object>> members;
	private String memberJson;
	private String currentPolicyId;
	private String currentPolicyName;
	private boolean notExistChengban;
	private V3xOrgMember nextMember;
	
	//流程期限和提前提醒
	private Long deadLine;
	private Date deadLineTime;
	private Long advanceRemind;
	private List<Comment> commentSenderList;
	
	//转发的标题
	private String subjectFromTrans;
    /**
     * 模板是否设置了流程超期
     */
    private boolean templateHasProcessTermType;
    /**
     * 模板是否设置了流程超期循环提醒
     */
    private boolean templateHasRemindInterval;
    private boolean processTrem;
    private boolean remindInterval;
	/**
	 * 从EdocInfo中合并属性
	 * @return
	 */
	private Comment comment;
	private Comment nibanComment;
	private String processXml;

	// 指定跟踪人员的ID连接字符串
	private String trackMemberId;
	// 是否是删除个人事项操作标记
	private boolean isDelAffair;

	private Long tId;
	private Long curTemId;

	private boolean isBatch;
	private Integer moduleType;

	private String phaseId;
	private Long currentAffairId;
	private Long currentProcessId;
	private boolean modifyFlag;
	
	private String workflowNewflowInput;
	private String workflowNodePeoplesInput;
	private String workflowNodeConditionInput;
	private CtpContentAllBean contentBean;
	private String contentSaveId;
	private String dr;
	private String reMeoToReGo;
	/*
	 * 流程动态路径匹配，底表数据Id,数据结构：底表FormAppId|节点ID|数据记录1ID#数据记录2ID,底表FormAppId|数据记录1ID#
	 * 数据记录2ID 例如：123|1#2#,32|3#4#
	 */
	private String dynamicFormMasterIds;
    
    /**
     * 拟文时控制显示表单还是正文 0正文 其他表单
     */
    private Integer formDefaultShow;

	// 是否制定回退
	private boolean isSpecialBacked;
	private boolean isSpecialBackReturn;
	private Date currentDate;
	private WFInfo wfInfo;

	private Long contentDataId;
	private Long contentTemplateId;
	private Long distributeAffairId;
	
	private GovdocMarkVO markOpenVo;
	private GovdocMarkVO docMarkVo;
	private GovdocMarkVO serialNoVo;
	private GovdocMarkVO signMarkVo;
	
	//转发文自动办结标志
	private String zfwZiDongBanJie;
	
	//新建用户的岗位名称
	private String postName;
	
	//附件说明处理相关
	private String cantEditFilesmName;
	private String filesmFieldName;
	private String filesmFormAttsName;
	private String filesmContentAttsName;
	
	private String subAppName;
	
	/** 合并处理参数值 start*/
	private Boolean canAnyDealMerge;
	private Boolean canPreDealMerge;
	private Boolean canStartMerge;
	/** 合并处理参数值 end*/
	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}

	public String getDynamicFormMasterIds() {
		return dynamicFormMasterIds;
	}

	public void setDynamicFormMasterIds(String dynamicFormMasterIds) {
		this.dynamicFormMasterIds = dynamicFormMasterIds;
	}

	public String getReMeoToReGo() {
		return reMeoToReGo;
	}

	public void setReMeoToReGo(String reMeoToReGo) {
		this.reMeoToReGo = reMeoToReGo;
	}

	private boolean m3Flag = false;

	public boolean isM3Flag() {
		return m3Flag;
	}

	public void setM3Flag(boolean m3Flag) {
		this.m3Flag = m3Flag;
	}

	public String getDR() {
		return dr;
	}

	public void setDR(String dR) {
		dr = dR;
	}

	public String getContentSaveId() {
		return contentSaveId;
	}

	public void setContentSaveId(String contentSaveId) {
		this.contentSaveId = contentSaveId;
	}

	private boolean isTemplateHasPigeonholePath;

	public boolean isTemplateHasPigeonholePath() {
		return isTemplateHasPigeonholePath;
	}

	public void setTemplateHasPigeonholePath(boolean isTemplateHasPigeonholePath) {
		this.isTemplateHasPigeonholePath = isTemplateHasPigeonholePath;
	}

	public void setDelAffair(boolean isDelAffair) {
		this.isDelAffair = isDelAffair;
	}

	public String getWorkflowNewflowInput() {
		return workflowNewflowInput;
	}

	public void setWorkflowNewflowInput(String workflowNewflowInput) {
		this.workflowNewflowInput = workflowNewflowInput;
	}

	public String getWorkflowNodePeoplesInput() {
		return workflowNodePeoplesInput;
	}

	public void setWorkflowNodePeoplesInput(String workflowNodePeoplesInput) {
		this.workflowNodePeoplesInput = workflowNodePeoplesInput;
	}

	public String getWorkflowNodeConditionInput() {
		return workflowNodeConditionInput;
	}

	public void setWorkflowNodeConditionInput(String workflowNodeConditionInput) {
		this.workflowNodeConditionInput = workflowNodeConditionInput;
	}

	public Long getCurTemId() {
		return curTemId;
	}

	public void setCurTemId(Long curTemId) {
		this.curTemId = curTemId;
	}

	public boolean isModifyFlag() {
		return modifyFlag;
	}

	public void setModifyFlag(boolean modifyFlag) {
		this.modifyFlag = modifyFlag;
	}

	public GovdocNewVO() {
		super();
	}

	public String getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(String phaseId) {
		this.phaseId = phaseId;
	}

	public boolean isBatch() {
		return isBatch;
	}
	public void setBatch(boolean isBatch) {
		this.isBatch = isBatch;
	}
	public Long gettId() {
		return tId;
	}
	public void settId(Long tId) {
		this.tId = tId;
	}
	public boolean getIsDelAffair() {
		return isDelAffair;
	}
	public void setIsDelAffair(Boolean isDelAffair) {
		this.isDelAffair = isDelAffair;
	}
	public String getTrackMemberId() {
		return trackMemberId;
	}
	public void setTrackMemberId(String trackMemberId) {
		this.trackMemberId = trackMemberId;
	}
	public String getProcessXml() {
		return processXml;
	}
	public void setProcessXml(String processXml) {
		this.processXml = processXml;
	}
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}

	/**
	 * 
	 * @param content
	 *            发起者附言内容
	 */
	public void setComment(String content) {
		this.comment = new Comment();
		comment.setContent(content);
		comment.setModuleType(ModuleType.edoc.getKey());
		comment.setModifyDate(null);
		comment.setCtype(Comment.CommentType.sender.getKey()); // 发起人附言
		comment.setPath("00");
		comment.setPid(0L);
		comment.setClevel(1);
		comment.setHidden(false);
	}

	public Integer getModuleType() {
		return moduleType;
	}

	public void setModuleType(Integer moduleType) {
		this.moduleType = moduleType;
	}
	public Long getCurrentAffairId() {
		return currentAffairId;
	}
	public void setCurrentAffairId(Long currentAffairId) {
		this.currentAffairId = currentAffairId;
	}
	public Long getCurrentProcessId() {
		return currentProcessId;
	}
	public void setCurrentProcessId(Long currentProcessId) {
		this.currentProcessId = currentProcessId;
	}
	public boolean isSpecialBacked() {
		return isSpecialBacked;
	}
	public void setSpecialBacked(boolean isSpecialBacked) {
		this.isSpecialBacked = isSpecialBacked;
	}
	public Comment getNibanComment() {
		return nibanComment;
	}
	public void setNibanComment(Comment nibanComment) {
		this.nibanComment = nibanComment;
	}
	public String getDr() {
		return dr;
	}
	public void setDr(String dr) {
		this.dr = dr;
	}
	public Date getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	public WFInfo getWfInfo() {
		return wfInfo;
	}
	public void setWfInfo(WFInfo wfInfo) {
		this.wfInfo = wfInfo;
	}
	public boolean isSpecialBackReturn() {
		return isSpecialBackReturn;
	}
	public void setSpecialBackReturn(boolean isSpecialBackReturn) {
		this.isSpecialBackReturn = isSpecialBackReturn;
	}

	
	public Long getDeadLine() {
		return deadLine;
	}
	public void setDeadLine(Long deadLine) {
		this.deadLine = deadLine;
	}
	public Date getDeadLineTime() {
		return deadLineTime;
	}
	public void setDeadLineTime(Date deadLineTime) {
		this.deadLineTime = deadLineTime;
	}
	public Long getAdvanceRemind() {
		return advanceRemind;
	}
	public void setAdvanceRemind(Long advanceRemind) {
		this.advanceRemind = advanceRemind;
	}
	
	public ContentConfig getContentCfg() {
		return contentCfg;
	}
	public void setContentCfg(ContentConfig contentCfg) {
		this.contentCfg = contentCfg;
	}
	public boolean isNew() {
		return isNew;
	}
	public boolean getIsNew() {
		return isNew;
	}
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	public Long getCaseId() {
		return caseId;
	}
	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}
	public boolean isCanEditColPigeonhole() {
		return canEditColPigeonhole;
	}
	public void setCanEditColPigeonhole(boolean canEditColPigeonhole) {
		this.canEditColPigeonhole = canEditColPigeonhole;
	}
	public String getSenderOpinionContent() {
		return senderOpinionContent;
	}
	public void setSenderOpinionContent(String senderOpinionContent) {
		this.senderOpinionContent = senderOpinionContent;
	}
	public boolean isCanDeleteOriginalAtts() {
		return canDeleteOriginalAtts;
	}
	public void setCanDeleteOriginalAtts(boolean canDeleteOriginalAtts) {
		this.canDeleteOriginalAtts = canDeleteOriginalAtts;
	}
	public boolean isCloneOriginalAtts() {
		return cloneOriginalAtts;
	}
	public void setCloneOriginalAtts(boolean cloneOriginalAtts) {
		this.cloneOriginalAtts = cloneOriginalAtts;
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
	public String getArchiveAllName() {
		return archiveAllName;
	}
	public void setArchiveAllName(String archiveAllName) {
		this.archiveAllName = archiveAllName;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getDeadLineDateTimeHidden() {
		return deadLineDateTimeHidden;
	}
	public void setDeadLineDateTimeHidden(String deadLineDateTimeHidden) {
		this.deadLineDateTimeHidden = deadLineDateTimeHidden;
	}
	public String getSubjectForCopy() {
		return subjectForCopy;
	}
	public void setSubjectForCopy(String subjectForCopy) {
		this.subjectForCopy = subjectForCopy;
	}
	public Long getAttachmentArchiveId() {
		return attachmentArchiveId;
	}
	public void setAttachmentArchiveId(Long attachmentArchiveId) {
		this.attachmentArchiveId = attachmentArchiveId;
	}
	public String getAdvancePigeonhole() {
		return advancePigeonhole;
	}
	public void setAdvancePigeonhole(String advancePigeonhole) {
		this.advancePigeonhole = advancePigeonhole;
	}
	
	public CtpSuperviseDetail getColSupervise() {
		return colSupervise;
	}
	public void setColSupervise(CtpSuperviseDetail colSupervise) {
		this.colSupervise = colSupervise;
	}
	public List<Attachment> getAtts() {
		return atts;
	}
	public void setAtts(List<Attachment> atts) {
		this.atts = atts;
	}
	public List<ProjectBO> getProjectList() {
		return projectList;
	}
	public void setProjectList(List<ProjectBO> projectList) {
		this.projectList = projectList;
	}
	public boolean isParentWrokFlowTemplete() {
		return parentWrokFlowTemplete;
	}
	public void setParentWrokFlowTemplete(boolean parentWrokFlowTemplete) {
		this.parentWrokFlowTemplete = parentWrokFlowTemplete;
	}
	public boolean isParentTextTemplete() {
		return parentTextTemplete;
	}
	public void setParentTextTemplete(boolean parentTextTemplete) {
		this.parentTextTemplete = parentTextTemplete;
	}
	public boolean isParentColTemplete() {
		return parentColTemplete;
	}
	public void setParentColTemplete(boolean parentColTemplete) {
		this.parentColTemplete = parentColTemplete;
	}
	public boolean isFromSystemTemplete() {
		return fromSystemTemplete;
	}
	public void setFromSystemTemplete(boolean fromSystemTemplete) {
		this.fromSystemTemplete = fromSystemTemplete;
	}
	public String getForShow() {
		return forShow;
	}
	public void setForShow(String forShow) {
		this.forShow = forShow;
	}
	public String getForGZShow() {
		return forGZShow;
	}
	public void setForGZShow(String forGZShow) {
		this.forGZShow = forGZShow;
	}
	public String getColSupervisors() {
		return colSupervisors;
	}
	public void setColSupervisors(String colSupervisors) {
		this.colSupervisors = colSupervisors;
	}
	public String getSuperviseDate() {
		return superviseDate;
	}
	public void setSuperviseDate(String superviseDate) {
		this.superviseDate = superviseDate;
	}
	public String getColSupervisorNames() {
		return colSupervisorNames;
	}
	public void setColSupervisorNames(String colSupervisorNames) {
		this.colSupervisorNames = colSupervisorNames;
	}
	public String getTrackIds() {
		return trackIds;
	}
	public void setTrackIds(String trackIds) {
		this.trackIds = trackIds;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public boolean isFromTemplate() {
		return fromTemplate;
	}
	public void setFromTemplate(boolean fromTemplate) {
		this.fromTemplate = fromTemplate;
	}
	public String getAttListJSON() {
		return attListJSON;
	}
	public void setAttListJSON(String attListJSON) {
		this.attListJSON = attListJSON;
	}
	public boolean isCollSubjectNotEdit() {
		return collSubjectNotEdit;
	}
	public void setCollSubjectNotEdit(boolean collSubjectNotEdit) {
		this.collSubjectNotEdit = collSubjectNotEdit;
	}
	public String getCollSubject() {
		return collSubject;
	}
	public void setCollSubject(String collSubject) {
		this.collSubject = collSubject;
	}
	public String getTemformParentId() {
		return temformParentId;
	}
	public void setTemformParentId(String temformParentId) {
		this.temformParentId = temformParentId;
	}
	public String getFormtitle() {
		return formtitle;
	}
	public void setFormtitle(String formtitle) {
		this.formtitle = formtitle;
	}
	public String getStandardDuration() {
		return standardDuration;
	}
	public void setStandardDuration(String standardDuration) {
		this.standardDuration = standardDuration;
	}
	
	public String getWfXMLInfo() {
		return wfXMLInfo;
	}
	public void setWfXMLInfo(String wfXMLInfo) {
		this.wfXMLInfo = wfXMLInfo;
	}
	public boolean issVisorsFromTemplate() {
		return sVisorsFromTemplate;
	}
	public void setsVisorsFromTemplate(boolean sVisorsFromTemplate) {
		this.sVisorsFromTemplate = sVisorsFromTemplate;
	}
	public boolean isNoDepManager() {
		return noDepManager;
	}
	public void setNoDepManager(boolean noDepManager) {
		this.noDepManager = noDepManager;
	}
	public String getUnCancelledVisor() {
		return unCancelledVisor;
	}
	public void setUnCancelledVisor(String unCancelledVisor) {
		this.unCancelledVisor = unCancelledVisor;
	}
	public boolean isForm() {
		return form;
	}
	public void setForm(boolean form) {
		this.form = form;
	}
	public boolean isSystemTemplate() {
		if(this.getTemplate() != null){
			return this.getTemplate().isSystem();
		}
		return systemTemplate;
	}
	public void setSystemTemplate(boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}
	public boolean isResendFlag() {
		return resendFlag;
	}
	public void setResendFlag(boolean resendFlag) {
		this.resendFlag = resendFlag;
	}
	public boolean isTempleteHasDeadline() {
		return templeteHasDeadline;
	}
	public void setTempleteHasDeadline(boolean templeteHasDeadline) {
		this.templeteHasDeadline = templeteHasDeadline;
	}
	public boolean isTempleteHasRemind() {
		return templeteHasRemind;
	}
	public void setTempleteHasRemind(boolean templeteHasRemind) {
		this.templeteHasRemind = templeteHasRemind;
	}
	public Long getParentSummaryId() {
		return parentSummaryId;
	}
	public void setParentSummaryId(Long parentSummaryId) {
		this.parentSummaryId = parentSummaryId;
	}
	public int getHideContentType() {
		return hideContentType;
	}
	public void setHideContentType(int hideContentType) {
		this.hideContentType = hideContentType;
	}
	public String getFormOperationId() {
		return formOperationId;
	}
	public void setFormOperationId(String formOperationId) {
		this.formOperationId = formOperationId;
	}
	public List<CtpEnumItem> getSecretLevelList() {
		return secretLevelList;
	}
	public void setSecretLevelList(List<CtpEnumItem> secretLevelList) {
		this.secretLevelList = secretLevelList;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getWorkflowNodesInfo() {
		return workflowNodesInfo;
	}
	public void setWorkflowNodesInfo(String workflowNodesInfo) {
		this.workflowNodesInfo = workflowNodesInfo;
	}
	public Long getSignSummaryId() {
		return signSummaryId;
	}
	public void setSignSummaryId(Long signSummaryId) {
		this.signSummaryId = signSummaryId;
	}
	public int getFenbanFile() {
		return fenbanFile;
	}
	public void setFenbanFile(int fenbanFile) {
		this.fenbanFile = fenbanFile;
	}
	public List<CtpTemplateCategory> getEdocCategoryList() {
		return edocCategoryList;
	}
	public void setEdocCategoryList(List<CtpTemplateCategory> edocCategoryList) {
		this.edocCategoryList = edocCategoryList;
	}
	public String getFormList() {
		return formList;
	}
	public void setFormList(String formList) {
		this.formList = formList;
	}
	public Long getDefaultFormId() {
		return defaultFormId;
	}
	public void setDefaultFormId(Long defaultFormId) {
		this.defaultFormId = defaultFormId;
	}
	public Long getDefaultCategoryId() {
		return defaultCategoryId;
	}
	public void setDefaultCategoryId(Long defaultCategoryId) {
		this.defaultCategoryId = defaultCategoryId;
	}
	public String getDisabledProjectId() {
		return disabledProjectId;
	}
	public void setDisabledProjectId(String disabledProjectId) {
		this.disabledProjectId = disabledProjectId;
	}
	public String getForwardText() {
		return forwardText;
	}
	public void setForwardText(String forwardText) {
		this.forwardText = forwardText;
	}
	public void setContentViewState(int contentViewState) {
		this.contentViewState = contentViewState;
	}
	public void setUuidlong(long uuidlong) {
		this.uuidlong = uuidlong;
	}
	public void setZwModuleId(Long zwModuleId) {
		this.zwModuleId = zwModuleId;
	}
	public void setZwRightId(String zwRightId) {
		this.zwRightId = zwRightId;
	}
	public void setZwIsnew(boolean zwIsnew) {
		this.zwIsnew = zwIsnew;
	}
	public void setZwViewState(int zwViewState) {
		this.zwViewState = zwViewState;
	}
	public int getContentViewState() {
		return contentViewState;
	}
	public long getUuidlong() {
		return uuidlong;
	}
	public Long getZwModuleId() {
		return zwModuleId;
	}
	public String getZwRightId() {
		return zwRightId;
	}
	public boolean isZwIsnew() {
		return zwIsnew;
	}
	public int getZwViewState() {
		return zwViewState;
	}
	public boolean isGovdocTemplate() {
		return isGovdocTemplate;
	}
	public void setGovdocTemplate(boolean isGovdocTemplate) {
		this.isGovdocTemplate = isGovdocTemplate;
	}
	public String getForwardAffairId() {
		return forwardAffairId;
	}
	public void setForwardAffairId(String forwardAffairId) {
		this.forwardAffairId = forwardAffairId;
	}
	public Long getForwardSummaryId() {
		return forwardSummaryId;
	}
	public void setForwardSummaryId(Long forwardSummaryId) {
		this.forwardSummaryId = forwardSummaryId;
	}
	public String getGovdocRelation1() {
		return govdocRelation1;
	}
	public void setGovdocRelation1(String govdocRelation1) {
		this.govdocRelation1 = govdocRelation1;
	}
	public String getGovdocRelation2() {
		return govdocRelation2;
	}
	public void setGovdocRelation2(String govdocRelation2) {
		this.govdocRelation2 = govdocRelation2;
	}
	public ContentViewRet getContentContext() {
		return contentContext;
	}
	public void setContentContext(ContentViewRet contentContext) {
		this.contentContext = contentContext;
	}
	public boolean isOnlyViewWF() {
		return onlyViewWF;
	}
	public void setOnlyViewWF(boolean onlyViewWF) {
		this.onlyViewWF = onlyViewWF;
	}
	public Long getFormAppId() {
		return formAppId;
	}
	public void setFormAppId(Long formAppId) {
		this.formAppId = formAppId;
	}
	public boolean isCustomSetTrack() {
		return customSetTrack;
	}
	public void setCustomSetTrack(boolean customSetTrack) {
		this.customSetTrack = customSetTrack;
	}
	public ConfigItem getGovdocViewConfigItem() {
		return govdocViewConfigItem;
	}
	public void setGovdocViewConfigItem(ConfigItem govdocViewConfigItem) {
		this.govdocViewConfigItem = govdocViewConfigItem;
	}

	public int getAllowCommentInForm() {
		return allowCommentInForm;
	}
	public void setAllowCommentInForm(int allowCommentInForm) {
		this.allowCommentInForm = allowCommentInForm;
	}
	public String getNoselfflow() {
		return noselfflow;
	}
	public void setNoselfflow(String noselfflow) {
		this.noselfflow = noselfflow;
	}
	public boolean isPrintSet() {
		return printSet;
	}
	public void setPrintSet(boolean printSet) {
		this.printSet = printSet;
	}
	public String getMainNum() {
		return mainNum;
	}
	public void setMainNum(String mainNum) {
		this.mainNum = mainNum;
	}
	public String getCopyNum() {
		return copyNum;
	}
	public void setCopyNum(String copyNum) {
		this.copyNum = copyNum;
	}
	public boolean getIsGzEdition() {
		return isGzEdition;
	}
	public void setGzEdition(boolean isGzEdition) {
		this.isGzEdition = isGzEdition;
	}
	public boolean getIsGovdocSystemTemplate() {
		return isGovdocSystemTemplate;
	}
	public void setGovdocSystemTemplate(boolean isGovdocSystemTemplate) {
		this.isGovdocSystemTemplate = isGovdocSystemTemplate;
	}
	public PermissionVO getDefaultPermission() {
		return defaultPermission;
	}
	public void setDefaultPermission(PermissionVO defaultPermission) {
		this.defaultPermission = defaultPermission;
	}
	public Long getOriginalContentId() {
		return originalContentId;
	}
	public void setOriginalContentId(Long originalContentId) {
		this.originalContentId = originalContentId;
	}
	public String getEditType() {
		return editType;
	}
	public void setEditType(String editType) {
		this.editType = editType;
	}
	public boolean isCustomDealWith() {
		return customDealWith;
	}
	public void setCustomDealWith(boolean customDealWith) {
		this.customDealWith = customDealWith;
	}
	public boolean isCustomDealWithTemplate() {
		return customDealWithTemplate;
	}
	public void setCustomDealWithTemplate(boolean customDealWithTemplate) {
		this.customDealWithTemplate = customDealWithTemplate;
	}
	public Integer getReturnPermissionsLength() {
		return returnPermissionsLength;
	}
	public void setReturnPermissionsLength(Integer returnPermissionsLength) {
		this.returnPermissionsLength = returnPermissionsLength;
	}
	public List<PermissionVO> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<PermissionVO> permissions) {
		this.permissions = permissions;
	}
	public String getCustomDealWithPermission() {
		return customDealWithPermission;
	}
	public void setCustomDealWithPermission(String customDealWithPermission) {
		this.customDealWithPermission = customDealWithPermission;
	}
	public String getCustomDealWithMemberId() {
		return customDealWithMemberId;
	}
	public void setCustomDealWithMemberId(String customDealWithMemberId) {
		this.customDealWithMemberId = customDealWithMemberId;
	}
	public List<Map<String,Object>> getMembers() {
		return members;
	}
	public void setMembers(List<Map<String,Object>> members) {
		this.members = members;
	}
	public String getMemberJson() {
		return memberJson;
	}
	public void setMemberJson(String memberJson) {
		this.memberJson = memberJson;
	}
	public String getCurrentPolicyId() {
		return currentPolicyId;
	}
	public void setCurrentPolicyId(String currentPolicyId) {
		this.currentPolicyId = currentPolicyId;
	}
	public String getCurrentPolicyName() {
		return currentPolicyName;
	}
	public void setCurrentPolicyName(String currentPolicyName) {
		this.currentPolicyName = currentPolicyName;
	}
	public boolean isNotExistChengban() {
		return notExistChengban;
	}
	public void setNotExistChengban(boolean notExistChengban) {
		this.notExistChengban = notExistChengban;
	}
	public V3xOrgMember getNextMember() {
		return nextMember;
	}
	public void setNextMember(V3xOrgMember nextMember) {
		this.nextMember = nextMember;
	}
	public String getForGZIds() {
		return forGZIds;
	}
	public void setForGZIds(String forGZIds) {
		this.forGZIds = forGZIds;
	}
	public Integer getTrackType() {
		return trackType;
	}
	public void setTrackType(Integer trackType) {
		this.trackType = trackType;
	}
	public List<Comment> getCommentSenderList() {
		return commentSenderList;
	}
	public void setCommentSenderList(List<Comment> commentSenderList) {
		this.commentSenderList = commentSenderList;
	}
	public CtpContentAllBean getContentBean() {
		return contentBean;
	}
	public void setContentBean(CtpContentAllBean contentBean) {
		this.contentBean = contentBean;
	}

	public Integer getFormDefaultShow() {
		return formDefaultShow;
	}

	public void setFormDefaultShow(Integer formDefaultShow) {
		this.formDefaultShow = formDefaultShow;
	}

	public GovdocMarkVO getMarkOpenVo() {
		return markOpenVo;
	}

	public void setMarkOpenVo(GovdocMarkVO markOpenVo) {
		this.markOpenVo = markOpenVo;
	}

	public GovdocMarkVO getDocMarkVo() {
		return docMarkVo;
	}

	public void setDocMarkVo(GovdocMarkVO docMarkVo) {
		this.docMarkVo = docMarkVo;
	}

	public GovdocMarkVO getSerialNoVo() {
		return serialNoVo;
	}

	public void setSerialNoVo(GovdocMarkVO serialNoVo) {
		this.serialNoVo = serialNoVo;
	}

	public GovdocMarkVO getSignMarkVo() {
		return signMarkVo;
	}

	public void setSignMarkVo(GovdocMarkVO signMarkVo) {
		this.signMarkVo = signMarkVo;
	}

	public Long getContentDataId() {
		return contentDataId;
	}

	public void setContentDataId(Long contentDataId) {
		this.contentDataId = contentDataId;
	}

	public Long getContentTemplateId() {
		return contentTemplateId;
	}

	public void setContentTemplateId(Long contentTemplateId) {
		this.contentTemplateId = contentTemplateId;
	}

	public Long getDistributeAffairId() {
		return distributeAffairId;
	}

	public void setDistributeAffairId(Long distributeAffairId) {
		this.distributeAffairId = distributeAffairId;
	}
	
	public String getZfwZiDongBanJie() {
		return zfwZiDongBanJie;
	}

	public void setZfwZiDongBanJie(String zfwZiDongBanJie) {
		this.zfwZiDongBanJie = zfwZiDongBanJie;
	}

	public String getCantEditFilesmName() {
		return cantEditFilesmName;
	}

	public void setCantEditFilesmName(String cantEditFilesmName) {
		this.cantEditFilesmName = cantEditFilesmName;
	}

	public String getFilesmFieldName() {
		return filesmFieldName;
	}

	public void setFilesmFieldName(String filesmFieldName) {
		this.filesmFieldName = filesmFieldName;
	}

	public void setSubAppName(String subAppName) {
		this.subAppName = subAppName;
	}

	public String getSubAppName() {
		if ("1".equals(getSubApp())) {
			return "govdocSend";
		} else if ("2".equals(getSubApp())) {
			return "govdocRec";
		} else if ("3".equals(getSubApp())) {
			return "govdocSign";
		} else if ("4".equals(getSubApp())) {
			return "govdocExchange";
		}
		return subAppName;
	}

	public boolean isTemplateHasProcessTermType() {
		return templateHasProcessTermType;
	}

	public void setTemplateHasProcessTermType(boolean templateHasProcessTermType) {
		this.templateHasProcessTermType = templateHasProcessTermType;
	}

	public boolean isProcessTrem() {
		return processTrem;
	}

	public void setProcessTrem(boolean processTrem) {
		this.processTrem = processTrem;
	}

	public boolean isTemplateHasRemindInterval() {
		return templateHasRemindInterval;
	}

	public void setTemplateHasRemindInterval(boolean templateHasRemindInterval) {
		this.templateHasRemindInterval = templateHasRemindInterval;
	}

	public boolean isRemindInterval() {
		return remindInterval;
	}

	public void setRemindInterval(boolean remindInterval) {
		this.remindInterval = remindInterval;
	}

	public String getFilesmFormAttsName() {
		return filesmFormAttsName;
	}

	public void setFilesmFormAttsName(String filesmFormAttsName) {
		this.filesmFormAttsName = filesmFormAttsName;
	}

	public String getFilesmContentAttsName() {
		return filesmContentAttsName;
	}

	public void setFilesmContentAttsName(String filesmContentAttsName) {
		this.filesmContentAttsName = filesmContentAttsName;
	}

	public Boolean getCanAnyDealMerge() {
		return canAnyDealMerge;
	}

	public void setCanAnyDealMerge(Boolean canAnyDealMerge) {
		this.canAnyDealMerge = canAnyDealMerge;
	}

	public Boolean getCanPreDealMerge() {
		return canPreDealMerge;
	}

	public void setCanPreDealMerge(Boolean canPreDealMerge) {
		this.canPreDealMerge = canPreDealMerge;
	}

	public Boolean getCanStartMerge() {
		return canStartMerge;
	}

	public void setCanStartMerge(Boolean canStartMerge) {
		this.canStartMerge = canStartMerge;
	}

	public String getSubjectFromTrans() {
		return subjectFromTrans;
	}

	public void setSubjectFromTrans(String subjectFromTrans) {
		this.subjectFromTrans = subjectFromTrans;
	}

	public List<ProcessLogDetail> getProcessLogDetails() {
		return processLogDetails;
	}

	public void setProcessLogDetails(List<ProcessLogDetail> processLogDetails) {
		this.processLogDetails = processLogDetails;
	}

}
