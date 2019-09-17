package com.seeyon.apps.govdoc.po;

import java.sql.Timestamp;

import com.seeyon.ctp.common.po.BasePO;


/**
 * 公文归档页面表对象，将公文数据 获取一份，处理一下，放入这个表中，公文归档页面 查询这张表
 */
public class GovdocRegister extends BasePO{
	private static final long serialVersionUID = -26721332706273020L;
	
	private Long id;
	private Long summaryId;//公文id
	private Timestamp createTime;//处理时间
	private String recUserName;
	private String recNo;
	private Timestamp recTime;

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

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getRecUserName() {
		return recUserName;
	}

	public void setRecUserName(String recUserName) {
		this.recUserName = recUserName;
	}

	public String getRecNo() {
		return recNo;
	}

	public void setRecNo(String recNo) {
		this.recNo = recNo;
	}

	public Timestamp getRecTime() {
		return recTime;
	}

	public void setRecTime(Timestamp recTime) {
		this.recTime = recTime;
	}
	
}
