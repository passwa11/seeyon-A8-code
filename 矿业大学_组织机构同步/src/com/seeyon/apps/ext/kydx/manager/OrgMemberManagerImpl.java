package com.seeyon.apps.ext.kydx.manager;

import com.seeyon.apps.ext.kydx.dao.OrgMemberDao;
import com.seeyon.apps.ext.kydx.dao.OrgMemberDaoImpl;
import com.seeyon.apps.ext.kydx.po.OrgMember;

import java.util.List;


public class OrgMemberManagerImpl implements OrgMemberManager {

    private OrgMemberDao memberDao = new OrgMemberDaoImpl();


    @Override
    public void queryDeleteMemberByGh() {
        memberDao.queryDeleteMemberByGh();
    }

    @Override
    public void insertMember() {
        List<OrgMember> list = memberDao.queryInsertMember();
        memberDao.insertMember(list);
    }

    @Override
    public void updateMember() {
        List<OrgMember> list = memberDao.queryUpdateMember();
        memberDao.updateMember(list);
    }

    @Override
    public void deleteMember() {
        List<OrgMember> list = memberDao.queryDeleteMember();
        memberDao.deleteMember(list);
    }

    @Override
    public void updateLdap() {
        List<OrgMember> list = memberDao.queryMiddleMember();
        memberDao.checkOaLdap(list);
    }
}
