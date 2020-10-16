package com.seeyon.apps.ext.edocDetail.po;

public class JdbcEntity {
    private String code;
    private String time;
    private String subject;
    private String edocMark;
    private String createUnit;

    private String text;
    private String text2;
    private String text3;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

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
