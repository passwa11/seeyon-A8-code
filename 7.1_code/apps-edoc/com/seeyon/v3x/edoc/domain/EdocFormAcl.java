package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import com.seeyon.v3x.common.domain.BaseModel;

public class EdocFormAcl extends BaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8169071732436061441L;
	
	//	 Fields    
	private Long formId;
	private Long domainId;


	private String entityType;
	// Constructors

	/** default constructor */
	public EdocFormAcl() {	
	}
	
	/** full constructor */
    public EdocFormAcl(Long id, Long formId, Long domainId) {
    	this.id = id;
    	this.formId = formId;
    	this.domainId = domainId;
    }

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

    //  Property accessors

    public Long getFormId() {
        return this.formId;
    }
    
    public void setFormId(Long formId) {
        this.formId = formId;
    }


	
}
