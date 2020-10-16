package com.seeyon.apps.testBPM.po;

import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;

/**
 * Description
 * 
 * <pre>定义传给第三方的实体</pre>
 * 
 * @author FanGaowei<br>
 *         Date 2018年1月11日 上午11:05:14<br>
 *         Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ThirdFormBean {
	
	private String name;	 // OA 的field000X
	private String display;  // 显示名称
	private Object value; 	 // 值
	
	public ThirdFormBean() {}
	
	public ThirdFormBean(FormDataMasterBean masterBean, FormFieldBean field) {
		this.name = field.getName();
		this.display = field.getDisplay();
		this.value = masterBean.getFieldValue(field.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
