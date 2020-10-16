package com.seeyon.v3x.edoc.util;

public class EdocStatEnum {

	public enum EdocStatListTypeEnum {
		doAll(1),//总经办
		finished(2),//已办结
		wait_finished(3),//未办结
		sent(4),//已发
		done(5),//已办
		pending(6),//待办
		zcdb(7),//在办
		readAll(8),//总阅件
		readed(9),//已阅
		reading(10),//待阅
		undertaker(11);//承办数
		
		private int key;
		EdocStatListTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return this.key;
		}
		public int key() {
			return this.key;
		}
	}
	
	public enum EdocStatResultTypeEnum {
		doAndRead(1),//待办/已办/待阅/已阅
		doAll(2),//总经办
		readAll(3),//已发
		zcdb(4),//在办
		undertaker(5),//承办数
		sent(6),//已发
		pending(7),//待办
		done(8),//已办
		reading(9),//待阅
		readed(10),//已阅
		pendingAndDone(11),//待办/已办
		readingAndReaded(12);//待阅/已阅
		
		private int key;
		EdocStatResultTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return this.key;
		}
		public int key() {
			return this.key;
		}
	}
	
	public enum EdocStatDisplayTypeEnum {
		department(1),//部门
		member(2),//人员
		time(3);//时间
		
		private int key;
		EdocStatDisplayTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return this.key;
		}
		public int key() {
			return this.key;
		}
	}
	
	public enum EdocStatDisplayTimeTypeEnum {
		year(1),//年
		quarter(2),//月
		month(3),//日
		day(4);
		
		private int key;
		EdocStatDisplayTimeTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return this.key;
		}
		public int key() {
			return this.key;
		}
	}	
	
	public enum EdocStatRoleTypeEnum {
		account(1),//单位
		dept(2),//部门
		dept_multi(3);//多部门
		
		private int key;
		EdocStatRoleTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return this.key;
		}
		public int key() {
			return this.key;
		}
	}	
	
}
