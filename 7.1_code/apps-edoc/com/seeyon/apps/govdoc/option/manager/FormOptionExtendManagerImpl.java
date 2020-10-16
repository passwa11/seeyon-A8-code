package com.seeyon.apps.govdoc.option.manager;

import com.seeyon.apps.govdoc.option.dao.FormOptionExtendDao;
import com.seeyon.apps.govdoc.po.FormOptionExtend;

public class FormOptionExtendManagerImpl implements FormOptionExtendManager{
	private FormOptionExtendDao formOptionExtendDao;
	
	@Override
	public void saveOrUpdate(FormOptionExtend govdocFormExtend) {
		formOptionExtendDao.deleteByFormId(govdocFormExtend.getFormId());
		formOptionExtendDao.saveOrUpdate(govdocFormExtend);
	}

	@Override
	public FormOptionExtend findByFormId(long formId) {
		return formOptionExtendDao.findByFormId(formId);
	}

	public FormOptionExtendDao getFormOptionExtendDao() {
		return formOptionExtendDao;
	}

	public void setFormOptionExtendDao(FormOptionExtendDao formOptionExtendDao) {
		this.formOptionExtendDao = formOptionExtendDao;
	}

	
	
	
}
