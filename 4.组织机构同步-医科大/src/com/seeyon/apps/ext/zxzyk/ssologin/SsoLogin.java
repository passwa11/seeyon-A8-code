package com.seeyon.apps.ext.zxzyk.ssologin;

import com.neusoft.education.tp.sso.client.filter.CASFilterRequestWrapper;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class SsoLogin {

    private static Logger log = LoggerFactory.getLogger(SsoLogin.class);

    public static void login(HttpServletRequest request, HttpServletResponse response) {
        CASFilterRequestWrapper reqWrapper = new CASFilterRequestWrapper(request);
        String loginName = reqWrapper.getRemoteUser();
        if (null != loginName) {
            try {
                response.sendRedirect("login/sso?from=xzykSso&ticket=" + loginName);
            } catch (IOException e) {
                log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
            }
        }
    }

    public static void oaBill(HttpServletRequest request, HttpServletResponse response) {
        try {
            String ticket = request.getParameter("ticket");
            String url = request.getParameter("redirectUrl");
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xzykSso");
            String urlt = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url);
            response.sendRedirect(urlt);
        } catch (IOException e) {
            log.error("医科系统打开Oa代办事项出错了，错误信息：" + e.getMessage());
        }
    }
}
