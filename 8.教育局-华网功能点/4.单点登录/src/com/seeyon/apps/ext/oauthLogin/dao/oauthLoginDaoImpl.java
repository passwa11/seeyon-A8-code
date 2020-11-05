package com.seeyon.apps.ext.oauthLogin.dao;


import com.seeyon.apps.ext.oauthLogin.po.LoginRecord;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class oauthLoginDaoImpl implements oauthLoginDao {

    @Override
    public void saveLoginRecord(LoginRecord loginRecord) {
        DBAgent.save(loginRecord);
    }

    @Override
    public void updateLoginRecord(LoginRecord loginRecord) {
        DBAgent.update(loginRecord);
    }

    @Override
    public List<LoginRecord> selectLoginRecordByLoginName(Map<String, Object> map) {
        return DBAgent.find("from LoginRecord where loginName =:loginName ", map);
    }

    @Override
    public String selectLoginNameByCode(String code) {
        String sql = "select p.LOGIN_NAME from ORG_MEMBER m,ORG_PRINCIPAL p where m.id=p.MEMBER_ID  and m.code=?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String loginName = "";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, code);
            rs = ps.executeQuery();
            while (rs.next()) {
                loginName = rs.getString("LOGIN_NAME");
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
        return loginName;
    }


}