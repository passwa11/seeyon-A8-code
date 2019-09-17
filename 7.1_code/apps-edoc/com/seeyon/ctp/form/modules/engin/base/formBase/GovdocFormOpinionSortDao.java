package com.seeyon.ctp.form.modules.engin.base.formBase;

import java.util.List;

import com.seeyon.ctp.form.po.GovdocFormOpinionSort;

public interface GovdocFormOpinionSortDao{
	public void saveOrUpdateList(List<GovdocFormOpinionSort> list);

	public List<GovdocFormOpinionSort> findByFormIdAndProcessName(
			long formId, String processName);

	public List<GovdocFormOpinionSort> findByFormIdAndProcessNameAndAccount(
			long formId, String processName, long accountId);
	public List<GovdocFormOpinionSort> findByFormIdAndAccount(long formId,long accountId);

	public List<GovdocFormOpinionSort> findByFormId(Long id);

	List<GovdocFormOpinionSort> findByFormIdAndFlowPermName(long formId,
			String flowPermName);
	
	public void deleteByFormId(long formId);
}
