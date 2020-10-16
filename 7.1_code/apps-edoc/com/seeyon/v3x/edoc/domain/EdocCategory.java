package com.seeyon.v3x.edoc.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.seeyon.v3x.common.domain.BaseModel;

@XmlRootElement
public class EdocCategory extends BaseModel {
	private static final long serialVersionUID = 7709614283781066137L;
	
	private Long id;
	private String name;
	private Long rootCategory;
	private Integer state;
	private Integer storeType;
	private Long modifyUserId;
	private Date modifyTime;
	public Long getModifyUserId() {
		return modifyUserId;
	}
	public void setModifyUserId(Long modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	private Long accountId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getRootCategory() {
		return rootCategory;
	}
	public void setRootCategory(Long rootCategory) {
		this.rootCategory = rootCategory;
	}
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public Integer getStoreType() {
		return storeType;
	}
	public void setStoreType(Integer storeType) {
		this.storeType = storeType;
	}
}
