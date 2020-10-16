package com.seeyon.apps.govdoc.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.po.QwqpDefaultTemplate;
import com.seeyon.apps.govdoc.po.QwqpEdocFormFileRelation;
import com.seeyon.apps.govdoc.util.QwqpUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;

public class QwqpController extends BaseController {

	private QwqpManager qwqpManager;
	private FormApi4Cap3 formApi4Cap3;
	private FileManager fileManager;

	// 操作反馈都是模板id和模板type
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		User user = AppContext.getCurrentUser();
		Long department_id = user.getLoginAccount();
		Long yoyo = null;
		ModelAndView mav = new ModelAndView("edoc/formManage/defaultTemplate");
		// 获取各类型非删除list
		// 发文
		List<CtpTemplate> list1 = qwqpManager.getTemplateByType(19l);
		List<CtpTemplate> fawenlist = new ArrayList<CtpTemplate>();
		for (int i = 0; i < list1.size(); i++) {
			yoyo = list1.get(i).getOrgAccountId();
			if (department_id.equals(yoyo) && list1.get(i).isSystem() == true) {
				fawenlist.add(list1.get(i));
				mav.addObject("fawenlist", fawenlist);
			}
		}
		// 收文
		List<CtpTemplate> list2 = qwqpManager.getTemplateByType(20l);
		List<CtpTemplate> shouwenlist = new ArrayList<CtpTemplate>();
		for (int j = 0; j < list2.size(); j++) {
			if (department_id.equals(list2.get(j).getOrgAccountId()) && list2.get(j).isSystem() == true) {
				shouwenlist.add(list2.get(j));
				mav.addObject("shouwenlist", shouwenlist);
			}
		}
		// 签报
		List<CtpTemplate> list3 = qwqpManager.getTemplateByType(21l);
		List<CtpTemplate> qianbaolist = new ArrayList<CtpTemplate>();
		for (int k = 0; k < list3.size(); k++) {
			if (department_id.equals(list3.get(k).getOrgAccountId()) && list3.get(k).isSystem() == true) {
				qianbaolist.add(list3.get(k));
				mav.addObject("qianbaolist", qianbaolist);
			}
		}

		// 获取现在选中各类型的list
		Long fwxz = qwqpManager.getExistDefaultTemplate(department_id, 19l);
		Long swxz = qwqpManager.getExistDefaultTemplate(department_id, 20l);
		Long qbxz = qwqpManager.getExistDefaultTemplate(department_id, 21l);
		// 发文
		QwqpDefaultTemplate d1 = new QwqpDefaultTemplate();
		if (qwqpManager.getDefaultTemplateById(fwxz).size() > 0) {
			d1 = qwqpManager.getDefaultTemplateById(fwxz).get(0);
			Long fwxzId = d1.getTemplate();
			List<CtpTemplate> listxz1 = qwqpManager.getTemplateById(fwxzId);
			if (listxz1.size() > 0) {
				if (department_id.equals(listxz1.get(0).getOrgAccountId())) {
					mav.addObject("fwxzlist", listxz1);
				}
			}
		}

		// 收文
		QwqpDefaultTemplate d2 = new QwqpDefaultTemplate();
		if (qwqpManager.getDefaultTemplateById(swxz).size() > 0) {
			d2 = qwqpManager.getDefaultTemplateById(swxz).get(0);
			Long swxzId = d2.getTemplate();
			List<CtpTemplate> listxz2 = qwqpManager.getTemplateById(swxzId);
			if (listxz2.size() > 0) {
				if (department_id.equals(listxz2.get(0).getOrgAccountId())) {
					mav.addObject("swxzlist", listxz2);
				}
			}
		}

		// 签报
		QwqpDefaultTemplate d3 = new QwqpDefaultTemplate();
		if (qwqpManager.getDefaultTemplateById(qbxz).size() > 0) {
			d3 = qwqpManager.getDefaultTemplateById(qbxz).get(0);
			Long qbxzId = d3.getTemplate();
			List<CtpTemplate> listxz3 = qwqpManager.getTemplateById(qbxzId);
			if (listxz3.size() > 0) {
				if (department_id.equals(listxz3.get(0).getOrgAccountId())) {
					mav.addObject("qbxzlist", listxz3);
				}
			}
		}

		return mav;
	}

	// 设置与取消默认模板
	public ModelAndView depDefaultTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		// System.out.println("11111");
		User user = AppContext.getCurrentUser();
		Long defaultTemplate_id = UUIDLong.longUUID();
		Long department_id = user.getLoginAccount();
		Long template_id = null;
		Long templateType = null;
		Long template_id2 = null;
		Long templateType2 = null;
		Long template_id3 = null;
		Long templateType3 = null;

		// 获取发文默认模板
		Long id = Long.parseLong(request.getParameter("fawen"));
		if (id != 0l) {
			template_id = Long.parseLong(request.getParameter("fawen"));
			templateType = 19l;
			qwqpManager.setDepDefaultTemplate(defaultTemplate_id, department_id, template_id, templateType);
		} else {
			templateType = 19l;
			defaultTemplate_id = qwqpManager.getExistDefaultTemplate(department_id, templateType);
			if (defaultTemplate_id != null)
				qwqpManager.deleteDepDefaultTemplate(defaultTemplate_id);
		}

		// 获取收文默认模板
		Long defaultTemplate_id2 = UUIDLong.longUUID();
		Long id2 = Long.parseLong(request.getParameter("shouwen"));
		if (id2 != 0l) {

			template_id2 = Long.parseLong(request.getParameter("shouwen"));
			templateType2 = 20l;
			qwqpManager.setDepDefaultTemplate(defaultTemplate_id2, department_id, template_id2, templateType2);
		} else {
			templateType2 = 20l;
			defaultTemplate_id2 = qwqpManager.getExistDefaultTemplate(department_id, templateType2);
			if (defaultTemplate_id2 != null)
				qwqpManager.deleteDepDefaultTemplate(defaultTemplate_id2);
		}

		// 获取签报默认模板
		Long defaultTemplate_id3 = UUIDLong.longUUID();
		Long id3 = Long.parseLong(request.getParameter("qianbao"));
		if (id3 != 0l) {
			template_id3 = Long.parseLong(request.getParameter("qianbao"));
			templateType3 = 21l;
			qwqpManager.setDepDefaultTemplate(defaultTemplate_id3, department_id, template_id3, templateType3);
		} else {
			templateType3 = 21l;
			defaultTemplate_id3 = qwqpManager.getExistDefaultTemplate(department_id, templateType3);
			if (defaultTemplate_id3 != null)
				qwqpManager.deleteDepDefaultTemplate(defaultTemplate_id3);
		}
		return null;
	}
	
	//cx 添加全文签批上传界面
	public ModelAndView qwqpSet(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	ModelAndView mav = new ModelAndView("plugin/qwqp/qwqp_set");
    	boolean isDoubleForm = true; 
    	FormBean fb = formApi4Cap3.getEditingForm();
    	List<QwqpEdocFormFileRelation> edocFormFileRelations = qwqpManager.findByEdocFormId(fb.getId());
    	mav.addObject("AIP",false);
    	mav.addObject("v","");
    	mav.addObject("fileId","");
    	mav.addObject("createDate","");
    	mav.addObject("filename","");
    	mav.addObject("formType",fb.getGovDocFormType());
    	if(null!=edocFormFileRelations&&edocFormFileRelations.size()>0){
    		mav.addObject("AIP",true);
    		mav.addObject("formType",fb.getGovDocFormType());
    		isDoubleForm = edocFormFileRelations.get(0).getDoubleForm();
    		V3XFile v3xFile = fileManager.getV3XFile(edocFormFileRelations.get(0).getFileId());
    		if (v3xFile != null) {
    			String v = SecurityHelper.digest(v3xFile.getId());
    			mav.addObject("v",v);
    			mav.addObject("fileId",v3xFile.getId());
    			mav.addObject("createDate",new SimpleDateFormat("yyyy-MM-dd").format(v3xFile.getCreateDate()));
    			mav.addObject("filename",v3xFile.getFilename());
			}
    	}
    	mav.addObject("isDoubleForm",isDoubleForm);
    	mav.addObject("fileType",QwqpUtil.getNowFileType());
    	return mav;
    }
	public void saveQwqpSet(HttpServletRequest request,HttpServletResponse response) throws Exception{
	    	String isDoubleFormStr = request.getParameter("isDoubleForm");
		boolean isDoubleForm = false;
		if(Strings.isNotBlank(isDoubleFormStr)){
			if("1".equals(isDoubleFormStr)){
				isDoubleForm = true;
			}
		}
		String att_fileUrl = request.getParameter("att_fileUrl");
		FormBean fb = formApi4Cap3.getEditingForm();
		List<QwqpEdocFormFileRelation> lists = qwqpManager.findByEdocFormId(fb.getId());
		
		if(null != att_fileUrl && !"".equals(att_fileUrl)){
			String att_filename = request.getParameter("att_filename");
			String att_size = request.getParameter("att_size");
			V3XFile file = new V3XFile(Long.parseLong(att_fileUrl));
	        file.setCategory(4);
	        file.setType(0);
	        file.setFilename(att_filename);
	        file.setMimeType("application/"+QwqpUtil.getNowFileType());
	        file.setCreateDate(new java.util.Date());
	        file.setSize(Long.parseLong(att_size));
	        file.setDescription("");
	        file.setCreateMember(AppContext.currentUserId());
	        file.setAccountId(AppContext.currentAccountId());
	        this.fileManager.save(file);
		}
		if(null!=lists&&lists.size()>0){
			for (QwqpEdocFormFileRelation edocFormFileRelation : lists) {
				edocFormFileRelation.setId(UUIDLong.longUUID());
				edocFormFileRelation.setDoubleForm(isDoubleForm);
				edocFormFileRelation.setFileType(QwqpUtil.getNowFileType());
				if(null != att_fileUrl && !"".equals(att_fileUrl)){
					edocFormFileRelation.setFileId(Long.parseLong(att_fileUrl));
				}
				qwqpManager.addEdocFormFileRelation(edocFormFileRelation);
			}
		}else{
			if (null != att_fileUrl && !"".equals(att_fileUrl)) {
				QwqpEdocFormFileRelation edocFormFileRelation = new QwqpEdocFormFileRelation();
		        edocFormFileRelation.setId(UUIDLong.longUUID());
		        edocFormFileRelation.setFormId(fb.getId());
		        edocFormFileRelation.setFileType(QwqpUtil.getNowFileType());//设置文件类型
		        edocFormFileRelation.setFileId(Long.parseLong(att_fileUrl));
		        edocFormFileRelation.setDoubleForm(isDoubleForm);
		        qwqpManager.addEdocFormFileRelation(edocFormFileRelation);
			}
		}
	}
	public ModelAndView qwqpView(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ModelAndView mav = new ModelAndView("plugin/qwqp/qwqp_view");
		String fileType = request.getParameter("fileType");
		String fromButton = request.getParameter("fromButton");
		String isHandWrite = request.getParameter("isHandWrite");
		String aipFileId = request.getParameter("aipFileId");
		if(Strings.isBlank(fileType)){
			fileType = QwqpUtil.getNowFileType();
		}
		
		if(Strings.isNotBlank(fromButton)){
			mav.addObject("fromButton",fromButton);
		}
		if(Strings.isNotBlank(isHandWrite)){
			mav.addObject("isHandWrite",isHandWrite);
		}
		mav.addObject("currentUserId",AppContext.currentUserId());
		mav.addObject("currentLoginAccout",AppContext.currentAccountId());
		mav.addObject("aipFileId",aipFileId);
		mav.addObject("fileType",fileType);
		mav.addObject("customName",QwqpUtil.getAipCustomName());
		return mav;
	}
	public void setQwqpManager(QwqpManager qwqpManager) {
		this.qwqpManager = qwqpManager;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	
}