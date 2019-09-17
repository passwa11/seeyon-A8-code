package com.seeyon.apps.govdoc.manager.impl;

import com.seeyon.apps.exchange.engine.service.ExchangeDataAbsManager;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 交换实现(暂无用)
 *
 */
public class SignGovdocObjManager extends ExchangeDataAbsManager {
	
	@Override
	public String getType() {
		return "signObj";
	}

	@Override
	public Object send(Object sendData) throws BusinessException {
		return null;
	}

	@Override
	public void callBack(Object backData) throws BusinessException {
		
	}

}
