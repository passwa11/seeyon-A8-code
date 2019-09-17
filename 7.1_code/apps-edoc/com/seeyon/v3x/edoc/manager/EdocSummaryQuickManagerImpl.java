package com.seeyon.v3x.edoc.manager;

import org.apache.commons.lang.StringUtils;

import com.seeyon.v3x.edoc.dao.EdocSummaryQuickDao;
import com.seeyon.v3x.edoc.domain.EdocSummaryQuick;

/**
 * 类描述：
 * 创建日期：
 *
 * @author puyc
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocSummaryQuickManagerImpl implements EdocSummaryQuickManager {
	
	private EdocSummaryQuickDao edocSummaryQuickDao;
	

	public EdocSummaryQuickDao getEdocSummaryQuickDao() {
		return edocSummaryQuickDao;
	}

	public void setEdocSummaryQuickDao(EdocSummaryQuickDao edocSummaryQuickDao) {
		this.edocSummaryQuickDao = edocSummaryQuickDao;
	}

	@Override
	public void saveEdocSummaryQuick(EdocSummaryQuick edocSummaryQuick) {
		edocSummaryQuickDao.save(edocSummaryQuick);
		
	}

	@Override
	public EdocSummaryQuick findBySummaryId(Long summaryId) {
		return edocSummaryQuickDao.findBySummaryId(summaryId);
	}

	@Override
	public void deleteBySummaryId(Long summaryId) {
		edocSummaryQuickDao.deleteEdocSummaryQuickBySummaryId(summaryId);
	}

	@Override
	public EdocSummaryQuick findBySummaryIdStr(String summaryIdStr) {
		if(StringUtils.isNotBlank(summaryIdStr)){
			return findBySummaryId(Long.parseLong(summaryIdStr));
		}
		return null;
	}



}