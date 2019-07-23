package com.seeyon.apps.samplePlugin;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

public class SSOLoginTest extends SSOLoginHandshakeAbstract {

	@Override
	public String handshake(String ticket) {
		if(ticket==null||ticket.equals("")) {
            return null;
        }
		System.out.println(ticket);
        // 返回ticket对应的协同登录名
        return ticket;

	}

	@Override
	public void logoutNotify(String ticket) {
		System.out.println("out success!");
		// TODO Auto-generated method stub

	}

}
