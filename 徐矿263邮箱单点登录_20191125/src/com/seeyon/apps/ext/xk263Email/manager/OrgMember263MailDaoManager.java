package com.seeyon.apps.ext.xk263Email.manager;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.ctp.organization.po.OrgUnit;

/**
 * 周刘成   2019-11-25
 */
public interface OrgMember263MailDaoManager {
    void insert263MailMapper(OrgMember263EmailMapper member263EmailMapper);

    OrgMember263EmailMapper get263MailMapper(String userid);

    OrgUnit getOrgUnit(String id);
}
