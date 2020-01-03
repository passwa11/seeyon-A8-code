package com.seeyon.apps.ext.syncPost.util;

import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

/**
 * Created by Administrator on 2019-7-29.
 */
public class SyncConnectionUtil {
    private static final Log log = LogFactory.getLog(SyncConnectionUtil.class);

    private static ReadConfigTools configTools = new ReadConfigTools();

    public SyncConnectionUtil() {
    }

    /**
     * 获取rest
     *
     * @return
     */
    public static CTPRestClient getOaRest() {
        String restUrl = configTools.getString("gcxy.restInfo.url");
        String restUser = configTools.getString("gcxy.restInfo.username");
        String restPwd = configTools.getString("gcxy.restInfo.password");
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(restUrl);
        CTPRestClient restClient = clientManager.getRestClient();
        boolean ltFlag = restClient.authenticate(restUser, restPwd);
        return ltFlag ? restClient : null;

    }

    /**
     * 获取连接
     *
     * @return
     */
    public static Connection getMidConnection() {
        String driverName = configTools.getString("gcxy.midDataLink.driver");
        String url = configTools.getString("gcxy.midDataLink.url");
        String username = configTools.getString("gcxy.midDataLink.username");
        String password = configTools.getString("gcxy.midDataLink.password");
        Connection connection = null;
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static int insertResult(String sql) {
        Connection connection = getMidConnection();
        Statement statement = null;
        int result = 0;
        try {
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
            closeConnection(connection);
        }
        return result;
    }

    /**
     * @param connection
     */
    public static void closeConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closePrepareStatement(PreparedStatement ps) {
        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet ps) {
        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement ps) {
        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public static ResultSet getResultSet(String sql) {
        Connection connection = getMidConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return resultSet;
    }

}
