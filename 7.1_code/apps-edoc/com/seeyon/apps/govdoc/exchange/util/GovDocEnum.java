package com.seeyon.apps.govdoc.exchange.util;

public class GovDocEnum {
	
	public enum GovdocExchangeOrgType{
		department(0,"部门"),
		account(1,"单位"),
		extrnalAccount(2,"外部单位");
		private int key;
		private String value;
		private GovdocExchangeOrgType(int key,String value){
			this.key = key;
			this.value = value;
		}
		
		public static GovdocExchangeOrgType getGovdocExchangeOrgType(int key){
			for(GovdocExchangeOrgType e:GovdocExchangeOrgType.values()){
				if(e.getKey() == key){
					return e;
				}
			}
			return null;
		}
		public int getKey() {
			return key;
		}
		public void setKey(int key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	/**
	 * 文件类型
	 * @author Administrator
	 *
	 */
	public enum GovdocExchangeFileFlag{
		mainBody(0,"正文"),
		attachment(1,"附件");
		private int key;
		private String value;
		private GovdocExchangeFileFlag(int key,String value){
			this.key = key;
			this.value = value;
		}
		
		public static GovdocExchangeFileFlag getGovdocExchangeFileFlag(int key){
			for(GovdocExchangeFileFlag e:GovdocExchangeFileFlag.values()){
				if(e.getKey() == key){
					return e;
				}
			}
			return null;
		}
		public int getKey() {
			return key;
		}
		public void setKey(int key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	/**
	 * 
	 * 数据交换状态
	 * @author Administrator
	 *
	 */
	public enum ExchangeDetailStatus{
		waitSend(0,"待交换"),
		waitSign(1,"待签收"),
		hasSign(2,"已签收"),
		hasFenBan(3,"已分办"),
		beingProcessed(4,"进行中"),
		hasSignOff(5,"签收停办"),
		hasBack(10,"已回退"),
		hasCancel(11,"已撤销"),
		hasStop(12,"已终止"),
		ended(13,"已结束"),
		voidByTakeback(14,"取回作废"), //省级专版，公文结束后可以取回。交换数据处于作废状态
		draftFenBan(15,"分办待发");//涉及到升级，但因数据很少，暂未升级
		
		private int key;
		private String value;
		private ExchangeDetailStatus(int key,String value){
			this.key = key;
			this.value = value;
		}
		
		public static ExchangeDetailStatus getExchangeDetailStatus(int key){
			for(ExchangeDetailStatus e:ExchangeDetailStatus.values()){
				if(e.getKey() == key){
					return e;
				}
			}
			return null;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	/**
	 * 联合发文字段
	 * @author Administrator
	 *
	 */
	public enum Field4JointlyIssued{
		subject,doc_mark,serial_no,send_to,copy_to,report_to,send_unit,unit_level,secret_level,doc_type,urgent_level,copies,createdate;
		private Field4JointlyIssued(){
			
		}
		public static Field4JointlyIssued getField4JointlyIssued(String name){
			for(Field4JointlyIssued field4JointlyIssued:Field4JointlyIssued.values()){
				if(field4JointlyIssued.name().equalsIgnoreCase(name)){
					return field4JointlyIssued;
				}
			}
			return null;
		}
	}

}
