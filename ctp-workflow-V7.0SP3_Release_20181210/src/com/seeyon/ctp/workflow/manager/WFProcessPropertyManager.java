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

package com.seeyon.ctp.workflow.manager;

import java.util.List;

import com.seeyon.ctp.workflow.po.WFProcessProperty;


/**
 * @author renwei
 *
 */
public interface WFProcessPropertyManager {
    
    public void insertProcessProperty(WFProcessProperty processProperty);
    
    public void insertProcessProperty(List<WFProcessProperty> processPropertyList);
    
    public void deleteProcessPropertyById(Long id);
    public void deleteProcessPropertyByProcessId(Long id) ;
    public void deleteProcessProperty(WFProcessProperty processProperty);
    
    public void updateProcessProperty(WFProcessProperty processProperty);

    public void updateProcessProperty(List<WFProcessProperty> processPropertyList);
    
    public WFProcessProperty getProcessPropertyById(Long id);
    
    public List<WFProcessProperty> findProcessPropertyByProcessId(Long processIdOrTemplateId);
    
    public WFProcessProperty getTemplateProcessPropertyByProcessId(Long processIdOrTemplateId);
    
    public WFProcessProperty getCaseProcessPropertyByProcessId(Long processIdOrTemplateId);

	public WFProcessProperty getCaseProcessPropertyFromCache(String matchRequestToken, String processId);
}
