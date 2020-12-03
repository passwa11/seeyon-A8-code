package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

public class XrdBtnCtrl extends FormFieldCustomCtrl {
	private static final Log LOGGER = CtpLogFactory.getLog(XrdBtnCtrl.class);

	private static final String KEY = "37370243924";

	@Override
	public String getFieldLength() {
		// TODO Auto-generated method stub
		return "20";
	}

	@Override
	public boolean canUse() {
		return false;
	}

	@Override
	public String getMBInjectionInfo() {
		return "{path:'http://querybtn.v5.cmp/v1.0.0/',jsUri:'js/index.js',initMethod:'init',nameSpace:'feild_"+this.getKey()+"'}";
	}

	@Override
	public String getPCInjectionInfo() {
		return "{path:'apps_res/cap/customCtrlResources/xrd/',jsUri:'js/index.js',initMethod:'init',nameSpace:'feild_"+this.getKey()+"'}";
	}

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        return super.authNotNullAndValIsNull(formDataMasterBean, field, authViewFieldBean, val);
    }

    @Override
	public void init() {
		LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        this.setIcon("cap-icon-trust-contract");
		ParamDefinition templateIdParam = new ParamDefinition();
		templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/xrd/html/xrd.html");
		templateIdParam.setDisplay("com.cap.ctrl.xrd.ctrlset");
		templateIdParam.setName("templateId");
		templateIdParam.setParamType(Enums.ParamType.button);
		templateIdParam.setDialogHeight("505");
		addDefinition(templateIdParam);
		LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
	}
	@Override
	public void importExtInfo(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean,
			Map<String, Object> fieldInfo) {
		formFieldBean.setCustomParam("");
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
	public String[] getDefaultVal(String arg0) {
		return new String[0];
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public List<String[]> getListShowDefaultVal(Integer arg0) {
		return null;
	}

	@Override
	public String getText() {
		return ResourceUtil.getString("com.cap.ctrl.xrd.ctrlname");
	}

	@Override
	public boolean isAttachment() {
		// TODO Auto-generated method stub
		return false;
	}
}
