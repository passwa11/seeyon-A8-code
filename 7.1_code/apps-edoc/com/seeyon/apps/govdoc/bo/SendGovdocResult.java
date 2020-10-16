/**
 * 
 */
package com.seeyon.apps.govdoc.bo;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * @author <a href="tanmf@seeyon.com">Tanmf</a>
 * @date 2013-1-11 
 */
public class SendGovdocResult {

    /**
     * 公文对象
     */
    private EdocSummary summary;
    /**
     * 已发事项
     */
    private CtpAffair  sentAffair;
    
    public SendGovdocResult() {
        super();
    }

    public SendGovdocResult(EdocSummary summary, CtpAffair sentAffair) {
        super();
        this.summary = summary;
        this.sentAffair = sentAffair;
    }

    public EdocSummary getSummary() {
        return summary;
    }

    public void setSummary(EdocSummary summary) {
        this.summary = summary;
    }

    public CtpAffair getSentAffair() {
        return sentAffair;
    }

    public void setSentAffair(CtpAffair sentAffair) {
        this.sentAffair = sentAffair;
    }

}
