package com.seeyon.apps.govdoc.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.report.engine.api.ReportConstants.DesignType;
import com.seeyon.ctp.report.engine.api.manager.ReportApi;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;

public class GovdocReportManagerImpl implements GovdocReportManager {
	private ReportApi reportApi;
	private PermissionManager permissionManager;
	
	public void setReportApi(ReportApi reportApi) {
		this.reportApi = reportApi;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	
	
	@Override
	public FlipInfo findReportDesigns(FlipInfo fi, Map<String, Object> params) throws BusinessException {
		if(null == params){
			params = new HashMap<String, Object>();
		}
//		params.put("category", ApplicationCategoryEnum.edoc.name());
		params.put("createMember", AppContext.currentUserId());
		List<Map<String, Object>> designs = reportApi.findDesignWithoutAuth(params);
		List<Map<String, Object>> pageDesigns = DBAgent.memoryPaging(designs, fi);
		for(Map<String, Object> map : pageDesigns){
			String type = map.get("designType").toString();
			map.put("designType", ResourceUtil.getString(DesignType.getByName(type).getI18n()));
		}
		fi.setData(pageDesigns);
		return fi;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PermissionVO> getPermissions(Map<String, Object> params) throws BusinessException {
		if(null == params){
			params = new HashMap<String, Object>();
		}
		params.put("configCategory", ApplicationCategoryEnum.edoc.name());
		params.put("from","formRightSetting");
		FlipInfo fi = new FlipInfo();
		fi = permissionManager.getPermissions(fi, params);
		return fi.getData();
	}
	@Override
	public void removeReports(List<String> recordIds) throws BusinessException {
		List<Long> designIds = new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(recordIds)){
			for(String id : recordIds){
				designIds.add(Long.valueOf(id));
			}
			reportApi.deleteReportDesign(designIds);
		}
	}
	

}
