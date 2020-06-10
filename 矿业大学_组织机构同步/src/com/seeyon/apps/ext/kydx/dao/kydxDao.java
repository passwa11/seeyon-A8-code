package com.seeyon.apps.ext.kydx.dao;


import com.seeyon.apps.ext.kydx.po.OrgLevel;

import java.util.List;

public interface kydxDao {

    List<OrgLevel> queryOrgLevel();

    List<OrgLevel> queryChangerLevel();

    List<OrgLevel> queryNotExistLevel();


    void insertOrgLevel(List<OrgLevel> list);

    void updateOrgLevel(List<OrgLevel> list);

    void deleteOrgLevel(List<OrgLevel> list);

}
