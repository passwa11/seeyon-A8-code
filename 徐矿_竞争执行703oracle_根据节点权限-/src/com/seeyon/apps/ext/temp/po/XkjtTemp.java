package com.seeyon.apps.ext.temp.po;

import java.io.Serializable;

public class XkjtTemp implements Serializable {

    private String id;
    private String summaryId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }

    public XkjtTemp() {
    }

    public XkjtTemp(String id, String summaryId) {
        this.id = id;
        this.summaryId = summaryId;
    }
}
