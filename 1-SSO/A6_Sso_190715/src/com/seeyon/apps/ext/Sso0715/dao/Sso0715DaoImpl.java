package com.seeyon.apps.ext.Sso0715.dao;

import com.seeyon.apps.ext.Sso0715.pojo.Ssoentity;
import com.seeyon.apps.ext.Sso0715.util.JDBCUtil;
import com.seeyon.ctp.util.DBAgent;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sso0715DaoImpl implements Sso0715Dao {

    @Override
    public void insertSso(List<Ssoentity> list) {
        DBAgent.saveAll(list);
    }

    @Override
    public List<Map<String, Object>> selectAccountOA() {
        String sql = "select m.NAME,m.EXT_ATTR_1 phone,p.LOGIN_NAME from ORG_MEMBER m RIGHT JOIN ORG_PRINCIPAL p on m. id=p.MEMBER_ID";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectThirdAccount() {
        Connection connection = JDBCUtil.getConnection4Oracle10();
        String sql = "select username ,REALNAME from SYS_USER";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("account", resultSet.getString("USERNAME"));
                m.put("realname", resultSet.getString("REALNAME"));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}