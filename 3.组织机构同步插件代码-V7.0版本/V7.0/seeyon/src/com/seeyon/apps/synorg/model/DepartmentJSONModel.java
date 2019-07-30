package com.seeyon.apps.synorg.model;

import java.util.List;

/**
 * 太极组织机构部门JSON数据对象
 * @author Yang.Yinghai
 * @date 2015-11-24下午4:57:24
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class DepartmentJSONModel {

    /** 返回状态 */
    private int returnCode;

    /** 数据条数 */
    private int count;

    /** 部门列表对象 */
    private List<DepartmentObject> Offices;

    /**
     * 获取returnCode
     * @return returnCode
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * 设置returnCode
     * @param returnCode returnCode
     */
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * 获取count
     * @return count
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置count
     * @param count count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 获取offices
     * @return offices
     */
    public List<DepartmentObject> getOffices() {
        return Offices;
    }

    /**
     * 设置offices
     * @param offices offices
     */
    public void setOffices(List<DepartmentObject> offices) {
        Offices = offices;
    }
}
