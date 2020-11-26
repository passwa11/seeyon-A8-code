package com.seeyon.apps.ext.kypending.manager;

import com.seeyon.apps.ext.kypending.po.TempPendingData;

import java.util.List;
import java.util.Map;

public interface TempPendingDataManager {

    void save(TempPendingData tm);

    List<TempPendingData> findTempPending(Map<String, Object> map);

    void deleteTempPending(List<TempPendingData> list);

    void updateTemp(TempPendingData tm);
}
