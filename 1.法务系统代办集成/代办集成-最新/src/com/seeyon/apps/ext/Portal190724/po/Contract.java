package com.seeyon.apps.ext.Portal190724.po;

/**
 * 合同
 */
public class Contract {

    private Long id;
    private Long oauserId;
    private String taskName;//标题
    private String busiType;//业务类型
    private String createOrg;//经办单位
    private String createUser;//经办人
    private String beginTime;//任务开始时间
    private String taskUrl;//待办URL
    private String handleUser;//当前审核人
    private String appTaskId;//待办唯一标识
    private String busiTypeId;

    public Contract() {
    }

    public Contract(Long id, Long oauserId, String taskName, String busiType, String createOrg, String createUser, String beginTime, String taskUrl, String handleUser, String appTaskId, String busiTypeId) {
        this.id = id;
        this.oauserId = oauserId;
        this.taskName = taskName;
        this.busiType = busiType;
        this.createOrg = createOrg;
        this.createUser = createUser;
        this.beginTime = beginTime;
        this.taskUrl = taskUrl;
        this.handleUser = handleUser;
        this.appTaskId = appTaskId;
        this.busiTypeId = busiTypeId;
    }

    public String getBusiTypeId() {
        return busiTypeId;
    }

    public void setBusiTypeId(String busiTypeId) {
        this.busiTypeId = busiTypeId;
    }

    public Long getOauserId() {
        return oauserId;
    }

    public void setOauserId(Long oauserId) {
        this.oauserId = oauserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getBusiType() {
        return busiType;
    }

    public void setBusiType(String busiType) {
        this.busiType = busiType;
    }

    public String getCreateOrg() {
        return createOrg;
    }

    public void setCreateOrg(String createOrg) {
        this.createOrg = createOrg;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    public String getHandleUser() {
        return handleUser;
    }

    public void setHandleUser(String handleUser) {
        this.handleUser = handleUser;
    }

    public String getAppTaskId() {
        return appTaskId;
    }

    public void setAppTaskId(String appTaskId) {
        this.appTaskId = appTaskId;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "taskName='" + taskName + '\'' +
                ", busiType='" + busiType + '\'' +
                ", createOrg='" + createOrg + '\'' +
                ", createUser='" + createUser + '\'' +
                ", beginTime='" + beginTime + '\'' +
                ", taskUrl='" + taskUrl + '\'' +
                ", handleUser='" + handleUser + '\'' +
                ", appTaskId='" + appTaskId + '\'' +
                '}';
    }
}
