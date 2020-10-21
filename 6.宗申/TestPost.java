package com.test;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TestPost {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String url ="http://192.168.1.88:8088/SrmCommSrv.asmx/upVender";
        CloseableHttpClient client= HttpClients.createDefault();
        HttpPost post=new HttpPost(url);
//        post.setHeader("Content-Type","application/json;charset=utf-8");
//        String params="{\"FormID\":\"QA00001\",\"NodeID\":\"09\",\"Statue\":\"同意\",\"User\":\"009608\",\"UserName\":\"信息部\",\"ApprovalTime\":\"2020-10-15 10:33:22\"}";
//        StringEntity postEntity=new StringEntity(params,"UTF-8");
        List<NameValuePair> pairs=new ArrayList<>();
        pairs.add(new BasicNameValuePair("FormID","QA00001"));
        pairs.add(new BasicNameValuePair("NodeID","09"));
        pairs.add(new BasicNameValuePair("Statue","同意"));
        pairs.add(new BasicNameValuePair("USER","009608"));
        pairs.add(new BasicNameValuePair("USERNAME","信息部"));
        UrlEncodedFormEntity encodedFormEntity=new UrlEncodedFormEntity(pairs,"utf-8");
        post.setEntity(encodedFormEntity);
        HttpResponse response=null;
        try{
            response=client.execute(post);
            response.setHeader("Cache-Control","no-cache");
            int code=response.getStatusLine().getStatusCode();
            System.out.println(code);
            if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
                String result=EntityUtils.toString(response.getEntity(),"utf-8");
                System.out.println(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
