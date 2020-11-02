package com.seeyon.apps.ext.oauthLogin.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.oauthLogin.dao.oauthLoginDao;
import com.seeyon.ctp.common.AppContext;


public class oauthLoginManagerImpl implements oauthLoginManager{
	private static final Log log = LogFactory.getLog(oauthLoginManagerImpl.class);

	private oauthLoginDao demoDao;

	private String authip;

	@Override
	public void selectPerson() {
		System.out.println(authip);
		getoauthLoginDao().selectPerson();
	}

	public oauthLoginDao getoauthLoginDao() {
		if (demoDao == null) {
			demoDao = (oauthLoginDao) AppContext.getBean("oauthLoginDaoDemo");
		}
	return demoDao;
	}

	public void setAuthip(String authip) {
		this.authip = authip;
	}

}