package com.seeyon.apps.govdoc.option.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.ctp.util.DBAgent;

public class FormOptionSortDaoImpl implements FormOptionSortDao{


	@Override
	public List<FormOptionSort> findByFormIdAndProcessName(
			long formId, String processName) {
		String hql = "from FormOptionSort where formId=:formId and processName=:processName";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("processName",processName);
		
		return DBAgent.find(hql,map);
	}
	@Override
	public List<FormOptionSort> findByFormIdAndFlowPermName(
			long formId, String flowPermName) {
		String hql = "from FormOptionSort where formId=:formId and flowPermName=:flowPermName";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("flowPermName",flowPermName);
		
		return DBAgent.find(hql,map);
	}
	@Override
	public List<FormOptionSort> findByFormIdAndProcessNameAndAccount(
			long formId, String processName, long accountId) {
		String hql = "from FormOptionSort where formId=:formId and processName=:processName and domainId=:accountId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("processName",processName);
		map.put("accountId", accountId);
		return DBAgent.find(hql,map);
	}

	@Override
	public void saveOrUpdateList(List<FormOptionSort> list) {
		if(CollectionUtils.isNotEmpty(list)){
			FormOptionSort govdocFormOpinionSort = list.get(0);
			long formId = govdocFormOpinionSort.getFormId();
			deleteByFormId(formId);
		}
		DBAgent.saveAll(list);
	}

	@Override
	public List<FormOptionSort> findByFormIdAndAccount(long formId,
			long accountId) {
		String hql = "from FormOptionSort where formId=:formId and domainId=:accountId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", formId);
		map.put("accountId", accountId);
		return DBAgent.find(hql,map);
	}

	@Override
	public List<FormOptionSort> findByFormId(Long id) {
		String hql = "from FormOptionSort where formId=:formId order by processName desc";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", id);
		return DBAgent.find(hql,map);
	}
	@Override
	public void deleteByFormId(long formId) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("formId", formId);
		DBAgent.bulkUpdate("delete from FormOptionSort where formId=:formId", params);
	}



}
