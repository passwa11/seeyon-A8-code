package com.seeyon.v3x.edoc.manager;

import java.util.Calendar;
import java.util.List;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.v3x.edoc.dao.EdocInnerMarkDefinitionDao;
import com.seeyon.v3x.edoc.domain.EdocInnerMarkDefinition;
import com.seeyon.v3x.edoc.util.Constants;

public class EdocInnerMarkDefinitionManagerImpl implements EdocInnerMarkDefinitionManager {
	private final static CacheAccessable cacheFactory = CacheFactory.getInstance(EdocInnerMarkDefinitionManager.class);
	private static CacheMap<Long,Integer> innerMarkStatusTable =cacheFactory.createMap("InnerMarkStatusTable");

	private EdocInnerMarkDefinitionDao edocInnerMarkDefinitionDao;
	
	public Boolean isUnificationType(Long id){
		EdocInnerMarkDefinition mark = (EdocInnerMarkDefinition)edocInnerMarkDefinitionDao.get(id);
		Integer type = mark.getType();
		if(Constants.EDOC_INNERMARK_UNIFICATION == type){
			return true;
		}else{
			return false;
		}
	}
	
	public void saveOrUpdate(EdocInnerMarkDefinition mark){
		edocInnerMarkDefinitionDao.saveOrUpdate(mark);
	}	
	
	public void create(EdocInnerMarkDefinition mark){
		edocInnerMarkDefinitionDao.save(mark);
	}

	public EdocInnerMarkDefinitionDao getEdocInnerMarkDefinitionDao() {
		return edocInnerMarkDefinitionDao;
	}

	public void setEdocInnerMarkDefinitionDao(
			EdocInnerMarkDefinitionDao edocInnerMarkDefinitionDao) {
		this.edocInnerMarkDefinitionDao = edocInnerMarkDefinitionDao;
	}

	public void deleteAll(Long domainId){		
		edocInnerMarkDefinitionDao.deleteAll(domainId);	
	}
			
	public synchronized String getInnerMark(Integer edocType, Long domainId, boolean incremental) {
		// 得到内部文号设置状态
		Integer status = getInnerMarkStatus(domainId);
		if (status == Constants.STATUS_INNERMARK_INITIAL) {
			return "";			
		}
		else {
			EdocInnerMarkDefinition markDef = null;
			String innerEdocMark = "";
			if (status == Constants.STATUS_INNERMARK_PUBLIC) {
				markDef = edocInnerMarkDefinitionDao.getUnificationEdocInnerMarkDefinition(domainId);
			}
			else {
				if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {
					markDef = edocInnerMarkDefinitionDao.getEdocInnerMarkDefinitionByType(Constants.EDOC_INNERMARK_SEND, domainId);
				}
				else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
					markDef = edocInnerMarkDefinitionDao.getEdocInnerMarkDefinitionByType(Constants.EDOC_INNERMARK_RECEIVED, domainId);
				}
				else if (edocType == EdocEnum.edocType.signReport.ordinal()) {
					markDef = edocInnerMarkDefinitionDao.getEdocInnerMarkDefinitionByType(Constants.EDOC_INNERMARK_SIGN_REPORT, domainId);
				}
			}
			
			if (markDef != null && markDef.getWordNo()!=null) {
				Integer currentNo = markDef.getCurrentNo();
				Integer maxNo = markDef.getMaxNo();
				if (currentNo < maxNo) {
					Calendar calendar = Calendar.getInstance();
					String yearNo = String.valueOf(calendar.get(Calendar.YEAR));
					String expression = markDef.getExpression();
					expression = expression.replaceFirst("\\$WORD", markDef.getWordNo());
					if (markDef.getYearEnabled()) {
						expression = expression.replaceFirst("\\$YEAR", yearNo);
					}	
					int length = markDef.getLength();
					int curNoLen = String.valueOf(currentNo).length();
					int maxNoLen = String.valueOf(maxNo).length();
					StringBuilder flowNo = new StringBuilder(currentNo);
					if (length > 0 && length == maxNoLen) {
						flowNo = new StringBuilder();
						for (int j = curNoLen; j < length; j++) {
							flowNo.append("0");
						}
						flowNo.append(currentNo);
					}
					expression = expression.replaceFirst("\\$NO", flowNo.toString());
					innerEdocMark = expression;
					if (incremental) {
						markDef.setCurrentNo(markDef.getCurrentNo() + 1);
						edocInnerMarkDefinitionDao.save(markDef);
					}	
				}
			}
			return innerEdocMark;
		}
	}
	
	public String getInnerMark(Integer edocType, Long domainId) {
		return getInnerMark(edocType, domainId, false);
	}
	
	public List<EdocInnerMarkDefinition> getEdocInnerMarkDefs(int type, long domainId) {
		return edocInnerMarkDefinitionDao.getEdocInnerMarkDefs(type, domainId);
	}
	
	public List<EdocInnerMarkDefinition> getEdocInnerMarkDefsList(long domainId) {
		return edocInnerMarkDefinitionDao.getAllList(domainId);
	}
	
	public int getInnerMarkStatus(long domainId) {		
		int status = Constants.STATUS_INNERMARK_UNKNOWN;
		if (innerMarkStatusTable.get(domainId) != null) {
			status = innerMarkStatusTable.get(domainId);
		}
		if (status == Constants.STATUS_INNERMARK_UNKNOWN) {
			List<EdocInnerMarkDefinition> innerMarkDefs = this.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_UNIFICATION, domainId);
			if (innerMarkDefs != null && innerMarkDefs.size() > 0) {
				status = Constants.STATUS_INNERMARK_PUBLIC;				
			}
			else {
				innerMarkDefs = this.getEdocInnerMarkDefs(Constants.EDOC_INNERMARK_SEND, domainId);
				if (innerMarkDefs != null && innerMarkDefs.size() > 0) {					
					status = Constants.STATUS_INNERMARK_PRIVATE;
				}
				else {
					status = Constants.STATUS_INNERMARK_INITIAL;
				}
			}
			innerMarkStatusTable.put(domainId, status);
		}		
		return status;
	}
	
	public void setInnerMarkStatus(long domainId, int status) {
		innerMarkStatusTable.put(domainId, status);
	}
	
}
