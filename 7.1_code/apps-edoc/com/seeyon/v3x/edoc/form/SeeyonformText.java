package com.seeyon.v3x.edoc.form;

import java.util.List;

import org.dom4j.Element;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.util.EdocFormHelper;

public class SeeyonformText extends InputObject{

    private boolean isNull = true;
    private boolean required;
    private String fieldtype;
    private int length;
    private String digit;
    
    public boolean getIsNull() {
        return isNull;
    }


    public void setIsNull(boolean isNull) {
        this.isNull = isNull;
    }


    public boolean isRequired() {
        return required;
    }


    public void setRequired(boolean required) {
        this.required = required;
    }


    public String getFieldtype() {
        return fieldtype;
    }


    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }


    public int getLength() {
        return length;
    }


    public void setLength(int length) {
        this.length = length;
    }


    public String getDigit() {
        return digit;
    }


    public void setDigit(String digit) {
        this.digit = digit;
    }


    public SeeyonformText(Element fieldInput) throws BusinessException{
        super(fieldInput);
        
        String is_null = fieldInput.attributeValue("is_null");
        if(Strings.isNotBlank(is_null)){
            this.setIsNull(Boolean.valueOf(is_null));
        }
        String required = fieldInput.attributeValue("required");
        if(Strings.isNotBlank(required)){
            this.setRequired(Boolean.valueOf(required));
        }
        String fieldtype = fieldInput.attributeValue("fieldtype");
        if(Strings.isNotBlank(fieldtype)){
            this.setFieldtype(fieldtype);
        }
        String length = fieldInput.attributeValue("length");
        if(Strings.isNotBlank(length)){
            this.setLength(Integer.parseInt(length));
        }
        String digit = fieldInput.attributeValue("digit");
        if(Strings.isNotBlank(digit)){
            this.setDigit(digit);
        }
    }
    
    
    @Override
    public void change(Element span) {
        String styleStr=span.attributeValue("style");
        String readOnlyStr="";
        String titleStr="";
        String onclickStr="";
        if("date".equals(this.fieldtype)){
            styleStr+=";cursor:hand;";
            readOnlyStr=" readOnly";
//            titleStr=jsStr_ClickInput;  
            onclickStr="whenstart(jsContextPath,this,575,140)";
        }
        if(this.required /*TODO && replaceApos(aArea.innerText) == "" */){
          //先保存原背景颜色
//            aArea.setAttribute("bgColor", aArea.style.backgroundColor);
            styleStr+=";background-Color:#FCDD8B";
        }
        String objValue=span.getText();
        objValue = EdocFormHelper.toJsStr(objValue);
        
        Element parent = span.getParent();
//        parent.remove(span);
        
//        Element text = parent.addElement("input");
//        text.addAttribute("access",this.getAccess());
//        text.addAttribute("allowprint",this.getAllowprint());
//        text.addAttribute("allowtransmit",this.getAllowtransmit());
//        text.addAttribute("required",String.valueOf(this.isRequired()));
//        text.addAttribute("class","xdTextBox");
//        text.addAttribute("canSubmit","true");
//        text.addAttribute("value",objValue);
//        text.addAttribute("name",this.fieldName);
//        text.addAttribute("id",this.fieldName);
//        text.addAttribute("style",styleStr);
//        text.addAttribute("title",titleStr);
//        text.addAttribute("onClick",onclickStr);
//        text.addAttribute("onpropertychange","onkeyupColor(this)");
//        if(Strings.isNotBlank(readOnlyStr)){
//            text.addAttribute("readOnly",readOnlyStr);
//        }
        
    }

}
