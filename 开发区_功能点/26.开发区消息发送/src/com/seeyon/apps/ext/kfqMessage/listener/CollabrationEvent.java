package com.seeyon.apps.ext.kfqMessage.listener;

import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.annotation.ListenEvent;

public class CollabrationEvent {

    @ListenEvent(event = CollaborationStartEvent.class, async = true)
    public void message(CollaborationStartEvent event) throws BusinessException {
        ColConstant.SendType sendType = event.getSendtype();
        String name = sendType.name();

        System.out.println(name);
    }



}
