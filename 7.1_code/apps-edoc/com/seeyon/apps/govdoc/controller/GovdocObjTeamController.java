package com.seeyon.apps.govdoc.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocObjTeamManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;

/**
 * 新公文机构组控制器
 * 
 * @author 唐桂林
 *
 */
public class GovdocObjTeamController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocObjTeamController.class);

	private GovdocObjTeamManager govdocObjTeamManager;
	private GovdocLogManager govdocLogManager;

	/**
	 * 机构组列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/objteam/list");
		try {
			String productEdition = (String) (SysFlag.valueOf("frontPage_showMenu").getFlag());
			mav.addObject("productEdition", productEdition);
		} catch(Exception e) {
			LOGGER.error("获取机构组列表出错", e);
		}
		return mav;
	}

	/**
	 * 机构组新建界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView createObjTeam(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/objteam/create");
		try {
			String productEdition = (String) (SysFlag.valueOf("frontPage_showMenu").getFlag());
			mav.addObject("productEdition", productEdition);
			EdocObjTeam team = new EdocObjTeam();
			FlipInfo flipInfo = govdocObjTeamManager.findList(new FlipInfo(), null);
			List<EdocObjTeam> list = flipInfo.getData();
			if(list != null && list.size() >0){
				team.setSortId(list.get(list.size()-1).getSortId()+1);
			}
			mav.addObject("team", team);
			mav.addObject("accountId", AppContext.getCurrentUser().getLoginAccount());
		} catch(Exception e) {
			LOGGER.error("机构组新建出错", e);
		}
		return mav;
	}

	/**
	 * 机构组编辑界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView editObjTeam(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/objteam/edit");
		try {
			String id = request.getParameter("id");
			EdocObjTeam team = govdocObjTeamManager.getObjTeamById(Long.parseLong(id));
			String productEdition = (String) (SysFlag.valueOf("frontPage_showMenu").getFlag());
			mav.addObject("productEdition", productEdition);
			mav.addObject("team", team);
			mav.addObject("accountId", AppContext.getCurrentUser().getLoginAccount());
			mav.addObject("flag", request.getParameter("flag"));
		} catch(Exception e) {
			LOGGER.error("机构组编辑出错", e);
		}		
		return mav;
	}

	/**
	 * 机构组保存
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView saveObjTeam(HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = AppContext.getCurrentUser();
			
			EdocObjTeam edocObjTeam = null;
			String idStr = request.getParameter("id");
			String comm = "save";
			if (idStr == null || "".equals(idStr)) {
				edocObjTeam = new EdocObjTeam();
				edocObjTeam.setIdIfNew();
			} else {
				edocObjTeam = govdocObjTeamManager.getObjTeamById(Long.parseLong(idStr));
				edocObjTeam.getEdocObjTeamMembers().clear();
				comm = "upd";
			}
			bind(request, edocObjTeam);
			edocObjTeam.setSelObjsStr(request.getParameter("grantedDepartId"));
			edocObjTeam.changeTeamMember();

			edocObjTeam.setOrgAccountId(user.getLoginAccount());

			if (edocObjTeam.getDescription() == null) {
				edocObjTeam.setDescription("");
			}

			if ("save".equals(comm)) {
				govdocObjTeamManager.saveObjTeam(edocObjTeam);
				// 记录应用日志
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ORGTEAM_CREATE.key(), user.getName(), edocObjTeam.getName());
			} else {
				govdocObjTeamManager.updateObjTeam(edocObjTeam);
				// 记录应用日志
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ORGTEAM_UPDATE.key(), user.getName(), edocObjTeam.getName());
			}
		} catch(Exception e) {
			LOGGER.error("机构组保存出错", e);
		}
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
	}

	/**
	 * 机构组删除
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView deleteObjTeam(HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = AppContext.getCurrentUser();
		
			String[] ids = request.getParameterValues("id");
			String idsStr = StringUtils.join(ids, ",");
			for (String id : idsStr.split(",")) {
				EdocObjTeam team = govdocObjTeamManager.getObjTeamById(Long.parseLong(id));
				// 记录应用日志
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ORGTEAM_DELETE.key(), user.getName(), team.getName());
			}
			govdocObjTeamManager.deleteObjTeam(idsStr);
		} catch (Exception e) {
			LOGGER.error("机构组删除出错", e);
		}
		return super.redirectModelAndView("/govdoc/objteam.do?method=list");
		//return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
	}

	public void setGovdocObjTeamManager(GovdocObjTeamManager govdocObjTeamManager) {
		this.govdocObjTeamManager = govdocObjTeamManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
}
