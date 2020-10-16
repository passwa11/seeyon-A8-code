package com.seeyon.apps.govdoc.manager.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.manager.GovdocQuarzManager;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

/**
 * 新公文定时器管理类
 * @author 唐桂林
 *
 */
public class GovdocQuarzManagerImpl implements GovdocQuarzManager {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocQuarzManagerImpl.class);
	
	private WorkTimeManager workTimeManager;
	
	@Override
	public void createSendQuartzJob(GovdocNewVO info) throws BusinessException {
		try {
			EdocSummary summary = info.getSummary();
			Date deadLine = summary.getDeadlineDatetime();
			Long summaryId = summary.getId();
			Long advanceRemind = summary.getAdvanceRemind();
			String DeadLine_jobName = "GovdocProcessDeadLine" + summaryId;
			String Remind_jobName = "GovdocProcessRemind" + summaryId;
			if(QuartzHolder.hasQuartzJob(DeadLine_jobName)){
				QuartzHolder.deleteQuartzJob(DeadLine_jobName);
			}
			if(QuartzHolder.hasQuartzJob(Remind_jobName)){
				QuartzHolder.deleteQuartzJob(Remind_jobName);
			}
			// 超期提醒
			if (deadLine != null) {
				// Date deadLineRunTime =
				// workTimeManager.getCompleteDate4Nature(createTime, deadLine,
				// orgAcconutID);
				StringBuilder log = new StringBuilder();
				{
					Map<String, String> datamap = new HashMap<String, String>(3);
					datamap.put("appType", String.valueOf(ApplicationCategoryEnum.edoc.key()));
					datamap.put("isAdvanceRemind", "1");
					datamap.put("objectId", String.valueOf(summaryId));
					QuartzHolder.newQuartzJob(DeadLine_jobName, deadLine, "processCycRemindQuartzJob", datamap);

					log.append("______CollaborationJob:createQuartzJob，objectId:" + String.valueOf(summaryId) + ",name:" + Remind_jobName + ",deadLine:" + deadLine);
				}
				// 提前提醒
				if (advanceRemind != null && advanceRemind > 0) {
					Date advanceRemindTime = workTimeManager.getRemindDate(deadLine, advanceRemind);
					Map<String, String> datamap = new HashMap<String, String>(3);
					datamap.put("appType", String.valueOf(ApplicationCategoryEnum.edoc.key()));
					datamap.put("isAdvanceRemind", "0");
					datamap.put("objectId", String.valueOf(summaryId));

					QuartzHolder.newQuartzJob(Remind_jobName, advanceRemindTime, "processCycRemindQuartzJob", datamap);

					log.append("name:" + Remind_jobName + ",advanceRemindTime:" + advanceRemindTime);
				}

				LOGGER.info(log);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}
	
}
