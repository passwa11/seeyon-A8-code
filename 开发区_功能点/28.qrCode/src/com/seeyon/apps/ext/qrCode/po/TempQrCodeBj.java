package com.seeyon.apps.ext.qrCode.po;

import java.util.Date;

public class TempQrCodeBj {

    private Long id;
    private String bjId;
    private Date createDate;
    private String fileUrl;
    private String filename;
    private String mimeType;
    private String createMemberId;
    private String bjP1;
    private String bjP2;
    private String bjP3;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBjId() {
        return bjId;
    }

    public void setBjId(String bjId) {
        this.bjId = bjId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCreateMemberId() {
        return createMemberId;
    }

    public void setCreateMemberId(String createMemberId) {
        this.createMemberId = createMemberId;
    }

    public String getBjP1() {
        return bjP1;
    }

    public void setBjP1(String bjP1) {
        this.bjP1 = bjP1;
    }

    public String getBjP2() {
        return bjP2;
    }

    public void setBjP2(String bjP2) {
        this.bjP2 = bjP2;
    }

    public String getBjP3() {
        return bjP3;
    }

    public void setBjP3(String bjP3) {
        this.bjP3 = bjP3;
    }
}
