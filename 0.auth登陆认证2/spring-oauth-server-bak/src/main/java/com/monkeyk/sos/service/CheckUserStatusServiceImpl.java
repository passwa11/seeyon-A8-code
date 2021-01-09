package com.monkeyk.sos.service;

import com.monkeyk.sos.domain.CheckUserStatus;
import com.monkeyk.sos.domain.CheckUserStatusDao;
import com.monkeyk.sos.domain.CheckUserStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CheckUserStatusServiceImpl implements CheckUserStatusDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int addUserStatus(CheckUserStatus userStatus) {
        String sql = "insert into check_user_status(token,loginname) values(?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, userStatus.getToken());
                ps.setString(2, userStatus.getLoginname());
                return ps;
            }
        }, keyHolder);
        return Integer.parseInt(keyHolder.getKey().toString());
    }

    @Override
    public void update(CheckUserStatus userStatus) {

    }

    @Override
    public List<Map<String, Object>> findAll(String loginName) {
        String sql = "select * from check_user_status where loginname=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[]{loginName}, new CheckUserStatusMapper());
        return list;
    }

    @Override
    public void delete(String loginName) {
        String sql = "delete from check_user_status where loginname = ?";
        jdbcTemplate.update(sql, loginName);
    }
}
