package com.seeyon.ctp.workflow.manager.impl;

import java.util.Date;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.dao.IProcessXmlTempDao;
import com.seeyon.ctp.workflow.manager.ProcessXmlTempManager;
import com.seeyon.ctp.workflow.po.ProcessXmlTemp;

public class ProcessXmlTempManagerImpl implements ProcessXmlTempManager {

	private static final Log logger = CtpLogFactory.getLog(ProcessXmlTempManagerImpl.class);
	
	public void init() {
		try {
			this.deleteAll();
		} catch(Exception e) {
			logger.error("删除流程ProcessXmlTemp表出错", e);
		}
		startJobToClearProcessXml();
	}
	
	public void startJobToClearProcessXml() {
		Date tomorrow = DateUtil.addDay(Datetimes.parse(Datetimes.format(new Date(), "yyyy-MM-dd")+" 04:00:00"), 1);
		try {
			logger.info("预约下一次流程processXml清理时间："+Datetimes.format(tomorrow, "yyyy-MM-dd HH:mm:ss"));
			if(!QuartzHolder.hasQuartzJob("process_xml_temp_id_clear")){
			    QuartzHolder.newQuartzJob("process_xml_temp_id_clear", tomorrow, "processXmlTempJob", null);
			}
		} catch(Exception e) {
			logger.error("流程processXml临时表清理失败", e);
		}
	}
	
	public ProcessXmlTemp saveProcessXmlTemp(String processId, String processXml, String activityId, String userId, String action) throws BusinessException {
		
		IProcessXmlTempDao processXmlTempDao = (IProcessXmlTempDao)AppContext.getBean("processXmlTempDao");
		ProcessXmlTemp temp = new ProcessXmlTemp();
		temp.setNewId();
		temp.setProcessId(Strings.isBlank(processId) ? "-1" : processId);
		temp.setProcessXml(processXml);
		temp.setActivityId(Strings.isBlank(activityId) ? -1 : Long.parseLong(activityId));
		temp.setUserId(Strings.isBlank(userId) ? -1 : Long.parseLong(userId));
		temp.setAction(Strings.isBlank(action) ? -1 : Integer.parseInt(action));
		temp.setCreateTime(new java.sql.Timestamp(DateUtil.currentDate().getTime()));
		processXmlTempDao.saveProcessXmlTemp(temp);
		return temp;
	}

	public ProcessXmlTemp getProcessXmlTemp(Long id) throws BusinessException {
		IProcessXmlTempDao processXmlTempDao = (IProcessXmlTempDao)AppContext.getBean("processXmlTempDao");
		return processXmlTempDao.getProcessXmlTemp(id);
	}
	
	public void deleteAll() throws BusinessException {
		IProcessXmlTempDao processXmlTempDao = (IProcessXmlTempDao)AppContext.getBean("processXmlTempDao");
		processXmlTempDao.deleteAll();
	}
	
}
