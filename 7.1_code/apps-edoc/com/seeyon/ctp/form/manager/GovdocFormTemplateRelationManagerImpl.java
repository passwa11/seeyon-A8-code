package com.seeyon.ctp.form.manager;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.dao.GovdocFormTemplateRelationDao;
import com.seeyon.ctp.form.po.GovdocFormTemplateRelation;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;

public class GovdocFormTemplateRelationManagerImpl implements GovdocFormTemplateRelationManager {

	private static final Log log = LogFactory.getLog(GovdocFormTemplateRelationManagerImpl.class);
	private GovdocFormTemplateRelationDao govdocFormTemplateRelationDao;

	public GovdocFormTemplateRelationDao getGovdocFormTemplateRelationDao() {
		return govdocFormTemplateRelationDao;
	}

	public void setGovdocFormTemplateRelationDao(GovdocFormTemplateRelationDao govdocFormTemplateRelationDao) {
		this.govdocFormTemplateRelationDao = govdocFormTemplateRelationDao;
	}
	@Override
	public Long getTemplateIdByFormId(Long formId) throws BusinessException {
		List list = govdocFormTemplateRelationDao.getTemplateIdByFormId(formId);
		if (list.size() > 0) {
			return (Long) list.get(0);
			/*EdocDocTemplate edocTemplate = edocDocTemplateDao.get(templateId);
			if (edocTemplate != null) {
				File file = null;
				List<Attachment> attList = attachmentManager.getByReference(edocTemplate.getId(), edocTemplate.getId());
				if (null != attList && attList.size() > 0) {
					try {
						file = fileManager.getFile(attList.get(0).getFileUrl());
					} catch (Throwable e) {
						log.error("", e);
					}
					if (null != file) {
						edocTemplate.setFileUrl(file.getAbsolutePath());
					}
				}
				return edocTemplate.getFileUrl()+"&"+edocTemplate.getTextType();
			}*/
		}
		return null;
	}

	@Override
	public void saveOrUpdate(GovdocFormTemplateRelation govdocFormTemplateRelation) throws BusinessException {
		govdocFormTemplateRelationDao.savaOrUpdate(govdocFormTemplateRelation);
	}

	@Override
	@AjaxAccess
	public void save(Long formId, Long templateId) throws BusinessException {
		List list = govdocFormTemplateRelationDao.getByFormId(formId);
		GovdocFormTemplateRelation govdocFormTemplateRelation = new GovdocFormTemplateRelation();
		if (list.isEmpty()) {
			govdocFormTemplateRelation = new GovdocFormTemplateRelation();
			govdocFormTemplateRelation.setId(UUIDLong.longUUID());
			govdocFormTemplateRelation.setFormId(formId);
			govdocFormTemplateRelation.setTemplateId(templateId);
		} else {
			govdocFormTemplateRelation = (GovdocFormTemplateRelation) list.get(0);
			govdocFormTemplateRelation.setTemplateId(templateId);
		}
		govdocFormTemplateRelationDao.savaOrUpdate(govdocFormTemplateRelation);
	}

	@Override
	public List getAllByTemplateId(Long templateId) throws BusinessException {
		
		return govdocFormTemplateRelationDao.getAllByTemplateId(templateId);
	}

	@Override
	public void deleteAll(List<GovdocFormTemplateRelation> list) throws BusinessException {
		govdocFormTemplateRelationDao.deleteAll(list);
	}

	@Override
	public GovdocFormTemplateRelation findByFormId(Long formId) throws BusinessException {
		List list = govdocFormTemplateRelationDao.findByFormId(formId);
		if(list.size()>0){
			return (GovdocFormTemplateRelation)list.get(0);
		}
		return null;
	}

	@Override
	@AjaxAccess
	public String deleteById(Long relationId) throws BusinessException {
		String result = "success";
		try{
			if(relationId != null){
				List<GovdocFormTemplateRelation> list = govdocFormTemplateRelationDao.findByFormId(relationId);
				govdocFormTemplateRelationDao.deleteAll(list);		
			}else{
				result = "传入的绑定id为空!";
			}
		}catch(Exception e){
			result = "解除模板绑定时出现异常!";
			throw new BusinessException(e);
		}
		return result;
	}

	

}
