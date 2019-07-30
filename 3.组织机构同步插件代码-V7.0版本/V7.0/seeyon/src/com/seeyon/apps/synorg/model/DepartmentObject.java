package com.seeyon.apps.synorg.model;

import java.util.Date;

/**
 * @author Yang.Yinghai
 * @date 2015-11-24下午4:48:57
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class DepartmentObject {

    private String id;

    private String name;

    private String parentId;

    private String parentIds;

    private String changeLog;

    private int grade;

    private String detailOfficeName;

    private String officeNameChangeFlag;

    private Date updateDate;

    private int delFlag;

    private String remarks;

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
     * 获取parentId
     * @return parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置parentId
     * @param parentId parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取parentIds
     * @return parentIds
     */
    public String getParentIds() {
        return parentIds;
    }

    /**
     * 设置parentIds
     * @param parentIds parentIds
     */
    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    /**
     * 获取changeLog
     * @return changeLog
     */
    public String getChangeLog() {
        return changeLog;
    }

    /**
     * 设置changeLog
     * @param changeLog changeLog
     */
    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    /**
     * 获取grade
     * @return grade
     */
    public int getGrade() {
        return grade;
    }

    /**
     * 设置grade
     * @param grade grade
     */
    public void setGrade(int grade) {
        this.grade = grade;
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
     * 获取officeNameChangeFlag
     * @return officeNameChangeFlag
     */
    public String getOfficeNameChangeFlag() {
        return officeNameChangeFlag;
    }

    /**
     * 设置officeNameChangeFlag
     * @param officeNameChangeFlag officeNameChangeFlag
     */
    public void setOfficeNameChangeFlag(String officeNameChangeFlag) {
        this.officeNameChangeFlag = officeNameChangeFlag;
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
}
