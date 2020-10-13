package com.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenUtil {

    public static String getToken() {
        String token = "";
        String url = "http://127.0.0.1:80/seeyon/rest/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        String requestParams = "{\"userName\":\"rest\",\"password\":\"55ffa403-230c-48c5-b74c-9d29fd100542\"}";
        StringEntity postingString = new StringEntity(requestParams, "utf-8");
        httpPost.setEntity(postingString);
        try {
            response = client.execute(httpPost);
            response.setHeader("Cache-Control", "no-cache");
            System.out.println(response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
                JSONObject jsonObject = JSON.parseObject(resultString);
                token = (String) jsonObject.get("id");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    public static String doPost(String name,String pwd,String token) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://127.0.0.1:80/seeyon/rest/verifyLogin/login");
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", name);
        String pairs=JSON.toJSONString(map);
        StringEntity formEntity = new StringEntity(pairs, "UTF-8");
        post.setHeader("Content-Type", "application/json;charset=utf-8");
        //设置post请求头
        post.addHeader("token",token);
        post.setEntity(formEntity);
        // 使用HttpClient发起请求，返回response
        CloseableHttpResponse response = client.execute(post);
        response.setHeader("Cache-Control", "no-cache");
        String resultString = "";
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
        }

        return resultString;
    }

}
