/**
 * Author : xuqw
 *   Date : 2015年8月28日 上午10:41:31
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.ctp.common.affair.bo.WorkflowAnalysisParam;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.DataTransUtil;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 专为绩效考核提供的接口</p>
 * <p>Copyright   : Copyright (c) 2015</p>
 * <p>Company     : seeyon.com</p>
 */
public class Edoc4WFAnalysisManagerImpl implements Edoc4WFAnalysisManager {
    
    private EdocSummaryManager edocSummaryManager = null;
    
    @Override
    public Integer getCaseCountByTemplateId(
            WorkflowAnalysisParam workflowAnalysisParam)
            throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        return edocSummaryManager.getCaseCountByTempleteId(accountId, templeteId, workFlowState, startDate, endDate);
    }

    @Override
    public Integer getAvgRunWorkTimeByTemplateId(
            WorkflowAnalysisParam workflowAnalysisParam)
            throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        return edocSummaryManager.getAvgRunWorkTimeByTempleteId(accountId, templeteId, workFlowState, startDate, endDate);
    }

    @Override
    public Integer getCaseCountGtStandardDuration(
            WorkflowAnalysisParam workflowAnalysisParam,
            Integer standardDuration) throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        return edocSummaryManager.getCaseCountGTSD(accountId, templeteId, workFlowState, startDate, endDate, standardDuration);
    }

    @Override
    public Double getOverCaseRatioByTemplateId(
            WorkflowAnalysisParam workflowAnalysisParam)
            throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        return edocSummaryManager.getOverCaseRatioByTempleteId(accountId, templeteId, workFlowState, startDate, endDate);
    }

    @Override
    public List<EdocSummaryBO> findEdocSummaryList(
            WorkflowAnalysisParam workflowAnalysisParam)
            throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        List<EdocSummary> summarys = edocSummaryManager.getEdocSummaryList(accountId, templeteId, workFlowState, startDate, endDate);
        
        List<EdocSummaryBO> bos = new ArrayList<EdocSummaryBO>();
        if(Strings.isNotEmpty(summarys)){
            for(EdocSummary s : summarys){
                bos.add(DataTransUtil.transEdocSummary2BO(s));
            }
        }
        
        return bos;
    }

    @Override
    public List<EdocSummaryBO> findEdocSummaryListByCompleteTime(
            WorkflowAnalysisParam workflowAnalysisParam)
            throws BusinessException {
        
        Long accountId = workflowAnalysisParam.getOrgAccountId();
        Long templeteId = workflowAnalysisParam.getTempleteId();
        List<Integer> workFlowState = workflowAnalysisParam.getWorkFlowStates();
        Date startDate =  workflowAnalysisParam.getStartDate();
        Date endDate =  workflowAnalysisParam.getEndDate();
        
        List<EdocSummary> summarys = edocSummaryManager.getEdocSummaryCompleteTimeList(accountId, templeteId, workFlowState, startDate, endDate);
        
        List<EdocSummaryBO> bos = new ArrayList<EdocSummaryBO>();
        if(Strings.isNotEmpty(summarys)){
            for(EdocSummary s : summarys){
                bos.add(DataTransUtil.transEdocSummary2BO(s));
            }
        }
        
        return bos;
    }
    
    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }
}
