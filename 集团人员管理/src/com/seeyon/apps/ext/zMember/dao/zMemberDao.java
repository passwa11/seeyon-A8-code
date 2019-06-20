package com.seeyon.apps.ext.zMember.dao;

import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public interface zMemberDao {

    /**
     * 周刘成  查询所有的人员信息
     * @param accountId
     * @param isInternal
     * @param enable
     * @param param
     * @param flipInfo
     * @return
     */
    List<OrgMember> getAllMemberPO(String username, Map<String, Object> param, FlipInfo flipInfo);


    int selectUnitPeopleCount();
}
