package com.seeyon.v3x.edoc.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;

@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
public class EdocObjTeamController extends BaseController {
	
	private EdocObjTeamManager edocObjTeamManager;
	private OrgManager orgManager;
	private AppLogManager appLogManager;

	@Override
	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		ModelAndView mav = new ModelAndView("edoc/orgTeam/orgTeam_list_main");		
		return mav;
	}
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView("edoc/orgTeam/orgTeam_list");
		List<EdocObjTeam> list=null;
		User user=AppContext.getCurrentUser();
		list=edocObjTeamManager.findAll(user.getLoginAccount());
		
		String productEdition = (String)(SysFlag.valueOf("frontPage_showMenu").getFlag());		
		mav.addObject("productEdition",productEdition);
		mav.addObject("teamList",list);
		return mav;
	}
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id=request.getParameter("id");
		ModelAndView mav = new ModelAndView("edoc/orgTeam/edit_team_detail");
		EdocObjTeam team=edocObjTeamManager.getById(Long.parseLong(id));
		String productEdition = (String)(SysFlag.valueOf("frontPage_showMenu").getFlag());		
		mav.addObject("productEdition",productEdition);
		mav.addObject("team",team);
		mav.addObject("accountId",AppContext.getCurrentUser().getLoginAccount());
		mav.addObject("flag",request.getParameter("flag"));
		
		return mav;
	}
	
	public ModelAndView addNew(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("edoc/orgTeam/team_detail");
		String productEdition = (String)(SysFlag.valueOf("frontPage_showMenu").getFlag());		
		mav.addObject("productEdition",productEdition);
		EdocObjTeam team=new EdocObjTeam();
		mav.addObject("team",team);
		mav.addObject("accountId",AppContext.getCurrentUser().getLoginAccount());
		return mav;
	}
	public ModelAndView save(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		User user=AppContext.getCurrentUser();
		
		EdocObjTeam edocObjTeam=null;
		String idStr=request.getParameter("id");
		String comm="save";
		if(idStr==null || "".equals(idStr))
		{
			edocObjTeam=new EdocObjTeam();
			edocObjTeam.setIdIfNew();
		}
		else
		{
			edocObjTeam=edocObjTeamManager.getById(Long.parseLong(idStr));
			edocObjTeam.getEdocObjTeamMembers().clear();
			comm="upd";
		}
		bind(request, edocObjTeam);
		edocObjTeam.setSelObjsStr(request.getParameter("grantedDepartId"));
		edocObjTeam.changeTeamMember();
		
		edocObjTeam.setOrgAccountId(user.getLoginAccount());		
		
		if(edocObjTeam.getDescription()==null){edocObjTeam.setDescription("");}
		
		if("save".equals(comm))
		{
			edocObjTeamManager.save(edocObjTeam);
	         //记录应用日志
		   appLogManager.insertLog(user, AppLogAction.Edoc_OrgTeam_Create, user.getName(),edocObjTeam.getName());
		}
		else
		{
			edocObjTeamManager.update(edocObjTeam);
			//记录应用日志
	      appLogManager.insertLog(user, AppLogAction.Edoc_OrgTeam_Update, user.getName(),edocObjTeam.getName());
		}
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
	}
	public ModelAndView delete(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String [] ids=request.getParameterValues("id");
	    String idsStr="";
	    idsStr=StringUtils.join(ids,",");
		//记录应用日志
        User user=AppContext.getCurrentUser();
        for(String id : idsStr.split(",")){     
            EdocObjTeam team = edocObjTeamManager.getById(Long.parseLong(id));
            appLogManager.insertLog(user, AppLogAction.Edoc_OrgTeam_Delete, user.getName(),team.getName());
        }
		edocObjTeamManager.delete(idsStr);
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
	}
	public EdocObjTeamManager getEdocObjTeamManager() {
		return edocObjTeamManager;
	}
	public void setEdocObjTeamManager(EdocObjTeamManager edocObjTeamManager) {
		this.edocObjTeamManager = edocObjTeamManager;
	}
	public OrgManager getOrgManager() {
		return orgManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }
}
