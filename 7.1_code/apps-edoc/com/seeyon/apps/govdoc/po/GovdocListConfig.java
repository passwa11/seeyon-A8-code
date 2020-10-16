package com.seeyon.apps.govdoc.po;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 公文列表配置
 * @author tanggl
 *
 */
public class GovdocListConfig extends BasePO {
	
	private static final long serialVersionUID = -8533194269862791050L;
	
	private Long id;
	private String name;
	private Long ownerId;
	private int listType;
	private String permissions;
    private java.util.Date createDate;
    private String configName;
    private String linkType;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}
	public int getListType() {
		return listType;
	}
	public void setListType(int listType) {
		this.listType = listType;
	}
	public String getPermissions() {
		return permissions;
	}
	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	public java.util.Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}
	public String getConfigName() {
		return configName;
	}
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
    
}
