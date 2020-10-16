package com.seeyon.apps.trustdo.model.sdk;

public class ResetData {
	
	String eventId;
	String expireTime;
    String eventData;
    String sign;
    
	public ResetData() {
		super();
	}
	
	public ResetData(String eventId, String expireTime, String eventData,
			String sign) {
		super();
		this.eventId = eventId;
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
	
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	@Override
	public String toString() {
		return "ResetData [eventId=" + eventId + ", expireTime=" + expireTime
				+ ", eventData=" + eventData + ", sign=" + sign + "]";
	}

}
