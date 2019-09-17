package com.seeyon.apps.wpstrans.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;

/**
 * Wps转版记录
 * @author 唐桂林
 *
 */
public class WpsTransRecordDao {

	/**
	 * 获取某公文成功转版记录
	 * @param objectId
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public WpsTransRecord getWpsTransRecordByObjectId(Long objectId) throws BusinessException {
		Map<String, Object> parametermap = new HashMap<String, Object>();
		parametermap.put("objectId", objectId);
		List list = DBAgent.find("from WpsTransRecord where objectId = :objectId", parametermap);
		if(Strings.isNotEmpty(list)) {
			return (WpsTransRecord)list.get(0);
		}
		return null;
	}
	
	/**
	 * 保存Wps转版记录
	 * @param po
	 * @throws BusinessException
	 */
	@SuppressWarnings("deprecation")
	public void saveOrUpdate(WpsTransRecord po) throws BusinessException {
		DBAgent.saveOrUpdate(po);
	}
	
	/**
	 * 逻辑删除Wps转版记录
	 * @param po
	 * @throws BusinessException
	 */
	public void deleteRecord(Long objectId) throws BusinessException {
		Map<String, Object> parametermap = new HashMap<String, Object>();
		parametermap.put("objectId", objectId);
		parametermap.put("status", WpsTransConstant.WPSTRANS_STATUS_DELETED);
		DBAgent.bulkUpdate("update WpsTransRecord set status=:status where objectId=:objectId ", parametermap);
	}
	
}
