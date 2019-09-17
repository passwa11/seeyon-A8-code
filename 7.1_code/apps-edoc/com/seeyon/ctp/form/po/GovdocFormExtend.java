package com.seeyon.ctp.form.po;

import com.seeyon.ctp.form.bean.FormBean;

public class GovdocFormExtend {
	private Long id;
	private Long formId;
	private Long accountId;
	private String optionFormatSet ;  //意见排序
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public String getOptionFormatSet() {
		return optionFormatSet;
	}
	public void setOptionFormatSet(String optionFormatSet) {
		this.optionFormatSet = optionFormatSet;
	}
	
	
}
