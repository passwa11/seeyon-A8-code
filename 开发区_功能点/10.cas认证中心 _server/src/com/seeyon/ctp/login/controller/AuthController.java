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
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthController extends BaseController {

    @NeedlessCheckLogin
    public ModelAndView loginOut(String server, HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        ModelAndView modelAndView=null;
        Map<String, String> loginOutMap = (Map<String, String>) httpSession.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<局部会话id，应用退出接口>
        if (loginOutMap != null) {
            // 登出系统
            // 直接使用map遍历并在遍历中删除元素会报错ConcurrentModificationException，不能在遍历中动态修改集合,解决办法：使用Iterator
            // for (String localSessionId : loginOutMap.keySet()) {
            // 正确的方法
            Iterator<Map.Entry<String, String>> iterator = loginOutMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                Map<String, String> params = new HashMap<>();
                params.put("localSessionId", entry.getKey());
                try {
                    logger.info("【【登出】Url：" + entry.getValue());
                    rg.sso.util.HttpUtil.http(entry.getValue(), params);
                    iterator.remove();// 删除已经退出的APP会话信息。
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("从未登陆过或登出会话异常，重启浏览器");
        }
        // 清除cookie
        logger.info("删除sso全局cookie");
        Cookie cookie = new Cookie("sso", "");
        cookie.setMaxAge(0);// 删除
        cookie.setPath("/");// 和创建时同一个作用域
        response.addCookie(cookie);
        // 视图控制
        if (StringUtilSso.isUnEmpty(server)) {
            // 应用请求而来
            logger.info("SSO(APP)退出成功，返回到：" + server);
            modelAndView=new ModelAndView("redirect:" + server);
            return modelAndView;
        } else {
            logger.info("SSO中心直接退出成功");
            modelAndView=new ModelAndView("redirect:index.jsp");
            return modelAndView;
        }

    }


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
