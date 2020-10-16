/**
 * 
 * Author: xiaolin
 * Date: 2018年12月10日
 *
 * Copyright (C) 2018 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.govdoc.report;

import com.seeyon.ctp.report.engine.api.interfaces.AbstractTableDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.bean.FormFieldComBean;
import com.seeyon.ctp.form.util.Enums.FieldType;
import com.seeyon.ctp.form.util.Enums.MasterTableField;
import com.seeyon.ctp.form.util.infopath.ElementUtil;
import com.seeyon.ctp.form.util.infopath.ElementUtil.GovDocElement;
import com.seeyon.ctp.report.engine.api.ReportConstants;
import com.seeyon.ctp.report.engine.api.ReportConstants.AddtionProp;
import com.seeyon.ctp.report.engine.api.ReportConstants.FieldComType;
import com.seeyon.ctp.report.engine.api.bean.FieldBean;
import com.seeyon.ctp.report.engine.api.bean.TableBean;
import com.seeyon.ctp.report.engine.api.bean.TablePenetrate;
import com.seeyon.ctp.report.engine.api.bo.PenetrateInfo;

/**
 * <p>Title:公文报表数据源定义 </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: com.seeyon.apps.govdoc.report</p>
 * <p>since Seeyon V6.1</p>
 */
public class GovdocTableDataSource extends AbstractTableDataSource {
	private static Log logger = LogFactory.getLog(GovdocTableDataSource.class);
	private static final String CATEGORY_ID 				= "2379153642063218724";
	private static final Long ENTITY_ID_EDOC_SUMMARY 		= 2254143089085677790L;
	private static final Long ENTITY_ID_CTP_AFFAIR 			= 6454143089085690690L;
	private static final Long ENTITY_ID_EDOC_SUMMAY_EXTEND 	= 1354143089082670690L;
	private static final Long ENTITY_ID_ORGMEMBER 			= -1712886575165913215L;
	private static final Long ENTITY_ID_COMMENT 			= 2612886575149813215L;
//	private static final Long ENTITY_ID_SUPERVISE_LOG 		= 5612886575149819815L;
	private static final Long ENTITY_ID_RPT_WF_PROCESS 		= -7885101930376045138L;
	private static final Long ENTITY_ID_RPT_WF_MEMBER 		= -8225208778970692625L;
//	private static final Long ENTITY_ID_RPT_WF_NODE 		= -6601467700014006405L;
//	private static final Long ENTITY_ID_CTP_TEMPLATE 		= -2301467700014006215L;
	
	private static final String ID_FIELD 					= "id";
	
	private EdocApi edocApi;
	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}
	
	@Override
	public String getCategory() {
		return GovdocReportCategory.EDOC_SOURCETYPE.getKey();
	}

	@Override
	public List<TableBean> findTableList(String appCategory,
			String appCategoryId) throws BusinessException {
		List<TableBean> beans = Lists.newArrayList();
		beans.addAll(getQueryTables());
		if(AppContext.hasPlugin("wfanalysis")) {
			beans.addAll(getStatTables());
		}
		return beans;
	}
	/**
	 * <p>查询表<p>
	 * @param 
	 * @date 2018年12月20日 下午7:28:18
	 * @since V5 7.0SP2
	 * @author xiaolin
	 * @return
	 */
	private List<TableBean> getQueryTables() {
		List<TableBean> beans = Lists.newArrayList();
		TableBean bean = new TableBean();
		bean.setCategory(ApplicationCategoryEnum.edoc.name());
		bean.setCategoryId(CATEGORY_ID);
		bean.setDisplayName("公文信息表");
		bean.setEntityId(ENTITY_ID_EDOC_SUMMARY);
		bean.setEntityName("公文信息表");
		bean.setIdField(ID_FIELD);
		bean.setMaster(true);
		bean.setTableName("edoc_summary");
		Map<String, Object> syProp = new HashMap<String, Object>();
		syProp.put(AddtionProp.DefaultWhereSql.key, " and ( {0}.state in (0,1,3) and {0}.org_account_id = :org_currentUserUnitId ) ");
		syProp.put(AddtionProp.DefaultParamsSysKey.key, "org_currentUserUnitId|long");
		bean.setProperties(syProp);
		beans.add(bean);
		
		bean = new TableBean();
		bean.setCategory(ApplicationCategoryEnum.edoc.name());
		bean.setCategoryId(CATEGORY_ID);
		bean.setDisplayName("节点信息表");
		bean.setEntityId(ENTITY_ID_CTP_AFFAIR);
		bean.setEntityName("节点信息表");
		bean.setIdField(ID_FIELD);
		bean.setMaster(true);
		bean.setTableName("ctp_affair");
		Map<String, Object> afrProp = new HashMap<String, Object>();
		afrProp.put(AddtionProp.DefaultWhereSql.key, " and ({0}.app = 4 and {0}.state in (3,4) and {0}.org_account_id = :org_currentUserUnitId ) ");
		afrProp.put(AddtionProp.DefaultParamsSysKey.key, "org_currentUserUnitId|long");
		bean.setProperties(afrProp);
		beans.add(bean);
		
		bean = new TableBean();
		bean.setCategory(ApplicationCategoryEnum.edoc.name());
		bean.setCategoryId(CATEGORY_ID);
		bean.setDisplayName("用户公文元素表");
		bean.setEntityId(ENTITY_ID_EDOC_SUMMAY_EXTEND);
		bean.setEntityName("用户公文元素表");
		bean.setIdField(ID_FIELD);
		bean.setMaster(true);
		bean.setTableName("edoc_summary_extend");
		beans.add(bean);
		
		bean = new TableBean();
		bean.setCategory(ApplicationCategoryEnum.edoc.name());
		bean.setCategoryId(CATEGORY_ID);
		bean.setEntityId(ENTITY_ID_ORGMEMBER);
		bean.setEntityName("处理人信息表");
		bean.setForeignField(null);
		bean.setOwnerTableName(null);
		bean.setIdField(ID_FIELD);
		bean.setMaster(true);
		bean.setTableName("org_member");
		bean.setDisplayName("处理人信息表");
		beans.add(bean);
		
		bean = new TableBean();
		bean.setCategory(ApplicationCategoryEnum.edoc.name());
		bean.setCategoryId(CATEGORY_ID);
		bean.setEntityId(ENTITY_ID_COMMENT);
		bean.setEntityName("回复信息表");
		bean.setForeignField(null);
		bean.setOwnerTableName(null);
		bean.setIdField(ID_FIELD);
		bean.setMaster(true);
		bean.setTableName("ctp_comment_all");
		bean.setDisplayName("回复信息表");
		Map<String, Object> ctaProp = new HashMap<String, Object>();
		ctaProp.put(AddtionProp.DefaultWhereSql.key, " and ( {0}.CTYPE in (0,1) or {0}.CTYPE is null) ");
		bean.setProperties(ctaProp);
		beans.add(bean);
		
		return beans;
	}
	/**
	 * <p>统计调度表<p>
	 * @param 
	 * @date 2018年12月20日 下午7:28:30
	 * @since V5 7.0SP2
	 * @author xiaolin
	 * @return
	 */
	private List<TableBean> getStatTables() {
		TableBean rptWfAnalysisTableBean = new TableBean();
		rptWfAnalysisTableBean.setCategory(ApplicationCategoryEnum.edoc.name());
		rptWfAnalysisTableBean.setCategoryId(CATEGORY_ID);
		rptWfAnalysisTableBean.setDisplayName("流程效率分析表");
		rptWfAnalysisTableBean.setEntityId(ENTITY_ID_RPT_WF_PROCESS);
		rptWfAnalysisTableBean.setEntityName("流程效率分析表");
		rptWfAnalysisTableBean.setIdField(ID_FIELD);
		rptWfAnalysisTableBean.setMaster(true);
		rptWfAnalysisTableBean.setTableName("rpt_wf_analysis");
		Map<String, Object> wfProp = new HashMap<String, Object>();
		wfProp.put(AddtionProp.DefaultWhereSql.key, " and ( {0}.MODULE_TYPE = 4 and {0}.org_account_id = :org_currentUserUnitId ) ");
		wfProp.put(AddtionProp.DefaultParamsSysKey.key, "org_currentUserUnitId|long");
		rptWfAnalysisTableBean.setProperties(wfProp);
		
		TableBean rptWfAnalysisMemberTableBean = new TableBean();
		rptWfAnalysisMemberTableBean.setCategory(ApplicationCategoryEnum.edoc.name());
		rptWfAnalysisMemberTableBean.setCategoryId(CATEGORY_ID);
		rptWfAnalysisMemberTableBean.setEntityId(ENTITY_ID_RPT_WF_MEMBER);
		rptWfAnalysisMemberTableBean.setEntityName("人员效率分析表");
		rptWfAnalysisMemberTableBean.setIdField(ID_FIELD);
		rptWfAnalysisMemberTableBean.setMaster(true);
		rptWfAnalysisMemberTableBean.setTableName("rpt_wf_analysis_member");
		rptWfAnalysisMemberTableBean.setDisplayName("人员效率分析表");
		Map<String, Object> memProp = new HashMap<String, Object>();
		memProp.put(AddtionProp.DefaultWhereSql.key, " and ( {0}.NODE_MODULE_TYPE in (401,402,403,404) and {0}.NODE_ACCOUNT_ID = :org_currentUserUnitId ) ");
		memProp.put(AddtionProp.DefaultParamsSysKey.key, "org_currentUserUnitId|long");
		rptWfAnalysisMemberTableBean.setProperties(memProp);
		
		List<TableBean> tables = new ArrayList<TableBean>();
		tables.add(rptWfAnalysisTableBean);
		tables.add(rptWfAnalysisMemberTableBean);
		return tables;
	}

	@Override
	public List<FieldBean> findFieldList(TableBean table)
			throws BusinessException {
		//---------------------------查询数据源字段-------------------------------
		GovDocElement[] elements = ElementUtil.GovDocElement.values();
		if("edoc_summary".equals(table.getTableName())){
			return getEdocSummaryFields(elements);
		}
		if("edoc_summary_extend".equals(table.getTableName())){
			return getEdocSummaryExtendFields(elements);
		}
		if("ctp_affair".equals(table.getTableName())){
			return getCtpAffairFields();
		}
		if("org_member".equals(table.getTableName())){
			return getOrgMemberFields();
		}
		if("ctp_comment_all".equals(table.getTableName())){
			return getCtpCommetnAllFields();
		}
		//----------------------------统计数据源字段------------------------------------
		if("rpt_wf_analysis".equals(table.getTableName())){
			return getWfAnalysisFields();
		}
		if("rpt_wf_analysis_member".equals(table.getTableName())){
			return getWfAnalysisMemberFields();
		}
		return null;
	}
	
	private List<FieldBean> getWfAnalysisFields() {
		List<FieldBean> fields = Lists.newArrayList();
		FieldBean id = new FieldBean();
		id.addLong(ID_FIELD, "表ID_系统字段");
		fields.add(id);
		
		FieldBean rptYear = new FieldBean();
		rptYear.addInteger("RPT_YEAR", "年份");
		fields.add(rptYear);
		
		FieldBean rptMonth = new FieldBean();
		rptMonth.addInteger("RPT_MONTH", "月季年");
		rptMonth.setFieldComType(FieldComType.CUSTOMENUM.getComName());
		fields.add(rptMonth);
		
		FieldBean orgAccountId = new FieldBean();
		orgAccountId.addLong("ORG_ACCOUNT_ID", "模板流程发起单位");
		orgAccountId.setFieldComType(FieldComType.ACCOUNT.getComName());
		fields.add(orgAccountId);
		
		FieldBean runTime = new FieldBean();
		runTime.addDate("RUN_TIME", "数据截止时间");
		fields.add(runTime);

		FieldBean createCaseCount = new FieldBean();
		createCaseCount.addDecimal("CREATE_CASE_COUNT", "发起流程数");
		fields.add(createCaseCount);
		
		FieldBean finishCaseCount = new FieldBean();
		finishCaseCount.addDecimal("FINISH_CASE_COUNT", "结束流程数");
		fields.add(finishCaseCount);
		
		FieldBean finishOverCaseCount = new FieldBean();
		finishOverCaseCount.addDecimal("FINISH_OVER_CASE_COUNT", "超期结束流程数");
		fields.add(finishOverCaseCount);
		
//		FieldBean unfinishCaseCount = new FieldBean();
//		unfinishCaseCount.addDecimal("UNFINISH_CASE_COUNT", "当前未结束流程数");
//		fields.add(unfinishCaseCount);
//		
//		FieldBean unfinishOverCaseCount = new FieldBean();
//		unfinishOverCaseCount.addDecimal("UNFINISH_OVER_CASE_COUNT", "当前超期未结束数");
//		fields.add(unfinishOverCaseCount);
		
		FieldBean avgRunTime = new FieldBean();
		avgRunTime.addDecimal("AVG_RUN_TIME", "模板流程运行时长（平均值）");
		fields.add(avgRunTime);
		
		FieldBean sumRunTime = new FieldBean();
		sumRunTime.addDecimal("SUM_RUN_TIME", "模板流程运行时长（合计值）");
		fields.add(sumRunTime);
		
		FieldBean avgOverTime = new FieldBean();
		avgOverTime.addDecimal("AVG_OVER_TIME", "模板流程超期运行时长（平均值）");
		fields.add(avgOverTime);
		
		FieldBean sumOverTime = new FieldBean();
		sumOverTime.addDecimal("SUM_OVER_TIME", "模板流程超期运行时长（合计值）");
		fields.add(sumOverTime);
		
		FieldBean deadLine = new FieldBean();
		deadLine.addLong("DEAD_LINE", "流程期限");
		fields.add(deadLine);
		
		return fields;

	}
	
	private List<FieldBean> getWfAnalysisMemberFields() {
		List<FieldBean> fields = Lists.newArrayList();
		FieldBean id = new FieldBean();
		id.addLong(ID_FIELD, "表ID_系统字段");
		fields.add(id);
		
		FieldBean rptYear = new FieldBean();
		rptYear.addInteger("RPT_YEAR", "年份");
		fields.add(rptYear);
		
		FieldBean rptMonth = new FieldBean();
		rptMonth.addInteger("RPT_MONTH", "月季年");
		rptMonth.setFieldComType(FieldComType.CUSTOMENUM.getComName());
		fields.add(rptMonth);
		
		FieldBean runTime = new FieldBean();
		runTime.addDate("RUN_TIME", "数据截止时间");
		fields.add(runTime);
		
		FieldBean orgAccountId = new FieldBean();
		orgAccountId.addLong("ORG_ACCOUNT_ID", "模板流程发起单位");
		orgAccountId.setFieldComType(FieldComType.ACCOUNT.getComName());
		fields.add(orgAccountId);
		
		FieldBean nodePolicyId = new FieldBean();
		nodePolicyId.addString("NODE_POLICY_ID", "节点权限ID");
		fields.add(nodePolicyId);
		
		FieldBean nodePolicyName = new FieldBean();
		nodePolicyName.addString("NODE_POLICY_NAME", "节点权限显示名称");
		fields.add(nodePolicyName);
		
//		FieldBean nodeModuleType = new FieldBean();
//		nodeModuleType.addInteger("NODE_MODULE_TYPE", "节点分类");
//		nodeModuleType.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
//		fields.add(nodeModuleType);
		
		FieldBean nodeMemberId = new FieldBean();
		nodeMemberId.addLong("NODE_MEMBER_ID", "处理人ID");
		nodeMemberId.setFieldComType(FieldComType.MEMBER.getComName());
		fields.add(nodeMemberId);
		
		FieldBean nodeMemberName = new FieldBean();
		nodeMemberName.addString("NODE_MEMBER_NAME", "处理人姓名");
		fields.add(nodeMemberName);
		
		FieldBean nodeDepartmentId = new FieldBean();
		nodeDepartmentId.addLong("NODE_DEPARTMENT_ID", "处理人所属部门ID");
		nodeDepartmentId.setFieldComType(FieldComType.DEPARTMENT.getComName());
		fields.add(nodeDepartmentId);
		
		FieldBean nodeDepartmentName = new FieldBean();
		nodeDepartmentName.addString("NODE_DEPARTMENT_NAME", "处理人所属部门名称");
		fields.add(nodeDepartmentName);
		
		FieldBean finishCaseCount = new FieldBean();
		finishCaseCount.addDecimal("FINISH_CASE_COUNT", "处理流程数");
		fields.add(finishCaseCount);
		
		FieldBean finishOverCaseCount = new FieldBean();
		finishOverCaseCount.addDecimal("FINISH_OVER_CASE_COUNT", "超期处理流程数");
		fields.add(finishOverCaseCount);
		
//		FieldBean unfinishCaseCount = new FieldBean();
//		unfinishCaseCount.addDecimal("UNFINISH_CASE_COUNT", "当前未处理流程数");
//		fields.add(unfinishCaseCount);
//		
//		FieldBean unfinishOverCaseCount = new FieldBean();
//		unfinishOverCaseCount.addDecimal("UNFINISH_OVER_CASE_COUNT", "当前超期未处理数");
//		fields.add(unfinishOverCaseCount);
		
		FieldBean avgRunTime = new FieldBean();
		avgRunTime.addDecimal("AVG_RUN_TIME", "模板流程处理时长（平均值）");
		fields.add(avgRunTime);
		
		FieldBean sumRunTime = new FieldBean();
		sumRunTime.addDecimal("SUM_RUN_TIME", "模板流程处理时长（合计值）");
		fields.add(sumRunTime);
		
		FieldBean avgOverTime = new FieldBean();
		avgOverTime.addDecimal("AVG_OVER_TIME", "模板流程超期处理时长（平均值）");
		fields.add(avgOverTime);
		
		FieldBean sumOverTime = new FieldBean();
		sumOverTime.addDecimal("SUM_OVER_TIME", "模板流程超期处理时长（合计值）");
		fields.add(sumOverTime);
		
		FieldBean nodeAccountId = new FieldBean();
		nodeAccountId.addLong("NODE_ACCOUNT_ID", "处理人所属主岗单位ID");
		nodeAccountId.setFieldComType(FieldComType.ACCOUNT.getComName());
		fields.add(nodeAccountId);
		
		FieldBean nodeAccountName = new FieldBean();
		nodeAccountName.addString("NODE_ACCOUNT_NAME", "处理人所属主岗单位名称");
		fields.add(nodeAccountName);
		return fields;
	}
	

	private List<FieldBean> getCtpCommetnAllFields() {
		List<FieldBean> beans = Lists.newArrayList();
		FieldBean createId = new FieldBean();
		createId.addLong("create_id", "处理人");
		createId.setFieldComType(FieldComType.MEMBER.getComName());
		beans.add(createId);
		
		FieldBean createDate = new FieldBean();
		createDate.addDatetime("create_date", "处理时间");
		beans.add(createDate);
		
		FieldBean moduleId = new FieldBean();
		moduleId.addLong("module_id", "公文ID");
		beans.add(moduleId);
		
		FieldBean affairId = new FieldBean();
		affairId.addLong("affair_id", "节点ID");
		beans.add(affairId);
		
		FieldBean content = new FieldBean();
		content.addString("content", "处理意见");
		beans.add(content);
		
		return beans;
	}

	private List<FieldBean> getOrgMemberFields() {
		/**人员组织机构表定义*/
		FieldBean id = new FieldBean();
		id.addLong(ID_FIELD, "处理人ID");
		id.setFieldComType(FieldComType.MEMBER.getComName());
		
		FieldBean name = new FieldBean();
		name.addString("name", "处理人姓名");
		
		FieldBean deptId = new FieldBean();
		deptId.addLong("ORG_DEPARTMENT_ID", "处理人所在部门");
		deptId.setFieldComType(FieldComType.DEPARTMENT.getComName());
		
//		FieldBean accountId = new FielDBEAN();
//		ACCOUNTID.ADDLONG("ORG_ACCOUNT_ID", "所在单位ID");
//		ACCOUNTID.SETFIELDCOMTYPE(FIELDCOMTYPE.ACCOUNT.getComName());
		
		return Lists.newArrayList(id,name,deptId);
	}

	private List<FieldBean> getCtpAffairFields() {
		List<FieldBean> beans = Lists.newArrayList();
		FieldBean affairId = new FieldBean();
		affairId.addLong(ID_FIELD, "节点ID");
		beans.add(affairId);
		
		FieldBean memberId = new FieldBean();
		memberId.addLong("member_id", "处理人ID");
		memberId.setFieldComType(ReportConstants.FieldComType.MEMBER.getComName());
		beans.add(memberId);
		
		FieldBean sender = new FieldBean();
		sender.addLong("sender_id", "发起人ID");
		sender.setFieldComType(ReportConstants.FieldComType.MEMBER.getComName());
		beans.add(sender);
		
		FieldBean subject = new FieldBean();
		subject.addString("subject", "公文标题");
		beans.add(subject);
		
		FieldBean objectId = new FieldBean();
		objectId.addLong("object_id", "公文ID");
		beans.add(objectId);
		
		FieldBean createDate = new FieldBean();
		createDate.addDatetime("create_date", "公文流程发起时间");
		beans.add(createDate);
		
		FieldBean receivetime = new FieldBean();
		receivetime.addDatetime("receive_time", "处理人收到时间");
		beans.add(receivetime);
		
		FieldBean completetime = new FieldBean();
		completetime.addDatetime("complete_time", "处理人处理时间");
		beans.add(completetime);
		
		FieldBean state = new FieldBean();
		state.addString("state", "处理人处理状态");
		state.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(state);
		
		FieldBean substate = new FieldBean();
		substate.addString("sub_state", "处理人待办状态");
		substate.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(substate);
		
		FieldBean covertime = new FieldBean();
		covertime.addString("is_cover_time", "节点超期");
		covertime.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(covertime);
		
		FieldBean nodePolicy = new FieldBean();
		nodePolicy.addString("node_policy", "节点权限");
		nodePolicy.setFieldComType(ReportConstants.FieldComType.NODEPOLICY.getComName());
		beans.add(nodePolicy);
		
		FieldBean runtime = new FieldBean();
		runtime.addDecimal("run_time", "处理自然时长");
		beans.add(runtime);
		
		FieldBean runWorktime = new FieldBean();
		runWorktime.addDecimal("run_worktime", "处理工作时长");
		beans.add(runWorktime);
		
		FieldBean overtime = new FieldBean();
		overtime.addDecimal("over_time", "处理超期自然时长");
		beans.add(overtime);
		
		FieldBean overWorktime = new FieldBean();
		overWorktime.addDecimal("over_worktime", "处理超期工作时长");
		beans.add(overWorktime);
		
		FieldBean expectedProcessTime = new FieldBean();
		expectedProcessTime.addDatetime("expected_process_time", "节点期限");
		beans.add(expectedProcessTime);
		
		FieldBean accountId = new FieldBean();
		accountId.addLong("org_account_id", "处理人所在单位");
		accountId.setFieldComType(ReportConstants.FieldComType.ACCOUNT.getComName());
		beans.add(accountId);
		
		return beans;
	}

	private List<FieldBean> getEdocSummaryExtendFields(GovDocElement[] elements) {
		List<FieldBean> beans = Lists.newArrayList();
		FieldBean summaryId = new FieldBean();
		summaryId.addLong("summary_id", "公文ID");
		beans.add(summaryId);
		
		for(GovDocElement el : elements){
			FieldType ftype = el.getFieldType();
			FieldBean bean = new FieldBean();
			String name = el.getCode();
//			String display = StringUtils.isNotBlank(el.getI18nStr()) ? ResourceUtil.getString(el.getI18nStr()):name;
			String display = edocApi.getGovDocElementName(el.getI18nStr(), name);
			
			if(name.matches("string[0-9]+") || name.matches("text[0-9]+") || name.matches("decimal[0-9]+")
					|| name.matches("integer[0-9]+") || name.matches("date[0-9]+") || name.matches("list[0-9]+")){
				if(FieldType.VARCHAR.equals(ftype)){
					bean.addString(getFieldName(name), display);
				}else if(FieldType.DECIMAL.equals(ftype)){
					bean.setDbType(ReportConstants.FieldType.DECIMAL.name());
					bean.setFieldComType(ReportConstants.FieldComType.TEXT.getComName());
					bean.setName(name);
					bean.setDisplay(display);
				}else if(FieldType.TIMESTAMP.equals(ftype)){
					bean.addDatetime(name, display);
				}
				beans.add(bean);
			}
		}
		return beans;
	}

	/**
	 * <p>Title:获取数据库字段名称 </p>
	 * <p>Company: seeyon.com</p>
	 * <p>author : fucz</p>
	 * <p>since V5 7.1 2019</p>
	 * @param name
	 * @return
	 */
	private String getFieldName(String name) {
		if(name.matches("string[0-9]+")){
			name = name.replace("string", "avarchar");
		}
		return name;
	}

	private List<FieldBean> getEdocSummaryFields(GovDocElement[] elements) {
		List<FieldBean> beans = Lists.newArrayList();
		//过滤字段
		List<GovDocElement> fiters = Lists.newArrayList(
				ElementUtil.GovDocElement.keyword,
				ElementUtil.GovDocElement.nibanyijian
				);
		FieldBean summaryId = new FieldBean();
		summaryId.addLong(ID_FIELD, "公文ID");
		beans.add(summaryId);
		
		FieldBean edocType = new FieldBean();
		edocType.addString("edoc_type", "公文类型");
		edocType.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(edocType);
		
		FieldBean createTime = new FieldBean();
		createTime.addDatetime("create_time", "流程发起时间");
		beans.add(createTime);
		
		FieldBean startUserId = new FieldBean();
		startUserId.addLong("start_user_id", "流程发起人");
		startUserId.setFieldComType(ReportConstants.FieldComType.MEMBER.getComName());
		beans.add(startUserId);
		
		FieldBean completeTime = new FieldBean();
		completeTime.addDatetime("complete_time", "流程结束时间");
		beans.add(completeTime);
		
		//特殊的多人，数据库总存的是人员123;567;12386;这样的数据，需要自己转换显示值
		FieldBean currentNodeInfo = new FieldBean();
		currentNodeInfo.addString("current_nodes_info", "当前待办人");
		currentNodeInfo.setFieldComType(ReportConstants.FieldComType.MULTIMEMBER.getComName());
		currentNodeInfo.addProperties(AddtionProp.NeedSpecialFormat.key, true);
		beans.add(currentNodeInfo);
		
		FieldBean state = new FieldBean();
		state.addString("state", "流程状态");
		state.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(state);
		
		FieldBean runtime = new FieldBean();
		runtime.addInteger("run_time", "流程运行自然时长");
		beans.add(runtime);
		
		FieldBean runWorktime = new FieldBean();
		runWorktime.addInteger("run_worktime", "流程运行工作时长");
		beans.add(runWorktime);
		
		FieldBean covertime = new FieldBean();
		covertime.addString("is_cover_time", "流程超期");
		covertime.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(covertime);
		
		FieldBean overtime = new FieldBean();
		overtime.addInteger("over_time", "流程超期自然时长");
		beans.add(overtime);
		
		FieldBean overWorktime = new FieldBean();
		overWorktime.addInteger("over_worktime", "流程超期工作时长");
		beans.add(overWorktime);
		
		FieldBean departmentId = new FieldBean();
		departmentId.addLong("org_department_id", "流程发起部门");
		departmentId.setFieldComType(ReportConstants.FieldComType.DEPARTMENT.getComName());
		beans.add(departmentId);
		
		FieldBean accountId = new FieldBean();
		accountId.addLong("org_account_id", "流程发起单位");
		accountId.setFieldComType(ReportConstants.FieldComType.ACCOUNT.getComName());
		beans.add(accountId);
		
		FieldBean deadLineTime = new FieldBean();
		deadLineTime.addDatetime("deadline_datetime", "流程期限");
		beans.add(deadLineTime);
		
		FieldBean signDate = new FieldBean();
		signDate.addDatetime("signing_date", "签发日期");
		beans.add(signDate);
		
		FieldBean hasArchive = new FieldBean();
		hasArchive.addString("has_archive", "归档状态");
		hasArchive.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
		beans.add(hasArchive);
		
		for(GovDocElement el : elements){
			FieldType ftype = el.getFieldType();
			FieldBean bean = new FieldBean();
			String name = el.getCode();
//			String display = StringUtils.isNotBlank(el.getI18nStr()) ? ResourceUtil.getString(el.getI18nStr()):name;
			String display = edocApi.getGovDocElementName(el.getI18nStr(), name);
			FormFieldComBean.FormFieldComEnum formField = el.getFormFieldComEnum(-1);
			if(fiters.contains(el)){
				continue;
			}
			if(formField.equals(FormFieldComBean.FormFieldComEnum.EDOCFLOWDEALOPITION)){
				//过滤处理意见元素
				continue;
			}
			
			if(name.matches("string[0-9]+") || name.matches("text[0-9]+") || name.matches("decimal[0-9]+")
					|| name.matches("integer[0-9]+") || name.matches("date[0-9]+") || name.matches("list[0-9]+")){
				//按规律过滤扩展元素
				continue;
			}
			if(ElementUtil.GovDocElement.sign_mark == el){
				bean.addString("DOC_MARK2", display);
			}else if(ElementUtil.GovDocElement.createdate == el){
				bean.addDatetime("START_TIME", display);
			}else if(ElementUtil.GovDocElement.packdate == el){
				bean.addDatetime("PACK_DATE", display);
			}else if(FieldType.VARCHAR.equals(ftype)){
				if("doc_type".equals(name) || "send_type".equals(name)
						|| "secret_level".equals(name) || "urgent_level".equals(name)
						|| "keep_period".equals(name) || "unit_level".equals(name)){
					bean.addString(name, display);
					bean.setFieldComType(ReportConstants.FieldComType.CUSTOMENUM.getComName());
				}else{
					bean.addString(name, display);
				}
			}else if(FieldType.DECIMAL.equals(ftype)){
				bean.setDbType(ReportConstants.FieldType.DECIMAL.name());
				bean.setFieldComType(ReportConstants.FieldComType.TEXT.getComName());
				bean.setName(name);
				bean.setDisplay(display);
			}else if(FieldType.TIMESTAMP.equals(ftype)){
				bean.addDatetime(name, display);
			}else{
				logger.debug("字段类型不匹配="+el.toString());
				continue;
			}
			beans.add(bean);
		}
		return beans;
	}

	@Override
	public TablePenetrate getTablePenetrate(TableBean table)
			throws BusinessException {
		TablePenetrate tp = new TablePenetrate();
    	if(ENTITY_ID_EDOC_SUMMARY.longValue() == table.getEntityId().longValue() ) {
    		tp.setPropValue("dataFieldName", MasterTableField.id.getKey());
    	}else if(ENTITY_ID_CTP_AFFAIR.longValue() == table.getEntityId().longValue() ) {
    		tp.setPropValue("dataFieldName","object_id");
    	}else if(ENTITY_ID_EDOC_SUMMAY_EXTEND.longValue() == table.getEntityId().longValue() ) {
    		tp.setPropValue("dataFieldName", "summary_id");
    	}else {
    		return null;
    	}
    	tp.setCategory(table.getCategory());
    	tp.setCategoryId(table.getCategoryId());
    	tp.setEntityId(table.getEntityId());
    	tp.setEntityName(table.getEntityName());
    	List<TablePenetrate.ViewBean> views = Lists.newArrayList();
    	tp.setViewList(views);
    	return tp;
	}

	public List<String> findPenetrateFieldNames( TablePenetrate tablePenet) {
		List<String> list = Lists.newArrayList("id");
		if(ENTITY_ID_CTP_AFFAIR.longValue() == tablePenet.getEntityId().longValue() ) {
    		list = Lists.newArrayList("object_id");
    	}else if(ENTITY_ID_EDOC_SUMMAY_EXTEND.longValue() == tablePenet.getEntityId().longValue() ) {
    		list = Lists.newArrayList("summary_id");
    	}
		
        return list;
    }
	
	@Override
	public List<PenetrateInfo> getTablePenetrateInfo(
			TablePenetrate tablePenetrate, String dataId)
			throws BusinessException {
		List<PenetrateInfo> ret = new ArrayList<PenetrateInfo>();
		PenetrateInfo info = new PenetrateInfo();
		String path = AppContext.getRawRequest().getContextPath()+"/edocController.do?method=detailIFrame&openFrom=F8Reprot";
		if(StringUtils.isNotBlank(dataId)){
            path = path + "&summaryId=" + dataId;
        }else{
        	path = path + "&summaryId={moduleId}";
        }
		info.put(PenetrateInfo.baseUrl, path);
		ret.add(info);
		return ret;	
	}

	@Override
	public String getDataSourceEntityName(String entityId) {
		String entityName = "";
		if(ENTITY_ID_EDOC_SUMMARY.toString().equals(entityId)){
			entityName = "公文信息表";
		}else if(ENTITY_ID_CTP_AFFAIR.toString().equals(entityId)){
			entityName = "节点信息表";
		}else if(ENTITY_ID_EDOC_SUMMAY_EXTEND.toString().equals(entityId)){
			entityName = "用户公文元素表";
		}else if(ENTITY_ID_ORGMEMBER.toString().equals(entityId)){
			entityName = "处理人信息表";
		}else if(ENTITY_ID_COMMENT.toString().equals(entityId)){
			entityName = "回复信息表";
		}else if(ENTITY_ID_RPT_WF_MEMBER.toString().equals(entityId)){
			entityName = "流程人员表";
		}
		return entityName;
	}

}
