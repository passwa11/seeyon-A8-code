package com.test;

import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import net.sf.json.JSONObject;

/**
 * 周刘成   2019-12-26
 */
public class Demo1 {
    public static void main(String[] args) {
        receviver();
        System.out.println("Hello World!");
    }

    public static void receviver() {
        String userName = "lisi";
        String url = "http://127.0.0.1:80";//OA的登录URL
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(url);
        CTPRestClient client = clientManager.getRestClient();
        client.authenticate("rest", "rest111111");//由致远OA提供

        char[] encodeStringCharArray = userName.toCharArray();
        for (int i = 0; i < encodeStringCharArray.length; i++) {
            encodeStringCharArray[i] += 1;
        }
        String ticket = new String(java.util.Base64.getEncoder().encode(new String(encodeStringCharArray).getBytes()));
        System.out.println("ticket ： " + ticket);

        JSONObject json = client.get("/affairs/pending?ticket=" + ticket + "&memberId=-7965025864247718111&apps=1&pageSize=" + "5", JSONObject.class);

        System.out.println(json);

//        JSONObject daibanfw = client.get("/edoc/receipt/pending?ticket=" + ticket + "&memberId=-7965025864247718111", JSONObject.class);
//        if (null != daibanfw) {
//            System.out.println(daibanfw.toString());
//        }
    }
}

