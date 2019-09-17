package com.seeyon.apps.govdoc.manager.impl;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.govdoc.manager.GovdocExchangeAccountManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;
import com.seeyon.v3x.exchange.manager.ExchangeAccountManager;

/**
 * 新公文外部单位管理类
 * @author 唐桂林
 *
 */
public class GovdocExchangeAccountManagerImpl implements GovdocExchangeAccountManager {

	private ExchangeAccountManager exchangeAccountManager;
	
	@Override
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId) {
		return exchangeAccountManager.getExternalAccounts(domainId);
	}

	@Override
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId,String condition,String textfield) {
		return exchangeAccountManager.getExternalAccountsforPage(domainId, condition, textfield);
	}

	public void setExchangeAccountManager(ExchangeAccountManager exchangeAccountManager) {
		this.exchangeAccountManager = exchangeAccountManager;
	}

	@Override
	public ExchangeAccount getExchangeAccount(Long id) throws BusinessException {
		return exchangeAccountManager.getExchangeAccount(id);
	}

	@Override
	public void create(String name, String description) throws BusinessException {
		try {
			exchangeAccountManager.create(name, description);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public void update(ExchangeAccount po) throws BusinessException {
		try {
			exchangeAccountManager.update(po);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public void delete(Long id) throws BusinessException {
		try {
			exchangeAccountManager.delete(id);
		} catch(Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean containExternalAccount(String name, Long domainId) throws BusinessException {
		return exchangeAccountManager.containExternalAccount(name, domainId);
	}

	@Override
	public boolean containExternalAccount(Long id, String name, Long domainId) throws BusinessException {
		return exchangeAccountManager.containExternalAccount(id, name, domainId);
	}

	@Override
	@AjaxAccess
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		flipInfo.setPagination();
		List<ExchangeAccount> list = this.getExternalAccountsforPage(AppContext.getCurrentUser().getLoginAccount(),
				condition.get("condition"), condition.get("textfield"));
		flipInfo.setData(list);
		return flipInfo;
	}
	
}
