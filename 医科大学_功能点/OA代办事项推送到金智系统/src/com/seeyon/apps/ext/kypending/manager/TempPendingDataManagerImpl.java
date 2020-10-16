package com.seeyon.apps.ext.kypending.manager;

import com.seeyon.apps.ext.kypending.dao.TempPendingDataDao;
import com.seeyon.apps.ext.kypending.dao.TempPendingDataDaoImpl;
import com.seeyon.apps.ext.kypending.po.TempPendingData;

import java.util.List;
import java.util.Map;

public class TempPendingDataManagerImpl implements TempPendingDataManager {

    private TempPendingDataDao dao = new TempPendingDataDaoImpl();

    @Override
    public void save(TempPendingData tm) {
        dao.save(tm);
    }

    @Override
    public List<TempPendingData> findTempPending(Map<String, Object> map) {
        return dao.findTempPending(map);
    }

    @Override
    public void deleteTempPending(List<TempPendingData> list) {
        dao.deleteTempPending(list);
    }

    @Override
    public void updateTemp(TempPendingData tm) {
        dao.updateTemp(tm);
    }
}
