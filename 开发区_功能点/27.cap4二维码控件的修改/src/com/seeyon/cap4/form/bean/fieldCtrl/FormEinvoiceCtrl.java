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
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangz on 2018/3/20.
 * 电子发票
 */
public class FormEinvoiceCtrl extends FormFieldCustomCtrl {

    private static final Log LOGGER = CtpLogFactory.getLog(FormEinvoiceCtrl.class);

    @Override
    public String getKey() {
        return "4578843378267869145";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.ctrl.einvoice.text");
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
            AttachmentManager attachmentManager = (AttachmentManager)AppContext.getBean("attachmentManager");
            result = authViewFieldBean.isNotNullable()&&attachmentManager.getBySubReference(Long.parseLong(strVal)).size()<=0;
        }
        return result;
    }

    @Override
    public void init() {
        this.setPluginId("formInvoiceBtn");
        this.setIcon("cap-icon-e-invoice");
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition eivoiceDef = new ParamDefinition();
        eivoiceDef.setDialogUrl("apps_res/cap/customCtrlResources/formEinvoiceCtrlResources/html/EinvoiceSetting.html");
        eivoiceDef.setDisplay("com.cap.ctrl.einvoice.paramtext");//如果要做国际化 这个地方只能存key
        eivoiceDef.setName("mapping");
        eivoiceDef.setParamType(Enums.ParamType.button);
        addDefinition(eivoiceDef);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/formEinvoiceCtrlResources/',jsUri:'js/formEinvoicePcRuning.js',initMethod:'init',nameSpace:'feild_" + this.getKey() + "'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://einvoice.v5.cmp/v1.0.0/',weixinpath:'invoice',jsUri:'js/formEinvoiceMbRuning.js',initMethod:'init',nameSpace:'feild_"+this.getKey()+"'}";

    }

    @Override
    public String getFieldLength() {
        return "20";
    }

    public String getEinvoiceFields() {
        Map<String,Object> fieldInfo = new HashMap<String,Object>();
        for (EinvoiceFieldType type : EinvoiceFieldType.values()) {
            fieldInfo.put(type.getKey(), type.getText());
        }

        return JSONUtil.toJSONString(fieldInfo);
    }

    @Override
    public boolean canInjectionWord() {
        return false;
    }

}
