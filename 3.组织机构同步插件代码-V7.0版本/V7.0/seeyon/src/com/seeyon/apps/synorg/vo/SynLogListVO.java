package com.seeyon.apps.synorg.vo;

import java.util.Date;

/**
 * @author Yang.Yinghai
 * @date 2016年8月4日下午2:33:19
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynLogListVO {

    /** 主建ID */
    private Long id;

    /** 实体类型：Department-部门 Member-人员 Post-职务 Level-岗位 */
    private String entityType;

    /** 实体名称 */
    private String entityName;

    /** 实体编码 */
    private String entityCode;

    /** 同步状态 0-失败 1-成功 */
    private String synState;

    /** 同步操作类型： 1-新建 2-更新 3-删除 */
    private String synType;

    /** 同步日志 */
    private String synLog;

    /** 同步时间 */
    private Date synDate;

    /**
     * 获取id
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置id
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取entityType
     * @return entityType
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * 设置entityType
     * @param entityType entityType
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * 获取entityName
     * @return entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * 设置entityName
     * @param entityName entityName
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * 获取entityCode
     * @return entityCode
     */
    public String getEntityCode() {
        return entityCode;
    }

    /**
     * 设置entityCode
     * @param entityCode entityCode
     */
    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    /**
     * 获取synState
     * @return synState
     */
    public String getSynState() {
        return synState;
    }

    /**
     * 设置synState
     * @param synState synState
     */
    public void setSynState(String synState) {
        this.synState = synState;
    }

    /**
     * 获取synType
     * @return synType
     */
    public String getSynType() {
        return synType;
    }

    /**
     * 设置synType
     * @param synType synType
     */
    public void setSynType(String synType) {
        this.synType = synType;
    }

    /**
     * 获取synLog
     * @return synLog
     */
    public String getSynLog() {
        return synLog;
    }

    /**
     * 设置synLog
     * @param synLog synLog
     */
    public void setSynLog(String synLog) {
        this.synLog = synLog;
    }

    /**
     * 获取synDate
     * @return synDate
     */
    public Date getSynDate() {
        return synDate;
    }

    /**
     * 设置synDate
     * @param synDate synDate
     */
    public void setSynDate(Date synDate) {
        this.synDate = synDate;
    }
}
