package com.seeyon.apps.trustdo.model;

import java.util.List;

/**
 * 手机盾签名结果模型
 * @author zhaopeng
 *
 */
public class XRDSignResult {
	private String version;
	private int signCount;
	private String originalData;
	private String sigAlg;
	private List<XRDSignValue> signValues;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getSignCount() {
		return signCount;
	}
	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}
	public String getOriginalData() {
		return originalData;
	}
	public void setOriginalData(String originalData) {
		this.originalData = originalData;
	}
	public String getSigAlg() {
		return sigAlg;
	}
	public void setSigAlg(String sigAlg) {
		this.sigAlg = sigAlg;
	}
	public List<XRDSignValue> getSignValues() {
		return signValues;
	}
	public void setSignValues(List<XRDSignValue> signValues) {
		this.signValues = signValues;
	}
	
}
