package com.seeyon.apps.testBPM.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.testBPM.po.ThirdFormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;

/**
 * Description
 * <pre></pre>
 * @author FanGaowei<br>
 * Date 2018年1月11日 上午11:13:27<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ParseMasterBean {
	
	public static List<ThirdFormBean> parse(FormDataMasterBean masterBean) {	
		FormFieldBean field = null;
		List<ThirdFormBean> list = new ArrayList<ThirdFormBean>();
		Map<String, Object> dataMap = masterBean.getAllDataMap();
		for(String key : dataMap.keySet()) {
			if(key.startsWith("field")) {
				try {
					field = masterBean.getFieldBeanByFieldName(key);
					list.add(new ThirdFormBean(masterBean, field));
				} catch (Exception e) {
				}
			}
		}
		return list;
	}
	
}
