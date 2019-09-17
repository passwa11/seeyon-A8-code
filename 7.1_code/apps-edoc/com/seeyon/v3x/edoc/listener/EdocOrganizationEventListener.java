package com.seeyon.v3x.edoc.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.organization.event.AddAccountEvent;
import com.seeyon.v3x.edoc.manager.EdocHelper;

public class EdocOrganizationEventListener {
	
	private static final Log log = LogFactory.getLog(EdocHelper.class);

	//@ListenEvent(event=AddAccountEvent.class)
	public void onAddAccount(AddAccountEvent evt)throws Exception{
		log.info("复制公文数据开始...");
		EdocHelper.generateZipperFleet(evt.getAccount().getId());
		log.info("复制公文数据结束...");
	}	
}
