package com.seeyon.apps.ext.xk263Email.dao;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.ctp.util.DBAgent;

/**
 * 周刘成   2019-11-28
 */
public class OrgMember263EmailMapperDaoImpl implements OrgMember263EmailMapperDao {
    @Override
    public void insertOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper) {
        DBAgent.save(orgMember263EmailMapper);
    }

    @Override
    public OrgMember263EmailMapper selectByUserId(Long userid) {
        return DBAgent.get(OrgMember263EmailMapper.class, userid);
    }
}
