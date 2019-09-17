package com.seeyon.v3x.edoc.domain;

import java.util.Comparator;

import com.seeyon.v3x.common.domain.BaseModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;

public class EdocMarkReserveNumber extends BaseModel implements java.io.Serializable, Comparator<EdocMarkReserveNumber> {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Long reserveId;
	private Long markDefineId;
	private Integer type;//1预留文号 2线下占用
	private String docMark;
	private Integer markNo;
	private Integer yearNo;
	private Boolean isUsed = Boolean.FALSE;
	private EdocMarkNoModel markNoVo;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReserveId() {
		return reserveId;
	}

	public void setReserveId(Long reserveId) {
		this.reserveId = reserveId;
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

	public String getDocMark() {
		return docMark;
	}

	public void setDocMark(String docMark) {
		this.docMark = docMark;
	}

	public Integer getMarkNo() {
		return markNo;
	}

	public void setMarkNo(Integer markNo) {
		this.markNo = markNo;
	}

	public Boolean getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}

	public EdocMarkNoModel getMarkNoVo() {
		return markNoVo;
	}

	public void setMarkNoVo(EdocMarkNoModel markNoVo) {
		this.markNoVo = markNoVo;
	}

	public Integer getYearNo() {
		return yearNo;
	}

	public void setYearNo(Integer yearNo) {
		this.yearNo = yearNo;
	}

	@Override
	public int compare(EdocMarkReserveNumber o1, EdocMarkReserveNumber o2) {
		int res=0;
		Integer d1 = o1.getMarkNo();
		Integer d2 = o2.getMarkNo();
		if(d1!=null && d2!=null) {
			res = d2.compareTo(d1);
		} else if(d2==null && d1!=null) {
			res=-1;
		} else if(d2!=null && d1==null) {
			res=1;
		}
		return res==0?0:res>0?-1:1;
	}
	
}
