package com.seeyon.apps.ext.meetingInfoTip.po;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 */
public class MeetingAppHistory implements Serializable {

    //	zhou:添加
    private String sqrdh;
    private Integer sfygwhldcj;
    private String hcyq;
    private String ldid;
    private String ldname;

    public String getLdid() {
        return ldid;
    }

    public void setLdid(String ldid) {
        this.ldid = ldid;
    }

    public String getLdname() {
        return ldname;
    }

    public void setLdname(String ldname) {
        this.ldname = ldname;
    }

    //	zhou:添加

    /** 数据库主键UUID */
    private Long id;

    /** 会议室id */
    private Long roomId;

    /** 会 议id */
    private Long meetingId;

    /** 会议模板id */
    private Long templateId;

    /** 周期设置id */
    private Long periodicityId;

    /** 开始使用时间 */
    private Date startDatetime;

    /** 结束使用时间 */
    private Date endDatetime;

    /**会议总耗时(单位分钟)*/
    private Integer timeDiff;

    /** 申请说明，用途 */
    private String description;

    /** 申请状态 */
    private Integer status;

    /** 使用状态 0正常 1提前结束 */
    private Integer usedStatus;

    /** 申请时间 */
    private Date appDatetime;

    /** 申请部门 */
    private Long departmentId;

    /** 申请单位*/
    private Long accountId;

    /** 申请人 */
    private Long perId;

    /** 审批人 */
    private Long auditingId;

    /**会议室管理员*/
    private Long roomAdmin;
    ////////////////////////////////////

    private boolean proxy;

    private Long proxyId;

    /** proxyName */
    private String proxyName;

    private boolean isFirstNotDisplay = false; //是否首页不显示(周期性会议只显示其中最近的一次会议)

    public String getSqrdh() {
        return sqrdh;
    }

    public void setSqrdh(String sqrdh) {
        this.sqrdh = sqrdh;
    }

    public Integer getSfygwhldcj() {
        return sfygwhldcj;
    }

    public void setSfygwhldcj(Integer sfygwhldcj) {
        this.sfygwhldcj = sfygwhldcj;
    }

    public String getHcyq() {
        return hcyq;
    }

    public void setHcyq(String hcyq) {
        this.hcyq = hcyq;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Date getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Date startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Date getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Date endDatetime) {
        this.endDatetime = endDatetime;
    }

    public Integer getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(Integer timeDiff) {
        this.timeDiff = timeDiff;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getAppDatetime() {
        return appDatetime;
    }

    public void setAppDatetime(Date appDatetime) {
        this.appDatetime = appDatetime;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getPerId() {
        return perId;
    }

    public void setPerId(Long perId) {
        this.perId = perId;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public Long getProxyId() {
        return proxyId;
    }

    public void setProxyId(Long proxyId) {
        this.proxyId = proxyId;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public boolean isFirstNotDisplay() {
        return isFirstNotDisplay;
    }

    public void setFirstNotDisplay(boolean isFirstNotDisplay) {
        this.isFirstNotDisplay = isFirstNotDisplay;
    }

    public Long getAuditingId() {
        return auditingId;
    }

    public void setAuditingId(Long auditingId) {
        this.auditingId = auditingId;
    }

    public Long getRoomAdmin() {
        return roomAdmin;
    }

    public void setRoomAdmin(Long roomAdmin) {
        this.roomAdmin = roomAdmin;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getPeriodicityId() {
        return periodicityId;
    }

    public void setPeriodicityId(Long periodicityId) {
        this.periodicityId = periodicityId;
    }

    public Integer getUsedStatus() {
        return usedStatus;
    }

    public void setUsedStatus(Integer usedStatus) {
        this.usedStatus = usedStatus;
    }

}
