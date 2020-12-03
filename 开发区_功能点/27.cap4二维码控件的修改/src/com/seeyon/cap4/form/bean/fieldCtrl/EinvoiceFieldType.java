package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.util.Enums.FieldType;
import com.seeyon.ctp.common.i18n.ResourceUtil;

/**
 * Created by yangz on 2018/3/21.
 * 电子发票字段映射枚举
 */
public enum EinvoiceFieldType {
    EINVOICETYPE("FPLX", "com.cap.ctrl.eivoice.fplx", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    EINVOICESTATE("FPZT", "com.cap.ctrl.eivoice.fpzt", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    EINVOICESIGN("ZJBZ", "com.cap.ctrl.eivoice.zjbz", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    CHECKNUM("JYM", "com.cap.ctrl.eivoice.jym", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    EINVOICECODE("FPDM", "com.cap.ctrl.eivoice.fpdm", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    EINVOICENUM("FPHM", "com.cap.ctrl.eivoice.fphm", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    RATEPAYERCODE("GMF_NSRSBH", "com.cap.ctrl.eivoice.gmfnsrsbh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    BUYERNAME("GMF_MC", "com.cap.ctrl.eivoice.gmfmc", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    BUYERADDRES("GMF_DZDH", "com.cap.ctrl.eivoice.gmfdzdh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    BUYERBANK("GMF_YHZH", "com.cap.ctrl.eivoice.gmfyhzh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    SELLERNAME("XSF_MC", "com.cap.ctrl.eivoice.xsfmc", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    SELLERADDRES("XSF_DZDH", "com.cap.ctrl.eivoice.xsfdzdh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    SELLERBANK("XSF_YHZH", "com.cap.ctrl.eivoice.xsfyhzh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    SELLERRATEPAYERCODE("XSF_NSRSBH", "com.cap.ctrl.eivoice.xsfnsrsbh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    REMARK("BZ", "com.cap.ctrl.eivoice.bz", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXTAREA}),
    TOTALINVOICE("HJJE", "com.cap.ctrl.eivoice.hjje", FieldType.DECIMAL, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    VALOREMSUM("JSHJ", "com.cap.ctrl.eivoice.jshj", FieldType.DECIMAL, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    AMOUNTTAXSUM("HJSE", "com.cap.ctrl.eivoice.hjse", FieldType.DECIMAL, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    //TICKETHOLDER("KPR", "开票人", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    //PAYEE("SKR", "收款人", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    //AUDITOR("FHR", "复核人", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    COMPUTERNO("JQBH", "com.cap.ctrl.eivoice.jqbh", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    CHECKRESULT("MS", "com.cap.ctrl.eivoice.ms", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXT}),
    CIPHERTEXT("FP_MW", "com.cap.ctrl.eivoice.fpmw", FieldType.VARCHAR, new FormFieldComEnum[]{FormFieldComEnum.TEXTAREA}),
    INVOICEDATE("KPRQ", "com.cap.ctrl.eivoice.kprq", FieldType.TIMESTAMP, new FormFieldComEnum[]{FormFieldComEnum.EXTEND_DATE});


    EinvoiceFieldType(String key, String fieldName, FieldType fieldType, FormFieldComEnum[] supportFieldType) {
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


    public static EinvoiceFieldType getEnumByKey(String key) {
        for (EinvoiceFieldType type : values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }

    public static boolean isFieldCanBeChoose(String inputType) {
        boolean result = false;
        for (EinvoiceFieldType type : values()) {
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
