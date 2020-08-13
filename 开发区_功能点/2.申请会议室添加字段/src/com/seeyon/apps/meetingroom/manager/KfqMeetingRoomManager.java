package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meetingroom.po.MeetingRoom;

import java.util.List;

public interface KfqMeetingRoomManager {
    List<MeetingRoom> findAllMeetingRoom();
}
