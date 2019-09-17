package com.seeyon.v3x.edoc.manager.statistics;

import java.util.HashMap;
import java.util.Map;

public class StatConstants {
	public static final String BAN_WEN = "1";
	public static final String YUE_WEN = "2";
	public static final String YI_BANJIE = "3";
	public static final String WEI_BANJIE = "4";
	public static final String BANJIE_LV = "5";
	public static final String YI_YUE = "6";
	public static final String WEI_YUE = "7";
	public static final String YUEDU_LV = "8";
	public static Map<String,String> DealMap = new HashMap<String,String>();
	
	static{
	    DealMap.put(BAN_WEN, "办文");
	    DealMap.put(YUE_WEN, "阅文");
	    DealMap.put(YI_BANJIE, "已办结");
	    DealMap.put(WEI_BANJIE, "未办结");
	    DealMap.put(BANJIE_LV, "办结率");
	    DealMap.put(YI_YUE, "已阅");
	    DealMap.put(WEI_YUE, "待阅");
	    DealMap.put(YUEDU_LV, "阅读率");
	}
	
	//子部门ID标识
	public static final String CHILD_ORG_SIGN = "CHILDREN_ORG_ID_";
	
	//发文类型节点后缀
	public static final String SEND_NODE_SUFFIX = "-send";
	//收文类型节点后缀
	public static final String REC_NODE_SUFFIX = "-rec";
	
	//组织类型 单位
	public static final int ACCOUNT = 1;
	//部门
	public static final int ORG = 2;
	//职务
	public static final int JOB = 3;
	//级别
	public static final int LEVEL = 4;
	//人员
	public static final int PERSON = 5;
	
	//时间类型 年
	public static final int YEAR = 1;
	//季度
	public static final int SEASON = 2;
	//月
	public static final int MONTH = 3;
	//日
	public static final int DAY = 4;
	
	public static final String SIGN = "=";
	public static final String LABEL_SIGN = "、";
}
