package cn.com.zhou.git;

import com.alibaba.fastjson.JSON;
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

public class RedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String client_id = "25e28dfa01eba790364c";
        String client_secret = "7335c3024f350f6bef3be48c78742d2b5e6aec58";
        System.out.println(code);
        String url = "https://github.com/login/oauth/access_token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>();
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        map.put("code", code);

        String requestParams = JSONObject.toJSONString(map);
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
                String[] arr = resultString.split("&");
                for (int i = 0; i < arr.length; i++) {
                    String s = arr[i];
                    if (s.contains("access_token")) {
                        String t = s.substring(s.indexOf("=") + 1);
                        System.out.println(t);
                        toGet(t);
                    }
                }
//                JSONObject jsonObject = JSON.parseObject(resultString);
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
