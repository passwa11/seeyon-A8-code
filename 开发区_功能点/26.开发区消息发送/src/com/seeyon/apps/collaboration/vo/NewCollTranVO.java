package com.seeyon.apps.collaboration.vo;

import java.util.Date;
import java.util.List;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplate;



/**
 * 凡是要走新建协同的地方采用这个值传递对象
 * @author libing
 *
 */
public class NewCollTranVO {
	//从哪里来的
	private String from;
	//协同的ID
	private String summaryId;
	
	//流程實例ID---caseId
	private Long caseId;
	
	//是否能编辑预归档路径
    private boolean canEditColPigeonhole;
	//模板Id
	private String templeteId;
	
	private String affairId;
	
	private ColSummary summary;
    
    private  CtpAffair affair ;
    
    private String senderOpinionContent;
    //附件集合
    private List<Attachment> atts;
     
    private boolean canDeleteOriginalAtts = true;
    
    private boolean cloneOriginalAtts;
    
    private Long archiveId;
    
    private String archiveName = "";
    
    private String archiveAllName;
    
    private  Long projectId ;
    
    private List<ProjectBO> projectList;
    
    private Date createDate;
    
    private String deadLineDateTimeHidden;
    
    private String subjectForCopy;
    
    private Long attachmentArchiveId;
    /**
     * 模板是否设置了流程超期
     */
    private boolean templateHasProcessTermType;
    /**
     * 模板是否设置了流程超期循环提醒
     */
    private boolean templateHasRemindInterval;
    
    public String getSubjectForCopy() {
		return subjectForCopy;
	}

	public void setSubjectForCopy(String subjectForCopy) {
		this.subjectForCopy = subjectForCopy;
	}


	/**
     * 预归档
     */
    private String advancePigeonhole;
    public String getAdvancePigeonhole() {
  	return advancePigeonhole;
  }

  public void setAdvancePigeonhole(String advancePigeonhole) {
  	this.advancePigeonhole = advancePigeonhole;
  }
    public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public boolean isFromSystemTemplete() {
		return fromSystemTemplete;
	}

	public void setFromSystemTemplete(boolean isFromSystemTemplete) {
		this.fromSystemTemplete = isFromSystemTemplete;
	}

	public List<ProjectBO> getProjectList() {
		return projectList;
	}

	public void setProjectList(List<ProjectBO> projectList) {
		this.projectList = projectList;
	}


	private User user;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(String summaryId) {
		this.summaryId = summaryId;
	}

	public String getTempleteId() {
		return templeteId;
	}

	public void setTempleteId(String templeteId) {
		this.templeteId = templeteId;
	}

	public ColSummary getSummary() {
		return summary;
	}

	public void setSummary(ColSummary summary) {
		this.summary = summary;
	}

	public CtpAffair getAffair() {
		return affair;
	}

	public void setAffair(CtpAffair affair) {
		this.affair = affair;
	}

	public String getSenderOpinionContent() {
		return senderOpinionContent;
	}

	public void setSenderOpinionContent(String senderOpinionContent) {
		this.senderOpinionContent = senderOpinionContent;
	}

	public List<Attachment> getAtts() {
		return atts;
	}

	public void setAtts(List<Attachment> atts) {
		this.atts = atts;
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

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
    
    //private  Templete templete;
	private String colSupervisors;

	public String getColSupervisors() {
		return colSupervisors;
	}

	public void setColSupervisors(String colSupervisors) {
		this.colSupervisors = colSupervisors;
	}
	
	private CtpSuperviseDetail colSupervise;

	public CtpSuperviseDetail getColSupervise() {
		return colSupervise;
	}

	public void setColSupervise(CtpSuperviseDetail detail) {
		this.colSupervise = detail;
	}
	
	private String superviseDate;

	public String getSuperviseDate() {
		return superviseDate;
	}

	public void setSuperviseDate(String superviseDate) {
		this.superviseDate = superviseDate;
	}
	
	private String colSupervisorNames;

	public String getColSupervisorNames() {
		return colSupervisorNames;
	}

	public void setColSupervisorNames(String colSupervisorNames) {
		this.colSupervisorNames = colSupervisorNames;
	}
	
	private String trackIds;

	public String getTrackIds() {
		return trackIds;
	}

	public void setTrackIds(String trackIds) {
		this.trackIds = trackIds;
	}
	
	//流程ID
	private String processId;

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	private boolean readOnly;

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	private boolean fromTemplate;

	public boolean isFromTemplate() {
		return fromTemplate;
	}

	public void setFromTemplate(boolean isFromTemplate) {
		this.fromTemplate = isFromTemplate;
	}
	
	private String attListJSON;

	public String getAttListJSON() {
		return attListJSON;
	}

	public void setAttListJSON(String attListJSON) {
		this.attListJSON = attListJSON;
	}
	
	private CtpTemplate template;

	public CtpTemplate getTemplate() {
		return template;
	}

	public void setTemplate(CtpTemplate template) {
		this.template = template;
	}
	//调用模板的一些信息
	private boolean collSubjectNotEdit;

	public boolean isCollSubjectNotEdit() {
		return collSubjectNotEdit;
	}

	public void setCollSubjectNotEdit(boolean collSubjectNotEdit) {
		this.collSubjectNotEdit = collSubjectNotEdit;
	}
	
	private String collSubject;
	
	private String temformParentId;
	
	private String formtitle;
	
	private String standardDuration;

	public String getStandardDuration() {
		return standardDuration;
	}

	public void setStandardDuration(String standardDuration) {
		this.standardDuration = standardDuration;
	}

	public String getFormtitle() {
		return formtitle;
	}

	public void setFormtitle(String formtitle) {
		this.formtitle = formtitle;
	}

	public String getTemformParentId() {
		return temformParentId;
	}

	public void setTemformParentId(String temformParentId) {
		this.temformParentId = temformParentId;
	}

	public String getNewBusiness() {
		return newBusiness;
	}

	public void setNewBusiness(String newBusiness) {
		this.newBusiness = newBusiness;
	}

	public String getCollSubject() {
		return collSubject;
	}

	public void setCollSubject(String collSubject) {
		this.collSubject = collSubject;
	}

	public boolean isParentColTemplete() {
		return parentColTemplete;
	}

	public void setParentColTemplete(boolean parentColTemplete) {
		this.parentColTemplete = parentColTemplete;
	}
	
	//用于判断是否是新建的业务
	private String newBusiness = "1";
	
	private String wfXMLInfo;

	public String getWfXMLInfo() {
		return wfXMLInfo;
	}

	public void setWfXMLInfo(String wfXMLInfo) {
		this.wfXMLInfo = wfXMLInfo;
	}
	
	private boolean sVisorsFromTemplate;

	public boolean issVisorsFromTemplate() {
		return sVisorsFromTemplate;
	}

	public void setsVisorsFromTemplate(boolean sVisorsFromTemplate) {
		this.sVisorsFromTemplate = sVisorsFromTemplate;
	}
	
	private boolean noDepManager;

	public boolean getNoDepManager() {
		return noDepManager;
	}

	public void setNoDepManager(boolean noDepManager) {
		this.noDepManager = noDepManager;
	}
	
	public String unCancelledVisor;

	public String getUnCancelledVisor() {
		return unCancelledVisor;
	}

	public void setUnCancelledVisor(String unCancelledVisor) {
		this.unCancelledVisor = unCancelledVisor;
	}
	
	private boolean form;

	public boolean isForm() {
		return form;
	}

	public void setForm(boolean form) {
		this.form = form;
	}
	
	private boolean systemTemplate;

	public boolean isSystemTemplate() {
		return systemTemplate;
	}

	public void setSystemTemplate(boolean systemTemplate) {
		this.systemTemplate = systemTemplate;
	}
	
	private boolean resendFlag ;

	public boolean isResendFlag() {
		return resendFlag;
	}

	public void setResendFlag(boolean resendFlag) {
		this.resendFlag = resendFlag;
	}
	
	private boolean templeteHasDeadline;
	private boolean templeteHasRemind;

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
	
	private boolean parentWrokFlowTemplete; //父模板是流程模板
	
	private boolean parentTextTemplete;//父模板是格式模板
	
	private boolean parentColTemplete;//父模板是协同模板
	
	private boolean fromSystemTemplete;//父模板是来源于系统模板
	
	private String forShow;//用于督办的回显展现
	
	private String forGZShow;//用于更总的回显展示
	
	public String getForGZShow() {
		return forGZShow;
	}

	public void setForGZShow(String forGZShow) {
		this.forGZShow = forGZShow;
	}

	public String getForShow() {
		return forShow;
	}

	public void setForShow(String forShow) {
		this.forShow = forShow;
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
	
	private String formViewOperation;//表单操作权限Id


    /**
     * @return the caseId
     */
    public Long getCaseId() {
        return caseId;
    }

    /**
     * @param caseId the caseId to set
     */
    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }
    public String getAffairId() {
		return affairId;
	}

	public void setAffairId(String affairId) {
		this.affairId = affairId;
	}

    public boolean isCanEditColPigeonhole() {
        return canEditColPigeonhole;
    }

    public void setCanEditColPigeonhole(boolean canEditColPigeonhole) {
        this.canEditColPigeonhole = canEditColPigeonhole;
    }

	public String getDeadLineDateTimeHidden() {
		return deadLineDateTimeHidden;
	}

	public void setDeadLineDateTimeHidden(String deadLineDateTimeHidden) {
		this.deadLineDateTimeHidden = deadLineDateTimeHidden;
	}

	public String getArchiveAllName() {
		return archiveAllName;
	}

	public void setArchiveAllName(String archiveAllName) {
		this.archiveAllName = archiveAllName;
	}

	public Long getAttachmentArchiveId() {
		return attachmentArchiveId;
	}

	public void setAttachmentArchiveId(Long attachmentArchiveId) {
		this.attachmentArchiveId = attachmentArchiveId;
	}

    public String getFormViewOperation() {
        return formViewOperation;
    }

    public void setFormViewOperation(String formViewOperation) {
        this.formViewOperation = formViewOperation;
    }
	public boolean isTemplateHasProcessTermType() {
		return templateHasProcessTermType;
	}

	public void setTemplateHasProcessTermType(boolean templateHasProcessTermType) {
		this.templateHasProcessTermType = templateHasProcessTermType;
	}

	public boolean isTemplateHasRemindInterval() {
		return templateHasRemindInterval;
	}

	public void setTemplateHasRemindInterval(boolean templateHasRemindInterval) {
		this.templateHasRemindInterval = templateHasRemindInterval;
	}
    
}
