package com.seeyon.apps.synorg.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 职务级别中间表实体对象
 * @author Yang.Yinghai
 * @date 2015-8-18下午3:49:45
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynLevel implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = 7564810248705762808L;

    /** 编码 */
    private String code;

    /** 名称 */
    private String name;

    /** 排序 */
    private Long sortId = 1L;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private Date createDate;

    /** 同步时间 */
    private Date syncDate;

    /** 同步状态 */
    private Integer syncState;

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
     * 获取code
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置code
     * @param code code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取sortId
     * @return sortId
     */
    public Long getSortId() {
        return sortId;
    }

    /**
     * 设置sortId
     * @param sortId sortId
     */
    public void setSortId(Long sortId) {
        this.sortId = sortId;
    }

    /**
     * 获取description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置description
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取createDate
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * 设置createDate
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取syncDate
     * @return syncDate
     */
    public Date getSyncDate() {
        return syncDate;
    }

    /**
     * 设置syncDate
     * @param syncDate syncDate
     */
    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    /**
     * 获取syncState
     * @return syncState
     */
    public Integer getSyncState() {
        return syncState;
    }

    /**
     * 设置syncState
     * @param syncState syncState
     */
    public void setSyncState(Integer syncState) {
        this.syncState = syncState;
    }
}
