package com.seeyon.apps.govdoc.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.UUIDLong;

public class GovdocSummaryVO extends GovdocBaseVO {

	private String operationId;

	private String formMutilOprationIds;

	private String openFrom;

	private String contentAnchor;

	private Integer pigeonholeType;

	private String isRecSendRel;

	private boolean isJointly;

	private String extFrom;

	private String leaderPishiType;

	private Integer govdocType;

	private String trackTypeRecord;
	private boolean isHistoryFlag;
	private String lenPotent;
	private String errorMsg;
	private Long backFormId;
	private Boolean allowCommentInForm;
	private boolean canEditAtt;
	private Long newPdfIdFirst = UUIDLong.longUUID();
	private Long newPdfIdSecond = UUIDLong.longUUID();
	private Long newOFDIdFirst = UUIDLong.longUUID();
	private Long newOFDIdSecond = UUIDLong.longUUID();
	private String pdfFileId;
	private String ofdFileId;
	//private String myContentNameId;
	//private String govdocBodyType;
	//private String govdocBodyTypeText;
	//private Timestamp govdocContentCreateTime;
	private Permission permission;
	private String openNewWindow;
	private boolean isNewflow;
	private String aipFileId;
	private String exchangeContentId;//交换前公文正文ID
	
	private Long agencyAffairMerberId;//代理或者代录的人员id
	private String agencyAffairMerberName;//代理或者代录的人员名
	
	private GovdocExchangeMain exchangeMain;
	private GovdocExchangeDetail exchangeDetail;
	private List<GovdocExchangeDetail> exchangeDetailList;

	public String getExchangeContentId() {
		return exchangeContentId;
	}

	public void setExchangeContentId(String exchangeContentId) {
		this.exchangeContentId = exchangeContentId;
	}

	public String getAipFileId() {
		return aipFileId;
	}

	public void setAipFileId(String aipFileId) {
		this.aipFileId = aipFileId;
	}

	/**
     * nodeId
     */
    private Long              activityId;
	
	 public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	/**
     * 当前用户是否是督办人员
     */
    private Boolean           isCurrentUserSupervisor;
    
	/**
	 * 发起人姓名
	 */
	private String startMemberName;
	/**
	 * 附件列表
	 * 
	 * @return
	 */
	private List<Attachment> attachments;
	
	  /**
     * 节点权限所在单位Id
     */
    private Long              flowPermAccountId;
    //是否有附件
    private Boolean           hasAttsFlag       = false;
    
	/**
     *  发起人部门ID
    */
    private Long    startMemberDepartmentId;
    /**
     *  发起人主岗ID
    */
    private java.lang.Long    startMemberPostId;
    /**
     *  发起人主岗名称
    */
    private String    startMemberPostName;
	
	/**文单打印相关参数 end**/
	
	
	private String affairMemberName;
	
	private String stepBack;
	
	
	/** 续办相关参数  start*/
	private Boolean canShowOpinion;
	private Boolean canShowAttitude;
	private Boolean canShowCommonPhrase;
	private Boolean canUploadAttachment;
	private Boolean canUploadRel;
	private Boolean isFaxingNode;
	private Integer formDefaultShow;
	private Integer toEdocLibFlag;
	private Integer toEdocLibSelectFlag;
	private String showCustomDealWith;
	private String customDealWith;
	private Object customDealWithPermission;
	private Object customDealWithMemberId;
	private int returnPermissionsLength;
	private List<PermissionVO> permissions;
	private List<Map<String,Object>> members;
	private String memberJson;
	private String currentPolicyId;
	private String currentPolicyName;
	private boolean notExistChengban;
	private String currentMember;
	private V3xOrgMember nextMember;
	/** 续办相关参数  end*/

	/* 领导批示编号相关参数 start*/
	private Object pishiname;
	private Integer nowyear;
	private List<Integer> pishiyear;
	private Object nowpishiNo;
	private String pishiNos;
	private String proxydate;
	//是否从文档中心打开
	private String isGovArchive;
	
	
	public String getIsGovArchive() {
		return isGovArchive;
	}

	public void setIsGovArchive(String isGovArchive) {
		this.isGovArchive = isGovArchive;
	}

	/* 领导批示编号相关参数 end*/
	public String getFormMutilOprationIds() {
		return formMutilOprationIds;
	}

	public void setFormMutilOprationIds(String formMutilOprationIds) {
		this.formMutilOprationIds = formMutilOprationIds;
	}

	public String getOpenFrom() {
		return openFrom;
	}

	public void setOpenFrom(String openFrom) {
		this.openFrom = openFrom;
	}

	public String getContentAnchor() {
		return contentAnchor;
	}

	public void setContentAnchor(String contentAnchor) {
		this.contentAnchor = contentAnchor;
	}

	public Integer getPigeonholeType() {
		return pigeonholeType;
	}

	public void setPigeonholeType(Integer pigeonholeType) {
		this.pigeonholeType = pigeonholeType;
	}

	public String getIsRecSendRel() {
		return isRecSendRel;
	}

	public void setIsRecSendRel(String isRecSendRel) {
		this.isRecSendRel = isRecSendRel;
	}

	public void setJointly(boolean isJointly) {
		this.isJointly = isJointly;
	}

	public boolean isJointly() {
		return isJointly;
	}

	public String getExtFrom() {
		return extFrom;
	}

	public void setExtFrom(String extFrom) {
		this.extFrom = extFrom;
	}

	public String getLeaderPishiType() {
		return leaderPishiType;
	}

	public void setLeaderPishiType(String leaderPishiType) {
		this.leaderPishiType = leaderPishiType;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Integer getGovdocType() {
		return govdocType;
	}

	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
	}

	public String getTrackTypeRecord() {
		return trackTypeRecord;
	}

	public void setTrackTypeRecord(String trackTypeRecord) {
		this.trackTypeRecord = trackTypeRecord;
	}

	public boolean isHistoryFlag() {
		return isHistoryFlag;
	}

	public void setHistoryFlag(boolean isHistoryFlag) {
		this.isHistoryFlag = isHistoryFlag;
	}

	public String getLenPotent() {
		return lenPotent;
	}

	public void setLenPotent(String lenPotent) {
		this.lenPotent = lenPotent;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Long getBackFormId() {
		return backFormId;
	}

	public void setBackFormId(Long backFormId) {
		this.backFormId = backFormId;
	}
	public Boolean getAllowCommentInForm() {
		return allowCommentInForm;
	}

	public void setAllowCommentInForm(Boolean allowCommentInForm) {
		this.allowCommentInForm = allowCommentInForm;
	}
	
	public boolean isCanEditAtt() {
		return canEditAtt;
	}

	public void setCanEditAtt(boolean canEditAtt) {
		this.canEditAtt = canEditAtt;
	}

	public List<Attachment> getAttachments() {
		if (attachments == null) {
			return new ArrayList<Attachment>();
		}
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	public Long getNewPdfIdFirst() {
		return newPdfIdFirst;
	}

	public void setNewPdfIdFirst(Long newPdfIdFirst) {
		this.newPdfIdFirst = newPdfIdFirst;
	}

	public Long getNewPdfIdSecond() {
		return newPdfIdSecond;
	}

	public void setNewPdfIdSecond(Long newPdfIdSecond) {
		this.newPdfIdSecond = newPdfIdSecond;
	}

	public Long getNewOFDIdFirst() {
		return newOFDIdFirst;
	}

	public void setNewOFDIdFirst(Long newOFDIdFirst) {
		this.newOFDIdFirst = newOFDIdFirst;
	}

	public Long getNewOFDIdSecond() {
		return newOFDIdSecond;
	}

	public void setNewOFDIdSecond(Long newOFDIdSecond) {
		this.newOFDIdSecond = newOFDIdSecond;
	}

	public String getPdfFileId() {
		return pdfFileId;
	}

	public void setPdfFileId(String pdfFileId) {
		this.pdfFileId = pdfFileId;
	}

	public String getOfdFileId() {
		return ofdFileId;
	}

	public void setOfdFileId(String ofdFileId) {
		this.ofdFileId = ofdFileId;
	}
/*
	public String getMyContentNameId() {
		return myContentNameId;
	}

	public void setMyContentNameId(String myContentNameId) {
		this.myContentNameId = myContentNameId;
	}

	public String getGovdocBodyType() {
		return govdocBodyType;
	}

	public void setGovdocBodyType(String govdocBodyType) {
		this.govdocBodyType = govdocBodyType;
	}

	public String getGovdocBodyTypeText() {
		return govdocBodyTypeText;
	}

	public void setGovdocBodyTypeText(String govdocBodyTypeText) {
		this.govdocBodyTypeText = govdocBodyTypeText;
	}
	
	public Timestamp getGovdocContentCreateTime() {
		return govdocContentCreateTime;
	}

	public void setGovdocContentCreateTime(Timestamp govdocContentCreateTime) {
		this.govdocContentCreateTime = govdocContentCreateTime;
	}
*/
	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public String getOpenNewWindow() {
		return openNewWindow;
	}

	public void setOpenNewWindow(String openNewWindow) {
		this.openNewWindow = openNewWindow;
	}

	public String getStartMemberName() {
		return startMemberName;
	}

	public void setStartMemberName(String startMemberName) {
		this.startMemberName = startMemberName;
	}
	public boolean getIsNewflow() {
		return isNewflow;
	}

	public void setNewflow(boolean isNewflow) {
		this.isNewflow = isNewflow;
	}

	public Boolean getIsCurrentUserSupervisor() {
		return isCurrentUserSupervisor;
	}

	public void setIsCurrentUserSupervisor(Boolean isCurrentUserSupervisor) {
		this.isCurrentUserSupervisor = isCurrentUserSupervisor;
	}
	public Long getFlowPermAccountId() {
		return flowPermAccountId;
	}
	public void setFlowPermAccountId(Long flowPermAccountId) {
		this.flowPermAccountId = flowPermAccountId;
	}

	public Boolean getHasAttsFlag() {
		return hasAttsFlag;
	}

	public void setHasAttsFlag(Boolean hasAttsFlag) {
		this.hasAttsFlag = hasAttsFlag;
	}
	public String getAffairMemberName() {
		return affairMemberName;
	}

	public void setAffairMemberName(String affairMemberName) {
		this.affairMemberName = affairMemberName;
	}

	public String getStepBack() {
		return stepBack;
	}

	public void setStepBack(String stepBack) {
		this.stepBack = stepBack;
	}
	
	/** 续办相关参数  start*/
	
	public Boolean getCanShowOpinion() {
		return canShowOpinion;
	}

	public void setCanShowOpinion(Boolean canShowOpinion) {
		this.canShowOpinion = canShowOpinion;
	}

	public Boolean getCanShowAttitude() {
		return canShowAttitude;
	}

	public void setCanShowAttitude(Boolean canShowAttitude) {
		this.canShowAttitude = canShowAttitude;
	}

	public Boolean getCanShowCommonPhrase() {
		return canShowCommonPhrase;
	}

	public void setCanShowCommonPhrase(Boolean canShowCommonPhrase) {
		this.canShowCommonPhrase = canShowCommonPhrase;
	}

	public Boolean getCanUploadAttachment() {
		return canUploadAttachment;
	}

	public void setCanUploadAttachment(Boolean canUploadAttachment) {
		this.canUploadAttachment = canUploadAttachment;
	}

	public Boolean getCanUploadRel() {
		return canUploadRel;
	}

	public void setCanUploadRel(Boolean canUploadRel) {
		this.canUploadRel = canUploadRel;
	}

	public Boolean getIsFaxingNode() {
		return isFaxingNode;
	}

	public void setIsFaxingNode(Boolean isFaxingNode) {
		this.isFaxingNode = isFaxingNode;
	}

	public Integer getFormDefaultShow() {
		return formDefaultShow;
	}

	public void setFormDefaultShow(Integer formDefaultShow) {
		this.formDefaultShow = formDefaultShow;
	}

	public Integer getToEdocLibFlag() {
		return toEdocLibFlag;
	}

	public void setToEdocLibFlag(Integer toEdocLibFlag) {
		this.toEdocLibFlag = toEdocLibFlag;
	}

	public Integer getToEdocLibSelectFlag() {
		return toEdocLibSelectFlag;
	}

	public void setToEdocLibSelectFlag(Integer toEdocLibSelectFlag) {
		this.toEdocLibSelectFlag = toEdocLibSelectFlag;
	}

	public String getShowCustomDealWith() {
		return showCustomDealWith;
	}

	public void setShowCustomDealWith(String showCustomDealWith) {
		this.showCustomDealWith = showCustomDealWith;
	}

	public String getCustomDealWith() {
		return customDealWith;
	}

	public void setCustomDealWith(String customDealWith) {
		this.customDealWith = customDealWith;
	}

	public Object getCustomDealWithPermission() {
		return customDealWithPermission;
	}

	public void setCustomDealWithPermission(Object customDealWithPermission) {
		this.customDealWithPermission = customDealWithPermission;
	}

	public Object getCustomDealWithMemberId() {
		return customDealWithMemberId;
	}

	public void setCustomDealWithMemberId(Object customDealWithMemberId) {
		this.customDealWithMemberId = customDealWithMemberId;
	}

	public int getReturnPermissionsLength() {
		return returnPermissionsLength;
	}

	public void setReturnPermissionsLength(int returnPermissionsLength) {
		this.returnPermissionsLength = returnPermissionsLength;
	}

	public List<PermissionVO> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PermissionVO> permissions) {
		this.permissions = permissions;
	}

	public List<Map<String, Object>> getMembers() {
		return members;
	}

	public void setMembers(List<Map<String, Object>> members) {
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

	public String getCurrentMember() {
		return currentMember;
	}

	public void setCurrentMember(String currentMember) {
		this.currentMember = currentMember;
	}

	public V3xOrgMember getNextMember() {
		return nextMember;
	}

	public void setNextMember(V3xOrgMember nextMember) {
		this.nextMember = nextMember;
	}
	/** 续办相关参数  end*/

	public Long getStartMemberDepartmentId() {
		return startMemberDepartmentId;
	}

	public void setStartMemberDepartmentId(Long startMemberDepartmentId) {
		this.startMemberDepartmentId = startMemberDepartmentId;
	}

	public java.lang.Long getStartMemberPostId() {
		return startMemberPostId;
	}

	public void setStartMemberPostId(java.lang.Long startMemberPostId) {
		this.startMemberPostId = startMemberPostId;
	}

	public String getStartMemberPostName() {
		return startMemberPostName;
	}

	public void setStartMemberPostName(String startMemberPostName) {
		this.startMemberPostName = startMemberPostName;
	}

	public Object getPishiname() {
		return pishiname;
	}

	public void setPishiname(Object pishiname) {
		this.pishiname = pishiname;
	}

	public Integer getNowyear() {
		return nowyear;
	}

	public void setNowyear(Integer nowyear) {
		this.nowyear = nowyear;
	}

	public List<Integer> getPishiyear() {
		return pishiyear;
	}

	public void setPishiyear(List<Integer> pishiyear) {
		this.pishiyear = pishiyear;
	}

	public Object getNowpishiNo() {
		return nowpishiNo;
	}

	public void setNowpishiNo(Object nowpishiNo) {
		this.nowpishiNo = nowpishiNo;
	}

	public String getPishiNos() {
		return pishiNos;
	}

	public void setPishiNos(String pishiNos) {
		this.pishiNos = pishiNos;
	}

	public String getProxydate() {
		return proxydate;
	}

	public void setProxydate(String proxydate) {
		this.proxydate = proxydate;
	}

	public Long getAgencyAffairMerberId() {
		return agencyAffairMerberId;
	}

	public void setAgencyAffairMerberId(Long agencyAffairMerberId) {
		this.agencyAffairMerberId = agencyAffairMerberId;
	}

	public String getAgencyAffairMerberName() {
		return agencyAffairMerberName;
	}

	public void setAgencyAffairMerberName(String agencyAffairMerberName) {
		this.agencyAffairMerberName = agencyAffairMerberName;
	}

	public GovdocExchangeMain getExchangeMain() {
		return exchangeMain;
	}

	public void setExchangeMain(GovdocExchangeMain exchangeMain) {
		this.exchangeMain = exchangeMain;
	}

	public GovdocExchangeDetail getExchangeDetail() {
		return exchangeDetail;
	}

	public void setExchangeDetail(GovdocExchangeDetail exchangeDetail) {
		this.exchangeDetail = exchangeDetail;
	}

	public List<GovdocExchangeDetail> getExchangeDetailList() {
		return exchangeDetailList;
	}

	public void setExchangeDetailList(List<GovdocExchangeDetail> exchangeDetailList) {
		this.exchangeDetailList = exchangeDetailList;
	}
	
}
