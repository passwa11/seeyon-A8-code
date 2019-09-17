package com.seeyon.apps.govdoc.listener;

import com.seeyon.apps.govdoc.manager.GovdocGenerateManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.organization.event.AddAccountEvent;
import com.seeyon.ctp.organization.event.AddAdminMemberEvent;
import com.seeyon.ctp.organization.event.DeleteDepartmentEvent;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.listener.EdocOrganizationEventListener;

public class GovdocOrganizationEventListener extends EdocOrganizationEventListener {
	
	private GovdocGenerateManager govdocGenerateManager;

	/**
	 *   新建单位时，表单公文触发的事件
	 * @param evt
	 * @throws Exception
	 */
	@ListenEvent(event=AddAccountEvent.class)
	public void onAddAccount(AddAccountEvent evt) throws Exception {
		if(!AppContext.hasPlugin("edoc")) {
    		return;
    	}
		govdocGenerateManager.generate(evt.getAccount());
	}
	
	/**
	 *   新建单位管理员时，表单公文触发的事件
	 * @param evt
	 * @throws Exception
	 */
	@ListenEvent(event=AddAdminMemberEvent.class)
	public void onAddAdminMember(AddAdminMemberEvent evt) throws Exception {
		if(!AppContext.hasPlugin("edoc")) {
    		return;
    	}
		govdocGenerateManager.generateGovform(evt.getMember());
	}
	
	/**
	 * 监听删除部门事件，从机构组中删除
	 * @param evt
	 * @throws Exception
	 */
	@ListenEvent(event=DeleteDepartmentEvent.class)
	public void onDeleteDepartment(DeleteDepartmentEvent evt) throws Exception {
		if(!AppContext.hasPlugin("edoc")) {
    		return;
    	}
		govdocGenerateManager.deleteEdocObjTeam(evt.getDept());
	}

	public void setGovdocGenerateManager(GovdocGenerateManager govdocGenerateManager) {
		this.govdocGenerateManager = govdocGenerateManager;
	}
	
}
