package com.seeyon.apps.trustdo.model.sdk;

public class TokenData {

	String status;
	String accToken;
	
	public TokenData() {
		super();
	}

	public TokenData(String status, String accToken) {
		super();
		this.status = status;
		this.accToken = accToken;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAccToken() {
		return accToken;
	}

	public void setAccToken(String accToken) {
		this.accToken = accToken;
	}

	@Override
	public String toString() {
		return "TokenData [status=" + status + ", accToken=" + accToken + "]";
	}
}
