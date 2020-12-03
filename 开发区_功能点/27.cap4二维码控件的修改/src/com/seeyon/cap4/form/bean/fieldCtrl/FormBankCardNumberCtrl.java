package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import java.util.List;

public class FormBankCardNumberCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(FormBankCardNumberCtrl.class);

    @Override
    public String getFieldLength() {
        return "30";
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formBankCardNumberCtrlResources/',jsUri:'js/formBankCardNumberPcRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://collaboration.v5.cmp/v1.0.0/',weixinpath:'collaboration',jsUri:'js/formBankCardNumberMbRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getKey() {
        return "4805703250428300292";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.bank.card.number.text");
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
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        this.setPluginId("formBankCardNumberCtrl");//控件所属插件id
        this.setIcon("cap-icon-ordermanage");//控件在表单编辑器中的图标
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String[] getDefaultVal(String s) {
        return new String[0];
    }
}
