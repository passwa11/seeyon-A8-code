package com.seeyon.apps.ext.oauthLogin.sso;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

public class KfqSsoLoginHandshake extends SSOLoginHandshakeAbstract {
    @Override
    public String handshake(String ticket) {
        String decodeloginName = "";
        if (null != ticket && !"".equals(ticket)) {
            decodeloginName = StringHandle.decode(ticket);
        } else {
            return null;
        }
        return decodeloginName;
    }

    @Override
    public void logoutNotify(String s) {

    }
}
