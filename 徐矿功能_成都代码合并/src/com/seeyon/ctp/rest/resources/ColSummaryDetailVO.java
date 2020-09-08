/**
 * Author : xuqw
 *   Date : 2015年12月11日 下午2:01:39
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.rest.resources;

import java.util.List;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.util.CollaborationUtils;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title       : 协同H5详细页面的VO</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class ColSummaryDetailVO {


    /* 协同 ID */
    private Long id = null;
    
    /* 标题 */
    private String subject = null;
    
    /* 发起人名称 */
    private String startMemberName = null;
    
    /* 创建时间 */
    private String createDate = null;
    
    /* 重要程度 */
    private Integer importantLevel = null;
    
    /* 附件 */
    private List<Attachment> attachments = null;
    
    /* affair状态 */
    private Integer affairState = null;
    
    /* affair二级状态 */
    private Integer affairSubState = null;
    
    /* 跟踪状态 */
    private Integer affairTrack = null;
    
    /*流程的Workitem Id*/
    private Long affairWorkitemId = null;
    
    /*affair Id*/
    private Long affairId = null;

    /*流程ID*/
    private String processId = null;
    
    /*流程实例ID*/
    private Long caseId = null;
    
    /*当前节点id*/
    private Long activityId = null;
    
    /* 是否结束 */
    private boolean finished = false;
    
    /*状态*/
    private Integer state = null;
    
    /*模板流程id*/
    private Long templateProcessId = null;
    
    /*发起人id*/
    private Long startMemberId = null;
    
    /** 数据关联DR **/
    private String dr = null;
    
    /** 表单ID **/
    private Long formAppid = null;
    
    /** 表单操作权限ID **/
    private String formViewOperation = null;
    
    /** 父表单参数 **/
    private Long formParentid = null;
    
    /* 表单主表ID */
    private Long formRecordId = null;
    
    private Long formAppId = null;
    
    private String rightId = null;
    
    private String nodePolicy = null;
    
    private Boolean affairReadOnly = Boolean.FALSE;
    
    /*模板ID*/
    private Long templateId = null;
    
    /*正文类型*/
    private String bodyType = "10";//默认为HTML
    
    private boolean isSystemTemplate = false;
    
    private Long accountId;
    
    private String  processDeadLineName;  //流程期限
    
    private Integer newflowType;//新流程类型 
    
    private Integer processTermType;
    
    private Long remindInterval;
    
    private String mergeDealType;
    
    private boolean isProcessTemplate = false; //是否为有流程的模板
    
    public Integer getProcessTermType() {
		return processTermType;
	}

	public void setProcessTermType(Integer processTermType) {
		this.processTermType = processTermType;
	}

	private String canForward = "1";
    
    private String canModify = "1";
    
    private String canEdit = "1";
    
    private boolean affairIsDelete;
    
    
    
    private String canEditAttachment = "1";
    
    private String canArchive = "1";
    
    private String canMergeDeal = "0";
    
    private String canAnyMerge = "0";
    
    private String canScanCode = "0";
    
    private String canSetSupervise = "1";
    
    private Long projectId;
    
    private Long archiveId;
    
    private Long advanceRemind;
    
    private Long deadline;
    
    private String deadlineDatetime;
    
    private String advancePigeonhole;
    
    private String archiveName;
    
    private String archiveAllName;
    
    private String listType;
    private Boolean isCanComment;
    
    private boolean canPraise = true;
    
    private Long attachmentArchiveId;
    
    private String hasFavorite;
    
    /** 是否在指定回退状态 **/
    private boolean specialStepback = false;

    private boolean hasWorkFlowAdvance = false;
    /**项目：徐矿集团 【在移动端返回公文详情时增加是否屏蔽按钮】 作者：jiangchenxi 时间：2019年3月13日  start*/
    private boolean shield;
    
    public boolean isShield() {
    	return shield;
    }
    public void setShield(boolean shield) {
    	this.shield = shield;
    }
    /**项目：徐矿集团 【在移动端返回公文详情时增加是否屏蔽按钮】 作者：jiangchenxi 时间：2019年3月13日  end*/

    public boolean isHasWorkFlowAdvance() {
        return hasWorkFlowAdvance;
    }
    
    public void setHasWorkFlowAdvance(boolean hasWorkFlowAdvance) {
        this.hasWorkFlowAdvance = hasWorkFlowAdvance;
    }

    public String getHasFavorite() {
		return hasFavorite;
	}


	public void setHasFavorite(String hasFavorite) {
		this.hasFavorite = hasFavorite;
	}


	public boolean getCanPraise() {
        return canPraise;
    }


    public void setCanPraise(boolean canPraise) {
        this.canPraise = canPraise;
    }


    public String getDeadlineDatetime() {
		return deadlineDatetime;
	}


	public void setDeadlineDatetime(String deadlineDatetime) {
		this.deadlineDatetime = deadlineDatetime;
	}

	
    public String getArchiveAllName() {
		return archiveAllName;
	}


	public void setArchiveAllName(String archiveAllName) {
		this.archiveAllName = archiveAllName;
	}


	public String getAdvancePigeonhole() {
		return advancePigeonhole;
	}


	public void setAdvancePigeonhole(String advancePigeonhole) {
		this.advancePigeonhole = advancePigeonhole;
	}


	public String getArchiveName() {
		return archiveName;
	}


	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}


	public Long getDeadline() {
		return deadline;
	}


	public void setDeadline(Long deadline) {
		this.deadline = deadline;
	}


	public Long getAdvanceRemind() {
		return advanceRemind;
	}


	public void setAdvanceRemind(Long advanceRemind) {
		this.advanceRemind = advanceRemind;
	}


	public Long getArchiveId() {
		return archiveId;
	}


	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}


	public Long getProjectId() {
		return projectId;
	}


	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}


	public String getCanScanCode() {
		return canScanCode;
	}


	public void setCanScanCode(String canScanCode) {
		this.canScanCode = canScanCode;
	}


	public String getCanSetSupervise() {
		return canSetSupervise;
	}


	public void setCanSetSupervise(String canSetSupervise) {
		this.canSetSupervise = canSetSupervise;
	}


	public String getCanMergeDeal() {
		return canMergeDeal;
	}


	public void setCanMergeDeal(String canMergeDeal) {
		this.canMergeDeal = canMergeDeal;
	}


	public String getCanAnyMerge() {
		return canAnyMerge;
	}


	public void setCanAnyMerge(String canAnyMerge) {
		this.canAnyMerge = canAnyMerge;
	}


	public String getCanForward() {
		return canForward;
	}


	public void setCanForward(String canForward) {
		this.canForward = canForward;
	}


	public String getCanModify() {
		return canModify;
	}


	public void setCanModify(String canModify) {
		this.canModify = canModify;
	}


	public String getCanEdit() {
		return canEdit;
	}


	public void setCanEdit(String canEdit) {
		this.canEdit = canEdit;
	}


	public String getCanEditAttachment() {
		return canEditAttachment;
	}


	public void setCanEditAttachment(String canEditAttachment) {
		this.canEditAttachment = canEditAttachment;
	}


	public String getCanArchive() {
		return canArchive;
	}


	public void setCanArchive(String canArchive) {
		this.canArchive = canArchive;
	}


	public static ColSummaryDetailVO valueOf(ColSummary summary){
        
        ColSummaryDetailVO vo = new ColSummaryDetailVO();
        
        vo.setId(summary.getId());
        vo.setSubject(ColUtil.showSubjectOfSummary(summary, false, -1, null).replaceAll("\r\n", "").replaceAll("\n", ""));
        vo.setStartMemberName(Functions.showMemberNameOnly(summary.getStartMemberId()));
        vo.setCreateDate(CollaborationUtils.showDate(summary.getCreateDate()));
        vo.setImportantLevel(summary.getImportantLevel());
        vo.setProcessId(summary.getProcessId());
        vo.setCaseId(summary.getCaseId());
        vo.setFinished(summary.getFinishDate() == null ? false : true);
        vo.setState(summary.getState());
        vo.setStartMemberId(summary.getStartMemberId());
        vo.setBodyType(summary.getBodyType());
        vo.setFormRecordId(summary.getFormRecordid());
        vo.setTemplateId(summary.getTempleteId());
        vo.setAccountId(summary.getPermissionAccountId());
        vo.setProjectId(summary.getProjectId());
        //流程期限名称
        if(summary.getDeadlineDatetime() != null) {
        	vo.setProcessDeadLineName(ColUtil.getDeadLineName(summary.getDeadlineDatetime()));
        } else {//兼容老数据，还是按时间段显示
            //流程期限名称
            vo.setProcessDeadLineName(ColUtil.getDeadLineName(summary.getDeadline()));
        }
        vo.setNewflowType(summary.getNewflowType());
        vo.setFormAppId(summary.getFormAppid());
        return vo;
    }
    
    
    public void setImportantLevel(Integer importantLevel) {
        this.importantLevel = importantLevel;
    }
    
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    
    public void setStartMemberName(String startMemberName) {
        this.startMemberName = startMemberName;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    /* 协同ID */
    public Long getId(){
        return id;
    }
    
    /* 协同标题 */
    public String getSubject(){
        return subject;
    }
    
    /* 发起人名称 */
    public String getStartMemberName(){
        return startMemberName;
    }
    
    /* 创建时间 */
    public String getCreateDate(){
        return createDate;
    }
    
    /* 重要程度 */
    public Integer getImportantLevel(){
        return importantLevel;
    }
    
    
    /* 附件  */
    public List<Attachment> getAttachments(){
        return this.attachments;
    }
    
    /* 附件数量 */
    public int getAttachmentCount(){
        int ret = 0;
        if(Strings.isNotEmpty(this.attachments)){
            ret = this.attachments.size();
        }
        return ret;
    }
    
    /** 获取实体附件数量  **/
    public int getFileAttachmentCount(){
        
        int ret = 0;
        
        if(Strings.isNotEmpty(this.attachments)){
            for(Attachment a : this.attachments){
              //添加附件到对象中，附件的type为0，关联文档的type为2（不显示关联文档在附件列表中）
                if(a.getType() == Constants.ATTACHMENT_TYPE.FILE.ordinal()){
                    ret++;
                }
            }
        }
        
        return ret;
    }
    
    /** 获取关联文档附件数量  **/
    public int getAssAttachmentCount(){
        
        return getAttachmentCount() - getFileAttachmentCount();
    }
    
    
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
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

	public String getProcessId() {
		return processId;
	}


	public void setProcessId(String processId) {
		this.processId = processId;
	}


	public Long getCaseId() {
		return caseId;
	}


	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}


	public Long getActivityId() {
		return activityId;
	}


	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

    public boolean isFinished() {
        return finished;
    }


    public void setFinished(boolean finished) {
        this.finished = finished;
    }


    public Integer getState() {
        return state;
    }


    public Long getTemplateProcessId() {
		return templateProcessId;
	}


	public void setTemplateProcessId(Long templateProcessId) {
		this.templateProcessId = templateProcessId;
	}


	public void setState(Integer state) {
        this.state = state;
    }


	public Long getStartMemberId() {
		return startMemberId;
	}


	public void setStartMemberId(Long startMemberId) {
		this.startMemberId = startMemberId;
	}


    public String getBodyType() {
        return bodyType;
    }


    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }


    public Long getAffairWorkitemId() {
        return affairWorkitemId;
    }


    public void setAffairWorkitemId(Long affairWorkitemId) {
        this.affairWorkitemId = affairWorkitemId;
    }


    public Long getAffairId() {
        return affairId;
    }


    public void setAffairId(Long affairId) {
        this.affairId = affairId;
    }


    public Long getFormRecordId() {
        return formRecordId;
    }


    public void setFormRecordId(Long formRecordId) {
        this.formRecordId = formRecordId;
    }


    public Long getTemplateId() {
        return templateId;
    }


    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }


	public boolean isSystemTemplate() {
		return isSystemTemplate;
	}


	public void setSystemTemplate(boolean isSystemTemplate) {
		this.isSystemTemplate = isSystemTemplate;
	}


	public Long getAccountId() {
		return accountId;
	}


	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}


	public Long getFormAppId() {
		return formAppId;
	}

	public void setFormAppId(Long formAppId) {
		this.formAppId = formAppId;
	}

	public String getRightId() {
		return rightId;
	}

	public void setRightId(String rightId) {
		this.rightId = rightId;
	}

	public String getNodePolicy() {
		return nodePolicy;
	}

	public void setNodePolicy(String nodePolicy) {
		this.nodePolicy = nodePolicy;
	}

	public Boolean getAffairReadOnly() {
		return affairReadOnly;
	}

	public void setAffairReadOnly(Boolean affairReadOnly) {
		this.affairReadOnly = affairReadOnly;
	}

	public String getProcessDeadLineName() {
		return processDeadLineName;
	}

	public void setProcessDeadLineName(String processDeadLineName) {
		this.processDeadLineName = processDeadLineName;
	}

	public Integer getNewflowType() {
		return newflowType;
	}

	public void setNewflowType(Integer newflowType) {
		this.newflowType = newflowType;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public Boolean getIsCanComment() {
		return isCanComment;
	}

	public void setIsCanComment(Boolean isCanComment) {
		this.isCanComment = isCanComment;
	}


    public Long getFormAppid() {
        return formAppid;
    }


    public void setFormAppid(Long formAppid) {
        this.formAppid = formAppid;
    }


    public Long getFormParentid() {
        return formParentid;
    }


    public void setFormParentid(Long formParentid) {
        this.formParentid = formParentid;
    }


    public String getDr() {
        return dr;
    }


    public void setDr(String dr) {
        this.dr = dr;
    }


	public boolean isAffairIsDelete() {
		return affairIsDelete;
	}


	public void setAffairIsDelete(boolean affairIsDelete) {
		this.affairIsDelete = affairIsDelete;
	}


	public Long getAttachmentArchiveId() {
		return attachmentArchiveId;
	}


	public void setAttachmentArchiveId(Long attachmentArchiveId) {
		this.attachmentArchiveId = attachmentArchiveId;
	}


    public boolean isSpecialStepback() {
        return specialStepback;
    }


    public void setSpecialStepback(boolean specialStepback) {
        this.specialStepback = specialStepback;
    }

    public String getFormViewOperation() {
        return formViewOperation;
    }

    public void setFormViewOperation(String formViewOperation) {
        this.formViewOperation = formViewOperation;
    }

	public Long getRemindInterval() {
		return remindInterval;
	}

	public String getMergeDealType() {
		return mergeDealType;
	}

	public void setRemindInterval(Long remindInterval) {
		this.remindInterval = remindInterval;
	}

	public void setMergeDealType(String mergeDealType) {
		this.mergeDealType = mergeDealType;
	}

	public boolean isProcessTemplate() {
		return isProcessTemplate;
	}

	public void setProcessTemplate(boolean isProcessTemplate) {
		this.isProcessTemplate = isProcessTemplate;
	}

	
}
