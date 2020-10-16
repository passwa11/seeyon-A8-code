package com.seeyon.apps.trustdo.model.sdk;


public class AccountData{
	
	String keyId;
	String signImg;
	String idCardNumber;
	String actStatus;
	String bizPhone;
	String locStatus;
	String reaStatus;
	String signCert;
    SignCertInfo signCertInfo;
    String bloStatus;
    String bizAccount;
    String idType;
    String idCardName;
    String useCertStatus;
    
	public AccountData() {
		super();
	}
	public AccountData(String keyId, String signImg, String idCardNumber,
			String actStatus, String bizPhone, String locStatus,
			String reaStatus, String signCert, SignCertInfo signCertInfo,
			String bloStatus, String bizAccount, String idType,
			String idCardName, String useCertStatus) {
		super();
		this.keyId = keyId;
		this.signImg = signImg;
		this.idCardNumber = idCardNumber;
		this.actStatus = actStatus;
		this.bizPhone = bizPhone;
		this.locStatus = locStatus;
		this.reaStatus = reaStatus;
		this.signCert = signCert;
		this.signCertInfo = signCertInfo;
		this.bloStatus = bloStatus;
		this.bizAccount = bizAccount;
		this.idType = idType;
		this.idCardName = idCardName;
		this.useCertStatus = useCertStatus;
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getSignImg() {
		return signImg;
	}
	public void setSignImg(String signImg) {
		this.signImg = signImg;
	}
	public String getIdCardNumber() {
		return idCardNumber;
	}
	public void setIdCardNumber(String idCardNumber) {
		this.idCardNumber = idCardNumber;
	}
	public String getActStatus() {
		return actStatus;
	}
	public void setActStatus(String actStatus) {
		this.actStatus = actStatus;
	}
	public String getBizPhone() {
		return bizPhone;
	}
	public void setBizPhone(String bizPhone) {
		this.bizPhone = bizPhone;
	}
	public String getLocStatus() {
		return locStatus;
	}
	public void setLocStatus(String locStatus) {
		this.locStatus = locStatus;
	}
	public String getReaStatus() {
		return reaStatus;
	}
	public void setReaStatus(String reaStatus) {
		this.reaStatus = reaStatus;
	}
	public String getSignCert() {
		return signCert;
	}
	public void setSignCert(String signCert) {
		this.signCert = signCert;
	}
	public SignCertInfo getSignCertInfo() {
		return signCertInfo;
	}
	public void setSignCertInfo(SignCertInfo signCertInfo) {
		this.signCertInfo = signCertInfo;
	}
	public String getBloStatus() {
		return bloStatus;
	}
	public void setBloStatus(String bloStatus) {
		this.bloStatus = bloStatus;
	}
	public String getBizAccount() {
		return bizAccount;
	}
	public void setBizAccount(String bizAccount) {
		this.bizAccount = bizAccount;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
	}
	public String getIdCardName() {
		return idCardName;
	}
	public void setIdCardName(String idCardName) {
		this.idCardName = idCardName;
	}
	public String getUseCertStatus() {
		return useCertStatus;
	}
	public void setUseCertStatus(String useCertStatus) {
		this.useCertStatus = useCertStatus;
	}
	@Override
	public String toString() {
		return "KeyIdResultData [keyId=" + keyId + ", signImg=" + signImg
				+ ", idCardNumber=" + idCardNumber + ", actStatus=" + actStatus
				+ ", bizPhone=" + bizPhone + ", locStatus=" + locStatus
				+ ", reaStatus=" + reaStatus + ", signCert=" + signCert
				+ ", signCertInfo=" + signCertInfo + ", bloStatus=" + bloStatus
				+ ", bizAccount=" + bizAccount + ", idType=" + idType
				+ ", idCardName=" + idCardName + ", useCertStatus="
				+ useCertStatus + "]";
	}
    
}
