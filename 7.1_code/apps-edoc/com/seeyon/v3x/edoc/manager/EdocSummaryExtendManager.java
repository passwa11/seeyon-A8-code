package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryExtend;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;

/**
 * 公文扩展字段管理，目前只供EdocSummaryManager调用
 * @author tanggl
 *
 */
interface EdocSummaryExtendManager {

	/**
	 * 获取所有公文数据
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocSummary> getAllEdocSummaryList() throws BusinessException;
	
	/**
	 * 获取公文扩展数据数量
	 * @return
	 * @throws BusinessException
	 */
	public Integer getEdocSummaryExtendCount() throws BusinessException;
	
	/**
	 * 
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public EdocSummaryExtend getEdocSummaryExtend(Long summaryId) throws BusinessException;
	
	/**
	 * 保存公文扩展数据
	 * @param summaryExtendList
	 * @throws BusinessException
	 */
	public void saveEdocSummaryExtend(List<EdocSummaryExtend> summaryExtendList) throws BusinessException;
	
	/**
	 * 保存公文扩展数据
	 * @param summaryExtendList
	 * @throws BusinessException
	 */
	public void saveEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException;
	
	/**
	 * 保存公文扩展数据
	 * @param summary
	 * @throws BusinessException
	 */
	public void saveEdocSummaryExtendBySummary(EdocSummary summary) throws BusinessException;
	
	/**
	 * 删除公文扩展字段
	 * @throws BusinessException
	 */
	public void deleteAllEdocSummaryExtend() throws BusinessException;
	
	/**
	 * 删除公文扩展字段
	 * @throws BusinessException
	 */
	public void deleteEdocSummaryExtend(EdocSummaryExtend summaryExtend) throws BusinessException;
	
	/**
	 * 删除公文扩展字段
	 * @param summmaryId
	 * @throws BusinessException
	 */
	public void deleteEdocSummaryExtendBySummaryId(Long summaryId) throws BusinessException;
	
	/**
	 * 设置公文扩展字段
	 * @param summary
	 * @return
	 * @throws BusinessException
	 */
	public EdocSummary transSetSummaryExtendValue(EdocSummary summary) throws BusinessException;
	
	/**
	 * 将summary扩展字段赋值给extend
	 * @param summary
	 * @param summaryExtend
	 * @return
	 * @throws BusinessException
	 */
	public EdocSummaryExtend transSetSummaryToExtend(EdocSummary summary, EdocSummaryExtend summaryExtend) throws BusinessException;
	
	/**
	 * 将extend赋值给summary扩展字段
	 * @param summary
	 * @param summaryExtend
	 * @return
	 * @throws BusinessException
	 */
	public EdocSummary transSetExtendToSummary(EdocSummary summary, EdocSummaryExtend summaryExtend) throws BusinessException;
	/**
	 * 根据summaryIds集合查询EdocSummaryExtend
	 * @param summaryIds
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(List<Long> summaryIds) throws BusinessException;

	/**
	 * 
	 * @param curUserId
	 * @param em
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocSummaryExtend> getEdocSummaryExtendBySummaryIds(long curUserId, EdocSearchModel em) throws BusinessException;
	
}
