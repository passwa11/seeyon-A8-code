package com.seeyon.apps.ext.DTdocument.po;

/**
 * 周刘成   2019-11-5
 */
public class TempDate {
    private long id;
    private String startdate;
    private String enddate;

    public TempDate() {
    }

    public TempDate(long id, String startdate, String enddate) {
        this.id = id;
        this.startdate = startdate;
        this.enddate = enddate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
}
