package com.seeyon.v3x.edoc.webmodel;

public class EdocInnerMarkModel {

	private Long id;
	private String prefixNo;
	private Integer minNo;
	private Integer maxNo;
	private Integer currentNo;
	private String expression;
	private Boolean yearEnabled;
	private Integer type;
	private Integer length;

	public Integer getCurrentNo() {
		return currentNo;
	}
	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
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
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public String getPrefixNo() {
		return prefixNo;
	}
	public void setPrefixNo(String prefixNo) {
		this.prefixNo = prefixNo;
	}
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

	
	
}
