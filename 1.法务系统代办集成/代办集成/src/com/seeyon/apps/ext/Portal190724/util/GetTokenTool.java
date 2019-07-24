package com.seeyon.apps.ext.Portal190724.util;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Portal190724.pojo.Result;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;

/**
 * 周刘成   2019/7/24
 */
public class GetTokenTool {

    private ReadConfigTools configTools;
    private String appKey;
    private String secretKey;
    private String getTokenUrl;

    public GetTokenTool() {
        configTools = new ReadConfigTools();
        appKey = configTools.getString("portlet.appKey");
        secretKey = configTools.getString("portlet.secretKey");
        getTokenUrl = configTools.getString("portlet.gettokenz.path");
    }

    public Map<String, Object> checkToken() {

        Long currentUserId = CurrentUser.get().getId();
        String Timespan = String.valueOf(new Date().getTime() / 1000);

        Map<String, String> getTokenMap = new HashMap<String, String>();
        getTokenMap.put("AppKey", getAppKey());
        getTokenMap.put("SecretKey", getSecretKey());
        getTokenMap.put("Timestamp", Timespan);
        String token=getTokenByPost(getTokenUrl,getTokenMap);
        return null;
    }

    /**
     * 获取token url是接口地址
     */
    public String getTokenByPost(String url, Map<String, String> map) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        String token = null;
        try {
            httpPost = new HttpPost(url);
            if (null != map) {
                List<NameValuePair> pairs = new ArrayList<>();
                for (String key : map.keySet()) {
                    pairs.add(new BasicNameValuePair(key, map.get(key)));
                }
                //模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
                httpPost.setEntity(entity);
            }
            response = client.execute(httpPost);
            response.setHeader("Cache-Control", "no-cache");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //成功后返回的结果：
                //{"result":{"expires_in":"3600","token":"91DD1057B385BA0B8AF5E3107857F940"},"status":200,"message":"成功"}
                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
                Result result = JSONObject.parseObject(resultString, Result.class);
                token = result.getResultInfo().getToken();
            } else {
                System.out.println("post请求出错了，无法获取token。。。。。。。。");
            }

        } catch (Exception e) {
            System.out.println("获取token出错了：" + e.getMessage());
        }
        return token;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getGetTokenUrl() {
        return getTokenUrl;
    }

    public void setGetTokenUrl(String getTokenUrl) {
        this.getTokenUrl = getTokenUrl;
    }
}
