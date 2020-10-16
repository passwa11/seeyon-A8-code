/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocParam;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public interface EdocMarkHistoryManager {
	
	/**
	 * 通过文号id获取历史占用文号记录
	 * @param markDefineId
	 * @return
	 */
	public List<EdocMarkHistory> findListByMarkDefineId(Long markDefineId);
	
	/**
     * 方法描述：保存公文文号历史
     */
    public void save(EdocMarkHistory edocMarkHistory);
    public void save(List<EdocMarkHistory> list);
    

    /**
     * 将公文文号保存到历史表，并删除此文号
     * @param edocMark  公文文号对象
     * @param userId  公文文号使用人id
     */
    public void seveEdocMarkHistory(EdocMark edocMark,Long userId);
    
    /**
     * @方法描述: 封发后将edocMark转移到edocMarkHistory
     *
     */
    public void afterSend(EdocSummary summary);
    
    /**
     * @方法描述: 根据公文id查找文号id
     * @param summaryId 公文Id
     */
    
    public Long findMarkIdBySummaryId(Long summaryId);
    
    public List<EdocMark> findMarkBySummaryId(Long summaryId);
    public List<EdocMark> findMarkBySummaryId(Long summaryId, Integer markType);
    
    /**
     * 保存公文文号历史
     * @param edocId
     * @param edocMark
     * @param markDefinitionId
     * @param markNum
     * @param createUserId
     * @param lastUserId
     */
    public void save(Long edocId,String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException;
    public void save(Long edocId,Integer currentNo,String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException;
    public void save(Long edocId,String edocMark,Long markDefinitionId,int markNum,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException;
    public void save(Long edocId,Integer currentNo,String edocMark,Long markDefinitionId,int markNum,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException;
    public void save(EdocSummary summary, Long edocId,Integer currentNo,String edocMark,Long markDefinitionId,int markNum,int govdocType,Long createUserId,Long lastUserId,boolean checkId,boolean autoIncrement) throws EdocMarkHistoryExistException;
    
    /**
     * 保存从断号中选择的文号，只用于签报
     * @param edocMarkId    断号id
     * @param edocId        公文id
     * @param markNum
     */
    public void saveMarkHistorySelectOld(Long edocMarkId,String edocMark,Long edocId,Long userId,boolean checkId) throws EdocMarkHistoryExistException;
    
    /**
     * 判断文号在历史表中是否使用
     * @param edocMark
     * @param edocId
     * @return true已使用；false未使用
     */
    public boolean isUsed(String edocMark,Long edocId);
    
    /**
     * 
     * @param edocMark
     * @param markDefId
     * @param edocId
     * @return
     */
    public boolean isUsedReserved(String edocMark, Long markDefId, Long edocId);
    
    /**
     * @方法描述: 根据公文id删除文号
     * @param summaryId 公文Id
     */
    public void deleteMarkIdBySummaryId(Long summaryId);
    
    public int increatementCurrentNo(EdocMarkDefinition markDef,int currentNo,EdocMarkCategory edocMarkCategory);
    
    /**
     * 将公文发行状态修改为已发行
     * @param transferStatus
     * @param summaryId
     */
	public void updateMarkHistoryTransferStatus(Integer transferStatus, Long summaryId) throws BusinessException;
	
	/**
	 * 保存公文占号、清除断号、跳转(2070721)
	 * @param edocParam
	 * @param model
	 * @param currentUser
	 * @throws EdocMarkHistoryExistException
	 */
	public void saveMarkHistoryNew(EdocParam edocParam, EdocMarkModel model, User currentUser) throws EdocMarkHistoryExistException;

	/**
     * 删除公文占号(2070721)
     * @param edocId
     */
    public void deleteMarkHistoryByEdocId(long edocId);
    public void deleteMarkHistoryByEdocId(long edocId, Integer markType);
    
    /**
     * 删除公文占号(2070721)
     */
    public void deleteMarkHistoryByMarkstr(List<String> markstrList);
    public void deleteMarkHistoryByMarkstr(List<String> markstrList, Integer markType);
    
    /**
     * 删除公文占号
     * @param markHistory
     */
    public List<EdocMarkModel> deleteMarkHistoryNew(EdocMarkHistory markHistory);
    
    public EdocMarkHistory getMarkHistoryByEdocID(Long summaryId);
    
    /**
     * 根据公文id查询相关文号历史记录（根据完成时间倒序排列）
     * @param summaryId
     * @return List<EdocMarkHistory>
     */
    public List<EdocMarkHistory> getMarkHistorysByEdocID(Long summaryId);
    public List<EdocMarkHistory> getMarkHistorysByEdocID(Long summaryId, Integer markType);
    
    /**
     * 删除对应的EdocMarkHistory 记录
     * @param edocMarkHistory
     */
    public void deleteMarkByModel(EdocMarkHistory edocMarkHistory);
    
}
