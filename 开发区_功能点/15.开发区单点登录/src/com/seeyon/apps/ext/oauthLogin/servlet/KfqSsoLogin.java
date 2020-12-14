package com.seeyon.apps.ext.oauthLogin.servlet;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.oauthLogin.sso.StringHandle;
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
            loginName = togetToken(code, prop);
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

    public String togetToken(String code, PropUtils prop) {
        String loginName = "";
        String client_id = prop.getSSO_ClientId();
        String client_secret = prop.getSSO_ClientSecret();
        StringBuffer sb = new StringBuffer();
        sb.append(prop.getSSO_OAuthAccess_token());
        sb.append("?client_id=" + client_id);
        sb.append("&client_secret=" + client_secret);
        sb.append("&grant_type=authorization_code");
        sb.append("&code=" + code);
        sb.append("&redirect_uri=" + prop.getApplicationUrl());
        String url = sb.toString();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

        try {
            response = client.execute(httpPost);
            response.setHeader("Cache-Control", "no-cache");
//            System.out.println(response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
                Map<String, Object> m = (Map<String, Object>) JSONObject.parse(resultString);
                //zhou:新加记录获取到的token
//                String saveToken = "insert into temp_token(id,token) values(?,?)";
//                Connection conn = null;
//                PreparedStatement ps = null;
//                try {
//                    conn = JDBCAgent.getRawConnection();
//                    ps = conn.prepareStatement(saveToken);
//                    ps.setLong(1, System.currentTimeMillis());
//                    ps.setString(2, (String) m.get("access_token"));
//                    ps.executeUpdate();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    ps.close();
//                    conn.close();
//                }

                loginName = toGet((String) m.get("access_token"), prop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginName;
    }

    public String toGet(String accessToken, PropUtils propUtils) throws IOException {
        String userName = "";
        CloseableHttpClient client = HttpClients.createDefault();
        StringBuffer sb = new StringBuffer();
        sb.append(propUtils.getSSO_UserInfo());
        sb.append("?access_token=" + accessToken);
        HttpGet get = new HttpGet(sb.toString());
        //设置post请求头
        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response = client.execute(get);
        response.setHeader("Cache-Control", "no-cache");
        String resultString = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
            Map<String, Object> map = (Map<String, Object>) JSONObject.parse(resultString);
            userName = (String) map.get("username");
        }
        return userName;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

}
