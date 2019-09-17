package com.seeyon.apps.govdoc.constant;

public class GovdocConfigKey {

	/**
	 * 新公文系统开关
	 */
	public static final String GOVDOC_SWITCH_KEY = "govdoc_switch";
	
	public static final String GOVDOC_SWITCH_GOVDOCVIEW = "govdocview";
	public static final String GOVDOC_SWITCH_GOVDOCVIEW0 = "0";//经典布局
	public static final String GOVDOC_SWITCH_GOVDOCVIEW1 = "1";//一屏布局A
	public static final String GOVDOC_SWITCH_GOVDOCVIEW2 = "2";//一屏布局B
	
	
	public static final String GOVDOC_SWITCH_DOC_MARK = "govdoc_switch_doc_mark";
	public static final String GOVDOC_SWITCH_SERIAL_NO = "govdoc_switch_serial_no";
	public static final String GOVDOC_SWITCH_SIGN_MARK = "govdoc_switch_sign_mark";
	
	//启用手写文号
	public static final String DOC_MARK_HANDINPUT = "doc_mark_handInput";
	public static final String SERIAL_NO_HANDINPUT = "serial_no_handInput";
	public static final String SIGN_MARK_HANDINPUT = "sign_mark_handInput";
	
	//编辑文号时显示断号、预留文号(公文文号)
	public static final String DOC_MARK_SHOW_CALL = "doc_mark_show_call";
	//编辑文号时显示断号、预留文号(内部文号)
	public static final String SERIAL_NO_SHOW_CALL = "serial_no_show_call";
	//编辑文号时显示断号、预留文号(签收编号)
	public static final String SIGN_MARK_SHOW_CALL = "sign_mark_show_call";
	
	//启用文号使用提醒(内部文号)
	public static final String SERIAL_NO_CHECK_CALL = "serial_no_check_call";
	//启用文号使用提醒(签收编号)
	public static final String SIGN_MARK_CHECK_CALL = "sign_mark_check_call";
	
	//公文文号按最大值自增
	public static final String DOC_MARK_MAX = "docMarkByMax";
	//公文文号按最大值自增(内部文号)
	public static final String SERIAL_NO_MAX = "innerMarkByMax";
	//公文文号按最大值自增(签收编号)
	public static final String SIGN_MARK_MAX = "signMarkByMax";
	
	//内部文号见办
	public static final String SERIAL_NO_JIANBAN = "edocInnerMarkJB";
	
	public static final int USED_TYPE_1 = 1;//模式一
	public static final int USED_TYPE_2 = 2;//模式二
	
	public static final int USED_TYPE_INDEX = 0;
	public static final int USED_TYPE_INDEX_FENGSONG = 1;
	public static final int USED_TYPE_INDEX_FINISH = 2;
	public static final int USED_TYPE_INDEX_CHECK_CALL = 3;
	
	//公文文号-发文
		//1-发起提交时占用文号，其它文不能再使用  值域(第1位分送后占号true/false,第2位启用流程结束占号true/false,第3位启用断号判重true/false)-这里只显示1,3位
		//2-发起提交时断号 值域(第1位分送后占号true/false,第2位启用流程结束占号true/false,第3位启用断号判重true/false)
	public static final String DOC_MARK_FAWEN = "doc_mark_fawen";
	public static final String DOC_MARK_FAWEN_DEFUALT_1 = "1.yes.no.no";
	public static final String DOC_MARK_FAWEN_DEFUALT_2 = "2...no";
	//公文文号-签报
		//1-发起提交时占用文号，其它文不能再使用 值域(第1位分送后占号true/false,第2位启用流程结束占号true/false,第3位启用断号判重true/false)-这里只显示3位
	public static final String DOC_MARK_QIAN = "doc_mark_qian";
	public static final String DOC_MARK_QIAN_DEFUALT_2 = "2...no";

	//内部文号-发文
		//1-发起提交时占用文号，其它文不能再使用
		//2-发起提交时断号
	//public static final String SERIAL_NO_FAWEN = "serial_no_fawen";
	//public static final String SERIAL_NO_FAWEN_DEFUALT_1 = "1...";
	//public static final String SERIAL_NO_FAWEN_DEFUALT_2 = "2...";
	//内部文号-收文/签报
		//1-发起提交时占用文号，其它文不能再使用
	public static final String SERIAL_NO_QIAN = "serial_no_qian";
	public static final String SERIAL_NO_QIAN_DEFUALT_2 = "2...";
	
	//内部文号-发文/收文/签报
		//1-发起提交时占用文号，其它文不能再使用
	public static final String SIGN_MARK_QIAN = "sign_mark_qian";
	public static final String SIGN_MARK_QIAN_DEFUALT_1 = "2...";

	public static Integer getMarkSort(String configItem) {
		if(configItem.endsWith("show_call")) {
			return 1;
		} else if(configItem.endsWith("handInput")) {
			return 2;
		} else if(configItem.endsWith("check_call")) {
			return 3;
		} else if(configItem.endsWith("ByMax")) {
			return 4;
		} else if(configItem.endsWith("_fawen")) {
			return 10;
		}  else if(configItem.endsWith("_qian")) {
			return 11;
		}
		return 1;
	}
	
	/**
	 * 
	 * @param markType
	 * @return
	 */
	public static String getMarkConfigCategory(String markType) {
		if("serial_no".equals(markType)) {
			return GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO;
		} else if("sign_mark".equals(markType)) {
			return GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK;
		} 
		return GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK;
	}
	
}
