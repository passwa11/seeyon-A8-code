package com.seeyon.v3x.edoc.util;

import com.seeyon.ctp.common.i18n.ResourceBundleUtil;

public class Constants {

	public final static String AJAX_JSON = "json";
	//按大流水
	public static final int MODE_SERIAL = 0;
	
	//按字号
	public static final int MODE_WORD = 1;
	
	public static Long groupDomainId=0L; //虚拟的集团ID(只用于公文元素的初始化赋值)
	
	public static int THE_EDOC=0;  //正文
	public static int THE_TABLE=1; //文单
	
	public static int EDOC_USEED=1;  //启用
	public static int EDOC_USELESS=0;//未启用
	public static int EDOC_All=-1;//所有
	
	public static int EDOC_MARK_EMPTY = 0;    //未使用
	public static int EDOC_MARK_USED = 1;     //暂时使用
	public static int EDOC_MARK_OCCUPIED = 2; //已占用
	
	public static short EDOC_MARK_DEFINITION_DRAFT = 0; // 公文文号定义状态：草稿
	public static short EDOC_MARK_DEFINITION_PUBLISHED = 1;//公文文号定义状态：已使用
	public static short EDOC_MARK_DEFINITION_DELETED = 2;//公文文号定义状态：已删除
	
	public static short EDOC_MARK_CATEGORY_BIGSTREAM = 1;   //公文文号定义: 大流水
	public static short EDOC_MARK_CATEGORY_SMALLSTREAM = 0; //公文文号定义: 小流水
	
	public static int EDOC_FORM_TYPE_SEND = 0; //公文单类型 : 发文
	public static int EDOC_FORM_TYPE_REC = 1; //公文单类型 : 收文
	public static int EDOC_FORM_TYPE_SIGN = 2; //公文单类型 : 签报
	
	public static int EDOC_INNERMARK_UNIFICATION = 0;        //统一内部文号
	public static int EDOC_INNERMARK_SEND = 1;          //发文文号
	public static int EDOC_INNERMARK_RECEIVED = 2;      //收文文号
	public static int EDOC_INNERMARK_SIGN_REPORT = 3;   //签报文号
	
	public final static int STATUS_INNERMARK_UNKNOWN = -1;//内部文号设置状态：未知
	public final static int STATUS_INNERMARK_INITIAL = 0; //内部文号设置状态：未设置内部文号
	public final static int STATUS_INNERMARK_PUBLIC = 1;//统一内部文号
	public final static int STATUS_INNERMARK_PRIVATE = 2;//独立内部文号
	
	public static int EDOC_SUPERVISE_REMINDMODE_ONLINE = 0;   //在线提醒
	public static int EDOC_SUPERVISE_REMINDMODE_MESSAGE = 1;  //手机短信
	public static int EDOC_SUPERVISE_REMINDMODE_EMAIL = 2;    //电子邮件
	
	public static int EDOC_SUPERVISE_PROGRESSING = 0;  //督办中
	public static int EDOC_SUPERVISE_TERMINAL = 1;   //督办结束
	public static int EDOC_SUPERVISE_ZCDB = 2;  //暂存待办
	
	public static int EDOC_SUPERVISE_HASTEN = 0;     //催办消息
	public static int EDOC_SUPERVISE_REPLAY = 1;	 //回复消息
	
	public static int EDOC_SUPERVISOR_DELETE = 0;    //删除
	public static int EDOC_SUPERVISOR_UNDELETE = 1;  //未删除
	
	public static int EDOC_SUPERVISOR_ID_UNCHANGED = 0;  //督办人ID -- 没有改变（没删除，也不是新添加的）
	public static int EDOC_SUPERVISOR_ID_ADD = 1;  //督办人ID -- 新添加的督办人
	public static int EDOC_SUPERVISOR_ID_DELETE = 2;  //督办人ID -- 从原来列表中删除的督办人
	
	public static int EDOC_SUPERVISOR_PERMISSION_READONLY = 0;   //督办权限: 只读 
	public static int EDOC_SUPERVISOR_PERMISSION_CHANGE = 1;     //督办权限: 可修改
	
	public static int EDOC_MARK_EDIT_NONE = 0; //未改变公文文号
	public static int EDOC_MARK_EDIT_SELECT_NEW = 1;//下拉选择的文号
	public static int EDOC_MARK_EDIT_SELECT_OLD = 2; // 选择的断号
	public static int EDOC_MARK_EDIT_INPUT = 3; // 手工输入的文号
	public static int EDOC_MARK_EDIT_SELECT_RESERVE = 4; //选择的预留文号
	
	public static int EDOC_DOCTEMPLATE_DISABLED = 0;  //公文模板【公文单】--禁用
	public static int EDOC_DOCTEMPLATE_ENABLED = 1;   //公文模板【公文单】--启用
	
	public static int EDOC_DOCTEMPLATE_WORD = 0;      //公文模板--正文
	public static int EDOC_DOCTEMPLATE_SCRIPT = 1;    //公文模板--稿纸
	
	public static int EDOC_STAT_PERIOD_TYPE_YEAR = 0;//统计时间段：全年
	public static int EDOC_STAT_PERIOD_TYPE_SEASON = 1;//统计时间段：季度
	public static int EDOC_STAT_PERIOD_TYPE_MONTH = 2;//统计时间段：月份
	
	public static int EDOC_STAT_GROUPBY_DEPT = 0;//统计分组：按部门分组
	public static int EDOC_STAT_GROUPBY_DOCTYPE = 1;//统计分组：按公文种类分组
	
	public static int EDOC_ATTITUDE_NULL = -1; //处理意见为空
	
	public static int EDOC_FILE_TYPE_EDOCFORM = 0 ; //公文预置文件的分类--公文单文件
	public static int EDOC_FILE_TYPE_TEMPLATE = 1 ; //公文预置文件的分类--公文套红模板文件
	public static int ORGNIZATION_FILE_TYPE = 2; //组织模型导入模板分类--人员与岗位
	
	public static String EDOC_DOCTEMPLATE_TEXTTYPE_OFFICEWORD = "officeword"; //公文套红模板类型：word
	public static String EDOC_DOCTEMPLATE_TEXTTYPE_WPSWORD = "wpsword"; //公文套红模板类型：wps
	public static String EDOC_DOCTEMPLATE_TEXTTYPE_EXTEND = "unknown"; //公文套红模板类型：未知
	
	public static String EDOC_EDOCFORM_XSL_SEND_START = "&&&&&& sendstart &&&&&&";  //预置公文单的样式文件分割标识符 ： 发文-开始
	public static String EDOC_EDOCFORM_XSL_SEND_END = "&&&&&& sendend &&&&&&";  //预置公文单的样式文件分割标识符 ： 发文-结束   
	public static String EDOC_EDOCFORM_XSL_REC_START = "&&&&&& recstart &&&&&&";  //预置公文单的样式文件分割标识符 ： 收文-开始
	public static String EDOC_EDOCFORM_XSL_REC_END = "&&&&&& recend &&&&&&";  //预置公文单的样式文件分割标识符 ： 收文-结束
	public static String EDOC_EDOCFORM_XSL_REC_YUEWEN_START = "&&&&&& recyuewenstart &&&&&&";  //预置公文单的样式文件分割标识符 ： 收文(阅文)-开始
	public static String EDOC_EDOCFORM_XSL_REC_YUEWEN_END = "&&&&&& reyuewencend &&&&&&";  //预置公文单的样式文件分割标识符 ： 收文(阅文)-结束
	public static String EDOC_EDOCFORM_XSL_SIGN_START = "&&&&&& signstart &&&&&&";  //预置公文单的样式文件分割标识符 ： 签报-开始
	public static String EDOC_EDOCFORM_XSL_SIGN_END = "&&&&&& signend &&&&&&";  //预置公文单的样式文件分割标识符 ： 签报-结束
	public static String Edoc_PAGE_SHOWPIGEONHOLE_SYMBOL = "...";  //JSP页面显示归档路径前缀
	public static String EDOC_EDOCFORM_XSL_REC_OPINION_START = "&&&&&& recopinionstart &&&&&&";//预置公文单的样式文件分割标识符 ： 新收文-开始
	public static String EDOC_EDOCFORM_XSL_REC_OPINION_END = "&&&&&& recopinionend &&&&&&";//预置公文单的样式文件分割标识符 ： 新收文-开始
	
    public static enum SendType {
        normal,
        resend,
        forward
    }
    
    public static enum ConfigCategory {
    	action_to_col_definition,
    	node_permission_policy
    }
    
    private static final String resource_baseName = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
    
   //private static ResourceBundle resourceBundle = ResourceBundleU.getBundle(resource_baseName);
    
    public static String getString(String key, Object... parameters){
    	return ResourceBundleUtil.getString(resource_baseName, key, parameters);
    }
    
    public static String getString4CurrentUser(String key, Object... parameters){
    	return ResourceBundleUtil.getString(resource_baseName, key, parameters);
    }
    
    public static enum SubEdocCategory{
    	normal,
    	cancel
    }
}
