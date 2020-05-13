package com.seeyon.apps.ext.allItems.manager;

import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.allItems.dao.allItemsDao;
import com.seeyon.ctp.common.AppContext;

import java.util.List;
import java.util.Map;


public class allItemsManagerImpl implements allItemsManager {
    private static final Log log = LogFactory.getLog(allItemsManagerImpl.class);

    private allItemsDao demoDao;

    @AjaxAccess
    @Override
    public FlipInfo findMoreCooprationXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        getallItemsDao().findMoreCooprationXkjtBanjie(flipInfo, map);
        return flipInfo;
    }

    @Override
    public List<Map<String, Object>> findCtpAffairIdbySummaryid(String id) {
        return getallItemsDao().findCtpAffairIdbySummaryid(id);
    }

    @Override
    public List<Map<String, Object>> findCoopratiionBanjie(String templetIds) {
        return getallItemsDao().findCoopratiionBanjie(templetIds);
    }

    @AjaxAccess
    @Override
    public FlipInfo findMoreXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        getallItemsDao().findMoreXkjtBanjie(flipInfo, map);
        return flipInfo;
    }
    @AjaxAccess
    @Override
    public FlipInfo findMoreXkjtNoBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        getallItemsDao().findMoreXkjtNoBanjie(flipInfo, map);
        return flipInfo;
    }

    @Override
    public List<Map<String,Object>> findXkjtAllNoBanJie(String templetIds) {
        return getallItemsDao().findXkjtAllNoBanJie(templetIds);
    }

    @Override
    public List<Object> findXkjtAllBanJie(String templetIds) {
        return getallItemsDao().findXkjtAllBanJie(templetIds);
    }

    public allItemsDao getallItemsDao() {
        if (demoDao == null) {
            demoDao = (allItemsDao) AppContext.getBean("allItemsDaoDemo");
        }
        return demoDao;
    }


}
