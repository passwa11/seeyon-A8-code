package com.seeyon.ctp.form.manager;

import com.seeyon.ctp.form.po.GovdocFormExtend;

public interface GovdocFormExtendManager {
	public void saveOrUpdate(GovdocFormExtend govdocFormExtend);
	
	public GovdocFormExtend findByFormId(long formId);
}
