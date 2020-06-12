package com.seeyon.apps.ext.kydx.po;

import java.util.Date;

public class CtpOrgUser {

    private Long id;
    private String type;
    private String loginName;
    private String exLoginName;
    private String exPassword;
    private String exId;
    private String exUserId;
    private Long memberId;
    private String exUnitCode;
    private String description;
    private Date actionTime;

    public CtpOrgUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getExLoginName() {
        return exLoginName;
    }

    public void setExLoginName(String exLoginName) {
        this.exLoginName = exLoginName;
    }

    public String getExPassword() {
        return exPassword;
    }

    public void setExPassword(String exPassword) {
        this.exPassword = exPassword;
    }

    public String getExId() {
        return exId;
    }

    public void setExId(String exId) {
        this.exId = exId;
    }

    public String getExUserId() {
        return exUserId;
    }

    public void setExUserId(String exUserId) {
        this.exUserId = exUserId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getExUnitCode() {
        return exUnitCode;
    }

    public void setExUnitCode(String exUnitCode) {
        this.exUnitCode = exUnitCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }
}
