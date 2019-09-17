package com.seeyon.v3x.edoc.manager.statistics;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.SystemProperties;

public enum StatisticsContentTypeEnum {

	sentContent(1){
		public ContentHandler getContentHandlerInstance(){
			ContentHandler handler = (ContentHandler)AppContext.getBean("sendContentImpl");
			return handler;
		}
		public String getStatContentName(){
		    String isG6=SystemProperties.getInstance().getProperty("isG6");
		    if("true".equals(isG6)){
		        return "select.statcontent.sentcontent";
		    }else{
		        return "edoc.element.doctype";
		    }
			
		}
	},
	workflowNode(2){
		public ContentHandler getContentHandlerInstance(){
			ContentHandler handler = (ContentHandler)AppContext.getBean("workflowNodeImpl");
			return handler;
		}
		public String getStatContentName(){
			return "select.statcontent.workflownode";
		}
	},
	dealSituation(3){
		public ContentHandler getContentHandlerInstance(){
			ContentHandler handler = (ContentHandler)AppContext.getBean("dealSituationImpl");
			return handler;
		}
		public String getStatContentName(){
			return "select.statcontent.dealsituation";
		}
	};
	
	
	public abstract ContentHandler getContentHandlerInstance();
	public abstract String getStatContentName();
	
	// 标识 用于数据库存储
	private int key;

	StatisticsContentTypeEnum(int key) {
		this.key = key;
	}

	public int getKey() {
		return this.key;
	}

	public int key() {
		return this.key;
	}
	
	/**
	 * 根据key得到枚举类型
	 * 
	 * @param key
	 * @return
	 */
	public static StatisticsContentTypeEnum valueOf(int key) {
		StatisticsContentTypeEnum[] enums = StatisticsContentTypeEnum.values();

		if (enums != null) {
			for (StatisticsContentTypeEnum enum1 : enums) {
				if (enum1.key() == key) {
					return enum1;
				}
			}
		}

		return null;
	}
}
