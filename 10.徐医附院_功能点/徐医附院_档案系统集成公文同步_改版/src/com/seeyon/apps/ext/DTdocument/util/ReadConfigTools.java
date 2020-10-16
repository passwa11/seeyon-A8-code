package com.seeyon.apps.ext.DTdocument.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019/7/24
 */
public class ReadConfigTools {
    private static ReadConfigTools configTools;

    public static ReadConfigTools getInstance() {
        return configTools = new ReadConfigTools();
    }

    private Properties properties;

    public ReadConfigTools() {
        InputStream inputStream = ReadConfigTools.class.getClassLoader().getResourceAsStream("law/docPlugin.properties");

        this.properties = new Properties();
        try {
            this.properties.load(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getString(String key) {
        if ((key == null) || (key.equals("")) || (key.equals("null"))) {
            return "";
        }
        String result = "";
        try {
            result = properties.getProperty(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
