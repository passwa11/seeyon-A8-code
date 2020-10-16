package com.seeyon.apps.ext.zMember.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public interface zMemberManager {
    //周刘成
    FlipInfo showPeople(FlipInfo fi, Map params) throws BusinessException;

    int selectUnitPeopleCount();
}
