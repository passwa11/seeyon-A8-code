package com.seeyon.apps.customFieldCtrl.fieldCtrl;

import java.util.List;

import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;

/**
 * <pre>
 * 自定义控件:条码打印
 * </pre>
 */
public class PrintFieldCtrl extends FormFieldCustomCtrl {

    public String getKey() {
        return "4793655815239855349";
    }

    public String getFieldLength() {
        return "20";
    }

    public void init() {
        setPluginId("print");
    }

    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/printResources/',jsUri:'js/printBtn.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
    }

    public String getMBInjectionInfo() {
        return null;
    }

    public String getText() {
        return "条码打印";
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
