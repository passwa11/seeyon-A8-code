package com.seeyon.v3x.exchange.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.exception.ExchangeException;

public interface EdocExchangeManager {
	
	/**
	 * 签收公文。
	 * @param id 公文记录id
	 * @param recUserId 签收人id
	 * @param registerUserId 登记人id
	 * @param recNo 签收编号
	 * @param remark 备考
	 * @param agentToId 被代理人ID
	 * @throws Exception
	 */
	public void recEdoc(long id, 
			long recUserId, 
			long registerUserId,
			String recNo,
			String remark,
			String keepPeriod,
			Long agentToId
			) throws Exception;
	
	/**
	 * 更改公文登记状态（待登记->已登记）,公文登记时调用此方法。
	 * @param id 公文记录id
	 */
	public void registerEdoc(long id) throws Exception;
	
	/**
	 * 发送公文，并将公文记录状态标记为已发。
	 * @param id 公文记录id
	 * @param sendUserId 发送用户id
	 * @throws Exception
	 */
	public void sendEdoc(long id, long sendUserId) throws Exception;
	
	/**
	 * 发送公文，并将公文记录状态标记为已发。
	 * @param edocSendRecord 公文待发送对象
	 * @param sendUserId 发送用户id
	 * @param sender 发文人姓名,直接保存到数据库中
	 * @throws Exception
	 */
	public void sendEdoc(EdocSendRecord edocSendRecord, long sendUserId, String sender, boolean reSend) throws Exception;
	/**
	 * 发送公文，并将公文记录状态标记为已发。
	 * @param edocSendRecord 公文待发送对象
	 * @param sendUserId 发送用户id
	 * @param sender 发文人姓名,直接保存到数据库中
	 * @param agentToId : 被代理人ID
	 * @param agentToId : exchangeModeValues  内部交换为0  书生交换为1
	 * @throws Exception
	 */
	public Map<String,String> sendEdoc(EdocSendRecord edocSendRecord, long sendUserId, String sender,Long agentToId, boolean reSend,String[] exchangeModeValues) throws Exception;
	
	/**
	 * 读取当前用户的待登记公文列表。
	 * @param userId 当前用户ID
	 * @return List
	 */
	
	public List<EdocRecieveRecord> getToRegisterEdocs(long userId);
	
	/**
	 * 读取当前用户的（待发送或已发送）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param status 状态（待发送或已发送）
	 * @param condition 查询条件
	 * @param value  查询值
	 * @return List
	 */
	public List<EdocSendRecord> getSendEdocs(long userId, long orgId, int status,String condition, String value)throws Exception;
	
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
	public List<EdocSendRecord> getSendEdocs(long userId, long orgId, int status,String condition, String value, int dateType)throws Exception;
	
	/**
	 * 读取当前用户的（待签收或已签收）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param status 状态（待签收，已签收）
	 * @return List
	 */
	public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value) throws Exception;
	
	
	/**
	 * 读取当前用户的（待签收或已签收）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param status 状态（待签收，已签收）
	 * @return List
	 */
	public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value, Map<String, Object> conditionParams) throws Exception;
	
	/**
	 * 读取当前用户的（待签收或已签收）公文列表。
	 * @param userId 当前用户id
	 * @param orgId 当前用户登录单位id
	 * @param statusSet 状态
	 * @param condition 查询条件
	 * @param value 查询值
	 * @param listType 签收列表
	 * @return List
	 */	
	public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value, int listType, int dateType, Map<String, Object> conditionParams) throws Exception;
	
	
	public EdocSendRecord getSendRecordById(long id);
	public EdocRecieveRecord getReceivedRecord(long id);
	public EdocRecieveRecord getReceivedRecordByEdocId(long edocId);
	
	public void deleteByType(String id,String type) throws Exception;
	/**
	 * 检查选择的单位部门是否有公文收发员
	 * @param objIds
	 * @param objNames
	 * @return
	 */
	public String checkExchangeRole(String typeAndIds);
	
	/**
	 * 检查选择的单位部门是否有公文收发员
	 * @param objIds
	 * @param objNames
	 * @param userId  --向凡 添加了一个参数，主要是Ajax异步请求验证的时候传入 选中的交换员的ID，需要验证此交换员是否具有交换权限，不需要时传入 null即可！
	 * @return
	 */
	public String checkExchangeRole(String typeAndIds, String userId);
	
	public List<EdocSendDetail> createSendRecord(Long sendRecordId,String typeAndIds) throws ExchangeException;
	
	/**
	 * 当前登录人员是否具有当前登录单位的收文登记权
	 */
	public boolean isEdocCreateRole();
	/**
	 * 判断是否具有指定单位下的收文登记权
	 * @param userId
	 * @param exchangeAccountId
	 * @return
	 */
	public boolean isEdocCreateRole(Long userId,Long exchangeAccountId);
	
	
	/**
	 * 判断是否具有指定单位下的收文分发权
	 * @param userId
	 * @param exchangeAccountId
	 * @return
	 */
	public boolean isEdocCreateRole(Long userId, Long exchangeAccountId, int edocType);
	

	
	public EdocSendRecord getEdocSendRecordByDetailId(long detailId);
	
	/**
	 * Ajax判断某个公文收发员是否有待交换和待签收的Affair事项。
	 * @param userId :用户ID
	 * @return
	 */
	public String checkEdocExchangeHasPendingAffair(Long userId);

	/**
	 * 更改公文登记人
	 * 
	 * @param edocRecieveRecordId
	 *            被更改的公文的签收记录id
	 * @param newRegisterUserId
	 *            新的登记人id
	 * @param newRegisterUserName
	 *            新的登记人name
	 * @param changeOperUserName
	 *            操作人name
	 * @param changeOperUserID
	 *            操作人ID
	 * @throws Exception
	 */
	public boolean changeRegisterEdocPerson(String edocRecieveRecordId,
			String newRegisterUserId, String newRegisterUserName,
			String changeOperUserName, String changeOperUserID)
			throws Exception;
	/**
	 * 判断公文是否可以被登记
	 * 
	 * @param edocRecieveRecordId
	 *            公文签收记录id
	 * @return true 可以被登记 false 公文已经被登记，不可以被登记
	 */
	public boolean isBeRegistered(String edocRecieveRecordId);

	
	/**
	 * 待登记退回到待签收
	 * @param id
	 * @param referenceAffairId
	 * @param stepBackInfo
	 * @throws Exception
	 */
	public void stepBackRecievedEdoc(long id, long referenceAffairId, String stepBackInfo) throws Exception;
	
	/**
	 * 登记退件箱/待登记退回到签收退件箱
	 * @param id
	 * @param referenceAffairId
	 * @param stepBackInfo
	 * @throws Exception
	 */
	public void stepBackRegisterEdoc(EdocRegister edocRegister, long id, long referenceAffairId, String stepBackInfo,String competitionAction) throws Exception;
	
	/**
	 * 取回交换出去的公文
	 * 
	 * @param stepBackEdocId
	 *            被取回的公文发文记录ID
	 * @param memberId
	 *            公文发文人ID
	 * @param currentUserName
	 *            操作用户名
	 * @param stepBackEdocSummary
	 *            被取回的公文对象
	 * @throws Exception
	 */
	public void takeBackEdoc(Long stepBackEdocId,
			Long currentUserId, String currentUserName,
			EdocSummary stepBackEdocSummary) throws Exception;
	
	/**xiangfan
	 * 判断某条公文交换是否被交换员撤销
	 * @return 已被撤销返回true，反之返回false
	 */
	public boolean exchangeEdocCancelById(Long exchangeRecieveEdocId);
	
	/**
	 * 获得当前单位的分发人，默认当前人有登记权限	--xiangfan G6 V1.0 SP1后续功能_签收时自动登记功能
	 * <li>当前登记人是分发人，就得到当前人ID</li>
	 * <li>当前人不是分发人，就默认返回有分发权限的第一个人</li>
	 * @param user 获得当前用户
	 * @return 分发用户的ID，没有分发人返回 null！
	 */
	public Long getDistributeUser(ConfigManager configManager, Long userId, Long accountId);
	
	/**
	 * 签收单位是否有分发人员	--xiangfan G6 V1.0 SP1后续功能_签收时自动登记功能
	 * @param AccountId 单位ID
	 * @return true:有， false:无
	 */
	public boolean hasDistributeUserByAccountId(Long AccountId);
	/**
	 * 检查如果不是自己单位建的签收编号，如果不是的话是没有修改权限的
	 * @param edocRecieveRecordId
	 * @return
	 */
	public String isEditEdocMark(String markId,String accountId);
	/**
	 * 查询本单位的签收编号
	 */
	public List<EdocMarkDefinition> getMarkList(Long accountId,Long depId) throws BusinessException;
	/**
	 * 检查签收编号是否重复
	 * @param edocRecieveRecordId
	 * @return
	 */
	public String isEditEdocMarkExist(String mark,String recieveId,String accountId,String depId);
	
	/********************************************公文交换列表       start*************************************************************/
	/**
	 * 读取当前用户的交换列表(待发送，已发送，待签收，已签收)- V5SP1
	 * @param type
	 * @param conditioin
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public List findEdocExchangeRecordList(int type, Map<String, Object> condition) throws BusinessException;
	
	/**
	 * 逻辑删除交换数据
	 * @param id
	 * @param type
	 * @throws BusinessException
	 */
	public void deleteExchangeDataByType(String type, Map<Long, String> map) throws BusinessException;
	
	/**
	 * 签收回退到发文分发
	 * @param stepBackEdocId 被回退的公文发文记录ID
	 * @param stepBackInfo 回退说明回退说明
	 * @return
	 * @throws Exception
	 */
	public boolean stepBackEdoc(Long stepBackEdocId, String stepBackInfo, boolean oneself) throws BusinessException;
	
	/**
	 * ajax请求：撤销交换记录 
	 * @param sendRecordId 公文交换纪录ID
	 * @param detailId 公文交换纪录详情ID
	 * @return
	 * @throws Exception
	 */
	public String withdraw(String sendRecordId, String detailId, String accountId, String sendCancelInfo) throws BusinessException;
	
	/**
	 * ajax请求：验证是否可撤销交换
	 * @param sendRecordId 
	 * @param detailId
	 * @return
	 * @throws Exception
	 */
	public boolean canWithdraw(String sendRecordId, String detailId) throws BusinessException;
	
	/**
	 * ajax请求：验证签收退回时，来文单位是否有公文收员
	 * @param exchangeSendEdocId
	 * @return
	 * @throws BusinessException
	 */
	public String[] checkEdocSendMember(String exchangeSendEdocId) throws BusinessException;
	/**
	 * ajax请求：验证登记退回时，来文单位是否有公文收员
	 * @param exchangeSendEdocId
	 * @return
	 * @throws BusinessException
	 */
	public String[] checkEdocRecieveMember(String recieveRecordId) throws BusinessException;
	/********************************************公文交换列表       end*************************************************************/
	
	public String cuiban(String detailId,String cuibanInfo)throws BusinessException;
	/*
	 *  判断当前人有没有登记权限
	 */
	public int isEdocRegister(long registerUserId) throws BusinessException;
	
	public int findEdocExchangeRecordCount(int type, Map<String, Object> condition) throws BusinessException;
	
	public String checkRegisterByRegisterEdocId(Long registerId) throws BusinessException;
	/**
	 * aJax判断当前收文是否已经被转收文
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public String checkTurnRec(String summaryId) throws BusinessException;
	
}
