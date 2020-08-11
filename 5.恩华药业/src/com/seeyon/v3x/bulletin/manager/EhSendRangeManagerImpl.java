package com.seeyon.v3x.bulletin.manager;

import com.seeyon.v3x.bulletin.dao.EhSendRangeDao;
import com.seeyon.v3x.bulletin.dao.EhSendRangeDaoImpl;
import com.seeyon.v3x.bulletin.domain.EhSendRange;

import java.util.List;
import java.util.Map;

public class EhSendRangeManagerImpl implements EhSendRangeManager {

    private EhSendRangeDao dao = new EhSendRangeDaoImpl();

    @Override
    public void saveEhSendRange(EhSendRange en) {
        dao.saveEhSendRange(en);
    }

    @Override
    public void updateEhSendRange(EhSendRange en) {
        dao.updateEhSendRange(en);
    }

    @Override
    public List<EhSendRange> findEhSendRangeByCondition(Map moduleId) {
        return dao.findEhSendRangeByCondition(moduleId);
    }

    public EhSendRangeDao getDao() {
        return dao;
    }
}
