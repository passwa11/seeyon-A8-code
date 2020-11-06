package com.seeyon.apps.ext.oauthLogin.servlet;

import com.seeyon.apps.ext.oauthLogin.util.PropUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ToRedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = req.getSession();
        PropUtils pUtils = new PropUtils();
        String code = req.getParameter("code");
        System.out.println(code);
        if (session.getAttribute(pUtils.getSSOSessionUser()) == null) {
            response.sendRedirect(pUtils.getSSOAuthPath() + "?returnUrl=" + java.net.URLEncoder.encode(pUtils.getSSOClientHomePage(), "utf-8"));//未登录跳转到转向服务器登录页面
        } else {
            response.getWriter().print("用户已登录;<br>Token:" + session.getAttribute(pUtils.getSSOSessionAccessToken()) + "<br>用户ID:" + session.getAttribute(pUtils.getSSOSessionUserId()) + "<br>超时时间:" + session.getAttribute(pUtils.getSSOSessionExpires_in()) + "<br>用户信息是：" + session.getAttribute(pUtils.getSSOSessionUser()));//拿到用户信息进行处理
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
