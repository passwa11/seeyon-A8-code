package com.seeyon.apps.govdoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

/**
 * 新公文外部单位接口
 * @author 唐桂林
 *
 */
public interface GovdocExchangeAccountManager {
	
	/**
	 * ajax 查询列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;

	/**
	 * 
	 * @param domainId
	 * @return
	 */
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId) throws BusinessException;
	
	/**
	 * 
	 * @param domainId
	 * @param condition
	 * @param textfield
	 * @return
	 */
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId, String condition, String textfield) throws BusinessException;

	/**
	 * 
	 * @param id
	 * @return
	 */
	public ExchangeAccount getExchangeAccount(Long id) throws BusinessException;
	
	/**
	 * 创建一个外部单位（该帐号不能交换，仅作发文时的录入收文单位用）。
	 * @param accountId
	 * @param name
	 * @param description
	 * @throws Exception
	 */
	public void create(String name, String description) throws BusinessException;
	
	/**
	 * 
	 * @param exchangeAccount
	 * @throws Exception
	 */
	public void update(ExchangeAccount po) throws BusinessException;
	
	/**
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void delete(Long id) throws BusinessException;
	
	/**
	 * 判断是否包含名称为name的外部单位（新建外部单位时调用）。
	 * @param name 外部单位名称
	 * @param domainId 单位id
	 * @return true - 包含; false - 不包含
	 */
	public boolean containExternalAccount(String name, Long domainId) throws BusinessException;
	
	/**
	 * 判断是否包含名称为name的外部单位（修改外部单位时调用）。
	 * @param id 外部单位id
	 * @param name 外部单位名称
	 * @param domainId 单位id
	 * @return true - 包含; false - 不包含
	 */
	public boolean containExternalAccount(Long id, String name, Long domainId) throws BusinessException;
	
	
	
}
