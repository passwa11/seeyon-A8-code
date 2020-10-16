package com.sms;

import com.linkage.netmsg.NetMsgclient;
import com.linkage.netmsg.server.ReceiveMsg;

public class Demo {
    public static void main(String[] args) {
        NetMsgclient client = new NetMsgclient();
        /*ReceiveMsgImpl为ReceiveMsg类的子类，构造时，构造自己继承的子类就行*/
        ReceiveMsg receiveMsg = new ReciverDemo();
        /*初始化参数*/
        client = client.initParameters("218.94.58.243", 9002, "account", "password", receiveMsg);
        client.setFileLog("run.log", 2);
        try {
            /*登录认证*/
            boolean isLogin = client.anthenMsg(client);
            if (isLogin) {
                System.out.println("login sucess");
                /*发送下行短信*/
                String result[] = client.sendMsg(client, 8, "106590250001", "18136001664", "test thread ", 0);
                for (int j = 0; result != null && j < result.length; j++) {
                    System.out.println("send [" + j + "] ---- " + result[j]);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

}
