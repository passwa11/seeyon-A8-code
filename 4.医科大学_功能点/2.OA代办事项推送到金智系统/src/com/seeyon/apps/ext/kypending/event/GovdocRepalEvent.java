package com.seeyon.apps.ext.kypending.event;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;

import java.util.List;

public class GovdocRepalEvent extends Event {

    private List<CtpAffair> list;

    public GovdocRepalEvent(Object source) {
        super(source);
    }

    public List<CtpAffair> getList() {
        return list;
    }

    public void setList(List<CtpAffair> list) {
        this.list = list;
    }
}
