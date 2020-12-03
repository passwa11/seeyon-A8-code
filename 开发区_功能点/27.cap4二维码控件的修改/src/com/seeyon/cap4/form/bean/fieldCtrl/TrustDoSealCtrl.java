package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.UUIDLong;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * 信任签字
 */
public class TrustDoSealCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(TrustDoSealCtrl.class);

    @Override
    public String getKey() {
        return "fff486d125bc48e5b01760a76913aa2a";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("信任度盖章");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public boolean canInSubTable() {
        return false;
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer integer) {
        return null;
    }

    /**
     * 信任签字
     *
     * @return
     */
    @Override
    public boolean canInjectionWord() {
        return true;
    }

    @Override
    public String[] getDefaultVal(String s) {
        return new String[0];
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        return super.authNotNullAndValIsNull(formDataMasterBean, field, authViewFieldBean, val);
    }

    @Override
    public void init() {
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        setPluginId("trustDoSign");
        this.setIcon("cap-icon-qianzhang");
        ParamDefinition eivoiceDef = new ParamDefinition();
        eivoiceDef.setDialogUrl("apps_res/cap/customCtrlResources/trustDoSealCtrlResources/html/trustDoSeal.html");
        //如果要做国际化 这个地方只能存key
        eivoiceDef.setDisplay("选择印章类型");
        eivoiceDef.setNotNull(true);
        eivoiceDef.setName("mapping");
        eivoiceDef.setParamType(Enums.ParamType.button);
        eivoiceDef.setDialogHeight("300");
        eivoiceDef.setDialogWidth("300");
        addDefinition(eivoiceDef);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/trustDoSealCtrlResources/',jsUri:'js/trustDoSealPCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://trustdo.v5.cmp/v1.0.0/',weixinpath:'trustdo',jsUri:'js/trustDoSeal/trustDoSealPCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "255";
    }

    @Override
    public void handleSaving(Map<String, Object> params) {
    }

    @Override
    public void clearWhenBackToStarter(FormBean formBean, Long masterDataId, FormFieldBean fieldBean, Map<String, Object> fillBackMap, Map<String, Long> attachments) {
    }

    @Override
    public String convertVal4Index(Map<String, Object> params) {
        return null;
    }

    @Override
    public String getTaoHongValue(FormDataMasterBean masterBean, FormFieldBean fieldBean, Map<String, Object> special) {
        return null;
    }
    @Override
    public Object genVal(Object oldVal) {
        if (StringUtil.checkNull(String.valueOf(oldVal))) {
            return UUIDLong.longUUID();
        } else {
            return oldVal;
        }
    }
}
