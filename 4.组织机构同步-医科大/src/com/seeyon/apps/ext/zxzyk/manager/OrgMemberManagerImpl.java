package com.seeyon.apps.ext.zxzyk.manager;

import com.seeyon.apps.ext.zxzyk.dao.OrgMemberDao;
import com.seeyon.apps.ext.zxzyk.dao.OrgMemberDaoImpl;
import com.seeyon.apps.ext.zxzyk.po.OrgMember;

import java.util.List;

public class OrgMemberManagerImpl implements OrgMemberManager {

    private OrgMemberDao orgMemberDao = new OrgMemberDaoImpl();

    @Override
    public void insertOrgMember() {
        List<OrgMember> list = orgMemberDao.queryAddOrgMember();
        orgMemberDao.insertOrgMember(list);
    }

    @Override
    public void updateOrgMember() {
        List<OrgMember> list = orgMemberDao.queryUpdateOrgMember();
        orgMemberDao.updateOrgMember(list);
    }

    @Override
    public void deleteOrgMember() {
        List<OrgMember> list = orgMemberDao.queryNotExistOrgMember();
        orgMemberDao.deleteOrgMember(list);
    }
}
