package com.seeyon.apps.yuwell.listener;

import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.yuwell.util.JDBCUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.event.EventTriggerMode;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormTableBean;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddCKSQBillListener extends AbstractWorkflowEvent {
	private static final Log log = LogFactory.getLog(AddCKSQBillListener.class);

	private FormManager formManager = (FormManager) AppContext
			.getBean("formManager");

	private ColManager colManager = (ColManager) AppContext
			.getBean("colManager");
	public static String billNo;
	private String crmNo;
	private String tableName;
	private String billNoField;
	private String CRM_GUIDField;
	private String CRM_GUID;
	private FormDataMasterBean col;
	private Long formId;
	private EnumManager enumManager;
	private String addCKSQBillHostName;

	public Long getFormId() {
		return this.formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public FormDataMasterBean getCol() {
		return this.col;
	}

	public void setCol(FormDataMasterBean col) {
		this.col = col;
	}

	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		billNo = billNo;
	}

	public String getCrmNo() {
		return this.crmNo;
	}

	public void setCrmNo(String crmNo) {
		this.crmNo = crmNo;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getBillNoField() {
		return this.billNoField;
	}

	public void setBillNoField(String billNoField) {
		this.billNoField = billNoField;
	}

	public String getCRM_GUIDField() {
		return this.CRM_GUIDField;
	}

	public void setCRM_GUIDField(String cRM_GUIDField) {
		this.CRM_GUIDField = cRM_GUIDField;
	}

	public String getCRM_GUID() {
		return this.CRM_GUID;
	}

	public void setCRM_GUID(String cRM_GUID) {
		this.CRM_GUID = cRM_GUID;
	}

	public String getAddCKSQBillHostName() {
		return this.addCKSQBillHostName;
	}

	public void setAddCKSQBillHostName(String addCKSQBillHostName) {
		this.addCKSQBillHostName = addCKSQBillHostName;
	}

	public String getId() {
		return "2017062317";
	}

	public String getLabel() {
		return "新增出库单";
	}

	@ListenEvent(event = CollaborationFinishEvent.class, mode = EventTriggerMode.afterCommit)
	public void onFinish(CollaborationFinishEvent event) throws Exception {
		log.info("进入出库单！！！");
		try {
			while (this.colManager.getColSummaryById(event.getSummaryId()) == null) {
				Thread.sleep(30000L);
			}
			log.info("进入流程结束后的新增出库单！");
			log.info("event.getSummaryId()==" + event.getSummaryId());
			ColSummary colSummary = this.colManager.getColSummaryById(event
					.getSummaryId());
			String sql = "SELECT templete_number FROM CTP_TEMPLATE WHERE form_appid ="
					+ colSummary.getFormAppid()
					+ " and IS_DELETE = 0 and TEMPLETE_NUMBER is not null ";
			List resultList = JDBCUtil.doQuery(sql);
			log.info("resultList的长度=" + resultList.size());
			if ((resultList == null) || (resultList.isEmpty()))
				return;
			String templateCode01 = (String) ((Map) resultList.get(0))
					.get("templete_number");
			log.info("=====templateCode01=====" + templateCode01);
			if (templateCode01 == null) {
				return;
			}
			String templateCode = templateCode01.replaceAll("[^a-z^A-Z]", "");
			log.info("=====templateCode=====" + templateCode);

			FormBean bean = this.formManager.getFormByFormCode(templateCode);
			this.col = FormService.findDataById(this.colManager
					.getColSummaryById(event.getSummaryId()).getFormRecordid()
					.longValue(), bean.getId().longValue());
			this.formId = bean.getId();
			log.info("formId的长度=" + this.formId);
			Map dataMap = this.col.getAllDataMap();
			log.info("=====dataMap=====" + dataMap);

			String info = convertToString(dataMap, bean, templateCode);

			billNo = callWs(info);
			log.info("=====调用出库单返回结果=====" + billNo);
			if (billNo.indexOf("OK") != -1) {
				String[] list = billNo.split(":");
				String str = list[1];
				String[] list01 = str.split("-");
				this.crmNo = list01[0];
				log.info("crmNo=======================" + this.crmNo);

				this.tableName = bean.getMasterTableBean().getTableName();
				log.info("=====tableName结果=====" + this.tableName);

				this.billNoField = bean.getMasterTableBean()
						.getFieldBeanByDisplay("ERP单号").getName();
				log.info("=====ERP单号=====" + this.billNoField);

				this.CRM_GUIDField = bean.getMasterTableBean()
						.getFieldBeanByDisplay("申请单号").getName();
				log.info("=====CRM_GUIDField申请单号=====" + this.CRM_GUIDField);

				this.CRM_GUID = ((String) dataMap.get(this.CRM_GUIDField));
				log.info("=====CRM_GUID结果=====" + this.CRM_GUID);
				log.info("=====新增出库单成功--节点前=====");

				log.info("*******************更新新增出库单接口返回值onFinishWorkitem--操作后2018**************************");
				log.info("billNo=======================" + billNo);
				String[] list03 = billNo.split(":");
				String str1 = list03[1];
				String[] list04 = str1.split("-");
				this.crmNo = list04[0];
				log.info("crmNo1111=======================" + this.crmNo);

				log.info("=====tableName结果=====" + this.tableName);
				log.info("=====billNoFieldERP单号列名=====" + this.billNoField);
				log.info("=====CRM_GUIDField申请单号=====" + this.CRM_GUIDField);
				log.info("=====CRM_GUID结果=====" + this.CRM_GUID);
				log.info("=====formId结果=====" + this.formId);
				log.info("=====col.getId()=====" + this.col.getId());
				log.info("=====col.getStartMemberId()====="
						+ this.col.getStartMemberId());
				this.col.addFieldValue(this.billNoField, this.crmNo);
				try {
					FormService.saveOrUpdateFormData(this.col, this.formId);
					log.info("=====执行对象更新结束=====");
				} catch (BusinessException e) {
					log.info("=====执行对象更新失败=====");

					e.printStackTrace();
				} catch (SQLException e) {
					log.info("=====执行对象更新失败=====");

					e.printStackTrace();
				}
				log.info("*******************更新新增出库单ERP单号结束2018**************************");
				return;
			}
			FormBean formBean = this.formManager.getFormByFormCode("CKDBCRZ");
			if (formBean != null) {
				String table_name = formBean.getMasterTableBean()
						.getTableName();
				log.info("********获取对应表单名称："
						+ formBean.getMasterTableBean().getTableName());

				int idNo = (int) System.currentTimeMillis();

				FormFieldBean formFieldBean01 = formBean
						.getFieldBeanByDisplay("单号");
				String danhao_tb = formFieldBean01.getName();
				String OA_danhao = bean.getMasterTableBean()
						.getFieldBeanByDisplay("申请单号").getName();
				log.info("=====OA_danhao申请单号对于字段=====" + OA_danhao);
				String danhao = (String) dataMap.get(OA_danhao);

				FormFieldBean formFieldBean03 = formBean
						.getFieldBeanByDisplay("时间");
				String shijian_tb = formFieldBean03.getName();
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String shijian = sdf.format(d);

				FormFieldBean formFieldBean04 = formBean
						.getFieldBeanByDisplay("操作人");
				String caozuoren_tb = formFieldBean04.getName();
				Long startMemberId = (Long) dataMap.get("start_member_id");
				String caozuoren = "";
				log.info("*********startMemberId********=" + startMemberId);
				String sql02 = " SELECT NAME FROM ORG_MEMBER WHERE id = "
						+ startMemberId;
				List resultList01 = JDBCUtil.doQuery(sql02);
				if (resultList01.size() > 0) {
					caozuoren = (String) ((Map) resultList01.get(0))
							.get("name");
				}

				FormFieldBean formFieldBean05 = formBean
						.getFieldBeanByDisplay("报错信息");
				String baocuoxinxi_tb = formFieldBean05.getName();
				String baocuoxinxi = billNo;

				FormFieldBean formFieldBean06 = formBean
						.getFieldBeanByDisplay("处理状态");
				String chulizhuangtai_tb = formFieldBean06.getName();
				String chulizhuangtai = "未处理";

				log.info("********单号字段=" + danhao_tb
						+ "***********************");
				log.info("********时间字段=" + shijian_tb
						+ "***********************");
				log.info("********操作人字段=" + caozuoren_tb
						+ "***********************");
				log.info("********报错信息字段=" + baocuoxinxi_tb
						+ "***********************");
				log.info("********处理状态字段=" + chulizhuangtai_tb
						+ "***********************");
				log.info("********id=" + idNo + "***********************");
				log.info("********单号=" + danhao + "***********************");
				log.info("********时间=" + shijian + "***********************");
				log.info("********操作人=" + caozuoren + "***********************");
				log.info("********报错信息=" + baocuoxinxi
						+ "***********************");
				log.info("********处理状态=" + chulizhuangtai
						+ "***********************");

				String sql01 = "insert into " + table_name + "(id," + danhao_tb
						+ "," + shijian_tb + "," + caozuoren_tb + ","
						+ baocuoxinxi_tb + "," + chulizhuangtai_tb
						+ ") values (" + idNo + ",'" + danhao + "','" + shijian
						+ "','" + caozuoren + "','" + baocuoxinxi + "','"
						+ chulizhuangtai + "') ";
				System.out.println("sql01========" + sql01);
				log.info("sql01========" + sql01);
				int num = JDBCUtil.doUpdateOrInsert(sql01);
				System.out.println("num========" + num);
				log.info("num========" + num);
				if (num > 0) {
					log.info("出库单报错信息插入表成功");
					return;
				}
				log.info("出库单报错信息插入表失败");

				return;
			}
			log.info("********没有对应的错误记录表***********************");
		} catch (Exception e) {
			e.printStackTrace();
			log.info("=====新增出库单接口异常=====:" + e);
		}
	}

	private String convertToString(Map<String, Object> dataMap, FormBean bean,
			String templateCode) throws Exception {
		log.info("*********进入convertToString********");
		String CRM_GUID = "";
		String FCRM_SupplyID = "";
		Date FDate = new Date();
		String FLinker = "";
		String FLinkPhone = "";
		String FLinkAddress = "";
		String FSmessagerID = "";
		String FOutType = "";
		String FCRM_NO = "";
		String FCustName = "";
		String FYSType = "";
		String FSPEmpName = "";
		Long approveId = (Long) dataMap.get("approve_member_id");
		log.info("*********approveId********=" + approveId);
		String sql = " SELECT NAME FROM ORG_MEMBER WHERE id = " + approveId;
		List resultList = JDBCUtil.doQuery(sql);
		log.info("*********resultList********=" + resultList);
		if (resultList.size() > 0) {
			FSPEmpName = (String) ((Map) resultList.get(0)).get("name");
		}
		log.info("*********FSPEmpName********=" + FSPEmpName);
		List K3ItemID = new ArrayList();
		List FQty = new ArrayList();
		String FBatchNO = "";
		List FPrice = new ArrayList();
		String FNote = "";
		Long enumId;
		String sql01;
		List resultList01;
		if (("zdcxsq".equals(templateCode)) || ("bzdcxsq".equals(templateCode))) {
			log.info("*********进入********");
			CRM_GUID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			log.info("*********申请单号********=" + CRM_GUID);
			FCRM_SupplyID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("商户ID").getName());
			log.info("*********客商ID********=" + FCRM_SupplyID);
			FDate = (Date) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("提交时间").getName());
			log.info("*********提交时间********=" + FDate);
			FLinker = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收件人").getName());
			log.info("*********收件人********=" + FLinker);
			FLinkPhone = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("联系方式").getName());
			log.info("*********联系方式********=" + FLinkPhone);
			FLinkAddress = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收货地址").getName());
			log.info("*********收货地址********=" + FLinkAddress);
			FSmessagerID = "";
			FSmessagerID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请人编号").getName());

			log.info("*********申请人编号********=" + FSmessagerID);
			FOutType = "2";
			FCRM_NO = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			log.info("*********申请单号********=" + FCRM_NO);
			FCustName = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("关联经销商").getName());
			log.info("*********关联经销商名称********=" + FCustName);
			FYSType = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("运输方式").getName());
			log.info("*********运输方式代码********=" + FYSType);
			enumId = Long.valueOf(FYSType);
			sql01 = " SELECT SHOWVALUE FROM CTP_ENUM_ITEM WHERE id = " + enumId;
			resultList01 = JDBCUtil.doQuery(sql01);
			if (resultList01.size() > 0)
				FYSType = (String) ((Map) resultList01.get(0)).get("showvalue");
			else {
				FYSType = "";
			}
			log.info("*********运输方式********=" + FYSType);
			log.info("*********bean.getSubTableBean().size()********="
					+ bean.getSubTableBean().size());

			K3ItemID = (List) dataMap.get(((FormTableBean) bean
					.getSubTableBean().get(1)).getFieldBeanByDisplay("物料内码")
					.getName());
			log.info("*********物料内码********=" + K3ItemID);
			FBatchNO = "";
			FQty = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(1)).getFieldBeanByDisplay("预计数量-统采").getName());
			log.info("*********统采预计数量********=" + FQty);
			FBatchNO = "";
			FPrice = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(1)).getFieldBeanByDisplay("单价-统采").getName());
			log.info("*********单价********=" + FPrice);
			FNote = "";
		} else if (("nlpjsq".equals(templateCode))
				|| ("nlpjsq1".equals(templateCode))) {
			log.info("*********进入零配件********");
			CRM_GUID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			FCRM_SupplyID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("商户ID").getName());
			FDate = (Date) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("提交时间").getName());
			FLinker = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收件人").getName());
			FLinkPhone = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("联系方式").getName());
			FLinkAddress = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收货地址").getName());
			FSmessagerID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请人编号").getName());
			FOutType = "2";
			FCRM_NO = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			FCustName = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("关联经销商").getName());
			log.info("*********FCustName********=" + FCustName);
			FYSType = "";
			log.info("*********运输方式代码********=" + FYSType);

			K3ItemID = (List) dataMap.get(((FormTableBean) bean
					.getSubTableBean().get(0)).getFieldBeanByDisplay("物料内码")
					.getName());
			log.info("*********2017物料内码的对应字段********="
					+ ((FormTableBean) bean.getSubTableBean().get(0))
							.getFieldBeanByDisplay("物料内码").getName());
			log.info("*********2017物料内码********=" + K3ItemID);
			FBatchNO = "";
			FQty = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(0)).getFieldBeanByDisplay("预计数量-统采").getName());
			FBatchNO = "";
			FPrice = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(0)).getFieldBeanByDisplay("单价-统采").getName());
			log.info("*********单价-统采********=" + FPrice);
			FNote = "";
		} else {
			CRM_GUID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			FCRM_SupplyID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("商户ID").getName());
			FDate = (Date) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("提交时间").getName());
			FLinker = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收件人").getName());
			FLinkPhone = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("联系方式").getName());
			FLinkAddress = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("收货地址").getName());
			FSmessagerID = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请人编号").getName());
			FOutType = "2";
			FCRM_NO = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("申请单号").getName());
			FCustName = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("关联经销商").getName());
			log.info("*********FCustName********=" + FCustName);
			FYSType = (String) dataMap.get(bean.getMasterTableBean()
					.getFieldBeanByDisplay("运输方式").getName());
			log.info("*********运输方式代码********=" + FYSType);
			enumId = Long.valueOf(FYSType);
			sql01 = " SELECT SHOWVALUE FROM CTP_ENUM_ITEM WHERE id = " + enumId;
			resultList01 = JDBCUtil.doQuery(sql01);
			if (resultList01.size() > 0)
				FYSType = (String) ((Map) resultList01.get(0)).get("showvalue");
			else {
				FYSType = "";
			}
			log.info("*********运输方式********=" + FYSType);

			K3ItemID = (List) dataMap.get(((FormTableBean) bean
					.getSubTableBean().get(0)).getFieldBeanByDisplay("物料内码")
					.getName());
			log.info("*********2017物料内码的对应字段********="
					+ ((FormTableBean) bean.getSubTableBean().get(0))
							.getFieldBeanByDisplay("物料内码").getName());
			log.info("*********2017物料内码********=" + K3ItemID);
			FBatchNO = "";
			FQty = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(0)).getFieldBeanByDisplay("预计数量-统采").getName());
			FBatchNO = "";
			FPrice = (List) dataMap.get(((FormTableBean) bean.getSubTableBean()
					.get(0)).getFieldBeanByDisplay("单价-统采").getName());
			log.info("*********单价-统采********=" + FPrice);
			FNote = "";
		}
		log.info("=====开始拼接出库单XML");
		StringBuffer info = new StringBuffer();
		info.append("");
		info.append("<Bill>");

		info.append("<BillHead>");
		info.append("<CRM_GUID>" + CRM_GUID + "</CRM_GUID>");
		info.append("<FCRM_SupplyID>" + FCRM_SupplyID + "</FCRM_SupplyID>");
		SimpleDateFormat dateFormater = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		info.append("<FDate>" + dateFormater.format(FDate) + "</FDate>");
		info.append("<FLinker>" + FLinker + "</FLinker>");
		info.append("<FLinkPhone>" + FLinkPhone + "</FLinkPhone>");
		info.append("<FLinkAddress>" + FLinkAddress + "</FLinkAddress>");
		info.append("<FSmessagerID>" + FSmessagerID + "</FSmessagerID>");
		info.append("<FOutType>" + FOutType + "</FOutType>");
		info.append("<FCRM_NO>" + FCRM_NO + "</FCRM_NO>");
		info.append("<FCustName>" + FCustName + "</FCustName>");
		info.append("<FYSType>" + FYSType + "</FYSType>");
		info.append("<FSPEmpName>" + FSPEmpName + "</FSPEmpName>");
		info.append("</BillHead>");

		for (int i = 0; i < K3ItemID.size(); ++i) {
			// 这里做个判断，物料内码为空时，不传到ERP--20180403
			if((K3ItemID.get(i) != null) && (!"null".equals(K3ItemID.get(i)))){ 
				info.append("<BillEntry>");
				info.append("<K3ItemID>" + ((String) K3ItemID.get(i))+ "</K3ItemID>");
				info.append("<FQty>" + ((BigDecimal) FQty.get(i)).toString()+ "</FQty>");
				info.append("<FBatchNO>" + FBatchNO + "</FBatchNO>");
				if ((FPrice.get(i) != null) && (!("null".equals(FPrice.get(i)))))
					info.append("<FPrice>"+ ((BigDecimal) FPrice.get(i)).toString() + "</FPrice>");
				else {
					info.append("<FPrice>0</FPrice>");
				}
				info.append("<FNote>" + FNote + "</FNote>");
				info.append("</BillEntry>");
			}
		}
		info.append("</Bill>");
		log.info("=====新增出库单XML:" + info.toString());
		return info.toString();
	}

	private String callWs(String info) {
		log.info("****************进入callWs***************");
		String result = "";
		try {
			String endpoint = this.addCKSQBillHostName
					+ "/YY_CKSQ/services/CKSQBill";
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(endpoint);
			call.setOperationName("AddCKSQBill");
			call.addParameter("in0", XMLType.XSD_STRING, ParameterMode.IN);
			call.setReturnType(XMLType.XSD_STRING);
			result = (String) call.invoke(new Object[] { info });
		} catch (Exception e) {
			result = "=====新增出库单调用接口失败:请求超时！";
			e.printStackTrace();
			log.info("=====新增出库单调用接口失败:" + e);
		}
		log.info("****************进入callWs结束***************");
		return result;
	}
}