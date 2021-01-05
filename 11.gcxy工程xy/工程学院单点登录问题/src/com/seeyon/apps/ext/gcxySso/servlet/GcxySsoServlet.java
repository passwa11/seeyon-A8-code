package com.seeyon.apps.ext.gcxySso.servlet;

import com.seeyon.apps.ext.gcxySso.handler.StringHandle;
import com.seeyon.apps.ext.gcxySso.util.OauthLoginUtil;
import com.seeyon.apps.ext.gcxySso.util.PropUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class GcxySsoServlet extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(GcxySsoServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PropUtils prop = new PropUtils();
        String clientId = prop.getSSO_ClientId();
        String oauthUrl = prop.getSSO_OAuthAuthorize();
        String redirect_uri = prop.getApplicationUrl();
        String code = request.getParameter("code");
        String loginName = "";
        if (null == code) {
            response.sendRedirect(oauthUrl + "?response_type=code&scope=read&client_id=" + clientId + "&redirect_uri=" + redirect_uri);
        } else {
            loginName = OauthLoginUtil.togetToken(code, prop);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(loginName + ":");
        sb.append(UUID.randomUUID().toString());
        String encodeloginName = StringHandle.encode(sb.toString());
        if (null != loginName && !"".equals(loginName)) {
            try {
                String servername = request.getServerName();
                int port = request.getServerPort();
                String url = "http://" + servername + ":" + port + "/seeyon/";
                response.sendRedirect(url + "login/sso?from=gcxySso&ticket=" + encodeloginName);
            } catch (IOException e) {
                log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
