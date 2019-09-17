package com.seeyon.v3x.exchange.manager;

import java.util.List;

import com.seeyon.v3x.exchange.domain.ExchangeServer;

public interface ExchangeServerManager {
	
	public void create(String exchangeServerId, 			
			String name,
			String serverAddress,
			String serverPort,
			String loginAccount,
			String loginPassword) throws Exception;	
	
	public void update(ExchangeServer exchangeServer) throws Exception;
	
	public ExchangeServer getExchangeServer(long id);
	
	public List<ExchangeServer> getExchangeServers();
	
	public void delete(long id) throws Exception;
	

}
