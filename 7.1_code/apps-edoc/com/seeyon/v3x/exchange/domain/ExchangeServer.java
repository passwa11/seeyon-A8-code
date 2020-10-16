package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class ExchangeServer extends BaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6358960368811382612L;
	private String exchangeServerId; //交换服务器id	
	private String name; //交换服务器名称
	private String serverAddress; //交换服务器地址
	private String serverPort; // 交换端口
	private String loginAccount; // 交换服务器登录帐号
	private String loginPassword; // 交换服务器登录密码
	private int status;//服务器连接：连接，断开（无法连接，账号或密码不正确）
	private boolean isActive;//启用，停用
	
	public String getExchangeServerId() {
		return exchangeServerId;
	}
	
	public void setExchangeServerId(String exchangeServerId) {
		this.exchangeServerId = exchangeServerId;
	}
		
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getServerAddress() {
		return serverAddress;
	}
	
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public String getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	public String getLoginAccount() {
		return loginAccount;
	}
	
	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}
	
	public String getLoginPassword() {
		return loginPassword;
	}
	
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public boolean getIsActive() {
		return isActive;
	}
	
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
}
