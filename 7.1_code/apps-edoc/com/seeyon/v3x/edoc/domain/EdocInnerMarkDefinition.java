package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import com.seeyon.v3x.common.domain.BaseModel;

public class EdocInnerMarkDefinition extends BaseModel implements Serializable{
	private static final long serialVersionUID = -4501908046060201594L;
	//	private String prefixNo;
	private Integer minNo;
	private Integer currentNo;
	private Integer maxNo;
	private String expression;
	private Integer length;
	private Boolean yearEnabled;
	private Integer type;
	private String wordNo;
	private Long domainId;
	public Integer getCurrentNo() {
		return currentNo;
	}
	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
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
	public Integer getMaxNo() {
		return maxNo;
	}
	public void setMaxNo(Integer maxNo) {
		this.maxNo = maxNo;
	}
	public Integer getMinNo() {
		return minNo;
	}
	public void setMinNo(Integer minNo) {
		this.minNo = minNo;
	}

//	public String getPrefixNo() {
//		return prefixNo;
//	}
//	public void setPrefixNo(String prefixNo) {
//		this.prefixNo = prefixNo;
//	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Boolean getYearEnabled() {
		return yearEnabled;
	}
	public void setYearEnabled(Boolean yearEnabled) {
		this.yearEnabled = yearEnabled;
	}
	public String getWordNo() {
		return wordNo;
	}
	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
	}

}
