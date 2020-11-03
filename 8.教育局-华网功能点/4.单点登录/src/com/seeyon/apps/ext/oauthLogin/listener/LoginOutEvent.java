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
        System.out.println("进来了吗？？？？？？？？");
        List<UserLogoutEvent.LogoutUser> list = event.getUsers();
        UserLogoutEvent.LogoutUser user = list.get(0);
        OnlineUser onlineUser = user.getUser();
        System.out.println(onlineUser.getLoginName());
        HttpServletResponse response = (HttpServletResponse) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_RESPONSE_KEY);
        PropUtils propUtils = new PropUtils();
        try {
            response.sendRedirect(propUtils.getSSOOAuthLogout() + "&clientId=" + propUtils.getSSOClientId() + "&returnUrl=" + java.net.URLEncoder.encode(propUtils.getSSOClientHomePage(), "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
