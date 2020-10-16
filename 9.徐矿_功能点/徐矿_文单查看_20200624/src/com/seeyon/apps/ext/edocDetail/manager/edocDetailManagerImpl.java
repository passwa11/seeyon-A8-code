package com.seeyon.apps.ext.edocDetail.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.edocDetail.dao.edocDetailDao;
import com.seeyon.ctp.common.AppContext;


public class edocDetailManagerImpl implements edocDetailManager{
	private static final Log log = LogFactory.getLog(edocDetailManagerImpl.class);

	private edocDetailDao demoDao;

	private String authip;

	@Override
	public void selectPerson() {
		System.out.println(authip);
		getedocDetailDao().selectPerson();
	}

	public edocDetailDao getedocDetailDao() {
		if (demoDao == null) {
			demoDao = (edocDetailDao) AppContext.getBean("edocDetailDaoDemo");
		}
	return demoDao;
	}

	public void setAuthip(String authip) {
		this.authip = authip;
	}

}