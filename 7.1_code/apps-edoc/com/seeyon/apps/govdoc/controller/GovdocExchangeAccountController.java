package com.seeyon.apps.govdoc.controller;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.manager.GovdocExchangeAccountManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

/**
 * 新公文外部单位控制器
 * 
 * @author 唐桂林
 *
 */
public class GovdocExchangeAccountController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocExchangeAccountController.class);

	private GovdocExchangeAccountManager govdocExchangeAccountManager;
	private GovdocLogManager govdocLogManager;

	/**
	 * 外部单位列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/exchangeaccount/list");
		try {
			User user = AppContext.getCurrentUser();

			String condition = request.getParameter("condition");// 查询条件
			String textfield = request.getParameter("textfield");
			List<ExchangeAccount> list = govdocExchangeAccountManager.getExternalAccountsforPage(user.getLoginAccount(),
					condition, textfield);
			mav.addObject("list", list);
			mav.addObject("condition", condition);
		} catch (Exception e) {
			LOGGER.error("获取外部单位列表出错", e);
		}
		return mav;
	}

	/**
	 * 外部单位新建界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView addExchangeAccountPage(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("govdoc/database/exchangeaccount/create");
	}

	/**
	 * 外部单位新建保存
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView addExchangeAccount(HttpServletRequest request, HttpServletResponse response) {
		try {
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			User user = AppContext.getCurrentUser();
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			boolean flag = govdocExchangeAccountManager.containExternalAccount(name, domainId);
			if (flag) {
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert(parent.v3x.getMessage('ExchangeLang.outter_unit_name_used'));");
				out.println("history.go(-1);");
				out.println("</script>");
				return null;
			}
			govdocExchangeAccountManager.create(name, description);
			// 记录应用 日志
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OUTACCOUNT_CREATE.key(), user.getName(), name);
		} catch (Exception e) {
			LOGGER.error("外部单位新建保存出错", e);
		}
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
	}

	/**
	 * 外部单位编辑界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView editExchangeAccountPage(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/exchangeaccount/edit");
		try {
			Long id = Long.valueOf(request.getParameter("id"));
			ExchangeAccount account = govdocExchangeAccountManager.getExchangeAccount(id);
			if (null != account) {
				mav.addObject("account", account);
			}
		} catch (Exception e) {
			LOGGER.error("外部单位编辑界面出错", e);
		}
		return mav;
	}

	/**
	 * 外部单位修改
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView updateExchangeAccount(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();

			Long id = Long.valueOf(request.getParameter("id"));
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			boolean flag = govdocExchangeAccountManager.containExternalAccount(id, name, domainId);
			if (flag) {
				out.println("<script>");
				out.println("alert(parent.v3x.getMessage('ExchangeLang.outter_unit_name_used'));");
				out.println("history.go(-1);");
				out.println("</script>");
				return null;
			}

			ExchangeAccount account = govdocExchangeAccountManager.getExchangeAccount(id);
			account.setName(name);
			account.setDescription(description);
			govdocExchangeAccountManager.update(account);
			// 记录应用 日志
			User user = AppContext.getCurrentUser();
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OUTACCOUNT_UPDATE.key(), user.getName(), name);
			//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-22 start
            return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
			/*out.print("<script>");
			out.print("parent.parent.location.reload(true)");
			out.print("</script>");*/
			//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-22 end
		} catch (Exception e) {
			LOGGER.error("外部单位编辑界面出错", e);
		}
		return null;
	}

	/**
	 * 外部单位删除
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView deleteExchangeAccount(HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = AppContext.getCurrentUser();

			String[] ids = request.getParameter("id").split(",");
			for (int i = 0; i < ids.length; i++) {
				ExchangeAccount account = govdocExchangeAccountManager.getExchangeAccount(Long.valueOf(ids[i]));
				if (account != null) {
					// 记录应用 日志
					govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OUTACCOUNT_DELETE.key(), user.getName(), account.getName());
					govdocExchangeAccountManager.delete(Long.valueOf(ids[i]));
				}
			}
		} catch (Exception e) {
			LOGGER.error("外部单位编辑界面出错", e);
		}
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
	}

	public void setGovdocExchangeAccountManager(GovdocExchangeAccountManager govdocExchangeAccountManager) {
		this.govdocExchangeAccountManager = govdocExchangeAccountManager;
	}

	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}

}
