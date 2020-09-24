package com.seeyon.ctp.login.controller;

import com.seeyon.ctp.login.util.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 周刘成   2019-12-31
 */
public class SsoLogin extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(SsoLogin.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loginName = request.getRemoteUser();
        if (null != loginName) {
            String encodeloginName = StringHandle.encode(loginName);
            try {
                String servername = request.getServerName();
                String url = "http://" + servername + "/seeyon/";
                response.sendRedirect(url + "login/sso?from=xkSso&ticket=" + encodeloginName);
            } catch (IOException e) {
                log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
