package com.seeyon.apps.meetingroom.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 周刘成   2019/7/24
 */
public class MeetingReadConfigTools {
    private Properties properties;


    public static MeetingReadConfigTools readConfigTools;

    public static MeetingReadConfigTools getInstance() {
        if (null != readConfigTools) {
            readConfigTools = new MeetingReadConfigTools();
        }
        return readConfigTools;
    }


    public MeetingReadConfigTools() {
        InputStream inputStream = MeetingReadConfigTools.class.getClassLoader().getResourceAsStream("config/meeting.properties");

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
