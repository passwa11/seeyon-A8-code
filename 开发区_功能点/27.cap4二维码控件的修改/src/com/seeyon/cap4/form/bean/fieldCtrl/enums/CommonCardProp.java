package com.seeyon.cap4.form.bean.fieldCtrl.enums;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 营业执照；组织机构代码证；税务登记证可选属性
 */
public enum CommonCardProp{
    NAME("name","com.cap.ctrl.ocr.businesslicence.name",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COMPANY("company","com.cap.ctrl.ocr.businesslicence.company",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    COMPANY_TYPE("company_type","com.cap.ctrl.ocr.businesslicence.companytype",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ADDRESS("address","com.cap.ctrl.ocr.businesslicence.address",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    OWNER("owner","com.cap.ctrl.ocr.businesslicence.owner",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REG_CAPITAL("reg_capital","com.cap.ctrl.ocr.businesslicence.regcapital",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    PAIDIN_CAPITAL("paidin_capital","com.cap.ctrl.ocr.businesslicence.paidincapital",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    START_TIME("start_time","com.cap.ctrl.ocr.businesslicence.starttime",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SCOPE("scope","com.cap.ctrl.ocr.businesslicence.scope",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REG_CODE("reg_code","com.cap.ctrl.ocr.businesslicence.regcode",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    SERIAL("serial","com.cap.ctrl.ocr.businesslicence.serial",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ORG_NUM("org_num","com.cap.ctrl.ocr.businesslicence.orgnum",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TAX_NUM("tax_num","税务登记证号",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    PERIOD_START("period_start","com.cap.ctrl.ocr.businesslicence.periodstart",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    PERIOD_END("period_end","com.cap.ctrl.ocr.businesslicence.periodend",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REG_INSTITUTION("reg_institution","com.cap.ctrl.ocr.businesslicence.reginstitution",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REG_TIME("reg_time","com.cap.ctrl.ocr.businesslicence.regtime",new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE,FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    STATUS("status","com.cap.ctrl.ocr.businesslicence.status",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    DUTY("duty","扣缴义务",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    TAXPAY_NUM("taxpay_num","纳税人编码",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    CNTAXPAY_NUM("cntaxpay_num","国税纳税编码",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    LOTAXPAY_NUM("lotaxpay_num","地税纳税编码",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    REF_NUM("ref_num","档案号",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    VALIDITY("validity","com.cap.ctrl.ocr.idcard.validity",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    ISSUE_ORG("issue_org","颁发单位",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA}),
    CREDIT_CODE("credit_code","com.cap.ctrl.ocr.businesslicence.creditcode",new FormFieldComEnum[]{FormFieldComEnum.TEXT,FormFieldComEnum.TEXTAREA});

    private String key;
    private String name;
    private FormFieldComEnum[] supportFieldType;

    CommonCardProp(String key, String name,FormFieldComEnum[] supportFieldType) {
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
        for(CommonCardProp commonCardProp : CommonCardProp.values()){
            Map<String,Object> tempMap = new HashMap<String, Object>();
            tempMap.put("key",commonCardProp.getKey());
            tempMap.put("name",commonCardProp.getName());
            values.add(tempMap);
        }
        return values;
    }

    public static CommonCardProp getByKey(String key) {
        for (CommonCardProp type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }
}
