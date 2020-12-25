package com.seeyon.apps.ext.pulldata.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.pulldata.dao.pulldataDao;
import com.seeyon.ctp.common.AppContext;


public class pulldataManagerImpl implements pulldataManager{
	private static final Log log = LogFactory.getLog(pulldataManagerImpl.class);

	private pulldataDao demoDao;



	public pulldataDao getpulldataDao() {
		if (demoDao == null) {
			demoDao = (pulldataDao) AppContext.getBean("pulldataDaoDemo");
		}
	return demoDao;
	}


}