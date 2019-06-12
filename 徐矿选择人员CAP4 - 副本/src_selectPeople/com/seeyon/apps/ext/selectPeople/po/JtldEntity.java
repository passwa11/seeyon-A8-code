package com.seeyon.apps.ext.selectPeople.po;

import java.util.Date;

/**
 * 周刘成   2019/5/7
 */

public class JtldEntity {
    private long id;
    private long state;
    private String startMemberId;
    private Date startDate;
    private String approveMemberId;
    private Date approveDate;
    private long finishedflag;
    private long ratifyflag;
    private String ratifyMemberId;
    private Date ratifyDate;
    private long sort;
    private String modifyMemberId;
    private Date modifyDate;
    private String field0001;
    private String field0002;
    private String field0003;
    private String field0004;

    public JtldEntity() {
    }

    public JtldEntity(String field0001, String field0002, String field0003, String field0004) {
        this.field0001 = field0001;
        this.field0002 = field0002;
        this.field0003 = field0003;
        this.field0004 = field0004;
    }

    public JtldEntity(long id, long state, String startMemberId, Date startDate, String approveMemberId, Date approveDate, long finishedflag, long ratifyflag, String ratifyMemberId, Date ratifyDate, long sort, String modifyMemberId, Date modifyDate, String field0001, String field0002, String field0003, String field0004) {
        this.id = id;
        this.state = state;
        this.startMemberId = startMemberId;
        this.startDate = startDate;
        this.approveMemberId = approveMemberId;
        this.approveDate = approveDate;
        this.finishedflag = finishedflag;
        this.ratifyflag = ratifyflag;
        this.ratifyMemberId = ratifyMemberId;
        this.ratifyDate = ratifyDate;
        this.sort = sort;
        this.modifyMemberId = modifyMemberId;
        this.modifyDate = modifyDate;
        this.field0001 = field0001;
        this.field0002 = field0002;
        this.field0003 = field0003;
        this.field0004 = field0004;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public String getStartMemberId() {
        return startMemberId;
    }

    public void setStartMemberId(String startMemberId) {
        this.startMemberId = startMemberId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getApproveMemberId() {
        return approveMemberId;
    }

    public void setApproveMemberId(String approveMemberId) {
        this.approveMemberId = approveMemberId;
    }

    public Date getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(Date approveDate) {
        this.approveDate = approveDate;
    }

    public long getFinishedflag() {
        return finishedflag;
    }

    public void setFinishedflag(long finishedflag) {
        this.finishedflag = finishedflag;
    }

    public long getRatifyflag() {
        return ratifyflag;
    }

    public void setRatifyflag(long ratifyflag) {
        this.ratifyflag = ratifyflag;
    }

    public String getRatifyMemberId() {
        return ratifyMemberId;
    }

    public void setRatifyMemberId(String ratifyMemberId) {
        this.ratifyMemberId = ratifyMemberId;
    }

    public Date getRatifyDate() {
        return ratifyDate;
    }

    public void setRatifyDate(Date ratifyDate) {
        this.ratifyDate = ratifyDate;
    }

    public long getSort() {
        return sort;
    }

    public void setSort(long sort) {
        this.sort = sort;
    }

    public String getModifyMemberId() {
        return modifyMemberId;
    }

    public void setModifyMemberId(String modifyMemberId) {
        this.modifyMemberId = modifyMemberId;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getField0001() {
        return field0001;
    }

    public void setField0001(String field0001) {
        this.field0001 = field0001;
    }

    public String getField0002() {
        return field0002;
    }

    public void setField0002(String field0002) {
        this.field0002 = field0002;
    }

    public String getField0003() {
        return field0003;
    }

    public void setField0003(String field0003) {
        this.field0003 = field0003;
    }

    public String getField0004() {
        return field0004;
    }

    public void setField0004(String field0004) {
        this.field0004 = field0004;
    }
}
