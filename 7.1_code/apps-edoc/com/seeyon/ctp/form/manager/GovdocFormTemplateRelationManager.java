package com.seeyon.ctp.form.manager;

import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.po.GovdocFormTemplateRelation;

public interface GovdocFormTemplateRelationManager {

	public Long getTemplateIdByFormId(Long formId) throws BusinessException;
	
	public void saveOrUpdate(GovdocFormTemplateRelation govdocFormTemplateRelation) throws BusinessException;
	
	public void save(Long formId,Long templateId) throws BusinessException;
	
	public List getAllByTemplateId(Long templateId) throws BusinessException;
	
	public void deleteAll(List<GovdocFormTemplateRelation> list) throws BusinessException;
	
	public GovdocFormTemplateRelation findByFormId(Long formId) throws BusinessException;
	
	public String deleteById(Long relationId) throws BusinessException;
}
