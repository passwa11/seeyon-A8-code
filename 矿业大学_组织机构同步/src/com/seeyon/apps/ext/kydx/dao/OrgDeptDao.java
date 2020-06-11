package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgDept;

import java.util.List;

public interface OrgDeptDao {

    List<OrgDept> queryFirstOrgDept();

    void insertOrgDept(List<OrgDept> list);

    List<OrgDept> queryOtherOrgDept();

}
