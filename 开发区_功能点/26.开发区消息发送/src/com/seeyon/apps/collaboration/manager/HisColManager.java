package com.seeyon.apps.collaboration.manager;

import java.util.List;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.po.HisColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public interface HisColManager {
	/**
	 * 保存协同对象
	 * @param hisColSummary
	 * @throws BusinessException
	 */
	public void save(ColSummary colSummary) throws BusinessException;
	/**
	 * 
	 * @param summaryId
	 * @param needBody
	 * @return
	 * @throws BusinessException
	 */
	public ColSummary getColSummaryById(long summaryId) throws BusinessException;
	/**
	 * 
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public ColSummary getColAllById(long summaryId) throws BusinessException;
	/**
	 * 
	 * @param cModel
	 * @return
	 * @throws BusinessException
	 */
	public List<ResultModel> iSearch(ConditionModel cModel) throws BusinessException;
	/**
	 * 
	 * @param formAppId
	 * @param formId
	 * @param formRecordId
	 * @return
	 * @throws BusinessException
	 */
	public List<ColSummary> getSummaryIdByFormIdAndRecordId(Long formAppId, Long formId, Long formRecordId) throws BusinessException;

}
