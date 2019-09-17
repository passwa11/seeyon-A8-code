package com.seeyon.v3x.edoc.manager.statistics;

public class OrderContent implements Comparable {
	private int order;	//排序号
	private String contentName; //统计名称
	private int num; //统计数量
	private int type; //组织类型
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	@Override
	public int compareTo(Object other) {
		if( other instanceof OrderContent){
			OrderContent oc = (OrderContent)other;
			if (order < oc.order)
		   {
		    return -1;
		   }
		   if (order > oc.order)
		   {
		    return 1;
		   }
		}
		return 0;
	}
	
	

}