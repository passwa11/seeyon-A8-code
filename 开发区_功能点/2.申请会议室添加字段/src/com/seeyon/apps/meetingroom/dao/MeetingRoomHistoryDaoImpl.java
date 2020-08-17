package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.meetingroom.po.MeetingRoomAppHistory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeetingRoomHistoryDaoImpl implements MeetingRoomHistoryDao {
    @Override
    public FlipInfo findPageByCondition(Map<String, Object> map, FlipInfo flipInfo) throws SQLException, BusinessException {
        List<Object> appHistories = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        String sql = "select id, (select name from meeting_room where id=meetingroomid) meetingName, (select name from ORG_MEMBER where id =perid) perName, (select name from ORG_UNIT where id =departmentid) deptName, startdatetime, enddatetime, meetingid, description, status, appdatetime, auditing_id, template_id, periodicity_id, used_status, time_diff, account_id, sqrdh, sfygwhldcj, hcyq, ldid, ldname from " +
                "meeting_room_app_history where 1=1  ";
        sb.append(sql);
        JDBCAgent jdbcAgent = new JDBCAgent(true, false);
        if(null !=map.get("memberId")){
//            sb.append(" perId="+map.get("memberId"));
        }
        try {
            jdbcAgent.execute(sb.toString());
            appHistories = jdbcAgent.resultSetToList();
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal(appHistories.size());
        List newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;
        for (int i = 0; i < size && i < (appHistories).size() - currIdx; ++i) {
            newList.add((appHistories).get(currIdx + i));
        }

        flipInfo.setData(newList);
        return flipInfo;
    }
}
