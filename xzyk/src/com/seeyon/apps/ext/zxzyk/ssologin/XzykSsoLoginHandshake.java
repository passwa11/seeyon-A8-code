package com.seeyon.apps.ext.zxzyk.ssologin;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

public class XzykSsoLoginHandshake extends SSOLoginHandshakeAbstract {

    @Override
    public String handshake(String ticket) {
        return ticket;
    }

    @Override
    public void logoutNotify(String s) {

    }
}
