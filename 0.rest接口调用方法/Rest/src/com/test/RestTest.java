package com.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 周刘成   2019-9-6
 */
public class RestTest {


    public static String getToken() {
        String token = "";
        String url = "http://127.0.0.1:80/seeyon/rest/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
//        String requestParams = "{\"userName\":\"rest\",\"password\":\"rest111111\"}";
        String requestParams = "{\"userName\":\"rest\",\"password\":\"5e9172a7-a3bc-4d89-94ec-601bcef6befc\"}";
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

    public static void testInterface() {
        String token = getToken();
        String urlpath = "http://127.0.0.1:81/seeyon/rest/affairs/pending/code/zhangsan?ticket=zhangsan&token=" + token;
        URL url = null;
        try {
            url = new URL(urlpath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {// 循环读取流
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            System.out.println(sb.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void testRestGet() {
        String token = getToken();
//        http://127.0.0.1/seeyon/rest/affairs/done?memberId={xxxx}
//        已办协同合一了
        String urlpath = "http://127.0.0.1:80/seeyon/rest/affairs/pending?memberId=-7231816252026829543&token=" + token;
//        String urlpath = "http://127.0.0.1:80/seeyon/rest/affairs/done?memberId=-7231816252026829543&token=" + token;
//        String urlpath = "http://127.0.0.1:80/seeyon/rest/orgMember/?loginName=zhangsan&token=" + token;
        CloseableHttpClient client = HttpClients.createDefault();
        RequestConfig.Builder requestConfig = RequestConfig.custom();
        //设置连接超时时间
        requestConfig.setConnectTimeout(5000);
        // 设置请求超时时间
        requestConfig.setConnectionRequestTimeout(5000);
        requestConfig.setSocketTimeout(5000);
        //默认允许自动重定向
        requestConfig.setRedirectsEnabled(true);
        HttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(urlpath);
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("HTTP请求未成功！HTTP Status Code:" + response.getStatusLine());
            }
            HttpEntity httpEntity = response.getEntity();
            if (null != httpEntity) {
                String responseContent = EntityUtils.toString(httpEntity, "UTF-8");
                //释放资源
                EntityUtils.consume(httpEntity);
                System.out.println("响应内容：" + responseContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        /**
         * 使用java自带的url方式访问
         */
//        testInterface();
        /**
         * 使用httpclient 包中的httpGet请求
         */
        testRestGet();
    }
}
