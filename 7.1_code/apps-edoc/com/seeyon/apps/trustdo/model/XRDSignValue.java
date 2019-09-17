package com.seeyon.apps.trustdo.model;

/**
 * 手机盾签名值模型
 * @author zhaopeng
 *
 */
public class XRDSignValue {
	
	private String cert;
	private String certSN;
	private String signValue;
	private String fpImage;
	private String signImage;
	private String snapshot;
	private String tsCert;
	private String tsSN;
	private String tsSignValue;
	public String getCert() {
		return cert;
	}
	public void setCert(String cert) {
		this.cert = cert;
	}
	public String getCertSN() {
		return certSN;
	}
	public void setCertSN(String certSN) {
		this.certSN = certSN;
	}
	public String getSignValue() {
		return signValue;
	}
	public void setSignValue(String signValue) {
		this.signValue = signValue;
	}
	public String getFpImage() {
		return fpImage;
	}
	public void setFpImage(String fpImage) {
		this.fpImage = fpImage;
	}
	public String getSignImage() {
		return signImage;
	}
	public void setSignImage(String signImage) {
		this.signImage = signImage;
	}
	public String getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
	public String getTsCert() {
		return tsCert;
	}
	public void setTsCert(String tsCert) {
		this.tsCert = tsCert;
	}
	public String getTsSN() {
		return tsSN;
	}
	public void setTsSN(String tsSN) {
		this.tsSN = tsSN;
	}
	public String getTsSignValue() {
		return tsSignValue;
	}
	public void setTsSignValue(String tsSignValue) {
		this.tsSignValue = tsSignValue;
	}
	
}
