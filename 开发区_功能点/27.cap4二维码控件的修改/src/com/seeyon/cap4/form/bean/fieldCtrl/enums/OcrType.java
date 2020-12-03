package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 证件类型
 */
public enum OcrType {

    IDENTIFICATION_CARD_FRONT("identification_card_front","com.cap.ctrl.ocr.identificationcardfront",4),
    IDENTIFICATION_CARD_BACK("identification_card_back","com.cap.ctrl.ocr.identificationcardback",4),
    VISITING_CARD("visiting_card","com.cap.ctrl.ocr.visitingcard",5),
    BANK_CARD("bank_card","com.cap.ctrl.ocr.bankcard",6),
    BUSINESS_LICENCE("business_licence","com.cap.ctrl.ocr.businesslicence",1),
    ORGANIZATION_CODE_CERTIFICATE("organization_code_certificate","com.cap.ctrl.ocr.organizationcodecertificate",2),
    TAX_REGISTRATION_CERTIFICATE("tax_registration_certificate","com.cap.ctrl.ocr.taxregistrationcertificate",3);

    private String key;
    private String name;
    private int sortNum;

    OcrType(String key, String name,int sortNum) {
        this.key = key;
        this.name = name;
        this.sortNum = sortNum;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return ResourceUtil.getString(name);
    }

    public int getSortNum() {
        return sortNum;
    }

    public void setSortNum(int sortNum) {
        this.sortNum = sortNum;
    }

    public static OcrType getByKey(String key) {
        for (OcrType type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 转map
     * @return
     */
    public static List<Map<String,Object>> list(){
        List<Map<String,Object>> values = new ArrayList<Map<String, Object>>();
        for(OcrType ocrType : OcrType.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",ocrType.getKey());
            tempMap.put("name",ocrType.getName());
            values.add(tempMap);
        }
        return values;
    }
}
