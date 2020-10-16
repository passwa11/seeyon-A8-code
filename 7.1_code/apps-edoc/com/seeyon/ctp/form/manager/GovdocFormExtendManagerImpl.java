package com.seeyon.ctp.form.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.form.modules.engin.base.formBase.GovdocFormExtendDao;
import com.seeyon.ctp.form.po.GovdocFormExtend;

public class GovdocFormExtendManagerImpl implements GovdocFormExtendManager{
	private GovdocFormExtendDao govdocFormExtendDao;
	
	@Override
	public void saveOrUpdate(GovdocFormExtend govdocFormExtend) {
		govdocFormExtendDao.deleteByFormId(govdocFormExtend.getFormId());
		govdocFormExtendDao.saveOrUpdate(govdocFormExtend);
	}

	@Override
	public GovdocFormExtend findByFormId(long formId) {
		if(null == govdocFormExtendDao){
			govdocFormExtendDao = (GovdocFormExtendDao)AppContext.getBean("govdocFormExtendDao");
		}
		return govdocFormExtendDao.findByFormId(formId);
	}

	public GovdocFormExtendDao getGovdocFormExtendDao() {
		return govdocFormExtendDao;
	}

	public void setGovdocFormExtendDao(GovdocFormExtendDao govdocFormExtendDao) {
		this.govdocFormExtendDao = govdocFormExtendDao;
	}
	
	
}
