package com.seeyon.apps.trustdo.model.sdk;

public class SignCertInfo {

	String serialNumber;
	String issuerName;
	String subjectDNName;
	String sigAlgName;
	String beforeDate;
	String afterDate;
	String publicKey;
	
	public SignCertInfo() {
		super();
	}
	public SignCertInfo(String serialNumber, String issuerName,
			String subjectDNName, String sigAlgName, String beforeDate,
			String afterDate, String publicKey) {
		super();
		this.serialNumber = serialNumber;
		this.issuerName = issuerName;
		this.subjectDNName = subjectDNName;
		this.sigAlgName = sigAlgName;
		this.beforeDate = beforeDate;
		this.afterDate = afterDate;
		this.publicKey = publicKey;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getIssuerName() {
		return issuerName;
	}
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	public String getSubjectDNName() {
		return subjectDNName;
	}
	public void setSubjectDNName(String subjectDNName) {
		this.subjectDNName = subjectDNName;
	}
	public String getSigAlgName() {
		return sigAlgName;
	}
	public void setSigAlgName(String sigAlgName) {
		this.sigAlgName = sigAlgName;
	}
	public String getBeforeDate() {
		return beforeDate;
	}
	public void setBeforeDate(String beforeDate) {
		this.beforeDate = beforeDate;
	}
	public String getAfterDate() {
		return afterDate;
	}
	public void setAfterDate(String afterDate) {
		this.afterDate = afterDate;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	@Override
	public String toString() {
		return "SignCertInfo [serialNumber=" + serialNumber + ", issuerName="
				+ issuerName + ", subjectDNName=" + subjectDNName
				+ ", sigAlgName=" + sigAlgName + ", beforeDate=" + beforeDate
				+ ", afterDate=" + afterDate + ", publicKey=" + publicKey + "]";
	}
	
}
