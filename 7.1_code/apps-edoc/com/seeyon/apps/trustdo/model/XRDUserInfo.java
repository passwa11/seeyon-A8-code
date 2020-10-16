package com.seeyon.apps.trustdo.model;

/**
 * 手机盾用户模型（待根据业务完善，暂时未用）
 * @author zhaopeng
 *
 */
public class XRDUserInfo {
	private String bizAccount;
	private String eventId;
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getBizAccount() {
		return bizAccount;
	}
	public void setBizAccount(String bizAccount) {
		this.bizAccount = bizAccount;
	}
	@Override
	public String toString() {
		return "UserInfo [bizAccount=" + bizAccount + ", eventId=" + eventId + "]";
	}
	
}
