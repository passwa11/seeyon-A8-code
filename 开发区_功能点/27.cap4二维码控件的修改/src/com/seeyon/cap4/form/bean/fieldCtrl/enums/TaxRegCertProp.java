package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 税务登记证属性
 */
public enum TaxRegCertProp {
    NAME("name","com.cap.ctrl.ocr.idcardfrontprop.name",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COMPANY("company","com.cap.ctrl.ocr.businesslicence.company",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COMPANY_TYPE("company_type","com.cap.ctrl.ocr.businesslicence.companytype",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ADDRESS("address","com.cap.ctrl.ocr.businesslicence.address",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    OWNER("owner","com.cap.ctrl.ocr.businesslicence.owner",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //REG_CAPITAL("reg_capital","注册资本",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //PAIDIN_CAPITAL("paidin_capital","实收资本",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //START_TIME("start_time","成立日期",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SCOPE("scope","com.cap.ctrl.ocr.businesslicence.scope",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //REG_CODE("reg_code","注册号/登记号",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SERIAL("serial","com.cap.ctrl.ocr.businesslicence.serial",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //ORG_NUM("org_num","组织机构代码证号",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //TAX_NUM("tax_num","税务登记证号",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //PERIOD_START("period_start","营业期限自",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //PERIOD_END("period_end","营业期限至",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //REG_INSTITUTION("reg_institution","登记机关",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REG_TIME("reg_time","com.cap.ctrl.ocr.businesslicence.regtime",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    STATUS("status","com.cap.ctrl.ocr.businesslicence.status",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    DUTY("duty","com.cap.ctrl.ocr.businesslicence.duty",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TAXPAY_NUM("taxpay_num","com.cap.ctrl.ocr.businesslicence.taxpaynum",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    CNTAXPAY_NUM("cntaxpay_num","com.cap.ctrl.ocr.businesslicence.cntaxpaynum",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    LOTAXPAY_NUM("lotaxpay_num","com.cap.ctrl.ocr.businesslicence.lotaxpaynum",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REF_NUM("ref_num","com.cap.ctrl.ocr.businesslicence.refnum",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    //VALIDITY("validity","有效期",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ISSUE_ORG("issue_org","com.cap.ctrl.ocr.orgcodecertprop.issueorg",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA});
    //CREDIT_CODE("credit_code","统一社会信用编码",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA});

    private String key;
    private String name;
    private FormFieldComEnum[] supportFieldType;

    TaxRegCertProp(String key, String name, FormFieldComEnum[] supportFieldType) {
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
        for(TaxRegCertProp taxRegCertProp : TaxRegCertProp.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",taxRegCertProp.getKey());
            tempMap.put("name",taxRegCertProp.getName());
            values.add(tempMap);
        }
        return values;
    }

    public static TaxRegCertProp getByKey(String key) {
        for (TaxRegCertProp type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
