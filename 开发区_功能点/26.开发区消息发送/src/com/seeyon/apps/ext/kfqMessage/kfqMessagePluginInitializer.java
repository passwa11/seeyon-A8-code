package com.seeyon.apps.ext.kfqMessage;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class kfqMessagePluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载kfqMessage插件");
}

@Override
public void destroy() {
	System.out.println("销毁kfqMessage插件");
}
}