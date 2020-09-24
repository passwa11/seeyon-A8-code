package com.bpm.test;

import com.alibaba.fastjson.JSONObject;
import com.bpm.TokenUtil;
import com.seeyon.client.CTPRestClient;

import java.util.HashMap;
import java.util.Map;

public class demo {
    public static void main(String[] args) {
        toSend();
    }

    public static void toSend() {
        CTPRestClient client = TokenUtil.getOARestInfo();
        String token = TokenUtil.getToken();
//        String data = "{\"field0001\":\"tom\",\"field0002\":\"lili\"}";
        Map<String,Object> d1=new HashMap<>();
        d1.put("field0001","qq");
        d1.put("field0002","www");
        Map<String, Object> map = new HashMap<>();
        map.put("templateCode", "bpm0916");
        map.put("token", token);
        map.put("senderLoginName", "yanyi");
        map.put("subject", "测试BPM");
        map.put("data", JSONObject.toJSONString(d1));

        String json = client.post("flow/bpm0916", map, String.class);
        System.out.println(json);
    }
}
