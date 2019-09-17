package com.seeyon.ctp.form.manager;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.form.modules.engin.base.formBase.GovdocFormOpinionSortDao;
import com.seeyon.ctp.form.po.GovdocFormOpinionSort;

public class GovdocFormOpinionSortManagerImpl implements GovdocFormOpinionSortManager{
	public GovdocFormOpinionSortDao govdocFormOpinionSortDao;
	private final static Log log = LogFactory.getLog(GovdocFormOpinionSortManagerImpl.class);
	
	public GovdocFormOpinionSortDao getGovdocFormOpinionSortDao() {
		return govdocFormOpinionSortDao;
	}
	public void setGovdocFormOpinionSortDao(
			GovdocFormOpinionSortDao govdocFormOpinionSortDao) {
		this.govdocFormOpinionSortDao = govdocFormOpinionSortDao;
	}
	@Override
	public List<GovdocFormOpinionSort> findByFormId(Long id) {
		return govdocFormOpinionSortDao.findByFormId(id);
	}
	@Override
	public void deleteByFormId(Long formId){
			govdocFormOpinionSortDao.deleteByFormId(formId);
	}
	@Override
	public void saveOrUpdateList(List<GovdocFormOpinionSort> list) {
		  govdocFormOpinionSortDao.saveOrUpdateList(list);
		
	}
}
