package com.seeyon.apps.ext.ssoLogin.sso;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

import java.util.Base64;

/**
 * 周刘成   2019-12-16
 */
public class XkdxSsoLoginHandshake extends SSOLoginHandshakeAbstract {
    @Override
    public String handshake(String ticket) {
        String encodeString = new String(Base64.getDecoder().decode(ticket.getBytes()));
        char[] charArray = encodeString.toCharArray();
        for (int i = 0; i < charArray.length; ++i) {
            --charArray[i];
        }
        String loginName = new String(charArray);
        return loginName;
    }

    @Override
    public void logoutNotify(String s) {

    }
}
