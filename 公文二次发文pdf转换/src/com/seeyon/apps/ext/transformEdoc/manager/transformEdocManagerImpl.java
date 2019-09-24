package com.seeyon.apps.ext.transformEdoc.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.transformEdoc.dao.transformEdocDao;
import com.seeyon.ctp.common.AppContext;


public class transformEdocManagerImpl implements transformEdocManager{
	private static final Log log = LogFactory.getLog(transformEdocManagerImpl.class);

	private transformEdocDao demoDao;

	private String authip;

	@Override
	public void selectPerson() {
		System.out.println(authip);
		gettransformEdocDao().selectPerson();
	}

	public transformEdocDao gettransformEdocDao() {
		if (demoDao == null) {
			demoDao = (transformEdocDao) AppContext.getBean("transformEdocDaoDemo");
		}
	return demoDao;
	}

	public void setAuthip(String authip) {
		this.authip = authip;
	}

}