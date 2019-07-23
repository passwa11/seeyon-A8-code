package com.seeyon.apps.ext.Sso0715.pojo;

/**
 * 周刘成   2019/7/17
 */
public class Department {
    private String id;
    private String name;
    private String parentid;

    public Department(String id, String name, String parentid) {
        this.id = id;
        this.name = name;
        this.parentid = parentid;
    }

    public Department() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }
}
