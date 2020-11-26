package com.seeyon.apps.collaboration.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.event.CollaborationAppointStepBackEvent;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.collaboration.event.CollaborationDelEvent;
import com.seeyon.apps.collaboration.event.CollaborationProcessEvent;
import com.seeyon.apps.collaboration.event.CollaborationStepBackEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

public class ColBugReportListener {
    private static Log LOGGER = LogFactory.getLog(ColBugReportListener.class);
    private WorkflowApiManager wapi;
    
    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    @ListenEvent(event = CollaborationDelEvent.class)
    public void onCollaborationDelEvent(CollaborationDelEvent collaborationDelEvent) throws BusinessException {
        CtpAffair affair =   collaborationDelEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    @ListenEvent(event = CollaborationProcessEvent.class)
    public void onCollaborationProcessEvent(CollaborationProcessEvent collaborationProcessEvent) throws BusinessException {
        CtpAffair affair =   collaborationProcessEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    @ListenEvent(event = CollaborationStepBackEvent.class)
    public void onCollaborationStepBackEvent(CollaborationStepBackEvent collaborationStepBackEvent) throws BusinessException {
        CtpAffair affair =   collaborationStepBackEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    
    @ListenEvent(event = CollaborationStopEvent.class)
    public void onCollaborationStopEvent(CollaborationStopEvent collaborationStopEvent) throws BusinessException {
        CtpAffair affair =   collaborationStopEvent.getAffair();
        if(affair != null){
            setAlreadyDeal(affair.getObjectId());
        }
    }
    
    @ListenEvent(event = CollaborationAppointStepBackEvent.class)
    public void onCollaborationAppointStepBackEvent(CollaborationAppointStepBackEvent collaborationAppointStepBackEvent) throws BusinessException {
        Long summaryId = collaborationAppointStepBackEvent.getSummaryId();
        if(summaryId != null){
            setAlreadyDeal(summaryId);
        }
    }
    
    @ListenEvent(event = CollaborationCancelEvent.class)
    public void onCollaborationCancelEvent(CollaborationCancelEvent collaborationCancelEvent) throws BusinessException {
        Long summaryId =   collaborationCancelEvent.getSummaryId();
        if(summaryId != null){
            setAlreadyDeal(summaryId);
        }

    }

    
    
    /**
     * 更新流程异常数据为已处理
     * @param affair
     */
    private void setAlreadyDeal(CtpAffair affair) {
        if(affair == null) {
            return ;
        }
        Map<String,String> map = new HashMap<String, String>();
        map.put("affairId", String.valueOf(affair.getId()));
        try {
            wapi.setAlreadyDeal(map);
        } catch (BusinessException e) {
            LOGGER.error("更新流程异常数据为已处理异常",e);
        }
    }
    
    /**
     * 更新流程异常数据为已处理
     * @param affair
     */
    private void setAlreadyDeal(Long summaryId) {
        if(summaryId == null) {
            return ;
        }
        Map<String,String> map = new HashMap<String, String>();
        map.put("summaryId", String.valueOf(summaryId));
        try {
            wapi.setAlreadyDeal(map);
        } catch (BusinessException e) {
            LOGGER.error("更新流程异常数据为已处理异常",e);
        }
    }
}
