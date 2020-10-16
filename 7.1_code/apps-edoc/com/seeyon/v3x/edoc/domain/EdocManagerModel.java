package com.seeyon.v3x.edoc.domain;

import java.util.List;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.po.affair.CtpAffair;

public class EdocManagerModel {
	private long formId; 
	private long registerId;
	private boolean isExsit = false;
	private EdocRegister edocRegister;
	private long nodeId;
	private String supervisorNames;
	private String edocMangerID;
	private String processChangeMessage; 
	private String recSummaryIdStr;
    private EdocSummary edocSummary;
    private List<Long> markDefinitionIdList;
    private Long automaticRegisterUserId = null;
    private String automaticRegisterMsg = "";
    private CtpAffair affair;
    private String processId;
    private long summaryId;
    private User user;
    private String ret;
    private String popNodeSelected;
    private String popNodeCondition;
    private boolean isNewSent;
    private EdocBody body;
    private EdocOpinion senderOninion;
    private String attaFlag;
    private Integer affairTrack;
    private String optionWay;
    
	public String getProcessChangeMessage() {
		return processChangeMessage;
	}

	public void setProcessChangeMessage(String processChangeMessage) {
		this.processChangeMessage = processChangeMessage;
	}

	public String getEdocMangerID() {
		return edocMangerID;
	}

	public void setEdocMangerID(String edocMangerID) {
		this.edocMangerID = edocMangerID;
	}

	public String getPopNodeCondition() {
		return popNodeCondition;
	}

	public void setPopNodeCondition(String popNodeCondition) {
		this.popNodeCondition = popNodeCondition;
	}

	public String getPopNodeSelected() {
		return popNodeSelected;
	}

	public void setPopNodeSelected(String popNodeSelected) {
		this.popNodeSelected = popNodeSelected;
	}

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	public String getSupervisorNames() {
		return supervisorNames;
	}

	public void setSupervisorNames(String supervisorNames) {
		this.supervisorNames = supervisorNames;
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	
	public long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(long summaryId) {
		this.summaryId = summaryId;
	}

	private String oldOpinionIdStr;

	public String getOldOpinionIdStr() {
		return oldOpinionIdStr;
	}

	public void setOldOpinionIdStr(String oldOpinionIdStr) {
		this.oldOpinionIdStr = oldOpinionIdStr;
	}

	private boolean flag = false;

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	private EdocOpinion signOpinion;

	public EdocOpinion getSignOpinion() {
		return signOpinion;
	}

	public void setSignOpinion(EdocOpinion signOpinion) {
		this.signOpinion = signOpinion;
	}

	private long affairId;

	public CtpAffair getAffair() {
		return affair;
	}

	public void setAffair(CtpAffair affair) {
		this.affair = affair;
	}

	public long getAffairId() {
		return affairId;
	}

	public void setAffairId(long affairId) {
		this.affairId = affairId;
	}

	public EdocBody getBody() {
		return body;
	}

	public void setBody(EdocBody body) {
		this.body = body;
	}

	public EdocOpinion getSenderOninion() {
		return senderOninion;
	}

	public void setSenderOninion(EdocOpinion senderOninion) {
		this.senderOninion = senderOninion;
	}

	public boolean isNewSent() {
		return isNewSent;
	}

	public void setNewSent(boolean isNewSent) {
		this.isNewSent = isNewSent;
	}

	public String getAutomaticRegisterMsg() {
		return automaticRegisterMsg;
	}

	public void setAutomaticRegisterMsg(String automaticRegisterMsg) {
		this.automaticRegisterMsg = automaticRegisterMsg;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getAutomaticRegisterUserId() {
		return automaticRegisterUserId;
	}

	public void setAutomaticRegisterUserId(Long automaticRegisterUserId) {
		this.automaticRegisterUserId = automaticRegisterUserId;
	}


	public EdocRegister getEdocRegister() {
		return edocRegister;
	}

	public String getRecSummaryIdStr() {
		return recSummaryIdStr;
	}

	public void setRecSummaryIdStr(String recSummaryIdStr) {
		this.recSummaryIdStr = recSummaryIdStr;
	}

	public EdocSummary getEdocSummary() {
		return edocSummary;
	}

	public void setEdocSummary(EdocSummary edocSummary) {
		this.edocSummary = edocSummary;
	}

	public List<Long> getMarkDefinitionIdList() {
		return markDefinitionIdList;
	}

	public void setMarkDefinitionIdList(List<Long> markDefinitionIdList) {
		this.markDefinitionIdList = markDefinitionIdList;
	}

	public String getAttaFlag() {
		return attaFlag;
	}

	public void setAttaFlag(String attaFlag) {
		this.attaFlag = attaFlag;
	}

	public void setEdocRegister(EdocRegister edocRegister) {
		this.edocRegister = edocRegister;
	}

	public boolean isExsit() {
		return isExsit;
	}

	public void setExsit(boolean isExsit) {
		this.isExsit = isExsit;
	}

	public long getFormId() {
		return formId;
	}

	public void setFormId(long formId) {
		this.formId = formId;
	}

	public long getRegisterId() {
		return registerId;
	}

	public void setRegisterId(long registerId) {
		this.registerId = registerId;
	}

	public Integer getAffairTrack() {
		return affairTrack;
	}

	public void setAffairTrack(Integer affairTrack) {
		this.affairTrack = affairTrack;
	}

	public String getOptionWay() {
		return optionWay;
	}

	public void setOptionWay(String optionWay) {
		this.optionWay = optionWay;
	}
	
}
