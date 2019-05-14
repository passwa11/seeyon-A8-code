package com.seeyon.apps.customFieldCtrl.kit;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;


/**
 * Description
 * <pre></pre>
 * @author FanGaowei<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class CAP4FormKit {
    
    private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormKit.class);

    public static FormBean getFormBean(CAP4FormManager cap4FormManager, String code) {
        FormBean formBean = null;
        try {
            formBean = cap4FormManager.getFormByFormCode(code);
        } catch(BusinessException e) {
            LOGGER.error("获取表单发生异常,编号：" + code, e);
        }
        return formBean;
    }
	
	/**
	 * 根据表单的显示名称获取字段的值
	 * @param bean
	 * @param disPlay
	 * @return
	 */
	public static Object getFieldValue(FormDataBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if(table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if(field == null) {
			return null;
		}
		return bean.getFieldValue(field.getName());
	}
	
	
	public static int getIntValue(FormDataBean bean, String disPlay) {
        Object value = getFieldValue(bean, disPlay);
        return StrKit.toInteger(value);
    }
	
	public static String getFieldStrValue(FormDataBean bean, String disPlay) {
	    Object value = getFieldValue(bean, disPlay);
	    return StrKit.str(value);
	}
	
	public static Object getFieldValueByName(FormDataBean bean, String fieldName) {
        return bean.getFieldValue(fieldName);
    }
	
	/**
	 * 根据表单的显示名称获取字段是  field000？
	 * @param bean
	 * @param disPlay
	 * @return
	 */
	public static String getFieldTaleId(FormBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		FormTableBean table = bean.getMasterTableBean();
		return getFieldTaleId(table, disPlay);
	} 
	
	/**
	 * 直接根据table来获取
	 * @param table
	 * @param disPlay
	 * @return
	 */
	public static String getFieldTaleId(FormTableBean table, String disPlay) {
        if(table == null) {
            return null;
        }
        FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
        if(field == null) {
            return null;
        }
        return field.getName();
    }
	
	public static String getFieldTaleId(FormDataBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if(table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if(field == null) {
			return null;
		}
		return field.getName();
	}
	
	public static FormFieldBean getFieldBean(FormDataBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if(table == null) {
			return null;
		}
		return table.getFieldBeanByDisplay(disPlay);
	}
	
	public static FormFieldBean getFieldBean(FormTableBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		return bean.getFieldBeanByDisplay(disPlay);
	}
	
	
	/**
	 * Description:
	 * <pre></pre>
	 * @param bean 这里的bean必须是getMasterBean() 方法获取到的bean
	 * @param disPlay 
	 * @param value
	 */
	public static void setCellValue(FormDataBean bean, String disPlay, Object value) {
		FormFieldBean cell = CAP4FormKit.getFieldBean(bean, disPlay);
		if(cell != null) {
			bean.addFieldValue(cell.getName(), value);
		}
	}
	
	public static void setCellValue(FormDataSubBean bean, String disPlay, Object value) {
        FormFieldBean cell = CAP4FormKit.getFieldBean(bean, disPlay);
        if(cell != null) {
            bean.addFieldValue(cell.getName(), value);
        }
    }
	
	/**
	 * Description:
	 * <pre>只适用于只有一个子表的表单</pre>
	 * @param colManager
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static List<FormDataSubBean> getSubBeans(FormDataMasterBean master) throws Exception {
		Map<String, List<FormDataSubBean>> subs = master.getSubTables();
		if(null != subs && subs.size() > 0) {
			for(String key : subs.keySet()) {
				return subs.get(key);
			}
		}
		return null;
	}
	
	/**
	 * Description:
	 * <pre>获取从表字段</pre>
	 * @param sub
	 * @param disPlay
	 * @return
	 */
	public static Object getSubFieldValue(FormDataSubBean sub, String disPlay) {
		return getFieldValue(sub, disPlay);
	}
}
