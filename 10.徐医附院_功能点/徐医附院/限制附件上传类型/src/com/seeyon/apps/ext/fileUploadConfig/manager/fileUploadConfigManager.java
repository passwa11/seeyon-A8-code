package com.seeyon.apps.ext.fileUploadConfig.manager;

import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUnit;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember;
import com.seeyon.apps.ext.fileUploadConfig.po.ZorgMember;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface fileUploadConfigManager {
    //周刘成
    List<ZorgMember> showPeople(Map<String,Object> params) throws BusinessException;

    int selectUnitPeopleCount();

    List<Map<String, Object>> getUnitByAccountId(Long accountId);

    void insertUploadMember(List<ZOrgUploadMember> zOrgUploadMember);

    ZOrgUploadMember selectUploadMemberByuserId(String userid);

    List<ZOrgUploadMember> selectAllUploadMem();

}
