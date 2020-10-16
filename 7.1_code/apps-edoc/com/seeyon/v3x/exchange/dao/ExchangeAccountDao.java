package com.seeyon.v3x.exchange.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;
import com.seeyon.v3x.exchange.util.Constants;
import com.seeyon.ctp.util.Strings;

public class ExchangeAccountDao extends BaseHibernateDao<ExchangeAccount> {
	
	public List<ExchangeAccount> getExternalAccounts(Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=:isInternalAccount and a.domainId =:domainId order by a.createTime asc,a.name";
		//GOV-4469 公文应用设置-外部单位，不能翻页。
		Map map = new HashMap();
		map.put("isInternalAccount", false);
		map.put("domainId", domainId);
		//OA-51905客户bug：普通A8-V5BUG_V5.0sp1_国家计算机网络应急技术处理协调中心 _公文主送单位选择"外部单位"只能看到一部分_20140106022206
		return super.find(hsql, -1, -1, map);
	}
	
	public List<ExchangeAccount> getExternalAccount(String name, Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=? and a.name=? and a.domainId=?";
		Object[] values = {false, name, domainId};
		return super.findVarargs(hsql, values);
	}
	
	public List<ExchangeAccount> getExternalAccount(long id, String name, Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=? and a.id!=? and a.name=? and a.domainId=?";
		Object[] values = {false, id, name, domainId};
		return super.findVarargs(hsql, values);
	}
	
	public List<ExchangeAccount> getInternalAccounts(Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=? and a.domainId = ?  order by a.name";
		return super.findVarargs(hsql, true,domainId);
	}
	
	public List<ExchangeAccount> getExchangeOrgs(Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=? and a.domainId = ? and (a.accountType=? or a.accountType=?) and status=? order by a.name";
		Object[] values = {false,domainId, EdocRecieveRecord.Exchange_Receive_iAccountType_Default, EdocRecieveRecord.Exchange_Receive_iAccountType_Org, ExchangeAccount.C_iStatus_Active};
		return super.findVarargs(hsql, values);
	}
	public List<ExchangeAccount> getExternalAccounts() {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=? order by a.name";
		return super.findVarargs(hsql, false);
	}
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=:isInternalAccount and a.domainId =:domainId order by a.createTime asc,a.name";
		Map map = new HashMap();
		map.put("isInternalAccount", false);
		map.put("domainId", domainId);
		return super.find(hsql, map);
	}
	
	public List<ExchangeAccount> getExternalAccountsforPage(Long domainId,String condition,String textfield) {
		String hsql = "from ExchangeAccount as a where a.isInternalAccount=:isInternalAccount and a.domainId =:domainId ";
		Map map = new HashMap();
		if("unitName".equals(condition)&& Strings.isNotBlank(textfield)){
			hsql += " and a.name like :name ";
			map.put("name", "%"+textfield+"%");
		}
		hsql += "order by a.createTime asc,a.name";
		map.put("isInternalAccount", false);
		map.put("domainId", domainId);
		return super.find(hsql, map);
	}
}
