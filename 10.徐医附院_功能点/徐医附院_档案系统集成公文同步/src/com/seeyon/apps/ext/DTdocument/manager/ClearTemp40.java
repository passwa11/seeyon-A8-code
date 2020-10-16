package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Administrator on 2020/5/25.
 */
public class ClearTemp40 {
    public static ClearTemp40 clearTemp40;

    public static ClearTemp40 getInstance() {
        if (null == clearTemp40) {
            clearTemp40 = new ClearTemp40();
        }
        return clearTemp40;
    }

    public void clearTableData() {
        String deleteSql = "delete from TEMP_NUMBER40";
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(deleteSql);
            ps.executeUpdate();
        } catch (SQLException sql) {
            sql.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
