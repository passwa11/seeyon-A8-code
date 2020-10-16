package com.seeyon.apps.trustdo.model.sdk;

public class WebLoginData {

	String expireTime;
	String eventId;
	String eventData;
	String sign;
	
	public WebLoginData() {
		super();
	}

	public WebLoginData(String expireTime, String eventId,
			String eventData, String sign) {
		super();
		this.expireTime = expireTime;
		this.eventId = eventId;
		this.eventData = eventData;
		this.sign = sign;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
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
		return "WebLoginData [expireTime=" + expireTime
				+ ", eventId=" + eventId + ", eventData=" + eventData
				+ ", sign=" + sign + "]";
	}
}
