package com.seeyon.apps.ext.xk263Email.dao;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.ctp.organization.po.OrgUnit;
import com.seeyon.ctp.util.DBAgent;

/**
 * 周刘成   2019-11-25
 */
public class OrgMember263MailDaoImpl implements OrgMember263MailDao {

    @Override
    public OrgUnit getOrgUnit(String id) {
        return DBAgent.get(OrgUnit.class, Long.parseLong(id));
    }

    @Override
    public void insert263MailMapper(OrgMember263EmailMapper member263EmailMapper) {
        DBAgent.save(member263EmailMapper);
    }

    @Override
    public OrgMember263EmailMapper get263MailMapper(String userid) {
        return DBAgent.get(OrgMember263EmailMapper.class, Long.parseLong(userid));
    }
}
