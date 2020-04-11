package com.seeyon.apps.ext.DTdocument.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Administrator on 2019-9-10.
 */
public class DbConnUtil {

    public static DbConnUtil dbConnUtil;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driverClassName = "";


    private String middleUrl = "";
    private String middleUsername = "";
    private String middlePassword = "";
    private String middleDriverClassName = "";


    public static DbConnUtil getInstance() {
        if (null == dbConnUtil) {
            dbConnUtil = new DbConnUtil();
        }
        return dbConnUtil;
    }

    public DbConnUtil() {
        ReadConfigTools configTools = new ReadConfigTools();
        url = configTools.getString("lowPortalPlugin.db.url");
        username = configTools.getString("lowPortalPlugin.db.username");
        password = configTools.getString("lowPortalPlugin.db.password");
        driverClassName = configTools.getString("lowPortalPlugin.db.driverClassName");

        middleUrl=configTools.getString("middle.db.url");
        middleUsername=configTools.getString("middle.db.username");
        middlePassword=configTools.getString("middle.db.password");
        middleDriverClassName=configTools.getString("middle.db.driverClassName");
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public Connection getMiddleConnection() {
        Connection conn = null;
        try {
            Class.forName(middleDriverClassName);
            conn = DriverManager.getConnection(middleUrl, middleUsername, middlePassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void deleteSql(String sql) {
        Connection connection = this.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (null != ps) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (null != connection) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
