package com.seeyon.apps.ext.oauthLogin.servlet;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.oauthLogin.sso.StringHandle;
import com.seeyon.apps.ext.oauthLogin.util.OauthLoginUtil;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.ctp.util.JDBCAgent;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public class KfqSsoLogin extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(KfqSsoLogin.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        String encodeloginName = StringHandle.encode(loginName);
        if (null != loginName && !"".equals(loginName)) {
            try {
                String servername = request.getServerName();
                int port = request.getServerPort();
                String url = "http://" + servername + ":" + port + "/seeyon/";
                response.sendRedirect(url + "login/sso?from=xkSso&ticket=" + encodeloginName);
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
