package com.seeyon.apps.exchange.bo;

import com.seeyon.apps.exchange.engine.service.ExchangeDataManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.event.Event;
import com.seeyon.ocip.exchange.model.BIZExchangeData;

public class ExchangeEvent extends Event {
	
	
	public ExchangeEvent(Object source) {
		super(source);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7018547929369932398L;
	private ExchangeDataManager exchangeDataManager;
	private BIZExchangeData bizExchangeData;
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ExchangeDataManager getExchangeDataManager() {
		return exchangeDataManager;
	}
	public void setExchangeDataManager(ExchangeDataManager exchangeDataManager) {
		this.exchangeDataManager = exchangeDataManager;
	}
	public BIZExchangeData getBizExchangeData() {
		return bizExchangeData;
	}
	public void setBizExchangeData(BIZExchangeData bizExchangeData) {
		this.bizExchangeData = bizExchangeData;
	}

}
