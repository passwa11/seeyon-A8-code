package com.seeyon.apps.govdoc.dao;

import com.seeyon.apps.govdoc.po.GovdocRegister;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;

public class GovdocRegisterDao {
	
	@SuppressWarnings("deprecation")
	public void saveOrUpdate(GovdocRegister po) throws BusinessException {
		DBAgent.saveOrUpdate(po);
	}

	public void delete(GovdocRegister po) throws BusinessException {
		DBAgent.delete(po);
	}
	
	public void deleteById(Long id) throws BusinessException {
		DBAgent.bulkUpdate("delete from GovdocRegister where id = ?", id);
	}
	
	public void deleteBySummaryId(Long summaryId) throws BusinessException {
		DBAgent.bulkUpdate("delete from GovdocRegister where summaryId = ?", summaryId);
	}

}
