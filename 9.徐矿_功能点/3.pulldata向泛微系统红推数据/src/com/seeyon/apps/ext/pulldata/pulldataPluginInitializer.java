package com.seeyon.apps.ext.pulldata;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class pulldataPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载pulldata插件");
}

@Override
public void destroy() {
	System.out.println("销毁pulldata插件");
}
}