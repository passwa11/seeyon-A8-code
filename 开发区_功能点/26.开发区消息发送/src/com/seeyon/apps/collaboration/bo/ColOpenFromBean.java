package com.seeyon.apps.collaboration.bo;

public class ColOpenFromBean {
	String listType;
    Boolean canComment = Boolean.FALSE;

	public String getListType() {
		return listType;
	}
	public ColOpenFromBean setListType(String listType) {
		this.listType = listType;
		return this;
	}
	public Boolean isCanComment() {
		return canComment;
	}
	public ColOpenFromBean setCanComment(Boolean canComment) {
		this.canComment = canComment;
		return this;
	}
}
