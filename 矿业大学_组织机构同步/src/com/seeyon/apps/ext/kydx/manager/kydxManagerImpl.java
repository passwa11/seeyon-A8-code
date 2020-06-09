package com.seeyon.apps.ext.kydx.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.kydx.dao.kydxDao;
import com.seeyon.ctp.common.AppContext;


public class kydxManagerImpl implements kydxManager {
    private static final Log log = LogFactory.getLog(kydxManagerImpl.class);

    private kydxDao demoDao;




    public kydxDao getkydxDao() {
        if (demoDao == null) {
            demoDao = (kydxDao) AppContext.getBean("kydxDaoDemo");
        }
        return demoDao;
    }

}
