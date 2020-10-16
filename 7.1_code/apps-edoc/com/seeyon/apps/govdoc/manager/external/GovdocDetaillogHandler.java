package com.seeyon.apps.govdoc.manager.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.detaillog.manager.DetaillogHandler;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocDetaillogHandler implements DetaillogHandler {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocDetaillogHandler.class);
	
	@Override
	public ModuleType getModuleType() {
		return ModuleType.edoc;
	}
	
	private GovdocSummaryManager govdocSummaryManager;
	private TemplateManager templateManager;

	@Override
	public String getAppName() {
		return ApplicationCategoryEnum.edoc.name();
	}

	@Override
	public long getFlowPermAccountId(long summaryId) throws BusinessException {
		EdocSummary  summary= govdocSummaryManager.getSummaryById(summaryId, false);
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

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
}
