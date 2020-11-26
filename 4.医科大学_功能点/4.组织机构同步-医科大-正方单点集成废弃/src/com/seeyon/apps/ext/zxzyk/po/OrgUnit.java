package com.seeyon.apps.ext.zxzyk.po;

/**
 * Created by Administrator on 2019-7-29.
 */
public class OrgUnit {

    private String unitcode;
    private String unitname;
    private String unitshortname;
    private Integer sortId;
    private String unitid;
    private String superior;

    public OrgUnit() {
    }

    public String getUnitcode() {
        return unitcode;
    }

    public void setUnitcode(String unitcode) {
        this.unitcode = unitcode;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public String getUnitshortname() {
        return unitshortname;
    }

    public void setUnitshortname(String unitshortname) {
        this.unitshortname = unitshortname;
    }

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public String getSuperior() {
        return superior;
    }

    public void setSuperior(String superior) {
        this.superior = superior;
    }
}
