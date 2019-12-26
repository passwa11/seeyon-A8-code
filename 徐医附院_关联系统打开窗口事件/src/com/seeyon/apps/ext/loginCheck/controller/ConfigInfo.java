package com.seeyon.apps.ext.loginCheck.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019-12-4
 */
public class ConfigInfo {
    public static String ipAddress;
    public static String yuanwaiNet;
    public static String gongwangNet;

    public static String neiLinkId;
    public static String yuanLinkId;
    public static String gongLinkId;


    private static ConfigInfo ConfigInfo;

    public static ConfigInfo getInstance() {
        if (null == ConfigInfo) {
            return ConfigInfo = new ConfigInfo();
        }
        return null;
    }

    static {
        InputStream in = ConfigInfo.class.getClassLoader().getResourceAsStream("config/ipConfig.properties");
        Properties prop = new Properties();
        try {
            prop.load(in);
            ipAddress = prop.getProperty("xyfy.ip.address");
            yuanwaiNet = prop.getProperty("xyfy.ip.yuanwai");
            gongwangNet = prop.getProperty("xyfy.ip.gongwang");

            neiLinkId = prop.getProperty("xyfy.ip.neilinkId");
            yuanLinkId = prop.getProperty("xyfy.ip.yuanlinkId");
            gongLinkId = prop.getProperty("xyfy.ip.gonglinkId");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getYuanwaiNet() {
        return yuanwaiNet;
    }

    public static String getGongwangNet() {
        return gongwangNet;
    }

    public static String getIpAddress() {
        return ipAddress;
    }

    public static String getNeiLinkId() {
        return neiLinkId;
    }

    public static String getYuanLinkId() {
        return yuanLinkId;
    }

    public static String getGongLinkId() {
        return gongLinkId;
    }
}
