package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import com.seeyon.v3x.common.domain.BaseModel;

public class EdocFormElement extends BaseModel implements Serializable {

	// default serial version id, required for serializable classes.
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2905370069946086612L;
	private Long formId;
    private Long elementId;
    private Boolean required;
	private String elementName;
    private boolean systemType;
    private boolean access;
    
    public Boolean isRequired() {
    	if(required == null) {
    		required = Boolean.FALSE;
    	}
		return required;
	}
    
    //required 改成Boolean类型，需要增加这个get方法，否则el表达式会报错
    public Boolean getRequired() {
    	return required;
    }

	public void setRequired(Boolean required) {
		this.required = required;
	}
    public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public boolean isSystemType() {
		return systemType;
	}

	public void setSystemType(boolean systemType) {
		this.systemType = systemType;
	}

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

	public Long getFormId()
    {
    	return formId;
    }
    
    public void setFormId(Long formId)
    {
    	this.formId = formId;
    }
    
    public Long getElementId()
    {
    	return elementId;
    }
    
    public void setElementId(Long elementId)
    {
    	this.elementId = elementId;    	
    }    
    
}
