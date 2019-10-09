package com.seeyon.apps.ext.DTdocument.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.apps.ext.DTdocument.util.ReadConfigTools;

public class DTdocumentDaoImpl implements DTdocumentDao {

    private ReadConfigTools configTools = null;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driverClassName = "";

    public DTdocumentDaoImpl() {
        configTools = new ReadConfigTools();
        url = configTools.getString("lowPortalPlugin.db.url");
        username = configTools.getString("lowPortalPlugin.db.username");
        password = configTools.getString("lowPortalPlugin.db.password");
        driverClassName = configTools.getString("lowPortalPlugin.db.driverClassName");
    }

    @Override
    public int updateDocState(String columnName, String id) {
        int i = -1;
        String sql = "update Law_Fox_Table lft set lft." + columnName + "='1' where lft.id=?";// 用户已登录法律系统
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            if (conn != null) {
                pst = conn.prepareStatement(sql);
                // 绑定参数
                pst.setString(1, id);
                int row = pst.executeUpdate();
                if (row == 1) {
                    i = 0;
                }
            }
        } catch (Exception e) {
            i = -1;
            System.out.println("更待状态发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return i;
    }

    @Override
    public void setDocAccount(UserPas userPas) throws Exception {
        String checkSql = "select * from Law_Fox_Table where id=" + userPas.getId();
        String updateSql = "update Law_Fox_Table lft set lft.RECORD_USERNAME='" + userPas.getRecord_user() + "',lft.RECORD_PASSWORD='" + userPas.getRecord_pas() + "',lft.RECORD_STATE='0' where lft.id=" + userPas.getId();
        String sql = "insert into Law_Fox_Table values(?,'','','','',?,?,0,0,0,0,0,0,0,0,0)";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(checkSql);
            rs = pst.executeQuery();
            boolean next = rs.next();
            if (next == true) {
                pst.executeUpdate(updateSql);
            } else {
                pst = conn.prepareStatement(sql);
                pst.setString(1, userPas.getId());
                pst.setString(2, userPas.getRecord_user());
                pst.setString(3, userPas.getRecord_pas());
                pst.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UserPas selectDocAccount(String id) {
        String sql = "select * from Law_Fox_Table where id=?";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        UserPas userPas = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            //绑定参数
            pst.setString(1, id);
            rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    userPas = new UserPas();
                    userPas.setLaw_user(rs.getString("LAW_USERNAME"));
                    userPas.setLaw_pas(rs.getString("LAW_PASSWORD"));
                    userPas.setFox_user(rs.getString("FOX_USERNAME"));
                    userPas.setFox_pas(rs.getString("FOX_PASSWORD"));
                    userPas.setRecord_user(rs.getString("RECORD_USERNAME"));
                    userPas.setRecord_pas(rs.getString("RECORD_PASSWORD"));
                }
            } else {
                return userPas;
            }
        } catch (Exception e) {
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userPas;
    }
}
