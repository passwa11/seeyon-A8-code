package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;


/**
 * EdocMarkHistory generated by MyEclipse - Hibernate Tools
 */
public class EdocMarkHistory extends BaseModel implements Serializable {
     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Fields
    private EdocMarkDefinition edocMarkDefinition;
    private Long categoryId;
    private Long markDefId = -1L;
	private Long edocId = -1L;
	private String subject;
	private String docMark;
	private Integer docMarkNo;
	private Integer yearNo;
	private Long createUserId;
	private Date createTime;
	private Long lastUserId;
	private Date completeTime;
	
	private int markNum=1;
	private Integer govdocType=0;
	private Integer transferStatus=0;
	private Long markDefinitionId;
	private Integer realUsed = 1;//1真正占用 0做为同流水号被存在(用于同流水号都被占用)
	private Long domainId;
	private Integer selectType = 0;//占号类型 0自动 1手写 2断号 3预留 4线下占用
	private Integer markType;//0公文文号 1内部文号 2签收编号
	private Long reserveId;//预留id
	private String description;
	
	public void setMarkNum(int markNum)
	{
		this.markNum=markNum;
	}
	public int getMarkNum()
	{
		return this.markNum;
	}

    // Constructors

    /** default constructor */
    public EdocMarkHistory() {
    }	
   
    // Property accessors   

    public EdocMarkDefinition getEdocMarkDefinition() {
        return this.edocMarkDefinition;
    }
    
    public void setEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        this.edocMarkDefinition = edocMarkDefinition;
    }

    public Long getEdocId() {
    	if(edocId == null) {
			return -1L;
		}
		return edocId;
    }
    
    public void setEdocId(Long edocId) {
    	if(edocId==null) {
			this.edocId = -1L;
		} else {
			this.edocId = edocId;
		}
    }

    public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getDocMark() {
        return this.docMark;
    }
    
    public void setDocMark(String docMark) {
        this.docMark = docMark;
    }

    public Long getCreateUserId() {
        return this.createUserId;
    }
    
    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getLastUserId() {
        return this.lastUserId;
    }
    
    public void setLastUserId(Long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Date getCompleteTime() {
        return this.completeTime;
    }
    
    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
	public Integer getDocMarkNo() {
		return docMarkNo;
	}
	public void setDocMarkNo(Integer docMarkNo) {
		this.docMarkNo = docMarkNo;
	}
	public Integer getGovdocType() {
		return govdocType;
	}
	public void setGovdocType(Integer govdocType) {
		this.govdocType = govdocType;
	}
	public Integer getTransferStatus() {
		return transferStatus;
	}
	public void setTransferStatus(Integer transferStatus) {
		this.transferStatus = transferStatus;
	}
	public Long getMarkDefinitionId() {
		return markDefinitionId;
	}
	public void setMarkDefinitionId(Long markDefinitionId) {
		this.markDefinitionId = markDefinitionId;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public Long getMarkDefId() {
		if(markDefId == null) {
			return -1L;
		}
		return markDefId;
	}
	public void setMarkDefId(Long markDefId) {
		if(markDefId==null) {
			this.markDefId = -1L;
		} else {
			this.markDefId = markDefId;
		}
	}
	public Integer getRealUsed() {
		return realUsed;
	}
	public void setRealUsed(Integer realUsed) {
		this.realUsed = realUsed;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	public Integer getSelectType() {
		return selectType;
	}
	public void setSelectType(Integer selectType) {
		this.selectType = selectType;
	}
	public Long getReserveId() {
		return reserveId;
	}
	public void setReserveId(Long reserveId) {
		this.reserveId = reserveId;
	}
	public Integer getMarkType() {
		return markType;
	}
	public void setMarkType(Integer markType) {
		this.markType = markType;
	}
	public Integer getYearNo() {
		return yearNo;
	}
	public void setYearNo(Integer yearNo) {
		this.yearNo = yearNo;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
   
}