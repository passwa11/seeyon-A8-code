package com.seeyon.ctp.form.modules.engin.base.formBase;

import com.seeyon.ctp.form.po.GovdocFormExtend;

public interface GovdocFormExtendDao {
	public void saveOrUpdate(GovdocFormExtend govdocFormExtend);
	
	public GovdocFormExtend findByFormId(long formId);
	
	public void deleteByFormId(long formId);
}
