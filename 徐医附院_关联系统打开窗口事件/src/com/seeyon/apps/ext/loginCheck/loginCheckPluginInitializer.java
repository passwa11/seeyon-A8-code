package com.seeyon.apps.ext.loginCheck;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class loginCheckPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载loginCheck插件");
}

@Override
public void destroy() {
	System.out.println("销毁loginCheck插件");
}
}