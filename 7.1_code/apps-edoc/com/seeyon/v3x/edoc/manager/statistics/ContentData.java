package com.seeyon.v3x.edoc.manager.statistics;

import java.util.List;

public class ContentData {

	private int contentType;	//统计内容类型
	private List contents;		//每一个类型下的 统计内容列表数据
	
	public int getContentType() {
		return contentType;
	}
	public void setContentType(int contentType) {
		this.contentType = contentType;
	}
	public List getContents() {
		return contents;
	}
	public void setContents(List contents) {
		this.contents = contents;
	}
}



