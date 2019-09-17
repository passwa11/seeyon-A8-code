package com.seeyon.apps.trustdo.model.sdk;

public class SDKLoginEventData {

	/**
	 * 超时时间
	 */
	String expireTime;
	
	/**
	 * 事件
	 */
	String eventData;
	
	/**
	 * 签名值
	 */
	String sign;

	public SDKLoginEventData() {
		super();
	}

	public SDKLoginEventData(String expireTime, String eventData, String sign) {
		super();
		this.expireTime = expireTime;
		this.eventData = eventData;
		this.sign = sign;
	}
	
	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getEventData() {
		return eventData;
	}

	public void setEventData(String eventData) {
		this.eventData = eventData;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public String toString() {
		return "ResultData [expireTime=" + expireTime + ", eventData="
				+ eventData + ", sign=" + sign + "]";
	}
	
}
