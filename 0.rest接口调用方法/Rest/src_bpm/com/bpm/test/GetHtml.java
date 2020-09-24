package com.bpm.test;

import com.alibaba.fastjson.JSONObject;
import com.bpm.TokenUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class GetHtml {

    public static void main(String[] args) {
//        System.out.println(TokenUtil.getToken());
        String token =TokenUtil.getToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        try {
            String url="http://127.0.0.1:80/seeyon/rest/form/export/4386607491941417409/-3394461689692140820";
            httpGet = new HttpGet(url);
            httpGet.setHeader("token", token);
            httpResponse = httpClient.execute(httpGet);
            System.out.println("------:"+httpResponse.getStatusLine().getStatusCode() );
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString(httpEntity);
                System.out.println(result);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
