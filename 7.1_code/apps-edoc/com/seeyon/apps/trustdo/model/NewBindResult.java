package com.seeyon.apps.trustdo.model;

public class NewBindResult {
	
	String errCode;
	
	int msg;
	
	NewBindResultData data;
	
	public NewBindResult() {
		super();
	}

	public NewBindResult(String errCode, int msg, NewBindResultData data) {
		super();
		this.errCode = errCode;
		this.msg = msg;
		this.data = data;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public int getMsg() {
		return msg;
	}

	public void setMsg(int msg) {
		this.msg = msg;
	}

	public NewBindResultData getData() {
		return data;
	}

	public void setData(NewBindResultData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "NewBindResult [errCode=" + errCode + ", msg=" + msg + ", data="
				+ data + "]";
	}
	
}
