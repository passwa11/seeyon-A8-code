/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.workflow.manager.impl;

import java.util.List;

import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.dao.WFProcessPropertyDao;
import com.seeyon.ctp.workflow.manager.WFProcessPropertyManager;
import com.seeyon.ctp.workflow.manager.WorkFlowMatchUserManager;
import com.seeyon.ctp.workflow.po.WFProcessProperty;

/**
 * @author renwei
 *
 */
public class WFProcessPropertyManagerImpl implements WFProcessPropertyManager{
    
    private WFProcessPropertyDao wfProcessPropertyDao;
    private WorkFlowMatchUserManager workflowMatchUserManager;

    /**
     * @param wfProcessPropertyDao the wfProcessPropertyDao to set
     */
    public void setWfProcessPropertyDao(WFProcessPropertyDao wfProcessPropertyDao) {
        this.wfProcessPropertyDao = wfProcessPropertyDao;
    }

    @Override
    public void deleteProcessPropertyById(Long id) {
        wfProcessPropertyDao.deleteProcessPropertyById(id);
    }

    public void deleteProcessPropertyByProcessId(Long id) {
        wfProcessPropertyDao.deleteProcessPropertyByProcessId(id);
    }
    @Override
    public void deleteProcessProperty(WFProcessProperty processProperty) {
        wfProcessPropertyDao.deleteProcessProperty(processProperty);
    }

    @Override
    public List<WFProcessProperty> findProcessPropertyByProcessId(Long processIdOrTemplateId) {
        return wfProcessPropertyDao.findProcessPropertyByProcessId(processIdOrTemplateId);
    }

    @Override
    public WFProcessProperty getProcessPropertyById(Long id) {
        return wfProcessPropertyDao.getProcessPropertyById(id);
    }

    @Override
    public void insertProcessProperty(WFProcessProperty processProperty) {
        wfProcessPropertyDao.insertProcessProperty(processProperty);
    }

    @Override
    public void insertProcessProperty(List<WFProcessProperty> processPropertyList) {
        wfProcessPropertyDao.insertProcessProperty(processPropertyList);        
    }

    @Override
    public void updateProcessProperty(WFProcessProperty processProperty) {
        wfProcessPropertyDao.updateProcessProperty(processProperty);
    }

    @Override
    public void updateProcessProperty(List<WFProcessProperty> processPropertyList) {
        wfProcessPropertyDao.updateProcessProperty(processPropertyList);        
    }

    @Override
    public WFProcessProperty getCaseProcessPropertyByProcessId(Long processIdOrTemplateId) {
        return wfProcessPropertyDao.getCaseProcessPropertyByProcessId(processIdOrTemplateId);
    }

    @Override
    public WFProcessProperty getTemplateProcessPropertyByProcessId(Long processIdOrTemplateId) {
        return wfProcessPropertyDao.getTemplateProcessPropertyByProcessId(processIdOrTemplateId);
    }

	@Override
	public WFProcessProperty getCaseProcessPropertyFromCache(String matchRequestToken, String processId) {
		WFProcessProperty processProperty= workflowMatchUserManager.getCaseProcessPropertyFromCache(matchRequestToken,processId);
		if(null==processProperty){
			processProperty = this.getCaseProcessPropertyByProcessId(Long.parseLong(processId));
			if(null==processProperty){
				processProperty= new WFProcessProperty();
				processProperty.setProcessId(Long.parseLong(processId));
			}
			workflowMatchUserManager.putCaseProcessPropertyToCache(matchRequestToken, processProperty);
		}
		
		if(Strings.isBlank(processProperty.getValue()) || null==processProperty.getId()){
			return null;
		}
		return processProperty;
	}

	public WorkFlowMatchUserManager getWorkflowMatchUserManager() {
		return workflowMatchUserManager;
	}

	public void setWorkflowMatchUserManager(WorkFlowMatchUserManager workflowMatchUserManager) {
		this.workflowMatchUserManager = workflowMatchUserManager;
	}

}
