package com.seeyon.apps.formrelation.db;

/**
 * @author Fangaowei
 * <pre>
 * 
 * </pre>
 * @date 2018年11月13日 下午9:34:18
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class FormCondition {
    /** 前端字段的显示名称 */ 
    private String display;
    private Operation opera;
    // 目前只有between需要用到两个
    private Object param1;
    private Object param2;
    
    public FormCondition() {
        
    }
    
    public FormCondition(String display, Operation opera, Object... objects) {
        this.display = display;
        this.opera = opera;
        this.param1 = objects[0];
        if(objects.length > 1) {
            this.param2 = objects[1];
        } else {
            this.param2 = null;
        }
    }
    
    public String getDisplay() {
        return display;
    }
    
    public void setDisplay(String display) {
        this.display = display;
    }
    
    public Operation getOpera() {
        return opera;
    }
    
    public void setOpera(Operation opera) {
        this.opera = opera;
    }
    
    public Object getParam1() {
        return param1;
    }
    
    public void setParam1(Object param1) {
        this.param1 = param1;
    }
    
    public Object getParam2() {
        return param2;
    }
    
    public void setParam2(Object param2) {
        this.param2 = param2;
    }

}
