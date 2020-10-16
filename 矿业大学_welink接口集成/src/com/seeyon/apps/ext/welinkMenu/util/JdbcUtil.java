package com.seeyon.apps.ext.welinkMenu.util;

import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcUtil {

    public static List<Map<String, Object>> doQuery(String sql) {
        try (JDBCAgent jdbcAgent = new JDBCAgent(JDBCAgent.getRawConnection())) {
            jdbcAgent.execute(sql);
            return jdbcAgent.resultSetToList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getConnection() {
        try {
            return JDBCAgent.getRawConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
