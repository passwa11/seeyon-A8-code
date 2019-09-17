/**
 * 
 */
package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.seeyon.v3x.common.domain.BaseModel;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 公文关键字Bean对象
 * @author Yang.Yinghai
 * @date 2011-10-9下午06:39:26
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class EdocKeyWord extends BaseModel implements Serializable, Comparable<EdocKeyWord> {

    /** 序列化ID */
    private static final long serialVersionUID = -5719033557735566086L;

    /** 关键字 */
    private String name;

    /** 排序号 */
    private long parentId;

    /** 排序号 */
    private int sortNum;

    /** 所属单位ID */
    private long accountId;

    /** 层级记录 */
    private int levelNum;

    /** 是否系统预置 */
    private boolean isSystem;

    /** 创建人员ID */
    private long createUserId;

    /** 创建时间 */
    private Timestamp createTime;

    /** 是否有子节点 */
    private boolean hasChild;

    /**
     * 对象克隆
     * @param accountId 单位ID
     * @return 重新设置了单位的对象
     */
    public EdocKeyWord clone(long accountId) {
        EdocKeyWord keyWord = new EdocKeyWord();
        keyWord.setNewId();
        keyWord.setName(this.name);
        keyWord.setParentId(this.parentId);
        keyWord.setSortNum(this.sortNum);
        keyWord.setAccountId(accountId);
        keyWord.setLevelNum(this.levelNum);
        keyWord.setIsSystem(this.isSystem);
        keyWord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        keyWord.setCreateUserId(-1);
        return keyWord;
    }

    @Override
    public int compareTo(EdocKeyWord obj) {
        int compareInt = 0;
        if(this.sortNum > obj.sortNum) {
            compareInt = 1;
        } else if(this.sortNum == obj.sortNum) {
            compareInt = 0;
        } else {
            compareInt = -1;
        }
        return compareInt;
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
    public long getParentId() {
        return parentId;
    }

    /**
     * 设置parentId
     * @param parentId parentId
     */
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取sortNum
     * @return sortNum
     */
    public int getSortNum() {
        return sortNum;
    }

    /**
     * 设置sortNum
     * @param sortNum sortNum
     */
    public void setSortNum(int sortNum) {
        this.sortNum = sortNum;
    }

    /**
     * 获取accountId
     * @return accountId
     */
    public long getAccountId() {
        return accountId;
    }

    /**
     * 设置accountId
     * @param accountId accountId
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    /**
     * 获取levelNum
     * @return levelNum
     */
    public int getLevelNum() {
        return levelNum;
    }

    /**
     * 设置levelNum
     * @param levelNum levelNum
     */
    public void setLevelNum(int levelNum) {
        this.levelNum = levelNum;
    }

    /**
     * 获取isSystem
     * @return isSystem
     */
    public boolean getIsSystem() {
        return isSystem;
    }

    /**
     * 设置isSystem
     * @param isSystem isSystem
     */
    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    /**
     * 获取createPerson
     * @return createPerson
     */
    public long getCreateUserId() {
        return createUserId;
    }

    /**
     * 设置createPerson
     * @param createPerson createPerson
     */
    public void setCreateUserId(long createUserId) {
        this.createUserId = createUserId;
    }

    /**
     * 获取createTime
     * @return createTime
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * 设置createTime
     * @param createTime createTime
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取hasChild
     * @return hasChild
     */
    public boolean isHasChild() {
        return hasChild;
    }

    /**
     * 设置hasChild
     * @param hasChild hasChild
     */
    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    /**
     * 设置isSystem
     * @param isSystem isSystem
     */
    public void setSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }
}
