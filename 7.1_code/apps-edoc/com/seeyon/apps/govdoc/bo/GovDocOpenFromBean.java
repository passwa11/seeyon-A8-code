package com.seeyon.apps.govdoc.bo;

public class GovDocOpenFromBean {
	String listType;
    Boolean canComment = Boolean.FALSE;

	public String getListType() {
		return listType;
	}
	public GovDocOpenFromBean setListType(String listType) {
		this.listType = listType;
		return this;
	}
	public Boolean isCanComment() {
		return canComment;
	}
	public GovDocOpenFromBean setCanComment(Boolean canComment) {
		this.canComment = canComment;
		return this;
	}
}
