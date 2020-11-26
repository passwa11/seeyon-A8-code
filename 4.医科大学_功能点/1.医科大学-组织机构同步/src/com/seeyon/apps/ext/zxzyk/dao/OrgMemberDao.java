package com.seeyon.apps.ext.zxzyk.dao;

import com.seeyon.apps.ext.zxzyk.po.OrgMember;

import java.util.List;

public interface OrgMemberDao {

    List<OrgMember> queryAddOrgMember();

    void insertOrgMember(List<OrgMember> list);

    List<OrgMember> queryUpdateOrgMember();

    void updateOrgMember(List<OrgMember> list);

    List<OrgMember> queryNotExistOrgMember();

    void deleteOrgMember(List<OrgMember> list);
    List<OrgMember> queryNoEnableMember();

    void updateIsEnableOfMember(List<OrgMember> list);
}
