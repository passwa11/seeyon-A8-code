package com.seeyon.apps.ext.KfqInform.manager;

import com.seeyon.apps.ext.KfqInform.po.KfqInform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.KfqInform.dao.KfqInformDao;
import com.seeyon.ctp.common.AppContext;

import java.util.List;
import java.util.Map;


public class KfqInformManagerImpl implements KfqInformManager {
    private static final Log log = LogFactory.getLog(KfqInformManagerImpl.class);

    private KfqInformDao demoDao;

    @Override
    public void saveInform(KfqInform inform) {
        getKfqInformDao().saveInform(inform);
    }

    @Override
    public List<KfqInform> findInformbyUserid(Map map) {
        return getKfqInformDao().findInformbyUserid(map);
    }

    @Override
    public void updateInform(List<KfqInform> inform) {
        getKfqInformDao().updateInform(inform);
    }

    @Override
    public List<KfqInform> findInformbySummaryid(Map map) {
        return getKfqInformDao().findInformbySummaryid(map);
    }

    public KfqInformDao getKfqInformDao() {
        if (demoDao == null) {
            demoDao = (KfqInformDao) AppContext.getBean("KfqInformDaoDemo");
        }
        return demoDao;
    }


}
