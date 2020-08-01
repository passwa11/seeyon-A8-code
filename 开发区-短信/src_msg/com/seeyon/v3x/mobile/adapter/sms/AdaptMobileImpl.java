package com.seeyon.v3x.mobile.adapter.sms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.seeyon.apps.common.kit.JsonKit;
import com.seeyon.apps.util.HttpclientUtil;
import com.seeyon.apps.util.NetServiceException;
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
            result = this.send();
            System.out.println(result);
//            if (null != result) {
//                String[] arr = result.split("&");
//                String code = arr[0].split("=")[1];
//                if (code.equals("0")) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }

    /**
     * 短信发送接口
     *
     * @return
     */
    public String send() {
        //请求地址
        String url = "http://180.101.185.166:7862/sms?action=send";
        //请求参数
        String action = "send";
        String account = "922553";
        String password = "7bE8Sx";
        String mobile = "18136001664";
        String content = "【短信签名】短信内容";
        String extno = "106901553";//接入码
        String rt = "json";
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("action", action);
        requestMap.put("account", account);
        requestMap.put("password", md5Encrypt(password + extno + content + mobile)); //MD5加密
        requestMap.put("mobile", mobile);
        requestMap.put("content", content);
        requestMap.put("extno", extno);
        requestMap.put("rt", rt);
        //返回JSON字符串
        String requestResult = null;
        try {
            //发送请求
            requestResult = HttpclientUtil.post(url, requestMap, "UTF-8");
        } catch (NetServiceException e) {
            e.printStackTrace();
        }

        return requestResult;
    }

    /**
     * MD5加密
     *
     * @param signContent
     * @return
     */
    public static String md5Encrypt(String signContent) {
        String hashResult = "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(signContent.getBytes("UTF-8"));
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashResult = sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm.");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding type.");
        }

        return hashResult;
    }

    @Override
    public boolean sendMessage(Long messageId, String srcPhone, Collection<String> destPhoneList, String content) {
        return true;
    }

}
