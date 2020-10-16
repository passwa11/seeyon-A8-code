package com.seeyon.apps.testBPM.constant;

/**
 * @author Fangaowei
 * <pre>
 * 表单编号枚举
 * </pre>
 * @date 2018年8月10日 下午2:25:07
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public enum FormTemplateCode{
    
    /** 出差单模板编号  */
    chuchai("chuchai"),
    /** 请假单模板编号 */
    qingjia("qingjia"),
    /** 出差计划 */
    chuchaijihua("chuchaijihua"),
    /** 转岗申请表 */
    zhuangang("zhuangang");
    
    private String code;
    
    FormTemplateCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    
}
