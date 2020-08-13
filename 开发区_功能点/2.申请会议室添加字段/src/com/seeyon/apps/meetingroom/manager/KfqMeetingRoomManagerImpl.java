package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meetingroom.dao.KfqMeetingRoomDao;
import com.seeyon.apps.meetingroom.dao.KfqMeetingRoomDaoImpl;
import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.util.List;

public class KfqMeetingRoomManagerImpl implements KfqMeetingRoomManager {

    private KfqMeetingRoomDao roomDao = new KfqMeetingRoomDaoImpl();

    @Override
    public List<MeetingRoom> findAllMeetingRoom() {
        return roomDao.findAllMeetingRoom();
    }

    public KfqMeetingRoomDao getRoomDao() {
        return roomDao;
    }
}
