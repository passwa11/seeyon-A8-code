package com.seeyon.apps.synorg.model;

import java.util.Date;

/**
 * @author Yang.Yinghai
 * @date 2015-11-24下午4:50:05
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class MemberObject {

    private String remarks;

    private Date updateDate;

    private int delFlag;

    private String id;

    private String loginName;

    private String name;

    private String email;

    private String duty;

    private String comPanyName;

    private String detailOfficeName;

    private String handPhone;

    private String officeId;

    private int userDutyFlag;

    /**
     * 获取remarks
     * @return remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * 设置remarks
     * @param remarks remarks
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 获取updateDate
     * @return updateDate
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * 设置updateDate
     * @param updateDate updateDate
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * 获取delFlag
     * @return delFlag
     */
    public int getDelFlag() {
        return delFlag;
    }

    /**
     * 设置delFlag
     * @param delFlag delFlag
     */
    public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

    /**
     * 获取id
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置id
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取loginName
     * @return loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * 设置loginName
     * @param loginName loginName
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * 获取name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置email
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取duty
     * @return duty
     */
    public String getDuty() {
        return duty;
    }

    /**
     * 设置duty
     * @param duty duty
     */
    public void setDuty(String duty) {
        this.duty = duty;
    }

    /**
     * 获取comPanyName
     * @return comPanyName
     */
    public String getComPanyName() {
        return comPanyName;
    }

    /**
     * 设置comPanyName
     * @param comPanyName comPanyName
     */
    public void setComPanyName(String comPanyName) {
        this.comPanyName = comPanyName;
    }

    /**
     * 获取detailOfficeName
     * @return detailOfficeName
     */
    public String getDetailOfficeName() {
        return detailOfficeName;
    }

    /**
     * 设置detailOfficeName
     * @param detailOfficeName detailOfficeName
     */
    public void setDetailOfficeName(String detailOfficeName) {
        this.detailOfficeName = detailOfficeName;
    }

    /**
     * 获取handPhone
     * @return handPhone
     */
    public String getHandPhone() {
        return handPhone;
    }

    /**
     * 设置handPhone
     * @param handPhone handPhone
     */
    public void setHandPhone(String handPhone) {
        this.handPhone = handPhone;
    }

    /**
     * 获取userDutyFlag
     * @return userDutyFlag
     */
    public int getUserDutyFlag() {
        return userDutyFlag;
    }

    /**
     * 设置userDutyFlag
     * @param userDutyFlag userDutyFlag
     */
    public void setUserDutyFlag(int userDutyFlag) {
        this.userDutyFlag = userDutyFlag;
    }

    /**
     * 获取officeId
     * @return officeId
     */
    public String getOfficeId() {
        return officeId;
    }

    /**
     * 设置officeId
     * @param officeId officeId
     */
    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }
}
