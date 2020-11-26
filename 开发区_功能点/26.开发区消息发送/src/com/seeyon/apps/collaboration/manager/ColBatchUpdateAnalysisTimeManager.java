package com.seeyon.apps.collaboration.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.task.AsynchronousBatchTask;
import com.seeyon.ctp.util.Strings;

public class ColBatchUpdateAnalysisTimeManager extends AsynchronousBatchTask<Map<String,String>> {
	private static Log LOG = CtpLogFactory.getLog(ColBatchUpdateAnalysisTimeManager.class);
	
	private AffairManager affairManager;
	
	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
	public void addTask(Map<String, String> data){
		super.addTask(data);
	}
	
	
	@Override
	protected void doBatch(List<Map<String, String>> list) {
		if(Strings.isNotEmpty(list)){
			try {
                affairManager.updateSignleViewTimes(list);
            }
            catch (BusinessException e) {
                LOG.error("", e);
            }
		}
	}
}
