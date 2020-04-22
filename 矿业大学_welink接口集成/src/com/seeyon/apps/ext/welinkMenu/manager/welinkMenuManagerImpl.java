package com.seeyon.apps.ext.welinkMenu.manager;

import com.seeyon.apps.ext.welinkMenu.dao.welinkMenuDao;
import com.seeyon.apps.ext.welinkMenu.dao.welinkMenuDaoImpl;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkOaMapper;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;


public class welinkMenuManagerImpl implements welinkMenuManager {
    private static final Log log = LogFactory.getLog(welinkMenuManagerImpl.class);

    private welinkMenuDao welinkMenuDao = new welinkMenuDaoImpl();

    @Override
    public List<String> selectUserIDByOrgId(String orgId) {
        return welinkMenuDao.selectUserIDByOrgId(orgId);
    }

    @Override
    public List<WeLinkUsers> selectListByOauserIdOfUnion(List<String> list) {
        return welinkMenuDao.selectListByOauserIdOfUnion(list);
    }

    @Override
    public int insertWebLinkUsers(WeLinkUsers users) {
        return welinkMenuDao.insertWebLinkUsers(users);
    }

    @Override
    public WeLinkUsers selectByCurrentUserId(String userId) {
        return welinkMenuDao.selectByCurrentUserId(userId);
    }

    @Override
    public int updateWebLinkUsers(WeLinkUsers users) {
        return welinkMenuDao.updateWebLinkUsers(users);
    }

    @Override
    public int insertWlAndOaMapper(WeLinkOaMapper weLinkOaMapper) {
        return welinkMenuDao.insertWlAndOaMapper(weLinkOaMapper);
    }

    @Override
    public WeLinkOaMapper selectByOaMeetingId(String oaMeetingId) {
        return welinkMenuDao.selectByOaMeetingId(oaMeetingId);
    }
}