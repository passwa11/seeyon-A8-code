package com.seeyon.apps.govdoc.vo;

import java.util.List;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;

public class GovdocComponentVO extends GovdocBaseVO {
	
	private Long affairId;
	private String rightId;
	private boolean readonly;
	private String openFrom;
	private String isRecSendRel;
	private boolean isHistoryFlag;
	private String isHasPraise;
	private boolean canMoveISignature;
	private boolean canDeleteISigntureHtml;
	private boolean isShowMoveMenu;
	private boolean isShowDocLockMenu;
	
	//加一个类型判断公文的电子签章是否可修改
	private String govCanOperatingSign;
	
	private Long formAppid;

	private String isTakeback;

	private String isGovArchive;

	private User user;
	private int viewState;
	
	private List<Comment> commentList;
	
	private List<CtpContentAllBean> contentList;
	
	private boolean isFromTransform;
	private boolean changeOpinion;
	private boolean allowEditMyOpinions;
	private boolean canEditComment;
	private String lederRelation;
	private long moduleId;
	
	private String logDescMap;//人员操作日志
	
	private String leaderPishiType;//代领导批示列表入口标识
	
	private String operationId;//从表单查询或其他页面传进的rightId
	
	
	public String getOperationId() {
		return operationId;
	}
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Long getAffairId() {
		return affairId;
	}

	public String getRightId() {
		return rightId;
	}


	public void setRightId(String rightId) {
		this.rightId = rightId;
	}


	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getOpenFrom() {
		return openFrom;
	}

	public void setOpenFrom(String openFrom) {
		this.openFrom = openFrom;
	}

	public String getIsRecSendRel() {
		return isRecSendRel;
	}

	public void setIsRecSendRel(String isRecSendRel) {
		this.isRecSendRel = isRecSendRel;
	}

	public boolean isHistoryFlag() {
		return isHistoryFlag;
	}

	public void setHistoryFlag(boolean isHistoryFlag) {
		this.isHistoryFlag = isHistoryFlag;
	}

	public String getIsHasPraise() {
		return isHasPraise;
	}

	public void setIsHasPraise(String isHasPraise) {
		this.isHasPraise = isHasPraise;
	}

	public boolean getCanMoveISignature() {
		return canMoveISignature;
	}

	public void setCanMoveISignature(boolean canMoveISignature) {
		this.canMoveISignature = canMoveISignature;
	}

	public boolean getCanDeleteISigntureHtml() {
		return canDeleteISigntureHtml;
	}

	public void setCanDeleteISigntureHtml(boolean canDeleteISigntureHtml) {
		this.canDeleteISigntureHtml = canDeleteISigntureHtml;
	}

	public boolean getIsShowMoveMenu() {
		return isShowMoveMenu;
	}

	public void setShowMoveMenu(boolean isShowMoveMenu) {
		this.isShowMoveMenu = isShowMoveMenu;
	}

	public boolean getIsShowDocLockMenu() {
		return isShowDocLockMenu;
	}

	public void setShowDocLockMenu(boolean isShowDocLockMenu) {
		this.isShowDocLockMenu = isShowDocLockMenu;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getFormAppid() {
		return formAppid;
	}

	public void setFormAppid(Long formAppid) {
		this.formAppid = formAppid;
	}

	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}

	public String getIsGovArchive() {
		return isGovArchive;
	}

	public void setIsGovArchive(String isGovArchive) {
		this.isGovArchive = isGovArchive;
	}

	public String getIsTakeback() {
		return isTakeback;
	}

	public void setIsTakeback(String isTakeback) {
		this.isTakeback = isTakeback;
	}

	public int getViewState() {
		return viewState;
	}

	public void setViewState(int viewState) {
		this.viewState = viewState;
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}

	public List<CtpContentAllBean> getContentList() {
		return contentList;
	}

	public void setContentList(List<CtpContentAllBean> contentList) {
		this.contentList = contentList;
	}

	public boolean getIsFromTransform() {
		return isFromTransform;
	}

	public void setFromTransform(boolean isFromTransform) {
		this.isFromTransform = isFromTransform;
	}

	public boolean getChangeOpinion() {
		return changeOpinion;
	}

	public void setChangeOpinion(boolean isChangeOpinion) {
		this.changeOpinion = isChangeOpinion;
	}

	public boolean getAllowEditMyOpinions() {
		return allowEditMyOpinions;
	}

	public void setAllowEditMyOpinions(boolean allowEditMyOpinions) {
		this.allowEditMyOpinions = allowEditMyOpinions;
	}
	public boolean getCanEditComment() {
		return canEditComment;
	}
	public void setCanEditComment(boolean canEditComment) {
		this.canEditComment = canEditComment;
	}
	public String getLederRelation() {
		return lederRelation;
	}
	public void setLederRelation(String lederRelation) {
		this.lederRelation = lederRelation;
	}
	public long getModuleId() {
		return moduleId;
	}
	public void setModuleId(long moduleId) {
		this.moduleId = moduleId;
	}
	public String getLogDescMap() {
		return logDescMap;
	}
	public void setLogDescMap(String logDescMap) {
		this.logDescMap = logDescMap;
	}
	public String getLeaderPishiType() {
		return leaderPishiType;
	}
	public void setLeaderPishiType(String leaderPishiType) {
		this.leaderPishiType = leaderPishiType;
	}
	public String getGovCanOperatingSign() {
		return govCanOperatingSign;
	}
	public void setGovCanOperatingSign(String govCanOperatingSign) {
		this.govCanOperatingSign = govCanOperatingSign;
	}	
}
