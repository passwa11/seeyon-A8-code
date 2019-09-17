package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.dao.GovdocDao;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.edoc.dao.EdocBodyDao;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;

/**
 * 新公文主表管理类
 * @author 唐桂林
 *
 */
public class GovdocSummaryManagerImpl implements GovdocSummaryManager {
	
	private EdocBodyDao edocBodyDao;
	private EdocSummaryDao edocSummaryDao;
	private GovdocDao govdocDao;
	private EdocSummaryManager edocSummaryManager;

	@Override
	public EdocSummary getSummaryById(Long id) throws BusinessException {
		return getSummaryById(id, false);
	}
	
	@Override
	public EdocSummary getSummaryById(Long id, boolean hasExtend) throws BusinessException {
		return edocSummaryManager.getEdocSummaryById(id, false, hasExtend);
	}
	
	public EdocSummary getSummaryByProcessId(String processId) throws BusinessException {
		return edocSummaryManager.getSummaryByProcessId(processId);
	}
	
	public EdocSummary getSummaryByIdHistory(Long id) {
		EdocSummary s = null;
    	try {
    		GovdocDao  govdocDaoFK = null;
        	if(AppContext.hasPlugin("fk")){
        		govdocDaoFK = (GovdocDao)AppContext.getBean("govdocDaoFK");
        	}
    		if(govdocDaoFK != null){
    			//s = govdocDaoFK.getColSummaryByIdHis(id);
    			//TODO
    		}
		} catch (Exception e) {
			return null;
		}
		return s;
    }
	
	public EdocSummary getSummaryByProcessIdHistory(Long processId) throws BusinessException {
    	/*ColSummary  s = null;
    	ColDao  colDaoFK = null;
    	if(AppContext.hasPlugin("fk")){
    		colDaoFK = (ColDao)AppContext.getBean("colDaoFK");
    	}
    	if(colDaoFK != null){
    		s = colDaoFK.getColSummaryByProcessId(processId);
    	}
    	return s;*/
		//TODO
		return null;
    }
	
	@Override
	public List<Object[]> getLargeFieldSummaryList(List<Long> summaryIdList) throws BusinessException {
		return edocSummaryDao.getLargeFieldSummaryList(summaryIdList);
	}
	
	@Override
	public List<Object[]> getInfoField2RecVo(Long summaryId) throws BusinessException {
		return edocSummaryDao.getInfoField2RecVo(summaryId);
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSummaryMapById(Long id) throws BusinessException {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", id);
		List<Object[]> result = DBAgent.find("select id, processId from EdocSummary where id = :summaryId", paramMap);
		if(com.seeyon.ctp.util.Strings.isNotEmpty(result)) {
			map.put("id", (Long)result.get(0)[0]);
			map.put("processId", (String)result.get(0)[1]);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getSummaryListById(List<Long> idList) throws BusinessException {
		List<Map<String, Object>> summaryList = new ArrayList<Map<String, Object>>();
		if(com.seeyon.ctp.util.Strings.isNotEmpty(idList)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("summaryIdList", idList);
			List<Object[]> result = DBAgent.find("select id, processId from EdocSummary where id in (:summaryIdList)",paramMap);
			if(com.seeyon.ctp.util.Strings.isNotEmpty(result)) {
				Map<String, Object> map = new HashMap<String, Object>();
				for(Object[] obj : result) {
					map.put("id", (Long)obj[0]);
					map.put("processId", (String)obj[1]);
					summaryList.add(map);
				}
			}
		}
		return summaryList;
	}
	
	@Override
	public void deleteSummaryByFormRecordId(Long formRecordid) {
		edocSummaryDao.deleteSummaryByFormRecordId(formRecordid);
	}
	@Override
	public EdocBody getFirstBody(Long summaryId) throws BusinessException {
		return edocBodyDao.getBodyByIdAndNum(String.valueOf(summaryId), 0);
	}
	
	@Override
	public List<EdocBody> getEdocBodys(Long summaryId) throws BusinessException {
		return edocBodyDao.getBodysById(summaryId);
	}
	
	@Override
	public EdocBody getBodyByIdAndNum(Long summaryId, int contentNum) throws BusinessException {
		return edocBodyDao.getBodyByIdAndNum(summaryId.toString(), contentNum);
	}
	
	@Override
	public void saveOrUpdateEdocSummary(EdocSummary po) throws BusinessException {
		edocSummaryManager.saveOrUpdateEdocSummary(po, true);
	}
	
	@Override
	public void updateEdocSummary(long id, Map<String, Object> columnValue) throws BusinessException {
		govdocDao.update(columnValue, new Object[][]{{"id", id}});
	}
	
	@Override
	public void updateEdocSummaryState(Long edocId, int state) throws BusinessException {
		edocSummaryManager.updateEdocSummaryState(edocId, state);
	}
	@Override
	public void deleteSummaryAndAffair(Long summaryId) {
		edocSummaryDao.deleteSummaryAndAffair(summaryId);
	}
	@Override
	public void update(Long id, Map<String, Object> columns) {
		edocSummaryManager.update(id, columns);
	}
	@Override
	public void deleteEdocSummary(EdocSummary po) throws BusinessException {
		edocSummaryDao.delete(po);
	}
	@Override
	public void deleteEdocSummary(Long summaryId) throws BusinessException {
		edocSummaryDao.delete(summaryId);
	}
	@Override
	public void updateEdocSummary(EdocSummary po) throws BusinessException {
		edocSummaryManager.updateEdocSummary(po, true);
	}
	@Override
	public void updateEdocSummary(EdocSummary po,boolean isSaveExtend) throws BusinessException {
		edocSummaryManager.updateEdocSummary(po, isSaveExtend);
	}
	@Override
	public void transSetFinishedFlag(EdocSummary summary) throws BusinessException {
		edocSummaryDao.transSetFinishedFlag(summary);
	}
	@Override
	public boolean unlockEdocFormLock(String objId, String userId) {
		return edocSummaryManager.deleteUpdateObj(objId, userId);
	}
	@Override
	public UserUpdateObject editObjectState(String objId) {
		return edocSummaryManager.editObjectState(objId);
	}
	@Override
	public EdocSummary getSummaryByCaseId(Long caseId){
		return edocSummaryDao.getSummaryByCaseId(caseId);
	}
	
	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}
	public void setGovdocDao(GovdocDao govdocDao) {
		this.govdocDao = govdocDao;
	}
	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}
	public void setEdocBodyDao(EdocBodyDao edocBodyDao) {
		this.edocBodyDao = edocBodyDao;
	}

	@Override
	public void saveEdocSummary(EdocSummary po, boolean isSaveExtend) throws BusinessException {
		edocSummaryManager.saveEdocSummary(po, isSaveExtend);
	}

	@Override
	public void updateSummaryArchiveId(Long edocId, Long archiveId) throws BusinessException {
		edocSummaryManager.updateEdocSummaryArchiveId(edocId, archiveId);
	}


}
