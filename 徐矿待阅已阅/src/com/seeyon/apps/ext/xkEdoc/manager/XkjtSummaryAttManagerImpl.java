package com.seeyon.apps.ext.xkEdoc.manager;

import com.seeyon.apps.ext.xkEdoc.dao.XkjtSummaryAttDao;
import com.seeyon.apps.ext.xkEdoc.dao.XkjtSummaryAttDaoImpl;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/7
 */
public class XkjtSummaryAttManagerImpl implements XkjtSummaryAttManager {

    private XkjtSummaryAttDao xkjtSummaryAttDao = new XkjtSummaryAttDaoImpl();

    @Override
    public List<Map> queryHostFile(String summaryId) {
        return xkjtSummaryAttDao.queryHostFile(summaryId);
    }
}
