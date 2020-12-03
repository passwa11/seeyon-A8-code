package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.util.Enums.FieldType;
import com.seeyon.ctp.common.i18n.ResourceUtil;

/**
 * Created by jinx on 2018/5/9.
 * 企业征信字段映射枚举
 */
public enum CreditqueryFieldType {
    ENTERPRISE_NUMBER("id", "com.cap.ctrl.creditquery.enterprisenumber", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    NAME("name", "com.cap.ctrl.creditquery.name", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    ECON_KIND("econ_kind", "com.cap.ctrl.creditquery.econkind", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    REGIST_CAPI("regist_capi", "com.cap.ctrl.creditquery.registcapi", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    ADDRESS("address", "com.cap.ctrl.creditquery.address", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    REG_NO("reg_no", "com.cap.ctrl.creditquery.regno", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    SCOPE("scope", "com.cap.ctrl.creditquery.scope", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    TERM_START("term_start", "com.cap.ctrl.creditquery.termstart", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE}),
    TERM_END("term_end", "com.cap.ctrl.creditquery.termend", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE}),
    BELONG_ORG("belong_org", "com.cap.ctrl.creditquery.belongorg", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    OPER_NAME("oper_name", "com.cap.ctrl.creditquery.opername", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    start_date("start_date", "com.cap.ctrl.creditquery.startdate", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE}),
    END_DATE("end_date", "com.cap.ctrl.creditquery.enddate", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE}),
    CHECK_DATE("check_date", "com.cap.ctrl.creditquery.checkdate", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE}),
    STATUS("status", "com.cap.ctrl.creditquery.status", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    ORG_NO("org_no", "com.cap.ctrl.creditquery.orgno", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    CREDIT_NO("credit_no", "com.cap.ctrl.creditquery.creditno", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    PROVINCE("province", "com.cap.ctrl.creditquery.province", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    PROVINCE1("province1", "com.cap.ctrl.creditquery.province1", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    CITY("city", "com.cap.ctrl.creditquery.city", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA}),
    DOMAINS("domains", "com.cap.ctrl.creditquery.domains", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA});

    CreditqueryFieldType(String key, String fieldName, FieldType fieldType, FormFieldComEnum[] supportFieldType) {
        this.key = key;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.supportFieldType = supportFieldType;
    }

    private String key;
    private String fieldName;
    private FieldType fieldType;
    private FormFieldComEnum[] supportFieldType;


    public String getKey() {
        return key;
    }

    public String getText() {
        return ResourceUtil.getString(fieldName);
    }

    public FormFieldComEnum[] getSupportFieldType() {
        return supportFieldType;
    }

    public FieldType getFieldType() {
        return fieldType;
    }


    public static CreditqueryFieldType getEnumByKey(String key) {
        for (CreditqueryFieldType type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }

    public static boolean isFieldCanBeChoose(String inputType) {
        boolean result = false;
        for (CreditqueryFieldType type : values()) {
            FormFieldComEnum[] comEnums = type.getSupportFieldType();
            for (FormFieldComEnum comEnum : comEnums) {
                if (comEnum.getKey().equalsIgnoreCase(inputType)) {
                    result = true;
                    break;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }

}
