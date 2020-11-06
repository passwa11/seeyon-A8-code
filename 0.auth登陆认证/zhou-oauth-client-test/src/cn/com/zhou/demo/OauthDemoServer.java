package cn.com.zhou.demo;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OauthDemoServer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clientId = "123456";
        String oauthUrl = "http://localhost:9000/oauth/authorize";
        String redirect_uri = "http://localhost:8081/client1/local";
//        String uuid = UUID.randomUUID().toString();
        String code = req.getParameter("code");
        if (null == code) {
//            resp.sendRedirect(oauthUrl + "?response_type=code&scope=read&client_id=" + clientId + "&redirect_uri=" + redirect_uri + "&state=" + uuid);
            resp.sendRedirect(oauthUrl + "?response_type=code&scope=read&client_id=" + clientId + "&redirect_uri=" + redirect_uri);
        } else {
            togetToken(code);
        }
    }


    public void togetToken(String code) {
        String client_id = "123456";
        String client_secret = "654321";
        String url = "http://localhost:9000/oauth/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        map.put("code", code);
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://localhost:8081/client1/local");

        String requestParams = "{\"code\":\"" + code + "\",\"grant_type\":\"authorization_code\",\"client_secret\":\"3EE0fU3HhXbJcloi8N2UC713kjodWT5O\",\"redirect_uri\":\"http://localhost:8081/client1/local\",\"client_id\":\"0d7888869bec4c7f862d28b9cbf05652\"}";
        System.out.println(requestParams);
        StringEntity postingString = new StringEntity(requestParams, "utf-8");
        httpPost.setEntity(postingString);
        try {
            response = client.execute(httpPost);
            response.setHeader("Cache-Control", "no-cache");
            System.out.println(response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
                System.out.println(resultString);
                Map<String, Object> m = (Map<String, Object>) JSONObject.parse(resultString);
                System.out.println(m.get("access_token"));
//                for (int i = 0; i < arr.length; i++) {
//                    String s = arr[i];
//                    if (s.contains("access_token")) {
//                        String t = s.substring(s.indexOf("=") + 1);
//                        System.out.println(t);
//                        toGet(t);
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void toGet(String accessToken) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet("https://api.github.com/user");
        get.setHeader("Content-Type", "application/json;charset=utf-8");
        //设置post请求头
        get.addHeader("Authorization", "token " + accessToken);
        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response = client.execute(get);
        response.setHeader("Cache-Control", "no-cache");
        String resultString = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
            System.out.println(resultString);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
