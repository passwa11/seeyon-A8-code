package com.seeyon.ctp.form.dao;

import java.util.List;

import com.seeyon.ctp.form.po.GovdocFormTemplateRelation;
/**
 * 省级专版   文单模板关联表dao
 * author chenyq
 * 
 */
public interface GovdocFormTemplateRelationDao {
	
	public List getTemplateIdByFormId(Long formId);
	
	public void savaOrUpdate(GovdocFormTemplateRelation govdocFormTemplateRelation);
	
	public void delete(GovdocFormTemplateRelation govdocFormTemplateRelation);
	
	public void updateTemplateId(Long formId ,Long templateId);
	
	public List getByFormId(Long formId);
	
	public List getAllByTemplateId(Long templateId);
	
	public void deleteAll(List<GovdocFormTemplateRelation> list);
	
	public List<GovdocFormTemplateRelation> findByFormId(Long formId);
	
}
