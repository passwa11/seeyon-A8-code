package com.seeyon.apps.govdoc.mark.vo;

import java.util.Date;
import java.util.List;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.govdoc.po.GovdocMarkRecord;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;

public class GovdocMarkVO {

	private String from;
	private Long templateMarkDefId;
	private Long markDefId;
	private String wordNo;
	private Integer markType;
	private String expression;
	private Integer length;
	private Integer sortNo;
	private Long categoryId;
	private String categoryName;
	private Short categoryCodeMode = 0;//0小流水 1大流水
	private Integer currentNo;
	private Integer minNo;
	private Integer maxNo;
	private Boolean readonly = false;
	private Boolean yearEnabled = true;
	private Integer twoYear = 0;
	private Long domainId;
	
	private String markstr;//当前文号
	private String markReserveUp;//预留文号
	private String markReserveDown;//线下占用文号
	private String aclEntityName;//授权
	private String description;//预留说明
	
	private Long callId;//断号/占号ID
	private Long reserveId;//断号/占号ID
	private Integer selectType;
	private Integer markNumber;
	private String yearNo;
	private String left;
	private String right;
	private String suffix;
	
	private String fieldName; 
	private Boolean isTemplate = false;
	private Boolean isSysTemplate = false;
	private Boolean isEnable = false;
	private Boolean isMapping = false;
	private boolean isFensong = false;
	private Boolean isIncreatement = true;
	private Integer govdocType = 1;
	private Integer edocType = 0;
	private Long summaryId;
	private Long formDataId;
	private User currentUser;
	private Short markDefStatus = 0;
	private Integer flowState = 0;
	private Integer usedState = 0;
	private Integer usedType = 1;
	private Integer jianbanType = 1;
	private Integer newflowType = 0;
	private Long parentSummaryId = null;
	private String childSummaryId = "";
	
	private boolean isLastNode = false;
	private String action = "";
	private String dealAction = "";
	private String subject = "";
	private String selectTypeName = "";
	private Date createTime;
	
	private EdocMarkDefinition markDef;
	private GovdocMarkRecord record;
	private List<EdocMark> markList;
	private List<EdocMarkHistory> historyList;
	private boolean needQueryHistory = true;
	
	private boolean isMarkShowCall_0 = false;
	private boolean isMarkHandInput_0 = false;
	private boolean isMarkFinish_0_1 = false;//公文文号-发文是否开启流程结束占号
	private boolean isMarkCheckCall_0_1 = false;//公文文号-发文是否启用文号使用提醒
	private boolean isMarkCheckCall_0_2 = false;//公文文号-签报是否启用文号使用提醒
	private String markUsedType_0_1 = "1";
	private String markUsedType_0_2 = "2";
	
	private boolean isMarkShowCall_1 = false;
	private boolean isMarkHandInput_1 = false;
	private boolean isMarkCheckCall_1_2 = false;//内部文号-签报/收文是否启用文号使用提醒
	private String markUsedType_1_2 = "2";

	private boolean isMarkShowCall_2 = false;
	private boolean isMarkHandInput_2 = false;
	private boolean isMarkCheckCall_2 = false;
	private boolean isMarkCheckCall_2_1 = false;//签收编号-发文是否启用文号使用提醒
	private boolean isMarkCheckCall_2_2 = false;//签收编号-签报/收文是否启用文号使用提醒
	private String markUsedType_2_1 = "1";
	private String markUsedType_2_2 = "2";
	
	public void toObject(Object[] object) {
		int n = 0;
		this.setMarkDefId((Long)object[n++]);
		this.setWordNo((String)object[n++]);
		this.setMarkType((Integer)object[n++]);
		this.setExpression((String)object[n++]);
		this.setLength((Integer)object[n++]);
		this.setSortNo((Integer)object[n++]);
		this.setCategoryId((Long)object[n++]);
		this.setCategoryName((String)object[n++]);
		this.setCategoryCodeMode((Short)object[n++]);
		this.setCurrentNo((Integer)object[n++]);
		this.setMinNo((Integer)object[n++]);
		this.setMaxNo((Integer)object[n++]);
		this.setReadonly((Boolean)object[n++]);
		this.setYearEnabled((Boolean)object[n++]);
		this.setTwoYear((Integer)object[n++]);
		this.setDomainId((Long)object[n++]);
	}

	public boolean isFromDraft() {
		if("draft".equals(action)) {
			return true;
		}
		return false;
	}
	public boolean isFromSend() {
		if("send".equals(action) && !this.isFensong()) {
			return true;
		}
		return false;
	}
	public boolean isFromDeal() {
		if("deal".equals(action) || "zcdb".equals(action)) {
			return true;
		}
		return false;
	}
	public boolean isSubmit() {
		if("deal".equals(action) && !this.isFensong()) {
			return true;
		}
		return false;
	}
	public boolean isFawen() {
		if(govdocType!=null && govdocType.intValue() == 1) {
			return true;
		}
		return false;
	}
	
	public boolean isFensong() {
		if("draft".equals(action)) {
			return false;
		}
		return isFensong;
	}
	public void setFensong(boolean isFensong) {
		this.isFensong = isFensong;
	}
	public boolean isFromTrigger() {
		if(Strings.isNotBlank(this.from) && ("child".equals(this.from)||"auto".equals(this.from))) {
			return true;
		}
		return false;
	}
	
	public boolean isFromTriggerChild() {
		if(Strings.isNotBlank(this.from) && ("child".equals(this.from))) {
			return true;
		}
		return false;
	}
	
	public boolean isChildFlow() {
		if(this.newflowType != null && this.newflowType.intValue()==EdocConstant.NewflowType.child.ordinal()) {
			return true;
		}
		return false;
	}
	
	public boolean isParentFlow() {
		if(this.newflowType != null && this.newflowType.intValue()==EdocConstant.NewflowType.main.ordinal()) {
			return true;
		}
		return false;
	}
	
	public boolean isNeedFinish() {
		return isLastNode;
	}	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Long getMarkDefId() {
		return markDefId;
	}
	public void setMarkDefId(Long markDefId) {
		this.markDefId = markDefId;
	}
	public String getWordNo() {
		if(wordNo == null) {
			return "";
		}
		return wordNo;
	}
	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
		if(this.wordNo == null) {
			this.wordNo = "";
		}
	}
	public Integer getMarkType() {
		return markType;
	}
	public void setMarkType(Integer markType) {
		this.markType = markType;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public Integer getSortNo() {
		return sortNo;
	}
	public void setSortNo(Integer sortNo) {
		this.sortNo = sortNo;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public Short getCategoryCodeMode() {
		return categoryCodeMode;
	}
	public void setCategoryCodeMode(Short categoryCodeMode) {
		this.categoryCodeMode = categoryCodeMode;
	}
	public Integer getCurrentNo() {
		return currentNo;
	}
	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
	}
	public Integer getMinNo() {
		return minNo;
	}
	public void setMinNo(Integer minNo) {
		this.minNo = minNo;
	}
	public Integer getMaxNo() {
		return maxNo;
	}
	public void setMaxNo(Integer maxNo) {
		this.maxNo = maxNo;
	}
	public Boolean getReadonly() {
		return readonly;
	}
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}
	public Boolean getYearEnabled() {
		return yearEnabled;
	}
	public void setYearEnabled(Boolean yearEnabled) {
		this.yearEnabled = yearEnabled;
	}
	public Boolean isTwoYear() {
		return (twoYear!=null && twoYear.intValue() == 1);
	}
	public Integer getTwoYear() {
		return twoYear;
	}
	public void setTwoYear(Integer twoYear) {
		this.twoYear = twoYear;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	public String getMarkstr() {
		return markstr;
	}
	public void setMarkstr(String markstr) {
		this.markstr = markstr;
	}
	public String getMarkReserveUp() {
		return markReserveUp;
	}
	public void setMarkReserveUp(String markReserveUp) {
		this.markReserveUp = markReserveUp;
	}
	public String getMarkReserveDown() {
		return markReserveDown;
	}
	public void setMarkReserveDown(String markReserveDown) {
		this.markReserveDown = markReserveDown;
	}
	public String getAclEntityName() {
		return aclEntityName;
	}
	public void setAclEntityName(String aclEntityName) {
		this.aclEntityName = aclEntityName;
	}
	public Integer getSelectType() {
		return selectType;
	}
	public void setSelectType(Integer selectType) {
		this.selectType = selectType;
	}
	public Integer getMarkNumber() {
		return markNumber;
	}
	public void setMarkNumber(Integer markNumber) {
		this.markNumber = markNumber;
	}
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getYearNo() {
		return yearNo;
	}
	public void setYearNo(String yearNo) {
		this.yearNo = yearNo;
	}
	public Long getCallId() {
		return callId;
	}
	public void setCallId(Long callId) {
		this.callId = callId;
	}
	public Boolean getIsMapping() {
		return isMapping;
	}
	public void setIsMapping(Boolean isMapping) {
		this.isMapping = isMapping;
	}
	public Boolean getIsEnable() {
		return isEnable;
	}
	public void setIsEnable(Boolean isEnable) {
		this.isEnable = isEnable;
	}
	public Integer getGovdocType() {
		return govdocType;
	}
	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
	}
	public Integer getEdocType() {
		return edocType;
	}
	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}
	public User getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public Long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}
	public Integer getUsedState() {
		return usedState;
	}
	public void setUsedState(Integer usedState) {
		this.usedState = usedState;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getDealAction() {
		return dealAction;
	}
	public void setDealAction(String dealAction) {
		this.dealAction = dealAction;
	}
	public Long getReserveId() {
		return reserveId;
	}
	public void setReserveId(Long reserveId) {
		this.reserveId = reserveId;
	}
	public boolean isLastNode() {
		return isLastNode;
	}
	public void setLastNode(boolean isLastNode) {
		this.isLastNode = isLastNode;
	}
	public Short getMarkDefStatus() {
		return markDefStatus;
	}
	public void setMarkDefStatus(Short markDefStatus) {
		this.markDefStatus = markDefStatus;
	}
	public EdocMarkDefinition getMarkDef() {
		return markDef;
	}
	public void setMarkDef(EdocMarkDefinition markDef) {
		this.markDef = markDef;
	}
	public GovdocMarkRecord getRecord() {
		return record;
	}
	public void setRecord(GovdocMarkRecord record) {
		this.record = record;
	}
	public boolean getIsMarkShowCall_0() {
		return isMarkShowCall_0;
	}
	public void setIsMarkShowCall_0(boolean isMarkShowCall_0) {
		this.isMarkShowCall_0 = isMarkShowCall_0;
	}
	public boolean getIsMarkHandInput_0() {
		return isMarkHandInput_0;
	}
	public void setIsMarkHandInput_0(boolean isMarkHandInput_0) {
		this.isMarkHandInput_0 = isMarkHandInput_0;
	}
	public boolean getIsMarkFinish_0_1() {
		return isMarkFinish_0_1;
	}
	public void setIsMarkFinish_0_1(boolean isMarkFinish_0_1) {
		this.isMarkFinish_0_1 = isMarkFinish_0_1;
	}
	public boolean getIsMarkCheckCall_0_1() {
		return isMarkCheckCall_0_1;
	}
	public void setIsMarkCheckCall_0_1(boolean isMarkCheckCall_0_1) {
		this.isMarkCheckCall_0_1 = isMarkCheckCall_0_1;
	}
	public boolean getIsMarkCheckCall_0_2() {
		return isMarkCheckCall_0_2;
	}
	public void setIsMarkCheckCall_0_2(boolean isMarkCheckCall_0_2) {
		this.isMarkCheckCall_0_2 = isMarkCheckCall_0_2;
	}
	public boolean getIsMarkShowCall_1() {
		return isMarkShowCall_1;
	}
	public void setIsMarkShowCall_1(boolean isMarkShowCall_1) {
		this.isMarkShowCall_1 = isMarkShowCall_1;
	}
	public boolean getIsMarkHandInput_1() {
		return isMarkHandInput_1;
	}
	public void setIsMarkHandInput_1(boolean isMarkHandInput_1) {
		this.isMarkHandInput_1 = isMarkHandInput_1;
	}
	public boolean getIsMarkCheckCall_1_2() {
		return isMarkCheckCall_1_2;
	}
	public void setIsMarkCheckCall_1_2(boolean isMarkCheckCall_1_2) {
		this.isMarkCheckCall_1_2 = isMarkCheckCall_1_2;
	}
	public boolean getIsMarkShowCall_2() {
		return isMarkShowCall_2;
	}
	public void setIsMarkShowCall_2(boolean isMarkShowCall_2) {
		this.isMarkShowCall_2 = isMarkShowCall_2;
	}
	public boolean getIsMarkHandInput_2() {
		return isMarkHandInput_2;
	}
	public void setIsMarkHandInput_2(boolean isMarkHandInput_2) {
		this.isMarkHandInput_2 = isMarkHandInput_2;
	}
	public boolean getIsMarkCheckCall_2() {
		return isMarkCheckCall_2;
	}
	public void setIsMarkCheckCall_2(boolean isMarkCheckCall_2) {
		this.isMarkCheckCall_2 = isMarkCheckCall_2;
	}
	public boolean getIsMarkCheckCall_2_1() {
		return isMarkCheckCall_2_1;
	}
	public void setIsMarkCheckCall_2_1(boolean isMarkCheckCall_2_1) {
		this.isMarkCheckCall_2_1 = isMarkCheckCall_2_1;
	}
	public boolean getIsMarkCheckCall_2_2() {
		return isMarkCheckCall_2_2;
	}
	public void setIsMarkCheckCall_2_2(boolean isMarkCheckCall_2_2) {
		this.isMarkCheckCall_2_2 = isMarkCheckCall_2_2;
	}
	public String getMarkUsedType_0_1() {
		return markUsedType_0_1;
	}
	public void setMarkUsedType_0_1(String markUsedType_0_1) {
		this.markUsedType_0_1 = markUsedType_0_1;
	}
	public String getMarkUsedType_0_2() {
		return markUsedType_0_2;
	}
	public void setMarkUsedType_0_2(String markUsedType_0_2) {
		this.markUsedType_0_2 = markUsedType_0_2;
	}
	public String getMarkUsedType_1_2() {
		return markUsedType_1_2;
	}
	public void setMarkUsedType_1_2(String markUsedType_1_2) {
		this.markUsedType_1_2 = markUsedType_1_2;
	}
	public String getMarkUsedType_2_1() {
		return markUsedType_2_1;
	}
	public void setMarkUsedType_2_1(String markUsedType_2_1) {
		this.markUsedType_2_1 = markUsedType_2_1;
	}
	public String getMarkUsedType_2_2() {
		return markUsedType_2_2;
	}
	public void setMarkUsedType_2_2(String markUsedType_2_2) {
		this.markUsedType_2_2 = markUsedType_2_2;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSelectTypeName() {
		return selectTypeName;
	}
	public void setSelectTypeName(String selectTypeName) {
		this.selectTypeName = selectTypeName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Integer getUsedType() {
		return usedType;
	}
	public void setUsedType(Integer usedType) {
		this.usedType = usedType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getIsTemplate() {
		return isTemplate;
	}
	public void setIsTemplate(Boolean isTemplate) {
		this.isTemplate = isTemplate;
	}
	public Boolean getIsSysTemplate() {
		return isSysTemplate;
	}
	public void setIsSysTemplate(Boolean isSysTemplate) {
		this.isSysTemplate = isSysTemplate;
	}
	public List<EdocMarkHistory> getHistoryList() {
		return historyList;
	}
	public void setHistoryList(List<EdocMarkHistory> historyList) {
		this.historyList = historyList;
	}
	public List<EdocMark> getMarkList() {
		return markList;
	}
	public void setMarkList(List<EdocMark> markList) {
		this.markList = markList;
	}
	public Integer getFlowState() {
		return flowState;
	}
	public void setFlowState(Integer flowState) {
		this.flowState = flowState;
	}
	public Long getTemplateMarkDefId() {
		return templateMarkDefId;
	}
	public void setTemplateMarkDefId(Long templateMarkDefId) {
		this.templateMarkDefId = templateMarkDefId;
	}
	public Integer getJianbanType() {
		return jianbanType;
	}
	public void setJianbanType(Integer jianbanType) {
		this.jianbanType = jianbanType;
	}
	public Integer getNewflowType() {
		return newflowType;
	}
	public void setNewflowType(Integer newflowType) {
		this.newflowType = newflowType;
	}	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public Long getParentSummaryId() {
		return parentSummaryId;
	}
	public void setParentSummaryId(Long parentSummaryId) {
		this.parentSummaryId = parentSummaryId;
	}
	public String getChildSummaryId() {
		return childSummaryId;
	}
	public void setChildSummaryId(String childSummaryId) {
		this.childSummaryId = childSummaryId;
	}
	public void setIsIncreatement(Boolean isIncreatement) {
		this.isIncreatement = isIncreatement;
	}
	public Boolean getIsIncreatement() {
		return isIncreatement;
	}
	public boolean isNeedQueryHistory() {
		return needQueryHistory;
	}
	public void setNeedQueryHistory(boolean needQueryHistory) {
		this.needQueryHistory = needQueryHistory;
	}

}
