package com.seeyon.apps.ext.oauthLogin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropUtils {

    private String SSO_ClientId;
    private String SSO_ClientSecret;
    private String SSO_OAuthAuthorize;
    private String SSO_OAuthAccess_token;
    private String SSO_Services;
    private String SSO_UserInfo;
    private String applicationUrl;
    private String SSO_Logout;
    private String checkUserStatus;

    private Properties pps;

    public PropUtils() {
        pps = new Properties();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            File file = new File(path, "config/sso.properties");
            InputStream in = new FileInputStream(file);
            pps.load(new InputStreamReader(in, "UTF-8"));
            SSO_ClientId = pps.getProperty("SSO_ClientId");
            SSO_ClientSecret = pps.getProperty("SSO_ClientSecret");
            SSO_OAuthAuthorize = pps.getProperty("SSO_OAuthAuthorize");
            SSO_OAuthAccess_token = pps.getProperty("SSO_OAuthAccess_token");
            SSO_Services = pps.getProperty("SSO_Services");
            SSO_UserInfo = pps.getProperty("SSO_UserInfo");
            applicationUrl = pps.getProperty("applicationUrl");
            SSO_Logout = pps.getProperty("SSO_Logout");
            checkUserStatus = pps.getProperty("Sso_check_user_status");
        } catch (Exception e) {
            System.out.println("未找到配置文件");
        }
    }

    public String getCheckUserStatus() {
        return checkUserStatus;
    }

    public void setCheckUserStatus(String checkUserStatus) {
        this.checkUserStatus = checkUserStatus;
    }

    public String getSSO_Logout() {
        return SSO_Logout;
    }

    public void setSSO_Logout(String SSO_Logout) {
        this.SSO_Logout = SSO_Logout;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getSSO_UserInfo() {
        return SSO_UserInfo;
    }

    public void setSSO_UserInfo(String SSO_UserInfo) {
        this.SSO_UserInfo = SSO_UserInfo;
    }

    public String getSSO_ClientId() {
        return SSO_ClientId;
    }

    public void setSSO_ClientId(String SSO_ClientId) {
        this.SSO_ClientId = SSO_ClientId;
    }

    public String getSSO_ClientSecret() {
        return SSO_ClientSecret;
    }

    public void setSSO_ClientSecret(String SSO_ClientSecret) {
        this.SSO_ClientSecret = SSO_ClientSecret;
    }

    public String getSSO_OAuthAuthorize() {
        return SSO_OAuthAuthorize;
    }

    public void setSSO_OAuthAuthorize(String SSO_OAuthAuthorize) {
        this.SSO_OAuthAuthorize = SSO_OAuthAuthorize;
    }

    public String getSSO_OAuthAccess_token() {
        return SSO_OAuthAccess_token;
    }

    public void setSSO_OAuthAccess_token(String SSO_OAuthAccess_token) {
        this.SSO_OAuthAccess_token = SSO_OAuthAccess_token;
    }

    public String getSSO_Services() {
        return SSO_Services;
    }

    public void setSSO_Services(String SSO_Services) {
        this.SSO_Services = SSO_Services;
    }
}
