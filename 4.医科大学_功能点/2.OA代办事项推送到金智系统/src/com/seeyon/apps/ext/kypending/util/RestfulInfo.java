package com.seeyon.apps.ext.kypending.util;

import java.io.Serializable;

public class RestfulInfo implements Serializable {
    private static final long serialVersionUID = -6928044023506681479L;
    private String url;
    private String appId;
    private String accessToken;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
