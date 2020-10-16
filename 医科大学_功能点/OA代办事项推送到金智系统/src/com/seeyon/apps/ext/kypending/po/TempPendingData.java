package com.seeyon.apps.ext.kypending.po;

import java.io.Serializable;

public class TempPendingData implements Serializable {

    private String id;
    private String preaffairid;
    private String nextaffairid;
    private String summaryid;
    private String prememberid;
    private String nextmemberid;
    private String processid;

    public TempPendingData() {
    }


    public String getPreaffairid() {
        return preaffairid;
    }

    public void setPreaffairid(String preaffairid) {
        this.preaffairid = preaffairid;
    }

    public String getNextaffairid() {
        return nextaffairid;
    }

    public void setNextaffairid(String nextaffairid) {
        this.nextaffairid = nextaffairid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getSummaryid() {
        return summaryid;
    }

    public void setSummaryid(String summaryid) {
        this.summaryid = summaryid;
    }

    public String getPrememberid() {
        return prememberid;
    }

    public void setPrememberid(String prememberid) {
        this.prememberid = prememberid;
    }

    public String getNextmemberid() {
        return nextmemberid;
    }

    public void setNextmemberid(String nextmemberid) {
        this.nextmemberid = nextmemberid;
    }

    public String getProcessid() {
        return processid;
    }

    public void setProcessid(String processid) {
        this.processid = processid;
    }
}
