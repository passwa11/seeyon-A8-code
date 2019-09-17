package com.seeyon.v3x.edoc.webmodel;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;

public class EdocMarkReserveVO implements java.io.Serializable, Comparator<EdocMarkReserveVO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2919272941610434190L;
	/**
	 * 
	 */
	
	private Long id;
	private Long markDefineId;
	private Integer type;//1预留文号 2线下占用
	private Integer startNo;
	private Integer endNo;
	private Long createUserId;
	private Timestamp createTime;
	private Long domainId;
	private String description;
	private String wordNo;
	private String yearNo;
	private String formatA;
	private String formatB;
	private String formatC;
	private String expression;
	private String reserveNo;
	private String reserveLimitNo;
	private String docMarkDisplay;
	private Integer markNumber = -1;
	private Integer orderBy = 1;//1时间排序 2流水排序
	private EdocMarkReserve edocMarkReserve;
	private EdocMarkCategory edocMarkCategory;
	private EdocMarkDefinition edocMarkDefinition;
	private List<EdocMarkReserveNumber> reserveNumberList;
	
	public void setFieldValue(EdocMarkReserve reserve) {
		this.id = reserve.getId();
		this.type = reserve.getType();
		this.startNo = reserve.getStartNo();
		this.endNo = reserve.getEndNo();
		this.yearNo = String.valueOf(reserve.getYearNo());
		this.markDefineId = reserve.getMarkDefineId();
		this.createTime  = reserve.getCreateTime();
		this.createUserId = reserve.getCreateUserId();
		this.domainId =  reserve.getDomainId();
	}
	
	public EdocMarkReserve convert(EdocMarkReserveVO reserveVO) {
		EdocMarkReserve reserve = new EdocMarkReserve(); 
		reserve.setId(this.getId());
		reserve.setType(this.getType());
		reserve.setStartNo(this.getStartNo());
		reserve.setEndNo(this.getEndNo());
		reserve.setYearNo(Integer.parseInt(this.getYearNo()));
		reserve.setMarkDefineId(this.getMarkDefineId());
		reserve.setCreateTime(this.getCreateTime());
		reserve.setCreateUserId(this.getCreateUserId());
		reserve.setDomainId(this.getDomainId());
		return reserve;
	}
	
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
	public String getWordNo() {
		return wordNo;
	}
	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
	}
	public String getYearNo() {
		return yearNo;
	}
	public void setYearNo(String yearNo) {
		this.yearNo = yearNo;
	}
	public String getFormatA() {
		return formatA;
	}
	public void setFormatA(String formatA) {
		this.formatA = formatA;
	}
	public String getFormatB() {
		return formatB;
	}
	public void setFormatB(String formatB) {
		this.formatB = formatB;
	}
	public String getFormatC() {
		return formatC;
	}
	public void setFormatC(String formatC) {
		this.formatC = formatC;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public EdocMarkCategory getEdocMarkCategory() {
		return edocMarkCategory;
	}
	public void setEdocMarkCategory(EdocMarkCategory edocMarkCategory) {
		this.edocMarkCategory = edocMarkCategory;
	}
	public EdocMarkDefinition getEdocMarkDefinition() {
		return edocMarkDefinition;
	}
	public void setEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
		this.edocMarkDefinition = edocMarkDefinition;
	}
	public Integer getMarkNumber() {
		return markNumber;
	}
	public void setMarkNumber(Integer markNumber) {
		this.markNumber = markNumber;
	}	
	public String getReserveNo() {
		return reserveNo;
	}
	public void setReserveNo(String reserveNo) {
		this.reserveNo = reserveNo;
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

	@Override
	public int compare(EdocMarkReserveVO o1, EdocMarkReserveVO o2) {
		Timestamp t1 = o1.getCreateTime();
		Timestamp t2 = o2.getCreateTime();
		int res=0;
		if(orderBy==null || orderBy.intValue() == 1) {
			if(t1!=null && t2!=null) {
				res = t1.compareTo(t2);
			} else if(t1==null && t2!=null) {
				res=-1;
			} else if(t1!=null && t2==null) {
				res=1;
			}
		}
		if(res == 0) {
			Integer d1 = o1.getMarkNumber();
			Integer d2 = o2.getMarkNumber();
			if(d1!=null && d2!=null) {
				res = d2.compareTo(d1);
			} else if(d2==null && d1!=null) {
				res=-1;
			} else if(d2!=null && d1==null) {
				res=1;
			}
		}
		return res==0?0:res>0?-1:1;
	}

	public String getReserveLimitNo() {
		return reserveLimitNo;
	}

	public void setReserveLimitNo(String reserveLimitNo) {
		this.reserveLimitNo = reserveLimitNo;
	}

	public Integer getOrderBy() {
		return orderBy;
	}

	public EdocMarkReserveVO setOrderBy(Integer orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public List<EdocMarkReserveNumber> getReserveNumberList() {
		return reserveNumberList;
	}

	public void setReserveNumberList(List<EdocMarkReserveNumber> reserveNumberList) {
		this.reserveNumberList = reserveNumberList;
	}

	public String getDocMarkDisplay() {
		return docMarkDisplay;
	}

	public void setDocMarkDisplay(String docMarkDisplay) {
		this.docMarkDisplay = docMarkDisplay;
	}

	public EdocMarkReserve getEdocMarkReserve() {
		return edocMarkReserve;
	}

	public void setEdocMarkReserve(EdocMarkReserve edocMarkReserve) {
		this.edocMarkReserve = edocMarkReserve;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
