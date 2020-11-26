package com.seeyon.apps.collaboration.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.po.HisColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public class HisColDaoImpl implements HisColDao {

	@Override
	public void save(HisColSummary hisColSummary) throws BusinessException {
		DBAgent.save(hisColSummary);
	}

	@Override
	public ColSummary getColAllById(long summaryId) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultModel> iSearch(ConditionModel cModel)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ColSummary> getSummaryIdByFormIdAndRecordId(Long formAppId,
			Long formId, Long formRecordId) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HisColSummary getColSummaryById(long summaryId) {
		StringBuilder hql=new StringBuilder("from HisColSummary where id=:id");
		Map<String,Object> params=new HashMap<String, Object>();
		params.put("id", summaryId);
		return (HisColSummary) DBAgent.find(hql.toString(), params).get(0);
	}

}
