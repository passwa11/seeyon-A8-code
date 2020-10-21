package com.seeyon.apps.ext.zs.manager;

import com.seeyon.apps.ext.zs.po.ZsTempFormCorrelation;

import java.util.List;
import java.util.Map;

public interface ZsTempFormCorrelationManager {

    void saveForm(ZsTempFormCorrelation zs);

    List<ZsTempFormCorrelation> getFormInfoBySummaryId(Map<String, Object> summaryId);


}
