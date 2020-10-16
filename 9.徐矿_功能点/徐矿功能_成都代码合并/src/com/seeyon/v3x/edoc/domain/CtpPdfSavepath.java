package com.seeyon.v3x.edoc.domain;

/**
 * 周刘成   2019/4/9 
 */
public class CtpPdfSavepath {

    private Long edocsummaryid;
    private String savepath;

    public CtpPdfSavepath() {
    }

    public CtpPdfSavepath(Long edocsummaryid, String savepath) {
        this.edocsummaryid = edocsummaryid;
        this.savepath = savepath;
    }


    public Long getEdocsummaryid() {
        return edocsummaryid;
    }

    public void setEdocsummaryid(Long edocsummaryid) {
        this.edocsummaryid = edocsummaryid;
    }

    public String getSavepath() {
        return savepath;
    }

    public void setSavepath(String savepath) {
        this.savepath = savepath;
    }
}
