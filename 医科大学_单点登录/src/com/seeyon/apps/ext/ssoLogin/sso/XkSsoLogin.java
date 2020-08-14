package com.seeyon.apps.ext.ssoLogin.sso;
import com.seeyon.apps.ext.ssoLogin.util.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
public class XkSsoLogin {
    private static Logger log = LoggerFactory.getLogger(XkSsoLogin.class);

    public XkSsoLogin() {
    }

    public static void login(HttpServletRequest request, HttpServletResponse response) {
        String ticket = request.getParameter("ticket");
        String loginName = request.getRemoteUser();
        String encodeloginName = StringHandle.encode(loginName);
        if (null != loginName) {
            try {
                response.sendRedirect("login/sso?from=xkSso&ticket=" + ticket);
            } catch (IOException var6) {
                log.error("单点登录OA系统出错了，错误信息：" + var6.getMessage());
            }
        }

    }
}
