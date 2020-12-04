package com.seeyon.apps.ext.qrCode.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.qrCode.dao.qrCodeDao;
import com.seeyon.ctp.common.AppContext;


public class qrCodeManagerImpl implements qrCodeManager{
	private static final Log log = LogFactory.getLog(qrCodeManagerImpl.class);

	private qrCodeDao demoDao;




	public qrCodeDao getqrCodeDao() {
		if (demoDao == null) {
			demoDao = (qrCodeDao) AppContext.getBean("qrCodeDaoDemo");
		}
	return demoDao;
	}



}