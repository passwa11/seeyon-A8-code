package com.seeyon.apps.govdoc.listener;

import com.seeyon.apps.govdoc.event.GovDocURLEvent;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.po.PagePermission;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.form.util.permission.factory.PermissionLoad;
import com.seeyon.ctp.form.util.permission.util.PermissionUtil;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.DataTransUtil;

public class GovdocPermissionListener {
	
	private static PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
	
	@ListenEvent(event = GovDocURLEvent.class)
	public void onPermChange(GovDocURLEvent govDoc) throws Exception {	
		String configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
		if (Integer.valueOf(govDoc.getSubApp()) == 3) {
			configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
		} else if (Integer.valueOf(govDoc.getSubApp()) == 4) {
			configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		}
		PermissionVO permission = permissionManager.getDefaultPermissionByConfigCategory(configCategory, AppContext.currentAccountId());
		PagePermission curVo = PermissionLoad.getDefaultPermission();
		if (govDoc.getFormTypeKey()!=null) {
			curVo = PermissionFatory.getPermByFormType(Integer.valueOf(govDoc.getFormTypeKey().toString()));
		}else if (Strings.isNotBlank(govDoc.getSubApp())) {			
			if (Strings.isNotBlank(govDoc.getApp()) && ApplicationCategoryEnum.edoc.getKey() == Integer.valueOf(govDoc.getApp())) {
				curVo = PermissionFatory.getPermBySubApp(govDoc.getSubApp());
				curVo.setAppName("edoc");//如果是公文的话 ,标识符需要改成公文的
				if (govDoc.getEdocSummary()!=null && govDoc.getAffairId() != null) {//拼接对象-多级会签
					EdocSummary summary = govDoc.getEdocSummary();
					curVo.setSummaryId(summary.getId());
					curVo.setProcessId(Long.valueOf(summary.getProcessId()));
					curVo.setAffairId(govDoc.getAffairId());
					curVo.setFlowPermAccountId(PermissionUtil.getFlowPermAccountId(DataTransUtil.transEdocSummary2BOAll(summary)));
					AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
					CtpAffair affair = affairManager.get(Long.valueOf(govDoc.getAffairId()));
					curVo.setWorkitemId(affair.getSubObjectId());
					curVo.setCurrentNodeId(affair.getActivityId());
					OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
					curVo.setDepartmentId(orgManager.getMemberById(AppContext.currentUserId()).getOrgDepartmentId());
				}
				curVo.setCurrentUserAccount(AppContext.currentAccountId());
				curVo.setCurrentUserName(AppContext.currentUserName());
				curVo.setCurrentUserId(AppContext.currentUserId());
				curVo.setDefaultPolicyId(permission.getName());
				curVo.setDefaultPolicyName(permission.getLabel());
			}
		}
		curVo.setDefaultPolicyName(ResourceUtil.getString(curVo.getDefaultPolicyName()));
		AppContext.getRawRequest().setAttribute("curPerm", curVo);
		AppContext.getRawRequest().setAttribute("curJsonPerm", JSONUtil.toJSONString(curVo));

	}
}
