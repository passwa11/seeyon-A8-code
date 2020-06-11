package com.seeyon.apps.ext.kydx.util;

import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

/**
 * Created by Administrator on 2019-7-29.
 */
public class SyncConnectionInfoUtil {
    private static final Log log = LogFactory.getLog(SyncConnectionInfoUtil.class);

    private static ReadConfigTools configTools = new ReadConfigTools();

    public SyncConnectionInfoUtil() {
    }

    /**
     * 获取rest
     *
     * @return
     */
    public static CTPRestClient getOARestInfo() {
        String restUrl = configTools.getString("restInfo.url");
        String restUser = configTools.getString("restInfo.username");
        String restPwd = configTools.getString("restInfo.password");
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
        String driverName = configTools.getString("midDataLink.driver");
        String url = configTools.getString("midDataLink.url");
        String username = configTools.getString("midDataLink.username");
        String password = configTools.getString("midDataLink.password");
        Connection connection = null;
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static int insertResult(String sql) throws Exception {
        Connection connection = getMidConnection();
        Statement statement = null;
        int result = 0;
        try {
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);
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

    public static void closePrepareStatement(PreparedStatement ps, Statement statement) {
        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != statement) {
            try {
                statement.close();
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
