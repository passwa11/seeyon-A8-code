package com.seeyon.apps.ext.oauthLogin.manager;

import com.seeyon.apps.ext.oauthLogin.dao.oauthLoginDaoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.oauthLogin.dao.oauthLoginDao;

public class oauthLoginManagerImpl implements oauthLoginManager {
    private static final Log log = LogFactory.getLog(oauthLoginManagerImpl.class);

    private oauthLoginDao demoDao = new oauthLoginDaoImpl();

    @Override
    public String selectLoginNameByCode(String code) {
        return demoDao.selectLoginNameByCode(code);
    }
}