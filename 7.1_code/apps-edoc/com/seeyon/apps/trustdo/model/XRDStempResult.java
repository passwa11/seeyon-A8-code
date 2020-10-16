package com.seeyon.apps.trustdo.model;

/**
 * 签名值对象
 * @author lip15
 *
 */
public class XRDStempResult {
	private String originalText;//二维码过期事件(毫秒)
	private String signValue;//签名值
	private String signImgData;//签章,签批图片

	public XRDStempResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	public XRDStempResult(String originalText, String signValue, String signImgData) {
		super();
		this.originalText = originalText;
		this.signValue = signValue;
		this.signImgData = signImgData;
	}
	public String getOriginalText() {
		return originalText;
	}
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}
	public String getSignValue() {
		return signValue;
	}
	public void setSignValue(String signValue) {
		this.signValue = signValue;
	}
	public String getSignImgData() {
		return signImgData;
	}
	public void setSignImgData(String signImgData) {
		this.signImgData = signImgData;
	}
}
