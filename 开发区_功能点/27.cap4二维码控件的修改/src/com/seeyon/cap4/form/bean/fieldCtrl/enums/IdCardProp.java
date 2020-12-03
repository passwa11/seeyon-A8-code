package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 身份证正面
 */
public enum IdCardProp {

    NAME("name","com.cap.ctrl.ocr.idcardfrontprop.name",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ID("id","com.cap.ctrl.ocr.idcardfrontprop.id",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    BIRTHDAY("birthday","com.cap.ctrl.ocr.idcardfrontprop.birthday",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SEX("sex","com.cap.ctrl.ocr.idcardfrontprop.sex",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    PEOPLE("people","com.cap.ctrl.ocr.idcardfrontprop.people",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ADDRESS("address","com.cap.ctrl.ocr.idcardfrontprop.address",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TYPE("type","com.cap.ctrl.ocr.idcard.type",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA});

    private String key;
    private String name;
    private FormFieldComEnum[] supportFieldType;

    IdCardProp(String key, String name,FormFieldComEnum[] supportFieldType) {
        this.key = key;
        this.name = name;
        this.supportFieldType = supportFieldType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return ResourceUtil.getString(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public FormFieldComEnum[] getSupportFieldType() {
        return supportFieldType;
    }

    public void setSupportFieldType(FormFieldComEnum[] supportFieldType) {
        this.supportFieldType = supportFieldType;
    }

    public static List<Map<String,Object>> list(){
        List<Map<String,Object>> values = new ArrayList<Map<String, Object>>();
        for(IdCardProp idCardProp : IdCardProp.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",idCardProp.getKey());
            tempMap.put("name",idCardProp.getName());
            values.add(tempMap);
        }
        return values;
    }

    public static IdCardProp getByKey(String key) {
        for (IdCardProp type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
