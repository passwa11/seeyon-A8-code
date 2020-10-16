package com.seeyon.v3x.exchange.util;

public class Constants {

	/** 公文交换状态：待发送 */
	public final static int C_iStatus_Tosend = 0;
	
	/** 公文交换状态：已发送 */
	public final static int C_iStatus_Sent = 1;
	
	/** 公文交换状态（发文记录）：已回退 */
	public final static int C_iStatus_Send_StepBacked = 2;
	
	/** 公文交换状态：已发送-被删除 */
	public final static int C_iStatus_Send_Delete = 5;
	
	/** 公文交换状态：待发送-被删除 */
	public final static int C_iStatus_ToSend_Delete = 6;
	
	/** 公文交换状态：待签收 */
	public final static int C_iStatus_Torecieve = 0;
	
	/** 公文交换状态：被退回到退件箱 */
	public final static int C_iStatus_Retreat = 4;

	/** 公文交换状态：已签收（待登记） */
	public final static int C_iStatus_Recieved = 1;
	
	/** 公文交换状态：已登记 */
	public final static int C_iStatus_Registered = 2;
	
	/** 公文交换状态（收文回执）：已回退 */
	public final static int C_iStatus_Receive_StepBacked = 3;
	
	/** 公文交换单位类型：部门交换 */
	public final static int C_iExchangeType_Dept = 0;
	
	/** 公文交换类型：内部单位交换 */
	public final static int C_iExchangeType_Org = 1;
	
	/** 公文交换类型：外部单位交换 */
	public final static int C_iExchangeType_ExternalOrg = 2;
	
	/** 交换帐号类型：手工添加的外部单位（无法交换） */
	public final static int C_iAccountType_Default = 0;
	
	/** 交换帐号类型：单位帐号 */
	public final static int C_iAccountType_Org = 1;
	
	/** 交换帐号类型：部门帐号 */
	public final static int C_iAccountType_Dept = 2;
	
	/** 交换帐号类型：人员帐号 */
	public final static int C_iAccountType_Person = 3;
	
	/** 与交换服务器连接状态：未连接 */
	public final static int C_iStatus_Disconnected = 0;
	
	/** 与交换服务器连接状态：已连接 */
	public final static int C_iStatus_Connected = 1;	
	
	/** 交换类型: 主送 */
	public final static int C_iStatus_Send = 0;
	
	/** 交换类型 抄送*/
	public final static int C_iStatus_Copy = 1;
	
	/** 交换类型 抄报*/
	public final static int C_iStatus_Report = 2;
	
	public final static int EDOC_EXCHANGE_UNION_NORMAL = 0; /** 联合发文标识 ： 保留的正文 */
	public final static int EDOC_EXCHANGE_UNION_FIRST = 1; /** 联合发文标识 : 一单位的正文 */
	public final static int EDOC_EXCHANGE_UNION_SECOND = 2; /** 联合发文标识 : 二单位的正文 */
	public final static int EDOC_EXCHANGE_UNION_PDF_FIRST = 3; /** 第一套PDF正文 */
	public final static int EDOC_EXCHANGE_UNION_PDF_SECOND = 4; /** 如果联合发文。第二套PDF正文 */
	
	/** 唐桂林添加公文收文类型常量 20110922 */
	public final static int EDOC_EXCHANGE_UNION_TYPE_A8_INNER= 1;//来文机关为同套A8中的内部单位
	public final static int EDOC_EXCHANGE_UNION_TYPE_A8_OUTER = 2;//来文机关为同套A8中的外部单位
	public final static int EDOC_EXCHANGE_UNION_TYPE_A8_TO_A8_OUTER = 3;//来文机关为交换中心多套A8的外部单位
	public final static int EDOC_EXCHANGE_UNION_TYPE_ISOMERISM_TO_A8_OUTER = 4;//来文机关为交换心中外部的异构系统单位
	public final static int EDOC_EXCHANGE_UNION_TYPE_EDOC_ISOMERISM_TO_A8_OUTER = 5;//来文机关为交换中心异构的公文传输系统单位
	public final static int EDOC_EXCHANGE_UNION_TYPE_UKNOW = -1;//来文机关为交换中心异构的公文传输系统单位	

}
