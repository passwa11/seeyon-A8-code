//package com.seeyon.apps.ext.zMember.util;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.seeyon.apps.fandi.util.JDBCUtil;
//import com.seeyon.ctp.common.AppContext;
//import com.seeyon.ctp.common.appLog.manager.AppLogManager;
//import com.seeyon.ctp.common.content.affair.AffairManager;
//import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
//import com.seeyon.ctp.common.exceptions.BusinessException;
//import com.seeyon.ctp.common.template.manager.TemplateManager;
//import com.seeyon.ctp.form.bean.FormBean;
//import com.seeyon.ctp.form.bean.FormDataMasterBean;
//import com.seeyon.ctp.form.bean.FormDataSubBean;
//import com.seeyon.ctp.form.bean.FormFieldBean;
//import com.seeyon.ctp.form.modules.engin.base.formData.FormDataDAO;
//import com.seeyon.ctp.form.modules.engin.base.formData.FormDataManager;
//import com.seeyon.ctp.form.service.FormCacheManager;
//import com.seeyon.ctp.form.service.FormManager;
//import com.seeyon.ctp.form.service.V5FormService;
//import com.seeyon.ctp.organization.bo.V3xOrgAccount;
//import com.seeyon.ctp.organization.manager.OrgManager;
//import com.seeyon.ctp.util.FlipInfo;
//import com.seeyon.ctp.util.StringUtil;
//import com.seeyon.ctp.util.annotation.AjaxAccess;
//
//@AjaxAccess
//public class FdManagerImpl implements FdManager {
//
//	static final Log LOGGER = LogFactory.getLog(FdManagerImpl.class);
//	// public static final String MBBH =
//	// AppContext.getSystemProperty("jtjt.addMBBH.MBBH");
//	OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
//	private FormManager formManager = (FormManager) AppContext
//			.getBean("formManager");
//	FormCacheManager formCacheManager = (FormCacheManager) AppContext
//			.getBean("formCacheManager");
//	FormDataManager formDataManager = (FormDataManager) AppContext
//			.getBean("formDataManager");
//	AppLogManager appLogManager = (AppLogManager) AppContext
//			.getBean("appLogManager");
//	TemplateManager templateManager = (TemplateManager) AppContext
//			.getBean("templateManager");
//	EnumManager enumManager = (EnumManager) AppContext
//			.getBean("enumManagerNew");
//	FormDataDAO formDataDAO = (FormDataDAO) AppContext.getBean("formDataDAO");
//	AffairManager affairManager = (AffairManager) AppContext
//			.getBean("affairManager");
//
//	/**
//	 * 获取公司列表
//	 */
//	@SuppressWarnings("deprecation")
//	@AjaxAccess
//	public FlipInfo getCompanyData(FlipInfo info, Map<String, Object> params)
//			throws BusinessException {
//		LOGGER.info("进入数据交换--getCompanyData");
//		LOGGER.info("进入数据交换--FlipInfo========="+info);
//		LOGGER.info("进入数据交换--params=========="+params);
//		String serial_id = params.get("serial_id").toString();
//		String condition = (String) params.get("condition");
//		LOGGER.info("condition=========="+condition);
//		String queryValue = (String) params.get("queryValue");
//		LOGGER.info("queryValue=========="+queryValue);
//		LOGGER.info("serial_id====" + serial_id);
//		String sql = "SELECT * FROM bd_supplier_sync ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("companyName".equals(condition)) {
//				sql += "WHERE suppliername like '%" + queryValue+"%'";
//			} else if ("companyCode".equals(condition)) {
//				sql += "WHERE suppliercode like '%" + queryValue+"%'";
//			}
//		}
//		LOGGER.info("sql ==================" + sql);
//		List resultList = JDBCUtil.executeUpdate4Oracle04(sql);
//		LOGGER.info("resultList的长度=" + resultList.size());
//
//		//分页
//		//mysql -- begin
//		int page = (info.getPage()-1)*20;
//		int pages = info.getPage()*20;
////		String sql01 = "select * from bd_supplier_sync ";
////		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
////			if ("companyName".equals(condition)) {
////				sql01 += " WHERE companyName like '%" + queryValue+"%'";
////			} else if ("companyCode".equals(condition)) {
////				sql01 += " WHERE companyCode like '%" + queryValue+"%'";
////			}
////		}else{
////			sql01 += "WHERE 1=1 ";
////		}
////		sql01 += " ORDER BY companyCode limit "+page+",20 ";
//		//mysql -- end
//
//		//oracle -- begin
//		String sql01 = "SELECT * FROM (SELECT ROWNUM AS rowno,r.* FROM( SELECT * FROM bd_supplier_sync t ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("companyName".equals(condition)) {
//				sql01 += " WHERE suppliername like '%" + queryValue+"%'";
//			} else if ("companyCode".equals(condition)) {
//				sql01 += " WHERE suppliercode like '%" + queryValue+"%'";
//			}
//		}else{
//			sql01 += "WHERE 1=1 ";
//		}
//		sql01 += " ORDER BY t.suppliercode) r where ROWNUM <= "+pages+" ) table_alias WHERE table_alias.rowno > "+page+" ";
//		//oracle -- end
//
//		LOGGER.info("分页sql01 ==================" + sql01);
//		List resultList01 = JDBCUtil.executeUpdate4Oracle04(sql01);
//		LOGGER.info("resultList01.size()==================" + resultList01.size());
//		List<Map> rows = new ArrayList<Map>();
//
//		if ((null != resultList01) && resultList01.size() > 0) {
//			for (int i = 0; i < resultList01.size(); i++) {
//				Map<String, Object> map = new HashMap<String, Object>();
//				map.put("serial_id", serial_id);
//				map.put("companyName",  (String) ((Map) resultList01.get(i)).get("SUPPLIERNAME"));
//				map.put("companyCode",  (String) ((Map) resultList01.get(i)).get("SUPPLIERCODE"));
//				map.put("bankNumber",  (String) ((Map) resultList01.get(i)).get("BANKACCNUM"));
//				map.put("bankCode", (String) ((Map) resultList01.get(i)).get("BANKDOCNAME"));
//				rows.add(map);
//			}
//		}
//		info.setData(rows);
//		info.setTotal(resultList.size());
//		return info;
//	}
//
//	/**
//	 * 获取产品项目列表
//	 */
//	@SuppressWarnings("deprecation")
//	@AjaxAccess
//	public FlipInfo getProductProjectData(FlipInfo info, Map<String, Object> params)
//			throws BusinessException {
//		LOGGER.info("进入数据交换--getProductProjectData");
//		LOGGER.info("进入数据交换--FlipInfo========="+info);
//		LOGGER.info("进入数据交换--params=========="+params);
//		String serial_id = params.get("serial_id").toString();
//		String condition = (String) params.get("condition");
//		LOGGER.info("condition=========="+condition);
//		String queryValue = (String) params.get("queryValue");
//		LOGGER.info("queryValue=========="+queryValue);
//		String sql = "SELECT * FROM bd_cpproject_sync ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("productName".equals(condition)) {
//				sql += "WHERE name like '%" + queryValue+"%'";
//			} else if ("productCode".equals(condition)) {
//				sql += "WHERE code like '%" + queryValue+"%'";
//			}
//		}
//		LOGGER.info("sql ==================" + sql);
//		List resultList = JDBCUtil.doQuery(sql);
//		LOGGER.info("resultList的长度=" + resultList.size());
//
//		//分页
//		//mysql -- begin
//		int page = (info.getPage()-1)*20;
//		int pages = info.getPage()*20;
//		String sql01 = "select * from bd_cpproject_sync ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("productName".equals(condition)) {
//				sql01 += " WHERE name like '%" + queryValue+"%'";
//			} else if ("productCode".equals(condition)) {
//				sql01 += " WHERE code like '%" + queryValue+"%'";
//			}
//		}else{
//			sql01 += "WHERE 1=1 ";
//		}
//		sql01 += " ORDER BY code limit "+page+",20 ";
//		//mysql -- end
//
//		//oracle -- begin
////		String sql01 = "SELECT * FROM (SELECT ROWNUM AS rowno,r.* FROM( SELECT * FROM bd_cpproject_sync t ";
////		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
////			if ("productName".equals(condition)) {
////				sql01 += " WHERE name like '%" + queryValue+"%'";
////			} else if ("productCode".equals(condition)) {
////				sql01 += " WHERE code like '%" + queryValue+"%'";
////			}
////		}else{
////			sql01 += "WHERE 1=1 ";
////		}
////		sql01 += " ORDER BY t.code) r where ROWNUM <= "+pages+" ) table_alias WHERE table_alias.rowno > "+page+" ";
//		//oracle -- end
//		LOGGER.info("分页sql01 ==================" + sql01);
//		List resultList01 = JDBCUtil.doQuery(sql01);
//		LOGGER.info("resultList01.size()==================" + resultList01.size());
//		List<Map> rows = new ArrayList<Map>();
//
//		if ((null != resultList01) && resultList01.size() > 0) {
//			for (int i = 0; i < resultList01.size(); i++) {
//				Map<String, Object> map = new HashMap<String, Object>();
//				//map.put("serial_id", serial_id);
//				map.put("productName",  (String) ((Map) resultList01.get(i)).get("NAME"));
//				LOGGER.info("productName ==================" + (String) ((Map) resultList01.get(i)).get("NAME"));
//				map.put("productCode",  (String) ((Map) resultList01.get(i)).get("CODE"));
//				LOGGER.info("productCode ==================" + (String) ((Map) resultList01.get(i)).get("CODE"));
//				rows.add(map);
//			}
//		}
//		info.setData(rows);
//		LOGGER.info("rows==================" + rows);
//		info.setTotal(resultList.size());
//		return info;
//	}
//
//	/**
//	 * 获取研发项目列表
//	 */
//	@AjaxAccess
//	public FlipInfo getRDProjectData(FlipInfo info, Map<String, Object> params)
//			throws BusinessException {
//		LOGGER.info("进入数据交换--getRDProjectData");
//		LOGGER.info("进入数据交换--FlipInfo========="+info);
//		LOGGER.info("进入数据交换--params=========="+params);
//		String serial_id = params.get("serial_id").toString();
//		String condition = (String) params.get("condition");
//		LOGGER.info("condition=========="+condition);
//		String queryValue = (String) params.get("queryValue");
//		LOGGER.info("queryValue=========="+queryValue);
//		LOGGER.info("serial_id====" + serial_id);
//		String sql = "SELECT * FROM bd_yfproject_sync ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("RDName".equals(condition)) {
//				sql += "WHERE name like '%" + queryValue+"%'";
//			} else if ("RDCode".equals(condition)) {
//				sql += "WHERE code like '%" + queryValue+"%'";
//			}
//		}
//		LOGGER.info("sql ==================" + sql);
//		List resultList = JDBCUtil.executeUpdate4Oracle04(sql);
//		LOGGER.info("resultList的长度=" + resultList.size());
//
//		//分页
//		//mysql -- begin
//		int page = (info.getPage()-1)*20;
//		int pages = info.getPage()*20;
////		String sql01 = "select * from bd_yfproject_sync ";
////		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
////			if ("RDName".equals(condition)) {
////				sql01 += " WHERE name like '%" + queryValue+"%'";
////			} else if ("RDCode".equals(condition)) {
////				sql01 += " WHERE code like '%" + queryValue+"%'";
////			}
////		}else{
////			sql01 += "WHERE 1=1 ";
////		}
////		sql01 += " ORDER BY code limit "+page+",20 ";
//		//mysql -- end
//
//		//oracle -- begin
//		String sql01 = "SELECT * FROM (SELECT ROWNUM AS rowno,r.* FROM( SELECT * FROM bd_yfproject_sync t ";
//		if (!StringUtil.checkNull(condition) && !StringUtil.checkNull(queryValue)) {
//			if ("RDName".equals(condition)) {
//				sql01 += " WHERE name like '%" + queryValue+"%'";
//			} else if ("RDCode".equals(condition)) {
//				sql01 += " WHERE code like '%" + queryValue+"%'";
//			}
//		}else{
//			sql01 += "WHERE 1=1 ";
//		}
//		sql01 += " ORDER BY t.orgcode) r where ROWNUM <= "+pages+" ) table_alias WHERE table_alias.rowno > "+page+" ";
//		//oracle -- end
//
//		LOGGER.info("分页sql01 ==================" + sql01);
//		List resultList01 = JDBCUtil.executeUpdate4Oracle04(sql01);
//		LOGGER.info("resultList01.size()==================" + resultList01.size());
//		List<Map> rows = new ArrayList<Map>();
//
//		if ((null != resultList01) && resultList01.size() > 0) {
//			for (int i = 0; i < resultList01.size(); i++) {
//				Map<String, Object> map = new HashMap<String, Object>();
//				map.put("serial_id", serial_id);
//				map.put("RDName",  (String) ((Map) resultList01.get(i)).get("NAME"));
//				LOGGER.info("RDName ==================" + (String) ((Map) resultList01.get(i)).get("NAME"));
//				map.put("RDCode",  (String) ((Map) resultList01.get(i)).get("CODE"));
//				LOGGER.info("RDCode ==================" + (String) ((Map) resultList01.get(i)).get("CODE"));
//				map.put("RDCompanyName",  (String) ((Map) resultList01.get(i)).get("ORGNAME"));
//				LOGGER.info("RDCompanyName ==================" + (String) ((Map) resultList01.get(i)).get("ORGNAME"));
//				rows.add(map);
//			}
//		}
//		info.setData(rows);
//		info.setTotal(resultList.size());
//		return info;
//	}
//
//	@AjaxAccess
//	public Map<String, Object> getReturnValue(Long formId, Long recordId,
//			Map<String, Object> data, String zxmxbh) {
//		Map<String, Object> map = new HashMap();
//
//		FormBean formBean = V5FormService.getForm(formId.longValue());
//
//		FormDataMasterBean masterData = this.formManager
//				.getSessioMasterDataBean(recordId);
//		if (masterData == null) {
//			return map;
//		}
//
//		FormFieldBean field = formBean.getFieldBeanByDisplay("计划项目编号");// 获取计划项目编号field
//		FormFieldBean field2 = formBean.getFieldBeanByDisplay("专项明细编号");
//		if (field != null) {
//			Object value = data.get("xmbh");// 获取项目编号
//			map.put(field.getName(), value);
//			// FormDataBean dataBean = null;
//			masterData.addFieldValue(field.getName(), value);
//		}
//
//		if (zxmxbh != null && zxmxbh != "") {
//			map.put("zxmxbh", zxmxbh);
//			masterData.addFieldValue(field2.getName(), zxmxbh);
//		}
//
//		return map;
//	}
//
//	@AjaxAccess
//	public int setYSValue(Long formId, Long recordId, String fieldName,
//			String fieldValue) {
//
//		FormBean formBean = V5FormService.getForm(formId.longValue());
//
//		FormDataMasterBean masterData = this.formManager
//				.getSessioMasterDataBean(recordId);
//
//		masterData.addFieldValue(fieldName, fieldValue);
//
//		return 1;
//	}
//
//
//	@AjaxAccess
//	public int setSubValue(Long formId, Long recordId, String fieldName,
//			String fieldValue) {
//
//		FormBean formBean = V5FormService.getForm(formId.longValue());
//
//		FormDataMasterBean masterData = this.formManager
//				.getSessioMasterDataBean(recordId);
////		masterData.gets
//		 FormDataMasterBean formDataMasterBean = formManager.getSessioMasterDataBean(formBean.getId());
//		 List<FormDataSubBean> formDataSubBeans = formDataMasterBean.getSubData("formson_0050");
//		 for (int i = 0; i < formDataSubBeans.size(); i++) {
//			 FormDataSubBean formDataSubBean = formDataSubBeans.get(i);
//			 LOGGER.info("formDataSubBean==================" + formDataSubBean);
//			 formDataSubBean.addFieldValue(fieldName, fieldValue);
//
//		}
//		Map<String, Object> map = masterData.getSubDataMapById("formson_0050", recordId);
//		LOGGER.info("map==================" + map);
//		return 1;
//	}
//
//	@AjaxAccess
//	public Object getMasterFieldValue(Long formId, Long recordId, String display)
//			throws NumberFormatException, BusinessException {
//		FormBean formBean = V5FormService.getForm(formId.longValue());
//		FormFieldBean field = formBean.getFieldBeanByDisplay(display);
//		String fieldName = field.getName();
//		FormDataMasterBean masterData = this.formManager
//				.getSessioMasterDataBean(recordId);
//		Object value = "";
//		value = masterData.getFieldValue(fieldName);
//		return value;
//	}
//
//	@AjaxAccess
//	public Object getMasterFieldName(Long formId, String display)
//			throws NumberFormatException, BusinessException {
//		FormBean formBean = V5FormService.getForm(formId.longValue());
//		FormFieldBean field = formBean.getFieldBeanByDisplay(display);
//		if (field == null) {
//			return "";
//		}
//		String fieldName = field.getName();
//		return fieldName;
//	}
//
//	@AjaxAccess
//	public Object getUnitCode(String value) throws NumberFormatException,
//			BusinessException {
//		String unitCode = "";
//		V3xOrgAccount account = orgManager.getAccountById(Long.valueOf(value
//				.toString()));
//		unitCode = account.getCode();
//		return unitCode;
//	}
//
//}
