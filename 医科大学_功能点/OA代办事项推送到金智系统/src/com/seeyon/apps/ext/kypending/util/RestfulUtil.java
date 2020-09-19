package com.seeyon.apps.ext.kypending.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @Author glhan
 * @Date��2020/1/16 10:08
 */
public class RestfulUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulUtil.class);

    /**
     * application/x-www-form-urlencoded
     *
     * @param info
     * @param params
     * @return
     */
    public static String post(RestfulInfo info, List<? extends NameValuePair> params) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(info.getUrl());
        httppost.setHeader("appId", info.getAppId());
        httppost.setHeader("accessToken", info.getAccessToken());
        UrlEncodedFormEntity uefEntity;
        StringBuilder sb = new StringBuilder();
        try {
            if (params != null) {
                uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
                httppost.setEntity(uefEntity);
            }
            CloseableHttpResponse response = httpclient.execute(httppost);
            BufferedReader in = null;
            try {
                HttpEntity entity = response.getEntity();
                in = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                String str = null;
                while ((str = in.readLine()) != null) {
                    sb.append(str);
                }
            } finally {
                response.close();
                in.close();
            }
        } catch (Exception e) {
            LOGGER.error("http请求异常", e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
