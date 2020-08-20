package com.seeyon.apps.ext.KfqInform.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.ext.KfqInform.po.KfqInform;
import com.seeyon.ctp.util.DBAgent;

public class KfqInformDaoImpl implements KfqInformDao{

    @Override
    public void saveInform(KfqInform inform) {
        DBAgent.save(inform);
    }

    @Override
    public List<KfqInform> findInformbyUserid(Map map) {
        return DBAgent.find("from KfqInform where createuserid =:createuserid order by sort asc ",map);
    }

}
