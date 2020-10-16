package com.seeyon.v3x.edoc.webmodel;

import java.io.Serializable;

public class EdocSummaryCountVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5851064007686530847L;
	/**
	 * 
	 */
	
	private Integer listPendingSize = 0;
	private Integer listZcdbSize = 0;
	private Integer listSentSize = 0;
	private Integer listWaitSize = 0;
	private Integer listDoneAllSize = 0;
	private Integer edocType = 0;
	
	public Integer getListPendingSize() {
		return listPendingSize;
	}
	public void setListPendingSize(Integer listPendingSize) {
		this.listPendingSize = listPendingSize;
	}
	public Integer getListZcdbSize() {
		return listZcdbSize;
	}
	public void setListZcdbSize(Integer listZcdbSize) {
		this.listZcdbSize = listZcdbSize;
	}
	public Integer getListSentSize() {
		return listSentSize;
	}
	public void setListSentSize(Integer listSentSize) {
		this.listSentSize = listSentSize;
	}
	public Integer getListWaitSize() {
		return listWaitSize;
	}
	public void setListWaitSize(Integer listWaitSize) {
		this.listWaitSize = listWaitSize;
	}
	public Integer getListDoneAllSize() {
		return listDoneAllSize;
	}
	public void setListDoneAllSize(Integer listDoneAllSize) {
		this.listDoneAllSize = listDoneAllSize;
	}
	public Integer getEdocType() {
		return edocType;
	}
	public void setEdocType(Integer edocType) {
		this.edocType = edocType;
	}
	
}
