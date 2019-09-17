package com.seeyon.apps.trustdo.model.sdk;

public class Result<T> {
	
	/**
	 * 结果标识
	 */
	String errCode;
	
	/**
	 * 结果描述
	 */
	String msg;
	
	T data;
	
	public Result() {
		super();
	}
	
	public Result(T data) {
		super();
		this.data = data;
	}

	public Result(String errCode, String msg, T data) {
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Result [errCode=" + errCode + ", msg=" + msg + ", data=" + data==null?"":data.toString()+ "]";
	}

}
