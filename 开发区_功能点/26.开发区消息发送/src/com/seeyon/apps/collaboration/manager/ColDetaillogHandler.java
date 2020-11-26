package com.seeyon.apps.collaboration.manager;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.detaillog.manager.DetaillogHandler;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;

public class ColDetaillogHandler implements DetaillogHandler {
	
	private ColManager colManager;
	private static Log LOG = CtpLogFactory.getLog(ColDetaillogHandler.class);
	

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.collaboration;
	}

	@Override
	public String getAppName() {
		return ApplicationCategoryEnum.collaboration.name();
	}

	@Override
	public long getFlowPermAccountId(long summaryId) throws BusinessException {
		long accountId = 0;
		try {
			ColSummary summary = colManager.getSummaryById(summaryId);
			if (summary != null) {
				accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
			}
		}catch(Exception e) {
		    LOG.error("", e);
		}
		return accountId;
	}

}
