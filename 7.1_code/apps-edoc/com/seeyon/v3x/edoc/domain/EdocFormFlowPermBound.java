package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 * 公文单处理意见绑定表
 * @author Administrator
 *
 */
public class EdocFormFlowPermBound extends BaseModel implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3109694331568305668L;
	private long edocFormId; //公文单Id
	private String processName; //处理意见名称
	private String flowPermName; //绑定节点权限名称
	private String flowPermNameLabel; //绑定节点权限的中文名称
	private String sortType;// 节点的排序方式
	private Long domainId;  //单位

	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	public String getFlowPermNameLabel() {
		return flowPermNameLabel;
	}
	public void setFlowPermNameLabel(String flowPermNameLabel) {
		this.flowPermNameLabel = flowPermNameLabel;
	}
	public long getEdocFormId() {
		return edocFormId;
	}
	public void setEdocFormId(long edocFormId) {
		this.edocFormId = edocFormId;
	}
	public String getFlowPermName() {
		return flowPermName;
	}
	public void setFlowPermName(String flowPermName) {
		this.flowPermName = flowPermName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	
	
}
