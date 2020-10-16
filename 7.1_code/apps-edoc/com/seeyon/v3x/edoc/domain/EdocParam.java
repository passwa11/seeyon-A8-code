package com.seeyon.v3x.edoc.domain;

import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;

public class EdocParam {
	//业务ID
	private Long summaryId;
	private String docMark;
	//手写或其他方式返回的字符串
	private String markStr;
	
	/**
	 * markType==FormFieldComEnum.base_mark 公文文号
	 * markType==FormFieldComEnum.base_inner_mark 内部文号
	 * markType == FormFieldComEnum.base_sign_mark签收编号
	 */
	private FormFieldComEnum markType;
	private String mappingField;
	private int markNum = 1;// 公文编号 TODO 跟踪老公文，发现要传这个值，但一直没有找到具体怎么用，默认情况都赋值为1，后期业务再改善
	private int govdocType = 0;
	private boolean isExchange = false;
	private String action;
	private EdocSummary summary;
	
	public int getMarkNum() {
		return markNum;
	}

	public void setMarkNum(int markNum) {
		this.markNum = markNum;
	}
	//内部文号自增数量
	Integer innerCount = 0;
	//签收编号自增数量
	Integer signCount = 0;
	
	public EdocParam(){	}
	
	public Integer getInnerCount() {
		return innerCount;
	}

	public void setInnerCount(Integer innerCount) {
		this.innerCount = innerCount;
	}

	public Integer getSignCount() {
		return signCount;
	}

	public void setSignCount(Integer signCount) {
		this.signCount = signCount;
	}

	public EdocParam(Long summaryId,String docMark,FormFieldComEnum markType,String mappingField) {
		this.summaryId = summaryId;
		this.docMark = docMark;
		this.markType = markType;
		this.mappingField = mappingField;
	}
	public EdocParam(Long summaryId,String markStr,FormFieldComEnum markType) {
		this.summaryId = summaryId;
		this.markStr = markStr;
		this.markType = markType;
	}
	
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getMarkStr() {
		return markStr;
	}
	public void setMarkStr(String markStr) {
		this.markStr = markStr;
	}
	public FormFieldComEnum getMarkType() {
		return markType;
	}
	public void setMarkType(FormFieldComEnum markType) {
		this.markType = markType;
	}

	public int getGovdocType() {
		return govdocType;
	}

	public void setGovdocType(int govdocType) {
		this.govdocType = govdocType;
	}

	public String getMappingField() {
		return mappingField;
	}

	public void setMappingField(String mappingField) {
		this.mappingField = mappingField;
	}

	public boolean isExchange() {
		return isExchange;
	}

	public void setExchange(boolean isExchange) {
		this.isExchange = isExchange;
	}

	public EdocSummary getSummary() {
		return summary;
	}

	public void setSummary(EdocSummary summary) {
		this.summary = summary;
	}

	public String getDocMark() {
		return docMark;
	}

	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
