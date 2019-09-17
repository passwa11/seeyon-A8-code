package com.seeyon.v3x.edoc.manager;

import java.util.List;
import com.seeyon.v3x.edoc.dao.EdocArchiveModifyLogDao;
import com.seeyon.v3x.edoc.domain.EdocArchiveModifyLog;


/**
 * 公文暂存待办信息表业务层实现
 * 
 */
public class EdocArchiveModifyLogManagerImpl implements EdocArchiveModifyLogManager{
	
	private EdocArchiveModifyLogDao  edocArchiveModifyLogDao;
	
	public void setEdocArchiveModifyLogDao(
			EdocArchiveModifyLogDao edocArchiveModifyLogDao) {
		this.edocArchiveModifyLogDao = edocArchiveModifyLogDao;
	}


	/**
	 * 根据summaryId获得公文归档修改历史
	 * 
	 * @param summaryId
	 * @return List<EdocArchiveModifyLog>
	 */
	public List<EdocArchiveModifyLog> getListBySummaryId(long summaryId){
		return edocArchiveModifyLogDao.getListBySummaryId(summaryId);
	}
	
	
	/**
	 * 保存公文归档修改历史对象
	 * 
	 * @param EdocArchiveModifyLog
	 */
	public void saveEdocArchiveModifyLog(EdocArchiveModifyLog edocArchiveModifyLog){
		edocArchiveModifyLogDao.saveEdocArchiveModifyLog(edocArchiveModifyLog);
	}
	
	/**
	 * 根据Id获得公文归档修改历史对象
	 * 
	 * @param id
	 * @return EdocArchiveModifyLog
	 */
	public EdocArchiveModifyLog getById(long id){
		return edocArchiveModifyLogDao.get(id);
	}

}
