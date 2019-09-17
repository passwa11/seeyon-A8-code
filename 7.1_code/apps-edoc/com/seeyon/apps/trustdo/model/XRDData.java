package com.seeyon.apps.trustdo.model;

/**
 * 签名结果模型：用于返回扫码事件的结果
 * @author zhaopeng
 *
 */
public class XRDData {
	
	private String eventId;//事件ID
	private long expireTime;//二维码过期事件(毫秒)
	private String sign;//签名值
	private String url;//二维码路径
	private String resetEvent;
	
	public String getResetEvent() {
		return resetEvent;
	}
	public void setResetEvent(String resetEvent) {
		this.resetEvent = resetEvent;
	}
	public XRDData() {
		super();
	}
	public long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	public XRDData(String eventId, int expireTime, String sign, String url) {
		super();
		this.eventId = eventId;
		this.expireTime = expireTime;
		this.sign = sign;
		this.url = url;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
