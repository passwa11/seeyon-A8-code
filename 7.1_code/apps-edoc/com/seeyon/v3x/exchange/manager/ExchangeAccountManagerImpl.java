package com.seeyon.v3x.exchange.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.v3x.exchange.dao.ExchangeAccountDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

public class ExchangeAccountManagerImpl extends AbstractSystemInitializer implements ExchangeAccountManager {
	
	private CacheMap<String, Date> modifyTimestamp = null;
	private final static String MODIFYDATE = "modifyTimestamp";
	
	private ExchangeAccountDao exchangeAccountDao;
	
	public ExchangeAccountDao getExchangeAccountDao() {
		return exchangeAccountDao;
	}
	
	public void setExchangeAccountDao(ExchangeAccountDao exchangeAccountDao) {
		this.exchangeAccountDao = exchangeAccountDao;
	}
    
	public void initialize(){      
        
        CacheAccessable factory = CacheFactory.getInstance(ExchangeAccountManagerImpl.class); 
        
        modifyTimestamp = factory.createMap("modifyTimestamp");
        
        modifyTimestamp.put(MODIFYDATE, new Date());
        
	}
	public void create(String accountId,
			String name,
			int accountType,
			String description,
			boolean isInternalAccount,
			long internalOrgId,
			long internalDeptId,
			long internalUserId,
			String exchangeServerId,
			int status) throws Exception {
		ExchangeAccount exchangeAccount = new ExchangeAccount();
		User user = AppContext.getCurrentUser();
		exchangeAccount.setIdIfNew();
		exchangeAccount.setAccountId(accountId);
		exchangeAccount.setName(name);
		exchangeAccount.setAccountType(accountType);
		exchangeAccount.setDescription(description);
		exchangeAccount.setIsInternalAccount(isInternalAccount);
		exchangeAccount.setInternalOrgId(internalOrgId);
		exchangeAccount.setInternalDeptId(internalDeptId);
		exchangeAccount.setInternalUserId(internalUserId);
		exchangeAccount.setExchangeServerId(exchangeServerId);
		long l = System.currentTimeMillis();
		exchangeAccount.setCreateTime(new Timestamp(l));
		exchangeAccount.setLastUpdate(new Timestamp(l));
		exchangeAccount.setStatus(status);
		exchangeAccount.setDomainId(user.getLoginAccount());
		exchangeAccountDao.save(exchangeAccount);
		
		updateModifyTimestamp();
	}
	
	public void create(String name, String description) throws Exception {
		User user = AppContext.getCurrentUser();
		ExchangeAccount exchangeAccount = new ExchangeAccount();		
		exchangeAccount.setIdIfNew();
		long l = System.currentTimeMillis();
		exchangeAccount.setAccountId(String.valueOf(l)); //为手工设置的外部单位随机生成一个AccountId
		exchangeAccount.setName(name);
		exchangeAccount.setAccountType(EdocRecieveRecord.Exchange_Receive_iAccountType_Default);
		exchangeAccount.setDescription(description);
		exchangeAccount.setIsInternalAccount(false);		
		exchangeAccount.setCreateTime(new Timestamp(l));
		exchangeAccount.setLastUpdate(new Timestamp(l));
		exchangeAccount.setStatus(ExchangeAccount.C_iStatus_Active);
		exchangeAccount.setDomainId(user.getLoginAccount());
		exchangeAccountDao.save(exchangeAccount);
		
		updateModifyTimestamp();
	}
	
	public void update(ExchangeAccount exchangeAccount) throws Exception {		
		exchangeAccountDao.update(exchangeAccount);
		updateModifyTimestamp();
	}
	
	public ExchangeAccount getExchangeAccount(long id) {
		return exchangeAccountDao.get(id);
	}
	
	public ExchangeAccount getExchangeAccountByAccountId(String accountId) {
		return exchangeAccountDao.findUniqueBy("accountId", accountId);
	}
	
	public List<ExchangeAccount> getExternalAccounts(Long domainId) {
		return exchangeAccountDao.getExternalAccounts(domainId);
	}
	
	public List<ExchangeAccount> getInternalAccounts(Long domainId) {
		return exchangeAccountDao.getInternalAccounts(domainId);
	}
		
	public List<ExchangeAccount> getExternalOrgs(Long domainId) {
		return exchangeAccountDao.getExchangeOrgs(domainId);
	}
	
	public void delete(long id) throws Exception {
		exchangeAccountDao.delete(id);
		updateModifyTimestamp();
	}
	
	/** 返回单位内部帐号（在交换中心创建交换帐号时调用） */
	public List<ExchangeAccount> getExternalAccounts(){
		return exchangeAccountDao.getExternalAccounts();
	}
	
	public boolean containExternalAccount(String name, long domainId) {
		List<ExchangeAccount> accounts = exchangeAccountDao.getExternalAccount(name, domainId);
		if (accounts.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean containExternalAccount(long id, String name, long domainId) {
		List<ExchangeAccount> accounts = exchangeAccountDao.getExternalAccount(id, name, domainId);
		if (accounts.size() > 0) {
			return true;
		}
		return false;
	}	
	
	public void batchCreate(String externalAccounts, String description)
			throws Exception {
		User user = AppContext.getCurrentUser();
		List<ExchangeAccount> exchangeAccountList = new ArrayList<ExchangeAccount>();
		String accounts[] = externalAccounts.split("↗");
		for (int i = 0; i < accounts.length; i++) {
			String externalAccount = accounts[i];
			ExchangeAccount exchangeAccount = new ExchangeAccount();
			exchangeAccount.setIdIfNew();
			long l = System.currentTimeMillis();
			exchangeAccount.setAccountId(String.valueOf(l)); // 为手工设置的外部单位随机生成一个AccountId
			exchangeAccount.setName(externalAccount);
			exchangeAccount.setAccountType(EdocRecieveRecord.Exchange_Receive_iAccountType_Default);
			exchangeAccount.setDescription("");
			exchangeAccount.setIsInternalAccount(false);
			exchangeAccount.setCreateTime(new Timestamp(l));
			exchangeAccount.setLastUpdate(new Timestamp(l));
			exchangeAccount.setStatus(ExchangeAccount.C_iStatus_Active);
			exchangeAccount.setDomainId(user.getLoginAccount());

			exchangeAccountList.add(exchangeAccount);
		}
		exchangeAccountDao.savePatchAll(exchangeAccountList);

		updateModifyTimestamp();
	}

	private void updateModifyTimestamp(){
		modifyTimestamp.put(MODIFYDATE,new Date());
	}
	
	public boolean isModifyExchangeAccounts(Date orginalTimestamp){
		return !modifyTimestamp.get(MODIFYDATE).equals(orginalTimestamp);
	}
	
	public Date getLastModifyTimestamp(){
		return this.modifyTimestamp.get(MODIFYDATE);
	}
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId) {
		return exchangeAccountDao.getExternalAccountsforPage(domainId);
	}
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId,String condition,String textfield) {
		return exchangeAccountDao.getExternalAccountsforPage(domainId,condition,textfield);
	}
	
}
