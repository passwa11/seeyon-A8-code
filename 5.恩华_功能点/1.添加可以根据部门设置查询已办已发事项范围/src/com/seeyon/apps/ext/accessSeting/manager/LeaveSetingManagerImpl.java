package com.seeyon.apps.ext.accessSeting.manager;

import com.seeyon.apps.ext.accessSeting.dao.LeaveSetingDao;
import com.seeyon.apps.ext.accessSeting.dao.LeaveSetingDaoImpl;
import com.seeyon.apps.ext.accessSeting.po.LeaveSeting;

import java.util.List;

public class LeaveSetingManagerImpl implements LeaveSetingManager {

    private LeaveSetingDao dao = new LeaveSetingDaoImpl();

    @Override
    public void saveLeaveSeting(LeaveSeting leaveSeting) {
        dao.saveLeaveSeting(leaveSeting);
    }

    @Override
    public void updateLeaveSeting(LeaveSeting leaveSeting) {
        dao.updateLeaveSeting(leaveSeting);
    }

    @Override
    public List<LeaveSeting> findAll() {
        return dao.findAll();
    }
}
