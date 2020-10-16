package com.seeyon.apps.customFieldCtrl.grid.fieldCtrl;

import java.util.List;

import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;

/**
 * <pre>
 * 自定义控件:表格选择数据
 * </pre>
 */
public class GridFieldCtrl extends FormFieldCustomCtrl {
	
    public String getKey() {
        return "4793689415239855520";
    }

    public String getFieldLength() {
        return "255";
    }

    public void init() {
        setPluginId("mapFieldCtrl");
    }

    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/mapResources/',jsUri:'js/location.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
    }

    public String getMBInjectionInfo() {
        return "{path:'http://mapResource.v5.cmp/v1.0.0/',weixinpath:'invoice',jsUri:'js/location.js',initMethod:'init',nameSpace:'feild_"+this.getKey()+"'}";
    }
    
    public String getText() {
        return "数据关联";
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
