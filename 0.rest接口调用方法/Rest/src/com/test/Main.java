package com.test;

import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
//import net.sf.json.JSONObject;

public class Main {

    public static void main(String[] args) {
        receviver();
        System.out.println("Hello World!");
    }

    public static void receviver() {
//        http://oa.xzhmu.edu.cn/seeyon/rest/affairs/pending/code/999992016998?ticket=999992016998
        String userName = "zhangsan";
        String url = "http://oa.xzhmu.edu.cn";//OA的登录URL
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(url);
        CTPRestClient client = clientManager.getRestClient();
//        client.authenticate("gw", "gw111111");//由致远OA提供
        client.authenticate("rest", "rest123456");//由致远OA提供

        char[] encodeStringCharArray = userName.toCharArray();
        for (int i = 0; i < encodeStringCharArray.length; i++) {
            encodeStringCharArray[i] += 1;
        }
        String ticket = new String(java.util.Base64.getEncoder().encode(new String(encodeStringCharArray).getBytes()));
        System.out.println("ticket ： " + ticket);

//        String json = client.get("/affairs/pending/code/"+userName+"?ticket=" + ticket, String.class);
//        System.out.println("协同的数据：");
//        System.out.println(json);

        //待办发文 /edoc/receipt/pending?ticket=xxxxxx
        String ybfw=client.get("/edoc/receipt/done?ticket=" + ticket, String.class);
        System.out.println("待办发文:");
        System.out.println(ybfw);


    }
}
