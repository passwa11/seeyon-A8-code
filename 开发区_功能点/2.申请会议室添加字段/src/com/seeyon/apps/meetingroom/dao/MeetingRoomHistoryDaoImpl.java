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
        List<MeetingRoomAppHistory> appHistories = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        String hql = "from MeetingRoomAppHistory where 1=1 and ";
        sb.append(hql);
        JDBCAgent jdbcAgent = new JDBCAgent(true, true);
        if(null !=map.get("memberId")){
            sb.append(" perId="+map.get("memberId"));
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
