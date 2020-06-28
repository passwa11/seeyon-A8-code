package com.seeyon.apps.ext.edocDetail.po;

public class JdbcEntity {
    private String code;
    private String time;
    private String subject;
    private String edocMark;
    private String createUnit;

    public String getCreateUnit() {
        return createUnit;
    }

    public void setCreateUnit(String createUnit) {
        this.createUnit = createUnit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEdocMark() {
        return edocMark;
    }

    public void setEdocMark(String edocMark) {
        this.edocMark = edocMark;
    }
}
