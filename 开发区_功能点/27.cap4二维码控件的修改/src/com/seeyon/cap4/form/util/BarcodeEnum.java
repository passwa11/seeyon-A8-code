package com.seeyon.cap4.form.util;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 二维码相关的枚举
 *
 * @author wangh
 * @create 2018-02-05 13:17
 **/
public class BarcodeEnum {
    /**
     * 二维码类型
     */
    public enum BarcodeType {
        matrix("QR_CODE", "矩阵式", "form.barcode.type.label.matrix", true),
        line("PDF_417", "行排式", "form.barcode.type.label.line", false);

        BarcodeType(String key, String text, String i18n, boolean canUse) {
            this.key = key;
            this.text = text;
            this.i18n = i18n;
            this.canUse = canUse;
        }

        private String key;
        private String text;
        private String i18n;
        private boolean canUse;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getText() {
            return ResourceUtil.getString(i18n);
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getI18n() {
            return i18n;
        }

        public void setI18n(String i18n) {
            this.i18n = i18n;
        }

        public boolean isCanUse() {
            return canUse;
        }

        public void setCanUse(boolean canUse) {
            this.canUse = canUse;
        }

        /**
         * 获取所有的类型
         *
         * @return
         */
        public static List<BarcodeType> getAllEnum() {
            List<BarcodeType> list = new ArrayList<BarcodeType>();
            for (BarcodeType bar : BarcodeType.values()) {
                if (bar.canUse) {
                    list.add(bar);
                }
            }
            return list;
        }
    }
    /**
     * 二维码纠错选项
     */
    public enum BarcodeCorrectionOption {
        low("L", "低", "form.barcode.correction.label.low", "7%"),
        middle("M", "中", "form.barcode.correction.label.middle", "15%"),
        middle_high("Q", "中高", "form.barcode.correction.label.middle-high", "25%"),
        high("H", "高", "form.barcode.correction.label.high", "30%");

        /**
         * ErrorCorrectionLevel
         */
        private String key;
        private String text;
        private String i18n;
        private String value;

        BarcodeCorrectionOption(String key, String text, String i18n, String value) {
            this.key = key;
            this.text = text;
            this.i18n = i18n;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getText() {
            return ResourceUtil.getString(i18n);
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getI18n() {
            return i18n;
        }

        public void setI18n(String i18n) {
            this.i18n = i18n;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static List<BarcodeCorrectionOption> getAllItem() {
            List<BarcodeCorrectionOption> list = new ArrayList<BarcodeCorrectionOption>();
            for (BarcodeCorrectionOption bar : BarcodeCorrectionOption.values()) {
                list.add(bar);
            }
            return list;
        }
    }

    /**
     * 二维码内容组成时，需要存入显示值的控件
     */
    public enum NeedSaveDisplayType {
        RADIO(FormFieldComEnum.RADIO.getKey(), FormFieldComEnum.RADIO.getText()),
        SELECT(FormFieldComEnum.SELECT.getKey(), FormFieldComEnum.SELECT.getText()),
        CHECKBOX(FormFieldComEnum.CHECKBOX.getKey(), FormFieldComEnum.CHECKBOX.getText()),
        RAIMAGE_RADIODIO(FormFieldComEnum.IMAGE_RADIO.getKey(), FormFieldComEnum.IMAGE_RADIO.getText()),
        IMAGE_SELECT(FormFieldComEnum.IMAGE_SELECT.getKey(), FormFieldComEnum.IMAGE_SELECT.getText()),
        EXTEND_MEMBER(FormFieldComEnum.EXTEND_MEMBER.getKey(), FormFieldComEnum.EXTEND_MEMBER.getText()),
        EXTEND_MULTI_MEMBER(FormFieldComEnum.EXTEND_MULTI_MEMBER.getKey(), FormFieldComEnum.EXTEND_MULTI_MEMBER.getText()),
        EXTEND_ACCOUNT(FormFieldComEnum.EXTEND_ACCOUNT.getKey(), FormFieldComEnum.EXTEND_ACCOUNT.getText()),
        EXTEND_MULTI_ACCOUNT(FormFieldComEnum.EXTEND_MULTI_ACCOUNT.getKey(), FormFieldComEnum.EXTEND_MULTI_ACCOUNT.getText()),
        EXTEND_DEPARTMENT(FormFieldComEnum.EXTEND_DEPARTMENT.getKey(), FormFieldComEnum.EXTEND_DEPARTMENT.getText()),
        EXTEND_MULTI_DEPARTMENT(FormFieldComEnum.EXTEND_MULTI_DEPARTMENT.getKey(), FormFieldComEnum.EXTEND_MULTI_DEPARTMENT.getText()),
        EXTEND_POST(FormFieldComEnum.EXTEND_POST.getKey(), FormFieldComEnum.EXTEND_POST.getText()),
        EXTEND_MULTI_POST(FormFieldComEnum.EXTEND_MULTI_POST.getKey(), FormFieldComEnum.EXTEND_MULTI_POST.getText()),
        EXTEND_LEVEL(FormFieldComEnum.EXTEND_LEVEL.getKey(), FormFieldComEnum.EXTEND_LEVEL.getText()),
        EXTEND_MULTI_LEVEL(FormFieldComEnum.EXTEND_MULTI_LEVEL.getKey(), FormFieldComEnum.EXTEND_MULTI_LEVEL.getText());

        private String key;
        private String text;

        NeedSaveDisplayType(String key, String text) {
            this.key = key;
            this.text = text;
        }

        public String getKey() {
            return key;
        }

        public String getText() {
            return text;
        }

        public static NeedSaveDisplayType getEnumByKey(String key) {
            for (NeedSaveDisplayType e : NeedSaveDisplayType.values()) {
                if (e.getKey().equals(key)) {
                    return e;
                }
            }
            return null;
        }

        public static List<NeedSaveDisplayType> getAllEnums() {
            List<NeedSaveDisplayType> list = new ArrayList<NeedSaveDisplayType>();
            for (NeedSaveDisplayType e : NeedSaveDisplayType.values()) {
                list.add(e);
            }
            return list;
        }
    }
}
