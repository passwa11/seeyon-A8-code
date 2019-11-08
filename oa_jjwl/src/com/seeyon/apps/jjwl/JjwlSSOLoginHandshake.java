package com.seeyon.apps.jjwl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;

public class JjwlSSOLoginHandshake extends SSOLoginHandshakeAbstract{

	private static final Log log = LogFactory.getLog(JjwlSSOLoginHandshake.class);
	
	@Override
	public String handshake(String ticket) {
		log.info(">>>>>>单点登录账号："+ticket);
		return ticket;
	}

	@Override
	public void logoutNotify(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
