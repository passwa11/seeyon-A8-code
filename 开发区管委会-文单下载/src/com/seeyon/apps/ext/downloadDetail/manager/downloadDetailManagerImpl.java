package com.seeyon.apps.ext.downloadDetail.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.downloadDetail.dao.downloadDetailDao;
import com.seeyon.ctp.common.AppContext;


public class downloadDetailManagerImpl implements downloadDetailManager{
	private static final Log log = LogFactory.getLog(downloadDetailManagerImpl.class);

	private downloadDetailDao demoDao;

	private String authip;

	@Override
	public void selectPerson() {
		System.out.println(authip);
		getdownloadDetailDao().selectPerson();
	}

	public downloadDetailDao getdownloadDetailDao() {
		if (demoDao == null) {
			demoDao = (downloadDetailDao) AppContext.getBean("downloadDetailDaoDemo");
		}
	return demoDao;
	}

	public void setAuthip(String authip) {
		this.authip = authip;
	}

}