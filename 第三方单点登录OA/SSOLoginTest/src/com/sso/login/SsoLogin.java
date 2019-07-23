package com.sso.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.seeyon.ctp.util.HttpClientUtil;
import sun.misc.BASE64Encoder;

public class SsoLogin extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String url = null;
        String username = request.getParameter("username");
        Cookie cookie = new Cookie("login", username);
        cookie.setMaxAge(36000);
        response.addCookie(cookie);
        String newName = username;
        url = "http://127.0.0.1:80/seeyon/login/sso?ticket=" + StringHandlerUtil.encode(newName) + "&from=sample";
//        url = "http://211.103.127.211:8888/seeyon/login/sso?ticket=" + newName + "&from=sample";

        response.sendRedirect(url);
    }

}
