package com.seeyon.apps.ext.oauthLogin.servlet;

import com.seeyon.apps.ext.oauthLogin.util.PropUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("=======================");
        String returnUrl = request.getParameter("returnUrl");
        response.setCharacterEncoding("UTF-8");
        PropUtils propUtils = new PropUtils();
        String url = propUtils.getSSOOAuthAuthorize() + "?clientId=" + propUtils.getSSOClientId() + "&returnUrl=" + java.net.URLEncoder.encode(returnUrl, "utf-8");
        response.sendRedirect("/WEB-INF/jsp/apps/ext/oauthLogin/index.jsp");
        System.out.println("=======================");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
