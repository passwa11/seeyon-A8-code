package com.seeyon.apps.ext.kypending.message;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.kypending.util.MD5Util;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.usermessage.pipeline.Message;
import com.seeyon.ctp.common.usermessage.pipeline.MessagePipeline;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YkdMessagePipeline implements MessagePipeline {

    Logger logger = LoggerFactory.getLogger(YkdMessagePipeline.class);

    @Override
    public void invoke(Message[] messages) {
//        处理消息
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost method = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        String url = "http://apis.xzhmu.edu.cn/mp_message_pocket_web-mp-restful-message-send/ProxyService/message_pocket_web-mp-restful-message-sendProxyService";
        String accessToken = "bb5133cadb3b7be7bfa8618f8e2c0e44";
        String appId = "amp";
        String schoolCode = "10313";
        for (Message message : messages) {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("appId", "amp");
                map.put("subject", "消息发送");
                map.put("content", message.getContent());
                map.put("sendType", 0);
                map.put("sendNow", 1);//发送方式，是否立即发送，0:定时发送 1:立即发送 默认1
                map.put("tagId", "9007");
                Map<String, Object> receivers = new HashMap<>();
                receivers.put("userId", message.getReceiverMember().getLoginName());

                map.put("receivers", receivers);
                map.put("schoolCode", schoolCode);
                String ch = accessToken + schoolCode + message.getReceiverMember().getLoginName();
                map.put("sign", MD5Util.getMD5Str(ch));

                String josn = JSONObject.toJSONString(map);
                StringEntity entity = new StringEntity(josn, "utf-8");// 解决中文乱码问题
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");

                // 获取调用方法
                method = new HttpPost(url);
                method.setEntity(entity);
                method.setHeader("appId", appId);
                method.setHeader("accessToken", accessToken);

                httpResponse = httpClient.execute(method);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String r = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                    jsonObject = JSONObject.parseObject(r);
                }
            } catch (Throwable e) {
                logger.error("集成金智消息推送接口出错了！" + e.getMessage());
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public int getSortId() {
        return 6;
    }

    @Override
    public String getName() {
        return "ykd";
    }

    @Override
    public String getShowName() {
        return null;
    }

    @Override
    public boolean isAvailability() {
        return SystemEnvironment.hasPlugin("rtx");
    }

    @Override
    public boolean isShowSetting() {
        return false;
    }

    @Override
    public String isAllowSetting(User user) {
        return null;
    }

    @Override
    public List<Integer> getAllowSettingCategory(User user) {
        return null;
    }

    @Override
    public boolean isDefaultSend() {
        return false;
    }
}
