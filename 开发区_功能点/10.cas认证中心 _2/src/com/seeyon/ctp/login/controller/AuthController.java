package com.seeyon.ctp.login.controller;

import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.security.MessageEncoder;
import com.seeyon.ctp.login.server.util.StringUtilSso;
import com.seeyon.ctp.login.server.util.TicketUtil;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.MemberManager;
import com.seeyon.ctp.organization.manager.MemberManagerImpl;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.StringUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthController extends BaseController {

    public ModelAndView login(String account, String password, String service, HttpServletRequest request,
                              HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        try {
            Map<String, String> map = getMemberInfo(account);
            if (null != map) {
                String pwd = map.get("pwd");
                MessageEncoder encoder = new MessageEncoder();
                String encodePwd = encoder.encode(account, password);
                if (pwd.equals(encodePwd)) {
                    Cookie cookie = new Cookie("sso", map.get("memberId"));
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
                //是否是应用服务器重定向而来
                if (StringUtilSso.isUnEmpty(service)) {
                    // ticket
                    String ticket = account + System.currentTimeMillis();
                    TicketUtil.put(ticket, account);
                    StringBuilder url = new StringBuilder();
                    url.append(service);
                    if (0 <= service.indexOf("?")) {
                        url.append("&");
                    } else {
                        url.append("?");
                    }
                    url.append("ticket=").append(ticket);
                    url.append("&globalSessionId=").append(request.getSession().getId());
                    logger.info("登录成功：回跳应用网站：" + url.toString());
                    modelAndView.setViewName("redirect:" + url.toString());
                    return modelAndView;
                }
            } else {
                modelAndView.setViewName("redirect:" + "/auth/toLogin?service=" + service);
                return modelAndView;
            }
        } catch (SQLException sql) {
            sql.printStackTrace();
        } catch (NoSuchAlgorithmException n) {
            n.printStackTrace();
        }

        return null;
    }

    public Map<String, String> getMemberInfo(String name) throws SQLException {
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select LOGIN_NAME,CREDENTIAL_VALUE,MEMBER_ID from  ORG_PRINCIPAL where LOGIN_NAME=? and rownum=1";
        Map<String, String> map = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            rs = ps.executeQuery();
            while (rs.next()) {
                map = new HashMap<>();
                map.put("loginName", rs.getString("LOGIN_NAME"));
                map.put("pwd", rs.getString("CREDENTIAL_VALUE"));
                map.put("memberId", rs.getLong("MEMBER_ID") + "");
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
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
        }
        return map;
    }
}
