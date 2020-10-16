package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.util.List;
import java.util.Map;

public interface KfqMeetingRoomManager {
    List<MeetingRoom> findAllMeetingRoom(Map<String,Object> map);
}
