package com.seeyon.apps.govdoc.manager.impl;

import static java.io.File.separator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.govdoc.constant.GovdocEnum.DocTemplateTypeEnum;
import com.seeyon.apps.govdoc.dao.GovdocDocTemplateDao;
import com.seeyon.apps.govdoc.manager.GovdocDocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.form.po.FromPrintBind;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.manager.EdocDocTemplateAclManager;
import com.seeyon.v3x.edoc.manager.EdocDocTemplateManager;

/**
 * 新公文套红模板管理类
 * @author 唐桂林
 *
 */
public class GovdocDocTemplateManagerImpl implements GovdocDocTemplateManager {
	
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocDocTemplateManagerImpl.class);
	
	private GovdocDocTemplateDao govdocDocTemplateDao;
	private EdocDocTemplateManager edocDocTemplateManager;
	private EdocDocTemplateAclManager edocDocTemplateAclManager;
	private GovdocFormManager govdocFormManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private OrgManager orgManager;
	
	@Override
	public List<EdocDocTemplate> findAllTemplate(String condition, String textfield) throws BusinessException {
		return edocDocTemplateManager.findAllTemplate(condition, textfield);
	}
	
	@Override
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		if(condition == null) {
			condition = new HashMap<String, String>();
		}
		if(!condition.containsKey("domainId")) {
			condition.put("domainId", String.valueOf(AppContext.currentAccountId()));
		}
		List<EdocDocTemplate> result = govdocDocTemplateDao.findList(flipInfo, condition);
		if(Strings.isNotEmpty(result)) {
			File file = null;
			for(EdocDocTemplate bean : result) {
				List<Attachment> attList = attachmentManager.getByReference(bean.getId(), bean.getId());
				if(null!=attList && attList.size()>0){
					try {
						file = fileManager.getFile(attList.get(0).getFileUrl());
					} catch(Exception e) {
						LOGGER.error("查找所有模版：得到模版文件错误，文件URL="+attList.get(0).getFileUrl(),e);
					}
					if(null!=file && null!=file.getAbsolutePath()) {
						bean.setFileUrl(file.getAbsolutePath());
					}
				}
				Set<EdocDocTemplateAcl> templateAcls = bean.getTemplateAcls();
				java.util.Iterator<EdocDocTemplateAcl> iterator = templateAcls.iterator();
				String grantNames = "";
				try {
					while (iterator.hasNext()) {
						EdocDocTemplateAcl templateAcl = iterator.next();
						V3xOrgEntity orgEntity = orgManager.getEntity(templateAcl.getDepType(), templateAcl.getDepId());
						grantNames += orgEntity.getName() + "、";
					}
					if(Strings.isNotBlank(grantNames)) {
						grantNames = grantNames.substring(0, grantNames.length()-1);
					}
				} catch(Exception e) {
					LOGGER.error("查找授权组织机构异常", e);
					throw new EdocException(e);
				}
				bean.setGrantNames(grantNames);
			}
		}
		flipInfo.setData(result);
		return flipInfo;
	}

	@Override
	public EdocDocTemplate getEdocDocTemplateById(long edocTemplateId) throws BusinessException {
		return edocDocTemplateManager.getEdocDocTemplateById(edocTemplateId);
	}

	@Override
	public String addEdocTemplate(EdocDocTemplate po) throws EdocException {
		return edocDocTemplateManager.addEdocTemplate(po);
	}

	@Override
	public String modifyEdocTemplate(EdocDocTemplate po, String name) throws BusinessException {
		return edocDocTemplateManager.modifyEdocTemplate(po, name);
	}

	@Override
	public void deleteEdocTemtlate(List<Long> edocTemplateIds) throws BusinessException {
		edocDocTemplateManager.deleteEdocTemtlate(edocTemplateIds);
	}

	@Override
	public boolean checkHasName(int type, String name, Long templateId, Long accountId) throws BusinessException {
		return edocDocTemplateManager.checkHasName(type, name, templateId, accountId);
	}

	@Override
	public List<EdocDocTemplateAcl> getEdocDocTemplateAcl(String templateId) throws BusinessException {
		return edocDocTemplateAclManager.getEdocDocTemplateAcl(templateId);
	}

	@Override
	public void saveEdocDocTemplateAcl(Long id, Long templateId, String[] departmentIds) throws BusinessException {
		try {
			edocDocTemplateAclManager.saveEdocDocTemplateAcl(id, templateId, departmentIds);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public void updateEdocDocTemplateAcl(Long id, Long templateId, String[] departmentIds) throws BusinessException {
		try {
			edocDocTemplateAclManager.updateEdocDocTemplateAcl(id, templateId, departmentIds);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public void deleteAclByTemplateId(Long templateId) throws BusinessException {
		edocDocTemplateAclManager.deleteAclByTemplateId(templateId);
	}

	@Override
	public List<EdocDocTemplate> findTemplateByType(int type) throws BusinessException {
		try {
			return edocDocTemplateManager.findTemplateByType(type);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public String hasEdocDocTemplate(String isFromAdmin, Long orgAccountId, String edocType,String bodyType) {
		String ret = "false";
		User user = AppContext.getCurrentUser();
		bodyType = bodyType.toLowerCase();
		if("edoc".equals(edocType) && !"wpsword".equals(bodyType)){
			bodyType = "officeword";
		}
		//文单套红不用区分word和wps,只有正文套红才区分
		if("script".equals(edocType) && ("wpsword".equals(bodyType) || "officeword".equals(bodyType))){
			bodyType = null;
		}
		try {
			List<EdocDocTemplate> list = getEdocDocTemplateList(isFromAdmin, orgAccountId, user, edocType, bodyType);
			if(Strings.isNotEmpty(list)) {
				ret = "true";
			}
		} catch(Exception e) {
			StringBuilder parameter=new StringBuilder();
			parameter.append("(");
			parameter.append("edocType=").append(edocType);
			parameter.append("bodyType=").append(bodyType);
			parameter.append("userId=").append(user.getId());
			parameter.append(")");
			LOGGER.error("ajax获取套红模板列表异常：",e);
		}
		return ret;
	}
	
	@Override
	public String hasEdocDocTemplate(Long orgAccountId, String edocType, String bodyType) {
		return hasEdocDocTemplate("true", orgAccountId, edocType, bodyType);
	}
	
	@AjaxAccess
	@Override
	public List<EdocDocTemplate> findGrantedListForTaoHong(Long userId, Integer type, String textType) throws BusinessException {
		try {
			return edocDocTemplateManager.findGrantedListForTaoHong(userId, type, textType);
		} catch(Exception e) {
			LOGGER.error("ajax获取套红模板列表异常：",e);
			throw new BusinessException();
		}
	}
	
	@Override
	public List<EdocDocTemplate> getEdocDocTemplateList(String isFromAdmin, Long orgAccountId, User user, String edocType, String bodyType) throws BusinessException {
		String bdType = bodyType == null ? null : bodyType.toLowerCase();
		
		List<EdocDocTemplate> list = new ArrayList<EdocDocTemplate>();
		if (null != edocType && ("edoc".equals(edocType) || "govdoc".equals(edocType))) {
			list = this.findTaohongList(isFromAdmin, orgAccountId, user, DocTemplateTypeEnum.word.ordinal(), bdType);
		} else if (null != edocType && "script".equals(edocType)) {
			list = this.findTaohongList(isFromAdmin, orgAccountId, user, DocTemplateTypeEnum.script.ordinal(), "");
		} else {
			list = edocDocTemplateManager.findAllTemplate();
		}
		// 过滤掉停用状态的
		Set<Long> ids = new HashSet<Long>();
		List<EdocDocTemplate> list2 = new ArrayList<EdocDocTemplate>();
		for (EdocDocTemplate t : list) {
			if (t.getStatus() == 1 && !ids.contains(t.getId())){
				list2.add(t);
				ids.add(t.getId());
			}
		}

		return list2;
	}
	
	private List<EdocDocTemplate> findTaohongList(String isFromAdmin, Long accountId, User user, int type, String textType) throws BusinessException{
		String theIds = "";
		boolean isAdmin = false;
		if("true".equals(isFromAdmin)) {
			isAdmin = user.isAdministrator() || orgManager.isRole(user.getId(), user.getLoginAccount(), "EdocManagement");
		}
		if(isAdmin) {
			theIds = String.valueOf(accountId);
		} else {
			accountId = V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
			try {
				theIds =orgManager.getUserIDDomain(user.getId(), accountId, V3xOrgEntity.ORGENT_TYPE_ACCOUNT, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT);
			} catch(Exception e) {
				LOGGER.error("根据登陆人查找单位错误",e);
			}
		}
		List<EdocDocTemplate> newList = new ArrayList<EdocDocTemplate>();
		if(!"".equals(theIds)) {
			List<EdocDocTemplate> list = govdocDocTemplateDao.findGrantedTemplateForTaohong(isAdmin, theIds, type, textType.toLowerCase());
			if(Strings.isNotEmpty(list)) {
				File file = null;
				for(EdocDocTemplate temp : list) {
					List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
					if(null!=attList && attList.size()>0){
						try {
							file = fileManager.getFile(attList.get(0).getFileUrl());
						} catch(Throwable e) {
							LOGGER.error("", e);
						}
						if(null != file) {
							temp.setFileUrl(file.getAbsolutePath());
							newList.add(temp);
						}	
					}
				}
			}
		}
		
		return newList;
	}
	
	@Override
	@AjaxAccess
	public String getTemplateIdByFormId(Long formId,long userId) throws BusinessException{
		Long templateId = govdocFormManager.getTemplateIdByFormId(formId);
		if(templateId == null){
			return null;
		}
		
		//判断设置的文单套红模板是否有权限使用---start
		String theIds = "";
		try {
			theIds =orgManager.getUserIDDomain(userId, V3xOrgEntity.VIRTUAL_ACCOUNT_ID,V3xOrgEntity.ORGENT_TYPE_ACCOUNT,V3xOrgEntity.ORGENT_TYPE_DEPARTMENT);
		} catch(Exception e) { 
			LOGGER.error("根据登陆人查找单位错误",e);
		}
		List<EdocDocTemplateAcl> docTemplateAcls = getEdocDocTemplateAcl(String.valueOf(templateId));
		if(docTemplateAcls == null  || docTemplateAcls.isEmpty()) {
			return null;
		}
		if(theIds.indexOf(String.valueOf(docTemplateAcls.get(0).getDepId())) == -1) {
			return null;
		}
		//判断设置的文单套红模板是否有权限使用---end
		EdocDocTemplate edocTemplate = edocDocTemplateManager.getEdocDocTemplateById(templateId);
		if (edocTemplate != null) {
			File file = null;
			List<Attachment> attList = attachmentManager.getByReference(edocTemplate.getId(), edocTemplate.getId());
			if (null != attList && attList.size() > 0) {
				try {
					file = fileManager.getFile(attList.get(0).getFileUrl());
				} catch (Throwable e) {
					LOGGER.error("", e);
				}
				if (null != file) {
					edocTemplate.setFileUrl(file.getAbsolutePath());
				}
			}
			return edocTemplate.getFileUrl()+"&"+edocTemplate.getTextType();
		}
		return null;
	}
	
	/**
	 * 根据表单ID获取下载到本地的打印模板
	 * @param summary
	 * @returnsetLianheTemplateAttr
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Override
	@AjaxAccess
	public String getLocalPrintTemplate(Long formId) throws IOException, FileNotFoundException {
		String root = "";
		if(formId == null) {
			LOGGER.error("getLocalPrintTemplate中获取formId为空！");
			return root;
		}
		try {
			FromPrintBind proEdocPrint = govdocFormManager.findPrintMode(AppContext.currentAccountId(), formId);
			if (proEdocPrint != null && Strings.isNotBlank(proEdocPrint.getFileUrl().toString())) {
				String tempContentType = MainbodyType.WpsWord.name();
				try {
					V3XFile file = fileManager.getV3XFile(proEdocPrint.getFileUrl());
				    root = fileManager.getFolder(file.getCreateDate(), true) + separator + file.getId();
				    if(proEdocPrint.getFileName().indexOf(".doc") > -1){
				    	tempContentType = MainbodyType.OfficeWord.name();
				    }
				    root = root +"&"+tempContentType;
				} catch (Exception e) {
					LOGGER.error("复制打印文件出错", e);
				}
			}
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
		return root;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocDocTemplateDao(GovdocDocTemplateDao govdocDocTemplateDao) {
		this.govdocDocTemplateDao = govdocDocTemplateDao;
	}
	public void setEdocDocTemplateManager(EdocDocTemplateManager edocDocTemplateManager) {
		this.edocDocTemplateManager = edocDocTemplateManager;
	}
	public void setEdocDocTemplateAclManager(EdocDocTemplateAclManager edocDocTemplateAclManager) {
		this.edocDocTemplateAclManager = edocDocTemplateAclManager;
	}
}
