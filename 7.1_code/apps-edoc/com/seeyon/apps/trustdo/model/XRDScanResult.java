package com.seeyon.apps.trustdo.model;

/**
 * 
 * 扫码登录置换客户信息模型
 * @author zhaopeng
 *
 */
public class XRDScanResult {
	private int code;
	private String msg;
	private String bizAccount;
	private String stampResult;
	
	public String getStampResult() {
		return stampResult;
	}
	public void setStampResult(String stampResult) {
		this.stampResult = stampResult;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getBizAccount() {
		return bizAccount;
	}
	public void setBizAccount(String bizAccount) {
		this.bizAccount = bizAccount;
	}
	@Override
	public String toString() {
		return "XRDScanResult [code=" + code + ", msg=" + msg + ", bizAccount="
				+ bizAccount + ", stampResult=" + stampResult + "]";
	}
	
}
