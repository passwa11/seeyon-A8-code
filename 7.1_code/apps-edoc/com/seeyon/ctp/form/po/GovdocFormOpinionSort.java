package com.seeyon.ctp.form.po;

import com.seeyon.ctp.common.po.BasePO;

public class GovdocFormOpinionSort extends BasePO {
	private long formId; //公文单Id
	private String processName; //处理意见名称
	private String flowPermName; //绑定节点权限名称
	private String flowPermNameLabel; //绑定节点权限的中文名称
	private String sortType;// 节点的排序方式
	private Long domainId;  //单位
	
	public long getFormId() {
		return formId;
	}
	public void setFormId(long formId) {
		this.formId = formId;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getFlowPermName() {
		return flowPermName;
	}
	public void setFlowPermName(String flowPermName) {
		this.flowPermName = flowPermName;
	}
	public String getFlowPermNameLabel() {
		return flowPermNameLabel;
	}
	public void setFlowPermNameLabel(String flowPermNameLabel) {
		this.flowPermNameLabel = flowPermNameLabel;
	}
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	
}
