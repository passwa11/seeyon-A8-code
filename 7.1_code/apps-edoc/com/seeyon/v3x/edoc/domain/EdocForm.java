package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocForm extends BaseModel implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2582021503024929332L;
	public static final Integer C_iStatus_Draft = 0;
	public static final Integer C_iStatus_Published = 1;
	public static final Integer C_iStatus_Deleted = 2;
	
	public static final Boolean Is_System = true;  //系统预置的公文单
	public static final Boolean Non_System = false;  //非系统预置的公文单
	
	public final static String Prop_Name = "name";
	public final static String Prop_Description = "description";
	public final static String Prop_Content = "content";
	public final static String Prop_type = "type";
	public final static String Prop_CreateUserId = "createUserId";
	public final static String Prop_CreateTime = "createTime";
	public final static String Prop_LastUserId = "lastUserId";
	public final static String Prop_LastUpdate = "lastUpdate";
	public final static String Prop_Status = "status";
	public final static String Prop_IsSystem = "isSystem";
	
	     
    
    private String name;
    private String description;
    private String content;
    private Integer type;
    private Boolean isDefault;
    private Long createUserId;
    private Timestamp createTime;
    private Long lastUserId;
    private Timestamp lastUpdate;
    private Integer status;    
    private Set<EdocFormElement> edocFormElements;
    private Set<EdocFormAcl> edocFormAcls;
    private Set<EdocFormFlowPermBound> edocFormFlowPermBound;
    private Set<EdocFormExtendInfo> edocFormExtendInfo;
	private Long domainId;
    private Long fileId;
    private Boolean showLog;
    private Boolean isSystem;
    private Boolean isunit;
    private Long subType;
    //自定义分类名称，不做持久化
    private String subTypeName;
    
	
    
	public String getSubTypeName() {
		return subTypeName;
	}
	public void setSubTypeName(String subTypeName) {
		this.subTypeName = subTypeName;
	}
	public Long getSubType() {
		return subType;
	}
	public void setSubType(Long subType) {
		this.subType = subType;
	}
	//页面传值,不持久化
	private String aclIds;
    private String domainName; //制作单位名称
    private String statusId ;//页面传值，edocFormStatusId
    private Boolean isOuterAcl ; //是否是外单位授权
    
    public Boolean getIsOuterAcl() {
		return isOuterAcl;
	}
	public void setIsOuterAcl(Boolean isOuterAcl) {
		this.isOuterAcl = isOuterAcl;
	}
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getAclIds() {
		return aclIds;
	}
	public void setAclIds(String aclIds) {
		this.aclIds = aclIds;
	}
	public Boolean getIsunit() {
		return isunit;
	}
	public void setIsunit(Boolean isunit) {
		this.isunit = isunit;
	}
    
    public Boolean getIsSystem() {
		return isSystem;
	}
	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}
	public Boolean getShowLog() {
		return showLog;
	}
	public void setShowLog(Boolean showLog) {
		this.showLog = showLog;
	}
	public void setFileId(Long fileId)
    {
    	this.fileId=fileId;
    }
    public Long getFileId()
    {
    	return this.fileId;
    }
    
    
    public String getName()
    {
    	return name;
    }
    
    public void setName(String name)
    {
    	this.name = name;
    }
    
    public String getDescription()
    {
    	return description;
    }
    
    public void setDescription(String description)
    {
    	this.description = description;
    }
    
    public String getContent()
    {
    	return content;
    }
    
    public void setContent(String content)
    {
    	this.content = content;
    }
    
    public Integer getType()
    {
    	return type;
    }
    
    public void setType(Integer type)
    {
    	this.type = type;
    }
 
    public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Long getCreateUserId()
    {
    	return createUserId;
    }
    
    public void setCreateUserId(Long createUserId)
    {
    	this.createUserId = createUserId;
    }
    
    public Timestamp getCreateTime()
    {
    	return createTime;
    }
    
    public void setCreateTime(Timestamp createTime)
    {
    	this.createTime = createTime;
    }
    
    public Long getLastUserId()
    {
    	return lastUserId;
    }
    
    public void setLastUserId(Long lastUserId)
    {
    	this.lastUserId = lastUserId;
    }
    
    public Timestamp getLastUpdate()
    {
    	return lastUpdate;
    }
    
    public void setLastUpdate(Timestamp lastUpdate)
    {    	
    	this.lastUpdate = lastUpdate;
    }
    
    public Integer getStatus()
    {
    	return status;
    }
    
    public void setStatus(Integer status)
    {
    	this.status = status;
    }   
    
    public Set<EdocFormElement> getEdocFormElements()
    {
    	return edocFormElements;
    }
    
    public void setEdocFormElements(Set<EdocFormElement> edocFormElements)
    {
    	this.edocFormElements = edocFormElements;
    }
    
    public Set<EdocFormAcl> getEdocFormAcls()
    {
    	return edocFormAcls;
    }
    
    public void setEdocFormAcls(Set<EdocFormAcl> edocFormAcls)
    {
    	this.edocFormAcls = edocFormAcls;
    }

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	
	public Object clone() throws CloneNotSupportedException {
		EdocForm ret=(EdocForm)super.clone();
		EdocFormElement tempObj=null;
		Set<EdocFormElement> edocFormElements=ret.getEdocFormElements();
		Set<EdocFormElement> efList=new HashSet(); 
		for(EdocFormElement fe:edocFormElements)
		{
			tempObj=(EdocFormElement)fe.clone();
			efList.add(tempObj);
		}
		ret.setEdocFormElements(efList);
		
		Set<EdocFormAcl> edocFormAcls=ret.getEdocFormAcls();
		Set<EdocFormAcl> newEedocFormAcls=new HashSet();
		EdocFormAcl tempFa=null;
		for(EdocFormAcl ea:edocFormAcls)
		{
			tempFa=(EdocFormAcl)ea.clone();
			newEedocFormAcls.add(tempFa);
		}
		ret.setEdocFormAcls(newEedocFormAcls);
		return ret;
	}
	
	//重新设置对象ID用于复制对象
	public void resetId()
	{
		this.setNewId();
		for(EdocFormElement fe:edocFormElements)
		{
			fe.setNewId();
			fe.setFormId(this.id);
		}
		for(EdocFormAcl ea:edocFormAcls)
		{
			ea.setNewId();
			ea.setFormId(this.id);
		}
	}
	public Set<EdocFormFlowPermBound> getEdocFormFlowPermBound() {
		return edocFormFlowPermBound;
	}
	public void setEdocFormFlowPermBound(
			Set<EdocFormFlowPermBound> edocFormFlowPermBound) {
		this.edocFormFlowPermBound = edocFormFlowPermBound;
	}
	
	public boolean isIncludeEdocElement(Long eleId)
	{
		for(EdocFormElement efe:edocFormElements)
		{
			if(efe.getElementId().longValue()==eleId.longValue())
			{
				return true;
			}
		}		
		return false;
	}
    public Set<EdocFormExtendInfo> getEdocFormExtendInfo() {
    	if(edocFormExtendInfo == null)
    		edocFormExtendInfo = new HashSet<EdocFormExtendInfo>();
		return edocFormExtendInfo;
	}
	public void setEdocFormExtendInfo(Set<EdocFormExtendInfo> edocFormExtendInfo) {
		this.edocFormExtendInfo = edocFormExtendInfo;
	}
}
