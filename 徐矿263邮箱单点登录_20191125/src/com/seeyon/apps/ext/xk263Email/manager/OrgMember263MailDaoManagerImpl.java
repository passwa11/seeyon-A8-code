package com.seeyon.apps.ext.xk263Email.manager;

import com.seeyon.apps.ext.xk263Email.dao.OrgMember263MailDao;
import com.seeyon.apps.ext.xk263Email.dao.OrgMember263MailDaoImpl;
import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.ctp.organization.po.OrgUnit;

/**
 * 周刘成   2019-11-25
 */
public class OrgMember263MailDaoManagerImpl implements OrgMember263MailDaoManager {
    private OrgMember263MailDao dao = new OrgMember263MailDaoImpl();

    @Override
    public OrgUnit getOrgUnit(String id) {
        return dao.getOrgUnit(id);
    }

    @Override
    public void insert263MailMapper(OrgMember263EmailMapper member263EmailMapper) {
        dao.insert263MailMapper(member263EmailMapper);
    }

    @Override
    public OrgMember263EmailMapper get263MailMapper(String userid) {
        return dao.get263MailMapper(userid);
    }
}
