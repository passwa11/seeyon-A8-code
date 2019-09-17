package com.seeyon.v3x.exchange.manager;

import java.util.Date;
import java.util.List;

import com.seeyon.v3x.exchange.domain.ExchangeAccount;

public interface ExchangeAccountManager {
	
	/**
	 * 创建交换帐号（从交换中心导入帐号时调用）。
	 * @param accountId
	 * @param name
	 * @param accountType
	 * @param description
	 * @param isInternalAccount
	 * @param internalOrgId
	 * @param internalDeptId
	 * @param internalUserId
	 * @param exchangeServerId
	 * @param status
	 * @throws Exception
	 */
	public void create(String accountId,
			String name,
			int accountType,
			String description,
			boolean isInternalAccount,
			long internalOrgId,
			long internalDeptId,
			long internalUserId,
			String exchangeServerId,
			int status) throws Exception;
	
	/**
	 * 创建一个外部单位（该帐号不能交换，仅作发文时的录入收文单位用）。
	 * @param accountId
	 * @param name
	 * @param description
	 * @throws Exception
	 */
	public void create(String name, String description) throws Exception;
	
	public void update(ExchangeAccount exchangeAccount) throws Exception;
	
	public ExchangeAccount getExchangeAccount(long id);
	
	/** 返回单位内部帐号（在交换中心创建交换帐号时调用） */
	public List<ExchangeAccount> getExternalAccounts();
	
	/**
	 * 根据外部单位帐号id，返回外部单位。
	 * @param accountId String 外部单位帐号id
	 * @return ExchangeAccount
	 */
	public ExchangeAccount getExchangeAccountByAccountId(String accountId);
	
	/** 返回外部帐号（管理外部帐号和外部单位时调用） */
	public List<ExchangeAccount> getExternalAccounts(Long domainId);
	
	/** 返回单位内部帐号（在交换中心创建交换帐号时调用） */
	public List<ExchangeAccount> getInternalAccounts(Long domainId);
		
	/** 返回公文交换的外部单位（选择收文单位时调用） */
	public List<ExchangeAccount> getExternalOrgs(Long domainId);
	
	public void delete(long id) throws Exception;
	
	/**
	 * 判断是否包含名称为name的外部单位（新建外部单位时调用）。
	 * @param name 外部单位名称
	 * @param domainId 单位id
	 * @return true - 包含; false - 不包含
	 */
	public boolean containExternalAccount(String name, long domainId);
	
	/**
	 * 判断是否包含名称为name的外部单位（修改外部单位时调用）。
	 * @param id 外部单位id
	 * @param name 外部单位名称
	 * @param domainId 单位id
	 * @return true - 包含; false - 不包含
	 */
	public boolean containExternalAccount(long id, String name, long domainId);

	/**
	 * 判断交换单位数据是否有变化
	 * 
	 * @param orginalTimestamp
	 * @return
	 */
	public boolean isModifyExchangeAccounts(Date orginalTimestamp);
	
	/**
	 * 获取最新的事件戳
	 * @return
	 */
	public Date getLastModifyTimestamp();

	public void batchCreate(String externalAccounts, String description)
			throws Exception;
	
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId) ;
	
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId,String condition,String textfield) ;
}
