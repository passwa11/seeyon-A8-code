package com.seeyon.apps.ext.modulePortlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 周刘成   2019-12-19
 */
public class ConfigUtil {

    private static String portletName;

    static {
        InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("config/portlet.properties");
        Properties prop = new Properties();
        try {
            prop.load(new InputStreamReader(in,"UTF-8"));
            portletName = prop.getProperty("portlet.name");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPortletName() {
        return portletName;
    }

    public static void setPortletName(String portletName) {
        ConfigUtil.portletName = portletName;
    }
}
