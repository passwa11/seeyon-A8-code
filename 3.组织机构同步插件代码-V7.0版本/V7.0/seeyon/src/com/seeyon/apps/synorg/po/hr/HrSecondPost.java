package com.seeyon.apps.synorg.po.hr;

import java.io.Serializable;

/**
 * 副岗中间表实体对象
 * @author Yang.Yinghai
 * @date 2015-8-18下午3:44:47 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class HrSecondPost implements Serializable {

    /** 序列化ID */
    private static final long serialVersionUID = -7763167719405115848L;

    /** 序号 */
    private String sortId;
    
    /** 人员编号 */
    private String userCode;

    /** 部门编码 */
    private String deptCode;

    /** 级别编号 */
    private String levelCode;

    /**
     * 获取userCode
     * @return userCode
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * 设置userCode
     * @param userCode userCode
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * 获取deptCode
     * @return deptCode
     */
    public String getDeptCode() {
        return deptCode;
    }

    /**
     * 设置deptCode
     * @param deptCode deptCode
     */
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    /**
     * 获取levelCode
     * @return levelCode
     */
    public String getLevelCode() {
        return levelCode;
    }

    /**
     * 设置levelCode
     * @param levelCode levelCode
     */
    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    
    /**
     * 获取sortId
     * @return sortId
     */
    public String getSortId() {
        return sortId;
    }

    
    /**
     * 设置sortId
     * @param sortId sortId
     */
    public void setSortId(String sortId) {
        this.sortId = sortId;
    }
}
