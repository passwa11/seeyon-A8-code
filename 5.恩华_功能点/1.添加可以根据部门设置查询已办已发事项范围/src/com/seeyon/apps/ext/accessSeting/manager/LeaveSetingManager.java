package com.seeyon.apps.ext.accessSeting.manager;

import com.seeyon.apps.ext.accessSeting.po.LeaveSeting;

import java.util.List;

public interface LeaveSetingManager {

    void saveLeaveSeting(LeaveSeting leaveSeting);
    void updateLeaveSeting(LeaveSeting leaveSeting);
    List<LeaveSeting> findAll();
}
