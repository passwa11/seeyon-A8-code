package com.seeyon.v3x.edoc.domain;

import java.sql.Timestamp;
import java.util.Comparator;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocMarkReserve extends BaseModel implements java.io.Serializable, Comparator<EdocMarkReserve> {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6373122309003879641L;
	private Long id;
	private Long markDefineId;
	private Integer type = 1;//1预留文号 2线下占用
	private Integer startNo;
	private Integer endNo;
	private String docMark;
	private String docMarkEnd;
	private Long createUserId;
	private Timestamp createTime;
	private Long domainId;
	private Integer yearNo;
	private Boolean yearEnabled;
	private String description;//预留说明
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMarkDefineId() {
		return markDefineId;
	}
	public void setMarkDefineId(Long markDefineId) {
		this.markDefineId = markDefineId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getStartNo() {
		return startNo;
	}
	public void setStartNo(Integer startNo) {
		this.startNo = startNo;
	}
	public Integer getEndNo() {
		return endNo;
	}
	public void setEndNo(Integer endNo) {
		this.endNo = endNo;
	}
	public Long getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
		
	public Boolean getYearEnabled() {
		return yearEnabled;
	}
	
	public void setYearEnabled(Boolean yearEnabled) {
		this.yearEnabled = yearEnabled;
	}
	
	@Override
	public int compare(EdocMarkReserve o1, EdocMarkReserve o2) {
		Timestamp t1 = o1.getCreateTime();
		Timestamp t2 = o2.getCreateTime();
		int res=0;
		if(t1!=null && t2!=null) {
			res = t1.compareTo(t2);
		} else if(t1==null && t2!=null) {
			res=-1;
		} else if(t1!=null && t2==null) {
			res=1;
		}
		if(res == 0) {
			Integer d1 = o1.getStartNo();
			Integer d2 = o2.getStartNo();
			if(d1!=null && d2!=null) {
				res = d1.compareTo(d2);
			} else if(d1==null && d2!=null) {
				res=-1;
			} else if(d1!=null && d2==null) {
				res=1;
			}
		}
		return res==0?0:res>0?-1:1;
	}
	public Integer getYearNo() {
		return yearNo;
	}
	public void setYearNo(Integer yearNo) {
		this.yearNo = yearNo;
	}
	public String getDocMark() {
		return docMark;
	}
	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}
	public String getDocMarkEnd() {
		return docMarkEnd;
	}
	public void setDocMarkEnd(String docMarkEnd) {
		this.docMarkEnd = docMarkEnd;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
