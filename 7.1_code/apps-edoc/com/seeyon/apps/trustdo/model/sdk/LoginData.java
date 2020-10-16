package com.seeyon.apps.trustdo.model.sdk;

public class LoginData {

	String bizAccount;
	
	public LoginData() {
		super();
	}
	
	public LoginData(String bizAccount) {
		super();
		this.bizAccount = bizAccount;
	}

	public String getBizAccount() {
		return bizAccount;
	}

	public void setBizAccount(String bizAccount) {
		this.bizAccount = bizAccount;
	}
	
}
