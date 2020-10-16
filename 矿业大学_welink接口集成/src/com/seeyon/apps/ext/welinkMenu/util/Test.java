package com.seeyon.apps.ext.welinkMenu.util;


import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    public static void maint(String[] args) {
        String s="Member|-7818842738369737823";
        String[] _s=s.split("\\|");
        System.out.println(_s[0]);
        System.out.println(_s[1]);
    }

    public static void main6(String[] args) throws Exception{
//        DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//        LocalDateTime dateTime=new Date().toInstant().atOffset(ZoneOffset.of("+0")).toLocalDateTime();
//        String s=dateTime.format(dtf);
//        System.out.println(s);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String start="2020-04-20 10:00";
        String end="2020-04-20 11:45";
        Long s=sdf.parse(start).getTime();
        Long e=sdf.parse(end).getTime();
        System.out.println((e-s)/60000);

    }

    public static void main4(String[] args) {
        String token = WelinkUtil.getInstance().getToken("18136001664", "zlc201692");
        System.out.println(token);
        Map<String, Object> _cMap = new ConcurrentHashMap<>();
        _cMap.put("mediaTypes", "Video,Voice");
        _cMap.put("conferenceType", 0);//普通会议
        _cMap.put("startTime", "2020-04-23 17:00");
        _cMap.put("subject", "会议5");
        _cMap.put("length", 30);

        List<Map<String,String>> list=new ArrayList<>();
        Map<String,String> u1=new ConcurrentHashMap<>();
        u1.put("accountId","18652202242");
        u1.put("name","18652202242");
        u1.put("role","0");
        u1.put("phone","18652202242");
        u1.put("isMute","0");
        list.add(u1);
        _cMap.put("attendees", list);

        String create = WelinkUtil.getInstance().welinkCreateMeeting(token, _cMap);
        System.out.println(create);
    }

    public static void main2(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//        String f=dateTimeFormatter.format(localDateTime);
        String s = LocalDateTime.now(ZoneOffset.of("+8")).format(dateTimeFormatter);
        System.out.println(s);
    }

    public static void maineee(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String url = "https://api.meeting.huaweicloud.com/v1/usg/acs/auth/proxy";

        Map<String, Object> map = new HashMap<>();
        map.put("account", "18136001664");
        map.put("clientType", "18");
        map.put("createTokenType", "0");
        map.put("pwd", "zlc201692");
        map.put("authType", "AccountAndPwd");
        map.put("authServerType", "workplace");
        Map<String, String> headers = new HashMap<>();
        headers.put("Context-Type", "application/json;charset=utf-8");
        JSONObject json = new JSONObject(map);
        String result = HttpUtilTool.getInstance().toHttpsPost(url, json.toJSONString(), headers);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String token = (String) jsonObject.get("accessToken");
        System.out.println(token);
//        String crecateMeeting="https://api.meeting.huaweicloud.com/v1/mmc/management/conferences";
//        Map<String,String> _cHeaders=new HashMap<>();
//        _cHeaders.put("X-Auth-Token",token);
//        _cHeaders.put("Context-Type", "application/json;charset=utf-8");
//
//
//        Map<String,Object> _cMap=new HashMap<>();
//        _cMap.put("mediaTypes","Video,Voice");
//        _cMap.put("startTime","2020-04-22 17:00");
//        _cMap.put("subject","会议4");
//        _cMap.put("length",30);

//        JSONObject _cjson=new JSONObject(_cMap);
//        String _cResult =HttpUtilTool.getInstance().toHttpsPost(crecateMeeting, _cjson.toJSONString(), _cHeaders);
//        System.out.println(_cResult);
//        met4(token);
        met(token);

    }

    public static void met4(String token) {
        String crecateMeeting = "https://api.meeting.huaweicloud.com/v1/mmc/management/conferences?conferenceID=989454795";
        Map<String, String> _cHeaders = new HashMap<>();
        _cHeaders.put("X-Auth-Token", token);
//        _cHeaders.put("Context-Type", "application/json;charset=utf-8");


        Map<String, Object> _cMap = new HashMap<>();
        _cMap.put("mediaTypes", "Video,Voice");
        _cMap.put("startTime", "2020-04-22 22:00");
        _cMap.put("subject", "会议4-----");
        _cMap.put("length", 30);

        JSONObject _cjson = new JSONObject(_cMap);
        String _cResult = HttpUtilTool.getInstance().toHttpsPut(crecateMeeting, _cjson.toJSONString(), _cHeaders);
        System.out.println(_cResult);
    }

    public static void met(String token) {
        String delMeeting = "https://api.meeting.huaweicloud.com//v1/mmc/management/conferences?conferenceID=987746463";
        Map<String, String> _cHeaders = new HashMap<>();
        _cHeaders.put("X-Auth-Token", token);
        String _cResult = HttpUtilTool.getInstance().toHttpsDelete(delMeeting, _cHeaders);
        System.out.println(_cResult);
    }
}
