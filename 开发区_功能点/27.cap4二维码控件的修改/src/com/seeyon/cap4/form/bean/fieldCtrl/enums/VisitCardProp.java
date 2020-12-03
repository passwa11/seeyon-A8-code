package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum VisitCardProp {
    NAME("name","com.cap.ctrl.ocr.visit.name",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COUNTRY("country","com.cap.ctrl.ocr.visit.country",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    CITY("city","com.cap.ctrl.ocr.visit.city",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    STREET("street","com.cap.ctrl.ocr.visit.street",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    LABEL("label","com.cap.ctrl.ocr.visit.label",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TEL("tel","com.cap.ctrl.ocr.visit.tel",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    EMAIL("email","com.cap.ctrl.ocr.visit.email",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TITLE("title","com.cap.ctrl.ocr.visit.title",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ROLE("role","com.cap.ctrl.ocr.visit.role",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ORG("org","com.cap.ctrl.ocr.visit.org",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COMMENT("comment","com.cap.ctrl.ocr.visit.comment",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    URL("url","com.cap.ctrl.ocr.visit.url",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SNS("sns","com.cap.ctrl.ocr.visit.sns",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    IM("im","com.cap.ctrl.ocr.visit.im",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TELEPHONE("telephone", "com.cap.ctrl.ocr.visit.telephone", new FormFieldComEnum[]{ FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA }),
    WORK_TEL("work_tel", "com.cap.ctrl.ocr.visit.workTel", new FormFieldComEnum[]{ FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA }),
    FAX("fax", "com.cap.ctrl.ocr.visit.fax", new FormFieldComEnum[]{ FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA });


    private String key;
    private String name;
    private FormFieldComEnum[] supportFieldType;

    VisitCardProp(String key, String name,FormFieldComEnum[] supportFieldType) {
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
        for(VisitCardProp visitCardProp : VisitCardProp.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",visitCardProp.getKey());
            tempMap.put("name",visitCardProp.getName());
            values.add(tempMap);
        }
        return values;
    }

    public static VisitCardProp getByKey(String key) {
        for (VisitCardProp type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
