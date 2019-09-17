package com.seeyon.v3x.edoc.webmodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.organization.bo.V3xOrgUnit;

public class GovdocStatSetVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 节点权限过滤 */
	private String fawenNodePolicy;
	private String shouwenNodePolicy;
	private List<Long> statRootIdList;
	private List<V3xOrgUnit> statRootList;
	private List<Long> statRangeList;
	private Map<Long, Long> statRangeMap;
	
	/**显示开关设置**/
	/************* 工作统计 *****************/
	private boolean isShowSend = false;//是否显示发文模块（0，显示，1显示）
	private boolean isShowRec = false;//是否显示收文模块（0，显示，1显示）
	private boolean isShowTotal = false;//是否显示总计（0，显示，1显示）
	private boolean isShowSendCount = false;//是否显示发文数（0，显示，1显示）
	private boolean isShowFontSize = false;//是否显示字数（0，显示，1显示）
	private boolean isShowSendDCount = false;//是否显示发文已办数（0，显示，1显示）
	private boolean isShowSendPCount = false;//是否显示发文办理中（0，显示，1显示）
	private boolean isShowSendDPer = false;//是否显示发文办结率（0，显示，1显示）
	private boolean isShowSendOCount = false;//是否显示发文超期件（0，显示，1显示）
	private boolean isShowSendOper = false;//是否显示发文超期率（0，显示，1显示）
	private boolean isShowRecCount = false;//是否显示收文数（0，显示，1显示）
	private boolean isShowRecDCount = false;//是否显示收文已办结（0，显示，1显示）
	private boolean isShowRecPCount = false;//是否显示收文办理中（0，显示，1显示）
	private boolean isShowRecDper = false;//是否显示收文办结率（0，显示，1显示）
	private boolean isShowRecOCount = false;//是否显示收文超期件数（0，显示，1显示）
	private boolean isShowRecOper = false;//是否显示收文超期率（0，显示，1显示）
	private boolean isShowAllCount = false;//是否显示总计（0，显示，1显示）
	private boolean isShowAllDCount = false;//是否显示已办结（0，显示，1显示）
	private boolean isShowAllPCount = false;//是否显示办理中（0，显示，1显示）
	private boolean isShowAllDPer = false;//是否显示办结率（0，显示，1显示）
	private boolean isShowAllOCount = false;//是否显示超期件数（0，显示，1显示）
	private boolean isShowAllOper = false;//是否显示超期率（0，显示，1显示）
	
	private int sendTdSize = 0;
	private int recTdSize = 0;
	private int totalTdSize = 0;
	
	/************* 签收统计 *****************/
	private boolean isSfShowback = false;//是否显示（0，不显示，1显示）
	private boolean isSfShowNoRec = false;//是否显示（0，不显示，1显示）
	private boolean isSfShowTwo = false;//是否显示（0，不显示，1显示）
	private boolean isSfShowThree = false;//是否显示（0，不显示，1显示）
	private boolean isSfShowFive = false;//是否显示（0，不显示，1显示）
	private boolean isSfShowZj = false;//是否显示总计（0，不显示，1显示）
	
	public boolean isSfShowZj() {
		return isSfShowZj;
	}
	public void setSfShowZj(boolean isSfShowZj) {
		this.isSfShowZj = isSfShowZj;
	}
	public boolean isShowSend() {
		return isShowSend;
	}
	public void setShowSend(boolean isShowSend) {
		this.isShowSend = isShowSend;
	}
	public boolean isShowRec() {
		return isShowRec;
	}
	public void setShowRec(boolean isShowRec) {
		this.isShowRec = isShowRec;
	}
	public boolean isShowTotal() {
		return isShowTotal;
	}
	public void setShowTotal(boolean isShowTotal) {
		this.isShowTotal = isShowTotal;
	}
	public boolean isShowSendCount() {
		return isShowSendCount;
	}
	public void setShowSendCount(boolean isShowSendCount) {
		this.isShowSendCount = isShowSendCount;
	}
	public boolean isShowFontSize() {
		return isShowFontSize;
	}
	public void setShowFontSize(boolean isShowFontSize) {
		this.isShowFontSize = isShowFontSize;
	}
	public boolean isShowSendDCount() {
		return isShowSendDCount;
	}
	public void setShowSendDCount(boolean isShowSendDCount) {
		this.isShowSendDCount = isShowSendDCount;
	}
	public boolean isShowSendPCount() {
		return isShowSendPCount;
	}
	public void setShowSendPCount(boolean isShowSendPCount) {
		this.isShowSendPCount = isShowSendPCount;
	}
	public boolean isShowSendDPer() {
		return isShowSendDPer;
	}
	public void setShowSendDPer(boolean isShowSendDPer) {
		this.isShowSendDPer = isShowSendDPer;
	}
	public boolean isShowSendOCount() {
		return isShowSendOCount;
	}
	public void setShowSendOCount(boolean isShowSendOCount) {
		this.isShowSendOCount = isShowSendOCount;
	}
	public boolean isShowSendOper() {
		return isShowSendOper;
	}
	public void setShowSendOper(boolean isShowSendOper) {
		this.isShowSendOper = isShowSendOper;
	}
	public boolean isShowRecCount() {
		return isShowRecCount;
	}
	public void setShowRecCount(boolean isShowRecCount) {
		this.isShowRecCount = isShowRecCount;
	}
	public boolean isShowRecDCount() {
		return isShowRecDCount;
	}
	public void setShowRecDCount(boolean isShowRecDCount) {
		this.isShowRecDCount = isShowRecDCount;
	}
	public boolean isShowRecPCount() {
		return isShowRecPCount;
	}
	public void setShowRecPCount(boolean isShowRecPCount) {
		this.isShowRecPCount = isShowRecPCount;
	}
	public boolean isShowRecDper() {
		return isShowRecDper;
	}
	public void setShowRecDper(boolean isShowRecDper) {
		this.isShowRecDper = isShowRecDper;
	}
	public boolean isShowRecOCount() {
		return isShowRecOCount;
	}
	public void setShowRecOCount(boolean isShowRecOCount) {
		this.isShowRecOCount = isShowRecOCount;
	}
	public boolean isShowRecOper() {
		return isShowRecOper;
	}
	public void setShowRecOper(boolean isShowRecOper) {
		this.isShowRecOper = isShowRecOper;
	}
	public boolean isShowAllCount() {
		return isShowAllCount;
	}
	public void setShowAllCount(boolean isShowAllCount) {
		this.isShowAllCount = isShowAllCount;
	}
	public boolean isShowAllDCount() {
		return isShowAllDCount;
	}
	public void setShowAllDCount(boolean isShowAllDCount) {
		this.isShowAllDCount = isShowAllDCount;
	}
	public boolean isShowAllPCount() {
		return isShowAllPCount;
	}
	public void setShowAllPCount(boolean isShowAllPCount) {
		this.isShowAllPCount = isShowAllPCount;
	}
	public boolean isShowAllDPer() {
		return isShowAllDPer;
	}
	public void setShowAllDPer(boolean isShowAllDPer) {
		this.isShowAllDPer = isShowAllDPer;
	}
	public boolean isShowAllOCount() {
		return isShowAllOCount;
	}
	public void setShowAllOCount(boolean isShowAllOCount) {
		this.isShowAllOCount = isShowAllOCount;
	}
	public boolean isShowAllOper() {
		return isShowAllOper;
	}
	public void setShowAllOper(boolean isShowAllOper) {
		this.isShowAllOper = isShowAllOper;
	}
	public int getSendTdSize() {
		return sendTdSize;
	}
	public void setSendTdSize(int sendTdSize) {
		this.sendTdSize = sendTdSize;
	}
	public int getRecTdSize() {
		return recTdSize;
	}
	public void setRecTdSize(int recTdSize) {
		this.recTdSize = recTdSize;
	}
	public int getTotalTdSize() {
		return totalTdSize;
	}
	public void setTotalTdSize(int totalTdSize) {
		this.totalTdSize = totalTdSize;
	}
	public boolean isSfShowNoRec() {
		return isSfShowNoRec;
	}
	public void setSfShowNoRec(boolean isSfShowNoRec) {
		this.isSfShowNoRec = isSfShowNoRec;
	}
	public boolean isSfShowTwo() {
		return isSfShowTwo;
	}
	public void setSfShowTwo(boolean isSfShowTwo) {
		this.isSfShowTwo = isSfShowTwo;
	}
	public boolean isSfShowThree() {
		return isSfShowThree;
	}
	public void setSfShowThree(boolean isSfShowThree) {
		this.isSfShowThree = isSfShowThree;
	}
	public boolean isSfShowFive() {
		return isSfShowFive;
	}
	public void setSfShowFive(boolean isSfShowFive) {
		this.isSfShowFive = isSfShowFive;
	}
	public String getFawenNodePolicy() {
		return fawenNodePolicy;
	}
	public void setFawenNodePolicy(String fawenNodePolicy) {
		this.fawenNodePolicy = fawenNodePolicy;
	}
	public String getShouwenNodePolicy() {
		return shouwenNodePolicy;
	}
	public void setShouwenNodePolicy(String shouwenNodePolicy) {
		this.shouwenNodePolicy = shouwenNodePolicy;
	}
	public List<Long> getStatRangeList() {
		return statRangeList;
	}
	public void setStatRangeList(List<Long> statRangeList) {
		this.statRangeList = statRangeList;
	}
	public Map<Long, Long> getStatRangeMap() {
		return statRangeMap;
	}
	public void setStatRangeMap(Map<Long, Long> statRangeMap) {
		this.statRangeMap = statRangeMap;
	}
	public List<Long> getStatRootIdList() {
		return statRootIdList;
	}
	public void setStatRootIdList(List<Long> statRootIdList) {
		this.statRootIdList = statRootIdList;
	}
	public void setStatRootList(List<V3xOrgUnit> statRootList) {
		this.statRootList = statRootList;
	}
	public List<V3xOrgUnit> getStatRootList() {
		return statRootList;
	}
	public boolean isSfShowback() {
		return isSfShowback;
	}
	public void setSfShowback(boolean isSfShowback) {
		this.isSfShowback = isSfShowback;
	}
	
}
