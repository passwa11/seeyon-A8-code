package com.seeyon.apps.ext.fileUploadConfig.po;

import java.math.BigDecimal;

/**
 * 周刘成   2019-11-20
 */
public class ZOrgUnit {

    private BigDecimal id;
    private String name;
    private String path;
    private BigDecimal orgAccountId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public BigDecimal getOrgAccountId() {
        return orgAccountId;
    }

    public void setOrgAccountId(BigDecimal orgAccountId) {
        this.orgAccountId = orgAccountId;
    }
}
