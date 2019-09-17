package com.seeyon.ctp.form.modules.bindprint.manager;

import com.seeyon.ctp.form.modules.bindprint.dao.FormPrintBindDao;
import com.seeyon.ctp.form.po.FromPrintBind;

public class FromPrintBindManagerImpl implements FormPrintBindManager {
	private FormPrintBindDao fromPrintBindDao;

	@Override
	public void deletePrintMode(long unitId, long edocXsnId) {
		fromPrintBindDao.deleteEdocLPrintModeById(unitId, edocXsnId);
	}

	@Override
	public FromPrintBind findPrintMode(long unitId, long edocXsnId) {
		return fromPrintBindDao.findEdocPrintModeById(unitId, edocXsnId);
	}

	@Override
	public void saveOrUpdatePrintMode(FromPrintBind printObj) {
		fromPrintBindDao.saveOrUpdateEdocPrintMode(printObj);
	}

	public void setFromPrintBindDao(FormPrintBindDao fromPrintBindDao) {
		this.fromPrintBindDao = fromPrintBindDao;
	}
	
}
