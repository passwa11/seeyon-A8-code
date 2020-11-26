package com.seeyon.apps.ext.zxzyk.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.zxzyk.dao.xzykDao;
import com.seeyon.ctp.common.AppContext;


public class xzykManagerImpl implements xzykManager {
    private static final Log log = LogFactory.getLog(xzykManagerImpl.class);

    private xzykDao demoDao;

    public xzykDao getxzykDao() {
        if (demoDao == null) {
            demoDao = (xzykDao) AppContext.getBean("xzykDaoDemo");
        }
        return demoDao;
    }


}
