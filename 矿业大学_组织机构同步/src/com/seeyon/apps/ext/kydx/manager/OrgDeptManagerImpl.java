package com.seeyon.apps.ext.kydx.manager;

import com.seeyon.apps.ext.kydx.dao.OrgDeptDao;
import com.seeyon.apps.ext.kydx.dao.OrgDeptDaoImpl;
import com.seeyon.apps.ext.kydx.po.OrgDept;

import java.util.List;

public class OrgDeptManagerImpl implements OrgDeptManager {

    private OrgDeptDao deptDao = new OrgDeptDaoImpl();

    @Override
    public void deleteOrgDept() {
        deptDao.deleteOrgDept();
    }

    @Override
    public void insertOrgDept() {
        List<OrgDept> list = deptDao.queryFirstOrgDept();
        deptDao.insertOrgDept(list);
    }

    @Override
    public void insertOtherOrgDept() {
        List<OrgDept> list = deptDao.queryOtherOrgDept();
        deptDao.insertOtherOrgDept(list);
    }

    @Override
    public void updateOrgDept() {
        deptDao.updateOrgDept();
    }
}
