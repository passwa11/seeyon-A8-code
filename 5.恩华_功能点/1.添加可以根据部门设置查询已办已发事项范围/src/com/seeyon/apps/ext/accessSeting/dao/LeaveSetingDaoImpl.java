package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.LeaveSeting;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;

public class LeaveSetingDaoImpl implements LeaveSetingDao{

    @Override
    public void saveLeaveSeting(LeaveSeting leaveSeting) {
        DBAgent.save(leaveSeting);
    }

    @Override
    public void updateLeaveSeting(LeaveSeting leaveSeting) {
        DBAgent.update(leaveSeting);
    }

    @Override
    public List<LeaveSeting> findAll() {
        return DBAgent.find("from LeaveSeting");
    }
}
