package com.seeyon.apps.ext.zs.dao;

import com.seeyon.apps.ext.zs.po.ZsTempFormCorrelation;

import java.util.List;
import java.util.Map;

public interface ZsTempFormCorrelationDao {

    void saveForm(ZsTempFormCorrelation zs);

    List<ZsTempFormCorrelation> getFormInfoBySummaryId(Map<String, Object> summaryId);

}
