package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocZcdb extends BaseModel  implements Serializable{
	//default serial version id, required for serializable classes.
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2236432403744739276L;
	private Long affairId;
	private java.sql.Timestamp zcdbTime;
	
	public Long getAffairId() {
		return affairId;
	}
	public void setAffairId(Long affairId) {
		this.affairId = affairId;
	}
	public java.sql.Timestamp getZcdbTime() {
		return zcdbTime;
	}
	public void setZcdbTime(java.sql.Timestamp zcdbTime) {
		this.zcdbTime = zcdbTime;
	}

	
}
