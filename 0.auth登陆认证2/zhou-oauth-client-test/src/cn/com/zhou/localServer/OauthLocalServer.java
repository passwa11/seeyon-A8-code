package cn.com.zhou.localServer;

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

public class OauthLocalServer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clientId = "0d7888869bec4c7f862d28b9cbf05652";
        String oauthUrl = "http://localhost:8080/oauth/authorize";
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
        String client_id = "0d7888869bec4c7f862d28b9cbf05652";
        String client_secret = "3EE0fU3HhXbJcloi8N2UC713kjodWT5O";
        String url = "http://localhost:8080/oauth/token?client_id=0d7888869bec4c7f862d28b9cbf05652&client_secret=3EE0fU3HhXbJcloi8N2UC713kjodWT5O&grant_type=authorization_code&code=" + code + "&redirect_uri=http://localhost:8081/client1/local";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

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

                toGet((String) m.get("access_token"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toGet(String accessToken) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://localhost:8080/unity/user_info?access_token=" + accessToken);
        //设置post请求头
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
