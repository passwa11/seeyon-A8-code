package com.seeyon.apps.govdoc.controller;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.helper.GovdocExcelHelper;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.domain.EdocElement;

/**
 * 新公文元素控制器
 * 
 * @author 唐桂林	
 *
 */
public class GovdocElementController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocElementController.class);

	private GovdocElementManager govdocElementManager;
	private GovdocLogManager govdocLogManager;
	private EnumManager enumManagerNew;
	private FileToExcelManager fileToExcelManager;

	/**
	 * 公文元素列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement, Role_NAME.AccountAdministrator })
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/element/list");
		try {
			// 处理查询条件
			List<EdocElement> list = null;
			String condition = request.getParameter("condition");
			String textfield = request.getParameter("textfield");
			String statusSelect = request.getParameter("statusSelect");
			Integer startIndex = 0;
			Integer first = 0;
			Integer pageSize = 0;
			Integer listCount = 0;

			// 有条件返回的查询,带分页
			if (Strings.isNotBlank(condition)) {
				list = govdocElementManager.getEdocElementsByContidion(condition, textfield, statusSelect, 1);
			}
			// 没有条件返回的查询
			else {
				listCount = govdocElementManager.getAllEdocElementCount();
				Pagination.setRowCount(listCount);
				first = Pagination.getFirstResult();
				pageSize = Pagination.getMaxResults();
				if ((first + 1) % pageSize == 0) {
					startIndex = first / pageSize;
				} else {
					startIndex = first / pageSize + 1;
				}
				if (pageSize == 1)
					startIndex = (first + 1) / pageSize;
				list = govdocElementManager.getAllEdocElements(startIndex, pageSize);
			}

			if (Strings.isNotBlank(condition)) {
				if (("elementStatus".equals(condition)) && (Strings.isNotBlank(statusSelect))) {
					mav.addObject("statusSelect", Integer.parseInt(statusSelect));
					mav.addObject("condition", condition);
				} else {
					mav.addObject("condition", condition);
					mav.addObject("textfield", textfield);
				}
			} else {
				mav.addObject("condition", null);
			}

			mav.addObject("list", list);
			mav.addObject("canEditEdocElements", GovdocRoleHelper.canEditEdocElements());
		} catch (Exception e) {
			LOGGER.error("获取公文元素列表出错", e);
		}
		return mav;
	}

	/**
	 * 公文元素编辑界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement, Role_NAME.AccountAdministrator })
	public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/element/create");
		try {
			String idStr = request.getParameter("id");
			EdocElement bean = govdocElementManager.getEdocElement(idStr);
			List<CtpEnumBean> edocMetadata = enumManagerNew.getAllEnumType();

			Locale local = LocaleContext.getLocale(request);
			String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
			String metadataName = ResourceBundleUtil.getString(resource, local, "edoc.element.chooseMetadata");

			if (bean.getMetadataId() != null) {
				if (enumManagerNew.getEnum(bean.getMetadataId()) != null) {
					CtpEnumBean metadata = enumManagerNew.getEnum(bean.getMetadataId());
					metadataName = ResourceBundleUtil.getString(metadata.getResourceBundle(), local, metadata.getLabel());
				}
				if (Strings.isBlank(metadataName) && enumManagerNew.getEnum(bean.getMetadataId()) != null) {
					metadataName = enumManagerNew.getEnum(bean.getMetadataId()).getDescription();
				}
			}

			mav.addObject("edocMetadata", edocMetadata);
			mav.addObject("bean", bean);
			mav.addObject("metadataName", metadataName);
		} catch (Exception e) {
			LOGGER.error("公文元素编辑界面出错", e);
		}
		return mav;
	}

	/**
	 * 公文元素修改
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement, Role_NAME.AccountAdministrator })
	public ModelAndView update(HttpServletRequest request, HttpServletResponse response) {
		try {
			String idStr = request.getParameter("id");
			String status = request.getParameter("status");
			String name = request.getParameter("name");
			String metadataId = request.getParameter("metadataId");
			String metadataName = request.getParameter("metadataName");
	
			EdocElement bean = govdocElementManager.getEdocElement(idStr);
	
			if (null != status && !"".equals(status)) {
				bean.setStatus(Integer.valueOf(status));
			}
	
			if (!bean.getIsSystem()) {// 如果是系统元素，则不能更改名字，反之更改
				bean.setName(name);
			}
			if (Strings.isNotBlank(metadataId)) {
				bean.setMetadataId(Long.parseLong(metadataId));
				CtpEnumBean ctpEnum = enumManagerNew.getEnum(Long.parseLong(metadataId));
				if (ctpEnum == null) {
					String outMsg = ResourceUtil.getString("form.formenum.enums");
					String stratMsg = ResourceUtil.getString("InputField.InputSelect.enumvaluenotexist");
					response.setContentType("text/html;charset=UTF-8");
					PrintWriter pw = response.getWriter();
					pw.println("<script>");
					pw.println("alert(\"" + outMsg + metadataName + stratMsg + "\");");
					pw.println("history.back()");
					pw.println("</script>");
					return null;
				}
				updateEnumRef(ctpEnum);
			} else if (bean.getIsSystem()) {
				// 如果是系统元素，则不对metadata做任何操作，已经将系统元素的关联代码初始化到数据中。
			} else {
				bean.setMetadataId(null);
			}
			govdocElementManager.updateEdocElement(bean);
			// 记录应用日志
			User user = AppContext.getCurrentUser();
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ELEMENT_UPDATE.key(), user.getName(), bean.getName());
		} catch (Exception e) {
			LOGGER.error("公文元素修改出错", e);
		}
		
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent.parent");
	}

	/**
	 * 公文元素状态修改
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement, Role_NAME.AccountAdministrator })
	public ModelAndView changeStatus(HttpServletRequest request, HttpServletResponse response) {
		try {
			String[] sIds = request.getParameterValues("ids");
			int status = Integer.valueOf(request.getParameter("status"));
			User user = AppContext.getCurrentUser();
			if (sIds != null && sIds.length > 0) {
				for (int i = 0; i < sIds.length; i++) {
					EdocElement element = govdocElementManager.getEdocElement(sIds[i]);
					element.setStatus(status);
					govdocElementManager.updateEdocElement(element);
					if (status == 0) {
						govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ELEMENT_STOP.key(), user.getName(), element.getName());
					} else {
						govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_ELEMENT_START.key(), user.getName(), element.getName());
					}
				}
			}
	
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.print("setTimeout(function(){parent.parent.listFrame.location.href = parent.parent.listFrame.location.href;}, 10)");
			out.println("</script>");
		} catch (Exception e) {
			LOGGER.error("公文元素状态修改出错", e);
		}
		return null;
	}

	/**
	 * 将枚举以及枚举值的引用状态改为“是”
	 * @param ctpEnum
	 * @throws BusinessException
	 */
	private void updateEnumRef(CtpEnumBean ctpEnum) throws BusinessException {
		enumManagerNew.refEnum(ctpEnum);
		List<com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem> items = enumManagerNew.getAllEnumItem();
		for (com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem item : items) {
			if (null != item && null != ctpEnum && null != item.getRefEnumid()
					&& item.getRefEnumid().equals(ctpEnum.getId())) {
				enumManagerNew.updateEnumItemRef(ctpEnum.getId(), item.getId());
			}
		}
	}
	
	/**
	 * 公文元素导出Excel
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement, Role_NAME.AccountAdministrator })
	public ModelAndView exportElementToExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			String exportValue = request.getParameter("exportValue");
			String conditionValue = request.getParameter("conditionValue");
			String statusSelect = request.getParameter("statusSelectValue");
			List<EdocElement> elementList = null;
			if (null == exportValue || "".equals(exportValue)) {// 无查询条件导出
				elementList = govdocElementManager.getAllEdocElements();
			} else {// 查询条件导出,不带分页				
				elementList = govdocElementManager.getEdocElementsByContidion(exportValue, conditionValue, statusSelect, 0);
			}

			Locale local = LocaleContext.getLocale(request);
			String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
			String element_title = ResourceBundleUtil.getString(resource, local, "edoc.element.code.reflection"); // 标题
	
			DataRecord dataRecord = GovdocExcelHelper.exportEdocElement(request, elementList, element_title);
			fileToExcelManager.save(response, element_title, dataRecord);
		} catch (Exception e) {
			LOGGER.error("公文元素状态修改出错", e);
		}
		return null;
	}

	public void setGovdocElementManager(GovdocElementManager govdocElementManager) {
		this.govdocElementManager = govdocElementManager;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
		this.fileToExcelManager = fileToExcelManager;
	}

}
