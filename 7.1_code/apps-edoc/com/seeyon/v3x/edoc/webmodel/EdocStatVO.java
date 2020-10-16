package com.seeyon.v3x.edoc.webmodel;

import java.util.List;

public class EdocStatVO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8444018607372333944L;
	/**
	 * 
	 */
	
	private Integer listType;
	private Integer edocType;
	private Integer displayType;//按部门、人员、时间类型显示
	private String displayId;//部门、人员ID
	private Long displayDeptId;
	private List<Long> subDeptIdList;
	private String displayName;//显示文字
	private Integer displayTimeType;//1年 2季 3月 4日
	private String statRangeType;
	private Long statRangeId;
	
	private Integer countDoAll = 0;//总办件(已办结+未办结)
	private Integer countHandleAll = 0;//总经办(待办+在办+已办)
	private Integer countFinish = 0;//已办结
	private Integer countWaitFinish = 0;//未办结
	private Integer countSent = 0;//已发
	private Integer countDone = 0;//已办
	private Integer countPending = 0;//待办
	private Integer countZcdb = 0;//在办
	private Integer countReadAll = 0;//总阅件
	private Integer countReading = 0;//待阅
	private Integer countReaded = 0;//已阅
	private Integer countUndertaker  = 0;//承办数

	public void setNullValue() {
		this.setCountDoAll(0);
		this.setCountHandleAll(0);
		this.setCountFinish(0);
		this.setCountWaitFinish(0);
		this.setCountDone(0);
		this.setCountPending(0);
		this.setCountZcdb(0);
		this.setCountReadAll(0);
		this.setCountReaded(0);
		this.setCountReading(0);
		this.setCountUndertaker(0);
	}
	
	public Integer getDisplayType() {
		return displayType;
	}

	public void setDisplayType(Integer displayType) {
		this.displayType = displayType;
	}

	public Integer getCountDoAll() {
		return countDoAll;
	}

	public void setCountDoAll(Integer countDoAll) {
		this.countDoAll = countDoAll;
	}

	public Integer getCountFinish() {
		return countFinish;
	}

	public void setCountFinish(Integer countFinish) {
		this.countFinish = countFinish;
	}

	public Integer getCountWaitFinish() {
		return countWaitFinish;
	}

	public void setCountWaitFinish(Integer countWaitFinish) {
		this.countWaitFinish = countWaitFinish;
	}

	public Integer getCountSent() {
		return countSent;
	}

	public void setCountSent(Integer countSent) {
		this.countSent = countSent;
	}

	public Integer getCountDone() {
		return countDone;
	}

	public void setCountDone(Integer countDone) {
		this.countDone = countDone;
	}

	public Integer getCountPending() {
		return countPending;
	}

	public void setCountPending(Integer countPending) {
		this.countPending = countPending;
	}

	public Integer getCountZcdb() {
		return countZcdb;
	}

	public void setCountZcdb(Integer countZcdb) {
		this.countZcdb = countZcdb;
	}

	public Integer getCountReadAll() {
		return countReadAll;
	}

	public void setCountReadAll(Integer countReadAll) {
		this.countReadAll = countReadAll;
	}

	public Integer getCountReaded() {
		return countReaded;
	}

	public void setCountReaded(Integer countReaded) {
		this.countReaded = countReaded;
	}

	public Integer getCountReading() {
		return countReading;
	}

	public void setCountReading(Integer countReading) {
		this.countReading = countReading;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public void setDisplayTimeType(Integer displayTimeType) {
		this.displayTimeType = displayTimeType;
	}

	public Integer getDisplayTimeType() {
		return displayTimeType;
	}

	public Integer getCountHandleAll() {
		return countHandleAll;
	}

	public void setCountHandleAll(Integer countHandleAll) {
		this.countHandleAll = countHandleAll;
	}

	public List<Long> getSubDeptIdList() {
		return subDeptIdList;
	}

	public void setSubDeptIdList(List<Long> subDeptIdList) {
		this.subDeptIdList = subDeptIdList;
	}

	public Integer getCountUndertaker() {
		return countUndertaker;
	}

	public void setCountUndertaker(Integer countUndertaker) {
		this.countUndertaker = countUndertaker;
	}

	public Integer getListType() {
		return listType;
	}

	public void setListType(Integer listType) {
		this.listType = listType;
	}

	public Integer getEdocType() {
		return edocType;
	}

	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
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

	public Long getDisplayDeptId() {
		return displayDeptId;
	}

	public void setDisplayDeptId(Long displayDeptId) {
		this.displayDeptId = displayDeptId;
	}

}
