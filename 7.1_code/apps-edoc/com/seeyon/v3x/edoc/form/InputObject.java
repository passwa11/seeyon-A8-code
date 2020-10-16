package com.seeyon.v3x.edoc.form;

import org.dom4j.Element;

/**
 * 所有公文元素控件的父类，公文元素都必须继承该类
 * @author changyi
 *
 */
public abstract class InputObject {
    protected String fieldName;
    protected String access;
    protected String allowprint;
    protected String allowtransmit;
    
    public InputObject(Element fieldInput){
        this.setFieldName(fieldInput.attributeValue("name"));
        this.setAccess(fieldInput.attributeValue("access"));
        this.setAllowprint(fieldInput.attributeValue("allowprint"));
        this.setAllowtransmit(fieldInput.attributeValue("allowtransmit"));
    }
    
    
    public abstract void change(Element span);

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getAllowprint() {
        return allowprint;
    }

    public void setAllowprint(String allowprint) {
        this.allowprint = allowprint;
    }

    public String getAllowtransmit() {
        return allowtransmit;
    }

    public void setAllowtransmit(String allowtransmit) {
        this.allowtransmit = allowtransmit;
    }

    
}
