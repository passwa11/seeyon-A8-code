package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocCustomerType extends BaseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -595004874740322118L;
	private long memberId;		
	private long typeId;
	private long bigTypeId;
	private long accountId;
	private long edocType;		//发文或收文
	private String typeName;	//国际化名称
	private String typeCode;	
	
	//以下两个字段是存储列表查询条件和值
	private String condition;
	private String textfield;
	
	private String edocElementLabel; //公文元素名称
	
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getTypeCode() {
		return typeCode;
	}
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	public long getAccountId() {
		return accountId;
	}
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	
	public long getEdocType() {
		return edocType;
	}
	public void setEdocType(long edocType) {
		this.edocType = edocType;
	}
	public long getBigTypeId() {
		return bigTypeId;
	}
	public void setBigTypeId(long bigTypeId) {
		this.bigTypeId = bigTypeId;
	}
	public long getMemberId() {
		return memberId;
	}
	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}
	public long getTypeId() {
		return typeId;
	}
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getTextfield() {
		return textfield;
	}
	public void setTextfield(String textfield) {
		this.textfield = textfield;
	}
	public String getEdocElementLabel() {
		return edocElementLabel;
	}
	public void setEdocElementLabel(String edocElementLabel) {
		this.edocElementLabel = edocElementLabel;
	}

	
}
