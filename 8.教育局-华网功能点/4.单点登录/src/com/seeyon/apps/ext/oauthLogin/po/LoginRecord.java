package com.seeyon.apps.ext.oauthLogin.po;

import java.util.Date;

public class LoginRecord {

    private Long id;
    private String loginName;
    private String loginType;
    private Date loginTime;

    public LoginRecord(Long id, String loginName, String loginType, Date loginTime) {
        this.id = id;
        this.loginName = loginName;
        this.loginType = loginType;
        this.loginTime = loginTime;
    }

    public LoginRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
