package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;

@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
public class EdocElementController extends BaseController{

    private static final Log log = LogFactory.getLog(EdocElementController.class);

	private OrgManager orgManager;

	
	private com.seeyon.ctp.common.ctpenumnew.manager.EnumManager enumManagerNew;

	private EdocElementManager edocElementManager;

	private FileToExcelManager fileToExcelManager;
	
	private AppLogManager appLogManager;


    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }
    
	public FileToExcelManager getFileToExcelManager() {
		return fileToExcelManager;
	}

	public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {		this.fileToExcelManager = fileToExcelManager;
	}

	public EdocElementManager getEdocElementManager() {
		return edocElementManager;
	}

	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	

	public com.seeyon.ctp.common.ctpenumnew.manager.EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}

	public void setEnumManagerNew(
			com.seeyon.ctp.common.ctpenumnew.manager.EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}

	@Override
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("edoc/elementManage/element_list_main");
		if(request.getParameter("id")!=null)
			mav.addObject("id", request.getParameter("id"));
		return mav;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//处理查询条件
		List<EdocElement> list = null;
		String condition=request.getParameter("condition");
		String textfield = request.getParameter("textfield");
		String statusSelect = request.getParameter("statusSelect");
		Integer startIndex = 0;
		Integer first = 0;
		Integer pageSize = 0;
		Integer listCount = 0;

		//有条件返回的查询,带分页
		if(StringUtils.isNotBlank(condition)){
			list = edocElementManager.getEdocElementsByContidion(condition,textfield,statusSelect,1);
		}
		//没有条件返回的查询
		else{
			listCount = edocElementManager.getAllEdocElementCount();
			Pagination.setRowCount(listCount);
			first = Pagination.getFirstResult();
			pageSize = Pagination.getMaxResults();
			if ((first+1) % pageSize == 0){
				startIndex = first / pageSize;
			}
			else{
				startIndex = first / pageSize + 1;
			}
			if (pageSize == 1) startIndex = (first+1) / pageSize;
				list = edocElementManager.getAllEdocElements(startIndex,pageSize);
		}

		ModelAndView ret = new ModelAndView("edoc/elementManage/element_list_iframe");
		if(StringUtils.isNotBlank(condition)){
			if (("elementStatus".equals(condition)) && (StringUtils.isNotBlank(statusSelect))) {
				ret.addObject("statusSelect", Integer.parseInt(statusSelect));
				ret.addObject("condition", condition);
			}else {
				ret.addObject("condition", condition);
				ret.addObject("textfield", textfield);
			}
		}
		else{
			ret.addObject("condition", null);
		}
		if(EdocHelper.isG6Version()){
			for(EdocElement e: list){
				if("edoc.element.dengji".equals(e.getName())){
					e.setName("edoc.element.fenfa");
					break;
				}
			}
		}

		ret.addObject("list", list);
		ret.addObject("canEditEdocElements", GovdocRoleHelper.canEditEdocElements());
		return ret;
	}

	/**
	 * 进入公文元素编辑界面。
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response) throws Exception {

		EdocElement bean = null;
		String idStr = request.getParameter("id");

		bean = edocElementManager.getEdocElement(idStr);

		//List<Metadata> edocMetadata = enumManager.getExtendMetadatas(ApplicationCategoryEnum.edoc);
		//Collection collection = enumManager.getAllMetadatas();
		/*
		Collection collection = enumManager.getAllSystemMetadatas();
		List<Metadata> edocMetadata = new ArrayList<Metadata>();
		if(null!=collection && collection.size()>0){
			edocMetadata.addAll(collection);
		}
		*/
		List<CtpEnumBean> edocMetadata = enumManagerNew.getAllEnumType();
		//enumManager.getUserDefinedItem(bean.getMetadataId());
		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		 String metadataName = ResourceBundleUtil.getString(resource, local, "edoc.element.chooseMetadata");

		if(bean.getMetadataId() !=null){

			if(enumManagerNew.getEnum(bean.getMetadataId()) !=null){
			    CtpEnumBean metadata = enumManagerNew.getEnum(bean.getMetadataId()) ;
				metadataName = ResourceBundleUtil.getString(metadata.getResourceBundle(), local, metadata.getLabel());
			}

			if(Strings.isBlank(metadataName) && enumManagerNew.getEnum(bean.getMetadataId()) !=null)
				metadataName = enumManagerNew.getEnum(bean.getMetadataId()).getDescription();

		}
		ModelAndView mav = new ModelAndView("edoc/elementManage/type_create");
		mav.addObject("edocMetadata", edocMetadata);
		mav.addObject("bean", bean);
		mav.addObject("metadataName", metadataName);

		return mav;
	}

	/**
	 * 修改公文元素信息。
	 */
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView update(HttpServletRequest request, HttpServletResponse response) throws Exception {

		EdocElement bean = null;
		String idStr = request.getParameter("id");
		String status = request.getParameter("status");
		String name = request.getParameter("name");
		String metadataId = request.getParameter("metadataId");
		String metadataName = request.getParameter("metadataName");

		bean = edocElementManager.getEdocElement(idStr);

		if(null!=status && !"".equals(status)){
			bean.setStatus(Integer.valueOf(status));
		}

		if(!bean.getIsSystem()){//如果是系统元素，则不能更改名字，反之更改
			bean.setName(name);
		}
		if (StringUtils.isNotBlank(metadataId)) {
			bean.setMetadataId(Long.parseLong(metadataId));
			CtpEnumBean ctpEnum=enumManagerNew.getEnum(Long.parseLong(metadataId));
			if(ctpEnum == null){
				String outMsg = ResourceUtil.getString("form.formenum.enums");
				String stratMsg = ResourceUtil.getString("InputField.InputSelect.enumvaluenotexist");
				response.setContentType("text/html;charset=UTF-8");
	    		PrintWriter pw = response.getWriter();
	    		pw.println("<script>");
	    		pw.println("alert(\""+outMsg+metadataName+stratMsg+"\");");
	    		pw.println("history.back()");
	    		pw.println("</script>");
	    		return null;
			}
			updateEnumRef(ctpEnum);
		}else if(bean.getIsSystem()){
			//如果是系统元素，则不对metadata做任何操作，已经将系统元素的关联代码初始化到数据中。
		}
		else {
			bean.setMetadataId(null);
		}
		edocElementManager.updateEdocElement(bean);
		//记录应用日志
        User user=AppContext.getCurrentUser();
        appLogManager.insertLog(user, AppLogAction.Edoc_Element_Update, user.getName(),bean.getName());

		//ModelAndView mav = new ModelAndView("edoc/elementManage/element_list_main");
		//mav.addObject("id", status);

		//return mav;
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent.parent.listFrame");
	}
	/**
	 * 将枚举以及枚举值的引用状态改为“是”
	 * @param ctpEnum
	 * @throws BusinessException
	 */
	private void updateEnumRef(CtpEnumBean ctpEnum) throws BusinessException{
		enumManagerNew.refEnum(ctpEnum);
		List<com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem> items=enumManagerNew.getAllEnumItem();
		for(com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem item:items){
			if(null!=item && null!= ctpEnum && null!=item.getRefEnumid() 
					&& item.getRefEnumid().equals(ctpEnum.getId())){
					enumManagerNew.updateEnumItemRef(ctpEnum.getId(), item.getId());
			}
		}
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView changeStatus(HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		String[] sIds = request.getParameterValues("ids");
		int status = Integer.valueOf(request.getParameter("status"));
		User user=AppContext.getCurrentUser();
		if (sIds != null && sIds.length > 0) {
			for (int i = 0; i < sIds.length; i++) {
				EdocElement element = edocElementManager.getEdocElement(sIds[i]);
				element.setStatus(status);
				edocElementManager.updateEdocElement(element);
				if(status ==0){
				    appLogManager.insertLog(user, AppLogAction.Edoc_Element_Stop, user.getName(),element.getName());
				}else{
				    appLogManager.insertLog(user, AppLogAction.Edoc_Element_Start, user.getName(),element.getName());
				}
			}
		}

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script>");
		//out.print("parent.parent.listFrame.location.reload(true);");
		out.print("setTimeout(function(){parent.parent.listFrame.location.href = parent.parent.listFrame.location.href;}, 10)");
		out.println("</script>");
		return null;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView exportElementToExcel(HttpServletRequest request, HttpServletResponse response)throws Exception{
		String exportValue = request.getParameter("exportValue");
		String conditionValue=request.getParameter("conditionValue");
		String statusSelect = request.getParameter("statusSelectValue");
		List<EdocElement> elementList = null;
		if(null  == exportValue || "".equals(exportValue)){
			//无查询条件导出
			elementList = edocElementManager.getAllEdocElements();
		}else
		{
			//查询条件导出,不带分页
			elementList = edocElementManager.getEdocElementsByContidion(exportValue,conditionValue,statusSelect,0);
		}

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
		String element_title = ResourceBundleUtil.getString(resource, local, "edoc.element.code.reflection"); //标题

    	DataRecord dataRecord = EdocHelper.exportEdocElement(request, elementList, element_title);
//		OrganizationHelper.exportToExcel(request, response, fileToExcelManager, element_title, dataRecord);
//    	EdocHelper.exportToExcel(request, response, fileToExcelManager, element_title, dataRecord);
    	fileToExcelManager.save(response, element_title, dataRecord);
    	return null;

	}

}
