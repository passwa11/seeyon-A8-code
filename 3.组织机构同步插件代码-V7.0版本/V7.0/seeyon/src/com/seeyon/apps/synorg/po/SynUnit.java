package com.seeyon.apps.synorg.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 部门中间表实体对象
 * @author Yang.Yinghai
 * @date 2015-8-18下午3:44:47
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynUnit implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = -8909556904628022584L;

    /** 编号 */
    private String code;

    /** 名称 */
    private String name;

    /** 上级部门编号 */
    private String parentCode;

    /** 排序 */
    private Long sortId = 1L;

    /** 描述 */
    private String description;

    /** 预留字段1 */
    private String extAttr1;

    /** 预留字段2 */
    private String extAttr2;

    /** 预留字段3 */
    private String extAttr3;

    /** 预留字段4 */
    private String extAttr4;

    /** 预留字段5 */
    private String extAttr5;

    /** 预留字段6 */
    private String extAttr6;

    /** 预留字段7 */
    private String extAttr7;

    /** 预留字段8 */
    private String extAttr8;

    /** 预留字段9 */
    private String extAttr9;

    /** 预留字段10 */
    private String extAttr10;

    /** 创建时间 */
    private Date createDate;

    /** 同步时间 */
    private Date syncDate;

    /** 同步状态 */
    private Integer syncState;
    
    /** 存放OA单位ID */
    private String oa_Id;

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
     * 获取parentCode
     * @return parentCode
     */
    public String getParentCode() {
        return parentCode;
    }

    /**
     * 设置parentCode
     * @param parentCode parentCode
     */
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
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
     * 获取extAttr1
     * @return extAttr1
     */
    public String getExtAttr1() {
        return extAttr1;
    }

    /**
     * 设置extAttr1
     * @param extAttr1 extAttr1
     */
    public void setExtAttr1(String extAttr1) {
        this.extAttr1 = extAttr1;
    }

    /**
     * 获取extAttr2
     * @return extAttr2
     */
    public String getExtAttr2() {
        return extAttr2;
    }

    /**
     * 设置extAttr2
     * @param extAttr2 extAttr2
     */
    public void setExtAttr2(String extAttr2) {
        this.extAttr2 = extAttr2;
    }

    /**
     * 获取extAttr3
     * @return extAttr3
     */
    public String getExtAttr3() {
        return extAttr3;
    }

    /**
     * 设置extAttr3
     * @param extAttr3 extAttr3
     */
    public void setExtAttr3(String extAttr3) {
        this.extAttr3 = extAttr3;
    }

    /**
     * 获取extAttr4
     * @return extAttr4
     */
    public String getExtAttr4() {
        return extAttr4;
    }

    /**
     * 设置extAttr4
     * @param extAttr4 extAttr4
     */
    public void setExtAttr4(String extAttr4) {
        this.extAttr4 = extAttr4;
    }

    /**
     * 获取extAttr5
     * @return extAttr5
     */
    public String getExtAttr5() {
        return extAttr5;
    }

    /**
     * 设置extAttr5
     * @param extAttr5 extAttr5
     */
    public void setExtAttr5(String extAttr5) {
        this.extAttr5 = extAttr5;
    }

    /**
     * 获取extAttr6
     * @return extAttr6
     */
    public String getExtAttr6() {
        return extAttr6;
    }

    /**
     * 设置extAttr6
     * @param extAttr6 extAttr6
     */
    public void setExtAttr6(String extAttr6) {
        this.extAttr6 = extAttr6;
    }

    /**
     * 获取extAttr7
     * @return extAttr7
     */
    public String getExtAttr7() {
        return extAttr7;
    }

    /**
     * 设置extAttr7
     * @param extAttr7 extAttr7
     */
    public void setExtAttr7(String extAttr7) {
        this.extAttr7 = extAttr7;
    }

    /**
     * 获取extAttr8
     * @return extAttr8
     */
    public String getExtAttr8() {
        return extAttr8;
    }

    /**
     * 设置extAttr8
     * @param extAttr8 extAttr8
     */
    public void setExtAttr8(String extAttr8) {
        this.extAttr8 = extAttr8;
    }

    /**
     * 获取extAttr9
     * @return extAttr9
     */
    public String getExtAttr9() {
        return extAttr9;
    }

    /**
     * 设置extAttr9
     * @param extAttr9 extAttr9
     */
    public void setExtAttr9(String extAttr9) {
        this.extAttr9 = extAttr9;
    }

    /**
     * 获取extAttr10
     * @return extAttr10
     */
    public String getExtAttr10() {
        return extAttr10;
    }

    /**
     * 设置extAttr10
     * @param extAttr10 extAttr10
     */
    public void setExtAttr10(String extAttr10) {
        this.extAttr10 = extAttr10;
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
    
    /**
     * 获取OA单位ID
     * @return oaid
     */
    public String getOa_Id() {
        return oa_Id;
    }

    /**
     * 设置OA单位ID
     * @param oaid oaid
     */
    public void setOa_Id(String oa_Id) {
        this.oa_Id = oa_Id;
    }
}
