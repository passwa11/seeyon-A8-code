package com.seeyon.apps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 周刘成   2019-12-13
 */
public class PropertiesUtil {
    private static String url;
    private static String SpCode;
    private static String LoginName;
    private static String Password;
    private static String f;

    private static String template;
    private static Properties prop;

    static {
        try {
            InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("sms/sms.properties");
            prop = new Properties();
            prop.load(new InputStreamReader(inputStream,"UTF-8"));
            url = (String) prop.get("sms.path");
            SpCode = (String) prop.get("sms.SpCode");
            LoginName = (String) prop.get("sms.LoginName");
            Password = (String) prop.get("sms.Password");
            template = (String) prop.get("sms.templet");
            f = (String) prop.get("sms.f");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getSpCode() {
        return SpCode;
    }

    public static String getLoginName() {
        return LoginName;
    }

    public static String getPassword() {
        return Password;
    }

    public static String getF() {
        return f;
    }

    public static Properties getProp() {
        return prop;
    }

    public static String getTemplate() {
        return template;
    }
}
