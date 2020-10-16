package com.seeyon.apps.ext.fileUploadConfig.dao;

import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUnit;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember;
import com.seeyon.apps.ext.fileUploadConfig.po.ZorgMember;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface fileUploadConfigDao {

    List<ZorgMember> getAllMemberPO_New(Map<String, Object> param, Boolean p1, Boolean p2);

    int selectUnitPeopleCount();

    List<ZOrgUnit> getUnitByAccountId(Long accountId);

    void insertUploadMember(ZOrgUploadMember zOrgUploadMember);

    ZOrgUploadMember selectUploadMemberByuserId(String userid);

    void deleteUploadMember();

    //查询已经拥有附件权限的用户
    List<ZOrgUploadMember> selectAllUploadMem();

}
