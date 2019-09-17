package com.seeyon.v3x.exchange.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.exchange.domain.ExchangeServer;

public class ExchangeServerDao extends BaseHibernateDao<ExchangeServer> {
	
	public List<ExchangeServer> getExchangeServers() {
		String hsql = "from ExchangeServer as a order by a.name";
		return super.findVarargs(hsql);
	}

}
