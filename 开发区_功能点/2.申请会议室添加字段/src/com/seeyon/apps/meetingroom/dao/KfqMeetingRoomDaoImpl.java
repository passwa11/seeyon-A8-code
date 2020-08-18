package com.seeyon.apps.meetingroom.dao;

import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KfqMeetingRoomDaoImpl implements KfqMeetingRoomDao {
    @Override
    public List<MeetingRoom> findAllMeetingRoom(Map<String, Object> map) throws SQLException {
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select room.id,room.name,room.seatcount from meeting_room room where id not in (" +
                " select a.meetingroomid from (select * from meeting_room_app where APPDATETIME>to_date(?,'yyyy-MM-dd') and id <>?) a " +
                " where a.enddatetime > to_date(?,'yyyy-MM-dd HH24:mi:ss') and a.startdatetime<to_date(?,'yyyy-MM-dd HH24:mi:ss')) " +
                " and room.off_admin =?";
        List<MeetingRoom> list = new ArrayList<>();
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, (String) map.get("dateRange"));
            ps.setLong(2, (Long) map.get("appId"));
            ps.setString(3, (String) map.get("starttime"));
            ps.setString(4, (String) map.get("endtime"));
            ps.setLong(5, (Long)map.get("userId"));
            rs = ps.executeQuery();
            MeetingRoom room = null;
            while (rs.next()) {
                room = new MeetingRoom();
                room.setId(rs.getLong("id"));
                room.setName(rs.getString("name"));
                room.setSeatCount(rs.getInt("seatcount"));
                list.add(room);
            }
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
        return list;
    }
}
