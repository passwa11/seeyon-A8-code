package com.seeyon.apps.ext.oauthLogin.servlet;

import com.seeyon.apps.ext.oauthLogin.util.PropUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PropUtils propUtils = new PropUtils();
        String url=propUtils.getSSOOAuthLogout() + "&clientId=" + propUtils.getSSOClientId() + "&returnUrl=" + java.net.URLEncoder.encode(propUtils.getSSOClientHomePage(), "utf-8");
        resp.sendRedirect(url);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
