/**
 * 
 */
package com.seeyon.apps.collaboration.bo;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.po.affair.CtpAffair;

/**
 * @author <a href="tanmf@seeyon.com">Tanmf</a>
 * @date 2013-1-11 
 */
public class SendCollResult {

    /**
     * 协同对象
     */
    private ColSummary summary;
    /**
     * 已发事项
     */
    private CtpAffair  sentAffair;
    
    public SendCollResult() {
        super();
    }

    public SendCollResult(ColSummary summary, CtpAffair sentAffair) {
        super();
        this.summary = summary;
        this.sentAffair = sentAffair;
    }

    public ColSummary getSummary() {
        return summary;
    }

    public void setSummary(ColSummary summary) {
        this.summary = summary;
    }

    public CtpAffair getSentAffair() {
        return sentAffair;
    }

    public void setSentAffair(CtpAffair sentAffair) {
        this.sentAffair = sentAffair;
    }

}
