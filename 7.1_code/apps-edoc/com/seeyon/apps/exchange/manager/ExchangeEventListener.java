package com.seeyon.apps.exchange.manager;

import com.seeyon.apps.exchange.bo.ExchangeEvent;
import com.seeyon.apps.exchange.engine.service.ExchangeDataManager;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ocip.exchange.model.BIZExchangeData;

/**
 * 公文/协同等交换事件
 * @author tanggl
 *
 */
public class ExchangeEventListener extends AbstractSystemInitializer{
	
	private ExchangeDataManager exchangeDataManager;
	
	public void initialize() {
		EventDispatcher.register(ExchangeEvent.class, "exchangeEventListener", "execute", true);
	}
	
	/**
	 * 触发事件
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void execute(ExchangeEvent event) throws Exception {
		BIZExchangeData bizExchangeData = event.getBizExchangeData();
		exchangeDataManager = event.getExchangeDataManager();
		exchangeDataManager.send(bizExchangeData);
		exchangeDataManager.callBack(bizExchangeData);
	}
	
}
