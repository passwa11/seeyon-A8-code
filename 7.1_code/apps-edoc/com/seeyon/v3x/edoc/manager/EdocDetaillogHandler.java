package com.seeyon.v3x.edoc.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.detaillog.manager.DetaillogHandler;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocDetaillogHandler implements DetaillogHandler {
	private static final Log LOGGER = LogFactory.getLog(EdocDetaillogHandler.class);
	private EdocManager edocManager;
	
	private TemplateManager templateManager;
	
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.edoc;
	}

	@Override
	public String getAppName() {
		return ApplicationCategoryEnum.edoc.name();
	}

	@Override
	public long getFlowPermAccountId(long summaryId) throws BusinessException {
		EdocSummary  summary= edocManager.getEdocSummaryById(summaryId, false);
		Long flowPermAccountId = AppContext.currentAccountId();
    	if(summary != null){
    		if(summary.getTempleteId() != null){
    			CtpTemplate templete;
				try {
					templete = templateManager.getCtpTemplate(summary.getTempleteId());
					if(templete != null){
	    				flowPermAccountId = templete.getOrgAccountId();
	    			}
				} catch (BusinessException e) {
				    LOGGER.error("", e);
				}
    		}
    		else{
    			if(summary.getOrgAccountId() != null){
    				flowPermAccountId = summary.getOrgAccountId();
    			}
    		}
    	}
    	return flowPermAccountId;
	}

}
