package com.seeyon.apps.govdoc.mark.vo;

public class GovdocMarkTemplate {
	
	private Integer selectType = 0;
	private Integer markTpye;
	private Long markDefId;
	private String wordNo;
	private String markstr;
	private Integer markNumber;
	private String fieldName;
	
	public Integer getSelectType() {
		return selectType;
	}
	public void setSelectType(Integer selectType) {
		this.selectType = selectType;
	}
	public Integer getMarkTpye() {
		return markTpye;
	}
	public void setMarkTpye(Integer markTpye) {
		this.markTpye = markTpye;
	}
	public Long getMarkDefId() {
		return markDefId;
	}
	public void setMarkDefId(Long markDefId) {
		this.markDefId = markDefId;
	}
	public String getWordNo() {
		return wordNo;
	}
	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
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
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}	
}
