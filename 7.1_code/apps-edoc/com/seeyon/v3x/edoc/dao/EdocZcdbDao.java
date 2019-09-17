package com.seeyon.v3x.edoc.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocZcdb;

/**
 * 公文暂存待办信息表DAO
 * 
 */
public class EdocZcdbDao extends BaseHibernateDao<EdocZcdb> {

	private static final Log log = LogFactory.getLog(EdocZcdbDao.class);

	/**
	 * 根据id获得暂存待办信息对象
	 * 
	 * @param id
	 * @return EdocZcdb
	 */
	public EdocZcdb getEdocZcdbById(long id) {
		return super.get(id);
	}

	/**
	 * 根据affairId获得暂存待办信息对象
	 * 
	 * @param id
	 * @return EdocZcdb
	 */
	public EdocZcdb getEdocZcdbByAffairId(long affairId) {
		try {
			String queryString = "from EdocZcdb as edocZcdb where edocZcdb.affairId = ? ";
			Object[] values = { affairId };
			List<EdocZcdb> list = super.findVarargs(queryString, values);
			if (list.size() == 0) {
				return null;
			} else {
				return list.get(0);
			}
		} catch (RuntimeException re) {
			log.debug("find by flowPermId failed", re);
			throw re;
		}
	}
	
	/**
	 * 保存暂存待办信息对象
	 * 
	 * @param EdocZcdb
	 */
	public void saveEdocZcdb(EdocZcdb edocZcdb){
		log.debug("saving EdocZcdb instance");
		try{
			super.save(edocZcdb);
			log.debug("save sucesslly");
		}catch(RuntimeException re){
			log.error("save failed", re);
			throw re;
		}
	}
	
	
	/**
	 * 更新暂存待办信息
	 * 
	 * @param affairId
	 * @param acdbTime
	 */
	public void updateEdocZcdbByAffairId(long affairId,Date zcdbTime){
		String hsql="update EdocZcdb as edocZcdb set edocZcdb.zcdbTime = :zcdbTime where edocZcdb.affairId = :affairId ";
		Map map=new HashMap();
		map.put("zcdbTime", zcdbTime);
		map.put("affairId", affairId);
		super.bulkUpdate(hsql,map);
	}
	
	/**
	 * 根据id删除Edoc_zcdb
	 * @param id
	 */
	public void delete(long id){
		super.delete(id);
	}

}
