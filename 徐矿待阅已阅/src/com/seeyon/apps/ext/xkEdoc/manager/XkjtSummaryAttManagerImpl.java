package com.seeyon.apps.ext.xkEdoc.manager;

import com.seeyon.apps.ext.xkEdoc.dao.XkjtSummaryAttDao;
import com.seeyon.apps.ext.xkEdoc.dao.XkjtSummaryAttDaoImpl;
import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.dao.impl.XkjtDaoImpl;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.exceptions.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/7
 */
public class XkjtSummaryAttManagerImpl implements XkjtSummaryAttManager {

    private XkjtSummaryAttDao xkjtSummaryAttDao = new XkjtSummaryAttDaoImpl();
    private XkjtDao xkjtDao = new XkjtDaoImpl();

    @Override
    public List<Map> queryHostFile(String summaryId) {
        return xkjtSummaryAttDao.queryHostFile(summaryId);
    }

    @Override
    public List<Map<String, Object>> queryEdocBody(String summaryId) {
        return xkjtSummaryAttDao.queryEdocBody(summaryId);
    }

    @Override
    public List<XkjtLeaderDaiYue> queryDaiyueByEdocIdAndLeaderId(long leaderId, long edocId) throws BusinessException {
        return xkjtDao.findXkjtLeaderDaiYueByMemberId(leaderId, edocId);
    }
}
