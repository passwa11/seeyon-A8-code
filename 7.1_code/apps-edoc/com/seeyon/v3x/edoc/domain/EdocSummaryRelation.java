package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSummaryRelation extends BaseModel implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2714953427831001275L;

	private Long id;
	
	/*********当前收文或者发文的summaryId****************/
	private Long summaryId;
	
	/*********关联当前收文或者发文的summaryId****************/
	private Long relationEdocId;
	
	/**********当前收文或者发文的edocType***************/
	private int edocType;
	
	private Long memberId;//转发人ID
	
	private Integer type; //区分已登记和待分发所关联的发文(1 已登记    2 待分发)

	private Long recAffairId; //转发为发文的收文 对应的affairId
	
	public Long getRecAffairId() {
        return recAffairId;
    }
    public void setRecAffairId(Long recAffairId) {
        this.recAffairId = recAffairId;
    }

    public Long getMemberId() {
		return memberId;
	}

	

	public Integer getType() {
		return type;
	}



	public void setType(Integer type) {
		this.type = type;
	}



	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(Long summaryId) {
		this.summaryId = summaryId;
	}

	public Long getRelationEdocId() {
		return relationEdocId;
	}

	public void setRelationEdocId(Long relationEdocId) {
		this.relationEdocId = relationEdocId;
	}

	public int getEdocType() {
		return edocType;
	}

	public void setEdocType(int edocType) {
		this.edocType = edocType;
	}
	
	
	

}
