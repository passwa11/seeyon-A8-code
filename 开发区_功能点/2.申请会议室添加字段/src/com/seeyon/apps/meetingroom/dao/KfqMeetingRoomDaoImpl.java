package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;

public class KfqMeetingRoomDaoImpl implements KfqMeetingRoomDao {
    @Override
    public List<MeetingRoom> findAllMeetingRoom() {
        return DBAgent.loadAll(MeetingRoom.class);
    }
}
