package com.seeyon.v3x.exchange.domain;

import static com.seeyon.ctp.organization.bo.V3xOrgEntity.TOXML_PROPERTY_NAME;
import static com.seeyon.ctp.organization.bo.V3xOrgEntity.TOXML_PROPERTY_id;

import java.io.Serializable;
import java.sql.Timestamp;

import com.seeyon.ctp.util.ObjectToXMLUtil;
import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.ctp.util.Strings;

public class ExchangeAccount extends BaseModel implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -312861441449955905L;
	public final static int C_iStatus_Inactive = 0;
	public final static int C_iStatus_Active = 1;
	
	public static final String ENTITY_TYPE_EXCHANGEACCOUNT = "ExchangeAccount";
		
	private String accountId;
	private String name;
	private int accountType;
	private String description;
	private boolean isInternalAccount;
	private long internalOrgId;
	private long internalDeptId;
	private long internalUserId;
	private String exchangeServerId;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private int status;
	private Long domainId;
	
	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getAccountId() {
		return accountId;
	}
	
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getAccountType() {
		return accountType;
	}
	
	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean getIsInternalAccount() {
		return isInternalAccount;
	}
	
	public void setIsInternalAccount(boolean isInternalAccount) {
		this.isInternalAccount = isInternalAccount;
	}
	
	public long getInternalOrgId() {
		return internalOrgId;
	}
	
	public void setInternalOrgId(long internalOrgId) {
		this.internalOrgId = internalOrgId;
	}
	
	public long getInternalDeptId() {
		return internalDeptId;
	}
	
	public void setInternalDeptId(long internalDeptId) {
		this.internalDeptId = internalDeptId;
	}
	
	public long getInternalUserId() {
		return internalUserId;
	}
	
	public void setInternalUserId(long internalUserId) {
		this.internalUserId = internalUserId;
	}
	
	public String getExchangeServerId() {
		return exchangeServerId;
	}
	
	public void setExchangeServerId(String exchangeServerId) {
		this.exchangeServerId = exchangeServerId;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public Timestamp getLastUpdate() {
		return lastUpdate;
	}
	
	public void setLastUpdate(Timestamp lastUpdate){
		this.lastUpdate = lastUpdate;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String toXML(){
		StringBuffer sb = new StringBuffer();
		sb.append(ObjectToXMLUtil.makeBeanNodeBegin(this.getClass()));
		
		sb.append(ObjectToXMLUtil.NEW_LINE);

		sb.append(ObjectToXMLUtil.makeProperties("id", this.getAccountId()));
		sb.append(ObjectToXMLUtil.makeProperties("name", this.getName()));
		sb.append(ObjectToXMLUtil.makeProperties("entityType", ENTITY_TYPE_EXCHANGEACCOUNT));
		
		sb.append(ObjectToXMLUtil.NEW_LINE);

		sb.append(ObjectToXMLUtil.makeBeanNodeEnd());
		sb.append(ObjectToXMLUtil.NEW_LINE);

		return sb.toString();
	}
	
	/**
	 * 给选人界面用的，不要轻易修改
	 */
	public void toJsonString(StringBuilder o) {
		o.append("{");
		o.append(TOXML_PROPERTY_id).append(":\"").append(this.getId()).append("\"");
		o.append(",").append(TOXML_PROPERTY_NAME).append(":\"").append(Strings.escapeJavascript(this.getName())).append("\"");
		o.append("}");
	}

}