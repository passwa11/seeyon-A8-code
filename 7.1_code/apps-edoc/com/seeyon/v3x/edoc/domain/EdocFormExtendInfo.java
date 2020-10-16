package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocFormExtendInfo extends BaseModel implements Serializable{
	
	private static final long serialVersionUID = -5695425703446090074L;
	private EdocForm edocForm;
	private Long accountId;
	private Integer status;
	private Boolean isDefault;
	private String optionFormatSet ;  //意见排序
	
	
	public String getOptionFormatSet() {
		return optionFormatSet;
	}
	public void setOptionFormatSet(String optionFormatSet) {
		this.optionFormatSet = optionFormatSet;
	}
	public EdocForm getEdocForm() {
		return edocForm;
	}
	public void setEdocForm(EdocForm edocForm) {
		this.edocForm = edocForm;
	}
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

}
