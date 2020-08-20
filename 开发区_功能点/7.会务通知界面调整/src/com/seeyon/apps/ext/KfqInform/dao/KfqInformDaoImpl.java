package com.seeyon.apps.ext.KfqInform.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.ext.KfqInform.po.KfqInform;
import com.seeyon.ctp.util.DBAgent;
import org.sqlite.core.DB;

public class KfqInformDaoImpl implements KfqInformDao{

    @Override
    public void saveInform(KfqInform inform) {
        DBAgent.save(inform);
    }

    @Override
    public List<KfqInform> findInformbyUserid(Map map) {
        return DBAgent.find("from KfqInform where createuserid =:createuserid order by sort asc ",map);
    }

    @Override
    public List<KfqInform> findInformbySummaryid(Map map) {
        return DBAgent.find("from KfqInform where summaryid =:summaryid order by sort asc ",map);
    }

    @Override
    public void updateInform(List<KfqInform> inform) {
        DBAgent.updateAll(inform);
    }
}
