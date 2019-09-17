package com.seeyon.v3x.edoc.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.v3x.edoc.dao.EdocDocTemplateDao;
import com.seeyon.v3x.edoc.domain.EdocDocTemplate;
import com.seeyon.v3x.edoc.domain.EdocDocTemplateAcl;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.Constants;

public class EdocDocTemplateManagerImpl implements EdocDocTemplateManager{
	private static final Log LOGGER = LogFactory.getLog(EdocDocTemplateManagerImpl.class);
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
		List<EdocDocTemplate> templateList = edocDocTemplateDao.findByDomainId(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
		
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
		
		boolean check = name.equals(edocTemplate.getName());
		
		if(!check && this.checkHasName(edocTemplate.getType(),name)){
            return "<script>alert(parent._('edocLang.templete_alertRepeatCategoryName'));</script>";
        }else{
            edocTemplate.setName(name);
            edocTemplate.setLastUserId(user.getId());
            edocTemplate.setLastUpdate(new java.sql.Timestamp(new Date().getTime()));
            edocDocTemplateDao.update(edocTemplate);    
            return "";
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
		User user = AppContext.getCurrentUser();
		List<EdocDocTemplate> list=edocDocTemplateDao.findByDomainId(user.getLoginAccount(),condition,textfield);
		File file = null;
		for(EdocDocTemplate temp:list){
			List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
			if(null!=attList && attList.size()>0){
				try{
					file = fileManager.getFile(attList.get(0).getFileUrl());
				}catch(Exception e){
					LOGGER.error("查找所有模版：得到模版文件错误，文件URL="+attList.get(0).getFileUrl(),e);
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
		}
		return list;
	}

	public List<EdocDocTemplate> findTemplateByType(int type)  throws EdocException {
		User user = AppContext.getCurrentUser();
		List<EdocDocTemplate> list =edocDocTemplateDao.findByDomainIdAndType(user.getLoginAccount(),type);
		//TODO 有性能问题，好像查出绝对路径也没地方用，先屏蔽，有其他问题再修改
		/*File file = null;
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
		}*/
		return list;
	}

	@AjaxAccess
	public List<EdocDocTemplate> findGrantedListForTaoHong(long userId, int type, String textType)throws Exception{
		return findGrantedListForTaoHong(V3xOrgEntity.VIRTUAL_ACCOUNT_ID,userId,type,textType);
	}
	public List<EdocDocTemplate> findGrantedListForTaoHong(Long accountId,long userId, int type, String textType)throws Exception{
		String theIds = "";
		try{
			theIds =orgManager.getUserIDDomain(userId, accountId,V3xOrgEntity.ORGENT_TYPE_ACCOUNT,V3xOrgEntity.ORGENT_TYPE_DEPARTMENT);
		}catch(Exception e){
			LOGGER.error("根据登陆人查找单位错误",e);
		}

		List<EdocDocTemplate> list = null;
		
		if(!"".equals(theIds)){
			list = edocDocTemplateDao.findGrantedTemplateForTaohong(theIds, type, textType);
		}
		
		File file = null;

		List<EdocDocTemplate> newList = new ArrayList<EdocDocTemplate>();
		
		for(EdocDocTemplate temp:list){
			List<Attachment> attList = attachmentManager.getByReference(temp.getId(), temp.getId());
			if(null!=attList && attList.size()>0){
				try{
					file = fileManager.getFile(attList.get(0).getFileUrl());
				}catch(Throwable e){
					LOGGER.error("", e);
				}
				if(null != file){
						temp.setFileUrl(file.getAbsolutePath());
						newList.add(temp);
					}	
				}
			}
		return newList;
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

		//如果最后一个条件不为空，加上正文类型（officeword or wpsword）
		if(null!=obj && obj.length!=0){
			  criteria.add(Restrictions.eq("textType", String.valueOf(obj[0]).toLowerCase()));
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
			return "<script>alert(parent._('edocLang.templete_alertRepeatName'));</script>";
		}
		else
		{
			edocDocTemplateDao.save(edocTemplate);
		}
		
		return "";
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
			List<EdocDocTemplate> list = getEdocDocTemplate(orgAccountId,user,edocType,bodyType);

			if(null==list || list.size()==0) ret="0";
			else ret="1";
		}catch(Exception e){
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
	/**
	 * 获取能够使用的模板，过滤掉停用的。
	 * @param user     ：用户
	 * @param edocType ：类型（正文/文单）
	 * @param bodyType : Officeword:word正文/Wpsword:wps正文

	 * @return
	 * @throws Exception
	 */
	private List<EdocDocTemplate> getEdocDocTemplate(Long orgAccountId,User user, String edocType, String bodyType)
			throws Exception {

		List<EdocDocTemplate> list = new ArrayList<EdocDocTemplate>();
		orgAccountId = V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
		String bdType = bodyType == null ? null : bodyType.toLowerCase();
		if (null != edocType &&("edoc".equals(edocType) || "govdoc".equals(edocType)) ) {
			list = this.findGrantedListForTaoHong(orgAccountId,user.getId(), Constants.EDOC_DOCTEMPLATE_WORD, bdType);
		} else if (null != edocType && "script".equals(edocType)) {
			list = this.findGrantedListForTaoHong(orgAccountId,user.getId(), Constants.EDOC_DOCTEMPLATE_SCRIPT, bdType);
		} else {
			list = this.findAllTemplate();
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
}
