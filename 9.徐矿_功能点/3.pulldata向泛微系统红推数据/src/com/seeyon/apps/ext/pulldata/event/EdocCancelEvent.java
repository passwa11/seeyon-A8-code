package com.seeyon.apps.ext.pulldata.event;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;

import java.util.List;


public class EdocCancelEvent extends Event {

    private CtpAffair ctpAffair;
    private List<CtpAffair> list;

    private Long summaryId;

    public EdocCancelEvent(Object source) {
        super(source);
    }

    public List<CtpAffair> getList() {
        return list;
    }

    public void setList(List<CtpAffair> list) {
        this.list = list;
    }

    public CtpAffair getCtpAffair() {
        return ctpAffair;
    }

    public void setCtpAffair(CtpAffair ctpAffair) {
        this.ctpAffair = ctpAffair;
    }

    public Long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Long summaryId) {
        this.summaryId = summaryId;
    }
}
