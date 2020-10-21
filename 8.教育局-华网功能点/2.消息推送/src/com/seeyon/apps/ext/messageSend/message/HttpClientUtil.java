package com.seeyon.apps.ext.messageSend.message;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpClientUtil {
    static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static void toPost(Map<String,Object> map) {
        String url = "http://app.xzzhxy.cn/schoolemon/sysmsg/sendjson";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);


            String json = JSONObject.toJSONString(map);

            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");

            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = client.execute(httpPost);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                String s = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                System.out.println(s);
            }

        } catch (Throwable e) {
            logger.error("集成金智消息推送接口出错了！" + e.getMessage());
        }
    }

}
