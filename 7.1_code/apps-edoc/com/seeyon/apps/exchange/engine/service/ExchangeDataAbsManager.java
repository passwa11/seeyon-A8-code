package com.seeyon.apps.exchange.engine.service;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.exceptions.BusinessException;

public abstract class ExchangeDataAbsManager implements ExchangeDataManager {
	
	protected static final Logger LOGGER = Logger.getLogger(ExchangeDataAbsManager.class);
	
	public final void doSendData(Object sendData) throws BusinessException{
		Object obj = this.send(sendData);
		callBack(obj);
	}
	
}
