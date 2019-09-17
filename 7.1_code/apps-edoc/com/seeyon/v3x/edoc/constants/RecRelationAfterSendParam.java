package com.seeyon.v3x.edoc.constants;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class RecRelationAfterSendParam {

	private EdocSummary summary;
	private EdocRegister register;
	private String recieveId;
	private String waitRegister_recieveId;
	private User user;
	private Integer processType;
	
	public EdocSummary getSummary() {
		return summary;
	}
	public void setSummary(EdocSummary summary) {
		this.summary = summary;
	}
	public EdocRegister getRegister() {
		return register;
	}
	public void setRegister(EdocRegister register) {
		this.register = register;
	}
	public String getRecieveId() {
		return recieveId;
	}
	public void setRecieveId(String recieveId) {
		this.recieveId = recieveId;
	}
	public String getWaitRegister_recieveId() {
		return waitRegister_recieveId;
	}
	public void setWaitRegister_recieveId(String waitRegister_recieveId) {
		this.waitRegister_recieveId = waitRegister_recieveId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getProcessType() {
		return processType;
	}
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}
}
