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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.OrgConstants.ORGENT_TYPE;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.event.AddAccountEvent;
import com.seeyon.ctp.organization.event.AddAdminMemberEvent;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.report.engine.api.ReportConstants.RptTplPlatform;
import com.seeyon.ctp.report.engine.api.bean.DataSourceType;
import com.seeyon.ctp.report.engine.api.interfaces.AbstractReportAppCategory;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.report.engine.api.manager.SystemReportInitManager;
import com.seeyon.ctp.report.engine.po.ReportDesign;
import com.seeyon.ctp.util.annotation.ListenEvent;

/**
 * <p>Title:公文报表定义 </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: com.seeyon.apps.govdoc.report</p>
 * <p>since Seeyon V6.1</p>
 */
public class GovdocReportCategory extends AbstractReportAppCategory{
	private static final Log logger = LogFactory.getLog(GovdocReportCategory.class);

	/**系统报表ID--单位公文办理情况*/
	public static Long SYSTEM_REPORTID_EDOC_ACCOUNT_BLQK 			= 2494923066698450852L;
	/**系统报表ID--部门公文办理情况*/
	public static Long SYSTEM_REPORTID_EDOC_DEPARTMENT_BLQK 		= 6654050457870615177L;
	/**系统报表ID--领导意见查询*/
	public static Long SYSTEM_REPORTID_EDOC_ACCOUNT_LDYJ 			= -6306351398045005056L;
	/**系统报表ID--单位人员效率*/
	public static Long SYSTEM_REPORTID_EDOC_ACCOUNT_RYXL 			= 8442988178728437240L;
	/**系统报表ID--部门人员效率*/
	public static Long SYSTEM_REPORTID_EDOC_DEPARTMENT_RYXL 		= 5259559524184312364L;
	/**系统报表ID--单位月度效率*/
	public static Long SYSTEM_REPORTID_EDOC_ACCOUNT_YDXL 			= -7954240039784853893L;
	/**系统报表ID--单位部门效率*/
	public static Long SYSTEM_REPORTID_EDOC_ACCOUNT_BMXL 			= -8548187392905163334L;
	/**系统报表ID--部门月度效率*/
	public static Long SYSTEM_REPORTID_EDOC_DEPARTMENT_YDXL 		= -3058876022559584487L;
	
	public static DataSourceType EDOC_SOURCETYPE = new DataSourceType("edoc", "公文");
	
	private ReportApi 					reportApi;
	private OrgManager 					orgManager;
	private SystemReportInitManager		systemReportInitManager;
	public void setReportApi(ReportApi reportApi) {
		this.reportApi = reportApi;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setSystemReportInitManager(SystemReportInitManager systemReportInitManager) {
		this.systemReportInitManager = systemReportInitManager;
	}

	@Override
	public String getAppCategory() {
		return ApplicationCategoryEnum.edoc.name();
	}

	/**
	 * <p>Title: 添加单位监听</p>
	 * <p>Company: seeyon.com</p>
	 * <p>author : fucz</p>
	 * <p>since V5 7.1 2019</p>
	 * @param evt
	 * @throws BusinessException
	 */
	@ListenEvent(event = AddAccountEvent.class)
	public void onAddAccount(AddAccountEvent evt) throws BusinessException {
	}
	@ListenEvent(event = AddAdminMemberEvent.class)
	public void onAddAccountAdmin(AddAdminMemberEvent evt) throws BusinessException {
		systemReportInitManager.initSystemReportByAccount(evt.getMember().getOrgAccountId(),evt.getMember());
		logger.info("预置公文系统报表授权成功！"+evt.getMember().getOrgAccountId());
	}
	
	@Override
	public void addSystemReportAuths(List<ReportDesign> designs) {
		try {
			List<Map<String, Object>> auths = Lists.newArrayList();
			for(ReportDesign design : designs){
				if(!ApplicationCategoryEnum.edoc.name().equals(design.getCategory())){
					continue;
				}
				List<V3xOrgRole> roles = orgManager.getRoleByCode(Role_NAME.AccountGovdocStat.name(),design.getAccountId());
				if(CollectionUtils.isNotEmpty(roles)){
					Long roleId = roles.get(0).getId();
					if(SYSTEM_REPORTID_EDOC_ACCOUNT_BLQK.longValue() == design.getSystemReportId().longValue()
							|| SYSTEM_REPORTID_EDOC_ACCOUNT_LDYJ.longValue() == design.getSystemReportId().longValue()
							|| SYSTEM_REPORTID_EDOC_ACCOUNT_RYXL.longValue() == design.getSystemReportId().longValue()
							|| SYSTEM_REPORTID_EDOC_ACCOUNT_YDXL.longValue() == design.getSystemReportId().longValue()
							|| SYSTEM_REPORTID_EDOC_ACCOUNT_BMXL.longValue() == design.getSystemReportId().longValue()){
						auths.add(getAuthInfo(design.getId(),ORGENT_TYPE.Role.ordinal(),roleId));
					}
				}
			}
			reportApi.saveDesignAuths(auths);
			logger.info("升级预置公文报表授权成功！");
		} catch (Exception e) {
			logger.error("公文预制报表授权出错",e);
		}
		
	}
	
	@Override
	public List<DataSourceType> getReportAppSourceType() {
		return Lists.newArrayList(EDOC_SOURCETYPE);
	}

	@Override
	public List<String> getAppCategoryShowDataSource(DataSourceType sourceType) {
		return Lists.newArrayList(ApplicationCategoryEnum.edoc.name());
	}
	
	@Override
	public Map<String,Boolean> getDesignShowModules(){
		Map<String,Boolean> ret = new HashMap<String,Boolean>();
		ret.put(SET_CATEGORY, false);
		ret.put(SET_SUMMARY, false);
		return ret;
	};
	@Override
	public Map<String, Object> getAppProperties() {
		Map<String, Object> props = super.getAppProperties();
		props.put(TPL_PLATFORM, String.valueOf(RptTplPlatform.PC.getKey()));
		props.put(TPL_WATER_MARK, "edoc");
		return props;
	}
}
