package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * 信任盖章
 */
public class TrustDoSignCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(TrustDoSignCtrl.class);

    @Override
    public String getKey() {
        return "232b4b0cedc94522b46b69284839673b";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("信任度签字");
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
     * 签章控件
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
        setPluginId("trustDoSignCtrl");
        this.setIcon("cap-icon-section-btn-setting");
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/trustDoSignCtrlResources/',jsUri:'js/trustDoSignPCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://trustdo.v5.cmp/v1.0.0/',weixinpath:'trustdo',jsUri:'js/trustDoSign/trustDoSignPCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "255";
    }

    @Override
    public void handleSaving(Map<String, Object> params) {
    }

    @Override
    public void clearWhenBackToStarter(FormBean formBean, Long masterDataId, FormFieldBean fieldBean, Map<String,Object> fillBackMap,Map<String,Long> attachments) throws BusinessException {

    }

    @Override
    public String convertVal4Index(Map<String,Object> params) {
        return null;
    }

    @Override
    public String getTaoHongValue(FormDataMasterBean masterBean, FormFieldBean fieldBean, Map<String, Object> special) throws BusinessException {
        special.put("inputType","handwrite");
        return super.getTaoHongValue(masterBean, fieldBean, special);
    }
}
