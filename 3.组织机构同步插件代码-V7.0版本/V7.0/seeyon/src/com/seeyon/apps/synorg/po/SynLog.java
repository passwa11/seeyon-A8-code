package com.seeyon.apps.synorg.po;

import java.io.Serializable;
import java.util.Date;

import com.seeyon.ctp.common.po.BasePO;

/**
 * 同步插件配置对象
 * @author Yang.Yinghai
 * @date 2015-8-18下午3:49:45
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynLog extends BasePO implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = -8756339939199433167L;

    /** 实体类型：Department-部门 Member-人员 Post-职务 Level-岗位 */
    private String entityType;

    /** 实体名称 */
    private String entityName;

    /** 实体编码 */
    private String entityCode;

    /** 同步状态 0-失败 1-成功 */
    private Integer synState;

    /** 同步操作类型： 1-新建 2-更新 3-删除 */
    private Integer synType;

    /** 同步日志 */
    private String synLog;

    /** 同步时间 */
    private Date synDate;

    /**
     * 日志对象构造函数
     * @param entityType 实体类型
     * @param entityName 实体名称
     * @param entityCode 实体编码
     */
    public SynLog(String entityType, String entityCode, String entityName) {
        setIdIfNew();
        this.entityCode = entityCode;
        this.entityName = entityName;
        this.entityType = entityType;
        this.synDate = new Date();
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
    public Integer getSynState() {
        return synState;
    }

    /**
     * 设置synState
     * @param synState synState
     */
    public void setSynState(Integer synState) {
        this.synState = synState;
    }

    /**
     * 获取synType
     * @return synType
     */
    public Integer getSynType() {
        return synType;
    }

    /**
     * 设置synType
     * @param synType synType
     */
    public void setSynType(Integer synType) {
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
