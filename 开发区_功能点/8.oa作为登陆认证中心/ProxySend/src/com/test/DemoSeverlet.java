package com.test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DemoSeverlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginName = req.getParameter("loginName");
        String pwd = req.getParameter("pwd");
        String token = TokenUtil.getToken();
        String resutl = TokenUtil.doPost(loginName, pwd, token);
        System.out.println(resutl);
        if (null != loginName && pwd != null) {
            req.getRequestDispatcher("/WEB-INF/jsp/homt.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }
}
