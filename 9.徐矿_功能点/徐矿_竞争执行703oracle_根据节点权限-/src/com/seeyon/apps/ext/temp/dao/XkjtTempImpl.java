package com.seeyon.apps.ext.temp.dao;

import com.seeyon.apps.ext.temp.po.XkjtTemp;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;
import java.util.Map;

public class XkjtTempImpl implements XkjtTempDao {

    @Override
    public void saveXkjtTemp(XkjtTemp tm) {
        DBAgent.save(tm);
    }

    @Override
    public List<XkjtTemp> findXkjtTemp(Map<String, Object> map) {
        return DBAgent.find("from XkjtTemp where summaryId=:summaryId",map);
    }

    @Override
    public void deleteXkjtTemp(List<XkjtTemp> list) {
        DBAgent.deleteAll(list);
    }
}
