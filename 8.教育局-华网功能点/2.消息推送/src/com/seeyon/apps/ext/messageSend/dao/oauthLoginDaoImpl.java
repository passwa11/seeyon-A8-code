package com.seeyon.apps.ext.messageSend.dao;


import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class oauthLoginDaoImpl implements oauthLoginDao {


    @Override
    public String selectCodeByLoginName(String loginName) {
        String sql = "select m.code from ORG_MEMBER m,ORG_PRINCIPAL p where m.id=p.MEMBER_ID  and p.LOGIN_NAME=?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String code = "";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, loginName);
            rs = ps.executeQuery();
            while (rs.next()) {
                code = rs.getString("CODE");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException s) {

            }
        }
        return code;
    }


}