package com.seeyon.apps.collaboration.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.task.AsynchronousBatchTask;
import com.seeyon.ctp.util.Strings;

public class ColTaskManager extends AsynchronousBatchTask<Map<String,Object>>{

	@Override
	public void addTask(Map<String, Object> data) {
		super.addTask(data);
	}

	@Override
	protected void doBatch(List<Map<String, Object>> list) {
		if(Strings.isEmpty(list)){
			return;
		}
		
		for(Map<String, Object> data : list){
			CtpAffair affair = (CtpAffair)data.get("affair");
			ColUtil.deleteQuartzJobForNode(affair);
		}
	}
}
