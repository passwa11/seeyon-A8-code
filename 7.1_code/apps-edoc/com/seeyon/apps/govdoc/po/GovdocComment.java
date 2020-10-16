package com.seeyon.apps.govdoc.po;

import org.apache.commons.beanutils.BeanUtils;

import com.seeyon.ctp.common.content.comment.Comment;

/**
 * 公文意见类(公文内部使用)
 *
 */
public class GovdocComment extends Comment{

    private java.util.Date 	  proxyDate;
    private String	  		  proxyDate1 = "";//保证前端页面显示的正确性
    private Integer 		  pishiNo;
    private String 			  pishiName;
    private String 			  pishiYear;
    
	public java.util.Date getProxyDate() {
		return proxyDate;
	}
	public void setProxyDate(java.util.Date proxyDate) {
		this.proxyDate = proxyDate;
	}
	public String getProxyDate1() {
		return proxyDate1;
	}
	public void setProxyDate1(String proxyDate1) {
		this.proxyDate1 = proxyDate1;
	}
	public Integer getPishiNo() {
		return pishiNo;
	}
	public void setPishiNo(Integer pishiNo) {
		this.pishiNo = pishiNo;
	}
	public String getPishiName() {
		return pishiName;
	}
	public void setPishiName(String pishiName) {
		this.pishiName = pishiName;
	}
	public String getPishiYear() {
		return pishiYear;
	}
	public void setPishiYear(String pishiYear) {
		this.pishiYear = pishiYear;
	}
    
    
}
