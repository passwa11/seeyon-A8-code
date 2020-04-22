package com.seeyon.apps.ext.welinkMenu.util;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WelinkUtil {

    private static WelinkUtil welink;

    public static WelinkUtil getInstance() {
        if (null == welink) {
            welink = new WelinkUtil();
        }
        return welink;
    }

    public WelinkUtil() {
    }

    public String getToken(String loginname, String pwd) {
        String url = "https://api.meeting.huaweicloud.com/v1/usg/acs/auth/proxy";
        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("account", loginname);//18136001664
        map.put("clientType", "18");
        map.put("createTokenType", "0");
        map.put("pwd", pwd);//zlc201692
        map.put("authType", "AccountAndPwd");
        map.put("authServerType", "workplace");
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("Context-Type", "application/json;charset=utf-8");
        JSONObject json = new JSONObject(map);
        String result = null;
        try {
            result = HttpUtilTool.getInstance().toHttpsPost(url, json.toJSONString(), headers);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String token = (String) jsonObject.get("accessToken");
        return token;
    }

    public String welinkDeleteMeeting(String token, String conferenceID) {
        String delMeeting = "https://api.meeting.huaweicloud.com//v1/mmc/management/conferences?conferenceID=" + conferenceID;
        Map<String, String> _cHeaders = new ConcurrentHashMap<>();
        _cHeaders.put("X-Auth-Token", token);
        String _cResult = HttpUtilTool.getInstance().toHttpsDelete(delMeeting, _cHeaders);
        return _cResult;
    }

    public String welinkCreateMeeting(String token, Map<String, Object> body) {
        String crecateMeeting = "https://api.meeting.huaweicloud.com/v1/mmc/management/conferences";
        Map<String, String> _cHeaders = new ConcurrentHashMap<>();
        _cHeaders.put("X-Auth-Token", token);
        _cHeaders.put("Context-Type", "application/json;charset=utf-8");
//        Map<String, Object> _cMap = new ConcurrentHashMap<>();
//        _cMap.put("mediaTypes", "Video,Voice");
//        _cMap.put("startTime", "2020-04-22 17:00");
//        _cMap.put("subject", "会议4");
//        _cMap.put("length", 30);

        JSONObject _cjson = new JSONObject(body);
        String _cResult = null;
        try {
            _cResult = HttpUtilTool.getInstance().toHttpsPost(crecateMeeting, _cjson.toJSONString(), _cHeaders);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _cResult;
    }

    public String welinkUpdateMeeting(String token, String conferenceID, Map<String, Object> body) {
        String crecateMeeting = "https://api.meeting.huaweicloud.com/v1/mmc/management/conferences?conferenceID=" + conferenceID;
        Map<String, String> _cHeaders = new ConcurrentHashMap<>();
        _cHeaders.put("X-Auth-Token", token);
//        _cHeaders.put("Context-Type", "application/json;charset=utf-8");
//        Map<String, Object> _cMap=new ConcurrentHashMap<>();
//        _cMap.put("mediaTypes","Video,Voice");
//        _cMap.put("startTime","2020-04-22 22:00");
//        _cMap.put("subject","会议4-----");
//        _cMap.put("length",30);
        JSONObject _cjson = new JSONObject(body);
        String _cResult = HttpUtilTool.getInstance().toHttpsPut(crecateMeeting, _cjson.toJSONString(), _cHeaders);
        System.out.println(_cResult);
        return _cResult;
    }

}
