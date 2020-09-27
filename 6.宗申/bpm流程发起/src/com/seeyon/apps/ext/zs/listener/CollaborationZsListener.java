package com.seeyon.apps.ext.zs.listener;

import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

public class CollaborationZsListener {

    @ListenEvent(event = CollaborationFinishEvent.class, async = true)
    public void finish(CollaborationFinishEvent event) {
        CtpAffair ctpAffair = event.getAffair();
        System.out.println("finish");
        System.out.println();
    }

    @ListenEvent(event = CollaborationStopEvent.class, async = true)
    public void stop(CollaborationStopEvent event) {
        CtpAffair ctpAffair = event.getAffair();
        System.out.println("stop");

    }

}
