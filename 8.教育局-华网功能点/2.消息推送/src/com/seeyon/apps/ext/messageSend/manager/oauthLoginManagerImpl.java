package com.seeyon.apps.ext.messageSend.manager;

import com.seeyon.apps.ext.messageSend.dao.oauthLoginDao;
import com.seeyon.apps.ext.messageSend.dao.oauthLoginDaoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class oauthLoginManagerImpl implements oauthLoginManager {
    private static final Log log = LogFactory.getLog(oauthLoginManagerImpl.class);

    private oauthLoginDao demoDao = new oauthLoginDaoImpl();

    @Override
    public String selectCodeByLoginName(String loginName) {
        return demoDao.selectCodeByLoginName(loginName);
    }
}