package com.seeyon.apps.govdoc.vo;

import java.util.List;

/**
 * 领导批示编号VO
 * @author tanggl
 *
 */
public class GovdocPishiVO {
	
	private Object pishiname;
	private Integer nowyear;
	private List<Integer> pishiyear;
	private Object nowpishiNo;
	private String pishiNos;
	private String proxydate;
	private String pishiType;
	
	public Object getPishiname() {
		return pishiname;
	}
	public void setPishiname(Object pishiname) {
		this.pishiname = pishiname;
	}
	public Integer getNowyear() {
		return nowyear;
	}
	public void setNowyear(Integer nowyear) {
		this.nowyear = nowyear;
	}
	public List<Integer> getPishiyear() {
		return pishiyear;
	}
	public void setPishiyear(List<Integer> pishiyear) {
		this.pishiyear = pishiyear;
	}
	public Object getNowpishiNo() {
		return nowpishiNo;
	}
	public void setNowpishiNo(Object nowpishiNo) {
		this.nowpishiNo = nowpishiNo;
	}
	public String getPishiNos() {
		return pishiNos;
	}
	public void setPishiNos(String pishiNos) {
		this.pishiNos = pishiNos;
	}
	public String getProxydate() {
		return proxydate;
	}
	public void setProxydate(String proxydate) {
		this.proxydate = proxydate;
	}
	public String getPishiType() {
		return pishiType;
	}
	public void setPishiType(String pishiType) {
		this.pishiType = pishiType;
	}
	
}
