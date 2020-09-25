package com.bpm.test;

import com.bpm.TokenUtil;
import com.seeyon.client.CTPRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SendHtml {

    public static void main(String[] args) {
        tosendHtml();
//        String token =TokenUtil.getToken();
//        System.out.println(token);
    }

    public static void tosendHtml() {
        CTPRestClient client = TokenUtil.getOARestInfo();

        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("templateCode", "bpm0916");
        data1.put("senderLoginName", "yanyi");
        data1.put("subject", "bpm集成测试思思");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("文本1", "22");
        dataMap.put("文本2", "333333");
        data1.put("data", com.alibaba.fastjson.JSONObject.toJSONString(dataMap));
        data1.put("param", "0");
        data1.put("transfertype", "json");
        String result = client.post("/flow/bpm0916", data1, String.class);
        System.out.println(result);
    }

}
