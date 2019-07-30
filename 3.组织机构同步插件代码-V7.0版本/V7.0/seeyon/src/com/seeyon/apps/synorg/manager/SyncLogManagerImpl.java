package com.seeyon.apps.synorg.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.synorg.dao.SyncLogDao;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.vo.SynLogListVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 同步插件日志管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午10:17:21
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncLogManagerImpl implements SyncLogManager {
    /** 日志对象 */
    private static final Log log = LogFactory.getLog(SyncLogManagerImpl.class);

    /**  */
    private SyncLogDao syncLogDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynLog> log) {
        syncLogDao.createAll(log);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlipInfo getSynLogList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
    	log.info("进入日志页面！");
        List<SynLogListVO> result = syncLogDao.queryByCondition(flipInfo, query);
        log.info("进入日志===="+result.size());
        if(flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSyncLogByIds(String ids) {
        syncLogDao.deleteSyncLogByIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        syncLogDao.deleteAll();
    }

    /**
     * 设置syncLogDao
     * @param syncLogDao syncLogDao
     */
    public void setSyncLogDao(SyncLogDao syncLogDao) {
        this.syncLogDao = syncLogDao;
    }
}
