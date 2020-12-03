package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import org.apache.commons.logging.Log;
import com.seeyon.cap4.form.util.Enums;

import java.util.List;

/**
 * Created by weijh on 2018/5/9.
 * 企业征信
 */
public class FormCreditqueryCtrl extends FormFieldCustomCtrl {

    private static final Log LOGGER = CtpLogFactory.getLog(FormCreditqueryCtrl.class);

    @Override
    public String getKey() {
        return "8586040726273737290";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.creditquery.text");
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
    public String[] getDefaultVal(String s) {
        return new String[0];
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        String strVal = String.valueOf(val);
        return authViewFieldBean.isNotNullable()&&(StringUtil.checkNull(strVal));
    }

    @Override
    public void init() {
        this.setPluginId("formCreditqueryCtrl");
        this.setIcon("cap-icon-corporation-credit");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition creditquerymap = new ParamDefinition();
        creditquerymap.setDialogUrl("apps_res/cap/customCtrlResources/setting_page_project/dist/creditquery/creditquery.html");
        creditquerymap.setDisplay("com.cap.ctrl.creditquery.paramset");
        creditquerymap.setName("mapping");
        creditquerymap.setParamType(Enums.ParamType.button);
        addDefinition(creditquerymap);

        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formCreditqueryResources/',jsUri:'js/formCreditqueryPCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://formcreditqueryctrl.v5.cmp/v1.0.0/',weixinpath:'formcreditqueryctrl',jsUri:'js/formCreditqueryMbRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "1000";
    }

    @Override
    public boolean canInjectionWord() {
        return false;
    }

    @Override
    public void importInfoAfterBizImport(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean) {
        formFieldBean.setCustomParam("");
    }
}
