package com.seeyon.apps.customFieldCtrl.fieldCtrl;

import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;

import java.util.List;

public class ScanBarcodeFieldCtrl extends FormFieldCustomCtrl {

    public String getKey() {
        return "5193746555244385517";
    }

    public String getFieldLength() {
        return "20";
    }

    public void init() {
        setPluginId("scanBarcode");
    }

    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/scanBarcode/',jsUri:'js/scanBarcode.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
    }

    public String getMBInjectionInfo() {
        return null;
    }

    public String getText() {
        return "条码录入";
    }

    public boolean canBathUpdate() {
        return false;
    }

    public List<String[]> getListShowDefaultVal(Integer externalType) {
        return null;
    }

    public String[] getDefaultVal(String defaultValue) {
        return new String[0];
    }

    public boolean canInjectionWord() {
        return false;
    }

}
