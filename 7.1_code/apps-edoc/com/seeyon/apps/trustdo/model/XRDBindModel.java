package com.seeyon.apps.trustdo.model;

/**
 * 绑定请求参数模型：用于封装请求手机盾进行绑定的若干参数
 * @author zhaopeng
 *
 */
public class XRDBindModel {
	//致远用户登录名（默认手机号）
	private String account;
	//致远用户手机号
	private String phone;
	//致远用户真实姓名（默认"seeyon"）
	private String realName = "seeyon";
	//致远用户身份标识（默认手机号）
	private String idCard;
	//致远用户身份类型（默认"1"）
	private String cardType = "1";
	
	public XRDBindModel() {
		super();
	}
	public XRDBindModel(String account, String phone, String realName,
			String idCard, String cardType) {
		super();
		this.account = account;
		this.phone = phone;
		this.realName = realName;
		this.idCard = idCard;
		this.cardType = cardType;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	@Override
	public String toString() {
		return "XRDBindModel [account=" + account + ", phone=" + phone
				+ ", realName=" + realName + ", idCard=" + idCard
				+ ", cardType=" + cardType + "]";
	}
	
}
