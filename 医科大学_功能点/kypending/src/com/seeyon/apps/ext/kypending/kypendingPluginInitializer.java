package com.seeyon.apps.ext.kypending;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class kypendingPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载kypending插件");
}

@Override
public void destroy() {
	System.out.println("销毁kypending插件");
}
}