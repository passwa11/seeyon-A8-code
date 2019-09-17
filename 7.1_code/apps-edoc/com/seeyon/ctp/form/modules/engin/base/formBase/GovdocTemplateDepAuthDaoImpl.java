package com.seeyon.ctp.form.modules.engin.base.formBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.ctp.form.po.GovdocTemplateDepAuth;
import com.seeyon.ctp.util.DBAgent;

public class GovdocTemplateDepAuthDaoImpl implements GovdocTemplateDepAuthDao{

	@Override
	public List<GovdocTemplateDepAuth> findByTemplateId(long templateId) {
		String hql = "from GovdocTemplateDepAuth c where c.templateId=:templateId";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("templateId", templateId);
		return DBAgent.find(hql,map);
	}

	@Override
	public void saveAll(List<GovdocTemplateDepAuth> list) {
		DBAgent.saveAll(list);
	}

	@Override
	public List<GovdocTemplateDepAuth> findByTemplateIdAndOrgId(
			Long templateId, Long orgId) {
		String hql = "from GovdocTemplateDepAuth c where c.templateId=:templateId and c.orgId=:orgId";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("templateId", templateId);
		map.put("orgId", orgId);
		return DBAgent.find(hql,map);
	}

	@Override
	public void deleteByTemplateIdAndOrgId(Long templateId, Long orgId) {
		String hql = "delete from GovdocTemplateDepAuth as c where c.templateId=:templateId and c.orgId=:orgId";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("templateId", templateId);
		map.put("orgId", orgId);
		DBAgent.bulkUpdate(hql, map);
	}
	
	@Override
	public List<GovdocTemplateDepAuth> findByAuthType(int authType) {
		String hql = "from GovdocTemplateDepAuth c where c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("authType", authType);
		return DBAgent.find(hql,map);
	}

	@Override
	public void deleteByTemplateAndAuthType(Long id, int authTypeExchange) {
		String hql = "delete from GovdocTemplateDepAuth as c where c.templateId=:templateId and c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("templateId", id);
		map.put("authType", authTypeExchange);
		DBAgent.bulkUpdate(hql, map);
	}

	@Override
	public GovdocTemplateDepAuth findExchangeByOrgId(long orgId) {
		String hql = "from GovdocTemplateDepAuth c where  c.orgId=:orgId and c.authType=0";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orgId", orgId);
		List<GovdocTemplateDepAuth> list =  DBAgent.find(hql,map);
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public List<GovdocTemplateDepAuth> findByOrgId(List<Long> orgIds) {
		String hql = "from GovdocTemplateDepAuth c where  c.orgId in (:orgIds) and c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orgIds", orgIds);
		map.put("authType", GovdocTemplateDepAuth.AUTH_TYPE_EXCHANGE);
		return DBAgent.find(hql,map);
	}
	

	@Override
	public void deleteByOrgIdAndTypeId(Long orgId,int authType) {
		String hql = "delete from GovdocTemplateDepAuth as c where c.orgId=:orgId and c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orgId", orgId);
		map.put("authType", authType);
		DBAgent.bulkUpdate(hql, map);
	}

	@Override
	public List<GovdocTemplateDepAuth> findByOrgIdAndAccountId(long orgId,
			long accountId,int authType) {
		String hql = "from GovdocTemplateDepAuth c where c.orgId=:orgId and c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orgId", orgId);
		map.put("authType", authType);
		return DBAgent.find(hql,map);
	}
	
	@Override
	public void deleteByAccountIdAndTypeId(Long orgId,int authType) {
		String hql = "delete from GovdocTemplateDepAuth as c where c.orgId=:orgId and c.authType=:authType";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("orgId", orgId);
		map.put("authType", authType);
		DBAgent.bulkUpdate(hql, map);
	}
}
