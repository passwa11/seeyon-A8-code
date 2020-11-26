package com.seeyon.apps.ext.kypending.dao;

import com.seeyon.apps.ext.kypending.po.TempPendingData;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.JDBCAgent;

import java.util.List;
import java.util.Map;

public class TempPendingDataDaoImpl implements TempPendingDataDao {
    @Override
    public void save(TempPendingData tm) {
        DBAgent.save(tm);
    }

    @Override
    public List<TempPendingData> findTempPending(Map<String, Object> map) {
        return DBAgent.find("from TempPendingData where summaryid=:summaryid", map);
    }

    @Override
    public void deleteTempPending(List<TempPendingData> list) {
        DBAgent.deleteAll(list);
    }

    @Override
    public void updateTemp(TempPendingData tm) {
        DBAgent.update(tm);
    }
}
