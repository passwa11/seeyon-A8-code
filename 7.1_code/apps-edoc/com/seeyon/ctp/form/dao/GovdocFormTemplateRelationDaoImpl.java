package com.seeyon.ctp.form.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.form.po.GovdocFormTemplateRelation;
import com.seeyon.ctp.util.DBAgent;

public class GovdocFormTemplateRelationDaoImpl extends BaseHibernateDao<GovdocFormTemplateRelation> implements GovdocFormTemplateRelationDao{

	@Override
	public List getTemplateIdByFormId(Long formId) {
		String hql = "select templateId from GovdocFormTemplateRelation where formId ="+formId;
		return DBAgent.find(hql);
	}

	@Override
	public void savaOrUpdate(GovdocFormTemplateRelation govdocFormTemplateRelation) {
		DBAgent.saveOrUpdate(govdocFormTemplateRelation);
		
	}

	@Override
	public void delete(GovdocFormTemplateRelation govdocFormTemplateRelation) {
		DBAgent.delete(govdocFormTemplateRelation);
		
	}

	@Override
	public void updateTemplateId(Long formId, Long templateId) {
		String hql = "update GovdocFormTemplateRelation set templateId = :templateId where formId = :formId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("templateId", templateId);
		param.put("formId", formId);
		DBAgent.bulkUpdate(hql, param);
		
	}

	@Override
	public List getByFormId(Long formId) {
		String hql = "from GovdocFormTemplateRelation where formId = :formId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("formId", formId);
	    return DBAgent.find(hql, param);
	}

	@Override
	public List getAllByTemplateId(Long templateId) {
		String hql = "from GovdocFormTemplateRelation where templateId = :templateId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("templateId", templateId);
	    return DBAgent.find(hql, param);
	}

	@Override
	public void deleteAll(List<GovdocFormTemplateRelation> list) {
		DBAgent.deleteAll(list);
	}

	@Override
	public List<GovdocFormTemplateRelation> findByFormId(Long formId) {
		String hql = "from GovdocFormTemplateRelation where formId = :formId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("formId", formId);
	    return DBAgent.find(hql, param);
	}

	
}
