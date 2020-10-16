package com.seeyon.apps.ext.copyFile.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class ReadConfigTool {
    private Properties properties;

    public ReadConfigTool() {
        InputStream istream = ReadConfigTool.class.getClassLoader().getResourceAsStream("config.properties");

        this.properties = new Properties();
        try {
            this.properties.load(new InputStreamReader(istream,"UTF-8"));
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
            result = this.properties.getProperty(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}