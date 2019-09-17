package com.seeyon.v3x.edoc.webmodel;

/**
 * 公文单处理意见与权限绑定WEB-MODEL
 * @author lindb
 *
 */
public class FormBoundPerm {
	private String permName;
	private String permItem;
	private String permItemName;
	private String permItemList;
	private String processName;
	private String processItemName;
	private String sortType;// 节点的排序方式
	
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	public String getProcessItemName() {
		return processItemName;
	}
	public void setProcessItemName(String processItemName) {
		this.processItemName = processItemName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getPermItemList() {
		return permItemList;
	}
	public void setPermItemList(String permItemList) {
		this.permItemList = permItemList;
	}
	public String getPermItemName() {
		return permItemName;
	}
	public void setPermItemName(String permItemName) {
		this.permItemName = permItemName;
	}
	public String getPermItem() {
		return permItem;
	}
	public void setPermItem(String permItem) {
		this.permItem = permItem;
	}
	public String getPermName() {
		return permName;
	}
	public void setPermName(String permName) {
		this.permName = permName;
	}
}
