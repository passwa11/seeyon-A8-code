package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.po.SynLevel;
import com.seeyon.ctp.util.DBAgent;

/**
 * 职务管理实现类
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncLevelDaoImpl implements SyncLevelDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAll(List<SynLevel> levelList) {
        DBAgent.saveAll(levelList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAll(List<SynLevel> levelList) {
        DBAgent.updateAll(levelList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SynLevel> findAll() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("from SynLevel where syncState=" + SynOrgConstants.SYN_STATE_NONE);
        return DBAgent.find(buffer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        DBAgent.bulkUpdate("delete from SynLevel where syncState="+SynOrgConstants.SYN_STATE_SUCCESS);
    }
}
