package com.seeyon.apps.ext.kypending.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019/7/24
 */
public class ReadConfigTools {
    private Properties properties;

    private static ReadConfigTools readConfigTools;

    public static ReadConfigTools getInstance() {
        if (null != readConfigTools) {
            return readConfigTools;
        } else {
            return readConfigTools = new ReadConfigTools();
        }
    }


    public ReadConfigTools() {
        InputStream inputStream = ReadConfigTools.class.getClassLoader().getResourceAsStream("config/oaToXzhmu.properties");

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
