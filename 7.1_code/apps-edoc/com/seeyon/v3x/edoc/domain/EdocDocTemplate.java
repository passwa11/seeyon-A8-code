package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.v3x.common.domain.BaseModel;

public class EdocDocTemplate extends BaseModel implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5640207538311154028L;

	private String name;

	private int type;

	private String description;

	private long templateFileId;

	private long createUserId;

	private java.sql.Timestamp createTime;

	private long lastUserId;

	private java.sql.Timestamp lastUpdate;

	private int status;

	private long domainId;
	
	private String textType;
	
	private String fileUrl;//用于在前台显示附件地址，不持久化
	
	// 授权对象名，不持久化
	private String grantNames;
	
	private Set<EdocDocTemplateAcl> templateAcls;
	
	private List<V3xOrgEntity> aclEntity;

	public List<V3xOrgEntity> getAclEntity() {
		return aclEntity;
	}

	public void setAclEntity(List<V3xOrgEntity> aclEntity) {
		this.aclEntity = aclEntity;
	}

	public Set<EdocDocTemplateAcl> getTemplateAcls() {
		return templateAcls;
	}

	public void setTemplateAcls(Set<EdocDocTemplateAcl> templateAcls) {
		this.templateAcls = templateAcls;
	}

	public java.sql.Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(java.sql.Timestamp createTime) {
		this.createTime = createTime;
	}

	public long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(long createUserId) {
		this.createUserId = createUserId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getDomainId() {
		return domainId;
	}

	public void setDomainId(long domainId) {
		this.domainId = domainId;
	}

	public java.sql.Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(java.sql.Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public long getLastUserId() {
		return lastUserId;
	}

	public void setLastUserId(long lastUserId) {
		this.lastUserId = lastUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getTemplateFileId() {
		return templateFileId;
	}

	public void setTemplateFileId(long templateFileId) {
		this.templateFileId = templateFileId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getGrantNames() {
		return grantNames;
	}

	public void setGrantNames(String grantNames) {
		this.grantNames = grantNames;
	}

	public String getTextType() {
		return textType;
	}

	public void setTextType(String textType) {
		this.textType = textType;
	}
}
