package com.seeyon.apps.ext.copyFile.util;

public class ConfigProperties {

    private static ReadConfigTool configTool = new ReadConfigTool();

    private static final String driverClassName;
    private static final String url;
    private static final String username;
    private static final String password;
    private static final String serverIp;
    private static final String restName;
    private static final String restPassword;
    private static final String downloadFileToSavePath;

    static {
        driverClassName = configTool.getString("driverClassName");
        url = configTool.getString("url");
        username = configTool.getString("username");
        password = configTool.getString("password");
        serverIp = configTool.getString("serverIp");
        restName = configTool.getString("restName");
        restPassword = configTool.getString("restPassword");
        downloadFileToSavePath = configTool.getString("downloadFileToSavePath");

    }

    public static String getDownloadFileToSavePath() {
        return downloadFileToSavePath;
    }

    public static ReadConfigTool getConfigTool() {
        return configTool;
    }

    public static String getDriverClassName() {
        return driverClassName;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getServerIp() {
        return serverIp;
    }

    public static String getRestName() {
        return restName;
    }

    public static String getRestPassword() {
        return restPassword;
    }
}