package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 新公文主表接口
 * @author 唐桂林
 *
 */
public interface GovdocSummaryManager {
	
	public EdocSummary getSummaryById(Long id) throws BusinessException;
	
	public EdocSummary getSummaryById(Long id, boolean hasExtend) throws BusinessException;
	
	public EdocSummary getSummaryByProcessId(String processId) throws BusinessException;

	public EdocSummary getSummaryByIdHistory(Long id) throws BusinessException;
	
	public EdocSummary getSummaryByProcessIdHistory(Long processId) throws BusinessException;

	public List<Object[]> getLargeFieldSummaryList(List<Long> summaryIdList) throws BusinessException;
	
	public List<Object[]> getInfoField2RecVo(Long summaryId) throws BusinessException;
	
	public Map<String, Object> getSummaryMapById(Long id) throws BusinessException;
	
	public List<Map<String, Object>> getSummaryListById(List<Long> idList) throws BusinessException;
	
	public EdocBody getFirstBody(Long summaryId) throws BusinessException;
	
	public List<EdocBody> getEdocBodys(Long summaryId) throws BusinessException;
	
	public void saveOrUpdateEdocSummary(EdocSummary po) throws BusinessException;
	
	public void saveEdocSummary(EdocSummary po,boolean isSaveExtend) throws BusinessException;
	
	public void updateEdocSummary(EdocSummary po) throws BusinessException;
	
	public void updateEdocSummary(EdocSummary po,boolean isSaveExtend) throws BusinessException;
	
	public void updateEdocSummary(long id, Map<String, Object> columnValue) throws BusinessException;
	
	public void updateEdocSummaryState(Long edocId, int state) throws BusinessException;
	
	public void update(Long id, Map<String, Object> columns);
	
	public void deleteEdocSummary(EdocSummary po) throws BusinessException;
	
	public void deleteEdocSummary(Long summaryId) throws BusinessException;
	
	public void transSetFinishedFlag(EdocSummary summary) throws BusinessException;
	
	public UserUpdateObject editObjectState(String objId);
	
	public void deleteSummaryAndAffair(Long summaryId);
	
	public boolean unlockEdocFormLock(String objId, String userId);
	
	public EdocSummary getSummaryByCaseId(Long caseId);

	public void deleteSummaryByFormRecordId(Long formRecordid);
	
	public EdocBody getBodyByIdAndNum(Long summaryId,int contentNum) throws BusinessException;
	
	public void updateSummaryArchiveId(Long state,Long archiveId) throws BusinessException;
}

