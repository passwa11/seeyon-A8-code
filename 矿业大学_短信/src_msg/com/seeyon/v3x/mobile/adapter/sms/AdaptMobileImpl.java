package com.seeyon.v3x.mobile.adapter.sms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import java.util.List;
import java.util.Properties;

import com.alibaba.fastjson.JSON;
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
        HttpClient httpclient = new HttpClient();

        try {
            String result = "";
            PostMethod post = new PostMethod(PropertiesUtil.getUrl());
            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gbk");
            post.addParameter("SpCode", PropertiesUtil.getSpCode());
            post.addParameter("LoginName", PropertiesUtil.getLoginName());
            post.addParameter("Password", PropertiesUtil.getPassword());
//            "您好%s您有%s条待办事项%s请及时处理%sOA%s";
            String msg=String.format(PropertiesUtil.getTemplate(),"!","1", ",","[","]");
            post.addParameter("MessageContent", msg);
//            post.addParameter("MessageContent", String.format(PropertiesUtil.getTemplate(), ((int)((Math.random()*9+1)*100000))+""));
            post.addParameter("UserNumber", destPhone);
            post.addParameter("SerialNumber", "");
            post.addParameter("ScheduleTime", "");
            post.addParameter("ExtendAccessNum", "");
            post.addParameter("f", PropertiesUtil.getF());
            httpclient.executeMethod(post);
            result = new String(post.getResponseBody(), "gbk");
            if (null != result) {
                String[] arr = result.split("&");
                String code = arr[0].split("=")[1];
                if (code.equals("0")) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }


    @Override
    public boolean sendMessage(Long messageId, String srcPhone, Collection<String> destPhoneList, String content) {
        return true;
    }

}
