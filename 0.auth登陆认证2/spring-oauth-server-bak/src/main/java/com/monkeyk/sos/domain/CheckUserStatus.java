package com.monkeyk.sos.domain;

import java.io.Serializable;

public class CheckUserStatus implements Serializable {

    private int id;
    private String token;
    private String loginname;

    public CheckUserStatus() {
    }

    public CheckUserStatus(int id, String token, String loginname) {
        super();
        this.id = id;
        this.token = token;
        this.loginname = loginname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }
}
