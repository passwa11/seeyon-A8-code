package com.seeyon.apps.ext.xk263Email.manager;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;

/**
 * 周刘成   2019-11-28
 */
public interface OrgMember263EmailMapperManager {

    void insertOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper);

    OrgMember263EmailMapper selectByUserId(String userid);

    void updateOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper);

}
