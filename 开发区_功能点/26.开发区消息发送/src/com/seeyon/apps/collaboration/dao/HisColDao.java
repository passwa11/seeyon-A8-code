package com.seeyon.apps.collaboration.dao;

import java.util.List;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.po.HisColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public interface HisColDao {

	public void save(HisColSummary hisColSummary) throws BusinessException;

	public ColSummary getColAllById(long summaryId) throws BusinessException;

	public List<ResultModel> iSearch(ConditionModel cModel)
			throws BusinessException;

	public List<ColSummary> getSummaryIdByFormIdAndRecordId(Long formAppId,
			Long formId, Long formRecordId) throws BusinessException;

	public HisColSummary getColSummaryById(long summaryId);

}
