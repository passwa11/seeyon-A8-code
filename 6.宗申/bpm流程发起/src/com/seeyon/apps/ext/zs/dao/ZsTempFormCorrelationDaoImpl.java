package com.seeyon.apps.ext.zs.dao;

import com.seeyon.apps.ext.zs.po.ZsTempFormCorrelation;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;
import java.util.Map;

public class ZsTempFormCorrelationDaoImpl implements ZsTempFormCorrelationDao {
    @Override
    public ZsTempFormCorrelation saveForm(ZsTempFormCorrelation zs) {
        return (ZsTempFormCorrelation) DBAgent.save(zs);
    }

    @Override
    public List<ZsTempFormCorrelation> getFormInfoBySummaryId(Map<String, Object> param) {
        return DBAgent.find("from ZsTempFormCorrelation where oaSummaryId:= oaSummaryId", param);
    }
}
