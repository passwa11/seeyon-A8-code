package com.seeyon.ctp.form.po;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 
 * @author cx
 *	流程模板授权部门
 */
public class GovdocTemplateDepAuth extends BasePO{
	
	public final static int AUTH_TYPE_EXCHANGE = 0;//
	public final static int AUTH_TYPE_LIANHE = 1;//
	private Long accountId;
	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	
	private Long orgId;
	
	private String orgName;
	
	private String orgType;
	
	private Long templateId;
	
	/**
	 * 授权类型  默认为0 表示交换单的授权类型
	 * 1表示联合发文授权类型
	 */
	private int authType = 0;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public int getAuthType() {
		return authType;
	}

	public void setAuthType(int authType) {
		this.authType = authType;
	}
	
	
}
