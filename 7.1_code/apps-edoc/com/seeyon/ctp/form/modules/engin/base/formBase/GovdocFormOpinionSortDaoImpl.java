package com.seeyon.ctp.form.modules.engin.base.formBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.form.po.GovdocFormOpinionSort;
import com.seeyon.ctp.util.DBAgent;

public class GovdocFormOpinionSortDaoImpl extends BaseHibernateDao<GovdocFormOpinionSort> implements GovdocFormOpinionSortDao{


	@Override
	public List<GovdocFormOpinionSort> findByFormIdAndProcessName(
			long formId, String processName) {
		String hql = "from GovdocFormOpinionSort where formId=:formId and processName=:processName";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("processName",processName);
		
		return DBAgent.find(hql,map);
	}
	@Override
	public List<GovdocFormOpinionSort> findByFormIdAndFlowPermName(
			long formId, String flowPermName) {
		String hql = "from GovdocFormOpinionSort where formId=:formId and flowPermName=:flowPermName";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("flowPermName",flowPermName);
		
		return DBAgent.find(hql,map);
	}
	@Override
	public List<GovdocFormOpinionSort> findByFormIdAndProcessNameAndAccount(
			long formId, String processName, long accountId) {
		String hql = "from GovdocFormOpinionSort where formId=:formId and processName=:processName and domainId=:accountId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("processName",processName);
		map.put("accountId", accountId);
		return DBAgent.find(hql,map);
	}

	@Override
	public void saveOrUpdateList(List<GovdocFormOpinionSort> list) {
		if(CollectionUtils.isNotEmpty(list)){
			GovdocFormOpinionSort govdocFormOpinionSort = list.get(0);
			long formId = govdocFormOpinionSort.getFormId();
			deleteByFormId(formId);
		}
		DBAgent.saveAll(list);
	}

	@Override
	public List<GovdocFormOpinionSort> findByFormIdAndAccount(long formId,
			long accountId) {
		String hql = "from GovdocFormOpinionSort where formId=:formId and domainId=:accountId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("accountId", accountId);
		return DBAgent.find(hql,map);
	}

	@Override
	public List<GovdocFormOpinionSort> findByFormId(Long id) {
		String hql = "from GovdocFormOpinionSort where formId= :fId order by processName desc";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fId", id);
		return DBAgent.find(hql,map);
	}
	@Override
	public void deleteByFormId(long formId) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("formId", formId);
		DBAgent.bulkUpdate("delete from GovdocFormOpinionSort where formId=:formId", params);
	}



}
