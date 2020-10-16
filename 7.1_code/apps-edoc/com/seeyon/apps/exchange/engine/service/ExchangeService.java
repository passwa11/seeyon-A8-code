package com.seeyon.apps.exchange.engine.service;

import java.util.Map;

import com.seeyon.apps.exchange.bo.ExchangeEvent;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.edoc.RETEdocObject;

/**
 * @author chenx
 *
 */
public class ExchangeService {
	
	/**
	 * @param obj 标准对象
	 * @return
	 * @throws BusinessException
	 */
	public static Object sendObject(Object obj) throws BusinessException{
		BIZExchangeData data = (BIZExchangeData) obj;
		Map<String,ExchangeDataManager> managers = AppContext.getBeansOfType(ExchangeDataManager.class);
		ExchangeEvent event = new ExchangeEvent(ExchangeService.class);
		ExchangeDataManager exchangeDataManager = null;
		switch (data.getBussnissMessage().getContentType()) {
		case OFC:	
			exchangeDataManager = managers.get("recieveGovdocObjManager");
			break;
		case RET:	
			RETEdocObject retEdocObject = (RETEdocObject) data.getBussnissMessage().getContent();
			switch (retEdocObject.getOperation()) {
			case RECEIVED:
				exchangeDataManager = managers.get("recieveInfoGovdocObjManager");
				break;
			case ACCEPTED:
				exchangeDataManager = managers.get("signGovdocObjManager");
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}
		event.setExchangeDataManager(exchangeDataManager);
		event.setBizExchangeData(data);
		event.setUser(AppContext.getCurrentUser());
		EventDispatcher.fireEventAfterCommit(event);
		return null;
	}

}
