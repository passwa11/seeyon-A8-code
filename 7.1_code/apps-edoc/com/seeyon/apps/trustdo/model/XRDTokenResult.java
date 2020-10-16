package com.seeyon.apps.trustdo.model;

/**
 * 手机盾获取token模型
 * @author zhaopeng
 *
 */
public class XRDTokenResult {
	/**
	 * key:eventId;
	 * value:accToken;
	 */
	public static final String EVENTID_ACCTOKEN_KEY = "temp:eventid:acctoken:";



	private Integer code = 200;
	private String msg = "SUCCESS";
	private String accToken;

	public XRDTokenResult(){}

	public XRDTokenResult(String accToken){
		this.accToken = accToken;
	}

	public XRDTokenResult(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public XRDTokenResult(Integer code, String msg, String accToken) {
		super();
		this.code = code;
		this.msg = msg;
		this.accToken = accToken;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getAccToken() {
		return accToken;
	}

	public void setAccToken(String accToken) {
		this.accToken = accToken;
	}

	@Override
	public String toString() {
		return "XRDTokenResult [code=" + code + ", msg=" + msg + ", accToken="
				+ accToken + "]";
	}
	
}
