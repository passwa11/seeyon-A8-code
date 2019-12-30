package com.seeyon.apps.ext.ssoLogin.sso;

import com.seeyon.apps.ext.ssoLogin.util.StringHandle;
import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

/**
 * 周刘成   2019-12-16
 */
public class XkdxSsoLoginHandshake extends SSOLoginHandshakeAbstract {
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
