package com.seeyon.apps.ext.oauthLogin.listener;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.login.event.UserLogoutEvent;
import com.seeyon.ctp.util.annotation.ListenEvent;


public class LoginOutEvent {

    @ListenEvent(event = UserLogoutEvent.class, async = true)
    public void out(UserLogoutEvent event) throws BusinessException {
    }
}
