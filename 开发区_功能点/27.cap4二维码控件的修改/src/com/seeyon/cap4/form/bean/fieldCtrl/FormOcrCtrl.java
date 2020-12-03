package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.UUIDLong;
import org.apache.commons.logging.Log;

import java.util.List;

/**
 * Created by weijh on 2018/3/20.
 * 证照识别
 */
public class FormOcrCtrl extends FormFieldCustomCtrl {

    private static final Log LOGGER = CtpLogFactory.getLog(FormOcrCtrl.class);

    @Override
    public String getKey() {
        return "7578843372267869145";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.ocr.text");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public boolean isAttachment() {
        return true;
    }

    /**
     * 新建的时候生成此自定义控件的值，此值会存放在对应动态表的对应字段上。
     *
     * @param oldVal
     * @return
     */
    @Override
    public Object genVal(Object oldVal) {
        if (StringUtil.checkNull(String.valueOf(oldVal))) {
            return UUIDLong.longUUID();
        } else {
            return oldVal;
        }
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
        boolean result = false;
        if(StringUtil.checkNull(strVal)&&authViewFieldBean.isNotNullable()){
            result = true;
        }else{
            AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
            result = authViewFieldBean.isNotNullable()&&attachmentManager.getBySubReference(Long.parseLong(strVal)).size()<=0;
        }
        return result;
    }

    @Override
    public void init() {
        this.setPluginId("formOcrCtrl");
        this.setIcon("cap-icon-credentials-recognition");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition eivoiceMap = new ParamDefinition();
        eivoiceMap.setDialogUrl("apps_res/cap/customCtrlResources/formOcrCtrlResources/html/ocrCtrlSetting.html");
        eivoiceMap.setDisplay("com.cap.ctrl.ocr.identification");
        eivoiceMap.setName("mapping");
        eivoiceMap.setParamType(Enums.ParamType.button);
        addDefinition(eivoiceMap);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formOcrCtrlResources/',jsUri:'js/ocrCtrPcRunning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://ocrbtn.v5.cmp/v/',weixinpath:'ocrbtn/',jsUri:'js/ocrCtrM3Running.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public String getFieldLength() {
        return "20";
    }

    @Override
    public boolean canInjectionWord() {
        return false;
    }
}
