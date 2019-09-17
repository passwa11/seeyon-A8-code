package com.seeyon.apps.govdoc.controller;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文套红模板控制器
 * @author 唐桂林
 *
 */
public class GovdocDocTemplateController extends BaseController {

	private static final Log LOGGER = CtpLogFactory.getLog(GovdocDocTemplateController.class);
	
	private GovdocDocTemplateManager govdocDocTemplateManager;
	private GovdocLogManager govdocLogManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private GovdocSummaryManager govdocSummaryManager;
	
	/**
	 * 新公文套红模板列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/doctemplate/list");
		//套红模板的文件
		Long templateFileId = -6001972826857714844L;
		V3XFile v3xfile = fileManager.getV3XFile(templateFileId);
		if(v3xfile!=null){
			mav.addObject("fileId", templateFileId);
			mav.addObject("fileName", v3xfile.getFilename());
			mav.addObject("createDate", new Timestamp(v3xfile.getCreateDate().getTime()).toString().substring(0, 10));		
		}
		return mav;
	}
	

	/**
	 * 新公文套红模板新建界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView newTemplate(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView mav = new ModelAndView("govdoc/database/doctemplate/create");
		mav.addObject("operType", "add");
		mav.addObject("type", Functions.toHTML(request.getParameter("type")));
		return mav;
	}
	
	/**
	 * 新公文套红模板保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView add(HttpServletRequest request,HttpServletResponse response) {
		try {
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
			} catch(Exception e) {
				LOGGER.error("保存模板记录失败", e);
				String alertNote = ResourceUtil.getString("templete.saved.error");
				out.println("<script>alert('"+alertNote+"');</script>");
				out.close();
				return null;
			}
			
			String alert = govdocDocTemplateManager.addEdocTemplate(template);
			if(null!=alert && !"".equals(alert)) {
				try {
					attachmentManager.deleteByReference(template.getId());
				} catch(Exception e) {
					LOGGER.error("新增模板时抛出异常",e);
				}
				out.println(alert);
				out.close();
				return null;
			}
			
			if(null!=departmentId && !"".equals(departmentId)) {
				String[] departmentIds = departmentId.split(",");			
				govdocDocTemplateManager.saveEdocDocTemplateAcl(template.getId(),template.getId(), departmentIds);
				//记录日志
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_DOCTEMPLETECREATE.key(), user.getName(), name);
			}
			
			out.print("<script>");
			out.print("parent.parent.reloadList()");
			out.print("</script>");
			out.flush();
			out.close();
		} catch(Exception e) {
			LOGGER.error("公文套红模板新增保存出错", e);
		}
		return null;
	}
	
	/**
	 * 新公文套红模板编辑界面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("govdoc/database/doctemplate/create");
		
		Long id = Long.parseLong(request.getParameter("id"));
		EdocDocTemplate bean = govdocDocTemplateManager.getEdocDocTemplateById(id);
		if(bean == null) {
			return null;
		}
		
		List<Attachment> attachments = attachmentManager.getByReference(id, id);
		if(null!=attachments && attachments.size()>0) {//判断附件是否为空
			Attachment attachment = attachments.get(0);
			mav.addObject("attachments",attachments);	
			mav.addObject("fileName",attachment.getFilename());
			mav.addObject("fileId", attachment.getFileUrl());
			mav.addObject("fileName", attachment.getFilename());
			mav.addObject("createDate", new Timestamp(attachment.getCreatedate().getTime()).toString().substring(0, 10));
		}
		
		List<EdocDocTemplateAcl> edocDocTemplateAcl = govdocDocTemplateManager.getEdocDocTemplateAcl(String.valueOf(id));
		if(null!=edocDocTemplateAcl && edocDocTemplateAcl.size()>0) {
			mav.addObject("elements",edocDocTemplateAcl);
		}
		
		mav.addObject("type", bean.getType());
		mav.addObject("bean", bean);
		mav.addObject("operType", "change");
		return mav;
	}
	
	/**
	 * 新公文套红模板修改
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView change(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			Long id = Long.parseLong(request.getParameter("id"));
			String name = request.getParameter("name");
			String status = request.getParameter("status");
			String textType = request.getParameter("text_type");
			String departmentId = request.getParameter("grantedDepartId");
			if(!Strings.isBlank(departmentId)) {
				String[] departmentIds = departmentId.split(",");
				govdocDocTemplateManager.updateEdocDocTemplateAcl(id, id, departmentIds);
			} else {
				govdocDocTemplateManager.deleteAclByTemplateId(id);
			}
	
			User user =AppContext.getCurrentUser();
			
			EdocDocTemplate bean = govdocDocTemplateManager.getEdocDocTemplateById(id);
			//是否重名
			boolean hasName = govdocDocTemplateManager.checkHasName(bean.getType(), name, id, user.getLoginAccount());
			if(hasName) {
				out.println("<script>");
				out.println("alert(parent.v3x.getMessage('edocLang.templete_alertRepeatName'));");
				out.println("</script>");
				out.flush();
				out.close();
				return null;
			}
			
		    //记录日志
			govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_DOCTEMPLETEAUTHORIZE.key(), user.getName(), name);
			List<Attachment> attachments = attachmentManager.getByReference(id, id);
			if(attachments!=null) {
				attachmentManager.update(ApplicationCategoryEnum.edoc, id, id, request);
			} else {
				attachmentManager.create(ApplicationCategoryEnum.edoc, id, id);
			}
			bean.setStatus(Integer.parseInt(status));
			bean.setTextType(textType);
			
			String alert = govdocDocTemplateManager.modifyEdocTemplate(bean, name);			
			if(null!=alert && !"".equals(alert)) {
				out.println(alert);
				out.flush();
				out.close();
				return null;
			}
			out.print("<script>");
			out.print("parent.parent.reloadList()");
			out.print("</script>");
			out.flush();
		} catch(Exception e) {
			LOGGER.error("公文套红模板修改出错", e);
		} finally {
			if(out != null){
				out.close();
			}
		}
		return null;
	}
	
	/**
	 * 新公文套红模板删除
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView delete(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			String id = request.getParameter("id");
			String[] ids = id.split(",");
			
			User user = AppContext.getCurrentUser();
			List<Long> list = new ArrayList<Long>();
			for(int i=0; i<ids.length; i++) {
				list.add(Long.valueOf(ids[i]));
				EdocDocTemplate templete = govdocDocTemplateManager.getEdocDocTemplateById(Long.valueOf(ids[i]));
				//记录日志
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_DOCTEMPLETE_DELETE.key(), user.getName(), templete.getName());
			}
			govdocDocTemplateManager.deleteEdocTemtlate(list);
			
			out = response.getWriter();
			out.print("<script>");
			out.print("location.href='doctemplate.do?method=list';");
			out.print("</script>");
			out.flush();
		} catch(Exception e) {
			LOGGER.error("删除套红模板出错", e);
		} finally {
		  if(out != null){
			  out.close();
		  }
		}
		return null;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
    public ModelAndView checkDocTemplateExist(HttpServletRequest request, HttpServletResponse response){
    	PrintWriter out = null;
    	 try {
    		 out = response.getWriter();
    		 String isFromAdmin = request.getParameter("isFromAdmin");
    		 String templateType = request.getParameter("templateType");
    		 String bodyType = request.getParameter("bodyType");
    		 Long orgAccountId = Long.parseLong(request.getParameter("orgAccountId"));
    		 String result = govdocDocTemplateManager.hasEdocDocTemplate(isFromAdmin, orgAccountId, templateType, bodyType);
    		 out.write(result);
    	 } catch (Exception e) {
			LOGGER.error(e);
			if(out != null){
				out.write("false");
			}
		} finally {
			if(out != null) {
				out.close();	
			}
		}
    	return null;
    }

	/**
	 * 套红
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView taoHong(HttpServletRequest request,HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/dialog/govdocTemplate_taohong");
		try {
			String templateType = request.getParameter("templateType");
			String bodyType = request.getParameter("bodyType");
			String orgAccountId = request.getParameter("orgAccountId");
			String isFromAdmin = request.getParameter("isAdmin");
			List<EdocDocTemplate> list = govdocDocTemplateManager.getEdocDocTemplateList(isFromAdmin, Long.parseLong(orgAccountId), AppContext.getCurrentUser(), templateType, bodyType);
		    if(null==list || list.size()==0) {
		    	mav.addObject("haveRecord", true);
		    	return mav;
		    }
			mav.addObject("templateList", list);
		} catch(Exception e) {
			LOGGER.error("套红出错", e);
		}
		return mav;
	}	
	
	/**
	 * 进入套红主页
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView taoHongEntry(HttpServletRequest request,HttpServletResponse response)throws Exception{
		ModelAndView  mav = new ModelAndView("govdoc/dialog/govdocTemplate_taohong_iframe");
		mav.addObject("templateType", request.getParameter("templateType"));
		mav.addObject("bodyType", request.getParameter("bodyType"));
		mav.addObject("isAdmin", request.getParameter("isAdmin"));
		mav.addObject("orgAccountId", request.getParameter("orgAccountId"));
		return mav;
	}
	
	/**
	 * 文单模板套红调整页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	 public ModelAndView govdocwendanTaohongIframe(HttpServletRequest request,HttpServletResponse response) throws Exception {
    	ModelAndView mv = new ModelAndView("govdoc/dialog/govdocwendantaohongIframe");
    	String s_summaryId = request.getParameter("summaryId");
        mv.addObject("summaryId", s_summaryId);
        String tempContentType = request.getParameter("tempContentType");
        mv.addObject("tempContentType", tempContentType);
    	return mv;
    }
	 
	/**
	 * 文单模板套红
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	 public ModelAndView govdocwendanTaohong(HttpServletRequest request,HttpServletResponse response) throws Exception {
    	ModelAndView mv = new ModelAndView("govdoc/dialog/govdocwendantaohong");
        mv.addObject("tempContentType", request.getParameter("tempContentType"));
        
        String s_summaryId = request.getParameter("summaryId");
        if(Strings.isNotBlank(s_summaryId)) {
			long summaryId = Long.parseLong(s_summaryId);
			EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
			mv.addObject("summary", summary);
        }
    	return mv;
    }
	    
	@SuppressWarnings("unused")
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
		
	public void setGovdocDocTemplateManager(GovdocDocTemplateManager govdocDocTemplateManager) {
		this.govdocDocTemplateManager = govdocDocTemplateManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}


	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
}
