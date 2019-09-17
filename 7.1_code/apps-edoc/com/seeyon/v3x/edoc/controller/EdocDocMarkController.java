package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.annotation.SetContentType;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.domain.EdocInnerMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocInnerMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMarkAclManager;
import com.seeyon.v3x.edoc.manager.EdocMarkCategoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.manager.EdocMarkHistoryManager;
import com.seeyon.v3x.edoc.manager.EdocMarkManager;
import com.seeyon.v3x.edoc.manager.EdocMarkReserveManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocMarkUtil;
import com.seeyon.v3x.edoc.util.EdocMarkUtil.ReserveTypeEnum;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

import edu.emory.mathcs.backport.java.util.Collections;

public class EdocDocMarkController extends BaseController {

	private static final Log log = LogFactory.getLog(EdocDocMarkController.class);

	private EdocMarkCategoryManager edocMarkCategoryManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	private EdocMarkAclManager edocMarkAclManager;
	private EdocMarkManager edocMarksManager;
	private EdocInnerMarkDefinitionManager edocInnerMarkDefinitionManager;
	private EdocMarkReserveManager edocMarkReserveManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
	private EdocManager edocManager;
	private OrgManager orgManager;
	private AppLogManager appLogManager;
	private String jsonView;
	private AffairManager affairManager = null;
	private TemplateManager templateManager;

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
	
	public AffairManager getAffairManager() {
        return affairManager;
    }
	
	public String getJsonView() {
		return jsonView;
	}
	
	public void setJsonView(String jsonView) {
		this.jsonView = jsonView;
	}

	@Override
	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	
	
	/**------------------------ 公文文号管理Start -----------------------------**/
	
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView listMain(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/mark_list_main");
		return mav;
	}

	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/mark_list_iframe");
		User user = AppContext.getCurrentUser();
		
		String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        //当前登录单位下所属部门ID
		Long depId = user.getDepartmentId();
		List<V3xOrgDepartment> depList = orgManager.getDepartmentsByUser(user.getId());
		for(V3xOrgDepartment dep : depList){
			if(dep.getOrgAccountId().longValue()==user.getLoginAccount().longValue()){
				depId = dep.getId();
				break;
			}
		}
		//G6 V1.0 SP1后续功能_自定义签收编号_把外单位授权给自己的文号也查出来start
		List<EdocMarkModel> markNoList = edocMarkDefinitionManager.getEdocMarkDefs(user.getLoginAccount(), depId, condition, textfield);
		mav.addObject("accountId", user.getLoginAccount());
		//G6 V1.0 SP1后续功能_自定义签收编号_把外单位授权给自己的文号也查出来end
		mav.addObject("markNoList", pagenate(markNoList));
		//预留文号的title
		mav.addObject("dialogTitleUp", ResourceUtil.getString("edoc.mark.reserve.up"));
		mav.addObject("dialogTitleDown", ResourceUtil.getString("edoc.mark.reserve.down"));
		return mav;
	}
	
	/**
	 * 进入新建公文文号界面。
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView addMarkPage(HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/addMark");
		Calendar cal = Calendar.getInstance();
		Integer yearNo = cal.get(Calendar.YEAR);				
		mav.addObject("yearNo", yearNo);
		
		Long domainId = AppContext.getCurrentUser().getLoginAccount();
		List<EdocMarkCategory> categories = edocMarkCategoryManager.getEdocMarkCategories(domainId);		
		//GOV-3932 新建的大流水号有特殊字符的时候，在选择大流水号的下拉框没有显示提示字符"</td>".预览引用特殊字符的大流水号的时候页面弹出a
		for(EdocMarkCategory c : categories){
			c.setCategoryName(Strings.toHTML(c.getCategoryName()));
		}
		mav .addObject("categories", categories);
		
		return mav;
	}
	/**
	 * 进入手动输入内部文号页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView serialNoInputEntry(HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/handInputSerialNo");
		
		return mav;
		
	}
	/**
	 * 进入手动输入签收编号页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView editorialNoInputEntry(HttpServletRequest request, HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/handInputeditorialNo");
		
		return mav;
		
	}

	
	/**
	 * 新建公文文号定义。 
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView createMark(HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		User user=AppContext.getCurrentUser();
		try {
			Long domainId = user.getLoginAccount();
			String wordNo = request.getParameter("wordNo");		
			String markType=request.getParameter("markType");
			int imarkType=0;
			if(markType!=null && !"".equals(markType))	
				imarkType=Integer.parseInt(markType);
			// 验证公文字号是否重名
			Boolean flag = edocMarkDefinitionManager.containEdocMarkDefinition(wordNo, domainId,imarkType);
			if (flag &&wordNo!=null&&!"".equals(wordNo)) {
				throw new Exception("edocLang.mark_alter_word_no_used");
			}
			Integer length = 0;			
			Boolean fixedLength = false;
			if (request.getParameter("fixedLength") != null) {
				fixedLength = true;
			}
			short mode = Short.valueOf(request.getParameter("flowNoType")); //0-小流水；1-大流水
			String expression = request.getParameter("markNo");												
			Integer currentNo = Integer.valueOf(0);
			EdocMarkCategory edocMarkCategory = null;
			if (mode == Constants.EDOC_MARK_CATEGORY_BIGSTREAM) {
				Long categoryId = Long.valueOf(request.getParameter("categoryId"));
				edocMarkCategory = edocMarkCategoryManager.findById(categoryId);
				edocMarkCategory.setReadonly(true);
				//大流水的时候页面disabled,不能直接获取参数。
				currentNo=edocMarkCategory.getCurrentNo();
				edocMarkCategoryManager.updateCategory(edocMarkCategory);
			}
			else {
				Integer minNo = Integer.valueOf(request.getParameter("minNo"));
				Integer maxNo = Integer.valueOf(request.getParameter("maxNo"));
				currentNo = Integer.valueOf(request.getParameter("currentNo"));
				Boolean yearEnabled = false;
				if (request.getParameter("yearEnabled") != null) {
					yearEnabled = true;
				}
				edocMarkCategory = new EdocMarkCategory();
				edocMarkCategory.setIdIfNew();
				edocMarkCategory.setCategoryName(wordNo);
				edocMarkCategory.setMinNo(minNo);
				edocMarkCategory.setMaxNo(maxNo);
				edocMarkCategory.setCurrentNo(currentNo);
				edocMarkCategory.setYearEnabled(yearEnabled);
				edocMarkCategory.setCodeMode(Constants.EDOC_MARK_CATEGORY_SMALLSTREAM);
				edocMarkCategory.setReadonly(true);
				edocMarkCategory.setDomainId(domainId);	
				edocMarkCategoryManager.saveCategory(edocMarkCategory);
			}
			
			
			EdocMarkDefinition edocMarkDef = new EdocMarkDefinition();
			if(markType!=null&&!"".equals(markType)){
				edocMarkDef.setMarkType(Integer.parseInt(markType));
			}
			edocMarkDef.setIdIfNew();
			edocMarkDef.setWordNo(wordNo);
			edocMarkDef.setEdocMarkCategory(edocMarkCategory);			
			if (fixedLength) {
				length = String.valueOf(edocMarkCategory.getMaxNo()).length();			
			}
			edocMarkDef.setLength(length);		
			edocMarkDef.setExpression(expression);				
			edocMarkDef.setDomainId(domainId);
			edocMarkDef.setEdocMarkCategory(edocMarkCategory);
			edocMarkDef.setStatus(Constants.EDOC_MARK_DEFINITION_DRAFT);		
			
			// 保存公文文号授权信息
			String deptIds = request.getParameter("grantedDepartId");
			Set<EdocMarkAcl> markAcls = new HashSet<EdocMarkAcl>();
			if (deptIds != null && !"".equals(deptIds.trim())) {
				String[] aDeptId = deptIds.split(",");
				for (String deptId : aDeptId) {
					EdocMarkAcl edocMarkAcl = new EdocMarkAcl();
					edocMarkAcl.setIdIfNew();
					String[] bDeptId = deptId.split("\\|");
					edocMarkAcl.setAclType(bDeptId[0]);
					edocMarkAcl.setDeptId(Long.valueOf(bDeptId[1]));
					edocMarkAcl.setEdocMarkDefinition(edocMarkDef);
					markAcls.add(edocMarkAcl);
				}
			}
			edocMarkDef.setEdocMarkAcls(markAcls);
			edocMarkDefinitionManager.saveMarkDefinition(edocMarkDef);
			
//			记录应用日志，安全权限
			Calendar cal = Calendar.getInstance();
			String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 	
			EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(edocMarkDef,yearNo,currentNo);
			appLogManager.insertLog(user, AppLogAction.Edoc_Mark_Create, user.getName(),model.getMark());
     		
			return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent.parent");
		}
		catch (Exception e) {
			out.print("<script>");
			out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
			out.print("</script>");
		}
		
		
		return null;
	}
		
	/**
	 * 进入修改公文文号界面。 
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView editMarkPage(HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/editMark");
		Long id = Long.valueOf(request.getParameter("id"));
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(id);
		
		List<EdocMarkAcl> edocDocTemplateAcl = edocMarkAclManager.getMarkAclById(id);
		
		
		
		// 组装公文文号授权的相关信息
		Set<EdocMarkAcl> markAcls = markDef.getEdocMarkAcls();		
		Iterator<EdocMarkAcl> iterator = markAcls.iterator();
		StringBuilder deptIds = new StringBuilder();
		StringBuilder deptNames = new StringBuilder();
		int count = markAcls.size();
		int temp = 0;		
		while (iterator.hasNext()) {
			EdocMarkAcl acl = iterator.next();
			long entityId = acl.getDeptId();
			V3xOrgEntity orgEntity = orgManager.getEntity(acl.getAclType(), entityId);
			if (orgEntity != null) {
			    deptIds.append(entityId);
                deptNames.append(orgEntity.getName());
                
				if (temp != count - 1 ) {
					deptIds.append(",");
					deptNames.append("、");
				}
			}	
			else
			{
				edocDocTemplateAcl.remove(acl);
			}
			temp++;
		}
		mav.addObject("elements", edocDocTemplateAcl);
		Long domainId = AppContext.getCurrentUser().getLoginAccount();
		List<EdocMarkCategory> categories = edocMarkCategoryManager.getEdocMarkCategories(domainId);
		//GOV-3932 新建的大流水号有特殊字符的时候，在选择大流水号的下拉框没有显示提示字符"</td>".预览引用特殊字符的大流水号的时候页面弹出a
		for(EdocMarkCategory c : categories){
			c.setCategoryName(Strings.toHTML(c.getCategoryName()));
		}
		
		Calendar cal = Calendar.getInstance();
		Integer yearNo = cal.get(Calendar.YEAR);
		mav.addObject("yearNo", yearNo);
		mav.addObject("categories", categories);
		mav.addObject("markDef", markDef);
		mav.addObject("deptIds", deptIds);
		mav.addObject("deptNames", deptNames);
		String expression = markDef.getExpression();
		String formatA = "";		
		Boolean yearEnabled =markDef.getEdocMarkCategory()!=null? markDef.getEdocMarkCategory().getYearEnabled():false;
		if (yearEnabled) {
			formatA = expression.substring(expression.indexOf("$WORD") + 5, expression.indexOf("$YEAR"));
		}
		String formatB = ""; 
		if (yearEnabled) { 
			formatB = expression.substring(expression.indexOf("$YEAR") + 5, expression.indexOf("$NO"));
		}
		else {
			formatB = expression.substring(5, expression.indexOf("$NO"));
		}
		String formatC = expression.substring(expression.indexOf("$NO") + 3);
		mav.addObject("formatA", formatA);
		mav.addObject("formatB", formatB);
		mav.addObject("formatC", formatC);
		
		return mav;
	}
	
	/**
	 * 修改公文文号定义。 
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView updateMark(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		User user =AppContext.getCurrentUser();
		try {
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			Long id = Long.valueOf(request.getParameter("id"));
			
			String  wordNo= request.getParameter("wordNo");
			if(Strings.isNotBlank(wordNo)){
				wordNo = wordNo.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
			}
			
			short mode = Short.valueOf(request.getParameter("flowNoType")); //0-小流水；1-大流水	
			Integer currentNo = Integer.valueOf(0);
			if (mode != Constants.EDOC_MARK_CATEGORY_BIGSTREAM){
				currentNo = Integer.valueOf(request.getParameter("currentNo"));
				if(edocMarkReserveManager.checkRepeatMarkReserved(id, currentNo, currentNo)) {
					rendJavaScript(response, "alert('"+ ResourceUtil.getString("edoc.current.mark.reserved.notset")+"');");
					return null;
				}
		    }
			
			EdocMarkDefinition edocMarkDef = edocMarkDefinitionManager.getMarkDefinition(id);
			int imarkType=0;
			if(edocMarkDef!=null && edocMarkDef.getMarkType()!=null) 
				imarkType=edocMarkDef.getMarkType().intValue();
			// 验证公文字号是否重名			
			Boolean flag = edocMarkDefinitionManager.containEdocMarkDefinition(id, wordNo, domainId,imarkType);
			if (flag) {
				throw new Exception("edocLang.mark_alter_word_no_used");
			}
			Integer length = 0;		
			short oldMode = -1; //修改之前的编号方式
			if(Strings.isNotBlank(request.getParameter("oldCodeMode"))){//有可能大流水被删掉，而文号还没保存是大流水还是小流水的选项就页面跳转了
				oldMode = Short.valueOf(request.getParameter("oldCodeMode"));
			}
			String expression = request.getParameter("markNo");
			Boolean fixedLength = false;
			if (request.getParameter("fixedLength") != null) {
				fixedLength = true;
			}	
			Boolean modeChanged = false;
			if (oldMode != mode) {
				modeChanged = true;
			}
			edocMarkAclManager.deleteByDefId(id);
			
			EdocMarkCategory edocMarkCategory = null;
			if (mode == Constants.EDOC_MARK_CATEGORY_BIGSTREAM) {
				//清除预留文号
				boolean isDelRes = Boolean.valueOf(request.getParameter("delMarkReserve"));
				if(isDelRes){
					edocMarkReserveManager.deleteByMarkDefineId(id);
				}
				Long categoryId = Long.valueOf(request.getParameter("categoryId")); 
				edocMarkCategory = edocMarkCategoryManager.findById(categoryId);
				edocMarkCategory.setReadonly(true);
				//大流水的时候页面disabled,不能直接获取参数。
				currentNo = edocMarkCategory.getCurrentNo();
				if(edocMarkReserveManager.checkRepeatMarkReserved(id, currentNo, currentNo)) {
					rendJavaScript(response, "alert('"+ ResourceUtil.getString("edoc.current.mark.reserved.notset")+"');");
					return null;
				}
				edocMarkCategoryManager.updateCategory(edocMarkCategory);
			}
			else {
				Integer minNo = Integer.valueOf(request.getParameter("minNo"));
				Integer maxNo = Integer.valueOf(request.getParameter("maxNo"));
				
				Boolean yearEnabled = false;
				if (request.getParameter("yearEnabled") != null) {
					yearEnabled = true;
				}
				if (modeChanged) {
					edocMarkCategory = new EdocMarkCategory();
					edocMarkCategory.setIdIfNew();
					edocMarkCategory.setCategoryName(wordNo);
					edocMarkCategory.setMinNo(minNo);
					edocMarkCategory.setMaxNo(maxNo);
					edocMarkCategory.setCurrentNo(currentNo);
					edocMarkCategory.setYearEnabled(yearEnabled);
					edocMarkCategory.setCodeMode(Constants.EDOC_MARK_CATEGORY_SMALLSTREAM);
					edocMarkCategory.setReadonly(true);					
					edocMarkCategory.setDomainId(domainId);	
					edocMarkCategoryManager.saveCategory(edocMarkCategory);
				} else {
					edocMarkCategory = edocMarkDef.getEdocMarkCategory();
					edocMarkCategory.setMinNo(minNo);
					edocMarkCategory.setMaxNo(maxNo);
					edocMarkCategory.setCurrentNo(currentNo);
					edocMarkCategory.setYearEnabled(yearEnabled);
					edocMarkCategoryManager.updateCategory(edocMarkCategory);
				}			
			}
			edocMarkDef.setEdocMarkCategory(edocMarkCategory);
			edocMarkDef.setWordNo(wordNo);
			edocMarkDef.setExpression(expression);
			if (fixedLength) {
				length = String.valueOf(edocMarkCategory.getMaxNo()).length();			
			}
			edocMarkDef.setLength(length);
			
			// 保存公文文号授权信息
			String deptIds = request.getParameter("grantedDepartId");
			Set<EdocMarkAcl> markAcls = new HashSet<EdocMarkAcl>();
			if (deptIds != null && !"".equals(deptIds.trim())) {
				String[] aDeptId = deptIds.split(",");
				for (String deptId : aDeptId) {
					EdocMarkAcl edocMarkAcl = new EdocMarkAcl();
					edocMarkAcl.setIdIfNew();
					String[] bDeptId = deptId.split("\\|");
					edocMarkAcl.setAclType(bDeptId[0]);
					edocMarkAcl.setDeptId(Long.valueOf(bDeptId[1]));
					edocMarkAcl.setEdocMarkDefinition(edocMarkDef);
					markAcls.add(edocMarkAcl);
				}
			}		
			edocMarkDef.setEdocMarkAcls(markAcls);	
			
			//记录应用日志，安全权限
			Calendar cal = Calendar.getInstance();
			String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 	
			EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(edocMarkDef,yearNo,currentNo);
			appLogManager.insertLog(user, AppLogAction.Edoc_MarkAuthorize, user.getName(),model.getMark());
			
			edocMarkDefinitionManager.updateMarkDefinition(edocMarkDef);
			
			return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent.parent");
		} catch (Exception e) {
			rendJavaScript(response, "alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
		}
		return null;
	}
	
	// 删除公文文号定义
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView deleteMark(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String[] ids = request.getParameterValues("markDefId");
		for (int i = 0; i < ids.length; i++) {
			Long id = Long.valueOf(ids[i]);
			EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(id);
			try {				
				if (markDef.getStatus() == Constants.EDOC_MARK_DEFINITION_DRAFT) {
					edocMarkDefinitionManager.deleteMarkDefinition(markDef);
				}
				else {
					// 如果文号已被使用，则进行逻辑删除
					//markDef.setStatus(Constants.EDOC_MARK_DEFINITION_DELETED);
					edocMarkDefinitionManager.logicalDeleteMarkDefinition(markDef.getId(), Constants.EDOC_MARK_DEFINITION_DELETED);
				}
		        //记录应用日志
		        User user = AppContext.getCurrentUser();
		        Calendar cal = Calendar.getInstance();
		        String yearNo = String.valueOf(cal.get(Calendar.YEAR));     
	            EdocMarkModel model = edocMarkDefinitionManager.markDef2Mode(markDef,yearNo,1);
	            appLogManager.insertLog(user, AppLogAction.Edoc_Mark_Delete, user.getName(),model.getMark());
			}
			catch (Exception exception) {
				//如果文号已被使用，则进行逻辑删除
				//markDef.setStatus(Constants.EDOC_MARK_DEFINITION_DELETED);
				//edocMarkDefinitionManager.saveMarkDefinition(markDef);
				edocMarkDefinitionManager.logicalDeleteMarkDefinition(markDef.getId(), Constants.EDOC_MARK_DEFINITION_DELETED);
			}
		}		

		out.println("<script>");
		out.println("parent.parent.location.reload(true);");
		out.println("</script>");
		return null;		
	}
	
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView manageBigStreamIframe(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/bigStreamListIframe");
		return mav;
	}
	
	// 进入大流水号管理界面
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView manageBigStreamPage(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		Long domainId = AppContext.getCurrentUser().getLoginAccount();
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/bigStreamList");
		
//		List<EdocMarkCategory> categories = edocMarkCategoryManager.findByTypeAndDomainId(Constants.EDOC_MARK_CATEGORY_BIGSTREAM, domainId);
		List<EdocMarkCategory> categories = edocMarkCategoryManager.findByPage(Constants.EDOC_MARK_CATEGORY_BIGSTREAM, domainId); //增加分页
		
		mav.addObject("categories", categories);
				
		return mav;
	}
		
	// 进入新建大流水号界面
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView addBigStreamPage(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/addBigStream");
		return mav;
	}
		
	/**
	 * 创建公文大流水号。
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView createBigStream(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			String name = request.getParameter("name");
			boolean flag = edocMarkCategoryManager.containEdocMarkCategory(name, domainId);
			if (flag) {
				throw new Exception("edocLang.big_stream_alter_name_used");
			}
			Integer minNo = Integer.valueOf(request.getParameter("minNo"));
			Integer maxNo = Integer.valueOf(request.getParameter("maxNo"));
			Integer currentNo = Integer.valueOf(request.getParameter("currentNo"));
			Boolean yearEnabled = true;
			Integer iYearEnabled = Integer.valueOf(request.getParameter("yearEnabled"));
			if (iYearEnabled == 0) {
				yearEnabled = false;
			}
			EdocMarkCategory category = new EdocMarkCategory();
			category.setIdIfNew();
			category.setCategoryName(name);
			category.setMinNo(minNo);
			category.setMaxNo(maxNo);
			category.setCurrentNo(currentNo);
			category.setYearEnabled(yearEnabled);
			category.setReadonly(false);
			category.setCodeMode(Constants.EDOC_MARK_CATEGORY_BIGSTREAM);			
			category.setDomainId(domainId);
			edocMarkCategoryManager.saveCategory(category);
						
			out.println("<script>");
			out.println("parent.window._returnValue(\"true\");");
			out.println("parent.window._closeWin();");
			out.println("</script>");
		}
		catch (Exception e) {
			out.println("<script>");
			out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
			out.println("</script>");
		}
		
		return null;
	}
	
	// 进入修改流水号界面
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView editBigStreamPage(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/editBigStream");
		Long id = Long.valueOf(request.getParameter("id"));		
		EdocMarkCategory category = edocMarkCategoryManager.findById(id);
		category.setCategoryName(Strings.toHTML(category.getCategoryName(), false));
		mav.addObject("category", category);
		return mav;
	}
	
	/**
	 * 修改公文大流水号。
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView updateBigStream(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			Long id = Long.valueOf(request.getParameter("id"));
			String name = request.getParameter("name");
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			boolean flag = edocMarkCategoryManager.containEdocMarkCategory(id, name, domainId);
			if (flag) {
				throw new Exception("edocLang.big_stream_alter_name_used");
			}
			Integer minNo = Integer.valueOf(request.getParameter("minNo"));
			Integer maxNo = Integer.valueOf(request.getParameter("maxNo"));
			Integer currentNo = Integer.valueOf(request.getParameter("currentNo"));
			Boolean yearEnabled = true;
			
			//判断页面的yearEnabled是否为空,如果为空,那么yearEnabled在页面被disabled,即不可以修改
			String s_iYearEnabled = request.getParameter("yearEnabled");
			Integer iYearEnabled = null;
			if(null!=s_iYearEnabled){
					iYearEnabled = Integer.valueOf(s_iYearEnabled);
				if (iYearEnabled == 0) {
					yearEnabled = false;
				}
			}
			
			EdocMarkCategory category = edocMarkCategoryManager.findById(id);
			Boolean readonly = category.isReadonly();
			category.setCategoryName(name);
			category.setMinNo(minNo);
			category.setMaxNo(maxNo);
			category.setCurrentNo(currentNo);
			//添加了一个判断, null!=s_iYearEnabled,判断s_iYearEnabled是否为空,如果为空，那么就不用修改category.yearEnabled的状态
			if (!readonly && null!=s_iYearEnabled) {
				category.setYearEnabled(yearEnabled);
			}
			edocMarkCategoryManager.updateCategory(category);
					
			out.println("<script>");	
			out.println("parent.window._returnValue(\"true\");");
            out.println("parent.window._closeWin();");
			out.println("</script>");
		}
		catch (Exception e) {
			out.println("<script>");
			out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
			out.println("</script>");
		}
		return null;
	}
		
	/**
	 * 删除公文大流水号。
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView deleteBigStream(HttpServletRequest request, HttpServletResponse response)
		throws Exception {	
		response.setContentType("text/html;charset=UTF-8");
		String[] ids = request.getParameterValues("categoryId");
		PrintWriter out = response.getWriter();
		for (int i = 0; i < ids.length; i++) {
			Long id = Long.valueOf(ids[i]);
			EdocMarkCategory category = edocMarkCategoryManager.findById(id);
			if (category.isReadonly()) {
				boolean flag = edocMarkDefinitionManager.containEdocMarkDefInCategory(id);				
				if (flag) {					
					out.println("<script>");
					out.println("alert(parent.v3x.getMessage('edocLang.big_stream_alter_used','"+category.getCategoryName()+"'))");
					out.println("</script>");
					continue;
				}
				else {
					edocMarkCategoryManager.deleteCategory(id);
				}
			}
			else {
				edocMarkCategoryManager.deleteCategory(id);
			}
			
		}		
		out.println("<script>");
		out.println("parent.location.href = parent.location.href;");
		out.println("</script>");
		return null;
	}
	
	// 修改公文大流水号的下拉列表选项（新建公文文号和编辑公文文号界面）
	@SetContentType
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView changeBigStreamOptions(HttpServletRequest request, HttpServletResponse response)
		throws Exception {		
		boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
		Long domainId = AppContext.getCurrentUser().getLoginAccount();
		List<EdocMarkCategory> categories = edocMarkCategoryManager.findByTypeAndDomainId(Constants.EDOC_MARK_CATEGORY_BIGSTREAM, domainId);
		JSONArray jsonArray = new JSONArray();
		for (EdocMarkCategory category : categories) {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.putOpt("optionValue", category.getId().toString());
			//GOV-3932  每次新建一个新的大流水号的时候，在选择大流水号的列表中就没有那个
			jsonObject.putOpt("optionName", Strings.toHTML(category.getCategoryName()));
			jsonObject.putOpt("optionMinNo", category.getMinNo().toString());
			jsonObject.putOpt("optionMaxNo", category.getMaxNo().toString());
			jsonObject.putOpt("optionCurrentNo", category.getCurrentNo().toString());
			jsonObject.putOpt("optionYearEnabled", category.getYearEnabled());
			jsonObject.putOpt("optionReadonly", category.isReadonly());
			jsonArray.put(jsonObject);
		}
		log.debug("json: " + jsonArray.toString());
		String view = null;
		if (isAjax) {
			view = this.getJsonView();
		}
		return new ModelAndView(view, Constants.AJAX_JSON, jsonArray);	
	}
	
	
	/**------------------------ 公文文号管理End -----------------------------**/
	
	
	
	/**------------------------ 内部文号管理Start -----------------------------**/
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView setInnerMarkDefPage(HttpServletRequest request,HttpServletResponse response) 
		throws Exception {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/setInnerMark");
		Long domainId = AppContext.getCurrentUser().getLoginAccount();
		int status = edocInnerMarkDefinitionManager.getInnerMarkStatus(domainId);
		mav.addObject("status", status);
		
		Calendar cal = Calendar.getInstance();
		Integer yearNo = cal.get(Calendar.YEAR);				
		mav.addObject("yearNo", yearNo);
		
		List<EdocInnerMarkDefinition> markDefs = null;
		if (status > 0) {			
			if (status == Constants.STATUS_INNERMARK_PUBLIC) {
				markDefs = edocInnerMarkDefinitionManager.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_UNIFICATION, domainId);
				if (markDefs != null && markDefs.size() > 0) {
					mav.addObject("markDef", markDefs.get(0));
				}
			}
			else if (status == Constants.STATUS_INNERMARK_PRIVATE) {
				markDefs = edocInnerMarkDefinitionManager.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_SEND, domainId);
				if (markDefs != null && markDefs.size() > 0) {
					mav.addObject("sendMarkDef", markDefs.get(0));
				}
				markDefs = edocInnerMarkDefinitionManager.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_RECEIVED, domainId);
				if (markDefs != null && markDefs.size() > 0) {
					mav.addObject("recieveMarkDef", markDefs.get(0));
				}
				markDefs = edocInnerMarkDefinitionManager.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_SIGN_REPORT, domainId);
				if (markDefs != null && markDefs.size() > 0) {
					mav.addObject("signReportMarkDef", markDefs.get(0));
				}				
			}
		}		
		
		return mav;
	}

	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView saveInnerMarkDef(HttpServletRequest request,HttpServletResponse response)throws Exception{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			
			Integer type = Integer.valueOf(request.getParameter("type"));
//			int status = edocInnerMarkDefinitionManager.getInnerMarkStatus(domainId);
			
			// 如果改变内部文号的类型，则删除已有的文号设置
//			if ( (status == Constants.STATUS_INNERMARK_PUBLIC && type != Constants.EDOC_INNERMARK_UNIFICATION) 
//					|| status == Constants.STATUS_INNERMARK_PRIVATE && type == Constants.EDOC_INNERMARK_UNIFICATION) {
//				edocInnerMarkDefinitionManager.deleteAll(domainId);
//			}
			edocInnerMarkDefinitionManager.deleteAll(domainId);
			
			EdocInnerMarkDefinition def = null;
			if (type == Constants.EDOC_INNERMARK_UNIFICATION) {
				def = new EdocInnerMarkDefinition();
				def.setIdIfNew();
				def.setWordNo(request.getParameter("wordNo"));
				def.setMinNo(Integer.valueOf(request.getParameter("minNo")));
				def.setMaxNo(Integer.valueOf(request.getParameter("maxNo")));
				def.setCurrentNo(Integer.valueOf(request.getParameter("currentNo")));				
				def.setExpression(request.getParameter("markNo"));								
				def.setType(Constants.EDOC_INNERMARK_UNIFICATION);	
				Boolean yearEnabled = false;
				if (request.getParameter("yearEnabled") != null) {
					yearEnabled = true;
				}
				def.setYearEnabled(yearEnabled);	
				Integer length = 0;
				Boolean fixedLength = false;
				if (request.getParameter("fixedLength") != null) {
					fixedLength = true;
				}
				if (fixedLength) {
					length = Integer.valueOf(request.getParameter("length"));
				}
				def.setLength(length);
				def.setDomainId(domainId);
				edocInnerMarkDefinitionManager.create(def);	
				edocInnerMarkDefinitionManager.setInnerMarkStatus(domainId, Constants.STATUS_INNERMARK_PUBLIC);
			}
			else {
				boolean edocPlugin = com.seeyon.ctp.common.SystemEnvironment.hasPlugin("edoc");
				String[] sTypes = null;
				if(edocPlugin){
					sTypes = new String[]{"send_", "receive_", "sign_report_"}; 
				}else{
					sTypes = new String[]{"sign_report_"};
				}
				Integer[] iTypes = null;
				if(edocPlugin){
					iTypes = new Integer[]{Constants.EDOC_INNERMARK_SEND, Constants.EDOC_INNERMARK_RECEIVED, Constants.EDOC_INNERMARK_SIGN_REPORT};
				}else{
					iTypes = new Integer[]{Constants.EDOC_INNERMARK_SIGN_REPORT};
				}				
				for (int i = 0; i < sTypes.length; i++) {
					def = new EdocInnerMarkDefinition();
					def.setIdIfNew();
					def.setWordNo(request.getParameter(sTypes[i] + "wordNo"));
					def.setMinNo(Integer.valueOf(request.getParameter(sTypes[i] + "minNo")));
					def.setMaxNo(Integer.valueOf(request.getParameter(sTypes[i] + "maxNo")));
					def.setCurrentNo(Integer.valueOf(request.getParameter(sTypes[i] + "currentNo")));					
					def.setExpression(request.getParameter(sTypes[i] + "markNo"));					
					def.setType(iTypes[i]);	
					Boolean yearEnabled = false;
					if (request.getParameter(sTypes[i] + "yearEnabled") != null) {
						yearEnabled = true;
					}
					def.setYearEnabled(yearEnabled);
					Integer length = 0;
					Boolean fixedLength = false;
					if (request.getParameter(sTypes[i] + "fixedLength") != null) {
						fixedLength = true;
					}
					if (fixedLength) {
						length = Integer.valueOf(request.getParameter(sTypes[i] + "length"));
					}
					def.setLength(length);
					def.setDomainId(domainId);
					edocInnerMarkDefinitionManager.create(def);
					edocInnerMarkDefinitionManager.setInnerMarkStatus(domainId, Constants.STATUS_INNERMARK_PRIVATE);
				}
			}	
			out.print("<script>");
			//out.print("alert('操作成功!');");	
			out.println("alert('"+ResourceBundleUtil.getString("www.seeyon.com.v3x.form.resources.i18n.FormResources","formapp.saveoperok.label")+"')");
			//out.println("parent.location.reload(true);");
			out.print("</script>");
			out.flush();
		}
		catch (Exception e) {
			out.print("<script>");
			out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
			out.print("</script>");
			out.flush();
		}
				
		return setInnerMarkDefPage(request,response);
	}	
	
	/**------------------------ 内部文号管理End -----------------------------**/
	
	
	

	/**-------------------------  文号调用Start ----------------------------**/
	
	/**
	 * 进入选择公文断号|手工输入公文文号界面(仅用于公文文号，不适用与内部文号)。
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView docMarkChoose(HttpServletRequest request,HttpServletResponse response)throws Exception{

		ModelAndView mav = new ModelAndView("edoc/docMarkManage/choose_all_mark");
		User user = AppContext.getCurrentUser();
		
		String orgAccountId=request.getParameter("orgAccountId");
		Long   _orgAccountId=V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
			
		String selDocmark = request.getParameter("selDocmark");
		String templeteId = request.getParameter("templeteId");
		String affairId = request.getParameter("affairId");
		
		Long memberId = user.getId();
		if(Strings.isNotBlank(affairId)){
		    
		    CtpAffair ctpAffair = affairManager.get(Long.parseLong(affairId));
		    if(ctpAffair != null){
		        memberId = ctpAffair.getMemberId();
		    }
		}
		
		EdocMarkModel model = null;
		List<EdocMarkModel> markDefs  = null;
		
		
		if(Strings.isNotBlank(templeteId)){
			MarkCategory category = null;
			if("my:doc_mark".equals(selDocmark)) {
			    category = MarkCategory.docMark;
			}
			if("my:doc_mark2".equals(selDocmark)){
			    category = MarkCategory.docMark2;
			} 
			model = edocMarkDefinitionManager.getEdocMarkByTempleteId(Long.valueOf(templeteId), category);
		}
		if(model == null){ //公文模板没有绑定字号
			String deptIds = orgManager.getUserIDDomain(memberId,_orgAccountId, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
			String[] depts = deptIds.split(",");
			StringBuilder finalDeptIds= new StringBuilder();
			for(String deptId : depts){
				V3xOrgUnit unit = orgManager.getUnitById(Long.parseLong(deptId));
				if(unit!=null){
				    Long unitId = null;
					if(!unit.getIsGroup()){
						if(unit.getOrgAccountId().longValue() == AppContext.currentAccountId()){
						    unitId = unit.getId();
						}
					}else{
					    unitId = unit.getId();
					}
					if(unitId != null){
					    if(finalDeptIds.length() > 0){
					        finalDeptIds.append(",");
					    }
					    finalDeptIds.append(unitId);
					}
				}
			}
			
			markDefs = edocMarkDefinitionManager.getEdocMarkDefinitions(finalDeptIds.toString(),EdocEnum.MarkType.edocMark.ordinal());
			
		}else{
			markDefs =new ArrayList<EdocMarkModel>();
			markDefs.add(model);
			
		}
		
		Long reserveNumberFirstDefinitionId = 0l;
		Long breakMarkFirstDefinitionId = 0l;
		if(Strings.isNotEmpty(markDefs)){
		    for(EdocMarkModel mark : markDefs){
		        String shotName = EdocHelper.getEdocMarkDispalyName(user.getLoginAccount(), mark);
		        mark.setAccountShotName(shotName);;
		        if(Long.valueOf(0).equals(breakMarkFirstDefinitionId)){
		        	breakMarkFirstDefinitionId = mark.getMarkDefinitionId();
		        }
		        if(Long.valueOf(0).equals(reserveNumberFirstDefinitionId)){
		        	if(mark.getCategoryCodeMode() == 0 ){
		        		reserveNumberFirstDefinitionId = mark.getMarkDefinitionId();
		        	}
		        }
		    }
		}
		
		//GOV-4731.调用发文模版，文号默认为空 start
		//mav.addObject("isBoundWordNo",model == null ? false :true);
		mav.addObject("isBoundWordNo", false);
		//GOV-4731.调用发文模版，文号默认为空 end
		
		mav.addObject("markDefs", markDefs);
		List<String> reservedMarkNoList = new ArrayList<String>();
		if (markDefs != null && markDefs.size() > 0) {	
			
			List<EdocMarkReserveNumber> reserveNumberList = new ArrayList<EdocMarkReserveNumber>();

			EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(reserveNumberFirstDefinitionId);
			
			EdocMarkDefinition breakMarkDef = edocMarkDefinitionManager.getMarkDefinition(breakMarkFirstDefinitionId);
			

			if(markDef != null){
				
				if("my:doc_mark".equals(selDocmark) || "my:doc_mark2".equals(selDocmark)) {
					
					List<EdocMarkReserveNumber> queryReserveNumberList = edocMarkReserveManager.findEdocMarkReserveNumberList(markDef, ReserveTypeEnum.reserve_up.getReserveType());
					
					if(Strings.isNotEmpty(queryReserveNumberList)){
						for(EdocMarkReserveNumber reserveNumber : queryReserveNumberList) {
							
							reservedMarkNoList.add(reserveNumber.getDocMark());
							
							if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())){
								continue;
							} 
							
							boolean isUsed = edocMarksManager.isUsed(reserveNumber.getDocMark() ,"0", orgAccountId);
							if(isUsed) {
								continue;
							}
							
							reserveNumberList.add(reserveNumber);
							
						}
					}
				}
				Collections.sort(reserveNumberList, new EdocMarkReserveNumber());
				mav.addObject("reserveNumberList", reserveNumberList);
			}
			
			List<EdocMarkNoModel> retList = new ArrayList<EdocMarkNoModel>();
			if(null!=breakMarkDef){
				List<EdocMarkNoModel> edocMarks = edocMarksManager.getDiscontinuousMarkNos(breakMarkDef);
				if(Strings.isNotEmpty(edocMarks)) {
					for(EdocMarkNoModel m : edocMarks) {
						
						if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(breakMarkDef ,m.getMarkNo())) {
							continue;
						}
						
						if(Strings.isNotEmpty(reservedMarkNoList) && reservedMarkNoList.contains(m.getMarkNo())) {
							continue;
						}
						
						boolean isUsed = edocMarksManager.isUsed(m.getMarkNo() ,"0",orgAccountId);
						if(isUsed) {
							continue;
						}
						
						retList.add(m);
					}
				}
			}
			mav.addObject("edocMarks", retList);
		}
		
		//OA-45372 公文模板制作单位设置公文开关不允许手动输入文号，外单位人员处理时却可以手动输入文号
		//不是模板时，取公文所属单位权限控制
		long edocAccountId = Long.parseLong(orgAccountId);
        //模板的公文开关权限应该取模板制作单位的权限控制
		if(Strings.isNotBlank(templeteId)){ 
            CtpTemplate t = templateManager.getCtpTemplate(Long.parseLong(templeteId));
            if(t!=null){
            	edocAccountId = t.getOrgAccountId();
            }
        }
		mav.addObject("personInput", EdocSwitchHelper.canInputEdocWordNum(edocAccountId));		
		return mav;		
	}
	
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView docMarkChooseEntry(HttpServletRequest request, HttpServletResponse response)throws Exception{		
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/choose_all_mark_iframe");
		return mav;
	}
	

	/**
	 * 选择断号时，改变公文文号定义时调用此方法。 
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView changeDocMarkDef(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
		Long edocMarkDefinitionId = ServletRequestUtils.getRequiredLongParameter(request, "definitionId");
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(edocMarkDefinitionId);
		List<EdocMarkNoModel> edocMarks = edocMarksManager.getDiscontinuousMarkNos(markDef);
		
		List<String> reservedMarkNoList = new ArrayList<String>();
		List<EdocMarkReserveNumber> queryReserveNumberList = edocMarkReserveManager.findEdocMarkReserveNumberList(markDef, ReserveTypeEnum.reserve_all.getReserveType());
		for(EdocMarkReserveNumber reserveNumber : queryReserveNumberList) {
			reservedMarkNoList.add(reserveNumber.getDocMark());
		}		
		
		
		JSONArray jsonArray = new JSONArray();
		if(Strings.isNotEmpty(edocMarks)) {
			for (EdocMarkNoModel model : edocMarks) {
				
				if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, model.getMarkNo())){
					continue;
				} 
				
				if(Strings.isNotEmpty(reservedMarkNoList) && reservedMarkNoList.contains(model.getMarkNo())) {
					continue;
				}
				
				boolean isUsed = edocMarksManager.isUsed(model.getMarkNo() ,"0", String.valueOf(AppContext.currentAccountId()));
				if(isUsed) {
					continue;
				}
				
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.putOpt("optionValue", model.getEdocMarkId().toString());
				jsonObject.putOpt("optionName", model.getMarkNo());			
				jsonArray.put(jsonObject);
			}
		}
		log.debug("json: " + jsonArray.toString());
		String view = null;
		if (isAjax) {
			view = this.getJsonView();
		}
		return new ModelAndView(view, Constants.AJAX_JSON, jsonArray);	
	}
		
	/**-------------------------  文号调用End ----------------------------**/
	
	/**
	 * 选择断号时，改变公文文号定义时调用此方法。 
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView changeDocMarkDefReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
		Long edocMarkDefinitionId = ServletRequestUtils.getRequiredLongParameter(request, "definitionId");
		Long edocId = ServletRequestUtils.getRequiredLongParameter(request, "edocId");
		JSONArray jsonArray = new JSONArray();
		//文号设置为预留文号才会显示在前台预留文号调用中
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(edocMarkDefinitionId);
		List<EdocMarkReserveNumber> reserveNumberList = edocMarkReserveManager.findEdocMarkReserveNumberList(markDef, ReserveTypeEnum.reserve_up.getReserveType());
		Collections.sort(reserveNumberList, new EdocMarkReserveNumber());
		
		
		for(EdocMarkReserveNumber reserveNumber : reserveNumberList) {
			
			if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, reserveNumber.getDocMark())) {
				continue;
			}

			boolean isUsed = edocMarksManager.isUsed(reserveNumber.getDocMark() ,"0", String.valueOf(AppContext.currentAccountId()));
			if(isUsed) {
				continue;
			}
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.putOpt("optionValue", String.valueOf(reserveNumber.getMarkDefineId()));
			jsonObject.putOpt("optionMarkNumber", String.valueOf(reserveNumber.getMarkNo()));
			jsonObject.putOpt("optionName", reserveNumber.getDocMark());		
			jsonArray.put(jsonObject);
		}
		log.debug("json: " + jsonArray.toString());
		String view = null;
		if (isAjax) {
			view = this.getJsonView();
		}
		return new ModelAndView(view, Constants.AJAX_JSON, jsonArray);	
	}
	
	/**
	 * 设置预留文号
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView openEdocMarkReserveDialog(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
		ModelAndView mav = new ModelAndView("edoc/docMarkManage/edoc_mark_reserve_dialog");
		Integer type = Strings.isBlank(request.getParameter("type")) ? 1 : Integer.parseInt(request.getParameter("type"));
		Long markDefineId = Strings.isBlank(request.getParameter("markDefineId")) ? -1 : Long.parseLong(request.getParameter("markDefineId"));
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(markDefineId);
		if(markDef == null) {
			try {
				StringBuilder buffer = new StringBuilder();
				buffer.append("alert('"+ResourceUtil.getString("edoc.mark.isDelete")+"');");
				buffer.append("transParams.parentWin.location.reload();");
				buffer.append("top.close();");
				rendJavaScript(response, buffer.toString());
			} catch(Exception e) {
			    log.error("", e);
			}
			return null;
		}
		EdocMarkReserveVO reserveVO = null;
		reserveVO = edocMarkReserveManager.getMarkReserveByFormat(markDef, null);
		List<Long> reservedIdList = new ArrayList<Long>();
		List<EdocMarkReserveVO> reserveVOList = edocMarkReserveManager.findEdocMarkReserveList(markDef);
		List<EdocMarkReserveVO> reserveLineList = new ArrayList<EdocMarkReserveVO>();
		List<EdocMarkReserveVO> reserveDownList = new ArrayList<EdocMarkReserveVO>();
		if(Strings.isNotEmpty(reserveVOList)) {
			for(EdocMarkReserveVO vo : reserveVOList) {
				
				//是否是本年的编号。
			    if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, vo.getReserveNumberList().get(0).getDocMark())){
			    	 continue;
				}
				if(vo.getType().intValue() == 1) {
					reserveLineList.add(vo);
				} else {
					reserveDownList.add(vo);
				}
				if((type.intValue()==1 && vo.getType().intValue() == 1)//线上占用
						|| (type.intValue()==2 && vo.getType().intValue() == 2)) {//线下占用
					reservedIdList.add(vo.getId());
				}
			}
		}
		mav.addObject("reserveLineList", reserveLineList);
		mav.addObject("reserveDownList", reserveDownList);
		mav.addObject("reservedIdList", reservedIdList);
		mav.addObject("reserveVO", reserveVO);
		mav.addObject("reserveToLabel", ResourceUtil.getString("edoc.oper.to"));
		return mav;
	}
	
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView listEdocMarkReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer type = Strings.isBlank(request.getParameter("type")) ? 1 : Integer.parseInt(request.getParameter("type"));
		Integer queryNumber = Strings.isBlank(request.getParameter("queryNumber")) ? -1 : Integer.parseInt(request.getParameter("queryNumber"));
		Long markDefineId = Strings.isBlank(request.getParameter("markDefineId")) ? -1 : Long.parseLong(request.getParameter("markDefineId"));
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(markDefineId);
		JSONArray jsonArray = new JSONArray();
		if(markDef != null) {
			List<EdocMarkReserveVO> reserveVOList = edocMarkReserveManager.findEdocMarkReserveList(markDef, queryNumber);
			if(Strings.isNotEmpty(reserveVOList)) {
				for(EdocMarkReserveVO reserveVO : reserveVOList) {
					boolean showFlag = false;
					if((type.intValue()==1 && reserveVO.getType().intValue() == 1) //线上占用
							||(type.intValue()==2 && reserveVO.getType().intValue() == 2)) {//线下占用
						showFlag = true;
					}
					if(showFlag) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.putOpt("optionReservedId", String.valueOf(reserveVO.getId()));
						jsonObject.putOpt("docMarkDisplay", reserveVO.getDocMarkDisplay());
						jsonObject.putOpt("optionStartNo", reserveVO.getStartNo());
						jsonObject.putOpt("optionEndNo", reserveVO.getEndNo());		
						jsonArray.put(jsonObject);
					}
				}
			}
		}
		boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
		String view = null;
		if (isAjax) {
			view = this.getJsonView();
		}
		return new ModelAndView(view, Constants.AJAX_JSON, jsonArray);	
	}
	
	/**
	 * 添加预留文号
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes" , "unchecked" })
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
	public ModelAndView saveMarkReserve(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
		Map reserveDataMap =  ParamUtil.getJsonDomain("reserveData");
		Long markDefineId = reserveDataMap.get("markDefineId")==null ? -1 : Long.parseLong((String)reserveDataMap.get("markDefineId"));
		Integer type = reserveDataMap.get("type")==null ? 1 : Integer.parseInt((String)reserveDataMap.get("type"));
		Map<String, String> map = ParamUtil.getJsonDomain("thisReservedIds");
		String[] addReservedIds = null;
		String[] thisReservedIds = null;
		String[] delReservedIds = null;
		if(map==null || map.size()==0) {
			List<Map<String,String>> thisReservedIdsMap =  ParamUtil.getJsonDomainGroup("thisReservedIds");	
			if(Strings.isNotEmpty(thisReservedIdsMap)) {
				thisReservedIds = new String [thisReservedIdsMap.size()];
				for(int i = 0; i<thisReservedIdsMap.size(); i++) {
					thisReservedIds[i] = thisReservedIdsMap.get(i).get("thisReservedId");
				}
			}
		} else {
			thisReservedIds = new String[1];
			thisReservedIds[0] = map.get("thisReservedId");
		}
		map = ParamUtil.getJsonDomain("delReserveIds");
		if(map==null || map.size()==0) {
			List<Map<String,String>> delReserveIdsMap =  ParamUtil.getJsonDomainGroup("delReserveIds");	
			if(Strings.isNotEmpty(delReserveIdsMap)) {
				delReservedIds = new String [delReserveIdsMap.size()];
				for(int i = 0; i<delReserveIdsMap.size(); i++) {
					delReservedIds[i] = delReserveIdsMap.get(i).get("delReservedId");
				}
			}
		} else {
			delReservedIds = new String[1];
			delReservedIds[0] = map.get("delReservedId");
		}
		map = ParamUtil.getJsonDomain("addReserveIds");
		if(map==null || map.size()==0) {
			List<Map<String,String>> addReserveIdsMap =  ParamUtil.getJsonDomainGroup("addReserveIds");	
			if(Strings.isNotEmpty(addReserveIdsMap)) {
				addReservedIds = new String [addReserveIdsMap.size()];
				for(int i = 0; i<addReserveIdsMap.size(); i++) {
					addReservedIds[i] = addReserveIdsMap.get(i).get("addReservedId");
				}
			}
		} else {
			addReservedIds = new String[1];
			addReservedIds[0] = map.get("addReservedId");
		}
		EdocMarkDefinition markDef = edocMarkDefinitionManager.getMarkDefinition(markDefineId);
		if(markDef == null) {
			try {
				PrintWriter out = response.getWriter();
				out.print("markDef_is_nul");
				out.close();
			} catch(Exception e) {
				log.error("", e);
			}
			return null;
		}
		
		List<Long> delReservedIdList = new ArrayList<Long>();
        if(delReservedIds!=null && delReservedIds.length>0) {
            for(int i=0; i<delReservedIds.length; i++) {
                Long reservedId = Long.parseLong(delReservedIds[i]);
                if(!delReservedIdList.contains(reservedId)){
                    delReservedIdList.add(reservedId);
                }
            }
        }
		
		List<EdocMarkReserveVO> addReserveList = new ArrayList<EdocMarkReserveVO>();
		if(addReservedIds!=null && addReservedIds.length>0) {
			User user = AppContext.getCurrentUser();
			List<Long> thisReserveIdList = new ArrayList<Long>();
			if(thisReservedIds!=null && thisReservedIds.length>0) {
				Collections.addAll(thisReserveIdList, thisReservedIds);
			}
			for(int i=0; i<addReservedIds.length; i++) {
				String[] startAndEnd =  addReservedIds[i].split("-");
				int startNo = Integer.parseInt(startAndEnd[0]);
				int endNo = Integer.parseInt(startAndEnd[1]);
				boolean flag = edocMarkReserveManager.checkRepeatMarkReserved(markDefineId, startNo, endNo, thisReserveIdList, delReservedIdList);
				if(flag) {
					try {
						PrintWriter out = response.getWriter();
						out.print("repeat");
						out.close();
					} catch(Exception e) {
					    log.error("", e);
					}
					return null;
				}
				EdocMarkReserveVO reserveVO = new EdocMarkReserveVO();
				reserveVO.setMarkDefineId(markDefineId);
				reserveVO.setType(type);
				reserveVO.setStartNo(startNo);
				reserveVO.setEndNo(endNo);
				reserveVO.setYearNo(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				reserveVO.setCreateTime(DateUtil.currentTimestamp());
				reserveVO.setCreateUserId(user.getId());
				reserveVO.setDomainId(user.getLoginAccount());
				reserveVO.setEdocMarkDefinition(markDef);
				reserveVO.setEdocMarkCategory(markDef.getEdocMarkCategory());
				addReserveList.add(reserveVO);
			}
		}
		
		try {
			edocMarkReserveManager.saveEdocMarkReserve(markDef, addReserveList, delReservedIdList);
		} catch(Exception e) {
			log.error("保存预留文号出错", e);
			/** 出现异常更新缓存 */
			edocMarkReserveManager.reloadCache();
		}
		try {
			PrintWriter out = response.getWriter();
			out.print("success");
			out.close();
		} catch(Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	/**
	 * 分页方法
	 */
	private <T> List<T> pagenate(List<T> list) {
		if (null == list || list.size() == 0)
			return new ArrayList<T>();
		Integer first = Pagination.getFirstResult();
		Integer pageSize = Pagination.getMaxResults();
		Pagination.setRowCount(list.size());
		List<T> subList = null;
		if (first + pageSize > list.size()) {
			subList = list.subList(first, list.size());
		} else {
			subList = list.subList(first, first + pageSize);
		}
		return subList;
	}
	
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	
	public void setEdocMarkAclManager(EdocMarkAclManager edocMarkAclManager) {
		this.edocMarkAclManager = edocMarkAclManager;
	}

	public EdocMarkCategoryManager getEdocMarkCategoryManager() {
		return edocMarkCategoryManager;
	}

	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategoryManager) {
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}

	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}

	public void setEdocMarksManager(EdocMarkManager edocMarksManager) {
		this.edocMarksManager = edocMarksManager;
	}
	
	public void setEdocInnerMarkDefinitionManager(EdocInnerMarkDefinitionManager edocInnerMarkDefinitionManager) {
		this.edocInnerMarkDefinitionManager = edocInnerMarkDefinitionManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}
	
}