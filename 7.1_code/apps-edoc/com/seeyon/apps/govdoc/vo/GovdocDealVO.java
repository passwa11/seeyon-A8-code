package com.seeyon.apps.govdoc.vo;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.po.affair.CtpAffair;

public class GovdocDealVO extends GovdocBaseVO {
	
	private String govdocContent;
	private boolean convertPdf;
	private boolean smsAlert;
	private String customDealWithActivitys;
	
	private String submitStyle;//0流程撤销 1提交给我
	private String selectTargetNodeId;//被指定回退的节点ID
	private String selectTargetNodeName;
	private List<CtpAffair> validAffairList;
	
	//跟踪信息
	private Map<String, Object> trackPara;
	
	private String templateColSubject;
	private String templateWorkflowId;
	
	private String extAtt1;
	private String repealComment;
	
	private String chooseOpinionType;
	
	private String pigeonholeValue;//处理后归档参数
	
	private String jointlyUnit;//联合发文单位,有值就表示需要联合发文
	
	private String leaderPishiFlag;//代录标识
	
	private Map<String, String> expandParams;//拓展字段，用于后续添加传递参数使用
	
	private Map<String, Object> pishiParams;//用于批示编号参数传递
	
	public String getGovdocContent() {
		return govdocContent;
	}
	public void setGovdocContent(String govdocContent) {
		this.govdocContent = govdocContent;
	}
	public boolean isConvertPdf() {
		return convertPdf;
	}
	public void setConvertPdf(boolean convertPdf) {
		this.convertPdf = convertPdf;
	}
	public boolean isSmsAlert() {
		return smsAlert;
	}
	public void setSmsAlert(boolean smsAlert) {
		this.smsAlert = smsAlert;
	}	
	public String getCustomDealWithActivitys() {
		return customDealWithActivitys;
	}
	public void setCustomDealWithActivitys(String customDealWithActivitys) {
		this.customDealWithActivitys = customDealWithActivitys;
	}
	public String getSubmitStyle() {
		return submitStyle;
	}
	public void setSubmitStyle(String submitStyle) {
		this.submitStyle = submitStyle;
	}
	public String getSelectTargetNodeId() {
		return selectTargetNodeId;
	}
	public void setSelectTargetNodeId(String selectTargetNodeId) {
		this.selectTargetNodeId = selectTargetNodeId;
	}
	
	public Map<String, Object> getTrackPara() {
		return trackPara;
	}
	public void setTrackPara(Map<String, Object> trackPara) {
		this.trackPara = trackPara;
	}
	public String getTemplateColSubject() {
		return templateColSubject;
	}
	public void setTemplateColSubject(String templateColSubject) {
		this.templateColSubject = templateColSubject;
	}
	public String getTemplateWorkflowId() {
		return templateWorkflowId;
	}
	public void setTemplateWorkflowId(String templateWorkflowId) {
		this.templateWorkflowId = templateWorkflowId;
	}
	public String getSelectTargetNodeName() {
		return selectTargetNodeName;
	}
	public void setSelectTargetNodeName(String selectTargetNodeName) {
		this.selectTargetNodeName = selectTargetNodeName;
	}
	public String getExtAtt1() {
		return extAtt1;
	}
	public void setExtAtt1(String extAtt1) {
		this.extAtt1 = extAtt1;
	}
	public String getRepealComment() {
		return repealComment;
	}
	public void setRepealComment(String repealComment) {
		this.repealComment = repealComment;
	}
	public String getChooseOpinionType() {
		return chooseOpinionType;
	}
	public void setChooseOpinionType(String chooseOpinionType) {
		this.chooseOpinionType = chooseOpinionType;
	}
	public String getPigeonholeValue() {
		return pigeonholeValue;
	}
	public void setPigeonholeValue(String pigeonholeValue) {
		this.pigeonholeValue = pigeonholeValue;
	}
	public List<CtpAffair> getValidAffairList() {
		return validAffairList;
	}
	public void setValidAffairList(List<CtpAffair> validAffairList) {
		this.validAffairList = validAffairList;
	}
	public String getJointlyUnit() {
		return jointlyUnit;
	}
	public void setJointlyUnit(String jointlyUnit) {
		this.jointlyUnit = jointlyUnit;
	}
	public String getLeaderPishiFlag() {
		return leaderPishiFlag;
	}
	public void setLeaderPishiFlag(String leaderPishiFlag) {
		this.leaderPishiFlag = leaderPishiFlag;
	}
	public Map<String, String> getExpandParams() {
		return expandParams;
	}
	public void setExpandParams(Map<String, String> expandParams) {
		this.expandParams = expandParams;
	}
	public Map<String, Object> getPishiParams() {
		return pishiParams;
	}
	public void setPishiParams(Map<String, Object> pishiParams) {
		this.pishiParams = pishiParams;
	}
}
