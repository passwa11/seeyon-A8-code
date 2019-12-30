package com.seeyon.apps.ext.ssoLogin.sso;

import com.wiscom.is.IdentityFactory;
import com.wiscom.is.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;

/**
 * 周刘成   2019-12-16
 */
public class XkSsoLogin extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(XkSsoLogin.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter pw = response.getWriter();
        URL is_config = XkSsoLogin.class.getResource("/client.properties");
        Cookie[] all_cookises = request.getCookies();
        String decodeCookieValue = null;
        IdentityFactory factory = null;
        if (all_cookises != null) {
            for (int i = 0; i < all_cookises.length; i++) {
                Cookie myCookie = all_cookises[i];
                if (myCookie.getName().equals("iPlanetDirectoryPro")) {
                    decodeCookieValue = URLDecoder.decode(myCookie.getValue(), "GB2312");
                    log.info("ticket：" + decodeCookieValue + "cookies 内容value是：" + myCookie.getValue());
                }
            }
        }
        try {
            factory = IdentityFactory.createFactory(is_config.getPath());
        } catch (Exception var12) {
            var12.printStackTrace();
            pw.println("is_config.getPath() is error!");
        }
        IdentityManager im = factory.getIdentityManager();
        String loginName = "";
        if (decodeCookieValue == null) {
            response.addHeader("SSOError", "no user login!");
        } else {
            loginName = im.getCurrentUser(decodeCookieValue);
            char[] encodeStringCharArray = loginName.toCharArray();

            for (int i = 0; i < encodeStringCharArray.length; ++i) {
                ++encodeStringCharArray[i];
            }

            String ticket = new String(Base64.getEncoder().encode((new String(encodeStringCharArray)).getBytes()));
            response.sendRedirect("login/sso?from=xkSso&ticket=" + ticket);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

//    public static void login(HttpServletRequest request, HttpServletResponse response) {
//
//        String loginName = request.getRemoteUser();
//        String encodeloginName = StringHandle.encode(loginName);
//        if (null != loginName) {
//            try {
//                response.sendRedirect("login/sso?from=xkSso&ticket=" + encodeloginName);
//            } catch (IOException e) {
//                log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
//            }
//        }
//    }

}
