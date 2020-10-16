package com.seeyon.v3x.exchange.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

public interface RecieveEdocManager {
	
	/**
	 * 内部接收公文调用接口。
	 * @param edocSendRecord 内部发文记录
	 * @param exchangeOrgId 交换单位或部门id
	 * @param exchangeType 交换类型
	 * @param replyId 回执id
	 * @param aRecUnit 收文单位[主送|抄送|抄报]
	 * @throws Exception
	 */
	public void create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,EdocSummary edocSummary) throws Exception;
	
	public void update(EdocRecieveRecord edocRecieveRecord) throws BusinessException;
	
	public EdocRecieveRecord getEdocRecieveRecord(long id);	
	public EdocRecieveRecord getEdocRecieveRecordByReciveEdocId(long id);	
	
	public EdocRecieveRecord getEdocRecieveRecordByEdocId(long edocId);	
	
	public List<EdocRecieveRecord> getEdocRecieveRecords(int status);
	
	/**
	 * 查询签收列表 (唐桂林-20110924)
	 * @param accountId 签收单位
	 * @param departIds 所有部门id
	 * @param statusSet 
	 * @param condition 查询条件
	 * @param value 查询值
	 * @param listType 查询列表类型
	 * @return 签收列表
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecords(String accountId, String departIds, Set<Integer> statusSet, String condition, String[] value, int listType, int dateType, Map<String, Object> conditionParams);	
	
	/**
	 * 登记签收的公文，更新签收日期，给签收人发消息
	 * @param id
	 * @return
	 */
	public Boolean registerRecieveEdoc(Long id) throws Exception;
	
	public List<EdocRecieveRecord> getWaitRegisterEdocRecieveRecords(Long userId);

	public List<EdocRecieveRecord> findWaitEdocRegisterList(int status, Map<String, Object> condition);
	public List<EdocRecieveRecord> findWaitRegisterEdocRecieveRecords(String registerIds, int state, int registerType, String condition, String[] value);
	
	public void delete(long id) throws Exception;

	//add by lindb
	
	public void create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,String sender,EdocSummary edocSummary) throws Exception;
	/**
	 * 内部接收公文调用接口。
	 * @param edocSendRecord 内部发文记录
	 * @param exchangeOrgId 交换单位或部门id
	 * @param exchangeType 交换类型
	 * @param replyId 回执id
	 * @param aRecUnit 收文单位[主送|抄送|抄报]
	 * @param sender
	 * @param agentToId :被代理人ID
	 * @throws Exception
	 */
	public String create(EdocSendRecord edocSendRecord,
			long exchangeOrgId,
			int exchangeType,
			Object replyId,
			String[] aRecUnit,
			String sender,
			Long agentToId,
			EdocSummary edocSummary) throws Exception;
	
	/**
	 * 根据replayId（签收回执ID来删除待签收记录）
	 * @param replayId 回执ID
	 * @return
	 * @throws Exception
	 */
	public void deleteRecRecordByReplayId(long replayId)throws Exception;
	
	/**
	 * 根据回执ID查找公文待签收记录
	 * @param replyId
	 * @return
	 * @throws Exception
	 */
	public EdocRecieveRecord getReceiveRecordByReplyId(long replyId)throws Exception;
	
	public void delete(EdocRecieveRecord o)throws Exception;
	
	//changyi moveTo5.0 TODO A8 APP加的方法
	public Boolean registerRecieveEdoc(Long id, Long reciveEdocId) throws Exception;
	
	public void update(Map<String, Object> columns, Object[][] where) throws BusinessException;
	
	/********************************************公文签收列表 start*************************************************************/
	/**
	 * 
	 * @param type 3：待签收列表 4已签收列表
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecordList(int type, Map<String, Object> condition) throws BusinessException;
	
	/**
	 * 
	 * @param ids
	 * @throws BusinessException
	 */
	public void deleteEdocRecieveRecordByLogic(String type, Map<Long, String> map) throws BusinessException;
	/********************************************公文签收列表       end*************************************************************/

	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param edocId
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocRecieveRecord> getEdocRecieveRecordByEdocIdAndOrgIdAndStatus(long edocId, long exchangeOrgId,int status);
	
	/**
	 * 根据以下参数获得公文交换记录的列表
	 * 兼容回退和撤销的
	 * @param exchangeOrgId
	 * @param status
	 * @return edocId的list
	 */
	public List<EdocRecieveRecord> getEdocRecieveRecordByOrgIdAndStatus( long exchangeOrgId, int status);
	
	/**
	 * 获得某单位下某个状态的签收数据
	 * 用于 登记开关切换时，需要判断该单位中是否还有待登记的数据
	 * @param status
	 * @param accountId
	 * @return
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecordByStatusAndAccountId(int status,long accountId);
	
	public int findEdocRecieveRecordCountByStatusAndAccountId(int status,long accountId);
	
	public Boolean registerRecieveEdoc(Long id,int state) throws Exception;
	public int findEdocRecieveRecordCount(int type, Map<String, Object> condition) throws BusinessException;
	/**
	 * 老公文交换待签收的数据的在待办列表显示正文类型图标 
	 * @return
	 * @throws BusinessException
	 */
	public String getEdocExchangeContentType(Long recieveId);
}
