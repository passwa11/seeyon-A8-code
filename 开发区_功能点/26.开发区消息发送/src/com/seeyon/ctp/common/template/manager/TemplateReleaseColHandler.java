package com.seeyon.ctp.common.template.manager;

import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;

/**  
* <p>Title: TemplateReleaseColHandler.java</p>  
* <p>Copyright: Copyright (c) 2018</p>  
* <p>Company: www.seeyon.com</p>  
* @author muj  
* @date 2018年12月7日  
* @version 1.0  
*/
public class TemplateReleaseColHandler extends TemplateReleaseHandler {

	private FormTemplateDesignManager formTemplateDesignManager;
	
	


	public FormTemplateDesignManager getFormTemplateDesignManager() {
		return formTemplateDesignManager;
	}

	public void setFormTemplateDesignManager(FormTemplateDesignManager formTemplateDesignManager) {
		this.formTemplateDesignManager = formTemplateDesignManager;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.collaboration;
	}

	@Override
	public void doWhenRelease(CtpTemplate template, CtpTemplateHistory history) throws BusinessException {

		formTemplateDesignManager.cloneAndSaveTemplateHistoryToTemplate(template,history,false,false);

	}

}
