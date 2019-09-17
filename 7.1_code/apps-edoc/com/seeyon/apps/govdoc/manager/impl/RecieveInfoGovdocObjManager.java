package com.seeyon.apps.govdoc.manager.impl;

import java.util.List;

import com.seeyon.apps.exchange.engine.service.ExchangeDataAbsManager;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.edoc.RETEdocObject;

/**
 * 返回交后后接收失败的单位的信息
 */
public class RecieveInfoGovdocObjManager extends ExchangeDataAbsManager {

	private GovdocMessageManager govdocMessageManager;
	
	@Override
	public String getType() {
		return "recieveInfo";
	}

	@Override
	public Object send(Object sendData) throws BusinessException {
		BIZExchangeData data = (BIZExchangeData) sendData;
		BIZMessage bizMessage = data.getBussnissMessage();
		RETEdocObject ofcEdocObject = (RETEdocObject) bizMessage.getContent();
		String messageInfo = ofcEdocObject.getOpinion();
		List<Organization> reciever = data.getRecivers();
		govdocMessageManager.sendMessage4ExchangeFail(Long.valueOf(reciever.get(0).getIdentification().getId()),data.getSubject(), messageInfo);
		return null;
	}

	@Override
	public void callBack(Object backData) throws BusinessException {
		
	}

	public void setGovdocMessageManager(GovdocMessageManager govdocMessageManager) {
		this.govdocMessageManager = govdocMessageManager;
	}
	
}
