package com.seeyon.apps.ext.selectPeople.po;

import java.util.Date;

/**
 * 周刘成   2019/5/9
 */
public class Fmain0148 extends Formmain0148{

    private String name;
    public Fmain0148() {
    }

    public Fmain0148(long id, long state, String startMemberId, Date startDate, String approveMemberId, Date approveDate, long finishedflag, long ratifyflag, String ratifyMemberId, Date ratifyDate, long sort, String modifyMemberId, Date modifyDate, String field0001, String field0002, String field0003, String name) {
        super(id, state, startMemberId, startDate, approveMemberId, approveDate, finishedflag, ratifyflag, ratifyMemberId, ratifyDate, sort, modifyMemberId, modifyDate, field0001, field0002, field0003);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
