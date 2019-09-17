package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;
/**
 * 快速发文信息表——保存快速发文时的选项信息
 * @author Administrator
 *
 */
public class EdocSummaryQuick extends BaseModel implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4565806016332286209L;
	private Long id;
	private Long summaryId;
	private String taohongTemplateUrl; //套红模板的url(格式：${taohong.fileUrl}&${taohong.textType})
	private Long archiveId; //归档id
	private Integer exchangeType; //交换类型，0：部门，1：单位
	private Long exchangeAccountMemberId; //如果交换类型是单位并且指定人员执行
	private String exchangeDeptType = null;//交换类型为部门，部门取发起人或封发人类型
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSummaryId() {
		return summaryId;
	}
	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}
	public String getTaohongTemplateUrl() {
		return taohongTemplateUrl;
	}
	public void setTaohongTemplateUrl(String taohongTemplateUrl) {
		this.taohongTemplateUrl = taohongTemplateUrl;
	}
	public Long getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(Long archiveId) {
		this.archiveId = archiveId;
	}
	public Long getExchangeAccountMemberId() {
		return exchangeAccountMemberId;
	}
	public void setExchangeAccountMemberId(Long exchangeAccountMemberId) {
		this.exchangeAccountMemberId = exchangeAccountMemberId;
	}
	public Integer getExchangeType() {
		return exchangeType;
	}
	public void setExchangeType(Integer exchangeType) {
		this.exchangeType = exchangeType;
	}
    public String getExchangeDeptType() {
        return exchangeDeptType;
    }
    public void setExchangeDeptType(String exchangeDeptType) {
        this.exchangeDeptType = exchangeDeptType;
    }
	
	
	
}
