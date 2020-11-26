package com.seeyon.apps.collaboration.manager;

import java.util.List;

import com.seeyon.apps.collaboration.dao.HisColDao;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.po.HisColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public class HisColManagerImpl implements HisColManager {
	private HisColDao hisColDao;

	@Override
	public void save(ColSummary summary) throws BusinessException {
		HisColSummary hisColSummary = new HisColSummary();
		BeanUtils.convert(hisColSummary, summary);
		hisColDao.save(hisColSummary);
	}


	@Override
	public ColSummary getColSummaryById(long summaryId)
			throws BusinessException {
		HisColSummary hisSummary=hisColDao.getColSummaryById(summaryId);
		ColSummary summary = new ColSummary();
        BeanUtils.convert(summary, hisSummary);
		return summary;
	}

	@Override
	public ColSummary getColAllById(long summaryId) throws BusinessException {
		return hisColDao.getColAllById(summaryId);
	}

	@Override
	public List<ResultModel> iSearch(ConditionModel cModel)
			throws BusinessException {
		return hisColDao.iSearch(cModel);
	}

	@Override
	public List<ColSummary> getSummaryIdByFormIdAndRecordId(Long formAppId,
			Long formId, Long formRecordId) throws BusinessException {
		return hisColDao.getSummaryIdByFormIdAndRecordId(formAppId, formId,
				formRecordId);
	}


    public void setHisColDao(HisColDao hisColDao) {
        this.hisColDao = hisColDao;
    }
}
