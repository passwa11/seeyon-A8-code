package com.seeyon.apps.ext.Sso0715.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.Sso0715.dao.Sso0715Dao;
import com.seeyon.ctp.common.AppContext;


public class Sso0715ManagerImpl implements Sso0715Manager {
    private static final Log log = LogFactory.getLog(Sso0715ManagerImpl.class);

    private Sso0715Dao demoDao;

    private String authip;

    @Override
    public void selectPerson() {
        System.out.println(authip);
        getSso0715Dao().selectPerson();
    }

    public Sso0715Dao getSso0715Dao() {
        if (demoDao == null) {
            demoDao = (Sso0715Dao) AppContext.getBean("Sso0715DaoDemo");
        }
        return demoDao;
    }

    public void setAuthip(String authip) {
        this.authip = authip;
    }

}