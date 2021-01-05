package com.seeyon.apps.ext.gcxySso.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.gcxySso.dao.gcxySsoDao;
import com.seeyon.ctp.common.AppContext;


public class gcxySsoManagerImpl implements gcxySsoManager {
    private static final Log log = LogFactory.getLog(gcxySsoManagerImpl.class);

    private gcxySsoDao demoDao;




    public gcxySsoDao getgcxySsoDao() {
        if (demoDao == null) {
            demoDao = (gcxySsoDao) AppContext.getBean("gcxySsoDaoDemo");
        }
        return demoDao;
    }


}