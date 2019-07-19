package com.pojo;

import java.util.List;

/**
 * 周刘成   2019/7/17
 */
public class Member {
    private String userid;

    private String alias;
    private String name;
    private String account;
    private String sex;
    private Photo photo;
    private String[] department_id;
    private String position;
    private String employee_id;
    private List<Mobile> phone;
    private List<Extend> extend;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public String[] getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(String[] department_id) {
        this.department_id = department_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public List<Mobile> getPhone() {
        return phone;
    }

    public void setPhone(List<Mobile> phone) {
        this.phone = phone;
    }

    public List<Extend> getExtend() {
        return extend;
    }

    public void setExtend(List<Extend> extend) {
        this.extend = extend;
    }
}
