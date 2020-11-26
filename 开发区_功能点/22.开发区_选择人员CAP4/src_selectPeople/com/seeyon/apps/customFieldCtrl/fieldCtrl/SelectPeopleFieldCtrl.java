package com.seeyon.apps.customFieldCtrl.fieldCtrl;

import java.util.List;

import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;

public class SelectPeopleFieldCtrl extends FormFieldCustomCtrl{


	@Override
	public String getKey() {
		//www.seeyon.com.utils.UUIDUtil.getUUIDString()接口	生成一个uuid，将此Id作为控件的key
		return "5461900594264253750";
	}

	//重写canUse()方法， 使控件可以在客户端使用  ，具体能不能生效  我也不晓得
	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public String getFieldLength() {
		return "255";
	}

	@Override
	public void init() {
		setPluginId("selectPeople");
	}

	@Override
	public String getMBInjectionInfo() {
		return null;
	}

	@Override
	public String getPCInjectionInfo() {
		String path="{path:'apps_res/cap/customCtrlResources/peopleResources/',jsUri:'js/selectPeople.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
		return path;
	}



	@Override
	public boolean canBathUpdate() {
		return false;
	}

	@Override
	public String[] getDefaultVal(String arg0) {
		return new String[0];
	}

	@Override
	public List<String[]> getListShowDefaultVal(Integer arg0) {
		return null;
	}

	@Override
	public String getText() {
		return "人员选择";
	}



}
