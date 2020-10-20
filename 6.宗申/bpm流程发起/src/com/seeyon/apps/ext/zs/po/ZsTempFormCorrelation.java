package com.seeyon.apps.ext.zs.po;

public class ZsTempFormCorrelation {
    private Long id;
    private String thirdId;
    private String oaSummaryId;

    public ZsTempFormCorrelation() {
    }

    public ZsTempFormCorrelation(Long id, String thirdId, String oaSummaryId) {
        this.id = id;
        this.thirdId = thirdId;
        this.oaSummaryId = oaSummaryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public String getOaSummaryId() {
        return oaSummaryId;
    }

    public void setOaSummaryId(String oaSummaryId) {
        this.oaSummaryId = oaSummaryId;
    }
}
