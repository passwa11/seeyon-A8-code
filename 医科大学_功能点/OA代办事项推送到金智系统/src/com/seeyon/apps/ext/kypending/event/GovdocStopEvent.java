package com.seeyon.apps.ext.kypending.event;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;

public class GovdocStopEvent extends Event {

    private CtpAffair currentAffair;

    public CtpAffair getCurrentAffair() {
        return currentAffair;
    }

    public void setCurrentAffair(CtpAffair currentAffair) {
        this.currentAffair = currentAffair;
    }

    public GovdocStopEvent(Object source) {
        super(source);
    }
}
