package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.constants.EdocCategoryStoreTypeEnum;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.manager.EdocCategoryManager;
import com.seeyon.v3x.edoc.util.Constants;
@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
public class EdocCategoryController extends BaseController {
	private static final Log LOGGER = LogFactory.getLog(EdocCategoryController.class);
	
	private EdocCategoryManager edocCategoryManager;
	private OrgManager orgManager;
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
		this.edocCategoryManager = edocCategoryManager;
	}

	public ModelAndView listMain(HttpServletRequest request,HttpServletResponse response) throws Exception{
		return new ModelAndView("edoc/edocCategory/listMain");
	} 
	
	public ModelAndView main(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ModelAndView mav = new ModelAndView("edoc/edocCategory/main");
		String condition = request.getParameter("condition");
		String textfield = request.getParameter("textfield");
		V3xOrgMember admin = this.orgManager.getAdministrator(AppContext.currentAccountId());
		//修改人、修改时间是v5.1新增字段，不采用升级数据方式，如果老数据中没有这两个值，默认为单位管理员，时间为2014年1月1日
		mav.addObject("defaultModifyUser", admin.getId());
		mav.addObject("defaultModifTime", Datetimes.parse("2014-01-01 00:00"));
		List<EdocCategory> list = this.edocCategoryManager.getCategoryByRoot(Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()), AppContext.getCurrentUser().getLoginAccount(),condition,textfield,true);
		return mav.addObject("list", list);
	}
	public ModelAndView checkUsed(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String strIds = request.getParameter("ids");
		if(Strings.isBlank(strIds)){
			super.rendText(response, "{}");
			return null;
		}
		String[] idArr = strIds.split(",");
		Long[] ids = new Long[idArr.length];
		try{
			for(int index=0;index<idArr.length;index++){
				ids[index] = Long.valueOf(idArr[index]);
			}
		}catch(Exception mye){
			super.rendText(response, "{'error':'"+ids+"'}");
			return null;
		}
		List<Long[]> id_count = this.edocCategoryManager.findCountOfCategory(ids);
		if(null == id_count){
			super.rendText(response, "{}");
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for(int index=0;index<id_count.size();index++){
				if(index>0)
					sb.append(",");
				sb.append("'").append(id_count.get(index)[0]).append("':'").append(id_count.get(index)[1]).append("'");
			}
			sb.append("}");
			super.rendText(response, sb.toString());
		}
		return null;
	}
	
	public ModelAndView detail(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ModelAndView mav = new ModelAndView("edoc/edocCategory/detail");
		String actionType = request.getParameter("actionType");
		if("editCategory".equals(actionType) || "showCategory".equals(actionType)){
			String id = request.getParameter("id");
			EdocCategory category = this.edocCategoryManager.getCategoryById(Long.parseLong(id));
			mav.addObject("category", category);
			mav.addObject("onlyShow", "showCategory".equals(actionType));
		}
		return mav;
	}
	
	public ModelAndView save(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String id = request.getParameter("id");
		String categoryName = request.getParameter("categoryName");
		Long userId = AppContext.currentUserId();
		Long accountId = AppContext.getCurrentUser().getLoginAccount();     
		Date now = new Date();
		if(Strings.isBlank(id)){
			this.edocCategoryManager.saveCategory(categoryName, Long.valueOf(ApplicationCategoryEnum.edocSend.getKey()), accountId, Constants.SubEdocCategory.normal.ordinal(), EdocCategoryStoreTypeEnum.USER_DEFINED.ordinal(), userId, now);
		}else{
			this.edocCategoryManager.updateCategory(Long.parseLong(id), categoryName, userId, now);
		}
		PrintWriter out = response.getWriter();
		out.print("<script>");
		out.print("parent.location.reload(true)");
		out.print("</script>");
		return null;
	}
	
	public ModelAndView deleteCategory(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String[] ids = request.getParameterValues("id");
		if(ids != null && ids.length >0){
			List<Long> list = new ArrayList<Long>(ids.length);
			for(String id:ids){
				list.add(Long.parseLong(id));
			}
			this.edocCategoryManager.deleteCategoryById(list);
		}
		PrintWriter out = response.getWriter();
		out.print("<script>");
		out.print("parent.location.reload(true)");
		out.print("</script>");
		return null;
	}
}
