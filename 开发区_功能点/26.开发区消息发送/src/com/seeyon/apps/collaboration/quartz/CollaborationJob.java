/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

/**
 * @author zhaifeng
 *
 */
public class CollaborationJob {
	private static Log LOG = CtpLogFactory.getLog(CollaborationJob.class);
	private static MessageRuleManager messageRuleManager;
	
    public MessageRuleManager getMessageRuleManager() {
		return messageRuleManager;
	}

	public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
		this.messageRuleManager = messageRuleManager;
	}

	public static void createQuartzJobOfSummary(ColSummary summary, WorkTimeManager workTimeManager){
        createQuartzJob(ApplicationCategoryEnum.collaboration, summary.getId(), summary.getCreateDate(),
                summary.getDeadlineDatetime(), summary.getAdvanceRemind(), summary.getOrgAccountId(), workTimeManager,summary.getMessageRuleId());
    }
    
    protected static void createQuartzJob(ApplicationCategoryEnum app,long summaryId, Date createTime, 
            Date deadLine, Long advanceRemind, Long orgAcconutID, WorkTimeManager workTimeManager,String messageRuleId){
        try{
            //超期提醒
            if(deadLine != null){
                //Date deadLineRunTime = workTimeManager.getCompleteDate4Nature(createTime, deadLine, orgAcconutID);
                StringBuilder log = new StringBuilder();
                {
                	String _jobName = "ColProcessDeadLine" + summaryId ;
                    Map<String, String> datamap = new HashMap<String, String>(3);
                    datamap.put("appType", String.valueOf(app.key()));
                    datamap.put("isAdvanceRemind", "1");
                    datamap.put("objectId", String.valueOf(summaryId));
                    if(Strings.isNotBlank(messageRuleId)){
                    	_jobName = _jobName+"_"+messageRuleId;
                    	datamap.put("messageRuleId", messageRuleId);
                    }
                    Date _deadLineRunDate = deadLine;
                    //流程期限小于当前时间，定时任务执行时间 取当前时间+10s执行
                    if(deadLine.before(new Date())){
                    	_deadLineRunDate = new Date(System.currentTimeMillis()+30*1000);
                    }
                    QuartzHolder.newQuartzJob(_jobName, _deadLineRunDate, "processCycRemindQuartzJob", datamap);
                    
                    log.append("______CollaborationJob:createQuartzJob，objectId:"+String.valueOf(summaryId)+",name:"+_jobName+",deadLine:"+ deadLine);
                }
                //提前提醒
                if (advanceRemind != null && advanceRemind > 0 ) {
                    Date advanceRemindTime = workTimeManager.getRemindDate(deadLine, advanceRemind);
                    Map<String, String> datamap = new HashMap<String, String>(3);
                    datamap.put("appType", String.valueOf(app.key()));
                    datamap.put("isAdvanceRemind", "0");
                    datamap.put("objectId", String.valueOf(summaryId));
                    String _jobName = "ColProcessRemind" + summaryId ;
                    
                    Date _advanceRemindRunDate = advanceRemindTime;
                    
                    //提前提醒小于当前时间，定时任务执行时间 取当前时间+10s执行
                    if(advanceRemindTime.before(new Date())){
                    	_advanceRemindRunDate = new Date(System.currentTimeMillis()+10*1000);
                    }
                    QuartzHolder.newQuartzJob(_jobName, _advanceRemindRunDate, "processCycRemindQuartzJob", datamap);
                    
                    log.append("name:"+_jobName+",advanceRemindTime:"+ advanceRemindTime);
                }
                
                LOG.info(log);
            }
        }catch (Exception e) {
           LOG.error("",e);
        }
    }
    
    public static void createUpdateAffairState4RepeatAffairJob(){
 		 try {
 			 	Calendar c = Calendar.getInstance();
 		    	c.setTime(new Date()); //当天
 		    	c.add(Calendar.DAY_OF_YEAR, 1); //下一天
 		        Date nexDate = c.getTime();
 		        //每天3点执行
 		        Date runTime = Datetimes.addHour(Datetimes.getTodayFirstTime(nexDate), 3);
 		        LOG.info("______增加定时任务__UpdateAffairState4RepeatAffairTask,预计执行时间:"+runTime);
 	        	if(!QuartzHolder.hasQuartzJob("updateAffairState4RepeatAffairJob_groupName", "updateAffairState4RepeatAffairJob_jobName")){
 	        		QuartzHolder.newQuartzJobPerDay("updateAffairState4RepeatAffairJob_groupName", "updateAffairState4RepeatAffairJob_jobName", runTime, "updateAffairState4RepeatAffairJob",null);
 	        	}
 	        } catch (Throwable e) {
 	        	LOG.error("",e);
 			}
 	}
}
