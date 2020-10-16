package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.manager.EdocDocTemplateAclManager;
import com.seeyon.v3x.edoc.manager.EdocDocTemplateManager;
import com.seeyon.v3x.edoc.manager.EdocHelper;

@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.EdocManagement})
public class EdocDocTemplateController extends BaseController{
	
	private static final Log log = LogFactory.getLog(EdocDocTemplateController.class);
	
	private EdocDocTemplateManager edocDocTemplateManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private EdocDocTemplateAclManager edocDocTemplateAclManager;
	private AppLogManager appLogManager;
	
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public EdocDocTemplateAclManager getEdocDocTemplateAclManager() {
		return edocDocTemplateAclManager;
	}
	public void setEdocDocTemplateAclManager(
			EdocDocTemplateAclManager edocDocTemplateAclManager) {
		this.edocDocTemplateAclManager = edocDocTemplateAclManager;
	}
	public EdocDocTemplateManager getEdocDocTemplateManager() {
		return edocDocTemplateManager;
	}
	public void setEdocDocTemplateManager(
			EdocDocTemplateManager edocDocTemplateManager) {
		this.edocDocTemplateManager = edocDocTemplateManager;
	}
	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	
	@Override
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView listMain(HttpServletRequest request, HttpServletResponse response) throws Exception {		
		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_list_main");
		if(request.getParameter("id")!=null)
			mav.addObject("id", request.getParameter("id"));
		return mav;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_list_iframe");
		List<EdocDocTemplate> list=null;
	
		String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
		list = edocDocTemplateManager.findAllTemplate(condition,textfield);
		
		mav.addObject("list", list);
		
		//套红模板的文件
		V3XFile v3xfile=fileManager.getV3XFile(-6001972826857714844L);
		
		if(v3xfile!=null){
    		mav.addObject("fileId", -6001972826857714844L);
    		mav.addObject("fileName", v3xfile.getFilename());
    		mav.addObject("createDate", new Timestamp(v3xfile.getCreateDate().getTime()).toString().substring(0, 10));		
		}
		
		return mav;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		EdocDocTemplate bean = null;
		Attachment attachment = null;
		String idStr=request.getParameter("id");
		
		bean = edocDocTemplateManager.getEdocDocTemplateById(Long.parseLong(idStr));
		if(null==bean)return null;
		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_modify");
		mav.addObject("type", bean.getType());
		List<Attachment> attachments = attachmentManager.getByReference(Long.valueOf(idStr), Long.valueOf(idStr));
		List<EdocDocTemplateAcl> edocDocTemplateAcl = edocDocTemplateAclManager.getEdocDocTemplateAcl(idStr);
		if(null!=attachments && attachments.size()>0){//判断附件是否为空
		attachment = attachments.get(0);
		mav.addObject("attachments",attachments);	
		mav.addObject("fileName",attachment.getFilename());
		mav.addObject("fileId", attachment.getFileUrl());
		mav.addObject("fileName", attachment.getFilename());
		mav.addObject("createDate", new Timestamp(attachment.getCreatedate().getTime()).toString().substring(0, 10));
		}
		mav.addObject("bean", bean);
		if(null!=edocDocTemplateAcl && edocDocTemplateAcl.size()>0){
		mav.addObject("elements",edocDocTemplateAcl);
		}
		
		mav.addObject("operType", "change");

		return mav;
	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView change(HttpServletRequest request,HttpServletResponse response)throws Exception{		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_list_main");
		User user =AppContext.getCurrentUser();
		EdocDocTemplate bean = null;
		
		String idStr=request.getParameter("id");
		String name = request.getParameter("name");
		
		String status = request.getParameter("status");
		String departmentId = request.getParameter("grantedDepartId");
		String textType = request.getParameter("text_type");
		
		if(!Strings.isBlank(departmentId)){
			
		String[] departmentIds = departmentId.split(",");

			edocDocTemplateAclManager.updateEdocDocTemplateAcl(Long.valueOf(idStr),Long.valueOf(idStr), departmentIds);
		}else{
			edocDocTemplateAclManager.deleteAclByTemplateId(Long.valueOf(idStr));
		}

		bean = edocDocTemplateManager.getEdocDocTemplateById(Long.parseLong(idStr));
		int type = bean.getType();
		//-start-如果启用已经被停用的模板 ? enabled = true : = false;
		//boolean enabled = false; //判断是否是从禁用-->启用,如果是:判断有没有同名的模板
		boolean hasName = false; //是否重名
		/*if(bean.getStatus() == Constants.EDOC_DOCTEMPLATE_DISABLED && Integer.valueOf(status) == Constants.EDOC_DOCTEMPLATE_ENABLED){
			enabled = true;
		}
		if(enabled){*/
		hasName = edocDocTemplateManager.checkHasName(type, name,Long.valueOf(idStr),AppContext.getCurrentUser().getLoginAccount());
		if(hasName){
			out.println("<script>");
			out.println("alert(parent._('edocLang.templete_alertRepeatName'));");
			out.println("</script>");
			out.flush();
			out.close();
			return null;
		}
		//}
		//-end-
		
	      //记录日志
        appLogManager.insertLog(user, AppLogAction.Edoc_DocTempleteAuthorize, user.getName(), name);
		
		List<Attachment> attachments = attachmentManager.getByReference(Long.valueOf(idStr), Long.valueOf(idStr));
		if(attachments!=null){
			attachmentManager.update(ApplicationCategoryEnum.edoc, Long.valueOf(idStr), Long.valueOf(idStr), request);
		}else{
			attachmentManager.create(ApplicationCategoryEnum.edoc, Long.valueOf(idStr), Long.valueOf(idStr));
		}
		//bean.setType(Integer.parseInt(type));不能修改模版的类型
		bean.setStatus(Integer.parseInt(status));
		bean.setTextType(textType);
		
		String alert = edocDocTemplateManager.modifyEdocTemplate(bean, name);		
		if(null!=alert && !"".equals(alert)){
			out.println(alert);
			out.flush();
			out.close();
			return null;
		}

		mav.addObject("id",idStr);
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent.parent");
	}
	
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView delete(HttpServletRequest request,HttpServletResponse response)throws Exception{
		String id = request.getParameter("id");
		
		String[] ids = id.split(",");
		
		User user = AppContext.getCurrentUser();
		List<Long> list = new ArrayList<Long>();
		for(int i=0;i<ids.length;i++){
			list.add(Long.valueOf(ids[i]));
			EdocDocTemplate templete = edocDocTemplateManager.getEdocDocTemplateById(Long.valueOf(ids[i]));
            appLogManager.insertLog(user, AppLogAction.Edoc_DocTemplete_Delete, user.getName(), templete.getName());
		}		
		edocDocTemplateManager.deleteEdocTemtlate(list);
        
		return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");

	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView newTemplate(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_modify");
		
		mav.addObject("operType", "add");
		mav.addObject("type", Functions.toHTML(request.getParameter("type")));
		
		return mav;

	}
	@CheckRoleAccess(roleTypes = { Role_NAME.EdocManagement,Role_NAME.AccountAdministrator})
	public ModelAndView add(HttpServletRequest request,HttpServletResponse response)throws Exception{

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		User user = AppContext.getCurrentUser();
		
		String name = request.getParameter("name");
		String type = request.getParameter("type");
		String status = request.getParameter("status");
		String departmentId = request.getParameter("grantedDepartId");	
		String textType = request.getParameter("text_type");
		
		String description = "";
	
		Long templateId = 1L;
		
		//--end
		
		EdocDocTemplate template = new EdocDocTemplate();
		template.setIdIfNew();
		template.setDescription(description);
		template.setName(name);
		template.setType(Integer.valueOf(type));
		template.setStatus(Integer.parseInt(status));
		template.setCreateUserId(user.getId());
		template.setDomainId(user.getLoginAccount());
		template.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
		template.setLastUpdate(new java.sql.Timestamp(new Date().getTime()));
		template.setLastUserId(user.getLoginAccount());
		template.setTemplateFileId(templateId);
		template.setTextType(textType);
		
		try{
			attachmentManager.create(ApplicationCategoryEnum.edoc, template.getId(), template.getId(), request);
		}catch(Exception e){
//			log.error("保存模板记录失败",e);
			String alertNote = ResourceUtil.getString("templete.saved.error");
						
			out.println("<script>alert('"+alertNote+"');</script>");
			out.close();
			return null;			
		}
		
		String alert = edocDocTemplateManager.addEdocTemplate(template);

		if(null!=alert && !"".equals(alert)){
			try {
				attachmentManager.deleteByReference(template.getId());
			}catch(Exception e) {
				log.error("新增模板时抛出异常",e);
			}
			out.println(alert);
			out.close();
			return null;
		}
		
		if(null!=departmentId && !"".equals(departmentId)){
			String[] departmentIds = departmentId.split(",");			
				
				edocDocTemplateAclManager.saveEdocDocTemplateAcl(template.getId(),template.getId(), departmentIds);
//				记录日志
				appLogManager.insertLog(user, AppLogAction.Edoc_DocTempleteCreate, user.getName(), name);
			//	edocDocTemplateAclManager.saveEdocDocTemplateAcl(Long.valueOf(idStr),Long.valueOf(idStr), departmentIds);
			
		}
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj","parent.parent");
	}
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView taoHong(HttpServletRequest request,HttpServletResponse response)throws Exception{
		User user = AppContext.getCurrentUser();
		
		String edocType = request.getParameter("templateType");
		String bodyType = request.getParameter("bodyType");
		//公文所属单位
		String orgAccountId = request.getParameter("orgAccountId");
		
		ModelAndView mav = new ModelAndView("edoc/docTemplate/docTemplate_taohong");	
		
		List<EdocDocTemplate> list = EdocHelper.getEdocDocTemplate(Long.parseLong(orgAccountId),user,edocType,bodyType);
	
		
		if(null==list || list.size()==0){
			mav.addObject("haveRecord", true);
			return mav;
		}
				
		mav.addObject("templateList", list);
		
		return mav;
	}	
	/**
	 * Ajax前台页面调用，判断是否存在套红模板
	 * @param edocType 类型（正文/文单）
	 * @param bodyType Officeword:word正文/Wpsword:wps正文
	 * @return "0":没有套红模板，“1”：有套红模板
	 */
	public String hasEdocDocTemplate(Long orgAccountId,String edocType,String bodyType){
		String ret="";
		User user = AppContext.getCurrentUser();
		try{
			List<EdocDocTemplate> list = EdocHelper.getEdocDocTemplate(orgAccountId,user,edocType,bodyType);

			if(null==list || list.size()==0) ret="0";
			else ret="1";
		}catch(Exception e){
			StringBuilder parameter=new StringBuilder();
			parameter.append("(");
			parameter.append("edocType=").append(edocType);
			parameter.append("bodyType=").append(bodyType);
			parameter.append("userId=").append(user.getId());
			parameter.append(")");
			log.error("ajax获取套红模板列表异常："+parameter.toString()+e.getMessage());
		}
		return ret;
	}
	
	@CheckRoleAccess(roleTypes={Role_NAME.NULL})
	public ModelAndView taoHongEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
		
		ModelAndView  mav = new ModelAndView("edoc/docTemplate/docTemplate_taohong_iframe");
		mav.addObject("templateType", request.getParameter("templateType"));
		mav.addObject("bodyType", request.getParameter("bodyType"));
		return mav;
		
	}
	
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
	public FileManager getFileManager() {
		return fileManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	
}