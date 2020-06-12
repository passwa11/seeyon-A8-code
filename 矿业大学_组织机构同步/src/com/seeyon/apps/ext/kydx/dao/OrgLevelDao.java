package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgLevel;

import java.util.List;

public interface OrgLevelDao {

    List<OrgLevel> queryInsertOrgLevelList();

    void insertOrgLevel(List<OrgLevel> list);

    List<OrgLevel> queryUpdateOrgLevelList();

    void updateOrgLevel(List<OrgLevel> list);

    List<OrgLevel> queryDeleteOrgLevel();

    void deleteOrglevel(List<OrgLevel> list);

}
