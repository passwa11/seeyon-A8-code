package com.seeyon.apps.ext.messageSend.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropUtils {

    private String oaUrl;

    private Properties pps;

    public PropUtils() {
        pps = new Properties();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            File file = new File(path, "sms/app.properties");
            InputStream is = new FileInputStream(file);
            pps.load(new InputStreamReader(is, "UTF-8"));

            oaUrl = pps.getProperty("oa-url");
        } catch (Exception e) {
            System.out.println("未找到配置文件");
        }
    }

    public String getOaUrl() {
        return oaUrl;
    }

    public void setOaUrl(String oaUrl) {
        this.oaUrl = oaUrl;
    }
}
