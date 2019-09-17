package com.seeyon.apps.govdoc.constant;

public class GovdocMarkEnum {

	//公文文号，内部文号
    public static enum MarkType{edocMark,edocInMark,edocSignMark};
    
	public enum MarkCategoryEnum {
		small(Short.parseShort("0")),//小流水
		big(Short.parseShort("1"));//大流水
		
		private short key;
		MarkCategoryEnum(short key) {
			this.key = key;
		}
		public short key() {
			return key;
		}
	}
	
    public enum SelectTypeEnum {
    	zidong,
    	shouxie,
    	duanhao,
    	yuliu,
    	xianxia
    }
    
    public enum UsedStateEnum {
    	unused,
    	used
    }
    
    public enum GovdocJianbanTypeEnum {
    	no(1),//非见办
    	yes(2);//是见办
    	
    	private int key;
    	GovdocJianbanTypeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return key;
		}
    }
    
    public static int EDOC_MARK_EMPTY = 0;    //未使用
	public static int EDOC_MARK_USED = 1;     //暂时使用
	public static int EDOC_MARK_OCCUPIED = 2; //已占用
    
	public enum EdocMarkStateEnum {
		empty(0),//未使用
    	used(1),//暂时使用
    	occupied(2);//已占用
    	
    	private int key;
    	EdocMarkStateEnum(int key) {
			this.key = key;
		}
		public int key() {
			return key;
		}
	}
	
    public enum MarkDefStateEnum {
    	draft(Short.parseShort("0")),//公文文号定义状态：草稿
    	published(Short.parseShort("1")),//公文文号定义状态：已使用
    	deleted(Short.parseShort("2"));//公文文号定义状态：已删除
    	
    	private short key;
    	MarkDefStateEnum(short key) {
			this.key = key;
		}
		public short key() {
			return key;
		}
    }
	
}
