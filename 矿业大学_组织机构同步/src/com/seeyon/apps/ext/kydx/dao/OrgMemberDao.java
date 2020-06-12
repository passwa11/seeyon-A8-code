package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgMember;

import java.util.List;

public interface OrgMemberDao {

    List<OrgMember> queryInsertMember();

    void insertMember(List<OrgMember> list);

    List<OrgMember> queryUpdateMember();

    void updateMember(List<OrgMember> list);

    List<OrgMember> queryDeleteMember();

    void deleteMember(List<OrgMember> list);


}
