package com.seeyon.v3x.edoc.webmodel;


public class WebSignCount {
	private String typeName;//类型名称
	private String deptName;
	private int allNum;//发文总数
	private int noRecSignNum;//未签收数量
	private String noRecSignNumPer;//未签收的百分比
	private int sfShowNoRec;//是否显示（0，不显示，1显示）
	private int twoSign;//2个工作日内接收数量
	private int sfShowTwo;//是否显示（0，不显示，1显示）
	private String twoSignPer;//2个工作日内接收百分比
	private int threeSign;//3至5个工作日内接收数量 
	private int sfShowThree;//是否显示（0，不显示，1显示）
	private String threeSignPer;//3至5个工作日内接收百分比
	private int fiveSign;//5个工作日后接收数量 
	private int sfShowFive;//是否显示（0，不显示，1显示）
	private String fiveSignPer;//5个工作日后接收百分比
	
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public int getAllNum() {
		return allNum;
	}
	public void setAllNum(int allNum) {
		this.allNum = allNum;
	}
	public int getNoRecSignNum() {
		return noRecSignNum;
	}
	public void setNoRecSignNum(int noRecSignNum) {
		this.noRecSignNum = noRecSignNum;
	}
	public String getNoRecSignNumPer() {
		return noRecSignNumPer;
	}
	public void setNoRecSignNumPer(String noRecSignNumPer) {
		this.noRecSignNumPer = noRecSignNumPer;
	}
	public int getSfShowNoRec() {
		return sfShowNoRec;
	}
	public void setSfShowNoRec(int sfShowNoRec) {
		this.sfShowNoRec = sfShowNoRec;
	}
	public int getTwoSign() {
		return twoSign;
	}
	public void setTwoSign(int twoSign) {
		this.twoSign = twoSign;
	}
	public int getSfShowTwo() {
		return sfShowTwo;
	}
	public void setSfShowTwo(int sfShowTwo) {
		this.sfShowTwo = sfShowTwo;
	}
	public String getTwoSignPer() {
		return twoSignPer;
	}
	public void setTwoSignPer(String twoSignPer) {
		this.twoSignPer = twoSignPer;
	}
	public int getThreeSign() {
		return threeSign;
	}
	public void setThreeSign(int threeSign) {
		this.threeSign = threeSign;
	}
	public int getSfShowThree() {
		return sfShowThree;
	}
	public void setSfShowThree(int sfShowThree) {
		this.sfShowThree = sfShowThree;
	}
	public String getThreeSignPer() {
		return threeSignPer;
	}
	public void setThreeSignPer(String threeSignPer) {
		this.threeSignPer = threeSignPer;
	}
	public int getFiveSign() {
		return fiveSign;
	}
	public void setFiveSign(int fiveSign) {
		this.fiveSign = fiveSign;
	}
	public int getSfShowFive() {
		return sfShowFive;
	}
	public void setSfShowFive(int sfShowFive) {
		this.sfShowFive = sfShowFive;
	}
	public String getFiveSignPer() {
		return fiveSignPer;
	}
	public void setFiveSignPer(String fiveSignPer) {
		this.fiveSignPer = fiveSignPer;
	}
	
	
	
	
}
