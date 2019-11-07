package com.seeyon.apps.ext.xkEdoc.manager;

import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.exceptions.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/7
 */
public interface XkjtSummaryAttManager {

    public List<Map> queryHostFile(String summaryId);

    public List<Map<String, Object>> queryEdocBody(String summaryId);

    List<XkjtLeaderDaiYue> queryDaiyueByEdocIdAndLeaderId(long leaderId ,long edocId) throws BusinessException;
}
