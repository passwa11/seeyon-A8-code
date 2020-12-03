package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yangyw on 2018/6/7.
 * 电子签章
 */
public class FormHandWriteCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(FormHandWriteCtrl.class);

    @Override
    public String getKey() {
        return "224852204965216426";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.handwrite.text");
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
     * 签章控件需要套红到word文档中
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
        boolean result = false;
        if(authViewFieldBean.isNotNullable()) {
            String strVal = String.valueOf(val);
            if (StringUtil.checkNull(strVal)) {
                result = true;
            }else{
                V3xHtmDocumentSignatManager v3xHtmDocumentSignatManager = (V3xHtmDocumentSignatManager) AppContext.getBean("v3xHtmDocumentSignatManager");
                List<V3xHtmDocumentSignature> signatures = v3xHtmDocumentSignatManager.getByFieldName(strVal);
                if(null!=signatures && signatures.size()>0){
                    result = false;
                }else{
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public void init() {
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        setPluginId("formHandWirteCtrl");
        this.setIcon("cap-icon-qianzhang");
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formHandWriteCtrlResources/',jsUri:'js/formHandWritePCRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://formhandwritectrl.v5.cmp/v1.0.0/',weixinpath:'formhandwritectrl',jsUri:'js/handwriteMbRunning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "255";
    }

    @Override
    public void handleSaving(Map<String, Object> params) {
        //签章控件保存只有移动端保存才会单独处理，PC保存是通过前端监听事件异步保存的。
        if (AppContext.getCurrentUser().getLoginSign() == Constants.login_sign.phone.value()) {
            //将参数中的大对象移除
            params.remove("param");
            params.remove("formFieldBean");
            params.remove("formDataMasterBean");
            params.remove("formBean");
            //执行签章数据保存
            if (params.size() > 0) {
                SignetManager signetManager = (SignetManager) AppContext.getBean("signetManager");
                try {
                    //如果参数里fieldName是null的话，说明不需要签章保存
                    if(params.get("fieldName") != null){
                        params.put("isNewImg", "false");
                        List<Map<String, Object>> qianpiList = new ArrayList<Map<String, Object>>();
                        qianpiList.add(params);
                        String qianpiData = JSONUtil.toJSONString(qianpiList);
                        params.put("qianpiData", qianpiData);
                        signetManager.saveSignets(params);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 回退或者撤销到待发的时候，实现控件值的清空接口
     *
     * @param formBean     表单定义bean
     * @param masterDataId 数据id
     * @param fieldBean    字段定义bean
     * @param fillBackMap  如果要清空此字段的值，直接在map中put以字段名称fieldxxxx为key以null为value即可，cap会自动将map中的值更新回数据库动态表
     */
    @Override
    public void clearWhenBackToStarter(FormBean formBean, Long masterDataId, FormFieldBean fieldBean, Map<String, Object> fillBackMap, Map<String, Long> attachments) throws BusinessException {
        super.clearWhenBackToStarter(formBean, masterDataId, fieldBean, fillBackMap, attachments);
        SignetManager signetManager = (SignetManager) AppContext.getBean("signetManager");
        V3xHtmDocumentSignatManager v3xHtmDocumentSignatManager = (V3xHtmDocumentSignatManager) AppContext.getBean("v3xHtmDocumentSignatManager");
        signetManager.deleteByRecordId(String.valueOf(masterDataId));
        v3xHtmDocumentSignatManager.deleteBySummaryId(masterDataId);
    }

    @Override
    public String convertVal4Index(Map<String, Object> params) {
        return null;
    }

    @Override
    public String getTaoHongValue(FormDataMasterBean masterBean, FormFieldBean fieldBean, Map<String, Object> special) throws BusinessException {
        special.put("inputType", "handwrite");
        return super.getTaoHongValue(masterBean, fieldBean, special);
    }
}
