package com.seeyon.apps.ext.xk263Email.po;

/**
 * 周刘成   2019-11-25
 */
public class OrgMember263EmailMapper {
    private Long userId;
    private String loginName;
    private String mail263Name;
    private String status;
    private String updateTime;
    private String createTime;
    private String deptId;
    private String deptName;

    public OrgMember263EmailMapper() {
    }

    public OrgMember263EmailMapper(Long userId, String loginName, String mail263Name, String status, String updateTime, String createTime, String deptId, String deptName) {
        this.userId = userId;
        this.loginName = loginName;
        this.mail263Name = mail263Name;
        this.status = status;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.deptId = deptId;
        this.deptName = deptName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getMail263Name() {
        return mail263Name;
    }

    public void setMail263Name(String mail263Name) {
        this.mail263Name = mail263Name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
