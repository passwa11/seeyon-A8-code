package com.seeyon.v3x.edoc.manager;

import java.util.Date;

import com.seeyon.v3x.edoc.domain.EdocZcdb;

/**
 * 公文暂存待办信息表业务层
 * 
 */
public interface EdocZcdbManager {

	
	/**
	 * 根据id获得暂存待办信息对象
	 * 
	 * @param id
	 * @return EdocZcdb
	 */
	public EdocZcdb getEdocZcdbById(long id);
	
	/**
	 * 根据affairId获得暂存待办信息对象
	 * 
	 * @param id
	 * @return EdocZcdb
	 */
	public EdocZcdb getEdocZcdbByAffairId(long affairId);
	
	
	/**
	 * 保存暂存待办信息对象
	 * 
	 * @param EdocZcdb
	 */
	public void saveEdocZcdb(EdocZcdb edocZcdb);
	
	
	/**
	 * 更新暂存待办信息
	 * 
	 * @param affairId
	 * @param acdbTime
	 */
	public void updateEdocZcdbByAffairId(long affairId,Date zcdbTime);
	
	/**
	 * 根据id删除Edoc_zcdb
	 * @param id
	 */
	public void deleteEdocZcdb(long id);
	
}
