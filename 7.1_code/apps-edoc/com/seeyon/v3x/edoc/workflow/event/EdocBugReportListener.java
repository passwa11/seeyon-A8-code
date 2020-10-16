package com.seeyon.v3x.edoc.workflow.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.event.EdocFinishEvent;
import com.seeyon.apps.edoc.event.EdocStepBackEvent;
import com.seeyon.apps.edoc.event.EdocStopEvent;
import com.seeyon.apps.edoc.event.EdocWorkflowFinishEvent;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

public class EdocBugReportListener {
    private static Log LOGGER = LogFactory.getLog(EdocBugReportListener.class);
    private WorkflowApiManager wapi;
    
    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }
    
    @ListenEvent(event = EdocFinishEvent.class)
    public void onEdocFinishEvent(EdocFinishEvent edocFinishEvent) throws BusinessException {
        CtpAffair affair =   edocFinishEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    @ListenEvent(event = EdocStepBackEvent.class)
    public void onEdocStepBackEvent(EdocStepBackEvent edocStepBackEvent) throws BusinessException {
        CtpAffair affair =   edocStepBackEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    @ListenEvent(event = EdocStopEvent.class)
    public void onEdocStopEvent(EdocStopEvent edocStopEvent) throws BusinessException {
        CtpAffair affair =   edocStopEvent.getAffair();
        if(affair != null && AffairUtil.isBugReport(affair)){
            setAlreadyDeal(affair);
        }
    }
    
    @ListenEvent(event = EdocWorkflowFinishEvent.class)
    public void onEdocWorkflowFinishEvent(EdocWorkflowFinishEvent edocWorkflowFinishEvent) throws BusinessException {
        Long summaryId = edocWorkflowFinishEvent.getSummaryId();
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
        map.put("summaryId", String.valueOf(affair.getObjectId()));
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
