package com.seeyon.apps.synorg.model;

import java.util.List;

/**
 * 太极组织机构人员JSON数据对象
 * @author Yang.Yinghai
 * @date 2015-11-24下午4:57:39
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class MemberJSONModel {

    /** 返回状态 */
    private int returnCode;

    /** 当前第几页 */
    private int pageNo;

    /** 每页条数 */
    private int pageSize;

    /** 总数 */
    private int count;

    /** 总页数 */
    private int totalPages;

    /** 人员数据 */
    private List<MemberObject> list;

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
     * 获取pageNo
     * @return pageNo
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 设置pageNo
     * @param pageNo pageNo
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 获取pageSize
     * @return pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置pageSize
     * @param pageSize pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
     * 获取totalPages
     * @return totalPages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 设置totalPages
     * @param totalPages totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * 获取list
     * @return list
     */
    public List<MemberObject> getList() {
        return list;
    }

    /**
     * 设置list
     * @param list list
     */
    public void setList(List<MemberObject> list) {
        this.list = list;
    }
}
