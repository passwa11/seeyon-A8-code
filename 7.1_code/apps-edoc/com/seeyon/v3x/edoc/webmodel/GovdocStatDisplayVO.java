package com.seeyon.v3x.edoc.webmodel;

import java.io.Serializable;

public class GovdocStatDisplayVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer statId;
	private Integer statType;
	private String statRangeType;//统计范围类型
	private Long statRangeId;//统计范围ID
	private int listType;//查询维度类型（0.单位，1.个人）
	
	private String displayId;
	private String displayType;
	private String displayName;
	private boolean sfClickOn = true;
	/************* 工作统计 *****************/
	private int sendCount;//发文数
	private long fontSize = 0;//字数
	private int sendDoneCount;//发文已办数
	private int sendPendingCount;//发文办理中
	private String sendDonePer = "0%";//发文办结率
	private int sendOverCount;//发文超期件
	private String sendOverPer = "0%";//发文超期率
	private int recCount;//收文数
	private int recDoneCount;//收文已办结
	private int recPendingCount;//收文办理中
	private String recDonePer = "0%";//收文办结率
	private int recOverCount;//收文超期件数
	private String recOverPer = "0%";//收文超期率
	private int allCount;//总计
	private int allDoneCount;//已办结
	private int allPendingCount;//办理中
	private String allDonePer = "0%";//办结率
	private int allOverCount;//超期件数
	private String allOverPer = "0%";//超期率
	
	/************* 签收统计 *****************/
	private int allNum;//发文总数
	private int noRecSignNum;//未签收数量
	private String noRecSignNumPer = "0%";//未签收的百分比
	private int twoSign;//2个工作日内接收数量
	private String twoSignPer = "0%";//2个工作日内接收百分比
	private int threeSign;//3至5个工作日内接收数量 
	private String threeSignPer = "0%";//3至5个工作日内接收百分比
	private int fiveSign;//5个工作日后接收数量 
	private String fiveSignPer = "0%";//5个工作日后接收百分比
	private int backCount;
	private String backSignPer = "0%";
	
	public int getBackCount() {
		return backCount;
	}
	public void setBackCount(int backCount) {
		this.backCount = backCount;
	}
	public String getBackSignPer() {
		return backSignPer;
	}
	public void setBackSignPer(String backSignPer) {
		this.backSignPer = backSignPer;
	}
	public boolean isSfClickOn() {
		return sfClickOn;
	}
	public void setSfClickOn(boolean sfClickOn) {
		this.sfClickOn = sfClickOn;
	}
	public Integer getStatType() {
		return statType;
	}
	public void setStatType(Integer statType) {
		this.statType = statType;
	}
	public String getDisplayId() {
		return displayId;
	}
	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}
	public String getDisplayType() {
		return displayType;
	}
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getStatRangeType() {
		return statRangeType;
	}
	public void setStatRangeType(String statRangeType) {
		this.statRangeType = statRangeType;
	}
	public Long getStatRangeId() {
		return statRangeId;
	}
	public void setStatRangeId(Long statRangeId) {
		this.statRangeId = statRangeId;
	}
	public int getListType() {
		return listType;
	}
	public void setListType(int listType) {
		this.listType = listType;
	}
	public int getSendCount() {
		return sendCount;
	}
	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	public long getFontSize() {
		return fontSize;
	}
	public void setFontSize(long fontSize) {
		this.fontSize = fontSize;
	}
	public int getSendDoneCount() {
		return sendDoneCount;
	}
	public void setSendDoneCount(int sendDoneCount) {
		this.sendDoneCount = sendDoneCount;
	}
	public int getSendPendingCount() {
		return sendPendingCount;
	}
	public void setSendPendingCount(int sendPendingCount) {
		this.sendPendingCount = sendPendingCount;
	}
	public String getSendDonePer() {
		return sendDonePer;
	}
	public void setSendDonePer(String sendDonePer) {
		this.sendDonePer = sendDonePer;
	}
	public int getSendOverCount() {
		return sendOverCount;
	}
	public void setSendOverCount(int sendOverCount) {
		this.sendOverCount = sendOverCount;
	}
	public String getSendOverPer() {
		return sendOverPer;
	}
	public void setSendOverPer(String sendOverPer) {
		this.sendOverPer = sendOverPer;
	}
	public int getRecCount() {
		return recCount;
	}
	public void setRecCount(int recCount) {
		this.recCount = recCount;
	}
	public int getRecDoneCount() {
		return recDoneCount;
	}
	public void setRecDoneCount(int recDoneCount) {
		this.recDoneCount = recDoneCount;
	}
	public int getRecPendingCount() {
		return recPendingCount;
	}
	public void setRecPendingCount(int recPendingCount) {
		this.recPendingCount = recPendingCount;
	}
	public String getRecDonePer() {
		return recDonePer;
	}
	public void setRecDonePer(String recDonePer) {
		this.recDonePer = recDonePer;
	}
	public int getRecOverCount() {
		return recOverCount;
	}
	public void setRecOverCount(int recOverCount) {
		this.recOverCount = recOverCount;
	}
	public String getRecOverPer() {
		return recOverPer;
	}
	public void setRecOverPer(String recOverPer) {
		this.recOverPer = recOverPer;
	}
	public int getAllCount() {
		return allCount;
	}
	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}
	public int getAllDoneCount() {
		return allDoneCount;
	}
	public void setAllDoneCount(int allDoneCount) {
		this.allDoneCount = allDoneCount;
	}
	public int getAllPendingCount() {
		return allPendingCount;
	}
	public void setAllPendingCount(int allPendingCount) {
		this.allPendingCount = allPendingCount;
	}
	public String getAllDonePer() {
		return allDonePer;
	}
	public void setAllDonePer(String allDonePer) {
		this.allDonePer = allDonePer;
	}
	public int getAllOverCount() {
		return allOverCount;
	}
	public void setAllOverCount(int allOverCount) {
		this.allOverCount = allOverCount;
	}
	public String getAllOverPer() {
		return allOverPer;
	}
	public void setAllOverPer(String allOverPer) {
		this.allOverPer = allOverPer;
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
	public int getTwoSign() {
		return twoSign;
	}
	public void setTwoSign(int twoSign) {
		this.twoSign = twoSign;
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
	public String getFiveSignPer() {
		return fiveSignPer;
	}
	public void setFiveSignPer(String fiveSignPer) {
		this.fiveSignPer = fiveSignPer;
	}
	public Integer getStatId() {
		return statId;
	}
	public void setStatId(Integer statId) {
		this.statId = statId;
	}
}
