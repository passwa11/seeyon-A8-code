package com.seeyon.apps.ext.zMember.manager.impl;

import com.seeyon.apps.ext.zMember.dao.impl.zMemberDaoImpl;
import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.manager.zMemberManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public class zMemberManagerImpl implements zMemberManager {

    private zMemberDao zMemberDao = new zMemberDaoImpl();

    @Override
    public FlipInfo showPeople(FlipInfo fi, Map params) throws BusinessException {
        String username = (String) params.get("username");
        zMemberDao.getAllMemberPO(username, params, fi);
        return fi;
    }
}
