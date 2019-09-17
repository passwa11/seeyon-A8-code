package com.seeyon.v3x.edoc.manager;

import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.util.ExchangeUtil;

public class RecRelationHandlerDefault implements RecRelationHandler{

	@Override
	public void transAfterSaveRec(EdocSummary summary, EdocRegister register,
			String recieveId, String comm) {
		
	}

	@Override
	public void transAfterSendRec(RecRelationAfterSendParam param) {
	}

	@Override
	public String getRecieveIdBeforeSendRec(EdocSummary summary,String recieveIdStr,
			String waitRegister_recieveId, boolean isNewSent) {
		return null;
	}

	protected void createRegisterDataByPaperEdoc(EdocSummary summary,EdocRegisterManager edocRegisterManager){
		ExchangeUtil.createRegisterDataByPaperEdoc(summary, edocRegisterManager);
	}
}
