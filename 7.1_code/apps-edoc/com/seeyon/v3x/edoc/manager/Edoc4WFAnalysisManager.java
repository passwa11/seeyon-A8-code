/**
 * Author : xuqw
 *   Date : 2015年8月28日 上午10:38:20
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.ctp.common.affair.bo.WorkflowAnalysisParam;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * <p>Title       : 公文对绩效考核提供的API</p>
 * <p>Description : 提供给绩效考核相关的统计数据</p>
 * <p>Copyright   : Copyright (c) 2015</p>
 * <p>Company     : seeyon.com</p>
 */
public interface Edoc4WFAnalysisManager {

    /**
     * 得到一个模板的实例总数
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回模板的实例总数<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam 绩效分析相关的参数类
     * @return
     * @throws BusinessException
     */
    public Integer getCaseCountByTemplateId (WorkflowAnalysisParam workflowAnalysisParam) throws BusinessException;
    
    
    /**
     * 根据模板得到此模板某段时间的平均运行时长。
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回模板某段时间的平均运行时长<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam
     * @return
     * @throws BusinessException
     */
    public Integer getAvgRunWorkTimeByTemplateId(WorkflowAnalysisParam workflowAnalysisParam) throws BusinessException;
    
    
    /**
     * 流程处理时间大于基准时间的实例数
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回流程处理时间大于基准时间的实例数<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam
     * @param standardDuration 模板的基准时间
     * @return
     * @throws BusinessException
     */
    public Integer getCaseCountGtStandardDuration(WorkflowAnalysisParam workflowAnalysisParam,Integer standardDuration) throws BusinessException;
    
    /**
     * 得到某个模板某段时间的流程超期率
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回模板某段时间的流程超期率<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam
     * @return
     * @throws BusinessException
     */
    public Double getOverCaseRatioByTemplateId(WorkflowAnalysisParam workflowAnalysisParam) throws BusinessException;
    
    /**
     * 得到某个模板某段时间的所有公文流程
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回模板某段时间的所有公文流程<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam
     * @return
     * @throws BusinessException
     */
    public List<EdocSummaryBO> findEdocSummaryList(WorkflowAnalysisParam workflowAnalysisParam) throws BusinessException;
    
    /**
     * 获得以公文结束时间作为查询开始或结束时间的列表，其他条件与getEdocSummaryList接口一致
     *
     * 正常:<br>
     *     1、传入正确的绩效分析相关参数实体，返回以公文结束时间作为查询开始或结束时间的列表<br>
     * 异常:<br>
     *     2、传入错误的绩效分析相关参数实体，返回空<br>
     *
     * @param workflowAnalysisParam
     * @return
     * @throws BusinessException
     */
    public List<EdocSummaryBO> findEdocSummaryListByCompleteTime(WorkflowAnalysisParam workflowAnalysisParam) throws BusinessException;
    
}
