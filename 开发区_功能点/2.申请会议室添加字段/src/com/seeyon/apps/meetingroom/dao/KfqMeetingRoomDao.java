package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.util.List;

public interface KfqMeetingRoomDao {

    List<MeetingRoom> findAllMeetingRoom();
}
