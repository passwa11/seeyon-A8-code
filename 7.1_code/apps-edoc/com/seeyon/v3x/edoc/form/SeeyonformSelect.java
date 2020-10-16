package com.seeyon.v3x.edoc.form;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.Strings;

public class SeeyonformSelect extends InputObject{

    private boolean required;
    private List<DisplayValue> valueList;
    
    public List<DisplayValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<DisplayValue> valueList) {
        this.valueList = valueList;
    }

    protected boolean isRequired() {
        return required;
    }

    protected void setRequired(boolean required) {
        this.required = required;
    }
    
    public SeeyonformSelect(Element fieldInput) throws BusinessException{
        super(fieldInput);
        this.setRequired(Boolean.valueOf(fieldInput.attributeValue("required")));
        
        List<Element> optionList = fieldInput.selectNodes("Input");
        if(optionList == null || optionList.size() ==0){
            throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.no.node","Input"));//XML信息不是SeeyonFormat的格式!<br>找不到 Input 节点
        }
        List<DisplayValue> valueList = new ArrayList<DisplayValue>();
        for(Element element : optionList){
            String display = element.attributeValue("display");
            String value = element.attributeValue("value");
            if(display == null){
                throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.input.no.property","Input","display"));//XML信息不是SeeyonFormat的格式!<br> Input节点没有display属性
            }
            if(value == null){
                throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.input.no.property","Input","value"));//XML信息不是SeeyonFormat的格式!<br> Input节点没有value属性
            }
            DisplayValue option = new DisplayValue();
            option.setLabel(display);
            option.setValue(value);
            valueList.add(option);
        }
        this.setValueList(valueList);        
    }

    @Override
    public void change(Element span) {
        String fOption = "";
        String innerText = span.getText();
        List<DisplayValue> valueList = this.getValueList();
        
        /*  TODO
        var fromSend=document.getElementById("fromSend");
        if(typeof(currentPage)!="undefined" && currentPage=="newEdoc"
            &&(this.fieldName=="my:doc_mark" || this.fieldName=="my:doc_mark2" || this.fieldName=="my:serial_no")
            && this.valueList.length==2 && fromSend && fromSend.value!="true"){
        */
        
        
        String styleStr = span.attributeValue("style");
        if(this.isRequired()){
            
            //TODO 先保存原背景颜色  (为何要保存背景颜色)
            //aArea.setAttribute("bgColor", aArea.style.backgroundColor);
            styleStr+=";background-Color:#FCDD8B";
        }
        
        Element parent = span.getParent();
        parent.remove(span);
        
        Element select = parent.addElement("select");
        select.addAttribute("access",this.getAccess());
        select.addAttribute("allowprint",this.getAllowprint());
        select.addAttribute("allowtransmit",this.getAllowtransmit());
        select.addAttribute("required",String.valueOf(this.isRequired()));
        select.addAttribute("name",this.getFieldName());
        select.addAttribute("id",this.getFieldName());
        select.addAttribute("class","xdComboBox xdBehavior_Select");
        select.addAttribute("style",styleStr);
        select.addAttribute("onpropertychange","onkeyupColor(this)");
        select.setText(fOption);
        
        for(int i=0;i<valueList.size();i++){
            DisplayValue option = valueList.get(i);
            Element op = select.addElement("option");
            op.addAttribute("value", option.getValue());
            op.setText(option.getLabel());
            if(innerText.equals(option.getValue())){
                op.addAttribute("selected","selected");
            }
        }
        
        
    }
}






