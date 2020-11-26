package com.seeyon.apps.common.kit;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.form.bean.*;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.workflow.event.WorkflowEventData;

import java.util.List;
import java.util.Map;

/**
 * Description
 * <pre>仅支持CAP3的表单数据获取</pre>
 * @author FanGaowei<br>
 * Date 2018年1月23日 上午11:22:28<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class FormKit {

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

	public static String getFieldTaleId(FormBean bean, String disPlay) {
		if(bean == null) {
			return null;
		}
		FormTableBean table = bean.getMasterTableBean();
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

	/**
	 * Description:
	 * <pre>根据summaryId获取session中的masterBean</pre>
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public static FormDataMasterBean getMasterBean(ColManager colManager, WorkflowEventData data) throws Exception {
		ColSummary summary = colManager.getColSummaryById(data.getSummaryId());
		if(summary == null) {
			return null;
		}
		return FormService.findDataById(summary.getFormRecordid(), Long.valueOf(data.getFormApp()));
	}

    public static FormDataMasterBean getMasterBean(ColManager colManager, Long summaryId) throws Exception {
        ColSummary summary = colManager.getColSummaryById(summaryId);
        if(summary == null) {
            return null;
        }
        return FormService.findDataById(summary.getFormRecordid(), summary.getFormAppid());
    }

    public static FormDataMasterBean getMasterBean(ColManager colManager, ColSummary summary) throws Exception {
        if(summary == null) {
            return null;
        }
        return FormService.findDataById(summary.getFormRecordid(), summary.getFormAppid());
    }

	/**
	 * Description:
	 * <pre>从数据库获取masterbean</pre>
	 * @param colManager
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static FormDataMasterBean getMasterBeanFromDB(ColManager colManager, WorkflowEventData data) throws Exception {
		ColSummary summary = colManager.getColSummaryById(data.getSummaryId());
		if(summary == null) {
			return null;
		}
		return FormService.findDataById(summary.getFormRecordid(), summary.getFormAppid());
	}

	/**
	 * Description:
	 * <pre></pre>
	 * @param bean 这里的bean必须是getMasterBean() 方法获取到的bean
	 * @param disPlay
	 * @param value
	 */
	public static void setCellValue(FormDataBean bean, String disPlay, Object value) {
		FormFieldBean cell = FormKit.getFieldBean(bean, disPlay);
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
