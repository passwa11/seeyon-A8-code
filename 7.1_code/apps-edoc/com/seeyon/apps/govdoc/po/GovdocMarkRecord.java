package com.seeyon.apps.govdoc.po;


import java.sql.Timestamp;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.po.BasePO;

/**
 * 公文文号记录表
 * 公文-公文文号/公文-内部文号/公文-签收编号 1V1
 */
public class GovdocMarkRecord extends BasePO {

	private static final long serialVersionUID = 7913008790637726311L;
	
	private Long id;
	private Integer govdocType;
	private Long summaryId;
	private Long formDataId;
	private Long categoryId;
	private Long markDefId;
	private Long callId;
	private Integer markType;
	private String markstr;
	private String wordNo;
	private String yearNo;
	private Integer markNumber;
	private Integer selectType;//0手工输入 1自动选择文号 2断号 3预留文号
	private Long domainId;
	private Timestamp createTime;
	private Timestamp lastTime;
	private Long createUserId;
	private Long lastUserId;
	private Integer flowState = 0;//流程状态
	private Integer usedState = 0;//使用状态0非占用 1占用
	private Integer usedType = 1;//使用类型 1模式1  2模式2
	private Integer newflowType;//0主流程 1子流程 2自动触发
	private Long parentSummaryId = null;//主流程GovdocMarkRecord的ID
	private String childSummaryId = "";//子流程GovdocMarkRecord的ID
	
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
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getGovdocType() {
		return govdocType;
	}
	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
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
	public Long getMarkDefId() {
		return markDefId;
	}
	public void setMarkDefId(Long markDefId) {
		this.markDefId = markDefId;
	}
	public String getMarkstr() {
		return markstr;
	}
	public void setMarkstr(String markstr) {
		this.markstr = markstr;
	}
	public Integer getMarkNumber() {
		return markNumber;
	}
	public void setMarkNumber(Integer markNumber) {
		this.markNumber = markNumber;
	}
	public Integer getSelectType() {
		return selectType;
	}
	public void setSelectType(Integer selectType) {
		this.selectType = selectType;
	}
	public Integer getMarkType() {
		return markType;
	}
	public void setMarkType(Integer markType) {
		this.markType = markType;
	}

	public Integer getUsedState() {
		return usedState;
	}
	public void setUsedState(Integer usedState) {
		this.usedState = usedState;
	}
	public Integer getUsedType() {
		return usedType;
	}
	public void setUsedType(Integer usedType) {
		this.usedType = usedType;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	public Long getCallId() {
		return callId;
	}
	public void setCallId(Long callId) {
		this.callId = callId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getLastTime() {
		return lastTime;
	}
	public void setLastTime(Timestamp lastTime) {
		this.lastTime = lastTime;
	}
	public Long getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}
	public Long getLastUserId() {
		return lastUserId;
	}
	public void setLastUserId(Long lastUserId) {
		this.lastUserId = lastUserId;
	}
	public String getYearNo() {
		return yearNo;
	}
	public void setYearNo(String yearNo) {
		this.yearNo = yearNo;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public String getWordNo() {
		return wordNo;
	}
	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
	}
	public Integer getFlowState() {
		return flowState;
	}
	public void setFlowState(Integer flowState) {
		this.flowState = flowState;
	}
	public Integer getNewflowType() {
		return newflowType;
	}
	public void setNewflowType(Integer newflowType) {
		this.newflowType = newflowType;
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
}
