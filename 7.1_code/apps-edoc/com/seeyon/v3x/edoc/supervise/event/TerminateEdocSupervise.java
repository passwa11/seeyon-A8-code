package com.seeyon.v3x.edoc.supervise.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;

public class TerminateEdocSupervise implements Job{
	private final static Log log = LogFactory
			.getLog(TerminateEdocSupervise.class);
	public void execute(JobExecutionContext datamap) throws JobExecutionException{
		
		try{
	    	List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
	    	String key = "edoc.supervise.overdue";
			JobDetail jobDetail = datamap.getJobDetail();
			JobDataMap jobDataMap = jobDetail.getJobDataMap();
			
			long edocSuperviseId = jobDataMap.getLongFromString("edocSuperviseId");
			String subject = jobDataMap.getString("subject");
			
			SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
			CtpSuperviseDetail detail = superviseManager.get(edocSuperviseId);
			
			if(null == detail)return;
			EdocSummaryDao edocSummaryDao = (EdocSummaryDao)AppContext.getBean("edocSummaryDao");
			EdocSummary edocSummary = edocSummaryDao.get(detail.getEntityId());
			ApplicationCategoryEnum appEnum =  ApplicationCategoryEnum.edoc;
			Long startUserId = 0L;
			if(null!=edocSummary){
    			if(edocSummary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()){
    				appEnum = ApplicationCategoryEnum.edocSend;
    			}else if(edocSummary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()){
    				appEnum = ApplicationCategoryEnum.edocRec;
    			}else if(edocSummary.getEdocType() == EdocEnum.edocType.signReport.ordinal()){
    				appEnum = ApplicationCategoryEnum.edocSign;
    			}
    			startUserId = edocSummary.getStartUserId();
    		}
	    	List<CtpSupervisor>  supervisors  = superviseManager.getSupervisors(detail.getId());
	    	if(Strings.isNotEmpty(supervisors)){
    	    	for(CtpSupervisor  supervisor : supervisors){
    	    		if(supervisor.getSupervisorId().longValue() != startUserId.longValue()) {
    	    			MessageReceiver receiver = new MessageReceiver(edocSuperviseId,supervisor.getSupervisorId(), "message.link.edoc.supervise.detail", detail.getEntityId());
    	    			receivers.add(receiver);
    	    		}
    	    	}
	    	}
			
	    	try{
	    		UserMessageManager userMessageManager = (UserMessageManager)AppContext.getBean("userMessageManager");
	    		userMessageManager.sendSystemMessage(new MessageContent(key, subject,appEnum.getKey()), appEnum, detail.getSenderId(), receivers,EdocMessageFilterParamEnum.supervise.key);
	    		
	    	}catch(Exception e){
	    		log.error(e.getMessage(), e);
	    	}
		}catch(Exception e1){
			log.error(e1.getMessage(), e1);
		}
    }
}
