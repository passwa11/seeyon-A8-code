package com.seeyon.apps.customFieldCtrl.fieldCtrl;

import java.util.List;

import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;
import com.seeyon.cap4.form.util.Enums.ParamType;

/**
 * <pre>
 * 自定义控件:直接打开底表
 * </pre>
 */
public class OpenUnflowFieldCtrl extends FormFieldCustomCtrl {

    public String getKey() {
        return "4793655815239859651";
    }

    public String getFieldLength() {
        return "20";
    }

    public void init() {
        setPluginId("openUnflow");
        setIcon("cap-icon-querystatistics");
        // 自定义参数
        ParamDefinition templateIdParam = new ParamDefinition();
        templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/openUnflowResources/html/setTemplate.html");
        templateIdParam.setDisplay("底表配置");
        templateIdParam.setName("templateId");
        templateIdParam.setParamType(ParamType.button);
        addDefinition(templateIdParam);
    }

    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/openUnflowResources/',jsUri:'js/openUnflow.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
    }

    /** (non-Javadoc)
     * @see com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl#getMBInjectionInfo()
     * 移动端的资源地址
     */
    public String getMBInjectionInfo() {
        return null;
    }

    public String getText() {
        return "底表绑定";
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
