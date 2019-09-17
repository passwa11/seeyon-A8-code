package com.seeyon.v3x.edoc.webmodel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;

/**
 * 公文统计过滤条件
 * @author 唐桂林
 *
 */
public class GovdocStatConditionVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 统计设置ID */
	private Long statId;
	
	/** 统计设置名称 */
	private String statName;
	
	/** 统计内容标题 */
	private String statTitle;
	
	/** 统计类型: work_count(工作统计)/v3x_edoc_sign_count(签收统计)*/
	private String statType;
	
	/** 统计范围：null(全部)/not null(具体的统计对象) */
	private Long statRangeId;
	
	/** 统计范围类型：Account(单位)/Department(部门) */
	private String statRangeType;
	
	/** 统计范围名称：单位/部门 */
	private String statRangeName;
	
	/** 统计时间：后端使用 */
	private Date startDate;
	private Date endDate;
	
	/** 统计时间：前端展现 */
	private String startTime;
	private String endTime;
	
	/** 公文文号/内部文号相关过滤 */
	private String docMark;
	private String docMarkDefId;
	private String serialNo;
	private String serialNoDefId;
	private EdocMarkDefinition docMarkDef;
	private EdocMarkDefinition serialNoMarkDef;
	
	private List<Long> summaryIdList;
	
	/** 统计结果穿透过滤参数，统计对象 */
	private Long displayId;
	private String displayType;
	
	/** 统计后台设置 */
	private GovdocStatSetVO statSetVo;
	
	public Long getStatId() {
		return statId;
	}
	public void setStatId(Long statId) {
		this.statId = statId;
	}
	public String getStatName() {
		return statName;
	}
	public void setStatName(String statName) {
		this.statName = statName;
	}
	public Long getStatRangeId() {
		return statRangeId;
	}
	public void setStatRangeId(Long statRangeId) {
		this.statRangeId = statRangeId;
	}
	public String getStatRangeType() {
		return statRangeType;
	}
	public void setStatRangeType(String statRangeType) {
		this.statRangeType = statRangeType;
	}
	public String getStatRangeName() {
		return statRangeName;
	}
	public void setStatRangeName(String statRangeName) {
		this.statRangeName = statRangeName;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getDocMark() {
		return docMark;
	}
	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}
	public String getDocMarkDefId() {
		return docMarkDefId;
	}
	public void setDocMarkDefId(String docMarkDefId) {
		this.docMarkDefId = docMarkDefId;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getSerialNoDefId() {
		return serialNoDefId;
	}
	public void setSerialNoDefId(String serialNoDefId) {
		this.serialNoDefId = serialNoDefId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Long getDisplayId() {
		return displayId;
	}
	public void setDisplayId(Long displayId) {
		this.displayId = displayId;
	}
	public String getDisplayType() {
		return displayType;
	}
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}
	public GovdocStatSetVO getStatSetVo() {
		return statSetVo;
	}
	public void setStatSetVo(GovdocStatSetVO statSetVo) {
		this.statSetVo = statSetVo;
	}
	public EdocMarkDefinition getSerialNoMarkDef() {
		return serialNoMarkDef;
	}
	public void setSerialNoMarkDef(EdocMarkDefinition serialNoMarkDef) {
		this.serialNoMarkDef = serialNoMarkDef;
	}
	public EdocMarkDefinition getDocMarkDef() {
		return docMarkDef;
	}
	public void setDocMarkDef(EdocMarkDefinition docMarkDef) {
		this.docMarkDef = docMarkDef;
	}
	public String getStatType() {
		return statType;
	}
	public void setStatType(String statType) {
		this.statType = statType;
	}
	public List<Long> getSummaryIdList() {
		return summaryIdList;
	}
	public void setSummaryIdList(List<Long> summaryIdList) {
		this.summaryIdList = summaryIdList;
	}
	
	public String getStatTitle() {
		return statTitle;
	}
	public void setStatTitle(String statTitle) {
		this.statTitle = statTitle;
	}

}
