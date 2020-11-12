package cn.com.zhou.git;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("=================");
        String clientId = "25e28dfa01eba790364c";
        String oauthUrl = "https://github.com/login/oauth/authorize";
        String redirect_uri = "http://localhost:8080/client1/oauth/redirect";
        resp.sendRedirect(oauthUrl + "?client_id=" + clientId + "&redirect_uri=" + redirect_uri);
        System.out.println("=================");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
