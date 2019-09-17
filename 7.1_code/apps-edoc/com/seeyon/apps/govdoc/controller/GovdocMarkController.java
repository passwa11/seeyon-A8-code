package com.seeyon.apps.govdoc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.constant.GovdocConfigKey;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.MarkCategoryEnum;
import com.seeyon.apps.govdoc.constant.GovdocMarkEnum.MarkDefStateEnum;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.mark.helper.GovdocMarkHelper;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkOpenManager;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.util.EdocMarkUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 新公文文号控制器
 * @author 唐桂林
 *
 */
public class GovdocMarkController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocMarkController.class);

	public final static String AJAX_JSON = "json";
	
	private GovdocMarkManager govdocMarkManager;
	private GovdocMarkOpenManager govdocMarkOpenManager;
	private GovdocLogManager govdocLogManager;
	
	private String jsonView;

	/****************************** 公文文号管理相关方法 start *******************************/
	/**
	 * 公文文号管理主框架
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/main");
		mav.addObject("skinPathKey", "blue.css");
		return mav;
	}
	/**
     * 公文文号列表操作说明
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
    public ModelAndView listDesc(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	ModelAndView mav = new ModelAndView("govdoc/database/mark/listDesc");
        mav.addObject("size", request.getParameter("size"));
        return mav;
    }
	/**
	 * 公文文号管理列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/list");
		try {
			User user = AppContext.getCurrentUser();
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("domainId", String.valueOf(user.getLoginAccount()));
			
			FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(params);
	        flipInfo = govdocMarkManager.findList(flipInfo, params);
	        
	        request.setAttribute("ffmarkList", flipInfo);
	        
			//预留文号的title
			mav.addObject("dialogTitleUp", ResourceUtil.getString("edoc.mark.reserve.up"));
			mav.addObject("dialogTitleDown", ResourceUtil.getString("edoc.mark.reserve.down"));
		} catch(Exception e) {
			LOGGER.error("获取公文文号管理列表出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号占号列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView listUsed(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/list_used");
		try {
			User user = AppContext.getCurrentUser();
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("domainId", String.valueOf(user.getLoginAccount()));
			
			FlipInfo flipInfo = new FlipInfo();
	        flipInfo.setParams(params);
	        flipInfo = govdocMarkManager.findUsedList(flipInfo, params);
	        
	        request.setAttribute("ffmarkUsedList", flipInfo);
	        
		} catch(Exception e) {
			LOGGER.error("获取公文文号占号列表出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号开关
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView listMarkSwitch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/switch");
		String markType = request.getParameter("markType");
		Long currentAccountId = AppContext.currentAccountId();
		List<ConfigItem> list = new ArrayList<ConfigItem>();
		List<ConfigItem> sendItemList = new ArrayList<ConfigItem>();
		List<ConfigItem> switchList = new ArrayList<ConfigItem>();
		
		String configCategory = GovdocConfigKey.getMarkConfigCategory(markType);
		list = govdocMarkOpenManager.findAllMarkSwitch(configCategory, currentAccountId);
		for(ConfigItem bean : list) {
			if(bean.getConfigItem().endsWith("_fawen") || bean.getConfigItem().endsWith("_qian")) {
				sendItemList.add(bean);
			} else {
				switchList.add(bean);
			}
		}
		
		mav.addObject("sendItemList", sendItemList);
		mav.addObject("switchList", switchList);
		mav.addObject("doc_mark_fawen_default_1", GovdocConfigKey.DOC_MARK_FAWEN_DEFUALT_1);
		mav.addObject("doc_mark_fawen_default_2", GovdocConfigKey.DOC_MARK_FAWEN_DEFUALT_2);
		mav.addObject("doc_mark_qian_default_2", GovdocConfigKey.DOC_MARK_QIAN_DEFUALT_2);
		mav.addObject("serial_no_qian_default_2", GovdocConfigKey.SERIAL_NO_QIAN_DEFUALT_2);
		mav.addObject("sign_mark_qian_default_1", GovdocConfigKey.SIGN_MARK_QIAN_DEFUALT_1);
		return mav;
	}
	/**
	 * 保存文号开关
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView saveMarkSwitch(HttpServletRequest request, HttpServletResponse response) {
		try {
			String markType = request.getParameter("markType");
			String configCategory = GovdocConfigKey.getMarkConfigCategory(markType);
			govdocMarkOpenManager.saveMarkSwitch(request, configCategory, AppContext.getCurrentUser());
			
	        response.getWriter().write("<script type=\"text/javascript\">parent.callbackt();</script>");
	        response.getWriter().flush();
	        response.getWriter().close();
    	} catch(Exception e) {
    		LOGGER.error("公文开关设置保存出错", e);
    	}
        return  null;
	}
	
	/**
     * 公文开关默认设置保存
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveMarkOpenToDefault(HttpServletRequest request, HttpServletResponse response) {
    	String markType = request.getParameter("markType");
    	try {
			String configCategory = GovdocConfigKey.getMarkConfigCategory(markType);
    		govdocMarkOpenManager.saveMarkSwitchToDefault(configCategory, AppContext.getCurrentUser());
    		
    		PrintWriter out = null;
			out = response.getWriter();
			out.println("<script>");
			out.println("try { parent.location.reload();}catch(e){parent.location.reload();}");
			out.println("</script>");
    	} catch(Exception e) {
    		LOGGER.error("公文开关默认设置保存出错", e);
    	}
        return null;
    }
	/****************************** 公文文号管理相关方法   end *******************************/
	
	
	/****************************** 公文文号定义相关方法 start *******************************/
	/**
	 * 公文文号新建界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView createMarkDef(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/create");
		try {				
			mav.addObject("yearNo", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			List<EdocMarkCategory> categories = govdocMarkManager.getMarkCategories(domainId);		
			//GOV-3932 新建的大流水号有特殊字符的时候，在选择大流水号的下拉框没有显示提示字符"</td>".预览引用特殊字符的大流水号的时候页面弹出a
			for(EdocMarkCategory c : categories) {
				c.setCategoryName(Strings.toHTML(c.getCategoryName()));
			}
			mav .addObject("categories", categories);
		} catch(Exception e) {
			LOGGER.error("公文文号新建界面出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号编辑界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView editMarkDef(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/edit");
		try {
			Long id = Long.valueOf(request.getParameter("id"));
			EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(id);
			
			if(markDef == null) {
				return null;
			}
			
			List<EdocMarkAcl> markAclList = new ArrayList<EdocMarkAcl>();
			// 组装公文文号授权的相关信息
			List<EdocMarkAcl> result = govdocMarkManager.getMarkAclById(id);
			
			String deptIds = "";
			String deptNames = "";
			if(Strings.isNotEmpty(result)) {
				for(EdocMarkAcl bean : result) {
					long entityId = bean.getDeptId();
					V3xOrgEntity orgEntity = GovdocOrgHelper.getOrgEntity(bean.getAclType(), entityId);
					if (orgEntity != null) {	
						if(Strings.isNotBlank(deptIds)) {
							deptIds += ",";
							deptNames += "、";
						}
						deptIds += entityId;
						deptNames += orgEntity.getName();
						
						markAclList.add(bean);
					}
				}
			}
			mav.addObject("elements", markAclList);
			
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			List<EdocMarkCategory> categories = govdocMarkManager.getMarkCategories(domainId);
			//GOV-3932 新建的大流水号有特殊字符的时候，在选择大流水号的下拉框没有显示提示字符"</td>".预览引用特殊字符的大流水号的时候页面弹出a
			for(EdocMarkCategory c : categories){
				c.setCategoryName(Strings.toHTML(c.getCategoryName()));
			}
			
			Integer yearNo = Calendar.getInstance().get(Calendar.YEAR);
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
		} catch(Exception e) {
			LOGGER.error("公文文号编辑界面出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号新建保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView saveMarkDef(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch(IOException e) {
			LOGGER.error("公文文号新建保存获取out出错", e);
		}
		try {
			Map<String, String> params = ParamUtil.getJsonParams();

			User user = AppContext.getCurrentUser();
			Long domainId = user.getLoginAccount();
			String wordNo = ParamUtil.getString(params, "wordNo");		
			if(Strings.isNotBlank(wordNo)){
				wordNo = wordNo.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
			}
			String markType = ParamUtil.getString(params, "markType");
			int imarkType=0;
			if(markType!=null && !"".equals(markType))	
				imarkType=Integer.parseInt(markType);
			// 验证公文字号是否重名
			Boolean flag = govdocMarkManager.containMarkDef(wordNo.trim(), domainId,imarkType);
			if (flag) {
				out.write("namerepeat");
				out.flush();
				return null;
			}
			Integer length = 0;			
			Boolean fixedLength = false;
			if (ParamUtil.getString(params, "fixedLength") != null) {
				fixedLength = true;
			}
			short mode = Short.valueOf(ParamUtil.getString(params, "flowNoType")); //0-小流水；1-大流水
			String expression = ParamUtil.getString(params, "markNo");												
			Integer currentNo = new Integer(0);
			EdocMarkCategory markCategory = null;
			if (mode == MarkCategoryEnum.big.ordinal()) {
				Long categoryId = Long.valueOf(ParamUtil.getString(params, "categoryId"));
				markCategory = govdocMarkManager.findById(categoryId);
				markCategory.setReadonly(true);
				//大流水的时候页面disabled,不能直接获取参数。
				currentNo=markCategory.getCurrentNo();
				govdocMarkManager.updateCategory(markCategory);
			} else {
				Integer minNo = Integer.valueOf(ParamUtil.getString(params, "minNo"));
				Integer maxNo = Integer.valueOf(ParamUtil.getString(params, "maxNo"));
				currentNo = Integer.valueOf(ParamUtil.getString(params, "currentNo"));
				Boolean yearEnabled = false;
				if (ParamUtil.getString(params, "yearEnabled") != null) {
					yearEnabled = true;
				}
				markCategory = new EdocMarkCategory();
				markCategory.setIdIfNew();
				markCategory.setCategoryName(wordNo);
				markCategory.setMinNo(minNo);
				markCategory.setMaxNo(maxNo);
				markCategory.setCurrentNo(currentNo);
				markCategory.setYearEnabled(yearEnabled);
				markCategory.setCodeMode(MarkCategoryEnum.small.key());
				markCategory.setReadonly(true);
				markCategory.setDomainId(domainId);	
				
				if(Strings.isNotBlank(ParamUtil.getString(params, "twoYear")) ) {
					markCategory.setTwoYear(1);
				}else {
					markCategory.setTwoYear(0);
				}
				govdocMarkManager.saveCategory(markCategory);
			}
			
			EdocMarkDefinition markDef = new EdocMarkDefinition();
			if(markType!=null&&!"".equals(markType)){
				markDef.setMarkType(Integer.parseInt(markType));
			}
			markDef.setIdIfNew();
			markDef.setWordNo(wordNo);			
			if (fixedLength) {
				length = String.valueOf(markCategory.getMaxNo()).length();			
			}
			
			if(Strings.isNotBlank(ParamUtil.getString(params, "sortNo"))) {
				markDef.setSortNo(Integer.valueOf(ParamUtil.getString(params, "sortNo")));
			} else {
				markDef.setSortNo(null);
			}
			markDef.setLength(length);		
			markDef.setExpression(expression);				
			markDef.setDomainId(domainId);
			markDef.setCategoryId(markCategory.getId());
			markDef.setEdocMarkCategory(markCategory);
			markDef.setStatus(MarkDefStateEnum.draft.key());		
			
			// 保存公文文号授权信息
			String deptIds = ParamUtil.getString(params, "grantedDepartId");
			List<EdocMarkAcl> markAclList = new ArrayList<EdocMarkAcl>();
			if (deptIds != null && !"".equals(deptIds.trim())) {
				String[] aDeptId = deptIds.split(",");
				for (String deptId : aDeptId) {
					EdocMarkAcl edocMarkAcl = new EdocMarkAcl();
					edocMarkAcl.setIdIfNew();
					String[] bDeptId = deptId.split("\\|");
					edocMarkAcl.setAclType(bDeptId[0]);
					edocMarkAcl.setDeptId(Long.valueOf(bDeptId[1]));
					edocMarkAcl.setMarkDefId(markDef.getId());
					markAclList.add(edocMarkAcl);
				}
			}
			markDef.setMarkAclList(markAclList);
			govdocMarkManager.saveMarkDef(markDef);
			
			//小流水切换为大流水，需占号
			if (mode == MarkCategoryEnum.big.key()) {
				//保存大流水的预留文号及占号
				govdocMarkManager.saveMarkReserveForCategory(user, markDef);
			}
			
			//记录应用日志，安全权限
			Calendar cal = Calendar.getInstance();
			String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 	
			GovdocMarkVO markVo = govdocMarkManager.markDef2Mode(markDef,yearNo,currentNo);
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_MARK_CREATE.key(), user.getName(), markVo.getMarkstr());
     		
		} catch(Exception e) {
			LOGGER.error("公文文号新建保存出错", e);
			out.write("failure");
		}
		out.write("success");
		out.flush();
		return null;
	}
	/**
	 * 公文文号修改
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView updateMarkDef(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch(IOException e) {
			LOGGER.error("公文文号新建保存获取out出错", e);
		}
		
		try {
			Map<String, String> params = ParamUtil.getJsonParams();

			User user = AppContext.getCurrentUser();
			Long domainId = user.getLoginAccount();
			String wordNo = ParamUtil.getString(params, "wordNo");		
			if(Strings.isNotBlank(wordNo)){
				wordNo = wordNo.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
			}
			int imarkType = ParamUtil.getInt(params, "markType", 0);
			
			Long id = ParamUtil.getLong(params, "id");
			
			short mode = Short.valueOf(ParamUtil.getString(params, "flowNoType")); //0-小流水；1-大流水	
			Integer currentNo = new Integer(0);
			if (mode != MarkCategoryEnum.big.key()) {
				currentNo = Integer.valueOf(ParamUtil.getString(params, "currentNo"));
				if(govdocMarkManager.checkRepeatMarkReserved(id, currentNo, currentNo)) {
					rendJavaScript(response, "alert('"+ ResourceUtil.getString("edoc.current.mark.reserved.notset")+"');");
					return null;
				}
		    }
			
			EdocMarkDefinition edocMarkDef = govdocMarkManager.getMarkDef(id);
			if(edocMarkDef == null) {
				rendJavaScript(response, "alert('"+ ResourceUtil.getString("edoc.current.mark.reserved.notset")+"');");
				return null;
			}
			imarkType = edocMarkDef.getMarkType();
			
			// 验证公文字号是否重名			
			Boolean flag = govdocMarkManager.containMarkDef(id, wordNo.trim(), domainId,imarkType);
			if (flag) {
				out.write("namerepeat");
				out.flush();
				return null;
			}
			
			Long oldCategoryId = edocMarkDef.getCategoryId();
			
			Integer length = 0;		
			short oldMode = -1; //修改之前的编号方式
			if(Strings.isNotBlank(ParamUtil.getString(params, "oldCodeMode"))) {//有可能大流水被删掉，而文号还没保存是大流水还是小流水的选项就页面跳转了
				oldMode = Short.valueOf(ParamUtil.getString(params, "oldCodeMode"));
			}
			String expression = ParamUtil.getString(params, "markNo");
			Boolean fixedLength = false;
			if (Strings.isNotBlank(ParamUtil.getString(params, "fixedLength"))) {
				fixedLength = true;
			}	
			Boolean modeChanged = false;
			if (oldMode != mode) {
				modeChanged = true;
			}
			govdocMarkManager.deleteByDefId(id);
			
			EdocMarkCategory edocMarkCategory = null;
			if (mode == MarkCategoryEnum.big.key()) {
				Long categoryId = ParamUtil.getLong(params, "categoryId"); 
				edocMarkCategory = govdocMarkManager.findById(categoryId);
				edocMarkCategory.setReadonly(true);
				//大流水的时候页面disabled,不能直接获取参数。
				currentNo = edocMarkCategory.getCurrentNo();
				if(Strings.isNotBlank(ParamUtil.getString(params, "twoYear"))) {
					edocMarkCategory.setTwoYear(1);
				}else {
					edocMarkCategory.setTwoYear(0);
				}
				govdocMarkManager.updateCategory(edocMarkCategory);
			}
			else {
				Integer minNo = ParamUtil.getInt(params, "minNo");
				Integer maxNo = ParamUtil.getInt(params, "maxNo");
				Boolean yearEnabled = false;
				if (Strings.isNotBlank(ParamUtil.getString(params, "yearEnabled"))) {
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
					edocMarkCategory.setCodeMode(MarkCategoryEnum.small.key());
					edocMarkCategory.setReadonly(true);					
					edocMarkCategory.setDomainId(domainId);	
					if (Strings.isNotBlank(ParamUtil.getString(params, "twoYear"))) {
						edocMarkCategory.setTwoYear(1);
					}else {
						edocMarkCategory.setTwoYear(0);
					}
					//由大流水变小流水时，需要验证
					if(govdocMarkManager.checkRepeatMarkReserved(id, currentNo, currentNo)) {
						rendJavaScript(response, "alert('"+ ResourceUtil.getString("edoc.current.mark.reserved.notset")+"');");
						return null;
					}
					govdocMarkManager.saveCategory(edocMarkCategory);
				} else {
					edocMarkCategory = edocMarkDef.getEdocMarkCategory();
					edocMarkCategory.setMinNo(minNo);
					edocMarkCategory.setMaxNo(maxNo);
					edocMarkCategory.setCurrentNo(currentNo);
					edocMarkCategory.setYearEnabled(yearEnabled);
					
					if (Strings.isNotBlank(ParamUtil.getString(params, "twoYear"))) {
						edocMarkCategory.setTwoYear(1);
					}else {
						edocMarkCategory.setTwoYear(0);
					}
					govdocMarkManager.updateCategory(edocMarkCategory);
				}			
			}
			edocMarkDef.setCategoryId(edocMarkCategory.getId());
			edocMarkDef.setEdocMarkCategory(edocMarkCategory);
			edocMarkDef.setWordNo(wordNo);
			edocMarkDef.setExpression(expression);
			if (Strings.isNotBlank(ParamUtil.getString(params, "sortNo"))) {
				edocMarkDef.setSortNo(Integer.valueOf(ParamUtil.getString(params, "sortNo")));
			}else {
				edocMarkDef.setSortNo(null);
			}
			if (fixedLength) {
				length = String.valueOf(edocMarkCategory.getMaxNo()).length();			
			}
			edocMarkDef.setLength(length);
			
			// 保存公文文号授权信息
			String deptIds = ParamUtil.getString(params, "grantedDepartId");
			List<EdocMarkAcl> markAclList = new ArrayList<EdocMarkAcl>();
			if (deptIds != null && !"".equals(deptIds.trim())) {
				String[] aDeptId = deptIds.split(",");
				for (String deptId : aDeptId) {
					EdocMarkAcl edocMarkAcl = new EdocMarkAcl();
					edocMarkAcl.setIdIfNew();
					String[] bDeptId = deptId.split("\\|");
					edocMarkAcl.setAclType(bDeptId[0]);
					edocMarkAcl.setDeptId(Long.valueOf(bDeptId[1]));
					edocMarkAcl.setMarkDefId(edocMarkDef.getId());
					markAclList.add(edocMarkAcl);
				}
			}		
			edocMarkDef.setMarkAclList(markAclList);	
			
			//记录应用日志，安全权限
			Calendar cal = Calendar.getInstance();
			String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 	
			GovdocMarkVO markVo = govdocMarkManager.markDef2Mode(edocMarkDef,yearNo,currentNo);
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_MARKAUTHORIZE.key(), user.getName(), markVo.getMarkstr());
			
			govdocMarkManager.updateMarkDef(edocMarkDef);
			
			//小流水切换为大流水，需占号
			if (mode == MarkCategoryEnum.big.key()) {
				//小流水切换为大流水，去掉预留文号及占号或大流水变更，都需要将原预留文号及预留的占号清除(selectType=4)
				boolean changeCategory = (oldCategoryId!=null && oldCategoryId.longValue()!=edocMarkCategory.getId().longValue());
				boolean changeToBig = oldMode == MarkCategoryEnum.small.key();
				if(changeToBig || changeCategory) {
					//清除预留文号
					govdocMarkManager.deleteReserveByMarkDefId(id);
					//删除预留的占号，不删除系统占号
					govdocMarkManager.deleteMarkHistoryByReserve(4, id);
				}
				//保存大流水的预留文号及占号
				if(changeCategory) {
					govdocMarkManager.saveMarkReserveForCategory(user, edocMarkDef);
				}
			}
		} catch (Exception e) {
			LOGGER.error("公文文号修改出错", e);
			out.write("failure");
		}
		out.write("success");
		out.flush();
		return null; 
	}
	/**
	 * 公文文号删除
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView deleteMarkDef(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch(IOException e) {
			LOGGER.error("公文文号新建保存获取out出错", e);
		}
		try {
			String[] ids = request.getParameterValues("markDefId");
			for (int i = 0; i < ids.length; i++) {
				Long id = Long.valueOf(ids[i]);
				EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(id);
				try {				
					if (markDef.getStatus() == MarkDefStateEnum.draft.key()) {
						govdocMarkManager.deleteMarkDef(markDef);
					}
					else {
						// 如果文号已被使用，则进行逻辑删除
						govdocMarkManager.logicalDeleteMarkDef(markDef.getId(), MarkDefStateEnum.deleted.key());
					}
			        //记录应用日志
			        User user = AppContext.getCurrentUser();
			        Calendar cal = Calendar.getInstance();
			        String yearNo = String.valueOf(cal.get(Calendar.YEAR));     
		            GovdocMarkVO markVo = govdocMarkManager.markDef2Mode(markDef, yearNo, 1);
		            govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_MARK_DELETE.key(), user.getName(), markVo.getMarkstr());
				}
				catch (Exception exception) {
					//如果文号已被使用，则进行逻辑删除
					govdocMarkManager.logicalDeleteMarkDef(markDef.getId(), MarkDefStateEnum.deleted.key());
				}
			}
		} catch (Exception e) {
			LOGGER.error("公文文号删除出错", e);
		}
		out.println("<script>");
		out.println("try { parent.reloadList();}catch(e){parent.parent.location.reload();}");
		out.println("</script>");
		return null;		
	}
	/****************************** 公文文号定义相关方法   end *******************************/
	

	/****************************** 公文文号大小流水相关请求 start *******************************/
	/**
	 * 公文文号大流水号列表弹出框
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView openCategoryDialog(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("govdoc/database/mark/category_dialog");
	}
	/**
	 * 公文文号大流水号列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView listMarkCategory(HttpServletRequest request, HttpServletResponse response) {		
		ModelAndView mav = new ModelAndView("govdoc/database/mark/list_category");
		try {
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			List<EdocMarkCategory> categories = govdocMarkManager.findByPage(MarkCategoryEnum.big.key(), domainId); //增加分页
			mav.addObject("categories", categories);
		} catch (Exception e) {
			LOGGER.error("进入公文文号大流水号列表出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号大流水号新建界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView createMarkCategory(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("govdoc/database/mark/create_category");
	}
	/**
	 * 公文文号大流水号编辑界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView editMarkCategory(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/edit_category");
		try {
			Long id = Long.valueOf(request.getParameter("id"));		
			EdocMarkCategory category = govdocMarkManager.findById(id);
			category.setCategoryName(Strings.toHTML(category.getCategoryName()));
			mav.addObject("category", category);
		} catch (Exception e) {
			LOGGER.error("进入公文文号大流水号编辑界面出错", e);
		}
		return mav;
	}
	/**
	 * 公文文号大流水号新建保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView saveMarkCategory(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			String name = request.getParameter("name");
			boolean flag = govdocMarkManager.containEdocMarkCategory(name, domainId);
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
			category.setCodeMode(MarkCategoryEnum.big.key());			
			category.setDomainId(domainId);
			govdocMarkManager.saveCategory(category);
						
			out.println("<script>");
			out.println("parent.window._returnValue(\"true\");");
			out.println("parent.window._closeWin();");
			out.println("</script>");
		} catch(IOException e) {
			LOGGER.error("公文文号大流水号新建保存获取out出错", e);
		} catch (Exception e) {
			LOGGER.error("公文文号大流水号新建保存出错", e);
			if(out != null){
				out.println("<script>");
				out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
				out.println("</script>");
			}			
		} finally{
			if(out != null){
				out.close();
			}
		}
		return null;
	}
	/**
	 * 公文文号大流水号修改
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView updateMarkCategory(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			Long id = Long.valueOf(request.getParameter("id"));
			String name = request.getParameter("name");
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			boolean flag = govdocMarkManager.containEdocMarkCategory(id, name, domainId);
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
			
			EdocMarkCategory category = govdocMarkManager.findById(id);
			Boolean readonly = category.isReadonly();
			category.setCategoryName(name);
			category.setMinNo(minNo);
			category.setMaxNo(maxNo);
			category.setCurrentNo(currentNo);
			//添加了一个判断, null!=s_iYearEnabled,判断s_iYearEnabled是否为空,如果为空，那么就不用修改category.yearEnabled的状态
			if (!readonly && null!=s_iYearEnabled) {
				category.setYearEnabled(yearEnabled);
			}
			govdocMarkManager.updateCategory(category);
					
			out.println("<script>");	
			out.println("parent.window._returnValue(\"true\");");
            out.println("parent.window._closeWin();");
			out.println("</script>");
		} catch(IOException e) {
			LOGGER.error("公文文号大流水号新建保存获取out出错", e);
		} catch (Exception e) {
			LOGGER.error("公文文号大流水号修改出错", e);
			if(out != null){
				out.println("<script>");
				out.print("alert(parent.v3x.getMessage('"+e.getMessage()+"'))");
				out.println("</script>");
			}			
		}
		finally{
			if(out != null){
				out.close();
			}
		}
		return null;
	}
	/**
	 * 公文文号大流水号删除
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView deleteMarkCategory(HttpServletRequest request, HttpServletResponse response) {	
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			String[] ids = request.getParameterValues("categoryId");
			for (int i = 0; i < ids.length; i++) {
				Long id = Long.valueOf(ids[i]);
				EdocMarkCategory category = govdocMarkManager.findById(id);
				if (category.isReadonly()) {
					boolean flag = govdocMarkManager.containMarkDefInCategory(id);				
					if (flag) {					
						out.println("<script>");
						out.println("alert(parent.v3x.getMessage('edocLang.big_stream_alter_used','"+category.getCategoryName()+"'))");
						out.println("</script>");
						continue;
					}
					else {
						govdocMarkManager.deleteCategory(id);
					}
				}
				else {
					govdocMarkManager.deleteCategory(id);
				}
			}
			out.println("<script>");
			out.println("parent.location.href = parent.location.href;");
			out.println("</script>");
		} catch(IOException e) {
			LOGGER.error("公文文号大流水号删除获取out出错", e);
		} catch (Exception e) {
			LOGGER.error("公文文号大流水号修改出错", e);
		} finally{
			if(out != null){
				out.close();
			}
		}
		
		return null;
	}
	/**
	 * 修改公文大流水号的下拉列表选项（新建公文文号和编辑公文文号界面）
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView changeMarkCategoryOptions(HttpServletRequest request, HttpServletResponse response) {
		String view = null;
		JSONArray jsonArray = new JSONArray();
		try {
			Long domainId = AppContext.getCurrentUser().getLoginAccount();
			List<EdocMarkCategory> categories = govdocMarkManager.findByTypeAndDomainId(MarkCategoryEnum.big.key(), domainId);
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
			LOGGER.debug("json: " + jsonArray.toString());
			
			boolean isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");
			if (isAjax) {
				view = this.getJsonView();
			}
		} catch (Exception e) {
			LOGGER.error("修改公文大流水号的下拉列表选项出错", e);
		}
		return new ModelAndView(view, AJAX_JSON, jsonArray);	
	}
	/****************************** 公文文号大小流水相关请求   end *******************************/
	

	/****************************** 公文文号预留文号相关请求 start *******************************/
	/**
	 * 设置预留文号
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	public ModelAndView openReserveDialog(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
		ModelAndView mav = new ModelAndView("govdoc/database/mark/reserve_dialog");
		Integer type = Strings.isBlank(request.getParameter("type")) ? 1 : Integer.parseInt(request.getParameter("type"));
		mav.addObject("type",type);
		Long markDefId = Strings.isBlank(request.getParameter("markDefId")) ? -1 : Long.parseLong(request.getParameter("markDefId"));
		EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(markDefId);
		if(markDef == null) {
			try {
				StringBuilder buffer = new StringBuilder();
				buffer.append("alert('"+ResourceUtil.getString("edoc.mark.isDelete")+"');");
				buffer.append("transParams.parentWin.location.reload();");
				buffer.append("top.close();");
				rendJavaScript(response, buffer.toString());
			} catch(Exception e) {}
			return null;
		}
		GovdocMarkVO markVo = govdocMarkManager.markDef2Mode(markDef,null,null); 
		EdocMarkReserveVO reserveVO = null;
		if(markDef != null) {
			reserveVO = govdocMarkManager.getMarkReserveByFormat(markDef, null);
			List<Long> reservedIdList = new ArrayList<Long>();
			List<EdocMarkReserveVO> reserveVOList = govdocMarkManager.findMarkReserveVoList(type, markDef, -1);
			List<EdocMarkReserveVO> reserveLineList = new ArrayList<EdocMarkReserveVO>();
			List<EdocMarkReserveVO> reserveDownList = new ArrayList<EdocMarkReserveVO>();
			if(Strings.isNotEmpty(reserveVOList)) {
				for(EdocMarkReserveVO vo : reserveVOList) {
					//是否是本年的编号。
					if(Strings.isNotEmpty(vo.getReserveNumberList())) {
					    if(EdocMarkUtil.isNeedExcludeDocMarkToSelect(markDef, vo.getReserveNumberList().get(0).getDocMark())) {
					    	 continue;
						}
					}
					if(vo.getType()!=null && vo.getType().intValue() == 1) {
						GovdocMarkVO newMarkVo = GovdocMarkHelper.parseDocMark(markVo, vo.getReserveNumberList().get(0).getDocMark());
						if(newMarkVo != null) {
							vo.setYearNo(newMarkVo.getYearNo());
							vo.setMarkNumber(newMarkVo.getMarkNumber());
						}
						reserveLineList.add(vo);
					} else {
						reserveDownList.add(vo);
					}
					if(type.intValue()==1 && vo.getType()!=null && vo.getType().intValue() == 1) {//线上占用
						reservedIdList.add(vo.getId());
					} else if(type.intValue()==2 && vo.getType()!=null && vo.getType().intValue() == 2) {//线下占用
						reservedIdList.add(vo.getId());
					}
				}
			}
			mav.addObject("reserveLineList", reserveLineList);
			mav.addObject("reserveDownList", reserveDownList);
			mav.addObject("reservedIdList", reservedIdList);
		}
		mav.addObject("reserveVO", reserveVO);
		mav.addObject("reserveToLabel", ResourceUtil.getString("edoc.oper.to"));
		return mav;
	}
	
	public ModelAndView listMarkReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer type = Strings.isBlank(request.getParameter("type")) ? 1 : Integer.parseInt(request.getParameter("type"));
		Integer queryNumber = Strings.isBlank(request.getParameter("queryNumber")) ? -1 : Integer.parseInt(request.getParameter("queryNumber"));
		Long markDefId = Strings.isBlank(request.getParameter("markDefId")) ? -1 : Long.parseLong(request.getParameter("markDefId"));
		EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(markDefId);
		JSONArray jsonArray = new JSONArray();
		if(markDef != null) {
			List<EdocMarkReserveVO> reserveVOList = govdocMarkManager.findMarkReserveVoList(type, markDef, queryNumber);
			if(Strings.isNotEmpty(reserveVOList)) {
				for(EdocMarkReserveVO reserveVO : reserveVOList) {
					boolean showFlag = false;
					if(type.intValue()==1 && reserveVO.getType().intValue() == 1) {//线上占用
						showFlag = true;
					} else if(type.intValue()==2 && reserveVO.getType().intValue() == 2) {//线下占用
						showFlag = true;
					}
					if(showFlag) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.putOpt("optionReservedId", String.valueOf(reserveVO.getId()));
						jsonObject.putOpt("docMarkDisplay", reserveVO.getDocMarkDisplay());
						jsonObject.putOpt("optionStartNo", reserveVO.getStartNo());
						jsonObject.putOpt("optionEndNo", reserveVO.getEndNo());
						//客开 项目名称： [修改功能：线下占用搜索加上备注] 作者：fzc 修改日期：2018-5-11 start
						jsonObject.putOpt("description",reserveVO.getEdocMarkReserve().getDescription());
						//客开 项目名称： [修改功能：线下占用搜索加上备注] 作者：fzc 修改日期：2018-5-11 end
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
		return new ModelAndView(view, AJAX_JSON, jsonArray);	
	}
	
	/**
	 * 添加预留文号
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes" , "unchecked" })
	public ModelAndView saveMarkReserve(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
		Map reserveDataMap =  ParamUtil.getJsonDomain("reserveData");
		Long markDefId = reserveDataMap.get("markDefId")==null ? -1 : Long.parseLong((String)reserveDataMap.get("markDefId"));
		
		Integer type = reserveDataMap.get("type")==null ? 1 : Integer.parseInt((String)reserveDataMap.get("type"));
		Map<String, String> map = ParamUtil.getJsonDomain("thisReservedIds");
		String[] addReservedIds = null;
		String[] thisReservedIds = null;
		String[] delReservedIds = null;
		String[] delReservedNos = null;
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
			List<Map<String,String>> delReserveNosMap =  ParamUtil.getJsonDomainGroup("delReserveIds");	
			if(Strings.isNotEmpty(delReserveNosMap)) {
				delReservedNos = new String [delReserveNosMap.size()];
				for(int i = 0; i<delReserveNosMap.size(); i++) {
					delReservedNos[i] = delReserveNosMap.get(i).get("delReservedNo");
				}
			}
		} else {
			delReservedIds = new String[1];
			delReservedIds[0] = map.get("delReservedId");
			delReservedNos = new String[1];
			delReservedNos[0] = map.get("delReservedNo");
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

		List<Long> delReservedIdList = new ArrayList<Long>();
        if(delReservedIds!=null && delReservedIds.length>0) {
            for(int i=0; i<delReservedIds.length; i++) {
                Long reservedId = Long.parseLong(delReservedIds[i]);
                if(!delReservedIdList.contains(reservedId)){
                    delReservedIdList.add(reservedId);
                }
            }
        }
        
        List<String> delReservedNoList = new ArrayList<String>();
        if(delReservedNos!=null && delReservedNos.length>0) {
        	for(int i=0; i<delReservedNos.length; i++) {
                if(!delReservedNoList.contains(delReservedNos[i])){
                	delReservedNoList.add(delReservedNos[i]);
                }
            }
        }
        
        EdocMarkDefinition markDef = govdocMarkManager.getMarkDef(markDefId);
		if(markDef == null) {
			try {
				PrintWriter out = response.getWriter();
				out.print("markDef_is_nul");
				out.close();
			} catch(Exception e) {
				LOGGER.error("", e);
			}
			return null;
		}
        
		if(addReservedIds!=null && addReservedIds.length>0) {
			List<Long> thisReserveIdList = new ArrayList<Long>();
			if(thisReservedIds!=null && thisReservedIds.length>0) {
				Collections.addAll(thisReserveIdList, thisReservedIds);
			}
	        for(int i=0; i<addReservedIds.length; i++) {
				String[] startAndEnd =  addReservedIds[i].split("-");
				int startNo = Integer.parseInt(startAndEnd[0]);
				int endNo = Integer.parseInt(startAndEnd[1]);
				boolean flag = govdocMarkManager.checkRepeatMarkReserved(markDef, startNo, endNo, thisReserveIdList, delReservedIdList, delReservedNoList);
				if(flag) {
					try {
						PrintWriter out = response.getWriter();
						out.print("repeat");
						out.close();
					} catch(Exception e) {}
					return null;
				}
			}
		}
        
		User user = AppContext.getCurrentUser();
		PrintWriter out = null;
		try {
			out = response.getWriter();
			if((addReservedIds!=null && addReservedIds.length>0) || Strings.isNotEmpty(delReservedIdList)) {
				List<EdocMarkDefinition> markDefList = new ArrayList<EdocMarkDefinition>();
		        if(markDef.getEdocMarkCategory().getCodeMode() == 0) {//小流水
		        	markDefList.add(markDef);
		        } else if(markDef.getEdocMarkCategory().getCodeMode() == 1) {//大流水
		        	markDefList.addAll(govdocMarkManager.getMarkDefsByCategory(markDef.getCategoryId()));
		        }
				for(EdocMarkDefinition bean : markDefList) {
					List<EdocMarkReserveVO> addReserveList = new ArrayList<EdocMarkReserveVO>();
					if(addReservedIds!=null && addReservedIds.length>0) {
						for(int i=0; i<addReservedIds.length; i++) {
							String[] startAndEnd =  addReservedIds[i].split("-");
							int startNo = Integer.parseInt(startAndEnd[0]);
							int endNo = Integer.parseInt(startAndEnd[1]);
							EdocMarkReserveVO reserveVO = new EdocMarkReserveVO();
							reserveVO.setMarkDefineId(markDefId);
							reserveVO.setType(type);
							reserveVO.setStartNo(startNo);
							reserveVO.setEndNo(endNo);
							reserveVO.setYearNo(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
							reserveVO.setCreateTime(DateUtil.currentTimestamp());
							reserveVO.setCreateUserId(user.getId());
							reserveVO.setDomainId(user.getLoginAccount());
							if(startAndEnd.length > 2) {
								reserveVO.setDescription(startAndEnd[2]);
							}
							reserveVO.setEdocMarkDefinition(markDef);
							reserveVO.setEdocMarkCategory(markDef.getEdocMarkCategory());
							addReserveList.add(reserveVO);
						}
					}
					govdocMarkManager.saveMarkReserve(user, type, bean, addReserveList, delReservedIdList, delReservedNoList);
				}
			}
		} catch(Exception e) {
			LOGGER.error("保存预留文号出错", e);
			/** 出现异常更新缓存 */
			govdocMarkManager.reloadCache();
		}
		
		try {
			out.print("success");
			out.flush();
			out.close();
		} catch(Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}
	/****************************** 公文文号预留文号相关请求   end *******************************/
	
	
	/****************************** 公文文号前端展现方法 start *******************************/
	/**
	 * 打开公文断号弹出框
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView openMarkChooseDialog(HttpServletRequest request,HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/dialog/mark_choose_dialog");
		try {
			response.setContentType("text/html;charset=UTF-8");
			String markType = request.getParameter("markType");
			Long templateMarkDefId = GovdocParamUtil.getLong(request, "templateMarkDefId");
			
			User user = AppContext.getCurrentUser();
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put("markType", markType);
			condition.put("userId", user.getId());
			condition.put("currentAccountId", user.getLoginAccount());
			condition.put("isAdmin", user.isAdmin());
			if(templateMarkDefId != null) {//系统模板查看断点
				condition.put("templateMarkDefId", templateMarkDefId);
			}
			List<GovdocMarkVO> markDefVoList = govdocMarkManager.getListByUserId(condition);
			
			if(Strings.isNotEmpty(markDefVoList)) {
				GovdocMarkVO markDefVo = markDefVoList.get(0);
				markDefVo.setMarkDef(govdocMarkManager.getMarkDef(markDefVo.getMarkDefId()));
				
				List<GovdocMarkVO> reserveVoList = govdocMarkManager.findReserveListByMarkDefId(1, markDefVo);
				mav.addObject("reserveVoList", reserveVoList);
				
				List<GovdocMarkVO> callVoList = govdocMarkManager.findCallVoListByMarkDefId(markDefVo);
				mav.addObject("callVoList", callVoList);
				
				mav.addObject("markDefVoList", markDefVoList);
			}
			
		} catch(Exception e) {
			
		}
		return mav;		
	}
	
	/**
	 * 公文断号弹出框，预留文号机构字切换
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView changeMarkDef(HttpServletRequest request,HttpServletResponse response) {
		JSONArray jsonArray = new JSONArray();
		boolean isAjax = true;
		try {
			response.setContentType("text/html;charset=UTF-8");
			isAjax = ServletRequestUtils.getRequiredBooleanParameter(request, "ajax");	
			Long markDefId = ServletRequestUtils.getRequiredLongParameter(request, "markDefId");
			int selectType = ServletRequestUtils.getRequiredIntParameter(request, "selectType");
			
			GovdocMarkVO markDefVo = govdocMarkManager.getVoByMarkDefId(markDefId);
			markDefVo.setMarkDef(govdocMarkManager.getMarkDef(markDefId));
			
			List<GovdocMarkVO> markVoList = null; 
			if(selectType == 2) {//断号
				markVoList = govdocMarkManager.findCallVoListByMarkDefId(markDefVo);
			} else {//预留文号
				markVoList = govdocMarkManager.findReserveListByMarkDefId(1, markDefVo);	
			}
			
			if(Strings.isNotEmpty(markVoList)) {
				for(GovdocMarkVO markVo : markVoList) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.putOpt("optionValue", markVo.getCallId()==null ? "" : markVo.getCallId().toString());
					jsonObject.putOpt("optionName", markVo.getMarkstr());
					jsonObject.putOpt("optionMarkDefId", markVo.getMarkDefId());
					jsonObject.putOpt("optionMarkNumber", markVo.getMarkNumber());
					jsonObject.putOpt("optionYearNo", markVo.getYearNo());
					jsonArray.put(jsonObject);
				}
			}
		} catch(Exception e) {
			LOGGER.error("公文断号弹出框出错", e);
		}
		LOGGER.debug("json: " + jsonArray.toString());
		String view = null;
		if (isAjax) {
			view = this.getJsonView();
		}
		return new ModelAndView(view, "json", jsonArray);
	}
		
	public String getJsonView() {
		if(Strings.isBlank(jsonView)) {
			jsonView = "jsonView";
		}
		return jsonView;
	}
	
	public void setJsonView(String jsonView) {
		this.jsonView = jsonView;
	}
	
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}
	public void setGovdocMarkOpenManager(GovdocMarkOpenManager govdocMarkOpenManager) {
		this.govdocMarkOpenManager = govdocMarkOpenManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	
}
