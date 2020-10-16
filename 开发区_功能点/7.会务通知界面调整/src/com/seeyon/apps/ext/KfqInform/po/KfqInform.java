package com.seeyon.apps.ext.KfqInform.po;

import java.io.Serializable;

public class KfqInform  implements Serializable {

    private Long id;
    private int sort;
    private String memberid;
    private String membername;
    private String summaryid;
    private String createuserid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getMemberid() {
        return memberid;
    }

    public void setMemberid(String memberid) {
        this.memberid = memberid;
    }

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public String getSummaryid() {
        return summaryid;
    }

    public void setSummaryid(String summaryid) {
        this.summaryid = summaryid;
    }

    public String getCreateuserid() {
        return createuserid;
    }

    public void setCreateuserid(String createuserid) {
        this.createuserid = createuserid;
    }
}
