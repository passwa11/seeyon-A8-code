package com.seeyon.v3x.mobile.adapter.sms;

import java.io.*;

import java.util.Collection;

import java.util.List;
import java.util.Properties;

import com.alibaba.fastjson.JSON;
import com.linkage.netmsg.NetMsgclient;
import com.linkage.netmsg.server.ReceiveMsg;
import com.seeyon.apps.common.kit.JsonKit;
import com.seeyon.apps.util.PropertiesUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.seeyon.v3x.mobile.adapter.AdapterMobileMessageManger;
import com.seeyon.v3x.mobile.message.domain.MobileReciver;
import sun.misc.BASE64Encoder;

/**
 * Description
 *
 * <pre>
 * 短信插件
 * </pre>
 *
 * @author FanGaowei<br>
 * Date 2018年3月9日 下午4:12:33<br>
 * MessagePipeline Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class AdaptMobileImpl implements AdapterMobileMessageManger {

    private static Log log = LogFactory.getLog(AdaptMobileImpl.class);

    private String account;
    private String pwd;
    private String url;

    protected Properties global = new Properties();

    protected Properties props = new Properties();

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // 短信网关名称
    @Override
    public String getName() {
        return "短信插件";
    }

    // 检查适配器是否适用
    public boolean isAvailability() {
        return true;
    }

    // 是否支持群发
    @Override
    public boolean isSupportQueueSend() {
        return false;
    }

    // 是否支持收短信
    @Override
    public boolean isSupportRecive() {
        return false;
    }

    // 从手机端返回协同平台
    @Override
    public List<MobileReciver> recive() {
        return null;
    }

    protected void init(String globalName, String fullName) throws Exception {
        InputStream in = new FileInputStream(fullName);
        props.load(in);
        InputStream globalin = new FileInputStream(globalName);
        global.load(globalin);
    }

    @Override
    public boolean sendMessage(Long messageId, String srcPhone, String destPhone, String content) {
        NetMsgclient client = new NetMsgclient();
        /*ReceiveMsgImpl为ReceiveMsg类的子类，构造时，构造自己继承的子类就行*/
        ReceiveMsg receiveMsg = new QxtReceive();
        /*初始化参数*/
        PropertiesUtil propertiesUtil=new PropertiesUtil();
        String url = propertiesUtil.getUrl();
        Integer port = propertiesUtil.getPort();
        String account = propertiesUtil.getLoginName();
        String password = propertiesUtil.getPassword();
        client = client.initParameters(url, port, account, password, receiveMsg);
        try {
            /*登录认证*/
            boolean isLogin = client.anthenMsg(client);
            if (isLogin) {
                /*发送下行短信*/
                String result = client.sendMsg(client, 0, destPhone, content, 1);
                return true;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean sendMessage(Long messageId, String srcPhone, Collection<String> destPhoneList, String content) {
        return true;
    }

}
