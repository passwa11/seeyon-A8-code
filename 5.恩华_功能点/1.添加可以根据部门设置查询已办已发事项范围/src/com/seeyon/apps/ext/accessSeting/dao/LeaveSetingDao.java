package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.LeaveSeting;

import java.util.List;

public interface LeaveSetingDao {

    void saveLeaveSeting(LeaveSeting leaveSeting);
    void updateLeaveSeting(LeaveSeting leaveSeting);
    List<LeaveSeting> findAll();
}
