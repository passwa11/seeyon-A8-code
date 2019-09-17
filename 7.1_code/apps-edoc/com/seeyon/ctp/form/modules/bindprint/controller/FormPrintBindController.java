package com.seeyon.ctp.form.modules.bindprint.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.modules.bindprint.manager.FormPrintBindManager;
import com.seeyon.ctp.form.po.FromPrintBind;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;

public class FormPrintBindController extends BaseController {
	
	private FormPrintBindManager formPrintBindManager;
	private FileManager fileManager;
	private FormApi4Cap3 formApi4Cap3;
	private static final Log LOGGER = LogFactory.getLog(FormPrintBindController.class);

    /**
     * 打印模版绑定
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView printModeSet(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	ModelAndView mav = new ModelAndView("ctp/form/design/fieldDesign/printModeSet");
    	FormBean fb = formApi4Cap3.getEditingForm();
    	Long unitId = AppContext.getCurrentUser().getAccountId();
    	mav.addObject("mode", false);
    	mav.addObject("v","");
    	mav.addObject("fileId","");
    	mav.addObject("createDate","");
    	mav.addObject("filename","");
    	mav.addObject("unitId",unitId);
    	mav.addObject("formId",fb.getId());
    	FormPrintBindManager formPrintBindManager = (FormPrintBindManager) AppContext.getBean("formPrintBindManager");
    	FromPrintBind edocPrintMode = formPrintBindManager.findPrintMode(unitId, fb.getId());
    	if(edocPrintMode!=null){
    		V3XFile v3xFile = fileManager.getV3XFile(edocPrintMode.getFileUrl());
    		if (v3xFile != null) {
    			String v = SecurityHelper.digest(v3xFile.getId());
    			mav.addObject("mode", true);
    			mav.addObject("v",v);
    			mav.addObject("fileId",v3xFile.getId());
    			mav.addObject("createDate",new SimpleDateFormat("yyyy-MM-dd").format(v3xFile.getCreateDate()));
    			mav.addObject("filename",v3xFile.getFilename());
    			mav.addObject("filesize",v3xFile.getSize());
			}
    	}
    	return mav;
    }

	/**
	 * desc 打印单模板数据保存数据库,并回显前台页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public void insertOrUpdatePrintMode(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String att_fileUrl = request.getParameter("att_fileUrl");
		String att_createDate = request.getParameter("att_createDate");
		String att_fileName = request.getParameter("att_filename");
		String att_size = request.getParameter("att_size");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		User user = AppContext.getCurrentUser();
		long unitId = user.getAccountId();
		V3XFile v3xfile =null;
		
		if(Strings.isNotBlank(att_fileUrl)){
			v3xfile = fileManager.getV3XFile(Long.parseLong(att_fileUrl));
			FormBean form = formApi4Cap3.getEditingForm();
			if(v3xfile==null){
				v3xfile = new V3XFile();
				v3xfile.setId(Long.parseLong(att_fileUrl));
				v3xfile.setAccountId(unitId);
				v3xfile.setCategory(4);
				v3xfile.setFilename(att_fileName);
				v3xfile.setMimeType("application/msword");
				v3xfile.setType(0);
				v3xfile.setCreateMember(user.getId());
				v3xfile.setCreateDate(new Timestamp(sdf.parse(att_createDate).getTime()));
				v3xfile.setUpdateDate(new Timestamp(sdf.parse(att_createDate).getTime()));
				v3xfile.setSize(Long.parseLong(att_size));
				try{
					fileManager.save(v3xfile);
				}catch (Exception e) {
					LOGGER.error(e);
				}
			}
			FromPrintBind proEdocPrint = formPrintBindManager.findPrintMode(unitId, form.getId());
			if(proEdocPrint==null){
				proEdocPrint = new FromPrintBind();
				proEdocPrint.setIdIfNew();
				proEdocPrint.setFileUrl(Long.valueOf(att_fileUrl));
				proEdocPrint.setUnitId(unitId);
				proEdocPrint.setFileName(att_fileName);
				proEdocPrint.setEdocXsnId(form.getId());
				proEdocPrint.setFileCreateTime(new Timestamp(sdf.parse(att_createDate).getTime()));
			}else{
				proEdocPrint.setFileUrl(Long.parseLong(att_fileUrl));
				proEdocPrint.setFileName(att_fileName);
				proEdocPrint.setFileCreateTime(new Timestamp(sdf.parse(att_createDate).getTime()));
			}
			formPrintBindManager.saveOrUpdatePrintMode(proEdocPrint);
		}
        
	}
	/**
	 * 解除绑定
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	 public ModelAndView deletePrintMode(HttpServletRequest request, HttpServletResponse response)throws BusinessException{
		 String unitId = request.getParameter("unitId");
		 String formId = request.getParameter("formId");
		 formPrintBindManager.deletePrintMode(Long.valueOf(unitId), Long.valueOf(formId));
		 return null;
	 }

	 public String ajaxDownLaodFile(String fileId){
		 User user = CurrentUser.get();
		 long unitId = user.getAccountId();
		 String info = "";
		 if(fileId!=null && !"".equals(fileId)){
			 FromPrintBind proEdocPrint = formPrintBindManager.findPrintMode(unitId, Long.parseLong(fileId));
			 if(proEdocPrint!=null){
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			     String att_createDate = sdf.format(proEdocPrint.getFileCreateTime());
			     info = proEdocPrint.getFileUrl()+"#"+proEdocPrint.getFileName()+"#"+att_createDate;
				 return info;
			 }
		 }
		 return "".equals(info)?"N":info;
	 }

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setFormPrintBindManager(FormPrintBindManager formPrintBindManager) {
		this.formPrintBindManager = formPrintBindManager;
	}


}
