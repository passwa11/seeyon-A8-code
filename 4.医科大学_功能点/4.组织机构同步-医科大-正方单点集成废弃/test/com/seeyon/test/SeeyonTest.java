package com.seeyon.test;

import com.seeyon.apps.ext.zxzyk.util.SyncConnectionUtil;
import com.seeyon.client.CTPRestClient;

/**
 * 周刘成   2019-8-26
 */
public class SeeyonTest {
    public static void main(String[] args) {
        receviver();
    }

    public static void receviver() {
        String userName = "zhangsan";
        CTPRestClient client = SyncConnectionUtil.getOaRest();

        char[] encodeStringCharArray = userName.toCharArray();
        for (int i = 0; i < encodeStringCharArray.length; i++) {
            encodeStringCharArray[i] += 1;
        }
        String ticket = new String(java.util.Base64.getEncoder().encode(new String(encodeStringCharArray).getBytes()));
        System.out.println("ticket ： " + ticket);

        String json = client.get("/affairs/pending?ticket=" + ticket + "&memberId=7486210341116071885&apps=1&pageSize=" + "5", String.class);
    }
}
