package com.seeyon.v3x.edoc.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.edoc.dao.EdocDocTemplateDao;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.Constants;

public class EdocTemplateManagerImpl implements EdocDocTemplateManager{
	private static final Log LOGGER = LogFactory.getLog(EdocTemplateManagerImpl.class);
	private EdocDocTemplateDao edocDocTemplateDao;
	private OrgManager orgManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private EdocDocTemplateAclManager edocDocTemplateAclManager;

	public EdocDocTemplateDao getEdocDocTemplateDao() {
		return edocDocTemplateDao;
	}

	public void setEdocDocTemplateDao(EdocDocTemplateDao edocDocTemplateDao) {
		this.edocDocTemplateDao = edocDocTemplateDao;
	}

	public String addEdocTemplate(String name, String desc,int type, long templateId, long domainId, int status)throws EdocException {
		boolean bool=this.checkHasName(type,name);
		if(bool){
			return "<script>alert(parent._('edocLang.templete_alertRepeatCategoryName'));</script>";
		}
		User user =AppContext.getCurrentUser();
		EdocDocTemplate template=new EdocDocTemplate();
		template.setIdIfNew();
		template.setName(name);
		template.setType(type);
		template.setDomainId(domainId);
		template.setTemplateFileId(templateId);
		template.setStatus(status);
		template.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
		template.setCreateUserId(user.getId());
		if(desc == null || "".equals(desc)){
			template.setDescription(" ");
		}
		template.setLastUserId(user.getId());
		template.setLastUpdate(new java.sql.Timestamp(new Date().getTime()));
		edocDocTemplateDao.save(template);
		return "";
	}
	
	/**
	 * 用于在集团管理中添加单位的时候,同时向新建的单位插入新的套红模板
	 * @param accountId 添加单位的id
	 * @return
	 * @throws EdocException
	 */
	public void addEdocTemplate(long accountId) throws Exception{
		
		User user = AppContext.getCurrentUser();
		//查找出系统预置的套红模板,默认是单位Id为0L;
		List<EdocDocTemplate> templateList = edocDocTemplateDao.findByDomainId(0L);
		
		for(EdocDocTemplate template:templateList){
			
			long ini_id = template.getId();                //取得模板原始的id,用于查找附件
			
			template = (EdocDocTemplate)template.clone();  //给模板重新赋id
			template.setNewId();
			template.setDomainId(accountId);
			template.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
			template.setLastUpdate(new java.sql.Timestamp(new Date().getTime()));
			template.setLastUserId(user.getLoginAccount());
			template.setCreateUserId(user.getId());        
			
			//根据系统预置模板的Id查找出与之对应的附件,用于克隆
			List<Attachment> attachmentList = attachmentManager.getByReference(ini_id, ini_id);
			List<V3XFile> fileList = new ArrayList<V3XFile>();
			for(Attachment attachment:attachmentList){
				//根据系统预置模板克隆出新的file对象,false代表不同时在数据库中保存
				V3XFile file = fileManager.clone(attachment.getFileUrl(), false);
				fileList.add(file);
			}
			//为新生成的模板保存附件,referenceId和subReferenceId为新生成的templateId
			if(null!=fileList && fileList.size()>0){
				attachmentManager.create(ApplicationCategoryEnum.edoc, template.getId(), template.getId());
			}
			edocDocTemplateDao.save(template);   //向数据库中插入一条新的套红模板
		}	
	}

	public void deleteEdocTemtlate(List<Long> edocTemplateIds) {
		if(edocTemplateIds.isEmpty())return ;
		for(int i=0;i<edocTemplateIds.size();i++){
			long theId=edocTemplateIds.get(i);
			edocDocTemplateAclManager.deleteAclByTemplateId(theId);
			edocDocTemplateDao.delete(theId);
		}
	}

	public EdocDocTemplate getEdocDocTemplateById(long edocTemplateId) {
		EdocDocTemplate docTemplate=edocDocTemplateDao.get(edocTemplateId);
		return docTemplate;
	}

	/**
	 * 
	 * @param edocTemplate
	 * @param name 判断修改时是否修改了名字,如果没有修改那么就不用checkHasName来检查是否重名
	 * @param  
	 * @return
	 * @throws EdocException
	 */
	public String modifyEdocTemplate(EdocDocTemplate edocTemplate,String name) throws EdocException{
		User user=AppContext.getCurrentUser();
		
		boolean flag = name.equals(edocTemplate.getName());
		if(!flag){
		    flag = !this.checkHasName(edocTemplate.getType(),name);
		}
		
		if(flag){
            edocTemplate.setName(name);
            edocTemplate.setLastUserId(user.getId());
            edocTemplate.setLastUpdate(new java.sql.Timestamp(new Date().getTime()));
            edocDocTemplateDao.update(edocTemplate);
            return "";
        } else {
			return "<script>alert(parent._('edocLang.templete_alertRepeatCategoryName'));</script>";
		}
	}
	
	public boolean checkHasName(int type,String name){
		
		User user = AppContext.getCurrentUser();
		
		DetachedCriteria criteria = DetachedCriteria.forClass(EdocDocTemplate.class);
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("status", Constants.EDOC_DOCTEMPLATE_ENABLED));
		criteria.add(Restrictions.eq("domainId", user.getLoginAccount()));
		
		List list = edocDocTemplateDao.searchByCriteria(criteria);
		
		if(list.isEmpty()==false){
			return true;
		}else{
			return false;
		}
		
	}
	
	public boolean checkHasName(int type,String name,Long templateId,Long accountId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(EdocDocTemplate.class);
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("status", Constants.EDOC_DOCTEMPLATE_ENABLED));
		criteria.add(Restrictions.eq("domainId", accountId));
		criteria.add(Restrictions.ne("id", templateId));
		return edocDocTemplateDao.getCountByCriteria(criteria)>0;
	}

	public List<EdocDocTemplate> findAllTemplate() throws EdocException {
		User user = AppContext.getCurrentUser();
		List<EdocDocTemplate> list=edocDocTemplateDao.findByDomainId(user.getLoginAccount());
		File file = null;
		for(EdocDocTemplate temp:list){
			List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
			if(null!=attList && attList.size()>0){
				try{
				file = fileManager.getFile(attList.get(0).getFileUrl());
				}catch(Exception e){
					LOGGER.error("查找所有模版：得到模版文件错误，文件URL="+attList.get(0).getFileUrl(),e);
					throw new EdocException(e);
				}
				if(null!=file && null!=file.getAbsolutePath()){
					temp.setFileUrl(file.getAbsolutePath());
				}
			}
			
			
			Set<EdocDocTemplateAcl> templateAcls = temp.getTemplateAcls();
			java.util.Iterator<EdocDocTemplateAcl> iterator = templateAcls.iterator();
			List<V3xOrgEntity> aclEntity = new ArrayList<V3xOrgEntity>();
			
			try{
			while (iterator.hasNext()) {

				EdocDocTemplateAcl templateAcl = iterator.next();
				V3xOrgEntity orgEntity = orgManager.getEntity(templateAcl.getDepType(), templateAcl.getDepId());
				aclEntity.add(orgEntity);
			}
			}catch(Exception e){
				LOGGER.error("查找授权组织机构异常", e);
				throw new EdocException(e);
			}
			temp.setAclEntity(aclEntity);
			
			/*
			List<EdocDocTemplateAcl> acls = edocDocTemplateAclManager.getEdocDocTemplateAcl(temp.getId().toString());
			
			V3xOrgEntity orgEntity = orgManager.getEntity(markAcl.getAclType(), markAcl.getDeptId());
			String names = "";
			if(acls != null)
				for(EdocDocTemplateAcl acl : acls){
					names += "," + acl.getDepType();
				}
			if(!"".equals(names))
				names = names.substring(1, names.length());
			temp.setGrantNames(names);
			*/
		}
		return list;
	}
	public List<EdocDocTemplate> findAllTemplate(String condition,String textfield) throws EdocException {
		List<EdocDocTemplate> list = null;
		return list;
	}

	public List<EdocDocTemplate> findTemplateByType(int type)  throws EdocException {
		User user = AppContext.getCurrentUser();
		List<EdocDocTemplate> list =edocDocTemplateDao.findByDomainIdAndType(user.getLoginAccount(),type);
		File file = null;
		for(EdocDocTemplate temp:list){
			List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
			if(null!=attList && attList.size()>0){
				try{
				file = fileManager.getFile(attList.get(0).getFileUrl());
				}catch(Exception e){
					LOGGER.error("根据类型查找模版：得到模版文件错误，文件URL="+attList.get(0).getFileUrl(),e);
					throw new EdocException(e);
				}
				temp.setFileUrl(null!=file ? file.getAbsolutePath() : null);
			}
		}
		return list;
	}

	public List<EdocDocTemplate> findTemplateByDomainId(long userId,int type, Object... obj)throws EdocException{
		
		
/*		String theIds=orgManager.getUserIDDomain(userId, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT,
								V3xOrgEntity.ORGENT_TYPE_MEMBER,V3xOrgEntity.ORGENT_TYPE_TEAM,V3xOrgEntity.ORGENT_TYPE_POST);
		
		List<Long> listId=new ArrayList<Long>();
		StringTokenizer token=new StringTokenizer(theIds,",");
		while(token.hasMoreTokens()){
			long id=Long.valueOf(token.nextToken());
			listId.add(id);
		}*/		
		List<EdocDocTemplate> list = null;
		
	    DetachedCriteria criteria = DetachedCriteria.forClass(EdocDocTemplate.class);
		criteria.add(Restrictions.eq("domainId", userId));
		criteria.add(Restrictions.eq("type", type));

		if(null!=obj && obj.length!=0){
			  criteria.add(Restrictions.eq("textType", obj[0]));
		}
		
		list = edocDocTemplateDao.executeCriteria(criteria);
		
		File file = null;
		for(EdocDocTemplate temp:list){
			List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
			if(null!=attList && attList.size()>0){
				try{
				file = fileManager.getFile(attList.get(0).getFileUrl());
				temp.setFileUrl(file.getAbsolutePath());
				}catch(Exception e){
					LOGGER.error("根据DomainId,类型查找模版：得到模版文件错误，文件URL="+attList.get(0).getFileUrl(),e);
					throw new EdocException(e);
				}				
			}
		}
		return list;
	}

	/**
	 * 添加公文套红模版
	 */
	public String addEdocTemplate(EdocDocTemplate edocTemplate) throws EdocException{
		if(this.checkHasName(edocTemplate.getType(),edocTemplate.getName())){
			return "<script>alert(parent._('edocLang.templete_alertRepeatCategoryName'));</script>";
		}
		edocDocTemplateDao.save(edocTemplate);
		return "";
	}
	
	public List<EdocDocTemplate> findGrantedListForTaoHong(long userId, int type, String textType)throws Exception{
		String theIds = "";
		try{
		theIds =orgManager.getUserIDDomain(userId, V3xOrgEntity.ORGENT_TYPE_DEPARTMENT,V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
		}catch(Exception e){
			LOGGER.error("根据登陆人查找单位错误",e);
		}

		List<EdocDocTemplate> list = null;
		
		if(!"".equals(theIds)){
			list = edocDocTemplateDao.findGrantedTemplateForTaohong(theIds, type, textType);
		}
		
		return list;
		/*
		List<Long> listId=new ArrayList<Long>();
		StringTokenizer token=new StringTokenizer(theIds,",");
		while(token.hasMoreTokens()){
			long id=Long.valueOf(token.nextToken());
			listId.add(id);
		}*/
		
	}
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public EdocDocTemplateAclManager getEdocDocTemplateAclManager() {
		return edocDocTemplateAclManager;
	}

	public void setEdocDocTemplateAclManager(
			EdocDocTemplateAclManager edocDocTemplateAclManager) {
		this.edocDocTemplateAclManager = edocDocTemplateAclManager;
	}
	
	public List<EdocDocTemplate> getAllTemplateByAccountId(long accountId){
		return edocDocTemplateDao.findByDomainId(accountId);
	}

	public List<EdocDocTemplate> findGrantedListForTaoHong(Long orgAccountId, long userId, int type, String textType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String hasEdocDocTemplate(Long orgAccountId, String edocType,
			String bodyType) {
		// TODO Auto-generated method stub
		return null;
	}

}
