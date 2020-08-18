package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meetingroom.dao.KfqMeetingRoomDao;
import com.seeyon.apps.meetingroom.dao.KfqMeetingRoomDaoImpl;
import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class KfqMeetingRoomManagerImpl implements KfqMeetingRoomManager {

    private KfqMeetingRoomDao roomDao = new KfqMeetingRoomDaoImpl();

    @Override
    public List<MeetingRoom> findAllMeetingRoom(Map<String,Object> map) {
        try {
            return roomDao.findAllMeetingRoom(map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KfqMeetingRoomDao getRoomDao() {
        return roomDao;
    }
}
