package com.seeyon.v3x.exchange.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.exception.ExchangeException;

public interface SendEdocManager {

	/**
	 * 创建公文发送记录（公文封发时调用）。
	 * 
	 * @param edocSummary
	 *            公文记录id
	 * @param exchangeOrgId
	 *            交换单位id或交换部门id
	 * @param exchangeType
	 *            交换类型
	 * @param edocMangerID
	 *            封发时选择的单位公文管理员
	 * @throws Exception
	 */
	public void create(EdocSummary edocSummary, long exchangeOrgId, 
			int exchangeType, String edocMangerID,CtpAffair affair,boolean isTurnRec) throws Exception;
	
	/**
	 * 撤销交换时，创建新的发文记录
	 * @param edocSendRecord
	 * @throws BusinessException
	 */
	public void create(EdocSendRecord edocSendRecord) throws BusinessException;
	
	/**
	 * 撤销交换时，创建新的发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public void createSendRecordDetail(EdocSendDetail detail) throws BusinessException;
	
	/**
	 * 更新发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public void updateSendRecordDetail(EdocSendDetail detail) throws BusinessException;
	
	/**
	 * 获取发文记录详细信息
	 * @param detail
	 * @throws BusinessException
	 */
	public EdocSendDetail getSendRecordDetail(long detailId) throws BusinessException;
	
	/**
	 * 再次发送公文
	 * @param edocSendRecord
	 * @param edocSummary
	 * @param exchangeOrgId
	 * @param exchangeType
	 * @throws Exception
	 */
	public void reSend(EdocSendRecord edocSendRecord, EdocSummary edocSummary) throws Exception;
	
	public void update(EdocSendRecord edocSendRecord) throws BusinessException;	
	
	public EdocSendRecord getEdocSendRecord(long id);
	
	public EdocSendRecord getEdocSendRecordByEdocId(long edocId);
	
	/**
	 * 根据edocId查询交换表的公文（表明此公文流程中有交换节点）
	 * @param edocId
	 * @throws Exception
	 */
	public List<EdocSendRecord> getEdocSendRecordOnlyByEdocId(long edocId);
	
	public EdocSendRecord getEdocSendRecordById(long id);
	
	public List<EdocSendRecord> getEdocSendRecords(int status);
	
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value);
	
	/**
	 * 读取当前用户的（待发送或已发送）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param status 状态（待发送或已发送）
	 * @param condition 查询条件
	 * @param value  查询值
	 * @param dateType 时间类型（本年，本月，上个月等）
	 * @return List
	 */
	public List<EdocSendRecord> findEdocSendRecords(String accountIds,String departIds,int status,String condition,String value,int dateType,long userId);
	
	/**
	 * 删除公文交换数据: 逻辑删除，将删除状态置为5
	 * @param id
	 * @throws Exception
	 */
	public void delete(long id) throws Exception;	
	public void deleteByPhysical(long id) throws Exception ;
	/**
	 * 删除公文交换数据: 逻辑删除，将删除状态置为deleteState(deleteState的值域是5,6)
	 * @param id
	 * @param deleteState
	 * @throws Exception
	 */
	public void delete(long id, int deleteState) throws Exception;	
	
	/**
	 * 生成待发送公文要发送的详细信息
	 * @param sendRecordId
	 * @param typeAndIds
	 */
	public List<EdocSendDetail>  createSendRecord(Long sendRecordId,String typeAndIds) throws ExchangeException;
	
	public EdocSendRecord getEdocSendRecordByDetailId(long detailId);  

	/**
	 * 删除公文交换的回执记录
	 * @param id
	 * @throws Exception
	 */
	public void deleteRecordDetailById(long id)throws Exception;	
	public List<EdocSendDetail> getDetailBySendId(long sendId);	
	public List<EdocSendDetail> getDetailBySendRecOrgId(String recOrgId);
	public String ajaxCheckContainSpecialUnit(Long sendRecordId,String unitIds);
	
	public void update(Map<String, Object> columns, Object[][] where) throws BusinessException;
	
	/********************************************公文交换列表 start*************************************************************/
	/**
	 * 公文发送列表
	 * @param type 3：待发送列表 4已发送列表
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocSendRecord> findEdocSendRecordList(int state, Map<String, Object> condition) throws BusinessException;
	
	/**
	 * 公文交换列表逻辑批量删除
	 * @param map
	 * @param type
	 * @throws BusinessException
	 */
	public void deleteEdocSendRecordByLogic(String type, Map<Long, String> map) throws BusinessException;
	/********************************************公文交换列表       end*************************************************************/
	
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param edocId
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocSendRecord> getEdocSendRecordByEdocIdAndOrgIdAndStatus(long edocId, long exchangeOrgId,int status);
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param exchangeOrgId
	 * @param status 撤销和回退的一起查出
	 * @return
	 */
	public List<EdocSendRecord> getEdocSendRecordByOrgIdAndStatus(long exchangeOrgId,int status);
	
	public int findEdocSendRecordCount(int type, Map<String, Object> condition) throws BusinessException;
	
	public EdocSendRecord getFirstSendRecord(Long edocId);
	public void deleteOldEdocSendDetail(Long sendEdocId , String  recOrgId);
}
