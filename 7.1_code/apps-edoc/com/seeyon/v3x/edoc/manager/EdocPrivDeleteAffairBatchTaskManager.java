package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.task.AsynchronousBatchTask;
import com.seeyon.ctp.util.Strings;

public class EdocPrivDeleteAffairBatchTaskManager extends AsynchronousBatchTask<Map<String,Object>> {

	private AffairManager affairManager;
	private static Log log = CtpLogFactory.getLog(EdocPrivDeleteAffairBatchTaskManager.class);
	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	@Override
	public void addTask(Map<String, Object> e) {
		super.addTask(e);
	}

	@Override
	protected void doBatch(List<Map<String, Object>> list) {
		if(Strings.isEmpty(list)){
			return;
		}

		for(Map<String,Object> m : list){
			List<Long> deleteAffairIds = (List<Long>)m.get("deleteAffairIds");
			if(Strings.isEmpty(deleteAffairIds)){
				continue;
			}
			
			if(deleteAffairIds.size()>0){
				for(Long c : deleteAffairIds){
					try {
						affairManager.deletePhysical(c);
					} catch (BusinessException e) {
						log.error("公文 删除affair数据错误", e);
					}
				}
			}
		}
	}
}
