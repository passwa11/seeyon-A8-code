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
package com.seeyon.apps.collaboration.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * @author mujun
 *
 */
public interface Col4WFAnalysisManager {
	public Integer getCaseCountByTempleteId(Long accountId, Long templateId,
			List<Integer> workFlowState, Date startDate, Date endDate)
			throws BusinessException;

	public Integer getCaseCountGTSD(Long accountId, Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate,
			Integer standarduration);

	public Double getOverCaseRatioByTempleteId(Long accountId, Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate);

	public List<ColSummary> getColSummaryList(Long acccountId, Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate);

	public Integer getAvgRunWorkTimeByTempleteId(Long accountId,
			Long templeteId, List<Integer> workFlowState, Date startDate,
			Date endDate);

	public List<Object[]> statByGroup(int appType, List<Long> entityId,
			String entityType, Date beginDate, Date endDate);

	public List<Object[]> statByAccount(int appType, List<Long> templateId,
			List<Long> entityId, String entityType, Date beginDate, Date endDate);

	public List<Object[]> transStatList(FlipInfo flipInfo,
			Map<String, Object> query);

	public List<Object[]> getAccountStat(Integer appType,
			List<Long> templateId, Boolean onlySelfFlow, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> map);

	public List<Object[]> getAccountDeadline(int appType,
			List<Long> templateId, boolean onlySelfFlow, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> namedParameterMap);

	public List<Object[]> getGroupDeadline(int appType, List<Long> entityId,
			String entityType, Date beginDate, Date endDate,
			Map<String, Object> namedParameterMap);
}
