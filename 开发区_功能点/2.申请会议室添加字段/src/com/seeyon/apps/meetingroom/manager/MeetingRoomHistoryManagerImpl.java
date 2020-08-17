package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meetingroom.dao.MeetingRoomHistoryDao;
import com.seeyon.apps.meetingroom.dao.MeetingRoomHistoryDaoImpl;
//import com.seeyon.apps.meetingroom.po.MeetingRoomAppHistory;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;

import java.sql.SQLException;
import java.util.Map;

public class MeetingRoomHistoryManagerImpl implements MeetingRoomHistoryManager {

    private MeetingRoomHistoryDao historyDao=(MeetingRoomHistoryDao)AppContext.getBean("meetingRoomHistoryDao");

    @AjaxAccess
    @Override
    public FlipInfo findPageByCondition(FlipInfo flipInfo,Map<String, Object> map) throws SQLException, BusinessException {
        User user = AppContext.getCurrentUser();
        map.put("memberId", user.getId());
        this.historyDao.findPageByCondition(map,flipInfo);
        return flipInfo;
    }

//    @Override
//    public void saveRoomappHistory(MeetingRoomAppHistory history) {
//        this.historyDao.saveRoomapp(history);
//    }

    public MeetingRoomHistoryDao getHistoryDao() {
        return historyDao;
    }
}
