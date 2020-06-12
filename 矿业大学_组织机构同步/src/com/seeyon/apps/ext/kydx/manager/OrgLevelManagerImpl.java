package com.seeyon.apps.ext.kydx.manager;

import com.seeyon.apps.ext.kydx.dao.OrgLevelDao;
import com.seeyon.apps.ext.kydx.dao.OrgLevelDaoImpl;
import com.seeyon.apps.ext.kydx.po.OrgLevel;

import java.util.List;

public class OrgLevelManagerImpl implements OrgLevelManager {

    private OrgLevelDao levelDao = new OrgLevelDaoImpl();

    @Override
    public void insertOrgLevel() {
        List<OrgLevel> list = levelDao.queryInsertOrgLevelList();
        levelDao.insertOrgLevel(list);

    }

    @Override
    public void updateOrgLevel() {
        List<OrgLevel> list = levelDao.queryUpdateOrgLevelList();
        levelDao.updateOrgLevel(list);
    }

    @Override
    public void deleteOrgLevel() {
        List<OrgLevel> list = levelDao.queryDeleteOrgLevel();
        levelDao.deleteOrglevel(list);
    }
}
