package com.seeyon.apps.trustdo.model;

public class NewBindResultData {

	String keyId;

	public NewBindResultData() {
		super();
	}

	public NewBindResultData(String keyId) {
		super();
		this.keyId = keyId;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	@Override
	public String toString() {
		return "NewBindResultData [keyId=" + keyId + "]";
	}

}
