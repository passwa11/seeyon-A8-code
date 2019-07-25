package com.seeyon.apps.ext.Portal190724.po;

import java.util.List;

/**
 * 周刘成   2019/7/24
 */
public class ResultInfo {

    private String expires_in;
    private String token;
    private int total;
    private int totalPage;
    private int pageSize;
    private int currPage;
    private List<Contract> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public List<Contract> getData() {
        return data;
    }

    public void setData(List<Contract> data) {
        this.data = data;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
