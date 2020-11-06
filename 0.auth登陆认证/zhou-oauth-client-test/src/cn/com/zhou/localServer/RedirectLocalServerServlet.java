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

public class RedirectLocalServerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String code = req.getParameter("code");
//        String client_id = "0d7888869bec4c7f862d28b9cbf05652";
//        String client_secret = "3EE0fU3HhXbJcloi8N2UC713kjodWT5O";
//        System.out.println(code);
//        String url = "http://localhost:8080/oauth/access_token";
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost httpPost = null;
//        HttpResponse response = null;
//        httpPost = new HttpPost(url);
//        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
//        Map<String, Object> map = new HashMap<>();
//        map.put("client_id", client_id);
//        map.put("client_secret", client_secret);
//        map.put("code", code);
//
//        String requestParams = JSONObject.toJSONString(map);
//        StringEntity postingString = new StringEntity(requestParams, "utf-8");
//        httpPost.setEntity(postingString);
//        try {
//            response = client.execute(httpPost);
//            response.setHeader("Cache-Control", "no-cache");
//            System.out.println(response.getStatusLine().getStatusCode());
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                HttpEntity entity = response.getEntity();
//                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
//                System.out.println(resultString);
////                String[] arr = resultString.split("&");
////                for (int i = 0; i < arr.length; i++) {
////                    String s = arr[i];
////                    if (s.contains("access_token")) {
////                        String t = s.substring(s.indexOf("=") + 1);
////                        System.out.println(t);
////                        toGet(t);
////                    }
////                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
