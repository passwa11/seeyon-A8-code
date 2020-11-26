package com.seeyon.apps.ext.zxzyk.po;

/**
 * Created by Administrator on 2019-7-29.
 */
public class OrgMember {
    private String memberid;
    private String membername;
    private String loginName;
    private String orgAccountId;
    private String orgLevelId;
    private String orgPostId;
    private String orgDepartmentId;
    private String membercode;
    private Integer sortId = 1;
    private Boolean enabled = true;
    private String telNumber;
    private String description;

    public OrgMember() {
    }

    public String getMemberid() {
        return memberid;
    }

    public void setMemberid(String memberid) {
        this.memberid = memberid;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getOrgAccountId() {
        return orgAccountId;
    }

    public void setOrgAccountId(String orgAccountId) {
        this.orgAccountId = orgAccountId;
    }

    public String getOrgLevelId() {
        return orgLevelId;
    }

    public void setOrgLevelId(String orgLevelId) {
        this.orgLevelId = orgLevelId;
    }

    public String getOrgPostId() {
        return orgPostId;
    }

    public void setOrgPostId(String orgPostId) {
        this.orgPostId = orgPostId;
    }

    public String getOrgDepartmentId() {
        return orgDepartmentId;
    }

    public void setOrgDepartmentId(String orgDepartmentId) {
        this.orgDepartmentId = orgDepartmentId;
    }

    public String getMembercode() {
        return membercode;
    }

    public void setMembercode(String membercode) {
        this.membercode = membercode;
    }

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
