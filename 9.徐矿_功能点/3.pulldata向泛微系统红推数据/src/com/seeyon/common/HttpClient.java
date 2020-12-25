package com.seeyon.common;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 发送Get请求：HttpResponse httpGet(String url,Map<String,String> headers,String encode)
 *发送Post请求，同表单Post提交：HttpResponse httpPostForm(String url,Map<String,String> params, Map<String,String> headers,String encode)
 *发送Post Raw请求：HttpResponse httpPostRaw(String url,String stringJson,Map<String,String> headers, String encode)
 *发送Put Raw请求：HttpResponse httpPutRaw(String url,String stringJson,Map<String,String> headers, String encode)
 *发送Delete请求：HttpResponse httpDelete(String url,Map<String,String> headers,String encode)
 */
public class HttpClient {

    /**
     * 发送http get请求
     */
    public static String httpGet(String url,Map<String,String> headers,String encode){

        if(encode == null){
            encode = "utf-8";
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        String content = null;
        //since 4.3 不再使用 DefaultHttpClient
        try {
            closeableHttpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(url);
            //设置header
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(),entry.getValue());
                }
            }

            httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {  //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    /**
     * 发送 http post 请求，参数以form表单键值对的形式提交。
     */
    public static String httpPostForm(String url,Map<String,String> params, Map<String,String> headers,String encode){

        if(encode == null){
            encode = "utf-8";
        }

        String content = null;
        CloseableHttpResponse  httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        try {

            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpost = new HttpPost(url);

            //设置header
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpost.setHeader(entry.getKey(),entry.getValue());
                }
            }
            //组织请求参数
            List<NameValuePair> paramList = new ArrayList <NameValuePair>();
            if(params != null && params.size() > 0){
                Set<String> keySet = params.keySet();
                for(String key : keySet) {
                    paramList.add(new BasicNameValuePair(key, params.get(key)));
                }
            }
                httpost.setEntity(new UrlEncodedFormEntity(paramList, encode));


            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {  //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 发送 http post 请求，参数以原生字符串进行提交
     * @param url
     * @param encode
     * @return
     */
    public static String httpPostRaw(String url,String stringJson,Map<String,String> headers, String encode){
        if(encode == null){
            encode = "utf-8";
        }
        String content = null;
        CloseableHttpResponse  httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        try {

            //HttpClients.createDefault()等价于 HttpClientBuilder.create().build();
            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpost = new HttpPost(url);

            //设置header
            httpost.setHeader("Content-type", "application/json");
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpost.setHeader(entry.getKey(),entry.getValue());
                }
            }
            //组织请求参数
            StringEntity stringEntity = new StringEntity(stringJson, encode);
            httpost.setEntity(stringEntity);


            //响应信息
            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {  //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 发送 http put 请求，参数以原生字符串进行提交
     * @param url
     * @param encode
     * @return
     */
    public static String httpPutRaw(String url,String stringJson,Map<String,String> headers, String encode){
        if(encode == null){
            encode = "utf-8";
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        String content = null;
        //since 4.3 不再使用 DefaultHttpClient
        try {

        //HttpClients.createDefault()等价于 HttpClientBuilder.create().build();
            closeableHttpClient = HttpClients.createDefault();
            HttpPut httpput = new HttpPut(url);

            //设置header
            httpput.setHeader("Content-type", "application/json");
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpput.setHeader(entry.getKey(),entry.getValue());
                }
            }
            //组织请求参数
            StringEntity stringEntity = new StringEntity(stringJson, encode);
            httpput.setEntity(stringEntity);
            //响应信息
            httpResponse = closeableHttpClient.execute(httpput);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            closeableHttpClient.close();  //关闭连接、释放资源
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    /**
     * 发送http delete请求
     */
    public static String httpDelete(String url,Map<String,String> headers,String encode){
        if(encode == null){
            encode = "utf-8";
        }
        String content = null;
        CloseableHttpResponse  httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        try {
            //since 4.3 不再使用 DefaultHttpClient
           closeableHttpClient = HttpClientBuilder.create().build();
            HttpDelete httpdelete = new HttpDelete(url);
            //设置header
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpdelete.setHeader(entry.getKey(),entry.getValue());
                }
            }

            httpResponse = closeableHttpClient.execute(httpdelete);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {   //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 发送 http post 请求，支持文件上传
     */
    public static String httpPostFormMultipart(String url,Map<String,String> params, List<File> files,Map<String,String> headers,String encode){
        if(encode == null){
            encode = "utf-8";
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient closeableHttpClient = null;
        String content = null;
        //since 4.3 不再使用 DefaultHttpClient
        try {

            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpost = new HttpPost(url);

            //设置header
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpost.setHeader(entry.getKey(),entry.getValue());
                }
            }
            MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
            mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            mEntityBuilder.setCharset(Charset.forName(encode));

            // 普通参数
            ContentType contentType = ContentType.create("text/plain",Charset.forName(encode));//解决中文乱码
            if (params != null && params.size() > 0) {
                Set<String> keySet = params.keySet();
                for (String key : keySet) {
                    mEntityBuilder.addTextBody(key, params.get(key),contentType);
                }
            }
            //二进制参数
            if (files != null && files.size() > 0) {
                for (File file : files) {
                    mEntityBuilder.addBinaryBody("file", file);
                }
            }
            httpost.setEntity(mEntityBuilder.build());
            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {  //关闭连接、释放资源
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }





}