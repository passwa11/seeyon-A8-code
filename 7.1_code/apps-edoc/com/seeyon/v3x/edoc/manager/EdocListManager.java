package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryCountVO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

public interface EdocListManager {

	/**
	 * 查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocPendingList(int type, Map<String, Object> condition) throws BusinessException;
	

	/**
	 * 查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocDoneList(int type, Map<String, Object> condition) throws BusinessException;

	/**
	 * 查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocSentList(int type, Map<String, Object> condition) throws BusinessException;

	/**
	 * 查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocWaitSendList(int type, Map<String, Object> condition) throws BusinessException;
	
	/**
	 * 组合查询
	 * @param type
	 * @param condition
	 * @param edocSearchModel
	 * @return
	 */
     public List<EdocSummaryModel> combQueryByCondition(int type, Map<String, Object> condition, EdocSearchModel edocSearchModel) throws BusinessException;
     public int findEdocPendingCount(int type, Map<String, Object> condition) throws BusinessException ;
     
     /**
      * 根据公文类型获取各类型待办、已办等数据条数
      * @return
      * @throws BusinessException
      */
     public List<EdocSummaryCountVO> getCountGroupByEdocType() throws BusinessException;
}
