package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.ext.meetingInfoTip.po.MeetingAppHistory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.sql.SQLException;
import java.util.Map;

public interface MeetingRoomHistoryDao {

    FlipInfo findPageByCondition(Map<String, Object> map, FlipInfo flipInfo) throws SQLException, BusinessException;

    void saveRoomapp(MeetingAppHistory roomApp);
}
