package com.seeyon.apps.ext.oauthLogin;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class oauthLoginPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����oauthLogin���");
}

@Override
public void destroy() {
	System.out.println("����oauthLogin���");
}
}