package com.seeyon.v3x.edoc.domain;

/**
 * 公文签收收据实体
 * @author muj
 */
public class EdocSignReceipt {
	/**
	 * 签收人姓名
	 */
	private String receipient;
	/**
	 * 签收单位名称，如果是部门签收，则为部门名称
	 */
	private String signUnit;
	/**
	 * 签收意见
	 */
	private String opinion;
	/**
	 * 签收时间
	 */
	private long signTime ;
	
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	public String getReceipient() {
		return receipient;
	}
	public void setReceipient(String receipient) {
		this.receipient = receipient;
	}
	public long getSignTime() {
		return signTime;
	}
	public void setSignTime(long signTime) {
		this.signTime = signTime;
	}
	public String getSignUnit() {
		return signUnit;
	}
	public void setSignUnit(String signUnit) {
		this.signUnit = signUnit;
	}
}
