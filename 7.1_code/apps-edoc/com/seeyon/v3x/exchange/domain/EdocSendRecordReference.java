package com.seeyon.v3x.exchange.domain;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

public class EdocSendRecordReference extends BaseModel implements Serializable{

    private long referenceSendRecodId;
    private long newSendRecodId;
    public long getReferenceSendRecodId() {
        return referenceSendRecodId;
    }
    public void setReferenceSendRecodId(long referenceSendRecodId) {
        this.referenceSendRecodId = referenceSendRecodId;
    }
    public long getNewSendRecodId() {
        return newSendRecodId;
    }
    public void setNewSendRecodId(long newSendRecodId) {
        this.newSendRecodId = newSendRecodId;
    }
}
