package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocArchiveModifyLog;

/**
 * 公文归档修改历史表业务层
 * 
 */
public interface EdocArchiveModifyLogManager {
	
	/**
	 * 根据summaryId获得公文归档修改历史
	 * 
	 * @param summaryId
	 * @return List<EdocArchiveModifyLog>
	 */
	public List<EdocArchiveModifyLog> getListBySummaryId(long summaryId);
	
	
	/**
	 * 保存公文归档修改历史对象
	 * 
	 * @param EdocArchiveModifyLog
	 */
	public void saveEdocArchiveModifyLog(EdocArchiveModifyLog edocArchiveModifyLog);
	
	/**
	 * 根据Id获得公文归档修改历史对象
	 * 
	 * @param id
	 * @return EdocArchiveModifyLog
	 */
	public EdocArchiveModifyLog getById(long id);
	
	
}
