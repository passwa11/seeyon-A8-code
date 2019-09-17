package com.seeyon.v3x.edoc.constants;


public enum EdocCustomerTypeTimeEnum {

	DAY(1){
		public String getName(){ return "edoc.customer.type.today";}
	},
	YESTERDAY(2){
		public String getName(){ return "edoc.customer.type.yesterday";}
	},
	WEEK(3){
		public String getName(){ return "edoc.customer.type.thisweek";}
	},
	LAST_WEEK(4){
		public String getName(){ return "edoc.customer.type.lastweek";}
	},
	MONTH(5){
		public String getName(){ return "edoc.customer.type.thismonth";}
	},
	LAST_MONTH(6){
		public String getName(){ return "edoc.customer.type.lastmonth";}
	},
	YEAR(7){
		public String getName(){ return "edoc.customer.type.thisyear";}
	},
	LAST_YEAR(8){
		public String getName(){ return "edoc.customer.type.lastyear";}
	};
	
	// 标识 用于数据库存储
	private int key;

	EdocCustomerTypeTimeEnum(int key) {
		this.key = key;
	}

	public int getKey() {
		return this.key;
	}

	public int key() {
		return this.key;
	}
		
	public abstract String getName();

	/**
	 * 根据key得到枚举类型
	 * 
	 * @param key
	 * @return
	 */
	public static EdocCustomerTypeTimeEnum valueOf(int key) {
		EdocCustomerTypeTimeEnum[] enums = EdocCustomerTypeTimeEnum.values();

		if (enums != null) {
			for (EdocCustomerTypeTimeEnum enum1 : enums) {
				if (enum1.key() == key) {
					return enum1;
				}
			}
		}

		return null;
	}
}
