package com.seeyon.apps.ext.oauthLogin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropUtils {

    private String SSOClientId;
    private String SSOClientSecret;
    private String SSOOAuthAuthorize;
    private String SSOOAuthAccess_token;
    private String SSOClientHomePage;
    private String SSOServices;
    private String SSOOAuthLogout;
    private String SSOAuthPath;
    private String SSOLogoutPath;
    private String SSOGetAuthCodePath;
    private String SSOErrorPath;
    private String SSOSessionUser;
    private String SSOSessionAccessToken;
    private String SSOSessionUserId;
    private String SSOSessionExpires_in;

    private Properties pps;

    public PropUtils() {
        pps = new Properties();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            File file = new File(path, "sms/oauth.properties");
            InputStream is = new FileInputStream(file);
            pps.load(new InputStreamReader(is, "UTF-8"));

            SSOClientId = pps.getProperty("SSO_ClientId");
            SSOClientSecret = pps.getProperty("SSO_ClientSecret");
            SSOOAuthAuthorize = pps.getProperty("SSO_OAuthAuthorize");
            SSOOAuthLogout = pps.getProperty("SSO_OAuthLogout");
            SSOOAuthAccess_token = pps.getProperty("SSO_OAuthAccess_token");
            SSOServices = pps.getProperty("SSO_Services");
            SSOClientHomePage = pps.getProperty("SSO_ClientHomePage");
            SSOAuthPath = pps.getProperty("SSO_AuthPath");
            SSOLogoutPath = pps.getProperty("SSO_LogoutPath");
            SSOGetAuthCodePath = pps.getProperty("SSO_GetAuthCodePath");
            SSOErrorPath = pps.getProperty("SSO_ErrorPath");
            SSOSessionUser = pps.getProperty("SSO_SessionUser");
            SSOSessionAccessToken = pps.getProperty("SSO_SessionAccessToken");
            SSOSessionUserId = pps.getProperty("SSO_SessionUserId");
            SSOSessionExpires_in = pps.getProperty("SSO_SessionExpires_in");

        } catch (Exception e) {
            System.out.println("未找到配置文件");
        }
    }

    public String getSSOClientId() {
        return SSOClientId;
    }

    public void setSSOClientId(String SSOClientId) {
        this.SSOClientId = SSOClientId;
    }

    public String getSSOClientSecret() {
        return SSOClientSecret;
    }

    public void setSSOClientSecret(String SSOClientSecret) {
        this.SSOClientSecret = SSOClientSecret;
    }

    public String getSSOOAuthAuthorize() {
        return SSOOAuthAuthorize;
    }

    public void setSSOOAuthAuthorize(String SSOOAuthAuthorize) {
        this.SSOOAuthAuthorize = SSOOAuthAuthorize;
    }

    public String getSSOOAuthAccess_token() {
        return SSOOAuthAccess_token;
    }

    public void setSSOOAuthAccess_token(String SSOOAuthAccess_token) {
        this.SSOOAuthAccess_token = SSOOAuthAccess_token;
    }

    public String getSSOClientHomePage() {
        return SSOClientHomePage;
    }

    public void setSSOClientHomePage(String SSOClientHomePage) {
        this.SSOClientHomePage = SSOClientHomePage;
    }

    public String getSSOServices() {
        return SSOServices;
    }

    public void setSSOServices(String SSOServices) {
        this.SSOServices = SSOServices;
    }

    public String getSSOOAuthLogout() {
        return SSOOAuthLogout;
    }

    public void setSSOOAuthLogout(String SSOOAuthLogout) {
        this.SSOOAuthLogout = SSOOAuthLogout;
    }

    public String getSSOAuthPath() {
        return SSOAuthPath;
    }

    public void setSSOAuthPath(String SSOAuthPath) {
        this.SSOAuthPath = SSOAuthPath;
    }

    public String getSSOLogoutPath() {
        return SSOLogoutPath;
    }

    public void setSSOLogoutPath(String SSOLogoutPath) {
        this.SSOLogoutPath = SSOLogoutPath;
    }

    public String getSSOGetAuthCodePath() {
        return SSOGetAuthCodePath;
    }

    public void setSSOGetAuthCodePath(String SSOGetAuthCodePath) {
        this.SSOGetAuthCodePath = SSOGetAuthCodePath;
    }

    public String getSSOErrorPath() {
        return SSOErrorPath;
    }

    public void setSSOErrorPath(String SSOErrorPath) {
        this.SSOErrorPath = SSOErrorPath;
    }

    public String getSSOSessionUser() {
        return SSOSessionUser;
    }

    public void setSSOSessionUser(String SSOSessionUser) {
        this.SSOSessionUser = SSOSessionUser;
    }

    public String getSSOSessionAccessToken() {
        return SSOSessionAccessToken;
    }

    public void setSSOSessionAccessToken(String SSOSessionAccessToken) {
        this.SSOSessionAccessToken = SSOSessionAccessToken;
    }

    public String getSSOSessionUserId() {
        return SSOSessionUserId;
    }

    public void setSSOSessionUserId(String SSOSessionUserId) {
        this.SSOSessionUserId = SSOSessionUserId;
    }

    public String getSSOSessionExpires_in() {
        return SSOSessionExpires_in;
    }

    public void setSSOSessionExpires_in(String SSOSessionExpires_in) {
        this.SSOSessionExpires_in = SSOSessionExpires_in;
    }
}
