//package com.sso.login;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
////import com.seeyon.ctp.util.HttpClientUtil;
//
//
//public class SsoLogin2 extends HttpServlet {
//
//    public void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        this.doPost(request, response);
//    }
//
//    public void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        request.setCharacterEncoding("UTF-8");
//        response.setContentType("text/html;charset=UTF-8");
//        PrintWriter out = response.getWriter();
//        String url = null;
//        String url1 = null;
//        String username = request.getParameter("username");
//        String check = request.getParameter("doc");
//        System.out.println(check);
//        Cookie cookie = new Cookie("login", username);
//        cookie.setMaxAge(36000);
//        response.addCookie(cookie);
////        BASE64Encoder base64Encoder = new BASE64Encoder();
////        String newName = base64Encoder.encode(username.getBytes());
//        String newName = username;
//        if (check.equals("1")) {
//            url = "http://127.0.0.1:80/seeyon/login/sso?ticket=" + newName + "&from=sample";
//        }
////        else {
////            url1 = "http://127.0.0.1:80/seeyon/login/sso?ticket=" + newName + "&from=sample";
////            HttpClientUtil u = new HttpClientUtil();
////            u.open(url1, "post");
////            u.send();
////            u.close();
////            url = "/seeyon/collaboration/collaboration.do?method=newColl";
////            url = "http://127.0.0.1:80/seeyon/main.do?method=login&ticket=" + newName + "&login.destination=" + url;
////
////        }
//        response.sendRedirect(url);
//    }
//
//}
