package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import java.util.List;

public class ProjectRelatedCtrl extends FormFieldCustomCtrl {

    private static final Log LOGGER = CtpLogFactory.getLog(ProjectRelatedCtrl.class);
    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/projectRelatedResources/',jsUri:'js/projectRelatedPCRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://formcreditqueryctrl.v5.cmp/v1.0.0/',weixinpath:'formcreditqueryctrl',jsUri:'js/projectRelatedResources/projectRelatedMbRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getKey() {
        return "1745730351955790712";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.project.related.text");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer integer) {
        return null;
    }

    @Override
    public void init() {
        this.setPluginId("projectRelatedCtrl");
        this.setIcon("cap-icon-guanlianxiangmu");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition projectRelatedMap = new ParamDefinition();

        projectRelatedMap.setDialogUrl("apps_res/cap/customCtrlResources/setting_page_project/dist/projectRelated/projectRelated.html");

        projectRelatedMap.setDisplay("com.cap.ctrl.project.related.paramset");
        projectRelatedMap.setName("mapping");
        projectRelatedMap.setParamType(Enums.ParamType.button);
        addDefinition(projectRelatedMap);

        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String[] getDefaultVal(String defaultValue) {
        return new String[0];
    }

    @Override
    public String getFieldLength() {
        return "1000";
    }
}
