package com.seeyon.apps.ext.xk263Email.dao;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;

/**
 * 周刘成   2019-11-28
 */
public interface OrgMember263EmailMapperDao {

    void insertOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper);

    OrgMember263EmailMapper selectByUserId(Long userid);
}
