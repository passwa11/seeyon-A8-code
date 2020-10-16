/**
 * 
 * Author: xiaolin
 * Date: 2018年12月13日
 *
 * Copyright (C) 2018 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.govdoc.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.report.engine.api.bean.FieldBean;
import com.seeyon.ctp.report.engine.api.interfaces.CustomEnumHandler;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title:公文自定义枚举类 </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: com.seeyon.apps.govdoc.report</p>
 * <p>since Seeyon V6.1</p>
 */
public class GovdocCustomEnumHandler implements CustomEnumHandler{
	private static Log logger = LogFactory.getLog(GovdocCustomEnumHandler.class);
	private OrgManager orgManager;
	private EnumManager enumManagerNew;
	private DocApi docApi;
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}

	private static final List<ReportEnum> EDOC_TYPE = Lists.newArrayList();
	private static final List<ReportEnum> EDOC_STATE = Lists.newArrayList();
	private static final List<ReportEnum> COMMON_YN = Lists.newArrayList();
	private static final List<ReportEnum> AFFAIR_APP = Lists.newArrayList();
	private static final List<ReportEnum> AFFAIR_SUB_APP = Lists.newArrayList();
	private static final List<ReportEnum> AFFAIR_STATE = Lists.newArrayList(); 
	private static final List<ReportEnum> AFFAIR_SUB_STATE = Lists.newArrayList(); 
	private static final List<ReportEnum> IS_DELETE = Lists.newArrayList(); 
	private static final List<ReportEnum> BODY_TYPE = Lists.newArrayList(); 
	
	static{
		//---------------------edoc_summary-------------
		EDOC_TYPE.add(new ReportEnum("发文", "edocType", 0));
		EDOC_TYPE.add(new ReportEnum("收文", "edocType", 1));
		EDOC_TYPE.add(new ReportEnum("签报", "edocType", 2));
		
		EDOC_STATE.add(new ReportEnum("运行中","edocState",0));
		EDOC_STATE.add(new ReportEnum("终止","edocState",1));
		EDOC_STATE.add(new ReportEnum("结束","edocState",3));
//		EDOC_STATE.add(new ReportEnum("删除","edocState",4));
		
		COMMON_YN.add(new ReportEnum("否","coverTime",0));
		COMMON_YN.add(new ReportEnum("是","coverTime",1));
		
		//-----------------------ctp_affari--------------
		AFFAIR_APP.add(new ReportEnum("公文","app",4));
		
		AFFAIR_SUB_APP.add(new ReportEnum("新收文","subApp",2));
		AFFAIR_SUB_APP.add(new ReportEnum("老收文","subApp",20));
		AFFAIR_SUB_APP.add(new ReportEnum("交换","subApp",4));
		AFFAIR_SUB_APP.add(new ReportEnum("新发文","subApp",1));
		AFFAIR_SUB_APP.add(new ReportEnum("老发文","subApp",19));
		AFFAIR_SUB_APP.add(new ReportEnum("签报","subApp",3));
		AFFAIR_SUB_APP.add(new ReportEnum("老签报","subApp",21));
		
//		AFFAIR_STATE.add(new ReportEnum("待发","state",1));
//		AFFAIR_STATE.add(new ReportEnum("已发","state",2));
		AFFAIR_STATE.add(new ReportEnum("待办","state",3));
		AFFAIR_STATE.add(new ReportEnum("已办","state",4));
//		AFFAIR_STATE.add(new ReportEnum("取消","state",5));
//		AFFAIR_STATE.add(new ReportEnum("回退","state",6));
//		AFFAIR_STATE.add(new ReportEnum("取回","state",7));
//		AFFAIR_STATE.add(new ReportEnum("终止","state",15));
		
		AFFAIR_SUB_STATE.add(new ReportEnum("待办未读","subState",11));
		AFFAIR_SUB_STATE.add(new ReportEnum("待办已读","subState",12));
		AFFAIR_SUB_STATE.add(new ReportEnum("待办暂存待办","subState",13));
		
		BODY_TYPE.add(new ReportEnum("表单正文","bodyType",20));
	}
	
	@Override
	public String getCategory() {
		return GovdocReportCategory.EDOC_SOURCETYPE.getKey();
	}

	@Override
	public Object format(String tableName, FieldBean field,Object value) {
		String display = "";
		if("edoc_summary".equals(tableName)){
			if("edoc_type".equals(field.getName())){
				display = this.getDisplayText(EDOC_TYPE,value);
			}else if("state".equals(field.getName())){
				display = this.getDisplayText(EDOC_STATE,value);
			}else if("is_cover_time".equals(field.getName()) || "has_archive".equals(field.getName())){
				display = this.getDisplayText(COMMON_YN,value);
			}else if("current_nodes_info".equals(field.getName())){
				display = convertPendingMember(value);
			}else if("doc_type".equals(field.getName()) || "send_type".equals(field.getName())
					|| "secret_level".equals(field.getName()) || "urgent_level".equals(field.getName())
					|| "keep_period".equals(field.getName()) || "	unit_level".equals(field.getName())){
				CtpEnumItem enumItem = enumManagerNew.getEnumItem(Long.valueOf(value.toString()));
				return enumItem.getShowvalue();
			}
		}else if("ctp_affair".equals(tableName)){
			if("app".equals(field.getName())){
				display = this.getDisplayText(AFFAIR_APP,value);
			}else if("sub_app".equals(field.getName())){
				display = this.getDisplayText(AFFAIR_SUB_APP,value);
			}else if("state".equals(field.getName())){
				display = this.getDisplayText(AFFAIR_STATE,value);
			}else if("sub_state".equals(field.getName())){
				display = this.getDisplayText(AFFAIR_SUB_STATE,value);
			}else if("is_delete".equals(field.getName())){
				display = this.getDisplayText(IS_DELETE,value);
			}else if("body_type".equals(field.getName())){
				display = this.getDisplayText(BODY_TYPE,value);
			}else if("id".equals(field.getName())){
				//归档路径
				display = getDocArchivePath(value);  
			}else if("is_cover_time".equals(field.getName())){
				display = this.getDisplayText(COMMON_YN,value);
			}
		}
		
		if("RPT_TYPE".equalsIgnoreCase(field.getName())){
			display = ReportRptTypeEnum.getByValue(value.toString()).name;
		}else if("RPT_MONTH".equalsIgnoreCase(field.getName())){
			display = ReportRptMonthEnum.getByValue(Integer.valueOf(value.toString())).name;
		}else if("NODE_MODULE_TYPE".equalsIgnoreCase(field.getName()) || "MODULE_TYPE".equalsIgnoreCase(field.getName())){
			display = ReportModuleTypeEnum.getByValue(Integer.valueOf(value.toString())).name;
		}
		return display;
	}
	
	public enum ReportModuleTypeEnum{
		collaboration("协同",ApplicationCategoryEnum.collaboration.getKey()),
		form("表单",ApplicationCategoryEnum.form.getKey()),
		edoc("公文",ApplicationCategoryEnum.edoc.getKey()),
		govdocSend("发文",ApplicationCategoryEnum.govdocSend.getKey()),
		govdocRec("收文",ApplicationCategoryEnum.govdocRec.getKey()),
		govdocExchange("签文",ApplicationCategoryEnum.govdocExchange.getKey()),
		govdocSign("签报",ApplicationCategoryEnum.govdocSign.getKey())
		;
		
		private ReportModuleTypeEnum(String name, Integer value) {
			this.name = name;
			this.value = value;
		}
		
		public static ReportModuleTypeEnum getByValue(Integer value){
			ReportModuleTypeEnum[] enums = ReportModuleTypeEnum.values();
			for (ReportModuleTypeEnum _enum : enums) {
				if(_enum.value.equals(value)){
					return _enum;
				}
			}
			return null;
		}
		
		private String name;
		private Integer value;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Integer getValue() {
			return value;
		}
		public void setValue(Integer value) {
			this.value = value;
		}
	}
	
	public enum ReportRptTypeEnum{
		
		MONTH("按月","MONTH"),
		QUARTER("按季度","QUARTER"),
		HALF_OF_YEAR("按半年","HALF_OF_YEAR"),
		YEAR("按年","YEAR");
		
		private ReportRptTypeEnum(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public static ReportRptTypeEnum getByValue(String value){
			ReportRptTypeEnum[] enums = ReportRptTypeEnum.values();
			for (ReportRptTypeEnum _enum : enums) {
				if(_enum.value.equals(value)){
					return _enum;
				}
			}
			return null;
		}
		
		private String name;
		private String value;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

	public enum ReportRptMonthEnum{
		m1("一月",1),
		m2("二月",2),
		m3("三月",3),
		m4("四月",4),
		m5("五月",5),
		m6("六月",6),
		m7("七月",7),
		m8("八月",8),
		m9("九月",9),
		m10("十月",10),
		m11("十一月",11),
		m12("十二月",12),
		q16("第一季度",16),
		q17("第二季度",17),
		q18("第三季度",18),
		q19("第四季度",19),
		y14("上半年",14),
		y15("下半年",15),
		y13("全年",13);
		
		private ReportRptMonthEnum(String name, Integer value) {
			this.name = name;
			this.value = value;
		}
		
		public static ReportRptMonthEnum getByValue(Integer value){
			ReportRptMonthEnum[] enums = ReportRptMonthEnum.values();
			for (ReportRptMonthEnum _enum : enums) {
				if(_enum.value.equals(value)){
					return _enum;
				}
			}
			return null;
		}
		
		private String name;
		private Integer value;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Integer getValue() {
			return value;
		}
		public void setValue(Integer value) {
			this.value = value;
		}
	}
	
	/**
	 * <p>获取归档路径<p>
	 * @param 
	 * @date 2018年12月14日 下午4:22:28
	 * @since V5 7.0SP2
	 * @author xiaolin
	 * @return
	 */
	private String getDocArchivePath(Object value) {
		String display = "";
		try {
			List<DocResourceBO> docs = docApi.findDocResourcesBySourceId(Long.valueOf(value.toString()));
			if(Strings.isNotEmpty(docs)) {
				String frName = "";
				for(DocResourceBO bo : docs){
						bo = docApi.getDocResource(bo.getParentFrId());
					String tempFrName=bo.getFrName();
					//获取完整文字路径
					String fullPath= docApi.getPhysicalPath(bo.getLogicalPath(), "\\", false, 0);
					if (GovdocHelper.needI18n(bo.getFrType())){
						tempFrName = ResourceUtil.getString(tempFrName);
					}
					
					if(bo.getLogicalPath()!=null && bo.getLogicalPath().split("\\.").length>1){
						tempFrName=fullPath;
					}
					frName = frName + "|"+tempFrName;
				}
				display = frName.length() > 1 ? frName.substring(1) : "";
			}
		} catch (BusinessException e) {
			logger.error("获取归档路径异常...",e);
		}
		return display;
	}

	/**
	 * <p>当前待办人id转换成人员名称,这里最多存储了20个<p>
	 * @param 
	 * @date 2018年12月13日 下午5:47:01
	 * @since V5 7.0SP2
	 * @author xiaolin
	 * @return		
	*/
	private String convertPendingMember(Object value) {
		try {
			if(value != null && !value.equals("")){
				String[] memberIds = value.toString().split(";");
				String memberNames = "";
				for(String memberId : memberIds){
					V3xOrgMember member = orgManager.getMemberById(Long.valueOf(memberId));
					if(member != null){
						memberNames += member.getName()+",";
					}
				}
				if(memberNames.length() > 0){
					memberNames = memberNames.substring(0, memberNames.length()-1);
				}
				return memberNames;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	@Override
	public List<ReportEnum> values(String tableName, String fieldName,Map<String, Object> properties) {
		List<ReportEnum> reportEnum = null;
		if("edoc_summary".equals(tableName)){
			if("edoc_type".equals(fieldName)){
				reportEnum = EDOC_TYPE;
			}else if("state".equals(fieldName)){
				reportEnum = EDOC_STATE;
			}else if("is_cover_time".equals(fieldName) || "has_archive".equals(fieldName)){
				reportEnum = COMMON_YN;
			}else if("doc_type".equals(fieldName) || "send_type".equals(fieldName)
					|| "secret_level".equals(fieldName) || "urgent_level".equals(fieldName)
					|| "keep_period".equals(fieldName) || "unit_level".equals(fieldName)){
				try {
					List<CtpEnumItem> secretList = enumManagerNew.getEnumItemByProCode(EnumNameEnum.valueOf("edoc_"+fieldName));
					reportEnum = new ArrayList<ReportEnum>();
					for(CtpEnumItem item :secretList){
						ReportEnum rpEnum = new ReportEnum(item.getShowvalue(), item.getId().toString(), item.getValue());
						reportEnum.add(rpEnum);
					}
				} catch (BusinessException e) {
					logger.error(e.getMessage());
				}
			}
		}else if("ctp_affair".equals(tableName)){
			if("app".equals(fieldName)){
				reportEnum = AFFAIR_APP;
			}else if("sub_app".equals(fieldName)){
				reportEnum = AFFAIR_SUB_APP;
			}else if("state".equals(fieldName)){
				reportEnum = AFFAIR_STATE;
			}else if("is_cover_time".equals(fieldName)){
				reportEnum = COMMON_YN;
			}else if("sub_state".equals(fieldName)){
				reportEnum = AFFAIR_SUB_STATE;
			}else if("is_delete".equals(fieldName)){
				reportEnum = IS_DELETE;
			}else if("body_type".equals(fieldName)){
				reportEnum = BODY_TYPE;
			}
		}
		
		if("RPT_TYPE".equalsIgnoreCase(fieldName)){
			reportEnum = new ArrayList<CustomEnumHandler.ReportEnum>();
			ReportRptTypeEnum[] enums = ReportRptTypeEnum.values();
			for (ReportRptTypeEnum _enum : enums) {
				reportEnum.add(new ReportEnum(_enum.name, "rptType", _enum.value));
			}
		}else if("RPT_MONTH".equalsIgnoreCase(fieldName)){
			reportEnum = new ArrayList<CustomEnumHandler.ReportEnum>();
			ReportRptMonthEnum[] enums = ReportRptMonthEnum.values();
			for (ReportRptMonthEnum _enum : enums) {
				reportEnum.add(new ReportEnum(_enum.name, "rptMonth", _enum.value));
			}
		}else if("NODE_MODULE_TYPE".equalsIgnoreCase(fieldName) || "MODULE_TYPE".equalsIgnoreCase(fieldName)){
			reportEnum = new ArrayList<CustomEnumHandler.ReportEnum>();
			ReportModuleTypeEnum[] enums = ReportModuleTypeEnum.values();
			for (ReportModuleTypeEnum _enum : enums) {
				reportEnum.add(new ReportEnum(_enum.name, "moduleType", _enum.value));
			}
		}
		return reportEnum;
	}
	
	private String getDisplayText(List<ReportEnum> reportEnum,Object value){
		for(ReportEnum e : reportEnum){
			if(e.getValue().toString().equals(value.toString())){
				return e.getName();
			}
		}
		return null;
	}

}
