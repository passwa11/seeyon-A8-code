package com.seeyon.apps.ext.kydx.po;

/**
 * 部门
 */
public class OrgDept {

    private String id;
    private String deptName;
    private String deptCode;
    private String deptDescription;
    private String deptEnable;
    private String deptParentId;
    private String orgAccountId;

    public String getOrgAccountId() {
        return orgAccountId;
    }

    public void setOrgAccountId(String orgAccountId) {
        this.orgAccountId = orgAccountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptDescription() {
        return deptDescription;
    }

    public void setDeptDescription(String deptDescription) {
        this.deptDescription = deptDescription;
    }

    public String getDeptEnable() {
        return deptEnable;
    }

    public void setDeptEnable(String deptEnable) {
        this.deptEnable = deptEnable;
    }

    public String getDeptParentId() {
        return deptParentId;
    }

    public void setDeptParentId(String deptParentId) {
        this.deptParentId = deptParentId;
    }
}
