package com.monkeyk.sos.domain;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckUserStatusMapper implements RowMapper<CheckUserStatus> {

    @Override
    public CheckUserStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
        CheckUserStatus stu = new CheckUserStatus();
        stu.setId(rs.getInt("id"));
        stu.setToken(rs.getString("token"));
        stu.setToken(rs.getString("loginname"));
        return stu;
    }
}
