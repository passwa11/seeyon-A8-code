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
    List<OrgMember> getAllMemberPO( Map<String, Object> param,Boolean p1,Boolean p2, FlipInfo flipInfo);

    List<OrgMember> getAllMemberPO_New( Map<String, Object> param,Boolean p1,Boolean p2, FlipInfo flipInfo);


    int selectUnitPeopleCount();
}
