package com.seeyon.apps.ext.gcxySso.handler;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

public class GcxySsoLoginHandshake extends SSOLoginHandshakeAbstract {
    @Override
    public String handshake(String ticket) {
        String decodeloginName = "";
        if (null != ticket && !"".equals(ticket)) {
            String t = StringHandle.decode(ticket);
            decodeloginName = t.split(":")[0];
        } else {
            return null;
        }
        return decodeloginName;
    }

    @Override
    public void logoutNotify(String s) {

    }
}
