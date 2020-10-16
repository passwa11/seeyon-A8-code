package com.seeyon.apps.ext.Sso0715.manager;

import com.seeyon.apps.ext.Sso0715.dao.Sso0715Dao;
import com.seeyon.apps.ext.Sso0715.dao.Sso0715DaoImpl;
import com.seeyon.ctp.common.AppContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;


public class Sso0715ManagerImpl implements Sso0715Manager {
    private static final Log log = LogFactory.getLog(Sso0715ManagerImpl.class);

    private Sso0715Dao sso0715Dao=new Sso0715DaoImpl();

    @Override
    public List<Map<String, Object>> selectAccountOA() {
        return sso0715Dao.selectAccountOA();
    }

    @Override
    public List<Map<String, Object>> selectThirdAccount() {
        return sso0715Dao.selectThirdAccount();
    }

    public Sso0715Dao getSso0715Dao() {
        return sso0715Dao;
    }
}