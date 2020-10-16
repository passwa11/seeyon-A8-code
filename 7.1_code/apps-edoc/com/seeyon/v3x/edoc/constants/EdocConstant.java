package com.seeyon.v3x.edoc.constants;

public interface EdocConstant {

	/** 直接新建公文操作 */
	public static final String COMM_NEW_EDOC_CREATE = "";
	
	/** 对已有流程的协同（重复发起、转发、保存待发） */
	public static final String COMM_NEW_EDOC_TRANSMITSEND = "transmitSend";
	
	/** 登记公文 */
	public static final String COMM_NEW_EDOC_REGISTER = "register";
	
	/** 登记删除状态 因为sqlerver tiyint不能置为-1，暂用10代替     这里不使用了，EdocNavigation */
	//public static final int DELETE_STATE = 10;
	
	//公文查看时将summary对象保存进session中的key名称
	public static final String SUMMARY_TO_SESSEION = "summaryToSession";
	
	//公文查看时将affair对象保存进session中的key名称
	public static final String AFFAIR_TO_SESSEION = "affairToSession";
	
	//公文查看时，意见保存进session中的key名称
	public static final String EDOC_OPINION_MODEL_MAP_TO_SESSEION = "EdocOpinionModelMapToSession";
	
	//公文查看时，节点权限为KEY,公文元素名称为value的Hashtable保存进session中的key名称
	public static final String POLICY_HASHTABLE_TO_SESSEION = "policyHashtableToSession";
	
	
	//-----资源注册常量---start--
	public static final String F07_SENDWAITSEND = "F07_sendWaitSend";  //发文待发
	
	public static final String F07_SENDAPPEND = "F07_sendAppend";  //发文在办
	
	public static final String EDOC_LISTREADING = "F07_listReading"; //收文待阅
	
	public static final String EDOC_LISTREADED = "F07_listReaded";   //收文已阅
	
	public static final String F07_RECWAITSEND = "F07_recWaitSend";  //收文待发
	
	public static final String F07_RECAPPEND = "F07_recAppend";   //收文在办
	
	public static final String F07_REC_REGISTER = "F07_recRegister";   //收文待登记
	
	public static final String F07_SIGNAPPEND = "F07_signAppend";   //签报在办
	
	public static final String F07_SIGNWAITSEND = "F07_signWaitSend";   //签报待发
	//-----资源注册常量---end--
}
