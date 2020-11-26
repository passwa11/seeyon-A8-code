package com.seeyon.apps.ext.kypending.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class RestfulUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulUtil.class);


    /**
     * 使用https协议
     */
    public static String post(RestfulInfo info, List<? extends NameValuePair> params) {
        HttpPost httppost = new HttpPost(info.getUrl());
        httppost.setHeader("appId", info.getAppId());
        httppost.setHeader("accessToken", info.getAccessToken());
        UrlEncodedFormEntity uefEntity;
        StringBuilder sb = new StringBuilder();
        try (CloseableHttpClient httpclient = createHttpClient();) {
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
        }
        return sb.toString();
    }

    //避免需要证书
    private static CloseableHttpClient createHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authtyep) -> true).build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, null, null, new NoopHostnameVerifier());
        return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }

    /**
     * application/x-www-form-urlencoded
     *
     * @param info
     * @param params
     * @return
     */
    public static String httpPost(RestfulInfo info, List<? extends NameValuePair> params) {
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
