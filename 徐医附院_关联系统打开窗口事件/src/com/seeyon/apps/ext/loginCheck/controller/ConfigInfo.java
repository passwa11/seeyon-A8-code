package com.seeyon.apps.ext.loginCheck.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019-12-4
 */
public class ConfigInfo {
    public static String ipAddress;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIpAddress() {
        return ipAddress;
    }
}
