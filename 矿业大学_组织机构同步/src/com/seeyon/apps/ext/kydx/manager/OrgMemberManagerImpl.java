package com.seeyon.apps.ext.kydx.manager;

import com.seeyon.apps.ext.kydx.dao.OrgMemberDao;
import com.seeyon.apps.ext.kydx.dao.OrgMemberDaoImpl;
import com.seeyon.apps.ext.kydx.po.OrgMember;

import java.util.List;


public class OrgMemberManagerImpl implements OrgMemberManager {

    private OrgMemberDao memberDao = new OrgMemberDaoImpl();

    @Override
    public void insertMember() {
        List<OrgMember> list = memberDao.queryInsertMember();
        memberDao.insertMember(list);
    }

    @Override
    public void updateMember() {

    }

    @Override
    public void deleteMember() {

    }
}
