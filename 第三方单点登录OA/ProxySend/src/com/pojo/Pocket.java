package com.pojo;

import java.util.List;

/**
 * 周刘成   2019/7/17
 */
public class Pocket {

    private int result;
    private String errmsg;
    private String access_token;

    private List<Department> departments;

    private List<Member> member;

    private Member user;

    public Member getUser() {
        return user;
    }

    public void setUser(Member user) {
        this.user = user;
    }

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }

    public Pocket(int result, String errmsg, List<Department> departments) {
        this.result = result;
        this.errmsg = errmsg;
        this.departments = departments;
    }

    public Pocket(int result, String errmsg, String access_token) {
        this.result = result;
        this.errmsg = errmsg;
        this.access_token = access_token;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Pocket() {
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
