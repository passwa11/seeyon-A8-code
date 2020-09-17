package com.bpm.test;

import com.bpm.TokenUtil;
import com.seeyon.client.CTPRestClient;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SendHtml {

    public static void main(String[] args) {
        tosendHtml();
    }

    public static void tosendHtml() {
        CTPRestClient client = TokenUtil.getOARestInfo();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("appName", "collaboration");

        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("templateCode", "bpm0916");
        data1.put("draft", "0");
        data1.put("attachments", new ArrayList<Long>() {
        });
        data1.put("relateDoc", "col|-2871660587841141706,-1609894079662438907");
        data1.put("subject", "bpm集成");

        Map<String, Object> data2 = new HashMap<>();
        Map<String, Object> mainMap = new HashMap<>();
//        mainMap.put("姓名", "张三");
//        mainMap.put("地址", "徐州");
        data2.put("formmain_0482", mainMap);


        map.put("data", data1);
        JSONObject json = client.post("bpm/process/start", map, JSONObject.class);
        if (null != json) {
            if (json.getBoolean("success")) {

            }
        }
    }

}
