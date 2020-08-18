package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface KfqMeetingRoomDao {

    List<MeetingRoom> findAllMeetingRoom(Map<String,Object> map) throws SQLException;
}
