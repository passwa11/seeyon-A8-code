package com.seeyon.v3x.edoc.form;

import org.dom4j.Element;

public class SeeyonformTextArea extends InputObject{

    private boolean required;
    
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public SeeyonformTextArea(Element fieldInput ){
        super(fieldInput);
        this.setRequired(Boolean.valueOf(fieldInput.attributeValue("required")));
    }
    
    
    @Override
    public void change(Element span) {
        String cssStr = span.attributeValue("style");
        if((cssStr.indexOf("TEXT-ALIGN: center;")!=-1 && cssStr.indexOf("WHITE-SPACE: nowrap;")!=-1 )
                || (cssStr.indexOf("text-align: center;")!=-1 && cssStr.indexOf("white-space: nowrap;")!=-1)
                || (cssStr.indexOf("TEXT-ALIGN: center")!=-1 && cssStr.indexOf("WHITE-SPACE: nowrap;")!=-1)){
            cssStr = cssStr.replace("/WHITE-SPACE: nowrap;/","word-break: break-all;");
        }
        //这里少了  replaceApos(aArea.innerText) == ""  判断
        /*显示时对单引号进行转义
        function replaceApos(aStr){
            return aStr.replace(/'/g, "&#039;");
        }*/
        if(this.isRequired()){
            cssStr+=";background-Color:#FCDD8B";
        }
        //增加access属性，处理公文时如果textarea是readOnly的，再判断access属性是否为edit
        
        String text = span.getText();
        Element parent = span.getParent();
        parent.remove(span);
        
        Element areatext = parent.addElement("textarea");
        areatext.addAttribute("access",this.getAccess());
        areatext.addAttribute("allowprint",this.getAllowprint());
        areatext.addAttribute("allowtransmit",this.getAllowtransmit());
        areatext.addAttribute("required",String.valueOf(this.isRequired()));
        areatext.addAttribute("name",this.getFieldName());
        areatext.addAttribute("id",this.getFieldName());
        areatext.addAttribute("wrap","physical");
        areatext.addAttribute("style",cssStr);
        areatext.addAttribute("canSubmit","true");
        areatext.addAttribute("onpropertychange","onkeyupColor(this)");
        
        areatext.addText(text);
        
    }
    
}
