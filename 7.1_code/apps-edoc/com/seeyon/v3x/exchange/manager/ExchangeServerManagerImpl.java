package com.seeyon.v3x.exchange.manager;

import java.util.List;

import com.seeyon.v3x.exchange.dao.ExchangeServerDao;
import com.seeyon.v3x.exchange.domain.ExchangeServer;
import com.seeyon.v3x.exchange.util.Constants;

public class ExchangeServerManagerImpl implements ExchangeServerManager {
	
	private ExchangeServerDao exchangeServerDao;
	
	public ExchangeServerDao getExchangeServerDao() {
		return exchangeServerDao;
	}
	
	public void setExchangeServerDao(ExchangeServerDao exchangeServerDao) {
		this.exchangeServerDao = exchangeServerDao;
	}

	public void create(String exchangeServerId, 			
			String name,
			String serverAddress,
			String serverPort,
			String loginAccount,
			String loginPassword) throws Exception {
		ExchangeServer exchangeServer = new ExchangeServer();
		exchangeServer.setIdIfNew();
		exchangeServer.setExchangeServerId(exchangeServerId);
		exchangeServer.setName(name);
		exchangeServer.setServerAddress(serverAddress);
		exchangeServer.setServerPort(serverPort);
		exchangeServer.setLoginAccount(loginAccount);
		exchangeServer.setLoginPassword(loginPassword);
		exchangeServer.setStatus(Constants.C_iStatus_Disconnected);
		exchangeServer.setIsActive(true);
		exchangeServerDao.save(exchangeServer);
		
	}
	
	public void update(ExchangeServer exchangeServer) throws Exception {
		exchangeServerDao.update(exchangeServer);
	}
	
	public ExchangeServer getExchangeServer(long id) {
		return exchangeServerDao.get(id);
	}
	
	public List<ExchangeServer> getExchangeServers() {
		return exchangeServerDao.getExchangeServers();
	}
	
	public void delete(long id) throws Exception {	
		exchangeServerDao.delete(id);
	}
	
}
