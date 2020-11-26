package com.seeyon.v3x.bulletin.dao;

import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.bulletin.domain.EhSendRange;

import java.util.List;
import java.util.Map;

public class EhSendRangeDaoImpl implements EhSendRangeDao {

    @Override
    public void saveEhSendRange(EhSendRange en) {
        DBAgent.save(en);
    }

    @Override
    public void updateEhSendRange(EhSendRange en) {
        DBAgent.update(en);
    }

    @Override
    public List<EhSendRange> findEhSendRangeByCondition(Map map) {
        return DBAgent.find("from EhSendRange where moduleId=:moduleId",map);
    }
}
