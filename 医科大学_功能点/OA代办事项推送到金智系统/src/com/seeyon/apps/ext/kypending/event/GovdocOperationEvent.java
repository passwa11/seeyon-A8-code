package com.seeyon.apps.ext.kypending.event;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.Event;

import java.util.List;

public class GovdocOperationEvent extends Event {

    private List<CtpAffair> affairs;

    private CtpAffair currentAffair;

    private boolean isStart;

    private String type;

    private String summaryId;

    public GovdocOperationEvent(Object source) {
        super(source);
    }

    public List<CtpAffair> getAffairs() {
        return affairs;
    }

    public void setAffairs(List<CtpAffair> affairs) {
        this.affairs = affairs;
    }

    public CtpAffair getCurrentAffair() {
        return currentAffair;
    }

    public void setCurrentAffair(CtpAffair currentAffair) {
        this.currentAffair = currentAffair;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }
}
