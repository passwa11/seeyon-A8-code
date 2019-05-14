package com.seeyon.apps.testBPM.event;

import java.util.Map;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.common.kit.FormKit;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.event.WorkflowEventResult;

/**
 * @author Fangaowei
 * 
 *         <pre>
 *         Hr高级事件共用基础事件
 *         </pre>
 * 
 * @date 2018年8月24日 下午1:26:58 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public abstract class HrCommonEvent extends AbstractWorkflowEvent {

	private ColManager colManager;

	@Override
	public WorkflowEventResult onBeforeCancel(WorkflowEventData data) {
		WorkflowEventResult res = new WorkflowEventResult();
		res.setAlertMessage("HR系统发起的流程不允许撤销");
		return res;
	}

	/**
	 * 结束前事件？
	 */
	@Override
	public WorkflowEventResult onBeforeProcessFinished(WorkflowEventData data) {
		System.out.println("流程结束前事件?");
		return super.onBeforeProcessFinished(data);
	}

	/**
	 * 发起事件校验只需要从HR系统发起
	 */
	@Override
	public WorkflowEventResult onBeforeStart(WorkflowEventData data) {
		WorkflowEventResult res = new WorkflowEventResult();
		Map<String, Object> map = data.getBusinessData();
		try {
			// CAP3
			// FormDataMasterBean master =
			// (FormDataMasterBean)map.get("formDataBean");
			FormDataMasterBean master = FormKit.getMasterBean(colManager, data);
			try {
				/*
				 * if(master == null) { res.setAlertMessage("OA获取表单失败,无法进行校验!");
				 * return res; }
				 */
				/*
				 * String from = (String)FormKit.getFieldValue(master, "来源");
				 * if(!"HR".equals(from)) {
				 * res.setAlertMessage("只能从HR系统进行流程的发起！"); }
				 */
				FormKit.setCellValue(master, "目的地", "成都");
			} catch (Exception e) {
				res.setAlertMessage("OA发生异常，请联系开发人员!" + e.getMessage());
				return res;
			}
		} catch (Exception e) {

			// CAP4
			// 从缓存中获取 formDataBean
			com.seeyon.cap4.form.bean.FormDataMasterBean master = (com.seeyon.cap4.form.bean.FormDataMasterBean) map
					.get("formDataBean");
			String a = (String) master.getFieldValue("field0001");
			if ("a".equals(a)) {
				res.setAlertMessage("值不能为a！");
			}
		}
		return res;
	}

	@Override
	public WorkflowEventResult onBeforeStop(WorkflowEventData data) {
		invokeHrSystem("流程被终止了");
		return super.onBeforeStop(data);
	}

	@Override
	public WorkflowEventResult onBeforeTakeBack(WorkflowEventData data) {
		invokeHrSystem("流程被取回了");
		return super.onBeforeTakeBack(data);
	}

	/**
	 * 记录日志到HR系统
	 */
	@Override
	public WorkflowEventResult onBeforeFinishWorkitem(WorkflowEventData data) {
		WorkflowEventResult res = new WorkflowEventResult();
		try {
			// 获取OA的一些处理信息
			ColSummary summary = colManager.getSummaryById(data.getSummaryId());
			FormDataMasterBean master = FormKit.getMasterBean(colManager, data);
			String msg = summary.getCurrentNodesInfo();
			invokeHrSystem("记录日志到HR系统：" + msg);
			// res.setAlertMessage(msg);
			FormKit.setCellValue(master, "目的地", "成都");
		} catch (Exception e) {
			res.setAlertMessage("OA发生异常：" + e);
		}
		return res;
	}

	/**
	 * 调用第三方业务系统的接口进行数据处理
	 * 
	 * @param msg
	 */
	protected void invokeHrSystem(String msg) {
		// 具体业务代码...
		System.out.println(msg);
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}
}
