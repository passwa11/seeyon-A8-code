package com.monkeyk.sos.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.sql.*;

@Controller
public class SyncOaAccountData {
    @Value("${oa.datasource.driver-class-name}")
    private String oaDriver;
    @Value("${oa.datasource.url}")
    private String oaUrl;
    @Value("${oa.datasource.username}")
    private String oaUsername;
    @Value("${oa.datasource.password}")
    private String oaPassword;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @RequestMapping(value = "toSync", method = RequestMethod.GET)
    public String toSync(HttpServletRequest request, HttpServletResponse response) {

        String deleteUser_ = "delete from user_ where username <>'admin'";
        String deletePrivilege = "delete from user_privilege where privilege <> 'ADMIN'";

        this.jdbcTemplate.update(deleteUser_);
        this.jdbcTemplate.update(deletePrivilege);

        String sql = "select m.id,m.name,m.EXT_ATTR_1,m.ext_attr_2,p.login_name,p.credential_value from (select * from ORG_MEMBER where IS_ENABLE=1 and IS_DELETED=0) m,org_principal p where m.id = p.member_id";
        Connection connection = getConnection();
        PreparedStatement ps = null;
        String insertSql = "insert into user_ (id,guid,username,password,realname,phone,email) values (?,?,?,?,?,?,?)";
        String insertRoleSql = "insert into user_privilege(user_id,privilege) values (?,?)";
        try {
            ps = connection.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int index = this.jdbcTemplate.update(insertSql, jps -> {
                    jps.setString(1, rs.getString("id"));
                    jps.setString(2, rs.getString("id"));
                    jps.setString(3, rs.getString("login_name"));
                    jps.setString(4, rs.getString("credential_value"));
                    jps.setString(5, rs.getString("name"));
                    jps.setString(6, rs.getString("EXT_ATTR_1"));
                    jps.setString(7, rs.getString("ext_attr_2"));
                });
                if (index == 1) {
                    String loginName = rs.getString("login_name");
                    if ("admin".equals(loginName)) {
                        this.jdbcTemplate.update(insertRoleSql, jps -> {
                            jps.setString(1, rs.getString("id"));
                            jps.setString(2, "ADMIN");
                        });
                        this.jdbcTemplate.update(insertRoleSql, jps -> {
                            jps.setString(1, rs.getString("id"));
                            jps.setString(2, "UNITY");
                        });
                    } else {
                        this.jdbcTemplate.update(insertRoleSql, jps -> {
                            jps.setString(1, rs.getString("id"));
                            jps.setString(2, "UNITY");
                        });
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/./";
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(oaDriver);
            connection = DriverManager.getConnection(oaUrl, oaUsername, oaPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}
