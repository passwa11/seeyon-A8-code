package com.seeyon.v3x.edoc.webmodel;

public class EdocMarkNoModel {

	private Long definitionId;//文号定义ID
	private Long edocMarkId;//文号记录id
	
	private String markNo;//文号
	
	private Integer currentNo;//文号当前号
	private Integer markNumber;//文号序号
	private String fullMarkNumber;//文号编号
	private String wordNo;//机构字
	private String yearNo;//年
	private String left;//左括号
	private String right;//右括号
	private String suffix;//号
	private boolean yearEnabled;//是否按年号
	private boolean twoYear;//可跨前后两年
	private int markLength;//文号编号长度
	private int status;
	
	//大流水还是小流水
	private Integer markCategoryCodeMode;
	
	
	public Integer getMarkCategoryCodeMode() {
		return markCategoryCodeMode;
	}

	public void setMarkCategoryCodeMode(Integer markCategoryCodeMode) {
		this.markCategoryCodeMode = markCategoryCodeMode;
	}

	public Long getEdocMarkId() {
		return edocMarkId;
	}
	
	public void setEdocMarkId(Long edocMarkId) {
		this.edocMarkId = edocMarkId;
	}
	
	public String getMarkNo() {
		return markNo;
	}

	public void setMarkNo(String markNo) {
		this.markNo = markNo;
	}

	public Integer getMarkNumber() {
		return markNumber;
	}

	public void setMarkNumber(Integer markNumber) {
		this.markNumber = markNumber;
	}

	public String getFullMarkNumber() {
		return fullMarkNumber;
	}

	public void setFullMarkNumber(String fullMarkNumber) {
		this.fullMarkNumber = fullMarkNumber;
	}

	public String getYearNo() {
		if(this.yearNo == null) {
			this.yearNo = "";
		}
		return yearNo;
	}

	public void setYearNo(String yearNo) {
		if(this.yearNo == null) {
			this.yearNo = "";
		}
		this.yearNo = yearNo;
	}

	public String getWordNo() {
		if(this.wordNo == null) {
			this.wordNo = "";
		}
		return wordNo;
	}

	public void setWordNo(String wordNo) {
		if(this.wordNo == null) {
			this.wordNo = "";
		}
		this.wordNo = wordNo;
	}

	public String getLeft() {
		if(this.left == null) {
			this.left = "";
		}
		return left;
	}

	public void setLeft(String left) {
		if(this.left == null) {
			this.left = "";
		}
		this.left = left;
	}

	public String getRight() {
		if(this.right == null) {
			this.right = "";
		}
		return right;
	}

	public void setRight(String right) {
		this.right = right;
		if(this.right == null) {
			this.right = "";
		}
	}

	public String getSuffix() {
		if(this.suffix == null) {
			this.suffix = "";
		}
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
		if(this.suffix == null) {
			this.suffix = "";
		}
	}

	public Long getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
	}

	public boolean isYearEnabled() {
		return yearEnabled;
	}

	public void setYearEnabled(boolean yearEnabled) {
		this.yearEnabled = yearEnabled;
	}

	public boolean isTwoYear() {
		return twoYear;
	}

	public void setTwoYear(boolean twoYear) {
		this.twoYear = twoYear;
	}

	public int getMarkLength() {
		return markLength;
	}

	public void setMarkLength(int markLength) {
		this.markLength = markLength;
	}

	public Integer getCurrentNo() {
		return currentNo;
	}

	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
