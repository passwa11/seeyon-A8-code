package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 身份证反面
 */
public enum IdCardBackProp {

    TYPE("type","com.cap.ctrl.ocr.idcard.type",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ISSUE_AUTHORITY("issue_authority","com.cap.ctrl.ocr.idcard.issueauthority",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    VALIDITY("validity","com.cap.ctrl.ocr.idcard.validity",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA});

    private String key;
    private String name;
    private FormFieldComEnum[] supportFieldType;

    IdCardBackProp(String key, String name, FormFieldComEnum[] supportFieldType) {
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
        for(IdCardBackProp idCardProp : IdCardBackProp.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",idCardProp.getKey());
            tempMap.put("name",idCardProp.getName());
            values.add(tempMap);
        }
        return values;
    }

    public static IdCardBackProp getByKey(String key) {
        for (IdCardBackProp type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
