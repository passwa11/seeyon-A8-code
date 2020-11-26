package com.seeyon.apps.collaboration.bo;

public class DeleteAjaxTranObj {

	
	private String affairIds;
	
	private String pageType;
	
	private String fromMethod;

	public String getAffairIds() {
		return affairIds;
	}

	public String getFromMethod() {
		return fromMethod;
	}

	public void setFromMethod(String fromMethod) {
		this.fromMethod = fromMethod;
	}

	public void setAffairIds(String affairIds) {
		this.affairIds = affairIds;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
	
}
