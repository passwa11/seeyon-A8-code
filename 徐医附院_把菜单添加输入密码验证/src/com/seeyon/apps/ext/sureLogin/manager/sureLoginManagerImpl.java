package com.seeyon.apps.ext.sureLogin.manager;

import com.seeyon.apps.ext.sureLogin.dao.sureLoginDaoImpl;
import com.seeyon.apps.ext.sureLogin.po.SureLogin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.sureLogin.dao.sureLoginDao;
import com.seeyon.ctp.common.AppContext;


public class sureLoginManagerImpl implements sureLoginManager {
    private static final Log log = LogFactory.getLog(sureLoginManagerImpl.class);

    private sureLoginDao demoDao = new sureLoginDaoImpl();

    @Override
    public SureLogin selectSureLoginByid(Long userId) {
        return demoDao.selectByID(userId);
    }

    @Override
    public void insertSureLogin(SureLogin sureLogin) {
        demoDao.insertSureLogin(sureLogin);
    }
}
