package com.seeyon.apps.ext.zxzyk.dao;

import com.seeyon.apps.ext.zxzyk.po.OrgLevel;

import java.util.List;

public interface OrgLevelDao {
    List<OrgLevel> queryOrgLevel();

    List<OrgLevel> queryChangerLevel();

    List<OrgLevel> queryNotExistLevel();


    void insertOrgLevel(List<OrgLevel> list);

    void updateOrgLevel(List<OrgLevel> list);

    void deleteOrgLevel(List<OrgLevel> list);
}
