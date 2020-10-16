package com.seeyon.apps.formrelation.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.seeyon.apps.customFieldCtrl.constants.FormFieldEnum;
import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.kit.StrKit;
import com.seeyon.apps.formrelation.kit.DBKit;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.modules.engin.relation.CAP4FormRelationRecordDAO;
import com.seeyon.cap4.form.po.CAPFormRelationRecord;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.event.WorkflowEventResult;

/**
 * <pre>
 * 
 * </pre>
 * 
 * @date 2018年11月16日 下午7:13:54 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class WuliaoLingyongEvent extends AbstractWorkflowEvent {

	/**
	 * 物料档案信息表的模板编号
	 */
	private static final String MM_INFO = "meterial";

	private CAP4FormManager cap4FormManager;

	private CAP4FormRelationRecordDAO cap4FormRelationRecordDAO;

	@Override
	public WorkflowEventResult onBeforeProcessFinished(WorkflowEventData data) {
		WorkflowEventResult res = new WorkflowEventResult();
		// 获取当前表单的数据
		Map<String, Object> map = data.getBusinessData();
		// 获取领用记录
		FormDataMasterBean record = (FormDataMasterBean) map.get("formDataBean");
		FormTableBean fromtableBean = record.getFormTable();
		try {
			// 单据编号
			Object djh = CAP4FormKit.getFieldValue(record, FormFieldEnum.danjubianhao.getText());
			// 领用人
			Object lyr = CAP4FormKit.getFieldValue(record, FormFieldEnum.lingyongren.getText());
			// 所属部门
			Object dept = CAP4FormKit.getFieldValue(record, FormFieldEnum.suoshubumen.getText());
			// 领用时间
			Object lingyongshijian = CAP4FormKit.getFieldValue(record, FormFieldEnum.lingyongshijian.getText());
			
			// 具体的领用物料信息
			List<FormDataSubBean> subs = CAP4FormKit.getSubBeans(record);
			// 获取所有的物料编号
			List<String> wlbhs = subs.stream().map(item -> CAP4FormKit.getFieldStrValue(item, FormFieldEnum.wuliaobianhao.getText()))
				.collect(Collectors.toList());
			
			// 获取底表的masterbean
			FormBean bean = cap4FormManager.getFormByFormCode(MM_INFO);
			
			FormTableBean tableBean = bean.getMasterTableBean();
			// 子表, 由于只有一个，未容错处理
			FormTableBean subBean = bean.getSubTableBean().get(0);
			FormFieldBean djbh = CAP4FormKit.getFieldBean(subBean, FormFieldEnum.danjubianhao.getText());
			FormFieldBean lyrField = CAP4FormKit.getFieldBean(subBean, FormFieldEnum.lingyongren.getText());
			FormFieldBean lyBm = CAP4FormKit.getFieldBean(subBean, FormFieldEnum.suoshubumen.getText());
			FormFieldBean lysjField = CAP4FormKit.getFieldBean(subBean, FormFieldEnum.lingyongshijian.getText());
			// 领用数量
			FormFieldBean lyslField = CAP4FormKit.getFieldBean(subBean, FormFieldEnum.lingyongshuliang.getText());

			
			// 底表物料编号字段
			FormFieldBean dbwl = bean.getFieldBeanByDisplay(FormFieldEnum.wuliaobianhao.getText());
			String sql = "select id, " + dbwl.getName() + " as dbwl from " + tableBean.getTableName() +
					" where 1 = 1 and (" ;
			List<Object> params = new ArrayList<>();
			for(String bh : wlbhs) {
				sql += dbwl.getName() + " = ? or ";
				params.add(bh);
			}
			sql += " 1 = 2 ) ";
			// 查询出所有的物料编号和底表的masterbean
			List<Map<String, Object>> rows = DBKit.excuteSQL(sql, params);
			// 保存关联关系
            List<CAPFormRelationRecord> records = new ArrayList<CAPFormRelationRecord>();
			for(FormDataSubBean sub : subs) {
				String wlbh = CAP4FormKit.getFieldStrValue(sub, FormFieldEnum.wuliaobianhao.getText());
				Long masterId = 0L;
				loop1: for(Map<String, Object> row : rows) {
					// 数据库的编号
					String dbbh = StrKit.str(row.get("dbwl"));
					if(wlbh.equals(dbbh)) {
						masterId = StrKit.toLong(row.get("id"));
						break loop1;
					}
				}
				// 获取到主表
				FormDataMasterBean master = cap4FormManager.getDataMasterBeanById(masterId, bean, null);
				List<FormDataSubBean> dbsubs = master.getSubData(subBean.getTableName());
				FormDataSubBean subdata = new FormDataSubBean(subBean, master, null);
				subdata.setFormmainId(master.getId());
				subdata.addFieldValue(djbh.getName(), djh);
				subdata.addFieldValue(lyrField.getName(), lyr);
				subdata.addFieldValue(lyBm.getName(), dept);
				subdata.addFieldValue(lysjField.getName(), lingyongshijian);
				subdata.addFieldValue(lyslField.getName(), CAP4FormKit.getFieldValue(sub, FormFieldEnum.lingyongshuliang.getText()));
				// 添加到子表
				dbsubs.add(subdata);
				dbsubs = dbsubs.stream().filter(item -> 
					!StrKit.isNull(CAP4FormKit.getFieldStrValue(item, FormFieldEnum.lingyongren.getText())))
						.collect(Collectors.toList());
				master.setSubData(subBean.getTableName(), dbsubs);
				cap4FormManager.getCap4FormDataManager().insertOrUpdateMasterData(master);

				// 关联关系
				CAPFormRelationRecord relationRecord = new CAPFormRelationRecord();
				relationRecord.setIdIfNew();
				// 当前底表的id
				relationRecord.setFromMasterDataId(master.getId());
				// 跳转到的表的id 流程表示summaryid   底表是masterid
				relationRecord.setToMasterDataId(data.getSummaryId());
				// 跳转字段 单据编号
				relationRecord.setFieldName(djbh.getName());
				// 流程表示1  底表是42
				relationRecord.setFormType(1);
				relationRecord.setType(1);
				relationRecord.setMemberId(StrKit.toLong(lyr));
				// 底表
				relationRecord.setFromFormId(tableBean.getFormId());
				// 跳转的表
				relationRecord.setToFormId(fromtableBean.getFormId());
				relationRecord.setFromSubdataId(subdata.getId());
				records.add(relationRecord);
			}
			cap4FormRelationRecordDAO.insertCAPFormRelationRecords(records);
		} catch (Exception e) {
			res.setAlertMessage("后台发生异常,请联系开发人员:" + e.getMessage());
			return res;
		}
		return res;
	}

	@Override
	public String getId() {
		return "wuliaoEvent";
	}

	@Override
	public String getLabel() {
		return "物料领用存档";
	}

	public void setCap4FormManager(CAP4FormManager cap4FormManager) {
		this.cap4FormManager = cap4FormManager;
	}

	public void setCap4FormRelationRecordDAO(CAP4FormRelationRecordDAO cap4FormRelationRecordDAO) {
		this.cap4FormRelationRecordDAO = cap4FormRelationRecordDAO;
	}

}
