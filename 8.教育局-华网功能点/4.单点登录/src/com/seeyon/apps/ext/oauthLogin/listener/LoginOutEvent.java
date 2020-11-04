package com.seeyon.apps.ext.oauthLogin.listener;

import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.login.event.UserLogoutEvent;
import com.seeyon.ctp.login.online.OnlineUser;
import com.seeyon.ctp.util.annotation.ListenEvent;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class LoginOutEvent {

    @ListenEvent(event = UserLogoutEvent.class, async = true)
    public void out(UserLogoutEvent event) throws BusinessException {
    }
}
