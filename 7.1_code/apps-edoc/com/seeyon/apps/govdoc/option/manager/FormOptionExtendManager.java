package com.seeyon.apps.govdoc.option.manager;

import com.seeyon.apps.govdoc.po.FormOptionExtend;

public interface FormOptionExtendManager {
	public void saveOrUpdate(FormOptionExtend govdocFormExtend);
	
	public FormOptionExtend findByFormId(long formId);
}
