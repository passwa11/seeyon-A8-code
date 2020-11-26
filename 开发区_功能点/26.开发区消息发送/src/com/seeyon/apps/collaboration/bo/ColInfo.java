/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.bo;

import java.util.Date;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
/**
 * @author mujun
 *
 */
public class ColInfo {
	
	private String subjectForCopy;
    
	public String getSubjectForCopy() {
		return subjectForCopy;
	}

	public void setSubjectForCopy(String subjectForCopy) {
		this.subjectForCopy = subjectForCopy;
	}

	private ColSummary summary;
    
    private Comment comment;
    
    private CtpContentAll content;
    
    private CtpAffair senderAffair;
   
    private User currentUser ;
    private String  processXml;
    
    private int trackType;
    //指定跟踪人员的ID连接字符串
    private String trackMemberId; 
    //是否是删除个人事项操作标记
    private boolean isDelAffair;
    
    private Long tId;
    private Long curTemId;

	private boolean isBatch;
    private Boolean newBusiness;
    private Integer moduleType;
    
    private String phaseId;
    
    private Long caseId;
    
    private Long currentAffairId;
    
    private Long currentProcessId;
    
    private boolean modifyFlag ;
    
    private String workflowNewflowInput;
    private String workflowNodePeoplesInput;
    private String workflowNodeConditionInput;
    private CtpContentAllBean ctpContentAll;
    private String contentSaveId;
    private String dr;
    private String reMeoToReGo;
    /*流程动态路径匹配，底表数据Id,数据结构：底表FormAppId|节点ID|数据记录1ID#数据记录2ID,底表FormAppId|数据记录1ID#数据记录2ID 例如：123|1#2#,32|3#4#*/
    private String dynamicFormMasterIds ;
    
    private String formViewOperation;
    //流程使用：子流程是否跳过第一个发起节点
    private boolean wfSubPRCSkipFSender;
    
    public boolean getWfSubPRCSkipFSender() {
        return wfSubPRCSkipFSender;
    }

    public void setWfSubPRCSkipFSender(boolean wfSubPRCSkipFSender) {
        this.wfSubPRCSkipFSender = wfSubPRCSkipFSender;
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

	public void setContent(CtpContentAll content) {
		this.content = content;
	}

	public void setDelAffair(boolean isDelAffair) {
		this.isDelAffair = isDelAffair;
	}

	public CtpContentAllBean getCtpContentAll() {
		return ctpContentAll;
	}

	public void setCtpContentAll(CtpContentAllBean ctpContentAll) {
		this.ctpContentAll = ctpContentAll;
	}

	public CtpAffair getSenderAffair() {
		return senderAffair;
	}

	public void setSenderAffair(CtpAffair senderAffair) {
		this.senderAffair = senderAffair;
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

	public ColInfo() {
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
	public Boolean getNewBusiness() {
		return newBusiness;
	}
	public void setNewBusiness(Boolean newBusiness) {
		this.newBusiness = newBusiness;
	}
	/**
     * @return the tId
     */
    public Long gettId() {
        return tId;
    }
    /**
     * @param tId the tId to set
     */
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
	public int getTrackType() {
		return trackType;
	}
	public void setTrackType(int trackType) {
		this.trackType = trackType;
	}
	/**
     * @return the processXml
     */
    public String getProcessXml() {
        return processXml;
    }
    /**
     * @param processXml the processXml to set
     */
    public void setProcessXml(String processXml) {
        this.processXml = processXml;
    }
    /**
     * @return the cuurentUser
     */
    public User getCurrentUser() {
        return currentUser;
    }
    /**
     * @param cuurentUser the cuurentUser to set
     */
    public void setCurrentUser(User cuurentUser) {
        this.currentUser = cuurentUser;
    }
    /**
     * @return the summary
     */
    public ColSummary getSummary() {
        return summary;
    }
    /**
     * @param summary the summary to set
     */
    public void setSummary(ColSummary summary) {
        this.summary = summary;
    }
    /**
     * @return the comment
     */
    public Comment getComment() {
        return comment;
    }
    /**
     * @param comment the comment to set
     */
    public void setComment(Comment comment) {
        this.comment = comment;
    }
    
    /**
     * 
     * @param content 发起者附言内容
     */
    public void setComment(String content){
        this.comment = new Comment();
        comment.setContent(content);
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setModifyDate(null);
        comment.setCtype(Comment.CommentType.sender.getKey()); //发起人附言
        comment.setPath("00");
        comment.setPid(0L);
        comment.setClevel(1);
        comment.setHidden(false);
    }
    
    /**
     * 正文
     * @param newBodyType
     * @param content
     * @param contentDataId
     * @param createDate
     */
    public void setBody(MainbodyType newBodyType, String content, Long contentDataId, Date createDate){
        this.content = new CtpContentAll();
        this.content.setNewId();
        this.content.setContent(content);
        this.content.setContentDataId(contentDataId);
        this.content.setContentType(newBodyType.getKey());
        this.content.setModuleType(ModuleType.collaboration.getKey());
        this.content.setContentTemplateId(0L);
        this.content.setModuleTemplateId(-1L);
        this.content.setCreateDate(createDate);
        this.content.setModifyDate(null);
        this.content.setModifyId(null);
        this.content.setSort(0);
    }
    
    public CtpContentAll getContent(){
        return this.content;
    }
    
    public Integer getModuleType() {
        return moduleType;
    }
    public void setModuleType(Integer moduleType) {
        this.moduleType = moduleType;
    }

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

    /**
     * @return the currentAffairId
     */
    public Long getCurrentAffairId() {
        return currentAffairId;
    }

    /**
     * @param currentAffairId the currentAffairId to set
     */
    public void setCurrentAffairId(Long currentAffairId) {
        this.currentAffairId = currentAffairId;
    }

    /**
     * @return the currentProcessId
     */
    public Long getCurrentProcessId() {
        return currentProcessId;
    }

    /**
     * @param currentProcessId the currentProcessId to set
     */
    public void setCurrentProcessId(Long currentProcessId) {
        this.currentProcessId = currentProcessId;
    }

    public String getFormViewOperation() {
        return formViewOperation;
    }

    public void setFormViewOperation(String formViewOperation) {
        this.formViewOperation = formViewOperation;
    }    
}
