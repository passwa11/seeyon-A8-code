/**
 * EdocElement.java
 * Created on 2007-4-19
 */
package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 *
 * @author <a href="mailto:handy@seeyon.com">Han Dongyou</a>
 *
 */
public class EdocElement extends BaseModel implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 20378738428390720L;
	/* 公文元素启用状态：未启用 */
    public final static int C_iStatus_Inactive = 0;
    /* 公文元素启用状态：已启用 */
    public final static int C_iStatus_Active = 1;   
    
    /* 公文元素类型：单行文本 */
    public final static int C_iElementType_String = 0;
    /* 公文元素类型：多行文本 */
    public final static int C_iElementType_Text = 1;
    /* 公文元素类型：整数类型 */
    public final static int C_iElementType_Integer = 2;
    /* 公文元素类型：小数类型 */
    public final static int C_iElementType_Decimal = 3;
    /* 公文元素类型：日期类型 */
    public final static int C_iElementType_Date = 4;
    /* 公文元素类型：下拉列表 */
    public final static int C_iElementType_List = 5;
    /* 公文元素类型：处理意见*/
    public final static int C_iElementType_Comment = 6;
    /* 公文元素类型：logo图片*/
    public final static int C_iElementType_LogoImg = 7;
    
    
    
    private String elementId;
    private String fieldName;
    private String name;
    private String inputMode;
    private int type;    
    private Long metadataId;    
    private boolean isSystem;
    private int status;
    private Long domainId;
    
    
  //属性对应的实体类名(文档中心使用)
    private String            poName;//实体名
    private String            poFieldName;//实体属性名
    
    
    
	public String getElementId() {
		return elementId;
	}
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public boolean getIsSystem() {
		return isSystem;
	}
	public void setIsSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}
	public Long getMetadataId() {
		return metadataId;
	}
	public void setMetadataId(Long metadataId) {
		this.metadataId = metadataId;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	} 
	
	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	public String getInputMode() {
		return inputMode;
	}
	public void setInputMode(String inputMode) {
		this.inputMode = inputMode;
	}
	
	public String getPoName() {
		return poName;
	}
	public void setPoName(String poName) {
		this.poName = poName;
	}
	public String getPoFieldName() {
		return poFieldName;
	}
	public void setPoFieldName(String poFieldName) {
		this.poFieldName = poFieldName;
	}
	public EdocElement clone(Long domainId){
		EdocElement ret=new EdocElement();
		ret.setNewId();
		ret.setDomainId(domainId);
		ret.setElementId(this.elementId);
		ret.setFieldName(this.fieldName);
		ret.setInputMode(this.inputMode);
		ret.setIsSystem(this.isSystem);
		ret.setMetadataId(this.metadataId);
		ret.setName(this.name);
		ret.setStatus(this.status);
		ret.setType(this.type);
		ret.setPoName(this.poName);
		ret.setPoFieldName(poFieldName);
		return ret;
	}
}

