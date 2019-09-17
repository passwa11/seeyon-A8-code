package com.seeyon.v3x.edoc.constants;


public class EdocOpinionDisplayEnum {
	
	/*public static String defaultOpinionType = "2,1-2,0,0";
	
	*//**
	 * 文单意见设置，EdocOpinion的opinionType值
	 * @author tanggl
	 *
	 *//*
	public enum OpinionSetIndexEnum {
		opinion_set_index_display_type,//第一位表示 意见保留设置
		opinion_set_index_display_deal_name,//第二位表示 意见留痕配置
		opinion_set_index_display_date_format,//第三位表示 处理时间显示格式
		opinion_set_index_display_option_type,//第四位表示 处理时间显示格式
		opinion_set_index_display_deal_name_type,//第5位，名称显示方式配置
		opinion_set_index_display_inscribed_show_type//第6位，落款是否换行显示配置
	}
	
	*//**
	 * 意见保留设置
	 * @author tanggl
	 *
	 *//*
	public enum OpinionSetTypeEnum {
		opinion_set_display,
		opinion_set_display_type_last,//全流程保留最后一次意见
		opinion_set_display_type_all,//全流程保留所有意见
		opinion_set_display_type_byway_other_last,//被退回者选择覆盖方式，其他处理人保留最后意见
		opinion_set_display_type_byway_other_all//被退回者选择覆盖方式，其他处理人保留所有意见 
	}	*/
	
	/**
	 * 处理时间显示格式
	 * @author tanggl
	 *
	 *//*
	public enum OpinionSetDealNameEnum {
		opinion_set_display_deal_name_dept,//显示处理人所在部门 （如：办公室 张三）
		opinion_set_display_deal_name_unit,//全流程保留所有意见显示处理人所在机关 （如：机关 办公室 张三）
		opinion_set_display_deal_name_person//全流程保留最后一次意见显示处理人名字 （如：张三）
	}
	
	*//**
	 * 处理时间显示格式
	 * @author tanggl
	 *
	 *//*
	public enum OpinionSetDateFormatEnum {
		opinion_set_display_date_format,//
		opinion_set_display_date_format_only_date,//显示日期时间
		opinion_set_display_date_format_and_time//显示日期
	}
	*/
	
	/**
	 * 公文配置页面，意见保留设置枚举
	 * @author xuqiangwei
	 *
	 */
	public enum OpinionDisplaySetEnum{
	    
	    /**全流程保留所有意见**/
	    DISPLAY_ALL("2"),
	    
	    /**全流程保留最后一次意见 **/
	    DISPLAY_LAST("1"),  
	    
	    /**被退回者选择覆盖方式，其他处理人保留所有意见**/
	    DISPLAY_BACK_OFF_SELECT("4"); 
	    
	    
	    private String value = null;
	    OpinionDisplaySetEnum(String value){
	        this.value = value;
	    }
	    public String getValue() {
            return value;
        }
	}
	
	/**
	 * 系统落款枚举
	 * @author xuqiangwei
	 *
	 */
	public enum OpinionInscriberSetEnum{
	    
	    /** 处理人所在单位 **/
	    UNIT("0"),
	    
	    /** 处理人所属部门  **/
	    DEPART("1"),
	    
	    /** 处理人姓名  **/
	    NAME("2"),
	    
	    /** 文单签批后不显示系统落款 **/
	    INSCRIBER("3"),
	    
	    /** 落款是否换行显示 **/
	    INSCRIBER_NEW_LINE("4");
	    
	    private String value = null;
	    OpinionInscriberSetEnum(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }
	}
	
	/**
	 * 处理时间显示格式
	 * @author xuqiangwei
	 *
	 */
	public enum OpinionDateFormatSetEnum{
	    
	    /**显示日期时间 **/
	    DATETIME("0"),
	    
	    /**显示日期**/
        DATE("1"),
        
        /**无**/
        NOTHION("2");
	    
	    private String value = null;
	    OpinionDateFormatSetEnum(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
	
	/**
	 * 处理时间显示样式
	 * @author dy
	 *
	 */
	public enum OpinionDateModelSetEnum{
	    
	    /**显示全称**/
	    FULL("0"),
	   
	    /**显示简称**/
        SIMPLE("1");
	    
        private String value = null;
	    OpinionDateModelSetEnum(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

	/**
	 * 文单设置，处理人姓名显示方式
	 * @author xuqiangwei
	 *
	 */
	public enum OpinionShowNameTypeEnum{
	    
	    /** 普通方式显示 **/
	    COMMON("0"),
	    
	    /** 签名方式显示 **/
	    SIGN("1");
	    
	    private String value = null;
	    
	    OpinionShowNameTypeEnum(String value){
	        this.value = value;
	    }
	    
	    public String getValue() {
            return value;
        }
	}
}
