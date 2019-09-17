package com.seeyon.v3x.edoc.webmodel;

public class EdocOpenFromBean {
	String listType;
    Boolean canComment = Boolean.FALSE;

	public String getListType() {
		return listType;
	}
	public EdocOpenFromBean setListType(String listType) {
		this.listType = listType;
		return this;
	}
	public Boolean isCanComment() {
		return canComment;
	}
	public EdocOpenFromBean setCanComment(Boolean canComment) {
		this.canComment = canComment;
		return this;
	}
}
