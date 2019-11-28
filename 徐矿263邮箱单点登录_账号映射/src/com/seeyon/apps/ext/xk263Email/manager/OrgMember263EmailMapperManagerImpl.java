package com.seeyon.apps.ext.xk263Email.manager;

import com.seeyon.apps.ext.xk263Email.dao.OrgMember263EmailMapperDao;
import com.seeyon.apps.ext.xk263Email.dao.OrgMember263EmailMapperDaoImpl;
import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;

/**
 * 周刘成   2019-11-28
 */
public class OrgMember263EmailMapperManagerImpl implements OrgMember263EmailMapperManager {

    private OrgMember263EmailMapperDao dao = new OrgMember263EmailMapperDaoImpl();

    @Override
    public void updateOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper) {
        dao.updateOrgMember263Email(orgMember263EmailMapper);

    }

    @Override
    public void insertOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper) {
        dao.insertOrgMember263Email(orgMember263EmailMapper);
    }

    @Override
    public OrgMember263EmailMapper selectByUserId(String userid) {
        return dao.selectByUserId(Long.parseLong(userid));
    }
}
