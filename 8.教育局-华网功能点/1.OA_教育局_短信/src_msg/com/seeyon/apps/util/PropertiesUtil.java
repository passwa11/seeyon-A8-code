package com.seeyon.apps.util;

import java.io.*;
import java.util.Properties;

/**
 * 周刘成   2019-12-13
 */
public class PropertiesUtil {
    private String url;
    private Integer port;
    private String LoginName;
    private String Password;
    private Properties prop;

    public PropertiesUtil() {
        try {
            prop = new Properties();
            String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            File file = new File(path, "sms/sms.properties");
            InputStream inputStream = new FileInputStream(file);
            prop.load(new InputStreamReader(inputStream, "UTF-8"));
            url = (String) prop.get("qxt.ipAddress");
            port = Integer.parseInt((String) prop.get("qxt.port"));
            LoginName = (String) prop.get("qxt.account");
            Password = (String) prop.get("qxt.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLoginName() {
        return LoginName;
    }

    public void setLoginName(String loginName) {
        LoginName = loginName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

}
