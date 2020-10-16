package com.seeyon.apps.govdoc.option.dao;

import java.util.List;

import com.seeyon.apps.govdoc.po.FormOptionSort;

public interface FormOptionSortDao{
	public void saveOrUpdateList(List<FormOptionSort> list);

	public List<FormOptionSort> findByFormIdAndProcessName(
			long formId, String processName);

	public List<FormOptionSort> findByFormIdAndProcessNameAndAccount(
			long formId, String processName, long accountId);
	public List<FormOptionSort> findByFormIdAndAccount(long formId,long accountId);

	public List<FormOptionSort> findByFormId(Long id);

	List<FormOptionSort> findByFormIdAndFlowPermName(long formId,
			String flowPermName);
	
	public void deleteByFormId(long formId);
}
