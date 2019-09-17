package com.seeyon.apps.govdoc.option.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

public interface FormOptionSortManager {
	public List<FormOptionSort> findBoundByFormId(long formId, String processName)throws Exception;

	public List<FormOptionSort> findBoundByFormId(long formId,
			String processName, long accountId);
	
	public void saveOrUpdateList(List<FormOptionSort> list);

	public Map<String, EdocOpinionModel> getGovdocOpinion(long formId,
			EdocSummary colSummary, FormOpinionConfig displayConfig);

	public List<FormOptionSort> findByFormId(Long id);

	public Map<String,String> getOpinionLocation(Long formAppid);

	public List<String> getOpinionElementLocationNames(Long formAppid);

	public String getDisOpsition(Long formAppid,EdocSummary summary,CtpAffair affair) throws BusinessException;
	
	public void deleteByFormId(Long formId);
}
