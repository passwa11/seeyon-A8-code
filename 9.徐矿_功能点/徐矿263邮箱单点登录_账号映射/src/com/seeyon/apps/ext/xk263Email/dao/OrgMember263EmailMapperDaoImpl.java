package com.seeyon.apps.ext.xk263Email.dao;

import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.ctp.util.DBAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019-11-28
 */
public class OrgMember263EmailMapperDaoImpl implements OrgMember263EmailMapperDao {
    @Override
    public void updateOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper) {
        DBAgent.update(orgMember263EmailMapper);
    }

    @Override
    public List<OrgMember263EmailMapper> selectBy263Name(String name) {
        Map map=new HashMap();
        map.put("mail263Name",name);
        return DBAgent.find("from OrgMember263EmailMapper where mail263Name =:mail263Name", map);
    }

    @Override
    public void insertOrgMember263Email(OrgMember263EmailMapper orgMember263EmailMapper) {
        DBAgent.save(orgMember263EmailMapper);
    }

    @Override
    public OrgMember263EmailMapper selectByUserId(Long userid) {
        return DBAgent.get(OrgMember263EmailMapper.class, userid);
    }
}
