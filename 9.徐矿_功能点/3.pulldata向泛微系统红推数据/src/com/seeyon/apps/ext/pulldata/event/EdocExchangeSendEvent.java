package com.seeyon.apps.ext.pulldata.event;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * zhou
 * 2020-12-19
 * 添加签收转换事件
 */
public class EdocExchangeSendEvent extends Event {

    private Long summaryId;

    private EdocSummary edocSummary;

    private CtpAffair ctpAffair;

    private List<CtpAffair> list = new ArrayList<>();

    public EdocSummary getEdocSummary() {
        return edocSummary;
    }

    public void setEdocSummary(EdocSummary edocSummary) {
        this.edocSummary = edocSummary;
    }

    public CtpAffair getCtpAffair() {
        return ctpAffair;
    }

    public void setCtpAffair(CtpAffair ctpAffair) {
        this.ctpAffair = ctpAffair;
    }

    public List<CtpAffair> getList() {
        return list;
    }

    public void setList(List<CtpAffair> list) {
        this.list = list;
    }

    public Long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Long summaryId) {
        this.summaryId = summaryId;
    }

    public EdocExchangeSendEvent(Object source) {
        super(source);
    }
}
