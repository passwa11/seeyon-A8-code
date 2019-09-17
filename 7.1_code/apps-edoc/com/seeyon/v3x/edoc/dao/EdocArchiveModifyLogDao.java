package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocArchiveModifyLog;

/**
 * 公文归档修改历史表DAO
 * 
 */
public class EdocArchiveModifyLogDao extends BaseHibernateDao<EdocArchiveModifyLog> {

	private static final Log log = LogFactory.getLog(EdocArchiveModifyLogDao.class);

	/**
	 * 根据summaryId获得公文归档修改历史
	 * 
	 * @param summaryId
	 * @return List<EdocArchiveModifyLog>
	 */
	public List<EdocArchiveModifyLog> getListBySummaryId(long summaryId) {
		try {
			String queryString = "from EdocArchiveModifyLog as e where e.summaryId = :summaryId  order by e.updateTime desc";
			Map map=new HashMap();
			map.put("summaryId",summaryId);
			List<EdocArchiveModifyLog> list = super.find(queryString, map);
			return list;

		} catch (RuntimeException re) {
			log.debug("find by summaryId failed", re);
			throw re;
		}
	}
	
	/**
	 * 保存公文归档修改历史对象
	 * 
	 * @param EdocArchiveModifyLog
	 */
	public void saveEdocArchiveModifyLog(EdocArchiveModifyLog edocArchiveModifyLog){
		log.debug("saving EdocArchiveModifyLog instance");
		try{
			super.save(edocArchiveModifyLog);
			log.debug("save sucessfully");
		}catch(RuntimeException re){
			log.error("save failed", re);
			throw re;
		}
	}
	

}
