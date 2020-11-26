package com.seeyon.apps.collaboration.quartz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.quartz.WorkflowNodeOvertimeAppHandler;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.util.Strings;

public class UpdateAffairState4RepeatAffairJob implements QuartzJob {
	private final static Log log = LogFactory.getLog(UpdateAffairState4RepeatAffairJob.class);
	private WorkflowNodeOvertimeAppHandler collNodeOverTimeManager;
	private AffairManager affairManager;
	private ColManager colManager;

	public void execute(Map<String, String> parameters) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("state", StateEnum.col_pending_repeat_auto_deal.getKey());
		log.info("开始更新自动跳过未跳过的数据");
		try {
			List<CtpAffair> affairs = affairManager.getByConditions(null,param);
			String forwardMember = "";
			if(Strings.isNotEmpty(affairs)){
				for (CtpAffair affair : affairs) {
					if(null!=affair && affair.getObjectId()!=null){
						
						ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
						if(null!=summary){
							forwardMember = summary.getForwardMember();
						}
						collNodeOverTimeManager.updateAffairState4RepeatAffair(affair, forwardMember);
					}
				}
			}
			
		} catch (BusinessException e) {
			log.error("", e);
		}
		log.info("更新自动跳过未跳过的数据结束");
	}






    public WorkflowNodeOvertimeAppHandler getCollNodeOverTimeManager() {
        return collNodeOverTimeManager;
    }






    public void setCollNodeOverTimeManager(WorkflowNodeOvertimeAppHandler collNodeOverTimeManager) {
        this.collNodeOverTimeManager = collNodeOverTimeManager;
    }






    public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

}