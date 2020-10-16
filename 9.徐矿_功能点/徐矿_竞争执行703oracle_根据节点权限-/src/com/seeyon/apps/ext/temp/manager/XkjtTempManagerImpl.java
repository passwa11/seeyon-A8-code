package com.seeyon.apps.ext.temp.manager;

import com.seeyon.apps.ext.temp.dao.XkjtTempDao;
import com.seeyon.apps.ext.temp.dao.XkjtTempImpl;
import com.seeyon.apps.ext.temp.po.XkjtTemp;

import java.util.List;
import java.util.Map;

public class XkjtTempManagerImpl implements XkjtTempManager {

    private XkjtTempDao xkjtTempDao = new XkjtTempImpl();

    public XkjtTempDao getXkjtTempDao() {
        return xkjtTempDao;
    }

    @Override
    public void saveXkjtTemp(XkjtTemp tm) {
        getXkjtTempDao().saveXkjtTemp(tm);
    }

    @Override
    public List<XkjtTemp> findXkjtTemp(Map<String,Object> map) {
        return getXkjtTempDao().findXkjtTemp(map);
    }

    @Override
    public void deleteXkjtTemp(List<XkjtTemp> list) {
        getXkjtTempDao().deleteXkjtTemp(list);
    }
}
