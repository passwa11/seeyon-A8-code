package com.seeyon.apps.ext.sureLogin;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class sureLoginPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载sureLogin插件");
}

@Override
public void destroy() {
	System.out.println("销毁sureLogin插件");
}
}