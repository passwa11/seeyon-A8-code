package com.seeyon.apps.ext.fileUploadConfig.po;

/**
 * 周刘成   2019-11-21
 */
public class ZOrgUploadMember {
    private Long userid;
    private String status;
    private String deptid;
    private String loginname;

    public ZOrgUploadMember() {
    }

    public ZOrgUploadMember(Long userid, String status, String deptid, String loginname) {
        this.userid = userid;
        this.status = status;
        this.deptid = deptid;
        this.loginname = loginname;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }
}
