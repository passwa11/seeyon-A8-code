package com.seeyon.v3x.edoc.manager;

import com.seeyon.v3x.edoc.domain.EdocSummaryQuick;

/**
 * 收文和发文相互关联表业务层
 */
public interface EdocSummaryQuickManager {
	
	public void saveEdocSummaryQuick(EdocSummaryQuick edocSummaryQuick);

	public EdocSummaryQuick findBySummaryId(Long summaryId);
	
	public EdocSummaryQuick findBySummaryIdStr(String summaryIdStr);
	
	public void deleteBySummaryId(Long summaryId);

}
