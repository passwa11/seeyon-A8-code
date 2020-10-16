package com.seeyon.apps.govdoc.option.dao;

import com.seeyon.apps.govdoc.po.FormOptionExtend;

public interface FormOptionExtendDao {
	public void saveOrUpdate(FormOptionExtend govdocFormExtend);
	
	public FormOptionExtend findByFormId(long formId);
	
	public void deleteByFormId(long formId);
}
