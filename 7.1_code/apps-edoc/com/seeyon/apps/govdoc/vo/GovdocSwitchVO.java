package com.seeyon.apps.govdoc.vo;

public class GovdocSwitchVO {

	private boolean hasDealArea = false;//是否显示回复区域
	private boolean collectFlag = false;//是否开启收藏开关
	private boolean canFavorite = false;//是否有收藏权限
	private boolean isCollect = false;//公文处理界面-是否收藏过，区分收藏/取消
	private boolean canArchive = false;//节点权限是否有归档权限
	private boolean readOnly = false;//公文处理界面-是否只读
	
	private boolean canModifyWorkFlow = false;//是否允许修改流程
	private boolean canEditAtt = false;//是否开启已发才能有开关修改附件
	private boolean canShowOpinion = false;//是否显示意见框
	private boolean canShowAttitude = false;//是否显示态度
	private boolean canShowCommonPhrase = false;//是否显示常用语
	private boolean canEditBody = false;//是否允许修改正文
	private boolean canUploadAttachment;// 是否允许上传附件
	private boolean canUploadRel = false;//是否允许关联文档
	private boolean canTrack = false;//是否允许跟踪
	private boolean canEditAttachment = false;//是否允许修改附件(已发开关)
	private boolean isFengsong = false;//是否是分送节点
	private boolean canComment = false;//节点权限是否配有提交策略
	private boolean canContentSign = false;//节点权限是否配有正文盖章策略
	private boolean canTransToBul = false;//节点权限是否配有转公告策略
	private boolean canTransToEvent = false;//节点权限是否配有转事件策略
	private boolean canTransToCol = false;//节点权限是否配有转事务策略
	private boolean hasNewColRole = false;//当前人员是否有转事务权限
	
	private boolean officecanPrint = false;//office正文是否可以印
    private boolean officecanSaveLocal = false;//office正文是否可以保存到本地
    private boolean canPrint = false;//是否允许打印(文单)
    private boolean canLocalPrint = false;//是否开启下载到本地打印
	
	private boolean chuantou;
	private boolean chuantouchakan1 = false;//开关配置：(发文/交换/收文)是否可以穿透查看
	private boolean chuantouchakan2 = false;//开关配置：(收文/转收文)是否可以穿透查看
	private boolean allowEditInForm = false;//开关配置：(公文开关)允许修改意见
	private boolean allowCommentInForm = false;//意见填写在true文单/false右侧，前端使用
	
	private boolean zhuanfawenTactics = false;//启用收文节点转发文策略 开关
	private String zhuanfawen;//转发文默认设置开关
	private boolean duanxintixing = false;//短信提醒
	
	private boolean noFindPermission = false;//是否能找到节点权限
	private String isHasPraise = "0";//是否允许点赞
	
	private boolean hasSendEdocRole = false;//是否有发文拟文权限
	private boolean onlySeeContent = false;//是否只允许查看公文正文
	private boolean showContentByGovdocNodePropertyConfig = false;//表单权限设置置中是否允许查看正文，默认否
	private int formDefaultShow = 1;//正文(0) 标准文单(2) 全文签批单(1)
	//公文布局开关
	private String newGovdocView = "0";
	//按钮显示名称
	private String showButton;
	//获得超级节点状态:0表示为非超级节点;1表示超级节点待处理;2表示超级节点待触发;3表示超级节点待回退
	private int superNodestatus = 0;
	private boolean canXuban = false;//节点是否支持续办
	
	public boolean isCollectFlag() {
		return collectFlag;
	}
	public void setCollectFlag(boolean collectFlag) {
		this.collectFlag = collectFlag;
	}
	public boolean isCanFavorite() {
		return canFavorite;
	}
	public void setCanFavorite(boolean canFavorite) {
		this.canFavorite = canFavorite;
	}
	public boolean isCanArchive() {
		return canArchive;
	}
	public void setCanArchive(boolean canArchive) {
		this.canArchive = canArchive;
	}
	public String getIsHasPraise() {
		return isHasPraise;
	}
	public void setIsHasPraise(String isHasPraise) {
		this.isHasPraise = isHasPraise;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public Boolean getIsCollect() {
		return isCollect;
	}
	public void setIsCollect(Boolean isCollect) {
		this.isCollect = isCollect;
	}
	public Boolean getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}
	public void setCollect(boolean isCollect) {
		this.isCollect = isCollect;
	}
	public boolean isCanShowOpinion() {
		return canShowOpinion;
	}
	public void setCanShowOpinion(boolean canShowOpinion) {
		this.canShowOpinion = canShowOpinion;
	}
	public boolean isCanShowAttitude() {
		return canShowAttitude;
	}
	public void setCanShowAttitude(boolean canShowAttitude) {
		this.canShowAttitude = canShowAttitude;
	}
	public boolean isCanShowCommonPhrase() {
		return canShowCommonPhrase;
	}
	public void setCanShowCommonPhrase(boolean canShowCommonPhrase) {
		this.canShowCommonPhrase = canShowCommonPhrase;
	}
	public boolean isCanUploadAttachment() {
		return canUploadAttachment;
	}
	public void setCanUploadAttachment(boolean canUploadAttachment) {
		this.canUploadAttachment = canUploadAttachment;
	}
	public boolean isCanUploadRel() {
		return canUploadRel;
	}
	public void setCanUploadRel(boolean canUploadRel) {
		this.canUploadRel = canUploadRel;
	}
	public boolean isCanModifyWorkFlow() {
		return canModifyWorkFlow;
	}
	public void setCanModifyWorkFlow(boolean canModifyWorkFlow) {
		this.canModifyWorkFlow = canModifyWorkFlow;
	}
	public boolean isCanEditAttachment() {
		return canEditAttachment;
	}
	public void setCanEditAttachment(boolean canEditAttachment) {
		this.canEditAttachment = canEditAttachment;
	}
	public boolean isFengsong() {
		return isFengsong;
	}
	public void setFengsong(boolean isFengsong) {
		this.isFengsong = isFengsong;
	}
	public boolean isHasSendEdocRole() {
		return hasSendEdocRole;
	}
	public void setHasSendEdocRole(boolean hasSendEdocRole) {
		this.hasSendEdocRole = hasSendEdocRole;
	}
	public boolean isChuantou() {
		return chuantou;
	}
	public void setChuantou(boolean chuantou) {
		this.chuantou = chuantou;
	}
	public boolean isChuantouchakan1() {
		return chuantouchakan1;
	}
	public void setChuantouchakan1(boolean chuantouchakan1) {
		this.chuantouchakan1 = chuantouchakan1;
	}
	public boolean isChuantouchakan2() {
		return chuantouchakan2;
	}
	public void setChuantouchakan2(boolean chuantouchakan2) {
		this.chuantouchakan2 = chuantouchakan2;
	}
	public boolean isAllowEditInForm() {
		return allowEditInForm;
	}
	public void setAllowEditInForm(boolean allowEditInForm) {
		this.allowEditInForm = allowEditInForm;
	}
	public boolean isAllowCommentInForm() {
		return allowCommentInForm;
	}
	public void setAllowCommentInForm(boolean allowCommentInForm) {
		this.allowCommentInForm = allowCommentInForm;
	}
	public boolean isZhuanfawenTactics() {
		return zhuanfawenTactics;
	}
	public void setZhuanfawenTactics(boolean zhuanfawenTactics) {
		this.zhuanfawenTactics = zhuanfawenTactics;
	}
	public String getZhuanfawen() {
		return zhuanfawen;
	}
	public void setZhuanfawen(String zhuanfawen) {
		this.zhuanfawen = zhuanfawen;
	}
	public boolean isDuanxintixing() {
		return duanxintixing;
	}
	public void setDuanxintixing(boolean duanxintixing) {
		this.duanxintixing = duanxintixing;
	}
	public boolean isCanEditAtt() {
		return canEditAtt;
	}
	public void setCanEditAtt(boolean canEditAtt) {
		this.canEditAtt = canEditAtt;
	}
	public boolean isCanEditBody() {
		return canEditBody;
	}
	public void setCanEditBody(boolean canEditBody) {
		this.canEditBody = canEditBody;
	}
	public boolean isOnlySeeContent() {
		return onlySeeContent;
	}
	public void setOnlySeeContent(boolean onlySeeContent) {
		this.onlySeeContent = onlySeeContent;
	}
	public boolean isCanPrint() {
		return canPrint;
	}
	public void setCanPrint(boolean canPrint) {
		this.canPrint = canPrint;
	}
	public boolean isOfficecanPrint() {
		return officecanPrint;
	}
	public void setOfficecanPrint(boolean officecanPrint) {
		this.officecanPrint = officecanPrint;
	}
	public boolean isOfficecanSaveLocal() {
		return officecanSaveLocal;
	}
	public void setOfficecanSaveLocal(boolean officecanSaveLocal) {
		this.officecanSaveLocal = officecanSaveLocal;
	}
	public boolean getShowContentByGovdocNodePropertyConfig() {
		return showContentByGovdocNodePropertyConfig;
	}
	public void setShowContentByGovdocNodePropertyConfig(boolean showContentByGovdocNodePropertyConfig) {
		this.showContentByGovdocNodePropertyConfig = showContentByGovdocNodePropertyConfig;
	}
	public Integer getFormDefaultShow() {
		return formDefaultShow;
	}
	public void setFormDefaultShow(Integer formDefaultShow) {
		this.formDefaultShow = formDefaultShow;
	}
	public String getNewGovdocView() {
		return newGovdocView;
	}
	public void setNewGovdocView(String newGovdocView) {
		this.newGovdocView = newGovdocView;
	}
	public boolean isCanLocalPrint() {
		return canLocalPrint;
	}
	public void setCanLocalPrint(boolean canLocalPrint) {
		this.canLocalPrint = canLocalPrint;
	}
	public boolean isNoFindPermission() {
		return noFindPermission;
	}
	public void setNoFindPermission(boolean noFindPermission) {
		this.noFindPermission = noFindPermission;
	}
	public void setFormDefaultShow(int formDefaultShow) {
		this.formDefaultShow = formDefaultShow;
	}
	public String isHasPraise() {
		return isHasPraise;
	}
	public void setHasPraise(String isHasPraise) {
		this.isHasPraise = isHasPraise;
	}
	public String getShowButton() {
		return showButton;
	}
	public void setShowButton(String showButton) {
		this.showButton = showButton;
	}
	public int getSuperNodestatus() {
		return superNodestatus;
	}
	public void setSuperNodestatus(int superNodestatus) {
		this.superNodestatus = superNodestatus;
	}
	public boolean isHasDealArea() {
		return hasDealArea;
	}
	public void setHasDealArea(boolean hasDealArea) {
		this.hasDealArea = hasDealArea;
	}
	public boolean isCanComment() {
		return canComment;
	}
	public void setCanComment(boolean canComment) {
		this.canComment = canComment;
	}
	public boolean isCanContentSign() {
		return canContentSign;
	}
	public void setCanContentSign(boolean canContentSign) {
		this.canContentSign = canContentSign;
	}
	public boolean isCanTransToBul() {
		return canTransToBul;
	}
	public void setCanTransToBul(boolean canTransToBul) {
		this.canTransToBul = canTransToBul;
	}
	public boolean isCanTransToEvent() {
		return canTransToEvent;
	}
	public void setCanTransToEvent(boolean canTransToEvent) {
		this.canTransToEvent = canTransToEvent;
	}
	public boolean isCanTransToCol() {
		return canTransToCol;
	}
	public void setCanTransToCol(boolean canTransToCol) {
		this.canTransToCol = canTransToCol;
	}
	public boolean isHasNewColRole() {
		return hasNewColRole;
	}
	public void setHasNewColRole(boolean hasNewColRole) {
		this.hasNewColRole = hasNewColRole;
	}
	public boolean isCanXuban() {
		return canXuban;
	}
	public void setCanXuban(boolean canXuban) {
		this.canXuban = canXuban;
	}
	public boolean isCanTrack() {
		return canTrack;
	}
	public void setCanTrack(boolean canTrack) {
		this.canTrack = canTrack;
	}
}
